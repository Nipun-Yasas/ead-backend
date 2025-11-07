# Invoice Generation API

## Endpoint
`POST /api/invoices/generate`

## Description
Generate a professional PDF invoice for a completed appointment. Returns the invoice as a PDF blob file and optionally sends it to the customer's email.

## Authorization
Required roles: `SUPER_ADMIN`, `ADMIN`, or `EMPLOYEE`

## Request

### Request Body
```json
{
  "appointmentId": 1,
  "taskBreakdown": [
    {
      "taskName": "Oil Change",
      "price": 45.00
    },
    {
      "taskName": "Air Filter Replacement",
      "price": 25.00
    },
    {
      "taskName": "Brake Inspection",
      "price": 30.00
    }
  ],
  "totalPrice": 100.00,
  "sendToCustomer": true
}
```

#### Fields
- `appointmentId` (Long, required) - ID of the appointment
- `taskBreakdown` (Array, required) - List of services performed
  - `taskName` (String, required) - Description of the service/task
  - `price` (Decimal, required) - Price for this specific task
- `totalPrice` (Decimal, required) - Total invoice amount
- `sendToCustomer` (Boolean, optional) - If `true`, sends invoice to customer's email. Default: `false`

## Response

### Success (200 OK)
Returns a PDF file as binary data (blob)

**Headers:**
```
Content-Type: application/pdf
Content-Disposition: attachment; filename="invoice-{appointmentId}.pdf"
Content-Length: {file_size}
```

**Body:** Binary PDF data

### Error (400 Bad Request)
```json
{
  "message": "Appointment not found with id: 1"
}
```

or

```json
{
  "message": "No customer associated with this appointment"
}
```

### Error (500 Internal Server Error)
```json
{
  "message": "Failed to generate invoice: {error details}"
}
```

## Invoice Contents

The generated PDF invoice includes:

### Header Section
- Company name and logo
- "INVOICE" title

### Company & Customer Information
- **From:** Company name, address, phone, email
- **Bill To:** Customer name, email, phone (from User table)

### Invoice Details
- Invoice number (auto-generated)
- Invoice date (current date)
- Service date (appointment date)
- Vehicle information (type and number)
- Service type

### Service Breakdown Table
Professional table with:
- Task descriptions
- Individual prices
- Alternating row colors for readability

### Totals Section
- Subtotal
- Tax (currently 0%, can be configured)
- **TOTAL** (highlighted)

### Footer
- Thank you message
- Payment terms
- Contact information

## Invoice Design Features

✨ **Professional Layout:**
- Clean, modern design
- Company brand colors (purple/blue gradient)
- Well-organized sections with proper spacing

📊 **Easy to Read:**
- Clear typography
- Alternating row colors in tables
- Highlighted totals

📧 **Email Integration:**
- Automatically sends to customer email when `sendToCustomer: true`
- Includes HTML email with invoice summary
- PDF attached to email

## Examples

### Example 1: Generate and Download Invoice
```bash
curl -X POST http://localhost:8080/api/invoices/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "appointmentId": 5,
    "taskBreakdown": [
      {"taskName": "Full Service", "price": 120.00},
      {"taskName": "Tire Rotation", "price": 40.00},
      {"taskName": "Wheel Alignment", "price": 80.00}
    ],
    "totalPrice": 240.00,
    "sendToCustomer": false
  }' \
  --output invoice.pdf
```

### Example 2: Generate and Email to Customer
```bash
curl -X POST http://localhost:8080/api/invoices/generate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "appointmentId": 5,
    "taskBreakdown": [
      {"taskName": "Engine Diagnostic", "price": 85.00},
      {"taskName": "Spark Plug Replacement", "price": 65.00},
      {"taskName": "Battery Test", "price": 15.00}
    ],
    "totalPrice": 165.00,
    "sendToCustomer": true
  }' \
  --output invoice.pdf
```

### Example 3: JavaScript/Fetch API
```javascript
const generateInvoice = async (appointmentId, tasks, total, emailCustomer) => {
  const response = await fetch('http://localhost:8080/api/invoices/generate', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      appointmentId: appointmentId,
      taskBreakdown: tasks,
      totalPrice: total,
      sendToCustomer: emailCustomer
    })
  });

  if (response.ok) {
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `invoice-${appointmentId}.pdf`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
  }
};

// Usage
generateInvoice(5, [
  { taskName: 'Oil Change', price: 45.00 },
  { taskName: 'Filter Replacement', price: 25.00 }
], 70.00, true);
```

### Example 4: React/Axios
```javascript
import axios from 'axios';

const downloadInvoice = async (invoiceData) => {
  try {
    const response = await axios.post(
      'http://localhost:8080/api/invoices/generate',
      invoiceData,
      {
        headers: {
          'Authorization': `Bearer ${token}`
        },
        responseType: 'blob'
      }
    );

    // Create download link
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `invoice-${invoiceData.appointmentId}.pdf`);
    document.body.appendChild(link);
    link.click();
    link.remove();

    if (invoiceData.sendToCustomer) {
      alert('Invoice generated and sent to customer email!');
    } else {
      alert('Invoice downloaded successfully!');
    }
  } catch (error) {
    console.error('Error generating invoice:', error);
    alert('Failed to generate invoice');
  }
};
```

## Configuration

You can customize company information in `.env`:

```env
COMPANY_NAME=Your Company Name
COMPANY_ADDRESS=123 Main Street, City, State 12345
COMPANY_PHONE=+1 (555) 123-4567
COMPANY_EMAIL=info@yourcompany.com
```

Or in `application.properties`:
```properties
company.name=Your Company Name
company.address=123 Main Street, City, State 12345
company.phone=+1 (555) 123-4567
company.email=info@yourcompany.com
```

## Notes

- Customer information is automatically fetched from the User table based on the appointment
- Invoice number is auto-generated using pattern: `INV-{appointmentId}-{timestamp}`
- PDF uses professional iText library for high-quality output
- Email is sent asynchronously if `sendToCustomer` is `true`
- The API returns immediately with the PDF; email sending happens in background
- Tax rate is currently 0% but can be configured in the service
