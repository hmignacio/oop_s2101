package dao;

import interfaces.OvertimeDao;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import model.OvertimeRequest;
import utils.Constants;

public class OvertimeCSVDao extends MainCSVDao implements OvertimeDao {
    private static final String HEADER =
            "RequestId,EmployeeNumber,Date,StartTime,EndTime,Reason,Status";

    public OvertimeCSVDao() {
        super(Constants.OVERTIME_CSV_PATH);
    }

    @Override
    public List<OvertimeRequest> getAllRequests() {
        List<OvertimeRequest> requests = new ArrayList<>();
        for (String[] row : readAllLines(false)) {
            if (row.length >= 7) {
                requests.add(new OvertimeRequest(
                        row[0], row[1], row[2], row[3], row[4], row[5], row[6]));
            }
        }
        return requests;
    }

    @Override
    public List<OvertimeRequest> findByEmployeeId(String employeeId) {
        String normalizedId = employeeId == null ? "" : employeeId.trim();
        return getAllRequests().stream()
                .filter(request -> request.getEmployeeNumber().equals(normalizedId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean addRequest(OvertimeRequest request) {
        List<OvertimeRequest> requests = getAllRequests();
        if (requests.stream().anyMatch(existing ->
                existing.getRequestId().equals(request.getRequestId()))) {
            return false;
        }
        requests.add(request);
        return saveAll(requests);
    }

    @Override
    public boolean updateStatus(String requestId, String status) {
        if (!"Approved".equals(status) && !"Rejected".equals(status)) {
            return false;
        }
        List<OvertimeRequest> requests = getAllRequests();
        for (OvertimeRequest request : requests) {
            if (request.getRequestId().equals(requestId)
                    && request.getStatus().equalsIgnoreCase("Pending")) {
                request.setStatus(status);
                return saveAll(requests);
            }
        }
        return false;
    }

    private boolean saveAll(List<OvertimeRequest> requests) {
        List<String> lines = requests.stream()
                .map(request -> String.join(",",
                        csvField(request.getRequestId()),
                        csvField(request.getEmployeeNumber()),
                        csvField(request.getDate()),
                        csvField(request.getStartTime()),
                        csvField(request.getEndTime()),
                        csvField(request.getReason()),
                        csvField(request.getStatus())))
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
