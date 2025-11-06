# Change Appointment Status API

## Endpoint
`PATCH /api/appointments/{id}/status`

## Description
Change the status of an appointment and automatically send an email notification to the customer about the status change.

## Authorization
Required roles: `SUPER_ADMIN`, `ADMIN`, or `EMPLOYEE`

## Request

### Path Parameters
- `id` (Long, required) - The ID of the appointment

### Request Body
```json
{
  "status": "APPROVE",
  "notes": "Optional notes about the status change"
}
```

#### Fields
- `status` (String, required) - The new status. Available values:
  - `PENDING` - Appointment is pending review
  - `APPROVE` - Appointment is approved
  - `ACCEPT` - Appointment is accepted
  - `CONFIRMED` - Appointment is confirmed
  - `IN_PROGRESS` - Service work is in progress
  - `ONGOING` - Appointment is ongoing
  - `REJECT` - Appointment is rejected
  
- `notes` (String, optional) - Additional notes about the status change that will be included in the email

## Response

### Success (200 OK)
```json
{
  "id": 1,
  "date": "2025-11-10",
  "time": "10:00:00",
  "vehicleType": "Toyota Camry",
  "vehicleNumber": "ABC-1234",
  "service": "Oil Change",
  "instructions": "Check brakes too",
  "status": "APPROVE",
  "customer": {
    "id": 5,
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "+1234567890"
  },
  "employee": {
    "id": 3,
    "name": "Mike Technician",
    "email": "mike@autocare.com"
  },
  "createdAt": "2025-11-06T10:30:00",
  "updatedAt": "2025-11-06T11:45:00"
}
```

### Error (400 Bad Request)
```json
{
  "message": "Appointment not found with id: 1"
}
```

or

```json
{
  "message": "You don't have permission to change appointment status"
}
```

## Examples

### Example 1: Approve Appointment
```bash
curl -X PATCH http://localhost:8080/api/appointments/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "status": "APPROVE",
    "notes": "Your appointment has been reviewed and approved. Looking forward to serving you!"
  }'
```

### Example 2: Mark as In Progress
```bash
curl -X PATCH http://localhost:8080/api/appointments/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "status": "IN_PROGRESS",
    "notes": "Our technician has started working on your vehicle"
  }'
```

### Example 3: Reject Appointment
```bash
curl -X PATCH http://localhost:8080/api/appointments/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "status": "REJECT",
    "notes": "Unfortunately, we don't have available slots. Please contact us to reschedule."
  }'
```

## Email Notifications

When the status is changed, the customer will automatically receive an email with:
- Status update information
- Appointment details (date, time, vehicle, service)
- Assigned technician information
- Any additional notes provided
- Styled email template with status-specific colors and icons

### Status-Specific Email Colors
- `PENDING` - Yellow/Amber
- `APPROVE` - Green
- `ACCEPT` - Blue
- `CONFIRMED` - Purple
- `IN_PROGRESS` - Cyan
- `ONGOING` - Teal
- `REJECT` - Red

## Notes
- Only users with ADMIN, SUPER_ADMIN, or EMPLOYEE roles can change status
- Customer email is fetched from the User table based on the appointment's customer relationship
- Email is sent asynchronously, so the API responds immediately
- Check application logs for email sending confirmation
