package interfaces;

import java.util.List;
import model.OvertimeRequest;

public interface OvertimeDao {
    List<OvertimeRequest> getAllRequests();
    List<OvertimeRequest> findByEmployeeId(String employeeId);
    boolean addRequest(OvertimeRequest request);
    boolean updateStatus(String requestId, String status);
}
