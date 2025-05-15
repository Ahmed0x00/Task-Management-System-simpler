package taskmanagement;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import taskmanagement.model.*;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JsonUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // File paths
    private static final String USER_FILE = "src/main/resources/data/users.json";
    private static final String CUSTOMER_FILE = "src/main/resources/data/customers.json";
    private static final String EMPLOYEE_TYPE_FILE = "src/main/resources/data/employee_types.json";
    private static final String EMPLOYEE_FILE = "src/main/resources/data/employees.json";
    private static final String LEAVE_TYPE_FILE = "src/main/resources/data/leave_types.json";
    private static final String PROJECT_FILE = "src/main/resources/data/projects.json";
    private static final String REQUESTS_FILE = "src/main/resources/data/requests.json";
    private static final String TASK_LOG_FILE = "src/main/resources/data/task_logs.json";
    private static final String TASK_PHASE_FILE = "src/main/resources/data/task_phases.json";
    private static final String TASK_FILE = "src/main/resources/data/tasks.json";
    private static final String TIME_CARDS_FILE = "src/main/resources/data/time_cards.json";

    // Type references
    private static final TypeReference<List<User>> USER_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<Customer>> CUSTOMER_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<EmployeeType>> EMPLOYEE_TYPE_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<Employee>> EMPLOYEE_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<LeaveType>> LEAVE_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<Project>> PROJECT_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<Request>> REQUEST_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<TaskLog>> TASK_LOG_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<TaskPhase>> TASK_PHASE_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<Task>> TASK_TYPE = new TypeReference<>() {};
    private static final TypeReference<List<TimeCard>> TIME_CARD_TYPE = new TypeReference<>() {};

    // Logged-in user
    private static User loggedInUser;

    // Core JSON operations
    public static <T> List<T> getAll(String path, TypeReference<List<T>> typeRef) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                return new ArrayList<>();
            }
            return MAPPER.readValue(file, typeRef);
        } catch (Exception e) {
            throw new RuntimeException("Error reading JSON file: " + path, e);
        }
    }

    public static <T> T getOne(String path, TypeReference<List<T>> typeRef, Predicate<T> predicate) {
        return getAll(path, typeRef).stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }

    public static <T> void add(String path, T newObject, TypeReference<List<T>> typeRef) {
        if (newObject == null) throw new IllegalArgumentException("Object cannot be null");
        List<T> list = getAll(path, typeRef);
        list.add(newObject);
        saveList(path, list);
    }

    public static <T> void edit(String path, Predicate<T> matcher, T updatedObject, TypeReference<List<T>> typeRef) {
        if (updatedObject == null) throw new IllegalArgumentException("Updated object cannot be null");
        List<T> list = getAll(path, typeRef).stream()
                .map(item -> matcher.test(item) ? updatedObject : item)
                .collect(Collectors.toList());
        saveList(path, list);
    }

    public static <T> void delete(String path, Predicate<T> matcher, TypeReference<List<T>> typeRef) {
        List<T> list = getAll(path, typeRef).stream()
                .filter(item -> !matcher.test(item))
                .collect(Collectors.toList());
        saveList(path, list);
    }

    private static <T> void saveList(String path, List<T> list) {
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(new File(path), list);
        } catch (Exception e) {
            throw new RuntimeException("Error saving JSON file: " + path, e);
        }
    }

    // User operations
    public static User getLoggedInUser() {
        return loggedInUser;
    }

    public static void setLoggedInUser(User user) {
        loggedInUser = user;
    }

    public static List<User> getAllUsers() {
        return getAll(USER_FILE, USER_TYPE);
    }

    public static User findByUsername(String username) {
        if (username == null) return null;
        Predicate<User> predicate = u -> username.equalsIgnoreCase(u.getUsername());
        return getOne(USER_FILE, USER_TYPE, predicate);
    }

    public static void addUser(User user) {
        add(USER_FILE, user, USER_TYPE);
    }

    public static void updateUser(User user) {
        if (user.getId() == null) throw new IllegalArgumentException("User ID cannot be null");
        edit(USER_FILE, u -> user.getId().equals(u.getId()), user, USER_TYPE);
    }

    public static void deleteUserById(String id) {
        if (id == null) throw new IllegalArgumentException("User ID cannot be null");
        delete(USER_FILE, u -> id.equals(u.getId()), USER_TYPE);
    }

    public static User getUserById(String id) {
        if (id == null) return null;
        return getOne(USER_FILE, USER_TYPE, u -> id.equals(u.getId()));
    }

    public static String getEmployeeName(String employeeId) {
        if (employeeId == null || employeeId.isEmpty()) {
            System.out.println("JsonUtil.getEmployeeName: Invalid employeeId: " + employeeId);
            return "Unknown";
        }
        String name = getAllUsers().stream()
                .filter(user -> employeeId.equals(user.getEmployeeId()))
                .map(User::getUsername)
                .findFirst()
                .orElse("Unknown");
        if (name.equals("Unknown")) {
            System.out.println("JsonUtil.getEmployeeName: No user found for employeeId: " + employeeId);
        }
        return name;
    }

    // Customer operations
    public static List<Customer> getAllCustomers() {
        return getAll(CUSTOMER_FILE, CUSTOMER_TYPE);
    }

    public static void addCustomer(Customer customer) {
        add(CUSTOMER_FILE, customer, CUSTOMER_TYPE);
    }

    public static void updateCustomer(Customer customer) {
        if (customer.getId() == null) throw new IllegalArgumentException("Customer ID cannot be null");
        edit(CUSTOMER_FILE, c -> customer.getId().equals(c.getId()), customer, CUSTOMER_TYPE);
    }

    public static void deleteCustomerById(String id) {
        if (id == null) throw new IllegalArgumentException("Customer ID cannot be null");
        delete(CUSTOMER_FILE, c -> id.equals(c.getId()), CUSTOMER_TYPE);
    }

    // EmployeeType operations
    public static List<EmployeeType> getAllEmployeeTypes() {
        return getAll(EMPLOYEE_TYPE_FILE, EMPLOYEE_TYPE_TYPE);
    }

    public static void addEmployeeType(EmployeeType employeeType) {
        if (employeeType == null) throw new IllegalArgumentException("EmployeeType cannot be null");
        List<EmployeeType> employeeTypes = getAllEmployeeTypes();
        if (employeeType.getId() == 0) {
            int newId = employeeTypes.stream()
                    .mapToInt(EmployeeType::getId)
                    .max()
                    .orElse(0) + 1;
            employeeType.setId(newId);
        }
        add(EMPLOYEE_TYPE_FILE, employeeType, EMPLOYEE_TYPE_TYPE);
    }

    public static void updateEmployeeType(EmployeeType employeeType) {
        if (employeeType == null || employeeType.getId() == 0) {
            throw new IllegalArgumentException("EmployeeType or ID cannot be null or zero");
        }
        edit(EMPLOYEE_TYPE_FILE, et -> et.getId() == employeeType.getId(), employeeType, EMPLOYEE_TYPE_TYPE);
    }

    public static void deleteEmployeeTypeById(int id) {
        if (id == 0) throw new IllegalArgumentException("EmployeeType ID cannot be zero");
        delete(EMPLOYEE_TYPE_FILE, et -> et.getId() == id, EMPLOYEE_TYPE_TYPE);
    }

    public static EmployeeType getEmployeeTypeById(int id) {
        if (id == 0) return null;
        return getOne(EMPLOYEE_TYPE_FILE, EMPLOYEE_TYPE_TYPE, et -> et.getId() == id);
    }

    // Employee operations
    public static List<Employee> getAllEmployees() {
        return getAll(EMPLOYEE_FILE, EMPLOYEE_TYPE);
    }

    public static Employee getEmployeeById(String id) {
        if (id == null) return null;
        return getOne(EMPLOYEE_FILE, EMPLOYEE_TYPE, e -> id.equals(e.getId()));
    }

    public static void addEmployee(Employee employee) {
        if (employee == null) throw new IllegalArgumentException("Employee cannot be null");
        if (employee.getEmployeeTypeId() != 0 && getEmployeeTypeById(employee.getEmployeeTypeId()) == null) {
            throw new IllegalArgumentException("Invalid employeeTypeId: " + employee.getEmployeeTypeId());
        }
        add(EMPLOYEE_FILE, employee, EMPLOYEE_TYPE);
    }

    public static void updateEmployee(Employee employee) {
        if (employee == null || employee.getId() == null) {
            throw new IllegalArgumentException("Employee or ID cannot be null");
        }
        if (employee.getEmployeeTypeId() != 0 && getEmployeeTypeById(employee.getEmployeeTypeId()) == null) {
            throw new IllegalArgumentException("Invalid employeeTypeId: " + employee.getEmployeeTypeId());
        }
        edit(EMPLOYEE_FILE, e -> employee.getId().equals(e.getId()), employee, EMPLOYEE_TYPE);
    }

    public static void deleteEmployeeById(String id) {
        if (id == null) throw new IllegalArgumentException("Employee ID cannot be null");
        delete(EMPLOYEE_FILE, e -> id.equals(e.getId()), EMPLOYEE_TYPE);
    }

    // LeaveType operations
    public static List<LeaveType> getAllLeaveTypes() {
        return getAll(LEAVE_TYPE_FILE, LEAVE_TYPE);
    }

    public static LeaveType getLeaveTypeById(String id) {
        if (id == null) return null;
        return getOne(LEAVE_TYPE_FILE, LEAVE_TYPE, lt -> id.equals(lt.getId()));
    }

    public static void addLeaveType(LeaveType leaveType) {
        add(LEAVE_TYPE_FILE, leaveType, LEAVE_TYPE);
    }

    public static void updateLeaveType(LeaveType leaveType) {
        if (leaveType.getId() == null) throw new IllegalArgumentException("LeaveType ID cannot be null");
        edit(LEAVE_TYPE_FILE, lt -> leaveType.getId().equals(lt.getId()), leaveType, LEAVE_TYPE);
    }

    public static void deleteLeaveTypeById(String id) {
        if (id == null) throw new IllegalArgumentException("LeaveType ID cannot be null");
        delete(LEAVE_TYPE_FILE, lt -> id.equals(lt.getId()), LEAVE_TYPE);
    }

    // Project operations
    public static List<Project> getAllProjects() {
        return getAll(PROJECT_FILE, PROJECT_TYPE);
    }

    public static Project findByName(String name) {
        if (name == null) return null;
        Predicate<Project> predicate = p -> name.equalsIgnoreCase(p.getName());
        return getOne(PROJECT_FILE, PROJECT_TYPE, predicate);
    }

    public static void addProject(Project project) {
        add(PROJECT_FILE, project, PROJECT_TYPE);
    }

    public static void updateProject(Project project) {
        if (project.getId() == null) throw new IllegalArgumentException("Project ID cannot be null");
        edit(PROJECT_FILE, p -> project.getId().equals(p.getId()), project, PROJECT_TYPE);
    }

    public static void deleteProjectById(String id) {
        if (id == null) throw new IllegalArgumentException("Project ID cannot be null");
        delete(PROJECT_FILE, p -> id.equals(p.getId()), PROJECT_TYPE);
    }

    public static Project getProjectById(String id) {
        if (id == null) return null;
        return getOne(PROJECT_FILE, PROJECT_TYPE, p -> id.equals(p.getId()));
    }

    // Request operations
    public static List<Request> getAllRequests() {
        return getAll(REQUESTS_FILE, REQUEST_TYPE); // Fixed TypeReference
    }

    public static List<Request> getRequestsByEmployee(String employeeId) {
        if (employeeId == null) return new ArrayList<>();
        return getAllRequests().stream()
                .filter(req -> employeeId.equals(req.getEmployeeId()))
                .toList();
    }

    public static boolean addRequest(Request request) {
        try {
            if (request == null) throw new IllegalArgumentException("Request cannot be null");
            List<Request> requests = getAllRequests();
            int newId = requests.stream().mapToInt(Request::getId).max().orElse(0) + 1;
            request.setId(newId);
            add(REQUESTS_FILE, request, REQUEST_TYPE); // Fixed TypeReference
            System.out.println("JsonUtil.addRequest: Request added with ID: " + newId);
            return true;
        } catch (Exception e) {
            System.out.println("JsonUtil.addRequest: Error adding request: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateRequest(Request request) {
        try {
            if (request == null || request.getId() == 0) {
                throw new IllegalArgumentException("Request or ID cannot be null or zero");
            }
            edit(REQUESTS_FILE, r -> r.getId() == request.getId(), request, REQUEST_TYPE); // Fixed TypeReference
            System.out.println("JsonUtil.updateRequest: Request updated with ID: " + request.getId());
            if ("LEAVE".equals(request.getType()) && request.getStatus() == Request.Status.APPROVED) {
                createTimeCardsForLeave(request);
            }
            return true;
        } catch (Exception e) {
            System.out.println("JsonUtil.updateRequest: Error updating request: " + e.getMessage());
            return false;
        }
    }

    private static void createTimeCardsForLeave(Request request) {
        try {
            LocalDate startDate = LocalDate.parse(request.getStartDate(), DATE_FORMATTER);
            LocalDate endDate = LocalDate.parse(request.getEndDate(), DATE_FORMATTER);
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                TimeCard timeCard = new TimeCard(
                        0, // ID will be set by addTimeCard
                        request.getEmployeeId(),
                        null, // No clock-in for leave
                        null, // No clock-out for leave
                        date.format(DATE_FORMATTER),
                        0.0 // Zero hours worked
                );
                addTimeCard(timeCard);
                System.out.println("JsonUtil.createTimeCardsForLeave: Added time card for date: " + date);
            }
        } catch (Exception e) {
            System.out.println("JsonUtil.createTimeCardsForLeave: Error creating time card: " + e.getMessage());
        }
    }

    // TaskLog operations
    public static List<TaskLog> getAllTaskLogs() {
        return getAll(TASK_LOG_FILE, TASK_LOG_TYPE);
    }

    public static void addTaskLog(TaskLog taskLog) {
        add(TASK_LOG_FILE, taskLog, TASK_LOG_TYPE);
    }

    public static void updateTaskLog(TaskLog taskLog) {
        if (taskLog.getId() == null) throw new IllegalArgumentException("TaskLog ID cannot be null");
        edit(TASK_LOG_FILE, log -> taskLog.getId().equals(log.getId()), taskLog, TASK_LOG_TYPE);
    }

    public static TaskLog getByTaskIdAndEmployeeId(String taskId, String employeeId) {
        if (taskId == null || employeeId == null) return null;
        Task task = getTaskById(taskId);
        Employee employee = getEmployeeById(employeeId);
        if (task == null || employee == null) return null;
        String taskTitle = task.getTitle();
        String employeeName = employee.getName();
        Predicate<TaskLog> predicate = log -> taskTitle.equals(log.getTaskName()) &&
                employeeName.equals(log.getEmployee()) && log.getEndedAt() == null;
        return getOne(TASK_LOG_FILE, TASK_LOG_TYPE, predicate);
    }

    // TaskPhase operations
    public static List<TaskPhase> getAllTaskPhases() {
        return getAll(TASK_PHASE_FILE, TASK_PHASE_TYPE);
    }

    public static TaskPhase findTaskPhaseByName(String name) {
        if (name == null) return null;
        Predicate<TaskPhase> predicate = p -> name.equalsIgnoreCase(p.getName());
        return getOne(TASK_PHASE_FILE, TASK_PHASE_TYPE, predicate);
    }

    public static void addTaskPhase(TaskPhase taskPhase) {
        add(TASK_PHASE_FILE, taskPhase, TASK_PHASE_TYPE);
    }

    public static void updateTaskPhase(TaskPhase taskPhase) {
        if (taskPhase.getId() == null) throw new IllegalArgumentException("TaskPhase ID cannot be null");
        edit(TASK_PHASE_FILE, p -> taskPhase.getId().equals(p.getId()), taskPhase, TASK_PHASE_TYPE);
    }

    public static void deleteTaskPhaseById(String id) {
        if (id == null) throw new IllegalArgumentException("TaskPhase ID cannot be null");
        delete(TASK_PHASE_FILE, p -> id.equals(p.getId()), TASK_PHASE_TYPE);
    }

    public static TaskPhase getTaskPhaseById(String id) {
        if (id == null) return null;
        return getOne(TASK_PHASE_FILE, TASK_PHASE_TYPE, p -> id.equals(p.getId()));
    }

    // Task operations
    public static List<Task> getAllTasks() {
        return getAll(TASK_FILE, TASK_TYPE);
    }

    public static List<Task> getTasksByEmployeeId(String employeeId) {
        if (employeeId == null) return new ArrayList<>();
        return getAllTasks().stream()
                .filter(t -> employeeId.equals(t.getAssignedEmployeeId()))
                .toList();
    }

    public static void addTask(Task task) {
        add(TASK_FILE, task, TASK_TYPE);
    }

    public static void updateTask(Task task) {
        if (task.getId() == null) throw new IllegalArgumentException("Task ID cannot be null");
        edit(TASK_FILE, t -> task.getId().equals(t.getId()), task, TASK_TYPE);
    }

    public static void deleteTaskById(String id) {
        if (id == null) throw new IllegalArgumentException("Task ID cannot be null");
        delete(TASK_FILE, t -> id.equals(t.getId()), TASK_TYPE);
    }

    public static Task getTaskById(String id) {
        if (id == null) return null;
        return getOne(TASK_FILE, TASK_TYPE, t -> id.equals(t.getId()));
    }

    // TimeCard operations
    public static List<TimeCard> getAllTimeCards() {
        return getAll(TIME_CARDS_FILE, TIME_CARD_TYPE);
    }

    public static List<TimeCard> getTimeCardsByEmployee(String employeeId) {
        if (employeeId == null) return new ArrayList<>();
        return getAllTimeCards().stream()
                .filter(tc -> employeeId.equals(tc.getEmployeeId()))
                .toList();
    }

    public static boolean addTimeCard(TimeCard timeCard) {
        try {
            if (timeCard == null) throw new IllegalArgumentException("TimeCard cannot be null");
            List<TimeCard> timeCards = getAllTimeCards();
            int newId = timeCards.stream().mapToInt(TimeCard::getId).max().orElse(0) + 1;
            timeCard.setId(newId);
            add(TIME_CARDS_FILE, timeCard, TIME_CARD_TYPE);
            System.out.println("JsonUtil.addTimeCard: Time card added with ID: " + newId);
            return true;
        } catch (Exception e) {
            System.out.println("JsonUtil.addTimeCard: Error adding time card: " + e.getMessage());
            return false;
        }
    }
}