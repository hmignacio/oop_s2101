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
    private final String employeeNo;
    private String password;
    private String role; // e.g., "Admin", "HR", "IT", "Regular", "Finance"

    /**
     * Standard Constructor used during the Login process.
     */
    public UserAccount(String employeeNo, String password, String role) {
        this.employeeNo = employeeNo != null ? employeeNo.trim() : "";
        this.password = password != null ? password.trim() : "";
        this.role = role != null ? role.trim() : "";
    }

    /**
     * CSV Constructor: Maps a String array (from MainCSVDao) to the Object.
     * Includes a length check to prevent crashes on empty CSV lines.
     */
    public UserAccount(String[] data) {
        if (data != null && data.length >= 3) {
            this.employeeNo = data[0].trim();
            this.password = data[1].trim();
            this.role = data[2].trim();
        } else {
            // Fallback for malformed data
            this.employeeNo = "";
            this.password = "";
            this.role = "Regular";
        }
    }

    /**
     * Serializer: Converts the Object back into a CSV line for saving.
     * Used by LoginCSVDao when updating passwords or roles.
     */
    public String toCSVString() {
        return String.join(",", employeeNo, password, role);
    }
    
    // --- Getters and Setters ---
    
    public String getEmployeeNo() { 
        return employeeNo; 
    }

    public String getUsername() { 
        // Per your requirement: Use EmployeeID as the username
        return employeeNo; 
    }

    public String getPassword() { 
        return password; 
    }

    public void setPassword(String password) { 
        this.password = password; 
    }

    public String getRole() { 
        return role; 
    }

    public void setRole(String role) { 
        this.role = role; 
    }
}