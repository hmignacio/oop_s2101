/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package view;

/**
 *
 * @author Vladimir Bernardo
 */
import dao.EmployeeDao;
import model.Employee;
import utils.Constants;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class HRUI extends mainMenuFunctions {
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private EmployeeDao employeeDao;

    public HRUI() {
        super("MotorPH - HR Employee Management");
        this.employeeDao = new EmployeeDao();
        initUI();
    }

    @Override
    protected void initUI() {
        setLayout(new BorderLayout(15, 15));

        // --- 1. Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 112)); 
        headerPanel.setPreferredSize(new Dimension(0, 60));

        JLabel lblHeader = new JLabel("  Employee Master Records", JLabel.LEFT);
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
        
        refreshTable();
        add(new JScrollPane(employeeTable), BorderLayout.CENTER);

        // --- 3. Toolbar ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        JButton btnAdd = new JButton("Add New Employee");
        JButton btnEdit = new JButton("Edit Selected");
        JButton btnDelete = new JButton("Delete Employee");
        JButton btnRefresh = new JButton("Refresh");
        JButton btnBack = new JButton("Logout");

        btnAdd.setBackground(new Color(34, 139, 34)); 
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setOpaque(true);
        btnAdd.setBorderPainted(false);

        btnDelete.setBackground(new Color(178, 34, 34)); 
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setOpaque(true);
        btnDelete.setBorderPainted(false);

        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(new JSeparator(SwingConstants.VERTICAL));
        toolbar.add(btnRefresh);
        toolbar.add(btnBack);
        add(toolbar, BorderLayout.SOUTH);

        // --- 4. Listeners ---
        btnRefresh.addActionListener(e -> refreshTable());
        btnBack.addActionListener(e -> { this.dispose(); new LoginFrame().setVisible(true); });
        btnAdd.addActionListener(e -> { new EmployeeDialog(this, null).setVisible(true); refreshTable(); });
        
        btnEdit.addActionListener(e -> {
            int row = employeeTable.getSelectedRow();
            if (row != -1) {
                String empId = tableModel.getValueAt(row, 0).toString();
                Employee emp = employeeDao.findById(empId);
                new EmployeeDialog(this, emp).setVisible(true);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, Constants.NO_SELECTION_ERROR);
            }
        });

        btnDelete.addActionListener(e -> {
            int row = employeeTable.getSelectedRow();
            if (row != -1) {
                String empId = tableModel.getValueAt(row, 0).toString();
                int confirm = JOptionPane.showConfirmDialog(this, "Delete Employee #" + empId + "?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (employeeDao.delete(empId)) refreshTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, Constants.NO_SELECTION_ERROR);
            }
        });
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
        private JTextField[] fields;
        private final String[] labels = {
            "Employee #", "Last Name", "First Name", "Birthday (MM-DD-YYYY)", "Address", 
            "Phone (####-###-####)", "Email", "SSS #", "Philhealth #", "TIN #", "Pag-ibig #", 
            "Status", "Position", "Immediate Supervisor", "Rice Subsidy", 
            "Phone Allowance", "Clothing Allowance", "Hourly Rate"
        };
        private Employee existingEmp;

        public EmployeeDialog(Frame owner, Employee emp) {
            super(owner, emp == null ? "Add Employee" : "Edit Employee", true);
            this.existingEmp = emp;
            setSize(600, 750);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());

            JPanel gridPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            fields = new JTextField[labels.length];

            for (int i = 0; i < labels.length; i++) {
                gridPanel.add(new JLabel(labels[i] + ":"));
                fields[i] = new JTextField();
                gridPanel.add(fields[i]);
            }

            if (emp != null) populateFields(emp);

            JButton btnSave = new JButton("Save Record");
            btnSave.setFont(new Font("SansSerif", Font.BOLD, 14));
            btnSave.setBackground(new Color(25, 25, 112));
            btnSave.setForeground(Color.BLACK);
            btnSave.addActionListener(e -> validateAndSave());

            add(new JScrollPane(gridPanel), BorderLayout.CENTER);
            add(btnSave, BorderLayout.SOUTH);
        }

        private void populateFields(Employee emp) {
            fields[0].setText(emp.getEmployeeNumber()); fields[0].setEditable(false);
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
            // 1. Reset red borders
            for (JTextField f : fields) f.setBorder(UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border"));

            try {
                // 2. Required Field Check
                for (int i = 0; i < fields.length; i++) {
                    if (fields[i].getText().trim().isEmpty()) { 
                        highlightError(fields[i]); 
                        throw new Exception(Constants.REQUIRED_FIELDS_ERROR); 
                    }
                }

                // 3. Duplicate ID Check
                if (existingEmp == null && employeeDao.findById(fields[0].getText().trim()) != null) {
                    highlightError(fields[0]); 
                    throw new Exception(Constants.DUPLICATE_EMP_NUM_ERROR);
                }

                // 4. Birthday Regex Validation (Using Constant)
                if (!fields[3].getText().trim().matches(Constants.DATE_PATTERN)) { 
                    highlightError(fields[3]); 
                    throw new Exception("Invalid Date: Use MM-DD-YYYY format."); 
                }

                // 5. Pattern Validations (Using Constants)
                if (!fields[5].getText().matches(Constants.CONTACT_NUMBER_PATTERN)) { 
                    highlightError(fields[5]); throw new Exception(Constants.INVALID_CONTACT_ERROR); 
                }
                if (!fields[6].getText().matches(Constants.EMAIL_PATTERN)) { 
                    highlightError(fields[6]); throw new Exception(Constants.INVALID_EMAIL_ERROR); 
                }
                if (!fields[7].getText().matches(Constants.SSS_NUMBER_PATTERN)) { 
                    highlightError(fields[7]); throw new Exception(Constants.INVALID_SSS_ERROR); 
                }
                if (!fields[8].getText().matches(Constants.PHILHEALTH_NUMBER_PATTERN)) { 
                    highlightError(fields[8]); throw new Exception(Constants.INVALID_PHILHEALTH_ERROR); 
                }
                if (!fields[9].getText().matches(Constants.TIN_NUMBER_PATTERN)) { 
                    highlightError(fields[9]); throw new Exception(Constants.INVALID_TIN_ERROR); 
                }
                if (!fields[10].getText().matches(Constants.PAGIBIG_NUMBER_PATTERN)) { 
                    highlightError(fields[10]); throw new Exception(Constants.INVALID_PAGIBIG_ERROR); 
                }

                // 6. Numeric Validations
                for (int i = 14; i <= 17; i++) {
                    try { 
                        Double.parseDouble(fields[i].getText()); 
                    } catch (Exception e) { 
                        highlightError(fields[i]); 
                        throw new Exception("'" + labels[i] + "' " + Constants.INVALID_NUMERIC_ERROR); 
                    }
                }

                // 7. --- CONFIRMATION DIALOG ---
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Are the details correct for: " + fields[2].getText() + " " + fields[1].getText() + "?", 
                    "Confirm Save", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (confirm != JOptionPane.YES_OPTION) return;

                // 8. Save Logic (Mapping to Employee Object)
                Employee emp = (existingEmp == null) ? new Employee() : existingEmp;
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
                
                // Keep salaries in model but hide from HR GUI
                emp.setBasicSalary(existingEmp != null ? existingEmp.getBasicSalary() : 0.0);
                emp.setRiceSubsidy(Double.parseDouble(fields[14].getText()));
                emp.setPhoneAllowance(Double.parseDouble(fields[15].getText()));
                emp.setClothingAllowance(Double.parseDouble(fields[16].getText()));
                emp.setGrossSalaryRate(existingEmp != null ? existingEmp.getGrossSalaryRate() : 0.0);
                emp.setHourlyRate(Double.parseDouble(fields[17].getText()));

                if (existingEmp == null ? employeeDao.addEmployee(emp) : employeeDao.updateEmployee(emp)) {
                    JOptionPane.showMessageDialog(this, "Record saved successfully.");
                    dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            }
        }

        private void highlightError(JTextField field) {
            field.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            field.requestFocusInWindow();
        }
    }
}