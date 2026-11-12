package view;

import interfaces.EmployeeDao;
import interfaces.LeaveDao;
import dao.EmployeeCSVDao;
import dao.LeaveCSVDao;
import model.Employee;
import model.LeaveRequest;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** MotorPH Administrator Dashboard: leave approvals and module oversight. */
public class AdminUI extends mainMenuFunctions {
    private JTable leaveTable;
    private DefaultTableModel leaveModel;
    private final LeaveDao leaveDao;
    private EmployeeDao employeeDao;
    private final Employee currentUser;

    public AdminUI(Employee emp) {
        super("MotorPH - Administrator Dashboard");
        this.currentUser = emp;
        this.leaveDao = new LeaveCSVDao();
        this.employeeDao = new EmployeeCSVDao();
        initUI();
    }

    @Override
    protected final void initUI() {
        setLayout(new BorderLayout(15, 15));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 25, 112));
        header.setPreferredSize(new Dimension(0, 70));
        JLabel lblTitle = new JLabel("  Executive Oversight: Admin " + currentUser.getFirstName(), JLabel.LEFT);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.add(lblTitle, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Pending Leave Requests", 0, 0,
                new Font("SansSerif", Font.BOLD, 14), new Color(25, 25, 112)));

        String[] columns = {"Req ID", "Emp #", "Type", "Start", "End", "Status", "Reason"};
        leaveModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        leaveTable = new JTable(leaveModel);
        leaveTable.setRowHeight(25);
        leaveTable.getTableHeader().setReorderingAllowed(false);
        leaveTable.getColumnModel().getColumn(6).setPreferredWidth(250);
        refreshLeaveTable();
        centerPanel.add(new JScrollPane(leaveTable), BorderLayout.CENTER);

        JPanel approvalActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnViewReason = new JButton("View Full Reason");
        JButton btnApprove = new JButton("Approve");
        JButton btnReject = new JButton("Reject");
        styleActionButton(btnViewReason, new Color(70, 130, 180));
        styleActionButton(btnApprove, new Color(34, 139, 34));
        styleActionButton(btnReject, new Color(178, 34, 34));
        approvalActions.add(new JLabel("Process Selected: "));
        approvalActions.add(btnViewReason);
        approvalActions.add(new JLabel("|"));
        approvalActions.add(btnApprove);
        approvalActions.add(btnReject);
        centerPanel.add(approvalActions, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(15, 10, 15, 10)));

        JButton btnOverseeHR = new JButton("Oversee HR Records");
        JButton btnOverseeFinance = new JButton("Oversee Finance");
        JButton btnOverseeIT = new JButton("Oversee IT Security");
        JButton btnBack = new JButton("Back to Main Dashboard");
        JButton btnLogout = new JButton("Logout");
        styleNavButton(btnOverseeHR);
        styleNavButton(btnOverseeFinance);
        styleNavButton(btnOverseeIT);
        styleNavButton(btnBack);
        styleNavButton(btnLogout);

        JLabel lblSidebar = new JLabel("MODULE OVERSIGHT");
        lblSidebar.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblSidebar.setForeground(Color.GRAY);
        sidebar.add(lblSidebar);
        sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
        sidebar.add(btnOverseeHR);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnOverseeFinance);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnOverseeIT);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(btnBack);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(btnLogout);
        add(sidebar, BorderLayout.WEST);

        btnViewReason.addActionListener(e -> showFullReason());
        btnApprove.addActionListener(e -> updateLeaveStatus("Approved"));
        btnReject.addActionListener(e -> updateLeaveStatus("Rejected"));

        btnOverseeHR.addActionListener(e -> {
            HRUI hr = new HRUI(currentUser);
            hr.disableEditing();
            hr.setVisible(true);
        });
        btnOverseeIT.addActionListener(e -> {
            ITUI it = new ITUI(currentUser);
            it.disableEditing();
            it.setVisible(true);
        });
        btnOverseeFinance.addActionListener(e -> new FinanceUI(currentUser).setVisible(true));

        btnBack.addActionListener(e -> returnToMainDashboard());

        btnLogout.addActionListener(e -> logout());

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowActivated(java.awt.event.WindowEvent e) {
                refreshLeaveTable();
            }
        });
        
        // Return to Main Dashboard when X is clicked
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                returnToMainDashboard();
            }
        });
    }

    public void refreshLeaveTable() {
        leaveModel.setRowCount(0);
        List<LeaveRequest> requests = leaveDao.getAllRequests();
        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        for (LeaveRequest lr : requests) {
            if (lr.getStatus().equalsIgnoreCase("Pending")) {
                String startDisp = lr.getStartDate();
                String endDisp = lr.getEndDate();
                try {
                    startDisp = LocalDate.parse(lr.getStartDate()).format(displayFormat);
                    endDisp = LocalDate.parse(lr.getEndDate()).format(displayFormat);
                } catch (Exception e) { /* Keep original date if parsing fails. */ }
                leaveModel.addRow(new Object[]{
                    lr.getRequestId(), lr.getEmployeeNumber(), lr.getLeaveType(),
                    startDisp, endDisp, lr.getStatus(), lr.getReason()
                });
            }
        }
    }

    private void updateLeaveStatus(String status) {
        int row = leaveTable.getSelectedRow();
        if (row != -1) {
            String id = leaveModel.getValueAt(row, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to " + status + " Request #" + id + "?",
                    "Confirm Action", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (leaveDao.updateStatus(id, status)) {
                    JOptionPane.showMessageDialog(this, "Request " + status + " successfully.");
                    refreshLeaveTable();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Could not update the request. It may already be processed or the CSV file could not be saved.",
                            "Leave Request Update Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a leave request first.");
        }
    }

    private void showFullReason() {
        int row = leaveTable.getSelectedRow();
        if (row != -1) {
            String reason = leaveModel.getValueAt(row, 6).toString();
            JTextArea area = new JTextArea(reason);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setEditable(false);
            area.setFont(new Font("SansSerif", Font.PLAIN, 13));
            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(350, 200));
            JOptionPane.showMessageDialog(this, scroll, "Leave Request Reason", JOptionPane.PLAIN_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Select a request to view reason.");
        }
    }

    private void styleNavButton(JButton b) {
        b.setMaximumSize(new Dimension(220, 45));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.PLAIN, 13));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleActionButton(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
    }
    
        private void returnToMainDashboard() {
        for (Window window : Window.getWindows()) {
            if (window instanceof RegularEmployeeUI && window.isDisplayable()) {
                window.setVisible(true);
                window.toFront();
                window.requestFocus();
                this.dispose();
                return;
            }
        }

        JOptionPane.showMessageDialog(this,
            "Main Employee Dashboard was not found.");
    }
}
