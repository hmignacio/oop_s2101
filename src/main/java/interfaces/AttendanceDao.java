/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;

/**
 *
 * @author Vladimir Bernardo
 */
import model.Attendance;
import model.Employee;
import java.util.List;

public interface AttendanceDao {
    List<Attendance> getAllAttendance();
    boolean hasRecordForToday(String empId, String date);
    boolean hasClockedOut(String empId, String date);
    boolean logTime(Employee emp, String date, String time, String type);
}