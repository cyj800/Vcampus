package database;

import model.Appeal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppealDAO {

    // 创建申诉记录
    public boolean createAppeal(Appeal appeal) {
        String sql = "INSERT INTO assignment_appeals (submission_id, student_id, reason, status) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, appeal.getSubmissionId());
            stmt.setString(2, appeal.getStudentId());
            stmt.setString(3, appeal.getReason());
            stmt.setString(4, appeal.getStatus());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // 获取生成的主键
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    appeal.setAppealId(generatedKeys.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 根据ID获取申诉记录
    public Appeal getAppealById(int appealId) {
        String sql = "SELECT ap.*, s.assignment_id, a.title as assignment_title, " +
                "c.course_name, u.nickname as student_nickname " +
                "FROM assignment_appeals ap " +
                "LEFT JOIN submissions s ON ap.submission_id = s.submission_id " +
                "LEFT JOIN assignments a ON s.assignment_id = a.assignment_id " +
                "LEFT JOIN courses c ON a.course_id = c.course_id " +
                "LEFT JOIN users u ON ap.student_id = u.username " +
                "WHERE ap.appeal_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, appealId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAppeal(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获取某学生的所有申诉记录
    public List<Appeal> getAppealsByStudentId(String studentId) {
        String sql = "SELECT ap.*, s.assignment_id, a.title as assignment_title, " +
                "c.course_name, u.nickname as student_nickname " +
                "FROM assignment_appeals ap " +
                "LEFT JOIN submissions s ON ap.submission_id = s.submission_id " +
                "LEFT JOIN assignments a ON s.assignment_id = a.assignment_id " +
                "LEFT JOIN courses c ON a.course_id = c.course_id " +
                "LEFT JOIN users u ON ap.student_id = u.username " +
                "WHERE ap.student_id = ? " +
                "ORDER BY ap.created_at DESC";
        List<Appeal> appeals = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                appeals.add(mapResultSetToAppeal(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appeals;
    }

    // 获取待处理的申诉记录（管理员使用）
    public List<Appeal> getPendingAppeals() {
        String sql = "SELECT ap.*, s.assignment_id, a.title as assignment_title, " +
                "c.course_name, u.nickname as student_nickname " +  // 只获取一次nickname
                "FROM assignment_appeals ap " +
                "LEFT JOIN submissions s ON ap.submission_id = s.submission_id " +
                "LEFT JOIN assignments a ON s.assignment_id = a.assignment_id " +
                "LEFT JOIN courses c ON a.course_id = c.course_id " +
                "LEFT JOIN users u ON ap.student_id = u.username " +  // 只JOIN一次
                "WHERE ap.status = 'pending' " +
                "ORDER BY ap.created_at ASC";
        List<Appeal> appeals = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                appeals.add(mapResultSetToAppeal(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appeals;
    }


    // 获取某状态的所有申诉记录
    public List<Appeal> getAppealsByStatus(String status) {
        String sql = "SELECT ap.*, s.assignment_id, a.title as assignment_title, " +
                "c.course_name, u.nickname as student_nickname " +
                "FROM assignment_appeals ap " +
                "LEFT JOIN submissions s ON ap.submission_id = s.submission_id " +
                "LEFT JOIN assignments a ON s.assignment_id = a.assignment_id " +
                "LEFT JOIN courses c ON a.course_id = c.course_id " +
                "LEFT JOIN users u ON ap.student_id = u.username " +
                "WHERE ap.status = ? " +
                "ORDER BY ap.created_at DESC";
        List<Appeal> appeals = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                appeals.add(mapResultSetToAppeal(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appeals;
    }

    // 更新申诉记录（处理申诉）
    public boolean updateAppeal(Appeal appeal) {
        String sql = "UPDATE assignment_appeals SET reason = ?, status = ?, response = ?, " +
                "handler_id = ?, handled_at = ? WHERE appeal_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, appeal.getReason());
            stmt.setString(2, appeal.getStatus());
            stmt.setString(3, appeal.getResponse());
            stmt.setString(4, appeal.getHandlerId());

            if (appeal.getHandledAt() != null) {
                stmt.setTimestamp(5, Timestamp.valueOf(appeal.getHandledAt()));
            } else {
                stmt.setNull(5, Types.TIMESTAMP);
            }

            stmt.setInt(6, appeal.getAppealId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 获取申诉统计信息
    public int getAppealCountByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM assignment_appeals WHERE status = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // 将ResultSet映射到Appeal对象
    private Appeal mapResultSetToAppeal(ResultSet rs) throws SQLException {
        Appeal appeal = new Appeal();
        appeal.setAppealId(rs.getInt("appeal_id"));
        appeal.setSubmissionId(rs.getInt("submission_id"));
        appeal.setStudentId(rs.getString("student_id"));
        appeal.setReason(rs.getString("reason"));
        appeal.setStatus(rs.getString("status"));
        appeal.setResponse(rs.getString("response"));
        appeal.setHandlerId(rs.getString("handler_id"));
        appeal.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        Timestamp handledAt = rs.getTimestamp("handled_at");
        if (handledAt != null) {
            appeal.setHandledAt(handledAt.toLocalDateTime());
        }

        // 附加字段（只从一个users表获取）
        appeal.setAssignmentTitle(rs.getString("assignment_title"));
        appeal.setCourseName(rs.getString("course_name"));
        appeal.setStudentNickname(rs.getString("student_nickname"));  // 只设置studentNickname

        return appeal;
    }
}