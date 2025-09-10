//package UI;
//
//import model.Course;
//import database.UserDAO;
//import model.User;
//
//import javax.swing.*;
//import javax.swing.event.DocumentEvent;
//import javax.swing.event.DocumentListener;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
//import java.util.List;
//import java.util.ArrayList;
//
//public class CourseDialog extends JDialog {
//    private JTextField courseIdField, courseNameField, teacherIdField, teacherNameField;
//    private JTextField creditsField, semesterField, classTimeField, classroomField, maxStudentsField;
//    private JButton saveButton, cancelButton;
//    private boolean confirmed = false;
//    private Course course;
//
//    // 教师选择相关组件
//    private JComboBox<TeacherItem> teacherComboBox;
//    private JTextField teacherSearchField;
//    private DefaultComboBoxModel<TeacherItem> teacherModel;
//    private List<User> allTeachers; // 缓存所有教师信息
//
//    // 课程编号相关
//    private boolean isEditMode = false;
//
//    public CourseDialog(JFrame parent, String title, boolean modal, Course existingCourse) {
//        super(parent, title, modal);
//        this.course = existingCourse;
//        this.isEditMode = (existingCourse != null);
//
//        initializeComponents();
//        setupLayout();
//        setupEventListeners();
//        loadTeachers(); // 加载教师数据
//        setupCourseId(); // 设置课程编号
//
//        if (existingCourse != null) {
//            populateFields(existingCourse);
//        }
//
//        setSize(550, 600);
//        setLocationRelativeTo(parent);
//        setResizable(false);
//    }
//
//    private void initializeComponents() {
//        // 设置输入框大小
//        Dimension fieldSize = new Dimension(250, 30);
//        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);
//
//        // 课程编号（只读）
//        courseIdField = new JTextField();
//        courseIdField.setPreferredSize(fieldSize);
//        courseIdField.setFont(fieldFont);
//        courseIdField.setEditable(false);
//        courseIdField.setBackground(new Color(245, 245, 245));
//
//        courseNameField = new JTextField();
//        courseNameField.setPreferredSize(fieldSize);
//        courseNameField.setFont(fieldFont);
//
//        // 教师选择组件
//        teacherModel = new DefaultComboBoxModel<>();
//        teacherComboBox = new JComboBox<>(teacherModel);
//        teacherComboBox.setPreferredSize(fieldSize);
//        teacherComboBox.setFont(fieldFont);
//        teacherComboBox.setRenderer(new TeacherListCellRenderer());
//
//        // 教师搜索框
//        teacherSearchField = new JTextField();
//        teacherSearchField.setPreferredSize(fieldSize);
//        teacherSearchField.setFont(fieldFont);
//        teacherSearchField.setToolTipText("输入教师姓名或用户名进行搜索");
//
//        // 隐藏的字段（用于存储实际值）
//        teacherIdField = new JTextField();
//        teacherIdField.setVisible(false);
//        teacherNameField = new JTextField();
//        teacherNameField.setVisible(false);
//
//        creditsField = new JTextField();
//        creditsField.setPreferredSize(fieldSize);
//        creditsField.setFont(fieldFont);
//
//        maxStudentsField = new JTextField();
//        maxStudentsField.setPreferredSize(fieldSize);
//        maxStudentsField.setFont(fieldFont);
//
//        semesterField = new JTextField();
//        semesterField.setPreferredSize(fieldSize);
//        semesterField.setFont(fieldFont);
//
//        classTimeField = new JTextField();
//        classTimeField.setPreferredSize(fieldSize);
//        classTimeField.setFont(fieldFont);
//
//        classroomField = new JTextField();
//        classroomField.setPreferredSize(fieldSize);
//        classroomField.setFont(fieldFont);
//
//        saveButton = new JButton("保存");
//        saveButton.setBackground(new Color(92, 184, 92));
//        saveButton.setForeground(Color.WHITE);
//        saveButton.setBorderPainted(false);
//        saveButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
//        saveButton.setPreferredSize(new Dimension(100, 40));
//
//        cancelButton = new JButton("取消");
//        cancelButton.setBackground(new Color(108, 117, 125));
//        cancelButton.setForeground(Color.WHITE);
//        cancelButton.setBorderPainted(false);
//        cancelButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
//        cancelButton.setPreferredSize(new Dimension(100, 40));
//    }
//
//    private void setupLayout() {
//        setLayout(new BorderLayout());
//
//        JPanel formPanel = new JPanel(new GridBagLayout());
//        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
//        formPanel.setBackground(Color.WHITE);
//
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(12, 10, 12, 10);
//        gbc.anchor = GridBagConstraints.WEST;
//
//        Font labelFont = new Font("微软雅黑", Font.PLAIN, 14);
//
//        // 课程编号
//        gbc.gridx = 0; gbc.gridy = 0;
//        JLabel courseIdLabel = new JLabel("课程编号:");
//        courseIdLabel.setFont(labelFont);
//        formPanel.add(courseIdLabel, gbc);
//        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
//        formPanel.add(courseIdField, gbc);
//
//        // 课程名称
//        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
//        JLabel courseNameLabel = new JLabel("课程名称:");
//        courseNameLabel.setFont(labelFont);
//        formPanel.add(courseNameLabel, gbc);
//        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
//        formPanel.add(courseNameField, gbc);
//
//        // 教师搜索
//        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
//        JLabel teacherSearchLabel = new JLabel("搜索教师:");
//        teacherSearchLabel.setFont(labelFont);
//        formPanel.add(teacherSearchLabel, gbc);
//        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
//        formPanel.add(teacherSearchField, gbc);
//
//        // 教师选择
//        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
//        JLabel teacherLabel = new JLabel("选择教师:");
//        teacherLabel.setFont(labelFont);
//        formPanel.add(teacherLabel, gbc);
//        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
//        formPanel.add(teacherComboBox, gbc);
//
//        // 学分
//        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
//        JLabel creditsLabel = new JLabel("学分:");
//        creditsLabel.setFont(labelFont);
//        formPanel.add(creditsLabel, gbc);
//        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
//        formPanel.add(creditsField, gbc);
//
//        // 课容量
//        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
//        JLabel maxStudentsLabel = new JLabel("课容量:");
//        maxStudentsLabel.setFont(labelFont);
//        formPanel.add(maxStudentsLabel, gbc);
//        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
//        formPanel.add(maxStudentsField, gbc);
//
//        // 学期
//        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
//        JLabel semesterLabel = new JLabel("学期:");
//        semesterLabel.setFont(labelFont);
//        formPanel.add(semesterLabel, gbc);
//        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
//        formPanel.add(semesterField, gbc);
//
//        // 上课时间
//        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
//        JLabel classTimeLabel = new JLabel("上课时间:");
//        classTimeLabel.setFont(labelFont);
//        formPanel.add(classTimeLabel, gbc);
//        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
//        formPanel.add(classTimeField, gbc);
//
//        // 教室
//        gbc.gridx = 0; gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
//        JLabel classroomLabel = new JLabel("教室:");
//        classroomLabel.setFont(labelFont);
//        formPanel.add(classroomLabel, gbc);
//        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
//        formPanel.add(classroomField, gbc);
//
//        // 按钮面板
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
//        buttonPanel.setBackground(Color.WHITE);
//        buttonPanel.add(saveButton);
//        buttonPanel.add(cancelButton);
//
//        add(formPanel, BorderLayout.CENTER);
//        add(buttonPanel, BorderLayout.SOUTH);
//
//        getContentPane().setBackground(Color.WHITE);
//    }
//
//    private void setupEventListeners() {
//        saveButton.addActionListener(e -> saveCourse());
//        cancelButton.addActionListener(e -> {
//            confirmed = false;
//            dispose();
//        });
//
//        // 教师搜索框事件
//        teacherSearchField.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                filterTeachers();
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                filterTeachers();
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                filterTeachers();
//            }
//        });
//
//        // 教师搜索框回车事件
//        teacherSearchField.addKeyListener(new KeyAdapter() {
//            @Override
//            public void keyPressed(KeyEvent e) {
//                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//                    if (teacherComboBox.getItemCount() > 0) {
//                        teacherComboBox.showPopup();
//                    }
//                }
//            }
//        });
//
//        // 教师选择事件
//        teacherComboBox.addActionListener(e -> {
//            TeacherItem selected = (TeacherItem) teacherComboBox.getSelectedItem();
//            if (selected != null) {
//                teacherIdField.setText(selected.getUserId());
//                teacherNameField.setText(selected.getUserName());
//            }
//        });
//    }
//
//    private void loadTeachers() {
//        // 使用SwingUtilities.invokeLater替代SwingWorker
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    UserDAO userDAO = new UserDAO();
//                    allTeachers = userDAO.getAllTeachers();
//                    SwingUtilities.invokeLater(() -> updateTeacherComboBox(""));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    SwingUtilities.invokeLater(() -> {
//                        JOptionPane.showMessageDialog(CourseDialog.this,
//                                "加载教师列表失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
//                    });
//                }
//            }
//        });
//    }
//
//    private void filterTeachers() {
//        String searchText = teacherSearchField.getText().trim().toLowerCase();
//        updateTeacherComboBox(searchText);
//    }
//
//    private void updateTeacherComboBox(String searchText) {
//        teacherModel.removeAllElements();
//        teacherModel.addElement(new TeacherItem("", "请选择教师", ""));
//
//        if (allTeachers != null) {
//            for (User teacher : allTeachers) {
//                String displayText = teacher.getNickname() + " (" + teacher.getUsername() + ")";
//                if (searchText.isEmpty() ||
//                        teacher.getNickname().toLowerCase().contains(searchText) ||
//                        teacher.getUsername().toLowerCase().contains(searchText)) {
//                    teacherModel.addElement(new TeacherItem(teacher.getUsername(), teacher.getNickname(), displayText));
//                }
//            }
//        }
//
//        if (teacherModel.getSize() > 0) {
//            teacherComboBox.setSelectedIndex(0);
//        }
//    }
//
//    private void setupCourseId() {
//        if (!isEditMode) {
//            // 使用简单的时间戳方式生成课程编号
//            String nextId = "CS" + (System.currentTimeMillis() % 100000);
//            courseIdField.setText(nextId);
//        }
//    }
//
//    private void populateFields(Course course) {
//        courseIdField.setText(course.getCourseId());
//        courseNameField.setText(course.getCourseName());
//
//        // 设置教师信息
//        if (allTeachers != null) {
//            for (User teacher : allTeachers) {
//                if (teacher.getUsername().equals(course.getTeacherId())) {
//                    teacherSearchField.setText(teacher.getNickname());
//                    // 在下拉框中选择对应的教师
//                    for (int i = 0; i < teacherModel.getSize(); i++) {
//                        TeacherItem item = teacherModel.getElementAt(i);
//                        if (item.getUserId().equals(course.getTeacherId())) {
//                            teacherComboBox.setSelectedItem(item);
//                            break;
//                        }
//                    }
//                    break;
//                }
//            }
//        }
//
//        creditsField.setText(String.valueOf(course.getCredits()));
//        maxStudentsField.setText(String.valueOf(course.getMaxStudents() != null ? course.getMaxStudents() : 50));
//        semesterField.setText(course.getSemester());
//        classTimeField.setText(course.getClassTime());
//        classroomField.setText(course.getClassroom());
//
//        // 编辑模式下课程编号不可修改
//        courseIdField.setEditable(false);
//    }
//
//    private void saveCourse() {
//        // 验证输入
//        if (!validateInputs()) {
//            return;
//        }
//
//        // 创建或更新课程对象
//        if (course == null) {
//            course = new Course();
//        }
//
//        course.setCourseId(courseIdField.getText().trim());
//        course.setCourseName(courseNameField.getText().trim());
//
//        // 从隐藏字段获取教师信息
//        course.setTeacherId(teacherIdField.getText().trim());
//        course.setTeacherName(teacherNameField.getText().trim());
//
//        course.setCredits(Integer.parseInt(creditsField.getText().trim()));
//        course.setMaxStudents(Integer.parseInt(maxStudentsField.getText().trim()));
//        course.setSemester(semesterField.getText().trim());
//        course.setClassTime(classTimeField.getText().trim());
//        course.setClassroom(classroomField.getText().trim());
//
//        confirmed = true;
//        dispose();
//    }
//
//    private boolean validateInputs() {
//        if (courseIdField.getText().trim().isEmpty()) {
//            showError("课程编号生成失败");
//            return false;
//        }
//
//        if (courseNameField.getText().trim().isEmpty()) {
//            showError("请输入课程名称");
//            courseNameField.requestFocus();
//            return false;
//        }
//
//        // 检查是否选择了教师
//        TeacherItem selectedTeacher = (TeacherItem) teacherComboBox.getSelectedItem();
//        if (selectedTeacher == null || selectedTeacher.getUserId().isEmpty()) {
//            showError("请选择授课教师");
//            teacherComboBox.requestFocus();
//            return false;
//        }
//
//        try {
//            int credits = Integer.parseInt(creditsField.getText().trim());
//            if (credits <= 0 || credits > 10) {
//                showError("学分必须在1-10之间");
//                creditsField.requestFocus();
//                return false;
//            }
//        } catch (NumberFormatException e) {
//            showError("请输入有效的学分数值");
//            creditsField.requestFocus();
//            return false;
//        }
//
//        try {
//            int maxStudents = Integer.parseInt(maxStudentsField.getText().trim());
//            if (maxStudents <= 0 || maxStudents > 500) {
//                showError("课容量必须在1-500之间");
//                maxStudentsField.requestFocus();
//                return false;
//            }
//        } catch (NumberFormatException e) {
//            showError("请输入有效的课容量数值");
//            maxStudentsField.requestFocus();
//            return false;
//        }
//
//        if (semesterField.getText().trim().isEmpty()) {
//            showError("请输入学期");
//            semesterField.requestFocus();
//            return false;
//        }
//
//        if (classTimeField.getText().trim().isEmpty()) {
//            showError("请输入上课时间");
//            classTimeField.requestFocus();
//            return false;
//        }
//
//        if (classroomField.getText().trim().isEmpty()) {
//            showError("请输入教室");
//            classroomField.requestFocus();
//            return false;
//        }
//
//        return true;
//    }
//
//    private void showError(String message) {
//        JOptionPane.showMessageDialog(this, message, "输入错误", JOptionPane.ERROR_MESSAGE);
//    }
//
//    public boolean isConfirmed() {
//        return confirmed;
//    }
//
//    public Course getCourse() {
//        return course;
//    }
//
//    // 教师列表项类
//    private static class TeacherItem {
//        private String userId;
//        private String userName;
//        private String displayText;
//
//        public TeacherItem(String userId, String userName, String displayText) {
//            this.userId = userId;
//            this.userName = userName;
//            this.displayText = displayText;
//        }
//
//        public String getUserId() {
//            return userId;
//        }
//
//        public String getUserName() {
//            return userName;
//        }
//
//        public String getDisplayText() {
//            return displayText;
//        }
//
//        @Override
//        public String toString() {
//            return displayText;
//        }
//    }
//
//    // 教师列表渲染器
//    private static class TeacherListCellRenderer extends DefaultListCellRenderer {
//        @Override
//        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
//                                                      boolean isSelected, boolean cellHasFocus) {
//            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//
//            if (value instanceof TeacherItem) {
//                setText(((TeacherItem) value).getDisplayText());
//            }
//
//            return this;
//        }
//    }
//}

