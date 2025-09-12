package UI;
import client.ClientNetwork;
import model.Course;
import model.User;
import model.UserRole;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class StudentCoursePanel extends JPanel {
    private String currentUsername;
    private String currentNickname;

    // 已选课程ID集合，用于快速查找
    private Set<String> enrolledCourseIds = new HashSet<>();

    // 可选课程相关组件
    private JTable availableCoursesTable;
    private DefaultTableModel availableCoursesModel;
    private JTextField searchField;
    private JComboBox<String> semesterFilter;
    private JButton refreshButton, searchButton;
    private TableRowSorter<DefaultTableModel> availableSorter;

    // 已选课程相关组件
    private JTable enrolledCoursesTable;
    private DefaultTableModel enrolledCoursesModel;
    private JButton refreshEnrolledButton;
    private TableRowSorter<DefaultTableModel> enrolledSorter;
    private JButton viewScheduleButton;

    // 状态标签
    private JLabel statusLabel;

    // 可选课程表格列名
    private final String[] availableCoursesColumns = {
            "课程编号", "课程名称", "教师", "学分", "学期", "时间", "教室", "容量", "已选人数", "余量", "状态", "操作"
    };

    // 已选课程表格列名
    private final String[] enrolledCoursesColumns = {
            "课程编号", "课程名称", "教师", "学分", "学期", "时间", "教室", "选课时间", "操作"
    };

    public StudentCoursePanel(String username, String nickname) {
        this.currentUsername = username;
        this.currentNickname = nickname;
        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadInitialData();
    }

    private void initializeComponents() {
        // 初始化可选课程表格
        availableCoursesModel = new DefaultTableModel(availableCoursesColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 11; // 只有操作列可编辑
            }
        };
        availableCoursesTable = new JTable(availableCoursesModel);
        availableCoursesTable.setRowHeight(45);
        availableCoursesTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        availableCoursesTable.setFont(new Font("微软雅黑", Font.PLAIN, 11));

        // 设置可选课程表格排序器
        availableSorter = new TableRowSorter<>(availableCoursesModel);
        availableCoursesTable.setRowSorter(availableSorter);

        // 设置可选课程表格列宽
        availableCoursesTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // 课程编号
        availableCoursesTable.getColumnModel().getColumn(1).setPreferredWidth(120); // 课程名称
        availableCoursesTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // 教师
        availableCoursesTable.getColumnModel().getColumn(3).setPreferredWidth(50);  // 学分
        availableCoursesTable.getColumnModel().getColumn(4).setPreferredWidth(70);  // 学期
        availableCoursesTable.getColumnModel().getColumn(5).setPreferredWidth(100); // 时间
        availableCoursesTable.getColumnModel().getColumn(6).setPreferredWidth(70);  // 教室
        availableCoursesTable.getColumnModel().getColumn(7).setPreferredWidth(50);  // 容量
        availableCoursesTable.getColumnModel().getColumn(8).setPreferredWidth(60);  // 已选人数
        availableCoursesTable.getColumnModel().getColumn(9).setPreferredWidth(50);  // 余量
        availableCoursesTable.getColumnModel().getColumn(10).setPreferredWidth(60); // 状态
        availableCoursesTable.getColumnModel().getColumn(11).setPreferredWidth(80); // 操作

        // 设置可选课程操作列渲染器和编辑器
        availableCoursesTable.getColumn("操作").setCellRenderer(new EnrollButtonRenderer());
        availableCoursesTable.getColumn("操作").setCellEditor(new EnrollButtonEditor(new JCheckBox()));
// 新增：查看课表按钮
        viewScheduleButton = new JButton("查看课表");
        viewScheduleButton.setBackground(new Color(255, 193, 7)); // 橙色
        viewScheduleButton.setForeground(Color.WHITE);
        viewScheduleButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        viewScheduleButton.setBorderPainted(false);
        viewScheduleButton.setPreferredSize(new Dimension(90, 35));
        // 初始化已选课程表格
        enrolledCoursesModel = new DefaultTableModel(enrolledCoursesColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8; // 只有操作列可编辑
            }
        };
        enrolledCoursesTable = new JTable(enrolledCoursesModel);
        enrolledCoursesTable.setRowHeight(45);
        enrolledCoursesTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        enrolledCoursesTable.setFont(new Font("微软雅黑", Font.PLAIN, 11));

        // 设置已选课程表格排序器
        enrolledSorter = new TableRowSorter<>(enrolledCoursesModel);
        enrolledCoursesTable.setRowSorter(enrolledSorter);

        // 设置已选课程表格列宽
        enrolledCoursesTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // 课程编号
        enrolledCoursesTable.getColumnModel().getColumn(1).setPreferredWidth(120); // 课程名称
        enrolledCoursesTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // 教师
        enrolledCoursesTable.getColumnModel().getColumn(3).setPreferredWidth(50);  // 学分
        enrolledCoursesTable.getColumnModel().getColumn(4).setPreferredWidth(70);  // 学期
        enrolledCoursesTable.getColumnModel().getColumn(5).setPreferredWidth(100); // 时间
        enrolledCoursesTable.getColumnModel().getColumn(6).setPreferredWidth(70);  // 教室
        enrolledCoursesTable.getColumnModel().getColumn(7).setPreferredWidth(120); // 选课时间
        enrolledCoursesTable.getColumnModel().getColumn(8).setPreferredWidth(80);  // 操作

        // 设置已选课程操作列渲染器和编辑器
        enrolledCoursesTable.getColumn("操作").setCellRenderer(new DropButtonRenderer());
        enrolledCoursesTable.getColumn("操作").setCellEditor(new DropButtonEditor(new JCheckBox()));

        // 初始化搜索框
        searchField = new JTextField();
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchField.setPreferredSize(new Dimension(200, 35));

        // 初始化按钮
        searchButton = new JButton("搜索");
        searchButton.setBackground(new Color(52, 144, 220));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        searchButton.setBorderPainted(false);
        searchButton.setPreferredSize(new Dimension(70, 35));

        refreshButton = new JButton("刷新");
        refreshButton.setBackground(new Color(92, 184, 92));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        refreshButton.setBorderPainted(false);
        refreshButton.setPreferredSize(new Dimension(70, 35));

        refreshEnrolledButton = new JButton("刷新已选");
        refreshEnrolledButton.setBackground(new Color(92, 184, 92));
        refreshEnrolledButton.setForeground(Color.WHITE);
        refreshEnrolledButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        refreshEnrolledButton.setBorderPainted(false);
        refreshEnrolledButton.setPreferredSize(new Dimension(90, 35));

        // 学期筛选器
        semesterFilter = new JComboBox<>(new String[]{"全部", "2024春", "2024秋", "2025春"});
        semesterFilter.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        semesterFilter.setPreferredSize(new Dimension(100, 35));

        // 状态标签
        statusLabel = new JLabel("就绪");
        statusLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 100, 100));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // 创建主分割面板
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setResizeWeight(0.6); // 上面占60%
        mainSplitPane.setBorder(null);

        // 上半部分：可选课程
        JPanel availableCoursesPanel = createAvailableCoursesPanel();
        mainSplitPane.setTopComponent(availableCoursesPanel);

        // 下半部分：已选课程
        JPanel enrolledCoursesPanel = createEnrolledCoursesPanel();
        mainSplitPane.setBottomComponent(enrolledCoursesPanel);

        add(mainSplitPane, BorderLayout.CENTER);

        // 底部状态栏
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(245, 245, 245));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private JPanel createAvailableCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "可选课程",
                0, 0,
                new Font("微软雅黑", Font.BOLD, 16),
                new Color(51, 51, 51)
        ));

        // 顶部控制面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        // 左侧用户信息
        JLabel userLabel = new JLabel("学生: " + currentNickname + " (" + currentUsername + ")");
        userLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        userLabel.setForeground(new Color(100, 200, 100)); // 绿色，表示学生身份

        // 中间搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(new JLabel("搜索课程:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // 右侧控制面板
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlPanel.setBackground(Color.WHITE);

        controlPanel.add(refreshButton);

        topPanel.add(userLabel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(controlPanel, BorderLayout.EAST);

        // 表格滚动面板
        JScrollPane scrollPane = new JScrollPane(availableCoursesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(221, 221, 221), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEnrolledCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "已选课程",
                0, 0,
                new Font("微软雅黑", Font.BOLD, 16),
                new Color(51, 51, 51)
        ));

        // 顶部控制面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        // 左侧统计信息
        JLabel statsLabel = new JLabel("已选课程统计");
        statsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        statsLabel.setForeground(new Color(100, 100, 100));

        // 右侧控制按钮
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlPanel.setBackground(Color.WHITE);
        controlPanel.add(refreshEnrolledButton);
        controlPanel.add(viewScheduleButton);
        topPanel.add(statsLabel, BorderLayout.WEST);
        topPanel.add(controlPanel, BorderLayout.EAST);

        // 表格滚动面板
        JScrollPane scrollPane = new JScrollPane(enrolledCoursesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(221, 221, 221), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void setupEventListeners() {
        refreshButton.addActionListener(e -> loadAvailableCourses());
        refreshEnrolledButton.addActionListener(e -> loadEnrolledCourses());
        searchButton.addActionListener(e -> searchCourses());
        semesterFilter.addActionListener(e -> filterCoursesBySemester());
        viewScheduleButton.addActionListener(e -> showStudentSchedule());

        // 搜索框实时搜索
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }
        });
    }
    private void showStudentSchedule() {
        // 创建课表对话框
        JDialog scheduleDialog = new JDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "我的课表 - " + currentNickname,
                true
        );

        scheduleDialog.setSize(900, 700);
        scheduleDialog.setLocationRelativeTo(this);

        // 创建课表面板
        StudentSchedulePanel schedulePanel = new StudentSchedulePanel(currentUsername, currentNickname);
        scheduleDialog.add(schedulePanel);

        scheduleDialog.setVisible(true);
    }
    private void loadInitialData() {

        loadEnrolledCoursesFirst();
        //loadSemesters();
    }

    private void loadAvailableCoursesAfterEnrolled() {
        System.out.println("=== 开始加载可选课程（已选课程加载完成后） ===");
        statusLabel.setText("正在加载可选课程...");
        availableCoursesModel.setRowCount(0);

        ClientNetwork.getAllCourses(new ClientNetwork.CourseCallback() {
            @Override
            public void onCourseResult(boolean success, String message, List<Course> courses) {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("可选课程回调 - 成功: " + success + ", 课程数量: " + (courses != null ? courses.size() : 0));

                    if (success && courses != null) {
                        System.out.println("已选课程ID集合: " + enrolledCourseIds);

                        for (Course course : courses) {
                            addAvailableCourseToTable(course);
                        }
                        statusLabel.setText("已加载 " + courses.size() + " 门可选课程");
                        System.out.println("可选课程加载完成");
                    } else {
                        statusLabel.setText("加载可选课程失败: " + message);
                        JOptionPane.showMessageDialog(StudentCoursePanel.this,
                                "加载可选课程失败: " + message, "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        });
    }

    private void loadEnrolledCoursesFirst() {
        System.out.println("=== 开始初始化加载已选课程 ===");
        statusLabel.setText("正在初始化...");

        // 清空数据
        enrolledCoursesModel.setRowCount(0);
        enrolledCourseIds.clear();

        ClientNetwork.getStudentCourses(currentUsername, new ClientNetwork.CourseCallback() {
            @Override
            public void onCourseResult(boolean success, String message, List<Course> courses) {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("初始化已选课程回调 - 成功: " + success + ", 消息: " + message);

                    if (success && courses != null && !courses.isEmpty()) {
                        System.out.println("获取到 " + courses.size() + " 门已选课程");

                        for (Course course : courses) {
                            String courseId = course.getCourseId();
                            enrolledCourseIds.add(courseId);
                            addEnrolledCourseToTable(course);
                            System.out.println("已选课程: " + courseId + " - " + course.getCourseName());
                        }
                        updateEnrolledStats(courses.size(), calculateTotalCredits(courses));
                    } else {
                        System.out.println("用户暂无已选课程");
                        updateEnrolledStats(0, 0);
                    }

                    // 无论是否有已选课程，都继续加载可选课程
                    loadAvailableCoursesAfterEnrolled();
                });
            }
        });
    }

    private void loadAvailableCourses() {
        System.out.println("=== 刷新可选课程 ===");
        statusLabel.setText("正在刷新可选课程...");
        availableCoursesModel.setRowCount(0);

        ClientNetwork.getAllCourses(new ClientNetwork.CourseCallback() {
            @Override
            public void onCourseResult(boolean success, String message, List<Course> courses) {
                SwingUtilities.invokeLater(() -> {
                    if (success && courses != null) {
                        System.out.println("刷新获取到 " + courses.size() + " 门课程");
                        System.out.println("当前已选课程ID: " + enrolledCourseIds);

                        for (Course course : courses) {
                            addAvailableCourseToTable(course);
                        }
                        statusLabel.setText("已刷新 " + courses.size() + " 门可选课程");
                    } else {
                        statusLabel.setText("刷新可选课程失败: " + message);
                        JOptionPane.showMessageDialog(StudentCoursePanel.this,
                                "刷新可选课程失败: " + message, "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        });
    }


    private void loadEnrolledCourses() {
        System.out.println("=== 刷新已选课程 ===");
        statusLabel.setText("正在刷新已选课程...");

        // 清空现有数据 - 关键修改
        enrolledCoursesModel.setRowCount(0);
        enrolledCourseIds.clear(); // 重置集合

        ClientNetwork.getStudentCourses(currentUsername, new ClientNetwork.CourseCallback() {
            @Override
            public void onCourseResult(boolean success, String message, List<Course> courses) {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("刷新已选课程回调 - 成功: " + success + ", 消息: " + message);

                    if (success && courses != null) {
                        System.out.println("刷新获取到 " + courses.size() + " 门已选课程");

                        // 重新构建数据 - 移除重复检查逻辑
                        for (Course course : courses) {
                            String courseId = course.getCourseId();
                            System.out.println("已选课程: " + courseId + " - " + course.getCourseName());
                            enrolledCourseIds.add(courseId); // 重新添加到集合
                            addEnrolledCourseToTable(course); // 添加到表格
                        }

                        updateEnrolledStats(courses.size(), calculateTotalCredits(courses));
                        statusLabel.setText("已刷新已选课程");

                        // 立即刷新可选课程状态
                        refreshAvailableCoursesStatus();
                    } else {
                        System.out.println("刷新已选课程失败或无课程: " + message);
                        if (message != null && !message.contains("未找到")) {
                            statusLabel.setText("刷新已选课程失败: " + message);
                        } else {
                            updateEnrolledStats(0, 0);
                            statusLabel.setText("暂无已选课程");
                        }
                    }
                });
            }
        });
    }


    private void loadSemesters() {
        ClientNetwork.getAllSemesters(new ClientNetwork.SemesterCallback() {
            @Override
            public void onSemesterResult(boolean success, List<String> semesters) {
                SwingUtilities.invokeLater(() -> {
                    if (success && semesters != null) {
                        semesterFilter.removeAllItems();
                        semesterFilter.addItem("全部");
                        for (String semester : semesters) {
                            semesterFilter.addItem(semester);
                        }
                    }
                });
            }
        });
    }

    private void searchCourses() {
        String courseId = searchField.getText().trim();
        if (courseId.isEmpty()) {
            loadAvailableCourses();
            return;
        }

        statusLabel.setText("正在搜索课程...");
        availableCoursesModel.setRowCount(0);

        ClientNetwork.searchCoursesByCourseId(courseId, new ClientNetwork.CourseCallback() {
            @Override
            public void onCourseResult(boolean success, String message, List<Course> courses) {
                SwingUtilities.invokeLater(() -> {
                    if (success && courses != null) {
                        for (Course course : courses) {
                            addAvailableCourseToTable(course);
                        }
                        statusLabel.setText("找到 " + courses.size() + " 门课程");
                    } else {
                        statusLabel.setText("搜索失败: " + message);
                    }
                });
            }
        });
    }

    private void filterCoursesBySemester() {
        String selectedSemester = (String) semesterFilter.getSelectedItem();
        if ("全部".equals(selectedSemester)) {
            loadAvailableCourses();
            return;
        }

        statusLabel.setText("正在筛选课程...");
        availableCoursesModel.setRowCount(0);

        ClientNetwork.getCoursesBySemester(selectedSemester, new ClientNetwork.CourseCallback() {
            @Override
            public void onCourseResult(boolean success, String message, List<Course> courses) {
                SwingUtilities.invokeLater(() -> {
                    if (success && courses != null) {
                        for (Course course : courses) {
                            addAvailableCourseToTable(course);
                        }
                        statusLabel.setText("筛选出 " + courses.size() + " 门课程");
                    } else {
                        statusLabel.setText("筛选失败: " + message);
                    }
                });
            }
        });
    }

    private void filterTable() {
        String searchText = searchField.getText().trim();
        String selectedSemester = (String) semesterFilter.getSelectedItem();

        if (searchText.isEmpty() && "全部".equals(selectedSemester)) {
            availableSorter.setRowFilter(null);
        } else {
            RowFilter<DefaultTableModel, Object> filter = new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    boolean searchMatch = searchText.isEmpty() ||
                            entry.getStringValue(0).toLowerCase().contains(searchText.toLowerCase()) ||
                            entry.getStringValue(1).toLowerCase().contains(searchText.toLowerCase()) ||
                            entry.getStringValue(2).toLowerCase().contains(searchText.toLowerCase());

                    boolean semesterMatch = "全部".equals(selectedSemester) ||
                            entry.getStringValue(4).equals(selectedSemester);

                    return searchMatch && semesterMatch;
                }
            };
            availableSorter.setRowFilter(filter);
        }
    }

    private void addAvailableCourseToTable(Course course) {
        int currentStudents = course.getCurrentStudents();
        int maxStudents = course.getMaxStudents() != null ? course.getMaxStudents() : 0;
        int remainingCapacity = Math.max(0, maxStudents - currentStudents);

        // 检查是否已选
        boolean isEnrolled = enrolledCourseIds.contains(course.getCourseId());
        String status = isEnrolled ? "已选" : (remainingCapacity > 0 ? "可选" : "已满");

        Object[] rowData = {
                course.getCourseId(),
                course.getCourseName(),
                course.getTeacherName(),
                course.getCredits(),
                course.getSemester(),
                course.getClassTime(),
                course.getClassroom(),
                maxStudents,
                currentStudents,
                remainingCapacity,
                status,
                isEnrolled ? "已选" : (remainingCapacity > 0 ? "选课" : "已满")
        };
        availableCoursesModel.addRow(rowData);
    }

    private void addEnrolledCourseToTable(Course course) {
        Object[] rowData = {
                course.getCourseId(),
                course.getCourseName(),
                course.getTeacherName(),
                course.getCredits(),
                course.getSemester(),
                course.getClassTime(),
                course.getClassroom(),
                course.getCreatedAt() != null ? course.getCreatedAt().toString().substring(0, 19) : "未知时间",
                "退课"
        };
        enrolledCoursesModel.addRow(rowData);
    }

    private void updateEnrolledStats(int courseCount, int totalCredits) {
        // 更新已选课程统计信息
        JPanel statsPanel = findStatsPanel();
        if (statsPanel != null) {
            Component[] components = statsPanel.getComponents();
            if (components.length > 0 && components[0] instanceof JLabel) {
                JLabel statsLabel = (JLabel) components[0];
                statsLabel.setText("已选课程: " + courseCount + " 门，总学分: " + totalCredits + " 分");
            }
        }
    }

    private JPanel findStatsPanel() {
        // 这个方法用于找到统计信息面板，需要根据实际布局调整
        return null; // 简化实现
    }

    private int calculateTotalCredits(List<Course> courses) {
        int totalCredits = 0;
        for (Course course : courses) {
            totalCredits += course.getCredits();
        }
        return totalCredits;
    }



    private void enrollCourse(int row) {
        String courseId = (String) availableCoursesModel.getValueAt(row, 0);
        String courseName = (String) availableCoursesModel.getValueAt(row, 1);
        String status = (String) availableCoursesModel.getValueAt(row, 10);

        // 检查课程状态
        if ("已选".equals(status)) {
            JOptionPane.showMessageDialog(this, "您已经选择了这门课程！", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Integer remainingCapacity = (Integer) availableCoursesModel.getValueAt(row, 9);
        if (remainingCapacity <= 0) {
            JOptionPane.showMessageDialog(this, "该课程已满员，无法选课！", "选课失败", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
                "确定要选修课程 \"" + courseName + "\" 吗？",
                "确认选课", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            statusLabel.setText("正在选课...");

            ClientNetwork.enrollCourse(courseId, new ClientNetwork.CourseActionCallback() {
                @Override
                public void onActionResult(boolean success, String message) {
                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            statusLabel.setText("选课成功！");
                            JOptionPane.showMessageDialog(StudentCoursePanel.this,
                                    "选课成功！", "成功", JOptionPane.INFORMATION_MESSAGE);

                            // 优化数据更新 - 先加载已选课程，再更新可选课程
                            loadEnrolledCoursesAndRefreshAvailable();
                        } else {
                            statusLabel.setText("选课失败: " + message);
                            JOptionPane.showMessageDialog(StudentCoursePanel.this,
                                    "选课失败: " + message, "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
            });
        }
    }


    // 改进退课成功后的处理
    private void dropCourse(int row) {
        String courseId = (String) enrolledCoursesModel.getValueAt(row, 0);
        String courseName = (String) enrolledCoursesModel.getValueAt(row, 1);

        int result = JOptionPane.showConfirmDialog(this,
                "确定要退选课程 \"" + courseName + "\" 吗？",
                "确认退课", JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            statusLabel.setText("正在退课...");

            ClientNetwork.dropCourse(courseId, new ClientNetwork.CourseActionCallback() {
                @Override
                public void onActionResult(boolean success, String message) {
                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            statusLabel.setText("退课成功！");
                            JOptionPane.showMessageDialog(StudentCoursePanel.this,
                                    "退课成功！", "成功", JOptionPane.INFORMATION_MESSAGE);

                            // 优化数据更新 - 先加载已选课程，再更新可选课程
                            loadEnrolledCoursesAndRefreshAvailable();
                        } else {
                            statusLabel.setText("退课失败: " + message);
                            JOptionPane.showMessageDialog(StudentCoursePanel.this,
                                    "退课失败: " + message, "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
            });
        }
    }
    // 新增：统一的数据更新方法
    private void loadEnrolledCoursesAndRefreshAvailable() {
        System.out.println("=== 统一更新选课数据 ===");
        statusLabel.setText("正在更新数据...");

        // 清空现有数据
        enrolledCoursesModel.setRowCount(0);
        enrolledCourseIds.clear();

        ClientNetwork.getStudentCourses(currentUsername, new ClientNetwork.CourseCallback() {
            @Override
            public void onCourseResult(boolean success, String message, List<Course> courses) {
                SwingUtilities.invokeLater(() -> {
                    if (success && courses != null) {
                        System.out.println("获取到 " + courses.size() + " 门已选课程");

                        // 更新已选课程数据
                        for (Course course : courses) {
                            String courseId = course.getCourseId();
                            enrolledCourseIds.add(courseId);
                            addEnrolledCourseToTable(course);
                            System.out.println("已选课程: " + courseId + " - " + course.getCourseName());
                        }

                        updateEnrolledStats(courses.size(), calculateTotalCredits(courses));

                        // 立即刷新可选课程状态
                        refreshAvailableCoursesTableData();

                        statusLabel.setText("数据更新完成");
                    } else {
                        System.out.println("获取已选课程失败: " + message);
                        updateEnrolledStats(0, 0);

                        // 即使没有已选课程，也要刷新可选课程状态
                        refreshAvailableCoursesTableData();

                        if (message != null && !message.contains("未找到")) {
                            statusLabel.setText("更新数据失败: " + message);
                        } else {
                            statusLabel.setText("数据更新完成");
                        }
                    }
                });
            }
        });
    }

    // 新增：刷新可选课程表格数据的方法
    private void refreshAvailableCoursesTableData() {
        System.out.println("=== 刷新可选课程表格状态 ===");
        System.out.println("当前已选课程ID: " + enrolledCourseIds);

        // 更新可选课程表格中每一行的状态和操作列
        for (int i = 0; i < availableCoursesModel.getRowCount(); i++) {
            String courseId = (String) availableCoursesModel.getValueAt(i, 0);
            boolean isEnrolled = enrolledCourseIds.contains(courseId);

            Integer maxStudents = (Integer) availableCoursesModel.getValueAt(i, 7);
            Integer currentStudents = (Integer) availableCoursesModel.getValueAt(i, 8);
            int remainingCapacity = Math.max(0, (maxStudents != null ? maxStudents : 0) -
                    (currentStudents != null ? currentStudents : 0));

            // 更新状态列
            String status = isEnrolled ? "已选" : (remainingCapacity > 0 ? "可选" : "已满");
            availableCoursesModel.setValueAt(status, i, 10);

            // 更新操作列
            String action = isEnrolled ? "已选" : (remainingCapacity > 0 ? "选课" : "已满");
            availableCoursesModel.setValueAt(action, i, 11);

            System.out.println("更新课程 " + courseId + " 状态: " + status + ", 操作: " + action);
        }

        // 刷新表格显示
        availableCoursesTable.repaint();
    }

    // 改进现有的刷新方法
    private void refreshAvailableCoursesStatus() {
        // 直接调用新的刷新方法
        refreshAvailableCoursesTableData();
    }
    // 选课按钮渲染器
    class EnrollButtonRenderer extends JButton implements TableCellRenderer {
        public EnrollButtonRenderer() {
            setBorderPainted(false);
            setFont(new Font("微软雅黑", Font.BOLD, 11));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            String status = (String) table.getValueAt(row, 10); // 状态列
            String courseId = (String) table.getValueAt(row, 0);

            if ("已选".equals(status) || enrolledCourseIds.contains(courseId)) {
                setBackground(new Color(108, 117, 125));
                setForeground(Color.WHITE);
                setText("已选");
                setEnabled(false);
            } else {
                Integer remainingCapacity = (Integer) table.getValueAt(row, 9);
                if (remainingCapacity != null && remainingCapacity <= 0) {
                    setBackground(Color.GRAY);
                    setForeground(Color.WHITE);
                    setText("已满");
                    setEnabled(false);
                } else {
                    setBackground(new Color(52, 144, 220));
                    setForeground(Color.WHITE);
                    setText("选课");
                    setEnabled(true);
                }
            }
            return this;
        }
    }

    // 选课按钮编辑器
    class EnrollButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int currentRow;

        public EnrollButtonEditor(JCheckBox checkBox) {
            super(checkBox);

            button = new JButton();
            button.setBorderPainted(false);
            button.setFont(new Font("微软雅黑", Font.BOLD, 11));

            button.addActionListener(e -> {
                fireEditingStopped();
                String status = (String) availableCoursesModel.getValueAt(currentRow, 10);
                if (!"已选".equals(status)) {
                    enrollCourse(currentRow);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = table.convertRowIndexToModel(row);

            String status = (String) table.getValueAt(row, 10);
            String courseId = (String) table.getValueAt(row, 0);

            if ("已选".equals(status) || enrolledCourseIds.contains(courseId)) {
                button.setBackground(new Color(108, 117, 125));
                button.setForeground(Color.WHITE);
                button.setText("已选");
                button.setEnabled(false);
            } else {
                Integer remainingCapacity = (Integer) table.getValueAt(row, 9);
                if (remainingCapacity != null && remainingCapacity <= 0) {
                    button.setBackground(Color.GRAY);
                    button.setForeground(Color.WHITE);
                    button.setText("已满");
                    button.setEnabled(false);
                } else {
                    button.setBackground(new Color(52, 144, 220));
                    button.setForeground(Color.WHITE);
                    button.setText("选课");
                    button.setEnabled(true);
                }
            }

            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }
    }

    // 退课按钮渲染器
    class DropButtonRenderer extends JButton implements TableCellRenderer {
        public DropButtonRenderer() {
            setText("退课");
            setBackground(new Color(217, 83, 79));
            setForeground(Color.WHITE);
            setBorderPainted(false);
            setFont(new Font("微软雅黑", Font.BOLD, 11));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // 退课按钮编辑器
    class DropButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int currentRow;

        public DropButtonEditor(JCheckBox checkBox) {
            super(checkBox);

            button = new JButton("退课");
            button.setBackground(new Color(217, 83, 79));
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);
            button.setFont(new Font("微软雅黑", Font.BOLD, 11));

            button.addActionListener(e -> {
                fireEditingStopped();
                dropCourse(currentRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = table.convertRowIndexToModel(row);
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "退课";
        }
    }
}