 /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author Vladimir Bernardo
 */
import dao.AttendanceDao;
import dao.LeaveDao;
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
import java.time.temporal.ChronoUnit;

public class RegularEmployeeUI extends mainMenuFunctions {
    private Employee currentEmployee;
    private AttendanceDao attendanceDao;
    private LeaveDao leaveDao; 
    private JTable attendanceTable;
    private JButton btnTimeIn, btnTimeOut;
    private JLabel lblClock;

    public RegularEmployeeUI(Employee emp) {
        super("MotorPH - Employee Portal");
        this.currentEmployee = emp;
        this.attendanceDao = new AttendanceDao();
        this.leaveDao = new LeaveDao(); 
        initUI();
    }

    @Override
    protected void initUI() {
        setLayout(new BorderLayout(15, 15));
        
        // --- 1. Header ---
        JPanel welcomePanel = new JPanel(new GridLayout(3, 1));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        welcomePanel.setBackground(new Color(240, 240, 245));

        JLabel lblName = new JLabel("Welcome, " + currentEmployee.getFullName());
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
        btnApplyLeave.addActionListener(e -> new LeaveRequestDialog(this).setVisible(true));
        btnLeaveHistory.addActionListener(e -> showLeaveHistory());
        btnViewPayslip.addActionListener(e -> handlePayslipSelection());
        btnLogout.addActionListener(e -> { this.dispose(); new LoginFrame().setVisible(true); });

        checkAttendanceStatus(); 
    }

