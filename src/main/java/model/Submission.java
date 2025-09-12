package model;

import java.time.LocalDateTime;

public class Submission {
    private int submissionId;      // 提交ID
    private int assignmentId;      // 作业ID
    private String studentId;      // 学生ID
    private String content;        // 文字内容
    private String filePath;       // 文件路径
    private String fileName;       // 文件名
    private Integer fileSize;      // 文件大小
    private LocalDateTime submitTime; // 提交时间
    private Integer score;         // 得分
    private String feedback;       // 教师评语
    private String status;         // 状态：submitted, graded, revised
    private LocalDateTime gradedTime; // 批改时间
    private String graderId;       // 批改教师ID

    // 用于显示的附加字段（不在数据库中存储）
    private String assignmentTitle; // 作业标题
    private String courseName;      // 课程名称
    private String studentName;     // 学生姓名

    // 构造函数
    public Submission() {}

    public Submission(int assignmentId, String studentId) {
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.status = "submitted";
    }

    // Getter和Setter方法
    public int getSubmissionId() { return submissionId; }
    public void setSubmissionId(int submissionId) { this.submissionId = submissionId; }

    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Integer getFileSize() { return fileSize; }
    public void setFileSize(Integer fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getGradedTime() { return gradedTime; }
    public void setGradedTime(LocalDateTime gradedTime) { this.gradedTime = gradedTime; }

    public String getGraderId() { return graderId; }
    public void setGraderId(String graderId) { this.graderId = graderId; }

    // 附加字段的Getter和Setter
    public String getAssignmentTitle() { return assignmentTitle; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    // 便捷方法
    public boolean isSubmitted() { return "submitted".equals(status); }
    public boolean isGraded() { return "graded".equals(status); }
    public boolean isRevised() { return "revised".equals(status); }

    @Override
    public String toString() {
        return "Submission{" +
                "submissionId=" + submissionId +
                ", assignmentId=" + assignmentId +
                ", studentId='" + studentId + '\'' +
                ", status='" + status + '\'' +
                ", score=" + score +
                '}';
    }
}