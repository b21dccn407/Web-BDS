package com.example.demo.Room;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Model.Room;
@Transactional

@SpringBootTest
@ActiveProfiles("test")
public class UpdateRoomTest {

    @Autowired
    private RoomController roomController;

    private Connection connection;
    private static final int TEST_PROPERTY_ID = 10022;
    private static final int TEST_OWNER_ID = 33;
    private static final int TEST_ROOM_ID = 1;

    @BeforeEach
    public void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jdbc_property_wejc_property_web", "root", "1234");
        connection.setAutoCommit(false);

        // Clear all data with delete = 0 from room
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM jdbc_property_wejc_property_web.room WHERE `delete` = 0")) {
            ps.executeUpdate();
        }

        // Clear all data with id_property = 10022 from property
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM jdbc_property_wejc_property_web.property WHERE id_property = ?")) {
            ps.setInt(1, TEST_PROPERTY_ID);
            ps.executeUpdate();
        }

        // Insert test property
        String insertPropertyQuery = "INSERT INTO jdbc_property_wejc_property_web.property (id_property, name, province, district, ward, detail_address, " +
                "legal_doc, surface_area, useable_area, width, length, flours, bedrooms, toilet, direction, price, price_type, " +
                "status, note, id_user, created_at, updated_at, `delete`, created_by_staff, created_by_user, type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertPropertyQuery)) {
            ps.setInt(1, TEST_PROPERTY_ID);
            ps.setString(2, "Test Property");
            ps.setString(3, "Hanoi");
            ps.setString(4, "Cau Giay");
            ps.setString(5, "Dich Vong");
            ps.setString(6, "123 Street");
            ps.setString(7, "[]");
            ps.setFloat(8, 100.0f);
            ps.setFloat(9, 80.0f);
            ps.setFloat(10, 10.0f);
            ps.setFloat(11, 10.0f);
            ps.setInt(12, 2);
            ps.setInt(13, 3);
            ps.setInt(14, 2);
            ps.setString(15, "[]");
            ps.setFloat(16, 1000000.0f);
            ps.setInt(17, 1);
            ps.setInt(18, 1);
            ps.setString(19, "Test note");
            ps.setInt(20, TEST_OWNER_ID);
            ps.setDate(21, Date.valueOf("2025-05-21"));
            ps.setDate(22, Date.valueOf("2025-05-21"));
            ps.setInt(23, 0);
            ps.setInt(24, 0);
            ps.setInt(25, TEST_OWNER_ID);
            ps.setInt(26, 1);

            int rows = ps.executeUpdate();
            assertEquals(1, rows, "Failed to insert test property");
        }

        // Insert test room
        String insertRoomQuery = "INSERT INTO jdbc_property_wejc_property_web.room (id_room, name, id_property, `delete`, status, price, id_owner, area, bathroom, bedroom, kitchen, interior, balcony, max_people, created_at, updated_at, frequency) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertRoomQuery)) {
            ps.setInt(1, TEST_ROOM_ID);
            ps.setString(2, "Test Room");
            ps.setInt(3, TEST_PROPERTY_ID);
            ps.setInt(4, 0);
            ps.setInt(5, 1);
            ps.setFloat(6, 5000000.0f);
            ps.setInt(7, TEST_OWNER_ID);
            ps.setFloat(8, 50.0f);
            ps.setInt(9, 1);
            ps.setInt(10, 2);
            ps.setInt(11, 1);
            ps.setString(12, "Fully furnished");
            ps.setInt(13, 1);
            ps.setInt(14, 4);
            ps.setDate(15, Date.valueOf("2025-05-21"));
            ps.setDate(16, Date.valueOf("2025-05-21"));
            ps.setInt(17, 1);
            int rows = ps.executeUpdate();
            assertEquals(1, rows, "Failed to insert test room");
        }

        connection.commit();
    }

    @AfterEach
    public void tearDown() throws SQLException {
        // Rollback all changes
        if (connection != null && !connection.isClosed()) {
            connection.rollback();
            connection.close();
        }
    }

    @Test
    public void testUpdateRoom_Success() {
        // Arrange
        Room room = new Room();
        room.setName("Updated Room");
        room.setArea(60.0f);
        room.setBathroom(2);
        room.setBedroom(3);
        room.setKitchen(1);
        room.setInterial("Modern furnished");
        room.setBalcony(2);
        room.setStatus(2);
        room.setMax_people(5);
        room.setId_property(TEST_PROPERTY_ID);
        room.setPrice(6000000.0f);
        room.setFrequency(2);

        // Act
        ResponseEntity<Map<String, String>> response = roomController.updateRoom(room, String.valueOf(TEST_ROOM_ID));

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "HTTP status should be CREATED");
        Map<String, String> responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals("Room updated successfully!", responseBody.get("message"), "Message should match");

        // Verify updated data
        try (PreparedStatement ps = connection.prepareStatement("SELECT name, area, bathroom, bedroom, kitchen, interior, balcony, status, max_people, id_property, price, frequency FROM jdbc_property_wejc_property_web.room WHERE id_room = ?")) {
            ps.setInt(1, TEST_ROOM_ID);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Room should exist");
                assertEquals("Updated Room", rs.getString("name"), "Updated name should match");
                assertEquals(60.0f, rs.getFloat("area"), 0.01, "Updated area should match");
                assertEquals(2, rs.getInt("bathroom"), "Updated bathroom should match");
                assertEquals(3, rs.getInt("bedroom"), "Updated bedroom should match");
                assertEquals(1, rs.getInt("kitchen"), "Updated kitchen should match");
                assertEquals("Modern furnished", rs.getString("interior"), "Updated interior should match");
                assertEquals(2, rs.getInt("balcony"), "Updated balcony should match");
                assertEquals(2, rs.getInt("status"), "Updated status should match");
                assertEquals(5, rs.getInt("max_people"), "Updated max_people should match");
                assertEquals(TEST_PROPERTY_ID, rs.getInt("id_property"), "Updated id_property should match");
                assertEquals(6000000.0f, rs.getFloat("price"), 0.01, "Updated price should match");
                assertEquals(2, rs.getInt("frequency"), "Updated frequency should match");
            }
        } catch (SQLException e) {
            fail("SQLException during verification: " + e.getMessage());
        }
    }

    @Test
    public void testUpdateRoom_NameEmpty() {
        // Arrange
        Room room = new Room();
        room.setName(""); // Empty name
        room.setArea(60.0f);
        room.setPrice(6000000.0f);

        // Act
        ResponseEntity<Map<String, String>> response = roomController.updateRoom(room, String.valueOf(TEST_ROOM_ID));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "HTTP status should be BAD_REQUEST");
        Map<String, String> responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals("Name not be null or empty", responseBody.get("message"), "Message should match");
    }

    @Test
    public void testUpdateRoom_NameNull() {
        // Arrange
        Room room = new Room();
        room.setName(null); // Null name
        room.setArea(60.0f);
        room.setPrice(6000000.0f);

        // Act
        ResponseEntity<Map<String, String>> response = roomController.updateRoom(room, String.valueOf(TEST_ROOM_ID));

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "HTTP status should be BAD_REQUEST");
        Map<String, String> responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals("Name not be null or empty", responseBody.get("message"), "Message should match");
    }

    @Test
    public void testUpdateRoom_InvalidId() {
        // Arrange
        Room room = new Room();
        room.setName("Updated Room");
        room.setArea(60.0f);
        room.setPrice(6000000.0f);
        String invalidId = "invalid";

        // Act
        ResponseEntity<Map<String, String>> response = roomController.updateRoom(room, invalidId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "HTTP status should be INTERNAL_SERVER_ERROR");
        Map<String, String> responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");
        assertEquals("Error occurred", responseBody.get("message"), "Message should match");
    }
}