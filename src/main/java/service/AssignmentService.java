package service;

import database.AssignmentDAO;
import database.SubmissionDAO;
import model.Assignment;
import model.Submission;
import java.time.LocalDateTime;
import java.util.List;

public class AssignmentService {
    private AssignmentDAO assignmentDAO;
    private SubmissionDAO submissionDAO;

    public AssignmentService() {
        this.assignmentDAO = new AssignmentDAO();
        this.submissionDAO = new SubmissionDAO();
    }

    // ==================== 教师功能 ====================

    /**
     * 创建作业
     */
    public boolean createAssignment(Assignment assignment) throws ServiceException {
        // 验证必填字段
        if (assignment.getCourseId() == null || assignment.getCourseId().trim().isEmpty()) {
            throw new ServiceException("课程ID不能为空");
        }
        if (assignment.getTeacherId() == null || assignment.getTeacherId().trim().isEmpty()) {
            throw new ServiceException("教师ID不能为空");
        }
        if (assignment.getTitle() == null || assignment.getTitle().trim().isEmpty()) {
            throw new ServiceException("作业标题不能为空");
        }
        if (assignment.getDeadline() == null) {
            throw new ServiceException("截止时间不能为空");
        }

        // 验证截止时间
        if (assignment.getDeadline().isBefore(LocalDateTime.now())) {
            throw new ServiceException("截止时间不能早于当前时间");
        }

        return assignmentDAO.createAssignment(assignment);
    }

    /**
     * 更新作业
     */
    public boolean updateAssignment(Assignment assignment) throws ServiceException {
        if (assignment.getAssignmentId() <= 0) {
            throw new ServiceException("作业ID无效");
        }

        // 验证截止时间
        if (assignment.getDeadline() != null &&
                assignment.getDeadline().isBefore(LocalDateTime.now())) {
            throw new ServiceException("截止时间不能早于当前时间");
        }

        return assignmentDAO.updateAssignment(assignment);
    }

    /**
     * 删除作业（逻辑删除）
     */
    public boolean deleteAssignment(int assignmentId) throws ServiceException {
        if (assignmentId <= 0) {
            throw new ServiceException("作业ID无效");
        }
        return assignmentDAO.deleteAssignment(assignmentId);
    }

    /**
     * 获取教师的所有作业
     */
    public List<Assignment> getTeacherAssignments(String teacherId) {
        return assignmentDAO.getAssignmentsByTeacherId(teacherId);
    }

    // ==================== 学生功能 ====================

    /**
     * 获取学生可见的作业
     */
    public List<Assignment> getStudentAssignments(String studentId, String courseId) {
        // 这里可以添加更复杂的逻辑，比如班级限制等
        return assignmentDAO.getAssignmentsByCourseId(courseId);
    }

    /**
     * 检查学生是否已提交作业
     */
    public boolean isAssignmentSubmitted(int assignmentId, String studentId) {
        return submissionDAO.isAssignmentSubmitted(assignmentId, studentId);
    }

    // ==================== 管理员功能 ====================

    /**
     * 获取所有活跃作业
     */
    public List<Assignment> getAllActiveAssignments() {
        return assignmentDAO.getActiveAssignments();
    }

    /**
     * 根据ID获取作业详情
     */
    public Assignment getAssignmentById(int assignmentId) {
        return assignmentDAO.getAssignmentById(assignmentId);
    }

    /**
     * 获取某课程的所有作业
     */
    public List<Assignment> getAssignmentsByCourseId(String courseId) {
        return assignmentDAO.getAssignmentsByCourseId(courseId);
    }

    // ==================== 统计功能 ====================

    /**
     * 获取作业提交统计
     */
    public int getSubmissionCount(int assignmentId) {
        return submissionDAO.getSubmissionCount(assignmentId);
    }

    /**
     * 计算作业平均分
     */
    public double getAverageScore(int assignmentId) {
        List<Submission> submissions = submissionDAO.getSubmissionsByAssignmentId(assignmentId);
        if (submissions.isEmpty()) {
            return 0.0;
        }

        int totalScore = 0;
        int gradedCount = 0;

        for (Submission submission : submissions) {
            if (submission.getScore() != null) {
                totalScore += submission.getScore();
                gradedCount++;
            }
        }

        return gradedCount > 0 ? (double) totalScore / gradedCount : 0.0;
    }
}