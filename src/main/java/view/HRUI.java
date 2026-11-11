/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author Vladimir Bernardo
 */
import interfaces.EmployeeDao;
import interfaces.LeaveDao;
import interfaces.AttendanceDao;
import interfaces.OvertimeDao;
import dao.EmployeeCSVDao;
import dao.LeaveCSVDao;
import dao.AttendanceCSVDao;
import dao.OvertimeCSVDao;
import model.Employee;
import model.LeaveRequest;
import model.Attendance;
import model.OvertimeRequest;
import utils.Constants;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * MotorPH HR Management Portal
 * Handles CRUD operations for Employee records.
 * @author Vladimir Bernardo
 */
public class HRUI extends mainMenuFunctions {
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> employeeSorter;
    private JTextField employeeSearchField;
    private JLabel employeeSearchStatus;
    private JTable leaveTable;
    private DefaultTableModel leaveModel;
    private JTable attendanceTable;
    private DefaultTableModel attendanceModel;
    private JTable overtimeTable;
    private DefaultTableModel overtimeModel;
    private JComboBox<AttendanceEmployeeOption> attendanceEmployeeFilter;
    private JTextField attendanceDateFilter;
    private final EmployeeDao employeeDao; 
    private final LeaveDao leaveDao;
    private final AttendanceDao attendanceDao;
    private final OvertimeDao overtimeDao;
    private final Employee currentUser;
    private final EmployeeCSVDao employeeCsvDao = new EmployeeCSVDao();
    
    private JButton btnAdd, btnEdit, btnDelete;
    private JButton btnApproveLeave, btnRejectLeave;
    private JLabel lblHeader;

    public HRUI(Employee emp) {
        super("MotorPH - HR Employee Management");
        this.currentUser = emp; 
        this.employeeDao = new EmployeeCSVDao();
        this.leaveDao = new LeaveCSVDao();
        this.attendanceDao = new AttendanceCSVDao();
        this.overtimeDao = new OvertimeCSVDao();
        initUI();
        
        if (lblHeader != null && currentUser != null) {
            lblHeader.setText("  HR Management - Welcome, " + currentUser.getFirstName());
        }
    }

