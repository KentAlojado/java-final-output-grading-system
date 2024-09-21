import java.sql.*;
import java.util.Scanner;

public class CourseManager {
    private Connection conn;

    public CourseManager(Connection conn) {
        this.conn = conn;
    }
    private static final String RESET = "\033[0m"; 
    private static final String RED = "\033[31m";
 
public void manageCourses(Scanner scanner) throws SQLException {
    while (true) {
        System.out.println("---------------------------------");
        System.out.println(RED+"Manage Courses"+RESET);
        System.out.println("---------------------------------");
        System.out.println("1. Add Course");
        System.out.println("2. Edit Course");
        System.out.println("3. Delete Course");
        System.out.println("4. View Students in Course");
        System.out.println("5. Assign Teacher to Course");
        System.out.println("6. Remove Teacher from Course");
        System.out.println("7. Add Student to Course"); // New option
        System.out.println("8. Remove Student from Course"); // New option
        System.out.println("9. Back to Main Menu");
        System.out.println("---------------------------------\n");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (choice) {
            case 1:
				System.out.println("---------------------------------");			
                System.out.println(RED+"You selected Add Course"+RESET);
				System.out.println("---------------------------------");				
                addCourse(scanner);
                break;
            case 2:
			    System.out.println("---------------------------------");
                System.out.println(RED+"You selected Edit Course"+RESET);
		        System.out.println("---------------------------------");		
                editCourse(scanner);
                break;
            case 3:
		        System.out.println("---------------------------------");	
                System.out.println(RED+"You selected Delete Course"+RESET);
		        System.out.println("---------------------------------");		
                deleteCourse(scanner);
                break;
            case 4:
                viewStudentsInCourse(scanner);
                break;
            case 5:
		        System.out.println("---------------------------------");	
                System.out.println(RED+"Assign Teacher to Course"+RESET);
		        System.out.println("---------------------------------");		
                assignTeacherToCourse(scanner);
                break;
            case 6:
		        System.out.println("---------------------------------");	
                System.out.println(RED+"Remove Teacher from Course"+RESET);
		        System.out.println("---------------------------------");		
                removeTeacherFromCourse(scanner);
                break;
            case 7:
		        System.out.println("---------------------------------");	
                System.out.println(RED+"Add Student to Course"+RESET);
		        System.out.println("---------------------------------");		
                addStudentToCourse(scanner);
                break;
            case 8:
		        System.out.println("---------------------------------");	
                System.out.println(RED+"Remove Student from Course"+RESET);
		        System.out.println("---------------------------------");		
                removeStudentFromCourse(scanner);
                break;
            case 9:
                return;
            default:
		        System.out.println("---------------------------------");	
                System.out.println(RED+"Invalid choice. Please try again."+RESET);
		        System.out.println("---------------------------------");		
        }
    }
}


