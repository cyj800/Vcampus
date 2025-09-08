//package database;
//
//import model.Grade;
//import model.Course;
//
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class GradeDAO {
//
//    // 获取所有成绩（包含课程信息）
//    public List<Grade> getAllGrades() {
//        List<Grade> grades = new ArrayList<>();
//        Connection conn = null;
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//
//        try {
//            conn = DatabaseManager.getInstance().getConnection();
//            String sql = "SELECT g.*, c.course_name, c.teacher_name, " +
//                    "(SELECT nickname FROM users WHERE username = g.student_id) as student_name " +
//                    "FROM grades g " +
//                    "JOIN courses c ON g.course_id = c.course_id " +
//                    "ORDER BY c.course_name, g.student_id";
//
//            stmt = conn.prepareStatement(sql);
//            rs = stmt.executeQuery();
//
//            while (rs.next()) {
//                grades.add(mapResultSetToGrade(rs));
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            // 确保资源正确关闭
//            closeResources(rs, stmt, conn);
//        }
//
//        return grades;
//    }
//
//    // 教师：获取自己教授课程的成绩
//    public List<Grade> getGradesByTeacher(String teacherUsername) {
//        List<Grade> grades = new ArrayList<>();
//        Connection conn = null;
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//
//        try {
//            conn = DatabaseManager.getInstance().getConnection();
//            String sql = "SELECT g.*, c.course_name, c.teacher_name, " +
//                    "(SELECT nickname FROM users WHERE username = g.student_id) as student_name " +
//                    "FROM grades g " +
//                    "JOIN courses c ON g.course_id = c.course_id " +
//                    "WHERE c.teacher_id = ? " +
//                    "ORDER BY c.course_name, g.student_id";
//
//            stmt = conn.prepareStatement(sql);
//            stmt.setString(1, teacherUsername);
//            rs = stmt.executeQuery();
//
//            while (rs.next()) {
//                grades.add(mapResultSetToGrade(rs));
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            closeResources(rs, stmt, conn);
//        }
//
//        return grades;
//    }
//
//    // 学生：获取自己的成绩
//    public List<Grade> getGradesByStudent(String studentUsername) {
//        List<Grade> grades = new ArrayList<>();
//        Connection conn = null;
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//
//        try {
//            conn = DatabaseManager.getInstance().getConnection();
//            String sql = "SELECT g.*, c.course_name, c.teacher_name, " +
//                    "(SELECT nickname FROM users WHERE username = g.student_id) as student_name " +
//                    "FROM grades g " +
//                    "JOIN courses c ON g.course_id = c.course_id " +
//                    "WHERE g.student_id = ? " +
//                    "ORDER BY c.course_name";
//
//            stmt = conn.prepareStatement(sql);
//            stmt.setString(1, studentUsername);
//            rs = stmt.executeQuery();
//
//            while (rs.next()) {
//                grades.add(mapResultSetToGrade(rs));
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            closeResources(rs, stmt, conn);
//        }
//
//        return grades;
//    }
//
//    // 获取课程平均分
//    public Double getCourseAverage(String courseId) {
//        Connection conn = null;
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//        Double average = null;
//
//        try {
//            conn = DatabaseManager.getInstance().getConnection();
//            String sql = "SELECT AVG(score) FROM grades WHERE course_id = ? AND score IS NOT NULL";
//
//            stmt = conn.prepareStatement(sql);
//            stmt.setString(1, courseId);
//            rs = stmt.executeQuery();
//
//            if (rs.next()) {
//                average = rs.getDouble(1);
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            closeResources(rs, stmt, conn);
//        }
//
//        return average;
//    }
//
//    // 获取学生平均分
//    public Double getStudentAverage(String studentUsername) {
//        Connection conn = null;
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//        Double average = null;
//
//        try {
//            conn = DatabaseManager.getInstance().getConnection();
//            String sql = "SELECT AVG(score) FROM grades WHERE student_id = ? AND score IS NOT NULL";
//
//            stmt = conn.prepareStatement(sql);
//            stmt.setString(1, studentUsername);
//            rs = stmt.executeQuery();
//
//            if (rs.next()) {
//                average = rs.getDouble(1);
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            closeResources(rs, stmt, conn);
//        }
//
//        return average;
//    }
//
//    // 统一的资源关闭方法
//    private void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
//        if (rs != null) {
//            try {
//                rs.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//        if (stmt != null) {
//            try {
//                stmt.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//        if (conn != null) {
//            DatabaseManager.getInstance().releaseConnection(conn);
//        }
//    }
//
//    private Grade mapResultSetToGrade(ResultSet rs) throws SQLException {
//        Grade grade = new Grade();
//        grade.setGradeId(rs.getInt("grade_id"));
//        grade.setStudentId(rs.getString("student_id"));
//        grade.setCourseId(rs.getString("course_id"));
//        grade.setCourseName(rs.getString("course_name"));
//        grade.setStudentName(rs.getString("student_name"));
//        grade.setTeacherName(rs.getString("teacher_name"));
//        grade.setScore(rs.getObject("score", Double.class));
//        grade.setGradeLetter(rs.getString("grade_letter"));
//        grade.setClassAverage(rs.getObject("class_average", Double.class));
//        grade.setClassRank(rs.getObject("class_rank", Integer.class));
//        Date examDate = rs.getDate("exam_date");
//        if (examDate != null) {
//            grade.setExamDate(examDate.toLocalDate());
//        }
//        grade.setRemark(rs.getString("remark"));
//        grade.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
//        grade.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
//        return grade;
//    }
//}

package database;

import model.Grade;
import model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradeDAO {

    // 获取所有课程信息
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT * FROM courses ORDER BY course_name";

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Course course = new Course();
                course.setCourseId(rs.getString("course_id"));
                course.setCourseName(rs.getString("course_name"));
                course.setTeacherId(rs.getString("teacher_id"));
                course.setTeacherName(rs.getString("teacher_name"));
                course.setCredits(rs.getInt("credits"));
                course.setSemester(rs.getString("semester"));
                course.setClassTime(rs.getString("class_time"));
                course.setClassroom(rs.getString("classroom"));
                course.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                course.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                courses.add(course);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }

        return courses;
    }

    // 根据教师获取课程
    public List<Course> getCoursesByTeacher(String teacherUsername) {
        List<Course> courses = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT * FROM courses WHERE teacher_id = ? ORDER BY course_name";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, teacherUsername);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Course course = new Course();
                course.setCourseId(rs.getString("course_id"));
                course.setCourseName(rs.getString("course_name"));
                course.setTeacherId(rs.getString("teacher_id"));
                course.setTeacherName(rs.getString("teacher_name"));
                course.setCredits(rs.getInt("credits"));
                course.setSemester(rs.getString("semester"));
                course.setClassTime(rs.getString("class_time"));
                course.setClassroom(rs.getString("classroom"));
                course.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                course.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                courses.add(course);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }

        return courses;
    }

    // 获取课程的所有选课学生
    public List<String> getStudentsByCourse(String courseId) {
        List<String> students = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT student_id FROM enrollments WHERE course_id = ? AND status = 'active'";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                students.add(rs.getString("student_id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }

        return students;
    }

    // 获取所有成绩（包含课程信息）
    public List<Grade> getAllGrades() {
        List<Grade> grades = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT g.*, c.course_name, c.teacher_name, " +
                    "(SELECT nickname FROM users WHERE username = g.student_id) as student_name " +
                    "FROM grades g " +
                    "JOIN courses c ON g.course_id = c.course_id " +
                    "ORDER BY c.course_name, g.student_id";

            stmt = conn.prepareStatement(sql);
            rs = stmt.executeQuery();

            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }

        return grades;
    }

    // 教师：获取自己教授课程的成绩
    public List<Grade> getGradesByTeacher(String teacherUsername) {
        List<Grade> grades = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT g.*, c.course_name, c.teacher_name, " +
                    "(SELECT nickname FROM users WHERE username = g.student_id) as student_name " +
                    "FROM grades g " +
                    "JOIN courses c ON g.course_id = c.course_id " +
                    "WHERE c.teacher_id = ? " +
                    "ORDER BY c.course_name, g.student_id";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, teacherUsername);
            rs = stmt.executeQuery();

            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }

        return grades;
    }

    // 学生：获取自己的成绩
    public List<Grade> getGradesByStudent(String studentUsername) {
        List<Grade> grades = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT g.*, c.course_name, c.teacher_name, " +
                    "(SELECT nickname FROM users WHERE username = g.student_id) as student_name " +
                    "FROM grades g " +
                    "JOIN courses c ON g.course_id = c.course_id " +
                    "WHERE g.student_id = ? " +
                    "ORDER BY c.course_name";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentUsername);
            rs = stmt.executeQuery();

            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }

        return grades;
    }

    // 获取特定课程的所有成绩（用于统计分析）
    public List<Grade> getGradesByCourse(String courseId) {
        List<Grade> grades = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT g.*, c.course_name, c.teacher_name, " +
                    "(SELECT nickname FROM users WHERE username = g.student_id) as student_name " +
                    "FROM grades g " +
                    "JOIN courses c ON g.course_id = c.course_id " +
                    "WHERE g.course_id = ? " +
                    "ORDER BY g.score DESC";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                grades.add(mapResultSetToGrade(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }

        return grades;
    }

    // 教师：上传或更新成绩
    public boolean saveGrade(Grade grade) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            String sql = "INSERT INTO grades (student_id, course_id, score, grade_letter, exam_date, remark) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "score = VALUES(score), " +
                    "grade_letter = VALUES(grade_letter), " +
                    "exam_date = VALUES(exam_date), " +
                    "remark = VALUES(remark), " +
                    "updated_at = CURRENT_TIMESTAMP";

            stmt = conn.prepareStatement(sql);
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
        } finally {
            closeResources(null, stmt, conn);
        }
    }

    // 获取课程统计信息
    public List<Grade> getCourseStatistics(String courseId) {
        List<Grade> grades = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT g.*, c.course_name, c.teacher_name, " +
                    "(SELECT nickname FROM users WHERE username = g.student_id) as student_name, " +
                    "(SELECT AVG(score) FROM grades WHERE course_id = g.course_id AND score IS NOT NULL) as class_average " +
                    "FROM grades g " +
                    "JOIN courses c ON g.course_id = c.course_id " +
                    "WHERE g.course_id = ? " +
                    "ORDER BY g.score DESC";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseId);
            rs = stmt.executeQuery();

            int rank = 1;
            while (rs.next()) {
                Grade grade = mapResultSetToGrade(rs);
                grade.setClassRank(rank++);
                grade.setClassAverage(rs.getDouble("class_average"));
                grades.add(grade);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }

        return grades;
    }

    // 获取课程平均分
    public Double getCourseAverage(String courseId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Double average = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT AVG(score) FROM grades WHERE course_id = ? AND score IS NOT NULL";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                average = rs.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }

        return average;
    }

    // 获取学生平均分
    public Double getStudentAverage(String studentUsername) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Double average = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT AVG(score) FROM grades WHERE student_id = ? AND score IS NOT NULL";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentUsername);
            rs = stmt.executeQuery();

            if (rs.next()) {
                average = rs.getDouble(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }

        return average;
    }

    // 获取课程成绩分布统计
    public List<Object[]> getGradeDistribution(String courseId) {
        List<Object[]> distribution = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseManager.getInstance().getConnection();
            String sql = "SELECT " +
                    "COUNT(CASE WHEN score >= 90 THEN 1 END) as excellent, " +
                    "COUNT(CASE WHEN score >= 80 AND score < 90 THEN 1 END) as good, " +
                    "COUNT(CASE WHEN score >= 70 AND score < 80 THEN 1 END) as fair, " +
                    "COUNT(CASE WHEN score >= 60 AND score < 70 THEN 1 END) as pass, " +
                    "COUNT(CASE WHEN score < 60 THEN 1 END) as fail, " +
                    "COUNT(*) as total " +
                    "FROM grades WHERE course_id = ? AND score IS NOT NULL";

            stmt = conn.prepareStatement(sql);
            stmt.setString(1, courseId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                distribution.add(new Object[]{"90-100分", rs.getInt("excellent")});
                distribution.add(new Object[]{"80-89分", rs.getInt("good")});
                distribution.add(new Object[]{"70-79分", rs.getInt("fair")});
                distribution.add(new Object[]{"60-69分", rs.getInt("pass")});
                distribution.add(new Object[]{"60分以下", rs.getInt("fail")});
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }

        return distribution;
    }

    // 统一的资源关闭方法
    private void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (conn != null) {
            DatabaseManager.getInstance().releaseConnection(conn);
        }
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