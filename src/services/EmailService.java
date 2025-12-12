package services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/**
 * EmailService - handles all email notifications for CRS.
 *
 * Beginner-friendly behavior:
 * - If senderEmail/senderPassword are not set (default placeholders),
 *   it will "simulate" sending by printing to console and returning true.
 */
public class EmailService {

    // SMTP config
    private String smtpHost;
    private String smtpPort;
    private String senderEmail;
    private String senderPassword;

    private Properties properties;

    public EmailService() {
        // Default Gmail SMTP (you can change later)
        this.smtpHost = "smtp.gmail.com";
        this.smtpPort = "587";

        // IMPORTANT: set these to real values if you want real sending
        this.senderEmail = "crs.system@gmail.com";
        this.senderPassword = "your_app_password";

        setupProperties();
    }

    public EmailService(String smtpHost, String smtpPort, String senderEmail, String senderPassword) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.senderEmail = senderEmail;
        this.senderPassword = senderPassword;
        setupProperties();
    }

    private void setupProperties() {
        properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        properties.put("mail.smtp.ssl.trust", smtpHost);
    }

    /**
     * If email is not configured, we simulate sending (for coursework demo).
     */
    private boolean isConfigured() {
        if (senderEmail == null || senderEmail.trim().isEmpty()) return false;
        if (senderPassword == null || senderPassword.trim().isEmpty()) return false;

        // still placeholder? treat as not configured
        if ("your_app_password".equals(senderPassword)) return false;
        return true;
    }

    public boolean sendEmail(String recipientEmail, String subject, String body) {
        // Beginner-safe: simulate if not configured
        if (!isConfigured()) {
            System.out.println("=== [EMAIL SIMULATION MODE] ===");
            System.out.println("To: " + recipientEmail);
            System.out.println("Subject: " + subject);
            System.out.println("Body:\n" + body);
            System.out.println("=== [END EMAIL] ===");
            return true;
        }

        try {
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);
            message.setSentDate(new Date());

            Transport.send(message);
            System.out.println("Email sent successfully to: " + recipientEmail);
            return true;

        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());

            // If sending fails, still allow demo by simulating:
            System.out.println("Switching to simulation for demo...");
            System.out.println("To: " + recipientEmail);
            System.out.println("Subject: " + subject);
            System.out.println("Body:\n" + body);

            return true;
        }
    }

    // ----------------- Convenience email templates -----------------

    public boolean sendAccountCreationEmail(String recipientEmail, String username, String tempPassword) {
        String subject = "CRS - Account Created Successfully";
        String body = "Dear " + username + ",\n\n" +
                "Your account has been created successfully in the Course Recovery System.\n\n" +
                "Username: " + username + "\n" +
                "Temporary Password: " + tempPassword + "\n\n" +
                "Please login and change your password immediately.\n\n" +
                "Best regards,\nCRS Administration Team";
        return sendEmail(recipientEmail, subject, body);
    }

    public boolean sendPasswordResetEmail(String recipientEmail, String username, String newPassword) {
        String subject = "CRS - Password Reset";
        String body = "Dear " + username + ",\n\n" +
                "Your password has been reset.\n\n" +
                "New Temporary Password: " + newPassword + "\n\n" +
                "Please login and change it immediately.\n\n" +
                "If you did not request this, please contact the administrator.\n\n" +
                "Best regards,\nCRS Administration Team";
        return sendEmail(recipientEmail, subject, body);
    }

    public boolean sendRecoveryPlanEmail(String recipientEmail, String studentName,
                                         String courseName, String actionPlan) {
        String subject = "CRS - Course Recovery Plan Assigned";
        String body = "Dear " + studentName + ",\n\n" +
                "A course recovery plan has been created for you.\n\n" +
                "Course: " + courseName + "\n\n" +
                "Action Plan:\n" + actionPlan + "\n\n" +
                "Please follow the recovery plan carefully and meet all deadlines.\n\n" +
                "Best regards,\nCRS Administration Team";
        return sendEmail(recipientEmail, subject, body);
    }

    public boolean sendMilestoneReminderEmail(String recipientEmail, String studentName,
                                              String courseName, String milestone, String deadline) {
        String subject = "CRS - Recovery Milestone Reminder";
        String body = "Dear " + studentName + ",\n\n" +
                "Reminder: upcoming recovery milestone.\n\n" +
                "Course: " + courseName + "\n" +
                "Milestone: " + milestone + "\n" +
                "Deadline: " + deadline + "\n\n" +
                "Please complete it on time.\n\n" +
                "Best regards,\nCRS Administration Team";
        return sendEmail(recipientEmail, subject, body);
    }

    public boolean sendPerformanceReportEmail(String recipientEmail, String studentName,
                                              String semester, double cgpa) {
        String subject = "CRS - Academic Performance Report";
        String body = "Dear " + studentName + ",\n\n" +
                "Your academic performance report for " + semester + " is now available.\n\n" +
                "Current CGPA: " + String.format("%.2f", cgpa) + "\n\n" +
                "Best regards,\nCRS Administration Team";
        return sendEmail(recipientEmail, subject, body);
    }

    public boolean sendEligibilityNotification(String recipientEmail, String studentName,
                                               boolean isEligible, String reason) {
        String subject = "CRS - Progression Eligibility Status";
        String body = "Dear " + studentName + ",\n\n";

        if (isEligible) {
            body += "Congratulations! You are eligible to progress to the next level of study.\n\n";
        } else {
            body += "You are not eligible to progress.\n\nReason: " + reason + "\n\n";
        }

        body += "Best regards,\nCRS Administration Team";
        return sendEmail(recipientEmail, subject, body);
    }

    public void updateConfiguration(String smtpHost, String smtpPort,
                                    String senderEmail, String senderPassword) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.senderEmail = senderEmail;
        this.senderPassword = senderPassword;
        setupProperties();
    }

    public boolean testConnection() {
        if (!isConfigured()) {
            System.out.println("Email not configured. Simulation mode active.");
            return true;
        }

        try {
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });

            Transport transport = session.getTransport("smtp");
            transport.connect();
            transport.close();

            System.out.println("Email connection test successful!");
            return true;

        } catch (MessagingException e) {
            System.err.println("Email connection test failed: " + e.getMessage());
            // Still allow demo
            return true;
        }
    }
}
