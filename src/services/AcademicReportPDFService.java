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

/**
 * AcademicReportPDFService
 * Generates 1 student's academic report as a PDF.
 */
public class AcademicReportPDFService {

    private static final String GRADES_PATH = "data/grades.txt";
    private static final String REPORTS_FOLDER = "reports";

    private final StudentRepository studentRepo;
    private final CourseRepository courseRepo;
    private final GradeFileHandler gradeFile;

    public AcademicReportPDFService() {
        studentRepo = new StudentRepository();
        courseRepo = new CourseRepository();
        gradeFile = new GradeFileHandler(GRADES_PATH);
    }

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

    public ReportResult generateAcademicReportPdf(String studentId, String semesterLabel) throws Exception {
        studentId = safe(studentId);
        String sem = safe(semesterLabel);

        if (studentId.isEmpty()) {
            throw new IllegalArgumentException("Student ID is required.");
        }

        Student student = studentRepo.findById(studentId);
        if (student == null) {
            throw new IllegalArgumentException("Student not found: " + studentId);
        }

        // latest grades per course (only generate if at least 1 grade exists)
        List<Grade> latest = gradeFile.latestByCourse(student.getStudentId());
        if (latest == null || latest.isEmpty()) {
            throw new IllegalStateException("No grades found for this student. Report cannot be generated.");
        }

        // build report rows + CGPA totals
        List<Row> rows = new ArrayList<Row>();
        double totalPoints = 0.0;
        int totalCredits = 0;

        for (Grade g : latest) {
            if (g == null) continue;

            Course c = courseRepo.findById(g.getCourseId());
            if (c == null) continue;

            // semester filter
            if (!sem.isEmpty()) {
                String courseSem = safe(c.getSemester());
                if (!courseSem.equalsIgnoreCase(sem)) continue;
            }

            int credits = c.getCredits();
            if (credits <= 0) continue;

            double gp = g.getGradePoint();

            rows.add(new Row(
                    c.getCourseId(),
                    c.getCourseName(),
                    credits,
                    g.getLetter(),
                    gp
            ));

            totalCredits += credits;
            totalPoints += (gp * credits);
        }

        if (rows.isEmpty()) {
            throw new IllegalStateException("No graded courses found for: " + (sem.isEmpty() ? "(All)" : sem));
        }

        // sort by course code
        Collections.sort(rows, new Comparator<Row>() {
            @Override
            public int compare(Row a, Row b) {
                return safe(a.courseCode).compareToIgnoreCase(safe(b.courseCode));
            }
        });

        double cgpa = (totalCredits == 0) ? 0.0 : (totalPoints / totalCredits);
        DecimalFormat df = new DecimalFormat("0.00");

        File outFile = buildOutputFile(student.getStudentId(), sem);

        // create PDF
        writePdf(outFile, student, sem, rows, cgpa, df);

        return new ReportResult(outFile.getAbsolutePath(), cgpa, totalCredits);
    }

    // ---------------- PDF helpers ----------------

    private void writePdf(File outFile, Student student, String sem, List<Row> rows,
                          double cgpa, DecimalFormat df) throws Exception {

        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(doc, new FileOutputStream(outFile));
        doc.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

        doc.add(new Paragraph("Academic Performance Report", titleFont));
        doc.add(Chunk.NEWLINE);

        doc.add(new Paragraph("Student Name: " + safe(student.getFullName()), normalFont));
        doc.add(new Paragraph("Student ID: " + safe(student.getStudentId()), normalFont));
        doc.add(new Paragraph("Program: " + safe(student.getMajor()), normalFont));
        doc.add(new Paragraph("Year of Study: " + student.getYear(), normalFont));
        doc.add(Chunk.NEWLINE);

        doc.add(new Paragraph("Semester: " + (sem.isEmpty() ? "(All)" : sem), boldFont));
        doc.add(Chunk.NEWLINE);

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
        doc.add(Chunk.NEWLINE);

        doc.add(new Paragraph("Recommendations:", boldFont));
        doc.add(new Paragraph("- Review weak topics and ask lecturer if needed.", normalFont));
        doc.add(new Paragraph("- Follow recovery milestones if you have weak courses.", normalFont));

        doc.close();
    }

    private File buildOutputFile(String studentId, String sem) {
        File outDir = new File(REPORTS_FOLDER);
        if (!outDir.exists()) outDir.mkdirs();

        String semPart = sem.isEmpty() ? "AllSemesters" : sem.replaceAll("\\s+", "");
        String filename = studentId + "_" + semPart + "_AcademicReport.pdf";
        return new File(outDir, filename);
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

    private static String safe(String s) {
        return (s == null) ? "" : s.trim();
    }

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
