package view;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import dao.OvertimeCSVDao;
import interfaces.OvertimeDao;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.time.format.SignStyle;
import java.util.Locale;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import model.OvertimeRequest;

public class OvertimeRequestFrame extends JFrame {
    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.HOUR_OF_DAY, 1, 2, SignStyle.NOT_NEGATIVE)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .toFormatter(Locale.ROOT)
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME_DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);

    private final String employeeId;
    private final OvertimeDao overtimeDao;
    private DatePicker datePicker;
    private JTextField startTimeField;
    private JTextField endTimeField;
    private JTextField reasonField;
    private DefaultTableModel requestModel;

    public OvertimeRequestFrame(String employeeId) {
        this.employeeId = employeeId;
        this.overtimeDao = new OvertimeCSVDao();
        initUI();
    }

    private void initUI() {
        setTitle("MotorPH - Overtime Request");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Submit Overtime Request"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFormatForDatesCommonEra("MM-dd-yyyy");
        dateSettings.setAllowKeyboardEditing(true);
        datePicker = new DatePicker(dateSettings);
        dateSettings.setDateRangeLimits(LocalDate.now(), null);
        startTimeField = new JTextField(8);
        startTimeField.setToolTipText("Enter time as HH:mm, for example 17:30.");
        endTimeField = new JTextField(8);
        endTimeField.setToolTipText("Enter time as HH:mm, for example 19:30.");
        reasonField = new JTextField(28);

        addFormRow(form, gbc, 0, "Overtime Date:", datePicker);
        addFormRow(form, gbc, 1, "Start Time (HH:mm):", startTimeField);
        addFormRow(form, gbc, 2, "End Time (HH:mm):", endTimeField);
        addFormRow(form, gbc, 3, "Reason:", reasonField);

        JButton submitButton = new JButton("Submit Request");
        JButton refreshButton = new JButton("Refresh My Requests");
        JPanel formActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        formActions.add(refreshButton);
        formActions.add(submitButton);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        form.add(formActions, gbc);
        add(form, BorderLayout.NORTH);

        requestModel = new DefaultTableModel(
                new String[]{"Date", "Start Time", "End Time", "Reason", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable requestTable = new JTable(requestModel);
        requestTable.setRowHeight(24);
        add(new JScrollPane(requestTable), BorderLayout.CENTER);

        submitButton.addActionListener(event -> submitRequest());
        refreshButton.addActionListener(event -> refreshRequests());
        refreshRequests();

        setSize(760, 500);
        setLocationRelativeTo(null);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
            String label, java.awt.Component component) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(component, gbc);
    }

    private void submitRequest() {
        LocalDate overtimeDate = datePicker.getDate();
        String startText = startTimeField.getText().trim();
        String endText = endTimeField.getText().trim();
        String reason = reasonField.getText().trim();

        if (overtimeDate == null || startText.isEmpty() || endText.isEmpty() || reason.isEmpty()) {
            showValidationError("Date, start time, end time, and reason are required.");
            return;
        }
        if (overtimeDate.isBefore(LocalDate.now())) {
            showValidationError("Overtime requests cannot be submitted for a past date.");
            return;
        }

        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.parse(startText, TIME_FORMATTER);
            endTime = LocalTime.parse(endText, TIME_FORMATTER);
        } catch (java.time.format.DateTimeParseException exception) {
            showValidationError("Enter valid 24-hour times in HH:mm format, such as 17:30.");
            return;
        }
        if (!endTime.isAfter(startTime)) {
            showValidationError("End time must be after start time.");
            return;
        }

        String requestId = "OT-" + UUID.randomUUID().toString().toUpperCase(Locale.ROOT);
        OvertimeRequest request = new OvertimeRequest(
                requestId, employeeId, overtimeDate.toString(),
                startTime.format(TIME_DISPLAY_FORMAT), endTime.format(TIME_DISPLAY_FORMAT),
                reason, "Pending");
        if (!overtimeDao.addRequest(request)) {
            JOptionPane.showMessageDialog(this,
                    "The overtime request could not be saved. Please try again.",
                    "Submission Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Overtime request submitted successfully.",
                "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
        startTimeField.setText("");
        endTimeField.setText("");
        reasonField.setText("");
        refreshRequests();
    }

    private void refreshRequests() {
        requestModel.setRowCount(0);
        for (OvertimeRequest request : overtimeDao.findByEmployeeId(employeeId)) {
            requestModel.addRow(new Object[]{
                request.getDate(), request.getStartTime(), request.getEndTime(),
                request.getReason(), request.getStatus()
            });
        }
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Request",
                JOptionPane.WARNING_MESSAGE);
    }
}
