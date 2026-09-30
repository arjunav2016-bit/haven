package haven.db;

public class TestUserDAO {

    public static void main(String[] args) {

        UserDAO userDAO = new UserDAO();

        System.out.println("Testing correct password:");
        userDAO.login("teststudent01", "TestPassword123");

        System.out.println();

        System.out.println("Testing wrong password:");
        userDAO.login("teststudent01", "WrongPassword123");

        System.out.println();

        System.out.println("Testing unknown user:");
        userDAO.login("doesnotexist", "TestPassword123");

        MongoDBConnection.getInstance().close();
    }
}