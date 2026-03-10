/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author Vladimir Bernardo
 */
import dao.EmployeeDao;
import model.Employee;
import service.PayrollService;
import utils.Constants;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class FinanceUI extends mainMenuFunctions {
    private JTable financeTable;
    private DefaultTableModel tableModel;
    private EmployeeDao employeeDao;
    private PayrollService payrollService;

    public FinanceUI() {
        super("MotorPH - Finance & Payroll Oversight");
        this.employeeDao = new EmployeeDao();
        this.payrollService = new PayrollService();
        initUI();
    }

    @Override
    protected void initUI() {
        setLayout(new BorderLayout(15, 15));

        // --- 1. Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 102)); // Deep Teal
        header.setPreferredSize(new Dimension(0, 60));
        JLabel lblTitle = new JLabel("  Payroll Processing & Salary Oversight", JLabel.LEFT);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.add(lblTitle, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // --- 2. Table Section ---
        String[] columns = {"Emp #", "Name", "Basic Salary", "Rice", "Phone", "Clothing", "Gross Salary", "Net Salary"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        financeTable = new JTable(tableModel);
        refreshTable();
        add(new JScrollPane(financeTable), BorderLayout.CENTER);

        // --- 3. Bottom Action Panel ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton btnGeneratePayslip = new JButton("View Detailed Payslip");
        JButton btnRefresh = new JButton("Refresh Data");
        JButton btnLogout = new JButton("Logout");

        // Styling
        btnGeneratePayslip.setBackground(new Color(46, 139, 87));
        btnGeneratePayslip.setForeground(Color.BLACK);

        actionPanel.add(btnRefresh);
        actionPanel.add(btnGeneratePayslip);
        actionPanel.add(btnLogout);
        add(actionPanel, BorderLayout.SOUTH);

        // --- 4. Logic ---
        btnRefresh.addActionListener(e -> refreshTable());
        
        btnGeneratePayslip.addActionListener(e -> {
            int row = financeTable.getSelectedRow();
            if (row != -1) {
                showPayslipDialog(tableModel.getValueAt(row, 0).toString());
            } else {
                JOptionPane.showMessageDialog(this, Constants.NO_SELECTION_ERROR);
            }
        });

        btnLogout.addActionListener(e -> { this.dispose(); new LoginFrame().setVisible(true); });
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Employee emp : employeeDao.getAllEmployees()) {
            // Using placeholder hours (e.g., 160) for the overview table
            double hoursWorked = 160.0; 
            double gross = payrollService.calculateGrossSalary(emp, hoursWorked);
            double net = payrollService.calculateNetSalary(emp, gross);

            tableModel.addRow(new Object[]{
                emp.getEmployeeNumber(),
                emp.getLastName() + ", " + emp.getFirstName(),
                String.format("%.2f", emp.getBasicSalary()),
                String.format("%.2f", emp.getRiceSubsidy()),
                String.format("%.2f", emp.getPhoneAllowance()),
                String.format("%.2f", emp.getClothingAllowance()),
                String.format("%.2f", gross),
                String.format("%.2f", net)
            });
        }
    }

    private void showPayslipDialog(String empId) {
        Employee emp = employeeDao.findById(empId);
        // In a real scenario, you'd fetch actual attendance logs here
        double hours = 160.0; 
        double gross = payrollService.calculateGrossSalary(emp, hours);
        double net = payrollService.calculateNetSalary(emp, gross);

        String message = String.format(
            "PAYSLIP FOR: %s\n" +
            "------------------------------\n" +
            "Total Hours: %.2f\n" +
            "Gross Pay: %.2f\n" +
            "Allowances: %.2f\n" +
            "------------------------------\n" +
            "NET TAKE HOME: %.2f",
            emp.getFullName(), hours, gross, 
            (emp.getRiceSubsidy() + emp.getPhoneAllowance() + emp.getClothingAllowance()),
            net
        );
        
        JOptionPane.showMessageDialog(this, message, "Employee Payslip", JOptionPane.INFORMATION_MESSAGE);
    }
}
