# Send Custom Email API

## Endpoint
`POST /api/email/send`

## Description
Send a custom email message to any recipient. This endpoint allows administrators and employees to send personalized messages to customers or other users.

## Authorization
Required roles: `SUPER_ADMIN`, `ADMIN`, or `EMPLOYEE`

## Request

### Request Body
```json
{
  "email": "customer@example.com",
  "message": "Dear Customer,\n\nThank you for choosing our service. We wanted to inform you that your vehicle is ready for pickup.\n\nBest regards,\nAutoCare Team"
}
```

#### Fields
- `email` (String, required) - Recipient's email address (must be valid email format)
- `message` (String, required) - The message content to send

## Response

### Success (200 OK)
```json
{
  "message": "Email sent successfully to customer@example.com"
}
```

### Error Responses

#### Invalid Email Format (400 Bad Request)
```json
{
  "timestamp": "2025-11-07T12:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid email format",
  "path": "/api/email/send"
}
```

#### Missing Required Field (400 Bad Request)
```json
{
  "timestamp": "2025-11-07T12:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Email is required",
  "path": "/api/email/send"
}
```

#### Email Sending Failed (500 Internal Server Error)
```json
{
  "message": "Failed to send email: Connection timeout"
}
```

#### Unauthorized (403 Forbidden)
```json
{
  "timestamp": "2025-11-07T12:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/email/send"
}
```

## Email Template

The email will be sent with:
- **Subject**: "Message from AutoCare" (or your configured company name)
- **Professional HTML template** with company branding
- **Purple gradient header** with email icon
- **Formatted message body** (preserves line breaks)
- **Company footer** with contact information

### Example Email Appearance:
```
┌─────────────────────────────────────┐
│  📧 Message from AutoCare           │ (Purple gradient)
├─────────────────────────────────────┤
│                                     │
│  Dear Customer,                     │
│                                     │
│  Thank you for choosing our         │
│  service. We wanted to inform you   │
│  that your vehicle is ready for     │
│  pickup.                            │
│                                     │
│  Best regards,                      │
│  AutoCare Team                      │
│                                     │
│  If you have any questions,         │
│  please don't hesitate to contact   │
│  us.                                │
│                                     │
├─────────────────────────────────────┤
│  AutoCare                           │ (Gray footer)
│  info.iymart@gmail.com              │
└─────────────────────────────────────┘
```

## Example Usage

### cURL
```bash
curl -X POST http://localhost:8080/api/email/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "email": "customer@example.com",
    "message": "Your vehicle service is complete and ready for pickup."
  }'
```

### JavaScript/Fetch
```javascript
const sendEmail = async (email, message, token) => {
  const response = await fetch('http://localhost:8080/api/email/send', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      email: email,
      message: message
    })
  });

  if (!response.ok) {
    throw new Error('Failed to send email');
  }

  return await response.json();
};

// Usage
sendEmail(
  'customer@example.com',
  'Your vehicle is ready for pickup!',
  token
)
  .then(result => console.log(result.message))
  .catch(error => console.error('Error:', error));
```

### Axios
```javascript
import axios from 'axios';

const sendCustomEmail = async (email, message) => {
  try {
    const response = await axios.post(
      'http://localhost:8080/api/email/send',
      {
        email: email,
        message: message
      },
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }
    );
    return response.data;
  } catch (error) {
    console.error('Error sending email:', error);
    throw error;
  }
};

// Usage
sendCustomEmail(
  'john@example.com',
  'Dear John,\n\nYour appointment has been confirmed.\n\nThank you!'
)
  .then(result => alert(result.message))
  .catch(error => alert('Failed to send email'));
```

### React Component Example
```javascript
import { useState } from 'react';
import axios from 'axios';

const SendEmailForm = () => {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [sending, setSending] = useState(false);
  const [result, setResult] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSending(true);
    setResult(null);

    try {
      const response = await axios.post(
        'http://localhost:8080/api/email/send',
        { email, message },
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
      );
      
      setResult({ type: 'success', message: response.data.message });
      setEmail('');
      setMessage('');
    } catch (error) {
      setResult({ 
        type: 'error', 
        message: error.response?.data?.message || 'Failed to send email' 
      });
    } finally {
      setSending(false);
    }
  };

  return (
    <div className="send-email-form">
      <h2>Send Custom Email</h2>
      
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Recipient Email:</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="customer@example.com"
            required
          />
        </div>

        <div className="form-group">
          <label>Message:</label>
          <textarea
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            placeholder="Enter your message here..."
            rows={6}
            required
          />
        </div>

        <button type="submit" disabled={sending}>
          {sending ? 'Sending...' : 'Send Email'}
        </button>
      </form>

      {result && (
        <div className={`alert alert-${result.type}`}>
          {result.message}
        </div>
      )}
    </div>
  );
};
```

