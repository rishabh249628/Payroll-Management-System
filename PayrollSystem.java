import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.*;

public class PayrollSystem extends JFrame {

    // Database Credentials - APNA MYSQL PASSWORD YAHAN UPDATE KAREIN
    private static final String DB_URL = "jdbc:mysql://localhost:3306/payroll_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root"; // <-- Apna MySQL password yahan likhein

    // UI Components
    private JTextField txtEmpId, txtName, txtBasic, txtHra, txtDa, txtPf, txtEsi, txtTax, txtNet;
    private JComboBox<String> cmbDept;
    private JTextArea txtPayslip;
    private JTable table;
    private DefaultTableModel tableModel;

    public PayrollSystem() {
        setTitle("Employee Payroll Management System - Java Swing & JDBC");
        setSize(1150, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Header Banner (AWT + Swing)
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(30, 58, 138));
        JLabel lblTitle = new JLabel("EMPLOYEE PAYROLL MANAGEMENT SYSTEM");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        add(headerPanel, BorderLayout.NORTH);

        // Main Center Panel (Left: Form, Right: Payslip)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left Panel: Input Form & Auto-Calculated Fields
        JPanel formPanel = new JPanel(new GridLayout(11, 2, 8, 8));
        formPanel.setBorder(BorderFactory.createTitledBorder("Employee & Salary Details"));

        txtEmpId = new JTextField();
        txtName = new JTextField();
        cmbDept = new JComboBox<>(new String[]{"IT", "HR", "Finance", "Sales", "Operations"});
        txtBasic = new JTextField();

        txtHra = new JTextField(); txtHra.setEditable(false);
        txtDa = new JTextField(); txtDa.setEditable(false);
        txtPf = new JTextField(); txtPf.setEditable(false);
        txtEsi = new JTextField(); txtEsi.setEditable(false);
        txtTax = new JTextField(); txtTax.setEditable(false);
        txtNet = new JTextField(); txtNet.setEditable(false);
        txtNet.setFont(new Font("Arial", Font.BOLD, 13));
        txtNet.setForeground(new Color(0, 128, 0));

        formPanel.add(new JLabel("Employee ID:")); formPanel.add(txtEmpId);
        formPanel.add(new JLabel("Employee Name:")); formPanel.add(txtName);
        formPanel.add(new JLabel("Department:")); formPanel.add(cmbDept);
        formPanel.add(new JLabel("Basic Salary (Monthly Rs.):")); formPanel.add(txtBasic);
        formPanel.add(new JLabel("HRA (20%):")); formPanel.add(txtHra);
        formPanel.add(new JLabel("DA (10%):")); formPanel.add(txtDa);
        formPanel.add(new JLabel("PF Deduction (12%):")); formPanel.add(txtPf);
        formPanel.add(new JLabel("ESI Deduction (0.75%):")); formPanel.add(txtEsi);
        formPanel.add(new JLabel("Income Tax (Slab-wise):")); formPanel.add(txtTax);
        formPanel.add(new JLabel("Net Salary (Rs.):")); formPanel.add(txtNet);

        JButton btnCalc = new JButton("1. Calculate Payroll");
        btnCalc.setBackground(new Color(2, 132, 199));
        btnCalc.setForeground(Color.WHITE);
        JButton btnClear = new JButton("Clear Form");
        formPanel.add(btnCalc);
        formPanel.add(btnClear);

        centerPanel.add(formPanel);

        // Right Panel: Printable Payslip Area
        JPanel slipPanel = new JPanel(new BorderLayout(5, 5));
        slipPanel.setBorder(BorderFactory.createTitledBorder("Printable Payslip Preview"));
        txtPayslip = new JTextArea();
        txtPayslip.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtPayslip.setEditable(false);
        slipPanel.add(new JScrollPane(txtPayslip), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        JButton btnSave = new JButton("2. Save to Database");
        btnSave.setBackground(new Color(22, 163, 74));
        btnSave.setForeground(Color.WHITE);

        JButton btnPrint = new JButton("3. Print Payslip (PDF)");
        btnPrint.setBackground(new Color(217, 119, 6));
        btnPrint.setForeground(Color.WHITE);

        JButton btnDelete = new JButton("Delete Selected");
        btnDelete.setBackground(new Color(220, 38, 38));
        btnDelete.setForeground(Color.WHITE);

        actionPanel.add(btnSave);
        actionPanel.add(btnPrint);
        actionPanel.add(btnDelete);
        slipPanel.add(actionPanel, BorderLayout.SOUTH);

        centerPanel.add(slipPanel);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Panel: Database Records Table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Saved Employee Payroll Records (MySQL Database)"));
        tablePanel.setPreferredSize(new Dimension(1100, 190));

        String[] cols = {"Emp ID", "Name", "Department", "Basic", "HRA", "DA", "PF", "ESI", "Tax", "Net Salary"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.SOUTH);

        // Button Listeners
        btnCalc.addActionListener(e -> calculateSalary());
        btnSave.addActionListener(e -> saveToDatabase());
        btnPrint.addActionListener(e -> printPayslip());
        btnDelete.addActionListener(e -> deleteRecord());
        btnClear.addActionListener(e -> clearForm());

        // Load data on startup
        loadRecordsFromDB();
    }

    // Auto Slab-Wise Tax, PF, and ESI Calculation Logic
    private void calculateSalary() {
        try {
            if (txtEmpId.getText().isEmpty() || txtName.getText().isEmpty() || txtBasic.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill Employee ID, Name, and Basic Salary!");
                return;
            }

            int empId = Integer.parseInt(txtEmpId.getText().trim());
            String name = txtName.getText().trim();
            String dept = cmbDept.getSelectedItem().toString();
            double basic = Double.parseDouble(txtBasic.getText().trim());

            // Allowances
            double hra = basic * 0.20; // 20% HRA
            double da = basic * 0.10;  // 10% DA
            double gross = basic + hra + da;

            // Statutory Deductions
            double pf = basic * 0.12; // 12% PF on Basic

            // ESI Slab: Applicable if Gross Salary <= Rs. 21,000 (0.75% of Gross)
            double esi = (gross <= 21000) ? (gross * 0.0075) : 0.0;

            // Income Tax Slab-Wise Calculation (Annualized)
            double annualGross = gross * 12;
            double annualTax = 0;

            if (annualGross <= 300000) {
                annualTax = 0; // Nil up to 3 Lakhs
            } else if (annualGross <= 700000) {
                annualTax = (annualGross - 300000) * 0.05; // 5% for 3L - 7L
            } else if (annualGross <= 1000000) {
                annualTax = (400000 * 0.05) + ((annualGross - 700000) * 0.10); // 10% for 7L - 10L
            } else if (annualGross <= 1200000) {
                annualTax = (400000 * 0.05) + (300000 * 0.10) + ((annualGross - 1000000) * 0.15); // 15% for 10L - 12L
            } else {
                annualTax = (400000 * 0.05) + (300000 * 0.10) + (200000 * 0.15) + ((annualGross - 1200000) * 0.20); // 20% above 12L
            }

            double monthlyTax = annualTax / 12.0;
            double totalDeductions = pf + esi + monthlyTax;
            double netSalary = gross - totalDeductions;

            // Update TextFields
            txtHra.setText(String.format("%.2f", hra));
            txtDa.setText(String.format("%.2f", da));
            txtPf.setText(String.format("%.2f", pf));
            txtEsi.setText(String.format("%.2f", esi));
            txtTax.setText(String.format("%.2f", monthlyTax));
            txtNet.setText(String.format("%.2f", netSalary));

            // Generate Printable Payslip Preview
            txtPayslip.setText(
                "==================================================\n" +
                "             OFFICIAL EMPLOYEE PAYSLIP            \n" +
                "==================================================\n" +
                String.format(" Employee ID   : %-15d Dept : %s\n", empId, dept) +
                String.format(" Employee Name : %-30s\n", name) +
                "--------------------------------------------------\n" +
                " EARNINGS                           AMOUNT (Rs.)  \n" +
                "--------------------------------------------------\n" +
                String.format(" Basic Salary                     : %12.2f\n", basic) +
                String.format(" House Rent Allowance (HRA 20%%)   : %12.2f\n", hra) +
                String.format(" Dearness Allowance (DA 10%%)      : %12.2f\n", da) +
                "--------------------------------------------------\n" +
                String.format(" GROSS SALARY                     : %12.2f\n", gross) +
                "--------------------------------------------------\n" +
                " DEDUCTIONS (SLAB-WISE)             AMOUNT (Rs.)  \n" +
                "--------------------------------------------------\n" +
                String.format(" Provident Fund (PF 12%%)          : %12.2f\n", pf) +
                String.format(" ESI (0.75%% if Gross <= 21k)      : %12.2f\n", esi) +
                String.format(" Income Tax (TDS Monthly Slab)    : %12.2f\n", monthlyTax) +
                "--------------------------------------------------\n" +
                String.format(" TOTAL DEDUCTIONS                 : %12.2f\n", totalDeductions) +
                "==================================================\n" +
                String.format(" NET TAKE-HOME SALARY             : Rs. %8.2f\n", netSalary) +
                "==================================================\n" +
                " * Computer Generated Payslip - Signature Not Req \n"
            );

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input! Employee ID and Basic Salary must be valid numbers.");
        }
    }

    private void saveToDatabase() {
        if (txtNet.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pehle '1. Calculate Payroll' button dabayein!");
            return;
        }

        String sql = "REPLACE INTO employees VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(txtEmpId.getText().trim()));
            pstmt.setString(2, txtName.getText().trim());
            pstmt.setString(3, cmbDept.getSelectedItem().toString());
            pstmt.setDouble(4, Double.parseDouble(txtBasic.getText().trim()));
            pstmt.setDouble(5, Double.parseDouble(txtHra.getText().trim()));
            pstmt.setDouble(6, Double.parseDouble(txtDa.getText().trim()));
            pstmt.setDouble(7, Double.parseDouble(txtPf.getText().trim()));
            pstmt.setDouble(8, Double.parseDouble(txtEsi.getText().trim()));
            pstmt.setDouble(9, Double.parseDouble(txtTax.getText().trim()));
            pstmt.setDouble(10, Double.parseDouble(txtNet.getText().trim()));

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Record Saved Successfully in MySQL!");
            loadRecordsFromDB();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }

    private void loadRecordsFromDB() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM employees";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("emp_id"),
                    rs.getString("name"),
                    rs.getString("department"),
                    rs.getDouble("basic_salary"),
                    rs.getDouble("hra"),
                    rs.getDouble("da"),
                    rs.getDouble("pf"),
                    rs.getDouble("esi"),
                    rs.getDouble("tax"),
                    rs.getDouble("net_salary")
                });
            }
        } catch (SQLException ex) {
            System.out.println("DB Load Notice (Check password/MySQL service): " + ex.getMessage());
        }
    }

    private void deleteRecord() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Delete karne ke liye niche table me se koi row select karein!");
            return;
        }
        int empId = (int) tableModel.getValueAt(selectedRow, 0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM employees WHERE emp_id = ?")) {
            pstmt.setInt(1, empId);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Employee Record Deleted!");
            loadRecordsFromDB();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void printPayslip() {
        if (txtPayslip.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pehle Payslip generate karein!");
            return;
        }
        try {
            boolean complete = txtPayslip.print();
            if (complete) {
                JOptionPane.showMessageDialog(this, "Payslip Printed / Saved as PDF Successfully!");
            }
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(this, "Printing Error: " + ex.getMessage());
        }
    }

    private void clearForm() {
        txtEmpId.setText(""); txtName.setText(""); txtBasic.setText("");
        txtHra.setText(""); txtDa.setText(""); txtPf.setText("");
        txtEsi.setText(""); txtTax.setText(""); txtNet.setText("");
        txtPayslip.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PayrollSystem().setVisible(true));
    }
}