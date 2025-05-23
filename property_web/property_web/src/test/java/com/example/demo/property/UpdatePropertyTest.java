package com.example.demo.Property;

import com.example.demo.Model.Property;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date; // Thêm import này
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UpdatePropertyTest {

    @Autowired
    private PropertyController propertyController;

    private Connection connection;
    private static final int TEST_USER_ID = 9999;
    private static final List<String[]> testResults = new ArrayList<>();
    private static final String CSV_FILE = "update_property_test_results.csv";

    @BeforeEach
    public void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bds", "root", "1234");
        connection.setAutoCommit(false);

        // Clear related data in room and property
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bds.room WHERE id_property IN (SELECT id_property FROM bds.property WHERE id_user = ?)")) {
            ps.setInt(1, TEST_USER_ID);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bds.property WHERE id_user = ?")) {
            ps.setInt(1, TEST_USER_ID);
            ps.executeUpdate();
        }

        // Insert test data (delete = 0)
        String insertQuery = "INSERT INTO bds.property (name, province, district, ward, detail_address, legal_doc, surface_area, useable_area, width, length, flours, bedrooms, toilet, direction, price, price_type, type, status, id_user, created_at, updated_at, created_by_staff, created_by_user, note, `delete`) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "Test Property");
            ps.setString(2, "Hanoi");
            ps.setString(3, "Cau Giay");
            ps.setString(4, "Dich Vong");
            ps.setString(5, "123 Street");
            ps.setString(6, "[]");
            ps.setFloat(7, 100.0f);
            ps.setFloat(8, 80.0f);
            ps.setFloat(9, 10.0f);
            ps.setFloat(10, 10.0f);
            ps.setInt(11, 2);
            ps.setInt(12, 3);
            ps.setInt(13, 2);
            ps.setString(14, "[]");
            ps.setFloat(15, 5000.0f);
            ps.setInt(16, 1);
            ps.setInt(17, 1);
            ps.setInt(18, 1);
            ps.setInt(19, TEST_USER_ID);
            ps.setDate(20, Date.valueOf("2025-05-23"));
            ps.setDate(21, Date.valueOf("2025-05-23"));
            ps.setInt(22, 0);
            ps.setInt(23, TEST_USER_ID);
            ps.setString(24, "Test note");
            ps.setInt(25, 0);
            int rows = ps.executeUpdate();
            assertEquals(1, rows, "Failed to insert test property (delete = 0)");
        }

        connection.commit();
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bds.room WHERE id_property IN (SELECT id_property FROM bds.property WHERE id_user = ?)")) {
            ps.setInt(1, TEST_USER_ID);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bds.property WHERE id_user = ?")) {
            ps.setInt(1, TEST_USER_ID);
            ps.executeUpdate();
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @AfterAll
    static void saveTestResultsToCsv() throws IOException {
        try (FileWriter writer = new FileWriter(CSV_FILE)) {
            writer.append("Mã testcase,Tên File / Folder,Tên hàm,Mục tiêu Testcase,Dữ liệu đầu vào,Kết quả mong muốn đầu ra,Kết quả thực tế,Kết quả,Ghi chú\n");
            for (String[] result : testResults) {
                writer.append(String.join(",", result)).append("\n");
            }
        }
    }

    @Test
    public void testUpdateProperty_Success() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "PROP_UPD01";

        // Get existing property ID
        int propertyId = 0;
        try (PreparedStatement ps = connection.prepareStatement("SELECT id_property FROM bds.property WHERE id_user = ? AND `delete` = 0")) {
            ps.setInt(1, TEST_USER_ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    propertyId = rs.getInt("id_property");
                }
            }
        } catch (SQLException e) {
            fail("Failed to get property ID: " + e.getMessage());
        }

        Property property = new Property();
        property.setId_property(propertyId);
        property.setName("Updated Property");
        property.setProvince("Hanoi");
        property.setDistrict("Ba Dinh");
        property.setWard("Kim Ma");
        property.setDetail_address("456 Street");
        property.setDoc_list(Collections.emptyList());
        property.setSurface_area(120.0f);
        property.setUseable_area(100.0f);
        property.setWidth(12.0f);
        property.setLength(10.0f);
        property.setFlours(3);
        property.setBedroom(4);
        property.setToilet(3);
        property.setDirection_list(Collections.emptyList());
        property.setPrice(6000.0f);
        property.setPrice_type(1);
        property.setType(1);
        property.setStatus(1);
        property.setId_user(TEST_USER_ID);
        property.setCreated_by_staff(0);
        property.setCreated_by_user(TEST_USER_ID);
        property.setNote("Updated note");

        try {
            ResponseEntity<Map<String, String>> response = propertyController.updateProperty(property, String.valueOf(propertyId));
            assertEquals(HttpStatus.CREATED, response.getStatusCode(), "HTTP status should be CREATED");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("News updated successfully!", body.get("message"), "Message should match");

            // Verify database
            try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM bds.property WHERE id_property = ?")) {
                ps.setInt(1, propertyId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Property should exist");
                    assertEquals("Updated Property", rs.getString("name"), "Property name should be updated");
                    assertEquals("Ba Dinh", rs.getString("district"), "District should be updated");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Property/PropertyController", "updateProperty",
                "Kiểm tra cập nhật property thành công",
                "id_property=" + propertyId + ", Property: name=\"Updated Property\"",
                "HTTP Status: 201 CREATED. Response: message=\"News updated successfully!\"",
                message, result, ""});
    }

    @Test
    public void testUpdateProperty_NullName() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "PROP_UPD02";

        // Get existing property ID
        int propertyId = 0;
        try (PreparedStatement ps = connection.prepareStatement("SELECT id_property FROM bds.property WHERE id_user = ? AND `delete` = 0")) {
            ps.setInt(1, TEST_USER_ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    propertyId = rs.getInt("id_property");
                }
            }
        } catch (SQLException e) {
            fail("Failed to get property ID: " + e.getMessage());
        }

        Property property = new Property();
        property.setId_property(propertyId);
        property.setName(null);
        property.setProvince("Hanoi");
        property.setDistrict("Ba Dinh");
        property.setWard("Kim Ma");
        property.setDetail_address("456 Street");
        property.setDoc_list(Collections.emptyList());
        property.setSurface_area(120.0f);
        property.setUseable_area(100.0f);
        property.setWidth(12.0f);
        property.setLength(10.0f);
        property.setFlours(3);
        property.setBedroom(4);
        property.setToilet(3);
        property.setDirection_list(Collections.emptyList());
        property.setPrice(6000.0f);
        property.setPrice_type(1);
        property.setType(1);
        property.setStatus(1);
        property.setId_user(TEST_USER_ID);
        property.setCreated_by_staff(0);
        property.setCreated_by_user(TEST_USER_ID);
        property.setNote("Updated note");

        try {
            ResponseEntity<Map<String, String>> response = propertyController.updateProperty(property, String.valueOf(propertyId));
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "HTTP status should be BAD_REQUEST");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Name must not be null or empty", body.get("message"), "Message should indicate error");

            // Verify database (no changes)
            try (PreparedStatement ps = connection.prepareStatement("SELECT name FROM bds.property WHERE id_property = ?")) {
                ps.setInt(1, propertyId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Property should exist");
                    assertEquals("Test Property", rs.getString("name"), "Property name should not change");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Property/PropertyController", "updateProperty",
                "Kiểm tra cập nhật property với name null",
                "id_property=" + propertyId + ", Property: name=null",
                "HTTP Status: 400 BAD_REQUEST. Response: message=\"Name must not be null or empty\"",
                message, result, ""});
    }

    @Test
    public void testUpdateProperty_NonExistentProperty() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "PROP_UPD03";

        Property property = new Property();
        property.setId_property(999);
        property.setName("Updated Property");
        property.setProvince("Hanoi");
        property.setDistrict("Ba Dinh");
        property.setWard("Kim Ma");
        property.setDetail_address("456 Street");
        property.setDoc_list(Collections.emptyList());
        property.setSurface_area(120.0f);
        property.setUseable_area(100.0f);
        property.setWidth(12.0f);
        property.setLength(10.0f);
        property.setFlours(3);
        property.setBedroom(4);
        property.setToilet(3);
        property.setDirection_list(Collections.emptyList());
        property.setPrice(6000.0f);
        property.setPrice_type(1);
        property.setType(1);
        property.setStatus(1);
        property.setId_user(TEST_USER_ID);
        property.setCreated_by_staff(0);
        property.setCreated_by_user(TEST_USER_ID);
        property.setNote("Updated note");

        try {
            ResponseEntity<Map<String, String>> response = propertyController.updateProperty(property, "999");
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "HTTP status should be INTERNAL_SERVER_ERROR");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Error occurred", body.get("message"), "Message should indicate error");

            // Verify database (no new property inserted)
            try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM bds.property WHERE id_property = ?")) {
                ps.setInt(1, 999);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Result set should have a count");
                    assertEquals(0, rs.getInt(1), "No property should exist");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Property/PropertyController", "updateProperty",
                "Kiểm tra cập nhật với id_property không tồn tại",
                "id_property=999",
                "HTTP Status: 500 INTERNAL_SERVER_ERROR. Response: message=\"Error occurred\"",
                message, result, "Controller nên trả 404 NOT_FOUND theo chuẩn REST"});
    }

    @Test
    public void testUpdateProperty_InvalidIdUser() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "PROP_UPD04";

        // Get existing property ID
        int propertyId = 0;
        try (PreparedStatement ps = connection.prepareStatement("SELECT id_property FROM bds.property WHERE id_user = ? AND `delete` = 0")) {
            ps.setInt(1, TEST_USER_ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    propertyId = rs.getInt("id_property");
                }
            }
        } catch (SQLException e) {
            fail("Failed to get property ID: " + e.getMessage());
        }

        Property property = new Property();
        property.setId_property(propertyId);
        property.setName("Updated Property");
        property.setProvince("Hanoi");
        property.setDistrict("Ba Dinh");
        property.setWard("Kim Ma");
        property.setDetail_address("456 Street");
        property.setDoc_list(Collections.emptyList());
        property.setSurface_area(120.0f);
        property.setUseable_area(100.0f);
        property.setWidth(12.0f);
        property.setLength(10.0f);
        property.setFlours(3);
        property.setBedroom(4);
        property.setToilet(3);
        property.setDirection_list(Collections.emptyList());
        property.setPrice(6000.0f);
        property.setPrice_type(1);
        property.setType(1);
        property.setStatus(1);
        property.setId_user(-1); // Invalid id_user
        property.setCreated_by_staff(0);
        property.setCreated_by_user(-1);
        property.setNote("Updated note");

        try {
            ResponseEntity<Map<String, String>> response = propertyController.updateProperty(property, String.valueOf(propertyId));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "HTTP status should be INTERNAL_SERVER_ERROR");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Error occurred", body.get("message"), "Message should indicate error");

            // Verify database (no changes)
            try (PreparedStatement ps = connection.prepareStatement("SELECT name FROM bds.property WHERE id_property = ?")) {
                ps.setInt(1, propertyId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Property should exist");
                    assertEquals("Test Property", rs.getString("name"), "Property name should not change");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Property/PropertyController", "updateProperty",
                "Kiểm tra cập nhật với id_user không hợp lệ",
                "id_property=" + propertyId + ", Property: id_user=-1",
                "HTTP Status: 500 INTERNAL_SERVER_ERROR. Response: message=\"Error occurred\"",
                message, result, "Controller nên trả 400 BAD_REQUEST theo chuẩn REST"});
    }
}