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
    private Timer clockTimer;
    private LeaveRequestFrame leaveRequestFrame;
    private OvertimeRequestFrame overtimeRequestFrame;

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
        
        clockTimer = new Timer(1000, e -> updateClock());
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
        JButton btnLeaveRequest = new JButton("Leave Request");
        JButton btnLeaveHistory = new JButton("My Leave History");
        JButton btnMyDetails = new JButton("My Details");
        JButton btnViewPayslip = new JButton("View Monthly Payslip");
        JButton btnOvertimeRequest = new JButton("Overtime Request");
        JButton btnLogout = new JButton("Logout");

        styleSidebarButton(btnTimeIn, new Color(0x16A34A));
        styleSidebarButton(btnTimeOut, new Color(0xB91C1C));
        styleSidebarButton(btnOvertimeRequest, new Color(0x2563EB));
        styleSidebarButton(btnLeaveRequest, new Color(0x0D9488));
        styleSidebarButton(btnViewPayslip, new Color(0x191970));
        styleSidebarButton(btnLeaveHistory, new Color(0x0284C7));
        styleSidebarButton(btnMyDetails, new Color(0x475569));
        styleSidebarButton(btnLogout, new Color(0x374151));

        sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        sidebar.add(btnTimeIn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(btnTimeOut);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnOvertimeRequest);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(btnLeaveRequest);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(btnViewPayslip);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(btnLeaveHistory);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(btnMyDetails);
        addRoleSpecificButtons(sidebar);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
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
        
        btnLeaveRequest.addActionListener(e -> openLeaveRequestForm());
        
        btnLeaveHistory.addActionListener(e -> showLeaveHistory());
        btnMyDetails.addActionListener(e -> showMyDetails());
        btnViewPayslip.addActionListener(e -> handlePayslipSelection());
        btnOvertimeRequest.addActionListener(e -> openOvertimeRequestForm());
        btnLogout.addActionListener(e -> logout());

        checkAttendanceStatus(); 
    }

    @Override
    public void dispose() {
        if (clockTimer != null) {
            clockTimer.stop();
        }
        super.dispose();
    }

    private void openLeaveRequestForm() {
        try {
            if (leaveRequestFrame == null || !leaveRequestFrame.isDisplayable()) {
                leaveRequestFrame = new LeaveRequestFrame(currentEmployee.getEmployeeNumber());
            }
            showRequestForm(leaveRequestFrame);
        } catch (RuntimeException exception) {
            showRequestFormError("Leave Request", exception);
        }
    }

    private void openOvertimeRequestForm() {
        try {
            if (overtimeRequestFrame == null || !overtimeRequestFrame.isDisplayable()) {
                overtimeRequestFrame = new OvertimeRequestFrame(currentEmployee.getEmployeeNumber());
            }
            showRequestForm(overtimeRequestFrame);
        } catch (RuntimeException exception) {
            showRequestFormError("Overtime Request", exception);
        }
    }

    private void showRequestForm(JFrame form) {
        form.setLocationRelativeTo(this);
        form.setVisible(true);
        form.toFront();
        form.requestFocus();
    }

    private void showRequestFormError(String formName, RuntimeException exception) {
        System.err.println("Unable to open " + formName + " form.");
        exception.printStackTrace(System.err);
        String failureReason = exception.getMessage();
        if (failureReason == null || failureReason.trim().isEmpty()) {
            failureReason = exception.getClass().getSimpleName();
        } else {
            failureReason = exception.getClass().getSimpleName() + ": " + failureReason;
        }
        JOptionPane.showMessageDialog(this,
                "The " + formName + " form could not be opened.\n\nReason: " + failureReason,
                formName + " Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showMyDetails() {
        JDialog detailsDialog = new JDialog(this, "My Details", Dialog.ModalityType.APPLICATION_MODAL);
        detailsDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        addProfileDetail(detailsPanel, "Employee ID:", currentEmployee.getEmployeeNumber());
        addProfileDetail(detailsPanel, "First Name:", currentEmployee.getFirstName());
        addProfileDetail(detailsPanel, "Last Name:", currentEmployee.getLastName());
        addProfileDetail(detailsPanel, "Birthday:", currentEmployee.getBirthday());
        addProfileDetail(detailsPanel, "Address:", currentEmployee.getAddress());
        addProfileDetail(detailsPanel, "Phone Number:", currentEmployee.getPhoneNumber());
        addProfileDetail(detailsPanel, "Email:", currentEmployee.getEmail());

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> detailsDialog.dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(btnClose);

        detailsDialog.add(detailsPanel, BorderLayout.CENTER);
        detailsDialog.add(actions, BorderLayout.SOUTH);
        detailsDialog.pack();
        detailsDialog.setLocationRelativeTo(this);
        detailsDialog.setVisible(true);
    }

    private void addProfileDetail(JPanel panel, String label, String value) {
        panel.add(new JLabel(label));
        JTextField valueField = new JTextField(value == null ? "" : value);
        valueField.setEditable(false);
        valueField.setFocusable(false);
        panel.add(valueField);
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
                PayrollService payrollService = new PayrollService();
                double totalHours = payrollService.calculateTotalHours(filteredLogs);
                PayrollService.PayrollBreakdown breakdown =
                        payrollService.calculatePayroll(currentEmployee, totalHours);
                JEditorPane payslipViewer = PayslipRenderer.createPayslip(
                        currentEmployee, breakdown, selMonth, selYear);
                JScrollPane scrollPane = new JScrollPane(payslipViewer);
                scrollPane.setPreferredSize(new Dimension(560, 620));

                JButton btnPrint = new JButton("Print Payslip");
                JButton btnClose = new JButton("Close");
                JDialog payslipDialog = new JDialog(
                    SwingUtilities.getWindowAncestor(this),
                    "Official Payslip Summary",
                    Dialog.ModalityType.APPLICATION_MODAL
                );
                payslipDialog.setLayout(new BorderLayout());
                payslipDialog.setResizable(true);
                payslipDialog.add(scrollPane, BorderLayout.CENTER);
                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
                buttonPanel.add(btnPrint);
                buttonPanel.add(btnClose);
                payslipDialog.add(buttonPanel, BorderLayout.SOUTH);

                btnPrint.addActionListener(e -> {
                    try {
                        if (payslipViewer.print()) {
                            JOptionPane.showMessageDialog(
                                payslipDialog,
                                "Payslip sent to printer successfully.",
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
                btnClose.addActionListener(e -> payslipDialog.dispose());
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
        Dimension buttonSize = new Dimension(200, 36);
        btn.setPreferredSize(buttonSize);
        btn.setMinimumSize(buttonSize);
        btn.setMaximumSize(buttonSize);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(c);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        Color hoverColor = new Color(
                (int) (c.getRed() * 0.88),
                (int) (c.getGreen() * 0.88),
                (int) (c.getBlue() * 0.88));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent event) {
                if (btn.isEnabled()) {
                    btn.setBackground(hoverColor);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent event) {
                btn.setBackground(c);
            }
        });
    }
    
    // Show additional management access for authorized roles.
    private void addRoleSpecificButtons(JPanel sidebar) {
        JButton managementButton;

        switch (currentRole.toUpperCase(java.util.Locale.ROOT)) {
            case "HR":
                managementButton = new JButton("HR Management");
                managementButton.addActionListener(e -> {
                    HRUI hrDashboard = new HRUI(currentEmployee);
                    hrDashboard.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    hrDashboard.setVisible(true);
                    this.setVisible(false);
                });
                break;

            case "FINANCE":
                managementButton = new JButton("Finance Management");
                managementButton.addActionListener(e -> {
                    FinanceUI financeDashboard = new FinanceUI(currentEmployee);
                    financeDashboard.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    financeDashboard.setVisible(true);
                    this.setVisible(false);
                });
                break;

            case "IT":
                managementButton = new JButton("IT Management");
                managementButton.addActionListener(e -> {
                    ITUI itDashboard = new ITUI(currentEmployee, this);
                    itDashboard.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    itDashboard.setVisible(true);
                    this.setVisible(false);
                });
                break;

            case "ADMIN":
                managementButton = new JButton("Admin Management");
                managementButton.addActionListener(e -> {
                    AdminUI adminDashboard = new AdminUI(currentEmployee);
                    adminDashboard.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    adminDashboard.setVisible(true);
                    this.setVisible(false);
                });
                break;

            default:
                return; // Regular employees have no management button.
        }

        styleSidebarButton(managementButton, new Color(0x6D4CC3));
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebar.add(managementButton);
    }
}
