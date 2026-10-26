/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package interfaces;

/**
 *
 * @author Vladimir Bernardo
 */
import model.Employee;
import java.util.List;

public interface EmployeeDao {
    List<Employee> getAllEmployees();
    Employee findById(String id);
    boolean addEmployee(Employee employee);
    boolean updateEmployee(Employee employee);
    
    // Change this from void to boolean
    boolean delete(String id); 
}
