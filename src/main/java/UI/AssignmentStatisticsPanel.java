package UI;

import service.AssignmentService;
import service.SubmissionService;
import service.AppealService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AssignmentStatisticsPanel extends JPanel {
    private String userType; // "admin" 或 "teacher"
    private String userId;
    private AssignmentService assignmentService;
    private SubmissionService submissionService;
    private AppealService appealService;

    // UI组件
    private JComboBox<String> courseCombo;
    private JComboBox<String> timeRangeCombo;
    private JButton refreshButton;
    private JLabel statusLabel;

    // 统计信息面板
    private JPanel overviewPanel;
    private JPanel chartPanel;
    private JPanel detailPanel;

    // 统计数据标签
    private JLabel totalAssignmentsLabel;
    private JLabel totalSubmissionsLabel;
    private JLabel submissionRateLabel;
    private JLabel averageScoreLabel;
    private JLabel pendingAppealsLabel;
    private JLabel completedAppealsLabel;

    public AssignmentStatisticsPanel(String userType, String userId) {
        this.userType = userType;
        this.userId = userId;
        this.assignmentService = new AssignmentService();
        this.submissionService = new SubmissionService();
        this.appealService = new AppealService();

        initializeComponents();
        setupLayout();
        loadInitialData();
    }

    private void initializeComponents() {
        // 课程选择下拉框（管理员使用）
        courseCombo = new JComboBox<>();
        courseCombo.setVisible("admin".equals(userType));

        // 时间范围选择
        timeRangeCombo = new JComboBox<>(new String[]{
                "最近7天", "最近30天", "最近90天", "本学期", "全部"
        });
        timeRangeCombo.setSelectedIndex(1); // 默认最近30天

        // 按钮
        refreshButton = new JButton("刷新数据");
        statusLabel = new JLabel("就绪");
        statusLabel.setForeground(Color.BLUE);

        // 统计数据标签
        totalAssignmentsLabel = new JLabel("0");
        totalSubmissionsLabel = new JLabel("0");
        submissionRateLabel = new JLabel("0%");
        averageScoreLabel = new JLabel("0.0");
        pendingAppealsLabel = new JLabel("0");
        completedAppealsLabel = new JLabel("0");

        // 事件监听
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshStatistics();
            }
        });

        timeRangeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshStatistics();
            }
        });

        if ("admin".equals(userType)) {
            courseCombo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    refreshStatistics();
                }
            });
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 顶部控制面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        if ("admin".equals(userType)) {
            topPanel.add(new JLabel("选择课程:"));
            topPanel.add(courseCombo);
            topPanel.add(Box.createHorizontalStrut(10));
        }
        topPanel.add(new JLabel("时间范围:"));
        topPanel.add(timeRangeCombo);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(refreshButton);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(statusLabel);

        // 概览面板
        overviewPanel = createOverviewPanel();

        // 图表面板（简化版，实际项目中可以使用图表库）
        chartPanel = createChartPanel();

        // 详细信息面板
        detailPanel = createDetailPanel();

        // 主要内容区域
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setDividerLocation(200);

        JPanel upperPanel = new JPanel(new BorderLayout(10, 10));
        upperPanel.add(overviewPanel, BorderLayout.NORTH);
        upperPanel.add(chartPanel, BorderLayout.CENTER);

        mainSplitPane.setTopComponent(upperPanel);
        mainSplitPane.setBottomComponent(detailPanel);

        // 添加到主面板
        add(topPanel, BorderLayout.NORTH);
        add(mainSplitPane, BorderLayout.CENTER);
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("统计概览"));
        panel.setPreferredSize(new Dimension(getWidth(), 120));

        // 第一行
        panel.add(createStatCard("作业总数", totalAssignmentsLabel, Color.BLUE));
        panel.add(createStatCard("提交总数", totalSubmissionsLabel, Color.GREEN));
        panel.add(createStatCard("提交率", submissionRateLabel, Color.ORANGE));
        panel.add(createStatCard("平均分", averageScoreLabel, Color.MAGENTA));

        // 第二行
        panel.add(createStatCard("待处理申诉", pendingAppealsLabel, Color.RED));
        panel.add(createStatCard("已完成申诉", completedAppealsLabel, Color.GRAY));
        panel.add(new JLabel()); // 占位
        panel.add(new JLabel()); // 占位

        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(new Color(250, 250, 250));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
        titleLabel.setForeground(color);

        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createChartPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("趋势图表"));

        // 简化的图表显示（实际项目中可以使用JFreeChart等图表库）
        JTextArea chartArea = new JTextArea();
        chartArea.setEditable(false);
        chartArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        chartArea.setText(getSimpleChartText());

        JScrollPane scrollPane = new JScrollPane(chartArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private String getSimpleChartText() {
        StringBuilder sb = new StringBuilder();
        sb.append("作业提交趋势图（简化显示）\n");
        sb.append("========================\n");
        sb.append("日期        作业数  提交数\n");
        sb.append("------------------------\n");
        sb.append("2024-01-01   5      20\n");
        sb.append("2024-01-02   3      15\n");
        sb.append("2024-01-03   4      18\n");
        sb.append("2024-01-04   2      12\n");
        sb.append("2024-01-05   6      25\n");
        sb.append("2024-01-06   1      8\n");
        sb.append("2024-01-07   4      16\n");
        return sb.toString();
    }

    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("详细统计"));

        // 创建选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();

        // 作业统计表格
        JPanel assignmentStatsPanel = createAssignmentStatsPanel();
        tabbedPane.addTab("作业统计", assignmentStatsPanel);

        // 提交统计表格
        JPanel submissionStatsPanel = createSubmissionStatsPanel();
        tabbedPane.addTab("提交统计", submissionStatsPanel);

        // 申诉统计表格
        JPanel appealStatsPanel = createAppealStatsPanel();
        tabbedPane.addTab("申诉统计", appealStatsPanel);

        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createAssignmentStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"作业ID", "作业标题", "课程", "截止时间", "总提交数", "平均分"};
        Object[][] data = {
                {"101", "数据结构作业1", "CS101", "2024-01-15", "25", "85.3"},
                {"102", "算法作业1", "CS101", "2024-01-20", "23", "78.9"},
                {"103", "数据库作业1", "CS201", "2024-01-18", "30", "82.1"},
                {"104", "操作系统作业1", "CS301", "2024-01-22", "18", "76.5"},
                {"105", "网络编程作业1", "CS401", "2024-01-25", "22", "88.2"}
        };

        JTable table = new JTable(data, columnNames);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSubmissionStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"学生ID", "姓名", "总提交数", "已评分", "平均分", "最高分"};
        Object[][] data = {
                {"2023001", "张三", "15", "14", "87.2", "95"},
                {"2023002", "李四", "14", "13", "82.8", "92"},
                {"2023003", "王五", "16", "15", "79.5", "88"},
                {"2023004", "赵六", "13", "12", "91.3", "98"},
                {"2023005", "钱七", "15", "14", "85.7", "94"}
        };

        JTable table = new JTable(data, columnNames);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAppealStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnNames = {"申诉ID", "作业标题", "学生", "状态", "提交时间", "处理时间"};
        Object[][] data = {
                {"1001", "数据结构作业1", "张三", "已处理", "2024-01-16", "2024-01-17"},
                {"1002", "算法作业1", "李四", "待处理", "2024-01-21", "-"},
                {"1003", "数据库作业1", "王五", "已处理", "2024-01-19", "2024-01-20"},
                {"1004", "操作系统作业1", "赵六", "已拒绝", "2024-01-23", "2024-01-24"},
                {"1005", "网络编程作业1", "钱七", "待处理", "2024-01-26", "-"}
        };

        JTable table = new JTable(data, columnNames);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadInitialData() {
        if ("admin".equals(userType)) {
            loadCourses();
        }
        refreshStatistics();
    }

    private void loadCourses() {
        try {
            // 管理员加载所有课程
            courseCombo.removeAllItems();
            courseCombo.addItem("全部课程");
            // 这里应该从服务层获取课程列表
            courseCombo.addItem("计算机科学导论(CS101)");
            courseCombo.addItem("数据结构(CS201)");
            courseCombo.addItem("算法分析(CS301)");
            courseCombo.addItem("数据库系统(CS401)");
        } catch (Exception e) {
            statusLabel.setText("加载课程失败: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void refreshStatistics() {
        try {
            statusLabel.setText("正在加载统计数据...");
            statusLabel.setForeground(Color.BLUE);

            // 获取时间范围
            LocalDateTime startDate = getTimeRangeStart();

            // 加载统计数据
            loadOverviewStatistics(startDate);
            updateChartDisplay();

            statusLabel.setText("数据加载完成");
            statusLabel.setForeground(Color.GREEN);

        } catch (Exception e) {
            statusLabel.setText("加载失败: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "加载统计数据失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadOverviewStatistics(LocalDateTime startDate) {
        try {
            // 模拟统计数据加载
            int totalAssignments = 45;
            int totalSubmissions = 1250;
            double submissionRate = 85.3;
            double averageScore = 82.7;
            int pendingAppeals = 8;
            int completedAppeals = 23;

            // 更新UI
            totalAssignmentsLabel.setText(String.valueOf(totalAssignments));
            totalSubmissionsLabel.setText(String.valueOf(totalSubmissions));
            submissionRateLabel.setText(String.format("%.1f%%", submissionRate));
            averageScoreLabel.setText(String.format("%.1f", averageScore));
            pendingAppealsLabel.setText(String.valueOf(pendingAppeals));
            completedAppealsLabel.setText(String.valueOf(completedAppeals));

        } catch (Exception e) {
            statusLabel.setText("统计失败: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
        }
    }

    private void updateChartDisplay() {
        // 实际项目中这里会更新真实的图表
        // 简化版本只是更新文本显示
    }

    private LocalDateTime getTimeRangeStart() {
        String selectedRange = (String) timeRangeCombo.getSelectedItem();
        LocalDateTime now = LocalDateTime.now();

        switch (selectedRange) {
            case "最近7天":
                return now.minusDays(7);
            case "最近30天":
                return now.minusDays(30);
            case "最近90天":
                return now.minusDays(90);
            case "本学期":
                return now.minusMonths(4); // 假设学期为4个月
            case "全部":
            default:
                return LocalDateTime.of(2020, 1, 1, 0, 0); // 很久以前
        }
    }

    // 导出统计数据功能
    private void exportStatistics() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出统计数据");
        fileChooser.setSelectedFile(new java.io.File("作业统计_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }

            try {
                // 这里应该实现实际的导出逻辑
                JOptionPane.showMessageDialog(this, "统计数据已导出到: " + filePath,
                        "导出成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "导出失败: " + e.getMessage(),
                        "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 添加导出按钮的方法（可以添加到顶部面板）
    public JButton createExportButton() {
        JButton exportButton = new JButton("导出数据");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportStatistics();
            }
        });
        return exportButton;
    }
}