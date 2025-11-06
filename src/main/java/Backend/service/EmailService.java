package Backend.service;

import Backend.entity.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:info.iymart@gmail.com}")
    private String fromEmail;

    @Value("${spring.mail.from-name:AutoCare}")
    private String fromName;

    @Async
    public void sendAppointmentConfirmation(Appointment appointment) {
        try {
            String recipientEmail = getRecipientEmail(appointment);
            String recipientName = getRecipientName(appointment);
            
            if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
                log.warn("Cannot send appointment confirmation - no email address found for appointment {}", appointment.getId());
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(recipientEmail);
            helper.setSubject("Appointment Confirmation - Pending Approval");

            String emailContent = buildAppointmentConfirmationEmail(appointment, recipientName);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Appointment confirmation email sent successfully to: {}", recipientEmail);

        } catch (MessagingException e) {
            log.error("Failed to send appointment confirmation email for appointment {}", appointment.getId(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending appointment confirmation email", e);
        }
    }

    @Async
    public void sendAppointmentApproval(Appointment appointment) {
        try {
            String recipientEmail = getRecipientEmail(appointment);
            String recipientName = getRecipientName(appointment);
            
            if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
                log.warn("Cannot send appointment approval - no email address found for appointment {}", appointment.getId());
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(recipientEmail);
            helper.setSubject("Appointment Approved - " + fromName);

            String emailContent = buildAppointmentApprovalEmail(appointment, recipientName);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Appointment approval email sent successfully to: {}", recipientEmail);

        } catch (MessagingException e) {
            log.error("Failed to send appointment approval email for appointment {}", appointment.getId(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending appointment approval email", e);
        }
    }

    private String getRecipientEmail(Appointment appointment) {
        if (appointment.getCustomer() != null) {
            String email = appointment.getCustomer().getEmail();
            log.info("Recipient email found from User table: {}", email);
            return email;
        }
        log.warn("No customer associated with appointment {}", appointment.getId());
        return null;
    }

    private String getRecipientName(Appointment appointment) {
        if (appointment.getCustomer() != null) {
            String name = appointment.getCustomer().getFullName();
            return name != null ? name : "Valued Customer";
        }
        return "Valued Customer";
    }

    private String buildAppointmentConfirmationEmail(Appointment appointment, String customerName) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        String instructionsRow = appointment.getInstructions() != null && !appointment.getInstructions().trim().isEmpty() 
            ? "<div class=\"detail-row\"><span class=\"detail-label\">📝 Instructions:</span><span class=\"detail-value\">" + appointment.getInstructions() + "</span></div>"
            : "";

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: 600;
                    }
                    .status-badge {
                        display: inline-block;
                        background: #fbbf24;
                        color: #78350f;
                        padding: 8px 20px;
                        border-radius: 20px;
                        font-weight: 600;
                        margin-top: 10px;
                        font-size: 14px;
                    }
                    .content {
                        padding: 30px;
                    }
                    .greeting {
                        font-size: 18px;
                        color: #1f2937;
                        margin-bottom: 15px;
                    }
                    .message {
                        color: #6b7280;
                        margin-bottom: 25px;
                        line-height: 1.8;
                    }
                    .details-box {
                        background: #f9fafb;
                        border-left: 4px solid #667eea;
                        padding: 20px;
                        margin: 25px 0;
                        border-radius: 5px;
                    }
                    .detail-row {
                        display: flex;
                        padding: 10px 0;
                        border-bottom: 1px solid #e5e7eb;
                    }
                    .detail-row:last-child {
                        border-bottom: none;
                    }
                    .detail-label {
                        font-weight: 600;
                        color: #374151;
                        min-width: 140px;
                    }
                    .detail-value {
                        color: #6b7280;
                        flex: 1;
                    }
                    .info-box {
                        background: #eff6ff;
                        border: 1px solid #bfdbfe;
                        border-radius: 8px;
                        padding: 15px;
                        margin: 20px 0;
                    }
                    .info-box p {
                        margin: 0;
                        color: #1e40af;
                        font-size: 14px;
                    }
                    .footer {
                        background: #f9fafb;
                        padding: 20px 30px;
                        text-align: center;
                        color: #6b7280;
                        font-size: 14px;
                        border-top: 1px solid #e5e7eb;
                    }
                    .footer strong {
                        color: #374151;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚗 Appointment Received</h1>
                        <span class="status-badge">⏳ PENDING APPROVAL</span>
                    </div>
                    
                    <div class="content">
                        <p class="greeting">Dear %s,</p>
                        
                        <p class="message">
                            Thank you for booking an appointment with <strong>%s</strong>! 
                            We have received your request and it is currently pending approval from our staff.
                        </p>
                        
                        <p class="message">
                            You will receive another email once your appointment has been reviewed and approved by our team.
                        </p>
                        
                        <div class="details-box">
                            <h3 style="margin-top: 0; color: #1f2937;">📋 Appointment Details</h3>
                            
                            <div class="detail-row">
                                <span class="detail-label">📅 Date:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🕐 Time:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🚙 Vehicle Type:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🔢 Vehicle Number:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🔧 Service:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            %s
                        </div>
                        
                        <div class="info-box">
                            <p>
                                <strong>ℹ️ What's Next?</strong><br>
                                Our team will review your appointment request and assign a technician. 
                                You'll receive a confirmation email once approved. This usually takes a few hours during business hours.
                            </p>
                        </div>
                        
                        <p class="message">
                            If you have any questions or need to make changes to your appointment, 
                            please don't hesitate to contact us.
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p>
                            <strong>%s</strong><br>
                            This is an automated message, please do not reply to this email.<br>
                            <a href="mailto:%s" style="color: #667eea; text-decoration: none;">%s</a>
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
                customerName != null ? customerName : "Valued Customer",
                fromName,
                appointment.getDate().format(dateFormatter),
                appointment.getTime().format(timeFormatter),
                appointment.getVehicleType(),
                appointment.getVehicleNumber(),
                appointment.getService(),
                instructionsRow,
                fromName,
                fromEmail,
                fromEmail
            );
    }

    private String buildAppointmentApprovalEmail(Appointment appointment, String customerName) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        String employeeName = appointment.getEmployee() != null ? appointment.getEmployee().getFullName() : "our team";

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #10b981 0%%, #059669 100%%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: 600;
                    }
                    .status-badge {
                        display: inline-block;
                        background: #d1fae5;
                        color: #065f46;
                        padding: 8px 20px;
                        border-radius: 20px;
                        font-weight: 600;
                        margin-top: 10px;
                        font-size: 14px;
                    }
                    .content {
                        padding: 30px;
                    }
                    .greeting {
                        font-size: 18px;
                        color: #1f2937;
                        margin-bottom: 15px;
                    }
                    .message {
                        color: #6b7280;
                        margin-bottom: 25px;
                        line-height: 1.8;
                    }
                    .details-box {
                        background: #f9fafb;
                        border-left: 4px solid #10b981;
                        padding: 20px;
                        margin: 25px 0;
                        border-radius: 5px;
                    }
                    .detail-row {
                        display: flex;
                        padding: 10px 0;
                        border-bottom: 1px solid #e5e7eb;
                    }
                    .detail-row:last-child {
                        border-bottom: none;
                    }
                    .detail-label {
                        font-weight: 600;
                        color: #374151;
                        min-width: 140px;
                    }
                    .detail-value {
                        color: #6b7280;
                        flex: 1;
                    }
                    .success-box {
                        background: #d1fae5;
                        border: 1px solid #6ee7b7;
                        border-radius: 8px;
                        padding: 15px;
                        margin: 20px 0;
                    }
                    .success-box p {
                        margin: 0;
                        color: #065f46;
                        font-size: 14px;
                    }
                    .footer {
                        background: #f9fafb;
                        padding: 20px 30px;
                        text-align: center;
                        color: #6b7280;
                        font-size: 14px;
                        border-top: 1px solid #e5e7eb;
                    }
                    .footer strong {
                        color: #374151;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Appointment Approved!</h1>
                        <span class="status-badge">CONFIRMED</span>
                    </div>
                    
                    <div class="content">
                        <p class="greeting">Dear %s,</p>
                        
                        <p class="message">
                            Great news! Your appointment with <strong>%s</strong> has been approved and confirmed.
                        </p>
                        
                        <p class="message">
                            We have assigned <strong>%s</strong> to handle your vehicle service. 
                            Please arrive on time and bring any necessary documentation.
                        </p>
                        
                        <div class="details-box">
                            <h3 style="margin-top: 0; color: #1f2937;">📋 Confirmed Appointment Details</h3>
                            
                            <div class="detail-row">
                                <span class="detail-label">📅 Date:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🕐 Time:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">👨‍🔧 Technician:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🚙 Vehicle Type:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🔢 Vehicle Number:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🔧 Service:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="success-box">
                            <p>
                                <strong>✨ Reminder:</strong><br>
                                Please arrive 10 minutes early to allow time for check-in. 
                                If you need to reschedule or cancel, please let us know at least 24 hours in advance.
                            </p>
                        </div>
                        
                        <p class="message">
                            We look forward to serving you!
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p>
                            <strong>%s</strong><br>
                            This is an automated message, please do not reply to this email.<br>
                            <a href="mailto:%s" style="color: #10b981; text-decoration: none;">%s</a>
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
                customerName != null ? customerName : "Valued Customer",
                fromName,
                employeeName,
                appointment.getDate().format(dateFormatter),
                appointment.getTime().format(timeFormatter),
                employeeName,
                appointment.getVehicleType(),
                appointment.getVehicleNumber(),
                appointment.getService(),
                fromName,
                fromEmail,
                fromEmail
            );
    }

    @Async
    public void sendStatusChangeEmail(Appointment appointment, String statusMessage) {
        try {
            String recipientEmail = getRecipientEmail(appointment);
            String recipientName = getRecipientName(appointment);
            
            if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
                log.warn("Cannot send status change email - no email address found for appointment {}", appointment.getId());
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(recipientEmail);
            helper.setSubject("Appointment Status Update - " + appointment.getStatus());

            String emailContent = buildStatusChangeEmail(appointment, recipientName, statusMessage);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Status change email sent successfully to: {} for status: {}", recipientEmail, appointment.getStatus());

        } catch (MessagingException e) {
            log.error("Failed to send status change email for appointment {}", appointment.getId(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending status change email", e);
        }
    }

    private String buildStatusChangeEmail(Appointment appointment, String customerName, String statusMessage) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        String statusColor = getStatusColor(appointment.getStatus());
        String statusIcon = getStatusIcon(appointment.getStatus());
        String employeeName = appointment.getEmployee() != null ? appointment.getEmployee().getFullName() : "Not assigned yet";

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: %s;
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: 600;
                    }
                    .status-badge {
                        display: inline-block;
                        background: rgba(255,255,255,0.2);
                        color: white;
                        padding: 8px 20px;
                        border-radius: 20px;
                        font-weight: 600;
                        margin-top: 10px;
                        font-size: 14px;
                    }
                    .content {
                        padding: 30px;
                    }
                    .greeting {
                        font-size: 18px;
                        color: #1f2937;
                        margin-bottom: 15px;
                    }
                    .message {
                        color: #6b7280;
                        margin-bottom: 25px;
                        line-height: 1.8;
                    }
                    .details-box {
                        background: #f9fafb;
                        border-left: 4px solid %s;
                        padding: 20px;
                        margin: 25px 0;
                        border-radius: 5px;
                    }
                    .detail-row {
                        display: flex;
                        padding: 10px 0;
                        border-bottom: 1px solid #e5e7eb;
                    }
                    .detail-row:last-child {
                        border-bottom: none;
                    }
                    .detail-label {
                        font-weight: 600;
                        color: #374151;
                        min-width: 140px;
                    }
                    .detail-value {
                        color: #6b7280;
                        flex: 1;
                    }
                    .footer {
                        background: #f9fafb;
                        padding: 20px 30px;
                        text-align: center;
                        color: #6b7280;
                        font-size: 14px;
                        border-top: 1px solid #e5e7eb;
                    }
                    .footer strong {
                        color: #374151;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s Appointment Status Update</h1>
                        <span class="status-badge">%s</span>
                    </div>
                    
                    <div class="content">
                        <p class="greeting">Dear %s,</p>
                        
                        <p class="message">
                            %s
                        </p>
                        
                        <div class="details-box">
                            <h3 style="margin-top: 0; color: #1f2937;">📋 Appointment Details</h3>
                            
                            <div class="detail-row">
                                <span class="detail-label">📅 Date:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🕐 Time:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">👨‍🔧 Technician:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🚙 Vehicle Type:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🔢 Vehicle Number:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🔧 Service:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">📊 Current Status:</span>
                                <span class="detail-value"><strong>%s</strong></span>
                            </div>
                        </div>
                        
                        <p class="message">
                            If you have any questions, please don't hesitate to contact us.
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p>
                            <strong>%s</strong><br>
                            This is an automated message, please do not reply to this email.<br>
                            <a href="mailto:%s" style="color: #667eea; text-decoration: none;">%s</a>
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
                statusColor,
                statusColor,
                statusIcon,
                appointment.getStatus(),
                customerName != null ? customerName : "Valued Customer",
                statusMessage,
                appointment.getDate().format(dateFormatter),
                appointment.getTime().format(timeFormatter),
                employeeName,
                appointment.getVehicleType(),
                appointment.getVehicleNumber(),
                appointment.getService(),
                appointment.getStatus(),
                fromName,
                fromEmail,
                fromEmail
            );
    }

    private String getStatusColor(Appointment.AppointmentStatus status) {
        return switch (status) {
            case PENDING -> "linear-gradient(135deg, #fbbf24 0%%, #f59e0b 100%%)";
            case APPROVE -> "linear-gradient(135deg, #10b981 0%%, #059669 100%%)";
           
            case IN_PROGRESS -> "linear-gradient(135deg, #06b6d4 0%%, #0891b2 100%%)";
           
            case REJECT -> "linear-gradient(135deg, #ef4444 0%%, #dc2626 100%%)";
        };
    }

    private String getStatusIcon(Appointment.AppointmentStatus status) {
        return switch (status) {
            case PENDING -> "⏳";
            case APPROVE -> "✅";
           
            case IN_PROGRESS -> "🔧";
          
            case REJECT -> "❌";
        };
    }

    @Async
    public void sendTaskAllocationToCustomer(Appointment appointment) {
        try {
            String recipientEmail = getRecipientEmail(appointment);
            String recipientName = getRecipientName(appointment);
            
            if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
                log.warn("Cannot send task allocation email to customer - no email address found for appointment {}", appointment.getId());
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(recipientEmail);
            helper.setSubject("Technician Assigned - Work in Progress");

            String emailContent = buildTaskAllocationCustomerEmail(appointment, recipientName);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Task allocation email sent successfully to customer: {}", recipientEmail);

        } catch (MessagingException e) {
            log.error("Failed to send task allocation email to customer for appointment {}", appointment.getId(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending task allocation email to customer", e);
        }
    }

    @Async
    public void sendTaskAllocationToEmployee(Appointment appointment) {
        try {
            if (appointment.getEmployee() == null) {
                log.warn("Cannot send task allocation email to employee - no employee assigned to appointment {}", appointment.getId());
                return;
            }

            String employeeEmail = appointment.getEmployee().getEmail();
            String employeeName = appointment.getEmployee().getFullName();
            
            if (employeeEmail == null || employeeEmail.trim().isEmpty()) {
                log.warn("Cannot send task allocation email - employee has no email address for appointment {}", appointment.getId());
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(employeeEmail);
            helper.setSubject("New Task Assigned - Appointment #" + appointment.getId());

            String emailContent = buildTaskAllocationEmployeeEmail(appointment, employeeName);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Task allocation email sent successfully to employee: {}", employeeEmail);

        } catch (MessagingException e) {
            log.error("Failed to send task allocation email to employee for appointment {}", appointment.getId(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending task allocation email to employee", e);
        }
    }

    private String buildTaskAllocationCustomerEmail(Appointment appointment, String customerName) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        String employeeName = appointment.getEmployee() != null ? appointment.getEmployee().getFullName() : "our team";

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #06b6d4 0%%, #0891b2 100%%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: 600;
                    }
                    .status-badge {
                        display: inline-block;
                        background: rgba(255,255,255,0.2);
                        color: white;
                        padding: 8px 20px;
                        border-radius: 20px;
                        font-weight: 600;
                        margin-top: 10px;
                        font-size: 14px;
                    }
                    .content {
                        padding: 30px;
                    }
                    .greeting {
                        font-size: 18px;
                        color: #1f2937;
                        margin-bottom: 15px;
                    }
                    .message {
                        color: #6b7280;
                        margin-bottom: 25px;
                        line-height: 1.8;
                    }
                    .highlight-box {
                        background: #ecfeff;
                        border: 2px solid #06b6d4;
                        border-radius: 8px;
                        padding: 20px;
                        margin: 25px 0;
                        text-align: center;
                    }
                    .highlight-box h2 {
                        margin: 0 0 10px 0;
                        color: #0891b2;
                        font-size: 22px;
                    }
                    .technician-name {
                        font-size: 24px;
                        font-weight: 700;
                        color: #0e7490;
                    }
                    .details-box {
                        background: #f9fafb;
                        border-left: 4px solid #06b6d4;
                        padding: 20px;
                        margin: 25px 0;
                        border-radius: 5px;
                    }
                    .detail-row {
                        display: flex;
                        padding: 10px 0;
                        border-bottom: 1px solid #e5e7eb;
                    }
                    .detail-row:last-child {
                        border-bottom: none;
                    }
                    .detail-label {
                        font-weight: 600;
                        color: #374151;
                        min-width: 140px;
                    }
                    .detail-value {
                        color: #6b7280;
                        flex: 1;
                    }
                    .footer {
                        background: #f9fafb;
                        padding: 20px 30px;
                        text-align: center;
                        color: #6b7280;
                        font-size: 14px;
                        border-top: 1px solid #e5e7eb;
                    }
                    .footer strong {
                        color: #374151;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔧 Technician Assigned!</h1>
                        <span class="status-badge">IN PROGRESS</span>
                    </div>
                    
                    <div class="content">
                        <p class="greeting">Dear %s,</p>
                        
                        <p class="message">
                            Great news! Your appointment has been assigned to one of our expert technicians 
                            and work is now <strong>in progress</strong>.
                        </p>
                        
                        <div class="highlight-box">
                            <h2>👨‍🔧 Your Assigned Technician</h2>
                            <div class="technician-name">%s</div>
                        </div>
                        
                        <p class="message">
                            Our technician will take excellent care of your vehicle and ensure the highest 
                            quality of service. You can expect updates as the work progresses.
                        </p>
                        
                        <div class="details-box">
                            <h3 style="margin-top: 0; color: #1f2937;">📋 Appointment Details</h3>
                            
                            <div class="detail-row">
                                <span class="detail-label">📅 Date:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🕐 Time:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🚙 Vehicle Type:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🔢 Vehicle Number:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🔧 Service:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        
                        <p class="message">
                            Thank you for choosing <strong>%s</strong>. We're committed to providing you 
                            with excellent service!
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p>
                            <strong>%s</strong><br>
                            This is an automated message, please do not reply to this email.<br>
                            <a href="mailto:%s" style="color: #06b6d4; text-decoration: none;">%s</a>
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
                customerName != null ? customerName : "Valued Customer",
                employeeName,
                appointment.getDate().format(dateFormatter),
                appointment.getTime().format(timeFormatter),
                appointment.getVehicleType(),
                appointment.getVehicleNumber(),
                appointment.getService(),
                fromName,
                fromName,
                fromEmail,
                fromEmail
            );
    }

    private String buildTaskAllocationEmployeeEmail(Appointment appointment, String employeeName) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        String customerName = appointment.getCustomer() != null ? appointment.getCustomer().getFullName() : "Unknown Customer";
        String customerPhone = appointment.getCustomer() != null && appointment.getCustomer().getPhone() != null 
            ? appointment.getCustomer().getPhone() 
            : "Not provided";

        String instructionsRow = appointment.getInstructions() != null && !appointment.getInstructions().trim().isEmpty() 
            ? String.format("""
                <div class="detail-row">
                    <span class="detail-label">📝 Instructions:</span>
                    <span class="detail-value">%s</span>
                </div>
                """, appointment.getInstructions())
            : "";

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        margin: 0;
                        padding: 0;
                        background-color: #f4f4f4;
                    }
                    .container {
                        max-width: 600px;
                        margin: 20px auto;
                        background: #ffffff;
                        border-radius: 10px;
                        overflow: hidden;
                        box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                    }
                    .header {
                        background: linear-gradient(135deg, #f59e0b 0%%, #d97706 100%%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        margin: 0;
                        font-size: 28px;
                        font-weight: 600;
                    }
                    .task-badge {
                        display: inline-block;
                        background: rgba(255,255,255,0.2);
                        color: white;
                        padding: 8px 20px;
                        border-radius: 20px;
                        font-weight: 600;
                        margin-top: 10px;
                        font-size: 14px;
                    }
                    .content {
                        padding: 30px;
                    }
                    .greeting {
                        font-size: 18px;
                        color: #1f2937;
                        margin-bottom: 15px;
                    }
                    .message {
                        color: #6b7280;
                        margin-bottom: 25px;
                        line-height: 1.8;
                    }
                    .alert-box {
                        background: #fef3c7;
                        border: 2px solid #f59e0b;
                        border-radius: 8px;
                        padding: 15px;
                        margin: 20px 0;
                    }
                    .alert-box p {
                        margin: 0;
                        color: #92400e;
                        font-weight: 600;
                    }
                    .details-box {
                        background: #f9fafb;
                        border-left: 4px solid #f59e0b;
                        padding: 20px;
                        margin: 25px 0;
                        border-radius: 5px;
                    }
                    .detail-row {
                        display: flex;
                        padding: 10px 0;
                        border-bottom: 1px solid #e5e7eb;
                    }
                    .detail-row:last-child {
                        border-bottom: none;
                    }
                    .detail-label {
                        font-weight: 600;
                        color: #374151;
                        min-width: 140px;
                    }
                    .detail-value {
                        color: #6b7280;
                        flex: 1;
                    }
                    .customer-box {
                        background: #dbeafe;
                        border-left: 4px solid #3b82f6;
                        padding: 20px;
                        margin: 25px 0;
                        border-radius: 5px;
                    }
                    .footer {
                        background: #f9fafb;
                        padding: 20px 30px;
                        text-align: center;
                        color: #6b7280;
                        font-size: 14px;
                        border-top: 1px solid #e5e7eb;
                    }
                    .footer strong {
                        color: #374151;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔔 New Task Assigned</h1>
                        <span class="task-badge">APPOINTMENT #%d</span>
                    </div>
                    
                    <div class="content">
                        <p class="greeting">Hello %s,</p>
                        
                        <p class="message">
                            You have been assigned a new service appointment. Please review the details below 
                            and ensure you're prepared to provide excellent service.
                        </p>
                        
                        <div class="alert-box">
                            <p>⚠️ This task is now IN PROGRESS - Please begin work as scheduled</p>
                        </div>
                        
                        <div class="customer-box">
                            <h3 style="margin-top: 0; color: #1f2937;">👤 Customer Information</h3>
                            
                            <div class="detail-row">
                                <span class="detail-label">Customer Name:</span>
                                <span class="detail-value"><strong>%s</strong></span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">Contact Number:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="details-box">
                            <h3 style="margin-top: 0; color: #1f2937;">📋 Service Details</h3>
                            
                            <div class="detail-row">
                                <span class="detail-label">📅 Date:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🕐 Time:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🚙 Vehicle Type:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🔢 Vehicle Number:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            
                            <div class="detail-row">
                                <span class="detail-label">🔧 Service Required:</span>
                                <span class="detail-value"><strong>%s</strong></span>
                            </div>
                            
                            %s
                        </div>
                        
                        <p class="message">
                            Please ensure all work is completed to our high standards. 
                            If you have any questions or need assistance, please contact the admin team.
                        </p>
                    </div>
                    
                    <div class="footer">
                        <p>
                            <strong>%s</strong><br>
                            This is an automated message, please do not reply to this email.<br>
                            <a href="mailto:%s" style="color: #f59e0b; text-decoration: none;">%s</a>
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
                appointment.getId(),
                employeeName != null ? employeeName : "Team Member",
                customerName,
                customerPhone,
                appointment.getDate().format(dateFormatter),
                appointment.getTime().format(timeFormatter),
                appointment.getVehicleType(),
                appointment.getVehicleNumber(),
                appointment.getService(),
                instructionsRow,
                fromName,
                fromEmail,
                fromEmail
            );
    }
}

