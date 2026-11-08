/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 * @author Vladimir Bernardo
 */
import dao.LoginCSVDao;
import dao.EmployeeCSVDao;
import model.UserAccount;
import model.Employee;
import interfaces.LoginDao;
import interfaces.EmployeeDao;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * IT System Administration UI
 * Handles User Account creation, password resets, and role updates.
 * @author Vladimir Bernardo
 */
public class ITUI extends mainMenuFunctions {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private final LoginDao loginDao;
    private final EmployeeDao employeeDao;
    private final Employee currentUser;
    private final RegularEmployeeUI employeeDashboard;

    private JButton btnResetPass;
    private JButton btnChangeRole;
    private JButton btnActivateAccount;
    private JButton btnDeleteAcc;
    private JLabel lblTitle;

    public ITUI(Employee emp) {
        this(emp, null);
    }

    public ITUI(Employee emp, RegularEmployeeUI employeeDashboard) {
        super("MotorPH - IT System Administration");
        this.currentUser = emp;
        this.employeeDashboard = employeeDashboard;
        this.loginDao = new LoginCSVDao();
        this.employeeDao = new EmployeeCSVDao();
        initUI();
    }

    @Override
    protected void initUI() {
        setLayout(new BorderLayout(15, 15));

        // --- 1. Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(45, 52, 54));

        lblTitle = new JLabel("  System User & Credential Management", JLabel.LEFT);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setPreferredSize(new Dimension(0, 50));
        header.add(lblTitle, BorderLayout.WEST);

        add(header, BorderLayout.NORTH);

        // --- 2. Table ---
        String[] columns = {"Employee ID", "Full Name", "Password", "System Role", "Account Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = new JTable(tableModel);
        userTable.setRowHeight(25);

        refreshUserTable();
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        // --- 3. Control Panel ---
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        JPanel accountActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        JPanel navigationActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));

        btnActivateAccount = new JButton("Activate Account");
        btnResetPass = new JButton("Reset Password");
        btnChangeRole = new JButton("Update Role");
        btnDeleteAcc = new JButton("Delete Account");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnBack = new JButton("Back to Main Dashboard");
        JButton btnLogout = new JButton("Logout");

        styleITButton(btnActivateAccount, new Color(46, 204, 113));
        styleITButton(btnResetPass, new Color(9, 132, 227));
        styleITButton(btnChangeRole, new Color(108, 92, 231));
        styleITButton(btnDeleteAcc, new Color(178, 34, 34));
        styleDashboardActionButton(btnRefresh);
        styleDashboardActionButton(btnBack);
        styleDashboardActionButton(btnLogout);

        accountActions.add(btnActivateAccount);
        accountActions.add(btnResetPass);
        accountActions.add(btnChangeRole);
        accountActions.add(btnDeleteAcc);
        navigationActions.add(btnRefresh);
        navigationActions.add(btnBack);
        navigationActions.add(btnLogout);
        controlPanel.add(accountActions);
        controlPanel.add(navigationActions);
        add(controlPanel, BorderLayout.SOUTH);

