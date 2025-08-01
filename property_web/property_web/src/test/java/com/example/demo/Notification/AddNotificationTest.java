package com.example.demo.Notification;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Model.Notification;
import com.example.demo.Model.TenantInContract;
@Transactional

@SpringBootTest
@ActiveProfiles("test")
public class AddNotificationTest {

    @Autowired
    private NotificationController notificationController;

    private Connection connection;
    private static final int TEST_OWNER_ID = 9999; // Unique id_owner
    private static final int TEST_NOTIFICATION_ID = 9999; // Unique id_notification

    @BeforeEach
    public void setUp() throws SQLException {
        // Establish connection to the test database
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/web_nhadat", "root", "1234");
        connection.setAutoCommit(false); // Start transaction

        // Clear existing data for TEST_NOTIFICATION_ID
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM web_nhadat.notification WHERE id_notification = ? OR id_owner = ?")) {
            ps.setInt(1, TEST_NOTIFICATION_ID);
            ps.setInt(2, TEST_OWNER_ID);
            ps.executeUpdate();
        }
        connection.commit();
    }

    @AfterEach
    public void tearDown() throws SQLException {
        // Clean up test data
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM web_nhadat.notification WHERE id_notification = ? OR id_owner = ?")) {
            ps.setInt(1, TEST_NOTIFICATION_ID);
            ps.setInt(2, TEST_OWNER_ID);
            ps.executeUpdate();
        }
        connection.commit();

        // Rollback to release any locks
        try {
            connection.rollback();
        } catch (SQLException e) {
            // Ignore rollback errors
        }

        // Close connection
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testAddNotification_Success() throws SQLException {
        // Arrange
        Notification notification = createValidNotification();
        notification.setId_owner(TEST_OWNER_ID);

        // Act
        ResponseEntity<Map<String, String>> response = notificationController.addNotification(notification);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "HTTP status should be CREATED");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals("Noti added successfully", response.getBody().get("message"), "Message should indicate success");

        // Verify data in database
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM web_nhadat.notification WHERE id_owner = ?")) {
            ps.setInt(1, TEST_OWNER_ID);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Notification should exist in database");
                assertEquals("Test Notification", rs.getString("label"), "Label should match");
                assertEquals("This is a test notification", rs.getString("content"), "Content should match");
                assertEquals("/test/path", rs.getString("path"), "Path should match");
                assertEquals(0, rs.getInt("status"), "Status should be 0");
            }
        }
    }

    @Test
    public void testAddNotification_InvalidInput() throws SQLException {
        // Arrange
        Notification notification = createValidNotification();
        notification.setLabel(null); // Invalid input: null label

        // Act
        ResponseEntity<Map<String, String>> response = notificationController.addNotification(notification);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode(),
                "HTTP status should be CREATED for null label");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertEquals("Noti added successfully", response.getBody().get("message"), "Message should indicate success");

        // Verify data in database
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM web_nhadat.notification WHERE id_owner = ?")) {
            ps.setInt(1, TEST_OWNER_ID);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Notification should exist in database");
                assertNull(rs.getString("label"), "Label should be null");
                assertEquals("This is a test notification", rs.getString("content"), "Content should match");
                assertEquals("/test/path", rs.getString("path"), "Path should match");
                assertEquals(0, rs.getInt("status"), "Status should be 0");
            }
        }
    }

    // Helper method to create a valid Notification object
    private Notification createValidNotification() {
        Notification notification = new Notification();
        notification.setLabel("Test Notification");
        notification.setContent("This is a test notification");
        notification.setPath("/test/path");
        notification.setId_owner(TEST_OWNER_ID);
        List<TenantInContract> tenantList = new ArrayList<>();
        TenantInContract tenant = new TenantInContract();
        tenant.setUsername("test_user");
        tenantList.add(tenant);
        notification.setTenant_list(tenantList);
        notification.setStatus(0);
        notification.setCreated_date(new Date(System.currentTimeMillis()));
        return notification;
    }
}