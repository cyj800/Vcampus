package UI;

import model.Grade;
import model.Course;
import model.UserRole;
import service.GradeService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class GradeManagementPanel extends JPanel {
    private UserRole currentUserRole;
    private String currentUserId;
    private String currentNickname;

    private JTable gradeTable;
    private DefaultTableModel tableModel;
    private JButton uploadButton;
    private JButton statisticsButton;
    private JLabel userInfoLabel;
    private JLabel averageLabel;
    private JLabel courseFilterLabel;
    private JComboBox<String> courseFilterComboBox;

    private GradeService gradeService;
    private List<Course> availableCourses;
    private Map<String, String> courseNameToIdMap;

    // 防止频繁刷新的标志
    private AtomicBoolean isLoading = new AtomicBoolean(false);
    private AtomicBoolean isFiltering = new AtomicBoolean(false);
    private javax.swing.Timer refreshTimer;
    // 在类中添加成员变量
    private Map<String, Course> courseNameMap = new HashMap<>();
    public GradeManagementPanel(UserRole role, String userId, String nickname) {
        this.currentUserRole = role;
        this.currentUserId = userId;
        this.currentNickname = nickname;
        this.gradeService = new GradeService();
        this.availableCourses = new ArrayList<>();
        this.courseNameToIdMap = new HashMap<>();

        initializeComponents();
        setupLayout();
        addEventListeners();
        loadCourses();
        loadData();
    }

    private void initializeComponents() {
        // 表格模型
        String[] columnNames = {"课程名称", "学生姓名", "成绩", "等级", "教师", "考试日期", "备注"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        gradeTable = new JTable(tableModel);
        gradeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gradeTable.getTableHeader().setReorderingAllowed(false);
        gradeTable.setRowHeight(25);

        // 设置表格列宽
        gradeTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        gradeTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        gradeTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        gradeTable.getColumnModel().getColumn(3).setPreferredWidth(60);
        gradeTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        gradeTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        gradeTable.getColumnModel().getColumn(6).setPreferredWidth(150);

        // 按钮（移除刷新按钮）
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

        // 第一行（移除刷新按钮）
        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(userInfoLabel, gbc);

        gbc.gridx = 1;
        topPanel.add(Box.createHorizontalStrut(20), gbc);

        // 直接从上传按钮开始
        gbc.gridx = 2;
        if (currentUserRole == UserRole.TEACHER || currentUserRole == UserRole.ADMIN) {
            topPanel.add(uploadButton, gbc);

            gbc.gridx = 3;
            topPanel.add(statisticsButton, gbc);
        }

        gbc.gridx = 4;
        topPanel.add(Box.createHorizontalStrut(20), gbc);

        gbc.gridx = 5;
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

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addEventListeners() {
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

        // 课程筛选 - 添加防抖动处理
        courseFilterComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isFiltering.compareAndSet(false, true)) {
                    // 使用定时器防抖动
                    if (refreshTimer != null) {
                        refreshTimer.stop();
                    }
                    refreshTimer = new javax.swing.Timer(300, new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            filterTableData();
                            isFiltering.set(false);
                            if (refreshTimer != null) {
                                refreshTimer.stop();
                            }
                        }
                    });
                    refreshTimer.setRepeats(false);
                    refreshTimer.start();
                }
            }
        });
    }

    private void loadCourses() {
        if (isLoading.get()) return;

        SwingWorker<List<Course>, Void> worker = new SwingWorker<List<Course>, Void>() {
            @Override
            protected List<Course> doInBackground() throws Exception {
                if (currentUserRole == UserRole.TEACHER) {
                    return gradeService.getCoursesByTeacher(currentUserId);
                } else {
                    return gradeService.getAllCourses();
                }
            }

            @Override
            protected void done() {
                try {
                    availableCourses = get();
                    courseNameToIdMap.clear();
                    for (Course course : availableCourses) {
                        courseNameToIdMap.put(course.getCourseName(), course.getCourseId());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void loadData() {
        if (!isLoading.compareAndSet(false, true)) {
            return;
        }

        SwingWorker<List<Grade>, Void> worker = new SwingWorker<List<Grade>, Void>() {
            @Override
            protected List<Grade> doInBackground() throws Exception {
                Thread.sleep(200);
                return gradeService.getGradesByRole(currentUserRole, currentUserId);
            }

            @Override
            protected void done() {
                try {
                    List<Grade> grades = get();
                    if (grades != null) {
                        updateTable(grades);
                        updateAverageInfo();
                        updateCourseFilter(grades);
                        updateRecordCount(grades);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(GradeManagementPanel.this,
                                "加载成绩数据失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    });
                } finally {
                    isLoading.set(false);
                }
            }
        };
        worker.execute();
    }

    // 在上传成绩成功后调用此方法自动刷新
    private void refreshDataAfterUpload() {
        loadData();
    }

    private void updateTable(List<Grade> grades) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
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
        });
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
                    SwingUtilities.invokeLater(() -> {
                        averageLabel.setText(averageText);
                        averageLabel.setForeground(Color.BLUE);
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        averageLabel.setText("计算平均分失败");
                        averageLabel.setForeground(Color.RED);
                    });
                }
            }
        };
        worker.execute();
    }

    private void updateCourseFilter(List<Grade> grades) {
        SwingUtilities.invokeLater(() -> {
            String currentSelection = (String) courseFilterComboBox.getSelectedItem();
            courseFilterComboBox.removeAllItems();
            courseFilterComboBox.addItem("全部课程");

            Set<String> uniqueCourses = new HashSet<>();
            for (Grade grade : grades) {
                String courseName = grade.getCourseName();
                if (courseName != null) {
                    uniqueCourses.add(courseName);
                }
            }

            List<String> sortedCourses = new ArrayList<>(uniqueCourses);
            Collections.sort(sortedCourses);

            for (String courseName : sortedCourses) {
                courseFilterComboBox.addItem(courseName);
            }

            if (currentSelection != null) {
                courseFilterComboBox.setSelectedItem(currentSelection);
            }
        });
    }

    private void updateRecordCount(List<Grade> grades) {
        SwingUtilities.invokeLater(() -> {
            Component[] components = ((JPanel) getComponent(2)).getComponents();
            for (Component component : components) {
                if (component instanceof JLabel && ((JLabel) component).getText().startsWith("总计:")) {
                    ((JLabel) component).setText("总计: " + grades.size() + " 条记录");
                    break;
                }
            }
        });
    }

    private void filterTableData() {
        if (isLoading.get()) return;

        String selectedCourse = (String) courseFilterComboBox.getSelectedItem();
        if ("全部课程".equals(selectedCourse) || selectedCourse == null) {
            loadData();
        } else {
            if (!isLoading.compareAndSet(false, true)) {
                return;
            }

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
                        updateRecordCount(filteredGrades);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        isLoading.set(false);
                    }
                }
            };
            worker.execute();
        }
    }

    private Color getRoleColor(UserRole role) {
        switch (role) {
            case ADMIN:
                return new Color(255, 100, 100);
            case TEACHER:
                return new Color(100, 150, 255);
            case STUDENT:
                return new Color(100, 200, 100);
            default:
                return Color.BLACK;
        }
    }

    private void showUploadGradeDialog() {
        JDialog uploadDialog = new JDialog();
        uploadDialog.setTitle("上传成绩");
        uploadDialog.setModal(true);
        uploadDialog.setSize(500, 400);
        uploadDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // 课程选择
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("课程:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JComboBox<String> courseComboBox = new JComboBox<>();
        courseComboBox.setPreferredSize(new Dimension(200, 25));
        // 加载课程数据时
        SwingWorker<List<Course>, Void> courseWorker = new SwingWorker<List<Course>, Void>() {
            @Override
            protected List<Course> doInBackground() throws Exception {
                if (currentUserRole == UserRole.TEACHER) {
                    return gradeService.getCoursesByTeacher(currentUserId);
                } else {
                    return gradeService.getAllCourses();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Course> courses = get();
                    courseNameMap.clear();
                    courseComboBox.removeAllItems();
                    for (Course course : courses) {
                        // 存储映射关系
                        courseNameMap.put(course.getCourseName(), course);
                        // 只显示课程名称
                        courseComboBox.addItem(course.getCourseName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        courseWorker.execute();
        panel.add(courseComboBox, gbc);

        // 学生选择
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("学生:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JComboBox<String> studentComboBox = new JComboBox<>();
        studentComboBox.setPreferredSize(new Dimension(200, 25));
        panel.add(studentComboBox, gbc);

        // 成绩输入
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("成绩 (0-100):"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JTextField scoreField = new JTextField(15);
        panel.add(scoreField, gbc);

        // 等级
        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("等级 (可选):"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JTextField gradeField = new JTextField(15);
        panel.add(gradeField, gbc);

        // 考试日期
        gbc.gridx = 0; gbc.gridy = 4; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("考试日期:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JTextField dateField = new JTextField(15);
        dateField.setText(LocalDate.now().toString());
        panel.add(dateField, gbc);

        // 备注
        gbc.gridx = 0; gbc.gridy = 5; gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("备注:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.WEST;
        JTextField remarkField = new JTextField(15);
        panel.add(remarkField, gbc);

//        // 课程选择事件 - 动态加载学生列表
//        courseComboBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                Course selectedCourse = (Course) courseComboBox.getSelectedItem();
        // 课程选择事件
        courseComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedCourseName = (String) courseComboBox.getSelectedItem();
                if (selectedCourseName != null && !selectedCourseName.isEmpty()) {
                    // 直接从Map中获取Course对象
                    Course selectedCourse = courseNameMap.get(selectedCourseName);
                    if (selectedCourse != null) {
                        SwingWorker<List<String>, Void> studentWorker = new SwingWorker<List<String>, Void>() {
                            @Override
                            protected List<String> doInBackground() throws Exception {
                                return gradeService.getStudentsByCourse(selectedCourse.getCourseId());
                            }

                            @Override
                            protected void done() {
                                try {
                                    List<String> students = get();
                                    studentComboBox.removeAllItems();
                                    for (String studentId : students) {
                                        // 这里可以获取学生姓名，简化处理直接显示学号
                                        studentComboBox.addItem(studentId);
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        };
                        studentWorker.execute();
                    }
                }
            }
        });

        // 按钮面板
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveButton = new JButton("保存");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);

        uploadDialog.add(panel);

        // 事件处理
//        saveButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                Course selectedCourse = (Course) courseComboBox.getSelectedItem();
//                String selectedStudent = (String) studentComboBox.getSelectedItem();
//                String scoreText = scoreField.getText().trim();
//                String gradeLetter = gradeField.getText().trim();
//                String examDate = dateField.getText().trim();
//                String remark = remarkField.getText().trim();
//
//                // 验证输入
//                if (selectedCourse == null) {
//                    JOptionPane.showMessageDialog(uploadDialog, "请选择课程", "提示", JOptionPane.WARNING_MESSAGE);
//                    return;
//                }
//
//                if (selectedStudent == null || selectedStudent.isEmpty()) {
//                    JOptionPane.showMessageDialog(uploadDialog, "请选择学生", "提示", JOptionPane.WARNING_MESSAGE);
//                    return;
//                }
//
//                if (scoreText.isEmpty()) {
//                    JOptionPane.showMessageDialog(uploadDialog, "请输入成绩", "提示", JOptionPane.WARNING_MESSAGE);
//                    return;
//                }
//
//                try {
//                    Double score = Double.parseDouble(scoreText);
//                    if (score < 0 || score > 100) {
//                        JOptionPane.showMessageDialog(uploadDialog, "成绩应在0-100之间", "提示", JOptionPane.WARNING_MESSAGE);
//                        return;
//                    }
//
//                    // 创建成绩对象
//                    Grade grade = new Grade();
//                    grade.setStudentId(selectedStudent);
//                    grade.setCourseId(selectedCourse.getCourseId());
//                    grade.setScore(score);
//                    grade.setGradeLetter(gradeLetter.isEmpty() ? calculateGradeLetter(score) : gradeLetter);
//
//                    try {
//                        grade.setExamDate(LocalDate.parse(examDate));
//                    } catch (Exception ex) {
//                        grade.setExamDate(LocalDate.now());
//                    }
//
//                    grade.setRemark(remark);
//
//                    // 保存成绩
//                    SwingWorker<Boolean, Void> saveWorker = new SwingWorker<Boolean, Void>() {
//                        @Override
//                        protected Boolean doInBackground() throws Exception {
//                            return gradeService.saveGrade(grade, currentUserRole, currentUserId);
//                        }
//
//                        @Override
//                        protected void done() {
//                            try {
//                                Boolean success = get();
//                                if (success) {
//                                    JOptionPane.showMessageDialog(uploadDialog, "成绩上传成功", "成功", JOptionPane.INFORMATION_MESSAGE);
//                                    uploadDialog.dispose();
//                                    // 上传成功后自动刷新数据
//                                    refreshDataAfterUpload();
//                                } else {
//                                    JOptionPane.showMessageDialog(uploadDialog, "成绩上传失败", "错误", JOptionPane.ERROR_MESSAGE);
//                                }
//                            } catch (Exception ex) {
//                                ex.printStackTrace();
//                                JOptionPane.showMessageDialog(uploadDialog, "成绩上传失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
//                            }
//                        }
//                    };
//                    saveWorker.execute();
//
//                } catch (NumberFormatException ex) {
//                    JOptionPane.showMessageDialog(uploadDialog, "请输入有效的数字", "错误", JOptionPane.ERROR_MESSAGE);
//                }
//            }
//        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取选中的课程名称（现在是String类型）
                String selectedCourseName = (String) courseComboBox.getSelectedItem();
                String selectedStudent = (String) studentComboBox.getSelectedItem();
                String scoreText = scoreField.getText().trim();
                String gradeLetter = gradeField.getText().trim();
                String examDate = dateField.getText().trim();
                String remark = remarkField.getText().trim();

                // 验证输入
                if (selectedCourseName == null || selectedCourseName.isEmpty()) {
                    JOptionPane.showMessageDialog(uploadDialog, "请选择课程", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (selectedStudent == null || selectedStudent.isEmpty()) {
                    JOptionPane.showMessageDialog(uploadDialog, "请选择学生", "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }

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

                    // 根据课程名称找到对应的Course对象
                    Course selectedCourse = null;
                    // 假设你有一个courseNameMap来存储映射关系
                    // 如果没有，你需要通过其他方式获取Course对象
                    for (Course course : availableCourses) {
                        if (selectedCourseName.equals(course.getCourseName())) {
                            selectedCourse = course;
                            break;
                        }
                    }

                    if (selectedCourse == null) {
                        JOptionPane.showMessageDialog(uploadDialog, "未找到选中的课程", "错误", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // 创建成绩对象
                    Grade grade = new Grade();
                    grade.setStudentId(selectedStudent);
                    grade.setCourseId(selectedCourse.getCourseId());
                    grade.setScore(score);
                    grade.setGradeLetter(gradeLetter.isEmpty() ? calculateGradeLetter(score) : gradeLetter);

                    try {
                        grade.setExamDate(LocalDate.parse(examDate));
                    } catch (Exception ex) {
                        grade.setExamDate(LocalDate.now());
                    }

                    grade.setRemark(remark);

                    // 保存成绩
                    SwingWorker<Boolean, Void> saveWorker = new SwingWorker<Boolean, Void>() {
                        @Override
                        protected Boolean doInBackground() throws Exception {
                            return gradeService.saveGrade(grade, currentUserRole, currentUserId);
                        }

                        @Override
                        protected void done() {
                            try {
                                Boolean success = get();
                                if (success) {
                                    JOptionPane.showMessageDialog(uploadDialog, "成绩上传成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                                    uploadDialog.dispose();
                                    // 上传成功后自动刷新数据
                                    refreshDataAfterUpload();
                                } else {
                                    JOptionPane.showMessageDialog(uploadDialog, "成绩上传失败", "错误", JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(uploadDialog, "成绩上传失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    };
                    saveWorker.execute();

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

//    private void showStatisticsDialog() {
//        JDialog statsDialog = new JDialog();
//        statsDialog.setTitle("成绩统计分析");
//        statsDialog.setModal(true);
//        statsDialog.setSize(600, 500);
//        statsDialog.setLocationRelativeTo(this);
//
//        JPanel mainPanel = new JPanel(new BorderLayout());
//
//        // 课程选择面板
//        JPanel topPanel = new JPanel(new FlowLayout());
//        topPanel.add(new JLabel("选择课程:"));
//
//        JComboBox<Course> courseComboBox = new JComboBox<>();
//        courseComboBox.setPreferredSize(new Dimension(200, 25));
//
//        // 加载课程数据
//        SwingWorker<List<Course>, Void> courseWorker = new SwingWorker<List<Course>, Void>() {
//            @Override
//            protected List<Course> doInBackground() throws Exception {
//                if (currentUserRole == UserRole.TEACHER) {
//                    return gradeService.getCoursesByTeacher(currentUserId);
//                } else {
//                    return gradeService.getAllCourses();
//                }
//            }
//
//            @Override
//            protected void done() {
//                try {
//                    List<Course> courses = get();
//                    courseComboBox.removeAllItems();
//                    for (Course course : courses) {
//                        courseComboBox.addItem(course);
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//        courseWorker.execute();
//
//        topPanel.add(courseComboBox);
//        JButton analyzeButton = new JButton("分析");
//        topPanel.add(analyzeButton);
//
//        // 结果显示面板
//        JTextArea resultArea = new JTextArea();
//        resultArea.setEditable(false);
//        resultArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
//        resultArea.setLineWrap(true);
//        resultArea.setWrapStyleWord(true);
//        JScrollPane scrollPane = new JScrollPane(resultArea);
//
//        mainPanel.add(topPanel, BorderLayout.NORTH);
//        mainPanel.add(scrollPane, BorderLayout.CENTER);
//
//        // 关闭按钮
//        JPanel bottomPanel = new JPanel(new FlowLayout());
//        JButton closeButton = new JButton("关闭");
//        bottomPanel.add(closeButton);
//        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
//
//        // 分析按钮事件
//        analyzeButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                Course selectedCourse = (Course) courseComboBox.getSelectedItem();
//                if (selectedCourse == null) {
//                    JOptionPane.showMessageDialog(statsDialog, "请选择课程", "提示", JOptionPane.WARNING_MESSAGE);
//                    return;
//                }
//
//                // 执行统计分析
//                SwingWorker<String, Void> analyzeWorker = new SwingWorker<String, Void>() {
//                    @Override
//                    protected String doInBackground() throws Exception {
//                        return generateStatisticsReport(selectedCourse);
//                    }
//
//                    @Override
//                    protected void done() {
//                        try {
//                            String report = get();
//                            resultArea.setText(report);
//                        } catch (Exception ex) {
//                            ex.printStackTrace();
//                            resultArea.setText("生成统计报告失败: " + ex.getMessage());
//                        }
//                    }
//                };
//                analyzeWorker.execute();
//            }
//        });
//
//        closeButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                statsDialog.dispose();
//            }
//        });
//
//        statsDialog.add(mainPanel);
//        statsDialog.setVisible(true);
//    }
private void showStatisticsDialog() {
    JDialog statsDialog = new JDialog();
    statsDialog.setTitle("成绩统计分析");
    statsDialog.setModal(true);
    statsDialog.setSize(600, 500);
    statsDialog.setLocationRelativeTo(this);

    JPanel mainPanel = new JPanel(new BorderLayout());

    // 课程选择面板
    JPanel topPanel = new JPanel(new FlowLayout());
    topPanel.add(new JLabel("选择课程:"));

    // 使用String类型的ComboBox，只显示课程名称
    JComboBox<String> courseComboBox = new JComboBox<>();
    courseComboBox.setPreferredSize(new Dimension(200, 25));

    // 添加Map来存储课程名称和Course对象的映射关系
    Map<String, Course> courseNameMap = new HashMap<>();

    // 加载课程数据
    SwingWorker<List<Course>, Void> courseWorker = new SwingWorker<List<Course>, Void>() {
        @Override
        protected List<Course> doInBackground() throws Exception {
            if (currentUserRole == UserRole.TEACHER) {
                return gradeService.getCoursesByTeacher(currentUserId);
            } else {
                return gradeService.getAllCourses();
            }
        }

        @Override
        protected void done() {
            try {
                List<Course> courses = get();
                courseNameMap.clear();
                courseComboBox.removeAllItems();
                for (Course course : courses) {
                    // 存储映射关系
                    courseNameMap.put(course.getCourseName(), course);
                    // 只显示课程名称
                    courseComboBox.addItem(course.getCourseName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    courseWorker.execute();

    topPanel.add(courseComboBox);
    JButton analyzeButton = new JButton("分析");
    topPanel.add(analyzeButton);

    // 结果显示面板
    JTextArea resultArea = new JTextArea();
    resultArea.setEditable(false);
    resultArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
    resultArea.setLineWrap(true);
    resultArea.setWrapStyleWord(true);
    JScrollPane scrollPane = new JScrollPane(resultArea);

    mainPanel.add(topPanel, BorderLayout.NORTH);
    mainPanel.add(scrollPane, BorderLayout.CENTER);

    // 关闭按钮
    JPanel bottomPanel = new JPanel(new FlowLayout());
    JButton closeButton = new JButton("关闭");
    bottomPanel.add(closeButton);
    mainPanel.add(bottomPanel, BorderLayout.SOUTH);

    // 分析按钮事件
    analyzeButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 获取选中的课程名称
            String selectedCourseName = (String) courseComboBox.getSelectedItem();
            if (selectedCourseName == null || selectedCourseName.isEmpty()) {
                JOptionPane.showMessageDialog(statsDialog, "请选择课程", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 根据课程名称找到对应的Course对象
            Course selectedCourse = courseNameMap.get(selectedCourseName);
            if (selectedCourse == null) {
                JOptionPane.showMessageDialog(statsDialog, "未找到选中的课程", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 执行统计分析
            SwingWorker<String, Void> analyzeWorker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return generateStatisticsReport(selectedCourse);
                }

                @Override
                protected void done() {
                    try {
                        String report = get();
                        resultArea.setText(report);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        resultArea.setText("生成统计报告失败: " + ex.getMessage());
                    }
                }
            };
            analyzeWorker.execute();
        }
    });

    closeButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            statsDialog.dispose();
        }
    });

    statsDialog.add(mainPanel);
    statsDialog.setVisible(true);
}

    private String generateStatisticsReport(Course course) {
        StringBuilder report = new StringBuilder();
        report.append("=== 成绩统计分析报告 ===\n\n");
        report.append("课程名称: ").append(course.getCourseName()).append("\n");
        report.append("课程编号: ").append(course.getCourseId()).append("\n");
        report.append("授课教师: ").append(course.getTeacherName()).append("\n");
        report.append("学期: ").append(course.getSemester() != null ? course.getSemester() : "未指定").append("\n\n");

        try {
            // 获取课程成绩数据
            List<Grade> grades = gradeService.getGradesByCourse(course.getCourseId());

            if (grades.isEmpty()) {
                report.append("该课程暂无成绩数据。\n");
                return report.toString();
            }

            // 计算统计信息
            int totalStudents = grades.size();
            double sum = 0;
            double maxScore = Double.MIN_VALUE;
            double minScore = Double.MAX_VALUE;

            for (Grade grade : grades) {
                if (grade.getScore() != null) {
                    double score = grade.getScore();
                    sum += score;
                    maxScore = Math.max(maxScore, score);
                    minScore = Math.min(minScore, score);
                }
            }

            double averageScore = sum / totalStudents;

            report.append("参考人数: ").append(totalStudents).append("人\n");
            report.append("平均分: ").append(String.format("%.2f", averageScore)).append("分\n");
            report.append("最高分: ").append(String.format("%.1f", maxScore)).append("分\n");
            report.append("最低分: ").append(String.format("%.1f", minScore)).append("分\n");

            // 计算及格率
            long passCount = grades.stream()
                    .filter(g -> g.getScore() != null && g.getScore() >= 60)
                    .count();
            double passRate = (double) passCount / totalStudents * 100;
            report.append("及格率: ").append(String.format("%.1f", passRate)).append("%\n\n");

            // 成绩分布
            report.append("成绩分布:\n");
            int[] distribution = new int[5]; // [90-100, 80-89, 70-79, 60-69, 0-59]

            for (Grade grade : grades) {
                if (grade.getScore() != null) {
                    double score = grade.getScore();
                    if (score >= 90) distribution[0]++;
                    else if (score >= 80) distribution[1]++;
                    else if (score >= 70) distribution[2]++;
                    else if (score >= 60) distribution[3]++;
                    else distribution[4]++;
                }
            }

            String[] ranges = {"90-100分", "80-89分", "70-79分", "60-69分", "60分以下"};
            for (int i = 0; i < ranges.length; i++) {
                double percentage = (double) distribution[i] / totalStudents * 100;
                report.append(ranges[i]).append(": ").append(distribution[i])
                        .append("人 (").append(String.format("%.1f", percentage)).append("%)\n");
            }

            // 前三名
            report.append("\n成绩排名前3名:\n");
            grades.sort((g1, g2) -> {
                if (g1.getScore() == null) return 1;
                if (g2.getScore() == null) return -1;
                return Double.compare(g2.getScore(), g1.getScore());
            });

            int rank = 1;
            for (Grade grade : grades) {
                if (rank <= 3 && grade.getScore() != null) {
                    report.append("第").append(rank).append("名: ")
                            .append(grade.getStudentName() != null ? grade.getStudentName() : grade.getStudentId())
                            .append(" - ").append(String.format("%.1f", grade.getScore())).append("分\n");
                    rank++;
                }
            }

        } catch (Exception e) {
            report.append("统计分析过程中发生错误: ").append(e.getMessage()).append("\n");
        }

        return report.toString();
    }

    private String calculateGradeLetter(Double score) {
        if (score >= 90) return "A";
        if (score >= 85) return "B+";
        if (score >= 80) return "B";
        if (score >= 75) return "C+";
        if (score >= 70) return "C";
        if (score >= 60) return "D";
        return "F";
    }

    // 确保组件正确清理
    @Override
    public void removeNotify() {
        super.removeNotify();
        if (refreshTimer != null) {
            refreshTimer.stop();
            refreshTimer = null;
        }
    }
}