package taskmanagement.model;

public class TimeCard {
    private int id;
    private String employeeId; // UUID string, e.g., "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    private String clockInTime; // Format: yyyy-MM-dd HH:mm:ss
    private String clockOutTime; // Nullable, same format
    private String date; // Format: yyyy-MM-dd
    private Double hoursWorked; // Nullable

    public TimeCard() {}

    public TimeCard(int id, String employeeId, String clockInTime, String clockOutTime, String date, Double hoursWorked) {
        this.id = id;
        this.employeeId = employeeId;
        this.clockInTime = clockInTime;
        this.clockOutTime = clockOutTime;
        this.date = date;
        this.hoursWorked = hoursWorked;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getClockInTime() { return clockInTime; }
    public void setClockInTime(String clockInTime) { this.clockInTime = clockInTime; }
    public String getClockOutTime() { return clockOutTime; }
    public void setClockOutTime(String clockOutTime) { this.clockOutTime = clockOutTime; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public Double getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(Double hoursWorked) { this.hoursWorked = hoursWorked; }
}