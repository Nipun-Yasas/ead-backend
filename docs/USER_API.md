# User Management API

## Get Employees Endpoint
`GET /api/users/employees`

### Description
Retrieve a list of all employees in the system with their task statistics. Returns only enabled employees with their basic information excluding sensitive data like passwords, along with a breakdown of their current workload by appointment status.

### Authorization
**Public** - No authentication required

### Request
No request body or parameters required.

### Response

#### Success (200 OK)
```json
[
  {
    "id": 3,
    "fullName": "John Technician",
    "email": "john.tech@autocare.com",
    "phone": "+1234567890",
    "role": "EMPLOYEE",
    "enabled": true,
    "taskStats": {
      "pending": 2,
      "approved": 1,
      "inProgress": 3,
      "completed": 15,
      "ongoing": 1,
      "total": 22
    }
  },
  {
    "id": 5,
    "fullName": "Sarah Mechanic",
    "email": "sarah.mech@autocare.com",
    "phone": "+1234567891",
    "role": "EMPLOYEE",
    "enabled": true,
    "taskStats": {
      "pending": 0,
      "approved": 2,
      "inProgress": 5,
      "completed": 28,
      "ongoing": 2,
      "total": 37
    }
  }
]
```

#### Response Fields
- `id` (Long) - Unique identifier of the employee
- `fullName` (String) - Full name of the employee
- `email` (String) - Email address of the employee
- `phone` (String, nullable) - Phone number of the employee
- `role` (String) - User role (will be "EMPLOYEE")
- `enabled` (boolean) - Account status
- `taskStats` (Object) - Task statistics for the employee
  - `pending` (Long) - Number of pending appointments assigned to this employee
  - `approved` (Long) - Number of approved appointments assigned to this employee
  - `inProgress` (Long) - Number of appointments currently in progress
  - `completed` (Long) - Number of completed appointments
  - `ongoing` (Long) - Number of ongoing appointments
  - `total` (Long) - Total number of appointments assigned (sum of all statuses)

#### Empty Response (200 OK)
```json
[]
```
Returns an empty array if no employees are found.

### Example Usage

#### cURL
```bash
curl -X GET http://localhost:8080/api/users/employees
```

#### JavaScript/Fetch
```javascript
fetch('http://localhost:8080/api/users/employees')
  .then(response => response.json())
  .then(employees => {
    console.log('Employees:', employees);
    // Use employees for dropdown selection, assignment, etc.
  })
  .catch(error => console.error('Error:', error));
```

#### Axios
```javascript
import axios from 'axios';

const getEmployees = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/users/employees');
    return response.data;
  } catch (error) {
    console.error('Error fetching employees:', error);
    throw error;
  }
};
```

### Use Cases
- **Task Allocation**: Select an employee to assign to an appointment with workload visibility
- **Employee List**: Display all available technicians with their current workload
- **Dropdown Population**: Populate employee selection dropdowns showing availability (e.g., "John Technician (3 in progress)")
- **Workload Balancing**: Assign new tasks to employees with fewer active appointments

### Workload Display Examples

**Simple Display:**
```
John Technician - 3 tasks in progress
Sarah Mechanic - 5 tasks in progress
```

**Detailed Display:**
```
John Technician
  In Progress: 3
  Approved: 1
  Total: 22 (15 completed)

Sarah Mechanic  
  In Progress: 5
  Approved: 2
  Total: 37 (28 completed)
```

**Color-coded Status:**
- 🟡 Pending: 2
- 🟢 Approved: 1
- 🔵 In Progress: 3
- ✅ Completed: 15
- 🔄 Ongoing: 1

---

## Get Customers Endpoint
`GET /api/users/customers`

### Description
Retrieve a list of all customers in the system. Returns only enabled customers with their basic information excluding sensitive data like passwords.

### Authorization
**Public** - No authentication required

### Request
No request body or parameters required.

### Response

#### Success (200 OK)
```json
[
  {
    "id": 10,
    "fullName": "Jane Doe",
    "email": "jane.doe@email.com",
    "phone": "+1987654321",
    "role": "CUSTOMER",
    "enabled": true
  },
  {
    "id": 15,
    "fullName": "Bob Smith",
    "email": "bob.smith@email.com",
    "phone": "+1987654322",
    "role": "CUSTOMER",
    "enabled": true
  }
]
```

#### Response Fields
- `id` (Long) - Unique identifier of the customer
- `fullName` (String) - Full name of the customer
- `email` (String) - Email address of the customer
- `phone` (String, nullable) - Phone number of the customer
- `role` (String) - User role (will be "CUSTOMER")
- `enabled` (boolean) - Account status

#### Empty Response (200 OK)
```json
[]
```
Returns an empty array if no customers are found.

### Example Usage

#### cURL
```bash
curl -X GET http://localhost:8080/api/users/customers
```

#### JavaScript/Fetch
```javascript
fetch('http://localhost:8080/api/users/customers')
  .then(response => response.json())
  .then(customers => {
    console.log('Customers:', customers);
    // Use customers for appointment creation, customer selection, etc.
  })
  .catch(error => console.error('Error:', error));
```

#### Axios
```javascript
import axios from 'axios';

const getCustomers = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/users/customers');
    return response.data;
  } catch (error) {
    console.error('Error fetching customers:', error);
    throw error;
  }
};
```

