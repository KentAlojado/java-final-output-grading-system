import java.sql.*;
import java.util.Scanner;

public class TeacherManager {
    private Connection conn;

    public TeacherManager(Connection conn) {
        this.conn = conn;
    }
	private static final String RED = "\033[0;31m";
	private static final String RESET = "\033[0m";

    public void manageTeachers(Scanner scanner) throws SQLException {
        while (true) {
            System.out.println("---------------------------------");
            System.out.println(RED+"Welcome to Teachers Dashboard:"+RESET);
            System.out.println("---------------------------------");
            System.out.println("1. Add Teacher");
            System.out.println("2. Edit Teacher");
            System.out.println("3. Delete Teacher");
            System.out.println("4. View Teachers");
            System.out.println("5. Sync Teachers to Users");
            System.out.println("6. Back to Main Menu");
            System.out.println("---------------------------------\n");
            System.out.print("Select:");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println(RED+"You selected Add Teacher\n"+RESET);
                    addTeacher(scanner);
                    break;
                case 2:
                    System.out.println(RED+"You selected Edit Teacher\n"+RESET);
                    editTeacher(scanner);
                    break;
                case 3:
                    System.out.println(RED+"You selected Delete Teacher\n"+RESET);
                    deleteTeacher(scanner);
                    break;
                case 4:
                    viewTeachers();
                    break;
                case 5:
                    System.out.println(RED+"You selected Sync Teachers to Users\n"+RESET);
                    syncTeachersToUsers();
                    break;
                case 6:
                    return; // Back to main menu
                default:
                    System.out.println(RED+"Invalid choice. Please try again."+RESET);
            }
        }
    }

    private void addTeacher(Scanner scanner) throws SQLException {
        System.out.print("Enter Teacher ID: ");
        int teacherId = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter Teacher Name: ");
        String teacherName = scanner.nextLine();

        System.out.print("Enter Username: ");
        String username = scanner.nextLine();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        // Check if teacher ID or username already exists
        if (checkIdExists("teachers", teacherId)) {
            System.out.println(RED+"Teacher ID already exists. Please enter a unique ID."+RESET);
            return;
        }

        if (checkUsernameExists(username)) {
            System.out.println(RED+"Username already exists. Please enter a unique username."+RESET);
            return;
        }

        // Add teacher to the teachers table
        String teacherSql = "INSERT INTO teachers (id, name, uname, pword) VALUES (?, ?, ?, ?)";
        try (PreparedStatement teacherPstmt = conn.prepareStatement(teacherSql)) {
            teacherPstmt.setInt(1, teacherId);
            teacherPstmt.setString(2, teacherName);
            teacherPstmt.setString(3, username);
            teacherPstmt.setString(4, password);

            int rowsInserted = teacherPstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("---------------------------------\n");
                System.out.println(RED+"\nTeacher added successfully!"+RESET);

                // Sync teacher to users table
                syncTeacherToUser(username, password);
            }
        }
    }

    private void syncTeacherToUser(String username, String password) throws SQLException {
        // Check if username already exists in the users table
        if (checkUsernameExists(username)) {
            System.out.println("Username already exists in users table. Skipping sync.");
            return;
        }

        // Add user to the users table
        String sql = "INSERT INTO users (username, password, role_id) VALUES (?, ?, 2)"; // 2 for Teacher role
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                // Get the auto-generated ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        System.out.println(RED+"User synced to users table with ID: "+RESET + userId);
                    }
                }
            }
        }
    }

    private boolean checkUsernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private void editTeacher(Scanner scanner) throws SQLException {
        System.out.println("Enter Teacher ID to edit:");
        int teacherId = scanner.nextInt();
        scanner.nextLine();

        if (!checkIdExists("teachers", teacherId)) {
            System.out.println("Teacher ID not found.");
            return;
        }

        System.out.print("Enter new Teacher Name (or press Enter to keep current name): ");
        String newTeacherName = scanner.nextLine();

        System.out.print("Enter new Username (or press Enter to keep current username): ");
        String newUsername = scanner.nextLine();

        System.out.print("Enter new Password (or press Enter to keep current password): ");
        String newPassword = scanner.nextLine();

        // Fetch current details to check if the username already exists or needs to be updated
        String currentTeacherName = null;
        String currentUsername = null;
        String currentPassword = null;

        String fetchSql = "SELECT name, uname, pword FROM teachers WHERE id = ?";
        try (PreparedStatement fetchPstmt = conn.prepareStatement(fetchSql)) {
            fetchPstmt.setInt(1, teacherId);

            try (ResultSet rs = fetchPstmt.executeQuery()) {
                if (rs.next()) {
                    currentTeacherName = rs.getString("name");
                    currentUsername = rs.getString("uname");
                    currentPassword = rs.getString("pword");
                } else {
                    System.out.println("Teacher ID not found.");
                    return;
                }
            }
        }

        // If no new name is provided, keep the old name
        if (newTeacherName.isEmpty()) {
            newTeacherName = currentTeacherName;
        }

        // If no new username is provided, keep the old username
        if (newUsername.isEmpty()) {
            newUsername = currentUsername;
        }

        // If no new password is provided, keep the old password
        if (newPassword.isEmpty()) {
            newPassword = currentPassword;
        }

        // Update the teachers table
        String updateTeacherSql = "UPDATE teachers SET name = ?, uname = ?, pword = ? WHERE id = ?";
        try (PreparedStatement updateTeacherPstmt = conn.prepareStatement(updateTeacherSql)) {
            updateTeacherPstmt.setString(1, newTeacherName);
            updateTeacherPstmt.setString(2, newUsername);
            updateTeacherPstmt.setString(3, newPassword);
            updateTeacherPstmt.setInt(4, teacherId);

            int rowsUpdated = updateTeacherPstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("---------------------------------\n");
                System.out.println("\nTeacher updated successfully!");

                // Update the users table with new username and password
                updateUser(newUsername, newPassword, currentUsername);
            }
        }
    }

    private void updateUser(String username, String password, String currentUsername) throws SQLException {
        String updateUserSql = "UPDATE users SET username = ?, password = ? WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(updateUserSql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, currentUsername);

            pstmt.executeUpdate();
        }
    }

    private void deleteTeacher(Scanner scanner) throws SQLException {
        System.out.print("Enter Teacher ID to delete: ");
        int teacherId = scanner.nextInt();

        if (!checkIdExists("teachers", teacherId)) {
            System.out.println("Teacher ID not found.");
            return;
        }

        try {
            // Attempt to delete the teacher
            String sql = "DELETE FROM teachers WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, teacherId);
                int rowsDeleted = pstmt.executeUpdate();

                if (rowsDeleted > 0) {
                    System.out.println("---------------------------------\n");
                    System.out.println("\nTeacher deleted successfully!");
                }
            }
        } catch (SQLException e) {
            // Handle foreign key constraint error
            if (e.getSQLState().equals("23000")) {
                System.out.println("---------------------------------\n");
                System.out.println("Error deleting teacher: This teacher is assigned to one or more courses and cannot be deleted.");
            } else {
                System.out.println("---------------------------------\n");
                System.out.println("Error deleting teacher: " + e.getMessage());
            }
        }
    }

    private void viewTeachers() throws SQLException {
        String sql = "SELECT id, name FROM teachers";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            System.out.println("\nList of Teachers for AY 2024-2025");
            System.out.println("---------------------------------");
            System.out.printf("%-10s %-20s\n", "Teacher ID", "Teacher Name");
            System.out.println("---------------------------------");

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                System.out.printf("%-10d %-20s\n", id, name);
            }
        }
    }

	private void syncTeachersToUsers() throws SQLException {
		String sql = "SELECT uname, pword FROM teachers";
		try (PreparedStatement pstmt = conn.prepareStatement(sql);
			 ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				String username = rs.getString("uname");
				String password = rs.getString("pword");

				// Check if username already exists in the users table
				if (!checkUsernameExists(username)) {
					// Add user to the users table
					String insertSql = "INSERT INTO users (username, password, role_id) VALUES (?, ?, 2)"; // 2 for Teacher role
					try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
						insertPstmt.setString(1, username);
						insertPstmt.setString(2, password);

						int rowsInserted = insertPstmt.executeUpdate();
						if (rowsInserted > 0) {
							// Get the auto-generated ID
							try (ResultSet generatedKeys = insertPstmt.getGeneratedKeys()) {
								if (generatedKeys.next()) {
									int userId = generatedKeys.getInt(1);
									System.out.println("Teacher synced to users table with ID: " + userId);
								}
							}
						}
					}
				}
			}
		}
	}


    private boolean checkIdExists(String tableName, int id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
