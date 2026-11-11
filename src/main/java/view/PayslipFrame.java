package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.print.PrinterException;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import model.Employee;
import service.PayrollService.PayrollBreakdown;

public class PayslipFrame extends JFrame {
    private final Employee employee;
    private final PayrollBreakdown breakdown;
    private final int payMonth;
    private final int payYear;

    public PayslipFrame(Employee emp, PayrollBreakdown breakdown, int month, int year) {
        this.employee = emp;
        this.breakdown = breakdown;
        this.payMonth = month;
        this.payYear = year;
        initUI();
    }

    private void initUI() {
        setTitle("MotorPH Official Payslip - " + employee.getEmployeeNumber());
        setSize(600, 720);
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JEditorPane payslip = PayslipRenderer.createPayslip(
                employee, breakdown, payMonth, payYear);
        add(new JScrollPane(payslip), BorderLayout.CENTER);

        JButton btnPrint = new JButton("Print Payslip");
        JButton btnClose = new JButton("Close");
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(btnPrint);
        footer.add(btnClose);
        add(footer, BorderLayout.SOUTH);

        btnPrint.addActionListener(e -> printPayslip(payslip));
        btnClose.addActionListener(e -> dispose());
    }

    private void printPayslip(JEditorPane payslip) {
        try {
            if (payslip.print()) {
                JOptionPane.showMessageDialog(this, "Payslip sent to printer successfully.",
                        "Print Payslip", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Unable to print payslip: " + ex.getMessage(),
                    "Printing Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
