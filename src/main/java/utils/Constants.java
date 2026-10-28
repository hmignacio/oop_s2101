/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

/**
 *
 * @author Vladimir Bernardo
 */
public class Constants {
    
    public static final String USER_CSV_PATH = "src/main/java/resources/users.csv";
    public static final String EMPLOYEE_CSV_PATH = "src/main/java/resources/employee.csv";
    public static final String ATTENDANCE_CSV_PATH = "src/main/java/resources/attendance.csv";
    public static final String LEAVE_CSV_PATH = "src/main/java/resources/leaves.csv";
    // --- 1. Pay Rates & Multipliers ---
    // Note: These are fallback values; PayrollService uses the Employee object data first.
    public static final double OVERTIME_RATE_MULTIPLIER = 1.25;
    
    // --- 2. Government Deduction Rates (2026) ---
    public static final double SSS_RATE = 0.045;
    public static final double PHILHEALTH_RATE = 0.05;
    public static final double PAGIBIG_RATE_HI = 0.02;
    public static final double PAGIBIG_RATE_LO = 0.01;
    public static final double TAX_RATE = 0.15;
    
    public static final double SSS_MAX_CONTRIBUTION = 3000.0;
    public static final double PHILHEALTH_MAX_CONTRIBUTION = 2000.0;
    public static final double PAGIBIG_MAX_CONTRIBUTION = 1000.0;
    
    // --- 3. Input Validation Patterns (Regex) ---
    public static final String NUMERIC_ONLY = "^\\d+$"; 
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$";
    
    // UPDATED: Matches the MM-DD-YYYY logic used in HRUI
    public static final String DATE_PATTERN = "^(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])-(19|20)\\d\\d$"; 
    
    public static final String SSS_NUMBER_PATTERN = "^\\d{2}-\\d{7}-\\d{1}$";           
    public static final String PHILHEALTH_NUMBER_PATTERN = "^\\d{4}-\\d{4}-\\d{4}$";    
    public static final String TIN_NUMBER_PATTERN = "^\\d{3}-\\d{3}-\\d{3}-\\d{3}$";    
    public static final String PAGIBIG_NUMBER_PATTERN = "^\\d{4}-\\d{4}-\\d{4}$";       
    public static final String CONTACT_NUMBER_PATTERN = "^\\d{4}-\\d{3}-\\d{4}$";       

    // --- 4. UI Error Messages ---
    public static final String REQUIRED_FIELDS_ERROR = "All fields are required and cannot be empty.";
    public static final String DUPLICATE_EMP_NUM_ERROR = "Employee number already exists in the system.";
    public static final String INVALID_NUMERIC_ERROR = "This field must contain numeric values only.";
    public static final String INVALID_EMAIL_ERROR = "Invalid email format (e.g., user@motorph.com).";
    public static final String INVALID_CONTACT_ERROR = "Invalid format. Use ####-###-####.";
    public static final String INVALID_SSS_ERROR = "Invalid SSS format. Use ##-#######-#.";
    public static final String INVALID_PHILHEALTH_ERROR = "Invalid PhilHealth format. Use ####-####-####.";
    public static final String INVALID_TIN_ERROR = "Invalid TIN format. Use ###-###-###-###.";
    public static final String INVALID_PAGIBIG_ERROR = "Invalid Pag-IBIG format. Use ####-####-####.";
    public static final String NO_SELECTION_ERROR = "Please select an employee from the table.";
}
