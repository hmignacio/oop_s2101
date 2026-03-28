/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author Vladimir Bernardo
 */
// These are the new imports for the Calendar Library
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import dao.LeaveCSVDao;
import interfaces.LeaveDao;
import model.LeaveRequest;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class LeaveRequestFrame extends JFrame {
    private final String employeeId;
    private final LeaveDao leaveDao;
    private DatePicker dateStart, dateEnd;
    private JTextField txtReason;
    private JComboBox<String> comboType;

    public LeaveRequestFrame(String empId) {
        this.employeeId = empId;
        this.leaveDao = new LeaveCSVDao();
        initUI();
    }

    private void initUI() {
        setTitle("MotorPH - Leave Application");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        // Using a main panel with padding for a cleaner, smaller look
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Components ---
        // Row 0: Type
        gbc.gridx = 0; gbc.gridy = 0; mainPanel.add(new JLabel("Leave Type:"), gbc);
        comboType = new JComboBox<>(new String[]{"Sick Leave", "Vacation Leave", "Emergency Leave"});
        gbc.gridx = 1; mainPanel.add(comboType, gbc);

        // Date Settings
        DatePickerSettings st = new DatePickerSettings();
        st.setFormatForDatesCommonEra("MM-dd-yyyy");
        st.setAllowKeyboardEditing(false);

        // Row 1: Start Date
        gbc.gridx = 0; gbc.gridy = 1; mainPanel.add(new JLabel("Start Date:"), gbc);
        dateStart = new DatePicker(st.copySettings());
        gbc.gridx = 1; mainPanel.add(dateStart, gbc);

        // Row 2: End Date
        gbc.gridx = 0; gbc.gridy = 2; mainPanel.add(new JLabel("End Date:"), gbc);
        dateEnd = new DatePicker(st.copySettings());
        gbc.gridx = 1; mainPanel.add(dateEnd, gbc);

        // Row 3: Reason
        gbc.gridx = 0; gbc.gridy = 3; mainPanel.add(new JLabel("Reason:"), gbc);
        txtReason = new JTextField(15);
        gbc.gridx = 1; mainPanel.add(txtReason, gbc);

        // Row 4: Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSubmit = new JButton("Submit");
        JButton btnCancel = new JButton("Cancel");
        btnPanel.add(btnSubmit);
        btnPanel.add(btnCancel);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        mainPanel.add(btnPanel, gbc);

        // --- LOGIC ---
        btnSubmit.addActionListener(e -> {
            LocalDate start = dateStart.getDate();
            LocalDate end = dateEnd.getDate();
            LocalDate today = LocalDate.now();
            String reason = txtReason.getText().trim();
            String type = (String) comboType.getSelectedItem();

            // 1. BLANK CHECKS
            if (start == null || end == null || reason.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields (Dates & Reason) are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. NEW DATE SEQUENCE VALIDATION
            // Check if end date comes BEFORE start date
            if (end.isBefore(start)) {
                JOptionPane.showMessageDialog(this, "End date cannot be earlier than the Start date.", "Date Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. 7-DAY BUSINESS LOGIC
            long diff = ChronoUnit.DAYS.between(today, start);
            boolean allowed = false;
            String errorMsg = "";

            if (type.contains("Sick") || type.contains("Emergency")) {
                if (diff >= -7 && diff <= 7) allowed = true;
                else errorMsg = type + " must be filed within 7 days of occurrence.";
            } else {
                if (diff >= -7) allowed = true;
                else errorMsg = "Vacation Leave must be filed at least 7 days before or for future dates.";
            }

            if (allowed) {
                String rId = "LR-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
                LeaveRequest lr = new LeaveRequest(rId, employeeId, type, start.toString(), end.toString(), "Pending", reason);
                if (leaveDao.addLeaveRequest(lr)) {
                    JOptionPane.showMessageDialog(this, "Application submitted successfully!");
                    this.dispose();
                }
            } else {
                JOptionPane.showMessageDialog(this, errorMsg, "Policy Violation", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnCancel.addActionListener(ev -> this.dispose());

        add(mainPanel);
        
        // FIX: Using pack() instead of setSize makes it a "little bit smaller" 
        // while ensuring all components fit perfectly.
        pack(); 
        setLocationRelativeTo(null);
    }
}
