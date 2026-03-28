/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

/**
 *
 * @author Vladimir Bernardo
 */
import model.Employee;
import model.Attendance;
import utils.Constants;
import java.time.LocalTime;
import java.time.Duration;
import java.util.List;

/**
 * Business logic for Salary and Deduction calculations.
 * @author Vladimir Bernardo
 */
public class PayrollService {

    /**
     * Requirement: Calculate total hours worked from attendance logs.
     * Logic: (TimeOut - TimeIn) - 1 hour lunch break if shift > 4.5 hours.
     */
    public double calculateTotalHours(List<Attendance> logs) {
        double totalHours = 0;
        for (Attendance log : logs) {
            try {
                // Handle cases where time might be "---"
                if (log.getTimeIn().equals("---") || log.getTimeOut().equals("---")) continue;

                LocalTime in = LocalTime.parse(log.getTimeIn());
                LocalTime out = LocalTime.parse(log.getTimeOut());
                
                long minutes = Duration.between(in, out).toMinutes();
                double hours = minutes / 60.0;
                
                // Business Rule: Deduct 1 hour for lunch if they worked more than half a day
                if (hours > 4.5) {
                    hours -= 1.0;
                }
                
                totalHours += Math.max(0, hours); 
            } catch (Exception e) {
                // Skip rows with invalid time formats
            }
        }
        return totalHours;
    }

    /**
     * Requirement: Calculate Gross Salary.
     * Formula: (Hourly Rate * Hours Worked) + All Allowances.
     */
    public double calculateGrossSalary(Employee emp, double hoursWorked) {
        double basicEarned = emp.getHourlyRate() * hoursWorked;
        double totalAllowances = emp.getRiceSubsidy() + 
                                 emp.getPhoneAllowance() + 
                                 emp.getClothingAllowance();
        
        return basicEarned + totalAllowances;
    }

    /**
     * Requirement: Calculate Net Salary after all 2026 deductions.
     */
    public double calculateNetSalary(Employee emp, double grossSalary) {
        double sss = calculateSSS(grossSalary);
        double philHealth = calculatePhilHealth(grossSalary);
        double pagIbig = calculatePagIbig(grossSalary);
        
        double totalDeductions = sss + philHealth + pagIbig;
        double taxableIncome = grossSalary - totalDeductions;
        
        // Applying the fixed 15% tax rate from your Constants
        double withholdingTax = taxableIncome * Constants.TAX_RATE;

        return grossSalary - totalDeductions - withholdingTax;
    }

    // --- Private Deduction Logic using Constants ---

    private double calculateSSS(double gross) {
        double contribution = gross * Constants.SSS_RATE;
        return Math.min(contribution, Constants.SSS_MAX_CONTRIBUTION);
    }

    private double calculatePhilHealth(double gross) {
        double contribution = gross * Constants.PHILHEALTH_RATE;
        return Math.min(contribution, Constants.PHILHEALTH_MAX_CONTRIBUTION);
    }

    private double calculatePagIbig(double gross) {
        // Higher rate if earning more than 1500
        double rate = (gross > 1500) ? Constants.PAGIBIG_RATE_HI : Constants.PAGIBIG_RATE_LO;
        double contribution = gross * rate;
        return Math.min(contribution, Constants.PAGIBIG_MAX_CONTRIBUTION);
    }
}
