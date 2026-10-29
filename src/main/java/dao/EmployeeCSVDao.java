/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author Vladimir Bernardo
 */
import interfaces.EmployeeDao;
import model.Employee;
import utils.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EmployeeCSVDao extends MainCSVDao implements EmployeeDao {

    private static final String HEADER = "Employee #,Last Name,First Name,Birthday,Address,Phone Number,Email,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Salary,Hourly Rate";

    public EmployeeCSVDao() {
        super(Constants.EMPLOYEE_CSV_PATH);
    }

    // ALIAS: This ensures FinanceUI and HRUI can call .findAll() without errors
    
    public List<Employee> findAll() {
        return getAllEmployees();
    }

    @Override
    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        List<String[]> rows = readAllLines();
        
        for (String[] row : rows) {
            // SKIP THE HEADER: If the first cell is "Employee #", don't add it to the list
            if (row.length == 0 || row[0].equalsIgnoreCase("Employee #")) {
                continue;
            }

            if (row.length >= 19) {
                list.add(new Employee(row));
            }
        }
        return list;
    }

    @Override
    public Employee findById(String id) {
        // 1. Get the fresh list from CSV
        List<Employee> all = getAllEmployees(); 
        
        System.out.println("DEBUG: Searching for Profile ID: '" + id + "'"); // Add this to see what you're looking for
        
        // 2. Search with aggressive trimming
        for (Employee e : all) {
            
            // ADD THE DEBUG LINE HERE:
            System.out.println("DEBUG: Comparing against CSV record: '" + e.getEmployeeNumber() + "'");
            
            if (e.getEmployeeNumber().trim().equalsIgnoreCase(id.trim())) {
                System.out.println("DEBUG: MATCH FOUND!");
                return e; // Found!
            }
        }
        
        System.out.println("DEBUG: NO MATCH FOUND in the entire list.");
        return null; // This triggers the "Profile not found" message
    }

    @Override
    public boolean addEmployee(Employee employee) {
        try {
            // 1. Get the existing list
            List<Employee> all = getAllEmployees();
            
            // 2. DUPLICATE CHECK: Look for the ID
            String newId = employee.getEmployeeNumber().trim();
            for (Employee existing : all) {
                if (existing.getEmployeeNumber().trim().equalsIgnoreCase(newId)) {
                    System.err.println("Validation Error: Employee ID " + newId + " already exists!");
                    return false; // Stop here, don't add
                }
            }
            
            // 3. If no duplicate, add and save
            all.add(employee);
            saveAll(all);
            return true;
        } catch (Exception e) {
            System.err.println("Add Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateEmployee(Employee employee) {
        List<Employee> all = getAllEmployees();
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getEmployeeNumber().trim().equals(employee.getEmployeeNumber().trim())) {
                all.set(i, employee); 
                found = true;
                break;
            }
        }
        if (found) saveAll(all);
        return found;
    }
    
    @Override
    public boolean delete(String id) {
        List<Employee> allEmployees = getAllEmployees();
        List<Employee> remainingEmployees = allEmployees.stream()
                .filter(e -> !e.getEmployeeNumber().trim().equalsIgnoreCase(id.trim()))
                .collect(Collectors.toList());

        if (remainingEmployees.size() < allEmployees.size()) {
            saveAll(remainingEmployees);
            return true;
        }
        return false;
    }

    private void saveAll(List<Employee> list) {
        List<String> lines = list.stream()
                .map(Employee::toCSVString)
                .collect(Collectors.toList());
        writeAllLines(lines, HEADER);
    }
    
    public String generateNextEmployeeId() {
        List<Employee> employees = getAllEmployees();

        int highestId = 10000;

        for (Employee employee : employees) {
            String employeeId = employee.getEmployeeNumber().trim();

            try {
                int currentId = Integer.parseInt(employeeId);

                if (currentId > highestId) {
                    highestId = currentId;
                }

            } catch (NumberFormatException e) {
                System.err.println(
                    "Skipping invalid Employee ID: " + employeeId
                );
            }
        }

        return String.valueOf(highestId + 1);
    }
}