package UI;



import client.ClientNetwork;
import model.Course;
import model.CourseEnrollment;
import javax.swing.JOptionPane;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class AdminCourseManagementPanel extends JPanel {
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private JButton addButton, refreshButton;
    private JComboBox<String> semesterFilter;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;

    // 表格列名 - 添加课容量和余量列
    private final String[] columnNames = {
            "课程编号", "课程名称", "教师", "学分", "学期", "时间", "教室", "当前人数", "课容量", "余量", "查看学生", "操作"
    };



    public AdminCourseManagementPanel() {
        initializeComponents();
        setupLayout();
        loadCourseData();
        setupEventListeners();
    }

    private void initializeComponents() {
        // 初始化表格
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 10 || column == 11; // 查看学生列和操作列可编辑
            }
        };
        courseTable = new JTable(tableModel);
        courseTable.setRowHeight(60);
        courseTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        courseTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        // 设置表格排序器
        sorter = new TableRowSorter<>(tableModel);
        courseTable.setRowSorter(sorter);

        // 设置列宽
        courseTable.getColumnModel().getColumn(0).setPreferredWidth(100); // 课程编号
        courseTable.getColumnModel().getColumn(1).setPreferredWidth(150); // 课程名称
        courseTable.getColumnModel().getColumn(2).setPreferredWidth(100); // 教师
        courseTable.getColumnModel().getColumn(3).setPreferredWidth(60);  // 学分
        courseTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 学期
        courseTable.getColumnModel().getColumn(5).setPreferredWidth(120); // 时间
        courseTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // 教室
        courseTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // 当前人数
        courseTable.getColumnModel().getColumn(8).setPreferredWidth(80);  // 课容量
        courseTable.getColumnModel().getColumn(9).setPreferredWidth(60);  // 余量
        courseTable.getColumnModel().getColumn(10).setPreferredWidth(100); // 查看学生
        courseTable.getColumnModel().getColumn(11).setPreferredWidth(160); // 操作

        // 设置渲染器和编辑器
        courseTable.getColumn("查看学生").setCellRenderer(new ViewStudentsButtonRenderer());
        courseTable.getColumn("查看学生").setCellEditor(new ViewStudentsButtonEditor(new JCheckBox()));
        courseTable.getColumn("操作").setCellRenderer(new ActionButtonRenderer());
        courseTable.getColumn("操作").setCellEditor(new ActionButtonEditor(new JCheckBox()));

        // 初始化搜索框
        searchField = new JTextField();
        searchField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        searchField.setPreferredSize(new Dimension(200, 35));

        // 初始化按钮
        addButton = new JButton("添加");
        addButton.setBackground(new Color(52, 144, 220));
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        addButton.setBorderPainted(false);
        addButton.setPreferredSize(new Dimension(80, 35));

        refreshButton = new JButton("刷新");
        refreshButton.setBackground(new Color(92, 184, 92));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        refreshButton.setBorderPainted(false);
        refreshButton.setPreferredSize(new Dimension(80, 35));

        // 学期筛选器
        semesterFilter = new JComboBox<>(new String[]{"全部", "2024春", "2024秋", "2025春"});
        semesterFilter.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        semesterFilter.setPreferredSize(new Dimension(120, 35));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // 顶部面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        // 左侧标题
        JLabel titleLabel = new JLabel("课程管理");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 51, 51));

        // 中间搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(new JLabel("搜索:"));
        searchPanel.add(searchField);

        // 右侧控制面板
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlPanel.setBackground(Color.WHITE);

        controlPanel.add(addButton);
        controlPanel.add(refreshButton);

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(controlPanel, BorderLayout.EAST);

        // 表格滚动面板
        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(221, 221, 221), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void setupEventListeners() {
        addButton.addActionListener(e -> showAddCourseDialog());
        refreshButton.addActionListener(e -> loadCourseData());
        semesterFilter.addActionListener(e -> filterCoursesBySemester());

        // 搜索功能
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

    private void filterTable() {
        String searchText = searchField.getText().trim();
        String selectedSemester = (String) semesterFilter.getSelectedItem();

        if (searchText.isEmpty() && "全部".equals(selectedSemester)) {
            sorter.setRowFilter(null);
        } else {
            RowFilter<DefaultTableModel, Object> filter = new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    // 搜索匹配：课程编号、课程名称、教师名称
                    boolean searchMatch = searchText.isEmpty() ||
                            entry.getStringValue(0).toLowerCase().contains(searchText.toLowerCase()) || // 课程编号
                            entry.getStringValue(1).toLowerCase().contains(searchText.toLowerCase()) || // 课程名称
                            entry.getStringValue(2).toLowerCase().contains(searchText.toLowerCase());   // 教师名称

                    // 学期匹配
                    boolean semesterMatch = "全部".equals(selectedSemester) ||
                            entry.getStringValue(4).equals(selectedSemester); // 学期列

                    return searchMatch && semesterMatch;
                }
            };
            sorter.setRowFilter(filter);
        }
    }


    private void loadCourseData() {
        System.out.println("=== loadCourseData() 开始 ===");
        System.out.println("网络连接状态: " + ClientNetwork.isConnected());

        // 清空现有数据
        int oldRowCount = tableModel.getRowCount();
        tableModel.setRowCount(0);
        System.out.println("清空表格，原有行数: " + oldRowCount);

        try {
            System.out.println("准备调用 ClientNetwork.getAllCourses()");
            ClientNetwork.getAllCourses(new ClientNetwork.CourseCallback() {
                @Override
                public void onCourseResult(boolean success, String message, List<Course> courses) {
                    System.out.println("=== getAllCourses回调被调用 ===");
                    System.out.println("Success: " + success);
                    System.out.println("Message: " + message);
                    System.out.println("Courses count: " + (courses != null ? courses.size() : "null"));

                    SwingUtilities.invokeLater(() -> {
                        if (success && courses != null) {
                            System.out.println("开始添加课程到表格...");
                            for (Course course : courses) {
                                System.out.println("添加课程: " + course.getCourseId() + " - " + course.getCourseName());
                                addCourseToTable(course);
                            }
                            System.out.println("表格最终行数: " + tableModel.getRowCount());
                        } else {
                            System.out.println("加载课程失败: " + message);
                            JOptionPane.showMessageDialog(AdminCourseManagementPanel.this,
                                    "加载课程数据失败: " + message, "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
            });
            System.out.println("ClientNetwork.getAllCourses() 调用完成");
        } catch (Exception e) {
            System.err.println("调用 getAllCourses 时发生异常: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== loadCourseData() 结束 ===");
    }

    private void addCourseToTable(Course course) {
        int currentStudents = course.getCurrentStudents();
        int maxStudents = course.getMaxStudents() != null ? course.getMaxStudents() : 0;
        int remainingCapacity = Math.max(0, maxStudents - currentStudents);

        Object[] rowData = {
                course.getCourseId(),
                course.getCourseName(),
                course.getTeacherName(),
                course.getCredits(),
                course.getSemester(),
                course.getClassTime(),
                course.getClassroom(),
                currentStudents,
                maxStudents,
                remainingCapacity,
                "查看学生", // 这里会被按钮渲染器替换
                "操作" // 这里会被按钮渲染器替换
        };
        tableModel.addRow(rowData);
    }

    private void filterCoursesBySemester() {
        filterTable();
    }

    private void showAddCourseDialog() {
        CourseDialog dialog = new CourseDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "添加课程",
                true,
                null
        );

        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Course newCourse = dialog.getCourse();



            ClientNetwork.createCourse(newCourse, new ClientNetwork.CourseActionCallback() {
                @Override
                public void onActionResult(boolean success, String message) {
                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            JOptionPane.showMessageDialog(AdminCourseManagementPanel.this,
                                    "课程添加成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                            loadCourseData();
                        } else {
                            // 简化的错误处理
                            String errorMsg = "课程添加失败";
                            if (message != null && message.contains("无效的教师信息")) {
                                errorMsg = "教师信息错误，请检查教师ID和姓名是否正确";
                            } else if (message != null) {
                                errorMsg = "课程添加失败: " + message;
                            }
                            JOptionPane.showMessageDialog(AdminCourseManagementPanel.this,
                                    errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
            });
        }
    }

    private void showEditCourseDialog(int row) {
        String courseId = (String) tableModel.getValueAt(row, 0);
        String courseName = (String) tableModel.getValueAt(row, 1);
        String teacherName = (String) tableModel.getValueAt(row, 2);
        Integer credits = (Integer) tableModel.getValueAt(row, 3);
        String semester = (String) tableModel.getValueAt(row, 4);
        String classTime = (String) tableModel.getValueAt(row, 5);
        String classroom = (String) tableModel.getValueAt(row, 6);
        Integer maxStudents = (Integer) tableModel.getValueAt(row, 8);

        Course course = new Course();
        course.setCourseId(courseId);
        course.setCourseName(courseName);
        course.setTeacherName(teacherName);
        course.setCredits(credits);
        course.setSemester(semester);
        course.setClassTime(classTime);
        course.setClassroom(classroom);
        course.setMaxStudents(maxStudents);

        CourseDialog dialog = new CourseDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                "编辑课程",
                true,
                course
        );

        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Course updatedCourse = dialog.getCourse();



            ClientNetwork.updateCourse(updatedCourse, new ClientNetwork.CourseActionCallback() {
                @Override
                public void onActionResult(boolean success, String message) {
                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            JOptionPane.showMessageDialog(AdminCourseManagementPanel.this,
                                    "课程更新成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                            loadCourseData();
                        } else {
                            // 简化的错误处理
                            String errorMsg = "课程更新失败";
                            if (message != null && message.contains("无效的教师信息")) {
                                errorMsg = "教师信息错误，请检查教师ID和姓名是否正确";
                            } else if (message != null) {
                                errorMsg = "课程更新失败: " + message;
                            }
                            JOptionPane.showMessageDialog(AdminCourseManagementPanel.this,
                                    errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
            });
        }
    }

    private void deleteCourse(int row) {
        String courseId = (String) tableModel.getValueAt(row, 0);
        String courseName = (String) tableModel.getValueAt(row, 1);
        Integer currentStudents = (Integer) tableModel.getValueAt(row, 7);

        // 检查是否有学生选课
        if (currentStudents > 0) {
            String confirmMsg = "课程 \"" + courseName + "\" 当前有 " + currentStudents + " 名学生选课。\n"
                    + "删除课程将同时删除所有选课记录，此操作不可撤销。\n"
                    + "确定要删除吗？";

// 步骤2：调用 JOptionPane，传入拼接好的信息
            int result = JOptionPane.showConfirmDialog(
                    this,               // 父组件
                    confirmMsg,         // 提示信息
                    "确认删除",          // 对话框标题
                    JOptionPane.YES_NO_OPTION, // 按钮类型（是/否）
                    JOptionPane.WARNING_MESSAGE // 图标类型（警告）
            );
            if (result != JOptionPane.YES_OPTION) {
                return;
            } }
        else {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "确定要删除课程 \"" + courseName + "\" 吗？\n此操作不可撤销。",
                    "确认删除",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }

        ClientNetwork.deleteCourse(courseId, new ClientNetwork.CourseActionCallback() {
            @Override
            public void onActionResult(boolean success, String message) {
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        JOptionPane.showMessageDialog(AdminCourseManagementPanel.this,
                                "课程删除成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                        loadCourseData();
                    } else {
                        JOptionPane.showMessageDialog(AdminCourseManagementPanel.this,
                                "课程删除失败: " + message, "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        });
    }

    private void showStudentsList(int row) {
        String courseId = (String) tableModel.getValueAt(row, 0);
        String courseName = (String) tableModel.getValueAt(row, 1);

        ClientNetwork.getCourseStudents(courseId, new ClientNetwork.CourseStudentsCallback() {
            @Override
            public void onStudentsResult(boolean success, List<CourseEnrollment> students) {
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        StudentListDialog dialog = new StudentListDialog(
                                (JFrame) SwingUtilities.getWindowAncestor(AdminCourseManagementPanel.this),
                                courseName + " - 学生名单",
                                students
                        );
                        dialog.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(AdminCourseManagementPanel.this,
                                "获取学生名单失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        });
    }

    // 查看学生按钮渲染器
    class ViewStudentsButtonRenderer extends JButton implements TableCellRenderer {
        public ViewStudentsButtonRenderer() {
            setText("查看学生");
            setBackground(new Color(23, 162, 184));
            setForeground(Color.WHITE);
            setBorderPainted(false);
            setFont(new Font("微软雅黑", Font.PLAIN, 11));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // 查看学生按钮编辑器
    class ViewStudentsButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int currentRow;

        public ViewStudentsButtonEditor(JCheckBox checkBox) {
            super(checkBox);

            button = new JButton("查看学生");
            button.setBackground(new Color(23, 162, 184));
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);
            button.setFont(new Font("微软雅黑", Font.PLAIN, 11));

            button.addActionListener(e -> {
                fireEditingStopped();
                showStudentsList(currentRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "查看学生";
        }
    }

    // 操作按钮渲染器
    class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        private JButton editBtn, deleteBtn;

        public ActionButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));

            editBtn = new JButton("编辑");
            editBtn.setPreferredSize(new Dimension(55, 28));
            editBtn.setBackground(new Color(91, 192, 222));
            editBtn.setForeground(Color.WHITE);
            editBtn.setBorderPainted(false);
            editBtn.setFont(new Font("微软雅黑", Font.PLAIN, 10));

            deleteBtn = new JButton("删除");
            deleteBtn.setPreferredSize(new Dimension(55, 28));
            deleteBtn.setBackground(new Color(217, 83, 79));
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setBorderPainted(false);
            deleteBtn.setFont(new Font("微软雅黑", Font.PLAIN, 10));

            add(editBtn);
            add(deleteBtn);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // 操作按钮编辑器
    class ActionButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private JButton editBtn, deleteBtn;
        private int currentRow;

        public ActionButtonEditor(JCheckBox checkBox) {
            super(checkBox);

            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));

            editBtn = new JButton("编辑");
            editBtn.setPreferredSize(new Dimension(55, 28));
            editBtn.setBackground(new Color(91, 192, 222));
            editBtn.setForeground(Color.WHITE);
            editBtn.setBorderPainted(false);
            editBtn.setFont(new Font("微软雅黑", Font.PLAIN, 10));

            deleteBtn = new JButton("删除");
            deleteBtn.setPreferredSize(new Dimension(55, 28));
            deleteBtn.setBackground(new Color(217, 83, 79));
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setBorderPainted(false);
            deleteBtn.setFont(new Font("微软雅黑", Font.PLAIN, 10));

            editBtn.addActionListener(e -> {
                fireEditingStopped();
                showEditCourseDialog(currentRow);
            });

            deleteBtn.addActionListener(e -> {
                fireEditingStopped();
                deleteCourse(currentRow);
            });

            panel.add(editBtn);
            panel.add(deleteBtn);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = table.convertRowIndexToModel(row); // 转换为模型中的行索引
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}