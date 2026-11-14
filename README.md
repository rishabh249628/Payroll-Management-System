# Payroll-Management-System
A Java Swing &amp; JDBC Desktop Application for managing employee salaries, automatic slab-wise tax/PF/ESI deductions, and generating printable payslips connected with MySQL Workbench.
# Employee Payroll Management System

A desktop-based Java application built using **Java Swing (UI), JDBC, and MySQL** for automating employee salary calculations, statutory deductions (PF, ESI, Income Tax), and generating professional printable payslips.

## ✨ Features
* **Interactive UI:** Built using Java Swing & AWT components.
* **Automatic Salary Breakdown:** Calculates HRA (20%), DA (10%), and Gross Salary instantly.
* **Statutory Deductions:** 
  * Provident Fund (PF @ 12%)
  * ESI (0.75% applicable for gross salary $\le$ ₹21,000)
  * Slab-wise Annual Income Tax (TDS) calculation.
* **Database Integration:** Securely saves, retrieves, and deletes records using MySQL Workbench & JDBC (`mysql-connector-j`).
* **Printable Payslip:** Generates an official formatted payslip layout with direct print-to-PDF support.

## 🛠️ Tech Stack
* **Language:** Java (JDK 21+)
* **UI Framework:** Java Swing & AWT
* **Database:** MySQL & JDBC Driver
* **IDE:** VS Code / Eclipse / IntelliJ

## 🚀 How to Run
1. Clone or download this repository into your local machine.
2. Setup the MySQL database using the provided schema.
3. Update your MySQL database credentials (`DB_PASS`) in `PayrollSystem.java`.
4. Compile and run using the terminal:
   ```bash
   javac PayrollSystem.java
   java -cp ".;mysql-connector-j-8.3.0.jar" PayrollSystem
