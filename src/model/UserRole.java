package model;

public enum UserRole {
    ADMIN("管理员", 0),
    TEACHER("教师", 1),
    STUDENT("学生", 2);

    private final String roleName;
    private final int roleCode;

    UserRole(String roleName, int roleCode) {
        this.roleName = roleName;
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public int getRoleCode() {
        return roleCode;
    }

    public static UserRole fromUsername(String username) {
        if (username == null || username.isEmpty()) {
            return STUDENT; // 默认为学生权限
        }

        char firstChar = username.charAt(0);
        if (Character.isDigit(firstChar)) {
            int firstDigit = Character.getNumericValue(firstChar);
            switch (firstDigit) {
                case 0:
                    return ADMIN;
                case 1:
                    return TEACHER;
                case 2:
                    return STUDENT;
                default:
                    return STUDENT; // 其他数字默认为学生
            }
        }
        return STUDENT; // 非数字开头默认为学生
    }
}