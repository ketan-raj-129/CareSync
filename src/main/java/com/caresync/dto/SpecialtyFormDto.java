package com.caresync.dto;

import java.io.Serializable;

/**
 * Data transfer object for creating and updating medical specialties.
 */
public class SpecialtyFormDto implements Serializable {

    private Long id;
    private String name;
    private String description;
    private String iconName;
    private boolean active = true;

    public SpecialtyFormDto() {}

    public SpecialtyFormDto(String name, String description, boolean active) {
        this(null, name, description, "stethoscope", active);
    }

    public SpecialtyFormDto(String name, String description, String iconName, boolean active) {
        this(null, name, description, iconName, active);
    }

    public SpecialtyFormDto(Long id, String name, String description, boolean active) {
        this(id, name, description, "stethoscope", active);
    }

    public SpecialtyFormDto(Long id, String name, String description, String iconName, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconName = iconName;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
