package com.inventory.models;

import java.sql.Timestamp;

public class Alert {
    private long id;
    private int orgId;
    private AlertType alertType;
    private AlertSeverity severity;
    private String title;
    private String message;
    private String entityType;
    private long entityId;
    private boolean isRead;
    private boolean isDismissed;
    private Timestamp createdAt;

    public Alert() {}

    public Alert(long id, int orgId, AlertType alertType, AlertSeverity severity, String title,
                 String message, String entityType, long entityId, boolean isRead,
                 boolean isDismissed, Timestamp createdAt) {
        this.id = id;
        this.orgId = orgId;
        this.alertType = alertType;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.entityType = entityType;
        this.entityId = entityId;
        this.isRead = isRead;
        this.isDismissed = isDismissed;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getOrgId() { return orgId; }
    public void setOrgId(int orgId) { this.orgId = orgId; }

    public AlertType getAlertType() { return alertType; }
    public void setAlertType(AlertType alertType) { this.alertType = alertType; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public long getEntityId() { return entityId; }
    public void setEntityId(long entityId) { this.entityId = entityId; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public boolean isDismissed() { return isDismissed; }
    public void setDismissed(boolean dismissed) { isDismissed = dismissed; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
