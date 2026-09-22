package com.inventory.daos;

import com.inventory.config.DatabaseConnection;
import com.inventory.models.AuditLog;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class AuditLogDao {

    public AuditLogDao() {
        ensureTableExists();
    }

    private void ensureTableExists() {
        String sql = "CREATE TABLE IF NOT EXISTS audit_logs (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "org_id INT NOT NULL, " +
                     "user_id INT, " +
                     "action VARCHAR(100) NOT NULL, " +
                     "entity_type VARCHAR(50) NOT NULL, " +
                     "entity_id INT, " +
                     "details TEXT, " +
                     "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                     "FOREIGN KEY (org_id) REFERENCES organizations(id) ON DELETE CASCADE, " +
                     "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL" +
                     ")";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            // Ignore if already exists or schema handles it
        }
    }

    public boolean logAction(AuditLog log) {
        String sql = "INSERT INTO audit_logs (org_id, user_id, action, entity_type, entity_id, details) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, log.getOrgId());
            if (log.getUserId() != null) {
                stmt.setInt(2, log.getUserId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, log.getAction());
            stmt.setString(4, log.getEntityType());
            if (log.getEntityId() != null) {
                stmt.setInt(5, log.getEntityId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }
            stmt.setString(6, log.getDetails());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        log.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<AuditLog> getLogsByOrgId(int orgId, int limit, int offset) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT a.*, u.username FROM audit_logs a " +
                     "LEFT JOIN users u ON a.user_id = u.id " +
                     "WHERE a.org_id = ? " +
                     "ORDER BY a.created_at DESC LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orgId);
            stmt.setInt(2, Math.max(1, limit));
            stmt.setInt(3, Math.max(0, offset));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setId(rs.getInt("id"));
                    log.setOrgId(rs.getInt("org_id"));
                    int uid = rs.getInt("user_id");
                    if (!rs.wasNull()) {
                        log.setUserId(uid);
                    }
                    log.setUsername(rs.getString("username"));
                    log.setAction(rs.getString("action"));
                    log.setEntityType(rs.getString("entity_type"));
                    int eid = rs.getInt("entity_id");
                    if (!rs.wasNull()) {
                        log.setEntityId(eid);
                    }
                    log.setDetails(rs.getString("details"));
                    log.setCreatedAt(rs.getTimestamp("created_at"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    public int countLogsByOrgId(int orgId) {
        String sql = "SELECT COUNT(*) FROM audit_logs WHERE org_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
