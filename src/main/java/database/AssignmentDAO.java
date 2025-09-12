package database;

import model.Assignment;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AssignmentDAO {
    // 创建作业
    public boolean createAssignment(Assignment assignment) {
        String sql = "INSERT INTO assignments (course_id, teacher_id, title, description, " +
                "deadline, max_score, submit_type, allowed_file_types, max_file_size, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, assignment.getCourseId());
            stmt.setString(2, assignment.getTeacherId());
            stmt.setString(3, assignment.getTitle());
            stmt.setString(4, assignment.getDescription());
            stmt.setTimestamp(5, Timestamp.valueOf(assignment.getDeadline()));
            stmt.setInt(6, assignment.getMaxScore());
            stmt.setString(7, assignment.getSubmitType());
            stmt.setString(8, assignment.getAllowedFileTypes());
            if (assignment.getMaxFileSize() != null) {
                stmt.setInt(9, assignment.getMaxFileSize());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }
            stmt.setString(10, assignment.getStatus());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // 获取生成的主键
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    assignment.setAssignmentId(generatedKeys.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 根据ID获取作业
    public Assignment getAssignmentById(int assignmentId) {
        String sql = "SELECT * FROM assignments WHERE assignment_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, assignmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAssignment(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获取某课程的所有作业
    public List<Assignment> getAssignmentsByCourseId(String courseId) {
        String sql = "SELECT * FROM assignments WHERE course_id = ? ORDER BY deadline ASC";
        List<Assignment> assignments = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                assignments.add(mapResultSetToAssignment(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assignments;
    }

    // 获取某教师的所有作业
    public List<Assignment> getAssignmentsByTeacherId(String teacherId) {
        String sql = "SELECT * FROM assignments WHERE teacher_id = ? ORDER BY deadline ASC";
        List<Assignment> assignments = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, teacherId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                assignments.add(mapResultSetToAssignment(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assignments;
    }

    // 更新作业
    public boolean updateAssignment(Assignment assignment) {
        String sql = "UPDATE assignments SET course_id = ?, teacher_id = ?, title = ?, " +
                "description = ?, deadline = ?, max_score = ?, submit_type = ?, " +
                "allowed_file_types = ?, max_file_size = ?, status = ?, updated_at = ? " +
                "WHERE assignment_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, assignment.getCourseId());
            stmt.setString(2, assignment.getTeacherId());
            stmt.setString(3, assignment.getTitle());
            stmt.setString(4, assignment.getDescription());
            stmt.setTimestamp(5, Timestamp.valueOf(assignment.getDeadline()));
            stmt.setInt(6, assignment.getMaxScore());
            stmt.setString(7, assignment.getSubmitType());
            stmt.setString(8, assignment.getAllowedFileTypes());
            if (assignment.getMaxFileSize() != null) {
                stmt.setInt(9, assignment.getMaxFileSize());
            } else {
                stmt.setNull(9, Types.INTEGER);
            }
            stmt.setString(10, assignment.getStatus());
            stmt.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(12, assignment.getAssignmentId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 删除作业（逻辑删除）
    public boolean deleteAssignment(int assignmentId) {
        String sql = "UPDATE assignments SET status = 'deleted' WHERE assignment_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, assignmentId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 获取活跃作业（未过期且未关闭的作业）
    public List<Assignment> getActiveAssignments() {
        String sql = "SELECT * FROM assignments WHERE status = 'active' AND deadline > ? " +
                "ORDER BY deadline ASC";
        List<Assignment> assignments = new ArrayList<>();

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                assignments.add(mapResultSetToAssignment(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assignments;
    }

    // 将ResultSet映射到Assignment对象
    private Assignment mapResultSetToAssignment(ResultSet rs) throws SQLException {
        Assignment assignment = new Assignment();
        assignment.setAssignmentId(rs.getInt("assignment_id"));
        assignment.setCourseId(rs.getString("course_id"));
        assignment.setTeacherId(rs.getString("teacher_id"));
        assignment.setTitle(rs.getString("title"));
        assignment.setDescription(rs.getString("description"));
        assignment.setDeadline(rs.getTimestamp("deadline").toLocalDateTime());
        assignment.setMaxScore(rs.getInt("max_score"));
        assignment.setSubmitType(rs.getString("submit_type"));
        assignment.setAllowedFileTypes(rs.getString("allowed_file_types"));

        int fileSize = rs.getInt("max_file_size");
        if (!rs.wasNull()) {
            assignment.setMaxFileSize(fileSize);
        }

        assignment.setStatus(rs.getString("status"));
        assignment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        assignment.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return assignment;
    }
}