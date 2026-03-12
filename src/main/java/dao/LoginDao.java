/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author Vladimir Bernardo
 */
import model.UserAccount;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User Accounts.
 * Manages authentication and account updates in users.csv.
 */
public class LoginDao {
    private final String csvFile = "src/main/java/resources/users.csv";

    /**
     * Verifies credentials against the CSV file.
     * @return UserAccount if found, null if invalid.
     */
    public UserAccount authenticate(String empId, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            br.readLine(); // Skip CSV Header

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 3) {
                    String csvEmpId = data[0].trim();
                    String csvPass = data[1].trim();
                    String csvRole = data[2].trim();

                    if (csvEmpId.equals(empId) && csvPass.equals(password)) {
                        return new UserAccount(csvEmpId, csvPass, csvRole);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Login Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * IT Requirement: Fetch all user accounts for the IT Management table.
     */
    public List<UserAccount> getAllUsers() {
        List<UserAccount> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            br.readLine(); // Skip CSV Header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 3) {
                    list.add(new UserAccount(data[0].trim(), data[1].trim(), data[2].trim()));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading user accounts: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates a user's password in the CSV file.
     * Consolidation: Handles both IT Resets and Employee self-service updates.
     */
    public boolean updatePassword(String empId, String newPassword) {
        return updateAccountField(empId, newPassword, 1); // Index 1 is Password
    }

    /**
     * IT Requirement: Update a user's system role (Admin, HR, IT, etc.)
     */
    public boolean updateRole(String empId, String newRole) {
        return updateAccountField(empId, newRole, 2); // Index 2 is Role
    }
    
    public boolean addAccount(UserAccount account) {
        // The 'true' parameter enables APPEND mode
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile, true))) {
            // Trim every field to prevent "Pending" status bugs caused by hidden spaces
            String empId = account.getEmployeeNo().trim();
            String pass = account.getPassword().trim();
            String role = account.getRole().trim();
            
            // Explicitly join with commas and NO extra spaces
            pw.println(empId + "," + pass + "," + role);
            return true;
        } catch (IOException e) {
            System.err.println("Error adding new account: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteAccount(String empId) {
        List<String> lines = new ArrayList<>();
        boolean deleted = false;
        String targetId = empId.trim();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String header = br.readLine();
            if (header != null) lines.add(header);

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                // If the ID doesn't match, keep the line
                if (!data[0].trim().equals(targetId)) {
                    lines.add(line);
                } else {
                    deleted = true; // Found the target to remove
                }
            }
        } catch (IOException e) {
            return false;
        }

        if (deleted) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
                for (String l : lines) pw.println(l);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Helper: Generic method to update a specific column in the CSV.
     * This handles the "Read-Modify-Write" process safely.
     */
    private boolean updateAccountField(String empId, String newValue, int fieldIndex) {
        List<String> lines = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String header = br.readLine();
            if (header != null) lines.add(header);

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data[0].trim().equals(empId)) {
                    data[fieldIndex] = newValue; // Update the specific field (Password or Role)
                    updated = true;
                }
                lines.add(String.join(",", data));
            }
        } catch (IOException e) {
            System.err.println("CSV Read Error: " + e.getMessage());
            return false;
        }

        // Only overwrite the file if a change was actually made
        if (updated) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
                for (String l : lines) {
                    pw.println(l);
                }
                return true;
            } catch (IOException e) {
                System.err.println("CSV Write Error: " + e.getMessage());
                return false;
            }
        }
        return false;
    }
}