package UI;

import model.Course;
import database.UserDAO;
import model.User;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ArrayList;
//import java.util.concurrent.SwingWorker;

public class CourseDialog extends JDialog {
    private JTextField courseIdField, courseNameField, teacherIdField, teacherNameField;
    private JTextField creditsField, semesterField, classroomField, maxStudentsField;
    private JButton saveButton, cancelButton;
    private boolean confirmed = false;
    private Course course;

    // 教师选择相关组件
    private JComboBox<TeacherItem> teacherComboBox;
    private JTextField teacherSearchField;
    private DefaultComboBoxModel<TeacherItem> teacherModel;
    private List<User> allTeachers;

    // 课程时间相关组件
    private JPanel timeInputPanel;
    private List<TimeSlotPanel> timeSlotPanels;
    private JButton addTimeSlotButton;
    private JTextField classTimeField; // 用于显示最终格式化的时间

    // 课程编号相关
    private boolean isEditMode = false;

    public CourseDialog(JFrame parent, String title, boolean modal, Course existingCourse) {
        super(parent, title, modal);
        this.course = existingCourse;
        this.isEditMode = (existingCourse != null);
        this.timeSlotPanels = new ArrayList<>();

        initializeComponents();
        setupLayout();
        setupEventListeners();
        loadTeachers();
        setupCourseId();

        if (existingCourse != null) {
            populateFields(existingCourse);
        }

        setSize(600, 700);
        setLocationRelativeTo(parent);
        setResizable(true);
    }

