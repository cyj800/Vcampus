package service;

import database.UserManagementDAO;
import model.User;
import model.UserRole;

import java.util.List;

public class UserManagementService {
    private UserManagementDAO userDAO;

    public UserManagementService() {
        this.userDAO = new UserManagementDAO();
    }

    // 获取所有用户
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    // 创建新用户
    public boolean createUser(User user) {
        // 检查用户名是否已存在
        if (userDAO.isUsernameExists(user.getUsername())) {
            return false;
        }
        return userDAO.createUser(user);
    }

    // 更新用户信息
    public boolean updateUser(User user) {
        return userDAO.updateUser(user);
    }

    // 更新用户密码
    public boolean updateUserPassword(String username, String newPassword) {
        return userDAO.updateUserPassword(username, newPassword);
    }

    // 删除用户
    public boolean deleteUser(String username) {
        return userDAO.deleteUser(username);
    }

    // 根据用户名获取用户
    public User getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }

    // 获取角色名称
    public String getRoleName(int roleCode) {
        switch (roleCode) {
            case 0: return "管理员";
            case 1: return "教师";
            case 2: return "学生";
            default: return "未知";
        }
    }
}