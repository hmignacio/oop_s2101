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
import java.time.format.DateTimeParseException;
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
            if (log == null || log.getTimeIn() == null || log.getTimeOut() == null
                    || log.getTimeIn().equals("---") || log.getTimeOut().equals("---")) {
                continue;
            }
            try {
                LocalTime in = LocalTime.parse(log.getTimeIn());
                LocalTime out = LocalTime.parse(log.getTimeOut());
                long minutes = Duration.between(in, out).toMinutes();
                double hours = minutes / 60.0;
                if (hours > 4.5) {
                    hours -= 1.0;
                }
                totalHours += Math.max(0, hours);
            } catch (DateTimeParseException e) {
                // Skip rows with invalid time formats.
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
        double totalAllowances = emp.getRiceSubsidy()
                + emp.getPhoneAllowance()
                + emp.getClothingAllowance();
        return basicEarned + totalAllowances;
    }

    public double calculateBasicPay(Employee emp, double hoursWorked) {
        return emp.getHourlyRate() * hoursWorked;
    }

    public double calculateTotalAllowances(Employee emp) {
        return emp.getRiceSubsidy()
                + emp.getPhoneAllowance()
                + emp.getClothingAllowance();
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
        double withholdingTax = taxableIncome * Constants.TAX_RATE;
        return grossSalary - totalDeductions - withholdingTax;
    }

    public double calculateTotalDeductions(double grossSalary) {
        double totalDeductions = calculateSSS(grossSalary)
                + calculatePhilHealth(grossSalary)
                + calculatePagIbig(grossSalary);
        double taxableIncome = grossSalary - totalDeductions;
        double withholdingTax = taxableIncome * Constants.TAX_RATE;
        return totalDeductions + withholdingTax;
    }

    public double calculateWithholdingTax(double grossSalary) {
        double contributions = calculateSSS(grossSalary)
                + calculatePhilHealth(grossSalary)
                + calculatePagIbig(grossSalary);
        double taxableIncome = grossSalary - contributions;
        return taxableIncome * Constants.TAX_RATE;
    }

    public double calculateSSS(double gross) {
        double contribution = gross * Constants.SSS_RATE;
        return Math.min(contribution, Constants.SSS_MAX_CONTRIBUTION);
    }

    public double calculatePhilHealth(double gross) {
        double contribution = gross * Constants.PHILHEALTH_RATE;
        return Math.min(contribution, Constants.PHILHEALTH_MAX_CONTRIBUTION);
    }

    public double calculatePagIbig(double gross) {
        // Higher rate if earning more than 1500
        double rate = (gross > 1500) ? Constants.PAGIBIG_RATE_HI : Constants.PAGIBIG_RATE_LO;
        double contribution = gross * rate;
        return Math.min(contribution, Constants.PAGIBIG_MAX_CONTRIBUTION);
    }

    public PayrollBreakdown calculatePayroll(Employee employee, double hoursWorked) {
        double basicPay = calculateBasicPay(employee, hoursWorked);
        double totalAllowances = calculateTotalAllowances(employee);
        double grossSalary = calculateGrossSalary(employee, hoursWorked);
        double sss = calculateSSS(grossSalary);
        double philHealth = calculatePhilHealth(grossSalary);
        double pagIbig = calculatePagIbig(grossSalary);
        double totalDeductions = sss + philHealth + pagIbig;
        double taxableIncome = grossSalary - totalDeductions;
        double withholdingTax = taxableIncome * Constants.TAX_RATE;
        double netSalary = grossSalary - totalDeductions - withholdingTax;

        return new PayrollBreakdown(hoursWorked, basicPay,
                employee.getRiceSubsidy(), employee.getPhoneAllowance(),
                employee.getClothingAllowance(), totalAllowances, grossSalary,
                sss, philHealth, pagIbig, withholdingTax,
                totalDeductions + withholdingTax, netSalary);
    }

    public static final class PayrollBreakdown {
        private final double hoursWorked;
        private final double basicPay;
        private final double riceAllowance;
        private final double phoneAllowance;
        private final double clothingAllowance;
        private final double totalAllowances;
        private final double grossSalary;
        private final double sss;
        private final double philHealth;
        private final double pagIbig;
        private final double withholdingTax;
        private final double totalDeductions;
        private final double netSalary;

        private PayrollBreakdown(double hoursWorked, double basicPay,
                double riceAllowance, double phoneAllowance, double clothingAllowance,
                double totalAllowances, double grossSalary, double sss,
                double philHealth, double pagIbig, double withholdingTax,
                double totalDeductions, double netSalary) {
            this.hoursWorked = hoursWorked;
            this.basicPay = basicPay;
            this.riceAllowance = riceAllowance;
            this.phoneAllowance = phoneAllowance;
            this.clothingAllowance = clothingAllowance;
            this.totalAllowances = totalAllowances;
            this.grossSalary = grossSalary;
            this.sss = sss;
            this.philHealth = philHealth;
            this.pagIbig = pagIbig;
            this.withholdingTax = withholdingTax;
            this.totalDeductions = totalDeductions;
            this.netSalary = netSalary;
        }

        public double getHoursWorked() { return hoursWorked; }
        public double getBasicPay() { return basicPay; }
        public double getRiceAllowance() { return riceAllowance; }
        public double getPhoneAllowance() { return phoneAllowance; }
        public double getClothingAllowance() { return clothingAllowance; }
        public double getTotalAllowances() { return totalAllowances; }
        public double getGrossSalary() { return grossSalary; }
        public double getSss() { return sss; }
        public double getPhilHealth() { return philHealth; }
        public double getPagIbig() { return pagIbig; }
        public double getWithholdingTax() { return withholdingTax; }
        public double getTotalDeductions() { return totalDeductions; }
        public double getNetSalary() { return netSalary; }
    }
}
