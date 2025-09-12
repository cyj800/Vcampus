package UI;

import client.ClientNetwork;
import model.Course;
import model.Submission;
import service.SubmissionService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StudentSubmissionPanel extends JPanel {
    private String studentId;
    private String studentName;
    private SubmissionService submissionService;

    // UI组件
    private JTable submissionTable;
    private SubmissionTableModel tableModel;
    private JTextArea submissionDetailArea;
    private JComboBox<String> statusFilterCombo;
    private JComboBox<CourseItem> courseFilterCombo; // 修改为CourseItem类型
    private JButton refreshButton;
    private JButton viewDetailButton;
    private JButton downloadButton;
    private JLabel statusLabel;

    // 数据存储
    private List<Course> studentCourses; // 存储学生课程列表
    private List<Submission> allSubmissions; // 所有提交记录

    // 课程筛选项包装类
    private class CourseItem {
        private String displayName;
        private String courseId;

        public CourseItem(String displayName, String courseId) {
            this.displayName = displayName;
            this.courseId = courseId;
        }

        public String getDisplayName() { return displayName; }
        public String getCourseId() { return courseId; }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public StudentSubmissionPanel(String studentId) {
        this.studentId = studentId;
        this.submissionService = new SubmissionService();
        this.studentCourses = new ArrayList<>();
        this.allSubmissions = new ArrayList<>();

        initializeComponents();
        setupLayout();
        setupEventListeners();
        initializeData();
    }

    private void initializeComponents() {
        // 表格模型和表格
        tableModel = new SubmissionTableModel();
        submissionTable = new JTable(tableModel);
        submissionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        submissionTable.setRowHeight(25);

        // 详细信息区域
        submissionDetailArea = new JTextArea(15, 40);
        submissionDetailArea.setEditable(false);
        submissionDetailArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        submissionDetailArea.setLineWrap(true);
        submissionDetailArea.setWrapStyleWord(true);

        // 筛选组件
        statusFilterCombo = new JComboBox<>(new String[]{"全部", "已提交", "已评分", "已修订"});
        statusFilterCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        courseFilterCombo = new JComboBox<>(); // 初始化为空
        courseFilterCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        courseFilterCombo.setPreferredSize(new Dimension(200, 35));

        // 按钮组件
        refreshButton = new JButton("刷新");
        viewDetailButton = new JButton("查看详情");
        downloadButton = new JButton("下载文件");
        statusLabel = new JLabel("就绪");
        statusLabel.setForeground(Color.BLUE);

        // 设置初始状态
        viewDetailButton.setEnabled(false);
        downloadButton.setEnabled(false);
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 顶部控制面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("状态筛选:"));
        topPanel.add(statusFilterCombo);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(new JLabel("课程筛选:"));
        topPanel.add(courseFilterCombo);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(refreshButton);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(statusLabel);

        // 主要内容区域 - 分割面板
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(500);

        // 左侧面板 - 提交记录列表
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("我的提交记录"));
        JScrollPane tableScrollPane = new JScrollPane(submissionTable);
        leftPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainSplitPane.setLeftComponent(leftPanel);

        // 右侧面板 - 详细信息
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("提交详情"));

        JScrollPane detailScrollPane = new JScrollPane(submissionDetailArea);
        rightPanel.add(detailScrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(viewDetailButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(downloadButton);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainSplitPane.setRightComponent(rightPanel);

        // 添加到主面板
        add(topPanel, BorderLayout.NORTH);
        add(mainSplitPane, BorderLayout.CENTER);
    }

    private void setupEventListeners() {
        // 刷新按钮事件
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadInitialData();
            }
        });

        // 状态筛选事件
        statusFilterCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterSubmissions();
            }
        });

        // 课程筛选事件
        courseFilterCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterSubmissions();
            }
        });

        // 表格选择事件
        submissionTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailButtonState();
            }
        });

        // 查看详情按钮事件
        viewDetailButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSelectedSubmissionDetail();
            }
        });

        // 下载文件按钮事件
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downloadSelectedFile();
            }
        });
    }

    private void initializeData() {
        statusLabel.setText("正在初始化数据...");
        statusLabel.setForeground(Color.BLUE);

        // 并行加载数据
        loadStudentCourses(); // 加载学生课程
        loadSubmissions();    // 加载提交记录
    }

    // 加载学生课程列表
    private void loadStudentCourses() {
        System.out.println("=== 开始加载学生课程列表 ===");
        System.out.println("学生ID: " + studentId);

        try {
            ClientNetwork.getStudentCourses(studentId, new ClientNetwork.CourseCallback() {
                @Override
                public void onCourseResult(boolean success, String message, List<Course> courses) {
                    SwingUtilities.invokeLater(() -> {
                        if (success && courses != null) {
                            studentCourses = courses;
                            System.out.println("成功加载 " + courses.size() + " 门课程");
                            updateCourseFilterOptions();

                            statusLabel.setText("课程加载完成，共 " + courses.size() + " 门");
                            statusLabel.setForeground(Color.GREEN);
                        } else {
                            System.err.println("加载课程失败: " + message);
                            studentCourses = new ArrayList<>();
                            updateCourseFilterOptions();

                            statusLabel.setText("课程加载失败: " + message);
                            statusLabel.setForeground(Color.RED);
                        }
                    });
                }
            });
        } catch (Exception e) {
            System.err.println("加载学生课程时出错: " + e.getMessage());
            e.printStackTrace();
            studentCourses = new ArrayList<>();
            updateCourseFilterOptions();

            statusLabel.setText("课程加载异常: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    // 更新课程筛选下拉框选项
    private void updateCourseFilterOptions() {
        System.out.println("=== 更新课程筛选选项 ===");

        try {
            courseFilterCombo.removeAllItems();
            courseFilterCombo.addItem(new CourseItem("全部课程", "ALL"));

            if (studentCourses != null && !studentCourses.isEmpty()) {
                System.out.println("添加 " + studentCourses.size() + " 门课程到筛选器");
                for (Course course : studentCourses) {
                    if (course != null && course.getCourseId() != null) {
                        CourseItem item = new CourseItem(
                                course.getCourseName() + " (" + course.getCourseId() + ")",
                                course.getCourseId()
                        );
                        courseFilterCombo.addItem(item);
                        System.out.println("添加课程: " + course.getCourseId() + " - " + course.getCourseName());
                    }
                }
            } else {
                System.out.println("学生暂无选课记录");
            }

            // 设置默认选中项
            courseFilterCombo.setSelectedIndex(0);

        } catch (Exception e) {
            System.err.println("更新课程筛选选项时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 加载提交记录
    private void loadSubmissions() {
        try {
            statusLabel.setText("正在加载提交记录...");
            statusLabel.setForeground(Color.BLUE);

            allSubmissions = submissionService.getStudentSubmissions(studentId);
            tableModel.setSubmissions(allSubmissions);
            tableModel.fireTableDataChanged();

            statusLabel.setText("加载完成，共 " + allSubmissions.size() + " 个提交");
            statusLabel.setForeground(Color.GREEN);

            // 清空详细信息
            submissionDetailArea.setText("");
            updateDetailButtonState();

        } catch (Exception e) {
            statusLabel.setText("加载失败: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "加载提交记录失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 加载初始数据
    private void loadInitialData() {
        statusLabel.setText("正在加载数据...");
        statusLabel.setForeground(Color.BLUE);

        // 重新加载所有数据
        loadStudentCourses();
        loadSubmissions();
    }

    // 筛选提交记录
    private void filterSubmissions() {
        try {
            System.out.println("=== 开始筛选提交记录 ===");

            // 获取选中的筛选条件
            String selectedStatus = (String) statusFilterCombo.getSelectedItem();
            CourseItem selectedCourseItem = (CourseItem) courseFilterCombo.getSelectedItem();

            String selectedCourseId = selectedCourseItem != null ?
                    selectedCourseItem.getCourseId() : "ALL";

            System.out.println("筛选条件 - 状态: " + selectedStatus + ", 课程: " + selectedCourseId);

            List<Submission> filteredSubmissions = new ArrayList<>(allSubmissions);

            // 按状态筛选
            if (!"全部".equals(selectedStatus)) {
                filteredSubmissions = filterByStatus(filteredSubmissions, selectedStatus);
            }

            // 按课程筛选
            if (!"ALL".equals(selectedCourseId)) {
                filteredSubmissions = filterByCourse(filteredSubmissions, selectedCourseId);
            }

            tableModel.setSubmissions(filteredSubmissions);
            tableModel.fireTableDataChanged();
            submissionDetailArea.setText("");
            updateDetailButtonState();

            statusLabel.setText("筛选完成，共 " + filteredSubmissions.size() + " 个提交");
            statusLabel.setForeground(Color.GREEN);

        } catch (Exception e) {
            System.err.println("筛选提交记录时出错: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("筛选失败: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "筛选失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 按状态筛选
    private List<Submission> filterByStatus(List<Submission> submissions, String status) {
        List<Submission> filtered = new ArrayList<>();
        System.out.println("按状态筛选: " + status);

        for (Submission submission : submissions) {
            String submissionStatus = getStatusText(submission.getStatus());
            if (status.equals(submissionStatus)) {
                filtered.add(submission);
            }
        }

        System.out.println("状态筛选结果: " + filtered.size() + " 个提交");
        return filtered;
    }

    // 按课程筛选
    private List<Submission> filterByCourse(List<Submission> submissions, String courseId) {
        List<Submission> filtered = new ArrayList<>();
        System.out.println("按课程筛选: " + courseId);

        for (Submission submission : submissions) {
            // 假设Submission中已经有courseId或可以通过其他方式获取
            if (courseId.equals(getCourseIdFromSubmission(submission))) {
                filtered.add(submission);
            }
        }

        System.out.println("课程筛选结果: " + filtered.size() + " 个提交");
        return filtered;
    }

    // 从提交记录中获取课程ID的辅助方法
    private String getCourseIdFromSubmission(Submission submission) {
        // 这里需要根据实际的数据结构来实现
        // 如果Submission中已经有courseId字段，直接返回
        // 如果没有，可能需要通过assignmentId查询作业表获取课程ID

        // 临时实现：如果Submission中有courseName，可以根据课程名称匹配
        if (submission.getCourseName() != null && studentCourses != null) {
            for (Course course : studentCourses) {
                if (submission.getCourseName().equals(course.getCourseName())) {
                    return course.getCourseId();
                }
            }
        }

        // 如果无法匹配，返回一个标识符
        return "UNKNOWN";
    }

    // 获取状态文本
    private String getStatusText(String status) {
        switch (status) {
            case "submitted": return "已提交";
            case "graded": return "已评分";
            case "revised": return "已修订";
            default: return status;
        }
    }

    // 更新详情按钮状态
    private void updateDetailButtonState() {
        int selectedRow = submissionTable.getSelectedRow();
        viewDetailButton.setEnabled(selectedRow >= 0);

        // 同时更新下载按钮状态
        if (selectedRow >= 0) {
            Submission submission = tableModel.getSubmissionAt(selectedRow);
            downloadButton.setEnabled(submission.getFilePath() != null && !submission.getFilePath().isEmpty());
        } else {
            downloadButton.setEnabled(false);
        }
    }

    // 显示选中提交记录的详细信息
    private void showSelectedSubmissionDetail() {
        int selectedRow = submissionTable.getSelectedRow();
        if (selectedRow >= 0) {
            Submission submission = tableModel.getSubmissionAt(selectedRow);
            showSubmissionDetails(submission);
        }
    }

    // 显示提交记录详情
    private void showSubmissionDetails(Submission submission) {





        StringBuilder detail = new StringBuilder();
        detail.append("=== 提交基本信息 ===\n\n");
        detail.append("提交ID: ").append(submission.getSubmissionId()).append("\n");
        detail.append("作业标题: ").append(submission.getAssignmentTitle()).append("\n");
        detail.append("课程名称: ").append(submission.getCourseName()).append("\n");
        detail.append("提交时间: ").append(submission.getSubmitTime()
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))).append("\n");
        detail.append("当前状态: ").append(getStatusText(submission.getStatus())).append("\n\n");

        if (submission.getScore() != null) {
            detail.append("成绩: ").append(submission.getScore()).append("分\n");
        }

        if (submission.getFeedback() != null && !submission.getFeedback().isEmpty()) {
            detail.append("教师评语:\n").append(submission.getFeedback()).append("\n\n");
        }

        detail.append("=== 提交内容 ===\n");
        if (submission.getContent() != null && !submission.getContent().isEmpty()) {
            detail.append(submission.getContent()).append("\n\n");
        } else {
            detail.append("无文字内容\n\n");
        }

        if (submission.getFilePath() != null && !submission.getFilePath().isEmpty()) {
            detail.append("附件信息:\n");
            detail.append("文件名: ").append(submission.getFileName()).append("\n");
            detail.append("文件大小: ").append(formatFileSize(submission.getFileSize())).append("\n");
            detail.append("文件路径: ").append(submission.getFilePath()).append("\n\n");
        } else {
            detail.append("无附件\n\n");
        }

        if (submission.getGradedTime() != null) {
            detail.append("批改时间: ").append(submission.getGradedTime()
                    .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))).append("\n");
        }

        if (submission.getGraderId() != null && !submission.getGraderId().isEmpty()) {
            detail.append("批改教师: ").append(submission.getGraderId()).append("\n");
        }

        submissionDetailArea.setText(detail.toString());
        submissionDetailArea.setCaretPosition(0);
    }

    // 格式化文件大小
    private String formatFileSize(Integer fileSize) {
        if (fileSize == null || fileSize <= 0) {
            return "未知大小";
        }
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024));
    }

    // 下载选中文件
    private void downloadSelectedFile() {
        int selectedRow = submissionTable.getSelectedRow();
        if (selectedRow >= 0) {
            Submission submission = tableModel.getSubmissionAt(selectedRow);
            if (submission.getSubmissionId() > 0) {
                downloadFile(submission.getSubmissionId(), submission.getFileName());
            } else {
                JOptionPane.showMessageDialog(this, "该提交没有附件", "提示", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // 文件下载方法
    private void downloadFile(int submissionId, String fileName) {
        statusLabel.setText("正在下载文件...");
        statusLabel.setForeground(Color.BLUE);
        downloadButton.setEnabled(false);

        ClientNetwork.downloadFile(submissionId, new ClientNetwork.FileDownloadCallback() {
            @Override
            public void onDownloadStart(String fileName, long fileSize) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("开始下载: " + fileName + " (" + formatFileSize((int) fileSize) + ")");
                });
            }

            @Override
            public void onDownloadProgress(long downloadedBytes, long totalBytes) {
                SwingUtilities.invokeLater(() -> {
                    int progress = (int) ((downloadedBytes * 100) / totalBytes);
                    statusLabel.setText("下载进度: " + progress + "%");
                });
            }

            @Override
            public void onDownloadComplete(byte[] fileData, String fileName) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("下载完成");
                    statusLabel.setForeground(Color.GREEN);

                    // 保存文件
                    saveFile(fileData, fileName);

                    downloadButton.setEnabled(true);
                });
            }

            @Override
            public void onDownloadError(String errorMessage) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("下载失败: " + errorMessage);
                    statusLabel.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(StudentSubmissionPanel.this,
                            "文件下载失败: " + errorMessage, "错误", JOptionPane.ERROR_MESSAGE);
                    downloadButton.setEnabled(true);
                });
            }
        });
    }

    // 保存文件到本地
    private void saveFile(byte[] fileData, String fileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(fileName));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File selectedFile = fileChooser.getSelectedFile();
                java.nio.file.Files.write(selectedFile.toPath(), fileData);
                JOptionPane.showMessageDialog(this, "文件保存成功: " + selectedFile.getAbsolutePath(),
                        "成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "文件保存失败: " + e.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 提交记录表格模型
    private class SubmissionTableModel extends DefaultTableModel {
        private List<Submission> submissions;
        private String[] columnNames = {"ID", "作业", "课程", "状态", "成绩", "提交时间"};

        public void setSubmissions(List<Submission> submissions) {
            this.submissions = submissions;
        }

        public Submission getSubmissionAt(int rowIndex) {
            return submissions != null && rowIndex < submissions.size() ? submissions.get(rowIndex) : null;
        }

        @Override
        public int getRowCount() {
            return submissions != null ? submissions.size() : 0;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (submissions == null || rowIndex >= submissions.size()) {
                return null;
            }

            Submission submission = submissions.get(rowIndex);
            switch (columnIndex) {
                case 0: return submission.getSubmissionId();
                case 1: return submission.getAssignmentTitle();
                case 2: return submission.getCourseName();
                case 3: return getStatusText(submission.getStatus());
                case 4: return submission.getScore() != null ? submission.getScore() + "分" : "未评分";
                case 5: return submission.getSubmitTime() != null ?
                        submission.getSubmitTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) : "";
                default: return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Integer.class;
            } else if (columnIndex == 4) {
                return String.class;
            }
            return String.class;
        }

        // 关键修改：设置表格不可编辑
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; // 所有单元格都不可编辑
        }
    }


}