package model;

import java.time.LocalDateTime;

public class Course {
    private String courseId;
    private String courseName;
    private String teacherId;
    private String teacherName;
    private int credits;
    private String semester;
    private Integer maxStudents;
    private String classTime;
    private String classroom;
    private int currentStudents;  // 当前选课学生数 (通过查询获得，不在数据库中存储)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public Course() {}

    public Course(String courseId, String courseName, String teacherId,
                  String teacherName, int credits, String semester,
                  String classTime, String classroom) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.credits = credits;
        this.semester = semester;
        this.classTime = classTime;
        this.classroom = classroom;
        this.currentStudents = 0;
    }
//    public Course(String courseId, String courseName, String teacherId, String teacherName) {
//        this.courseId = courseId;
//        this.courseName = courseName;
//        this.teacherId = teacherId;
//        this.teacherName = teacherName;
//        this.credits = 3;
//    }


    // Getter和Setter方法
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getClassTime() { return classTime; }
    public void setClassTime(String classTime) { this.classTime = classTime; }

    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }

    public int getCurrentStudents() { return currentStudents; }
    public void setCurrentStudents(int currentStudents) { this.currentStudents = currentStudents; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Integer getMaxStudents() {
        return maxStudents;
    }
    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", teacherId='" + teacherId + '\'' +
                ", teacherName='" + teacherName + '\'' +
                ", credits=" + credits +
                ", semester='" + semester + '\'' +
                ", classTime='" + classTime + '\'' +
                ", classroom='" + classroom + '\'' +
                ", currentStudents=" + currentStudents +
                '}';
    }
}