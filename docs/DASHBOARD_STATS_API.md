# Dashboard Statistics API

## Get Dashboard Stats Endpoint
`GET /api/dashboard/stats`

## Description
Retrieve comprehensive dashboard statistics for the admin panel. This endpoint provides an overview of all appointments, services, employee workload, and trends.

## Authorization
Required roles: `SUPER_ADMIN`, `ADMIN`, or `EMPLOYEE`

## Request
No request body or parameters required.

**Headers:**
```
Authorization: Bearer {JWT_TOKEN}
```

## Response

### Success (200 OK)
```json
{
  "totalServices": 1240,
  "completedServices": 980,
  "inProgressServices": 120,
  "pendingServices": 140,
  "todayAppointments": 18,
  "cancelledServices": 25,
  "servicesByStatus": [
    {
      "status": "COMPLETED",
      "count": 980
    },
    {
      "status": "IN_PROGRESS",
      "count": 120
    },
    {
      "status": "PENDING",
      "count": 140
    },
    {
      "status": "APPROVE",
      "count": 75
    },
    {
      "status": "REJECT",
      "count": 25
    }
  ],
  "monthlyTrend": [
    {
      "month": "Jan",
      "value": 80
    },
    {
      "month": "Feb",
      "value": 95
    },
    {
      "month": "Mar",
      "value": 110
    },
    {
      "month": "Apr",
      "value": 120
    },
    {
      "month": "May",
      "value": 130
    },
    {
      "month": "Jun",
      "value": 140
    },
    {
      "month": "Jul",
      "value": 150
    },
    {
      "month": "Aug",
      "value": 135
    },
    {
      "month": "Sep",
      "value": 125
    },
    {
      "month": "Oct",
      "value": 140
    },
    {
      "month": "Nov",
      "value": 155
    },
    {
      "month": "Dec",
      "value": 160
    }
  ],
  "employeeWorkload": [
    {
      "employeeName": "A. Silva",
      "taskCount": 24
    },
    {
      "employeeName": "B. Perera",
      "taskCount": 18
    },
    {
      "employeeName": "C. Fernando",
      "taskCount": 15
    },
    {
      "employeeName": "D. Kumar",
      "taskCount": 12
    }
  ],
  "upcomingAppointments": [
    {
      "customerName": "Michael Scott",
      "vehicleModel": "Toyota Corolla",
      "serviceType": "Oil Change",
      "appointmentDate": "2025-11-07T10:00:00"
    },
    {
      "customerName": "Pam Beesly",
      "vehicleModel": "Honda Civic",
      "serviceType": "Brake Service",
      "appointmentDate": "2025-11-08T14:30:00"
    },
    {
      "customerName": "Jim Halpert",
      "vehicleModel": "Ford Ranger",
      "serviceType": "Inspection",
      "appointmentDate": "2025-11-09T09:00:00"
    }
  ]
}
```

### Response Fields

#### Top Level Fields
- `totalServices` (Long) - Total number of all appointments/services
- `completedServices` (Long) - Count of completed appointments
- `inProgressServices` (Long) - Count of appointments currently in progress
- `pendingServices` (Long) - Count of pending appointments awaiting approval
- `todayAppointments` (Long) - Count of appointments scheduled for today
- `cancelledServices` (Long) - Count of rejected/cancelled appointments

#### servicesByStatus (Array)
Breakdown of appointments by each status:
- `status` (String) - Status name (PENDING, APPROVE, REJECT, IN_PROGRESS, COMPLETED)
- `count` (Long) - Number of appointments with this status

#### monthlyTrend (Array)
Appointment count for the last 12 months:
- `month` (String) - Short month name (Jan, Feb, Mar, etc.)
- `value` (Long) - Number of appointments in that month

#### employeeWorkload (Array)
Top 4 employees by number of assigned tasks:
- `employeeName` (String) - Full name of the employee
- `taskCount` (Long) - Number of appointments assigned to this employee

#### upcomingAppointments (Array)
Next 5 upcoming appointments:
- `customerName` (String) - Customer's full name
- `vehicleModel` (String) - Vehicle type/model
- `serviceType` (String) - Type of service requested
- `appointmentDate` (String) - ISO 8601 date-time string

## Example Usage

