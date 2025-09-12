package model;

import java.time.LocalDateTime;

public class StatisticsData {
    private int totalAssignments;      // 作业总数
    private int totalSubmissions;      // 提交总数
    private double completionRate;     // 完成率
    private double averageScore;       // 平均分
    private int pendingAppeals;        // 待处理申诉
    private int processedAppeals;      // 已处理申诉
    private int activeCourses;         // 活跃课程数
    private int activeTeachers;        // 活跃教师数
    private int activeStudents;        // 活跃学生数
    private LocalDateTime startTime;   // 统计开始时间
    private LocalDateTime endTime;     // 统计结束时间
    private double scoreStandardDeviation; // 成绩标准差
    private int excellentRate;         // 优秀率(90分以上)
    private int passRate;             // 及格率(60分以上)
    private double avgProcessingTime;  // 平均申诉处理时间
    private int totalAppeals;          // 总申诉数

    // 构造函数
    public StatisticsData() {
        this.startTime = LocalDateTime.now();
        this.endTime = LocalDateTime.now();
    }

    // Getter和Setter方法
    public int getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(int totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public int getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(int totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(double completionRate) {
        this.completionRate = completionRate;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public int getPendingAppeals() {
        return pendingAppeals;
    }

    public void setPendingAppeals(int pendingAppeals) {
        this.pendingAppeals = pendingAppeals;
    }

    public int getProcessedAppeals() {
        return processedAppeals;
    }

    public void setProcessedAppeals(int processedAppeals) {
        this.processedAppeals = processedAppeals;
    }

    public int getActiveCourses() {
        return activeCourses;
    }

    public void setActiveCourses(int activeCourses) {
        this.activeCourses = activeCourses;
    }

    public int getActiveTeachers() {
        return activeTeachers;
    }

    public void setActiveTeachers(int activeTeachers) {
        this.activeTeachers = activeTeachers;
    }

    public int getActiveStudents() {
        return activeStudents;
    }

    public void setActiveStudents(int activeStudents) {
        this.activeStudents = activeStudents;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public double getScoreStandardDeviation() {
        return scoreStandardDeviation;
    }

    public void setScoreStandardDeviation(double scoreStandardDeviation) {
        this.scoreStandardDeviation = scoreStandardDeviation;
    }

    public int getExcellentRate() {
        return excellentRate;
    }

    public void setExcellentRate(int excellentRate) {
        this.excellentRate = excellentRate;
    }

    public int getPassRate() {
        return passRate;
    }

    public void setPassRate(int passRate) {
        this.passRate = passRate;
    }

    public double getAvgProcessingTime() {
        return avgProcessingTime;
    }

    public void setAvgProcessingTime(double avgProcessingTime) {
        this.avgProcessingTime = avgProcessingTime;
    }

    public int getTotalAppeals() {
        return totalAppeals;
    }

    public void setTotalAppeals(int totalAppeals) {
        this.totalAppeals = totalAppeals;
    }

    // 计算衍生字段
    public double getAppealProcessingRate() {
        return totalAppeals > 0 ? (double) processedAppeals / totalAppeals * 100 : 0;
    }

    public int getUnprocessedAppeals() {
        return totalAppeals - processedAppeals;
    }

    public double getSubmissionGrowthRate() {
        // 简化实现，实际应该对比历史数据
        return 0.0;
    }

    // 便捷方法
    public String getFormattedCompletionRate() {
        return String.format("%.1f%%", completionRate);
    }

    public String getFormattedAverageScore() {
        return String.format("%.1f", averageScore);
    }

    public String getFormattedAppealRate() {
        return String.format("%.1f%%", getAppealProcessingRate());
    }

    @Override
    public String toString() {
        return "StatisticsData{" +
                "totalAssignments=" + totalAssignments +
                ", totalSubmissions=" + totalSubmissions +
                ", completionRate=" + completionRate +
                ", averageScore=" + averageScore +
                ", pendingAppeals=" + pendingAppeals +
                ", processedAppeals=" + processedAppeals +
                ", activeCourses=" + activeCourses +
                ", activeTeachers=" + activeTeachers +
                ", activeStudents=" + activeStudents +
                '}';
    }
}