import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class FirstConnectionToDataBase {
    private static FirstConnectionToDataBase instance;
    private Connection connection;
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/diplombd?useSSL=true";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    private FirstConnectionToDataBase() throws SQLException {
        try {
            // Загрузка драйвера
            Class.forName(DRIVER_CLASS);
            // Подключение к базе данных
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            // Обработка ошибки, если драйвер не найден
            throw new SQLException("Database driver not found", e);
        } catch (SQLException e) {
            // Обработка ошибки, если не удалось установить соединение
            throw new SQLException("Failed to establish database connection", e);
        }
    }

    public static synchronized FirstConnectionToDataBase getInstance() throws SQLException {
        if (instance == null || (instance.connection != null && instance.connection.isClosed())) {
            instance = new FirstConnectionToDataBase();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                throw e;
            }
        }
        return connection;
    }
}