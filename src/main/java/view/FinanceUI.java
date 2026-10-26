/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author Vladimir Bernardo
 */
import interfaces.EmployeeDao; // Use the interface
import interfaces.AttendanceDao; // Use the interface
import dao.EmployeeCSVDao; // Import the implementation
import dao.AttendanceCSVDao; // Import the implementation
import model.Employee;
import model.Attendance;
import service.PayrollService;
import utils.Constants;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class FinanceUI extends mainMenuFunctions {
    private JTable financeTable;
    private DefaultTableModel tableModel;
    private final EmployeeDao employeeDao;
    private final AttendanceDao attendanceDao;
    private final PayrollService payrollService;
    private Employee currentUser;

    public FinanceUI(Employee emp) {
        super("MotorPH - Finance & Payroll Oversight");
        // FIX: Point the interfaces to the Concrete CSV implementation classes
        this.currentUser = emp; // Store the employee object
        this.employeeDao = new EmployeeCSVDao();
        this.attendanceDao = new AttendanceCSVDao();
        this.payrollService = new PayrollService();
        initUI();
    }

    @Override
    protected final void initUI() { // Added final to prevent overridable method warning
        setLayout(new BorderLayout(15, 15));

        // --- 1. Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 102, 102)); 
        header.setPreferredSize(new Dimension(0, 60));
        JLabel lblTitle = new JLabel("  Payroll Processing & Salary Oversight", JLabel.LEFT);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.add(lblTitle, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // --- 2. Table Section ---
        String[] columns = {"Emp #", "Name", "Basic Salary", "Gross (Standard)", "Net (Standard)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        financeTable = new JTable(tableModel);
        financeTable.setRowHeight(25);
        refreshTable();
        add(new JScrollPane(financeTable), BorderLayout.CENTER);

        // --- 3. Bottom Action Panel ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        
        
        JButton btnGeneratePayslip = new JButton("View Detailed Payslip");
        JButton btnRefresh = new JButton("Refresh List");
        JButton btnLogout = new JButton("Logout");

        // Styling
        
        btnGeneratePayslip.setBackground(new Color(46, 139, 87));

       
        actionPanel.add(btnRefresh);
        actionPanel.add(btnGeneratePayslip);
        actionPanel.add(btnLogout);
        add(actionPanel, BorderLayout.SOUTH);

        // --- 4. Event Listeners ---
        btnRefresh.addActionListener(e -> refreshTable());
        
        btnGeneratePayslip.addActionListener(e -> {
            int row = financeTable.getSelectedRow();
            if (row != -1) {
                handlePayslipGeneration(tableModel.getValueAt(row, 0).toString());
            } else {
                JOptionPane.showMessageDialog(this, Constants.NO_SELECTION_ERROR);
            }
        });

        

        btnLogout.addActionListener(e -> { 
            this.dispose(); 
            new LoginFrame().setVisible(true); 
        });
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        // Ensure your EmployeeDao has findAll() or getAllEmployees()
        List<Employee> employees = employeeDao.getAllEmployees(); 
        
        for (Employee emp : employees) {
            double hours = 160.0; // Standard reference month
            double gross = payrollService.calculateGrossSalary(emp, hours);
            double net = payrollService.calculateNetSalary(emp, gross);

            tableModel.addRow(new Object[]{
                emp.getEmployeeNumber(),
                emp.getLastName() + ", " + emp.getFirstName(),
                String.format("%,.2f", emp.getBasicSalary()),
                String.format("%,.2f", gross),
                String.format("%,.2f", net)
            });
        }
    }

    private void handlePayslipGeneration(String empId) {
        // Ensure your EmployeeDao has findById
        Employee emp = employeeDao.findById(empId);
        if (emp == null) return;

        JPanel selectPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        String[] months = {"January", "February", "March", "April", "May", "June", 
                           "July", "August", "September", "October", "November", "December"};
        JComboBox<String> monthCombo = new JComboBox<>(months);
        Integer[] years = {2024, 2025, 2026}; 
        JComboBox<Integer> yearCombo = new JComboBox<>(years);
        
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        yearCombo.setSelectedItem(LocalDate.now().getYear());

        selectPanel.add(new JLabel("Select Month:")); selectPanel.add(monthCombo);
        selectPanel.add(new JLabel("Select Year:")); selectPanel.add(yearCombo);

        int result = JOptionPane.showConfirmDialog(this, selectPanel, 
                "Payslip Period for " + emp.getFirstName(), JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            int selMonth = monthCombo.getSelectedIndex() + 1;
            int selYear = (int) yearCombo.getSelectedItem();
            
            List<Attendance> allLogs = attendanceDao.getAllAttendance();
            List<Attendance> filtered = allLogs.stream()
                .filter(log -> log.getEmployeeNumber().equals(empId))
                .filter(log -> {
                    try {
                        LocalDate d = LocalDate.parse(log.getDate(), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                        return d.getMonthValue() == selMonth && d.getYear() == selYear;
                    } catch (Exception ex) { return false; }
                }).collect(Collectors.toList());

            if (filtered.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No records found for " + monthCombo.getSelectedItem() + " " + selYear);
            } else {
                double actualHours = payrollService.calculateTotalHours(filtered);
                // Ensure PayslipFrame exists and matches this constructor
                new PayslipFrame(emp, actualHours).setVisible(true);
            }
        }
    }

}
