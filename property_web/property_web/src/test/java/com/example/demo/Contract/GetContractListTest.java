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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class GetContractListTest {

    @Autowired
    private ContractController contractController;

    private Connection connection;
    private static final int TEST_OWNER_ID = 9999;
    private static final String TEST_USERNAME = "testuser";
    private static final List<String[]> testResults = new ArrayList<>();
    private static final String CSV_FILE = "get_contract_list_test_results.csv";

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
        try (PreparedStatement ps = connection.prepareStatement(insertQuery)) {
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

        // Insert test data (delete = 1)
        try (PreparedStatement ps = connection.prepareStatement(insertQuery)) {
            ps.setInt(1, TEST_OWNER_ID);
            ps.setString(2, "Jane Doe");
            ps.setInt(3, 0);
            ps.setString(4, "jane@example.com");
            ps.setString(5, "0987654321");
            ps.setString(6, "1995-01-01");
            ps.setInt(7, 2);
            ps.setString(8, "Deleted Property");
            ps.setInt(9, 2);
            ps.setString(10, "R002");
            ps.setInt(11, 2);
            ps.setString(12, "[{\"username\": \"deleteduser\", \"legal_doc\": \"654321\"}]");
            ps.setFloat(13, 3000.0f);
            ps.setInt(14, 1);
            ps.setString(15, "No pets");
            ps.setInt(16, 0);
            ps.setDate(17, Date.valueOf("2025-05-01"));
            ps.setDate(18, Date.valueOf("2026-05-01"));
            ps.setDate(19, Date.valueOf("2025-05-01"));
            ps.setDate(20, Date.valueOf("2025-05-01"));
            ps.setFloat(21, 8.0f);
            ps.setFloat(22, 4.0f);
            ps.setInt(23, 1);
            ps.setFloat(24, 15.0f);
            ps.setFloat(25, 10.0f);
            ps.setFloat(26, 0.0f);
            ps.setFloat(27, 0.0f);
            ps.setFloat(28, 500.0f);
            ps.setInt(29, 1);
            int rows = ps.executeUpdate();
            assertEquals(1, rows, "Failed to insert test contract (delete = 1)");
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
    public void testGetContractList_Success() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_GET01";

        try {
            ResponseEntity<List<Contract>> response = contractController.getContractList(null, String.valueOf(TEST_OWNER_ID));
            assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status should be OK");
            List<Contract> contracts = response.getBody();
            assertNotNull(contracts, "Contract list should not be null");
            assertEquals(1, contracts.size(), "Contract list should contain one contract (delete = 0)");
            Contract contract = contracts.get(0);
            assertEquals("John Doe", contract.getProp_owner_name(), "Owner name should be John Doe");
            assertEquals(TEST_OWNER_ID, contract.getProp_owner_id(), "Owner ID should match");
            assertEquals("Test Property", contract.getProp_name(), "Property name should match");
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "getContractList",
                "Kiểm tra lấy danh sách contract thành công với prop_owner_id hợp lệ",
                "prop_owner_id=\"" + TEST_OWNER_ID + "\"",
                "HTTP Status: 200 OK. Response: List<Contract> không rỗng, chứa bản ghi với prop_owner_id=" + TEST_OWNER_ID,
                "", result, ""});
    }

    @Test
    public void testGetContractList_NoDataFound() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_GET02";

        try {
            ResponseEntity<List<Contract>> response = contractController.getContractList(null, "999");
            assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP status should be OK");
            List<Contract> contracts = response.getBody();
            assertNotNull(contracts, "Contract list should not be null");
            assertTrue(contracts.isEmpty(), "Contract list should be empty");
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "getContractList",
                "Kiểm tra lấy danh sách với prop_owner_id không tồn tại",
                "prop_owner_id=\"999\"",
                "HTTP Status: 200 OK. Response: List<Contract> rỗng",
                "", result, ""});
    }

    @Test
    public void testGetContractList_InvalidOwnerId() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_GET03";

        try {
            ResponseEntity<List<Contract>> response = contractController.getContractList(null, "abc");
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "HTTP status should be INTERNAL_SERVER_ERROR");
            assertNull(response.getBody(), "Response body should be null");
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "getContractList",
                "Kiểm tra lấy danh sách với prop_owner_id không phải số",
                "prop_owner_id=\"abc\"",
                "HTTP Status: 400 BAD_REQUEST. Response: Không trả List<Contract>",
                "", result, ""});
    }

    @Test
    public void testGetContractList_EmptyOwnerId() {
        String result = "PASSED";
        String message = "";
        String testCaseId = "CON_GET04";

        try {
            ResponseEntity<List<Contract>> response = contractController.getContractList(null, "");
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "HTTP status should be INTERNAL_SERVER_ERROR");
            assertNull(response.getBody(), "Response body should be null");
        } catch (Exception e) {
            result = "FAILED";
            message = e.getMessage();
        }
        testResults.add(new String[]{testCaseId, "Contract/ContractController", "getContractList",
                "Kiểm tra lấy danh sách với prop_owner_id rỗng",
                "prop_owner_id=\"\"",
                "HTTP Status: 400 BAD_REQUEST. Response: Không trả List<Contract>",
                "", result, ""});
    }
}