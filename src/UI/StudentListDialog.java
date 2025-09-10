package UI;

import model.CourseEnrollment;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentListDialog extends JDialog {
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JLabel totalStudentsLabel;

    private final String[] columnNames = {
            "学生学号", "学生姓名", "选课状态", "选课时间", "学分"
    };

    public StudentListDialog(JFrame parent, String title, List<CourseEnrollment> students) {
        super(parent, title, true);

        initializeComponents();
        setupLayout();
        populateStudentData(students);

        setSize(600, 400);
        setLocationRelativeTo(parent);
        setResizable(true);
    }

    private void initializeComponents() {
        // 初始化表格
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 所有单元格不可编辑
            }
        };

        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(40);
        studentTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        studentTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 设置列宽
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(120); // 学生学号
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(100); // 学生姓名
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // 选课状态
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(150); // 选课时间
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(60);  // 学分

        // 总人数标签
        totalStudentsLabel = new JLabel();
        totalStudentsLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        totalStudentsLabel.setForeground(new Color(51, 51, 51));
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // 顶部信息面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        topPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("学生名单");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setForeground(new Color(51, 51, 51));

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(totalStudentsLabel, BorderLayout.EAST);

        // 表格滚动面板
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // 底部按钮面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));
        bottomPanel.setBackground(Color.WHITE);

        JButton exportButton = new JButton("导出Excel");
        exportButton.setBackground(new Color(40, 167, 69));
        exportButton.setForeground(Color.WHITE);
        exportButton.setBorderPainted(false);
        exportButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        exportButton.setPreferredSize(new Dimension(100, 35));
        exportButton.addActionListener(e -> exportToExcel());

        JButton closeButton = new JButton("关闭");
        closeButton.setBackground(new Color(108, 117, 125));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBorderPainted(false);
        closeButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        closeButton.setPreferredSize(new Dimension(80, 35));
        closeButton.addActionListener(e -> dispose());

        bottomPanel.add(exportButton);
        bottomPanel.add(Box.createHorizontalStrut(10));
        bottomPanel.add(closeButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        getContentPane().setBackground(Color.WHITE);
    }

    private void populateStudentData(List<CourseEnrollment> students) {
        System.out.println("=== 填充学生数据开始 ===");
        System.out.println("收到的学生数据数量: " + (students != null ? students.size() : 0));

        if (students == null || students.isEmpty()) {
            totalStudentsLabel.setText("总人数: 0");
            System.out.println("没有学生数据，显示空表格");
            return;
        }

        // 清空现有数据
        tableModel.setRowCount(0);

        // 添加学生数据
        for (int i = 0; i < students.size(); i++) {
            CourseEnrollment enrollment = students.get(i);
            System.out.println("处理第 " + (i + 1) + " 个学生:");
            System.out.println("  学生ID: " + enrollment.getStudentId());
            System.out.println("  学生姓名: " + enrollment.getStudentName());
            System.out.println("  状态: " + enrollment.getStatus());
            System.out.println("  选课时间: " + enrollment.getEnrollmentDate());
            System.out.println("  学分: " + enrollment.getCredits());

            // 修复：正确填充所有5个字段
            Object[] rowData = {
                    // 学生学号
                    enrollment.getStudentId() != null ? enrollment.getStudentId() : "未知",

                    // 学生姓名 - 修复：之前缺少这个字段
                    enrollment.getStudentName() != null ? enrollment.getStudentName() : getDisplayNameFromId(enrollment.getStudentId()),

                    // 选课状态
                    enrollment.getStatus() != null ? getStatusDisplay(enrollment.getStatus()) : "已选课",

                    // 选课时间
                    enrollment.getEnrollmentDate() != null ?
                            enrollment.getEnrollmentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "未知",

                    // 学分 - 修复：之前缺少这个字段
                    enrollment.getCredits() > 0 ? enrollment.getCredits() : "未设置"
            };

            tableModel.addRow(rowData);
            System.out.println("成功添加第 " + (i + 1) + " 行数据");
        }

        // 更新总人数
        totalStudentsLabel.setText("总人数: " + students.size());
        System.out.println("学生数据填充完成，总人数: " + students.size());
        System.out.println("=== 填充学生数据结束 ===");
    }

    // 新增：根据学生ID生成显示名称（基于首字母识别身份）
    private String getDisplayNameFromId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return "未知用户";
        }

        String id = studentId.trim();
        char firstChar = id.charAt(0);

        // 根据首字母判断身份
        if (firstChar >= '0' && firstChar <= '9') {
            return "学生-" + id; // 数字开头为学生
        } else if (firstChar >= 'A' && firstChar <= 'Z') {
            return "教师-" + id; // 大写字母开头为教师
        } else if (firstChar >= 'a' && firstChar <= 'z') {
            return "管理员-" + id; // 小写字母开头为管理员
        } else {
            return "用户-" + id; // 其他情况
        }
    }

    // 新增：状态显示转换
    private String getStatusDisplay(String status) {
        if (status == null) {
            return "已选课";
        }

        switch (status.toLowerCase()) {
            case "active":
                return "已选课";
            case "dropped":
                return "已退课";
            case "pending":
                return "待确认";
            default:
                return status;
        }
    }

    private void exportToExcel() {
        // 这里可以实现导出Excel功能
        // 使用Apache POI库来导出Excel文件
        JOptionPane.showMessageDialog(this,
                "导出功能开发中...",
                "提示",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
