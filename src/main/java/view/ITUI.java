/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author Vladimir Bernardo
 */
import dao.LoginDao;
import model.UserAccount;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ITUI extends mainMenuFunctions {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private LoginDao loginDao;
    
    // Class-level fields to allow Read-Only access
    private JButton btnResetPass;
    private JButton btnChangeRole;
    private JLabel lblTitle;

    public ITUI() {
        super("MotorPH - IT System Administration");
        this.loginDao = new LoginDao();
        initUI();
    }

    @Override
    protected void initUI() {
        setLayout(new BorderLayout(15, 15));

        // --- 1. Header Section ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(45, 52, 54)); // Dark Slate Gray
        
        lblTitle = new JLabel("  System User & Credential Management", JLabel.LEFT);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setPreferredSize(new Dimension(0, 50));
        header.add(lblTitle, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // --- 2. Table Section ---
        String[] columns = {"Employee ID", "Password", "System Role"};
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
        btnResetPass = new JButton("Reset Password");
        btnChangeRole = new JButton("Update Role");
        JButton btnLogout = new JButton("Logout"); 

        // Styling (Text set to White for better contrast on dark colors)
        btnResetPass.setBackground(new Color(9, 132, 227)); 
        btnResetPass.setForeground(Color.BLACK);
        
        btnChangeRole.setBackground(new Color(108, 92, 231)); 
        btnChangeRole.setForeground(Color.BLACK);
        
        btnLogout.setBackground(new Color(214, 48, 49)); 
        btnLogout.setForeground(Color.BLACK);

        controlPanel.add(btnResetPass);
        controlPanel.add(btnChangeRole);
        controlPanel.add(btnLogout);
        add(controlPanel, BorderLayout.SOUTH);

        // --- 4. Event Listeners ---
        btnLogout.addActionListener(e -> {
            this.dispose();
            new LoginFrame().setVisible(true); 
        });

        btnResetPass.addActionListener(e -> resetPasswordLogic());
        btnChangeRole.addActionListener(e -> updateRoleLogic());
    }

    /**
     * Requirement: ITUI must load data from user.csv and show plain-text passwords.
     */
    private void refreshUserTable() {
        tableModel.setRowCount(0);
        List<UserAccount> users = loginDao.getAllUsers(); 
        
        for (UserAccount u : users) {
            tableModel.addRow(new Object[]{
                u.getEmployeeNo(), 
                u.getPassword(), 
                u.getRole()
            });
        }
    }

    /**
     * Requirement: Admin oversight should not be able to change data.
     */
    public void setReadOnly() {
        if (btnResetPass != null) btnResetPass.setVisible(false);
        if (btnChangeRole != null) btnChangeRole.setVisible(false);
        if (lblTitle != null) {
            lblTitle.setText("  SYSTEM VIEW (READ-ONLY OVERSIGHT)");
            lblTitle.setForeground(new Color(255, 184, 108)); // Orange alert color
        }
    }

    private void resetPasswordLogic() {
        int row = userTable.getSelectedRow();
        if (row != -1) {
            String empId = tableModel.getValueAt(row, 0).toString();
            String newPass = JOptionPane.showInputDialog(this, 
                "Enter new password for ID: " + empId, "IT Password Reset", JOptionPane.QUESTION_MESSAGE);
            
            if (newPass != null && !newPass.trim().isEmpty()) {
                if (loginDao.updatePassword(empId, newPass.trim())) {
                    JOptionPane.showMessageDialog(this, "Password updated successfully for " + empId);
                    refreshUserTable();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user from the table first.");
        }
    }

    private void updateRoleLogic() {
        int row = userTable.getSelectedRow();
        if (row != -1) {
            String empId = tableModel.getValueAt(row, 0).toString();
            String[] roles = {"Admin", "HR", "Finance", "IT", "Regular"};
            
            String newRole = (String) JOptionPane.showInputDialog(this, 
                "Select new role for ID: " + empId, "Update System Access", 
                JOptionPane.QUESTION_MESSAGE, null, roles, tableModel.getValueAt(row, 2));

            if (newRole != null) {
                if (loginDao.updateRole(empId, newRole)) {
                    JOptionPane.showMessageDialog(this, "Role updated to: " + newRole);
                    refreshUserTable();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
        }
    }
}
