package taskmanagement.model;

import taskmanagement.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Request {
    private int id;
    private String employeeId; // UUID string, e.g., "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
    private String type; // MISSION, PERMISSION, LEAVE
    private String leaveTypeId; // UUID string from LeaveType.id (nullable for non-LEAVE)
    private String startDate; // Format: yyyy-MM-dd
    private String endDate; // Format: yyyy-MM-dd
    private Status status; // PENDING, APPROVED, DENIED
    private String reason; // Description

    public enum Status {
        PENDING, APPROVED, DENIED
    }

    public Request() {}

    public Request(int id, String employeeId, String type, String leaveTypeId, String startDate, String endDate, Status status, String reason) {
        this.id = id;
        this.employeeId = employeeId;
        this.type = type;
        this.leaveTypeId = leaveTypeId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.reason = reason;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(String leaveTypeId) { this.leaveTypeId = leaveTypeId; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    // Helper to display type in UI
    @JsonIgnore
    public String getDisplayType() {
        if ("LEAVE".equals(type) && leaveTypeId != null) {
            LeaveType leaveType = JsonUtil.getLeaveTypeById(leaveTypeId);
            return leaveType != null ? "LEAVE: " + leaveType.getTitle() : "LEAVE: Unknown";
        }
        return type;
    }
}