/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Vladimir Bernardo
 */
public class Employee {
    // Basic Information
    private String employeeNumber;
    private String lastName;
    private String firstName;
    private String birthday;
    private String address;
    private String phoneNumber;
    private String email;

    // Government IDs
    private String sssNumber;
    private String philhealthNumber;
    private String tinNumber;
    private String pagibigNumber;

    // Employment Details
    private String status;
    private String position;
    private String immediateSupervisor;

    // Financial Data
    private double basicSalary;
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;
    private double grossSalary;
    private double hourlyRate;

    // Default Constructor
    public Employee() {}

    /**
     * Constructor mapping a String array (from a CSV row) to Employee fields.
     * Trims all string inputs to prevent "Profile Not Found" errors.
     */
    public Employee(String[] data) {
        if (data == null || data.length < 19) return;

        this.employeeNumber = data[0].replaceAll("[^0-9]", "").trim();
        this.lastName = data[1] != null ? data[1].trim() : "";
        this.firstName = data[2] != null ? data[2].trim() : "";
        this.birthday = data[3] != null ? data[3].trim() : "";
        this.address = data[4] != null ? data[4].trim() : "";
        this.phoneNumber = data[5] != null ? data[5].trim() : "";
        this.email = data[6] != null ? data[6].trim() : "";
        this.sssNumber = data[7] != null ? data[7].trim() : "";
        this.philhealthNumber = data[8] != null ? data[8].trim() : "";
        this.tinNumber = data[9] != null ? data[9].trim() : "";
        this.pagibigNumber = data[10] != null ? data[10].trim() : "";
        this.status = data[11] != null ? data[11].trim() : "";
        this.position = data[12] != null ? data[12].trim() : "";
        this.immediateSupervisor = data[13] != null ? data[13].trim() : "";
        
        // Safety: Ensure we don't index out of bounds if rows are shorter than expected
        this.basicSalary = data.length > 14 ? parseDouble(data[14]) : 0.0;
        this.riceSubsidy = data.length > 15 ? parseDouble(data[15]) : 0.0;
        this.phoneAllowance = data.length > 16 ? parseDouble(data[16]) : 0.0;
        this.clothingAllowance = data.length > 17 ? parseDouble(data[17]) : 0.0;
        this.grossSalary = data.length > 18 ? parseDouble(data[18]) : 0.0;
        this.hourlyRate = data.length > 19 ? parseDouble(data[19]) : 0.0;
    }

    /**
     * Converts the Employee object back into a CSV line.
     * Used by EmployeeCSVDao.saveAll().
     */
    public String toCSVString() {
        return String.join(",", 
            employeeNumber, 
            lastName, 
            firstName, 
            birthday, 
            "\"" + address.replace("\"", "'") + "\"", // Replace inner quotes to prevent CSV corruption
            phoneNumber, 
            email, 
            sssNumber, 
            philhealthNumber, 
            tinNumber, 
            pagibigNumber, 
            status, 
            position, 
            immediateSupervisor,
            String.valueOf(basicSalary), 
            String.valueOf(riceSubsidy), 
            String.valueOf(phoneAllowance), 
            String.valueOf(clothingAllowance), 
            String.valueOf(grossSalary), 
            String.valueOf(hourlyRate)
        );
    }

    private double parseDouble(String val) {
        try {
            if (val == null || val.trim().isEmpty() || val.equals("---") || val.equals("-")) {
                return 0.0;
            }
            // Remove commas from numbers like "25,000.00" before parsing
            String cleanVal = val.trim().replace(",", "");
            return Double.parseDouble(cleanVal);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // --- Getters ---
    public String getEmployeeNumber() { return employeeNumber; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getBirthday() { return birthday; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getSssNumber() { return sssNumber; }
    public String getPhilhealthNumber() { return philhealthNumber; }
    public String getTinNumber() { return tinNumber; }
    public String getPagibigNumber() { return pagibigNumber; }
    public String getStatus() { return status; }
    public String getPosition() { return position; }
    public String getImmediateSupervisor() { return immediateSupervisor; }
    public double getBasicSalary() { return basicSalary; }
    public double getRiceSubsidy() { return riceSubsidy; }
    public double getPhoneAllowance() { return phoneAllowance; }
    public double getClothingAllowance() { return clothingAllowance; }
    public double getGrossSalary() { return grossSalary; } // Renamed for consistency
    public double getHourlyRate() { return hourlyRate; }

    // --- Setters ---
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public void setAddress(String address) { this.address = address; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setEmail(String email) { this.email = email; }
    public void setSssNumber(String sssNumber) { this.sssNumber = sssNumber; }
    public void setPhilhealthNumber(String philhealthNumber) { this.philhealthNumber = philhealthNumber; }
    public void setTinNumber(String tinNumber) { this.tinNumber = tinNumber; }
    public void setPagibigNumber(String pagibigNumber) { this.pagibigNumber = pagibigNumber; }
    public void setStatus(String status) { this.status = status; }
    public void setPosition(String position) { this.position = position; }
    public void setImmediateSupervisor(String immediateSupervisor) { this.immediateSupervisor = immediateSupervisor; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }
    public void setRiceSubsidy(double riceSubsidy) { this.riceSubsidy = riceSubsidy; }
    public void setPhoneAllowance(double phoneAllowance) { this.phoneAllowance = phoneAllowance; }
    public void setClothingAllowance(double clothingAllowance) { this.clothingAllowance = clothingAllowance; }
    public void setGrossSalary(double grossSalary) { this.grossSalary = grossSalary; } // Renamed for consistency
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }
}
