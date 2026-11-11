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
        DatePickerSettings startSettings = new DatePickerSettings();
        startSettings.setFormatForDatesCommonEra("MM-dd-yyyy");
        startSettings.setAllowKeyboardEditing(false);

        DatePickerSettings endSettings = new DatePickerSettings();
        endSettings.setFormatForDatesCommonEra("MM-dd-yyyy");
        endSettings.setAllowKeyboardEditing(false);

        // Row 1: Start Date
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(new JLabel("Start Date:"), gbc);

        dateStart = new DatePicker(startSettings);
        gbc.gridx = 1;
        mainPanel.add(dateStart, gbc);

        // Row 2: End Date
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(new JLabel("End Date:"), gbc);

        dateEnd = new DatePicker(endSettings);
        gbc.gridx = 1;
        mainPanel.add(dateEnd, gbc);
        
        // Row 3: Reason
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(new JLabel("Reason:"), gbc);

        txtReason = new JTextField(15);

        gbc.gridx = 1;
        mainPanel.add(txtReason, gbc);

        // Apply restrictions only after both DatePickers exist
        comboType.addActionListener(e -> updateDateRestrictions());
        updateDateRestrictions();



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


        String reason = txtReason.getText().trim();
        String type = (String) comboType.getSelectedItem();

        // 1. REQUIRED FIELD VALIDATION
        if (start == null || end == null || reason.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "All fields (Dates & Reason) are required.",
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        LocalDate today = LocalDate.now();

        // 2. PREVENT PAST DATE APPLICATIONS
        if (start.isBefore(today) || end.isBefore(today)) {
            JOptionPane.showMessageDialog(this,
                "Past dates are not allowed for leave applications.",
                "Date Restriction",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. VALIDATE DATE SEQUENCE
        if (end.isBefore(start)) {
            JOptionPane.showMessageDialog(this,
                "End date cannot be earlier than Start date.",
                "Date Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 4. VACATION LEAVE - 7 DAYS ADVANCE NOTICE
        if ("Vacation Leave".equalsIgnoreCase(type)) {
            LocalDate minimumVacationDate = today.plusDays(7);

            if (start.isBefore(minimumVacationDate)) {
                JOptionPane.showMessageDialog(this,
                    "Vacation Leave must be filed at least 7 days in advance.",
                    "Policy Violation",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // 5. SICK LEAVE - PREVENT DUPLICATE DATES PER EMPLOYEE
        if ("Sick Leave".equalsIgnoreCase(type)) {

            for (LeaveRequest existing : leaveDao.getAllRequests()) {

                // Only check the current employee's requests
                if (!existing.getEmployeeNumber().trim()
                        .equals(employeeId.trim())) {
                    continue;
                }

                // Only compare Sick Leave applications
                if (!existing.getLeaveType()
                        .equalsIgnoreCase("Sick Leave")) {
                    continue;
                }

                // Rejected requests do not block new applications
                if (existing.getStatus()
                        .equalsIgnoreCase("Rejected")) {
                    continue;
                }

                try {
                    LocalDate existingStart =
                        LocalDate.parse(existing.getStartDate());

                    LocalDate existingEnd =
                        LocalDate.parse(existing.getEndDate());

                    // Check for overlapping leave dates
                    boolean overlaps =
                        !end.isBefore(existingStart) &&
                        !start.isAfter(existingEnd);

                    if (overlaps) {
                        JOptionPane.showMessageDialog(this,
                            "You already have a Sick Leave application " +
                            "covering one or more of the selected dates.",
                            "Duplicate Sick Leave",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        "Unable to verify existing Sick Leave dates.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }

        // 6. GENERATE UNIQUE LEAVE REQUEST ID
        String rId = "LR-" +
            UUID.randomUUID().toString()
                .substring(0, 4).toUpperCase();

        // 7. CREATE LEAVE REQUEST
        // Preserve the complete reason, including commas
        LeaveRequest lr = new LeaveRequest(
            rId,
            employeeId,
            type,
            start.toString(),
            end.toString(),
            "Pending",
            reason
        );

        // 8. SAVE LEAVE REQUEST
        if (leaveDao.addLeaveRequest(lr)) {
            JOptionPane.showMessageDialog(this,
                "Application submitted successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

            this.dispose();

        } else {
            JOptionPane.showMessageDialog(this,
                "Unable to submit leave application.",
                "Submission Error",
                JOptionPane.ERROR_MESSAGE);
        }
    });

        
        


        btnCancel.addActionListener(ev -> this.dispose());

        add(mainPanel);
        
        // FIX: Using pack() instead of setSize makes it a "little bit smaller" 
        // while ensuring all components fit perfectly.
        pack(); 
        setLocationRelativeTo(null);
    }
    
    
   
        private void updateDateRestrictions() {
            String type = (String) comboType.getSelectedItem();

            LocalDate today = LocalDate.now();

            LocalDate minimumDate =
                "Vacation Leave".equals(type)
                    ? today.plusDays(7)
                    : today;

            // Apply restrictions to the constructed DatePickers
            dateStart.getSettings().setDateRangeLimits(minimumDate, null);
            dateEnd.getSettings().setDateRangeLimits(minimumDate, null);

            // Clear dates that are no longer valid
            if (dateStart.getDate() != null &&
                dateStart.getDate().isBefore(minimumDate)) {
                dateStart.clear();
            }

            if (dateEnd.getDate() != null &&
                dateEnd.getDate().isBefore(minimumDate)) {
                dateEnd.clear();
            }
        }


}
