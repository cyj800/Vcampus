package UI;

import service.AppealService;
import model.Submission;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;

public class AppealSubmitDialog extends JDialog {
    private Submission submission;
    private String studentId;
    private AppealService appealService;

    // UI组件
    private JLabel assignmentLabel;
    private JLabel courseLabel;
    private JLabel scoreLabel;
    private JLabel submitTimeLabel;
    private JTextArea reasonArea;
    private JTextArea additionalInfoArea;
    private JButton submitButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private JProgressBar submitProgressBar;

    private boolean appealSubmitted = false;

    public AppealSubmitDialog(Frame parent, Submission submission, String studentId) {
        super(parent, "提交申诉 - " + submission.getAssignmentTitle(), true);
        this.submission = submission;
        this.studentId = studentId;
        this.appealService = new AppealService();

        initializeComponents();
        setupLayout();
        initializeData();
        setupEventListeners();

        setSize(550, 500);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initializeComponents() {
        // 作业信息标签
        assignmentLabel = new JLabel();
        courseLabel = new JLabel();
        scoreLabel = new JLabel();
        submitTimeLabel = new JLabel();

        // 申诉原因输入区域
        reasonArea = new JTextArea(8, 40);
        reasonArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(BorderFactory.createLoweredBevelBorder());

        // 补充信息区域
        additionalInfoArea = new JTextArea(4, 40);
        additionalInfoArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        additionalInfoArea.setLineWrap(true);
        additionalInfoArea.setWrapStyleWord(true);
        additionalInfoArea.setBorder(BorderFactory.createLoweredBevelBorder());

        // 状态标签和进度条
        statusLabel = new JLabel("请填写申诉理由");
        statusLabel.setForeground(Color.BLUE);
        submitProgressBar = new JProgressBar();
        submitProgressBar.setVisible(false);
        submitProgressBar.setStringPainted(true);

        // 按钮
        submitButton = new JButton("提交申诉");
        cancelButton = new JButton("取消");
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // 顶部信息面板
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("作业信息"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        infoPanel.add(new JLabel("作业标题:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        infoPanel.add(assignmentLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        infoPanel.add(new JLabel("课程名称:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(courseLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        infoPanel.add(new JLabel("作业成绩:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(scoreLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        infoPanel.add(new JLabel("提交时间:"), gbc);
        gbc.gridx = 1;
        infoPanel.add(submitTimeLabel, gbc);

        // 申诉原因面板
        JPanel reasonPanel = new JPanel(new BorderLayout());
        reasonPanel.setBorder(BorderFactory.createTitledBorder("申诉理由 *"));
        reasonPanel.add(new JLabel("请详细说明您对成绩的异议和申诉理由："), BorderLayout.NORTH);
        JScrollPane reasonScrollPane = new JScrollPane(reasonArea);
        reasonPanel.add(reasonScrollPane, BorderLayout.CENTER);
        reasonPanel.add(new JLabel("<html><font color='gray'>请详细描述具体问题，这将有助于教师重新审核您的作业。</font></html>"),
                BorderLayout.SOUTH);

        // 补充信息面板
        JPanel additionalPanel = new JPanel(new BorderLayout());
        additionalPanel.setBorder(BorderFactory.createTitledBorder("补充信息（可选）"));
        additionalPanel.add(new JLabel("如有其他相关信息，请在此补充："), BorderLayout.NORTH);
        JScrollPane additionalScrollPane = new JScrollPane(additionalInfoArea);
        additionalPanel.add(additionalScrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(submitButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(statusLabel);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(submitProgressBar);

        // 主要内容区域
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(reasonPanel, BorderLayout.CENTER);
        mainPanel.add(additionalPanel, BorderLayout.SOUTH);

        // 添加到对话框
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // 设置边框
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }

    private void initializeData() {
        // 初始化作业信息显示
        assignmentLabel.setText("<html><b>" + submission.getAssignmentTitle() + "</b></html>");
        courseLabel.setText(submission.getCourseName());
        scoreLabel.setText(submission.getScore() != null ? submission.getScore() + " 分" : "未评分");
        submitTimeLabel.setText(submission.getSubmitTime() != null ?
                submission.getSubmitTime().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")) : "未知");

        // 设置焦点到申诉原因区域
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                reasonArea.requestFocusInWindow();
            }
        });
    }

    private void setupEventListeners() {
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                submitAppeal();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // 添加文本变化监听器
        reasonArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateSubmitButtonState();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateSubmitButtonState();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateSubmitButtonState();
            }
        });
    }

    private void updateSubmitButtonState() {
        submitButton.setEnabled(!reasonArea.getText().trim().isEmpty());
    }

    private void submitAppeal() {
        try {
            // 验证输入
            if (!validateInput()) {
                return;
            }

            // 确认提交
            int result = JOptionPane.showConfirmDialog(this,
                    "确定要提交申诉吗？提交后不可修改申诉内容。",
                    "确认提交", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result != JOptionPane.YES_OPTION) {
                return;
            }

            statusLabel.setText("正在提交申诉...");
            statusLabel.setForeground(Color.BLUE);
            submitButton.setEnabled(false);
            cancelButton.setEnabled(false);
            submitProgressBar.setVisible(true);

            // 构造申诉理由（合并主要原因和补充信息）
            StringBuilder fullReason = new StringBuilder();
            fullReason.append(reasonArea.getText().trim());

            String additionalInfo = additionalInfoArea.getText().trim();
            if (!additionalInfo.isEmpty()) {
                fullReason.append("\n\n补充信息：\n").append(additionalInfo);
            }

            // 提交申诉
            boolean success = appealService.createAppeal(
                    submission.getSubmissionId(),
                    studentId,
                    fullReason.toString()
            );

            if (success) {
                appealSubmitted = true;
                statusLabel.setText("申诉提交成功！");
                statusLabel.setForeground(Color.GREEN);
                JOptionPane.showMessageDialog(this,
                        "<html><b>申诉提交成功！</b><br><br>" +
                                "您的申诉已成功提交，教师将在3个工作日内处理。<br>" +
                                "您可以在\"我的申诉\"中查看处理进度。</html>",
                        "提交成功", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                statusLabel.setText("申诉提交失败");
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "申诉提交失败，请重试", "错误", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            statusLabel.setText("提交失败: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "提交失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            submitButton.setEnabled(true);
            cancelButton.setEnabled(true);
            submitProgressBar.setVisible(false);
        }
    }

    private boolean validateInput() {
        String reason = reasonArea.getText().trim();

        // 检查申诉理由是否为空
        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入申诉理由", "提示", JOptionPane.WARNING_MESSAGE);
            reasonArea.requestFocus();
            return false;
        }

        // 检查申诉理由长度
        if (reason.length() < 10) {
            JOptionPane.showMessageDialog(this, "申诉理由过于简单，请详细说明申诉原因（至少10个字符）",
                    "提示", JOptionPane.WARNING_MESSAGE);
            reasonArea.requestFocus();
            return false;
        }

        // 检查是否包含敏感词汇（简化示例）
        String[] sensitiveWords = {"骂", "shit", "fuck", "傻"};
        for (String word : sensitiveWords) {
            if (reason.toLowerCase().contains(word.toLowerCase())) {
                JOptionPane.showMessageDialog(this, "申诉理由包含不当内容，请修改后重新提交",
                        "内容审核", JOptionPane.WARNING_MESSAGE);
                reasonArea.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void previewAppeal() {
        StringBuilder preview = new StringBuilder();
        preview.append("=== 申诉预览 ===\n\n");
        preview.append("作业标题: ").append(submission.getAssignmentTitle()).append("\n");
        preview.append("课程名称: ").append(submission.getCourseName()).append("\n");
        preview.append("当前成绩: ").append(submission.getScore() != null ?
                submission.getScore() + "分" : "未评分").append("\n\n");

        preview.append("申诉理由:\n");
        preview.append(reasonArea.getText().trim()).append("\n\n");

        if (!additionalInfoArea.getText().trim().isEmpty()) {
            preview.append("补充信息:\n");
            preview.append(additionalInfoArea.getText().trim()).append("\n");
        }

        JTextArea previewArea = new JTextArea(preview.toString());
        previewArea.setEditable(false);
        previewArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(previewArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "申诉预览", JOptionPane.INFORMATION_MESSAGE);
    }

    // Getter方法
    public boolean isAppealSubmitted() {
        return appealSubmitted;
    }

    // 添加预览按钮（可选功能）
    private void addPreviewButton() {
        JButton previewButton = new JButton("预览");
        previewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!reasonArea.getText().trim().isEmpty()) {
                    previewAppeal();
                } else {
                    JOptionPane.showMessageDialog(AppealSubmitDialog.this,
                            "请先填写申诉理由", "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // 找到按钮面板并添加预览按钮
        Component[] components = ((JPanel) getContentPane().getComponent(1)).getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                JPanel buttonPanel = (JPanel) component;
                buttonPanel.add(Box.createHorizontalStrut(10));
                buttonPanel.add(previewButton, 0); // 添加到开头
                break;
            }
        }
    }
}