    private void initializeComponents() {
        // 设置输入框大小
        Dimension fieldSize = new Dimension(250, 30);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);

        // 课程编号（只读）
        courseIdField = new JTextField();
        courseIdField.setPreferredSize(fieldSize);
        courseIdField.setFont(fieldFont);
        courseIdField.setEditable(false);
        courseIdField.setBackground(new Color(245, 245, 245));

        courseNameField = new JTextField();
        courseNameField.setPreferredSize(fieldSize);
        courseNameField.setFont(fieldFont);

        // 教师选择组件
        teacherModel = new DefaultComboBoxModel<>();
        teacherComboBox = new JComboBox<>(teacherModel);
        teacherComboBox.setPreferredSize(fieldSize);
        teacherComboBox.setFont(fieldFont);
        teacherComboBox.setRenderer(new TeacherListCellRenderer());

        // 教师搜索框
        teacherSearchField = new JTextField();
        teacherSearchField.setPreferredSize(fieldSize);
        teacherSearchField.setFont(fieldFont);
        teacherSearchField.setToolTipText("输入教师姓名或用户名进行搜索");

        // 隐藏的字段（用于存储实际值）
        teacherIdField = new JTextField();
        teacherIdField.setVisible(false);
        teacherNameField = new JTextField();
        teacherNameField.setVisible(false);

