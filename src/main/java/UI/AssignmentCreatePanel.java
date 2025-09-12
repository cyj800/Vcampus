package UI;

import service.AssignmentService;
import model.Assignment;
import model.Course;
import database.CourseDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.util.List;

public class AssignmentCreatePanel extends JPanel {
    private String teacherId;
    private String courseId;
    private AssignmentService assignmentService;
    private CourseDAO courseDAO;

    // UI组件
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<CourseItem> courseCombo; // 修改为CourseItem下拉框
    private JTextField deadlineDateField;
    private JTextField deadlineTimeField;
    private JSpinner maxScoreSpinner;
    private JComboBox<String> submitTypeCombo;
    private JTextField allowedFileTypesField;
    private JTextField maxFileSizeField;
    private JButton createButton;
    private JButton resetButton;
    private JButton previewButton;
    private JLabel statusLabel;

    public AssignmentCreatePanel(String teacherId, String courseId) {
        this.teacherId = teacherId;
        this.courseId = courseId;
        this.assignmentService = new AssignmentService();
        this.courseDAO = new CourseDAO(); // 初始化CourseDAO

        initializeComponents();
        setupLayout();
        loadTeacherCourses(); // 加载教师课程
        initializeDefaultValues();
    }

    private void initializeComponents() {
        // 基本信息组件
        titleField = new JTextField(30);
        descriptionArea = new JTextArea(8, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        // 课程选择下拉框（替换原来的courseIdField）
        courseCombo = new JComboBox<>();
        courseCombo.setPreferredSize(new Dimension(200, 25));

        // 截止时间组件
        deadlineDateField = new JTextField(15);
        deadlineTimeField = new JTextField(10);
        deadlineDateField.setText(java.time.LocalDate.now().plusDays(7).toString()); // 默认一周后
        deadlineTimeField.setText("23:59");

        // 分数组件
        maxScoreSpinner = new JSpinner(new SpinnerNumberModel(100, 0, 1000, 5));

        // 提交类型组件
        submitTypeCombo = new JComboBox<>(new String[]{"both", "text_only", "file_only"});
        submitTypeCombo.setSelectedItem("both");

        // 文件限制组件
        allowedFileTypesField = new JTextField(30);
        allowedFileTypesField.setText("pdf,doc,docx,jpg,png"); // 默认允许的文件类型
        maxFileSizeField = new JTextField(20);
        maxFileSizeField.setText("10485760"); // 默认10MB

        // 按钮组件
        createButton = new JButton("创建作业");
        resetButton = new JButton("重置");
        previewButton = new JButton("预览");
        statusLabel = new JLabel("就绪");
        statusLabel.setForeground(Color.BLUE);

        // 事件监听
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createAssignment();
            }
        });

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetForm();
            }
        });

        previewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                previewAssignment();
            }
        });

        submitTypeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateFileFieldsVisibility();
            }
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 主要内容面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 标题
        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(new JLabel("作业标题:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        mainPanel.add(titleField, gbc);

        // 课程选择
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        mainPanel.add(new JLabel("选择课程:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        mainPanel.add(courseCombo, gbc);

        // 描述
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        mainPanel.add(new JLabel("作业描述:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(new JScrollPane(descriptionArea), gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0;

        // 截止时间
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(new JLabel("截止时间:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel deadlinePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        deadlinePanel.add(new JLabel("日期:"));
        deadlinePanel.add(deadlineDateField);
        deadlinePanel.add(new JLabel("时间:"));
        deadlinePanel.add(deadlineTimeField);
        mainPanel.add(deadlinePanel, gbc);

        // 满分
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        mainPanel.add(new JLabel("满分:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel scorePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        scorePanel.add(maxScoreSpinner);
        scorePanel.add(new JLabel("分"));
        mainPanel.add(scorePanel, gbc);

        // 提交类型
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        mainPanel.add(new JLabel("提交类型:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        mainPanel.add(submitTypeCombo, gbc);

        // 文件类型限制
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0;
        mainPanel.add(new JLabel("允许文件类型:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        mainPanel.add(allowedFileTypesField, gbc);

        // 文件大小限制
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0;
        mainPanel.add(new JLabel("文件大小限制:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel fileSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        fileSizePanel.add(maxFileSizeField);
        fileSizePanel.add(new JLabel("字节 (0表示无限制)"));
        mainPanel.add(fileSizePanel, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(createButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(previewButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(statusLabel);

        // 添加到主面板
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // 加载教师的课程列表
    private void loadTeacherCourses() {
        try {
            statusLabel.setText("正在加载课程...");
            statusLabel.setForeground(Color.BLUE);

            courseCombo.removeAllItems();

            // 从数据库获取该教师教授的课程
            List<Course> courses = courseDAO.getCoursesByTeacher(teacherId);

            if (courses != null && !courses.isEmpty()) {
                for (Course course : courses) {
                    courseCombo.addItem(new CourseItem(course));
                }

                // 如果有传入的courseId，设置为默认选中
                if (courseId != null && !courseId.trim().isEmpty()) {
                    setSelectedCourse(courseId);
                }

                statusLabel.setText("课程加载完成");
                statusLabel.setForeground(Color.GREEN);
            } else {
                statusLabel.setText("未找到课程");
                statusLabel.setForeground(Color.ORANGE);
                JOptionPane.showMessageDialog(this, "您尚未教授任何课程，请先创建课程",
                        "提示", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            statusLabel.setText("加载课程失败: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "加载课程列表失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // 设置默认选中的课程
    private void setSelectedCourse(String courseId) {
        for (int i = 0; i < courseCombo.getItemCount(); i++) {
            CourseItem item = courseCombo.getItemAt(i);
            if (item.getCourse().getCourseId().equals(courseId)) {
                courseCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void initializeDefaultValues() {
        updateFileFieldsVisibility();
    }

    private void updateFileFieldsVisibility() {
        String submitType = (String) submitTypeCombo.getSelectedItem();
        boolean showFileFields = !"text_only".equals(submitType);
        allowedFileTypesField.setEnabled(showFileFields);
        maxFileSizeField.setEnabled(showFileFields);
    }

    private void createAssignment() {
        try {
            // 验证输入
            if (!validateInput()) {
                return;
            }

            statusLabel.setText("正在创建作业...");
            statusLabel.setForeground(Color.BLUE);
            createButton.setEnabled(false);

            // 创建作业对象
            Assignment assignment = new Assignment();

            // 获取选中的课程
            CourseItem selectedCourseItem = (CourseItem) courseCombo.getSelectedItem();
            if (selectedCourseItem != null) {
                Course selectedCourse = selectedCourseItem.getCourse();
                assignment.setCourseId(selectedCourse.getCourseId());
            }

            assignment.setTeacherId(teacherId);
            assignment.setTitle(titleField.getText().trim());
            assignment.setDescription(descriptionArea.getText().trim());

            // 解析截止时间
            String dateStr = deadlineDateField.getText().trim();
            String timeStr = deadlineTimeField.getText().trim();
            String dateTimeStr = dateStr + "T" + timeStr;
            assignment.setDeadline(LocalDateTime.parse(dateTimeStr));

            assignment.setMaxScore((Integer) maxScoreSpinner.getValue());
            assignment.setSubmitType((String) submitTypeCombo.getSelectedItem());

            if (allowedFileTypesField.isEnabled()) {
                assignment.setAllowedFileTypes(allowedFileTypesField.getText().trim());
            }

            if (maxFileSizeField.isEnabled() && !maxFileSizeField.getText().trim().isEmpty()) {
                try {
                    int fileSize = Integer.parseInt(maxFileSizeField.getText().trim());
                    assignment.setMaxFileSize(fileSize > 0 ? fileSize : null);
                } catch (NumberFormatException e) {
                    assignment.setMaxFileSize(null);
                }
            }

            assignment.setStatus("active");

            // 调用服务层创建作业
            boolean success = assignmentService.createAssignment(assignment);

            if (success) {
                statusLabel.setText("作业创建成功！");
                statusLabel.setForeground(Color.GREEN);
                JOptionPane.showMessageDialog(this, "作业创建成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                resetForm();
            } else {
                statusLabel.setText("作业创建失败");
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "作业创建失败，请重试", "错误", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            statusLabel.setText("创建失败: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "创建失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            createButton.setEnabled(true);
        }
    }

    private boolean validateInput() {
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入作业标题", "提示", JOptionPane.WARNING_MESSAGE);
            titleField.requestFocus();
            return false;
        }

        if (courseCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "请选择课程", "提示", JOptionPane.WARNING_MESSAGE);
            courseCombo.requestFocus();
            return false;
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入作业描述", "提示", JOptionPane.WARNING_MESSAGE);
            descriptionArea.requestFocus();
            return false;
        }

        if (deadlineDateField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入截止日期", "提示", JOptionPane.WARNING_MESSAGE);
            deadlineDateField.requestFocus();
            return false;
        }

        if (deadlineTimeField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入截止时间", "提示", JOptionPane.WARNING_MESSAGE);
            deadlineTimeField.requestFocus();
            return false;
        }

        try {
            String dateTimeStr = deadlineDateField.getText().trim() + "T" + deadlineTimeField.getText().trim();
            LocalDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "截止时间格式不正确，请使用 yyyy-MM-dd 和 HH:mm 格式",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private void resetForm() {
        titleField.setText("");
        descriptionArea.setText("");
        deadlineDateField.setText(java.time.LocalDate.now().plusDays(7).toString());
        deadlineTimeField.setText("23:59");
        maxScoreSpinner.setValue(100);
        submitTypeCombo.setSelectedItem("both");
        allowedFileTypesField.setText("pdf,doc,docx,jpg,png");
        maxFileSizeField.setText("10485760");
        statusLabel.setText("就绪");
        statusLabel.setForeground(Color.BLUE);
        updateFileFieldsVisibility();

        // 重新加载课程列表
        loadTeacherCourses();
    }

    private void previewAssignment() {
        if (!validateInput()) {
            return;
        }

        StringBuilder preview = new StringBuilder();
        preview.append("=== 作业预览 ===\n\n");
        preview.append("标题: ").append(titleField.getText().trim()).append("\n\n");
        preview.append("描述:\n").append(descriptionArea.getText().trim()).append("\n\n");

        // 显示选中的课程信息
        CourseItem selectedCourseItem = (CourseItem) courseCombo.getSelectedItem();
        if (selectedCourseItem != null) {
            Course course = selectedCourseItem.getCourse();
            preview.append("课程: ").append(course.getCourseName())
                    .append(" (").append(course.getCourseId()).append(")\n");
        }

        preview.append("截止时间: ").append(deadlineDateField.getText().trim())
                .append(" ").append(deadlineTimeField.getText().trim()).append("\n");
        preview.append("满分: ").append(maxScoreSpinner.getValue()).append("分\n");
        preview.append("提交类型: ").append(getSubmitTypeText((String) submitTypeCombo.getSelectedItem())).append("\n");

        if (allowedFileTypesField.isEnabled() && !allowedFileTypesField.getText().trim().isEmpty()) {
            preview.append("允许文件类型: ").append(allowedFileTypesField.getText().trim()).append("\n");
        }

        if (maxFileSizeField.isEnabled() && !maxFileSizeField.getText().trim().isEmpty()) {
            try {
                int size = Integer.parseInt(maxFileSizeField.getText().trim());
                preview.append("文件大小限制: ").append(formatFileSize(size)).append("\n");
            } catch (NumberFormatException e) {
                preview.append("文件大小限制: 无限制\n");
            }
        }

        JTextArea previewArea = new JTextArea(preview.toString());
        previewArea.setEditable(false);
        previewArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(previewArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));

        JOptionPane.showMessageDialog(this, scrollPane, "作业预览", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getSubmitTypeText(String submitType) {
        switch (submitType) {
            case "text_only": return "仅文字";
            case "file_only": return "仅文件";
            case "both": return "文字和文件";
            default: return submitType;
        }
    }

    private String formatFileSize(int size) {
        if (size <= 0) return "无限制";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024));
    }

    // 课程项包装类
    private class CourseItem {
        private Course course;

        public CourseItem(Course course) {
            this.course = course;
        }

        public Course getCourse() {
            return course;
        }

        @Override
        public String toString() {
            return course.getCourseId() + " - " + course.getCourseName();
        }
    }
}