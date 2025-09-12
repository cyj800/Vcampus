package service;

import database.AppealDAO;
import database.SubmissionDAO;
import model.Appeal;
import model.Submission;
import java.time.LocalDateTime;
import java.util.List;

public class AppealService {
    private AppealDAO appealDAO;
    private SubmissionDAO submissionDAO;

    public AppealService() {
        this.appealDAO = new AppealDAO();
        this.submissionDAO = new SubmissionDAO();
    }

    /**
     * 学生创建申诉
     */
    public boolean createAppeal(int submissionId, String studentId, String reason)
            throws ServiceException {
        // 验证提交记录是否存在
        Submission submission = submissionDAO.getSubmissionById(submissionId);
        if (submission == null) {
            throw new ServiceException("提交记录不存在");
        }

        // 验证是否是该学生的提交
        if (!submission.getStudentId().equals(studentId)) {
            throw new ServiceException("无权对此提交创建申诉");
        }

        // 检查是否已经有申诉
        List<Appeal> existingAppeals = appealDAO.getAppealsByStudentId(studentId);
        for (Appeal appeal : existingAppeals) {
            if (appeal.getSubmissionId() == submissionId &&
                    (appeal.isPending() || appeal.isApproved())) {
                throw new ServiceException("该提交已有未处理的申诉");
            }
        }

        // 验证申诉理由
        if (reason == null || reason.trim().isEmpty()) {
            throw new ServiceException("申诉理由不能为空");
        }

        Appeal appeal = new Appeal(submissionId, studentId, reason);
        return appealDAO.createAppeal(appeal);
    }

    /**
     * 获取学生的所有申诉记录
     */
    public List<Appeal> getStudentAppeals(String studentId) {
        return appealDAO.getAppealsByStudentId(studentId);
    }

    /**
     * 教师处理申诉
     */
    public boolean handleAppeal(int appealId, String response, String status, String handlerId)
            throws ServiceException {
        Appeal appeal = appealDAO.getAppealById(appealId);
        if (appeal == null) {
            throw new ServiceException("申诉记录不存在");
        }

        if (!appeal.isPending()) {
            throw new ServiceException("该申诉已被处理");
        }

        // 验证状态
        if (!"approved".equals(status) && !"rejected".equals(status) &&
                !"resolved".equals(status)) {
            throw new ServiceException("无效的处理状态");
        }

        appeal.setResponse(response);
        appeal.setStatus(status);
        appeal.setHandlerId(handlerId);
        appeal.setHandledAt(LocalDateTime.now());

        return appealDAO.updateAppeal(appeal);
    }

    /**
     * 管理员获取待处理申诉
     */
    public List<Appeal> getPendingAppeals() {
        return appealDAO.getPendingAppeals();
    }

    /**
     * 获取所有申诉记录
     */
    public List<Appeal> getAllAppeals() {
        // 可以根据需要添加分页或其他筛选条件
        return appealDAO.getAppealsByStatus("pending"); // 或者获取所有状态的申诉
    }

    /**
     * 根据状态获取申诉记录
     */
    public List<Appeal> getAppealsByStatus(String status) {
        return appealDAO.getAppealsByStatus(status);
    }

    /**
     * 根据ID获取申诉记录
     */
    public Appeal getAppealById(int appealId) {
        return appealDAO.getAppealById(appealId);
    }

    /**
     * 获取申诉统计信息
     */
    public int getAppealCountByStatus(String status) {
        return appealDAO.getAppealCountByStatus(status);
    }
}