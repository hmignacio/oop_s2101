package model;

public class OvertimeRequest {
    private final String requestId;
    private final String employeeNumber;
    private final String date;
    private final String startTime;
    private final String endTime;
    private final String reason;
    private String status;

    public OvertimeRequest(String requestId, String employeeNumber, String date,
            String startTime, String endTime, String reason, String status) {
        this.requestId = requestId == null ? "" : requestId.trim();
        this.employeeNumber = employeeNumber == null ? "" : employeeNumber.trim();
        this.date = date == null ? "" : date.trim();
        this.startTime = startTime == null ? "" : startTime.trim();
        this.endTime = endTime == null ? "" : endTime.trim();
        this.reason = reason == null ? "" : reason.trim();
        this.status = status == null ? "" : status.trim();
    }

    public String getRequestId() { return requestId; }
    public String getEmployeeNumber() { return employeeNumber; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getReason() { return reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status == null ? "" : status.trim(); }
}
