/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author Vladimir Bernardo
 */
import interfaces.UserAccountDao;
import model.UserAccount;
import utils.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles all User Credential operations using CSV storage.
 * @author Vladimir Bernardo
 */
public class UserAccountCSVDao extends MainCSVDao implements UserAccountDao {

    // Using the Header from your actual users.csv
    private static final String HEADER = "EmployeeID,Password,Role";

    public UserAccountCSVDao() {
        // Pointing to the central path in Constants
        super(Constants.USER_CSV_PATH);
    }

    @Override
    public UserAccount authenticate(String employeeNo, String password) {
        // Retrieve fresh data from the CSV, then filter
        return getAllAccounts().stream()
                .filter(acc -> acc.getEmployeeNo().trim().equals(employeeNo.trim()) 
                            && acc.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean addAccount(UserAccount account) {
        try {
            List<UserAccount> accounts = getAllAccounts();
            accounts.add(account);
            saveAll(accounts);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updatePassword(String employeeNo, String newPassword) {
        List<UserAccount> accounts = getAllAccounts();
        boolean found = false;
        for (UserAccount acc : accounts) {
            if (acc.getEmployeeNo().trim().equals(employeeNo.trim())) {
                acc.setPassword(newPassword);
                found = true;
                break;
            }
        }
        if (found) saveAll(accounts);
        return found;
    }

    @Override
    public boolean deleteAccount(String employeeNo) {
        try {
            List<UserAccount> filtered = getAllAccounts().stream()
                    .filter(acc -> !acc.getEmployeeNo().trim().equals(employeeNo.trim()))
                    .collect(Collectors.toList());
            saveAll(filtered);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<UserAccount> getAllAccounts() {
        List<UserAccount> list = new ArrayList<>();
        // readAllLines() is inherited from MainCSVDao
        List<String[]> rows = readAllLines();
        for (String[] row : rows) {
            // Mapping the String array to the UserAccount Object
            if (row.length >= 3) {
                list.add(new UserAccount(row));
            }
        }
        return list;
    }

    /**
     * Persists the list of accounts back to the CSV file.
     */
    private void saveAll(List<UserAccount> list) {
        List<String> lines = list.stream()
                .map(UserAccount::toCSVString)
                .collect(Collectors.toList());
        
        // writeAllLines is inherited from MainCSVDao
        writeAllLines(lines, HEADER);
    }
}
