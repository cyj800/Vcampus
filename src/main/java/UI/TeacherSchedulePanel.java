package UI;



import client.ClientNetwork;
import model.Course;
import model.Schedule;
import model.TimeSlot;
import tool.TimeSlotParser;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TeacherSchedulePanel extends JPanel {
    private String currentUsername;
    private String currentNickname;
    private ScheduleTablePanel scheduleTablePanel;
    private JLabel statusLabel;
    private JLabel statsLabel;
    private JButton refreshButton;
    private Schedule currentSchedule;

    public TeacherSchedulePanel(String username, String nickname) {
        this.currentUsername = username;
        this.currentNickname = nickname;
        this.currentSchedule = new Schedule();

        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadTeacherSchedule();
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
        refreshButton.setBackground(new Color(92, 184, 92));
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

        // 左侧教师信息
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(Color.WHITE);

        JLabel teacherLabel = new JLabel("教师: " + currentNickname + " (" + currentUsername + ")");
        teacherLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        teacherLabel.setForeground(new Color(220, 53, 69));
        leftPanel.add(teacherLabel);

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
        refreshButton.addActionListener(e -> loadTeacherSchedule());
    }

    private void loadTeacherSchedule() {
        statusLabel.setText("正在加载课表...");
        refreshButton.setEnabled(false);

        ClientNetwork.getCoursesByTeacher(currentUsername, new ClientNetwork.CourseCallback() {
            @Override
            public void onCourseResult(boolean success, String message, List<Course> courses) {
                SwingUtilities.invokeLater(() -> {
                    refreshButton.setEnabled(true);

                    if (success && courses != null) {
                        generateSchedule(courses);
                        statusLabel.setText("课表加载完成");
                    } else {
                        statusLabel.setText("课表加载失败: " + message);
                        JOptionPane.showMessageDialog(TeacherSchedulePanel.this,
                                "加载课表失败: " + message, "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        });
    }

    private void generateSchedule(List<Course> courses) {
        currentSchedule = new Schedule();
        int totalCredits = 0;
        int conflictCount = 0;

        for (Course course : courses) {
            List<TimeSlot> timeSlots = TimeSlotParser.parseClassTime(course);

            for (TimeSlot slot : timeSlots) {
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

        // 更新统计信息
        updateStats(courses.size(), totalCredits, conflictCount);

        // 更新课表显示
        scheduleTablePanel.updateSchedule(currentSchedule);

        // 如果有冲突，显示警告
        if (conflictCount > 0) {
            JOptionPane.showMessageDialog(this,
                    "检测到 " + conflictCount + " 个时间冲突，请检查课程安排！",
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
