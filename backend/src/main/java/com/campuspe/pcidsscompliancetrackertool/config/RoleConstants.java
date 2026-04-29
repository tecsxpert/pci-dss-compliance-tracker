package com.campuspe.pcidsscompliancetrackertool.config;

public final class RoleConstants {

    private RoleConstants() {
        
    }

    public static final String PREFIX = "ROLE_";

    public static final String ADMIN = "ADMIN";

    public static final String MANAGER = "MANAGER";

    public static final String AUDITOR = "AUDITOR";

    public static final String VIEWER = "VIEWER";

    public static final String HAS_ROLE_ADMIN_OR_MANAGER =
            "hasAnyRole(T(com.campuspe.pcidsscompliancetrackertool.config.RoleConstants).ADMIN, " +
            "T(com.campuspe.pcidsscompliancetrackertool.config.RoleConstants).MANAGER)";

    public static final String IS_AUTHENTICATED = "isAuthenticated()";
}
