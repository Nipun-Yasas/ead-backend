# Appointment Filtering API

## Get Appointments by Employee ID
`GET /api/appointments/employee/{employeeId}`

### Description
Retrieve all appointments assigned to a specific employee. This endpoint returns all appointments where the specified employee is assigned, regardless of status.

### Authorization
Required roles: `SUPER_ADMIN`, `ADMIN`, or `EMPLOYEE`

### Path Parameters
- `employeeId` (Long, required) - The ID of the employee

### Response

#### Success (200 OK)
```json
[
  {
    "id": 1,
    "date": "2025-11-10",
    "time": "10:00:00",
    "vehicleType": "Toyota Camry",
    "vehicleNumber": "ABC-1234",
    "service": "Oil Change",
    "instructions": "Check brakes too",
    "status": "IN_PROGRESS",
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
    "updatedAt": "2025-11-07T09:15:00"
  },
  {
    "id": 5,
    "date": "2025-11-12",
    "time": "14:00:00",
    "vehicleType": "Honda Civic",
    "vehicleNumber": "XYZ-5678",
    "service": "Full Service",
    "instructions": null,
    "status": "APPROVE",
    "customer": {
      "id": 8,
      "name": "Jane Smith",
      "email": "jane@example.com",
      "phone": "+1234567891"
    },
    "employee": {
      "id": 3,
      "name": "Mike Technician",
      "email": "mike@autocare.com"
    },
    "createdAt": "2025-11-07T11:00:00",
    "updatedAt": "2025-11-07T11:00:00"
  }
]
```

#### Error Responses

**Employee Not Found (400 Bad Request)**
```json
{
  "message": "Employee not found with id: 999"
}
```

**Unauthorized (403 Forbidden)**
```json
{
  "timestamp": "2025-11-07T12:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/appointments/employee/3"
}
```

### Example Usage

#### cURL
```bash
curl -X GET http://localhost:8080/api/appointments/employee/3 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### JavaScript/Fetch
```javascript
const getEmployeeAppointments = async (employeeId, token) => {
  const response = await fetch(
    `http://localhost:8080/api/appointments/employee/${employeeId}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  
  if (!response.ok) {
    throw new Error('Failed to fetch employee appointments');
  }
  
  return await response.json();
};

// Usage
getEmployeeAppointments(3, token)
  .then(appointments => {
    console.log('Employee appointments:', appointments);
    // Display employee's workload, schedule, etc.
  })
  .catch(error => console.error('Error:', error));
```

#### Axios
```javascript
import axios from 'axios';

const getEmployeeAppointments = async (employeeId) => {
  try {
    const response = await axios.get(
      `http://localhost:8080/api/appointments/employee/${employeeId}`,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }
    );
    return response.data;
  } catch (error) {
    console.error('Error fetching employee appointments:', error);
    throw error;
  }
};
```

### Use Cases
- **Employee Dashboard**: Show all appointments assigned to logged-in employee
- **Workload View**: Display employee's current workload and schedule
- **Task Management**: List tasks for a specific technician
- **Performance Tracking**: Analyze employee's completed appointments

---

## Get Appointments by Customer ID
`GET /api/appointments/customer/{customerId}`

### Description
Retrieve all appointments for a specific customer. This endpoint returns all appointments created by the specified customer, regardless of status.

### Authorization
Required roles: `SUPER_ADMIN`, `ADMIN`, or `EMPLOYEE`

### Path Parameters
- `customerId` (Long, required) - The ID of the customer

### Response

#### Success (200 OK)
```json
[
  {
    "id": 2,
    "date": "2025-11-08",
    "time": "09:00:00",
    "vehicleType": "BMW X5",
    "vehicleNumber": "BMW-9876",
    "service": "Brake Repair",
    "instructions": "Grinding noise when braking",
    "status": "PENDING",
    "customer": {
      "id": 10,
      "name": "Sarah Johnson",
      "email": "sarah@example.com",
      "phone": "+1234567892"
    },
    "employee": null,
    "createdAt": "2025-11-07T08:30:00",
    "updatedAt": "2025-11-07T08:30:00"
  },
  {
    "id": 7,
    "date": "2025-10-15",
    "time": "15:00:00",
    "vehicleType": "BMW X5",
    "vehicleNumber": "BMW-9876",
    "service": "Oil Change",
    "instructions": null,
    "status": "COMPLETED",
    "customer": {
      "id": 10,
      "name": "Sarah Johnson",
      "email": "sarah@example.com",
      "phone": "+1234567892"
    },
    "employee": {
      "id": 4,
      "name": "Tom Mechanic",
      "email": "tom@autocare.com"
    },
    "createdAt": "2025-10-14T10:00:00",
    "updatedAt": "2025-10-15T16:30:00"
  }
]
```

#### Error Responses

**Customer Not Found (400 Bad Request)**
```json
{
  "message": "Customer not found with id: 999"
}
```

**Unauthorized (403 Forbidden)**
```json
{
  "timestamp": "2025-11-07T12:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/appointments/customer/10"
}
```

### Example Usage

#### cURL
```bash
curl -X GET http://localhost:8080/api/appointments/customer/10 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### JavaScript/Fetch
```javascript
const getCustomerAppointments = async (customerId, token) => {
  const response = await fetch(
    `http://localhost:8080/api/appointments/customer/${customerId}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  
  if (!response.ok) {
    throw new Error('Failed to fetch customer appointments');
  }
  
  return await response.json();
};

