package database;

import model.Grade;
import database.UserDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradeDAO {
    private UserDAO userDAO;

    public GradeDAO() {
        this.userDAO = new UserDAO();
    }

    // 管理员：获取所有成绩
    public List<Grade> getAllGrades() {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.*, c.course_name, c.teacher_name, " +
                "(SELECT nickname FROM users WHERE username = g.student_id) as student_name " +
                "FROM grades g " +
                "JOIN courses c ON g.course_id = c.course_id " +
                "ORDER BY c.course_name, g.student_id";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return grades;
    }

    // 教师：获取自己教授课程的成绩
    public List<Grade> getGradesByTeacher(String teacherUsername) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.*, c.course_name, c.teacher_name, " +
                "(SELECT nickname FROM users WHERE username = g.student_id) as student_name " +
                "FROM grades g " +
                "JOIN courses c ON g.course_id = c.course_id " +
                "WHERE c.teacher_id = ? " +
                "ORDER BY c.course_name, g.student_id";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, teacherUsername);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return grades;
    }

    // 学生：获取自己的成绩
    public List<Grade> getGradesByStudent(String studentUsername) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.*, c.course_name, c.teacher_name, " +
                "(SELECT nickname FROM users WHERE username = g.student_id) as student_name " +
                "FROM grades g " +
                "JOIN courses c ON g.course_id = c.course_id " +
                "WHERE g.student_id = ? " +
                "ORDER BY c.course_name";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentUsername);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return grades;
    }

    // 获取课程平均分
    public Double getCourseAverage(String courseId) {
        String sql = "SELECT AVG(score) FROM grades WHERE course_id = ? AND score IS NOT NULL";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 教师：上传或更新成绩
    public boolean saveGrade(Grade grade) {
        String sql = "INSERT INTO grades (student_id, course_id, score, grade_letter, exam_date, remark) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "score = VALUES(score), " +
                "grade_letter = VALUES(grade_letter), " +
                "exam_date = VALUES(exam_date), " +
                "remark = VALUES(remark), " +
                "updated_at = CURRENT_TIMESTAMP";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, grade.getStudentId());
            stmt.setString(2, grade.getCourseId());
            stmt.setDouble(3, grade.getScore());
            stmt.setString(4, grade.getGradeLetter());
            stmt.setDate(5, grade.getExamDate() != null ? Date.valueOf(grade.getExamDate()) : null);
            stmt.setString(6, grade.getRemark());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 获取课程统计信息
    public List<Grade> getCourseStatistics(String courseId) {
        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT g.*, c.course_name, c.teacher_name, " +
                "(SELECT nickname FROM users WHERE username = g.student_id) as student_name, " +
                "(SELECT AVG(score) FROM grades WHERE course_id = g.course_id AND score IS NOT NULL) as class_average " +
                "FROM grades g " +
                "JOIN courses c ON g.course_id = c.course_id " +
                "WHERE g.course_id = ? " +
                "ORDER BY g.score DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            int rank = 1;
            while (rs.next()) {
                Grade grade = mapResultSetToGrade(rs);
                grade.setClassRank(rank++);
                grade.setClassAverage(rs.getDouble("class_average"));
                grades.add(grade);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return grades;
    }

    // 获取学生平均分
    public Double getStudentAverage(String studentUsername) {
        String sql = "SELECT AVG(score) FROM grades WHERE student_id = ? AND score IS NOT NULL";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentUsername);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
        Grade grade = new Grade();
        grade.setGradeId(rs.getInt("grade_id"));
        grade.setStudentId(rs.getString("student_id"));
        grade.setCourseId(rs.getString("course_id"));
        grade.setCourseName(rs.getString("course_name"));
        grade.setStudentName(rs.getString("student_name"));
        grade.setTeacherName(rs.getString("teacher_name"));
        grade.setScore(rs.getObject("score", Double.class));
        grade.setGradeLetter(rs.getString("grade_letter"));
        grade.setClassAverage(rs.getObject("class_average", Double.class));
        grade.setClassRank(rs.getObject("class_rank", Integer.class));
        Date examDate = rs.getDate("exam_date");
        if (examDate != null) {
            grade.setExamDate(examDate.toLocalDate());
        }
        grade.setRemark(rs.getString("remark"));
        grade.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        grade.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return grade;
    }
}