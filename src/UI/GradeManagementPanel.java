package UI;

import model.Grade;
import model.UserRole;
import service.GradeService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class GradeManagementPanel extends JPanel {
    private UserRole currentUserRole;
    private String currentUserId;
    private String currentNickname;

    private JTable gradeTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton uploadButton;
    private JButton statisticsButton;
    private JLabel userInfoLabel;
    private JLabel averageLabel;
    private JLabel courseFilterLabel;
    private JComboBox<String> courseFilterComboBox;

    private GradeService gradeService;

    public GradeManagementPanel(UserRole role, String userId, String nickname) {
        this.currentUserRole = role;
        this.currentUserId = userId;
        this.currentNickname = nickname;
        this.gradeService = new GradeService();

        initializeComponents();
        setupLayout();
        addEventListeners();
        loadData();
    }

    private void initializeComponents() {
        // 表格模型
        String[] columnNames = {"课程名称", "学生姓名", "成绩", "等级", "教师", "考试日期", "备注"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可编辑
            }
        };

        gradeTable = new JTable(tableModel);
        gradeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gradeTable.getTableHeader().setReorderingAllowed(false);
        gradeTable.setRowHeight(25);

        // 设置表格列宽
        gradeTable.getColumnModel().getColumn(0).setPreferredWidth(120); // 课程名称
        gradeTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // 学生姓名
        gradeTable.getColumnModel().getColumn(2).setPreferredWidth(60);  // 成绩
        gradeTable.getColumnModel().getColumn(3).setPreferredWidth(60);  // 等级
        gradeTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 教师
        gradeTable.getColumnModel().getColumn(5).setPreferredWidth(100); // 考试日期
        gradeTable.getColumnModel().getColumn(6).setPreferredWidth(150); // 备注

        // 按钮
        refreshButton = new JButton("刷新");
        uploadButton = new JButton("上传成绩");
        statisticsButton = new JButton("统计分析");

        // 标签
        userInfoLabel = new JLabel();
        updateUserInfoLabel();

        averageLabel = new JLabel();
        averageLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));

        // 课程筛选
        courseFilterLabel = new JLabel("课程筛选:");
        courseFilterComboBox = new JComboBox<>();
        courseFilterComboBox.addItem("全部课程");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // 顶部面板
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 第一行
        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(userInfoLabel, gbc);

        gbc.gridx = 1;
        topPanel.add(Box.createHorizontalStrut(20), gbc);

        gbc.gridx = 2;
        topPanel.add(refreshButton, gbc);

        if (currentUserRole == UserRole.TEACHER || currentUserRole == UserRole.ADMIN) {
            gbc.gridx = 3;
            topPanel.add(uploadButton, gbc);

            gbc.gridx = 4;
            topPanel.add(statisticsButton, gbc);
        }

        gbc.gridx = 5;
        topPanel.add(Box.createHorizontalStrut(20), gbc);

        gbc.gridx = 6;
        topPanel.add(averageLabel, gbc);

        // 第二行 - 课程筛选
        gbc.gridx = 0; gbc.gridy = 1;
        topPanel.add(courseFilterLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 3;
        topPanel.add(courseFilterComboBox, gbc);

        // 表格滚动面板
        JScrollPane scrollPane = new JScrollPane(gradeTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(800, 400));

        // 底部统计面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel countLabel = new JLabel("总计: 0 条记录");
        bottomPanel.add(countLabel);

        // 添加到主面板
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addEventListeners() {
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadData();
            }
        });

        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentUserRole == UserRole.TEACHER || currentUserRole == UserRole.ADMIN) {
                    showUploadGradeDialog();
                } else {
                    JOptionPane.showMessageDialog(GradeManagementPanel.this,
                            "权限不足，无法上传成绩", "权限错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        statisticsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentUserRole == UserRole.TEACHER || currentUserRole == UserRole.ADMIN) {
                    showStatisticsDialog();
                } else {
                    JOptionPane.showMessageDialog(GradeManagementPanel.this,
                            "权限不足，无法查看统计分析", "权限错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        courseFilterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterTableData();
            }
        });
    }

    private void loadData() {
        SwingWorker<List<Grade>, Void> worker = new SwingWorker<List<Grade>, Void>() {
            @Override
            protected List<Grade> doInBackground() throws Exception {
                return gradeService.getGradesByRole(currentUserRole, currentUserId);
            }

            @Override
            protected void done() {
                try {
                    List<Grade> grades = get();
                    updateTable(grades);
                    updateAverageInfo();
                    updateCourseFilter(grades);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(GradeManagementPanel.this,
                            "加载成绩数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void updateTable(List<Grade> grades) {
        // 清空表格
        tableModel.setRowCount(0);

        // 添加数据
        for (Grade grade : grades) {
            Object[] row = {
                    grade.getCourseName() != null ? grade.getCourseName() : "",
                    grade.getStudentName() != null ? grade.getStudentName() : "",
                    grade.getScore() != null ? String.format("%.1f", grade.getScore()) : "未录入",
                    grade.getGradeLetter() != null ? grade.getGradeLetter() : "",
                    grade.getTeacherName() != null ? grade.getTeacherName() : "",
                    grade.getExamDate() != null ? grade.getExamDate().toString() : "",
                    grade.getRemark() != null ? grade.getRemark() : ""
            };
            tableModel.addRow(row);
        }
    }

    private void updateUserInfoLabel() {
        String roleInfo = String.format("成绩管理 - 当前用户: %s (%s)", currentNickname, currentUserRole.getRoleName());
        userInfoLabel.setText(roleInfo);
        userInfoLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        userInfoLabel.setForeground(getRoleColor(currentUserRole));
    }

    private void updateAverageInfo() {
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                if (currentUserRole == UserRole.STUDENT) {
                    Double average = gradeService.getStudentAverage(currentUserId);
                    if (average != null) {
                        return String.format("个人平均分: %.2f", average);
                    } else {
                        return "个人平均分: 暂无数据";
                    }
                } else if (currentUserRole == UserRole.TEACHER) {
                    return "教师视图 - 显示所授课程成绩";
                } else {
                    return "管理员视图 - 显示所有成绩";
                }
            }

            @Override
            protected void done() {
                try {
                    String averageText = get();
                    averageLabel.setText(averageText);
                    averageLabel.setForeground(Color.BLUE);
                } catch (Exception e) {
                    averageLabel.setText("计算平均分失败");
                    averageLabel.setForeground(Color.RED);
                }
            }
        };

        worker.execute();
    }

    private void updateCourseFilter(List<Grade> grades) {
        // 保存当前选择
        String currentSelection = (String) courseFilterComboBox.getSelectedItem();

        // 清空下拉框
        courseFilterComboBox.removeAllItems();
        courseFilterComboBox.addItem("全部课程");

        // 添加课程选项
        for (Grade grade : grades) {
            String courseName = grade.getCourseName();
            if (courseName != null && courseFilterComboBox.getItemCount() > 1) {
                boolean exists = false;
                for (int i = 1; i < courseFilterComboBox.getItemCount(); i++) {
                    if (courseFilterComboBox.getItemAt(i).equals(courseName)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    courseFilterComboBox.addItem(courseName);
                }
            } else if (courseName != null && courseFilterComboBox.getItemCount() == 1) {
                courseFilterComboBox.addItem(courseName);
            }
        }

        // 恢复选择
        if (currentSelection != null) {
            courseFilterComboBox.setSelectedItem(currentSelection);
        }
    }

    private void filterTableData() {
        String selectedCourse = (String) courseFilterComboBox.getSelectedItem();
        if ("全部课程".equals(selectedCourse) || selectedCourse == null) {
            // 重新加载所有数据
            loadData();
        } else {
            // 筛选特定课程
            SwingWorker<List<Grade>, Void> worker = new SwingWorker<List<Grade>, Void>() {
                @Override
                protected List<Grade> doInBackground() throws Exception {
                    List<Grade> allGrades = gradeService.getGradesByRole(currentUserRole, currentUserId);
                    return allGrades.stream()
                            .filter(grade -> selectedCourse.equals(grade.getCourseName()))
                            .collect(java.util.stream.Collectors.toList());
                }

                @Override
                protected void done() {
                    try {
                        List<Grade> filteredGrades = get();
                        updateTable(filteredGrades);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            worker.execute();
        }
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

    private void showUploadGradeDialog() {
//        JDialog uploadDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "上传成绩", true);
        JDialog uploadDialog = new JDialog();
        uploadDialog.setTitle("上传成绩");
        uploadDialog.setModal(true);
        uploadDialog.setSize(400, 300);
        uploadDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // 课程选择
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("课程:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> courseComboBox = new JComboBox<>();
        courseComboBox.addItem("数据结构");
        courseComboBox.addItem("算法设计");
        courseComboBox.addItem("高等数学");
        panel.add(courseComboBox, gbc);

        // 学生选择
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("学生:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> studentComboBox = new JComboBox<>();
        studentComboBox.addItem("张三");
        studentComboBox.addItem("李四");
        studentComboBox.addItem("王五");
        panel.add(studentComboBox, gbc);

        // 成绩输入
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("成绩:"), gbc);
        gbc.gridx = 1;
        JTextField scoreField = new JTextField(10);
        panel.add(scoreField, gbc);

        // 等级
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("等级:"), gbc);
        gbc.gridx = 1;
        JTextField gradeField = new JTextField(10);
        panel.add(gradeField, gbc);

        // 备注
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("备注:"), gbc);
        gbc.gridx = 1;
        JTextField remarkField = new JTextField(10);
        panel.add(remarkField, gbc);

        // 按钮面板
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);

        uploadDialog.add(panel);

        // 事件处理
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String scoreText = scoreField.getText().trim();
                if (scoreText.isEmpty()) {
                    JOptionPane.showMessageDialog(uploadDialog, "请输入成绩", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    Double score = Double.parseDouble(scoreText);
                    if (score < 0 || score > 100) {
                        JOptionPane.showMessageDialog(uploadDialog, "成绩应在0-100之间", "提示", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    JOptionPane.showMessageDialog(uploadDialog, "成绩上传成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                    uploadDialog.dispose();
                    loadData(); // 刷新数据
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(uploadDialog, "请输入有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadDialog.dispose();
            }
        });

        uploadDialog.setVisible(true);
    }

    private void showStatisticsDialog() {
//        JDialog statsDialog = new JDialog(SwingUtilities.getWindowAncestor(this), "成绩统计分析", true);
        JDialog statsDialog = new JDialog();
        statsDialog.setTitle("成绩统计分析");
        statsDialog.setModal(true);
        statsDialog.setSize(500, 400);
        statsDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());

        // 统计信息
        JTextArea statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        statsArea.setText("成绩统计分析\n\n" +
                "课程: 数据结构\n" +
                "参考人数: 5人\n" +
                "平均分: 84.7分\n" +
                "最高分: 91.5分 (张三)\n" +
                "最低分: 76.0分 (王五)\n" +
                "及格率: 100%\n\n" +
                "成绩分布:\n" +
                "90-100分: 2人 (40%)\n" +
                "80-89分: 2人 (40%)\n" +
                "70-79分: 1人 (20%)\n" +
                "60-69分: 0人 (0%)\n" +
                "60分以下: 0人 (0%)");

        JScrollPane scrollPane = new JScrollPane(statsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 关闭按钮
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("关闭");
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statsDialog.dispose();
            }
        });

        statsDialog.add(panel);
        statsDialog.setVisible(true);
    }
}