package model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Grade {
    private int gradeId;
    private String studentId;       // 学生用户名
    private String courseId;
    private String courseName;      // 额外字段，用于显示
    private String studentName;     // 额外字段，用于显示
    private String teacherName;     // 额外字段，用于显示
    private Double score;
    private String gradeLetter;
    private Double classAverage;
    private Integer classRank;
    private LocalDate examDate;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public Grade() {}

    // Getter和Setter方法
    public int getGradeId() { return gradeId; }
    public void setGradeId(int gradeId) { this.gradeId = gradeId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public String getGradeLetter() { return gradeLetter; }
    public void setGradeLetter(String gradeLetter) { this.gradeLetter = gradeLetter; }

    public Double getClassAverage() { return classAverage; }
    public void setClassAverage(Double classAverage) { this.classAverage = classAverage; }

    public Integer getClassRank() { return classRank; }
    public void setClassRank(Integer classRank) { this.classRank = classRank; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Grade{" +
                "gradeId=" + gradeId +
                ", studentId='" + studentId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", studentName='" + studentName + '\'' +
                ", teacherName='" + teacherName + '\'' +
                ", score=" + score +
                ", gradeLetter='" + gradeLetter + '\'' +
                ", classAverage=" + classAverage +
                ", classRank=" + classRank +
                ", examDate=" + examDate +
                ", remark='" + remark + '\'' +
                '}';
    }
}