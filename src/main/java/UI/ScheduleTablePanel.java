package UI;



import model.Schedule;
import model.TimeSlot;
import tool.TimeSlotParser;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.List;

public class ScheduleTablePanel extends JPanel {
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private Schedule schedule;

    // 表格列名：时间 + 7天
    private final String[] columnNames = {"时间", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};

    public ScheduleTablePanel() {
        initializeComponents();
        setupLayout();
    }

    private void initializeComponents() {
        // 创建表格模型，13行8列（时间+7天）
        tableModel = new DefaultTableModel(columnNames, 13) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 课表不可编辑
            }
        };

        scheduleTable = new JTable(tableModel);
        setupTable();
        initializeTimeColumn();
    }

    private void setupTable() {
        scheduleTable.setRowHeight(60);
        scheduleTable.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        scheduleTable.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 12));
        scheduleTable.setGridColor(new Color(200, 200, 200));
        scheduleTable.setShowGrid(true);

        // 设置列宽
        TableColumn timeColumn = scheduleTable.getColumnModel().getColumn(0);
        timeColumn.setPreferredWidth(100);
        timeColumn.setMaxWidth(120);
        timeColumn.setMinWidth(80);

        for (int i = 1; i < columnNames.length; i++) {
            TableColumn dayColumn = scheduleTable.getColumnModel().getColumn(i);
            dayColumn.setPreferredWidth(150);
        }

        // 设置单元格渲染器
        scheduleTable.setDefaultRenderer(Object.class, new ScheduleCellRenderer());
    }

    private void initializeTimeColumn() {
        // 初始化时间列
        for (int i = 0; i < 13; i++) {
            int period = i + 1;
            // 使用\n表示换行，并保持代码格式整洁
            String timeDesc = "第" + period + "节\n"
                    + TimeSlotParser.getPeriodTimeDescription(period);
            tableModel.setValueAt(timeDesc, i, 0);
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("课程表"));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void updateSchedule(Schedule newSchedule) {
        this.schedule = newSchedule;
        clearScheduleData();

        if (schedule != null) {
            fillScheduleData();
        }

        scheduleTable.repaint();
    }

    private void clearScheduleData() {
        // 清空课程数据，保留时间列
        for (int row = 0; row < 13; row++) {
            for (int col = 1; col < columnNames.length; col++) {
                tableModel.setValueAt("", row, col);
            }
        }
    }

    private void fillScheduleData() {
        for (String day : Schedule.DAYS_OF_WEEK) {
            int dayColumn = getDayColumnIndex(day);
            if (dayColumn == -1) continue;

            List<TimeSlot> daySlots = schedule.getTimeSlotsForDay(day);

            for (TimeSlot slot : daySlots) {
                fillTimeSlot(slot, dayColumn);
            }
        }
    }

    private void fillTimeSlot(TimeSlot slot, int dayColumn) {
        for (int period = slot.getStartPeriod(); period <= slot.getEndPeriod(); period++) {
            int row = period - 1; // 节数从1开始，数组从0开始

            if (row >= 0 && row < 13) {
                String cellContent = formatCellContent(slot, period);
                tableModel.setValueAt(cellContent, row, dayColumn);
            }
        }
    }

    private String formatCellContent(TimeSlot slot, int currentPeriod) {
        StringBuilder content = new StringBuilder();
        // 修正换行符，并统一代码缩进
        content.append(slot.getCourseName()).append("\n")
                .append(slot.getClassroom()).append("\n")
                .append(slot.getTeacherName());

        // 如果是跨节课程，在第一节显示完整时间信息
        if (currentPeriod == slot.getStartPeriod() && slot.getStartPeriod() != slot.getEndPeriod()) {
            content.append("\n(第").append(slot.getStartPeriod())
                    .append("-").append(slot.getEndPeriod()).append("节)");
        }

        return content.toString();
    }

    private int getDayColumnIndex(String day) {
        for (int i = 1; i < columnNames.length; i++) {
            if (columnNames[i].equals(day)) {
                return i;
            }
        }
        return -1;
    }

    // 自定义单元格渲染器
    private class ScheduleCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // 设置文本对齐
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);

            // 时间列特殊样式
            if (column == 0) {
                c.setBackground(new Color(240, 240, 240));
                c.setFont(new Font("微软雅黑", Font.BOLD, 10));
            } else {
                // 课程单元格样式
                if (value != null && !value.toString().trim().isEmpty()) {
                    c.setBackground(new Color(220, 240, 255)); // 浅蓝色背景
                    c.setFont(new Font("微软雅黑", Font.PLAIN, 10));
                } else {
                    c.setBackground(Color.WHITE);
                }
            }

            // 设置边框
            setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

            return c;
        }
    }
}
