package services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/**
 * EmailService - Gmail SMTP implementation
 * Uses Gmail App Password (NOT normal password)
 */
public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    // ===============================
    // 🔴 PUT YOUR DETAILS HERE (LOCAL)
    // ===============================
    private static final String SENDER_EMAIL = "crsjavaproject@gmail.com";
    private static final String APP_PASSWORD = "nebr psow nsbx kptq";
    // ===============================

    private final Properties props;

    public EmailService() {
        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
    }

    // ---------- CORE SEND METHOD ----------

    public boolean sendEmail(String to, String subject, String body) {
        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
                }
            });

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(SENDER_EMAIL));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject);
            msg.setText(body);
            msg.setSentDate(new Date());

            Transport.send(msg);
            System.out.println("✅ Email sent to " + to);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Email failed: " + e.getMessage());
            return false;
        }
    }

    // ---------- REQUIRED FEATURES ----------

    // User account management
    public boolean sendAccountCreationEmail(String email, String username, String tempPassword) {
        return sendEmail(
                email,
                "CRS - Account Created",
                "Hello " + username + ",\n\n" +
                "Your CRS account has been created.\n\n" +
                "Username: " + username + "\n" +
                "Temporary Password: " + tempPassword + "\n\n" +
                "Please change it after login.\n\nCRS System"
        );
    }

    // Password & recovery management
    public boolean sendPasswordResetEmail(String email, String username, String newPassword) {
        return sendEmail(
                email,
                "CRS - Password Reset",
                "Hello " + username + ",\n\n" +
                "Your password has been reset.\n\n" +
                "New Password: " + newPassword + "\n\nCRS System"
        );
    }

    // Course recovery plan
    public boolean sendRecoveryPlanEmail(String email, String studentName,
                                         String courseId, String plan) {
        return sendEmail(
                email,
                "CRS - Recovery Plan (" + courseId + ")",
                "Hello " + studentName + ",\n\n" +
                "A recovery plan has been assigned.\n\n" +
                "Course: " + courseId + "\n\n" +
                plan + "\n\nCRS System"
        );
    }

    // Academic performance report
    public boolean sendPerformanceReportEmail(String email, String studentName,
                                              String semester, double cgpa) {
        return sendEmail(
                email,
                "CRS - Academic Report (" + semester + ")",
                "Hello " + studentName + ",\n\n" +
                "Semester: " + semester + "\n" +
                "CGPA: " + String.format("%.2f", cgpa) + "\n\nCRS System"
        );
    }

    // ---------- TEST ----------

    public boolean testConnection() {
        return sendEmail(
                SENDER_EMAIL,
                "CRS Test Email",
                "If you received this email, the EmailService is working."
        );
    }
}
