package tool;

import model.Course;
import model.TimeSlot;
import java.util.*;

public class TimeSlotParser {

    /**
     * 解析课程时间字符串，生成时间段列表
     * 输入格式: "周一 8-9；周二 1-10；"
     */
    public static List<TimeSlot> parseClassTime(Course course) {
        List<TimeSlot> timeSlots = new ArrayList<>();

        if (course.getClassTime() == null || course.getClassTime().trim().isEmpty()) {
            return timeSlots;
        }

        String classTime = course.getClassTime().trim();

        // 按分号分割不同的时间段
        String[] timeSegments = classTime.split("；|;");

        for (String segment : timeSegments) {
            segment = segment.trim();
            if (segment.isEmpty()) continue;

            try {
                TimeSlot timeSlot = parseTimeSegment(segment, course);
                if (timeSlot != null) {
                    timeSlots.add(timeSlot);
                }
            } catch (Exception e) {
                System.err.println("解析时间段失败: " + segment + " - " + e.getMessage());
            }
        }

        return timeSlots;
    }

    /**
     * 解析单个时间段
     * 格式: "周一 8-9" 或 "周二 1-10"
     */
    private static TimeSlot parseTimeSegment(String segment, Course course) {
        // 按空格分割星期和节数
        String[] parts = segment.split("\\s+");
        if (parts.length != 2) {
            System.err.println("时间段格式错误: " + segment);
            return null;
        }

        String dayOfWeek = parts[0].trim();
        String periodRange = parts[1].trim();

        // 验证星期格式
        if (!isValidDay(dayOfWeek)) {
            System.err.println("无效的星期: " + dayOfWeek);
            return null;
        }

        // 解析节数范围
        String[] periodParts = periodRange.split("-");
        if (periodParts.length != 2) {
            System.err.println("节数格式错误: " + periodRange);
            return null;
        }

        try {
            int startPeriod = Integer.parseInt(periodParts[0].trim());
            int endPeriod = Integer.parseInt(periodParts[1].trim());

            // 验证节数范围
            if (startPeriod < 1 || endPeriod > 13 || startPeriod > endPeriod) {
                System.err.println("无效的节数范围: " + periodRange);
                return null;
            }

            return new TimeSlot(
                    dayOfWeek,
                    startPeriod,
                    endPeriod,
                    course.getCourseId(),
                    course.getCourseName(),
                    course.getClassroom(),
                    course.getTeacherName(),
                    course.getCredits()
            );

        } catch (NumberFormatException e) {
            System.err.println("节数解析失败: " + periodRange);
            return null;
        }
    }

    /**
     * 验证星期是否有效
     */
    private static boolean isValidDay(String day) {
        String[] validDays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        for (String validDay : validDays) {
            if (validDay.equals(day)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取节数对应的时间描述
     */
    public static String getPeriodTimeDescription(int period) {
        String[] timeDescriptions = {
                "", // 占位符，节数从1开始
                "8:00-8:45",   // 第1节
                "8:55-9:40",   // 第2节
                "10:00-10:45", // 第3节
                "10:55-11:40", // 第4节
                "11:50-12:35", // 第5节
                "14:00-14:45", // 第6节
                "14:55-15:40", // 第7节
                "16:00-16:45", // 第8节
                "16:55-17:40", // 第9节
                "18:30-19:15", // 第10节
                "19:25-20:10", // 第11节
                "20:20-21:05", // 第12节
                "21:15-22:00"  // 第13节
        };

        if (period >= 1 && period <= 13) {
            return timeDescriptions[period];
        }
        return "未知时间";
    }

    /**
     * 获取时间段的完整描述
     */
    public static String getTimeSlotDescription(TimeSlot timeSlot) {
        if (timeSlot.getStartPeriod() == timeSlot.getEndPeriod()) {
            return "第" + timeSlot.getStartPeriod() + "节 " +
                    getPeriodTimeDescription(timeSlot.getStartPeriod());
        } else {
            return "第" + timeSlot.getStartPeriod() + "-" + timeSlot.getEndPeriod() + "节 " +
                    getPeriodTimeDescription(timeSlot.getStartPeriod()) + "-" +
                    getPeriodTimeDescription(timeSlot.getEndPeriod()).split("-")[1];
        }
    }
}