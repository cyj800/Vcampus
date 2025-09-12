package model;

import java.time.LocalDateTime;

public class CourseEnrollment {
    private int enrollmentId;     // 选课记录ID (对应数据库的enrollment_id)
    private String studentId;     // 学生用户名 (对应数据库的student_id)
    private String courseId;      // 课程编号 (对应数据库的course_id)
    private LocalDateTime enrollmentDate; // 选课时间 (对应数据库的enrollment_date)
    private String status;
    private String StudentName;// 选课状态：active(激活)/dropped(已退课)

    // 用于显示的附加字段，不在数据库中存储
    private String courseName;    // 课程名称
    private String teacherName;   // 教师姓名
    private String classroom;     // 教室
    private String classTime;     // 上课时间
    private int credits;          // 学分

    // 构造函数
    public CourseEnrollment() {}

    public CourseEnrollment(String studentId, String courseId) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.status = "active";
    }

    public CourseEnrollment(String studentId, String courseId, String status) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.status = status;
    }

    // Getter和Setter方法
    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return StudentName; }
    public void setStudentName(String studentName) { this.StudentName = studentName; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public LocalDateTime getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDateTime enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // 附加字段的getter和setter
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getClassroom() { return classroom; }
    public void setClassroom(String classroom) { this.classroom = classroom; }

    public String getClassTime() { return classTime; }
    public void setClassTime(String classTime) { this.classTime = classTime; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    // 便捷方法
    public boolean isActive() { return "active".equals(status); }
    public boolean isDropped() { return "dropped".equals(status); }

    @Override
    public String toString() {
        return "CourseEnrollment{" +
                "enrollmentId=" + enrollmentId +
                ", studentId='" + studentId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", status='" + status + '\'' +
                ", enrollmentDate=" + enrollmentDate +
                '}';
    }
}