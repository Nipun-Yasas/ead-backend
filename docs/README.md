# EAD Backend - Appointment Management System

A Spring Boot REST API for managing vehicle service appointments with user authentication and role-based access control.

## Table of Contents
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
- [Database Schema](#database-schema)
- [Error Handling](#error-handling)
- [Testing](#testing)

## Features

- **User Management**: Registration, login, and role-based access (SUPER_ADMIN, ADMIN, EMPLOYEE, CUSTOMER)
- **Appointment Booking**: Create, view, update, and cancel vehicle service appointments
- **Employee Assignment**: Assign employees to appointments
- **Real-time Chat**: WebSocket-based messaging system
- **Dashboard Analytics**: Admin dashboard with statistics
- **Time Slot Management**: Prevent double booking of time slots
- **Vehicle Support**: Support for cars, motorcycles, trucks, and other vehicle types

## Technology Stack

- **Backend Framework**: Spring Boot 3.x
- **Database**: PostgreSQL
- **Authentication**: JWT (JSON Web Tokens)
- **Security**: Spring Security
- **Real-time Communication**: WebSocket
- **Build Tool**: Maven
- **Java Version**: 17+

## Getting Started

### Prerequisites
- Java 17 or higher
- PostgreSQL database
- Maven 3.6+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Nipun-Yasas/ead-backend.git
   cd ead-backend
   ```

2. **Configure database**
   
   Update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/your_database_name
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   
   # JWT Configuration
   jwt.secret=your_secret_key_here
   jwt.expiration=86400000
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   The API will be available at `http://localhost:8080`

## Authentication

The API uses JWT tokens for authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
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

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login user |

### Appointment Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/appointments` | Create new appointment | Yes |
| GET | `/api/appointments` | Get all appointments (paginated) | Yes (Admin/Employee) |
| GET | `/api/appointments/my` | Get current user's appointments | Yes |
| GET | `/api/appointments/{id}` | Get appointment by ID | Yes |
| PUT | `/api/appointments/{id}` | Update appointment | Yes |
| DELETE | `/api/appointments/{id}` | Delete appointment | Yes (Admin only) |
| POST | `/api/appointments/{id}/assign/{employeeId}` | Assign employee | Yes (Admin/Employee) |
| PUT | `/api/appointments/{id}/cancel` | Cancel appointment | Yes |

### Admin Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/admin/dashboard` | Get dashboard statistics | Yes (Admin) |

### Chat Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/chat/{appointmentId}` | Get chat messages | Yes |
| POST | `/api/chat/{appointmentId}` | Send message | Yes |

## Detailed API Documentation

### Create Appointment

**POST** `/api/appointments`

Create a new appointment. Can be used by authenticated users or for anonymous bookings.

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <token> (optional for anonymous booking)
```

**Request Body:**
```json
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

**Field Descriptions:**
- `date`: Appointment date (YYYY-MM-DD format)
- `time`: Appointment time (HH:MM format)
- `vehicleType`: Type of vehicle (car, motorcycle, truck, etc.)
- `vehicleNumber`: Vehicle registration number
- `service`: Service type (repair, maintenance, inspection, etc.)
- `instructions`: Special instructions or notes
- `userId`: Customer ID (optional, for admin/employee creating appointments)
- `customerName`: Customer name (for anonymous bookings)
- `customerEmail`: Customer email (for anonymous bookings)
- `customerPhone`: Customer phone (for anonymous bookings)

**Success Response (201 Created):**
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

**GET** `/api/appointments/my`

Retrieve all appointments for the currently authenticated user.

**Headers:**
```
Authorization: Bearer <token>
```

**Success Response (200 OK):**
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

### Update Appointment

**PUT** `/api/appointments/{id}`

Update an existing appointment. Users can only update their own appointments unless they have admin/employee role.

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <token>
```

**Request Body (all fields optional):**
```json
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

## Database Schema

### Users Table
- `id`: Primary key
- `name`: User full name
- `email`: User email (unique)
- `password`: Encrypted password
- `phone`: Phone number
- `role_id`: Foreign key to roles table

### Appointments Table
- `id`: Primary key
- `date`: Appointment date
- `time`: Appointment time
- `vehicle_type`: Type of vehicle
- `vehicle_number`: Vehicle registration number
- `service`: Service type
- `instructions`: Special instructions
- `status`: Appointment status (PENDING, APPROVED, REJECTED, COMPLETED)
- `customer_id`: Foreign key to users table
- `employee_id`: Foreign key to users table (assigned employee)
- `customer_name`: For anonymous bookings
- `customer_email`: For anonymous bookings
- `customer_phone`: For anonymous bookings

### Appointment Status Values
- `PENDING`: Newly created appointment waiting for approval
- `APPROVED`: Appointment approved and employee assigned
- `REJECTED`: Appointment rejected
- `COMPLETED`: Service completed

## Error Handling

The API returns standard HTTP status codes and JSON error responses:

### Common Error Responses

**400 Bad Request**
```json
{
  "timestamp": "2024-11-05T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/appointments"
}
```

**401 Unauthorized**
```json
{
  "timestamp": "2024-11-05T10:30:00Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is missing or invalid",
  "path": "/api/appointments/my"
}
```

**403 Forbidden**
```json
{
  "timestamp": "2024-11-05T10:30:00Z",
  "status": 403,
  "error": "Forbidden",
  "message": "You don't have permission to access this resource",
  "path": "/api/appointments/1"
}
```

**404 Not Found**
```json
{
  "timestamp": "2024-11-05T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Appointment not found with id: 1",
  "path": "/api/appointments/1"
}
```

**409 Conflict**
```json
{
  "timestamp": "2024-11-05T10:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "The selected time slot is not available",
  "path": "/api/appointments"
}
```

## Business Rules

1. **Time Slot Management**: No two appointments can be booked for the same date and time
2. **User Permissions**: 
   - Customers can only view/modify their own appointments
   - Employees can view all appointments and assign themselves
   - Admins have full access to all appointments
3. **Appointment Status Flow**: PENDING → APPROVED → COMPLETED
4. **Employee Assignment**: Only admins/employees can assign employees to appointments
5. **Anonymous Bookings**: Appointments can be created without authentication by providing customer contact details

## Testing

### Running Tests
```bash
mvn test
```

### Example Test Cases
- User registration and login
- Appointment creation with valid/invalid data
- Time slot conflict validation
- Permission-based access control
- JWT token validation

## Development

### Code Structure
```
src/
├── main/
│   ├── java/Backend/
│   │   ├── controller/     # REST controllers
│   │   ├── service/        # Business logic
│   │   ├── repository/     # Data access layer
│   │   ├── entity/         # JPA entities
│   │   ├── dto/           # Data transfer objects
│   │   ├── config/        # Configuration classes
│   │   └── security/      # Security components
│   └── resources/
│       └── application.properties
└── test/                  # Test classes
```

### Adding New Features
1. Create/update entities in `entity/` package
2. Add repository methods in `repository/` package
3. Implement business logic in `service/` package
4. Create REST endpoints in `controller/` package
5. Add DTOs in `dto/` package for request/response objects

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Contact

For questions or support, please contact the development team.