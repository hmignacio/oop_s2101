/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author Vladimir Bernardo
 */
import model.Employee;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Access Object for Employees.
 * Optimized for the new CSV structure: Email after Phone Number.
 */
public class EmployeeDao {
    private final String csvFile = "src/main/java/resources/employee.csv";
    private final String lineSeparator = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"; 

    /**
     * Reads the CSV and maps indices to the Employee Model.
     */
    public List<Employee> getAllEmployees() {
        List<Employee> employeeList = new ArrayList<>();
        File file = new File(csvFile);
        if (!file.exists()) return employeeList;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Skip Header
            String line;
            while ((line = br.readLine()) != null) {
                String[] v = line.split(lineSeparator, -1);
                if (v.length >= 20) { // Expecting 20 columns now
                    Employee emp = new Employee();
                    emp.setEmployeeNumber(v[0].trim());
                    emp.setLastName(v[1].trim());
                    emp.setFirstName(v[2].trim());
                    emp.setBirthday(v[3].trim());
                    emp.setAddress(v[4].replace("\"", "").trim());
                    emp.setPhoneNumber(v[5].trim());
                    emp.setEmail(v[6].trim()); // Email at Index 6
                    emp.setSssNumber(v[7].trim());
                    emp.setPhilhealthNumber(v[8].trim());
                    emp.setTinNumber(v[9].trim());
                    emp.setPagibigNumber(v[10].trim());
                    emp.setStatus(v[11].trim());
                    emp.setPosition(v[12].trim());
                    emp.setImmediateSupervisor(v[13].trim());
                    
                    // Numeric Parsing
                    emp.setBasicSalary(Double.parseDouble(v[14].trim()));
                    emp.setRiceSubsidy(Double.parseDouble(v[15].trim()));
                    emp.setPhoneAllowance(Double.parseDouble(v[16].trim()));
                    emp.setClothingAllowance(Double.parseDouble(v[17].trim()));
                    emp.setGrossSalaryRate(Double.parseDouble(v[18].trim())); // Gross Salary
                    emp.setHourlyRate(Double.parseDouble(v[19].trim()));
                    
                    employeeList.add(emp);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading CSV: " + e.getMessage());
        }
        return employeeList;
    }

    /**
     * Helper to format Employee to CSV line (Matches Header Order).
     */
    private String mapToCSV(Employee emp) {
        return String.join(",",
            emp.getEmployeeNumber(),
            emp.getLastName(),
            emp.getFirstName(),
            emp.getBirthday(),
            "\"" + emp.getAddress() + "\"",
            emp.getPhoneNumber(),
            emp.getEmail(), // Email at Index 6
            emp.getSssNumber(),
            emp.getPhilhealthNumber(),
            emp.getTinNumber(),
            emp.getPagibigNumber(),
            emp.getStatus(),
            emp.getPosition(),
            emp.getImmediateSupervisor(),
            String.valueOf(emp.getBasicSalary()),
            String.valueOf(emp.getRiceSubsidy()),
            String.valueOf(emp.getPhoneAllowance()),
            String.valueOf(emp.getClothingAllowance()),
            String.valueOf(emp.getGrossSalaryRate()),
            String.valueOf(emp.getHourlyRate())
        );
    }

    private final String CSV_HEADER = "Employee #,Last Name,First Name,Birthday,Address,Phone Number,Email,SSS #,Philhealth #,TIN #,Pag-ibig #,Status,Position,Immediate Supervisor,Basic Salary,Rice Subsidy,Phone Allowance,Clothing Allowance,Gross Salary,Hourly Rate";

    public boolean addEmployee(Employee emp) {
        try (FileWriter fw = new FileWriter(csvFile, true);
             PrintWriter out = new PrintWriter(new BufferedWriter(fw))) {
            out.println(mapToCSV(emp));
            return true;
        } catch (IOException e) { return false; }
    }

    public boolean updateEmployee(Employee updatedEmp) {
        List<Employee> list = getAllEmployees();
        try (PrintWriter out = new PrintWriter(new FileWriter(csvFile))) {
            out.println(CSV_HEADER);
            for (Employee e : list) {
                out.println(mapToCSV(e.getEmployeeNumber().equals(updatedEmp.getEmployeeNumber()) ? updatedEmp : e));
            }
            return true;
        } catch (IOException e) { return false; }
    }

    public boolean delete(String id) {
        List<Employee> list = getAllEmployees().stream()
                .filter(e -> !e.getEmployeeNumber().equals(id))
                .collect(Collectors.toList());
        try (PrintWriter out = new PrintWriter(new FileWriter(csvFile))) {
            out.println(CSV_HEADER);
            for (Employee e : list) out.println(mapToCSV(e));
            return true;
        } catch (IOException e) { return false; }
    }

    public Employee findById(String id) {
        return getAllEmployees().stream().filter(e -> e.getEmployeeNumber().equals(id)).findFirst().orElse(null);
    }
}
