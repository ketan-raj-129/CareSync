package com.caresync.model;

/**
 * System administrator entity inheriting core credentials and establishing governance scopes.
 */
public class Admin extends User {

    private String department = "System Administration";
    private String adminLevel = "SUPER_ADMIN";

    public Admin() {
        super();
        setRole(Role.ADMIN);
    }

    public Admin(Long id) {
        super(id);
        setRole(Role.ADMIN);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(String adminLevel) {
        this.adminLevel = adminLevel;
    }

    @Override
    public String getDashboardUrl() {
        return "/admin/dashboard";
    }

    @Override
    public boolean canAccessPatientRecord(Long targetPatientId, Object validator) {
        // System administrators manage user accounts, not private medical consultation notes
        return false;
    }
}

