package UI;

import model.UserRole;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class MainInterface extends JFrame {
    private JPanel mainPanel;
    private JPanel leftPanel;      // 左侧功能选择面板
    private JPanel rightPanel;     // 右侧功能显示面板
    private JPanel contentPanel;   // 右侧内容显示区域
    private JLabel titleLabel;     // 标题标签
    private JLabel userInfoLabel;  // 用户信息标签

    // 功能按钮
    private JButton libraryButton;
    private JButton courseButton;
    private JButton homeworkButton;
    private JButton gradeButton;
    private JButton messageButton;
    private JButton systemButton;  // 系统管理按钮（仅管理员可见）
    private JButton logoutButton;

    private String currentUsername;
    private String currentNickname;
    private UserRole currentUserRole; // 当前用户权限

    // 权限控制映射
    private Map<JButton, UserRole[]> buttonPermissions;

    public MainInterface(String username, String nickname) {
        this.currentUsername = username;
        this.currentNickname = nickname;
        this.currentUserRole = UserRole.fromUsername(username); // 根据用户名确定权限

        initializeComponents();
        setupLayout();
        setupPermissions();
        addEventListeners();
        updateButtonVisibility(); // 根据权限更新按钮可见性

        setTitle("校园平台 - 主界面");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // 最大化窗口
    }

    private void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout());

        // 左侧面板
        leftPanel = new JPanel();
        leftPanel.setBackground(new Color(240, 240, 240));
        leftPanel.setBorder(BorderFactory.createEtchedBorder());

        // 右侧面板
        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEtchedBorder());

        // 标题标签
        titleLabel = new JLabel("欢迎使用校园平台", SwingConstants.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(50, 50, 150));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // 用户信息标签
        userInfoLabel = new JLabel();
        updateUserInfoLabel();
        userInfoLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        userInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        userInfoLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        // 内容面板
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        // 创建功能按钮
        Dimension buttonSize = new Dimension(150, 40);

        libraryButton = new JButton("图书馆功能");
        libraryButton.setPreferredSize(buttonSize);
        libraryButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        courseButton = new JButton("课程管理");
        courseButton.setPreferredSize(buttonSize);
        courseButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        homeworkButton = new JButton("作业管理");
        homeworkButton.setPreferredSize(buttonSize);
        homeworkButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        gradeButton = new JButton("成绩管理");
        gradeButton.setPreferredSize(buttonSize);
        gradeButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        messageButton = new JButton("消息通知");
        messageButton.setPreferredSize(buttonSize);
        messageButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        systemButton = new JButton("系统管理");
        systemButton.setPreferredSize(buttonSize);
        systemButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        systemButton.setBackground(new Color(255, 180, 100));
        systemButton.setForeground(Color.BLACK);

        logoutButton = new JButton("退出登录");
        logoutButton.setPreferredSize(buttonSize);
        logoutButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        logoutButton.setBackground(new Color(255, 100, 100));
        logoutButton.setForeground(Color.BLACK);
    }

    private void setupLayout() {
        setContentPane(mainPanel);

        // 设置左侧功能面板布局
        leftPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 添加用户信息
        gbc.gridx = 0; gbc.gridy = 0;
        leftPanel.add(userInfoLabel, gbc);

        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setPreferredSize(new Dimension(160, 2));
        gbc.gridy = 1;
        leftPanel.add(separator, gbc);

        // 添加功能按钮
        gbc.gridy = 2;
        leftPanel.add(libraryButton, gbc);

        gbc.gridy = 3;
        leftPanel.add(courseButton, gbc);

        gbc.gridy = 4;
        leftPanel.add(homeworkButton, gbc);

        gbc.gridy = 5;
        leftPanel.add(gradeButton, gbc);

        gbc.gridy = 6;
        leftPanel.add(messageButton, gbc);

        gbc.gridy = 7;
        leftPanel.add(systemButton, gbc);

        gbc.gridy = 8;
        gbc.weighty = 1.0; // 占据剩余空间
        leftPanel.add(Box.createVerticalGlue(), gbc);

        gbc.gridy = 9;
        gbc.weighty = 0;
        leftPanel.add(logoutButton, gbc);

        // 设置右侧显示面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.CENTER);
        topPanel.add(userInfoLabel, BorderLayout.SOUTH);

        rightPanel.add(topPanel, BorderLayout.NORTH);
        rightPanel.add(contentPanel, BorderLayout.CENTER);

        // 添加到主面板
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        // 设置左右面板的大小比例
        leftPanel.setPreferredSize(new Dimension(250, getHeight()));
    }

    private void setupPermissions() {
        buttonPermissions = new HashMap<>();

        // 设置每个按钮的权限要求
        buttonPermissions.put(libraryButton, new UserRole[]{UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT});
        buttonPermissions.put(courseButton, new UserRole[]{UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT});
        buttonPermissions.put(homeworkButton, new UserRole[]{UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT});
        buttonPermissions.put(gradeButton, new UserRole[]{UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT});
        buttonPermissions.put(messageButton, new UserRole[]{UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT});
        buttonPermissions.put(systemButton, new UserRole[]{UserRole.ADMIN}); // 仅管理员可见
    }

    private void updateButtonVisibility() {
        for (Map.Entry<JButton, UserRole[]> entry : buttonPermissions.entrySet()) {
            JButton button = entry.getKey();
            UserRole[] allowedRoles = entry.getValue();

            boolean isVisible = false;
            for (UserRole role : allowedRoles) {
                if (currentUserRole == role) {
                    isVisible = true;
                    break;
                }
            }
            button.setVisible(isVisible);
        }
    }

    private void addEventListeners() {
        libraryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showLibraryContent();
            }
        });

        courseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCourseContent();
            }
        });

        homeworkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHomeworkContent();
            }
        });

        gradeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showGradeContent();
            }
        });

        messageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMessageContent();
            }
        });

        systemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSystemContent();
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(
                        MainInterface.this,
                        "确定要退出登录吗？",
                        "退出登录",
                        JOptionPane.YES_NO_OPTION
                );

                if (result == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(MainInterface.this, "已退出登录");
                    // 返回登录界面
                    dispose();
                    // 这里应该重新打开登录界面
                    SwingUtilities.invokeLater(() -> {
                        try {
                            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        new Login().setVisible(true);
                    });
                }
            }
        });
    }

    private void updateUserInfoLabel() {
        String roleInfo = String.format("用户: %s (%s)", currentNickname, currentUserRole.getRoleName());
        userInfoLabel.setText(roleInfo);
        userInfoLabel.setForeground(getRoleColor(currentUserRole));
    }

    private Color getRoleColor(UserRole role) {
        switch (role) {
            case ADMIN:
                return new Color(255, 100, 100); // 红色
            case TEACHER:
                return new Color(100, 150, 255); // 蓝色
            case STUDENT:
                return new Color(100, 200, 100); // 绿色
            default:
                return Color.BLACK;
        }
    }

    private void showLibraryContent() {
        String content = "这里是图书馆功能模块\n\n";
        content += "当前权限: " + currentUserRole.getRoleName() + "\n\n";
        content += "可用功能:\n";

        switch (currentUserRole) {
            case ADMIN:
                content += "• 图书管理\n• 用户借阅记录查询\n• 系统配置\n• 统计报表\n• 图书采购\n• 违章处理";
                break;
            case TEACHER:
                content += "• 图书查询\n• 借阅管理\n• 预约服务\n• 电子资源访问\n• 学术资源推荐";
                break;
            case STUDENT:
                content += "• 图书查询\n• 借阅管理\n• 预约服务\n• 电子资源访问";
                break;
        }

        displayContent("图书馆功能", content);
    }

    private void showCourseContent() {
        String content = "这里是课程管理模块\n\n";
        content += "当前权限: " + currentUserRole.getRoleName() + "\n\n";
        content += "可用功能:\n";

        switch (currentUserRole) {
            case ADMIN:
                content += "• 课程设置\n• 教师分配\n• 课程时间安排\n• 教室资源管理\n• 课程统计分析";
                break;
            case TEACHER:
                content += "• 课程信息管理\n• 学生名单查看\n• 课程资料上传\n• 课程通知发布";
                break;
            case STUDENT:
                content += "• 课程查询\n• 选课管理\n• 课表查看\n• 课程评价";
                break;
        }

        displayContent("课程管理", content);
    }

    private void showHomeworkContent() {
        String content = "这里是作业管理模块\n\n";
        content += "当前权限: " + currentUserRole.getRoleName() + "\n\n";
        content += "可用功能:\n";

        switch (currentUserRole) {
            case ADMIN:
                content += "• 作业系统管理\n• 教师作业监控\n• 学生作业统计\n• 作业质量分析";
                break;
            case TEACHER:
                content += "• 作业发布\n• 作业批改\n• 成绩录入\n• 作业反馈\n• 作业统计";
                break;
            case STUDENT:
                content += "• 作业查看\n• 作业提交\n• 成绩查询\n• 教师反馈查看";
                break;
        }

        displayContent("作业管理", content);
    }

