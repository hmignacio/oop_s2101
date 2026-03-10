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
import javax.swing.*;
import java.awt.*;

/**
 * Displays the detailed salary breakdown for a specific employee.
 */
public class PayslipFrame extends JFrame {
    
    private Employee employee;
    private PayrollService payrollService;
    private double hoursWorked;

    public PayslipFrame(Employee emp, double hours) {
        this.employee = emp;
        this.hoursWorked = hours;
        this.payrollService = new PayrollService();
        initUI();
    }

    private void initUI() {
        setTitle("MotorPH - Payslip: " + employee.getEmployeeNumber());
        setSize(500, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Header Section ---
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(new Color(25, 25, 112));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("MOTORPH PAYROLL SYSTEM");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        
        JLabel lblPeriod = new JLabel("Payroll Period: Monthly Summary");
        lblPeriod.setForeground(Color.LIGHT_GRAY);

        header.add(lblTitle);
        header.add(lblPeriod);

        // --- Main Content (The Breakdown) ---
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // Employee Info Section
        addDetail(content, "Employee Name:", employee.getFullName());
        addDetail(content, "Position:", employee.getPosition());
        addDetail(content, "Hourly Rate:", "PHP " + employee.getHourlyRate());
        addDetail(content, "Hours Worked:", String.format("%.2f", hoursWorked));
        
        content.add(new JSeparator());
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        // Calculation Logic
        double gross = payrollService.calculateGrossSalary(employee, hoursWorked);
        double sss = 500.00; // Simplified for this example
        double philhealth = gross * 0.03; 
        double pagibig = 100.00;
        double totalDeductions = sss + philhealth + pagibig;
        double netPay = gross - totalDeductions;

        // Earnings
        JLabel lblEarnings = new JLabel("EARNINGS");
        lblEarnings.setFont(new Font("SansSerif", Font.BOLD, 14));
        content.add(lblEarnings);
        addDetail(content, "Gross Salary:", "PHP " + String.format("%,.2f", gross));
        
        content.add(Box.createRigidArea(new Dimension(0, 15)));

        // Deductions
        JLabel lblDeductions = new JLabel("DEDUCTIONS");
        lblDeductions.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblDeductions.setForeground(Color.RED);
        content.add(lblDeductions);
        addDetail(content, "SSS Contribution:", "PHP " + sss);
        addDetail(content, "PhilHealth:", "PHP " + String.format("%.2f", philhealth));
        addDetail(content, "Pag-IBIG:", "PHP " + pagibig);
        
        content.add(new JSeparator());
        
        // Net Pay (The Big Number)
        JLabel lblNet = new JLabel("NET TAKE-HOME PAY");
        lblNet.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblNet.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(Box.createRigidArea(new Dimension(0, 20)));
        content.add(lblNet);

        JLabel lblAmount = new JLabel("PHP " + String.format("%,.2f", netPay));
        lblAmount.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblAmount.setForeground(new Color(34, 139, 34));
        lblAmount.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(lblAmount);

        // --- Footer Buttons ---
        JPanel footer = new JPanel();
        JButton btnPrint = new JButton("Print Payslip");
        JButton btnClose = new JButton("Close");
        footer.add(btnPrint);
        footer.add(btnClose);

        btnClose.addActionListener(e -> this.dispose());

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(content), BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void addDetail(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.add(new JLabel(label), BorderLayout.WEST);
        JLabel val = new JLabel(value);
        val.setFont(new Font("SansSerif", Font.BOLD, 12));
        row.add(val, BorderLayout.EAST);
        panel.add(row);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
    }
}
