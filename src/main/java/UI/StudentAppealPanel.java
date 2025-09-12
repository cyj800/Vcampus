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

public class StudentAppealPanel extends JPanel {
    private String studentId;
    private AppealService appealService;

    // UI组件
    private JTable appealTable;
    private AppealTableModel tableModel;
    private JTextArea appealDetailArea;
    private JButton refreshButton;
    private JButton viewDetailButton;
    private JComboBox<String> statusFilterCombo;
    private JLabel statusLabel;

    public StudentAppealPanel(String studentId) {
        this.studentId = studentId;
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
                    updateDetailButtonState();
                }
            }
        });

        // 详细信息区域
        appealDetailArea = new JTextArea(10, 40);
        appealDetailArea.setEditable(false);
        appealDetailArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        appealDetailArea.setLineWrap(true);
        appealDetailArea.setWrapStyleWord(true);

        // 状态筛选下拉框
        statusFilterCombo = new JComboBox<>(new String[]{"全部", "待处理", "已批准", "已拒绝", "已解决"});
        statusFilterCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterAppeals();
            }
        });

        // 按钮组件
        refreshButton = new JButton("刷新");
        viewDetailButton = new JButton("查看详情");
        viewDetailButton.setEnabled(false);
        statusLabel = new JLabel("就绪");
        statusLabel.setForeground(Color.BLUE);

        // 按钮事件监听
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadAppeals();
            }
        });

        viewDetailButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showSelectedAppealDetail();
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
        mainSplitPane.setDividerLocation(400);

        // 左侧面板 - 申诉列表
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("我的申诉记录"));
        JScrollPane tableScrollPane = new JScrollPane(appealTable);
        leftPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainSplitPane.setLeftComponent(leftPanel);

        // 右侧面板 - 详细信息
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("申诉详情"));
        JScrollPane detailScrollPane = new JScrollPane(appealDetailArea);
        rightPanel.add(detailScrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(viewDetailButton);

        rightPanel.add(buttonPanel, BorderLayout.SOUTH);
        mainSplitPane.setRightComponent(rightPanel);

        // 添加到主面板
        add(topPanel, BorderLayout.NORTH);
        add(mainSplitPane, BorderLayout.CENTER);
    }

    private void loadAppeals() {
        try {
            statusLabel.setText("正在加载申诉记录...");
            statusLabel.setForeground(Color.BLUE);

            List<Appeal> appeals = appealService.getStudentAppeals(studentId);
            tableModel.setAppeals(appeals);
            tableModel.fireTableDataChanged();

            statusLabel.setText("加载完成，共 " + appeals.size() + " 个申诉");
            statusLabel.setForeground(Color.GREEN);

            // 清空详细信息
            appealDetailArea.setText("");
            updateDetailButtonState();

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
            List<Appeal> allAppeals = appealService.getStudentAppeals(studentId);
            List<Appeal> filteredAppeals;

            switch (selectedStatus) {
                case "待处理":
                    filteredAppeals = filterAppealsByStatus(allAppeals, "pending");
                    break;
                case "已批准":
                    filteredAppeals = filterAppealsByStatus(allAppeals, "approved");
                    break;
                case "已拒绝":
                    filteredAppeals = filterAppealsByStatus(allAppeals, "rejected");
                    break;
                case "已解决":
                    filteredAppeals = filterAppealsByStatus(allAppeals, "resolved");
                    break;
                case "全部":
                default:
                    filteredAppeals = allAppeals;
                    break;
            }

            tableModel.setAppeals(filteredAppeals);
            tableModel.fireTableDataChanged();
            appealDetailArea.setText("");
            updateDetailButtonState();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "筛选失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<Appeal> filterAppealsByStatus(List<Appeal> appeals, String status) {
        java.util.List<Appeal> filtered = new java.util.ArrayList<>();
        for (Appeal appeal : appeals) {
            if (status.equals(appeal.getStatus())) {
                filtered.add(appeal);
            }
        }
        return filtered;
    }

    private void updateDetailButtonState() {
        viewDetailButton.setEnabled(appealTable.getSelectedRow() >= 0);
    }

    private void showSelectedAppealDetail() {
        int selectedRow = appealTable.getSelectedRow();
        if (selectedRow >= 0) {
            Appeal appeal = tableModel.getAppealAt(selectedRow);
            showAppealDetails(appeal);
        }
    }

    private void showAppealDetails(Appeal appeal) {
        StringBuilder detail = new StringBuilder();
        detail.append("=== 申诉基本信息 ===\n\n");
        detail.append("申诉ID: ").append(appeal.getAppealId()).append("\n");
        detail.append("作业标题: ").append(appeal.getAssignmentTitle()).append("\n");
        detail.append("课程名称: ").append(appeal.getCourseName()).append("\n");
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
        private String[] columnNames = {"ID", "作业", "课程", "状态", "提交时间"};

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
                case 1: return appeal.getAssignmentTitle();
                case 2: return appeal.getCourseName();
                case 3: return getStatusText(appeal.getStatus());
                case 4: return appeal.getCreatedAt().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
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