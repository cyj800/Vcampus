package UI;

import service.AppealService;
import model.Appeal;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AppealManagementPanel extends JPanel {
    private String userType; // "admin" 或 "teacher"
    private String userId;
    private AppealService appealService;

    // UI组件
    private JTable appealTable;
    private AppealTableModel tableModel;
    private JTextArea appealDetailArea;
    private JTextArea responseArea;
    private JButton approveButton;
    private JButton rejectButton;
    private JButton resolveButton;
    private JButton refreshButton;
    private JComboBox<String> statusFilterCombo;
    private JLabel statusLabel;

    private Appeal selectedAppeal;

    public AppealManagementPanel(String userType, String userId) {
        this.userType = userType;
        this.userId = userId;
        this.appealService = new AppealService();

        initializeComponents();
        setupLayout();
        loadAppeals();
    }

    private void initializeComponents() {
        // 表格模型和表格
        tableModel = new AppealTableModel();
        appealTable = new JTable(tableModel);
        appealTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appealTable.setRowHeight(25);
        appealTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = appealTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        selectedAppeal = tableModel.getAppealAt(selectedRow);
                        showAppealDetails(selectedAppeal);
                    }
                }
            }
        });

        // 详细信息区域
        appealDetailArea = new JTextArea(8, 40);
        appealDetailArea.setEditable(false);
        appealDetailArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        appealDetailArea.setLineWrap(true);
        appealDetailArea.setWrapStyleWord(true);

        // 处理回复区域
        responseArea = new JTextArea(5, 40);
        responseArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        responseArea.setEnabled(false);

        // 状态筛选下拉框
        statusFilterCombo = new JComboBox<>(new String[]{"全部", "待处理", "已批准", "已拒绝", "已解决"});
        statusFilterCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterAppeals();
            }
        });

        // 按钮组件
        approveButton = new JButton("批准申诉");
        approveButton.setEnabled(false);
        rejectButton = new JButton("拒绝申诉");
        rejectButton.setEnabled(false);
        resolveButton = new JButton("标记为已解决");
        resolveButton.setEnabled(false);
        refreshButton = new JButton("刷新");
        statusLabel = new JLabel("就绪");
        statusLabel.setForeground(Color.BLUE);

        // 按钮事件监听
        approveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAppeal("approved");
            }
        });

        rejectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAppeal("rejected");
            }
        });

        resolveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAppeal("resolved");
            }
        });

        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAppeals();
            }
        });
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 顶部控制面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("状态筛选:"));
        topPanel.add(statusFilterCombo);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(refreshButton);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(statusLabel);

        // 主要内容区域 - 分割面板
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setDividerLocation(500);

        // 左侧面板 - 申诉列表
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("申诉列表"));
        JScrollPane tableScrollPane = new JScrollPane(appealTable);
        leftPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainSplitPane.setLeftComponent(leftPanel);

        // 右侧面板 - 详细信息和处理
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));

        // 申诉详细信息
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBorder(BorderFactory.createTitledBorder("申诉详情"));
        JScrollPane detailScrollPane = new JScrollPane(appealDetailArea);
        detailPanel.add(detailScrollPane, BorderLayout.CENTER);

        // 处理回复区域
        JPanel responsePanel = new JPanel(new BorderLayout());
        responsePanel.setBorder(BorderFactory.createTitledBorder("处理回复"));
        JScrollPane responseScrollPane = new JScrollPane(responseArea);
        responsePanel.add(responseScrollPane, BorderLayout.CENTER);

        // 处理按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(approveButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(rejectButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(resolveButton);

        // 右侧面板布局
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setTopComponent(detailPanel);
        rightSplitPane.setBottomComponent(responsePanel);
        rightSplitPane.setDividerLocation(200);

        JPanel rightMainPanel = new JPanel(new BorderLayout());
        rightMainPanel.add(rightSplitPane, BorderLayout.CENTER);
        rightMainPanel.add(buttonPanel, BorderLayout.SOUTH);

        rightPanel.add(rightMainPanel, BorderLayout.CENTER);

        mainSplitPane.setRightComponent(rightPanel);

        // 添加到主面板
        add(topPanel, BorderLayout.NORTH);
        add(mainSplitPane, BorderLayout.CENTER);
    }

    private void loadAppeals() {
        try {
            statusLabel.setText("正在加载申诉记录...");
            statusLabel.setForeground(Color.BLUE);

            List<Appeal> appeals;
            if ("admin".equals(userType)) {
                // 管理员加载所有待处理申诉
                appeals = appealService.getPendingAppeals();
            } else {
                // 教师加载相关申诉（这里简化处理）
                appeals = appealService.getAllAppeals();
            }

            tableModel.setAppeals(appeals);
            tableModel.fireTableDataChanged();

            statusLabel.setText("加载完成，共 " + appeals.size() + " 个申诉");
            statusLabel.setForeground(Color.GREEN);

            // 清空详细信息
            clearAppealDetails();

        } catch (Exception e) {
            statusLabel.setText("加载失败: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "加载申诉记录失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterAppeals() {
        try {
            String selectedStatus = (String) statusFilterCombo.getSelectedItem();
            List<Appeal> appeals;

            switch (selectedStatus) {
                case "待处理":
                    appeals = appealService.getAppealsByStatus("pending");
                    break;
                case "已批准":
                    appeals = appealService.getAppealsByStatus("approved");
                    break;
                case "已拒绝":
                    appeals = appealService.getAppealsByStatus("rejected");
                    break;
                case "已解决":
                    appeals = appealService.getAppealsByStatus("resolved");
                    break;
                case "全部":
                default:
                    if ("admin".equals(userType)) {
                        appeals = appealService.getPendingAppeals();
                    } else {
                        appeals = appealService.getAllAppeals();
                    }
                    break;
            }

            tableModel.setAppeals(appeals);
            tableModel.fireTableDataChanged();
            clearAppealDetails();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "筛选失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAppealDetails(Appeal appeal) {
        this.selectedAppeal = appeal;

        // 显示申诉详细信息
        StringBuilder detail = new StringBuilder();
        detail.append("=== 申诉基本信息 ===\n\n");
        detail.append("申诉ID: ").append(appeal.getAppealId()).append("\n");
        detail.append("作业标题: ").append(appeal.getAssignmentTitle()).append("\n");
        detail.append("课程名称: ").append(appeal.getCourseName()).append("\n");
        detail.append("学生姓名: ").append(appeal.getStudentNickname()).append("\n");
        detail.append("学生ID: ").append(appeal.getStudentId()).append("\n");
        detail.append("提交时间: ").append(appeal.getCreatedAt()
                .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))).append("\n");
        detail.append("当前状态: ").append(getStatusText(appeal.getStatus())).append("\n\n");

        detail.append("=== 申诉理由 ===\n");
        detail.append(appeal.getReason()).append("\n\n");

        if (appeal.getResponse() != null && !appeal.getResponse().isEmpty()) {
            detail.append("=== 处理回复 ===\n");
            detail.append(appeal.getResponse()).append("\n\n");
        }

        if (appeal.getHandlerId() != null && !appeal.getHandlerId().isEmpty()) {
            detail.append("处理人: ").append(appeal.getHandlerId()).append("\n");
        }

        if (appeal.getHandledAt() != null) {
            detail.append("处理时间: ").append(appeal.getHandledAt()
                    .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))).append("\n");
        }

        appealDetailArea.setText(detail.toString());
        appealDetailArea.setCaretPosition(0);

        // 设置回复区域
        responseArea.setText(appeal.getResponse() != null ? appeal.getResponse() : "");
        responseArea.setEnabled(appeal.isPending());

        // 更新按钮状态
        updateButtonStates(appeal);
    }

    private void clearAppealDetails() {
        selectedAppeal = null;
        appealDetailArea.setText("");
        responseArea.setText("");
        responseArea.setEnabled(false);
        approveButton.setEnabled(false);
        rejectButton.setEnabled(false);
        resolveButton.setEnabled(false);
    }

    private void updateButtonStates(Appeal appeal) {
        if (appeal == null) {
            approveButton.setEnabled(false);
            rejectButton.setEnabled(false);
            resolveButton.setEnabled(false);
            return;
        }

        if (appeal.isPending()) {
            approveButton.setEnabled(true);
            rejectButton.setEnabled(true);
            resolveButton.setEnabled(false);
        } else if (appeal.isApproved() || appeal.isRejected()) {
            approveButton.setEnabled(false);
            rejectButton.setEnabled(false);
            resolveButton.setEnabled(true);
        } else {
            approveButton.setEnabled(false);
            rejectButton.setEnabled(false);
            resolveButton.setEnabled(false);
        }
    }

    private void handleAppeal(String action) {
        if (selectedAppeal == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个申诉记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String response = responseArea.getText().trim();
            if (response.isEmpty()) {
                response = getDefaultResponse(action);
            }

            statusLabel.setText("正在处理申诉...");
            statusLabel.setForeground(Color.BLUE);

            boolean success = false;
            switch (action) {
                case "approved":
                    success = appealService.handleAppeal(selectedAppeal.getAppealId(),
                            response, "approved", userId);
                    break;
                case "rejected":
                    success = appealService.handleAppeal(selectedAppeal.getAppealId(),
                            response, "rejected", userId);
                    break;
                case "resolved":
                    success = appealService.handleAppeal(selectedAppeal.getAppealId(),
                            response, "resolved", userId);
                    break;
            }

            if (success) {
                statusLabel.setText("申诉处理成功！");
                statusLabel.setForeground(Color.GREEN);
                JOptionPane.showMessageDialog(this, "申诉处理成功！", "成功", JOptionPane.INFORMATION_MESSAGE);

                // 刷新列表
                loadAppeals();
            } else {
                statusLabel.setText("申诉处理失败");
                statusLabel.setForeground(Color.RED);
                JOptionPane.showMessageDialog(this, "申诉处理失败，请重试", "错误", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            statusLabel.setText("处理失败: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "处理失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getDefaultResponse(String action) {
        switch (action) {
            case "approved":
                return "申诉已批准，将重新审核相关作业。";
            case "rejected":
                return "经审核，申诉理由不充分，维持原成绩不变。";
            case "resolved":
                return "申诉处理已完成。";
            default:
                return "已处理。";
        }
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending": return "待处理";
            case "approved": return "已批准";
            case "rejected": return "已拒绝";
            case "resolved": return "已解决";
            default: return status;
        }
    }

    // 申诉表格模型
    private class AppealTableModel extends AbstractTableModel {
        private List<Appeal> appeals;
        private String[] columnNames = {"ID", "学生", "作业", "课程", "状态", "提交时间"};

        public void setAppeals(List<Appeal> appeals) {
            this.appeals = appeals;
        }

        public Appeal getAppealAt(int rowIndex) {
            return appeals != null && rowIndex < appeals.size() ? appeals.get(rowIndex) : null;
        }

        @Override
        public int getRowCount() {
            return appeals != null ? appeals.size() : 0;
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
            if (appeals == null || rowIndex >= appeals.size()) {
                return null;
            }

            Appeal appeal = appeals.get(rowIndex);
            switch (columnIndex) {
                case 0: return appeal.getAppealId();
                case 1: return appeal.getStudentNickname();
                case 2: return appeal.getAssignmentTitle();
                case 3: return appeal.getCourseName();
                case 4: return getStatusText(appeal.getStatus());
                case 5: return appeal.getCreatedAt().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
                default: return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Integer.class;
            }
            return String.class;
        }
    }
}