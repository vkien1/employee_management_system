import java.sql.*;
import java.util.Scanner;

public class EmployeeManagementSystem {

    private Connection connect() {
        String url = "jdbc:mysql://localhost:3306/EmployeeDB?useSSL=false";
        String user = "root";
        String password = "password";
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            return null;
        }
    }

    public void searchEmployee(String criteria, String value) {
        String sql = "SELECT * FROM Employee WHERE " + criteria + " = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, value);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("ID") + ", Name: " + rs.getString("Name") +
                                   ", SSN: " + rs.getString("SSN") + ", Job Title: " + rs.getString("JobTitle") +
                                   ", Division: " + rs.getString("Division"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void updateSalary(double percentage, double minSalary, double maxSalary) {
        String sql = "UPDATE Salary SET Amount = Amount * (1 + ? / 100) WHERE Amount >= ? AND Amount < ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, percentage);
            pstmt.setDouble(2, minSalary);
            pstmt.setDouble(3, maxSalary);
            int updatedRows = pstmt.executeUpdate();
            System.out.println("Updated rows: " + updatedRows);
        } catch (SQLException e) {
            System.out.println("Error updating salaries: " + e.getMessage());
        }
    }
    public void updateEmployeeProfile() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter employee ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine(); 
        System.out.print("Enter new name: ");
        String name = scanner.nextLine();

        System.out.print("Enter new SSN: ");
        String ssn = scanner.nextLine();

        System.out.print("Enter new Job Title: ");
        String jobTitle = scanner.nextLine();

        System.out.print("Enter new Division: ");
        String division = scanner.nextLine();

        String sql = "UPDATE Employee SET Name = ?, SSN = ?, JobTitle = ?, Division = ? WHERE ID = ?";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, ssn);
            pstmt.setString(3, jobTitle);
            pstmt.setString(4, division);
            pstmt.setInt(5, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Employee profile updated successfully.");
            } else {
                System.out.println("Employee not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating employee: " + e.getMessage());
        }
    }

    public void addEmployeeAndSalary(String name, String ssn, String jobTitle, String division, double salary) {
        String sqlEmployee = "INSERT INTO Employee (Name, SSN, JobTitle, Division) VALUES (?, ?, ?, ?)";
        String sqlSalary = "INSERT INTO Salary (EmployeeID, Amount) VALUES (LAST_INSERT_ID(), ?)";
        try (Connection conn = this.connect();
             PreparedStatement pstmtEmployee = conn.prepareStatement(sqlEmployee);
             PreparedStatement pstmtSalary = conn.prepareStatement(sqlSalary)) {
            conn.setAutoCommit(false);
            pstmtEmployee.setString(1, name);
            pstmtEmployee.setString(2, ssn);
            pstmtEmployee.setString(3, jobTitle);
            pstmtEmployee.setString(4, division);
            pstmtEmployee.executeUpdate();
            pstmtSalary.setDouble(1, salary);
            pstmtSalary.executeUpdate();
            conn.commit();
            System.out.println("Employee and salary added successfully.");
        } catch (SQLException e) {
            System.out.println("Error adding employee and salary: " + e.getMessage());
        }
    }

    public void viewAllEmployees() {
        String sql = "SELECT Employee.ID, Employee.Name, Employee.SSN, Employee.JobTitle, Employee.Division, Salary.Amount AS Salary " +
                     "FROM Employee JOIN Salary ON Employee.ID = Salary.EmployeeID";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            System.out.println("List of all employees:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("ID") + ", Name: " + rs.getString("Name") +
                                   ", SSN: " + rs.getString("SSN") + ", Job Title: " + rs.getString("JobTitle") +
                                   ", Division: " + rs.getString("Division") + ", Salary: $" + rs.getDouble("Salary"));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving employee data: " + e.getMessage());
        }
    }
    public void printFullTimeEmployeesWithPayHistory() {
        String sql = "SELECT e.ID, e.Name, e.JobTitle, e.Division, SUM(p.AmountPaid) AS TotalPaid " +
                     "FROM Employee e JOIN PayStatement p ON e.ID = p.EmployeeID " +
                     "GROUP BY e.ID, e.Name, e.JobTitle, e.Division";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            System.out.println("Full-time Employees and Their Pay History:");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("ID") +
                                   ", Name: " + rs.getString("Name") +
                                   ", Job Title: " + rs.getString("JobTitle") +
                                   ", Division: " + rs.getString("Division") +
                                   ", Total Paid: $" + rs.getDouble("TotalPaid"));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving full-time employee data: " + e.getMessage());
        }
    }
    
    public void printTotalPayByJobTitle() {
        String sql = "SELECT e.JobTitle, SUM(s.Amount) AS TotalPaid " +
                     "FROM Employee e " +
                     "JOIN Salary s ON e.ID = s.EmployeeID " +
                     "GROUP BY e.JobTitle";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            System.out.println("Total Pay by Job Title:");
            while (rs.next()) {
                System.out.println("Job Title: " + rs.getString("JobTitle") +
                                   ", Total Paid: $" + rs.getDouble("TotalPaid"));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving pay by job title: " + e.getMessage());
        }
    }
    
    public void printTotalPayByDivision() {
        String sql = "SELECT e.Division, SUM(s.Amount) AS TotalPaid " +
                     "FROM Employee e " +
                     "JOIN Salary s ON e.ID = s.EmployeeID " +
                     "GROUP BY e.Division";
        try (Connection conn = this.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            System.out.println("Total Pay by Division:");
            while (rs.next()) {
                System.out.println("Division: " + rs.getString("Division") +
                                   ", Total Paid: $" + rs.getDouble("TotalPaid"));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving pay by division: " + e.getMessage());
        }
    }
    
    public void printCombinedPayStats() {
        printTotalPayByJobTitle();
        printTotalPayByDivision();
    }
 

    public static void main(String[] args) {
        EmployeeManagementSystem dbOps = new EmployeeManagementSystem();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.println("\n1. Search for an employee");
            System.out.println("2. Update salary");
            System.out.println("3. Add employee and salary");
            System.out.println("4. View all employees");
            System.out.println("5. Display Pay Statement");
            System.out.println("6. Update employee profile");
            System.out.println("7. Exit");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    scanner.nextLine(); 
                    System.out.print("Enter search criteria (Name, SSN, or ID): ");
                    String criteria = scanner.nextLine();
                    System.out.print("Enter value: ");
                    String value = scanner.nextLine();
                    dbOps.searchEmployee(criteria, value);
                    break;
                case 2:
                    System.out.print("Enter salary increase percentage: ");
                    double percentage = scanner.nextDouble();
                    System.out.print("Enter minimum salary range: ");
                    double minSalary = scanner.nextDouble();
                    System.out.print("Enter maximum salary range: ");
                    double maxSalary = scanner.nextDouble();
                    dbOps.updateSalary(percentage, minSalary, maxSalary);
                    break;
                case 3:
                    scanner.nextLine(); 
                    System.out.print("Enter name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter SSN: ");
                    String ssn = scanner.nextLine();
                    System.out.print("Enter job title: ");
                    String jobTitle = scanner.nextLine();
                    System.out.print("Enter division: ");
                    String division = scanner.nextLine();
                    System.out.print("Enter salary: ");
                    double salary = scanner.nextDouble();
                    dbOps.addEmployeeAndSalary(name, ssn, jobTitle, division, salary);
                    break;
                case 4:
                    dbOps.viewAllEmployees();
                    break;
                case 5:
                    dbOps.printCombinedPayStats(); 
                    break;
                case 6:
                    dbOps.updateEmployeeProfile();
                    break;
                case 7:
                    System.out.println("Exiting...");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number between 1 and 7.");
            }
        }
        scanner.close();
    }
}