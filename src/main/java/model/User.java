//package model;
//
//import java.time.LocalDateTime;
//
//public class User {
//    private int id;
//    private String username;
//    private String password;
//    private String email;
//    private String nickname;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//    private UserRole role; // 添加角色字段
//
//    // 构造函数
//    public User() {}
//
//    public User(String username, String password, String email, String nickname) {
//        this.username = username;
//        this.password = password;
//        this.email = email;
//        this.nickname = nickname;
//        this.role = UserRole.fromUsername(username); // 根据用户名自动设置权限
//    }
//
//    // Getter和Setter方法
//    public int getId() { return id; }
//    public void setId(int id) { this.id = id; }
//
//    public String getUsername() { return username; }
//    public void setUsername(String username) {
//        this.username = username;
//        this.role = UserRole.fromUsername(username); // 更新用户名时同步更新权限
//    }
//
//    public String getPassword() { return password; }
//    public void setPassword(String password) { this.password = password; }
//
//    public String getEmail() { return email; }
//    public void setEmail(String email) { this.email = email; }
//
//    public String getNickname() { return nickname; }
//    public void setNickname(String nickname) { this.nickname = nickname; }
//
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//
//    public LocalDateTime getUpdatedAt() { return updatedAt; }
//    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
//
//    // 权限相关方法
//    public UserRole getRole() { return role; }
//    public void setRole(UserRole role) { this.role = role; }
//
//    public String getRoleName() { return role != null ? role.getRoleName() : "未知"; }
//    public int getRoleCode() { return role != null ? role.getRoleCode() : -1; }
//
//    @Override
//    public String toString() {
//        return "User{" +
//                "id=" + id +
//                ", username='" + username + '\'' +
//                ", email='" + email + '\'' +
//                ", nickname='" + nickname + '\'' +
//                ", role=" + (role != null ? role.getRoleName() : "null") +
//                '}';
//    }
//}

package model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private String nickname;
    private int roleCode;       // 角色代码: 0-管理员, 1-教师, 2-学生
    private String className;   // 班级（学生用）
    private String department;  // 院系（教师用）
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserRole role; // 添加角色字段

    // 构造函数
    public User() {}

    public User(String username, String password, String email, String nickname) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.role = UserRole.fromUsername(username); // 根据用户名自动设置权限
        this.roleCode = this.role.getRoleCode();
    }

    // Getter和Setter方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        this.username = username;
        this.role = UserRole.fromUsername(username); // 更新用户名时同步更新权限
        this.roleCode = this.role.getRoleCode();
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public int getRoleCode() { return roleCode; }
    public void setRoleCode(int roleCode) {
        this.roleCode = roleCode;
        // 根据roleCode更新UserRole
        switch (roleCode) {
            case 0: this.role = UserRole.ADMIN; break;
            case 1: this.role = UserRole.TEACHER; break;
            case 2: this.role = UserRole.STUDENT; break;
            default: this.role = UserRole.STUDENT; break;
        }
    }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 权限相关方法
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getRoleName() { return role != null ? role.getRoleName() : "未知"; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", nickname='" + nickname + '\'' +
                ", role=" + (role != null ? role.getRoleName() : "null") +
                ", className='" + className + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}