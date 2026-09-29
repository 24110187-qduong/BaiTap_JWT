package vn.iotstar.baitap10.configs;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

@Component
public class DatabaseInitializer implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Environment env = event.getEnvironment();

        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");

        if (url == null || username == null) {
            return;
        }

        // Tách tên database ra khỏi URL
        String databaseName = extractDatabaseName(url);
        if (databaseName == null) {
            return;
        }

        // URL kết nối không có database (dùng để tạo database)
        String baseUrl = url.substring(0, url.indexOf("/", url.indexOf("//") + 2) + 1);

        try (Connection conn = DriverManager.getConnection(baseUrl, username, password);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE DATABASE IF NOT EXISTS `" + databaseName + "` "
                    + "CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            stmt.executeUpdate(sql);
            System.out.println("✅ Database '" + databaseName + "' đã sẵn sàng.");

        } catch (Exception e) {
            System.err.println("❌ Không thể tạo database: " + e.getMessage());
        }
    }

    private String extractDatabaseName(String url) {
        try {
            // URL dạng: jdbc:mysql://localhost:3306/jwt_springboot3?...
            int start = url.indexOf("/", url.indexOf("//") + 2) + 1;
            int end = url.indexOf("?", start);
            if (end == -1) end = url.length();
            String dbName = url.substring(start, end);
            return dbName.isEmpty() ? null : dbName;
        } catch (Exception e) {
            return null;
        }
    }
}