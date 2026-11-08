/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 *
 * @author Vladimir Bernardo
 */
public class Employee {
    private static final Pattern EMPLOYEE_NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final Pattern PHONE_NUMBER_PATTERN =
            Pattern.compile("(?:\\d{3}-\\d{3}-\\d{3}|\\d{4}-\\d{3}-\\d{4})");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[A-Za-z0-9+_.-]+@[^\\s@]+");
    private static final Pattern SSS_NUMBER_PATTERN = Pattern.compile("\\d{2}-\\d{7}-\\d");
    private static final Pattern PHILHEALTH_NUMBER_PATTERN =
            Pattern.compile("(?:\\d{12}|\\d{4}-\\d{4}-\\d{4})");
    private static final Pattern TIN_NUMBER_PATTERN =
            Pattern.compile("\\d{3}-\\d{3}-\\d{3}-\\d{3}");
    private static final Pattern PAGIBIG_NUMBER_PATTERN =
            Pattern.compile("(?:\\d{12}|\\d{4}-\\d{4}-\\d{4})");
    private static final DateTimeFormatter BIRTHDAY_SLASH_FORMAT =
            DateTimeFormatter.ofPattern("MM/dd/uuuu", Locale.ROOT)
                    .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter BIRTHDAY_DASH_FORMAT =
            DateTimeFormatter.ofPattern("MM-dd-uuuu", Locale.ROOT)
                    .withResolverStyle(ResolverStyle.STRICT);

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
    public void setEmployeeNumber(String employeeNumber) {
        String value = requireString(employeeNumber, "Employee number");
        requirePattern(value, EMPLOYEE_NUMBER_PATTERN, "Employee number must contain digits only.");
        this.employeeNumber = value;
    }

    public void setLastName(String lastName) { this.lastName = requireString(lastName, "Last name"); }
    public void setFirstName(String firstName) { this.firstName = requireString(firstName, "First name"); }

    public void setBirthday(String birthday) {
        String value = requireString(birthday, "Birthday");
        if (!isValidBirthday(value)) {
            throw new IllegalArgumentException(
                    "Birthday must be a valid date in MM/dd/yyyy or MM-dd-yyyy format.");
        }
        this.birthday = value;
    }

    public void setAddress(String address) { this.address = requireString(address, "Address"); }

    public void setPhoneNumber(String phoneNumber) {
        String value = requireString(phoneNumber, "Phone number");
        requirePattern(value, PHONE_NUMBER_PATTERN,
                "Phone number must use ###-###-### or ####-###-#### format.");
        this.phoneNumber = value;
    }

    public void setEmail(String email) {
        String value = requireString(email, "Email");
        requirePattern(value, EMAIL_PATTERN, "Email must be a valid address without whitespace.");
        this.email = value;
    }

    public void setSssNumber(String sssNumber) {
        String value = requireString(sssNumber, "SSS number");
        requirePattern(value, SSS_NUMBER_PATTERN, "SSS number must use ##-#######-# format.");
        this.sssNumber = value;
    }

    public void setPhilhealthNumber(String philhealthNumber) {
        String value = requireString(philhealthNumber, "PhilHealth number");
        requirePattern(value, PHILHEALTH_NUMBER_PATTERN,
                "PhilHealth number must contain 12 digits, optionally formatted as ####-####-####.");
        this.philhealthNumber = value;
    }

    public void setTinNumber(String tinNumber) {
        String value = requireString(tinNumber, "TIN");
        requirePattern(value, TIN_NUMBER_PATTERN, "TIN must use ###-###-###-### format.");
        this.tinNumber = value;
    }

    public void setPagibigNumber(String pagibigNumber) {
        String value = requireString(pagibigNumber, "Pag-IBIG number");
        requirePattern(value, PAGIBIG_NUMBER_PATTERN,
                "Pag-IBIG number must contain 12 digits, optionally formatted as ####-####-####.");
        this.pagibigNumber = value;
    }

    public void setStatus(String status) { this.status = requireString(status, "Status"); }
    public void setPosition(String position) { this.position = requireString(position, "Position"); }
    public void setImmediateSupervisor(String immediateSupervisor) {
        this.immediateSupervisor = requireString(immediateSupervisor, "Immediate supervisor");
    }

    public void setBasicSalary(double basicSalary) {
        this.basicSalary = requireNonNegativeFinite(basicSalary, "Basic salary");
    }

    public void setRiceSubsidy(double riceSubsidy) {
        this.riceSubsidy = requireNonNegativeFinite(riceSubsidy, "Rice subsidy");
    }

    public void setPhoneAllowance(double phoneAllowance) {
        this.phoneAllowance = requireNonNegativeFinite(phoneAllowance, "Phone allowance");
    }

    public void setClothingAllowance(double clothingAllowance) {
        this.clothingAllowance = requireNonNegativeFinite(clothingAllowance, "Clothing allowance");
    }

    public void setGrossSalary(double grossSalary) {
        this.grossSalary = requireNonNegativeFinite(grossSalary, "Gross salary");
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = requireNonNegativeFinite(hourlyRate, "Hourly rate");
    }

    private static String requireString(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank.");
        }
        return value.trim();
    }

    private static void requirePattern(String value, Pattern pattern, String message) {
        if (!pattern.matcher(value).matches()) {
            throw new IllegalArgumentException(message);
        }
    }

    private static boolean isValidBirthday(String value) {
        try {
            LocalDate.parse(value, BIRTHDAY_SLASH_FORMAT);
            return true;
        } catch (DateTimeParseException slashFormatFailure) {
            try {
                LocalDate.parse(value, BIRTHDAY_DASH_FORMAT);
                return true;
            } catch (DateTimeParseException dashFormatFailure) {
                return false;
            }
        }
    }

    private static double requireNonNegativeFinite(double value, String fieldName) {
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException(fieldName + " must be a finite, non-negative number.");
        }
        return value;
    }
}
