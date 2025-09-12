package UI;

import client.ClientNetwork;
import model.Course;
import model.CourseEnrollment;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class TeacherCoursePanel extends JPanel {
    private String currentUsername;
    private String currentNickname;
    private JButton viewScheduleButton;
    // 课程表格相关组件
    private JTable coursesTable;
    private DefaultTableModel coursesModel;
    private JButton refreshButton;
    private TableRowSorter<DefaultTableModel> sorter;

    // 状态标签
    private JLabel statusLabel;
    private JLabel statsLabel;

    // 课程表格列名
    private final String[] coursesColumns = {
            "课程编号", "课程名称", "学分", "学期", "上课时间", "教室", "容量", "已选人数", "余量", "操作"
    };

    public TeacherCoursePanel(String username, String nickname) {
        this.currentUsername = username;
        this.currentNickname = nickname;
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadInitialData();
    }

    private void initializeComponents() {
        // 初始化课程表格
        coursesModel = new DefaultTableModel(coursesColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 9; // 只有操作列可编辑
            }
        };

        coursesTable = new JTable(coursesModel);
        coursesTable.setRowHeight(45);
        coursesTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        coursesTable.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        coursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 设置表格排序器
        sorter = new TableRowSorter<>(coursesModel);
        coursesTable.setRowSorter(sorter);

        // 设置列宽
        coursesTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // 课程编号
        coursesTable.getColumnModel().getColumn(1).setPreferredWidth(150); // 课程名称
        coursesTable.getColumnModel().getColumn(2).setPreferredWidth(50);  // 学分
        coursesTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // 学期
        coursesTable.getColumnModel().getColumn(4).setPreferredWidth(120); // 上课时间
        coursesTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // 教室
        coursesTable.getColumnModel().getColumn(6).setPreferredWidth(60);  // 容量
        coursesTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // 已选人数
        coursesTable.getColumnModel().getColumn(8).setPreferredWidth(60);  // 余量
        coursesTable.getColumnModel().getColumn(9).setPreferredWidth(100); // 操作

        // 设置操作列渲染器和编辑器
        coursesTable.getColumn("操作").setCellRenderer(new ViewStudentsButtonRenderer());
        coursesTable.getColumn("操作").setCellEditor(new ViewStudentsButtonEditor(new JCheckBox()));

        // 初始化按钮
        refreshButton = new JButton("刷新课程");
        refreshButton.setBackground(new Color(92, 184, 92));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        refreshButton.setBorderPainted(false);
        refreshButton.setPreferredSize(new Dimension(100, 35));
        viewScheduleButton = new JButton("查看课表");
        viewScheduleButton.setBackground(new Color(255, 193, 7)); // 橙色
        viewScheduleButton.setForeground(Color.WHITE);
        viewScheduleButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        viewScheduleButton.setBorderPainted(false);
        viewScheduleButton.setPreferredSize(new Dimension(100, 35));

        // 状态标签
        statusLabel = new JLabel("就绪");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 100, 100));

        // 统计标签
        statsLabel = new JLabel("课程统计: 0 门课程");
        statsLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        statsLabel.setForeground(new Color(51, 51, 51));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // 主面板
        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);

        // 底部状态栏
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(245, 245, 245));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "我的课程",
                0, 0,
                new Font("微软雅黑", Font.BOLD, 18),
                new Color(51, 51, 51)
        ));

        // 顶部信息面板
        JPanel topPanel = createTopPanel();
        panel.add(topPanel, BorderLayout.NORTH);

        // 表格滚动面板
        JScrollPane scrollPane = new JScrollPane(coursesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(221, 221, 221), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 左侧教师信息
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setBackground(Color.WHITE);

        JLabel teacherLabel = new JLabel("教师: " + currentNickname + " (" + currentUsername + ")");
        teacherLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        teacherLabel.setForeground(new Color(220, 53, 69)); // 红色，表示教师身份
        teacherLabel.setIcon(createTeacherIcon());

        leftPanel.add(teacherLabel);

        // 中间统计信息
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(statsLabel);

        // 右侧控制面板
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(refreshButton);
        rightPanel.add(viewScheduleButton);
        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(centerPanel, BorderLayout.CENTER);
        topPanel.add(rightPanel, BorderLayout.EAST);

        return topPanel;
    }

    private Icon createTeacherIcon() {
        // 创建一个简单的教师图标
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 53, 69));
                g2.fillOval(x + 2, y + 2, 12, 12);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("微软雅黑", Font.BOLD, 8));
                g2.drawString("师", x + 5, y + 11);
                g2.dispose();
            }

            @Override
            public int getIconWidth() { return 16; }

            @Override
            public int getIconHeight() { return 16; }
        };
    }

    private void setupEventListeners() {
        refreshButton.addActionListener(e -> loadTeacherCourses());
        viewScheduleButton.addActionListener(e -> showTeacherSchedule());
    }

    private void loadInitialData() {
        loadTeacherCourses();
    }
    private void showTeacherSchedule() {
        // 创建课表对话框
        JDialog scheduleDialog = new JDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "我的课表 - " + currentNickname,
                true
        );

        scheduleDialog.setSize(900, 700);
        scheduleDialog.setLocationRelativeTo(this);

        // 创建课表面板
        TeacherSchedulePanel schedulePanel = new TeacherSchedulePanel(currentUsername, currentNickname);
        scheduleDialog.add(schedulePanel);

        scheduleDialog.setVisible(true);
    }
    private void loadTeacherCourses() {
        System.out.println("=== 加载教师课程，教师ID: " + currentUsername + " ===");
        statusLabel.setText("正在加载课程...");
        coursesModel.setRowCount(0);

        ClientNetwork.getCoursesByTeacher(currentUsername, new ClientNetwork.CourseCallback() {
            @Override
            public void onCourseResult(boolean success, String message, List<Course> courses) {
                SwingUtilities.invokeLater(() -> {
                    if (success && courses != null) {
                        System.out.println("获取到 " + courses.size() + " 门课程");

                        for (Course course : courses) {
                            addCourseToTable(course);
                            System.out.println("教师课程: " + course.getCourseId() + " - " + course.getCourseName());
                        }

                        updateStats(courses.size(), calculateTotalStudents(courses));
                        statusLabel.setText("已加载 " + courses.size() + " 门课程");
                    } else {
                        System.err.println("加载教师课程失败: " + message);
                        statusLabel.setText("加载课程失败: " + message);
                        updateStats(0, 0);

                        if (message != null && !message.contains("未找到")) {
                            JOptionPane.showMessageDialog(TeacherCoursePanel.this,
                                    "加载课程失败: " + message, "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
            }
        });
    }

    private void addCourseToTable(Course course) {
        int currentStudents = course.getCurrentStudents();
        int maxStudents = course.getMaxStudents() != null ? course.getMaxStudents() : 0;
        int remainingCapacity = Math.max(0, maxStudents - currentStudents);

        Object[] rowData = {
                course.getCourseId(),
                course.getCourseName(),
                course.getCredits(),
                course.getSemester(),
                course.getClassTime(),
                course.getClassroom(),
                maxStudents,
                currentStudents,
                remainingCapacity,
                "查看名单"
        };
        coursesModel.addRow(rowData);
    }

    private void updateStats(int courseCount, int totalStudents) {
        statsLabel.setText("课程统计: " + courseCount + " 门课程，共 " + totalStudents + " 名学生");
    }

    private int calculateTotalStudents(List<Course> courses) {
        int total = 0;
        for (Course course : courses) {
            total += course.getCurrentStudents();
        }
        return total;
    }

    private void viewStudentList(int row) {
        String courseId = (String) coursesModel.getValueAt(row, 0);
        String courseName = (String) coursesModel.getValueAt(row, 1);

        System.out.println("=== 查看课程学生名单: " + courseId + " ===");
        statusLabel.setText("正在加载学生名单...");

        // 使用正确的方法和回调
        ClientNetwork.getCourseStudents(courseId, new ClientNetwork.CourseStudentsCallback() {
            @Override
            public void onStudentsResult(boolean success, List<CourseEnrollment> students) {
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        System.out.println("获取到 " + (students != null ? students.size() : 0) + " 名学生");
                        statusLabel.setText("已加载学生名单");

                        // 使用现有的 StudentListDialog
                        String dialogTitle = courseName + " - 学生名单";
                        StudentListDialog dialog = new StudentListDialog(
                                (JFrame) SwingUtilities.getWindowAncestor(TeacherCoursePanel.this),
                                dialogTitle,
                                students
                        );
                        dialog.setVisible(true);
                    } else {
                        System.err.println("获取学生名单失败");
                        statusLabel.setText("获取学生名单失败");
                        JOptionPane.showMessageDialog(TeacherCoursePanel.this,
                                "获取学生名单失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        });
    }


    // 查看学生名单按钮渲染器
    class ViewStudentsButtonRenderer extends JButton implements TableCellRenderer {
        public ViewStudentsButtonRenderer() {
            setText("查看名单");
            setBackground(new Color(52, 144, 220));
            setForeground(Color.WHITE);
            setBorderPainted(false);
            setFont(new Font("微软雅黑", Font.BOLD, 11));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Integer studentCount = (Integer) table.getValueAt(row, 7); // 已选人数列
            if (studentCount != null && studentCount > 0) {
                setText("查看名单(" + studentCount + ")");
                setBackground(new Color(52, 144, 220));
                setEnabled(true);
            } else {
                setText("暂无学生");
                setBackground(new Color(108, 117, 125));
                setEnabled(false);
            }
            return this;
        }
    }

    // 查看学生名单按钮编辑器
    class ViewStudentsButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int currentRow;

        public ViewStudentsButtonEditor(JCheckBox checkBox) {
            super(checkBox);

            button = new JButton();
            button.setBorderPainted(false);
            button.setFont(new Font("微软雅黑", Font.BOLD, 11));

            button.addActionListener(e -> {
                fireEditingStopped();
                Integer studentCount = (Integer) coursesModel.getValueAt(currentRow, 7);
                if (studentCount != null && studentCount > 0) {
                    viewStudentList(currentRow);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = table.convertRowIndexToModel(row);

            Integer studentCount = (Integer) table.getValueAt(row, 7);
            if (studentCount != null && studentCount > 0) {
                button.setText("查看名单(" + studentCount + ")");
                button.setBackground(new Color(52, 144, 220));
                button.setForeground(Color.WHITE);
                button.setEnabled(true);
            } else {
                button.setText("暂无学生");
                button.setBackground(new Color(108, 117, 125));
                button.setForeground(Color.WHITE);
                button.setEnabled(false);
            }

            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }
    }
}


