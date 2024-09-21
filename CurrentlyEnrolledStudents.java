import java.sql.*;
import java.util.Scanner;

public class CurrentlyEnrolledStudents {
    private Connection conn;
    private int teacherId;

    public CurrentlyEnrolledStudents(Connection conn, int teacherId) {
        this.conn = conn;
        this.teacherId = teacherId;
    }

    public void viewEnrolledStudents(Scanner scanner) throws SQLException {
        // Get the list of courses the teacher is assigned to
        String sql = "SELECT course_id FROM teacher_courses WHERE teacher_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teacherId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No courses assigned to this teacher.");
                    return;
                }

                System.out.println("Select the course ID to view enrolled students:");
                // Display the list of course IDs
                rs.beforeFirst(); // Move cursor to the start
                while (rs.next()) {
                    System.out.println("Course ID: " + rs.getInt("course_id"));
                }

                int selectedCourseId = scanner.nextInt();
                if (!isTeacherAssignedToCourse(selectedCourseId)) {
                    System.out.println("Error: You are not assigned to this course.");
                    return;
                }

                // Query to get the list of students enrolled in the selected course
                sql = "SELECT s.student_id, s.student_name, cs.grade " +
                      "FROM course_students cs " +
                      "JOIN students s ON cs.student_id = s.id " +
                      "WHERE cs.course_id = ?";
                try (PreparedStatement courseStmt = conn.prepareStatement(sql)) {
                    courseStmt.setInt(1, selectedCourseId);

                    try (ResultSet courseRs = courseStmt.executeQuery()) {
                        if (!courseRs.next()) {
                            System.out.println("No students enrolled in this course.");
                            return;
                        }

                        // Display students and grades
                        System.out.println("\nEnrolled Students:");
                        System.out.println("Student ID | Student Name | Grade");
                        do {
                            System.out.println(courseRs.getInt("student_id") + " | " +
                                               courseRs.getString("student_name") + " | " +
                                               courseRs.getString("grade"));
                        } while (courseRs.next());

                        // Update grades
                        System.out.print("\nEnter student ID to update grade: ");
                        int studentId = scanner.nextInt();
                        System.out.print("Enter new grade: ");
                        String newGrade = scanner.next();

                        updateStudentGrade(selectedCourseId, studentId, newGrade);
                    }
                }
            }
        }
    }

    private boolean isTeacherAssignedToCourse(int courseId) throws SQLException {
        String sql = "SELECT 1 FROM teacher_courses WHERE teacher_id = ? AND course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teacherId);
            pstmt.setInt(2, courseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void updateStudentGrade(int courseId, int studentId, String newGrade) throws SQLException {
        String sql = "UPDATE course_students SET grade = ? WHERE course_id = ? AND student_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newGrade);
            pstmt.setInt(2, courseId);
            pstmt.setInt(3, studentId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Grade updated successfully.");
            } else {
                System.out.println("Error updating grade. Please check the student ID and course ID.");
            }
        }
    }
}