    private void updateClock() {
        lblClock.setText("System Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")));
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
            
            List<Attendance> allLogs = attendanceDao.getAllAttendance();
            List<Attendance> filteredLogs = allLogs.stream()
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
                double totalHours = new PayrollService().calculateTotalHours(filteredLogs);
                new PayslipFrame(currentEmployee, totalHours).setVisible(true);
            }
        }
    }

    private void recordPunch(String type) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        String time = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        if (attendanceDao.logTime(currentEmployee, date, time, type)) {
            refreshAttendanceTable();
            checkAttendanceStatus();
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

    // --- LEAVE DIALOG WITH VALIDATION ---
    private class LeaveRequestDialog extends JDialog {
        private JComboBox<String> comboType;
        private JTextField txtStart, txtEnd;
        private JTextArea txtReason;
        private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM-dd-yyyy");

        public LeaveRequestDialog(Frame owner) {
            super(owner, "Submit Leave Request", true);
            setSize(450, 500);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout(10, 10));

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL; gbc.insets = new Insets(5, 5, 5, 5);

            comboType = new JComboBox<>(new String[]{"Sick Leave", "Emergency Leave", "Vacation Leave"});
            txtStart = new JTextField(10); txtStart.setEditable(false);
            txtEnd = new JTextField(10); txtEnd.setEditable(false);
            JButton btnStart = new JButton("...");
            JButton btnEnd = new JButton("...");
            txtReason = new JTextArea(4, 20); txtReason.setLineWrap(true);

            gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Type:"), gbc);
            gbc.gridx = 1; form.add(comboType, gbc);
            gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("Start Date:"), gbc);
            JPanel p1 = new JPanel(new BorderLayout()); p1.add(txtStart); p1.add(btnStart, BorderLayout.EAST);
            gbc.gridx = 1; form.add(p1, gbc);
            gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("End Date:"), gbc);
            JPanel p2 = new JPanel(new BorderLayout()); p2.add(txtEnd); p2.add(btnEnd, BorderLayout.EAST);
            gbc.gridx = 1; form.add(p2, gbc);
            gbc.gridx = 0; gbc.gridy = 3; form.add(new JLabel("Reason:"), gbc);
            gbc.gridx = 1; form.add(new JScrollPane(txtReason), gbc);

            btnStart.addActionListener(e -> txtStart.setText(new CalendarPicker(owner).getSelectedDate()));
            btnEnd.addActionListener(e -> txtEnd.setText(new CalendarPicker(owner).getSelectedDate()));

            JButton btnSubmit = new JButton("Submit to Admin");
            btnSubmit.setBackground(new Color(70, 130, 180)); btnSubmit.setForeground(Color.BLACK);
            btnSubmit.addActionListener(e -> validateAndSubmit());

            add(form, BorderLayout.CENTER);
            add(btnSubmit, BorderLayout.SOUTH);
        }

        private void validateAndSubmit() {
            try {
                if (txtStart.getText().isEmpty() || txtEnd.getText().isEmpty()) 
                    throw new Exception("Please select both Start and End dates.");

                LocalDate today = LocalDate.now();
                LocalDate start = LocalDate.parse(txtStart.getText(), dtf);
                LocalDate end = LocalDate.parse(txtEnd.getText(), dtf);

                if (end.isBefore(start)) 
                    throw new Exception("End Date cannot be before Start Date.");

                String type = comboType.getSelectedItem().toString();
                long daysFromToday = ChronoUnit.DAYS.between(today, start);

                // --- APPLY BUSINESS RULES ---
                if (type.equals("Sick Leave") || type.equals("Emergency Leave")) {
                    if (Math.abs(daysFromToday) > 7) {
                        throw new Exception(type + " is only allowed within 7 days of today.");
                    }
                } 
                else if (type.equals("Vacation Leave")) {
                    if (daysFromToday < -7) {
                        throw new Exception("Vacation Leave cannot be back-dated more than 7 days.");
                    }
                }

                if (txtReason.getText().trim().isEmpty()) throw new Exception("Reason required.");

                int confirm = JOptionPane.showConfirmDialog(this, "Submit application?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    String reqId = "REQ-" + (System.currentTimeMillis() % 100000);
                    LeaveRequest lr = new LeaveRequest(reqId, currentEmployee.getEmployeeNumber(), 
                        type, txtStart.getText(), txtEnd.getText(), "Pending", txtReason.getText());
                    
                    if (leaveDao.addLeaveRequest(lr)) {
                        JOptionPane.showMessageDialog(this, "Submitted successfully!");
                        dispose();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- CALENDAR PICKER ---
    private class CalendarPicker extends JDialog {
        private String selectedDate = "";
        private LocalDate displayDate = LocalDate.now();
        private JPanel daysPanel = new JPanel(new GridLayout(0, 7));
        private JLabel lblMonth = new JLabel("", JLabel.CENTER);

        public CalendarPicker(Frame parent) {
            super(parent, "Select Date", true);
            setSize(350, 350);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());

            JButton btnPrev = new JButton("<");
            JButton btnNext = new JButton(">");
            btnPrev.addActionListener(e -> { displayDate = displayDate.minusMonths(1); updateCalendar(); });
            btnNext.addActionListener(e -> { displayDate = displayDate.plusMonths(1); updateCalendar(); });

            JPanel header = new JPanel(new BorderLayout());
            header.add(btnPrev, BorderLayout.WEST);
            header.add(lblMonth, BorderLayout.CENTER);
            header.add(btnNext, BorderLayout.EAST);
            
            add(header, BorderLayout.NORTH);
            add(daysPanel, BorderLayout.CENTER);
            updateCalendar();
            setVisible(true);
        }

        private void updateCalendar() {
            daysPanel.removeAll();
            lblMonth.setText(displayDate.getMonth().name() + " " + displayDate.getYear());
            String[] days = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
            for (String d : days) daysPanel.add(new JLabel(d, JLabel.CENTER));

            LocalDate first = displayDate.withDayOfMonth(1);
            int gap = first.getDayOfWeek().getValue() % 7;
            for (int i = 0; i < gap; i++) daysPanel.add(new JLabel(""));

            int lastDay = YearMonth.from(displayDate).lengthOfMonth();
            for (int i = 1; i <= lastDay; i++) {
                final int day = i;
                JButton btnDay = new JButton(String.valueOf(i));
                btnDay.addActionListener(e -> {
                    selectedDate = String.format("%02d-%02d-%d", displayDate.getMonthValue(), day, displayDate.getYear());
                    dispose();
                });
                daysPanel.add(btnDay);
            }
            daysPanel.revalidate(); daysPanel.repaint();
        }
        public String getSelectedDate() { return selectedDate; }
    }

    private void showLeaveHistory() {
        String[] cols = {"Type", "Date", "Status"};
        DefaultTableModel m = new DefaultTableModel(cols, 0);
        List<LeaveRequest> history = leaveDao.getAllRequests().stream()
            .filter(lr -> lr.getEmployeeNumber().equals(currentEmployee.getEmployeeNumber()))
            .collect(Collectors.toList());
        for (LeaveRequest lr : history) m.addRow(new Object[]{lr.getLeaveType(), lr.getStartDate(), lr.getStatus()});
        JOptionPane.showMessageDialog(this, new JScrollPane(new JTable(m)), "My Leave Status", JOptionPane.PLAIN_MESSAGE);
    }

    private void styleSidebarButton(JButton btn, Color c) {
        btn.setMaximumSize(new Dimension(200, 40)); btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(c); btn.setForeground(Color.WHITE); btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setOpaque(true); btn.setBorderPainted(false);
    }
}
    

