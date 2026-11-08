package view;

import java.awt.Dimension;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.JEditorPane;
import model.Employee;
import service.PayrollService.PayrollBreakdown;

final class PayslipRenderer {
    private PayslipRenderer() {
    }

    static JEditorPane createPayslip(Employee employee, PayrollBreakdown breakdown,
            int month, int year) {
        JEditorPane payslip = new JEditorPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return true;
            }
        };
        payslip.setContentType("text/html");
        payslip.setEditable(false);
        payslip.setText(createPayslipHtml(employee, breakdown, month, year));
        payslip.setPreferredSize(new Dimension(520, 600));
        payslip.setCaretPosition(0);
        return payslip;
    }

    static String createPayslipHtml(Employee employee, PayrollBreakdown breakdown,
            int month, int year) {
        DecimalFormat amountFormat = new DecimalFormat(
                "#,##0.00", DecimalFormatSymbols.getInstance(Locale.ROOT));
        String period = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year;
        String html = "<html><body style='font-family: sans-serif; margin: 12px;'>"
                + "<div style='text-align: center; background-color: #191970; color: white; padding: 10px;'>"
                + "<h2 style='margin:0;'>MotorPH</h2>"
                + "<p style='margin:0;'>Monthly Payslip: " + period + "</p></div>"
                + "<br><b>Employee:</b> " + escapeHtml(employee.getFullName())
                + "<br><b>Position:</b> " + escapeHtml(employee.getPosition())
                + "<hr><b>Earnings:</b>"
                + "<table style='width: 100%; font-size: 11px;'>"
                + row("Hours (" + amountFormat.format(breakdown.getHoursWorked())
                        + ") x Rate (" + amountFormat.format(employee.getHourlyRate()) + "):",
                        amountFormat.format(breakdown.getBasicPay()))
                + row("Rice Allowance:", amountFormat.format(breakdown.getRiceAllowance()))
                + row("Phone Allowance:", amountFormat.format(breakdown.getPhoneAllowance()))
                + row("Clothing Allowance:", amountFormat.format(breakdown.getClothingAllowance()))
                + row("<b>Total Gross:</b>", "<b>PHP "
                        + amountFormat.format(breakdown.getGrossSalary()) + "</b>")
                + "</table><br><b style='color: #b22222;'>Deductions:</b>"
                + "<table style='width: 100%; font-size: 11px; color: #b22222;'>"
                + row("SSS Contribution:", "(" + amountFormat.format(breakdown.getSss()) + ")")
                + row("PhilHealth:", "(" + amountFormat.format(breakdown.getPhilHealth()) + ")")
                + row("Pag-IBIG:", "(" + amountFormat.format(breakdown.getPagIbig()) + ")")
                + row("Withholding Tax:", "("
                        + amountFormat.format(breakdown.getWithholdingTax()) + ")")
                + row("<b>Total Deductions:</b>", "<b>PHP ("
                        + amountFormat.format(breakdown.getTotalDeductions()) + ")</b>")
                + "</table><div style='margin-top: 15px; padding: 12px; background-color: #f4f4f9;"
                + " text-align: center; border: 2px solid #191970;'>"
                + "<span style='font-size: 12px;'><b>NET TAKE-HOME:</b></span><br>"
                + "<span style='font-size: 22px; color: #006400;'><b>PHP "
                + amountFormat.format(breakdown.getNetSalary()) + "</b></span></div>"
                + "<p style='font-size: 9px; text-align: center; color: #888;'>Generated for Employee ID: "
                + escapeHtml(employee.getEmployeeNumber()) + "</p></body></html>";

        return html;
    }

    private static String row(String label, String value) {
        return "<tr><td>" + label + "</td><td style='text-align: right;'>" + value + "</td></tr>";
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
}
