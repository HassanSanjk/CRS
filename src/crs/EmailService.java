package crs;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Date;

/**
 * EmailService class handles all email notification functionality
 * Uses Java Mail API to send emails for various CRS system events
 */
public class EmailService {
    
    // Email configuration
    private String smtpHost;
    private String smtpPort;
    private String senderEmail;
    private String senderPassword;
    private Properties properties;
    
    /**
     * Constructor - initializes email service with default configuration
     */
    public EmailService() {
        // Default configuration for Gmail SMTP
        this.smtpHost = "smtp.gmail.com";
        this.smtpPort = "587";
        this.senderEmail = "crs.system@gmail.com";  // Replace with actual email
        this.senderPassword = "your_app_password";   // Replace with actual password
        
        setupProperties();
    }
    
    /**
     * Constructor with custom configuration
     */
    public EmailService(String smtpHost, String smtpPort, String senderEmail, String senderPassword) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.senderEmail = senderEmail;
        this.senderPassword = senderPassword;
        
        setupProperties();
    }
    
    /**
     * Sets up email properties for SMTP connection
     */
    private void setupProperties() {
        properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        properties.put("mail.smtp.ssl.trust", smtpHost);
    }
    
    /**
     * Sends a basic email
     * @param recipientEmail The recipient's email address
     * @param subject The email subject
     * @param body The email body content
     * @return true if email sent successfully
     */
    public boolean sendEmail(String recipientEmail, String subject, String body) {
        try {
            // Create session with authentication
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });
            
            // Create email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, 
                                InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(body);
            message.setSentDate(new Date());
            
            // Send email
            Transport.send(message);
            System.out.println("Email sent successfully to: " + recipientEmail);
            return true;
            
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Sends account creation notification
     */
    public boolean sendAccountCreationEmail(String recipientEmail, String username, String tempPassword) {
        String subject = "CRS - Account Created Successfully";
        String body = "Dear " + username + ",\n\n" +
                     "Your account has been created successfully in the Course Recovery System.\n\n" +
                     "Username: " + username + "\n" +
                     "Temporary Password: " + tempPassword + "\n\n" +
                     "Please login and change your password immediately.\n\n" +
                     "Best regards,\n" +
                     "CRS Administration Team";
        
        return sendEmail(recipientEmail, subject, body);
    }
    
    /**
     * Sends password reset notification
     */
    public boolean sendPasswordResetEmail(String recipientEmail, String username, String newPassword) {
        String subject = "CRS - Password Reset Request";
        String body = "Dear " + username + ",\n\n" +
                     "Your password has been reset as requested.\n\n" +
                     "New Temporary Password: " + newPassword + "\n\n" +
                     "Please login with this password and change it immediately.\n\n" +
                     "If you did not request this reset, please contact the administrator.\n\n" +
                     "Best regards,\n" +
                     "CRS Administration Team";
        
        return sendEmail(recipientEmail, subject, body);
    }
    
    /**
     * Sends course recovery plan notification
     */
    public boolean sendRecoveryPlanEmail(String recipientEmail, String studentName, 
                                        String courseName, String actionPlan) {
        String subject = "CRS - Course Recovery Plan Assigned";
        String body = "Dear " + studentName + ",\n\n" +
                     "A course recovery plan has been created for you.\n\n" +
                     "Course: " + courseName + "\n\n" +
                     "Action Plan:\n" + actionPlan + "\n\n" +
                     "Please follow the recovery plan carefully and meet all deadlines.\n\n" +
                     "Best regards,\n" +
                     "CRS Administration Team";
        
        return sendEmail(recipientEmail, subject, body);
    }
    
    /**
     * Sends milestone reminder notification
     */
    public boolean sendMilestoneReminderEmail(String recipientEmail, String studentName,
                                              String courseName, String milestone, String deadline) {
        String subject = "CRS - Recovery Milestone Reminder";
        String body = "Dear " + studentName + ",\n\n" +
                     "This is a reminder for your upcoming milestone.\n\n" +
                     "Course: " + courseName + "\n" +
                     "Milestone: " + milestone + "\n" +
                     "Deadline: " + deadline + "\n\n" +
                     "Please ensure you complete this milestone on time.\n\n" +
                     "Best regards,\n" +
                     "CRS Administration Team";
        
        return sendEmail(recipientEmail, subject, body);
    }
    
    /**
     * Sends academic performance report notification
     */
    public boolean sendPerformanceReportEmail(String recipientEmail, String studentName,
                                             String semester, double cgpa) {
        String subject = "CRS - Academic Performance Report";
        String body = "Dear " + studentName + ",\n\n" +
                     "Your academic performance report for " + semester + " is now available.\n\n" +
                     "Current CGPA: " + String.format("%.2f", cgpa) + "\n\n" +
                     "Please login to the CRS system to view your detailed report.\n\n" +
                     "Best regards,\n" +
                     "CRS Administration Team";
        
        return sendEmail(recipientEmail, subject, body);
    }
    
    /**
     * Sends eligibility status notification
     */
    public boolean sendEligibilityNotification(String recipientEmail, String studentName,
                                               boolean isEligible, String reason) {
        String subject = "CRS - Progression Eligibility Status";
        String body = "Dear " + studentName + ",\n\n";
        
        if (isEligible) {
            body += "Congratulations! You are eligible to progress to the next level of study.\n\n" +
                   "You may proceed with course registration for the next semester.\n\n";
        } else {
            body += "Based on your current academic performance, you are not eligible to progress.\n\n" +
                   "Reason: " + reason + "\n\n" +
                   "Please contact your academic advisor for guidance.\n\n";
        }
        
        body += "Best regards,\n" +
               "CRS Administration Team";
        
        return sendEmail(recipientEmail, subject, body);
    }
    
    /**
     * Updates email configuration
     */
    public void updateConfiguration(String smtpHost, String smtpPort, 
                                   String senderEmail, String senderPassword) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.senderEmail = senderEmail;
        this.senderPassword = senderPassword;
        setupProperties();
    }
    
    /**
     * Tests email connection
     */
    public boolean testConnection() {
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
            return false;
        }
    }
}