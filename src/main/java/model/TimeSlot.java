package model;
public class TimeSlot {
    private String dayOfWeek;    // 星期几
    private int startPeriod;     // 开始节数
    private int endPeriod;       // 结束节数
    private String courseId;     // 课程ID
    private String courseName;   // 课程名称
    private String classroom;    // 教室
    private String teacherName;  // 教师姓名
    private int credits;         // 学分

    public TimeSlot() {}

    public TimeSlot(String dayOfWeek, int startPeriod, int endPeriod,
                    String courseId, String courseName, String classroom,
                    String teacherName, int credits) {
        this.dayOfWeek = dayOfWeek;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.courseId = courseId;
        this.courseName = courseName;
        this.classroom = classroom;
        this.teacherName = teacherName;
        this.credits = credits;
    }

    // Getters and Setters
    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public int getStartPeriod() { return startPeriod; }
    public void setStartPeriod(int startPeriod) { this.startPeriod = startPeriod; }

    public int getEndPeriod() { return endPeriod; }
    public void setEndPeriod(int endPeriod) { this.endPeriod = endPeriod; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    @Override
    public String toString() {
        return courseName + "\n"
                + classroom + "\n"
                + teacherName;
    }
}

