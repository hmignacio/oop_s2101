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
import dao.LeaveDao;
import model.Employee;
import model.LeaveRequest;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminUI extends mainMenuFunctions {
    private JTable leaveTable;
    private DefaultTableModel leaveModel;
    private LeaveDao leaveDao;
    private EmployeeDao employeeDao;

    public AdminUI() {
        super("MotorPH - Administrator Dashboard");
        this.leaveDao = new LeaveDao();
        this.employeeDao = new EmployeeDao();
        initUI();
    }

    @Override
    protected void initUI() {
        setLayout(new BorderLayout(15, 15));

        // --- 1. Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 25, 112)); // Midnight Blue
        header.setPreferredSize(new Dimension(0, 70));
        
        JLabel lblTitle = new JLabel("  Executive Oversight & Leave Approval", JLabel.LEFT);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.add(lblTitle, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // --- 2. Center: Leave Approval Table (Full Access) ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Pending Leave Requests", 0, 0, 
                new Font("SansSerif", Font.BOLD, 14), new Color(25, 25, 112)));
        
        // Added "Reason" column to the array
        String[] columns = {"Req ID", "Emp #", "Type", "Start", "End", "Status", "Reason"};
        leaveModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        leaveTable = new JTable(leaveModel);
        leaveTable.setRowHeight(25);
        leaveTable.getTableHeader().setReorderingAllowed(false);
        
        // Adjust column width for Reason
        leaveTable.getColumnModel().getColumn(6).setPreferredWidth(250);
        
        refreshLeaveTable();
        centerPanel.add(new JScrollPane(leaveTable), BorderLayout.CENTER);

        // Action Buttons
        JPanel approvalActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnViewReason = new JButton("View Full Reason");
        JButton btnApprove = new JButton("Approve");
        JButton btnReject = new JButton("Reject");

        // Styling Buttons
        styleActionButton(btnViewReason, new Color(70, 130, 180));
        styleActionButton(btnApprove, new Color(34, 139, 34));
        styleActionButton(btnReject, new Color(178, 34, 34));
        
        approvalActions.add(new JLabel("Process Selected Request: "));
        approvalActions.add(btnViewReason);
        approvalActions.add(new JLabel("|"));
        approvalActions.add(btnApprove);
        approvalActions.add(btnReject);
        centerPanel.add(approvalActions, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);

        // --- 3. Sidebar: Oversight Navigation (Read-Only Logic) ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(15, 10, 15, 10)));

        JButton btnOverseeHR = new JButton("Oversee HR Records");
        JButton btnOverseeFinance = new JButton("Oversee Finance");
        JButton btnOverseeIT = new JButton("Oversee IT Security");
        JButton btnLogout = new JButton("Logout");

        styleNavButton(btnOverseeHR);
        styleNavButton(btnOverseeFinance);
        styleNavButton(btnOverseeIT);
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
        sidebar.add(btnLogout);
        add(sidebar, BorderLayout.WEST);

        // --- 4. Listeners ---
        btnViewReason.addActionListener(e -> showFullReason());
        btnApprove.addActionListener(e -> updateLeaveStatus("Approved"));
        btnReject.addActionListener(e -> updateLeaveStatus("Rejected"));

        btnOverseeHR.addActionListener(e -> {
            HRUI hr = new HRUI();
            hr.disableEditing(); // Hides management buttons
            hr.setVisible(true);
        });

        btnOverseeFinance.addActionListener(e -> {
            FinanceUI fin = new FinanceUI();
            fin.setVisible(true);
        });

        btnOverseeIT.addActionListener(e -> {
            ITUI it = new ITUI();
            it.setReadOnly(); // Unmasks passwords but prevents changes
            it.setVisible(true);
        });

        btnLogout.addActionListener(e -> { 
            this.dispose(); 
            new LoginFrame().setVisible(true); 
        });
    }

    private void refreshLeaveTable() {
        leaveModel.setRowCount(0);
        List<LeaveRequest> requests = leaveDao.getAllRequests();
        for (LeaveRequest lr : requests) {
            // Only show Pending requests for Admin to action
            if (lr.getStatus().equalsIgnoreCase("Pending")) {
                leaveModel.addRow(new Object[]{
                    lr.getRequestId(), 
                    lr.getEmployeeNumber(), 
                    lr.getLeaveType(), 
                    lr.getStartDate(), 
                    lr.getEndDate(), 
                    lr.getStatus(),
                    lr.getReason() // Now populated
                });
            }
        }
    }

    private void updateLeaveStatus(String status) {
        int row = leaveTable.getSelectedRow();
        if (row != -1) {
            String id = leaveModel.getValueAt(row, 0).toString();
            if (leaveDao.updateStatus(id, status)) {
                JOptionPane.showMessageDialog(this, "Request successfully " + status);
                refreshLeaveTable();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a leave request first.");
        }
    }

    private void showFullReason() {
        int row = leaveTable.getSelectedRow();
        if (row != -1) {
            String empId = leaveModel.getValueAt(row, 1).toString();
            String reason = leaveModel.getValueAt(row, 6).toString();
            
            JTextArea area = new JTextArea(reason);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setEditable(false);
            area.setBackground(null);
            
            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(300, 150));
            
            JOptionPane.showMessageDialog(this, scroll, "Reason for Request (Emp ID: " + empId + ")", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a request to view reason.");
        }
    }

    private void styleNavButton(JButton b) {
        b.setMaximumSize(new Dimension(220, 45));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }

    private void styleActionButton(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
    }
}
