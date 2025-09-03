package service;

import database.GradeDAO;
import model.Grade;
import model.UserRole;

import java.util.List;

public class GradeService {
    private GradeDAO gradeDAO;

    public GradeService() {
        this.gradeDAO = new GradeDAO();
    }

    // 根据用户角色获取成绩列表
    public List<Grade> getGradesByRole(UserRole role, String userId) {
        switch (role) {
            case ADMIN:
                return gradeDAO.getAllGrades();
            case TEACHER:
                return gradeDAO.getGradesByTeacher(userId);
            case STUDENT:
                return gradeDAO.getGradesByStudent(userId);
            default:
                return gradeDAO.getGradesByStudent(userId);
        }
    }

    // 保存成绩（仅教师和管理员）
    public boolean saveGrade(Grade grade, UserRole role, String userId) {
        if (role == UserRole.TEACHER || role == UserRole.ADMIN) {
            return gradeDAO.saveGrade(grade);
        }
        return false;
    }

    // 获取课程统计信息
    public List<Grade> getCourseStatistics(String courseId, UserRole role, String userId) {
        // 教师只能查看自己课程的统计，管理员可以查看所有
        if (role == UserRole.TEACHER || role == UserRole.ADMIN) {
            return gradeDAO.getCourseStatistics(courseId);
        }
        return null;
    }

    // 获取课程平均分
    public Double getCourseAverage(String courseId) {
        return gradeDAO.getCourseAverage(courseId);
    }

    // 获取学生平均分
    public Double getStudentAverage(String studentId) {
        return gradeDAO.getStudentAverage(studentId);
    }
}