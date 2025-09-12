package database;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class StatisticsDAO {

    // 获取指定时间范围内的作业数量
    public int getAssignmentCount(LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT COUNT(*) FROM assignments WHERE created_at BETWEEN ? AND ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startTime));
            stmt.setTimestamp(2, Timestamp.valueOf(endTime));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("查询作业数量失败: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // 获取待处理申诉数量
    public int getPendingAppealCount() {
        String sql = "SELECT COUNT(*) FROM assignment_appeals WHERE status = 'pending'";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("查询待处理申诉数量失败: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // 获取已处理申诉数量
    public int getProcessedAppealCount() {
        String sql = "SELECT COUNT(*) FROM assignment_appeals WHERE status IN ('approved', 'rejected')";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("查询已处理申诉数量失败: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // 获取教师总数 - 修正版本
    public int getTotalTeacherCount() {
        // 根据您的UserRole枚举，教师用户名以"1"开头
        String sql = "SELECT COUNT(*) FROM users WHERE username LIKE '1%'";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("查询到教师数量: " + count);
                return count;
            }

        } catch (SQLException e) {
            System.err.println("查询教师数量失败: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // 获取学生总数 - 修正版本
    public int getTotalStudentCount() {
        // 根据您的UserRole枚举，学生用户名以"2"开头
        String sql = "SELECT COUNT(*) FROM users WHERE username LIKE '2%'";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("查询到学生数量: " + count);
                return count;
            }

        } catch (SQLException e) {
            System.err.println("查询学生数量失败: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // 获取总课程数量
    public int getTotalCourseCount() {
        String sql = "SELECT COUNT(*) FROM courses";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("查询课程数量失败: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // 获取指定时间范围内的提交数量
    public int getSubmissionCount(LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT COUNT(*) FROM submissions WHERE submit_time BETWEEN ? AND ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startTime));
            stmt.setTimestamp(2, Timestamp.valueOf(endTime));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("查询提交数量失败: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // 获取指定时间范围内的平均分
    public double getAverageScore(LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT AVG(score) FROM submissions WHERE score IS NOT NULL " +
                "AND submit_time BETWEEN ? AND ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(startTime));
            stmt.setTimestamp(2, Timestamp.valueOf(endTime));

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }

        } catch (SQLException e) {
            System.err.println("查询平均分失败: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    // 获取同比时间段的作业数量
    public int getAssignmentCountYoY(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime lastYearStart = startTime.minus(1, ChronoUnit.YEARS);
        LocalDateTime lastYearEnd = endTime.minus(1, ChronoUnit.YEARS);

        return getAssignmentCount(lastYearStart, lastYearEnd);
    }

    // 获取同比时间段的提交数量
    public int getSubmissionCountYoY(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime lastYearStart = startTime.minus(1, ChronoUnit.YEARS);
        LocalDateTime lastYearEnd = endTime.minus(1, ChronoUnit.YEARS);

        return getSubmissionCount(lastYearStart, lastYearEnd);
    }

    // 获取同比时间段的平均分
    public double getAverageScoreYoY(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime lastYearStart = startTime.minus(1, ChronoUnit.YEARS);
        LocalDateTime lastYearEnd = endTime.minus(1, ChronoUnit.YEARS);

        return getAverageScore(lastYearStart, lastYearEnd);
    }

    // 计算同比增长率
    public String calculateGrowthRate(int currentValue, int previousValue) {
        if (previousValue == 0) {
            return currentValue > 0 ? "+100%" : "0%";
        }
        double rate = ((double)(currentValue - previousValue) / previousValue) * 100;
        return (rate >= 0 ? "+" : "") + String.format("%.1f", rate) + "%";
    }

    // 计算同比增长率（double版本）
    public String calculateGrowthRate(double currentValue, double previousValue) {
        if (previousValue == 0) {
            return currentValue > 0 ? "+100%" : "0%";
        }
        double rate = ((currentValue - previousValue) / previousValue) * 100;
        return (rate >= 0 ? "+" : "") + String.format("%.1f", rate) + "%";
    }
}