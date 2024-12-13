package com.mjy.exchange.etc;

import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class SecureSQL {
    private Connection connection;

    public SecureSQL(Connection connection) {
        this.connection = connection;
    }

    public boolean checkUserLogin(String email, String password) throws SQLException {
        String query = "SELECT * FROM Member WHERE email = ? AND password = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, email);  // 첫 번째 파라미터
        pstmt.setString(2, password);  // 두 번째 파라미터

        ResultSet rs = pstmt.executeQuery();
        return rs.next();
    }
}

class VulnerableSQL {
    private final Connection connection;

    public VulnerableSQL(Connection connection) {
        this.connection = connection;
    }

    public boolean checkUserLogin(String email, String password) throws SQLException {
        Statement stmt = connection.createStatement();
        // 취약한 쿼리 - 사용자 입력을 그대로 삽입
        String query = "SELECT * FROM Member WHERE email = '" + email + "' AND password = '" + password + "'";
        ResultSet rs = stmt.executeQuery(query);
        return rs.next();
    }
}

public class SQLInjectionTest {
    // 취약한 코드 테스트
    @Test
    public void testVulnerableSQL() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:14306/Exchange", "root", "1234");

        VulnerableSQL vulnerableSQL = new VulnerableSQL(connection);

        // 악의적인 SQL Injection 값
        String maliciousUsername = "' OR 1=1 -- ";
        String maliciousPassword = "";

        boolean result = vulnerableSQL.checkUserLogin(maliciousUsername, maliciousPassword);

        // SQL Injection 공격이 성공하여 로그인되었으므로 true 반환
        assertTrue(result, "SQL Injection 공격이 성공하여 로그인 우회가 발생해야 합니다.");
    }

    // 방어 코드 테스트
    @Test
    public void testSecureSQL() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:14306/Exchange", "root", "1234");

        SecureSQL secureSQL = new SecureSQL(connection);

        // 악의적인 SQL Injection 값
        String maliciousUsername = "' OR 1=1 -- ";
        String maliciousPassword = "";

        boolean result = secureSQL.checkUserLogin(maliciousUsername, maliciousPassword);

        // PreparedStatement로 SQL Injection이 방어되므로 false 반환
        assertFalse(result, "SQL Injection 공격이 방어되어 로그인 실패해야 합니다.");
    }
}
