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

public class PayrollService {

    /**
     * Calculates total hours worked from a list of attendance records.
     */
    public double calculateTotalHours(List<Attendance> logs) {
        double totalHours = 0;
        for (Attendance log : logs) {
            try {
                LocalTime in = LocalTime.parse(log.getTimeIn());
                LocalTime out = LocalTime.parse(log.getTimeOut());
                
                // Calculate duration
                long minutes = Duration.between(in, out).toMinutes();
                double hours = minutes / 60.0;
                
                // Subtract 1 hour for lunch break if worked a full shift (e.g., > 4.5 hours)
                if (hours > 4.5) hours -= 1.0; 
                
                totalHours += Math.max(0, hours); 
            } catch (Exception e) {
                // Skip invalid logs
            }
        }
        return totalHours;
    }

    /**
     * UPDATED: Calculates Gross Salary.
     * Logic: (Hourly Rate * Hours Worked) + Allowances.
     */
    public double calculateGrossSalary(Employee emp, double hours) {
        // 1. Calculate the 'Earned Basic' based on actual hours
        double earnedBasic = emp.getHourlyRate() * hours;
        
        // 2. Sum up the fixed allowances
        double totalAllowances = emp.getRiceSubsidy() + 
                                emp.getPhoneAllowance() + 
                                emp.getClothingAllowance();
        
        // 3. Gross Salary is the sum of both
        return earnedBasic + totalAllowances;
    }

    /**
     * Calculates Net Salary by applying deductions from Constants.
     */
    public double calculateNetSalary(Employee emp, double grossSalary) {
        double sss = calculateSSS(grossSalary);
        double philHealth = calculatePhilHealth(grossSalary);
        double pagIbig = calculatePagIbig(grossSalary);
        
        double totalDeductions = sss + philHealth + pagIbig;
        double taxableIncome = grossSalary - totalDeductions;
        
        // Note: Using a fixed TAX_RATE from Constants
        double tax = taxableIncome * Constants.TAX_RATE;

        return grossSalary - totalDeductions - tax;
    }

    // --- Private Deduction Logic using Constants ---

    private double calculateSSS(double gross) {
        double deduction = gross * Constants.SSS_RATE;
        return Math.min(deduction, Constants.SSS_MAX_CONTRIBUTION);
    }

    private double calculatePhilHealth(double gross) {
        double deduction = gross * Constants.PHILHEALTH_RATE;
        return Math.min(deduction, Constants.PHILHEALTH_MAX_CONTRIBUTION);
    }

    private double calculatePagIbig(double gross) {
        double rate = (gross > 1500) ? Constants.PAGIBIG_RATE_HI : Constants.PAGIBIG_RATE_LO;
        double deduction = gross * rate;
        return Math.min(deduction, Constants.PAGIBIG_MAX_CONTRIBUTION);
    }
}
