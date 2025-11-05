# API Documentation for Frontend

Base URL: `http://localhost:8080`

## Authentication

All protected endpoints require a JWT token in the Authorization header:
```
Authorization: Bearer <your_jwt_token>
```

## Authentication Endpoints

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "user@example.com",
    "role": "CUSTOMER"
  }
}
```

### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "user@example.com",
  "password": "password123",
  "phone": "+1234567890"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "user@example.com",
    "role": "CUSTOMER"
  }
}
```

## Appointment Endpoints

### Create Appointment
```http
POST /api/appointments
Content-Type: application/json
Authorization: Bearer <token> (optional for anonymous booking)

{
  "date": "2024-11-15",
  "time": "10:30",
  "vehicleType": "car",
  "vehicleNumber": "ABC-1234",
  "service": "repair",
  "instructions": "Check engine noise",
  "userId": 28,
  "customerName": "John Doe",
  "customerEmail": "john@example.com",
  "customerPhone": "+1234567890"
}
```

**Field Rules:**
- `date`: Required, format YYYY-MM-DD
- `time`: Required, format HH:MM
- `vehicleType`: Required (car, motorcycle, truck, van, etc.)
- `vehicleNumber`: Required
- `service`: Required (repair, maintenance, inspection, etc.)
- `instructions`: Optional
- `userId`: Optional, use when admin/employee creates for customer
- `customerName`, `customerEmail`, `customerPhone`: Required for anonymous bookings

**Response (201 Created):**
```json
{
  "id": 1,
  "date": "2024-11-15",
  "time": "10:30",
  "vehicleType": "car",
  "vehicleNumber": "ABC-1234",
  "service": "repair",
  "instructions": "Check engine noise",
  "status": "PENDING",
  "customerName": null,
  "customerEmail": null,
  "customerPhone": null,
  "customer": {
    "id": 28,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "employee": null,
  "createdAt": "2024-11-05T10:30:00Z",
  "updatedAt": "2024-11-05T10:30:00Z"
}
```

### Get My Appointments
```http
GET /api/appointments/my
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "date": "2024-11-15",
    "time": "10:30",
    "vehicleType": "car",
    "vehicleNumber": "ABC-1234",
    "service": "repair",
    "instructions": "Check engine noise",
    "status": "PENDING",
    "customer": {
      "id": 28,
      "name": "John Doe",
      "email": "john@example.com"
    },
    "employee": null
  }
]
```

### Get All Appointments (Admin/Employee)
```http
GET /api/appointments?page=0&size=10
Authorization: Bearer <token>
```

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Items per page (default: 10)

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "date": "2024-11-15",
      "time": "10:30",
      "vehicleType": "car",
      "vehicleNumber": "ABC-1234",
      "service": "repair",
      "status": "PENDING",
      "customer": {
        "id": 28,
        "name": "John Doe",
        "email": "john@example.com"
      },
      "employee": null
    }
  ],
  "totalElements": 50,
  "totalPages": 5,
  "size": 10,
  "number": 0
}
```

### Get Appointment by ID
```http
GET /api/appointments/{id}
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "date": "2024-11-15",
  "time": "10:30",
  "vehicleType": "car",
  "vehicleNumber": "ABC-1234",
  "service": "repair",
  "instructions": "Check engine noise",
  "status": "PENDING",
  "customer": {
    "id": 28,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "employee": null
}
```

### Update Appointment
```http
PUT /api/appointments/{id}
Content-Type: application/json
Authorization: Bearer <token>

{
  "date": "2024-11-16",
  "time": "14:00",
  "vehicleType": "motorcycle",
  "vehicleNumber": "XYZ-9876",
  "service": "maintenance",
  "instructions": "Regular service",
  "status": "APPROVED",
  "employeeId": 5
}
```

**All fields are optional. Only provide fields you want to update.**

**Response (200 OK):**
```json
{
  "id": 1,
  "date": "2024-11-16",
  "time": "14:00",
  "vehicleType": "motorcycle",
  "vehicleNumber": "XYZ-9876",
  "service": "maintenance",
  "instructions": "Regular service",
  "status": "APPROVED",
  "customer": {
    "id": 28,
    "name": "John Doe",
    "email": "john@example.com"
  },
  "employee": {
    "id": 5,
    "name": "Mike Smith",
    "email": "mike@company.com"
  }
}
```

### Cancel Appointment
```http
PUT /api/appointments/{id}/cancel
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "status": "REJECTED",
  "message": "Appointment cancelled successfully"
}
```

### Assign Employee (Admin/Employee only)
```http
POST /api/appointments/{appointmentId}/assign/{employeeId}
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "status": "APPROVED",
  "employee": {
    "id": 5,
    "name": "Mike Smith",
    "email": "mike@company.com"
  }
}
```

### Delete Appointment (Admin only)
```http
DELETE /api/appointments/{id}
Authorization: Bearer <token>
```

**Response (204 No Content)**

## Admin Endpoints

### Dashboard Statistics
```http
GET /api/admin/dashboard
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "totalAppointments": 150,
  "pendingAppointments": 25,
  "approvedAppointments": 100,
  "completedAppointments": 20,
  "rejectedAppointments": 5,
  "totalCustomers": 80,
  "totalEmployees": 10,
  "todaysAppointments": 12,
  "weeklyAppointments": 45,
  "monthlyAppointments": 150
}
```

## Chat Endpoints

### Get Chat Messages
```http
GET /api/chat/{appointmentId}
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "content": "Hello, I have a question about my appointment",
    "timestamp": "2024-11-05T10:30:00Z",
    "sender": {
      "id": 28,
      "name": "John Doe",
      "role": "CUSTOMER"
    }
  },
  {
    "id": 2,
    "content": "Sure, how can I help you?",
    "timestamp": "2024-11-05T10:35:00Z",
    "sender": {
      "id": 5,
      "name": "Mike Smith",
      "role": "EMPLOYEE"
    }
  }
]
```

### Send Message
```http
POST /api/chat/{appointmentId}
Content-Type: application/json
Authorization: Bearer <token>

