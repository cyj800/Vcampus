package model;

import java.time.LocalDateTime;

public class Appeal {
    private int appealId;          // 申诉ID
    private int submissionId;      // 提交ID
    private String studentId;      // 学生ID
    private String reason;         // 申诉理由
    private String status;         // 状态：pending, approved, rejected, resolved，即待定，批准，拒绝，已解决
    private String response;       // 处理回复
    private String handlerId;      // 处理人ID
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime handledAt; // 处理时间

    // 用于显示的附加字段（不在数据库中存储）
    private String assignmentTitle; // 作业标题
    private String courseName;      // 课程名称
    private String studentNickname; // 学生昵称（删除了studentName）

    // 构造函数
    public Appeal() {}

    public Appeal(int submissionId, String studentId, String reason) {
        this.submissionId = submissionId;
        this.studentId = studentId;
        this.reason = reason;
        this.status = "pending";
    }

    // Getter和Setter方法（只保留studentNickname相关方法）
    public int getAppealId() { return appealId; }
    public void setAppealId(int appealId) { this.appealId = appealId; }

    public int getSubmissionId() { return submissionId; }
    public void setSubmissionId(int submissionId) { this.submissionId = submissionId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getHandlerId() { return handlerId; }
    public void setHandlerId(String handlerId) { this.handlerId = handlerId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }

    // 附加字段的Getter和Setter（只保留studentNickname）
    public String getAssignmentTitle() { return assignmentTitle; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getStudentNickname() { return studentNickname; }
    public void setStudentNickname(String studentNickname) { this.studentNickname = studentNickname; }

    // 便捷方法
    public boolean isPending() { return "pending".equals(status); }
    public boolean isApproved() { return "approved".equals(status); }
    public boolean isRejected() { return "rejected".equals(status); }
    public boolean isResolved() { return "resolved".equals(status); }

    @Override
    public String toString() {
        return "Appeal{" +
                "appealId=" + appealId +
                ", submissionId=" + submissionId +
                ", studentId='" + studentId + '\'' +
                ", studentNickname='" + studentNickname + '\'' +  // 修改为studentNickname
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}