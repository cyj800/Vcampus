package UI;

import database.StatisticsDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StatisticsAnalysisPanel extends JPanel {
    private StatisticsDAO statisticsDAO;
    private String currentUsername;

    // UI组件
    private JComboBox<String> timeRangeCombo;
    private JButton refreshButton;
    private JLabel statusLabel;

    // 关键指标面板组件
    private JPanel summaryPanel;
    private JLabel totalAssignmentsValue;    // 作业总数
    private JLabel totalSubmissionsValue;    // 提交总数
    private JLabel completionRateValue;      // 完成率
    private JLabel averageScoreValue;        // 平均分
    private JLabel pendingAppealsValue;      // 待处理申诉
    private JLabel processedAppealsValue;    // 已处理申诉

    // 数据表格
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private JTextArea analysisResultArea;

    public StatisticsAnalysisPanel() {
        this.statisticsDAO = new StatisticsDAO();
        this.currentUsername = getCurrentUsername(); // 获取当前用户名

        initializeComponents();
        setupLayout();
        loadInitialData();
    }

    private void initializeComponents() {
        // 时间范围选择
        timeRangeCombo = new JComboBox<>(new String[]{
                "最近7天", "最近30天", "最近90天", "本学期", "本学年", "全部"
        });
        timeRangeCombo.setSelectedIndex(1); // 默认最近30天

        // 按钮
        refreshButton = new JButton("刷新数据");
        statusLabel = new JLabel("就绪");
        statusLabel.setForeground(Color.BLUE);

        // 关键指标面板组件初始化
        totalAssignmentsValue = new JLabel("0");
        totalSubmissionsValue = new JLabel("0");
        completionRateValue = new JLabel("0%");
        averageScoreValue = new JLabel("0.0");
        pendingAppealsValue = new JLabel("0");
        processedAppealsValue = new JLabel("0");

        // 初始化关键指标面板
        initializeSummaryPanel();

        // 数据表格 - 删除系统健康度列
        String[] columnNames = {"指标", "当前值"}; // 删除同比变化和系统健康度列
        Object[][] data = new Object[9][2]; // 减少一行
        for (int i = 0; i < 9; i++) {
            data[i][0] = "加载中...";
            data[i][1] = "0";
        }
        tableModel = new DefaultTableModel(data, columnNames);
        dataTable = new JTable(tableModel);

        // 修正：设置合适的行高和字体
        dataTable.setRowHeight(35); // 增加行高到35像素
        dataTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        dataTable.setFont(new Font("微软雅黑", Font.PLAIN, 13));
        dataTable.setShowGrid(true);
        dataTable.setGridColor(new Color(220, 220, 220));

        // 修正：设置表格首选大小
        dataTable.setPreferredScrollableViewportSize(new Dimension(600, 350)); // 增加表格显示区域高度
        dataTable.setFillsViewportHeight(true);

        // 分析结果区域
        analysisResultArea = new JTextArea(8, 40);
        analysisResultArea.setEditable(false);
        analysisResultArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        analysisResultArea.setLineWrap(true);
        analysisResultArea.setWrapStyleWord(true);

        // 事件监听
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadStatisticsData();
            }
        });

        timeRangeCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadStatisticsData();
            }
        });
    }

    private void initializeSummaryPanel() {
        summaryPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("关键指标"));
        summaryPanel.setPreferredSize(new Dimension(getWidth(), 150)); // 增加面板高度
        summaryPanel.setBackground(Color.WHITE);

        // 创建关键指标卡片并显示具体数值
        summaryPanel.add(createMetricCard("作业总数", totalAssignmentsValue, Color.BLUE));
        summaryPanel.add(createMetricCard("提交总数", totalSubmissionsValue, Color.GREEN));
        summaryPanel.add(createMetricCard("完成率", completionRateValue, Color.ORANGE));
        summaryPanel.add(createMetricCard("平均分", averageScoreValue, Color.MAGENTA));
        summaryPanel.add(createMetricCard("待处理申诉", pendingAppealsValue, Color.RED));
        summaryPanel.add(createMetricCard("已处理申诉", processedAppealsValue, Color.GRAY));
    }

    // 创建带具体数值显示的指标卡片
    private JPanel createMetricCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15) // 增加内边距
        ));
        card.setBackground(new Color(250, 250, 250));

        // 标题
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        titleLabel.setForeground(color);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // 数值
        valueLabel.setFont(new Font("微软雅黑", Font.BOLD, 18)); // 增大字体
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 顶部控制面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("时间范围:"));
        topPanel.add(timeRangeCombo);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(refreshButton);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(statusLabel);

        // 主要内容区域 - 分割面板
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setDividerLocation(300); // 增加分割位置，给上半部分更多空间

        // 上半部分 - 关键指标和表格
        JPanel upperPanel = new JPanel(new BorderLayout(10, 10));

        // 关键指标面板
        upperPanel.add(summaryPanel, BorderLayout.NORTH);

        // 数据表格面板 - 修正：增加面板大小和边框
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1), // 添加明显边框
                "统计数据"
        ));
        tablePanel.setPreferredSize(new Dimension(getWidth(), 300)); // 设置合适的首选高度

        JScrollPane tableScrollPane = new JScrollPane(dataTable);
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setPreferredSize(new Dimension(600, 300)); // 设置滚动面板大小

        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
        upperPanel.add(tablePanel, BorderLayout.CENTER);

        mainSplitPane.setTopComponent(upperPanel);

        // 下半部分 - 分析结果
        JPanel lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.setBorder(BorderFactory.createTitledBorder("分析结果"));
        lowerPanel.setPreferredSize(new Dimension(getWidth(), 250)); // 设置合适的首选高度

        JScrollPane resultScrollPane = new JScrollPane(analysisResultArea);
        resultScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        resultScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        resultScrollPane.setPreferredSize(new Dimension(getWidth(), 200)); // 设置滚动面板大小

        lowerPanel.add(resultScrollPane, BorderLayout.CENTER);
        mainSplitPane.setBottomComponent(lowerPanel);

        // 添加到主面板
        add(topPanel, BorderLayout.NORTH);
        add(mainSplitPane, BorderLayout.CENTER);
    }

    private void loadInitialData() {
        loadStatisticsData();
    }

    private void loadStatisticsData() {
        try {
            statusLabel.setText("正在加载统计数据...");
            statusLabel.setForeground(Color.BLUE);
            refreshButton.setEnabled(false);

            // 获取时间范围
            LocalDateTime[] timeRange = getTimeRange();
            LocalDateTime startTime = timeRange[0];
            LocalDateTime endTime = timeRange[1];

            // 加载真实统计数据
            loadRealStatistics(startTime, endTime);

        } catch (Exception e) {
            statusLabel.setText("加载失败: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "加载统计数据失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        } finally {
            refreshButton.setEnabled(true);
        }
    }

    private void loadRealStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        System.out.println("=== 开始加载真实统计数据 ===");
        System.out.println("时间范围: " + startTime + " 到 " + endTime);

        try {
            // 1. 获取作业统计数据
            int totalAssignments = statisticsDAO.getAssignmentCount(startTime, endTime);
            System.out.println("作业总数: " + totalAssignments);

            // 2. 获取提交统计数据
            int totalSubmissions = statisticsDAO.getSubmissionCount(startTime, endTime);
            System.out.println("提交总数: " + totalSubmissions);

            // 3. 计算完成率
            double completionRate = totalAssignments > 0 ?
                    (double) totalSubmissions / totalAssignments * 100 : 0;
            System.out.println("完成率: " + completionRate + "%");

            // 4. 获取平均分
            double averageScore = statisticsDAO.getAverageScore(startTime, endTime);
            System.out.println("平均分: " + averageScore);

            // 5. 获取申诉统计数据
            int pendingAppeals = statisticsDAO.getPendingAppealCount();
            int processedAppeals = statisticsDAO.getProcessedAppealCount();
            System.out.println("待处理申诉: " + pendingAppeals + ", 已处理申诉: " + processedAppeals);

            // 6. 获取用户统计数据
            int totalTeachers = statisticsDAO.getTotalTeacherCount();
            int totalStudents = statisticsDAO.getTotalStudentCount();
            int totalCourses = statisticsDAO.getTotalCourseCount();
            System.out.println("教师: " + totalTeachers + ", 学生: " + totalStudents + ", 课程: " + totalCourses);

            // 更新UI
            SwingUtilities.invokeLater(() -> {
                // 更新关键指标显示具体数值
                updateKeyMetrics(totalAssignments, totalSubmissions, completionRate,
                        averageScore, pendingAppeals, processedAppeals);

                // 更新表格数据 - 删除系统健康度
                updateTableDataWithoutHealth(totalAssignments, totalSubmissions, completionRate,
                        averageScore, pendingAppeals, processedAppeals,
                        totalTeachers, totalStudents, totalCourses);

                // 更新分析结果
                updateAnalysisResult(totalAssignments, totalSubmissions, completionRate,
                        averageScore, pendingAppeals, processedAppeals,
                        totalTeachers, totalStudents, totalCourses);

                statusLabel.setText("数据加载完成 - " + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("HH:mm:ss")));
                statusLabel.setForeground(Color.GREEN);
            });

        } catch (Exception e) {
            System.err.println("加载真实统计数据失败: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("加载失败: " + e.getMessage());
            statusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "加载统计数据失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 更新关键指标显示具体数值
    private void updateKeyMetrics(int totalAssignments, int totalSubmissions,
                                  double completionRate, double averageScore,
                                  int pendingAppeals, int processedAppeals) {
        System.out.println("=== 更新关键指标 ===");
        System.out.println("作业总数: " + totalAssignments);
        System.out.println("提交总数: " + totalSubmissions);
        System.out.println("完成率: " + completionRate + "%");
        System.out.println("平均分: " + averageScore);
        System.out.println("待处理申诉: " + pendingAppeals);
        System.out.println("已处理申诉: " + processedAppeals);

        // 更新各个指标的具体数值显示
        totalAssignmentsValue.setText(String.valueOf(totalAssignments));
        totalSubmissionsValue.setText(String.valueOf(totalSubmissions));
        completionRateValue.setText(String.format("%.1f%%", completionRate));
        averageScoreValue.setText(String.format("%.1f", averageScore));
        pendingAppealsValue.setText(String.valueOf(pendingAppeals));
        processedAppealsValue.setText(String.valueOf(processedAppeals));

        System.out.println("=== 关键指标更新完成 ===");
    }

    // 更新表格数据 - 删除系统健康度列
    private void updateTableDataWithoutHealth(int totalAssignments, int totalSubmissions,
                                              double completionRate, double averageScore,
                                              int pendingAppeals, int processedAppeals,
                                              int totalTeachers, int totalStudents, int totalCourses) {
        System.out.println("=== 更新表格数据（不含系统健康度） ===");

        // 减少一行数据，删除系统健康度
        Object[][] data = {
                {"作业总数", String.valueOf(totalAssignments)},
                {"提交总数", String.valueOf(totalSubmissions)},
                {"完成率", String.format("%.1f%%", completionRate)},
                {"平均分", String.format("%.1f", averageScore)},
                {"待处理申诉", String.valueOf(pendingAppeals)},
                {"已处理申诉", String.valueOf(processedAppeals)},
                {"教师数量", String.valueOf(totalTeachers)},
                {"学生数量", String.valueOf(totalStudents)},
                {"课程总数", String.valueOf(totalCourses)}
                // 删除了 {"系统健康度", "98.5%"}
        };

        String[] columnNames = {"指标", "当前值"}; // 删除同比变化列
        tableModel.setDataVector(data, columnNames);

        // 修正：设置列宽以确保表格显示良好
        if (dataTable.getColumnModel().getColumnCount() > 0) {
            dataTable.getColumnModel().getColumn(0).setPreferredWidth(150); // 指标列
            dataTable.getColumnModel().getColumn(1).setPreferredWidth(200); // 当前值列
        }

        System.out.println("=== 表格数据更新完成 ===");
    }

    private void updateAnalysisResult(int totalAssignments, int totalSubmissions,
                                      double completionRate, double averageScore,
                                      int pendingAppeals, int processedAppeals,
                                      int totalTeachers, int totalStudents, int totalCourses) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("=== 统计分析报告 ===\n\n");

        // 作业完成率分析
        analysis.append("📊 作业完成率分析:\n");
        if (completionRate >= 90) {
            analysis.append("   • 优秀！作业完成率达到 ").append(String.format("%.1f", completionRate)).append("%\n");
        } else if (completionRate >= 80) {
            analysis.append("   • 良好，作业完成率为 ").append(String.format("%.1f", completionRate)).append("%\n");
        } else {
            analysis.append("   • 需要关注，作业完成率仅为 ").append(String.format("%.1f", completionRate)).append("%\n");
        }

        // 成绩分析
        analysis.append("\n📈 成绩分析:\n");
        if (averageScore >= 85) {
            analysis.append("   • 优秀！平均分为 ").append(String.format("%.1f", averageScore)).append("分\n");
        } else if (averageScore >= 75) {
            analysis.append("   • 良好，平均分为 ").append(String.format("%.1f", averageScore)).append("分\n");
        } else {
            analysis.append("   • 需要提升，平均分仅为 ").append(String.format("%.1f", averageScore)).append("分\n");
        }

        // 用户分析
        analysis.append("\n👥 用户分析:\n");
        analysis.append("   • 教师数量: ").append(totalTeachers).append("人\n");
        analysis.append("   • 学生数量: ").append(totalStudents).append("人\n");
        analysis.append("   • 课程总数: ").append(totalCourses).append("门\n");
        analysis.append("   • 师生比例: 1:").append(totalStudents > 0 ?
                String.format("%.1f", (double) totalStudents / totalTeachers) : "0").append("\n");

        // 申诉分析
        analysis.append("\n⚠️  申诉分析:\n");
        int totalAppeals = pendingAppeals + processedAppeals;
        if (totalAppeals > 0) {
            double processingRate = (double) processedAppeals / totalAppeals * 100;
            analysis.append("   • 总申诉数: ").append(totalAppeals).append("个\n");
            analysis.append("   • 处理率: ").append(String.format("%.1f", processingRate)).append("%\n");
            if (pendingAppeals > 5) {
                analysis.append("   • 待处理申诉较多(").append(pendingAppeals).append("个)，建议优先处理\n");
            }
        } else {
            analysis.append("   • 暂无申诉记录\n");
        }

        // 管理建议
        analysis.append("\n💡 管理建议:\n");
        if (completionRate < 80) {
            analysis.append("   • 建议加强作业督促，提高学生参与度\n");
        }
        if (averageScore < 75) {
            analysis.append("   • 建议优化作业难度，适当调整评分标准\n");
        }
        if (pendingAppeals > 5) {
            analysis.append("   • 建议增加申诉处理人员，缩短处理周期\n");
        }

        analysisResultArea.setText(analysis.toString());
        analysisResultArea.setCaretPosition(0);
    }

    private LocalDateTime[] getTimeRange() {
        String selectedRange = (String) timeRangeCombo.getSelectedItem();
        LocalDateTime now = LocalDateTime.now();

        switch (selectedRange) {
            case "最近7天":
                return new LocalDateTime[]{now.minusDays(7), now};
            case "最近30天":
                return new LocalDateTime[]{now.minusDays(30), now};
            case "最近90天":
                return new LocalDateTime[]{now.minusDays(90), now};
            case "本学期":
                return new LocalDateTime[]{now.minusMonths(4), now}; // 假设学期为4个月
            case "本学年":
                return new LocalDateTime[]{now.minusMonths(10), now}; // 假设学年为10个月
            case "全部":
            default:
                return new LocalDateTime[]{LocalDateTime.of(2020, 1, 1, 0, 0), now}; // 很久以前
        }
    }

    // 获取当前用户名的辅助方法
    private String getCurrentUsername() {
        // 这里应该从登录信息或系统上下文中获取当前用户名
        // 简化实现，返回示例值
        return "admin"; // 实际项目中应该动态获取
    }
}