// Usage
getCustomerAppointments(10, token)
  .then(appointments => {
    console.log('Customer appointments:', appointments);
    // Display customer history, upcoming appointments, etc.
  })
  .catch(error => console.error('Error:', error));
```

#### Axios
```javascript
import axios from 'axios';

const getCustomerAppointments = async (customerId) => {
  try {
    const response = await axios.get(
      `http://localhost:8080/api/appointments/customer/${customerId}`,
      {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      }
    );
    return response.data;
  } catch (error) {
    console.error('Error fetching customer appointments:', error);
    throw error;
  }
};
```

#### React Component Example
```javascript
import { useState, useEffect } from 'react';
import axios from 'axios';

const CustomerAppointmentHistory = ({ customerId }) => {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchAppointments = async () => {
      try {
        const response = await axios.get(
          `http://localhost:8080/api/appointments/customer/${customerId}`,
          {
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          }
        );
        setAppointments(response.data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchAppointments();
  }, [customerId]);

  if (loading) return <div>Loading appointments...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      <h2>Customer Appointment History</h2>
      {appointments.length === 0 ? (
        <p>No appointments found</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Date</th>
              <th>Time</th>
              <th>Service</th>
              <th>Vehicle</th>
              <th>Status</th>
              <th>Technician</th>
            </tr>
          </thead>
          <tbody>
            {appointments.map(apt => (
              <tr key={apt.id}>
                <td>{apt.date}</td>
                <td>{apt.time}</td>
                <td>{apt.service}</td>
                <td>{apt.vehicleType} ({apt.vehicleNumber})</td>
                <td>{apt.status}</td>
                <td>{apt.employee?.name || 'Not assigned'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};
```

### Use Cases
- **Customer Profile**: Display customer's appointment history
- **Service History**: Show previous services for vehicle maintenance tracking
- **Customer Support**: View customer's past and upcoming appointments
- **Analytics**: Track customer service patterns and preferences

---

## Combined Use Case Example

### Employee Workload Dashboard with Statistics

```javascript
import { useState, useEffect } from 'react';
import axios from 'axios';

const EmployeeWorkloadDashboard = ({ employeeId }) => {
  const [appointments, setAppointments] = useState([]);
  const [stats, setStats] = useState({
    pending: 0,
    approve: 0,
    inProgress: 0,
    completed: 0,
    total: 0
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        // Fetch all appointments for this employee
        const response = await axios.get(
          `http://localhost:8080/api/appointments/employee/${employeeId}`,
          {
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
          }
        );
        
        const appointments = response.data;
        setAppointments(appointments);

        // Calculate statistics
        const stats = {
          pending: appointments.filter(a => a.status === 'PENDING').length,
          approve: appointments.filter(a => a.status === 'APPROVE').length,
          inProgress: appointments.filter(a => a.status === 'IN_PROGRESS').length,
          completed: appointments.filter(a => a.status === 'COMPLETED').length,
          total: appointments.length
        };
        setStats(stats);
      } catch (error) {
        console.error('Error:', error);
      }
    };

    fetchData();
  }, [employeeId]);

  return (
    <div className="employee-dashboard">
      <h2>My Workload</h2>
      
      {/* Statistics Cards */}
      <div className="stats-grid">
        <div className="stat-card">
          <h3>In Progress</h3>
          <p className="stat-number">{stats.inProgress}</p>
        </div>
        <div className="stat-card">
          <h3>Approved</h3>
          <p className="stat-number">{stats.approve}</p>
        </div>
        <div className="stat-card">
          <h3>Completed</h3>
          <p className="stat-number">{stats.completed}</p>
        </div>
        <div className="stat-card">
          <h3>Total</h3>
          <p className="stat-number">{stats.total}</p>
        </div>
      </div>

      {/* Appointments List */}
      <div className="appointments-list">
        <h3>Current Appointments</h3>
        {appointments
          .filter(a => a.status !== 'COMPLETED')
          .map(appointment => (
            <div key={appointment.id} className="appointment-card">
              <h4>{appointment.customer.name}</h4>
              <p>{appointment.vehicleType} - {appointment.vehicleNumber}</p>
              <p>{appointment.service}</p>
              <p>Date: {appointment.date} {appointment.time}</p>
              <span className={`status-badge ${appointment.status}`}>
                {appointment.status}
              </span>
            </div>
          ))}
      </div>
    </div>
  );
};
```

---

## Notes

### Response Data
- Both endpoints return **all** appointments for the specified user
- No filtering by status - returns appointments in all states
- Sorted by creation date (most recent first)
- Employee can be `null` if not assigned yet

### Security
- Both endpoints require authentication
- Restricted to ADMIN, SUPER_ADMIN, and EMPLOYEE roles
- Customers cannot directly access these endpoints

### Performance
- Returns complete appointment list (no pagination)
- Consider adding pagination for users with many appointments
- Use status filtering on the frontend if needed

### Related Endpoints
- `GET /api/appointments/my` - Get current user's appointments (customer view)
- `GET /api/appointments/status/{status}` - Filter all appointments by status
- `GET /api/users/employees` - Get list of all employees with task statistics
