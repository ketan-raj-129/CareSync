package com.caresync.model;

/**
 * Medical specialty category.
 */
public class Specialty extends BaseModel {

    private String name;
    private String description;
    private String iconName = "stethoscope";
    private boolean active = true;

    public Specialty() {
        super();
    }

    public Specialty(Long id) {
        super(id);
    }

    public Specialty(String name, String description, String iconName) {
        super();
        this.name = name;
        this.description = description;
        this.iconName = iconName != null ? iconName : "stethoscope";
        this.active = true;
    }

    public Long getSpecialtyId() {
        return getId();
    }

    public void setSpecialtyId(Long specialtyId) {
        setId(specialtyId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
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

