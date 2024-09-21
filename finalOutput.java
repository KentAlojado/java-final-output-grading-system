/*The system aims to manage courses, users (teachers and students), and the grading process. It will provide functionalities for an administrator to manage the overall system, including adding and managing teachers, students, and courses. Teachers can enter, update, and view grades, while students can view their grades and course information.
*/
import java.sql.*;
import java.util.Scanner;

public class finalOutput {
    private static final String URL = "jdbc:mysql://localhost:3306/school";
    private static final String USER = "kent";
    private static final String PASSWORD = "1234";
    private static final String RESET = "\033[0m"; 
    private static final String RED = "\033[31m";
 
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("\nConnected to the database!");
            System.out.println("-----------------------------------------------------------------------------\n");
            System.out.println(RED+ "MNG GROUP 2 FINAL OUTPUT - GRADING SYSTEM\n"+RESET);
			System.out.println("The system aims to manage courses, users, and the grading process.");
			System.out.println("It will provide functionalities for an administrator to manage the overall,");
			System.out.println("system including adding and managing teachers, students, and courses."); 
			System.out.println("Teachers can enter, update, and view grades while students can"); 
			System.out.println("view their grades and course information.\n");
			System.out.println("Presenters:");
			System.out.println("-----------");			
			System.out.println("Enrife Abuela, Choolin Rosas, Sol Angela Laurio");
			System.out.println("Lora Mae Infante, Justine Recaido, Kent Paul Alojado");			
            System.out.println("-----------------------------------------------------------------------------");
			System.out.println("To continue, please enter your login credentials.\n");
            int roleId = authenticateUser(conn, scanner);
            if (roleId == -1) {
                System.out.println("Exiting...");
                return;
            }

            TeacherManager teacherManager = new TeacherManager(conn);
            StudentManager studentManager = new StudentManager(conn);
            CourseManager courseManager = new CourseManager(conn);

            boolean running = true;
            while (running) {
                switch (roleId) {
                    case 1: // Admin
                        running = adminMenu(conn, scanner, teacherManager, studentManager, courseManager, roleId);
                        break;
                    case 2: // Teacher
                        running = teacherMenu(conn, scanner, teacherManager, studentManager, roleId);
                        break;
                    case 3: // Student
                        running = studentMenu(conn, scanner);
                        break;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static int authenticateUser(Connection conn, Scanner scanner) throws SQLException {
        System.out.print("Username: ");
        String username = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        String sql = "SELECT role_id FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("role_id");
                } else {
                    System.out.println("Invalid username or password.");
                    return -1; // Authentication failed
                }
            }
        }
    }

    private static boolean adminMenu(Connection conn, Scanner scanner, TeacherManager teacherManager, StudentManager studentManager, CourseManager courseManager, int roleId) throws SQLException {
        while (true) {
            System.out.println("\n---------------------------------");			
            System.out.println(RED+"You're now on the Admin Menu:"+RESET);
            System.out.println("---------------------------------");			
            System.out.println("1. Manage Students");
            System.out.println("2. Manage Teachers");
            System.out.println("3. Manage Courses");
            System.out.println("4. Exit");
            System.out.println("---------------------------------\n");			
            System.out.print("Select: ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    studentManager.manageStudents(scanner, roleId);
                    break;
                case 2:
                    teacherManager.manageTeachers(scanner);
                    break;
                case 3:
                    courseManager.manageCourses(scanner);
                    break;
                case 4:
                    System.out.println("Exiting...");
                    System.exit(0); 
                    return false;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static boolean teacherMenu(Connection conn, Scanner scanner, TeacherManager teacherManager, StudentManager studentManager, int roleId) throws SQLException {
        // Fetch teacher ID by username
        System.out.println("\n---------------------------------");
        System.out.println("To access the course you're currently assigned on,\nkindly enter your username.");
        System.out.println("\n---------------------------------");
        System.out.print("Enter your username: ");
        String username = scanner.next();

        int teacherId = getTeacherIdByUsername(conn, username);
        if (teacherId == -1) {
            System.out.println("Error: Teacher ID not found.");
            return false;
        }

        CurrentlyEnrolledStudents enrolledStudents = new CurrentlyEnrolledStudents(conn, teacherId);

        while (true) {
            System.out.println("\n---------------------------------");		
            System.out.println(RED+"Teacher Menu:"+RESET);
            System.out.println("---------------------------------");	
            System.out.println("1. Manage Students");
            //System.out.println("2. View Enrolled Students");
            System.out.println("2. Exit");
            System.out.print("Select: ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    studentManager.manageStudents(scanner, roleId); 
                    break;
                //case 2:
                //    enrolledStudents.viewEnrolledStudents(scanner);
                //    break;
                case 2:
                    System.out.println(RED+"Exiting..."+RESET);
                    System.exit(0); 
                    return false;
                default:
                    System.out.println(RED+"Invalid choice. Please try again."+RESET);
            }
        }
    }

    private static boolean studentMenu(Connection conn, Scanner scanner) throws SQLException {
        StudentIndividual studentIndividual = new StudentIndividual(conn);

        while (true) {
            System.out.println(RED+"\nStudent Menu"+RESET);
            System.out.println("1. View My Courses and Grades");
            System.out.println("2. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    studentIndividual.viewStudentInfo(scanner);
                    break;
                case 2:
                    System.out.println(RED+"Exiting...."+RESET);
                    System.exit(0); 
                default:
                    System.out.println(RED+"Invalid choice. Please try again."+RESET);
            }
        }
    }

    private static int getTeacherIdByUsername(Connection conn, String username) throws SQLException {
        String sql = "SELECT id FROM teachers WHERE uname = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    return -1; // Teacher not found
                }
            }
        }
    }
}
