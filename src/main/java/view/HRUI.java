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
import dao.EmployeeCSVDao;
import model.Employee;
import utils.Constants;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * MotorPH HR Management Portal
 * Handles CRUD operations for Employee records.
 * @author Vladimir Bernardo
 */
public class HRUI extends mainMenuFunctions {
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private final EmployeeDao employeeDao; 
    private final Employee currentUser;    
    
    private JButton btnAdd, btnEdit, btnDelete;
    private JLabel lblHeader;

    public HRUI(Employee emp) {
        super("MotorPH - HR Employee Management");
        this.currentUser = emp; 
        this.employeeDao = new EmployeeCSVDao(); 
        
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
        
        refreshTable();
        add(new JScrollPane(employeeTable), BorderLayout.CENTER);

        // --- 3. Toolbar ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        btnAdd = new JButton("Add New Employee");
        btnEdit = new JButton("Edit Selected");
        btnDelete = new JButton("Delete Employee");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnLogout = new JButton("Logout");

        styleToolbarButton(btnAdd, new Color(34, 139, 34)); 
        styleToolbarButton(btnDelete, new Color(178, 34, 34));
        styleToolbarButton(btnEdit, new Color(70, 130, 180));

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(btnRefresh);
        toolbar.add(btnLogout);
        add(toolbar, BorderLayout.SOUTH);

        // --- 4. Listeners ---
        btnRefresh.addActionListener(e -> refreshTable());
        btnLogout.addActionListener(e -> { this.dispose(); new LoginFrame().setVisible(true); });
        
        btnAdd.addActionListener(e -> { 
            new EmployeeDialog(this, null).setVisible(true); 
            refreshTable(); 
        });
        
        btnEdit.addActionListener(e -> {
            int row = employeeTable.getSelectedRow();
            if (row != -1) {
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

    /**
     * PUBLIC method used by AdminUI to restrict access.
     */
    public void disableEditing() {
        if (btnAdd != null) btnAdd.setVisible(false);
        if (btnEdit != null) btnEdit.setVisible(false);
        if (btnDelete != null) btnDelete.setVisible(false);
        
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
    }

    private class EmployeeDialog extends JDialog {
        private final JTextField[] fields;
        private final String[] labels = {
            "Employee #", "Last Name", "First Name", "Birthday (MM-DD-YYYY)", "Address", 
            "Phone #", "Email", "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", 
            "Status", "Position", "Immediate Supervisor", "Rice Subsidy", 
            "Phone Allowance", "Clothing Allowance", "Hourly Rate"
        };
        private final Employee existingEmp;

        public EmployeeDialog(Frame owner, Employee emp) {
            super(owner, emp == null ? "Add New Employee" : "Edit Employee Record", true);
            this.existingEmp = emp;
            setSize(500, 700);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());

            JPanel gridPanel = new JPanel(new GridLayout(0, 2, 10, 8));
            gridPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            fields = new JTextField[labels.length];

            for (int i = 0; i < labels.length; i++) {
                gridPanel.add(new JLabel(labels[i] + ":"));
                fields[i] = new JTextField();
                gridPanel.add(fields[i]);
            }

            if (emp != null) populateFields(emp);

            JButton btnSave = new JButton("SAVE RECORD");
            btnSave.setFont(new Font("SansSerif", Font.BOLD, 14));
            btnSave.setBackground(new Color(25, 25, 112));
            btnSave.setForeground(Color.BLACK); 
            btnSave.addActionListener(e -> validateAndSave());

            add(new JScrollPane(gridPanel), BorderLayout.CENTER);
            add(btnSave, BorderLayout.SOUTH);
        }

        private void populateFields(Employee emp) {
            fields[0].setText(emp.getEmployeeNumber()); 
            fields[0].setEditable(false); 
            fields[1].setText(emp.getLastName()); fields[2].setText(emp.getFirstName());
            fields[3].setText(emp.getBirthday()); fields[4].setText(emp.getAddress());
            fields[5].setText(emp.getPhoneNumber()); fields[6].setText(emp.getEmail());
            fields[7].setText(emp.getSssNumber()); fields[8].setText(emp.getPhilhealthNumber());
            fields[9].setText(emp.getTinNumber()); fields[10].setText(emp.getPagibigNumber());
            fields[11].setText(emp.getStatus()); fields[12].setText(emp.getPosition());
            fields[13].setText(emp.getImmediateSupervisor());
            fields[14].setText(String.valueOf(emp.getRiceSubsidy()));
            fields[15].setText(String.valueOf(emp.getPhoneAllowance()));
            fields[16].setText(String.valueOf(emp.getClothingAllowance()));
            fields[17].setText(String.valueOf(emp.getHourlyRate()));
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

                for (int i = 14; i <= 17; i++) {
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
            emp.setRiceSubsidy(Double.parseDouble(fields[14].getText().trim()));
            emp.setPhoneAllowance(Double.parseDouble(fields[15].getText().trim()));
            emp.setClothingAllowance(Double.parseDouble(fields[16].getText().trim()));
            emp.setHourlyRate(Double.parseDouble(fields[17].getText().trim()));
        }

        private void highlightError(JTextField field) {
            field.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            field.requestFocusInWindow();
        }
    }
}