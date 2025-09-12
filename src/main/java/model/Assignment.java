package model;

import java.time.LocalDateTime;

public class Assignment {
    private int assignmentId;      // 作业ID
    private String courseId;       // 课程ID
    private String teacherId;      // 教师ID
    private String title;          // 作业标题
    private String description;    // 作业描述
    private LocalDateTime deadline; // 截止时间
    private int maxScore;          // 满分
    private String submitType;     // 提交类型：text_only, file_only, both
    private String allowedFileTypes; // 允许的文件类型
    private Integer maxFileSize;   // 最大文件大小
    private String status;         // 状态：active, closed, deleted
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间

    // 构造函数
    public Assignment() {}

    public Assignment(String courseId, String teacherId, String title,
                      String description, LocalDateTime deadline) {
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.maxScore = 100;
        this.submitType = "both";
        this.status = "active";
    }

    // Getter和Setter方法
    public int getAssignmentId() { return assignmentId; }
    public void setAssignmentId(int assignmentId) { this.assignmentId = assignmentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public int getMaxScore() { return maxScore; }
    public void setMaxScore(int maxScore) { this.maxScore = maxScore; }

    public String getSubmitType() { return submitType; }
    public void setSubmitType(String submitType) { this.submitType = submitType; }

    public String getAllowedFileTypes() { return allowedFileTypes; }
    public void setAllowedFileTypes(String allowedFileTypes) { this.allowedFileTypes = allowedFileTypes; }

    public Integer getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(Integer maxFileSize) { this.maxFileSize = maxFileSize; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 便捷方法
    public boolean isActive() { return "active".equals(status); }
    public boolean isClosed() { return "closed".equals(status); }
    public boolean isDeleted() { return "deleted".equals(status); }

    @Override
    public String toString() {
        return "Assignment{" +
                "assignmentId=" + assignmentId +
                ", courseId='" + courseId + '\'' +
                ", title='" + title + '\'' +
                ", deadline=" + deadline +
                ", status='" + status + '\'' +
                '}';
    }
}