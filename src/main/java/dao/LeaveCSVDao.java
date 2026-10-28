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
        List<String[]> rows = readAllLines();
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
        try {
            List<LeaveRequest> all = getAllRequests();
            all.add(request);
            saveAll(all);
            return true;
        } catch (Exception e) {
            return false;
        }
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
                lr.setStatus(status);
                found = true;
                break;
            }
        }
        
        if (found) {
            saveAll(all);
        }
        
        return found; 
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
            saveAll(remainingRequests); // Write the smaller list back to CSV
            return true;
        }
        
        return false; // Request ID not found
    }

    private void saveAll(List<LeaveRequest> list) {
        List<String> lines = list.stream()
                .map(lr -> String.join(",", 
                    lr.getRequestId(),
                    lr.getEmployeeNumber(),
                    lr.getLeaveType(),
                    lr.getStartDate(),
                    lr.getEndDate(),
                    lr.getStatus(),
                    lr.getReason()
                ))
                .collect(Collectors.toList());
        writeAllLines(lines, HEADER);
    }
}
