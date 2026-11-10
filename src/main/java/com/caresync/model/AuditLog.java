package com.caresync.model;

/**
 * Append-only security and operational audit trail entry.
 */
public class AuditLog extends BaseModel {

    private Long userId;
    private String action;
    private String targetEntity;
    private Long targetId;
    private String ipAddress;
    private String userAgent;
    private String details;
    private String logHash;
    private String prevLogHash;

    public AuditLog() {
        super();
    }

    public AuditLog(Long id) {
        super(id);
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getLogHash() {
        return logHash;
    }

    public void setLogHash(String logHash) {
        this.logHash = logHash;
    }

    public String getPrevLogHash() {
        return prevLogHash;
    }

    public void setPrevLogHash(String prevLogHash) {
        this.prevLogHash = prevLogHash;
    }
}

