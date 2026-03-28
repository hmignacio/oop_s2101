/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author Vladimir Bernardo
 */
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public abstract class MainCSVDao {
    protected String filePath;

    public MainCSVDao(String filePath) {
        this.filePath = filePath;
    }

    protected List<String[]> readAllLines() {
        List<String[]> data = new ArrayList<>();
        File file = new File(filePath);
        
        if (!file.exists()) {
            // Log the absolute path so you know EXACTLY where the app is looking
            System.err.println("CRITICAL ERROR: CSV not found at " + file.getAbsolutePath());
            return data;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue; 
                
                // Skip the header row
                if (isHeader) { 
                    isHeader = false; 
                    continue; 
                }
                
                // 1. Split using your Regex for quotes
                String[] rawRow = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                
                // 2. CLEANING: Trim every individual cell in the row
                for (int i = 0; i < rawRow.length; i++) {
                    rawRow[i] = rawRow[i].trim().replace("\"", ""); // Remove quotes after splitting
                }
                
                if (rawRow.length > 0) {
                    rawRow[0] = rawRow[0].replaceAll("[^0-9]", "");
                }
                
                data.add(rawRow);
            }
        } catch (IOException e) {
            System.err.println("Read Error: " + e.getMessage());
        }
        return data;
    }

    protected void writeAllLines(List<String> lines, String header) {
        // Ensure the parent directory exists before writing
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            if (header != null && !header.isEmpty()) {
                pw.println(header);
            }
            for (String line : lines) {
                pw.println(line);
            }
        } catch (IOException e) {
            System.err.println("Write Error: " + e.getMessage());
        }
    }
}
