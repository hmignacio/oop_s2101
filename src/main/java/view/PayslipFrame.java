
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author Vladimir Bernardo
 */

import model.Employee;
import service.PayrollService;
import utils.Constants;
import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * Displays the detailed salary breakdown for a specific employee.
 */
public class PayslipFrame extends JFrame {
    
    private Employee employee;
    private PayrollService payrollService;
    private double hoursWorked;
    private DecimalFormat df = new DecimalFormat("#,##0.00");

    public PayslipFrame(Employee emp, double hours) {
        this.employee = emp;
        this.hoursWorked = hours;
        this.payrollService = new PayrollService();
        initUI();
    }

    private void initUI() {
        setTitle("MotorPH Official Payslip - " + employee.getEmployeeNumber());
        setSize(450, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. Calculations using Constants
        double grossSalary = payrollService.calculateGrossSalary(employee, hoursWorked);
        
        // Government Deductions based on 2026 rates
        double sss = Math.min(grossSalary * Constants.SSS_RATE, Constants.SSS_MAX_CONTRIBUTION);
        double philhealth = Math.min(grossSalary * Constants.PHILHEALTH_RATE, Constants.PHILHEALTH_MAX_CONTRIBUTION);
        double pagibig = Math.min(grossSalary * Constants.PAGIBIG_RATE_HI, Constants.PAGIBIG_MAX_CONTRIBUTION);
        
        double totalDeductions = sss + philhealth + pagibig;
        double taxableIncome = grossSalary - totalDeductions;
        double tax = taxableIncome * Constants.TAX_RATE;
        double netPay = grossSalary - totalDeductions - tax;

        // --- UI Construction ---
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        content.setBackground(Color.WHITE);

        // Header
        JLabel lblHeader = new JLabel("MOTORPH PAYSLIP", JLabel.CENTER);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(lblHeader);
        content.add(Box.createRigidArea(new Dimension(0, 20)));

        // Employee Info
        addDetail(content, "Employee Name:", employee.getFullName());
        addDetail(content, "ID Number:", employee.getEmployeeNumber());
        addDetail(content, "Position:", employee.getPosition());
        content.add(new JSeparator());
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        // Earnings Section
        addSectionHeader(content, "EARNINGS");
        addDetail(content, "Basic Pay (Hours x Rate):", df.format(hoursWorked * employee.getHourlyRate()));
        addDetail(content, "Rice Subsidy:", df.format(employee.getRiceSubsidy()));
        addDetail(content, "Phone Allowance:", df.format(employee.getPhoneAllowance()));
        addDetail(content, "Clothing Allowance:", df.format(employee.getClothingAllowance()));
        addDetail(content, "TOTAL GROSS SALARY:", "PHP " + df.format(grossSalary));
        
        content.add(Box.createRigidArea(new Dimension(0, 15)));
        content.add(new JSeparator());

        // Deductions Section
        addSectionHeader(content, "DEDUCTIONS");
        addDetail(content, "SSS Contribution:", df.format(sss));
        addDetail(content, "PhilHealth:", df.format(philhealth));
        addDetail(content, "Pag-IBIG:", df.format(pagibig));
        addDetail(content, "Withholding Tax:", df.format(tax));
        addDetail(content, "TOTAL DEDUCTIONS:", "PHP " + df.format(totalDeductions + tax));

        content.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Final Net Pay
        JPanel netPanel = new JPanel(new BorderLayout());
        netPanel.setBackground(new Color(240, 240, 240));
        netPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JLabel lblNetLabel = new JLabel("NET TAKE-HOME PAY:");
        lblNetLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel lblNetValue = new JLabel("PHP " + df.format(netPay));
        lblNetValue.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblNetValue.setForeground(new Color(0, 102, 0));
        
        netPanel.add(lblNetLabel, BorderLayout.WEST);
        netPanel.add(lblNetValue, BorderLayout.EAST);
        content.add(netPanel);

        add(new JScrollPane(content), BorderLayout.CENTER);

        // Footer
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());
        add(btnClose, BorderLayout.SOUTH);
    }

    private void addSectionHeader(JPanel panel, String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(Color.GRAY);
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private void addDetail(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        row.add(new JLabel(label), BorderLayout.WEST);
        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 12));
        row.add(val, BorderLayout.EAST);
        panel.add(row);
    }
}