        creditsField = new JTextField("3");
        creditsField.setPreferredSize(fieldSize);
        creditsField.setFont(fieldFont);

        maxStudentsField = new JTextField("50");
        maxStudentsField.setPreferredSize(fieldSize);
        maxStudentsField.setFont(fieldFont);

        semesterField = new JTextField();
        semesterField.setPreferredSize(fieldSize);
        semesterField.setFont(fieldFont);

        classroomField = new JTextField();
        classroomField.setPreferredSize(fieldSize);
        classroomField.setFont(fieldFont);

        // 课程时间组件
        timeInputPanel = new JPanel();
        timeInputPanel.setLayout(new BoxLayout(timeInputPanel, BoxLayout.Y_AXIS));
        timeInputPanel.setBorder(BorderFactory.createTitledBorder("上课时间设置"));

        addTimeSlotButton = new JButton("添加时间段");
        addTimeSlotButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        // 隐藏的格式化时间字段
        classTimeField = new JTextField();
        classTimeField.setVisible(false);

        saveButton = new JButton("保存");
        saveButton.setBackground(new Color(92, 184, 92));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBorderPainted(false);
        saveButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        saveButton.setPreferredSize(new Dimension(100, 40));

        cancelButton = new JButton("取消");
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBorderPainted(false);
        cancelButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        cancelButton.setPreferredSize(new Dimension(100, 40));

