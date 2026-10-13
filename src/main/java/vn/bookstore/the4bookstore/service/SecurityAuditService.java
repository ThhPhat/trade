package vn.bookstore.the4bookstore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SecurityAuditService {
    private static final Logger auditLogger = LoggerFactory.getLogger("SECURITY_AUDIT");

    public void logLoginSuccess(String username, String ipAddress) {
        auditLogger.info("LOGIN_SUCCESS | User: {} | IP: {}", username, ipAddress);
    }

    public void logLoginFailure(String username, String ipAddress, String reason) {
        auditLogger.warn("LOGIN_FAILURE | User: {} | IP: {} | Reason: {}", username, ipAddress, reason);
    }

    public void logRoleChange(String targetUser, String oldRole, String newRole, String changedBy) {
        auditLogger.info("ROLE_CHANGE | Target User: {} | Old Role: {} | New Role: {} | Changed By: {}", 
                targetUser, oldRole, newRole, changedBy);
    }

    public void logAccountLocked(String targetUser, String lockedBy) {
        auditLogger.warn("ACCOUNT_LOCKED | Target User: {} | Locked By: {}", targetUser, lockedBy);
    }

    public void logAccountUnlocked(String targetUser, String unlockedBy) {
        auditLogger.info("ACCOUNT_UNLOCKED | Target User: {} | Unlocked By: {}", targetUser, unlockedBy);
    }

    public void logPasswordChanged(String username, boolean isReset) {
        auditLogger.info("PASSWORD_CHANGED | User: {} | Is Reset: {}", username, isReset);
    }

    public void logWebhookReceived(String webhookType, String ipAddress, boolean verified) {
        if (verified) {
            auditLogger.info("WEBHOOK_RECEIVED | Type: {} | IP: {} | Verified: true", webhookType, ipAddress);
        } else {
            auditLogger.warn("WEBHOOK_RECEIVED_UNVERIFIED | Type: {} | IP: {} | Verified: false", webhookType, ipAddress);
        }
    }

    public void logAccessDenied(String username, String resource, String ipAddress) {
        auditLogger.warn("ACCESS_DENIED | User: {} | Resource: {} | IP: {}", username, resource, ipAddress);
    }

    public void logTokenRevoked(String username, String reason) {
        auditLogger.info("TOKEN_REVOKED | User: {} | Reason: {}", username, reason);
    }
}