{
  "content": "What time should I arrive?"
}
```

**Response (201 Created):**
```json
{
  "id": 3,
  "content": "What time should I arrive?",
  "timestamp": "2024-11-05T10:40:00Z",
  "sender": {
    "id": 28,
    "name": "John Doe",
    "role": "CUSTOMER"
  }
}
```

## Status Codes and Error Responses

### Success Responses
- `200 OK`: Request successful
- `201 Created`: Resource created successfully
- `204 No Content`: Request successful, no content to return

### Error Responses

#### 400 Bad Request
```json
{
  "timestamp": "2024-11-05T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/appointments"
}
```

#### 401 Unauthorized
```json
{
  "timestamp": "2024-11-05T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is missing or invalid",
  "path": "/api/appointments/my"
}
```

#### 403 Forbidden
```json
{
  "timestamp": "2024-11-05T10:30:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have permission to access this resource",
  "path": "/api/appointments/1"
}
```

#### 404 Not Found
```json
{
  "timestamp": "2024-11-05T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Appointment not found with id: 1",
  "path": "/api/appointments/1"
}
```

#### 409 Conflict
```json
{
  "timestamp": "2024-11-05T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "The selected time slot is not available",
  "path": "/api/appointments"
}
```

#### 500 Internal Server Error
```json
{
  "timestamp": "2024-11-05T10:30:00Z",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/appointments"
}
```

## Appointment Status Values

- `PENDING`: Newly created, waiting for approval
- `APPROVED`: Approved and employee assigned
- `REJECTED`: Rejected/cancelled
- `COMPLETED`: Service completed

## User Roles

- `CUSTOMER`: Can create and manage own appointments
- `EMPLOYEE`: Can view all appointments, assign themselves
- `ADMIN`: Full access to all appointments and admin functions
- `SUPER_ADMIN`: Full system access

## Important Notes for Frontend

1. **Authentication**: Store JWT token securely (localStorage/sessionStorage)
2. **Token Expiration**: Handle 401 responses by redirecting to login
3. **Time Format**: Use 24-hour format (HH:MM) for time fields
4. **Date Format**: Use ISO format (YYYY-MM-DD) for date fields
5. **Pagination**: Admin appointment list uses Spring Boot pagination format
6. **Anonymous Bookings**: CustomerName, email, phone required when no auth token
7. **Time Slots**: Check for 409 conflict when creating appointments
8. **Permissions**: Different users see different data based on roles
9. **Real-time**: Consider WebSocket for chat functionality
10. **Error Handling**: Always check response status and handle errors appropriately

## JavaScript Fetch Examples

### Login Example
```javascript
const login = async (email, password) => {
  try {
    const response = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password }),
    });
    
    if (response.ok) {
      const data = await response.json();
      localStorage.setItem('token', data.token);
      return data;
    } else {
      throw new Error('Login failed');
    }
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};
```

### Get My Appointments Example
```javascript
const getMyAppointments = async () => {
  try {
    const token = localStorage.getItem('token');
    const response = await fetch('http://localhost:8080/api/appointments/my', {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });
    
    if (response.ok) {
      return await response.json();
    } else if (response.status === 401) {
      // Token expired, redirect to login
      window.location.href = '/login';
    } else {
      throw new Error('Failed to fetch appointments');
    }
  } catch (error) {
    console.error('Error fetching appointments:', error);
    throw error;
  }
};
```

### Create Appointment Example
```javascript
const createAppointment = async (appointmentData) => {
  try {
    const token = localStorage.getItem('token');
    const response = await fetch('http://localhost:8080/api/appointments', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify(appointmentData),
    });
    
    if (response.ok) {
      return await response.json();
    } else if (response.status === 409) {
      throw new Error('Time slot is not available');
    } else {
      const errorData = await response.json();
      throw new Error(errorData.message || 'Failed to create appointment');
    }
  } catch (error) {
    console.error('Error creating appointment:', error);
    throw error;
  }
};
```