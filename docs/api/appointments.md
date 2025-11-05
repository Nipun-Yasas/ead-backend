# Appointments API

This document explains how to load appointments for a customer in your Spring Boot backend.

- Base URL: `/api/appointments`
- Auth: JWT Bearer token in the `Authorization` header (issued by your auth endpoints)
- DTO used in responses: `Backend.dto.Response.AppointmentResponse`

## Get current user's appointments

Use this when the frontend is authenticated as the customer and you want that customer's own appointments.

- Method: GET
- Path: `/api/appointments/my`
- Auth: required (customer JWT)
- Roles: any authenticated user
- Query params: none
- Pagination: none (returns a list)

Example request:

```http
GET /api/appointments/my HTTP/1.1
Host: your-api.example.com
Authorization: Bearer <jwt-token>
```

Example response (200):

```json
[
  {
    "id": 42,
    "date": "2025-11-05",
    "time": "10:30:00",
    "vehicleType": "Car",
    "vehicleNumber": "ABC-1234",
    "service": "Oil Change",
    "instructions": "rattle noise near engine",
    "status": "PENDING",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "customerPhone": "+1-555-0100",
    "customer": {
      "id": 7,
      "fullName": "John Doe",
      "email": "john@example.com"
    },
    "employee": {
      "id": 3,
      "fullName": "Jane Smith",
      "email": "jane@shop.example"
    },
    "createdAt": "2025-11-01T15:04:21",
    "updatedAt": "2025-11-02T09:12:00"
  }
]
```

Possible status codes:
- 200 OK — list returned (possibly empty)
- 401 Unauthorized — missing/invalid token

Notes:
- `status` values: `ACCEPT`, `REJECT`, `APPROVE`, `PENDING`, `ONGOING`.
- For anonymous bookings, top-level `customerName`, `customerEmail`, `customerPhone` may be set and the nested `customer` object may be `null`.

## Get appointments for a specific customer (by ID) — Admin/Employee use-case

The repository and service layer support fetching by customer, but there is no controller endpoint exposed for "by customer ID" yet. If you need this for admin/employee tooling, consider adding an endpoint like below and secure it for staff roles only.

Proposed endpoint (not yet implemented in the controller):

- Method: GET
- Path: `/api/appointments/customer/{customerId}`
- Auth: required
- Roles: `SUPER_ADMIN`, `ADMIN`, or `EMPLOYEE`
- Path params:
  - `customerId` (long) — the user's ID
- Query params: optional filters in future (e.g., `status`)

Example request:

```http
GET /api/appointments/customer/7 HTTP/1.1
Host: your-api.example.com
Authorization: Bearer <admin-or-employee-jwt>
```

Example response (200): same schema as above (array of `AppointmentResponse`).

Possible status codes:
- 200 OK — list returned (possibly empty)
- 400 Bad Request — invalid `customerId`
- 401 Unauthorized — missing/invalid token
- 403 Forbidden — caller lacks required role

### Minimal controller sketch (for reference only)

```java
@GetMapping("/customer/{customerId}")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('EMPLOYEE')")
public ResponseEntity<List<AppointmentResponse>> getByCustomer(@PathVariable Long customerId) {
    List<AppointmentResponse> list = appointmentService
        .getAppointmentsByCustomerId(customerId); // implement in service
    return ResponseEntity.ok(list);
}
```

### Response schema (AppointmentResponse)

Fields returned by the API (based on `Backend.dto.Response.AppointmentResponse`):
- `id` (number)
- `date` (string, ISO date, e.g., `2025-11-05`)
- `time` (string, `HH:mm:ss`)
- `vehicleType` (string)
- `vehicleNumber` (string)
- `service` (string)
- `instructions` (string)
- `status` (string; one of `ACCEPT`, `REJECT`, `APPROVE`, `PENDING`, `ONGOING`)
- `customerName` (string)
- `customerEmail` (string)
- `customerPhone` (string)
- `customer` (object | null)
  - `id` (number)
  - `fullName` (string)
  - `email` (string)
- `employee` (object | null)
  - `id` (number)
  - `fullName` (string)
  - `email` (string)
- `createdAt` (string, ISO datetime)
- `updatedAt` (string, ISO datetime)

## Curl examples

- Current user's appointments:

```bash
curl -s -H "Authorization: Bearer $TOKEN" \
  https://your-api.example.com/api/appointments/my
```

- Proposed, by customer ID (admin/employee):

```bash
curl -s -H "Authorization: Bearer $ADMIN_TOKEN" \
  https://your-api.example.com/api/appointments/customer/7
```

## Related code

- Controller: `src/main/java/Backend/controller/AppointmentController.java`
- Service: `src/main/java/Backend/service/AppointmentService.java`
- Repository: `src/main/java/Backend/repository/AppointmentRepository.java`
- DTO: `src/main/java/Backend/dto/Response/AppointmentResponse.java`
- Entity: `src/main/java/Backend/entity/Appointment.java`
