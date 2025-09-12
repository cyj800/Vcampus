package UI;

import client.ClientNetwork;
import model.Course;
import model.Schedule;
import model.TimeSlot;
import tool.TimeSlotParser;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StudentSchedulePanel extends JPanel {
    private String currentUsername;
    private String currentNickname;
    private ScheduleTablePanel scheduleTablePanel;
    private JLabel statusLabel;
    private JLabel statsLabel;
    private JButton refreshButton;
    private Schedule currentSchedule;

    public StudentSchedulePanel(String username, String nickname) {
        this.currentUsername = username;
        this.currentNickname = nickname;
        this.currentSchedule = new Schedule();

        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadStudentSchedule();
    }

    private void initializeComponents() {
        scheduleTablePanel = new ScheduleTablePanel();

        statusLabel = new JLabel("就绪");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 100, 100));

        statsLabel = new JLabel("课程统计: 0 门课程，0 学分");
        statsLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        statsLabel.setForeground(new Color(51, 51, 51));

        refreshButton = new JButton("刷新课表");
        refreshButton.setBackground(new Color(52, 144, 220));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        refreshButton.setBorderPainted(false);
        refreshButton.setPreferredSize(new Dimension(100, 35));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // 顶部面板
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // 课表面板
        add(scheduleTablePanel, BorderLayout.CENTER);

        // 底部状态栏
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(245, 245, 245));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "我的课表",
                0, 0,
                new Font("微软雅黑", Font.BOLD, 18),
                new Color(51, 51, 51)
        ));

        // 左侧学生信息
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(Color.WHITE);

        JLabel studentLabel = new JLabel("学生: " + currentNickname + " (" + currentUsername + ")");
        studentLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        studentLabel.setForeground(new Color(52, 144, 220));
        leftPanel.add(studentLabel);

        // 中间统计信息
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(statsLabel);

        // 右侧控制面板
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(refreshButton);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(centerPanel, BorderLayout.CENTER);
        topPanel.add(rightPanel, BorderLayout.EAST);

        return topPanel;
    }

    private void setupEventListeners() {
        refreshButton.addActionListener(e -> loadStudentSchedule());
    }

    private void loadStudentSchedule() {
        statusLabel.setText("正在加载课表...");
        refreshButton.setEnabled(false);

        ClientNetwork.getStudentCourses(currentUsername, new ClientNetwork.CourseCallback() {
            @Override
            public void onCourseResult(boolean success, String message, List<Course> courses) {
                SwingUtilities.invokeLater(() -> {
                    refreshButton.setEnabled(true);

                    if (success && courses != null) {
                        generateSchedule(courses);
                        statusLabel.setText("课表加载完成");
                    } else {
                        statusLabel.setText("课表加载失败: " + message);
                        currentSchedule = new Schedule(); // 清空课表
                        scheduleTablePanel.updateSchedule(currentSchedule);
                        updateStats(0, 0, 0);

                        if (message != null && !message.contains("未找到")) {
                            JOptionPane.showMessageDialog(StudentSchedulePanel.this,
                                    "加载课表失败: " + message, "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
            }
        });
    }

    private void generateSchedule(List<Course> courses) {
        currentSchedule = new Schedule();
        int totalCredits = 0;
        int conflictCount = 0;

        System.out.println("=== 开始生成学生课表 ===");
        System.out.println("学生: " + currentUsername + ", 课程数量: " + courses.size());

        for (Course course : courses) {
            System.out.println("处理课程: " + course.getCourseId() + " - " + course.getCourseName());
            System.out.println("上课时间: " + course.getClassTime());

            List<TimeSlot> timeSlots = TimeSlotParser.parseClassTime(course);
            System.out.println("解析出 " + timeSlots.size() + " 个时间段");

            for (TimeSlot slot : timeSlots) {
                System.out.println("  时间段: " + slot.getDayOfWeek() +
                        " 第" + slot.getStartPeriod() + "-" + slot.getEndPeriod() + "节");

                if (currentSchedule.hasConflict(slot)) {
                    conflictCount++;
                    System.err.println("发现时间冲突: " + course.getCourseName() +
                            " 在 " + slot.getDayOfWeek() +
                            " 第" + slot.getStartPeriod() + "-" + slot.getEndPeriod() + "节");
                }
                currentSchedule.addTimeSlot(slot);
            }

            totalCredits += course.getCredits();
        }

        System.out.println("课表生成完成 - 总课程: " + courses.size() +
                ", 总学分: " + totalCredits +
                ", 冲突数: " + conflictCount);

        // 更新统计信息
        updateStats(courses.size(), totalCredits, conflictCount);

        // 更新课表显示
        scheduleTablePanel.updateSchedule(currentSchedule);

        // 如果有冲突，显示警告
        if (conflictCount > 0) {
            JOptionPane.showMessageDialog(this,
                    "检测到 " + conflictCount + " 个时间冲突，请联系教务处处理！",
                    "时间冲突警告", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateStats(int courseCount, int totalCredits, int conflictCount) {
        String statsText = "课程统计: " + courseCount + " 门课程，" + totalCredits + " 学分";
        if (conflictCount > 0) {
            statsText += " (⚠️ " + conflictCount + " 个冲突)";
            statsLabel.setForeground(Color.RED);
        } else {
            statsLabel.setForeground(new Color(51, 51, 51));
        }
        statsLabel.setText(statsText);
    }
}
