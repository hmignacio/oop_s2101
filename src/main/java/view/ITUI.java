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
import java.util.ArrayList;
import java.util.List;

public class ITUI extends mainMenuFunctions {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private LoginDao loginDao;

    public ITUI() {
        super("MotorPH - IT System Administration");
        this.loginDao = new LoginDao();
        initUI();
    }

    @Override
    protected void initUI() {
        setLayout(new BorderLayout(15, 15));

        // --- Header Section ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(45, 52, 54)); // Dark Slate Gray
        JLabel lblTitle = new JLabel(" System User Management", JLabel.LEFT);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setPreferredSize(new Dimension(0, 50));
        header.add(lblTitle, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // --- Table Section ---
        String[] columns = {"Employee ID", "Password (Masked)", "System Role"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = new JTable(tableModel);
        userTable.setRowHeight(25);
        add(new JScrollPane(userTable), BorderLayout.CENTER);

        // --- Control Panel ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnResetPass = new JButton("Reset Password");
        JButton btnChangeRole = new JButton("Update Role");
        JButton btnBack = new JButton("Back to Admin");

        // Styling Buttons
        btnResetPass.setBackground(new Color(9, 132, 227)); // Professional Blue
        btnResetPass.setForeground(Color.WHITE);
        
        controlPanel.add(btnResetPass);
        controlPanel.add(btnChangeRole);
        controlPanel.add(btnBack);
        add(controlPanel, BorderLayout.SOUTH);

        // --- Logic ---
        btnBack.addActionListener(e -> {
            this.dispose();
            new AdminUI().setVisible(true);
        });

        btnResetPass.addActionListener(e -> resetPasswordLogic());

        refreshUserTable();
    }

    private void refreshUserTable() {
        tableModel.setRowCount(0);
        // In a real scenario, you'd add a getAllUsers() method to your LoginDao
        // For now, we simulate the display logic:
        List<UserAccount> users = fetchAllUsersFromCSV(); 
        for (UserAccount u : users) {
            tableModel.addRow(new Object[]{
                u.getEmployeeNo(), 
                "********", // Never show plain text passwords in the UI
                u.getRole()
            });
        }
    }

    private void resetPasswordLogic() {
        int row = userTable.getSelectedRow();
        if (row != -1) {
            String empId = tableModel.getValueAt(row, 0).toString();
            String newPass = JOptionPane.showInputDialog(this, "Enter new password for ID: " + empId);
            if (newPass != null && !newPass.trim().isEmpty()) {
                // Here you would call loginDao.updatePassword(empId, newPass)
                JOptionPane.showMessageDialog(this, "Password updated successfully for " + empId);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user first.");
        }
    }

    // Mock method - you should move this logic into LoginDao.java
    private List<UserAccount> fetchAllUsersFromCSV() {
        List<UserAccount> list = new ArrayList<>();
        // Logic similar to LoginDao authenticate but returning all rows
        return list; 
    }
}
