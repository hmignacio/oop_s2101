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

import dao.LeaveDao;
import model.LeaveRequest;
import javax.swing.*;
import java.awt.*;
import java.util.UUID;
import java.time.LocalDate; // Needed for date logic

public class LeaveRequestFrame extends JFrame {
    private String employeeId;
    private LeaveDao leaveDao;
    private JComboBox<String> comboType;
    private DatePicker dateStart; 
    private DatePicker dateEnd;
    private JTextField txtReason;

    public LeaveRequestFrame(String empId) {
        this.employeeId = empId;
        this.leaveDao = new LeaveDao();
        initUI();
    }

    private void initUI() {
        setTitle("MotorPH - New Leave Request");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 10, 10));

        // Generate a random Request ID (e.g., LR-1234)
        String requestId = "LR-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        add(new JLabel(" Request ID:"));
        add(new JLabel(requestId));

        add(new JLabel(" Leave Type:"));
        comboType = new JComboBox<>(new String[]{"Sick", "Vacation", "Emergency"});
        add(comboType);

        DatePickerSettings startSettings = new DatePickerSettings();
        startSettings.setFormatForDatesCommonEra("MM-dd-yyyy");
        startSettings.setAllowKeyboardEditing(false); 

        DatePickerSettings endSettings = new DatePickerSettings();
        endSettings.setFormatForDatesCommonEra("MM-dd-yyyy");
        endSettings.setAllowKeyboardEditing(false);

        // 2. Initialize the DatePickers
        dateStart = new DatePicker(startSettings);
        dateEnd = new DatePicker(endSettings);

        // 3. Add to Grid
        add(new JLabel(" Start Date:"));
        add(dateStart);
        
        add(new JLabel(" End Date:"));
        add(dateEnd);
        // ---------------------------------------

        add(new JLabel(" Reason:"));
        txtReason = new JTextField();
        add(txtReason);

        JButton btnSubmit = new JButton("Submit Request");
        JButton btnCancel = new JButton("Cancel");

        // FIX: Updated the logic to pull from dateStart/dateEnd instead of text fields
        btnSubmit.addActionListener(e -> {
            if (dateStart.getDate() == null || dateEnd.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Please select both Start and End dates.");
                return;
            }

            LeaveRequest req = new LeaveRequest(
                requestId,
                employeeId,
                comboType.getSelectedItem().toString(),
                dateStart.getDateStringOrEmptyString(), // Get date as MM-dd-yyyy
                dateEnd.getDateStringOrEmptyString(),   // Get date as MM-dd-yyyy
                "Pending",
                txtReason.getText()
            );

            if (leaveDao.addRequest(req)) {
                JOptionPane.showMessageDialog(this, "Leave Request Submitted Successfully!");
                this.dispose();
            }
        });

        btnCancel.addActionListener(e -> this.dispose());

        add(btnSubmit);
        add(btnCancel);
    }
}
