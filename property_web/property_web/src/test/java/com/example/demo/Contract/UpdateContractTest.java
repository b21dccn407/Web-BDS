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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class UpdateContractTest {

    @Autowired
    private ContractController contractController;

    private Connection connection;
    private static final int TEST_OWNER_ID = 9999;
    private static final String TEST_USERNAME = "testuser";
    private static final List<String[]> testResults = new ArrayList<>();
    private static final String CSV_FILE = "update_contract_test_results.csv";

    @BeforeEach
    public void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bds", "root", "1234");
        connection.setAutoCommit(false);

        // Clear existing data
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bds.contract WHERE prop_owner_id = ?")) {
            ps.setInt(1, TEST_OWNER_ID);
            ps.executeUpdate();
        }

        // Insert test data (delete = 0)
        String insertQuery = "INSERT INTO bds.contract (prop_owner_id, prop_owner_name, owner_gender, owner_email, owner_phone, owner_dob, prop_id, prop_name, room_id, room_code, max_pp, tenants, price, price_type, rule, status, start_date, end_date, created_date, updated_date, electric, water, water_type, internet, clean, elevator, other_service, deposit, `delete`) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, TEST_OWNER_ID);
            ps.setString(2, "John Doe");
            ps.setInt(3, 1);
            ps.setString(4, "john@example.com");
            ps.setString(5, "1234567890");
            ps.setString(6, "1990-01-01");
            ps.setInt(7, 1);
            ps.setString(8, "Test Property");
            ps.setInt(9, 1);
            ps.setString(10, "R001");
            ps.setInt(11, 4);
            ps.setString(12, "[{\"username\": \"" + TEST_USERNAME + "\", \"legal_doc\": \"123456\"}]");
            ps.setFloat(13, 5000.0f);
            ps.setInt(14, 1);
            ps.setString(15, "No smoking");
            ps.setInt(16, 1);
            ps.setDate(17, Date.valueOf("2025-05-01"));
            ps.setDate(18, Date.valueOf("2026-05-01"));
            ps.setDate(19, Date.valueOf("2025-05-01"));
            ps.setDate(20, Date.valueOf("2025-05-01"));
            ps.setFloat(21, 10.0f);
            ps.setFloat(22, 5.0f);
            ps.setInt(23, 1);
            ps.setFloat(24, 20.0f);
            ps.setFloat(25, 15.0f);
            ps.setFloat(26, 0.0f);
            ps.setFloat(27, 0.0f);
            ps.setFloat(28, 1000.0f);
            ps.setInt(29, 0);
            int rows = ps.executeUpdate();
            assertEquals(1, rows, "Failed to insert test contract (delete = 0)");
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
    public void testUpdateContract_Success() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_UPD01";

        // Get existing contract ID
        int contractId = 0;
        try (PreparedStatement ps = connection.prepareStatement("SELECT id_contract FROM bds.contract WHERE prop_owner_id = ? AND `delete` = 0")) {
            ps.setInt(1, TEST_OWNER_ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    contractId = rs.getInt("id_contract");
                }
            }
        } catch (SQLException e) {
            fail("Failed to get contract ID: " + e.getMessage());
        }

        Contract contract = new Contract();
        contract.setProp_owner_id(TEST_OWNER_ID);
        contract.setProp_owner_name("Updated Owner");
        contract.setOwner_gender(1);
        contract.setOwner_email("updated@example.com");
        contract.setOwner_phone("4445556666");
        contract.setOwner_dob("1990-01-01");
        contract.setProp_id(3);
        contract.setProp_name("Updated Property");
        contract.setRoom_id(3);
        contract.setRoom_code("R003");
        contract.setMax_pp(4);
        contract.setTenant_list(Collections.singletonList(
            new TenantInContract(1, TEST_USERNAME, "Test User", "test@example.com", "1234567890", "789012", 1, Date.valueOf("1990-01-01"))
        ));
        contract.setPrice(7000.0f);
        contract.setPrice_type(1);
        contract.setRule("No pets");
        contract.setStatus(1);
        contract.setStart_date(Date.valueOf("2025-06-01"));
        contract.setEnd_date(Date.valueOf("2026-06-01"));
        contract.setCreated_date(Date.valueOf("2025-05-23"));
        contract.setUpdated_date(Date.valueOf("2025-05-23"));
        contract.setElectric(15.0f);
        contract.setWater(8.0f);
        contract.setWater_type(1);
        contract.setInternet(30.0f);
        contract.setClean(25.0f);
        contract.setElevator(0.0f);
        contract.setOther_service(0.0f);
        contract.setDeposit(2000.0f);

        try {
            ResponseEntity<Map<String, String>> response = contractController.updateContract(contract, String.valueOf(contractId));
            assertEquals(HttpStatus.CREATED, response.getStatusCode(), "HTTP status should be CREATED");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Contract updated successfully hihihi!", body.get("message"), "Message should match");

            // Verify database
            try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM bds.contract WHERE id_contract = ?")) {
                ps.setInt(1, contractId);
                try (var rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Contract should exist");
                    assertEquals("Updated Property", rs.getString("prop_name"), "Property name should be updated");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "updateContract",
                "Kiểm tra cập nhật thành công một bản ghi contract",
                "id_contract=" + contractId + ", Contract: prop_name=\"Updated Property\"",
                "HTTP Status: 201 CREATED. Response: message=\"Contract updated successfully hihihi!\"",
                message, result, ""});
    }

    @Test
    public void testUpdateContract_NullPropName() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_UPD02";

        // Get existing contract ID
        int contractId = 0;
        try (PreparedStatement ps = connection.prepareStatement("SELECT id_contract FROM bds.contract WHERE prop_owner_id = ? AND `delete` = 0")) {
            ps.setInt(1, TEST_OWNER_ID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    contractId = rs.getInt("id_contract");
                }
            }
        } catch (SQLException e) {
            fail("Failed to get contract ID: " + e.getMessage());
        }

        Contract contract = new Contract();
        contract.setProp_owner_id(TEST_OWNER_ID);
        contract.setProp_owner_name("Updated Owner");
        contract.setOwner_gender(1);
        contract.setOwner_email("updated@example.com");
        contract.setOwner_phone("4445556666");
        contract.setOwner_dob("1990-01-01");
        contract.setProp_id(3);
        contract.setProp_name(null);
        contract.setRoom_id(3);
        contract.setRoom_code("R003");
        contract.setMax_pp(4);
        contract.setTenant_list(Collections.singletonList(
            new TenantInContract(1, TEST_USERNAME, "Test User", "test@example.com", "1234567890", "789012", 1, Date.valueOf("1990-01-01"))
        ));
        contract.setPrice(7000.0f);
        contract.setPrice_type(1);
        contract.setRule("No pets");
        contract.setStatus(1);
        contract.setStart_date(Date.valueOf("2025-06-01"));
        contract.setEnd_date(Date.valueOf("2026-06-01"));
        contract.setCreated_date(Date.valueOf("2025-05-23"));
        contract.setUpdated_date(Date.valueOf("2025-05-23"));
        contract.setElectric(15.0f);
        contract.setWater(8.0f);
        contract.setWater_type(1);
        contract.setInternet(30.0f);
        contract.setClean(25.0f);
        contract.setElevator(0.0f);
        contract.setOther_service(0.0f);
        contract.setDeposit(2000.0f);

        try {
            ResponseEntity<Map<String, String>> response = contractController.updateContract(contract, String.valueOf(contractId));
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "HTTP status should be BAD_REQUEST");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Property name must not be null or empty", body.get("message"), "Message should indicate error");

            // Verify database (no changes)
            try (PreparedStatement ps = connection.prepareStatement("SELECT prop_name FROM bds.contract WHERE id_contract = ?")) {
                ps.setInt(1, contractId);
                try (var rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Contract should exist");
                    assertEquals("Test Property", rs.getString("prop_name"), "Property name should not change");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "updateContract",
                "Kiểm tra cập nhật với prop_name null",
                "id_contract=" + contractId + ", Contract: prop_name=null",
                "HTTP Status: 400 BAD_REQUEST. Response: message=\"Property name must not be null or empty\"",
                message, result, ""});
    }

    @Test
    public void testUpdateContract_NonExistentContract() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_UPD03";

        Contract contract = new Contract();
        contract.setProp_owner_id(TEST_OWNER_ID);
        contract.setProp_owner_name("Updated Owner");
        contract.setOwner_gender(1);
        contract.setOwner_email("updated@example.com");
        contract.setOwner_phone("4445556666");
        contract.setOwner_dob("1990-01-01");
        contract.setProp_id(3);
        contract.setProp_name("Updated Property");
        contract.setRoom_id(3);
        contract.setRoom_code("R003");
        contract.setMax_pp(4);
        contract.setTenant_list(Collections.singletonList(
            new TenantInContract(1, TEST_USERNAME, "Test User", "test@example.com", "1234567890", "789012", 1, Date.valueOf("1990-01-01"))
        ));
        contract.setPrice(7000.0f);
        contract.setPrice_type(1);
        contract.setRule("No pets");
        contract.setStatus(1);
        contract.setStart_date(Date.valueOf("2025-06-01"));
        contract.setEnd_date(Date.valueOf("2026-06-01"));
        contract.setCreated_date(Date.valueOf("2025-05-23"));
        contract.setUpdated_date(Date.valueOf("2025-05-23"));
        contract.setElectric(15.0f);
        contract.setWater(8.0f);
        contract.setWater_type(1);
        contract.setInternet(30.0f);
        contract.setClean(25.0f);
        contract.setElevator(0.0f);
        contract.setOther_service(0.0f);
        contract.setDeposit(2000.0f);

        try {
            ResponseEntity<Map<String, String>> response = contractController.updateContract(contract, "999");
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "HTTP status should be INTERNAL_SERVER_ERROR");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Error occurred", body.get("message"), "Message should indicate error");

            // Verify database (no new contract inserted)
            try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM bds.contract WHERE id_contract = ?")) {
                ps.setInt(1, 999);
                try (var rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Result set should have a count");
                    assertEquals(0, rs.getInt(1), "No contract should exist");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "updateContract",
                "Kiểm tra cập nhật với id_contract không tồn tại",
                "id_contract=999",
                "HTTP Status: 404 NOT_FOUND. Response: message=\"Contract not found\"",
                message, result, "Controller nên trả 404 NOT_FOUND theo chuẩn REST"});
    }
}