package UI;

import service.SubmissionService;
import service.AssignmentService;
import model.Submission;
import model.Assignment;
import client.ClientNetwork;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AssignmentGradingPanel extends JPanel {
    private String teacherId;
    private SubmissionService submissionService;
    private AssignmentService assignmentService;

    // UI组件
    private JComboBox<AssignmentItem> assignmentCombo;
    private JTable submissionTable;
    private SubmissionTableModel tableModel;
    private JTextArea submissionContentArea;
    private JButton downloadFileButton;
    private JSpinner scoreSpinner;
    private JTextArea feedbackArea;
    private JButton saveGradeButton;
    private JButton refreshButton;
    private JLabel statusLabel;
    private JLabel fileInfoLabel;

    private Submission selectedSubmission;
    private Assignment selectedAssignment;

    public AssignmentGradingPanel(String teacherId) {
        this.teacherId = teacherId;
        this.submissionService = new SubmissionService();
        this.assignmentService = new AssignmentService();

        initializeComponents();
        setupLayout();
        loadAssignments();
    }

    private void initializeComponents() {
        // 作业选择下拉框
        assignmentCombo = new JComboBox<>();
        assignmentCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadSubmissions();
            }
        });

        // 提交记录表格
        tableModel = new SubmissionTableModel();
        submissionTable = new JTable(tableModel);
        submissionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        submissionTable.setRowHeight(25);
        submissionTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = submissionTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        Submission submission = tableModel.getSubmissionAt(selectedRow);
                        showSubmissionDetails(submission);
                    }
                }
            }
        });

        // 提交内容显示区域
        submissionContentArea = new JTextArea(10, 40);
        submissionContentArea.setEditable(false);
        submissionContentArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        submissionContentArea.setLineWrap(true);
        submissionContentArea.setWrapStyleWord(true);

        // 文件下载按钮
        downloadFileButton = new JButton("下载文件");
        downloadFileButton.setEnabled(false);
        downloadFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                downloadSubmissionFile();
            }
        });

        // 评分组件
        scoreSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        scoreSpinner.setEnabled(false);

        // 反馈区域
        feedbackArea = new JTextArea(5, 40);
        feedbackArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        feedbackArea.setLineWrap(true);
        feedbackArea.setWrapStyleWord(true);
        feedbackArea.setEnabled(false);

        // 按钮组件
        saveGradeButton = new JButton("保存评分");
        saveGradeButton.setEnabled(false);
        refreshButton = new JButton("刷新");
        statusLabel = new JLabel("就绪");
        statusLabel.setForeground(Color.BLUE);
        fileInfoLabel = new JLabel();

        // 按钮事件
        saveGradeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveGrade();
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
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 顶部面板 - 作业选择
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("选择作业:"));
        topPanel.add(assignmentCombo);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(refreshButton);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(statusLabel);

        // 中间主面板
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(400);

        // 左侧面板 - 提交记录列表
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("学生提交记录"));
        JScrollPane tableScrollPane = new JScrollPane(submissionTable);
        leftPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainSplitPane.setLeftComponent(leftPanel);

        // 右侧面板 - 详细信息和评分
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));

        // 提交内容面板
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createTitledBorder("提交内容"));
        JScrollPane contentScrollPane = new JScrollPane(submissionContentArea);
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filePanel.add(downloadFileButton);
        filePanel.add(fileInfoLabel);
        contentPanel.add(contentScrollPane, BorderLayout.CENTER);
        contentPanel.add(filePanel, BorderLayout.SOUTH);

        // 评分面板
        JPanel gradePanel = new JPanel(new GridBagLayout());
        gradePanel.setBorder(BorderFactory.createTitledBorder("评分"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        gradePanel.add(new JLabel("分数 (0-100):"), gbc);
        gbc.gridx = 1;
        gradePanel.add(scoreSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gradePanel.add(new JLabel("教师反馈:"), gbc);
        gbc.gridy = 2;
        JScrollPane feedbackScrollPane = new JScrollPane(feedbackArea);
        feedbackScrollPane.setPreferredSize(new Dimension(300, 100));
        gradePanel.add(feedbackScrollPane, gbc);

        gbc.gridy = 3; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        JPanel gradeButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        gradeButtonPanel.add(saveGradeButton);
        gradePanel.add(gradeButtonPanel, gbc);

        // 右侧面板布局
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setTopComponent(contentPanel);
        rightSplitPane.setBottomComponent(gradePanel);
        rightSplitPane.setDividerLocation(200);

        rightPanel.add(rightSplitPane, BorderLayout.CENTER);

        mainSplitPane.setRightComponent(rightPanel);

        // 添加到主面板
        add(topPanel, BorderLayout.NORTH);
        add(mainSplitPane, BorderLayout.CENTER);
    }

    private void loadAssignments() {
        try {
            statusLabel.setText("正在加载作业...");
            statusLabel.setForeground(Color.BLUE);

            assignmentCombo.removeAllItems();
            List<Assignment> assignments = assignmentService.getTeacherAssignments(teacherId);

            for (Assignment assignment : assignments) {
                assignmentCombo.addItem(new AssignmentItem(assignment));
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

    private void loadSubmissions() {
        try {
            AssignmentItem selectedItem = (AssignmentItem) assignmentCombo.getSelectedItem();
            if (selectedItem != null) {
                selectedAssignment = selectedItem.getAssignment();
                statusLabel.setText("正在加载提交记录...");
                statusLabel.setForeground(Color.BLUE);

                List<Submission> submissions = submissionService.getAssignmentSubmissions(
                        selectedAssignment.getAssignmentId());
                tableModel.setSubmissions(submissions);
                tableModel.fireTableDataChanged();

                statusLabel.setText("加载完成，共 " + submissions.size() + " 个提交");
                statusLabel.setForeground(Color.GREEN);

                // 清空详细信息
                clearSubmissionDetails();
            }

        } catch (Exception e) {
            statusLabel.setText("加载失败: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "加载提交记录失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSubmissionDetails(Submission submission) {
        this.selectedSubmission = submission;

        // 显示提交内容
        submissionContentArea.setText(submission.getContent() != null ?
                submission.getContent() : "无文字内容");
        submissionContentArea.setCaretPosition(0);

        // 显示文件信息
        if (submission.getFilePath() != null && !submission.getFilePath().isEmpty()) {
            fileInfoLabel.setText("文件: " + submission.getFileName() +
                    " (" + formatFileSize(submission.getFileSize()) + ")");
            downloadFileButton.setEnabled(true);
        } else {
            fileInfoLabel.setText("无附件");
            downloadFileButton.setEnabled(false);
        }

        // 显示评分信息
        if (submission.getScore() != null) {
            scoreSpinner.setValue(submission.getScore());
        } else {
            scoreSpinner.setValue(0);
        }

        feedbackArea.setText(submission.getFeedback() != null ?
                submission.getFeedback() : "");

        // 启用评分组件
        scoreSpinner.setEnabled(true);
        feedbackArea.setEnabled(true);
        saveGradeButton.setEnabled(true);
    }

    private void clearSubmissionDetails() {
        selectedSubmission = null;
        submissionContentArea.setText("");
        fileInfoLabel.setText("");
        downloadFileButton.setEnabled(false);
        scoreSpinner.setValue(0);
        scoreSpinner.setEnabled(false);
        feedbackArea.setText("");
        feedbackArea.setEnabled(false);
        saveGradeButton.setEnabled(false);
    }

    private void downloadSubmissionFile() {
        if (selectedSubmission != null && selectedSubmission.getSubmissionId() > 0) {
            downloadFile(selectedSubmission.getSubmissionId(), selectedSubmission.getFileName());
        }
    }

    // 文件下载方法
    private void downloadFile(int submissionId, String fileName) {
        statusLabel.setText("正在下载文件...");
        statusLabel.setForeground(Color.BLUE);
        downloadFileButton.setEnabled(false);

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

                    downloadFileButton.setEnabled(true);
                });
            }

            @Override
            public void onDownloadError(String errorMessage) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("下载失败: " + errorMessage);
                    statusLabel.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(AssignmentGradingPanel.this,
                            "文件下载失败: " + errorMessage, "错误", JOptionPane.ERROR_MESSAGE);
                    downloadFileButton.setEnabled(true);
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

    private void saveGrade() {
        if (selectedSubmission == null) {
            return;
        }

        try {
            int score = (Integer) scoreSpinner.getValue();
            String feedback = feedbackArea.getText().trim();

            statusLabel.setText("正在保存评分...");
            statusLabel.setForeground(Color.BLUE);
            saveGradeButton.setEnabled(false);

            boolean success = submissionService.gradeSubmission(
                    selectedSubmission.getSubmissionId(),
                    score,
                    feedback,
                    teacherId
            );

            if (success) {
                statusLabel.setText("评分保存成功！");
                statusLabel.setForeground(Color.GREEN);
                JOptionPane.showMessageDialog(this, "评分保存成功！", "成功", JOptionPane.INFORMATION_MESSAGE);

                // 刷新列表
                loadSubmissions();
            } else {
                statusLabel.setText("评分保存失败");
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "评分保存失败，请重试", "错误", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            statusLabel.setText("保存失败: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "保存失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            saveGradeButton.setEnabled(true);
        }
    }

    private String formatFileSize(Integer fileSize) {
        if (fileSize == null || fileSize <= 0) return "0 B";
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        return String.format("%.1f MB", fileSize / (1024.0 * 1024));
    }

    // 作业项包装类
    private class AssignmentItem {
        private Assignment assignment;

        public AssignmentItem(Assignment assignment) {
            this.assignment = assignment;
        }

        public Assignment getAssignment() {
            return assignment;
        }

        @Override
        public String toString() {
            return String.format("[%d] %s", assignment.getAssignmentId(), assignment.getTitle());
        }
    }

    // 提交记录表格模型
    private class SubmissionTableModel extends AbstractTableModel {
        private List<Submission> submissions;
        private String[] columnNames = {"ID", "学生", "提交时间", "状态", "成绩"};

        public void setSubmissions(List<Submission> submissions) {
            this.submissions = submissions;
        }

        public Submission getSubmissionAt(int rowIndex) {
            return submissions != null && rowIndex < submissions.size() ?
                    submissions.get(rowIndex) : null;
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
                case 1: return submission.getStudentName() != null ?
                        submission.getStudentName() : submission.getStudentId();
                case 2: return submission.getSubmitTime() != null ?
                        submission.getSubmitTime().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")) : "";
                case 3: return getStatusText(submission.getStatus());
                case 4: return submission.getScore() != null ? submission.getScore() + "分" : "未评分";
                default: return null;
            }
        }

        private String getStatusText(String status) {
            switch (status) {
                case "submitted": return "已提交";
                case "graded": return "已评分";
                case "revised": return "已修订";
                default: return status;
            }
        }
    }
}