//package service;
//
//import database.GradeDAO;
//import model.Grade;
//import model.UserRole;
//
//import java.util.List;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//public class GradeService {
//    private GradeDAO gradeDAO;
//    private AtomicBoolean isLoading = new AtomicBoolean(false);
//
//    public GradeService() {
//        this.gradeDAO = new GradeDAO();
//    }
//
//    // 根据用户角色获取成绩列表
//    public List<Grade> getGradesByRole(UserRole role, String userId) {
//        // 防止重复加载
//        if (isLoading.get()) {
//            return null; // 或返回空列表
//        }
//
//        try {
//            isLoading.set(true);
//            switch (role) {
//                case ADMIN:
//                    return gradeDAO.getAllGrades();
//                case TEACHER:
//                    return gradeDAO.getGradesByTeacher(userId);
//                case STUDENT:
//                    return gradeDAO.getGradesByStudent(userId);
//                default:
//                    return gradeDAO.getGradesByStudent(userId);
//            }
//        } finally {
//            isLoading.set(false);
//        }
//    }
//
//    // 获取学生平均分
//    public Double getStudentAverage(String studentId) {
//        return gradeDAO.getStudentAverage(studentId);
//    }
//}

package service;

import database.GradeDAO;
import model.Grade;
import model.Course;
import model.UserRole;

import java.util.List;

public class GradeService {
    private GradeDAO gradeDAO;

    public GradeService() {
        this.gradeDAO = new GradeDAO();
    }

    // 获取所有课程
    public List<Course> getAllCourses() {
        return gradeDAO.getAllCourses();
    }

    // 根据教师获取课程
    public List<Course> getCoursesByTeacher(String teacherUsername) {
        return gradeDAO.getCoursesByTeacher(teacherUsername);
    }

    // 获取课程的所有选课学生
    public List<String> getStudentsByCourse(String courseId) {
        return gradeDAO.getStudentsByCourse(courseId);
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

    // 获取特定课程的成绩（用于统计分析）
    public List<Grade> getGradesByCourse(String courseId) {
        return gradeDAO.getGradesByCourse(courseId);
    }

    // 保存成绩（仅教师和管理员）
    public boolean saveGrade(Grade grade, UserRole role, String userId) {
        if (role == UserRole.TEACHER || role == UserRole.ADMIN) {
            return gradeDAO.saveGrade(grade);
        }
        return false;
    }

    // 获取课程统计信息
    public List<Grade> getCourseStatistics(String courseId) {
        return gradeDAO.getCourseStatistics(courseId);
    }

    // 获取课程平均分
    public Double getCourseAverage(String courseId) {
        return gradeDAO.getCourseAverage(courseId);
    }

    // 获取学生平均分
    public Double getStudentAverage(String studentId) {
        return gradeDAO.getStudentAverage(studentId);
    }

    // 获取课程成绩分布
    public List<Object[]> getGradeDistribution(String courseId) {
        return gradeDAO.getGradeDistribution(courseId);
    }
}