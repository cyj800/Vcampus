package model;
import java.util.*;
public class Schedule {
    private Map<String, List<TimeSlot>> weeklySchedule; // 按星期组织的课表
    private List<TimeSlot> allTimeSlots; // 所有时间段

    public static final String[] DAYS_OF_WEEK = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
    public static final int MAX_PERIODS = 13; // 每天最多13节课

    public Schedule() {
        this.weeklySchedule = new HashMap<>();
        this.allTimeSlots = new ArrayList<>();

        // 初始化每天的时间段列表
        for (String day : DAYS_OF_WEEK) {
            weeklySchedule.put(day, new ArrayList<>());
        }
    }

    public void addTimeSlot(TimeSlot timeSlot) {
        allTimeSlots.add(timeSlot);
        String day = timeSlot.getDayOfWeek();
        if (weeklySchedule.containsKey(day)) {
            weeklySchedule.get(day).add(timeSlot);
            // 按开始时间排序
            weeklySchedule.get(day).sort(Comparator.comparingInt(TimeSlot::getStartPeriod));
        }
    }

    public List<TimeSlot> getTimeSlotsForDay(String day) {
        return weeklySchedule.getOrDefault(day, new ArrayList<>());
    }

    public List<TimeSlot> getAllTimeSlots() {
        return new ArrayList<>(allTimeSlots);
    }

    public Map<String, List<TimeSlot>> getWeeklySchedule() {
        return weeklySchedule;
    }

    public boolean hasConflict(TimeSlot newSlot) {
        List<TimeSlot> daySlots = getTimeSlotsForDay(newSlot.getDayOfWeek());

        for (TimeSlot existingSlot : daySlots) {
            if (isTimeOverlap(newSlot, existingSlot)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTimeOverlap(TimeSlot slot1, TimeSlot slot2) {
        return !(slot1.getEndPeriod() < slot2.getStartPeriod() ||
                slot2.getEndPeriod() < slot1.getStartPeriod());
    }

    public int getTotalCourses() {
        return allTimeSlots.size();
    }

    public int getTotalCredits() {
        return allTimeSlots.stream().mapToInt(TimeSlot::getCredits).sum();
    }
}


