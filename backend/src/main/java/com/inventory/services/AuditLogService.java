package com.inventory.services;

import com.inventory.daos.AuditLogDao;
import com.inventory.models.AuditLog;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogDao auditLogDao;

    public AuditLogService() {
        this.auditLogDao = new AuditLogDao();
    }

    public AuditLogService(AuditLogDao auditLogDao) {
        this.auditLogDao = auditLogDao;
    }

    public boolean log(int orgId, Integer userId, String action, String entityType, Integer entityId, String details) {
        AuditLog log = new AuditLog(orgId, userId, action, entityType, entityId, details);
        return auditLogDao.logAction(log);
    }

    public List<AuditLog> getLogs(int orgId, int page, int limit) {
        int offset = (page - 1) * limit;
        return auditLogDao.getLogsByOrgId(orgId, limit, offset);
    }

    public int getTotalCount(int orgId) {
        return auditLogDao.countLogsByOrgId(orgId);
    }
}
