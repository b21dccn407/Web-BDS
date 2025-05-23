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
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class DeletePropertyTest {

    @Autowired
    private PropertyController propertyController;

    private Connection connection;
    private static final int TEST_USER_ID = 9999;
    private static final List<String[]> testResults = new ArrayList<>();
    private static final String CSV_FILE = "delete_property_test_results.csv";

    @BeforeEach
    public void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bds", "root", "1234");
        connection.setAutoCommit(false);

        // Clear related data in contract, room, and property
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bds.contract WHERE prop_id IN (SELECT id_property FROM bds.property WHERE id_user = ?)")) {
            ps.setInt(1, TEST_USER_ID);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bds.room WHERE id_property IN (SELECT id_property FROM bds.property WHERE id_user = ?)")) {
            ps.setInt(1, TEST_USER_ID);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bds.property WHERE id_user = ?")) {
            ps.setInt(1, TEST_USER_ID);
            ps.executeUpdate();
        }

        // Insert test property (delete = 0)
        String insertPropertyQuery = "INSERT INTO bds.property (name, province, district, ward, detail_address, legal_doc, surface_area, useable_area, width, length, flours, bedrooms, toilet, direction, price, price_type, type, status, id_user, created_at, updated_at, created_by_staff, created_by_user, note, `delete`) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int propertyId;
        try (PreparedStatement ps = connection.prepareStatement(insertPropertyQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
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

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    propertyId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to get property ID");
                }
            }
        }

        // Insert test contract (delete = 0) for testing contract check
        String insertContractQuery = "INSERT INTO bds.contract (prop_id, prop_owner_id, status, `delete`) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertContractQuery)) {
            ps.setInt(1, propertyId);
            ps.setInt(2, TEST_USER_ID);
            ps.setInt(3, 1);
            ps.setInt(4, 0);
            ps.executeUpdate();
        }

        connection.commit();
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bds.contract WHERE prop_id IN (SELECT id_property FROM bds.property WHERE id_user = ?)")) {
            ps.setInt(1, TEST_USER_ID);
            ps.executeUpdate();
        }
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
    public void testDeleteProperty_SuccessNoContract() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "PROP_DEL01";

        // Get property ID
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

        // Remove contract
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bds.contract WHERE prop_id = ?")) {
            ps.setInt(1, propertyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            fail("Failed to remove contract: " + e.getMessage());
        }

        Property property = new Property();
        property.setId_property(propertyId);

        try {
            ResponseEntity<Map<String, String>> response = propertyController.deleteProperty(property, String.valueOf(propertyId));
            assertEquals(HttpStatus.CREATED, response.getStatusCode(), "HTTP status should be CREATED");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Delete successfully!", body.get("message"), "Message should match");

            // Verify database
            try (PreparedStatement ps = connection.prepareStatement("SELECT `delete` FROM bds.property WHERE id_property = ?")) {
                ps.setInt(1, propertyId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Property should exist");
                    assertEquals(1, rs.getInt("delete"), "Property should be soft deleted");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Property/PropertyController", "deleteProperty",
                "Kiểm tra xóa property thành công khi không có hợp đồng thuê",
                "id_property=" + propertyId + ", no contract",
                "HTTP Status: 201 CREATED. Response: message=\"Delete successfully!\"",
                message, result, ""});
    }

    @Test
    public void testDeleteProperty_HasContract() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "PROP_DEL02";

        // Get property ID
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

        try {
            ResponseEntity<Map<String, String>> response = propertyController.deleteProperty(property, String.valueOf(propertyId));
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "HTTP status should be BAD_REQUEST");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Cannot delete property with active contract", body.get("message"), "Message should indicate error");

            // Verify database
            try (PreparedStatement ps = connection.prepareStatement("SELECT `delete` FROM bds.property WHERE id_property = ?")) {
                ps.setInt(1, propertyId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Property should exist");
                    assertEquals(0, rs.getInt("delete"), "Property should not be deleted");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Property/PropertyController", "deleteProperty",
                "Kiểm tra xóa property khi có hợp đồng thuê",
                "id_property=" + propertyId + ", has contract",
                "HTTP Status: 400 BAD_REQUEST. Response: message=\"Cannot delete property with active contract\"",
                message, result, "Controller không kiểm tra hợp đồng thuê"});
    }

    @Test
    public void testDeleteProperty_NonExistentProperty() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "PROP_DEL03";

        Property property = new Property();
        property.setId_property(999);

        try {
            ResponseEntity<Map<String, String>> response = propertyController.deleteProperty(property, "999");
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
        testResults.add(new String[]{testCaseId, "Property/PropertyController", "deleteProperty",
                "Kiểm tra xóa với id_property không tồn tại",
                "id_property=999",
                "HTTP Status: 500 INTERNAL_SERVER_ERROR. Response: message=\"Error occurred\"",
                message, result, "Controller nên trả 404 NOT_FOUND theo chuẩn REST"});
    }

    @Test
    public void testDeleteProperty_InvalidIdProperty() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "PROP_DEL04";

        Property property = new Property();
        property.setId_property(0);

        try {
            ResponseEntity<Map<String, String>> response = propertyController.deleteProperty(property, "abc");
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "HTTP status should be INTERNAL_SERVER_ERROR");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Error occurred", body.get("message"), "Message should indicate error");
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Property/PropertyController", "deleteProperty",
                "Kiểm tra xóa với id_property không phải số",
                "id_property=\"abc\"",
                "HTTP Status: 500 INTERNAL_SERVER_ERROR. Response: message=\"Error occurred\"",
                message, result, "Controller nên trả 400 BAD_REQUEST theo chuẩn REST"});
    }

    @Test
    public void testDeleteProperty_EmptyIdProperty() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "PROP_DEL05";

        Property property = new Property();
        property.setId_property(0);

        try {
            ResponseEntity<Map<String, String>> response = propertyController.deleteProperty(property, "");
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "HTTP status should be INTERNAL_SERVER_ERROR");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Error occurred", body.get("message"), "Message should indicate error");
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Property/PropertyController", "deleteProperty",
                "Kiểm tra xóa với id_property rỗng",
                "id_property=\"\"",
                "HTTP Status: 500 INTERNAL_SERVER_ERROR. Response: message=\"Error occurred\"",
                message, result, "Controller nên trả 400 BAD_REQUEST theo chuẩn REST"});
    }
}