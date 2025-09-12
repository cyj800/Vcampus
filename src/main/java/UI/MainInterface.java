package UI;

import model.UserRole;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import UI.Library_interface;
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
    private JButton storeButton;
    private JButton systemButton;  // 系统管理按钮（仅管理员可见）
    private JButton logoutButton;
    private JButton userManagementButton;
    private String currentUsername;
    private String currentNickname;
    private UserRole currentUserRole; // 当前用户权限
    private Library_interface libraryInterface;
    // 权限控制映射
    private Map<JButton, UserRole[]> buttonPermissions;

    public MainInterface(String username, String nickname) {
        this.currentUsername = username;
        this.currentNickname = nickname;
        this.currentUserRole = UserRole.fromUsername(username); // 根据用户名确定权限
        this.libraryInterface = new Library_interface();
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

        storeButton = new JButton("校园商店");
        storeButton.setPreferredSize(buttonSize);
        storeButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
//        storeButton.setBackground(new Color(100, 200, 100));
//        storeButton.setForeground(Color.BLACK);

        messageButton = new JButton("消息通知");
        messageButton.setPreferredSize(buttonSize);
        messageButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));



        logoutButton = new JButton("退出登录");
        logoutButton.setPreferredSize(buttonSize);
        logoutButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        logoutButton.setBackground(new Color(255, 100, 100));
        logoutButton.setForeground(Color.BLACK);

        userManagementButton = new JButton("学籍管理");
        userManagementButton.setPreferredSize(buttonSize);
        userManagementButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
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
        leftPanel.add(storeButton, gbc);

        gbc.gridy = 7;
        leftPanel.add(messageButton, gbc);

        gbc.gridy = 8; // 调整位置
        leftPanel.add(userManagementButton, gbc);

        gbc.gridy = 9;
        gbc.weighty = 1.0; // 占据剩余空间
        leftPanel.add(Box.createVerticalGlue(), gbc);

        gbc.gridy = 10;
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
        buttonPermissions.put(storeButton, new UserRole[]{UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT});
        buttonPermissions.put(messageButton, new UserRole[]{UserRole.ADMIN, UserRole.TEACHER, UserRole.STUDENT});

        buttonPermissions.put(userManagementButton, new UserRole[]{UserRole.ADMIN});
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

        storeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showStoreContent();
            }
        });

        messageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showMessageContent();
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
        userManagementButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentUserRole == UserRole.ADMIN) {
                    showUserManagementContent();
                } else {
                    JOptionPane.showMessageDialog(MainInterface.this,
                            "权限不足，无法访问学籍管理功能",
                            "权限错误",
                            JOptionPane.ERROR_MESSAGE);
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
        this.contentPanel.removeAll();
        String currentUsername = this.getCurrentUsername();
        this.displayContent("车大图书馆", "");
        Library_interface libraryInterface = new Library_interface();
        JPanel libraryPanel = libraryInterface.createLibraryPanel(currentUsername);
        this.contentPanel.add(libraryPanel, "Center");
        this.contentPanel.revalidate();
        this.contentPanel.repaint();
    }

    private void showCourseContent() {
        // 清空内容面板
        contentPanel.removeAll();
        titleLabel.setText("课程管理");

        JPanel coursePanel = null;

        switch (currentUserRole) {
            case ADMIN:
                // 管理员看到课程管理界面
                coursePanel = new AdminCourseManagementPanel();
                break;
            case TEACHER:
                // 教师看到自己的课程管理界面
                coursePanel = new TeacherCoursePanel(currentUsername,currentNickname);
                break;
            case STUDENT:
                // 学生看到选课界面
                coursePanel = new StudentCoursePanel(currentUsername, currentNickname);
                break;
            default:
                JOptionPane.showMessageDialog(this, "用户角色错误", "错误", JOptionPane.ERROR_MESSAGE);
                return;
        }

        if (coursePanel != null) {
            contentPanel.add(coursePanel, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    private void showHomeworkContent() {
        // 清空内容面板
        contentPanel.removeAll();
        titleLabel.setText("作业管理");

        JPanel homeworkPanel = null;

        switch (currentUserRole) {
            case ADMIN:
                // 管理员看到作业统计和申诉管理界面
                JPanel adminPanel = new JPanel(new BorderLayout());

                // 创建选项卡面板
                JTabbedPane tabbedPane = new JTabbedPane();

                // 作业统计面板
                StatisticsAnalysisPanel statsPanel = new StatisticsAnalysisPanel();
                tabbedPane.addTab("统计分析", statsPanel);

                // 申诉管理面板
                AppealManagementPanel appealPanel = new AppealManagementPanel("admin", currentUsername);
                tabbedPane.addTab("申诉管理", appealPanel);

                adminPanel.add(tabbedPane, BorderLayout.CENTER);
                homeworkPanel = adminPanel;
                break;

            case TEACHER:
                // 教师看到完整的作业管理界面
                JPanel teacherPanel = new JPanel(new BorderLayout());

                // 创建选项卡面板
                JTabbedPane teacherTabbedPane = new JTabbedPane();

                // 作业布置面板
                AssignmentCreatePanel createPanel = new AssignmentCreatePanel(currentUsername, null);
                teacherTabbedPane.addTab("布置作业", createPanel);

                // 作业批改面板
                AssignmentGradingPanel gradingPanel = new AssignmentGradingPanel(currentUsername);
                teacherTabbedPane.addTab("批改作业", gradingPanel);

                // 申诉管理面板
                AppealManagementPanel teacherAppealPanel = new AppealManagementPanel("teacher", currentUsername);
                teacherTabbedPane.addTab("申诉处理", teacherAppealPanel);

                teacherPanel.add(teacherTabbedPane, BorderLayout.CENTER);
                homeworkPanel = teacherPanel;
                break;

            case STUDENT:
                // 学生看到作业查看和提交界面
                JPanel studentPanel = new JPanel(new BorderLayout());

                // 创建选项卡面板
                JTabbedPane studentTabbedPane = new JTabbedPane();

                // 作业查看面板
                AssignmentViewPanel viewPanel = new AssignmentViewPanel(currentUsername, getCurrentCourseId());
                studentTabbedPane.addTab("作业列表", viewPanel);

                // 我的提交面板 - 使用完整的学生提交历史面板
                StudentSubmissionPanel submissionPanel = new StudentSubmissionPanel(currentUsername);
                studentTabbedPane.addTab("我的提交", submissionPanel);

                // 我的申诉面板
                StudentAppealPanel studentAppealPanel = new StudentAppealPanel(currentUsername);
                studentTabbedPane.addTab("我的申诉", studentAppealPanel);

                studentPanel.add(studentTabbedPane, BorderLayout.CENTER);
                homeworkPanel = studentPanel;
                break;

            default:
                JOptionPane.showMessageDialog(this, "用户角色错误", "错误", JOptionPane.ERROR_MESSAGE);
                return;
        }

        if (homeworkPanel != null) {
            contentPanel.add(homeworkPanel, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();
        }
    }

    private String getCurrentCourseId() {
        // 这里应该根据实际业务逻辑获取用户的课程ID
        // 对于教师，可能需要让用户选择课程
        // 对于学生，可以获取他们选修的课程

        try {
            // 临时实现：可以返回一个默认课程ID
            // 实际项目中应该根据用户角色动态获取
            switch (currentUserRole) {
                case ADMIN:
                    return "ALL"; // 管理员可以查看所有课程
                case TEACHER:
                    return null; // 教师在布置作业时会从下拉框选择课程
                case STUDENT:
                    // 学生获取他们选修的课程之一
                    database.CourseDAO courseDAO = new database.CourseDAO();
                    java.util.List<model.Course> studentCourses = courseDAO.getStudentCourses(currentUsername);
                    if (studentCourses != null && !studentCourses.isEmpty()) {
                        return studentCourses.get(0).getCourseId();
                    }
                    return "DEFAULT_COURSE";
                default:
                    return "DEFAULT_COURSE";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "DEFAULT_COURSE";
        }
    }

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

    public void showStoreContent() {
        try {
            // 创建User对象（从数据库获取真实ID）
            model.User currentUserObj = new model.User();
            currentUserObj.setUsername(currentUsername);
            currentUserObj.setNickname(currentNickname);
            database.UserDAO userDAO = new database.UserDAO();
            model.User dbUser = userDAO.getUserByUsername(currentUsername);
            if (dbUser != null) {
                currentUserObj.setId(dbUser.getId());
            }

            // 创建商店内容面板
            StoreContentPanel storeContentPanel = new StoreContentPanel(currentUserObj, this);


            // 切换到商店内容
            contentPanel.removeAll();
            contentPanel.add(storeContentPanel, BorderLayout.CENTER);
            contentPanel.revalidate();
            contentPanel.repaint();

            // 更新标题
            UserRole role = UserRole.fromUsername(currentUserObj.getUsername());
            if (role == UserRole.ADMIN) {
                titleLabel.setText("商品管理系统");
            } else {
                titleLabel.setText("校园商店");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "商店模块加载失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void showMessageContent() {
        titleLabel.setText("消息通知");

        // 清空内容面板
        contentPanel.removeAll();

        // 创建消息面板
        MessagePanel messagePanel = new MessagePanel(currentUserRole, currentUsername, currentNickname);
        contentPanel.add(messagePanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
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

    private void showUserManagementContent() {
        titleLabel.setText("学籍管理");

        contentPanel.removeAll();

        UserManagementPanel userPanel = new UserManagementPanel();
        contentPanel.add(userPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
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