## Use Cases

### 1. Vehicle Ready Notification
```javascript
sendCustomEmail(
  customer.email,
  `Dear ${customer.name},\n\nGreat news! Your ${vehicle.type} (${vehicle.number}) is now ready for pickup.\n\nService completed: ${service.name}\n\nPlease contact us to arrange a convenient pickup time.\n\nBest regards,\nAutoCare Team`
);
```

### 2. Special Promotion
```javascript
sendCustomEmail(
  customer.email,
  `Dear ${customer.name},\n\nWe're offering a special 20% discount on our premium oil change service this month!\n\nBook your appointment now and save.\n\nValid until: ${promoEndDate}\n\nBest regards,\nAutoCare Team`
);
```

### 3. Appointment Reminder
```javascript
sendCustomEmail(
  customer.email,
  `Dear ${customer.name},\n\nThis is a friendly reminder about your upcoming appointment:\n\nDate: ${appointment.date}\nTime: ${appointment.time}\nService: ${appointment.service}\n\nPlease arrive 10 minutes early.\n\nBest regards,\nAutoCare Team`
);
```

### 4. Service Feedback Request
```javascript
sendCustomEmail(
  customer.email,
  `Dear ${customer.name},\n\nThank you for choosing our service for your recent ${service.name}.\n\nWe'd love to hear about your experience! Please take a moment to share your feedback.\n\nYour opinion helps us improve our services.\n\nBest regards,\nAutoCare Team`
);
```

## Message Formatting

### Line Breaks
Use `\n` (newline character) in your message to create line breaks:
```json
{
  "message": "Line 1\nLine 2\n\nLine 4 (with blank line above)"
}
```

### Special Characters
The message content is automatically escaped and sanitized for HTML display:
- Line breaks (`\n`) are converted to `<br>` tags
- HTML special characters are preserved
- Text wrapping is handled automatically

## Notes

### Asynchronous Sending
- Emails are sent **asynchronously** using Spring's `@Async`
- API responds immediately after queuing the email
- Actual sending happens in the background
- Check application logs for sending confirmation

### Email Configuration
Make sure your email settings are configured in `application.properties`:
```properties
spring.mail.from=info.iymart@gmail.com
spring.mail.from-name=AutoCare
```

### Security
- Only authenticated users with ADMIN, SUPER_ADMIN, or EMPLOYEE roles can send emails
- Prevents email spam by restricting access
- Consider adding rate limiting for production use

### Rate Limiting (Recommended for Production)
```java
// Add to controller or service
@RateLimiter(name = "emailService", fallbackMethod = "rateLimitFallback")
public ResponseEntity<?> sendEmail(@Valid @RequestBody SendEmailRequest request) {
    // ... existing code
}
```

### Validation
- Email format is validated using `@Email` annotation
- Both email and message fields are required (`@NotBlank`)
- Empty or whitespace-only messages are rejected

### Best Practices
1. **Keep messages concise** - Recipients prefer short, clear messages
2. **Include context** - Mention the service, vehicle, or appointment details
3. **Be professional** - Use proper grammar and formatting
4. **Add call-to-action** - Tell recipients what to do next
5. **Test thoroughly** - Send test emails before using in production

## Troubleshooting

### Email Not Received
1. Check spam/junk folder
2. Verify email address is correct
3. Check application logs for errors
4. Verify SMTP configuration in `application.properties`
5. Ensure Gmail app password is set correctly (if using Gmail)

### Common Errors
- **"Invalid email format"**: Check the email address syntax
- **"Failed to send email"**: Check SMTP settings and internet connection
- **"Access Denied"**: Ensure user has proper role (ADMIN/EMPLOYEE)
- **"Email is required"**: Provide both email and message fields

## Testing

### Test Email Sending
```bash
# Test with valid data
curl -X POST http://localhost:8080/api/email/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "email": "test@example.com",
    "message": "This is a test message."
  }'

# Test with invalid email
curl -X POST http://localhost:8080/api/email/send \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "email": "invalid-email",
    "message": "Test"
  }'

# Test without authentication (should fail)
curl -X POST http://localhost:8080/api/email/send \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "message": "Test"
  }'
```

### Check Logs
After sending an email, check the application logs:
```
2025-11-07T12:00:00.000  INFO 12345 --- Backend.service.EmailService : Custom email sent successfully to: customer@example.com
```

Or for errors:
```
2025-11-07T12:00:00.000  ERROR 12345 --- Backend.service.EmailService : Failed to send custom email to customer@example.com
```