        // --- 4. Listeners ---
        btnRefresh.addActionListener(e -> refreshUserTable());
        btnBack.addActionListener(e -> returnToMainDashboard());
        btnLogout.addActionListener(e -> logout());
        btnResetPass.addActionListener(e -> resetPasswordLogic());
        btnChangeRole.addActionListener(e -> updateRoleLogic());
        btnActivateAccount.addActionListener(e -> activateAccountLogic());
        btnDeleteAcc.addActionListener(e -> deleteAccountLogic());
    }

    private void returnToMainDashboard() {
        if (employeeDashboard != null && employeeDashboard.isDisplayable()) {
            employeeDashboard.setVisible(true);
            employeeDashboard.toFront();
            this.dispose();
            return;
        }

        for (Window window : Window.getWindows()) {
            if (window instanceof RegularEmployeeUI && window.isDisplayable()) {
                window.setVisible(true);
                window.toFront();
                this.dispose();
                return;
            }
        }
        JOptionPane.showMessageDialog(this,
                "Main Employee Dashboard is not available.",
                "Navigation Error", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * PUBLIC method called by AdminUI to enforce Read-Only Oversight.
     * Hides all modification buttons and updates the header.
     */
    public void disableEditing() {
        if (btnResetPass != null) btnResetPass.setVisible(false);
        if (btnChangeRole != null) btnChangeRole.setVisible(false);
        if (btnActivateAccount != null) btnActivateAccount.setVisible(false);
        if (btnDeleteAcc != null) btnDeleteAcc.setVisible(false);
        if (lblTitle != null) {
            lblTitle.setText("  SYSTEM VIEW (READ-ONLY OVERSIGHT)");
            lblTitle.setForeground(new Color(255, 184, 108));
        }
    }

    private void refreshUserTable() {
        tableModel.setRowCount(0);
        List<UserAccount> accounts = loginDao.getAllAccounts();
        List<Employee> employees = employeeDao.getAllEmployees();

        for (Employee emp : employees) {
            String hrId = emp.getEmployeeNumber().trim();

            UserAccount found = null;
            for (UserAccount acc : accounts) {
                if (acc.getEmployeeNo().trim().equals(hrId)) {
                    found = acc;
                    break;
                }
            }

            if (found != null) {
                tableModel.addRow(new Object[]{
                    hrId, emp.getLastName() + ", " + emp.getFirstName(),
                    found.getPassword(), found.getRole(), found.getStatus()
                });
            } else {
                tableModel.addRow(new Object[]{
                    hrId, emp.getLastName() + ", " + emp.getFirstName(),
                    "---", "---", "PENDING ACCOUNT"
                });
            }
        }
    }

    private void activateAccountLogic() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an inactive or pending account first.",
                    "No Account Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = userTable.convertRowIndexToModel(row);
        String empId = tableModel.getValueAt(modelRow, 0).toString().trim();
        String status = tableModel.getValueAt(modelRow, 4).toString().trim();

        if (status.equalsIgnoreCase("ACTIVE")) {
            JOptionPane.showMessageDialog(this, "This account is already active.",
                    "Account Already Active", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (status.equalsIgnoreCase("PENDING ACCOUNT")) {
            String pass = JOptionPane.showInputDialog(this,
                    "Set an initial password for pending employee " + empId + ":");
            if (pass == null) {
                return;
            }
            if (pass.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "An initial password is required.",
                        "Invalid Password", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] roles = {"Regular", "HR", "IT", "Finance", "Admin"};
            String role = (String) JOptionPane.showInputDialog(this, "Assign Role:",
                    "System Access", JOptionPane.QUESTION_MESSAGE, null, roles, roles[0]);
            if (role == null) {
                return;
            }

            if (loginDao.addAccount(new UserAccount(empId, pass.trim(), role))) {
                JOptionPane.showMessageDialog(this, "Account activated successfully for " + empId + ".");
                refreshUserTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "The account could not be activated. It may already exist or the CSV could not be saved.",
                        "Activation Failed", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        if (!status.equalsIgnoreCase("INACTIVE") && !status.equalsIgnoreCase("PENDING")) {
            JOptionPane.showMessageDialog(this,
                    "Only inactive or pending accounts can be activated.",
                    "Invalid Account Status", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Activate the existing account for " + empId + "?",
                "Confirm Activation", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        if (loginDao.activateAccount(empId)) {
            JOptionPane.showMessageDialog(this, "Account activated successfully for " + empId + ".");
            refreshUserTable();
        } else {
            JOptionPane.showMessageDialog(this,
                    "The account could not be activated. It may already be active or the CSV could not be saved.",
                    "Activation Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetPasswordLogic() {
        int row = userTable.getSelectedRow();
        if (row != -1) {
            String empId = tableModel.getValueAt(row, 0).toString();
            String newPass = JOptionPane.showInputDialog(this, "New password for ID: " + empId);
            if (newPass != null && !newPass.trim().isEmpty()) {
                if (loginDao.updatePassword(empId, newPass.trim())) {
                    JOptionPane.showMessageDialog(this, "Password reset successful.");
                    refreshUserTable();
                }
            }
        }
    }

    private void updateRoleLogic() {
        int row = userTable.getSelectedRow();
        if (row != -1) {
            String empId = tableModel.getValueAt(row, 0).toString();
            String[] roles = {"Admin", "HR", "Finance", "IT", "Regular"};
            String newRole = (String) JOptionPane.showInputDialog(this, "Select new role:", "Update Role",
                    JOptionPane.QUESTION_MESSAGE, null, roles, tableModel.getValueAt(row, 3));
            if (newRole != null && loginDao.updateRole(empId, newRole)) {
                refreshUserTable();
            }
        }
    }

    private void deleteAccountLogic() {
        int row = userTable.getSelectedRow();
        if (row != -1) {
            String empId = tableModel.getValueAt(row, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(this,
                "Revoke system access for ID: " + empId + "?",
                "Confirm Revocation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                if (loginDao.deleteAccount(empId)) {
                    JOptionPane.showMessageDialog(this, "Account deleted.");
                    refreshUserTable();
                }
            }
        }
    }

    private void styleITButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    private void styleDashboardActionButton(JButton button) {
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
