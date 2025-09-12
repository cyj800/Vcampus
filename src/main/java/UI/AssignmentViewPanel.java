package UI;

import service.AssignmentService;
import service.SubmissionService;
import model.Assignment;
import model.Submission;
import client.ClientNetwork;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AssignmentViewPanel extends JPanel {
    private String studentId;
    private String courseId;
    private AssignmentService assignmentService;
    private SubmissionService submissionService;

    // UI组件
    private JList<Assignment> assignmentList;
    private DefaultListModel<Assignment> listModel;
    private JTextArea assignmentDetailArea;
    private JButton submitButton;
    private JButton appealButton; // 新增申诉按钮
    private JButton refreshButton;
    private JButton downloadButton; // 新增下载按钮
    private JLabel statusLabel;

    public AssignmentViewPanel(String studentId, String courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.assignmentService = new AssignmentService();
        this.submissionService = new SubmissionService();

        initializeComponents();
        setupLayout();
        loadAssignments();
    }

    private void initializeComponents() {
        // 作业列表
        listModel = new DefaultListModel<>();
        assignmentList = new JList<>(listModel);
        assignmentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        assignmentList.setCellRenderer(new AssignmentListCellRenderer());

        // 详细信息区域
        assignmentDetailArea = new JTextArea(15, 40);
        assignmentDetailArea.setEditable(false);
        assignmentDetailArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        assignmentDetailArea.setLineWrap(true);
        assignmentDetailArea.setWrapStyleWord(true);

        // 按钮
        submitButton = new JButton("提交作业");
        submitButton.setEnabled(false);
        appealButton = new JButton("申诉成绩"); // 新增申诉按钮
        appealButton.setEnabled(false);
        downloadButton = new JButton("下载文件"); // 新增下载按钮
        downloadButton.setEnabled(false);
        refreshButton = new JButton("刷新");
        statusLabel = new JLabel("就绪");
        statusLabel.setForeground(Color.BLUE);

        // 事件监听
        assignmentList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    Assignment selected = assignmentList.getSelectedValue();
                    if (selected != null) {
                        showAssignmentDetails(selected);
                        updateSubmitButtonState(selected);
                    }
                }
            }
        });

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSubmitDialog();
            }
        });

        // 新增申诉按钮事件监听
        appealButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAppealDialog();
            }
        });

        // 新增下载按钮事件监听
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downloadSelectedFile();
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAssignments();
            }
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 左侧面板 - 作业列表
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("作业列表"));
        leftPanel.setPreferredSize(new Dimension(300, getHeight()));

        JScrollPane listScrollPane = new JScrollPane(assignmentList);
        leftPanel.add(listScrollPane, BorderLayout.CENTER);

        JPanel leftButtonPanel = new JPanel(new FlowLayout());
        leftButtonPanel.add(refreshButton);
        leftPanel.add(leftButtonPanel, BorderLayout.SOUTH);

        // 右侧面板 - 作业详情
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("作业详情"));

        JScrollPane detailScrollPane = new JScrollPane(assignmentDetailArea);
        rightPanel.add(detailScrollPane, BorderLayout.CENTER);

        // 修改按钮面板，添加申诉按钮和下载按钮
        JPanel rightButtonPanel = new JPanel(new FlowLayout());
        rightButtonPanel.add(submitButton);
        rightButtonPanel.add(appealButton); // 添加申诉按钮
        rightButtonPanel.add(downloadButton); // 添加下载按钮
        rightButtonPanel.add(statusLabel);
        rightPanel.add(rightButtonPanel, BorderLayout.SOUTH);

        // 添加到主面板
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private void loadAssignments() {
        try {
            statusLabel.setText("正在加载作业...");
            statusLabel.setForeground(Color.BLUE);

            List<Assignment> assignments = assignmentService.getStudentAssignments(studentId, courseId);
            listModel.clear();

            for (Assignment assignment : assignments) {
                listModel.addElement(assignment);
            }

            statusLabel.setText("加载完成，共 " + assignments.size() + " 个作业");
            statusLabel.setForeground(Color.GREEN);

        } catch (Exception e) {
            statusLabel.setText("加载失败: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "加载作业列表失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAssignmentDetails(Assignment assignment) {
        StringBuilder detail = new StringBuilder();
        detail.append("=== 作业详情 ===\n\n");
        detail.append("作业标题: ").append(assignment.getTitle()).append("\n\n");
        detail.append("作业描述:\n").append(assignment.getDescription()).append("\n\n");
        detail.append("课程ID: ").append(assignment.getCourseId()).append("\n");
        detail.append("截止时间: ").append(assignment.getDeadline()
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))).append("\n");
        detail.append("满分: ").append(assignment.getMaxScore()).append("分\n\n");

        detail.append("=== 提交要求 ===\n");
        detail.append("提交类型: ").append(getSubmitTypeText(assignment.getSubmitType())).append("\n");
        if (assignment.getAllowedFileTypes() != null && !assignment.getAllowedFileTypes().isEmpty()) {
            detail.append("允许文件类型: ").append(assignment.getAllowedFileTypes()).append("\n");
        }
        if (assignment.getMaxFileSize() != null) {
            detail.append("文件大小限制: ").append(formatFileSize(assignment.getMaxFileSize())).append("\n");
        }

        detail.append("\n=== 状态信息 ===\n");
        try {
            boolean isSubmitted = assignmentService.isAssignmentSubmitted(assignment.getAssignmentId(), studentId);
            detail.append("提交状态: ").append(isSubmitted ? "已提交" : "未提交").append("\n");

            if (isSubmitted) {
                List<Submission> submissions = submissionService.getStudentSubmissions(studentId);
                for (Submission submission : submissions) {
                    if (submission.getAssignmentId() == assignment.getAssignmentId()) {
                        detail.append("提交时间: ").append(submission.getSubmitTime()
                                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))).append("\n");
                        if (submission.getScore() != null) {
                            detail.append("成绩: ").append(submission.getScore()).append("分\n");
                            if (submission.getFeedback() != null && !submission.getFeedback().isEmpty()) {
                                detail.append("教师评语: ").append(submission.getFeedback()).append("\n");
                            }
                        }
                        // 修复文件信息显示
                        if (submission.getFilePath() != null && !submission.getFilePath().isEmpty()) {
                            detail.append("附件: ").append(submission.getFileName()).append("\n");
                            // 修复文件大小显示
                            if (submission.getFileSize() != null && submission.getFileSize() > 0) {
                                detail.append("文件大小: ").append(formatFileSize(submission.getFileSize())).append("\n");
                            } else {
                                detail.append("文件大小: 未知\n");
                            }
                        } else {
                            detail.append("无附件\n");
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            detail.append("状态查询失败: " + e.getMessage() + "\n"); // 显示具体错误信息
        }
        assignmentDetailArea.setText(detail.toString());
        assignmentDetailArea.setCaretPosition(0);
    }

    private void updateSubmitButtonState(Assignment assignment) {
        try {
            boolean isSubmitted = assignmentService.isAssignmentSubmitted(assignment.getAssignmentId(), studentId);
            boolean isActive = assignment.isActive() &&
                    assignment.getDeadline().isAfter(java.time.LocalDateTime.now());

            // 检查是否已评分
            Submission gradedSubmission = getGradedSubmission(assignment.getAssignmentId());

            if (isSubmitted && gradedSubmission != null) {
                // 已提交且已评分，启用申诉按钮
                submitButton.setText("已提交");
                submitButton.setEnabled(false);
                appealButton.setEnabled(true);
                appealButton.setText("申诉成绩");

                // 如果有文件，启用下载按钮
                if (gradedSubmission.getFilePath() != null && !gradedSubmission.getFilePath().isEmpty()) {
                    downloadButton.setEnabled(true);
                } else {
                    downloadButton.setEnabled(false);
                }
            } else if (!isSubmitted && isActive) {
                // 未提交且作业活跃，启用提交按钮
                submitButton.setText("提交作业");
                submitButton.setEnabled(true);
                appealButton.setEnabled(false);
                downloadButton.setEnabled(false);
            } else {
                // 其他情况
                submitButton.setText(isSubmitted ? "已提交" : "已过期");
                submitButton.setEnabled(false);
                appealButton.setEnabled(false);
                downloadButton.setEnabled(false);
            }
        } catch (Exception e) {
            submitButton.setEnabled(false);
            appealButton.setEnabled(false);
            downloadButton.setEnabled(false);
        }
    }

    private void openSubmitDialog() {
        Assignment selected = assignmentList.getSelectedValue();
        if (selected != null) {
            AssignmentSubmitDialog dialog = new AssignmentSubmitDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    selected,
                    studentId
            );
            dialog.setVisible(true);

            // 提交完成后刷新界面
            if (dialog.isSubmitted()) {
                loadAssignments();
                showAssignmentDetails(selected);
                updateSubmitButtonState(selected);
            }
        }
    }

    // 新增打开申诉对话框的方法
    private void openAppealDialog() {
        Assignment selected = assignmentList.getSelectedValue();
        if (selected != null) {
            Submission gradedSubmission = getGradedSubmission(selected.getAssignmentId());
            if (gradedSubmission != null) {
                AppealSubmitDialog dialog = new AppealSubmitDialog(
                        (Frame) SwingUtilities.getWindowAncestor(this),
                        gradedSubmission,
                        studentId
                );
                dialog.setVisible(true);

                // 申诉提交完成后刷新界面
                if (dialog.isAppealSubmitted()) {
                    loadAssignments();
                    showAssignmentDetails(selected);
                    updateSubmitButtonState(selected);
                }
            } else {
                JOptionPane.showMessageDialog(this, "未找到已评分的提交记录", "提示", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // 添加获取已评分提交记录的方法
    private Submission getGradedSubmission(int assignmentId) {
        try {
            List<Submission> submissions = submissionService.getStudentSubmissions(studentId);
            for (Submission submission : submissions) {
                if (submission.getAssignmentId() == assignmentId &&
                        submission.getScore() != null) {
                    return submission;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 新增下载文件方法
    private void downloadSelectedFile() {
        Assignment selected = assignmentList.getSelectedValue();
        if (selected != null) {
            try {
                // 获取对应的提交记录
                List<Submission> submissions = submissionService.getStudentSubmissions(studentId);
                Submission targetSubmission = null;

                for (Submission submission : submissions) {
                    if (submission.getAssignmentId() == selected.getAssignmentId() &&
                            submission.getFilePath() != null && !submission.getFilePath().isEmpty()) {
                        targetSubmission = submission;
                        break;
                    }
                }

                if (targetSubmission != null) {
                    downloadFile(targetSubmission.getSubmissionId(), targetSubmission.getFileName());
                } else {
                    JOptionPane.showMessageDialog(this, "未找到可下载的文件", "提示", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "获取提交记录失败: " + e.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
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
                    JOptionPane.showMessageDialog(AssignmentViewPanel.this,
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

    private String getSubmitTypeText(String submitType) {
        switch (submitType) {
            case "text_only": return "仅文字";
            case "file_only": return "仅文件";
            case "both": return "文字和文件";
            default: return submitType;
        }
    }

    private String formatFileSize(int size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024));
    }

    // 自定义列表单元格渲染器
    private class AssignmentListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Assignment) {
                Assignment assignment = (Assignment) value;
                String text = String.format("[%d] %s", assignment.getAssignmentId(), assignment.getTitle());
                setText(text);

                // 根据状态设置颜色
                try {
                    boolean isSubmitted = assignmentService.isAssignmentSubmitted(assignment.getAssignmentId(), studentId);
                    if (isSubmitted) {
                        setForeground(Color.GREEN);
                        setText(text + " (已提交)");
                    } else if (!assignment.isActive() ||
                            assignment.getDeadline().isBefore(java.time.LocalDateTime.now())) {
                        setForeground(Color.RED);
                        setText(text + " (已过期)");
                    } else {
                        setForeground(Color.BLUE);
                    }
                } catch (Exception e) {
                    setForeground(Color.BLACK);
                }
            }

            return this;
        }
    }

    private String formatFileSize(Integer fileSize) {
        if (fileSize == null || fileSize <= 0) {
            return "未知大小";
        }
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024));
    }
}