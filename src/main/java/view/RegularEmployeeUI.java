 /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author Vladimir Bernardo
 */
import interfaces.AttendanceDao;
import interfaces.LeaveDao;
import dao.AttendanceCSVDao;
import dao.LeaveCSVDao;
import model.Attendance;
import model.Employee;
import model.LeaveRequest;
import service.PayrollService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;

public class RegularEmployeeUI extends mainMenuFunctions {
    private final Employee currentEmployee;
    // Authenticated role determines which management option is displayed.
    private final String currentRole;
    private final AttendanceDao attendanceDao;
    private final LeaveDao leaveDao; 
    private JTable attendanceTable;
    private JButton btnTimeIn, btnTimeOut;
    private JLabel lblClock;

    public RegularEmployeeUI(Employee emp, String role) {
        super("MotorPH - Employee Portal");
        this.currentEmployee = emp;
        this.currentRole = role == null ? "" : role.trim();
        this.attendanceDao = new AttendanceCSVDao();
        this.leaveDao = new LeaveCSVDao(); 
        initUI();
    }

    @Override
    protected final void initUI() {
        setLayout(new BorderLayout(15, 15));
        
        // --- 1. Header ---
        JPanel welcomePanel = new JPanel(new GridLayout(3, 1));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        welcomePanel.setBackground(new Color(240, 240, 245));

        JLabel lblName = new JLabel("Welcome, " + currentEmployee.getFirstName() + " " + currentEmployee.getLastName());
        lblName.setFont(new Font("SansSerif", Font.BOLD, 22));
        
        lblClock = new JLabel();
        lblClock.setFont(new Font("Monospaced", Font.BOLD, 16));
        updateClock();
        
        Timer clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();

        welcomePanel.add(lblName);
        welcomePanel.add(new JLabel("Position: " + currentEmployee.getPosition() + " | ID: " + currentEmployee.getEmployeeNumber()));
        welcomePanel.add(lblClock);

        // --- 2. Sidebar ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(220, 0));

        btnTimeIn = new JButton("Time-In");
        btnTimeOut = new JButton("Time-Out");
        JButton btnApplyLeave = new JButton("Apply for Leave");
        JButton btnLeaveHistory = new JButton("My Leave History");
        JButton btnViewPayslip = new JButton("View Monthly Payslip");
        JButton btnLogout = new JButton("Logout");

        styleSidebarButton(btnTimeIn, new Color(34, 139, 34));   
        styleSidebarButton(btnTimeOut, new Color(178, 34, 34));  
        styleSidebarButton(btnApplyLeave, new Color(70, 130, 180)); 
        styleSidebarButton(btnLeaveHistory, new Color(60, 179, 113));
        styleSidebarButton(btnViewPayslip, new Color(25, 25, 112)); 
        styleSidebarButton(btnLogout, new Color(100, 100, 100)); 

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnTimeIn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnTimeOut);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));
        sidebar.add(btnApplyLeave);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnLeaveHistory);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnViewPayslip);
        addRoleSpecificButtons(sidebar);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnLogout);

       
        // --- 3. Content ---
        attendanceTable = new JTable(new DefaultTableModel(new String[]{"Date", "Time In", "Time Out"}, 0));
        refreshAttendanceTable(); 

        add(welcomePanel, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(new JScrollPane(attendanceTable), BorderLayout.CENTER);

        // --- 4. Listeners ---
        btnTimeIn.addActionListener(e -> recordPunch("IN"));
        btnTimeOut.addActionListener(e -> recordPunch("OUT"));
        
        // Connects to the LeaveRequestFrame we fixed
        btnApplyLeave.addActionListener(e -> {
            new LeaveRequestFrame(currentEmployee.getEmployeeNumber()).setVisible(true);
        });
        
        btnLeaveHistory.addActionListener(e -> showLeaveHistory());
        btnViewPayslip.addActionListener(e -> handlePayslipSelection());
        btnLogout.addActionListener(e -> { this.dispose(); new LoginFrame().setVisible(true); });

        checkAttendanceStatus(); 
    }

    private void updateClock() {
        lblClock.setText("System Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")));
    }

    private void recordPunch(String type) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        String time = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        
        if (attendanceDao.logTime(currentEmployee, date, time, type)) { 
            refreshAttendanceTable();
            checkAttendanceStatus();
            JOptionPane.showMessageDialog(this, type + " recorded successfully.");
        }
    }

    private void checkAttendanceStatus() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        boolean hasIn = attendanceDao.hasRecordForToday(currentEmployee.getEmployeeNumber(), today);
        boolean hasOut = attendanceDao.hasClockedOut(currentEmployee.getEmployeeNumber(), today);
        btnTimeIn.setEnabled(!hasIn);
        btnTimeOut.setEnabled(hasIn && !hasOut);
    }

    private void refreshAttendanceTable() {
        DefaultTableModel model = (DefaultTableModel) attendanceTable.getModel();
        model.setRowCount(0); 
        List<Attendance> logs = attendanceDao.getAllAttendance();
        for (Attendance log : logs) {
            if (log.getEmployeeNumber().equals(currentEmployee.getEmployeeNumber())) {
                model.addRow(new Object[]{log.getDate(), log.getTimeIn(), log.getTimeOut()});
            }
        }
    }

    private void handlePayslipSelection() {
        JPanel selectionPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        String[] months = {"January", "February", "March", "April", "May", "June", 
                           "July", "August", "September", "October", "November", "December"};
        JComboBox<String> monthCombo = new JComboBox<>(months);
        Integer[] years = {2024, 2025, 2026}; 
        JComboBox<Integer> yearCombo = new JComboBox<>(years);
        
        monthCombo.setSelectedIndex(LocalDate.now().getMonthValue() - 1);
        yearCombo.setSelectedItem(LocalDate.now().getYear());

        selectionPanel.add(new JLabel("Select Month:")); selectionPanel.add(monthCombo);
        selectionPanel.add(new JLabel("Select Year:")); selectionPanel.add(yearCombo);

        int result = JOptionPane.showConfirmDialog(this, selectionPanel, "View Payslip History", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            int selMonth = monthCombo.getSelectedIndex() + 1;
            int selYear = (int) yearCombo.getSelectedItem();
            
            List<Attendance> filteredLogs = attendanceDao.getAllAttendance().stream()
                .filter(log -> log.getEmployeeNumber().equals(currentEmployee.getEmployeeNumber()))
                .filter(log -> {
                    try {
                        LocalDate d = LocalDate.parse(log.getDate(), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                        return d.getMonthValue() == selMonth && d.getYear() == selYear;
                    } catch (Exception ex) { return false; }
                }).collect(Collectors.toList());

            if (filteredLogs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No records found for the selected period.");
            } else {
                // --- 1. EARNINGS CALCULATIONS ---
                double totalHours = new PayrollService().calculateTotalHours(filteredLogs);
                double hourlyRate = currentEmployee.getHourlyRate();
                double grossBasic = totalHours * hourlyRate;
                
                double rice = currentEmployee.getRiceSubsidy();
                double phone = currentEmployee.getPhoneAllowance();
                double clothing = currentEmployee.getClothingAllowance();
                double totalAllowances = rice + phone + clothing;

                // --- 2. DEDUCTIONS (Example standard formulas) ---
                // You can replace these with calls to your PayrollService if you have them defined there
                double sss = (grossBasic > 24750) ? 1125.00 : grossBasic * 0.045; 
                double philhealth = grossBasic * 0.025;
                double pagibig = 100.00;
                double totalDeductions = sss + philhealth + pagibig;
                
                double netTakeHome = (grossBasic + totalAllowances) - totalDeductions;

                // --- 3. FORMATTING ---
                java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
                String selectedMonthName = months[selMonth - 1];

                // --- 4. BUILD READ-ONLY HTML VIEW WITH DEDUCTIONS ---
                String payslipHtml = "<html><body style='width: 320px; font-family: sans-serif;'>"
                        + "<div style='text-align: center; background-color: #191970; color: white; padding: 10px;'>"
                        + "<h2 style='margin:0;'>MotorPH</h2>"
                        + "<p style='margin:0;'>Monthly Payslip: " + selectedMonthName + " " + selYear + "</p></div>"
                        + "<br><b>Employee:</b> " + currentEmployee.getFirstName() + " " + currentEmployee.getLastName()
                        + "<br><b>Position:</b> " + currentEmployee.getPosition()
                        + "<hr>"
                        + "<b>Earnings:</b>"
                        + "<table style='width: 100%; font-size: 11px;'>"
                        + "<tr><td>Hours (" + df.format(totalHours)
                        + ") x Rate (" + df.format(hourlyRate)
                        + "):</td><td style='text-align: right;'>"
                        + df.format(grossBasic) + "</td></tr>"
                        + "<tr><td>Allowances (Rice/Phone/Cloth):</td><td style='text-align: right;'>" + df.format(totalAllowances) + "</td></tr>"
                        + "<tr><td><b>Total Gross:</b></td><td style='text-align: right;'><b>PHP " + df.format(grossBasic + totalAllowances) + "</b></td></tr>"
                        + "</table>"
                        + "<br><b style='color: #b22222;'>Deductions:</b>"
                        + "<table style='width: 100%; font-size: 11px; color: #b22222;'>"
                        + "<tr><td>SSS Contribution:</td><td style='text-align: right;'>(" + df.format(sss) + ")</td></tr>"
                        + "<tr><td>PhilHealth:</td><td style='text-align: right;'>(" + df.format(philhealth) + ")</td></tr>"
                        + "<tr><td>Pag-IBIG:</td><td style='text-align: right;'>(" + df.format(pagibig) + ")</td></tr>"
                        + "<tr><td><b>Total Deductions:</b></td><td style='text-align: right;'><b>PHP (" + df.format(totalDeductions) + ")</b></td></tr>"
                        + "</table>"
                        + "<div style='margin-top: 15px; padding: 12px; background-color: #f4f4f9; text-align: center; border: 2px solid #191970;'>"
                        + "<span style='font-size: 12px;'><b>NET TAKE-HOME:</b></span><br>"
                        + "<span style='font-size: 22px; color: #006400;'><b>PHP " + df.format(netTakeHome) + "</b></span>"
                        + "</div>"
                        + "<p style='font-size: 9px; text-align: center; color: #888;'>Generated for Employee ID: " + currentEmployee.getEmployeeNumber() + "</p>"
                        + "</body></html>";

                // Create HTML payslip viewer
                JEditorPane payslipViewer = new JEditorPane();
                payslipViewer.setContentType("text/html");
                payslipViewer.setText(payslipHtml);
                payslipViewer.setEditable(false);
                payslipViewer.setCaretPosition(0);

                // Set preferred size
                payslipViewer.setPreferredSize(new Dimension(420, 520));

                // Add scroll pane
                JScrollPane scrollPane = new JScrollPane(payslipViewer);
                scrollPane.setPreferredSize(new Dimension(440, 540));

                // Create buttons
                JButton btnPrint = new JButton("Print Payslip");
                JButton btnClose = new JButton("Close");

                // Create custom dialog
                JDialog payslipDialog = new JDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Official Payslip Summary",
                    Dialog.ModalityType.APPLICATION_MODAL
                );

                payslipDialog.setLayout(new BorderLayout());

                // Add payslip content
                payslipDialog.add(scrollPane, BorderLayout.CENTER);

                // Create button panel
                JPanel buttonPanel = new JPanel(
                    new FlowLayout(FlowLayout.CENTER, 10, 10)
                );

                buttonPanel.add(btnPrint);
                buttonPanel.add(btnClose);

                payslipDialog.add(buttonPanel, BorderLayout.SOUTH);

                // Print button action
                btnPrint.addActionListener(e -> {
                    try {
                        boolean printed = payslipViewer.print();

                        if (printed) {
                            JOptionPane.showMessageDialog(
                                payslipDialog,
                                "Payslip sent to printer successfully!",
                                "Print Payslip",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        }

                    } catch (PrinterException ex) {
                        JOptionPane.showMessageDialog(
                            payslipDialog,
                            "Unable to print payslip: " + ex.getMessage(),
                            "Printing Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                });

                // Close button action
                btnClose.addActionListener(e -> payslipDialog.dispose());

                // Configure dialog
                payslipDialog.pack();
                payslipDialog.setLocationRelativeTo(this);
                payslipDialog.setVisible(true);
            }
        }
    }
    /**
     * READ-ONLY Leave History with MM-DD-YYYY Date Formatting
     */
    private void showLeaveHistory() {
        String[] cols = {"Type", "Start Date", "End Date", "Status", "Reason"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        // Date Translator
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy");

        List<LeaveRequest> history = leaveDao.getAllRequests().stream()
            .filter(lr -> lr.getEmployeeNumber().equals(currentEmployee.getEmployeeNumber()))
            .collect(Collectors.toList());

        for (LeaveRequest lr : history) {
            String startDisp = lr.getStartDate();
            String endDisp = lr.getEndDate();

            try {
                // Formats the CSV's YYYY-MM-DD to MM-DD-YYYY for the UI
                startDisp = LocalDate.parse(lr.getStartDate()).format(displayFormat);
                endDisp = LocalDate.parse(lr.getEndDate()).format(displayFormat);
            } catch (Exception e) {
                // If already formatted or empty, skip translation
            }

            m.addRow(new Object[]{
                lr.getLeaveType(), 
                startDisp, 
                endDisp, 
                lr.getStatus(), 
                lr.getReason()
            });
        }
        
        JTable historyTable = new JTable(m);
        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setPreferredSize(new Dimension(600, 300));
        
        JOptionPane.showMessageDialog(this, scroll, "My Leave Status History", JOptionPane.PLAIN_MESSAGE);
    }

    private void styleSidebarButton(JButton btn, Color c) {
        btn.setMaximumSize(new Dimension(200, 40)); 
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(c); 
        btn.setForeground(Color.WHITE); 
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setOpaque(true); 
        btn.setBorderPainted(false);
    }
    
    // Show additional management access for authorized roles.
    private void addRoleSpecificButtons(JPanel sidebar) {
        JButton managementButton;

        switch (currentRole.toUpperCase(java.util.Locale.ROOT)) {
            case "HR":
                managementButton = new JButton("HR Management");
                managementButton.addActionListener(e -> new HRUI(currentEmployee).setVisible(true));
                break;
            case "FINANCE":
                managementButton = new JButton("Finance Management");
                managementButton.addActionListener(e -> new FinanceUI(currentEmployee).setVisible(true));
                break;
            case "IT":
                managementButton = new JButton("IT Management");
                managementButton.addActionListener(e -> new ITUI(currentEmployee).setVisible(true));
                break;
            case "ADMIN":
                managementButton = new JButton("Admin Management");
                managementButton.addActionListener(e -> new AdminUI(currentEmployee).setVisible(true));
                break;
            default:
                return; // Regular employees have no management button.
        }

        styleSidebarButton(managementButton, new Color(70, 70, 120));
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(managementButton);
    }
}
