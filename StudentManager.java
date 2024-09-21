import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class StudentManager {
    private Connection conn;

    public StudentManager(Connection conn) {
        this.conn = conn;
    }
	private static final String RED = "\033[0;31m";
	private static final String RESET = "\033[0m";
    public void manageStudents(Scanner scanner, int roleId) throws SQLException {
        while (true) {
            System.out.println("\n----------------------------------");
            System.out.println(RED+"Student Management Dashboard"+RESET);
            System.out.println("----------------------------------");
            System.out.println("1. Add Student");
            System.out.println("2. Edit Student");
            System.out.println("3. Delete Student");
            System.out.println("4. View Students");
            System.out.println("5. Back to Main Menu");
            System.out.println("----------------------------------\n");
            System.out.print("Select: ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("You selected Add Student\n");
                    addStudent(scanner);
                    break;
                case 2:
                    System.out.println("You selected Edit Student\n");
                    editStudent(scanner);
                    break;
                case 3:
                    if (roleId == 1) { // Admins only
                        System.out.println("You selected Delete Student\n");
                        deleteStudent(scanner);
                    } else {
                        System.out.println("Error: You do not have permission to delete students.");
                    }
                    break;
                case 4:
                    viewStudents();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void addStudent(Scanner scanner) throws SQLException {
        System.out.print("Enter Student ID:");
        int id = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter Student Name:");
        String name = scanner.nextLine();

        System.out.print("Enter Student Grade:");
        int studentGrade = scanner.nextInt();
        scanner.nextLine();

        String sql = "INSERT INTO students (id, name, grade) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.setString(2, name);
            pstmt.setInt(3, studentGrade);
            pstmt.executeUpdate();
            System.out.println("------");
            System.out.println("Student added successfully.");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println("------");
                System.out.println("Error: Duplicate student ID.");
            } else {
                System.out.println("------");
                System.out.println("Error adding student: " + e.getMessage());
            }
        }
    }

    private void editStudent(Scanner scanner) throws SQLException {
        System.out.print("Enter Student ID to edit:");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        System.out.print("Enter new Student Name:");
        String name = scanner.nextLine();

        System.out.print("Enter new Student Grade:");
        int studentGrade = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // Check if the student exists before updating
        if (!studentExists(id)) {
            System.out.println("------");
            System.out.println("No student found with the given ID.");
            return;
        }

        String sql = "UPDATE students SET name = ?, grade = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, studentGrade);
            pstmt.setInt(3, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("------");
                System.out.println("Student updated successfully.");
                // Update course_students table to synchronize grades
                updateCourseStudentsGrade(id, studentGrade);
            } else {
                System.out.println("------");
                System.out.println("No student found with the given ID.");
            }
        } catch (SQLException e) {
            System.out.println("------");
            System.out.println("Error updating student: " + e.getMessage());
        }
    }

    private void updateCourseStudentsGrade(int studentId, int newGrade) throws SQLException {
        String sql = "UPDATE course_students SET grade = ? WHERE student_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newGrade);
            pstmt.setInt(2, studentId);
            pstmt.executeUpdate();
        }
    }

    private void deleteStudent(Scanner scanner) throws SQLException {
        System.out.print("Enter Student ID to delete:");
        int id = scanner.nextInt();
        scanner.nextLine();

        if (isStudentEnrolled(id)) {
            System.out.println("------");
            System.out.println("Error: The student is currently enrolled in a course and cannot be deleted.");
        } else {
            String sql = "DELETE FROM students WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("------");
                    System.out.println("Student deleted successfully.");
                } else {
                    System.out.println("------");
                    System.out.println("No student found with the given ID.");
                }
            }
        }
    }

    private boolean isStudentEnrolled(int studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM course_students WHERE student_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private void viewStudents() throws SQLException {
        String sql = "SELECT * FROM students";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n----------------------------------");
            System.out.println("Student List of 2024:");
            System.out.println("----------------------------------\n");
            System.out.println("Student ID\tStudent Name\t\tGrade");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int grade = rs.getInt("grade");
                System.out.printf("%d\t\t%s\t\t%d\n", id, name, grade);
            }
        }
    }

    private boolean studentExists(int studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM students WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }
}
