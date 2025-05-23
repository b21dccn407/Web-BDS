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
import org.springframework.ui.Model;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class GetPropertyListTest {

    @Autowired
    private PropertyController propertyController;

    private Connection connection;
    private static final int TEST_USER_ID = 9999;
    private static final List<String[]> testResults = new ArrayList<>();
    private static final String CSV_FILE = "get_property_list_test_results.csv";

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
        try (PreparedStatement ps = connection.prepareStatement(insertQuery)) {
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
    public void testGetPropertyList_Success() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "PROP_GET01";

        try {
            ResponseEntity<List<Property>> response = propertyController.getPropertyList(null);
            assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status should be OK");
            List<Property> properties = response.getBody();
            assertNotNull(properties, "Property list should not be null");
            assertFalse(properties.isEmpty(), "Property list should not be empty");
            boolean hasTestProperty = properties.stream().anyMatch(p -> p.getName().equals("Test Property") && p.getId_user() == TEST_USER_ID);
            assertTrue(hasTestProperty, "Property list should contain the test property");
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Property/PropertyController", "getPropertyList",
                "Kiểm tra lấy danh sách property thành công",
                "Database: Có bản ghi với delete = 0",
                "HTTP Status: 200 OK. Response: List<Property> không rỗng, chứa bản ghi với delete = 0",
                message, result, ""});
    }

    @Test
    public void testGetPropertyList_NoData() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "PROP_GET02";

        // Clear test data
        try (PreparedStatement ps = connection.prepareStatement("UPDATE bds.property SET `delete` = 1 WHERE id_user = ?")) {
            ps.setInt(1, TEST_USER_ID);
            ps.executeUpdate();
        } catch (SQLException e) {
            fail("Failed to update properties: " + e.getMessage());
        }

        try {
            ResponseEntity<List<Property>> response = propertyController.getPropertyList(null);
            assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status should be OK");
            List<Property> properties = response.getBody();
            assertNotNull(properties, "Property list should not be null");
            assertTrue(properties.isEmpty(), "Property list should be empty");
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Property/PropertyController", "getPropertyList",
                "Kiểm tra lấy danh sách khi không có dữ liệu",
                "Database: Không có bản ghi với delete = 0",
                "HTTP Status: 200 OK. Response: List<Property> rỗng",
                message, result, ""});
    }
}