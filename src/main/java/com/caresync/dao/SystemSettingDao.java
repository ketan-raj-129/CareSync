package com.caresync.dao;

import com.caresync.model.SystemSetting;

import java.util.Optional;

public interface SystemSettingDao extends GenericDao<SystemSetting, String> {

    String getValue(String key, String defaultValue);

    boolean saveOrUpdate(SystemSetting setting);
}

