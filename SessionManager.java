import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

public class SessionManager {
    private static volatile SessionManager instance;
    private static final ReentrantLock lock = new ReentrantLock();

    private boolean clientEnter;
    private String currentClientName;
    private boolean managerEnter;
    private String currentManagerName;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = new SessionManager();
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    public boolean isClientEnter() {
        return clientEnter;
    }

    public void setClientEnter(boolean clientEnter) {
        this.clientEnter = clientEnter;
    }

    public String getCurrentClientName() {
        return currentClientName;
    }

    public void setCurrentClientName(String currentClientName) {
        this.currentClientName = currentClientName;
    }

    public boolean isManagerEnter() {
        return managerEnter;
    }

    public void setManagerEnter(boolean managerEnter) {
        this.managerEnter = managerEnter;
    }

    public String getCurrentManagerName() {
        return currentManagerName;
    }

    public void setCurrentManagerName(String currentManagerName) {
        this.currentManagerName = currentManagerName;
    }

    public int getCurrentUserId() {
        return getClientIdByName(currentClientName);
    }

    public Integer getCurrentEmployeeId() {
        if (currentManagerName != null && !currentManagerName.isEmpty()) {
            return getEmployeeIdByName(currentManagerName);
        }
        return null;
    }

    public String getIpAddress() {
        String ipAddress = "1.0.0.1"; // Default value
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream()))) {
            ipAddress = in.readLine().trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ipAddress;
    }

    public String getDeviceType() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "Windows";
        } else if (os.contains("mac")) {
            return "Mac";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            return "Unix/Linux";
        } else {
            return "Other";
        }
    }

    public String getCountryByIp(String ipAddress) {
        String country = "Unknown";
        try {
            URL url = new URL("http://ip-api.com/json/" + ipAddress);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                JSONObject json = new JSONObject(response.toString());
                country = json.optString("country", "Unknown");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return country;
    }

    public int getClientIdByName(String clientName) {
        int clientId = -1;
        String sql = "SELECT id FROM users WHERE user_name = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, clientName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    clientId = resultSet.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching client ID: " + e.getMessage());
            e.printStackTrace();
        }
        return clientId;
    }

    public int getEmployeeIdByName(String managerName) {
        int managerId = -1;
        String sql = "SELECT id_managers FROM managers WHERE manager_name = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, managerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    managerId = resultSet.getInt("id_managers");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching manager ID: " + e.getMessage());
            e.printStackTrace();
        }
        return managerId;
    }

    public String getEmployeeStatusByName(String managerName) {
        String employeeStatus = "Unknown";
        String sql = "SELECT employee_status FROM managers WHERE manager_name = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, managerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    employeeStatus = resultSet.getString("employee_status");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching employee status: " + e.getMessage());
            e.printStackTrace();
        }
        return employeeStatus;
    }

    public void logActivity(int userId, String actionType, String objectType, String details) {
        String sql = "INSERT INTO activity_log (user_id, action_type, object_type, details, timestamp, user_ip, user_device_type) VALUES (?, ?, ?, ?, NOW(), ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, actionType);
            pstmt.setString(3, objectType);
            pstmt.setString(4, details);
            pstmt.setString(5, getIpAddress());
            pstmt.setString(6, getDeviceType());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging activity: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void logManagerActivity(int managerId, String actionType, String objectType, String details) {
        String sql = "INSERT INTO activity_log (user_id, manager_id, action_type, object_type, details, timestamp, user_ip, user_device_type) VALUES (NULL, ?, ?, ?, ?, NOW(), ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, managerId);
            pstmt.setString(2, actionType);
            pstmt.setString(3, objectType);
            pstmt.setString(4, details);
            pstmt.setString(5, getIpAddress());
            pstmt.setString(6, getDeviceType());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging manager activity: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            return FirstConnectionToDataBase.getInstance().getConnection();
        } catch (SQLException e) {
            System.err.println("Error getting database connection: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}