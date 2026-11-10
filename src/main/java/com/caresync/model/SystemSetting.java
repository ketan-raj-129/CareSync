package com.caresync.model;

import java.io.Serializable;
import java.time.Instant;

/**
 * System configuration key-value pair managed by administrators.
 */
public class SystemSetting implements Serializable {

    private String settingKey;
    private String settingValue;
    private String description;
    private Instant updatedAt;
    private Long updatedByUserId;

    public SystemSetting() {
        this.updatedAt = Instant.now();
    }

    public SystemSetting(String settingKey, String settingValue, String description) {
        this();
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getUpdatedByUserId() {
        return updatedByUserId;
    }

    public void setUpdatedByUserId(Long updatedByUserId) {
        this.updatedByUserId = updatedByUserId;
    }
}

