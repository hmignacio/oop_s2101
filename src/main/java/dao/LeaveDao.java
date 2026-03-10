/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author Vladimir Bernardo
 */
import model.LeaveRequest;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter; // Added for date standardization

public class LeaveDao {
    private final String csvFile = "src/main/java/resources/leaves.csv";
    
    // Centralized date format to ensure consistency across the system
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd-yyyy");

    public List<LeaveRequest> getAllRequests() {
        List<LeaveRequest> list = new ArrayList<>();
        File file = new File(csvFile);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] d = line.split(",");
                if (d.length >= 7) {
                    // Logic: Even if the model stores Strings, we ensure they are trimmed
                    list.add(new LeaveRequest(
                        d[0].trim(), // RequestId
                        d[1].trim(), // EmployeeNumber
                        d[2].trim(), // LeaveType
                        d[3].trim(), // StartDate
                        d[4].trim(), // EndDate
                        d[5].trim(), // Status
                        d[6].trim()  // Reason
                    ));
                }
            }
        } catch (IOException e) { 
            System.err.println("Error reading leaves CSV: " + e.getMessage());
        }
        return list;
    }

    public List<LeaveRequest> getByEmployee(String empId) {
        return getAllRequests().stream()
                .filter(r -> r.getEmployeeNumber().equals(empId))
                .collect(Collectors.toList());
    }

    /**
     * Update functional requirement: Overwrites the CSV with the updated status.
     */
    public boolean updateStatus(String reqId, String newStatus) {
        List<String> lines = new ArrayList<>();
        boolean found = false;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String header = br.readLine();
            if (header != null) lines.add(header); 

            String line;
            while ((line = br.readLine()) != null) {
                String[] d = line.split(",");
                if (d[0].equals(reqId)) {
                    // Reconstruct line keeping the standardized date strings
                    lines.add(String.format("%s,%s,%s,%s,%s,%s,%s", 
                              d[0], d[1], d[2], d[3], d[4], newStatus, d[6]));
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            return false;
        }

        return found ? saveAll(lines) : false;
    }

    /**
     * Add functional requirement: Saves new leaves in the MM-dd-yyyy format.
     */
    public boolean addRequest(LeaveRequest req) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(csvFile, true)))) {
            // Formatting dates before saving to CSV ensures the MM-dd-yyyy requirement is met
            String line = String.format("%s,%s,%s,%s,%s,%s,%s",
                    req.getRequestId(), 
                    req.getEmployeeNumber(), 
                    req.getLeaveType(),
                    req.getStartDate(), // Assuming this is already formatted via the UI form
                    req.getEndDate(), 
                    req.getStatus(), 
                    req.getReason());
            out.println(line);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // Helper method to overwrite the file safely
    private boolean saveAll(List<String> lines) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
            for (String line : lines) {
                pw.println(line);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