#### React Component Example
```javascript
import { useState, useEffect } from 'react';
import axios from 'axios';

const CustomerSelector = () => {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchCustomers = async () => {
      try {
        const response = await axios.get('http://localhost:8080/api/users/customers');
        setCustomers(response.data);
      } catch (error) {
        console.error('Error fetching customers:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchCustomers();
  }, []);

  if (loading) return <div>Loading customers...</div>;

  return (
    <select>
      <option value="">Select a customer</option>
      {customers.map(customer => (
        <option key={customer.id} value={customer.id}>
          {customer.fullName} ({customer.email})
        </option>
      ))}
    </select>
  );
};
```

### Use Cases
- **Appointment Creation**: Select a customer when creating appointments (admin view)
- **Customer List**: Display all registered customers
- **Customer Management**: View and manage customer accounts
- **Dropdown Population**: Populate customer selection dropdowns in forms

---

## Notes

### Security
- Both endpoints are currently **public** (no authentication required) for testing purposes
- In production, you may want to add role-based access control:
  - Employees endpoint: Should be accessible by ADMIN, SUPER_ADMIN, EMPLOYEE
  - Customers endpoint: Should be accessible by ADMIN, SUPER_ADMIN

### Filtering
- Both endpoints automatically filter out **disabled** users
- Only users with `enabled: true` are returned

### Data Privacy
- **Passwords are excluded** from the response
- Only essential user information is returned
- Phone numbers may be null if not provided during registration

### Performance
- These endpoints return all users of the specified role
- For large datasets, consider adding pagination parameters:
  - `?page=0&size=20`
  - `?sort=fullName,asc`

### Future Enhancements
Consider adding these query parameters:
```
GET /api/users/employees?search=john
GET /api/users/customers?enabled=true
GET /api/users/employees?page=0&size=10&sort=fullName,asc
```

---

## Error Handling

### Invalid Role Name (in UserService)
If the service is called with an invalid role name:
```json
{
  "message": "Invalid role name: INVALID. Valid roles are: SUPER_ADMIN, ADMIN, EMPLOYEE, CUSTOMER"
}
```

### Server Error (500)
```json
{
  "timestamp": "2025-11-07T12:00:00.000+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "path": "/api/users/employees"
}
```

---

## Testing

### Test with cURL

**Get Employees:**
```bash
curl -X GET http://localhost:8080/api/users/employees \
  -H "Content-Type: application/json" | jq
```

**Get Customers:**
```bash
curl -X GET http://localhost:8080/api/users/customers \
  -H "Content-Type: application/json" | jq
```

### Test with Postman
1. **Method**: GET
2. **URL**: `http://localhost:8080/api/users/employees` or `http://localhost:8080/api/users/customers`
3. **Headers**: None required
4. **Body**: None
5. **Expected Response**: 200 OK with JSON array

### Integration with Frontend

**Example: Populate Employee Dropdown in Appointment Assignment Form**
```javascript
// Fetch employees when component mounts
const [employees, setEmployees] = useState([]);

useEffect(() => {
  axios.get('http://localhost:8080/api/users/employees')
    .then(response => setEmployees(response.data))
    .catch(error => console.error('Error:', error));
}, []);

// Render dropdown with workload info
<select name="employeeId">
  <option value="">Select Employee</option>
  {employees.map(emp => (
    <option key={emp.id} value={emp.id}>
      {emp.fullName} - {emp.email} 
      ({emp.taskStats.inProgress} in progress, {emp.taskStats.total} total)
    </option>
  ))}
</select>

// Or sort by workload (least busy first)
<select name="employeeId">
  <option value="">Select Employee (sorted by workload)</option>
  {employees
    .sort((a, b) => a.taskStats.inProgress - b.taskStats.inProgress)
    .map(emp => (
      <option key={emp.id} value={emp.id}>
        {emp.fullName} - {emp.taskStats.inProgress} active tasks
      </option>
    ))
  }
</select>
```

**Example: Display Employee Cards with Statistics**
```javascript
const EmployeeList = () => {
  const [employees, setEmployees] = useState([]);

  useEffect(() => {
    axios.get('http://localhost:8080/api/users/employees')
      .then(response => setEmployees(response.data))
      .catch(error => console.error('Error:', error));
  }, []);

  return (
    <div className="employee-grid">
      {employees.map(emp => (
        <div key={emp.id} className="employee-card">
          <h3>{emp.fullName}</h3>
          <p>{emp.email}</p>
          
          <div className="task-stats">
            <div className="stat">
              <span className="label">In Progress:</span>
              <span className="value">{emp.taskStats.inProgress}</span>
            </div>
            <div className="stat">
              <span className="label">Approved:</span>
              <span className="value">{emp.taskStats.approved}</span>
            </div>
            <div className="stat">
              <span className="label">Completed:</span>
              <span className="value">{emp.taskStats.completed}</span>
            </div>
            <div className="stat">
              <span className="label">Total:</span>
              <span className="value">{emp.taskStats.total}</span>
            </div>
          </div>
          
          <button onClick={() => assignTask(emp.id)}>
            Assign Task
          </button>
        </div>
      ))}
    </div>
  );
};
```

**Example: Populate Customer Dropdown in Admin Appointment Creation**
```javascript
// Fetch customers when component mounts
const [customers, setCustomers] = useState([]);

useEffect(() => {
  axios.get('http://localhost:8080/api/users/customers')
    .then(response => setCustomers(response.data))
    .catch(error => console.error('Error:', error));
}, []);

// Render dropdown
<select name="customerId">
  <option value="">Select Customer</option>
  {customers.map(customer => (
    <option key={customer.id} value={customer.id}>
      {customer.fullName} - {customer.email}
      {customer.phone && ` - ${customer.phone}`}
    </option>
  ))}
</select>
```
