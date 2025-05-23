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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AddPropertyTest {

    @Autowired
    private PropertyController propertyController;

    private Connection connection;
    private static final int TEST_USER_ID = 9999;
    private static final List<String[]> testResults = new ArrayList<>();
    private static final String CSV_FILE = "add_property_test_results.csv";

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
    public void testAddProperty_Success() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "PROP_ADD01";

        Property property = new Property();
        property.setName("New Property");
        property.setProvince("Hanoi");
        property.setDistrict("Cau Giay");
        property.setWard("Dich Vong");
        property.setDetail_address("123 Street");
        property.setDoc_list(Collections.emptyList());
        property.setSurface_area(100.0f);
        property.setUseable_area(80.0f);
        property.setWidth(10.0f);
        property.setLength(10.0f);
        property.setFlours(2);
        property.setBedroom(3);
        property.setToilet(2);
        property.setDirection_list(Collections.emptyList());
        property.setPrice(5000.0f);
        property.setPrice_type(1);
        property.setType(1);
        property.setStatus(1);
        property.setId_user(TEST_USER_ID);
        property.setCreated_by_staff(0);
        property.setCreated_by_user(TEST_USER_ID);
        property.setNote("Test note");

        try {
            ResponseEntity<Map<String, String>> response = propertyController.addProperty(property);
            assertEquals(HttpStatus.CREATED, response.getStatusCode(), "HTTP status should be CREATED");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Property added successfully", body.get("message"), "Message should match");

            // Verify database
            try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM bds.property WHERE id_user = ? AND `delete` = 0")) {
                ps.setInt(1, TEST_USER_ID);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Property should be inserted");
                    assertEquals("New Property", rs.getString("name"), "Property name should match");
                    assertEquals(TEST_USER_ID, rs.getInt("created_by_user"), "Created by user should match");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Property/PropertyController", "addProperty",
                "Kiểm tra thêm property thành công với dữ liệu hợp lệ",
                "Property: name=\"New Property\", id_user=" + TEST_USER_ID,
                "HTTP Status: 201 CREATED. Response: message=\"Property added successfully\"",
                message, result, ""});
    }

    @Test
    public void testAddProperty_NullName() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "PROP_ADD02";

        Property property = new Property();
        property.setName(null);
        property.setProvince("Hanoi");
        property.setDistrict("Cau Giay");
        property.setWard("Dich Vong");
        property.setDetail_address("123 Street");
        property.setDoc_list(Collections.emptyList());
        property.setSurface_area(100.0f);
        property.setUseable_area(80.0f);
        property.setWidth(10.0f);
        property.setLength(10.0f);
        property.setFlours(2);
        property.setBedroom(3);
        property.setToilet(2);
        property.setDirection_list(Collections.emptyList());
        property.setPrice(5000.0f);
        property.setPrice_type(1);
        property.setType(1);
        property.setStatus(1);
        property.setId_user(TEST_USER_ID);
        property.setCreated_by_staff(0);
        property.setCreated_by_user(TEST_USER_ID);
        property.setNote("Test note");

        try {
            ResponseEntity<Map<String, String>> response = propertyController.addProperty(property);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "HTTP status should be BAD_REQUEST");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Name must not be null or empty", body.get("message"), "Message should indicate error");

            // Verify database
            try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM bds.property WHERE id_user = ? AND `delete` = 0")) {
                ps.setInt(1, TEST_USER_ID);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Result set should have a count");
                    assertEquals(0, rs.getInt(1), "No property should be inserted");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Property/PropertyController", "addProperty",
                "Kiểm tra thêm property với name null",
                "Property: name=null",
                "HTTP Status: 400 BAD_REQUEST. Response: message=\"Name must not be null or empty\"",
                message, result, ""});
    }

    @Test
    public void testAddProperty_EmptyName() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "PROP_ADD03";

        Property property = new Property();
        property.setName("");
        property.setProvince("Hanoi");
        property.setDistrict("Cau Giay");
        property.setWard("Dich Vong");
        property.setDetail_address("123 Street");
        property.setDoc_list(Collections.emptyList());
        property.setSurface_area(100.0f);
        property.setUseable_area(80.0f);
        property.setWidth(10.0f);
        property.setLength(10.0f);
        property.setFlours(2);
        property.setBedroom(3);
        property.setToilet(2);
        property.setDirection_list(Collections.emptyList());
        property.setPrice(5000.0f);
        property.setPrice_type(1);
        property.setType(1);
        property.setStatus(1);
        property.setId_user(TEST_USER_ID);
        property.setCreated_by_staff(0);
        property.setCreated_by_user(TEST_USER_ID);
        property.setNote("Test note");

        try {
            ResponseEntity<Map<String, String>> response = propertyController.addProperty(property);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "HTTP status should be BAD_REQUEST");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Name must not be null or empty", body.get("message"), "Message should indicate error");

            // Verify database
            try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM bds.property WHERE id_user = ? AND `delete` = 0")) {
                ps.setInt(1, TEST_USER_ID);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Result set should have a count");
                    assertEquals(0, rs.getInt(1), "No property should be inserted");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Property/PropertyController", "addProperty",
                "Kiểm tra thêm property với name rỗng",
                "Property: name=\"\"",
                "HTTP Status: 400 BAD_REQUEST. Response: message=\"Name must not be null or empty\"",
                message, result, ""});
    }

    @Test
    public void testAddProperty_InvalidIdUser() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "PROP_ADD04";

        Property property = new Property();
        property.setName("New Property");
        property.setProvince("Hanoi");
        property.setDistrict("Cau Giay");
        property.setWard("Dich Vong");
        property.setDetail_address("123 Street");
        property.setDoc_list(Collections.emptyList());
        property.setSurface_area(100.0f);
        property.setUseable_area(80.0f);
        property.setWidth(10.0f);
        property.setLength(10.0f);
        property.setFlours(2);
        property.setBedroom(3);
        property.setToilet(2);
        property.setDirection_list(Collections.emptyList());
        property.setPrice(5000.0f);
        property.setPrice_type(1);
        property.setType(1);
        property.setStatus(1);
        property.setId_user(-1); // Invalid id_user
        property.setCreated_by_staff(0);
        property.setCreated_by_user(-1);
        property.setNote("Test note");

        try {
            ResponseEntity<Map<String, String>> response = propertyController.addProperty(property);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "HTTP status should be INTERNAL_SERVER_ERROR");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Error occurred", body.get("message"), "Message should indicate error");

            // Verify database
            try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM bds.property WHERE id_user = ? AND `delete` = 0")) {
                ps.setInt(1, -1);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Result set should have a count");
                    assertEquals(0, rs.getInt(1), "No property should be inserted");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Property/PropertyController", "addProperty",
                "Kiểm tra thêm property với id_user không hợp lệ",
                "Property: id_user=-1",
                "HTTP Status: 500 INTERNAL_SERVER_ERROR. Response: message=\"Error occurred\"",
                message, result, "Controller nên trả 400 BAD_REQUEST theo chuẩn REST"});
    }
}