        // 添加第一个时间槽
        addTimeSlot();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("微软雅黑", Font.PLAIN, 14);

        // 课程编号
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel courseIdLabel = new JLabel("课程编号:");
        courseIdLabel.setFont(labelFont);
        formPanel.add(courseIdLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(courseIdField, gbc);

        // 课程名称
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel courseNameLabel = new JLabel("课程名称:");
        courseNameLabel.setFont(labelFont);
        formPanel.add(courseNameLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(courseNameField, gbc);

        // 教师搜索
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel teacherSearchLabel = new JLabel("搜索教师:");
        teacherSearchLabel.setFont(labelFont);
        formPanel.add(teacherSearchLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(teacherSearchField, gbc);

        // 教师选择
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel teacherLabel = new JLabel("选择教师:");
        teacherLabel.setFont(labelFont);
        formPanel.add(teacherLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(teacherComboBox, gbc);

        // 学分
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel creditsLabel = new JLabel("学分:");
        creditsLabel.setFont(labelFont);
        formPanel.add(creditsLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(creditsField, gbc);

        // 课容量
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel maxStudentsLabel = new JLabel("课容量:");
        maxStudentsLabel.setFont(labelFont);
        formPanel.add(maxStudentsLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(maxStudentsField, gbc);

        // 学期
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel semesterLabel = new JLabel("学期:");
        semesterLabel.setFont(labelFont);
        formPanel.add(semesterLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(semesterField, gbc);

        // 教室
        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel classroomLabel = new JLabel("教室:");
        classroomLabel.setFont(labelFont);
        formPanel.add(classroomLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(classroomField, gbc);

        // 课程时间
        gbc.gridx = 0; gbc.gridy = 8; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel classTimeLabel = new JLabel("上课时间:");
        classTimeLabel.setFont(labelFont);
        formPanel.add(classTimeLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;


        JPanel timePanel = new JPanel(new BorderLayout());
        timePanel.setPreferredSize(new Dimension(250, 220)); // 增加整体面板高度

        JScrollPane timeScrollPane = new JScrollPane(timeInputPanel);
        timeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        timeScrollPane.setMinimumSize(new Dimension(250, 100));  // 增加最小高度
        timeScrollPane.setPreferredSize(new Dimension(250, 100)); // 增加首选高度

        JPanel timeControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timeControlPanel.add(addTimeSlotButton);

        JPanel timeFullPanel = new JPanel(new BorderLayout());
        timeFullPanel.add(timeScrollPane, BorderLayout.CENTER);
        timeFullPanel.add(timeControlPanel, BorderLayout.SOUTH);

        formPanel.add(timeFullPanel, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().setBackground(Color.WHITE);
    }

    private void setupEventListeners() {
        saveButton.addActionListener(e -> saveCourse());
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        addTimeSlotButton.addActionListener(e -> addTimeSlot());

        // 教师搜索框事件
        teacherSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTeachers();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTeachers();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTeachers();
            }
        });

        // 教师搜索框回车事件
        teacherSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (teacherComboBox.getItemCount() > 0) {
                        teacherComboBox.showPopup();
                    }
                }
            }
        });

        // 教师选择事件
        teacherComboBox.addActionListener(e -> {
            TeacherItem selected = (TeacherItem) teacherComboBox.getSelectedItem();
            if (selected != null) {
                teacherIdField.setText(selected.getUserId());
                teacherNameField.setText(selected.getUserName());
            }
        });
    }

    // 添加时间槽
    private void addTimeSlot() {
        TimeSlotPanel timeSlotPanel = new TimeSlotPanel();
        timeSlotPanels.add(timeSlotPanel);
        timeInputPanel.add(timeSlotPanel);
        timeInputPanel.revalidate();
        timeInputPanel.repaint();
    }

    // 删除时间槽
    private void removeTimeSlot(TimeSlotPanel panel) {
        if (timeSlotPanels.size() > 1) { // 至少保留一个时间槽
            timeSlotPanels.remove(panel);
            timeInputPanel.remove(panel);
            timeInputPanel.revalidate();
            timeInputPanel.repaint();
        }
    }

//    private void loadTeachers() {
//        SwingWorker<List<User>, Void> worker = new SwingWorker<List<User>, Void>() {
//            @Override
//            protected List<User> doInBackground() throws Exception {
//                UserDAO userDAO = new UserDAO();
//                return userDAO.getAllTeachers();
//            }
//
//            @Override
//            protected void done() {
//                try {
//                    List<User> teachers = get();
//                    allTeachers = teachers;
//                    updateTeacherComboBox("");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    JOptionPane.showMessageDialog(CourseDialog.this,
//                            "加载教师列表失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
//                }
//            }
//        };
//        worker.execute();
//    }
        private void loadTeachers() {
        // 使用SwingUtilities.invokeLater替代SwingWorker
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UserDAO userDAO = new UserDAO();
                    allTeachers = userDAO.getAllTeachers();
                    SwingUtilities.invokeLater(() -> updateTeacherComboBox(""));
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(CourseDialog.this,
                                "加载教师列表失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }
        });
    }
    private void filterTeachers() {
        String searchText = teacherSearchField.getText().trim().toLowerCase();
        updateTeacherComboBox(searchText);
    }

    private void updateTeacherComboBox(String searchText) {
        teacherModel.removeAllElements();
        teacherModel.addElement(new TeacherItem("", "请选择教师", ""));

        if (allTeachers != null) {
            for (User teacher : allTeachers) {
                String displayText = teacher.getNickname() + " (" + teacher.getUsername() + ")";
                if (searchText.isEmpty() ||
                        teacher.getNickname().toLowerCase().contains(searchText) ||
                        teacher.getUsername().toLowerCase().contains(searchText)) {
                    teacherModel.addElement(new TeacherItem(teacher.getUsername(), teacher.getNickname(), displayText));
                }
            }
        }

        if (teacherModel.getSize() > 0) {
            teacherComboBox.setSelectedIndex(0);
        }
    }

    private void setupCourseId() {
        if (!isEditMode) {
            courseIdField.setText("CS" + (System.currentTimeMillis() % 100000));
        }
    }

    private void populateFields(Course course) {
        courseIdField.setText(course.getCourseId());
        courseNameField.setText(course.getCourseName());

        // 设置教师信息
        if (allTeachers != null) {
            for (User teacher : allTeachers) {
                if (teacher.getUsername().equals(course.getTeacherId())) {
                    teacherSearchField.setText(teacher.getNickname());
                    for (int i = 0; i < teacherModel.getSize(); i++) {
                        TeacherItem item = teacherModel.getElementAt(i);
                        if (item.getUserId().equals(course.getTeacherId())) {
                            teacherComboBox.setSelectedItem(item);
                            break;
                        }
                    }
                    break;
                }
            }
        }

        creditsField.setText(String.valueOf(course.getCredits()));
        maxStudentsField.setText(String.valueOf(course.getMaxStudents() != null ? course.getMaxStudents() : 50));
        semesterField.setText(course.getSemester());
        classroomField.setText(course.getClassroom());

        // 解析并设置课程时间
        parseAndSetClassTime(course.getClassTime());

        courseIdField.setEditable(false);
    }

    // 解析已有的课程时间并设置到界面
    private void parseAndSetClassTime(String classTime) {
        if (classTime == null || classTime.isEmpty()) {
            return;
        }

        // 清空现有的时间槽
        timeSlotPanels.clear();
        timeInputPanel.removeAll();

        // 按分号分割不同的时间段
        String[] timeSlots = classTime.split("；");
        for (String slot : timeSlots) {
            TimeSlotPanel timeSlotPanel = new TimeSlotPanel();
            timeSlotPanel.parseTimeSlot(slot);
            timeSlotPanels.add(timeSlotPanel);
            timeInputPanel.add(timeSlotPanel);
        }

        timeInputPanel.revalidate();
        timeInputPanel.repaint();
    }

    // 获取格式化的课程时间字符串
    private String getFormattedClassTime() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (TimeSlotPanel panel : timeSlotPanels) {
            String timeSlot = panel.getTimeSlotString();
            if (timeSlot != null && !timeSlot.isEmpty()) {
                if (!first) {
                    sb.append("；");
                }
                sb.append(timeSlot);
                first = false;
            }
        }

        return sb.toString();
    }

