package service;

import database.SubmissionDAO;
import database.AssignmentDAO;
import model.Submission;
import model.Assignment;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class SubmissionService {
    private SubmissionDAO submissionDAO;
    private AssignmentDAO assignmentDAO;
    private FileService fileService;

    public SubmissionService() {
        this.submissionDAO = new SubmissionDAO();
        this.assignmentDAO = new AssignmentDAO();
        this.fileService = new FileService();
    }

    /**
     * 学生提交作业（支持文字和文件）
     * @param assignmentId 作业ID
     * @param studentId 学生ID
     * @param content 文字内容（可选）
     * @param tempFile 上传的文件（可选）
     * @param originalFileName 原始文件名
     * @return 提交是否成功
     */
    public boolean submitAssignment(int assignmentId, String studentId,
                                    String content, File tempFile, String originalFileName)
            throws ServiceException {

        // 1. 检查作业是否存在且可提交
        Assignment assignment = assignmentDAO.getAssignmentById(assignmentId);
        if (assignment == null) {
            throw new ServiceException("作业不存在");
        }

        if (!assignment.isActive()) {
            throw new ServiceException("作业已关闭，无法提交");
        }

        if (assignment.getDeadline().isBefore(LocalDateTime.now())) {
            throw new ServiceException("作业已过截止时间");
        }

        // 2. 检查是否已经提交过
        if (submissionDAO.isAssignmentSubmitted(assignmentId, studentId)) {
            throw new ServiceException("您已经提交过这个作业了");
        }

        // 3. 处理文件上传
        String filePath = null;
        Integer fileSize = null;

        if (tempFile != null && tempFile.exists()) {
            // 验证文件类型
            if (assignment.getAllowedFileTypes() != null &&
                    !fileService.validateFileType(originalFileName, assignment.getAllowedFileTypes())) {
                throw new ServiceException("不支持的文件类型");
            }

            // 验证文件大小
            if (assignment.getMaxFileSize() != null &&
                    !fileService.validateFileSize(tempFile.length(), assignment.getMaxFileSize())) {
                throw new ServiceException("文件大小超出限制");
            }

            // 上传文件
            try {
                filePath = fileService.uploadAssignmentFile(tempFile, originalFileName,
                        assignmentId, studentId);
                fileSize = (int) tempFile.length();
            } catch (ServiceException e) {
                throw new ServiceException("文件上传失败: " + e.getMessage());
            }
        }

        // 4. 创建提交记录
        Submission submission = new Submission(assignmentId, studentId);
        submission.setContent(content);
        submission.setFilePath(filePath);
        submission.setFileName(originalFileName);
        submission.setFileSize(fileSize);
        submission.setSubmitTime(LocalDateTime.now());
        submission.setStatus("submitted");

        // 5. 保存到数据库
        boolean success = submissionDAO.createSubmission(submission);

        if (!success) {
            // 如果数据库保存失败，删除已上传的文件
            if (filePath != null) {
                fileService.deleteFile(filePath);
            }
            throw new ServiceException("提交失败，请重试");
        }

        return true;
    }

    /**
     * 获取学生的所有提交记录
     */
    public List<Submission> getStudentSubmissions(String studentId) {
        return submissionDAO.getSubmissionsByStudentId(studentId);
    }

    /**
     * 获取某作业的所有提交记录（教师使用）
     */
    public List<Submission> getAssignmentSubmissions(int assignmentId) {
        return submissionDAO.getSubmissionsByAssignmentId(assignmentId);
    }

    /**
     * 教师批改作业
     */
    public boolean gradeSubmission(int submissionId, int score, String feedback, String graderId)
            throws ServiceException {
        Submission submission = submissionDAO.getSubmissionById(submissionId);
        if (submission == null) {
            throw new ServiceException("提交记录不存在");
        }

        // 验证分数范围
        Assignment assignment = assignmentDAO.getAssignmentById(submission.getAssignmentId());
        if (assignment != null && score > assignment.getMaxScore()) {
            throw new ServiceException("分数不能超过作业满分(" + assignment.getMaxScore() + "分)");
        }
        if (score < 0) {
            throw new ServiceException("分数不能为负数");
        }

        submission.setScore(score);
        submission.setFeedback(feedback);
        submission.setStatus("graded");
        submission.setGradedTime(LocalDateTime.now());
        submission.setGraderId(graderId);

        return submissionDAO.updateSubmission(submission);
    }

    /**
     * 学生下载自己的作业文件
     */
    public File downloadSubmissionFile(int submissionId, String studentId)
            throws ServiceException {
        Submission submission = submissionDAO.getSubmissionById(submissionId);

        // 验证权限
        if (submission == null || !submission.getStudentId().equals(studentId)) {
            throw new ServiceException("无权访问该文件");
        }

        if (submission.getFilePath() == null || submission.getFilePath().isEmpty()) {
            throw new ServiceException("该提交没有附件");
        }

        File file = new File(submission.getFilePath());
        if (!file.exists()) {
            throw new ServiceException("文件不存在");
        }

        return file;
    }

    /**
     * 根据ID获取提交记录
     */
    public Submission getSubmissionById(int submissionId) {
        return submissionDAO.getSubmissionById(submissionId);
    }

    /**
     * 获取学生在某课程中的提交记录
     */
    public List<Submission> getStudentSubmissionsByCourse(String studentId, String courseId) {
        return submissionDAO.getSubmissionsByStudentAndCourse(studentId, courseId);
    }

    /**
     * 获取提交统计信息
     */
    public int getSubmissionCount(int assignmentId) {
        return submissionDAO.getSubmissionCount(assignmentId);
    }

    /**
     * 检查作业是否已提交
     */
    public boolean isAssignmentSubmitted(int assignmentId, String studentId) {
        return submissionDAO.isAssignmentSubmitted(assignmentId, studentId);
    }

    /**
     * 获取文件路径信息（用于文件下载）
     */
    public String getSubmissionFilePath(int submissionId) throws ServiceException {
        Submission submission = submissionDAO.getSubmissionById(submissionId);
        if (submission == null) {
            throw new ServiceException("提交记录不存在");
        }

        if (submission.getFilePath() == null || submission.getFilePath().isEmpty()) {
            throw new ServiceException("该提交没有附件");
        }

        return submission.getFilePath();
    }

    /**
     * 获取文件名信息
     */
    public String getSubmissionFileName(int submissionId) throws ServiceException {
        Submission submission = submissionDAO.getSubmissionById(submissionId);
        if (submission == null) {
            throw new ServiceException("提交记录不存在");
        }

        return submission.getFileName();
    }

    /**
     * 获取文件大小信息
     */
    public Integer getSubmissionFileSize(int submissionId) throws ServiceException {
        Submission submission = submissionDAO.getSubmissionById(submissionId);
        if (submission == null) {
            throw new ServiceException("提交记录不存在");
        }

        return submission.getFileSize();
    }
}