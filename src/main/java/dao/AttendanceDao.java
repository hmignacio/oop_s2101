/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author Vladimir Bernardo
 */
import model.Attendance;
import model.Employee;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AttendanceDao {
    private final String csvFile = "src/main/java/resources/attendance.csv";

    /**
     * Reads the CSV and returns a list of all Attendance objects.
     */
    public List<Attendance> getAllAttendance() {
        List<Attendance> attendanceList = new ArrayList<>();
        File file = new File(csvFile);
        
        if (!file.exists()) return attendanceList;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            br.readLine(); // Skip header row

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // Expecting: ID, Last, First, Date, In, Out
                if (data.length >= 6) { 
                    attendanceList.add(new Attendance(
                        data[0].trim(), // Employee #
                        data[3].trim(), // Date
                        data[4].trim(), // Log In
                        data[5].trim()  // Log Out
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("Attendance Load Error: " + e.getMessage());
        }
        return attendanceList;
    }

    /**
     * Filters logs for a specific employee and specific month (formatted as MM).
     */
    public List<Attendance> getEmployeeAttendance(String empId, String month) {
        return getAllAttendance().stream()
                .filter(a -> a.getEmployeeNumber().equals(empId) && a.getDate().startsWith(month + "/"))
                .collect(Collectors.toList());
    }

    /**
     * Logs Time-In (Append) or Time-Out (Update Row).
     * @param type "IN" or "OUT"
     */
    public boolean logTime(Employee emp, String date, String time, String type) {
        if (type.equalsIgnoreCase("IN")) {
            // Check if record already exists to prevent double clock-in
            if (hasRecordForToday(emp.getEmployeeNumber(), date)) return false;

            try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(csvFile, true)))) {
                // ID, Last, First, Date, In, Out (default 00:00)
                String newEntry = String.format("%s,%s,%s,%s,%s,00:00",
                        emp.getEmployeeNumber(), emp.getLastName(), emp.getFirstName(), date, time);
                out.println(newEntry);
                return true;
            } catch (IOException e) { return false; }
        } else {
            // LOG OUT: Must find the existing row for today and update index 5
            List<String> lines = new ArrayList<>();
            boolean updated = false;

            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                String line;
                String header = br.readLine();
                if (header != null) lines.add(header); 

                while ((line = br.readLine()) != null) {
                    String[] d = line.split(",");
                    if (d[0].equals(emp.getEmployeeNumber()) && d[3].equals(date) && d[5].equals("00:00")) {
                        // Update the Log Out column
                        lines.add(String.format("%s,%s,%s,%s,%s,%s", d[0], d[1], d[2], d[3], d[4], time));
                        updated = true;
                    } else {
                        lines.add(line);
                    }
                }
            } catch (IOException e) { return false; }

            if (updated) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
                    for (String l : lines) pw.println(l);
                    return true;
                } catch (IOException e) { return false; }
            }
        }
        return false;
    }

    public boolean hasRecordForToday(String empId, String date) {
        return getAllAttendance().stream()
                .anyMatch(a -> a.getEmployeeNumber().equals(empId) && a.getDate().equals(date));
    }
    
    public boolean hasClockedOut(String empId, String date) {
        return getAllAttendance().stream()
                .anyMatch(a -> a.getEmployeeNumber().equals(empId) 
                            && a.getDate().equals(date) 
                            && !a.getTimeOut().equals("00:00"));
    }
}
    