    private void saveCourse() {
        // 验证输入
        if (!validateInputs()) {
            return;
        }

        // 创建或更新课程对象
        if (course == null) {
            course = new Course();
        }

        course.setCourseId(courseIdField.getText().trim());
        course.setCourseName(courseNameField.getText().trim());
        course.setTeacherId(teacherIdField.getText().trim());
        course.setTeacherName(teacherNameField.getText().trim());
        course.setCredits(Integer.parseInt(creditsField.getText().trim()));
        course.setMaxStudents(Integer.parseInt(maxStudentsField.getText().trim()));
        course.setSemester(semesterField.getText().trim());
        course.setClassTime(getFormattedClassTime()); // 获取格式化的时间
        course.setClassroom(classroomField.getText().trim());

        confirmed = true;
        dispose();
    }

    private boolean validateInputs() {
        if (courseIdField.getText().trim().isEmpty()) {
            showError("课程编号生成失败");
            return false;
        }

        if (courseNameField.getText().trim().isEmpty()) {
            showError("请输入课程名称");
            courseNameField.requestFocus();
            return false;
        }

        // 检查是否选择了教师
        TeacherItem selectedTeacher = (TeacherItem) teacherComboBox.getSelectedItem();
        if (selectedTeacher == null || selectedTeacher.getUserId().isEmpty()) {
            showError("请选择授课教师");
            teacherComboBox.requestFocus();
            return false;
        }

        try {
            int credits = Integer.parseInt(creditsField.getText().trim());
            if (credits <= 0 || credits > 10) {
                showError("学分必须在1-10之间");
                creditsField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("请输入有效的学分数值");
            creditsField.requestFocus();
            return false;
        }

        try {
            int maxStudents = Integer.parseInt(maxStudentsField.getText().trim());
            if (maxStudents <= 0 || maxStudents > 500) {
                showError("课容量必须在1-500之间");
                maxStudentsField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("请输入有效的课容量数值");
            maxStudentsField.requestFocus();
            return false;
        }

        if (semesterField.getText().trim().isEmpty()) {
            showError("请输入学期");
            semesterField.requestFocus();
            return false;
        }

        // 验证课程时间
        String formattedTime = getFormattedClassTime();
        if (formattedTime.isEmpty()) {
            showError("请至少添加一个上课时间段");
            return false;
        }

        if (classroomField.getText().trim().isEmpty()) {
            showError("请输入教室");
            classroomField.requestFocus();
            return false;
        }

        return true;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "输入错误", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Course getCourse() {
        return course;
    }

    // 时间槽面板类
    private class TimeSlotPanel extends JPanel {
        private JComboBox<String> dayComboBox;
        private JTextField startPeriodField;
        private JTextField endPeriodField;
        private JButton removeButton;

        public TimeSlotPanel() {
            initializeComponents();
            setupLayout();
            setupEventListeners();
        }

        private void initializeComponents() {
            // 星期选择
            dayComboBox = new JComboBox<>(new String[]{"周一", "周二", "周三", "周四", "周五", "周六", "周日"});
            dayComboBox.setPreferredSize(new Dimension(60, 25));
            dayComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));

            // 开始节数
            startPeriodField = new JTextField();
            startPeriodField.setPreferredSize(new Dimension(30, 25));
            startPeriodField.setHorizontalAlignment(JTextField.CENTER);
            startPeriodField.setFont(new Font("微软雅黑", Font.PLAIN, 12));

            // 结束节数
            endPeriodField = new JTextField();
            endPeriodField.setPreferredSize(new Dimension(30, 25));
            endPeriodField.setHorizontalAlignment(JTextField.CENTER);
            endPeriodField.setFont(new Font("微软雅黑", Font.PLAIN, 12));

            // 删除按钮
            removeButton = new JButton("×");
            removeButton.setPreferredSize(new Dimension(25, 25));
            removeButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
            removeButton.setToolTipText("删除此时间段");
        }

        private void setupLayout() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
            setBackground(new Color(248, 248, 248));
            setBorder(BorderFactory.createEtchedBorder());

            add(new JLabel("周"));
            add(dayComboBox);
            add(new JLabel("第"));
            add(startPeriodField);
            add(new JLabel("-"));
            add(endPeriodField);
            add(new JLabel("节"));
            add(removeButton);
        }

        private void setupEventListeners() {
            removeButton.addActionListener(e -> removeTimeSlot(this));

            // 限制输入为数字
            startPeriodField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!(Character.isDigit(c) || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE)) {
                        e.consume();
                    }
                }
            });

            endPeriodField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    if (!(Character.isDigit(c) || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE)) {
                        e.consume();
                    }
                }
            });
        }

        // 获取时间槽字符串
        public String getTimeSlotString() {
            String day = (String) dayComboBox.getSelectedItem();
            String start = startPeriodField.getText().trim();
            String end = endPeriodField.getText().trim();

            if (start.isEmpty() || end.isEmpty()) {
                return "";
            }

            try {
                int startNum = Integer.parseInt(start);
                int endNum = Integer.parseInt(end);

                if (startNum <= 0 || endNum <= 0 || startNum > endNum) {
                    return "";
                }

                return day + " " + startNum + "-" + endNum;
            } catch (NumberFormatException e) {
                return "";
            }
        }

        // 解析时间槽字符串
        public void parseTimeSlot(String timeSlot) {
            if (timeSlot == null || timeSlot.isEmpty()) {
                return;
            }

            // 格式：周一 5-8
            String[] parts = timeSlot.split(" ");
            if (parts.length == 2) {
                // 设置星期
                for (int i = 0; i < dayComboBox.getItemCount(); i++) {
                    if (dayComboBox.getItemAt(i).equals(parts[0])) {
                        dayComboBox.setSelectedIndex(i);
                        break;
                    }
                }

                // 设置节数范围
                String[] periods = parts[1].split("-");
                if (periods.length == 2) {
                    startPeriodField.setText(periods[0]);
                    endPeriodField.setText(periods[1]);
                }
            }
        }
    }

    // 教师列表项类
    private static class TeacherItem {
        private String userId;
        private String userName;
        private String displayText;

        public TeacherItem(String userId, String userName, String displayText) {
            this.userId = userId;
            this.userName = userName;
            this.displayText = displayText;
        }

        public String getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public String getDisplayText() {
            return displayText;
        }

        @Override
        public String toString() {
            return displayText;
        }
    }

    // 教师列表渲染器
    private static class TeacherListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof TeacherItem) {
                setText(((TeacherItem) value).getDisplayText());
            }

            return this;
        }
    }
}