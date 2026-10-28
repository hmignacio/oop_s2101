/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author Vladimir Bernardo
 */
import interfaces.AttendanceDao;
import model.Attendance;
import model.Employee;
import utils.Constants;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Handles employee punch-in/out records.
 * Matches requirements: One Time-In/Out per day.
 */
public class AttendanceCSVDao extends MainCSVDao implements AttendanceDao {

    private static final String HEADER = "Employee #,Last Name,First Name,Date,Log In,Log Out";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/uuuu", Locale.ROOT)
                    .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)
                    .withResolverStyle(ResolverStyle.STRICT);

    public AttendanceCSVDao() {
        super(Constants.ATTENDANCE_CSV_PATH);
    }

    @Override
    public List<Attendance> getAllAttendance() {
        List<Attendance> list = new ArrayList<>();
        List<String[]> rows = readAllLines();
        for (String[] row : rows) {
            // Mapping based on attendance.csv: 0=ID, 1=Last, 2=First, 3=Date, 4=In, 5=Out
            if (row.length >= 6) {
                // Ensure your Attendance model has this 4-string constructor
                list.add(new Attendance(row[0], row[3], row[4], row[5]));
            }
        }
        return list;
    }

    @Override
    public boolean hasRecordForToday(String empId, String date) {
        return getAllAttendance().stream()
                .anyMatch(a -> a.getEmployeeNumber().trim().equals(empId.trim()) 
                            && a.getDate().equals(date));
    }

    @Override
    public boolean hasClockedOut(String empId, String date) {
        return getAllAttendance().stream()
                .anyMatch(a -> a.getEmployeeNumber().trim().equals(empId.trim()) 
                            && a.getDate().equals(date) 
                            && !a.getTimeOut().equals("---") 
                            && !a.getTimeOut().isEmpty());
    }

    @Override
    public boolean logTime(Employee emp, String date, String time, String type) {
        if (emp == null || emp.getEmployeeNumber() == null
                || emp.getEmployeeNumber().trim().isEmpty()
                || !emp.getEmployeeNumber().matches(Constants.NUMERIC_ONLY)
                || date == null || date.trim().isEmpty()
                || time == null || time.trim().isEmpty() || type == null) {
            return false;
        }

        if (!date.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")
                || !time.matches("[0-9]{2}:[0-9]{2}")) {
            return false;
        }
        try {
            if (LocalDate.parse(date, DATE_FORMATTER).getYear() < 1) {
                return false;
            }
            LocalTime.parse(time, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return false;
        }

        String employeeNumber = emp.getEmployeeNumber();
        if (new EmployeeCSVDao().getAllEmployees().stream()
                .noneMatch(employee -> employeeNumber.equals(employee.getEmployeeNumber()))) {
            return false;
        }

        Path path = Paths.get(filePath);
        try {
            String content = Files.exists(path)
                    ? new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
                    : "";

            if (type.equalsIgnoreCase("IN")) {
                if (findRecord(content, emp.getEmployeeNumber(), date) != null) {
                    return false;
                }
                appendRecord(path, content, emp, date, time);
                return true;
            }

            if (type.equalsIgnoreCase("OUT")) {
                RecordLocation record = findRecord(content, emp.getEmployeeNumber(), date);
                if (record == null) {
                    return false;
                }
                String line = content.substring(record.lineStart, record.lineEnd);
                int[] bounds = findFieldBounds(line, 5);
                if (bounds == null) {
                    return false;
                }
                String replacement = replaceFieldValue(line.substring(bounds[0], bounds[1]), time);
                String updated = content.substring(0, record.lineStart)
                        + line.substring(0, bounds[0]) + replacement + line.substring(bounds[1])
                        + content.substring(record.lineEnd);
                writeAtomically(path, updated);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Attendance update failed: " + e.getMessage());
        }
        return false;
    }

    private void appendRecord(Path path, String content, Employee employee, String date, String time)
            throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        String newline = detectNewline(content);
        StringBuilder addition = new StringBuilder();
        if (content.isEmpty()) {
            addition.append(HEADER).append(newline);
        } else if (!content.endsWith("\n") && !content.endsWith("\r")) {
            addition.append(newline);
        }
        addition.append(csvField(employee.getEmployeeNumber())).append(',')
                .append(csvField(employee.getLastName())).append(',')
                .append(csvField(employee.getFirstName())).append(',')
                .append(csvField(date)).append(',')
                .append(csvField(time)).append(',')
                .append("---").append(newline);
        Files.write(path, addition.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private RecordLocation findRecord(String content, String employeeNumber, String date) {
        int lineStart = 0;
        boolean headerSkipped = false;
        String normalizedEmployeeNumber = normalizeEmployeeNumber(employeeNumber);

        while (lineStart < content.length()) {
            int lineEnd = lineStart;
            while (lineEnd < content.length() && content.charAt(lineEnd) != '\r'
                    && content.charAt(lineEnd) != '\n') {
                lineEnd++;
            }

            String line = content.substring(lineStart, lineEnd);
            if (!line.trim().isEmpty()) {
                if (!headerSkipped) {
                    headerSkipped = true;
                } else {
                    List<String> fields = parseCsvLine(line);
                    if (fields.size() >= 6
                            && normalizeEmployeeNumber(fields.get(0)).equals(normalizedEmployeeNumber)
                            && fields.get(3).trim().equals(date.trim())) {
                        return new RecordLocation(lineStart, lineEnd);
                    }
                }
            }

            if (lineEnd < content.length() && content.charAt(lineEnd) == '\r'
                    && lineEnd + 1 < content.length() && content.charAt(lineEnd + 1) == '\n') {
                lineStart = lineEnd + 2;
            } else {
                lineStart = lineEnd + 1;
            }
        }
        return null;
    }

    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char current = line.charAt(i);
            if (current == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (current == ',' && !inQuotes) {
                fields.add(field.toString().trim());
                field.setLength(0);
            } else {
                field.append(current);
            }
        }
        fields.add(field.toString().trim());
        return fields;
    }

    private int[] findFieldBounds(String line, int targetField) {
        int fieldStart = 0;
        int fieldIndex = 0;
        boolean inQuotes = false;

        for (int i = 0; i <= line.length(); i++) {
            if (i < line.length() && line.charAt(i) == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (i == line.length() || (line.charAt(i) == ',' && !inQuotes)) {
                if (fieldIndex == targetField) {
                    return new int[] {fieldStart, i};
                }
                fieldIndex++;
                fieldStart = i + 1;
            }
        }
        return null;
    }

    private String replaceFieldValue(String existingField, String value) {
        if (existingField.length() >= 2 && existingField.startsWith("\"")
                && existingField.endsWith("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return csvField(value);
    }

    private String csvField(String value) {
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"")
                || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private String normalizeEmployeeNumber(String employeeNumber) {
        return employeeNumber.trim().replaceAll("[^0-9]", "");
    }

    private String detectNewline(String content) {
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) == '\r') {
                return i + 1 < content.length() && content.charAt(i + 1) == '\n' ? "\r\n" : "\r";
            }
            if (content.charAt(i) == '\n') {
                return "\n";
            }
        }
        return System.lineSeparator();
    }

    private void writeAtomically(Path path, String content) throws IOException {
        Path absolutePath = path.toAbsolutePath();
        Path parent = absolutePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temporaryFile = Files.createTempFile(parent, path.getFileName().toString(), ".tmp");
        try {
            Files.write(temporaryFile, content.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            try {
                Files.move(temporaryFile, absolutePath, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, absolutePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private static final class RecordLocation {
        private final int lineStart;
        private final int lineEnd;

        private RecordLocation(int lineStart, int lineEnd) {
            this.lineStart = lineStart;
            this.lineEnd = lineEnd;
        }
    }
}
