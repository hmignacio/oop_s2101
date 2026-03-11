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
import dao.AttendanceDao;
import model.Employee;
import model.Attendance;
import service.PayrollService;
import utils.Constants;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class FinanceUI extends mainMenuFunctions {
    private JTable financeTable;
    private DefaultTableModel tableModel;
    private EmployeeDao employeeDao;
    private AttendanceDao attendanceDao;
    private PayrollService payrollService;

    public FinanceUI() {
        super("MotorPH - Finance & Payroll Oversight");
        this.employeeDao = new EmployeeDao();
        this.attendanceDao = new AttendanceDao();
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
        
        JButton btnSummaryReport = new JButton("Generate Monthly Summary");
        JButton btnGeneratePayslip = new JButton("View Detailed Payslip");
        JButton btnRefresh = new JButton("Refresh List");
        JButton btnLogout = new JButton("Logout");

        // Styling Buttons
        btnSummaryReport.setBackground(new Color(70, 130, 180));
        btnSummaryReport.setForeground(Color.BLACK);
        btnGeneratePayslip.setBackground(new Color(46, 139, 87));
        btnGeneratePayslip.setForeground(Color.BLACK);

        actionPanel.add(btnSummaryReport);
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

        btnSummaryReport.addActionListener(e -> generateMonthlySummaryReport());

        btnLogout.addActionListener(e -> { 
            this.dispose(); 
            new LoginFrame().setVisible(true); 
        });
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Employee emp : employeeDao.getAllEmployees()) {
            double hours = 160.0; // Standard reference
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
        Employee emp = employeeDao.findById(empId);
        
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
                new PayslipFrame(emp, actualHours).setVisible(true);
            }
        }
    }

    private void generateMonthlySummaryReport() {
        JPanel selectPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        String[] months = {"January", "February", "March", "April", "May", "June", 
                           "July", "August", "September", "October", "November", "December"};
        JComboBox<String> monthCombo = new JComboBox<>(months);
        Integer[] years = {2024, 2025, 2026}; 
        JComboBox<Integer> yearCombo = new JComboBox<>(years);
        
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        yearCombo.setSelectedItem(LocalDate.now().getYear());

        selectPanel.add(new JLabel("Report Month:")); selectPanel.add(monthCombo);
        selectPanel.add(new JLabel("Report Year:")); selectPanel.add(yearCombo);

        int result = JOptionPane.showConfirmDialog(this, selectPanel, "Generate Summary", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            int selMonth = monthCombo.getSelectedIndex() + 1;
            int selYear = (int) yearCombo.getSelectedItem();
            
            JDialog reportDialog = new JDialog(this, "Monthly Payout Summary: " + monthCombo.getSelectedItem(), true);
            reportDialog.setSize(850, 600);
            reportDialog.setLocationRelativeTo(this);
            reportDialog.setLayout(new BorderLayout(10, 10));

            // Search Header
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JTextField txtSearch = new JTextField(25);
            searchPanel.add(new JLabel("Filter Employee: "));
            searchPanel.add(txtSearch);
            searchPanel.setBorder(BorderFactory.createTitledBorder("Search Controls"));

            // Table Setup
            String[] cols = {"Emp ID", "Full Name", "Gross Salary", "Deductions", "Net Pay"};
            DefaultTableModel reportModel = new DefaultTableModel(cols, 0){
        
            @Override
            public boolean isCellEditable(int row, int column) {
            return false; // This makes the summary report read-only
        }
    };
            JTable reportTable = new JTable(reportModel);
            reportTable.setRowHeight(25);
            reportTable.getTableHeader().setReorderingAllowed(false);
            
            List<Attendance> allLogs = attendanceDao.getAllAttendance();
            double grandTotal = 0;

            for (Employee emp : employeeDao.getAllEmployees()) {
                List<Attendance> filtered = allLogs.stream()
                    .filter(log -> log.getEmployeeNumber().equals(emp.getEmployeeNumber()))
                    .filter(log -> {
                        try {
                            LocalDate d = LocalDate.parse(log.getDate(), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                            return d.getMonthValue() == selMonth && d.getYear() == selYear;
                        } catch (Exception ex) { return false; }
                    }).collect(Collectors.toList());

                if (!filtered.isEmpty()) {
                    double actualHours = payrollService.calculateTotalHours(filtered);
                    double gross = payrollService.calculateGrossSalary(emp, actualHours);
                    double net = payrollService.calculateNetSalary(emp, gross);
                    reportModel.addRow(new Object[]{emp.getEmployeeNumber(), emp.getFullName(), 
                        String.format("%,.2f", gross), String.format("%,.2f", gross - net), String.format("%,.2f", net)});
                    grandTotal += net;
                }
            }

            // Search/Filter Logic
            TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(reportModel);
            reportTable.setRowSorter(sorter);
            txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
                private void filter() {
                    String text = txtSearch.getText();
                    if (text.trim().length() == 0) sorter.setRowFilter(null);
                    else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            });

            // Footer Summary
            JPanel footer = new JPanel(new BorderLayout());
            footer.setBackground(new Color(235, 235, 235));
            JLabel lblTotal = new JLabel("TOTAL MONTHLY PAYOUT: PHP " + String.format("%,.2f", grandTotal));
            lblTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
            lblTotal.setForeground(new Color(0, 102, 102));
            footer.add(lblTotal, BorderLayout.WEST);
            
            JButton btnClose = new JButton("Close Summary");
            btnClose.addActionListener(e -> reportDialog.dispose());
            footer.add(btnClose, BorderLayout.EAST);
            footer.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            reportDialog.add(searchPanel, BorderLayout.NORTH);
            reportDialog.add(new JScrollPane(reportTable), BorderLayout.CENTER);
            reportDialog.add(footer, BorderLayout.SOUTH);
            reportDialog.setVisible(true);
        }
    }
}
