package services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import model.Course;
import model.Grade;
import model.Student;
import repository.CourseRepository;
import repository.GradeFileHandler;
import repository.StudentRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * AcademicReportPDFService
 * - Generates ONE selected student's Academic Performance Report as PDF
 * - Filters by semester (based on Course.semester)
 * - Calculates CGPA using latest attempt per course from grades.txt
 * - Only generates if the student has at least one grade
 */
public class AcademicReportPDFService {

    private static final String DEFAULT_GRADES_PATH = "data/grades.txt";
    private static final String REPORTS_FOLDER = "reports";

    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;
    private final GradeFileHandler gradeFile;

    public AcademicReportPDFService() {
        this.studentRepo = new StudentRepository();
        this.courseRepo = new CourseRepository();
        this.gradeFile = new GradeFileHandler(DEFAULT_GRADES_PATH);
    }

    /** Result object so UI can show path and optionally email CGPA */
    public static class ReportResult {
        public final String pdfPath;
        public final double cgpa;
        public final int totalCredits;

        public ReportResult(String pdfPath, double cgpa, int totalCredits) {
            this.pdfPath = pdfPath;
            this.cgpa = cgpa;
            this.totalCredits = totalCredits;
        }
    }

    /**
     * Generate Academic Performance Report PDF for ONE student.
     *
     * @param studentId Student ID (e.g. 2025A1234)
     * @param semesterLabel Semester filter (e.g. "Semester 1"). If null/empty, includes all.
     * @return ReportResult containing pdfPath + cgpa
     */
    public ReportResult generateAcademicReportPdf(String studentId, String semesterLabel) throws Exception {

        if (studentId == null || studentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID is required.");
        }

        String sem = (semesterLabel == null) ? "" : semesterLabel.trim();

        // 1) Load student
        Student student = studentRepo.findById(studentId.trim());
        if (student == null) {
            throw new IllegalArgumentException("Student not found: " + studentId);
        }

        // 2) Latest grades per course (only generate if has at least one)
        Map<String, Grade> latest = gradeFile.latestByCourse(student.getStudentId());
        if (latest == null || latest.isEmpty()) {
            throw new IllegalStateException("No grades found for this student. Report cannot be generated.");
        }

        // 3) Build rows + compute CGPA
        List<Row> rows = new ArrayList<Row>();  // IMPORTANT: typed list for NetBeans
        double totalGradePoints = 0.0;
        int totalCredits = 0;

        for (Grade g : latest.values()) {
            if (g == null) continue;

            Course c = courseRepo.findById(g.getCourseId());
            if (c == null) continue;

            // Semester filter (Course.getSemester())
            if (!sem.isEmpty()) {
                String courseSem = (c.getSemester() == null) ? "" : c.getSemester().trim();
                if (!courseSem.equalsIgnoreCase(sem)) continue;
            }

            int credits = c.getCredits();
            double gradePoint = g.getGradePoint();

            rows.add(new Row(
                    c.getCourseId(),
                    c.getCourseName(),
                    credits,
                    g.getLetter(),
                    gradePoint
            ));

            totalGradePoints += (gradePoint * credits);
            totalCredits += credits;
        }

        if (rows.isEmpty()) {
            throw new IllegalStateException("No graded courses found for the selected semester: "
                    + (sem.isEmpty() ? "(All)" : sem));
        }

        // 4) Sort rows (NetBeans-friendly: no lambda)
        Collections.sort(rows, new Comparator<Row>() {
            @Override
            public int compare(Row a, Row b) {
                String x = (a.courseCode == null) ? "" : a.courseCode;
                String y = (b.courseCode == null) ? "" : b.courseCode;
                return x.compareToIgnoreCase(y);
            }
        });

        // 5) CGPA
        double cgpa = (totalCredits == 0) ? 0.0 : (totalGradePoints / totalCredits);
        DecimalFormat df = new DecimalFormat("0.00");

        // 6) Output PDF path
        File outDir = new File(REPORTS_FOLDER);
        if (!outDir.exists()) outDir.mkdirs();

        String semPart = sem.isEmpty() ? "AllSemesters" : sem.replaceAll("\\s+", "");
        File outFile = new File(outDir, student.getStudentId() + "_" + semPart + "_AcademicReport.pdf");

        // 7) Build PDF (iText 5)
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(doc, new FileOutputStream(outFile));
        doc.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        Font boldFont  = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

        doc.add(new Paragraph("Academic Performance Report", titleFont));
        doc.add(Chunk.NEWLINE);

        // Header info
        doc.add(new Paragraph("Student Name: " + safe(student.getFullName()), normalFont));
        doc.add(new Paragraph("Student ID: " + safe(student.getStudentId()), normalFont));
        doc.add(new Paragraph("Program: " + safe(student.getMajor()), normalFont));
        doc.add(new Paragraph("Year of Study: " + student.getYear(), normalFont));
        doc.add(Chunk.NEWLINE);

        // Semester heading
        doc.add(new Paragraph(sem.isEmpty() ? "Semester: (All)" : "Semester: " + sem, boldFont));
        doc.add(Chunk.NEWLINE);

        // Table
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5);
        table.setWidths(new float[]{2.0f, 4.5f, 1.5f, 1.5f, 2.0f});

        addHeaderCell(table, "Course Code");
        addHeaderCell(table, "Course Title");
        addHeaderCell(table, "Credit Hours");
        addHeaderCell(table, "Grade");
        addHeaderCell(table, "Grade Point (4.0)");

        for (Row r : rows) {
            addBodyCell(table, safe(r.courseCode));
            addBodyCell(table, safe(r.courseTitle));
            addBodyCell(table, String.valueOf(r.creditHours));
            addBodyCell(table, safe(r.gradeLetter));
            addBodyCell(table, df.format(r.gradePoint));
        }

        doc.add(table);
        doc.add(Chunk.NEWLINE);

        doc.add(new Paragraph("Cumulative GPA (CGPA): " + df.format(cgpa), boldFont));

        // Optional recommendations
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Recommendations:", boldFont));
        doc.add(new Paragraph("- Review weak topics and seek lecturer consultation.", normalFont));
        doc.add(new Paragraph("- Follow recovery milestones if any course is at risk of failing.", normalFont));

        doc.close();

        return new ReportResult(outFile.getAbsolutePath(), cgpa, totalCredits);
    }

    // ---------- Helpers ----------

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }

    private void addHeaderCell(PdfPTable table, String text) {
        Font f = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(text, f));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text) {
        Font f = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, f));
        cell.setPadding(6);
        table.addCell(cell);
    }

    // Simple internal row class
    private static class Row {
        final String courseCode;
        final String courseTitle;
        final int creditHours;
        final String gradeLetter;
        final double gradePoint;

        Row(String courseCode, String courseTitle, int creditHours, String gradeLetter, double gradePoint) {
            this.courseCode = courseCode;
            this.courseTitle = courseTitle;
            this.creditHours = creditHours;
            this.gradeLetter = gradeLetter;
            this.gradePoint = gradePoint;
        }
    }
}
