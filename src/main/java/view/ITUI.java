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

    private JButton btnResetPass;
    private JButton btnChangeRole;
    private JButton btnCreateAccount;
    private JButton btnDeleteAcc;
    private JLabel lblTitle;

    public ITUI(Employee emp) {
        super("MotorPH - IT System Administration");
        this.currentUser = emp;
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
        JPanel controlPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        controlPanel.setBorder(
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        );

        btnCreateAccount = new JButton("Create New Account");
        btnResetPass = new JButton("Reset Password");
        btnChangeRole = new JButton("Update Role");
        btnDeleteAcc = new JButton("Delete Account");
        JButton btnRefresh = new JButton("Refresh List");
        JButton btnBack = new JButton("Back to Main Dashboard");

        styleITButton(btnCreateAccount, new Color(46, 204, 113));
        styleITButton(btnResetPass, new Color(9, 132, 227));
        styleITButton(btnChangeRole, new Color(108, 92, 231));
        styleITButton(btnDeleteAcc, new Color(178, 34, 34));
        styleITButton(btnBack, new Color(70, 70, 120));

        controlPanel.add(btnCreateAccount);
        controlPanel.add(btnResetPass);
        controlPanel.add(btnChangeRole);
        controlPanel.add(btnDeleteAcc);
        controlPanel.add(btnRefresh);
        controlPanel.add(btnBack);
        add(controlPanel, BorderLayout.SOUTH);

        // --- 4. Listeners ---
        btnRefresh.addActionListener(e -> refreshUserTable());
        btnBack.addActionListener(e -> returnToMainDashboard());
        btnResetPass.addActionListener(e -> resetPasswordLogic());
        btnChangeRole.addActionListener(e -> updateRoleLogic());
        btnCreateAccount.addActionListener(e -> createAccountLogic());
        btnDeleteAcc.addActionListener(e -> deleteAccountLogic());
        
        
        // Return to Main Dashboard when X is clicked
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                returnToMainDashboard();
            }
        });
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
                "Main Employee Dashboard was not found. Please log in again.",
                "Navigation Error",
                JOptionPane.WARNING_MESSAGE);
}

    /**
     * PUBLIC method called by AdminUI to enforce Read-Only Oversight.
     * Hides all modification buttons and updates the header.
     */
    public void disableEditing() {
        if (btnResetPass != null) btnResetPass.setVisible(false);
        if (btnChangeRole != null) btnChangeRole.setVisible(false);
        if (btnCreateAccount != null) btnCreateAccount.setVisible(false);
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
                    "********", found.getRole(), "ACTIVE"
                });
            } else {
                tableModel.addRow(new Object[]{
                    hrId, emp.getLastName() + ", " + emp.getFirstName(),
                    "---", "---", "PENDING ACCOUNT"
                });
            }
        }
    }

    
        private void createAccountLogic() {
            int row = userTable.getSelectedRow();

            if (row == -1) {
                JOptionPane.showMessageDialog(this,
                    "Please select a pending employee account.");
                return;
            }

            String status = tableModel.getValueAt(row, 4).toString();

            if (!status.equals("PENDING ACCOUNT")) {
                JOptionPane.showMessageDialog(this,
                    "This employee already has an account.");
                return;
            }

            String empId = tableModel.getValueAt(row, 0).toString().trim();

            JPasswordField passwordField = new JPasswordField(20);

            int result = JOptionPane.showConfirmDialog(
                this,
                passwordField,
                "Set Password for " + empId + " (Minimum 8 characters)",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            );

            if (result != JOptionPane.OK_OPTION) {
                return;
            }

            char[] passwordChars = passwordField.getPassword();
            String password = new String(passwordChars);
            java.util.Arrays.fill(passwordChars, '\0');

            if (password.length() < 8) {
                JOptionPane.showMessageDialog(this,
                    "Password must contain at least 8 characters.",
                    "Invalid Password",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] roles = {"Regular", "HR", "IT", "Finance", "Admin"};

            String role = (String) JOptionPane.showInputDialog(
                this,
                "Assign Role:",
                "System Access",
                JOptionPane.QUESTION_MESSAGE,
                null,
                roles,
                roles[0]
            );

            if (role != null) {
                UserAccount newAccount =
                    new UserAccount(empId, password, role);

                if (loginDao.addAccount(newAccount)) {
                    JOptionPane.showMessageDialog(this,
                        "Account created successfully for " + empId);
                    refreshUserTable();
                }
            }
        }


    
            private void resetPasswordLogic() {
                int row = userTable.getSelectedRow();

                if (row == -1) {
                    JOptionPane.showMessageDialog(this,
                        "Please select an employee account.");
                    return;
                }

                String empId = tableModel.getValueAt(row, 0).toString().trim();

                JPasswordField passwordField = new JPasswordField(20);

                int result = JOptionPane.showConfirmDialog(
                    this,
                    passwordField,
                    "Reset Password for " + empId + " (Minimum 8 characters)",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                );

                if (result != JOptionPane.OK_OPTION) {
                    return;
                }

                char[] passwordChars = passwordField.getPassword();
                String newPassword = new String(passwordChars);
                java.util.Arrays.fill(passwordChars, '\0');

                if (newPassword.length() < 8) {
                    JOptionPane.showMessageDialog(this,
                        "Password must contain at least 8 characters.",
                        "Invalid Password",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (loginDao.updatePassword(empId, newPassword)) {
                    JOptionPane.showMessageDialog(this,
                        "Password reset successfully.");
                    refreshUserTable();
                }
            }


    
    private void updateRoleLogic() {
        int row = userTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select an employee account.");
            return;
        }

        String empId = tableModel.getValueAt(row, 0)
                                 .toString().trim();

        String currentEmpId = currentUser.getEmployeeNumber().trim();

        // Prevent IT users from changing their own role
        if (empId.equals(currentEmpId)) {
            JOptionPane.showMessageDialog(this,
                "You cannot change your own system role.",
                "Access Denied",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] roles = {
            "Admin", "HR", "Finance", "IT", "Regular"
        };

        String newRole = (String) JOptionPane.showInputDialog(
            this,
            "Select new role:",
            "Update Role",
            JOptionPane.QUESTION_MESSAGE,
            null,
            roles,
            tableModel.getValueAt(row, 3)
        );

        if (newRole != null && loginDao.updateRole(empId, newRole)) {
            JOptionPane.showMessageDialog(this,
                "Employee role updated successfully.");
            refreshUserTable();
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
}
