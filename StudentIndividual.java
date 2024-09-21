import java.sql.*;
import java.util.Scanner;

public class StudentIndividual {
    private Connection conn;

    public StudentIndividual(Connection conn) {
        this.conn = conn;
    }

    public void viewStudentInfo(Scanner scanner) throws SQLException {
        System.out.print("Enter your Student ID: ");
        int studentId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        // Query to get student's name and grade directly from the students table
        String sql = "SELECT s.name AS student_name, cs.course_id, c.course_name, s.grade " +
                     "FROM students s " +
                     "JOIN course_students cs ON s.id = cs.student_id " +
                     "JOIN courses c ON cs.course_id = c.course_id " +
                     "WHERE s.id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, studentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean found = false;
                if (rs.next()) {
                    System.out.println("-----------------------------------------------------");
                    String studentName = rs.getString("student_name");
                    System.out.printf("\nName: %s\n", studentName);
                    do {
                        String courseName = rs.getString("course_name");
                        double grade = rs.getDouble("grade");
                        System.out.println("Course: " + courseName);
                        System.out.println("Grade: " + grade);
                        System.out.println("\n-----------------------------------------------------");
                        found = true;
                    } while (rs.next());
                }

                if (!found) {
					System.out.println("-----------------------------------------------------");	
                    System.out.println("\nNo information found for the provided Student ID.");
                    System.out.println("Please make sure this student is enrolled to one of our courses.");
                    System.out.println("\n-----------------------------------------------------");					
                }
            }
        }
    }
}
