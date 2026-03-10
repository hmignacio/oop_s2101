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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AdminUI extends mainMenuFunctions {
    private EmployeeDao employeeDao;
    private LeaveDao leaveDao;
    private JTabbedPane tabbedPane;
    private JTextField txtSearch;
    private JTable leaveTable;

    public AdminUI() {
        super("MotorPH - System Administrator Oversight");
        this.employeeDao = new EmployeeDao();
        this.leaveDao = new LeaveDao();
        initUI();
    }

    @Override
    protected void initUI() {
        setLayout(new BorderLayout(10, 10));

        // --- 1. Top Header & Search Panel ---
        JPanel topContainer = new JPanel(new GridLayout(2, 1));
        
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(44, 62, 80));
        JLabel lblTitle = new JLabel("   ADMINISTRATIVE MASTER CONSOLE", JLabel.LEFT);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.add(lblTitle, BorderLayout.WEST);
        
        JButton btnLogout = new JButton("Logout ");
        btnLogout.addActionListener(e -> { this.dispose(); new LoginFrame().setVisible(true); });
        header.add(btnLogout, BorderLayout.EAST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createEtchedBorder());
        txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Find Employee ID");
        searchPanel.add(new JLabel("Global Search: "));
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);
        
        topContainer.add(header);
        topContainer.add(searchPanel);
        add(topContainer, BorderLayout.NORTH);

        // --- 2. Central Tabbed Pane ---
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("HR Records", createHRView());
        tabbedPane.addTab("Finance/Payroll", createFinanceView());
        tabbedPane.addTab("IT Credentials", createITView());
        tabbedPane.addTab("Leave Approvals", createLeaveApprovalView());

        add(tabbedPane, BorderLayout.CENTER);

        // --- 3. Search Logic ---
        btnSearch.addActionListener(e -> performGlobalSearch());
    }

    // --- Leave Approval View ---
    private JPanel createLeaveApprovalView() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        String[] columns = {"Req ID", "Emp #", "Type", "Start (MM-DD-YYYY)", "End (MM-DD-YYYY)", "Status", "Reason"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        leaveTable = new JTable(model);
        refreshLeaveTable();

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnApprove = new JButton("Approve");
        JButton btnReject = new JButton("Reject");
        
        btnApprove.setBackground(new Color(34, 139, 34));
        btnApprove.setForeground(Color.BLACK);
        btnReject.setBackground(new Color(178, 34, 34));
        btnReject.setForeground(Color.BLACK);

        actionPanel.add(btnApprove);
        actionPanel.add(btnReject);

        btnApprove.addActionListener(e -> updateLeaveStatus("Approved"));
        btnReject.addActionListener(e -> updateLeaveStatus("Rejected"));

        panel.add(new JScrollPane(leaveTable), BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void updateLeaveStatus(String status) {
        int row = leaveTable.getSelectedRow();
        if (row != -1) {
            String reqId = leaveTable.getValueAt(row, 0).toString();
            if (leaveDao.updateStatus(reqId, status)) {
                JOptionPane.showMessageDialog(this, "Request " + reqId + " " + status);
                refreshLeaveTable();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a request first.");
        }
    }

    /**
     * UPDATED: Correctly parses Strings to LocalDate before formatting for display
     */
    private void refreshLeaveTable() {
        DefaultTableModel model = (DefaultTableModel) leaveTable.getModel();
        model.setRowCount(0);
        
        // Formatter for the CSV data (adjust pattern if your CSV uses different separators)
        DateTimeFormatter csvFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        // Formatter for UI display
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        
        List<LeaveRequest> requests = leaveDao.getAllRequests();
        for (LeaveRequest req : requests) {
            String startDateStr;
            String endDateStr;
            
            try {
                // Parse the String from model into LocalDate
                LocalDate startDate = LocalDate.parse(req.getStartDate(), csvFormatter);
                LocalDate endDate = LocalDate.parse(req.getEndDate(), csvFormatter);
                
                // Format the LocalDate back into the desired display String
                startDateStr = startDate.format(displayFormatter);
                endDateStr = endDate.format(displayFormatter);
            } catch (Exception e) {
                // Fallback to raw string if parsing fails (e.g., legacy data in CSV)
                startDateStr = req.getStartDate();
                endDateStr = req.getEndDate();
            }

            model.addRow(new Object[]{
                req.getRequestId(), 
                req.getEmployeeNumber(), 
                req.getLeaveType(),
                startDateStr, 
                endDateStr,
                req.getStatus(), 
                req.getReason()
            });
        }
    }

    private void performGlobalSearch() {
        String targetID = txtSearch.getText().trim();
        if (targetID.isEmpty()) return;

        JScrollPane scrollPane = (JScrollPane) tabbedPane.getSelectedComponent();
        JTable currentTable = (JTable) scrollPane.getViewport().getView();
        
        boolean found = false;
        for (int i = 0; i < currentTable.getRowCount(); i++) {
            if (currentTable.getValueAt(i, 0).toString().equals(targetID)) {
                currentTable.setRowSelectionInterval(i, i);
                currentTable.scrollRectToVisible(currentTable.getCellRect(i, 0, true));
                found = true;
                break;
            }
        }
        if (!found) JOptionPane.showMessageDialog(this, "ID " + targetID + " not found.");
    }

    // --- Tab Creators (Read-Only) ---
    private JScrollPane createHRView() {
        String[] cols = {"ID", "Last Name", "First Name", "Position", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        for (Employee e : employeeDao.getAllEmployees()) {
            model.addRow(new Object[]{e.getEmployeeNumber(), e.getLastName(), e.getFirstName(), e.getPosition(), e.getStatus()});
        }
        return new JScrollPane(table);
    }

    private JScrollPane createFinanceView() {
        String[] cols = {"ID", "Last Name", "Basic Salary", "Rice", "Phone", "Clothing"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        for (Employee e : employeeDao.getAllEmployees()) {
            model.addRow(new Object[]{e.getEmployeeNumber(), e.getLastName(), e.getBasicSalary(), e.getRiceSubsidy(), e.getPhoneAllowance(), e.getClothingAllowance()});
        }
        return new JScrollPane(table);
    }

    private JScrollPane createITView() {
        String[] cols = {"ID", "System Role", "Last Login", "Account Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        return new JScrollPane(new JTable(model));
    }
}
