package services;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;


import java.io.File;
import java.util.Date;
import java.util.Properties;

/**
 * EmailService
 * Handles sending emails for CRS using Gmail SMTP.
 * Used for account creation, password reset, recovery plan and reports.
 */
public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    // Gmail account used for sending system emails
    private static final String SENDER_EMAIL = "crsjavaproject@gmail.com";
    private static final String APP_PASSWORD = "nebr psow nsbx kptq";

    private final Properties props;

    public EmailService() {
        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
    }

    // ---------------- Session Helper ----------------

    // Create email session with authentication
    private Session createSession() {
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });
    }

    // ---------------- Basic Email ----------------

    public boolean sendEmail(String to, String subject, String body) {
        to = safe(to);
        subject = safe(subject);
        body = (body == null) ? "" : body;

        if (to.isEmpty() || subject.isEmpty()) return false;

        try {
            Session session = createSession();

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SENDER_EMAIL));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject);
            msg.setText(body);
            msg.setSentDate(new Date());

            Transport.send(msg);
            return true;

        } catch (Exception e) {
            System.out.println("Email error: " + e.getMessage());
            return false;
        }
    }

    // ---------------- Email with Attachment ----------------

    public boolean sendEmailWithAttachment(String to, String subject, String body, File attachment) {
        to = safe(to);
        subject = safe(subject);
        body = (body == null) ? "" : body;

        if (to.isEmpty() || subject.isEmpty()) return false;
        if (attachment == null || !attachment.exists()) return false;

        try {
            Session session = createSession();

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SENDER_EMAIL));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject);
            msg.setSentDate(new Date());

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);

            MimeBodyPart filePart = new MimeBodyPart();
            FileDataSource source = new FileDataSource(attachment);
            filePart.setDataHandler(new DataHandler(source));
            filePart.setFileName(attachment.getName());

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(filePart);

            msg.setContent(multipart);

            Transport.send(msg);
            return true;

        } catch (Exception e) {
            System.out.println("Email attachment error: " + e.getMessage());
            return false;
        }
    }

    // ---------------- Project Required Emails ----------------

    // Account creation email
    public boolean sendAccountCreationEmail(String email, String username, String tempPassword) {
        String body =
                "Hello " + username + ",\n\n" +
                "Your CRS account has been created.\n\n" +
                "Username: " + username + "\n" +
                "Temporary Password: " + tempPassword + "\n\n" +
                "Please change it after login.\n\n" +
                "CRS System";

        return sendEmail(email, "CRS - Account Created", body);
    }

    // Password reset email
    public boolean sendPasswordResetEmail(String email, String username, String newPassword) {
        String body =
                "Hello " + username + ",\n\n" +
                "Your password has been reset.\n\n" +
                "New Password: " + newPassword + "\n\n" +
                "CRS System";

        return sendEmail(email, "CRS - Password Reset", body);
    }

    // Course recovery plan email
    public boolean sendRecoveryPlanEmail(String email, String studentName,
                                         String courseId, String plan) {
        String body =
                "Hello " + studentName + ",\n\n" +
                "A recovery plan has been assigned.\n\n" +
                "Course: " + courseId + "\n\n" +
                plan + "\n\n" +
                "CRS System";

        return sendEmail(email, "CRS - Recovery Plan (" + courseId + ")", body);
    }

    // Academic performance email (no attachment)
    public boolean sendPerformanceReportEmail(String email, String studentName,
                                              String semester, double cgpa) {
        String body =
                "Hello " + studentName + ",\n\n" +
                "Semester: " + semester + "\n" +
                "CGPA: " + String.format("%.2f", cgpa) + "\n\n" +
                "CRS System";

        return sendEmail(email, "CRS - Academic Report (" + semester + ")", body);
    }

    // Academic performance email with PDF
    public boolean sendPerformanceReportWithAttachment(String to, String studentName,
                                                       String semester, double cgpa, File pdfFile) {
        String body =
                "Hello " + studentName + ",\n\n" +
                "Please find attached your academic performance report.\n\n" +
                "Semester: " + semester + "\n" +
                "CGPA: " + String.format("%.2f", cgpa) + "\n\n" +
                "CRS System";

        String subject = "CRS - Academic Performance Report (" + semester + ")";
        return sendEmailWithAttachment(to, subject, body, pdfFile);
    }

    // Test email function
    public boolean testConnection() {
        return sendEmail(
                SENDER_EMAIL,
                "CRS Test Email",
                "If you received this email, the EmailService is working."
        );
    }

    // ---------------- Utility ----------------

    private String safe(String s) {
        return (s == null) ? "" : s.trim();
    }
}
