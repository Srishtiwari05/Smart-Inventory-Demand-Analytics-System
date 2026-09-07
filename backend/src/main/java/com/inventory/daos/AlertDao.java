package com.inventory.daos;

import com.inventory.config.DatabaseConnection;
import com.inventory.models.Alert;
import com.inventory.models.AlertSeverity;
import com.inventory.models.AlertSummary;
import com.inventory.models.AlertType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlertDao {

    public boolean createAlert(Alert alert) {
        String sql = "INSERT INTO alerts (org_id, alert_type, severity, title, message, entity_type, entity_id, is_read, is_dismissed) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, alert.getOrgId());
            stmt.setString(2, alert.getAlertType().name());
            stmt.setString(3, alert.getSeverity().name());
            stmt.setString(4, alert.getTitle());
            stmt.setString(5, alert.getMessage());
            stmt.setString(6, alert.getEntityType());
            stmt.setLong(7, alert.getEntityId());
            stmt.setBoolean(8, alert.isRead());
            stmt.setBoolean(9, alert.isDismissed());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        alert.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existsActiveAlert(int orgId, AlertType alertType, String entityType, long entityId) {
        String sql = "SELECT COUNT(*) FROM alerts WHERE org_id = ? AND alert_type = ? AND entity_type = ? AND entity_id = ? AND is_dismissed = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            stmt.setString(2, alertType.name());
            stmt.setString(3, entityType);
            stmt.setLong(4, entityId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Alert> getAlertsByOrgId(int orgId, boolean unreadOnly) {
        List<Alert> alerts = new ArrayList<>();
        String sql = "SELECT * FROM alerts WHERE org_id = ? AND is_dismissed = FALSE " +
                     (unreadOnly ? "AND is_read = FALSE " : "") +
                     "ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    alerts.add(mapResultSetToAlert(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alerts;
    }

    public AlertSummary getAlertSummaryByOrgId(int orgId) {
        String sql = "SELECT " +
                     "COUNT(CASE WHEN is_read = FALSE THEN 1 END) AS unread_total, " +
                     "COUNT(CASE WHEN is_read = FALSE AND severity = 'CRITICAL' THEN 1 END) AS critical_count, " +
                     "COUNT(CASE WHEN is_read = FALSE AND severity = 'HIGH' THEN 1 END) AS high_count, " +
                     "COUNT(CASE WHEN is_read = FALSE AND severity = 'MEDIUM' THEN 1 END) AS medium_count, " +
                     "COUNT(CASE WHEN is_read = FALSE AND severity = 'LOW' THEN 1 END) AS low_count " +
                     "FROM alerts WHERE org_id = ? AND is_dismissed = FALSE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new AlertSummary(
                        rs.getInt("unread_total"),
                        rs.getInt("critical_count"),
                        rs.getInt("high_count"),
                        rs.getInt("medium_count"),
                        rs.getInt("low_count")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new AlertSummary(0, 0, 0, 0, 0);
    }

    public boolean markAsRead(long id, int orgId) {
        String sql = "UPDATE alerts SET is_read = TRUE WHERE id = ? AND org_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setInt(2, orgId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean dismissAlert(long id, int orgId) {
        String sql = "UPDATE alerts SET is_dismissed = TRUE WHERE id = ? AND org_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setInt(2, orgId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean dismissAll(int orgId) {
        String sql = "UPDATE alerts SET is_dismissed = TRUE WHERE org_id = ? AND is_dismissed = FALSE";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orgId);
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Alert mapResultSetToAlert(ResultSet rs) throws SQLException {
        return new Alert(
            rs.getLong("id"),
            rs.getInt("org_id"),
            AlertType.valueOf(rs.getString("alert_type")),
            AlertSeverity.valueOf(rs.getString("severity")),
            rs.getString("title"),
            rs.getString("message"),
            rs.getString("entity_type"),
            rs.getLong("entity_id"),
            rs.getBoolean("is_read"),
            rs.getBoolean("is_dismissed"),
            rs.getTimestamp("created_at")
        );
    }
}