    @Override
    public final void initUI() {
        setLayout(new BorderLayout(15, 15));

        // --- 1. Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 112)); 
        headerPanel.setPreferredSize(new Dimension(0, 60));

        lblHeader = new JLabel("  Employee Master Records", JLabel.LEFT);
        lblHeader.setForeground(Color.WHITE);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerPanel.add(lblHeader, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // --- 2. Employee Table ---
        String[] columns = {
            "Emp #", "Last Name", "First Name", "Birthday", "Address", 
            "Phone", "Email", "SSS", "Philhealth", "TIN", "Pag-ibig", 
            "Status", "Position", "Supervisor", "Rice", "Phone Allw.", "Cloth Allw.", "Hourly"
        };
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        employeeTable = new JTable(tableModel);
        employeeTable.setRowHeight(25);
        employeeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        
        employeeTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        employeeTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        employeeTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        employeeSorter = new TableRowSorter<>(tableModel);
        employeeTable.setRowSorter(employeeSorter);
        employeeSearchField = new JTextField(24);
        employeeSearchStatus = new JLabel();
        JPanel employeeSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        employeeSearchPanel.add(new JLabel("Search employees:"));
        employeeSearchPanel.add(employeeSearchField);
        employeeSearchPanel.add(employeeSearchStatus);
        employeeSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyEmployeeSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyEmployeeSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyEmployeeSearch();
            }
        });

        refreshTable();

        JPanel employeePanel = new JPanel(new BorderLayout());
        employeePanel.add(employeeSearchPanel, BorderLayout.NORTH);
        employeePanel.add(new JScrollPane(employeeTable), BorderLayout.CENTER);
        JTabbedPane dashboardTabs = new JTabbedPane();
        dashboardTabs.addTab("Employees", employeePanel);
        dashboardTabs.addTab("Leave Requests", createLeaveManagementPanel());
        dashboardTabs.addTab("Attendance", createAttendancePanel());
        dashboardTabs.addTab("Overtime Requests", createOvertimeManagementPanel());
        add(dashboardTabs, BorderLayout.CENTER);

        // --- 3. Toolbar ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        btnAdd = new JButton("Add New Employee");
        btnEdit = new JButton("Edit Selected");
        btnDelete = new JButton("Delete Employee");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnLogout = new JButton("Logout");
        JButton btnBack = new JButton("Back to Main Dashboard");

        styleToolbarButton(btnAdd, new Color(34, 139, 34)); 
        styleToolbarButton(btnDelete, new Color(178, 34, 34));
        styleToolbarButton(btnEdit, new Color(70, 130, 180));

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(btnRefresh);
        toolbar.add(btnBack);
        toolbar.add(btnLogout);
        add(toolbar, BorderLayout.SOUTH);

        // --- 4. Listeners ---
        btnRefresh.addActionListener(e -> refreshTable());
        btnBack.addActionListener(e -> {
            for (Window window : Window.getWindows()) {
                if (window instanceof RegularEmployeeUI && window.isDisplayable()) {
                    window.setVisible(true);
                    window.toFront();
                    dispose();
                    return;
                }
            }
            JOptionPane.showMessageDialog(this,
                    "Main Employee Dashboard is not available.",
                    "Navigation Error", JOptionPane.WARNING_MESSAGE);
        });
        btnLogout.addActionListener(e -> logout());
        
        btnAdd.addActionListener(e -> { 
            new EmployeeDialog(this, null).setVisible(true); 
            refreshTable(); 
        });
        
        btnEdit.addActionListener(e -> {
            int row = employeeTable.getSelectedRow();
            if (row != -1) {
                row = employeeTable.convertRowIndexToModel(row);
                String empId = tableModel.getValueAt(row, 0).toString();
                Employee emp = employeeDao.findById(empId);
                if (emp != null) {
                    new EmployeeDialog(this, emp).setVisible(true);
                    refreshTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, Constants.NO_SELECTION_ERROR);
            }
        });

        btnDelete.addActionListener(e -> {
            int row = employeeTable.getSelectedRow();
            if (row != -1) {
                row = employeeTable.convertRowIndexToModel(row);
                String empId = tableModel.getValueAt(row, 0).toString();
                int confirm = JOptionPane.showConfirmDialog(this, "Delete Employee #" + empId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (employeeDao.delete(empId)) refreshTable();
                    else JOptionPane.showMessageDialog(this, "Error deleting employee.");
                }
            } else {
                JOptionPane.showMessageDialog(this, Constants.NO_SELECTION_ERROR);
            }
        });
    }

    private JPanel createLeaveManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        String[] columns = {
            "Request ID", "Emp #", "Employee", "Type", "Start Date",
            "End Date", "Status", "Reason"
        };
        leaveModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        leaveTable = new JTable(leaveModel);
        leaveTable.setRowHeight(25);
        leaveTable.getTableHeader().setReorderingAllowed(false);
        leaveTable.getColumnModel().getColumn(7).setPreferredWidth(250);
        panel.add(new JScrollPane(leaveTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton btnRefreshLeave = new JButton("Refresh Requests");
        JButton btnViewReason = new JButton("View Full Reason");
        btnApproveLeave = new JButton("Approve");
        btnRejectLeave = new JButton("Reject");
        styleToolbarButton(btnApproveLeave, new Color(34, 139, 34));
        styleToolbarButton(btnRejectLeave, new Color(178, 34, 34));
        actions.add(btnRefreshLeave);
        actions.add(btnViewReason);
        actions.add(btnApproveLeave);
        actions.add(btnRejectLeave);
        panel.add(actions, BorderLayout.SOUTH);

        btnRefreshLeave.addActionListener(e -> refreshLeaveTable());
        btnViewReason.addActionListener(e -> showLeaveReason());
        btnApproveLeave.addActionListener(e -> updateLeaveStatus("Approved"));
        btnRejectLeave.addActionListener(e -> updateLeaveStatus("Rejected"));

        refreshLeaveTable();
        return panel;
    }

    private void refreshLeaveTable() {
        leaveModel.setRowCount(0);
        Map<String, Employee> employeesById = new HashMap<>();
        for (Employee employee : employeeDao.getAllEmployees()) {
            employeesById.put(employee.getEmployeeNumber(), employee);
        }

        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy");
        for (LeaveRequest request : leaveDao.getAllRequests()) {
            Employee employee = employeesById.get(request.getEmployeeNumber());
            String employeeName = employee == null ? "Unknown employee"
                    : employee.getFullName();
            leaveModel.addRow(new Object[]{
                request.getRequestId(),
                request.getEmployeeNumber(),
                employeeName,
                request.getLeaveType(),
                formatLeaveDate(request.getStartDate(), displayFormat),
                formatLeaveDate(request.getEndDate(), displayFormat),
                request.getStatus(),
                request.getReason()
            });
        }
    }

    private String formatLeaveDate(String value, DateTimeFormatter displayFormat) {
        try {
            return LocalDate.parse(value).format(displayFormat);
        } catch (java.time.format.DateTimeParseException e) {
            return value;
        }
    }

    private void updateLeaveStatus(String status) {
        int selectedRow = leaveTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a leave request first.");
            return;
        }

        int row = leaveTable.convertRowIndexToModel(selectedRow);
        String requestId = leaveModel.getValueAt(row, 0).toString();
        String currentStatus = leaveModel.getValueAt(row, 6).toString();
        if (!currentStatus.equalsIgnoreCase("Pending")) {
            JOptionPane.showMessageDialog(this,
                    "Only pending leave requests can be approved or rejected.",
                    "Request Already Processed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + status + " Request #" + requestId + "?",
                "Confirm Action", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        if (leaveDao.updateStatus(requestId, status)) {
            JOptionPane.showMessageDialog(this, "Request " + status + " successfully.");
            refreshLeaveTable();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Could not update the request. It may already be processed or the CSV file could not be saved.",
                    "Leave Request Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showLeaveReason() {
        int selectedRow = leaveTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a request to view its reason.");
            return;
        }

        int row = leaveTable.convertRowIndexToModel(selectedRow);
        JTextArea reason = new JTextArea(leaveModel.getValueAt(row, 7).toString());
        reason.setLineWrap(true);
        reason.setWrapStyleWord(true);
        reason.setEditable(false);
        JScrollPane scroll = new JScrollPane(reason);
        scroll.setPreferredSize(new Dimension(350, 200));
        JOptionPane.showMessageDialog(this, scroll, "Leave Request Reason", JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel createOvertimeManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        String[] columns = {
            "Request ID", "Emp #", "Employee", "Date",
            "Start Time", "End Time", "Reason", "Status"
        };
        overtimeModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        overtimeTable = new JTable(overtimeModel);
        overtimeTable.setRowHeight(25);
        overtimeTable.getTableHeader().setReorderingAllowed(false);
        overtimeTable.getColumnModel().getColumn(6).setPreferredWidth(250);
        panel.add(new JScrollPane(overtimeTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        JButton refreshButton = new JButton("Refresh Requests");
        JButton approveButton = new JButton("Approve");
        JButton rejectButton = new JButton("Reject");
        styleToolbarButton(approveButton, new Color(34, 139, 34));
        styleToolbarButton(rejectButton, new Color(178, 34, 34));
        actions.add(refreshButton);
        actions.add(approveButton);
        actions.add(rejectButton);
        panel.add(actions, BorderLayout.SOUTH);

        refreshButton.addActionListener(event -> refreshOvertimeTable());
        approveButton.addActionListener(event -> updateOvertimeStatus("Approved"));
        rejectButton.addActionListener(event -> updateOvertimeStatus("Rejected"));
        refreshOvertimeTable();
        return panel;
    }

    private void refreshOvertimeTable() {
        overtimeModel.setRowCount(0);
        Map<String, Employee> employeesById = new HashMap<>();
        for (Employee employee : employeeDao.getAllEmployees()) {
            employeesById.put(employee.getEmployeeNumber().trim(), employee);
        }

        for (OvertimeRequest request : overtimeDao.getAllRequests()) {
            Employee employee = employeesById.get(request.getEmployeeNumber().trim());
            overtimeModel.addRow(new Object[]{
                request.getRequestId(),
                request.getEmployeeNumber(),
                employee == null ? "Unknown employee" : employee.getFullName(),
                request.getDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getReason(),
                request.getStatus()
            });
        }
    }

    private void updateOvertimeStatus(String status) {
        int selectedRow = overtimeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an overtime request first.",
                    "No Request Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int row = overtimeTable.convertRowIndexToModel(selectedRow);
        String requestId = overtimeModel.getValueAt(row, 0).toString();
        String currentStatus = overtimeModel.getValueAt(row, 7).toString();
        if (!currentStatus.equalsIgnoreCase("Pending")) {
            JOptionPane.showMessageDialog(this,
                    "Only pending overtime requests can be approved or rejected.",
                    "Request Already Processed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + status.toLowerCase(Locale.ROOT)
                        + " overtime request " + requestId + "?",
                "Confirm Action", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        if (overtimeDao.updateStatus(requestId, status)) {
            JOptionPane.showMessageDialog(this, "Overtime request " + status.toLowerCase(Locale.ROOT)
                    + " successfully.", "Request Updated", JOptionPane.INFORMATION_MESSAGE);
            refreshOvertimeTable();
        } else {
            JOptionPane.showMessageDialog(this,
                    "The request could not be updated. It may already be processed or the CSV could not be saved.",
                    "Overtime Request Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createAttendancePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        String[] columns = {"Emp #", "Employee", "Date", "Clock In", "Clock Out"};
        attendanceModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        attendanceTable = new JTable(attendanceModel);
        attendanceTable.setRowHeight(25);
        attendanceTable.getTableHeader().setReorderingAllowed(false);
        panel.add(new JScrollPane(attendanceTable), BorderLayout.CENTER);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        attendanceEmployeeFilter = new JComboBox<>();
        attendanceEmployeeFilter.setPreferredSize(new Dimension(240, 26));
        attendanceDateFilter = new JTextField(10);
        attendanceDateFilter.setToolTipText("Optional date filter: MM/dd/yyyy");
        JButton btnApplyFilters = new JButton("Filter");
        JButton btnClearFilters = new JButton("Clear");
        JButton btnRefreshAttendance = new JButton("Refresh");
        filters.add(new JLabel("Employee:"));
        filters.add(attendanceEmployeeFilter);
        filters.add(new JLabel("Date (MM/dd/yyyy):"));
        filters.add(attendanceDateFilter);
        filters.add(btnApplyFilters);
        filters.add(btnClearFilters);
        filters.add(btnRefreshAttendance);
        panel.add(filters, BorderLayout.NORTH);

        refreshAttendanceEmployeeOptions();
        refreshAttendanceTable();
        btnApplyFilters.addActionListener(e -> refreshAttendanceTable());
        btnClearFilters.addActionListener(e -> {
            attendanceEmployeeFilter.setSelectedIndex(0);
            attendanceDateFilter.setText("");
            refreshAttendanceTable();
        });
        btnRefreshAttendance.addActionListener(e -> {
            refreshAttendanceEmployeeOptions();
            refreshAttendanceTable();
        });
        attendanceDateFilter.addActionListener(e -> refreshAttendanceTable());
        return panel;
    }

    private void refreshAttendanceEmployeeOptions() {
        String selectedEmployeeNumber = null;
        AttendanceEmployeeOption selected =
                (AttendanceEmployeeOption) attendanceEmployeeFilter.getSelectedItem();
        if (selected != null) {
            selectedEmployeeNumber = selected.employeeNumber;
        }

        attendanceEmployeeFilter.removeAllItems();
        attendanceEmployeeFilter.addItem(new AttendanceEmployeeOption(null, "All employees"));
        for (Employee employee : employeeDao.getAllEmployees()) {
            attendanceEmployeeFilter.addItem(new AttendanceEmployeeOption(
                    employee.getEmployeeNumber(),
                    employee.getFullName() + " (" + employee.getEmployeeNumber() + ")"));
        }

        if (selectedEmployeeNumber != null) {
            for (int i = 0; i < attendanceEmployeeFilter.getItemCount(); i++) {
                if (selectedEmployeeNumber.equals(attendanceEmployeeFilter.getItemAt(i).employeeNumber)) {
                    attendanceEmployeeFilter.setSelectedIndex(i);
                    return;
                }
            }
        }
        attendanceEmployeeFilter.setSelectedIndex(0);
    }

    private void refreshAttendanceTable() {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MM/dd/uuuu", Locale.ROOT)
                .withResolverStyle(ResolverStyle.STRICT);
        String dateText = attendanceDateFilter.getText().trim();
        LocalDate selectedDate = null;
        if (!dateText.isEmpty()) {
            try {
                selectedDate = LocalDate.parse(dateText, dateFormat);
            } catch (java.time.format.DateTimeParseException e) {
                JOptionPane.showMessageDialog(this,
                        "Enter a valid date in MM/dd/yyyy format.",
                        "Invalid Date", JOptionPane.WARNING_MESSAGE);
                attendanceDateFilter.requestFocusInWindow();
                return;
            }
        }

        AttendanceEmployeeOption selectedEmployee =
                (AttendanceEmployeeOption) attendanceEmployeeFilter.getSelectedItem();
        String selectedEmployeeNumber = selectedEmployee == null
                ? null : selectedEmployee.employeeNumber;
        Map<String, Employee> employeesById = new HashMap<>();
        for (Employee employee : employeeDao.getAllEmployees()) {
            employeesById.put(employee.getEmployeeNumber(), employee);
        }

        attendanceModel.setRowCount(0);
        for (Attendance attendance : attendanceDao.getAllAttendance()) {
            if (selectedEmployeeNumber != null
                    && !selectedEmployeeNumber.equals(attendance.getEmployeeNumber().trim())) {
                continue;
            }
            if (selectedDate != null) {
                try {
                    if (!selectedDate.equals(LocalDate.parse(attendance.getDate(), dateFormat))) {
                        continue;
                    }
                } catch (java.time.format.DateTimeParseException e) {
                    continue;
                }
            }

            Employee employee = employeesById.get(attendance.getEmployeeNumber().trim());
            String employeeName = employee == null ? "Unknown employee" : employee.getFullName();
            attendanceModel.addRow(new Object[]{
                attendance.getEmployeeNumber(),
                employeeName,
                attendance.getDate(),
                attendance.getTimeIn(),
                attendance.getTimeOut()
            });
        }
    }

    private static final class AttendanceEmployeeOption {
        private final String employeeNumber;
        private final String label;

        private AttendanceEmployeeOption(String employeeNumber, String label) {
            this.employeeNumber = employeeNumber;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /**
     * PUBLIC method used by AdminUI to restrict access.
     */
    public void disableEditing() {
        if (btnAdd != null) btnAdd.setVisible(false);
        if (btnEdit != null) btnEdit.setVisible(false);
        if (btnDelete != null) btnDelete.setVisible(false);
        if (btnApproveLeave != null) btnApproveLeave.setVisible(false);
        if (btnRejectLeave != null) btnRejectLeave.setVisible(false);
        
        if (lblHeader != null) {
            lblHeader.setText("  EMPLOYEE MASTER RECORDS (READ-ONLY VIEW)");
            lblHeader.setForeground(new Color(255, 165, 0)); // Warning Orange
        }
    }

    private void styleToolbarButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Employee> employees = employeeDao.getAllEmployees();
        for (Employee emp : employees) {
            tableModel.addRow(new Object[]{
                emp.getEmployeeNumber(), emp.getLastName(), emp.getFirstName(), emp.getBirthday(),
                emp.getAddress(), emp.getPhoneNumber(), emp.getEmail(), emp.getSssNumber(),
                emp.getPhilhealthNumber(), emp.getTinNumber(), emp.getPagibigNumber(),
                emp.getStatus(), emp.getPosition(), emp.getImmediateSupervisor(),
                String.format("%.2f", emp.getRiceSubsidy()), String.format("%.2f", emp.getPhoneAllowance()),
                String.format("%.2f", emp.getClothingAllowance()), String.format("%.2f", emp.getHourlyRate())
            });
        }
        updateEmployeeSearchStatus();
    }

    private void applyEmployeeSearch() {
        String query = employeeSearchField.getText().trim();
        if (query.isEmpty()) {
            employeeSorter.setRowFilter(null);
        } else {
            String[] searchTerms = query.toLowerCase(Locale.ROOT).split("\\s+");
            employeeSorter.setRowFilter(new RowFilter<TableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
                    StringBuilder searchableRow = new StringBuilder();
                    for (int column = 0; column < entry.getValueCount(); column++) {
                        searchableRow.append(entry.getStringValue(column).toLowerCase(Locale.ROOT))
                                .append(' ');
                    }
                    String rowText = searchableRow.toString();
                    for (String term : searchTerms) {
                        if (!rowText.contains(term)) {
                            return false;
                        }
                    }
                    return true;
                }
            });
        }
        updateEmployeeSearchStatus();
    }

    private void updateEmployeeSearchStatus() {
        int visibleCount = employeeTable.getRowCount();
        if (visibleCount == 0) {
            String query = employeeSearchField.getText().trim();
            employeeSearchStatus.setText(query.isEmpty()
                    ? "No employees available."
                    : "No employees match \"" + query + "\".");
        } else {
            employeeSearchStatus.setText(visibleCount + " employee"
                    + (visibleCount == 1 ? "" : "s"));
        }
    }

    private class EmployeeDialog extends JDialog {
        private final JTextField[] fields;
        private final String[] labels = {
            "Employee #", "Last Name", "First Name", "Birthday (MM-DD-YYYY)", "Address", 
            "Phone #", "Email", "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", 
            "Status", "Position", "Immediate Supervisor", "Basic Salary", "Rice Subsidy",
            "Phone Allowance", "Clothing Allowance", "Gross Salary", "Hourly Rate"
        };
        private final Employee existingEmp;

        public EmployeeDialog(Frame owner, Employee emp) {
            super(owner, emp == null ? "Add New Employee" : "Edit Employee Record", true);
            this.existingEmp = emp;
            setResizable(true);
            setLayout(new BorderLayout());

            JPanel gridPanel = new JPanel(new GridBagLayout());
            gridPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            fields = new JTextField[labels.length];

            for (int i = 0; i < labels.length; i++) {
                GridBagConstraints labelConstraints = new GridBagConstraints();
                labelConstraints.gridx = 0;
                labelConstraints.gridy = i;
                labelConstraints.anchor = GridBagConstraints.LINE_START;
                labelConstraints.insets = new Insets(3, 4, 3, 12);
                gridPanel.add(new JLabel(labels[i] + ":"), labelConstraints);

                fields[i] = new JTextField(20);
                fields[i].setMaximumSize(new Dimension(360, fields[i].getPreferredSize().height));
                GridBagConstraints fieldConstraints = new GridBagConstraints();
                fieldConstraints.gridx = 1;
                fieldConstraints.gridy = i;
                fieldConstraints.weightx = 0.25;
                fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
                fieldConstraints.anchor = GridBagConstraints.LINE_START;
                fieldConstraints.insets = new Insets(3, 4, 3, 4);
                gridPanel.add(fields[i], fieldConstraints);
            }

            if (emp != null) {
                populateFields(emp);
            }
            else{
                fields[0].setText(employeeCsvDao.generateNextEmployeeId());
                fields[0].setEditable(false);
            }
                

            JButton btnSave = new JButton("Save Record");
            btnSave.setFont(new Font("SansSerif", Font.BOLD, 14));
            btnSave.setBackground(new Color(25, 25, 112));
            btnSave.setForeground(Color.BLACK); 
            btnSave.addActionListener(e -> validateAndSave());

            JScrollPane formScroll = new JScrollPane(gridPanel);
            formScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            add(formScroll, BorderLayout.CENTER);
            JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            savePanel.add(btnSave);
            add(savePanel, BorderLayout.SOUTH);

            setPreferredSize(new Dimension(680, 680));
            pack();
            Rectangle screenBounds = getGraphicsConfiguration().getBounds();
            Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
            int availableWidth = screenBounds.width - screenInsets.left - screenInsets.right - 24;
            int availableHeight = screenBounds.height - screenInsets.top - screenInsets.bottom - 48;
            setSize(Math.min(getWidth(), availableWidth), Math.min(getHeight(), availableHeight));
            setMinimumSize(new Dimension(Math.min(480, availableWidth), Math.min(360, availableHeight)));
            setLocationRelativeTo(owner);
        }

        private void populateFields(Employee emp) {
            fields[0].setText(emp.getEmployeeNumber());
            fields[0].setEnabled(false);
            fields[1].setText(emp.getLastName()); fields[2].setText(emp.getFirstName());
            fields[3].setText(emp.getBirthday()); fields[4].setText(emp.getAddress());
            fields[5].setText(emp.getPhoneNumber()); fields[6].setText(emp.getEmail());
            fields[7].setText(emp.getSssNumber()); fields[8].setText(emp.getPhilhealthNumber());
            fields[9].setText(emp.getTinNumber()); fields[10].setText(emp.getPagibigNumber());
            fields[11].setText(emp.getStatus()); fields[12].setText(emp.getPosition());
            fields[13].setText(emp.getImmediateSupervisor());
            fields[14].setText(String.valueOf(emp.getBasicSalary()));
            fields[15].setText(String.valueOf(emp.getRiceSubsidy()));
            fields[16].setText(String.valueOf(emp.getPhoneAllowance()));
            fields[17].setText(String.valueOf(emp.getClothingAllowance()));
            fields[18].setText(String.valueOf(emp.getGrossSalary()));
            fields[19].setText(String.valueOf(emp.getHourlyRate()));
        }

        private void validateAndSave() {
            for (JTextField f : fields) {
                f.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));
            }

            try {
                for (int i = 0; i < fields.length; i++) {
                    if (fields[i].getText().trim().isEmpty()) { 
                        highlightError(fields[i]); 
                        throw new Exception("Field '" + labels[i] + "' is required."); 
                    }
                }

                if (existingEmp == null && employeeDao.findById(fields[0].getText().trim()) != null) {
                    highlightError(fields[0]);
                    throw new Exception("Duplicate Error: Employee #" + fields[0].getText().trim() + " already exists.");
                }

                validatePatterns();

                for (int i = 14; i <= 19; i++) {
                    try { 
                        Double.parseDouble(fields[i].getText().trim()); 
                    } catch (NumberFormatException e) { 
                        highlightError(fields[i]); 
                        throw new Exception("'" + labels[i] + "' must be a valid number."); 
                    }
                }

                int confirm = JOptionPane.showConfirmDialog(this, "Confirm saving record for " + fields[2].getText() + "?", "Save", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    Employee emp = (existingEmp == null) ? new Employee() : existingEmp;
                    mapData(emp); 
                    if (existingEmp == null ? employeeDao.addEmployee(emp) : employeeDao.updateEmployee(emp)) {
                        JOptionPane.showMessageDialog(this, "Success: Record Saved.");
                        dispose();
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        }

        private void validatePatterns() throws Exception {
            if (!fields[5].getText().matches(Constants.CONTACT_NUMBER_PATTERN)) { highlightError(fields[5]); throw new Exception(Constants.INVALID_CONTACT_ERROR); }
            if (!fields[6].getText().matches(Constants.EMAIL_PATTERN)) { highlightError(fields[6]); throw new Exception(Constants.INVALID_EMAIL_ERROR); }
            
            // Birthday: MM-DD-YYYY
            if (!fields[3].getText().trim().matches("\\d{2}-\\d{2}-\\d{4}")) {
                highlightError(fields[3]);
                throw new Exception("Birthday must be in MM-DD-YYYY format.");
            }

            // Government IDs
            if (!fields[7].getText().trim().matches("\\d{2}-\\d{7}-\\d{1}")) { highlightError(fields[7]); throw new Exception("Invalid SSS: 00-0000000-0"); }
            if (!fields[8].getText().trim().matches("\\d{4}-\\d{4}-\\d{4}")) { highlightError(fields[8]); throw new Exception("Invalid PhilHealth: 0000-0000-0000"); }
            if (!fields[9].getText().trim().matches("\\d{3}-\\d{3}-\\d{3}-\\d{3}")) { highlightError(fields[9]); throw new Exception("Invalid TIN: 000-000-000-000"); }
            if (!fields[10].getText().trim().matches("\\d{4}-\\d{4}-\\d{4}")) { highlightError(fields[10]); throw new Exception("Invalid Pag-Ibig: 0000-0000-0000"); }
        }

        private void mapData(Employee emp) {
            emp.setEmployeeNumber(fields[0].getText().trim());
            emp.setLastName(fields[1].getText().trim());
            emp.setFirstName(fields[2].getText().trim());
            emp.setBirthday(fields[3].getText().trim());
            emp.setAddress(fields[4].getText().trim());
            emp.setPhoneNumber(fields[5].getText().trim());
            emp.setEmail(fields[6].getText().trim());
            emp.setSssNumber(fields[7].getText().trim());
            emp.setPhilhealthNumber(fields[8].getText().trim());
            emp.setTinNumber(fields[9].getText().trim());
            emp.setPagibigNumber(fields[10].getText().trim());
            emp.setStatus(fields[11].getText().trim());
            emp.setPosition(fields[12].getText().trim());
            emp.setImmediateSupervisor(fields[13].getText().trim());
            emp.setBasicSalary(Double.parseDouble(fields[14].getText().trim()));
            emp.setRiceSubsidy(Double.parseDouble(fields[15].getText().trim()));
            emp.setPhoneAllowance(Double.parseDouble(fields[16].getText().trim()));
            emp.setClothingAllowance(Double.parseDouble(fields[17].getText().trim()));
            emp.setGrossSalary(Double.parseDouble(fields[18].getText().trim()));
            emp.setHourlyRate(Double.parseDouble(fields[19].getText().trim()));
        }

        private void highlightError(JTextField field) {
            field.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            field.requestFocusInWindow();
        }
    }
}
