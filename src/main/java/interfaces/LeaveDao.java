/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;

/**
 *
 * @author Vladimir Bernardo
 */
import model.LeaveRequest;
import java.util.List;

public interface LeaveDao {
    List<LeaveRequest> getAllRequests();
    List<LeaveRequest> findByEmployeeId(String id);
    boolean addLeaveRequest(LeaveRequest request);
    boolean updateStatus(String requestId, String status);
    
    // Change this from void to boolean
    boolean deleteRequest(String requestId); 
}
