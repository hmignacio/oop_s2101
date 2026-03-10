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
     * Updates a user's password in the CSV file.
     * Used by the ITUI Reset Password feature.
     */
    public boolean updatePassword(String empId, String newPassword) {
        List<String> lines = new ArrayList<>();
        boolean found = false;

        // 1. Read all data and modify the target line
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            String header = br.readLine();
            lines.add(header); // Keep the header

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data[0].trim().equals(empId)) {
                    // Reconstruct line with new password: ID, NewPass, Role
                    lines.add(data[0] + "," + newPassword + "," + data[2]);
                    found = true;
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            return false;
        }

        // 2. Overwrite the file with updated list
        if (found) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
                for (String l : lines) {
                    pw.println(l);
                }
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * Fetches all user accounts for the ITUI display.
     */
    public List<UserAccount> getAllUsers() {
        List<UserAccount> users = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 3) {
                    users.add(new UserAccount(data[0].trim(), data[1].trim(), data[2].trim()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }
}
