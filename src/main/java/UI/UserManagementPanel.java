package UI;

import model.User;
import model.UserRole;
import service.UserManagementService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class UserManagementPanel extends JPanel {
    private UserManagementService userService;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton resetPasswordButton;
    private AtomicBoolean isLoading = new AtomicBoolean(false);

    public UserManagementPanel() {
        this.userService = new UserManagementService();
        initializeComponents();
        setupLayout();
        addEventListeners();
        loadData();
    }

    private void initializeComponents() {
        // 表格模型
        String[] columnNames = {"学号", "姓名", "职责", "学院", "班级", "邮箱", "创建时间"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setReorderingAllowed(false);
        userTable.setRowHeight(25);

        // 设置表格列宽
        userTable.getColumnModel().getColumn(0).setPreferredWidth(100); // 用户名
        userTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // 昵称
        userTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // 职责
        userTable.getColumnModel().getColumn(3).setPreferredWidth(120); // 学院
        userTable.getColumnModel().getColumn(4).setPreferredWidth(100); // 班级
        userTable.getColumnModel().getColumn(5).setPreferredWidth(150); // 邮箱
        userTable.getColumnModel().getColumn(6).setPreferredWidth(150); // 创建时间

        // 按钮
        refreshButton = new JButton("刷新");
        addButton = new JButton("新增用户");
        editButton = new JButton("编辑用户");
        deleteButton = new JButton("删除用户");
        resetPasswordButton = new JButton("重置密码");

        // 设置按钮样式
        styleButton(addButton, new Color(92, 184, 92));
        styleButton(editButton, new Color(66, 139, 202));
        styleButton(deleteButton, new Color(217, 83, 79));
        styleButton(resetPasswordButton, new Color(240, 173, 78));
        styleButton(refreshButton, new Color(91, 192, 222));
    }

    private void styleButton(JButton button, Color backgroundColor) {
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        button.setPreferredSize(new Dimension(90, 30));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // 顶部按钮面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.add(refreshButton);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(addButton);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(editButton);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(deleteButton);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(resetPasswordButton);

        // 标题
        JLabel titleLabel = new JLabel("学籍管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // 表格滚动面板
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createTitledBorder("用户列表"));

        add(titleLabel, BorderLayout.NORTH);
        add(topPanel, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void addEventListeners() {
        refreshButton.addActionListener(e -> loadData());

        addButton.addActionListener(e -> showAddUserDialog());

        editButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow >= 0) {
                String username = (String) tableModel.getValueAt(selectedRow, 0);
                User user = userService.getUserByUsername(username);
                if (user != null) {
                    showEditUserDialog(user);
                }
            } else {
                JOptionPane.showMessageDialog(this, "请先选择要编辑的用户", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow >= 0) {
                String username = (String) tableModel.getValueAt(selectedRow, 0);
                String nickname = (String) tableModel.getValueAt(selectedRow, 1);

                int result = JOptionPane.showConfirmDialog(this,
                        "确定要删除用户 [" + nickname + "] 吗？\n此操作不可恢复！",
                        "确认删除",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (result == JOptionPane.YES_OPTION) {
                    deleteUser(username);
                }
            } else {
                JOptionPane.showMessageDialog(this, "请先选择要删除的用户", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        resetPasswordButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow >= 0) {
                String username = (String) tableModel.getValueAt(selectedRow, 0);
                String nickname = (String) tableModel.getValueAt(selectedRow, 1);

                int result = JOptionPane.showConfirmDialog(this,
                        "确定要重置用户 [" + nickname + "] 的密码为默认密码(123)吗？",
                        "确认重置密码",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.YES_OPTION) {
                    resetUserPassword(username);
                }
            } else {
                JOptionPane.showMessageDialog(this, "请先选择要重置密码的用户", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        // 双击表格行显示用户详情
        userTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = userTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        String username = (String) tableModel.getValueAt(selectedRow, 0);
                        User user = userService.getUserByUsername(username);
                        if (user != null) {
                            showUserDetails(user);
                        }
                    }
                }
            }
        });
    }

    private void loadData() {
        if (!isLoading.compareAndSet(false, true)) {
            return;
        }

        SwingWorker<List<User>, Void> worker = new SwingWorker<List<User>, Void>() {
            @Override
            protected List<User> doInBackground() throws Exception {
                return userService.getAllUsers();
            }

            @Override
            protected void done() {
                try {
                    List<User> users = get();
                    updateUserTable(users);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(UserManagementPanel.this,
                            "加载用户数据失败: " + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    isLoading.set(false);
                }
            }
        };
        worker.execute();
    }

    private void updateUserTable(List<User> users) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);

            for (User user : users) {
                Object[] row = {
                        user.getUsername(),
                        user.getNickname(),
                        userService.getRoleName(user.getRoleCode()),
                        user.getDepartment() != null ? user.getDepartment() : "",
                        user.getClassName() != null ? user.getClassName() : "",
                        user.getEmail() != null ? user.getEmail() : "",
                        user.getCreatedAt() != null ? user.getCreatedAt().toString().substring(0, 19) : ""
                };
                tableModel.addRow(row);
            }
        });
    }

    private void showAddUserDialog() {
        UserDialog dialog = new UserDialog(SwingUtilities.getWindowAncestor(this), "新增用户", true, null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            User newUser = dialog.getUser();
            if (newUser != null) {
                // 设置默认密码
                newUser.setPassword("123");

                SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return userService.createUser(newUser);
                    }

                    @Override
                    protected void done() {
                        try {
                            Boolean success = get();
                            if (success) {
                                JOptionPane.showMessageDialog(UserManagementPanel.this,
                                        "用户创建成功！\n默认密码为：123",
                                        "成功",
                                        JOptionPane.INFORMATION_MESSAGE);
                                loadData();
                            } else {
                                JOptionPane.showMessageDialog(UserManagementPanel.this,
                                        "用户创建失败，用户名可能已存在",
                                        "错误",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(UserManagementPanel.this,
                                    "用户创建失败: " + e.getMessage(),
                                    "错误",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
            }
        }
    }

    private void showEditUserDialog(User user) {
        UserDialog dialog = new UserDialog(SwingUtilities.getWindowAncestor(this), "编辑用户", true, user);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            User updatedUser = dialog.getUser();
            if (updatedUser != null) {
                SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return userService.updateUser(updatedUser);
                    }

                    @Override
                    protected void done() {
                        try {
                            Boolean success = get();
                            if (success) {
                                JOptionPane.showMessageDialog(UserManagementPanel.this,
                                        "用户信息更新成功！",
                                        "成功",
                                        JOptionPane.INFORMATION_MESSAGE);
                                loadData();
                            } else {
                                JOptionPane.showMessageDialog(UserManagementPanel.this,
                                        "用户信息更新失败",
                                        "错误",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(UserManagementPanel.this,
                                    "用户信息更新失败: " + e.getMessage(),
                                    "错误",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
            }
        }
    }

    private void deleteUser(String username) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return userService.deleteUser(username);
            }

            @Override
            protected void done() {
                try {
                    Boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(UserManagementPanel.this,
                                "用户删除成功！",
                                "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(UserManagementPanel.this,
                                "用户删除失败",
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(UserManagementPanel.this,
                            "用户删除失败: " + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void resetUserPassword(String username) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return userService.updateUserPassword(username, "123");
            }

            @Override
            protected void done() {
                try {
                    Boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(UserManagementPanel.this,
                                "用户密码重置成功！\n新密码为：123",
                                "成功",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(UserManagementPanel.this,
                                "密码重置失败",
                                "错误",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(UserManagementPanel.this,
                            "密码重置失败: " + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void showUserDetails(User user) {
        JDialog detailDialog = new JDialog();
        detailDialog.setTitle("用户详情");
        detailDialog.setModal(true);
        detailDialog.setSize(400, 300);
        detailDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("微软雅黑", Font.PLAIN, 12);
        Font valueFont = new Font("微软雅黑", Font.PLAIN, 12);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("用户名:"), gbc);
        gbc.gridx = 1;
        JLabel usernameLabel = new JLabel(user.getUsername());
        usernameLabel.setFont(valueFont);
        panel.add(usernameLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("昵称:"), gbc);
        gbc.gridx = 1;
        JLabel nicknameLabel = new JLabel(user.getNickname());
        nicknameLabel.setFont(valueFont);
        panel.add(nicknameLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("职责:"), gbc);
        gbc.gridx = 1;
        JLabel roleLabel = new JLabel(userService.getRoleName(user.getRoleCode()));
        roleLabel.setFont(valueFont);
        panel.add(roleLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("学院:"), gbc);
        gbc.gridx = 1;
        JLabel departmentLabel = new JLabel(user.getDepartment() != null ? user.getDepartment() : "");
        departmentLabel.setFont(valueFont);
        panel.add(departmentLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("班级:"), gbc);
        gbc.gridx = 1;
        JLabel classLabel = new JLabel(user.getClassName() != null ? user.getClassName() : "");
        classLabel.setFont(valueFont);
        panel.add(classLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("邮箱:"), gbc);
        gbc.gridx = 1;
        JLabel emailLabel = new JLabel(user.getEmail() != null ? user.getEmail() : "");
        emailLabel.setFont(valueFont);
        panel.add(emailLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("创建时间:"), gbc);
        gbc.gridx = 1;
        JLabel createdAtLabel = new JLabel(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
        createdAtLabel.setFont(valueFont);
        panel.add(createdAtLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 7;
        panel.add(new JLabel("更新时间:"), gbc);
        gbc.gridx = 1;
        JLabel updatedAtLabel = new JLabel(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : "");
        updatedAtLabel.setFont(valueFont);
        panel.add(updatedAtLabel, gbc);

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> detailDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(closeButton);

        detailDialog.add(panel, BorderLayout.CENTER);
        detailDialog.add(buttonPanel, BorderLayout.SOUTH);
        detailDialog.setVisible(true);
    }
}