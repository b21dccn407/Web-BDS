package com.example.demo.Contract;

import com.example.demo.Model.Contract;
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
public class DeleteContractTest {

    @Autowired
    private ContractController contractController;

    private Connection connection;
    private static final int TEST_OWNER_ID = 9999;
    private static final String TEST_USERNAME = "testuser";
    private static final List<String[]> testResults = new ArrayList<>();
    private static final String CSV_FILE = "delete_contract_test_results.csv";

    @BeforeEach
    public void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bds", "root", "1234");
        connection.setAutoCommit(false);

        // Clear existing data
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bds.contract WHERE prop_owner_id = ?")) {
            ps.setInt(1, TEST_OWNER_ID);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bds.invoice WHERE id_contract IN (SELECT id_contract FROM bds.contract WHERE prop_owner_id = ?)")) {
            ps.setInt(1, TEST_OWNER_ID);
            ps.executeUpdate();
        }

        // Insert test contract (delete = 0)
        String insertContractQuery = "INSERT INTO bds.contract (prop_owner_id, prop_owner_name, owner_gender, owner_email, owner_phone, owner_dob, prop_id, prop_name, room_id, room_code, max_pp, tenants, price, price_type, rule, status, start_date, end_date, created_date, updated_date, electric, water, water_type, internet, clean, elevator, other_service, deposit, `delete`) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int contractId;
        try (PreparedStatement ps = connection.prepareStatement(insertContractQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
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
            assertEquals(1, rows, "Failed to insert test contract");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    contractId = rs.getInt(1);
                } else {
                    throw new SQLException("Failed to get contract ID");
                }
            }
        }

        // Insert invoice for contract (delete = 0) for testing invoice check
        // Use minimal columns to avoid schema issues
        String insertInvoiceQuery = "INSERT INTO bds.invoice (id_contract, `delete`) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertInvoiceQuery)) {
            ps.setInt(1, contractId);
            ps.setInt(2, 0);
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
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bds.invoice WHERE id_contract IN (SELECT id_contract FROM bds.contract WHERE prop_owner_id = ?)")) {
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
    public void testDeleteContract_SuccessNoInvoices() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_DEL01";

        // Get contract ID
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

        // Remove invoices
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM bds.invoice WHERE id_contract = ?")) {
            ps.setInt(1, contractId);
            ps.executeUpdate();
        } catch (SQLException e) {
            fail("Failed to remove invoices: " + e.getMessage());
        }

        Contract contract = new Contract();
        contract.setId_contract(contractId);

        try {
            ResponseEntity<Map<String, String>> response = contractController.deleteContract(contract, String.valueOf(contractId));
            assertEquals(HttpStatus.CREATED, response.getStatusCode(), "HTTP status should be CREATED");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Contract deleted successfully!", body.get("message"), "Message should match");

            // Verify database
            try (PreparedStatement ps = connection.prepareStatement("SELECT `delete` FROM bds.contract WHERE id_contract = ?")) {
                ps.setInt(1, contractId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Contract should exist");
                    assertEquals(1, rs.getInt("delete"), "Contract should be soft deleted");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "deleteContract",
                "Kiểm tra xóa contract thành công khi không có hóa đơn liên quan",
                "id_contract=" + contractId + ", no invoices",
                "HTTP Status: 201 CREATED. Response: message=\"Contract deleted successfully!\"",
                message, result, ""});
    }

    @Test
    public void testDeleteContract_HasInvoices() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_DEL02";

        // Get contract ID
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
        contract.setId_contract(contractId);

        try {
            ResponseEntity<Map<String, String>> response = contractController.deleteContract(contract, String.valueOf(contractId));
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "HTTP status should be BAD_REQUEST");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Cannot delete contract with active invoices", body.get("message"), "Message should indicate error");

            // Verify database
            try (PreparedStatement ps = connection.prepareStatement("SELECT `delete` FROM bds.contract WHERE id_contract = ?")) {
                ps.setInt(1, contractId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next(), "Contract should exist");
                    assertEquals(0, rs.getInt("delete"), "Contract should not be deleted");
                }
            }
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "deleteContract",
                "Kiểm tra xóa contract khi có hóa đơn liên quan",
                "id_contract=" + contractId + ", has invoices",
                "HTTP Status: 400 BAD_REQUEST. Response: message=\"Cannot delete contract with active invoices\"",
                message, result, "Controller không kiểm tra hóa đơn"});
    }

    @Test
    public void testDeleteContract_NonExistentContract() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_DEL03";

        Contract contract = new Contract();
        contract.setId_contract(999);

        try {
            ResponseEntity<Map<String, String>> response = contractController.deleteContract(contract, "999");
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "HTTP status should be NOT_FOUND");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Contract not found or already deleted", body.get("message"), "Message should indicate error");
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "deleteContract",
                "Kiểm tra xóa với id_contract không tồn tại",
                "id_contract=999",
                "HTTP Status: 404 NOT_FOUND. Response: message=\"Contract not found or already deleted\"",
                message, result, "Controller không kiểm tra tồn tại contract"});
    }

    @Test
    public void testDeleteContract_InvalidContractId() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_DEL04";

        Contract contract = new Contract();
        contract.setId_contract(0);

        try {
            ResponseEntity<Map<String, String>> response = contractController.deleteContract(contract, "abc");
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "HTTP status should be BAD_REQUEST");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Invalid id_contract", body.get("message"), "Message should indicate error");
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "deleteContract",
                "Kiểm tra xóa với id_contract không phải số",
                "id_contract=\"abc\"",
                "HTTP Status: 400 BAD_REQUEST. Response: message=\"Invalid id_contract\"",
                message, result, ""});
    }

    @Test
    public void testDeleteContract_EmptyContractId() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_DEL05";

        Contract contract = new Contract();
        contract.setId_contract(0);

        try {
            ResponseEntity<Map<String, String>> response = contractController.deleteContract(contract, "");
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "HTTP status should be BAD_REQUEST");
            Map<String, String> body = response.getBody();
            assertNotNull(body, "Response body should not be null");
            assertEquals("Invalid id_contract", body.get("message"), "Message should indicate error");
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "deleteContract",
                "Kiểm tra xóa với id_contract rỗng",
                "id_contract=\"\"",
                "HTTP Status: 400 BAD_REQUEST. Response: message=\"Invalid id_contract\"",
                message, result, ""});
    }
}