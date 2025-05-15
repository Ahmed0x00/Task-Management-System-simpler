
## 🧩 **Data Models**

Here’s a well-thought set of Java-style class models (no code, just structure).

---

### 1. **User**

* `id`
* `username`
* `password`
* `role` (enum: ADMIN, EMPLOYEE, LEADER)
* `employeeId` (nullable, if the user is also an employee)

---

### 2. **Employee**

* `id`
* `name`
* `email`
* `phone`
* `employeeTypeId`

---

### 3. **EmployeeType**

* `id`
* `title`

---

### 4. **Task**

* `id`
* `code`
* `title`
* `description`
* `assignedEmployeeId`
* `projectId`
* `phase` (enum: PENDING, UNDER\_WORK, TEST, EVALUATION, CANCELED, etc.)
* `priority` (LOW, MEDIUM, HIGH)
* `creatorUserId`
* `startDate`
* `endDate`
* `estimatedHours`

---

### 5. **TaskLog**

* `id`
* `taskId`
* `employeeId`
* `fromTime`
* `toTime`
* `actualHours` (can be derived)

---

### 6. **Project**

* `id`
* `name`
* `description`
* `startDate`
* `endDate`

### Customer
* `id`
* `name`
* `PhoneNumber`

---

### 7. **TimeCard**

* `id`
* `employeeId`
* `date`
* `checkIn`
* `checkOut`


---

### 10. **LeaveType**

* `id`
* `title`
* `description`

---

### 11. **Request**

```java
public class Request {
    private String id;
    private String employeeId;
    private RequestType type; // MISSION, PERMISSION, LEAVE
    private String title; // Optional for MISSION & PERMISSION
    private String description; // Optional
    private LocalDate date; // For MISSION or PERMISSION
    private LocalDate startDate; // For LEAVE (and optionally MISSION)
    private LocalDate endDate; // For LEAVE (and optionally MISSION)
    private String leaveTypeId; // Only used if type == LEAVE
    private RequestStatus status; // PENDING, APPROVED, REJECTED
}

```

---

## 🔄 **Controllers + Logic Responsibility**

### 1. **AuthController**

* `login(username, password)` – validates user and loads role
* `logout()`

---

### 2. **UserController (Admin only)**

* `add/update/delete User`

---

### 3. **EmployeeController (Admin only)**

* `add/update/delete Employee`

---

### 4. **EmployeeTypeController (Admin only)**

* `add/update/delete Employee types`

---

### 5. **TaskController**

* `create/update/delete task` – Leaders only
* `reassign task`, `change phase`, `update priority`, etc.
* `view task (by ID or list)` – Filters: employee role
* `calendarView(employeeId)` – show tasks by phase/date
* `getMyTasks()` – filter by logged-in employee

---

### 6. **TaskLogController**

* `add log entry (employee only)`
* `view logs (leader can view all, employee only theirs)`

---

### 7. **ProjectController**

* `add/update/delete project or customer` (Admin)

---

### 8. **TimeCardController**

* `record checkIn/checkOut`
* `view own attendance`

---

### 9. **RequestController**

Handles:

* **MissionRequest**
* **PermissionRequest**
* **LeaveRequest**

Logic:

* `submitRequest(type, data)`
* `approve/disapprove (only Leader or Admin)`
* `view own requests`
* `view team requests` (Leader)

---

## 🎯 Notes and Assumptions

* **Only leaders** (or admins) can approve/reject requests. This should be enforced by `role` checks.
* **Data stored in JSON**: Design a `DataManager<T>` interface per entity with methods like `saveAll`, `loadAll`, `findById`, etc.
* **Enums** should be used for: Roles, Task Phases, Leave Status, Request Status, Priority.
* **Permissions**: Always use the role from `loggedInUser` to determine visibility and action availability.

Excellent foundation laid. Now shifting to **UI/UX architecture mode** 🧭

Let’s map the **data models + controller logic** into all required **UI pages (views/screens)**. Since you’re using JSON backend and Java OOP (likely with Swing, JavaFX, or web frontend later), this list is framework-agnostic.

---

## 🖥️ **Complete List of UI Pages (Views)**


### 🔐 **Authentication Module**

1. **Login Page**

   * Username / password
   * Error handling
   * Redirects based on role

2. **Logout Button**

   * Visible in header/nav bar

---

### 🧑‍💼 **Admin Panel (Role: ADMIN)**

3. **Dashboard (Admin Home)**

   * Quick links to manage users, employees, projects, etc.
   * Stats: active users, pending requests, total employees

4. **Manage Users Page**

   * List of users (username, role)
   * Add / Edit / Delete User

5. **Manage Employees Page**

   * Employees list
   * Add / Edit / Delete Employee

3. **Manage Employee Types Page**

   * Add / Edit / Delete Employee Types

7. **Manage Projects Page**

   * Add / Edit / Delete Projects

8. **Manage Customers Page**

   * Add / Edit / Delete Customers

9. **Manage Tasks Phases**

   * Add / Edit / Delete Tasks Phases

---

### 📋 **Task Management (Role: EMPLOYEE / LEADER / ADMIN)**

9. **Task List Page**

   * For Employees: only their tasks
   * For Leaders/Admin: full access → Create/Edit/Delete Task 

10. **Task Calendar View**

    * Weekly/monthly calendar per employee (Leaders can toggle between all)

11. **Task Logs Page**

    * Employee: log time on task
    * Leader: view logs by employee/task/date

---

### 🕒 **Attendance and Time Management (Role: EMPLOYEE)**

14. **Timecard Page**

* Check-in / Check-out buttons
* View past attendance history

---

### 🧾 **Request Management (Leave, Permission, Mission)**

15. **Submit Request Page**

* Form to create: Leave / Permission / Mission request
* Select type + dynamic fields

16. **My Requests Page**

* View all personal requests + statuses

15. **Requests Page (Leader only)**

* Approve / Reject pending requests
* View by employee

15. **Manage Leave Types Page (Admin/Leader)**
* Approve / Reject pending requests


# **Requests**
### **Differences**

|Type|Purpose|Affects Attendance?|Typical Use Case|
|---|---|---|---|
|**Mission**|Employee is out for _work-related_ reasons (e.g., client visit, fieldwork).|✔ Yes|Out of office but still considered working.|
|**Permission**|Employee needs to leave work temporarily for _personal_ reasons (e.g., doctor visit, early departure).|✔ Yes (Partial)|Away during part of the workday.|
|**Leave**|Employee is absent for a full day or more, usually pre-approved (e.g., vacation, sick leave).|✔ Yes (Full Day)|Not expected to show up at all.|


---

## 🔁 View Access Matrix (Simplified)

| Page                  | Admin | Leader   | Employee     |
| --------------------- | ----- | -------- | ------------ |
| Login / Logout        | ✅     | ✅        | ✅            |
| Dashboard             | ✅     | ✅        | ✅ (basic)    |
| Manage Users          | ✅     | ❌        | ❌            |
| Manage Employees      | ✅     | ❌        | ❌            |
| Manage Employee Types | ✅     | ❌        | ❌            |
| Projects / Customers  | ✅     | ✅        | ❌            |
| Task Pages            | ✅     | ✅        | ✅ (view own) |
| Calendar View         | ✅     | ✅ (team) | ✅ (own)      |
| Task Logs             | ✅     | ✅        | ✅ (own)      |
| Timecards             | ✅     | ✅        | ✅            |
| Submit Requests       | ✅     | ✅        | ✅            |
| Approve Requests      | ✅     | ✅        | ❌            |
| Manage Leave Types    | ✅     | ❌        | ❌            |

