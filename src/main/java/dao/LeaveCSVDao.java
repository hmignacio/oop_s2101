/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
/**
 * @author Vladimir Bernardo
 */
import interfaces.LeaveDao;
import model.LeaveRequest;
import utils.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles Leave Request submissions and Administrative approvals.
 * @author Vladimir Bernardo
 */
public class LeaveCSVDao extends MainCSVDao implements LeaveDao {

    private static final String HEADER = "RequestId,EmployeeNumber,LeaveType,StartDate,EndDate,Status,Reason";

    public LeaveCSVDao() {
        super(Constants.LEAVE_CSV_PATH);
    }

    @Override
    public List<LeaveRequest> getAllRequests() {
        List<LeaveRequest> list = new ArrayList<>();
        List<String[]> rows = readAllLines(false);
        for (String[] row : rows) {
            if (row.length >= 7) {
                list.add(new LeaveRequest(
                    row[0], // RequestId
                    row[1], // EmployeeNumber
                    row[2], // Type
                    row[3], // Start
                    row[4], // End
                    row[5], // Status
                    row[6]  // Reason
                ));
            }
        }
        return list;
    }

    @Override
    public List<LeaveRequest> findByEmployeeId(String id) {
        return getAllRequests().stream()
                .filter(lr -> lr.getEmployeeNumber().equals(id))
                .collect(Collectors.toList());
    }

    @Override
    public boolean addLeaveRequest(LeaveRequest request) {
        List<LeaveRequest> all = getAllRequests();
        all.add(request);
        return saveAll(all);
    }

    /**
     * Updates the status of a specific leave request in the CSV file.
     * * @param requestId the unique identifier for the leave request
     * @param status the new status to be set
     * @return true if the request was found and updated, false otherwise
     */
    @Override
    public boolean updateStatus(String requestId, String status) {
        List<LeaveRequest> all = getAllRequests();
        boolean found = false;
        
        for (LeaveRequest lr : all) {
            if (lr.getRequestId().trim().equals(requestId.trim())) {
                if (!lr.getStatus().equalsIgnoreCase("Pending")) {
                    return false;
                }
                lr.setStatus(status);
                found = true;
                break;
            }
        }
        
        if (found) {
            return saveAll(all);
        }
        
        return false;
    }
    
    @Override
    public boolean deleteRequest(String requestId) {
        List<LeaveRequest> allRequests = getAllRequests();
        
        // Create a new list excluding the request with the matching ID
        List<LeaveRequest> remainingRequests = allRequests.stream()
                .filter(lr -> !lr.getRequestId().trim().equals(requestId.trim()))
                .collect(Collectors.toList());

        // Logic: if the size decreased, we successfully found and removed the record
        if (remainingRequests.size() < allRequests.size()) {
            return saveAll(remainingRequests);
        }
        
        return false; // Request ID not found
    }

    private boolean saveAll(List<LeaveRequest> list) {
        List<String> lines = list.stream()
                .map(lr -> String.join(",", 
                    csvField(lr.getRequestId()),
                    csvField(lr.getEmployeeNumber()),
                    csvField(lr.getLeaveType()),
                    csvField(lr.getStartDate()),
                    csvField(lr.getEndDate()),
                    csvField(lr.getStatus()),
                    csvField(lr.getReason())
                ))
                .collect(Collectors.toList());
        return writeAllLinesChecked(lines, HEADER);
    }

    private String csvField(String value) {
        String escaped = value == null ? "" : value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"")
                || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
