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

    // --- Setters (Used by Admin/HR to update status) ---
    public void setStatus(String status) { this.status = status; }
}
