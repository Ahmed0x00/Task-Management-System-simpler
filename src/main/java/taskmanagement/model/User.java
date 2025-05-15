package taskmanagement.model;

public class User {
    private String id;
    private String username;
    private String password;
    private Role role;
    private String employeeId;

    public enum Role {
        ADMIN, EMPLOYEE, LEADER
    }

    public User() {}

    public User(String id, String username, String password, Role role, String employeeId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.employeeId = employeeId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
}