//
    private void showGradeContent() {
        titleLabel.setText("成绩管理");

        // 清空内容面板
        contentPanel.removeAll();

        // 创建成绩管理面板
        GradeManagementPanel gradePanel = new GradeManagementPanel(currentUserRole, currentUsername, currentNickname);
        contentPanel.add(gradePanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void showMessageContent() {
        String content = "这里是消息通知模块\n\n";
        content += "当前权限: " + currentUserRole.getRoleName() + "\n\n";
        content += "可用功能:\n";

        switch (currentUserRole) {
            case ADMIN:
                content += "• 系统公告发布\n• 全校通知管理\n• 消息模板设置\n• 消息统计分析";
                break;
            case TEACHER:
                content += "• 班级通知发布\n• 学生消息发送\n• 课程通知管理\n• 消息历史查看";
                break;
            case STUDENT:
                content += "• 系统通知查看\n• 个人消息\n• 课程通知\n• 消息设置";
                break;
        }

        displayContent("消息通知", content);
    }

    private void showSystemContent() {
        if (currentUserRole != UserRole.ADMIN) {
            JOptionPane.showMessageDialog(this, "权限不足，无法访问系统管理功能", "权限错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String content = "这里是系统管理模块\n\n";
        content += "当前权限: " + currentUserRole.getRoleName() + " (最高权限)\n\n";
        content += "可用功能:\n";
        content += "• 用户管理\n• 角色权限设置\n• 系统配置\n• 日志管理\n• 数据备份\n• 系统监控\n• 统计报表\n• 安全设置";

        displayContent("系统管理", content);
    }

    private void displayContent(String title, String content) {
        titleLabel.setText(title);

        // 清空内容面板
        contentPanel.removeAll();

        // 创建内容显示区域
        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setBackground(Color.WHITE);
        contentArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(contentArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public UserRole getCurrentUserRole() {
        return currentUserRole;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public String getCurrentNickname() {
        return currentNickname;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 测试不同权限的用户
                new MainInterface("0admin", "管理员").setVisible(true); // 管理员
                // new MainInterface("1teacher", "教师").setVisible(true); // 教师
                // new MainInterface("2student", "学生").setVisible(true); // 学生
            }
        });
    }
}