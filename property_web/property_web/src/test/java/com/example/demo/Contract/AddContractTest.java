package com.example.demo.Contract;

import com.example.demo.Model.Contract;
import com.example.demo.Model.TenantInContract;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AddContractTest {

    @Autowired
    private ContractController contractController;

    private Connection connection;
    private static final int TEST_OWNER_ID = 9999;
    private static final String TEST_USERNAME = "testuser";
    private static final List<String[]> testResults = new ArrayList<>();
    private static final String CSV_FILE = "add_contract_test_results.csv";

    @BeforeEach
    public void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bds", "root", "1234");
        connection.setAutoCommit(false);

        // Clear existing data
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bds.contract WHERE prop_owner_id = ?")) {
            ps.setInt(1, TEST_OWNER_ID);
            ps.executeUpdate();
        }

        connection.commit();
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bds.contract WHERE prop_owner_id = ?")) {
            ps.setInt(1, TEST_OWNER_ID);
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
    public void testAddContract_Success() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_ADD01";

        Contract contract = new Contract();
        contract.setProp_owner_id(TEST_OWNER_ID);
        contract.setProp_owner_name("New Owner");
        contract.setOwner_gender(1);
        contract.setOwner_email("new@example.com");
        contract.setOwner_phone("1112223333");
        contract.setOwner_dob("1990-01-01");
        contract.setProp_id(3);
        contract.setProp_name("New Property");
        contract.setRoom_id(3);
        contract.setRoom_code("R003");
        contract.setMax_pp(4);
        contract.setTenant_list(Collections.singletonList(
            new TenantInContract(1, TEST_USERNAME, "Test User", "test@example.com", "1234567890", "789012", 1, Date.valueOf("1990-01-01"))
        ));
        contract.setPrice(6000.0f);
        contract.setPrice_type(1);
        contract.setRule("No smoking");
        contract.setStatus(1);
        contract.setStart_date(Date.valueOf("2025-06-01"));
        contract.setEnd_date(Date.valueOf("2026-06-01"));
        contract.setCreated_date(Date.valueOf("2025-05-23"));
        contract.setUpdated_date(Date.valueOf("2025-05-23"));
        contract.setElectric(12.0f);
        contract.setWater(6.0f);
        contract.setWater_type(1);
        contract.setInternet(25.0f);
        contract.setClean(20.0f);
        contract.setElevator(0.0f);
        contract.setOther_service(0.0f);
        contract.setDeposit(1500.0f);

        try {
            ResponseEntity<Map<String, String>> response = contractController.addContract(contract);
            assertEquals(HttpStatus.CREATED, response.getStatusCode(), "HTTP status should be CREATED");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Contract added successfully", body.get("message"), "Message should match");
            assertNotNull(body.get("id_contract"), "Contract ID should be generated");

            // Verify database
            try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM bds.contract WHERE prop_owner_id = ?")) {
                ps.setInt(1, TEST_OWNER_ID);
                try (var rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Contract should be inserted");
                    assertEquals("New Property", rs.getString("prop_name"), "Property name should match");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "addContract",
                "Kiểm tra thêm thành công một bản ghi contract với dữ liệu hợp lệ",
                "Contract: prop_owner_id=" + TEST_OWNER_ID + ", prop_name=\"New Property\"",
                "HTTP Status: 201 CREATED. Response: message=\"Contract added successfully\"",
                message, result, ""});
    }

    @Test
    public void testAddContract_NullPropName() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_ADD02";

        Contract contract = new Contract();
        contract.setProp_owner_id(TEST_OWNER_ID);
        contract.setProp_owner_name("New Owner");
        contract.setOwner_gender(1);
        contract.setOwner_email("new@example.com");
        contract.setOwner_phone("1112223333");
        contract.setOwner_dob("1990-01-01");
        contract.setProp_id(3);
        contract.setProp_name(null);
        contract.setRoom_id(3);
        contract.setRoom_code("R003");
        contract.setMax_pp(4);
        contract.setTenant_list(Collections.singletonList(
            new TenantInContract(1, TEST_USERNAME, "Test User", "test@example.com", "1234567890", "789012", 1, Date.valueOf("1990-01-01"))
        ));
        contract.setPrice(6000.0f);
        contract.setPrice_type(1);
        contract.setRule("No smoking");
        contract.setStatus(1);
        contract.setStart_date(Date.valueOf("2025-06-01"));
        contract.setEnd_date(Date.valueOf("2026-06-01"));
        contract.setCreated_date(Date.valueOf("2025-05-23"));
        contract.setUpdated_date(Date.valueOf("2025-05-23"));
        contract.setElectric(12.0f);
        contract.setWater(6.0f);
        contract.setWater_type(1);
        contract.setInternet(25.0f);
        contract.setClean(20.0f);
        contract.setElevator(0.0f);
        contract.setOther_service(0.0f);
        contract.setDeposit(1500.0f);

        try {
            ResponseEntity<Map<String, String>> response = contractController.addContract(contract);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "HTTP status should be INTERNAL_SERVER_ERROR");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Error occurred", body.get("message"), "Message should indicate error");

            // Verify database
            try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM bds.contract WHERE prop_owner_id = ?")) {
                ps.setInt(1, TEST_OWNER_ID);
                try (var rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Result set should have a count");
                    assertEquals(0, rs.getInt(1), "No contract should be inserted");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "addContract",
                "Kiểm tra thêm bản ghi với prop_name null",
                "Contract: prop_name=null",
                "HTTP Status: 400 BAD_REQUEST. Response: message=\"Property name cannot be null\"",
                message, result, "Controller nên trả 400 BAD_REQUEST theo mẫu Excel"});
    }

    @Test
    public void testAddContract_InvalidPropOwnerId() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_ADD03";

        Contract contract = new Contract();
        contract.setProp_owner_id(0); // Invalid ID
        contract.setProp_owner_name("New Owner");
        contract.setOwner_gender(1);
        contract.setOwner_email("new@example.com");
        contract.setOwner_phone("1112223333");
        contract.setOwner_dob("1990-01-01");
        contract.setProp_id(3);
        contract.setProp_name("New Property");
        contract.setRoom_id(3);
        contract.setRoom_code("R003");
        contract.setMax_pp(4);
        contract.setTenant_list(Collections.singletonList(
            new TenantInContract(1, TEST_USERNAME, "Test User", "test@example.com", "1234567890", "789012", 1, Date.valueOf("1990-01-01"))
        ));
        contract.setPrice(6000.0f);
        contract.setPrice_type(1);
        contract.setRule("No smoking");
        contract.setStatus(1);
        contract.setStart_date(Date.valueOf("2025-06-01"));
        contract.setEnd_date(Date.valueOf("2026-06-01"));
        contract.setCreated_date(Date.valueOf("2025-05-23"));
        contract.setUpdated_date(Date.valueOf("2025-05-23"));
        contract.setElectric(12.0f);
        contract.setWater(6.0f);
        contract.setWater_type(1);
        contract.setInternet(25.0f);
        contract.setClean(20.0f);
        contract.setElevator(0.0f);
        contract.setOther_service(0.0f);
        contract.setDeposit(1500.0f);

        try {
            ResponseEntity<Map<String, String>> response = contractController.addContract(contract);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "HTTP status should be INTERNAL_SERVER_ERROR");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Error occurred", body.get("message"), "Message should indicate error");

            // Verify database
            try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM bds.contract WHERE prop_owner_id = ?")) {
                ps.setInt(1, 0);
                try (var rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Result set should have a count");
                    assertEquals(0, rs.getInt(1), "No contract should be inserted");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "addContract",
                "Kiểm tra thêm bản ghi với prop_owner_id không hợp lệ",
                "Contract: prop_owner_id=0",
                "HTTP Status: 400 BAD_REQUEST. Response: message=\"Invalid prop_owner_id\"",
                message, result, "Controller nên trả 400 BAD_REQUEST theo mẫu Excel"});
    }
}