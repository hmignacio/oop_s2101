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
        
        // Connect interfaces to CSV implementations
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
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        
        btnCreateAccount = new JButton("Create New Account");
        btnResetPass = new JButton("Reset Password");
        btnChangeRole = new JButton("Update Role");
        btnDeleteAcc = new JButton("Delete Account"); 
        JButton btnRefresh = new JButton("Refresh List");
        JButton btnLogout = new JButton("Logout"); 

        styleITButton(btnCreateAccount, new Color(46, 204, 113)); 
        styleITButton(btnResetPass, new Color(9, 132, 227));      
        styleITButton(btnChangeRole, new Color(108, 92, 231));    
        styleITButton(btnDeleteAcc, new Color(178, 34, 34));      
        styleITButton(btnLogout, new Color(214, 48, 49));         

        controlPanel.add(btnCreateAccount);
        controlPanel.add(btnResetPass);
        controlPanel.add(btnChangeRole);
        controlPanel.add(btnDeleteAcc);
        controlPanel.add(btnRefresh);
        controlPanel.add(btnLogout);
        add(controlPanel, BorderLayout.SOUTH);

        // --- 4. Listeners ---
        btnRefresh.addActionListener(e -> refreshUserTable());
        btnLogout.addActionListener(e -> { this.dispose(); new LoginFrame().setVisible(true); });
        btnResetPass.addActionListener(e -> resetPasswordLogic());
        btnChangeRole.addActionListener(e -> updateRoleLogic());
        btnCreateAccount.addActionListener(e -> createAccountLogic());
        btnDeleteAcc.addActionListener(e -> deleteAccountLogic());
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
            lblTitle.setForeground(new Color(255, 184, 108)); // Warning Orange
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
                    found.getPassword(), found.getRole(), "ACTIVE"
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
        if (row != -1) {
            String status = tableModel.getValueAt(row, 4).toString();
            if (!status.equals("PENDING ACCOUNT")) {
                JOptionPane.showMessageDialog(this, "This employee already has an account.");
                return;
            }

            String empId = tableModel.getValueAt(row, 0).toString().trim();
            String pass = JOptionPane.showInputDialog(this, "Set initial password for " + empId + ":");
            
            if (pass != null && !pass.trim().isEmpty()) {
                String[] roles = {"Regular", "HR", "IT", "Finance", "Admin"};
                String role = (String) JOptionPane.showInputDialog(this, "Assign Role:", "System Access",
                        JOptionPane.QUESTION_MESSAGE, null, roles, roles[0]);
                
                if (role != null) {
                    UserAccount newAccount = new UserAccount(empId, pass.trim(), role);
                    if (loginDao.addAccount(newAccount)) { 
                        JOptionPane.showMessageDialog(this, "Account created successfully for " + empId);
                        refreshUserTable();
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a 'PENDING' employee row.");
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
}