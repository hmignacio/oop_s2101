/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Vladimir Bernardo
 */
public class UserAccount {
    private String employeeNo;
    private String password;
    private String role; // e.g., "Admin", "HR", "IT", "Regular"

    public UserAccount(String employeeNo, String password, String role) {
        this.employeeNo = employeeNo;
        this.password = password;
        this.role = role;
    }

    // Getters
    public String getEmployeeNo() { return employeeNo; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}
