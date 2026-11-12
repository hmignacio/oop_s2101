/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author Vladimir Bernardo
 */
import interfaces.LoginDao;
import model.UserAccount;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * @author Vladimir Bernardo
 */
public class LoginCSVDao extends MainCSVDao implements LoginDao {

    private static final String HEADER = "EmployeeID,Password,Role,Status";

    public LoginCSVDao() {
        // Points to your specific User credentials file
        super("src/main/java/resources/users.csv");
    }

    @Override
    public UserAccount authenticate(String inputId, String inputPass) {
        List<String[]> rows = readAllLines(); 
    
        for (String[] row : rows) {
            // row[0] = EmployeeID, row[1] = Password, row[2] = Role
            if (row.length < 3) continue;

            String csvId = row[0].replaceAll("[^0-9]", "");
            String csvPass = row[1].trim();
            String csvRole = row[2].trim();
            String status = row.length >= 4 ? row[3].trim() : "ACTIVE";
        
            // Compare input vs CSV
            if (csvId.equals(inputId.trim()) && csvPass.equals(inputPass.trim())) {
                if (!status.equalsIgnoreCase("ACTIVE")) {
                    return null;
                }
                return new UserAccount(csvId, csvPass, csvRole, status);
            }
        }
        return null;
    }

    @Override
    public boolean updateRole(String employeeId, String newRole) {
        // 1. Get all current accounts
        List<UserAccount> allAccounts = getAllAccounts();
        boolean isUpdated = false;

        // 2. Find the user by Employee ID and change their role
        for (UserAccount acc : allAccounts) {
            if (acc.getEmployeeNo().trim().equals(employeeId.trim())) {
                acc.setRole(newRole);
                isUpdated = true;
                break;
            }
        }

        // 3. If a change was made, save the entire list back to the CSV
        return isUpdated && saveAll(allAccounts);
    }

    @Override
    public boolean activateAccount(String employeeId) {
        List<UserAccount> accounts = getAllAccounts();
        for (UserAccount account : accounts) {
            if (account.getEmployeeNo().trim().equals(employeeId.trim())) {
                if (account.getStatus().equalsIgnoreCase("ACTIVE")) {
                    return false;
                }
                if (!account.getStatus().equalsIgnoreCase("INACTIVE")
                        && !account.getStatus().equalsIgnoreCase("PENDING")) {
                    return false;
                }
                account.setStatus("ACTIVE");
                return saveAll(accounts);
            }
        }
        return false;
    }
    
    @Override
    public boolean addAccount(UserAccount account) {
        try {
            List<UserAccount> accounts = getAllAccounts();
            if (accounts.stream().anyMatch(existing ->
                    existing.getEmployeeNo().trim().equals(account.getEmployeeNo().trim()))) {
                return false;
            }
            accounts.add(account);
            return saveAll(accounts);
        } catch (Exception e) {
            System.err.println("Error adding account: " + e.getMessage());
            return false;      // Failed to save
        }
    }

    @Override
public boolean updatePassword(String username, String newPassword) {
    List<UserAccount> accounts = getAllAccounts();
    boolean found = false;
    
    for (UserAccount acc : accounts) {
        // Change getUsername() to getEmployeeNo()
        if (acc.getEmployeeNo().trim().equals(username.trim())) {
            acc.setPassword(newPassword);
            found = true;
            break;
        }
    }
    
    return found && saveAll(accounts);
}

    @Override
public boolean deleteAccount(String username) {
    // 1. Get all current accounts
    List<UserAccount> allAccounts = getAllAccounts();
    
    // 2. Filter using getEmployeeNo() instead of getUsername()
    List<UserAccount> filteredList = allAccounts.stream()
            .filter(acc -> !acc.getEmployeeNo().trim().equals(username.trim()))
            .collect(Collectors.toList());
    
    // 3. Logic: If the list size decreased, something was actually deleted
    if (filteredList.size() < allAccounts.size()) {
        return saveAll(filteredList);
    }
    
    return false;
}

    @Override
    public List<UserAccount> getAllAccounts() {
        List<UserAccount> list = new ArrayList<>();
        List<String[]> rows = readAllLines(); // Inherited from MainCSVDao
        
        for (String[] row : rows) {
            // Assumes UserAccount has a constructor: public UserAccount(String[] data)
            list.add(new UserAccount(row));
        }
        return list;
    }

    /**
     * Internal helper to persist the list back to CSV.
     * Demonstrates encapsulation by keeping the writing logic private.
     */
    private boolean saveAll(List<UserAccount> list) {
        List<String> lines = list.stream()
                .map(UserAccount::toCSVString) // Requires toCSVString() in UserAccount model
                .collect(Collectors.toList());
        
        // Use inherited writeAllLines from MainCSVDao
        return writeAllLinesChecked(lines, HEADER);
    }
}