### cURL
```bash
curl -X GET http://localhost:8080/api/dashboard/stats \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

### JavaScript/Fetch
```javascript
const getDashboardStats = async () => {
  try {
    const response = await fetch('http://localhost:8080/api/dashboard/stats', {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    });
    
    if (response.ok) {
      const stats = await response.json();
      console.log('Dashboard Stats:', stats);
      return stats;
    }
  } catch (error) {
    console.error('Error fetching dashboard stats:', error);
  }
};
```

### Axios
```javascript
import axios from 'axios';

const fetchDashboardStats = async () => {
  try {
    const response = await axios.get(
      'http://localhost:8080/api/dashboard/stats',
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );
    return response.data;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
};
```

### React Component Example
```typescript
import { useState, useEffect } from 'react';
import axios from 'axios';

interface DashboardStats {
  totalServices: number;
  completedServices: number;
  inProgressServices: number;
  pendingServices: number;
  todayAppointments: number;
  cancelledServices: number;
  servicesByStatus: { status: string; count: number }[];
  monthlyTrend: { month: string; value: number }[];
  employeeWorkload: { employeeName: string; taskCount: number }[];
  upcomingAppointments: {
    customerName: string;
    vehicleModel: string;
    serviceType: string;
    appointmentDate: string;
  }[];
}

const DashboardPage = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const token = localStorage.getItem('token');
        const response = await axios.get(
          'http://localhost:8080/api/dashboard/stats',
          {
            headers: { Authorization: `Bearer ${token}` }
          }
        );
        setStats(response.data);
      } catch (error) {
        console.error('Error fetching stats:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, []);

  if (loading) return <div>Loading...</div>;
  if (!stats) return <div>No data available</div>;

  return (
    <div>
      <h1>Dashboard</h1>
      
      {/* Stats Cards */}
      <div className="stats-grid">
        <StatCard 
          title="Total Services" 
          value={stats.totalServices} 
          icon="📊"
        />
        <StatCard 
          title="Completed" 
          value={stats.completedServices} 
          icon="✅"
        />
        <StatCard 
          title="In Progress" 
          value={stats.inProgressServices} 
          icon="⚙️"
        />
        <StatCard 
          title="Pending" 
          value={stats.pendingServices} 
          icon="⏳"
        />
        <StatCard 
          title="Today's Appointments" 
          value={stats.todayAppointments} 
          icon="📅"
        />
        <StatCard 
          title="Cancelled" 
          value={stats.cancelledServices} 
          icon="❌"
        />
      </div>

      {/* Charts */}
      <div className="charts">
        <MonthlyTrendChart data={stats.monthlyTrend} />
        <StatusPieChart data={stats.servicesByStatus} />
        <EmployeeWorkloadChart data={stats.employeeWorkload} />
      </div>

      {/* Upcoming Appointments */}
      <UpcomingAppointmentsList appointments={stats.upcomingAppointments} />
    </div>
  );
};
```

## Error Responses

### Unauthorized (401)
```json
{
  "timestamp": "2025-11-07T12:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/dashboard/stats"
}
```

### Forbidden (403)
```json
{
  "timestamp": "2025-11-07T12:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/dashboard/stats"
}
```

## Data Calculation Details

### Total Services
- Counts all appointments regardless of status

### Completed Services
- Counts appointments with status `COMPLETED`

### In Progress Services
- Counts appointments with status `IN_PROGRESS`

### Pending Services
- Counts appointments with status `PENDING`

### Today's Appointments
- Counts appointments scheduled for current date
- Includes all statuses except REJECT and COMPLETED

### Cancelled Services
- Counts appointments with status `REJECT`

### Monthly Trend
- Shows appointment count for last 12 months
- Calculated from appointment creation date
- Months displayed in short format (Jan, Feb, Mar, etc.)

### Employee Workload
- Shows top 4 employees by number of assigned appointments
- Includes all appointments assigned to each employee
- Sorted by task count in descending order

### Upcoming Appointments
- Shows next 5 upcoming appointments
- Filters out:
  - Past appointments
  - Rejected appointments
  - Completed appointments
- Sorted by date and time (earliest first)

## Notes

- All counts are calculated in real-time from the database
- Statistics are based on the Appointment entity
- Employee workload only includes appointments with assigned employees
- Upcoming appointments are limited to 5 items
- Monthly trend covers the last 12 months from current date
- All data is fresh on every request (no caching)

## Use Cases

1. **Admin Dashboard**: Display overview of business operations
2. **Performance Monitoring**: Track completion rates and trends
3. **Resource Planning**: View employee workload distribution
4. **Appointment Management**: See upcoming appointments at a glance
5. **Business Analytics**: Analyze monthly trends and patterns
