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
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * RegularEmployeeUI - The Employee Self-Service Portal.
 * Features: Real-time Attendance (In/Out), Leave Management, and Dashboard.
 * Author: Vladimir Bernardo
 */
public class RegularEmployeeUI extends mainMenuFunctions {
    private Employee currentEmployee;
    private AttendanceDao attendanceDao;
    private LeaveDao leaveDao;
    private JTable attendanceTable;
    
    // UI Components for real-time manipulation
    private JButton btnTimeIn;
    private JButton btnTimeOut;
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
        
        // --- 1. Welcome Header (North) ---
        JPanel welcomePanel = new JPanel(new GridLayout(3, 1));
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        welcomePanel.setBackground(new Color(240, 240, 245));

        JLabel lblName = new JLabel("Welcome, " + currentEmployee.getFullName());
        lblName.setFont(new Font("SansSerif", Font.BOLD, 22));
        
        JLabel lblDetails = new JLabel("Position: " + currentEmployee.getPosition() + " | ID: " + currentEmployee.getEmployeeNumber());
        lblDetails.setFont(new Font("SansSerif", Font.PLAIN, 14));

        lblClock = new JLabel();
        lblClock.setFont(new Font("Monospaced", Font.BOLD, 16));
        lblClock.setForeground(new Color(25, 25, 112));
        updateClock();
        
        // Start the real-time clock timer
        Timer clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();

        welcomePanel.add(lblName);
        welcomePanel.add(lblDetails);
        welcomePanel.add(lblClock);

        // --- 2. Sidebar Quick Actions (West) ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createTitledBorder("Attendance & Actions"));
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBackground(new Color(245, 245, 245));

        btnTimeIn = new JButton("Time-In (Real-time)");
        btnTimeOut = new JButton("Time-Out (Real-time)");
        JButton btnApplyLeave = new JButton("Apply for Leave");
        JButton btnLeaveHistory = new JButton("My Leave History");
        JButton btnViewPayslip = new JButton("View Last Payslip");
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
        
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebar.add(new JSeparator(SwingConstants.HORIZONTAL)); 
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

        sidebar.add(btnApplyLeave);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnLeaveHistory);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnViewPayslip);
        sidebar.add(Box.createVerticalGlue()); 
        sidebar.add(btnLogout);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- 3. Center Content (Cards + Table) ---
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        cardsPanel.add(createCard("SSS No.", currentEmployee.getSssNumber()));
        cardsPanel.add(createCard("PhilHealth No.", currentEmployee.getPhilhealthNumber()));
        cardsPanel.add(createCard("TIN", currentEmployee.getTinNumber()));

        String[] columns = {"Date", "Time In", "Time Out"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        attendanceTable = new JTable(model);
        refreshAttendanceTable(); 

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("My Recent Attendance Logs"));
        tablePanel.add(new JScrollPane(attendanceTable), BorderLayout.CENTER);

        centerPanel.add(cardsPanel, BorderLayout.NORTH);
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        // --- 4. Logic & Event Listeners ---
        btnTimeIn.addActionListener(e -> recordPunch("IN"));
        btnTimeOut.addActionListener(e -> recordPunch("OUT"));

        btnApplyLeave.addActionListener(e -> {
            new LeaveRequestFrame(currentEmployee.getEmployeeNumber()).setVisible(true);
        });

        btnLeaveHistory.addActionListener(e -> showLeaveHistory());

        btnViewPayslip.addActionListener(e -> {
            new PayslipFrame(currentEmployee, 160.0).setVisible(true);
        });

        btnLogout.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true);
        });

        // --- 5. Final Setup ---
        add(welcomePanel, BorderLayout.NORTH);
        add(sidebar, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        
        checkAttendanceStatus(); 
    }

    // --- Core Logic Methods ---

    private void updateClock() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
        lblClock.setText("Current System Time: " + LocalDateTime.now().format(dtf));
    }

    private void recordPunch(String type) {
        String date = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        String time = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        String empId = currentEmployee.getEmployeeNumber();

        // Safety check for Time-Out without a Time-In
        if (type.equals("OUT") && !attendanceDao.hasRecordForToday(empId, date)) {
            JOptionPane.showMessageDialog(this, "Action Denied: No Time-In record found for today.", "Attendance Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (attendanceDao.logTime(currentEmployee, date, time, type)) {
            JOptionPane.showMessageDialog(this, "Successful Clock-" + type + " at " + time);
            refreshAttendanceTable();
            checkAttendanceStatus();
        } else {
            JOptionPane.showMessageDialog(this, "Could not record attendance. Contact your IT administrator.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkAttendanceStatus() {
        String today = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        String empId = currentEmployee.getEmployeeNumber();
        
        boolean hasIn = attendanceDao.hasRecordForToday(empId, today);
        boolean hasOut = attendanceDao.hasClockedOut(empId, today);

        // Time-In Validation
        if (hasIn) {
            btnTimeIn.setEnabled(false);
            btnTimeIn.setBackground(Color.LIGHT_GRAY);
            btnTimeIn.setToolTipText("Clock-in already recorded for today.");
        } else {
            btnTimeIn.setEnabled(true);
            btnTimeIn.setBackground(new Color(34, 139, 34));
            btnTimeIn.setToolTipText("Click to clock-in for your shift.");
        }

        // Time-Out Validation
        if (!hasIn) {
            btnTimeOut.setEnabled(false);
            btnTimeOut.setBackground(Color.LIGHT_GRAY);
            btnTimeOut.setToolTipText("Must Clock-In before Clocking-Out.");
        } else if (hasOut) {
            btnTimeOut.setEnabled(false);
            btnTimeOut.setBackground(Color.LIGHT_GRAY);
            btnTimeOut.setToolTipText("Clock-out already recorded for today.");
        } else {
            btnTimeOut.setEnabled(true);
            btnTimeOut.setBackground(new Color(178, 34, 34));
            btnTimeOut.setToolTipText("Click to end your shift.");
        }
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

    // --- UI Helper Methods ---
    
    private void styleSidebarButton(JButton btn, Color color) {
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(color);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JPanel createCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createEtchedBorder());
        card.setBackground(Color.WHITE);
        JLabel lblTitle = new JLabel(title, JLabel.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.ITALIC, 11));
        JLabel lblValue = new JLabel(value != null ? value : "N/A", JLabel.CENTER);
        lblValue.setFont(new Font("SansSerif", Font.BOLD, 14));
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.setPreferredSize(new Dimension(150, 60));
        return card;
    }

    private void showLeaveHistory() {
        String[] cols = {"Type", "Start", "End", "Status"};
        DefaultTableModel historyModel = new DefaultTableModel(cols, 0);
        List<LeaveRequest> leaves = leaveDao.getByEmployee(currentEmployee.getEmployeeNumber());
        for (LeaveRequest lr : leaves) {
            historyModel.addRow(new Object[]{lr.getLeaveType(), lr.getStartDate(), lr.getEndDate(), lr.getStatus()});
        }
        JTable historyTable = new JTable(historyModel);
        if (leaves.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No leave history found.");
        } else {
            JOptionPane.showMessageDialog(this, new JScrollPane(historyTable), "My Leave History", JOptionPane.PLAIN_MESSAGE);
        }
    }
}
