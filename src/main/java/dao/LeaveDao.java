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

public class LeaveDao {
    private final String csvFile = "src/main/java/resources/leaves.csv";

    public List<LeaveRequest> getAllRequests() {
        List<LeaveRequest> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            br.readLine(); // Skip Header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 7) {
                    list.add(new LeaveRequest(
                        data[0].trim(), data[1].trim(), data[2].trim(), 
                        data[3].trim(), data[4].trim(), data[5].trim(), data[6].trim()
                    ));
                }
            }
        } catch (IOException e) { return list; }
        return list;
    }

    // This matches the call in RegularEmployeeUI
    public boolean addLeaveRequest(LeaveRequest lr) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile, true))) {
            pw.println(lr.toString());
            return true;
        } catch (IOException e) { return false; }
    }

    public List<LeaveRequest> getByEmployee(String empNo) {
        return getAllRequests().stream()
                .filter(lr -> lr.getEmployeeNumber().equals(empNo))
                .collect(Collectors.toList());
    }

    public boolean updateStatus(String requestId, String newStatus) {
        List<String> lines = new ArrayList<>();
        boolean updated = false;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String header = br.readLine();
            if (header != null) lines.add(header);
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data[0].trim().equals(requestId)) {
                    data[5] = newStatus;
                    updated = true;
                }
                lines.add(String.join(",", data));
            }
        } catch (IOException e) { return false; }

        if (updated) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
                for (String l : lines) pw.println(l);
                return true;
            } catch (IOException e) { return false; }
        }
        return false;
    }
}
