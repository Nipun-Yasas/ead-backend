package Backend.service;

import Backend.dto.Request.GenerateInvoiceRequest;
import Backend.entity.Appointment;
import Backend.entity.User;
import Backend.repository.AppointmentRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final AppointmentRepository appointmentRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:info.iymart@gmail.com}")
    private String fromEmail;

    @Value("${spring.mail.from-name:AutoCare}")
    private String companyName;

    @Value("${company.address:123 Auto Street, Car City, CC 12345}")
    private String companyAddress;

    @Value("${company.phone:+1 (555) 123-4567}")
    private String companyPhone;

    @Value("${company.email:info.iymart@gmail.com}")
    private String companyEmail;

    public byte[] generateInvoice(GenerateInvoiceRequest request) throws Exception {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + request.getAppointmentId()));

        if (appointment.getCustomer() == null) {
            throw new RuntimeException("No customer associated with this appointment");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Set margins
        document.setMargins(40, 40, 40, 40);

        // Add invoice content
        addHeader(document, appointment);
        addCompanyAndCustomerInfo(document, appointment);
        addInvoiceDetails(document, appointment);
        addTaskBreakdown(document, request);
        addTotal(document, request.getTotalPrice());
        addFooter(document);

        document.close();

        byte[] pdfBytes = baos.toByteArray();

        // Send email if requested
        if (Boolean.TRUE.equals(request.getSendToCustomer())) {
            sendInvoiceEmail(appointment, pdfBytes);
        }

        return pdfBytes;
    }

    private void addHeader(Document document, Appointment appointment) {
        // Company name and invoice title
        Paragraph companyTitle = new Paragraph(companyName)
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(new DeviceRgb(102, 126, 234));

        Paragraph invoiceTitle = new Paragraph("INVOICE")
                .setFontSize(28)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5)
                .setMarginBottom(20);

        document.add(companyTitle);
        document.add(invoiceTitle);
    }

    private void addCompanyAndCustomerInfo(Document document, Appointment appointment) {
        User customer = appointment.getCustomer();

        // Create table with 2 columns
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        // Company information
        Cell companyCell = new Cell()
                .setBorder(null)
                .add(new Paragraph("From:").setBold().setFontSize(12).setMarginBottom(5))
                .add(new Paragraph(companyName).setFontSize(11))
                .add(new Paragraph(companyAddress).setFontSize(10))
                .add(new Paragraph("Phone: " + companyPhone).setFontSize(10))
                .add(new Paragraph("Email: " + companyEmail).setFontSize(10));

        // Customer information
        Cell customerCell = new Cell()
                .setBorder(null)
                .add(new Paragraph("Bill To:").setBold().setFontSize(12).setMarginBottom(5))
                .add(new Paragraph(customer.getFullName()).setFontSize(11))
                .add(new Paragraph("Email: " + customer.getEmail()).setFontSize(10));

        if (customer.getPhone() != null && !customer.getPhone().isEmpty()) {
            customerCell.add(new Paragraph("Phone: " + customer.getPhone()).setFontSize(10));
        }

        infoTable.addCell(companyCell);
        infoTable.addCell(customerCell);

        document.add(infoTable);
    }

    private void addInvoiceDetails(Document document, Appointment appointment) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        String invoiceNumber = "INV-" + appointment.getId() + "-" + System.currentTimeMillis();

        Table detailsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        // Invoice details
        Cell detailsCell = new Cell()
                .setBorder(null)
                .add(new Paragraph("Invoice Number: " + invoiceNumber).setFontSize(10).setBold())
                .add(new Paragraph("Invoice Date: " + LocalDateTime.now().format(dateFormatter)).setFontSize(10))
                .add(new Paragraph("Service Date: " + appointment.getDate().format(dateFormatter)).setFontSize(10));

        // Service details
        Cell serviceCell = new Cell()
                .setBorder(null)
                .add(new Paragraph("Vehicle: " + appointment.getVehicleType()).setFontSize(10))
                .add(new Paragraph("Vehicle Number: " + appointment.getVehicleNumber()).setFontSize(10))
                .add(new Paragraph("Service Type: " + appointment.getService()).setFontSize(10));

        detailsTable.addCell(detailsCell);
        detailsTable.addCell(serviceCell);

        document.add(detailsTable);
    }

    private void addTaskBreakdown(Document document, GenerateInvoiceRequest request) {
        // Service breakdown title
        Paragraph breakdownTitle = new Paragraph("Service Breakdown")
                .setFontSize(14)
                .setBold()
                .setMarginTop(10)
                .setMarginBottom(10);
        document.add(breakdownTitle);

        // Create table for tasks
        Table taskTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .useAllAvailableWidth();

        // Header row
        DeviceRgb headerColor = new DeviceRgb(102, 126, 234);
        taskTable.addHeaderCell(new Cell()
                .add(new Paragraph("Task Description").setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(headerColor)
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(10));

        taskTable.addHeaderCell(new Cell()
                .add(new Paragraph("Amount").setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(headerColor)
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(10));

        // Add tasks
        DeviceRgb rowColor = new DeviceRgb(249, 250, 251);
        boolean alternateRow = false;

        for (GenerateInvoiceRequest.TaskItem task : request.getTaskBreakdown()) {
            Cell taskCell = new Cell()
                    .add(new Paragraph(task.getTaskName()).setFontSize(10))
                    .setPadding(8)
                    .setBorder(null);

            Cell priceCell = new Cell()
                    .add(new Paragraph("$" + task.getPrice().toString()).setFontSize(10))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPadding(8)
                    .setBorder(null);

            if (alternateRow) {
                taskCell.setBackgroundColor(rowColor);
                priceCell.setBackgroundColor(rowColor);
            }

            taskTable.addCell(taskCell);
            taskTable.addCell(priceCell);
            alternateRow = !alternateRow;
        }

        document.add(taskTable);
    }

    private void addTotal(Document document, BigDecimal totalPrice) {
        // Add some space
        document.add(new Paragraph("\n"));

        // Total table
        Table totalTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .useAllAvailableWidth();

        // Subtotal
        totalTable.addCell(new Cell()
                .add(new Paragraph("Subtotal:").setBold())
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(null)
                .setPadding(5));
        totalTable.addCell(new Cell()
                .add(new Paragraph("$" + totalPrice.toString()))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(null)
                .setPadding(5));

        // Tax (you can calculate this or receive from frontend)
        BigDecimal tax = totalPrice.multiply(new BigDecimal("0.00")); // 0% tax
        totalTable.addCell(new Cell()
                .add(new Paragraph("Tax:").setBold())
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(null)
                .setPadding(5));
        totalTable.addCell(new Cell()
                .add(new Paragraph("$" + tax.toString()))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(null)
                .setPadding(5));

        // Total
        DeviceRgb totalBgColor = new DeviceRgb(102, 126, 234);
        BigDecimal finalTotal = totalPrice.add(tax);

        totalTable.addCell(new Cell()
                .add(new Paragraph("TOTAL:").setBold().setFontSize(14).setFontColor(ColorConstants.WHITE))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBackgroundColor(totalBgColor)
                .setPadding(10));
        totalTable.addCell(new Cell()
                .add(new Paragraph("$" + finalTotal.toString()).setBold().setFontSize(14).setFontColor(ColorConstants.WHITE))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBackgroundColor(totalBgColor)
                .setPadding(10));

        document.add(totalTable);
    }

    private void addFooter(Document document) {
        document.add(new Paragraph("\n\n"));

        // Thank you message
        Paragraph thankYou = new Paragraph("Thank you for your business!")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(12)
                .setMarginBottom(10);

        // Notes
        Paragraph notes = new Paragraph("Payment is due within 30 days. Please make checks payable to " + companyName + ".")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(5);

        Paragraph contact = new Paragraph("For any questions concerning this invoice, contact us at " + companyEmail)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(9)
                .setFontColor(ColorConstants.GRAY);

        document.add(thankYou);
        document.add(notes);
        document.add(contact);

        // Add border line
        document.add(new Paragraph("\n"));
        Table borderTable = new Table(1).useAllAvailableWidth();
        borderTable.addCell(new Cell().setBorder(new SolidBorder(new DeviceRgb(102, 126, 234), 2)).setHeight(2));
        document.add(borderTable);
    }

    private void sendInvoiceEmail(Appointment appointment, byte[] pdfBytes) {
        try {
            User customer = appointment.getCustomer();
            String recipientEmail = customer.getEmail();

            if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
                log.warn("Cannot send invoice - no email address found for customer {}", customer.getId());
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, companyName);
            helper.setTo(recipientEmail);
            helper.setSubject("Invoice for Your Service - " + companyName);

            String emailBody = buildInvoiceEmailBody(appointment, customer);
            helper.setText(emailBody, true);

            // Attach PDF
            String filename = "Invoice-" + appointment.getId() + ".pdf";
            helper.addAttachment(filename, new ByteArrayDataSource(pdfBytes, "application/pdf"));

            mailSender.send(message);
            log.info("Invoice email sent successfully to: {}", recipientEmail);

        } catch (MessagingException e) {
            log.error("Failed to send invoice email for appointment {}", appointment.getId(), e);
        } catch (Exception e) {
            log.error("Unexpected error sending invoice email", e);
        }
    }

    private String buildInvoiceEmailBody(Appointment appointment, User customer) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #fff; padding: 30px; border: 1px solid #ddd; border-radius: 0 0 10px 10px; }
                    .footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }
                    h1 { margin: 0; font-size: 24px; }
                    .button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>📄 Invoice Attached</h1>
                    </div>
                    <div class="content">
                        <p>Dear %s,</p>
                        <p>Thank you for choosing <strong>%s</strong> for your vehicle service needs.</p>
                        <p>Please find your invoice attached to this email for the service performed on <strong>%s</strong>.</p>
                        <p><strong>Service Details:</strong></p>
                        <ul>
                            <li>Vehicle: %s (%s)</li>
                            <li>Service Type: %s</li>
                            <li>Service Date: %s</li>
                        </ul>
                        <p>The invoice includes a detailed breakdown of all services performed and their costs.</p>
                        <p>If you have any questions about this invoice, please don't hesitate to contact us.</p>
                        <p>We appreciate your business and look forward to serving you again!</p>
                    </div>
                    <div class="footer">
                        <p><strong>%s</strong><br>%s<br>Email: %s | Phone: %s</p>
                        <p>This is an automated email. Please do not reply directly to this message.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                customer.getFullName(),
                companyName,
                appointment.getDate().format(dateFormatter),
                appointment.getVehicleType(),
                appointment.getVehicleNumber(),
                appointment.getService(),
                appointment.getDate().format(dateFormatter),
                companyName,
                companyAddress,
                companyEmail,
                companyPhone
        );
    }
}
