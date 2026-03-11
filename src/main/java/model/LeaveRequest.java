/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Vladimir Bernardo
 */
public class LeaveRequest {
    private String requestId;
    private String employeeNumber;
    private String leaveType; // e.g., Sick, Vacation, Emergency
    private String startDate;
    private String endDate;
    private String status;    // e.g., Pending, Approved, Rejected
    private String reason;

    // Default Constructor
    public LeaveRequest() {}

    // Full Constructor
    public LeaveRequest(String requestId, String employeeNumber, String leaveType, 
                        String startDate, String endDate, String status, String reason) {
        this.requestId = requestId;
        this.employeeNumber = employeeNumber;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.reason = reason;
    }

    // --- Getters ---
    public String getRequestId() { return requestId; }
    public String getEmployeeNumber() { return employeeNumber; }
    public String getLeaveType() { return leaveType; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public String getReason() { return reason; }

    // --- Setters ---
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setStatus(String status) { this.status = status; }
    public void setReason(String reason) { this.reason = reason; }

    /**
     * Converts the object into a CSV-friendly string for saving.
     */
    @Override
    public String toString() {
        return String.join(",", 
            requestId, 
            employeeNumber, 
            leaveType, 
            startDate, 
            endDate, 
            status, 
            reason
        );
    }
}
