package database;

import model.Submission;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubmissionDAO {

    // 创建作业提交记录
    public boolean createSubmission(Submission submission) {
        String sql = "INSERT INTO submissions (assignment_id, student_id, content, file_path, " +
                "file_name, file_size, submit_time, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, submission.getAssignmentId());
            stmt.setString(2, submission.getStudentId());
            stmt.setString(3, submission.getContent());
            stmt.setString(4, submission.getFilePath());
            stmt.setString(5, submission.getFileName());

            if (submission.getFileSize() != null) {
                stmt.setInt(6, submission.getFileSize());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setTimestamp(7, Timestamp.valueOf(submission.getSubmitTime() != null ?
                    submission.getSubmitTime() : LocalDateTime.now()));
            stmt.setString(8, submission.getStatus());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // 获取生成的主键
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    submission.setSubmissionId(generatedKeys.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 根据ID获取提交记录
    public Submission getSubmissionById(int submissionId) {
        String sql = "SELECT s.*, a.title as assignment_title, c.course_name, u.nickname as student_name " +
                "FROM submissions s " +
                "LEFT JOIN assignments a ON s.assignment_id = a.assignment_id " +
                "LEFT JOIN courses c ON a.course_id = c.course_id " +
                "LEFT JOIN users u ON s.student_id = u.username " +  // 确保JOIN users表
                "WHERE s.submission_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, submissionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToSubmission(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获取某作业的所有提交记录
    public List<Submission> getSubmissionsByAssignmentId(int assignmentId) {
        String sql = "SELECT s.*, a.title as assignment_title, c.course_name, u.nickname as student_name " +
                "FROM submissions s " +
                "LEFT JOIN assignments a ON s.assignment_id = a.assignment_id " +
                "LEFT JOIN courses c ON a.course_id = c.course_id " +
                "LEFT JOIN users u ON s.student_id = u.username " +
                "WHERE s.assignment_id = ? " +
                "ORDER BY s.submit_time ASC";
        List<Submission> submissions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, assignmentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                submissions.add(mapResultSetToSubmission(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return submissions;
    }

    // 获取某学生的所有提交记录
    public List<Submission> getSubmissionsByStudentId(String studentId) {
        String sql = "SELECT s.*, a.title as assignment_title, c.course_name, u.nickname as student_name " +
                "FROM submissions s " +
                "LEFT JOIN assignments a ON s.assignment_id = a.assignment_id " +
                "LEFT JOIN courses c ON a.course_id = c.course_id " +
                "LEFT JOIN users u ON s.student_id = u.username " +  // 确保JOIN users表
                "WHERE s.student_id = ? " +
                "ORDER BY s.submit_time DESC";
        List<Submission> submissions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                submissions.add(mapResultSetToSubmission(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return submissions;
    }

    // 获取某学生在某课程中的所有提交记录
    public List<Submission> getSubmissionsByStudentAndCourse(String studentId, String courseId) {
        String sql = "SELECT s.*, a.title as assignment_title, c.course_name, u.nickname as student_name " +
                "FROM submissions s " +
                "LEFT JOIN assignments a ON s.assignment_id = a.assignment_id " +
                "LEFT JOIN courses c ON a.course_id = c.course_id " +
                "LEFT JOIN users u ON s.student_id = u.username " +  // 确保JOIN users表
                "WHERE s.student_id = ? AND a.course_id = ? " +
                "ORDER BY s.submit_time DESC";
        List<Submission> submissions = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setString(2, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                submissions.add(mapResultSetToSubmission(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return submissions;
    }

    // 更新提交记录（如录入成绩）
    public boolean updateSubmission(Submission submission) {
        String sql = "UPDATE submissions SET content = ?, file_path = ?, file_name = ?, " +
                "file_size = ?, score = ?, feedback = ?, status = ?, " +
                "graded_time = ?, grader_id = ? WHERE submission_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, submission.getContent());
            stmt.setString(2, submission.getFilePath());
            stmt.setString(3, submission.getFileName());

            if (submission.getFileSize() != null) {
                stmt.setInt(4, submission.getFileSize());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            if (submission.getScore() != null) {
                stmt.setInt(5, submission.getScore());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setString(6, submission.getFeedback());
            stmt.setString(7, submission.getStatus());

            if (submission.getGradedTime() != null) {
                stmt.setTimestamp(8, Timestamp.valueOf(submission.getGradedTime()));
            } else {
                stmt.setNull(8, Types.TIMESTAMP);
            }

            stmt.setString(9, submission.getGraderId());
            stmt.setInt(10, submission.getSubmissionId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 检查学生是否已提交某作业
    public boolean isAssignmentSubmitted(int assignmentId, String studentId) {
        String sql = "SELECT COUNT(*) FROM submissions WHERE assignment_id = ? AND student_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, assignmentId);
            stmt.setString(2, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 获取某作业的提交统计信息
    public int getSubmissionCount(int assignmentId) {
        String sql = "SELECT COUNT(*) FROM submissions WHERE assignment_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, assignmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ==================== 安全的字段获取方法 ====================

    /**
     * 安全获取字符串字段
     */
    private String safeGetString(ResultSet rs, String columnName) {
        try {
            return rs.getString(columnName);
        } catch (SQLException e) {
            System.err.println("警告: 无法获取字符串字段 '" + columnName + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * 安全获取整数字段
     */
    private int safeGetInt(ResultSet rs, String columnName) {
        try {
            int value = rs.getInt(columnName);
            return rs.wasNull() ? 0 : value;
        } catch (SQLException e) {
            System.err.println("警告: 无法获取整数字段 '" + columnName + "': " + e.getMessage());
            return 0;
        }
    }

    /**
     * 安全获取长整数字段
     */
    private long safeGetLong(ResultSet rs, String columnName) {
        try {
            long value = rs.getLong(columnName);
            return rs.wasNull() ? 0L : value;
        } catch (SQLException e) {
            System.err.println("警告: 无法获取长整数字段 '" + columnName + "': " + e.getMessage());
            return 0L;
        }
    }

    /**
     * 安全获取时间戳字段
     */
    private Timestamp safeGetTimestamp(ResultSet rs, String columnName) {
        try {
            return rs.getTimestamp(columnName);
        } catch (SQLException e) {
            System.err.println("警告: 无法获取时间戳字段 '" + columnName + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * 检查字段是否为NULL
     */
    private boolean safeWasNull(ResultSet rs) {
        try {
            return rs.wasNull();
        } catch (SQLException e) {
            return true;
        }
    }

    /**
     * 安全获取LocalDateTime字段
     */
    private LocalDateTime safeGetLocalDateTime(ResultSet rs, String columnName) {
        Timestamp timestamp = safeGetTimestamp(rs, columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    // ==================== 字段映射方法 ====================

    // 将ResultSet映射到Submission对象
    private Submission mapResultSetToSubmission(ResultSet rs) throws SQLException {
        Submission submission = new Submission();

        // 基本字段映射 - 使用安全方法
        submission.setSubmissionId(safeGetInt(rs, "submission_id"));
        submission.setAssignmentId(safeGetInt(rs, "assignment_id"));
        submission.setStudentId(safeGetString(rs, "student_id"));
        submission.setContent(safeGetString(rs, "content"));
        submission.setFilePath(safeGetString(rs, "file_path"));
        submission.setFileName(safeGetString(rs, "file_name"));

        // 安全处理可能为null的字段
        int fileSize = safeGetInt(rs, "file_size");
        if (fileSize > 0) {
            submission.setFileSize(fileSize);
        } else {
            submission.setFileSize(null); // 确保为null而不是0
        }

        // 时间字段 - 使用安全方法
        submission.setSubmitTime(safeGetLocalDateTime(rs, "submit_time"));
        submission.setGradedTime(safeGetLocalDateTime(rs, "graded_time"));

        // 整数字段
        int score = safeGetInt(rs, "score");
        if (score > 0) {
            submission.setScore(score);
        }

        // 字符串字段
        submission.setFeedback(safeGetString(rs, "feedback"));
        submission.setStatus(safeGetString(rs, "status"));
        submission.setGraderId(safeGetString(rs, "grader_id"));

        // 附加字段
        submission.setAssignmentTitle(safeGetString(rs, "assignment_title"));
        submission.setCourseName(safeGetString(rs, "course_name"));
        submission.setStudentName(safeGetString(rs, "student_name"));

        return submission;
    }
}