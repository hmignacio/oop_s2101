/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Vladimir Bernardo
 */
public class Attendance {
    private final String employeeNumber;
    private final String date;
    private final String timeIn;
    private String timeOut;

    public Attendance(String employeeNumber, String date, String timeIn, String timeOut) {
        this.employeeNumber = employeeNumber;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    // Getters
    public String getEmployeeNumber() { return employeeNumber; }
    public String getDate() { return date; }
    public String getTimeIn() { return timeIn; }
    public String getTimeOut() { return timeOut; }
    
    public void setTimeOut(String timeOut) {
        this.timeOut = timeOut;
    }
}
