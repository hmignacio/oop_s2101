/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author Vladimir Bernardo
 */
import interfaces.AttendanceDao;
import model.Attendance;
import model.Employee;
import utils.Constants;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles employee punch-in/out records.
 * Matches requirements: One Time-In/Out per day.
 */
public class AttendanceCSVDao extends MainCSVDao implements AttendanceDao {

    private static final String HEADER = "Employee #,Last Name,First Name,Date,Log In,Log Out";

    public AttendanceCSVDao() {
        super(Constants.ATTENDANCE_CSV_PATH);
    }

    @Override
    public List<Attendance> getAllAttendance() {
        List<Attendance> list = new ArrayList<>();
        List<String[]> rows = readAllLines();
        for (String[] row : rows) {
            // Mapping based on attendance.csv: 0=ID, 1=Last, 2=First, 3=Date, 4=In, 5=Out
            if (row.length >= 6) {
                // Ensure your Attendance model has this 4-string constructor
                list.add(new Attendance(row[0], row[3], row[4], row[5]));
            }
        }
        return list;
    }

    @Override
    public boolean hasRecordForToday(String empId, String date) {
        return getAllAttendance().stream()
                .anyMatch(a -> a.getEmployeeNumber().trim().equals(empId.trim()) 
                            && a.getDate().equals(date));
    }

    @Override
    public boolean hasClockedOut(String empId, String date) {
        return getAllAttendance().stream()
                .anyMatch(a -> a.getEmployeeNumber().trim().equals(empId.trim()) 
                            && a.getDate().equals(date) 
                            && !a.getTimeOut().equals("---") 
                            && !a.getTimeOut().isEmpty());
    }

    @Override
    public boolean logTime(Employee emp, String date, String time, String type) {
        List<Attendance> allRecords = getAllAttendance();
        boolean success = false;

        if (type.equalsIgnoreCase("IN")) {
            if (!hasRecordForToday(emp.getEmployeeNumber(), date)) {
                // Add new row for the day
                allRecords.add(new Attendance(emp.getEmployeeNumber(), date, time, "---"));
                success = true;
            }
        } else if (type.equalsIgnoreCase("OUT")) {
            // Find existing row and update the Time-Out
            for (Attendance record : allRecords) {
                if (record.getEmployeeNumber().trim().equals(emp.getEmployeeNumber().trim()) 
                        && record.getDate().equals(date)) {
                    record.setTimeOut(time);
                    success = true;
                    break;
                }
            }
        }

        if (success) {
            saveAll(allRecords, emp);
        }
        return success;
    }

    /**
     * Persists all attendance records. 
     * Uses empInfo to populate names for the currently active user's row.
     */
    private void saveAll(List<Attendance> list, Employee empInfo) {
        List<String> lines = new ArrayList<>();
        for (Attendance a : list) {
            String lastName = "---";
            String firstName = "---";

            // If this row belongs to the person currently logging time, use their real name
            if (a.getEmployeeNumber().equals(empInfo.getEmployeeNumber())) {
                lastName = empInfo.getLastName();
                firstName = empInfo.getFirstName();
            }

            String line = String.join(",", 
                a.getEmployeeNumber(),
                lastName,
                firstName,
                a.getDate(),
                a.getTimeIn(),
                a.getTimeOut()
            );
            lines.add(line);
        }
        writeAllLines(lines, HEADER);
    }
}
