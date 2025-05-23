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
import org.springframework.ui.Model;

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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class GetContractDetailTest {

    @Autowired
    private ContractController contractController;

    private Connection connection;
    private static final int TEST_OWNER_ID = 9999;
    private static final String TEST_USERNAME = "testuser";
    private static final List<String[]> testResults = new ArrayList<>();
    private static final String CSV_FILE = "get_contract_detail_test_results.csv";

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
    public void testGetContractDetail_Success() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_DET_GET01";

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

        try {
            ResponseEntity<Contract> response = contractController.getContractDetail(null, String.valueOf(contractId));
            assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status should be OK");
            Contract contract = response.getBody();
            assertNotNull(contract, "Contract should not be null");
            assertEquals(contractId, contract.getId_contract(), "Contract ID should match");
            assertEquals("Test Property", contract.getProp_name(), "Property name should match");
            assertEquals(TEST_OWNER_ID, contract.getProp_owner_id(), "Owner ID should match");
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "getContractDetail",
                "Kiểm tra lấy chi tiết contract thành công với id_contract hợp lệ",
                "id_contract=" + contractId,
                "HTTP Status: 200 OK. Response: Contract với id_contract=" + contractId + ", prop_name không null",
                message, result, ""});
    }

    @Test
    public void testGetContractDetail_NonExistentContract() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_DET_GET02";

        try {
            ResponseEntity<Contract> response = contractController.getContractDetail(null, "999");
            assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status should be OK");
            Contract contract = response.getBody();
            assertNotNull(contract, "Contract should not be null");
            assertEquals(0, contract.getId_contract(), "Contract ID should be 0 for non-existent contract");
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "getContractDetail",
                "Kiểm tra lấy chi tiết với id_contract không tồn tại",
                "id_contract=999",
                "HTTP Status: 400 BAD_REQUEST. Response: message=\"Contract not found\"",
                message, result, "Controller nên trả 400 BAD_REQUEST theo mẫu Excel"});
    }

    @Test
    public void testGetContractDetail_InvalidContractId() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_DET_GET03";

        try {
            ResponseEntity<Contract> response = contractController.getContractDetail(null, "abc");
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "HTTP status should be INTERNAL_SERVER_ERROR");
            assertNull(response.getBody(), "Response body should be null");
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "getContractDetail",
                "Kiểm tra lấy chi tiết với id_contract không phải số",
                "id_contract=\"abc\"",
                "HTTP Status: 400 BAD_REQUEST. Response: message=\"Invalid id_contract\"",
                message, result, "Controller nên trả 400 BAD_REQUEST theo mẫu Excel"});
    }

    @Test
    public void testGetContractDetail_EmptyContractId() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_DET_GET04";

        try {
            ResponseEntity<Contract> response = contractController.getContractDetail(null, "");
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "HTTP status should be INTERNAL_SERVER_ERROR");
            assertNull(response.getBody(), "Response body should be null");
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "getContractDetail",
                "Kiểm tra lấy chi tiết với id_contract rỗng",
                "id_contract=\"\"",
                "HTTP Status: 400 BAD_REQUEST. Response: message=\"Invalid id_contract\"",
                message, result, "Controller nên trả 400 BAD_REQUEST theo mẫu Excel"});
    }
}