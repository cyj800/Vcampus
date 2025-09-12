package UI;

import service.SubmissionService;
import model.Assignment;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AssignmentSubmitDialog extends JDialog {
    private Assignment assignment;
    private String studentId;
    private SubmissionService submissionService;

    // UI组件
    private JTextArea contentArea;
    private JTextField fileNameField;
    private JButton browseButton;
    private JButton submitButton;
    private JButton cancelButton;
    private JLabel fileInfoLabel;
    private JLabel deadlineLabel;
    private JLabel statusLabel;
    private JProgressBar uploadProgressBar;

    private File selectedFile;
    private boolean submitted = false;

    public AssignmentSubmitDialog(Frame parent, Assignment assignment, String studentId) {
        super(parent, "提交作业 - " + assignment.getTitle(), true);
        this.assignment = assignment;
        this.studentId = studentId;
        this.submissionService = new SubmissionService();

        initializeComponents();
        setupLayout();
        initializeData();
        setupEventListeners();

        setSize(600, 500);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeComponents() {
        // 文本内容区域
        contentArea = new JTextArea(12, 40);
        contentArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);

        // 文件选择组件
        fileNameField = new JTextField(30);
        fileNameField.setEditable(false);
        browseButton = new JButton("浏览文件");

        // 信息标签
        fileInfoLabel = new JLabel("未选择文件");
        deadlineLabel = new JLabel();
        statusLabel = new JLabel("就绪");
        statusLabel.setForeground(Color.BLUE);

        // 进度条
        uploadProgressBar = new JProgressBar();
        uploadProgressBar.setVisible(false);
        uploadProgressBar.setStringPainted(true);

        // 按钮
        submitButton = new JButton("提交作业");
        cancelButton = new JButton("取消");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // 顶部信息面板
        JPanel topPanel = new JPanel(new GridBagLayout());
        topPanel.setBorder(BorderFactory.createTitledBorder("作业信息"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("作业标题:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        topPanel.add(new JLabel("<html><b>" + assignment.getTitle() + "</b></html>"), gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        topPanel.add(new JLabel("截止时间:"), gbc);
        gbc.gridx = 1;
        deadlineLabel.setText(assignment.getDeadline().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")));
        deadlineLabel.setForeground(assignment.getDeadline().isBefore(LocalDateTime.now()) ?
                Color.RED : Color.BLACK);
        topPanel.add(deadlineLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        topPanel.add(new JLabel("提交类型:"), gbc);
        gbc.gridx = 1;
        topPanel.add(new JLabel(getSubmitTypeText()), gbc);

        if (assignment.getAllowedFileTypes() != null && !assignment.getAllowedFileTypes().isEmpty()) {
            gbc.gridx = 0; gbc.gridy = 3;
            topPanel.add(new JLabel("允许文件类型:"), gbc);
            gbc.gridx = 1;
            topPanel.add(new JLabel(assignment.getAllowedFileTypes()), gbc);
        }

        if (assignment.getMaxFileSize() != null && assignment.getMaxFileSize() > 0) {
            gbc.gridx = 0; gbc.gridy = 4;
            topPanel.add(new JLabel("文件大小限制:"), gbc);
            gbc.gridx = 1;
            topPanel.add(new JLabel(formatFileSize(assignment.getMaxFileSize())), gbc);
        }

        // 内容输入面板
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createTitledBorder("文字内容"));
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        contentPanel.add(contentScrollPane, BorderLayout.CENTER);

        // 文件上传面板
        JPanel filePanel = new JPanel(new GridBagLayout());
        filePanel.setBorder(BorderFactory.createTitledBorder("文件上传"));
        GridBagConstraints fileGbc = new GridBagConstraints();
        fileGbc.insets = new Insets(5, 5, 5, 5);
        fileGbc.anchor = GridBagConstraints.WEST;

        fileGbc.gridx = 0; fileGbc.gridy = 0;
        filePanel.add(new JLabel("选择文件:"), fileGbc);
        fileGbc.gridx = 1; fileGbc.weightx = 1.0; fileGbc.fill = GridBagConstraints.HORIZONTAL;
        filePanel.add(fileNameField, fileGbc);
        fileGbc.gridx = 2; fileGbc.weightx = 0; fileGbc.fill = GridBagConstraints.NONE;
        filePanel.add(browseButton, fileGbc);

        fileGbc.gridx = 1; fileGbc.gridy = 1; fileGbc.gridwidth = 2;
        filePanel.add(fileInfoLabel, fileGbc);

        fileGbc.gridy = 2;
        filePanel.add(uploadProgressBar, fileGbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(submitButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(statusLabel);

        // 主要内容区域
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(filePanel, BorderLayout.SOUTH);

        // 添加到对话框
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // 设置边框
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 根据提交类型控制组件可见性
        updateComponentVisibility();
    }

    private void initializeData() {
        // 根据作业的提交类型设置组件状态
        updateComponentVisibility();
    }

    private void setupEventListeners() {
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseFile();
            }
        });

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitAssignment();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void updateComponentVisibility() {
        String submitType = assignment.getSubmitType();
        boolean showText = !"file_only".equals(submitType);
        boolean showFile = !"text_only".equals(submitType);

        // 控制文字内容区域
        contentArea.setEnabled(showText);
        contentArea.setBackground(showText ? Color.WHITE : new Color(240, 240, 240));

        // 控制文件上传区域
        browseButton.setEnabled(showFile);
        fileNameField.setEnabled(showFile);
        fileInfoLabel.setEnabled(showFile);
    }

    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择要上传的文件");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            fileNameField.setText(selectedFile.getName());
            fileInfoLabel.setText("文件大小: " + formatFileSize((int) selectedFile.length()));

            // 验证文件类型
            if (assignment.getAllowedFileTypes() != null &&
                    !assignment.getAllowedFileTypes().trim().isEmpty()) {
                if (!isValidFileType(selectedFile.getName())) {
                    JOptionPane.showMessageDialog(this,
                            "不支持的文件类型。允许的类型: " + assignment.getAllowedFileTypes(),
                            "文件类型错误", JOptionPane.WARNING_MESSAGE);
                    selectedFile = null;
                    fileNameField.setText("");
                    fileInfoLabel.setText("未选择文件");
                    return;
                }
            }

            // 验证文件大小
            if (assignment.getMaxFileSize() != null && assignment.getMaxFileSize() > 0) {
                if (selectedFile.length() > assignment.getMaxFileSize()) {
                    JOptionPane.showMessageDialog(this,
                            "文件大小超出限制。最大允许: " + formatFileSize(assignment.getMaxFileSize()),
                            "文件大小错误", JOptionPane.WARNING_MESSAGE);
                    selectedFile = null;
                    fileNameField.setText("");
                    fileInfoLabel.setText("未选择文件");
                    return;
                }
            }
        }
    }

    private boolean isValidFileType(String fileName) {
        if (assignment.getAllowedFileTypes() == null ||
                assignment.getAllowedFileTypes().trim().isEmpty()) {
            return true;
        }

        String fileExtension = getFileExtension(fileName).toLowerCase();
        String[] allowedExtensions = assignment.getAllowedFileTypes().toLowerCase().split(",");

        for (String extension : allowedExtensions) {
            if (fileExtension.equals(extension.trim())) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    private void submitAssignment() {
        try {
            // 验证输入
            if (!validateInput()) {
                return;
            }

            // 检查是否已过截止时间
            if (assignment.getDeadline().isBefore(LocalDateTime.now())) {
                int result = JOptionPane.showConfirmDialog(this,
                        "作业已过截止时间，确定要提交吗？",
                        "截止时间提醒", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            statusLabel.setText("正在提交作业...");
            statusLabel.setForeground(Color.BLUE);
            submitButton.setEnabled(false);
            cancelButton.setEnabled(false);
            uploadProgressBar.setVisible(true);

            // 提交作业
            String content = contentArea.getText().trim();
            String fileName = selectedFile != null ? selectedFile.getName() : null;

            boolean success = submissionService.submitAssignment(
                    assignment.getAssignmentId(),
                    studentId,
                    content.isEmpty() ? null : content,
                    selectedFile,
                    fileName
            );

            if (success) {
                submitted = true;
                statusLabel.setText("作业提交成功！");
                statusLabel.setForeground(Color.GREEN);
                JOptionPane.showMessageDialog(this, "作业提交成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                statusLabel.setText("作业提交失败");
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "作业提交失败，请重试", "错误", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            statusLabel.setText("提交失败: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "提交失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            submitButton.setEnabled(true);
            cancelButton.setEnabled(true);
            uploadProgressBar.setVisible(false);
        }
    }

    private boolean validateInput() {
        String submitType = assignment.getSubmitType();

        // 检查文字内容（如果需要）
        if (!"file_only".equals(submitType)) {
            if (contentArea.getText().trim().isEmpty()) {
                // 如果是纯文字提交，必须有内容
                if ("text_only".equals(submitType)) {
                    JOptionPane.showMessageDialog(this, "请输入文字内容", "提示", JOptionPane.WARNING_MESSAGE);
                    contentArea.requestFocus();
                    return false;
                }
            }
        }

        // 检查文件（如果需要）
        if (!"text_only".equals(submitType)) {
            if (selectedFile == null) {
                // 如果是纯文件提交，必须有文件
                if ("file_only".equals(submitType)) {
                    JOptionPane.showMessageDialog(this, "请选择要上传的文件", "提示", JOptionPane.WARNING_MESSAGE);
                    browseButton.requestFocus();
                    return false;
                }
            }
        }

        return true;
    }

    private String getSubmitTypeText() {
        switch (assignment.getSubmitType()) {
            case "text_only": return "仅文字";
            case "file_only": return "仅文件";
            case "both": return "文字和文件";
            default: return assignment.getSubmitType();
        }
    }

    private String formatFileSize(int size) {
        if (size <= 0) return "0 B";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024));
    }

    // Getter方法
    public boolean isSubmitted() {
        return submitted;
    }
}