    private void addCourse(Scanner scanner) throws SQLException {
        System.out.print("Enter Course Name: ");
        String courseName = scanner.nextLine();

        String sql = "INSERT INTO courses (course_name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, courseName);
            pstmt.executeUpdate();
			System.out.println("---");
            System.out.println(RED+"Course added successfully."+RESET);
        }
    }

    private void editCourse(Scanner scanner) throws SQLException {
        System.out.print("Enter Course ID: ");
        int courseId = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter new Course Name: ");
        String newCourseName = scanner.nextLine();

        String sql = "UPDATE courses SET course_name = ? WHERE course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newCourseName);
            pstmt.setInt(2, courseId);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
				System.out.println("---");
                System.out.println(RED+"Course updated successfully."+RESET);
            } else {
				System.out.println("---");
                System.out.println("Course not found.");
            }
        }
    }

    private void deleteCourse(Scanner scanner) throws SQLException {
        System.out.print("Enter Course ID: ");
        int courseId = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        String sql = "DELETE FROM courses WHERE course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
				System.out.println("---");		
                System.out.println(RED+"Course deleted successfully."+RESET);
            } else {
				System.out.println("---");			
                System.out.println("Course not found.");
            }
        }
    }

	private void viewStudentsInCourse(Scanner scanner) throws SQLException {
		System.out.print("Enter Course ID: ");
		int courseId = scanner.nextInt();
		scanner.nextLine(); // Consume newline

		// Query to get course name and teacher
		String courseInfoSql = "SELECT c.course_name, t.name AS teacher_name " +
							   "FROM courses c " +
							   "LEFT JOIN teachers t ON c.teacher_id = t.id " +
							   "WHERE c.course_id = ?";

		// Query to get students and their grades in the course
		String studentSql = "SELECT s.id, s.name, cs.grade " +
							"FROM course_students cs " +
							"JOIN students s ON cs.student_id = s.id " +
							"WHERE cs.course_id = ?";

		try (PreparedStatement courseStmt = conn.prepareStatement(courseInfoSql);
			 PreparedStatement studentStmt = conn.prepareStatement(studentSql)) {

			// Set parameters for the queries
			courseStmt.setInt(1, courseId);
			studentStmt.setInt(1, courseId);

			// Execute course query
			try (ResultSet courseRs = courseStmt.executeQuery()) {
				if (courseRs.next()) {
					String courseName = courseRs.getString("course_name");
					String teacherName = courseRs.getString("teacher_name");
					System.out.println("---------------------------------");
					System.out.printf("\nStudents currently enrolled in %s \n(Teacher: %s)\n", courseName, teacherName != null ? teacherName : "No teacher assigned");
					System.out.println("\n----------------------------------------");
					System.out.printf("%-10s %-20s %-10s\n", "ID", "Student Name", "Grade");

					// Execute student query
					try (ResultSet studentRs = studentStmt.executeQuery()) {
						while (studentRs.next()) {
							int studentId = studentRs.getInt("id");
							String studentName = studentRs.getString("name");
							int grade = studentRs.getInt("grade"); // Fetch grade as int
							System.out.printf("%-10d %-20s %-10d\n", studentId, studentName, grade);
						}
					}
				} else {
					System.out.println("Course not available.");
				}
			}
		}
	}



    private void assignTeacherToCourse(Scanner scanner) throws SQLException {
        System.out.print("Enter Course ID: ");
        int courseId = scanner.nextInt();
        scanner.nextLine(); 

        System.out.print("Enter Teacher ID: ");
        int teacherId = scanner.nextInt();
        scanner.nextLine(); 

        String sql = "UPDATE courses SET teacher_id = ? WHERE course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, teacherId);
            pstmt.setInt(2, courseId);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
				System.out.println("---");			
                System.out.println("Teacher assigned to course successfully.");
            } else {
				System.out.println("---");			
                System.out.println("Course not found.");
            }
        }
    }

    private void removeTeacherFromCourse(Scanner scanner) throws SQLException {
        System.out.print("Enter Course ID: ");
        int courseId = scanner.nextInt();
        scanner.nextLine(); 
        String sql = "UPDATE courses SET teacher_id = NULL WHERE course_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, courseId);
            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
				System.out.println("---");			
                System.out.println("Teacher removed from course successfully.");
            } else {
				System.out.println("---");			
                System.out.println("Course not found.");
            }
        }
    }
	
	private void addStudentToCourse(Scanner scanner) throws SQLException {
		System.out.print("Enter Course ID: ");
		int courseId = scanner.nextInt();
		scanner.nextLine(); // Consume newline

		System.out.print("Enter Student ID: ");
		int studentId = scanner.nextInt();
		scanner.nextLine(); // Consume newline

		String sql = "INSERT INTO course_students (course_id, student_id) VALUES (?, ?)";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, courseId);
			pstmt.setInt(2, studentId);
			int rowsInserted = pstmt.executeUpdate();
			if (rowsInserted > 0) {
				System.out.println("---");
				System.out.println("Student added to course successfully.");
			} else {
				System.out.println("---");
				System.out.println("Error adding student to course.");
			}
		} catch (SQLException e) {
			System.out.println("---");
			System.out.println("Error adding student to course: " + e.getMessage());
		}
	}

	private void removeStudentFromCourse(Scanner scanner) throws SQLException {
		System.out.print("Enter Course ID: ");
		int courseId = scanner.nextInt();
		scanner.nextLine(); // Consume newline

		System.out.print("Enter Student ID: ");
		int studentId = scanner.nextInt();
		scanner.nextLine(); // Consume newline

		String sql = "DELETE FROM course_students WHERE course_id = ? AND student_id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, courseId);
			pstmt.setInt(2, studentId);
			int rowsDeleted = pstmt.executeUpdate();
			if (rowsDeleted > 0) {
				System.out.println("---");
				System.out.println("Student removed from course successfully.");
			} else {
				System.out.println("---");
				System.out.println("Error removing student from course or student not found in course.");
			}
		} catch (SQLException e) {
			System.out.println("---");
			System.out.println("Error removing student from course: " + e.getMessage());
		}
	}
	
	
	
}
