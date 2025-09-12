package database;
import model.Course;
import model.CourseEnrollment;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {

    // 创建课程 - 添加max_students字段
    public boolean createCourse(Course course) {
        if (!isValidTeacher(course.getTeacherId(), course.getTeacherName())) {
            System.err.println("无效的教师信息，不能创建课程: 教师ID=" + course.getTeacherId() + ", 教师姓名=" + course.getTeacherName());
            return false;
        }

        String sql = "INSERT INTO courses (course_id, course_name, teacher_id, teacher_name, " +
                "credits, max_students, semester, class_time, classroom) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, course.getCourseId());
            stmt.setString(2, course.getCourseName());
            stmt.setString(3, course.getTeacherId());
            stmt.setString(4, course.getTeacherName());
            stmt.setInt(5, course.getCredits());
            stmt.setInt(6, course.getMaxStudents() != null ? course.getMaxStudents() : 50); // 默认50人
            stmt.setString(7, course.getSemester());
            stmt.setString(8, course.getClassTime());
            stmt.setString(9, course.getClassroom());

            int rowsAffected = stmt.executeUpdate();
            System.out.println("Course created successfully: " + course.getCourseName());
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Failed to create course: " + course.getCourseName());
            e.printStackTrace();
            return false;
        }
    }

    // 更新课程信息 - 添加max_students字段
    public boolean updateCourse(Course course) {
        if (!isValidTeacher(course.getTeacherId(), course.getTeacherName())) {
            System.err.println("无效的教师信息，不能更新课程: 教师ID=" + course.getTeacherId() + ", 教师姓名=" + course.getTeacherName());
            return false;
        }

        String sql = "UPDATE courses SET course_name = ?, teacher_id = ?, teacher_name = ?, " +
                "credits = ?, max_students = ?, semester = ?, class_time = ?, classroom = ?, " +
                "updated_at = CURRENT_TIMESTAMP WHERE course_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, course.getCourseName());
            stmt.setString(2, course.getTeacherId());
            stmt.setString(3, course.getTeacherName());
            stmt.setInt(4, course.getCredits());
            stmt.setInt(5, course.getMaxStudents() != null ? course.getMaxStudents() : 50);
            stmt.setString(6, course.getSemester());
            stmt.setString(7, course.getClassTime());
            stmt.setString(8, course.getClassroom());
            stmt.setString(9, course.getCourseId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Course updated successfully: " + course.getCourseName());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Failed to update course: " + course.getCourseName());
            e.printStackTrace();
        }

        return false;
    }

    // 修改 getAllCourses() 方法 - 添加当前学生数查询
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        System.out.println("=== 服务器端 getAllCourses() 开始 ===");

        // 使用子查询获取当前选课人数
        String sql = "SELECT c.course_id, c.course_name, c.teacher_id, c.teacher_name, " +
                "c.credits, c.semester, c.class_time, c.classroom, c.max_students, " +
                "c.created_at, c.updated_at, " +
                "(SELECT COUNT(*) FROM enrollments e WHERE e.course_id = c.course_id AND e.status = 'active') as current_students " +
                "FROM courses c ORDER BY c.created_at DESC";

        System.out.println("执行SQL: " + sql);

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("SQL执行成功，开始处理结果");
            while (rs.next()) {
                try {
                    System.out.println("处理课程: " + rs.getString("course_id"));
                    Course course = mapResultSetToCourse(rs);
                    courses.add(course);
                    System.out.println("成功添加课程: " + course.getCourseId() +
                            ", 当前人数: " + course.getCurrentStudents() +
                            ", 最大人数: " + course.getMaxStudents());

                } catch (Exception e) {
                    System.err.println("处理单个课程时出错: " + e.getMessage());
                    e.printStackTrace();
                    // 继续处理下一条记录
                }
            }

            System.out.println("总共处理了 " + courses.size() + " 门课程");

        } catch (SQLException e) {
            System.err.println("执行SQL查询时出错: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("=== 服务器端 getAllCourses() 结束 ===");
        return courses;
    }

    // 新增：综合搜索课程 - 支持按课程ID、课程名称、教师名称搜索
    public List<Course> searchCourses(String keyword) {
        List<Course> courses = new ArrayList<>();
        System.out.println("=== 综合搜索课程，关键词: " + keyword + " ===");

        String sql = "SELECT c.*, " +
                "(SELECT COUNT(*) FROM enrollments e WHERE e.course_id = c.course_id AND e.status = 'active') as current_students " +
                "FROM courses c " +
                "WHERE c.course_id LIKE ? OR c.course_name LIKE ? OR c.teacher_name LIKE ? " +
                "ORDER BY c.course_id";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            System.out.println("搜索模式: " + searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Course course = mapResultSetToCourse(rs);
                courses.add(course);
                System.out.println("找到课程: " + course.getCourseId() + " - " + course.getCourseName() +
                        " (教师: " + course.getTeacherName() + ")");
            }

            System.out.println("搜索完成，找到 " + courses.size() + " 门课程");

        } catch (SQLException e) {
            System.err.println("Failed to search courses with keyword: " + keyword);
            e.printStackTrace();
        }

        return courses;
    }

    // 保留原有的按课程号搜索方法（为了兼容性）
    public List<Course> searchCoursesByCourseId(String courseId) {
        return searchCourses(courseId);
    }

    // 删除：按学期筛选课程的方法已移除

    // 删除：获取所有可用学期的方法已移除

    // 根据教师获取课程
    public List<Course> getCoursesByTeacher(String teacherId) {
        List<Course> courses = new ArrayList<>();
        System.out.println("=== 获取教师课程，教师ID: " + teacherId + " ===");

        String sql = "SELECT c.*, " +
                "(SELECT COUNT(*) FROM enrollments e WHERE e.course_id = c.course_id AND e.status = 'active') as current_students " +
                "FROM courses c WHERE c.teacher_id = ? ORDER BY c.class_time";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, teacherId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Course course = mapResultSetToCourse(rs);
                courses.add(course);
                System.out.println("找到教师课程: " + course.getCourseId() + " - " + course.getCourseName());
            }

            System.out.println("教师 " + teacherId + " 共有 " + courses.size() + " 门课程");

        } catch (SQLException e) {
            System.err.println("Failed to get courses by teacher: " + teacherId);
            e.printStackTrace();
        }

        return courses;
    }

    // 根据课程ID获取课程
    public Course getCourseById(String courseId) {
        System.out.println("=== 根据ID获取课程: " + courseId + " ===");

        String sql = "SELECT c.*, " +
                "(SELECT COUNT(*) FROM enrollments e WHERE e.course_id = c.course_id AND e.status = 'active') as current_students " +
                "FROM courses c WHERE c.course_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Course course = mapResultSetToCourse(rs);
                System.out.println("找到课程: " + course.getCourseId() + " - " + course.getCourseName() +
                        ", 当前人数: " + course.getCurrentStudents());
                return course;
            } else {
                System.out.println("未找到课程: " + courseId);
            }

        } catch (SQLException e) {
            System.err.println("Failed to get course by ID: " + courseId);
            e.printStackTrace();
        }

        return null;
    }

    // 删除课程
    public boolean deleteCourse(String courseId) {
        System.out.println("=== 删除课程: " + courseId + " ===");

        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 首先删除相关的选课记录
            String deleteEnrollmentsSql = "DELETE FROM enrollments WHERE course_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteEnrollmentsSql)) {
                stmt.setString(1, courseId);
                int enrollmentDeleted = stmt.executeUpdate();
                System.out.println("Deleted " + enrollmentDeleted + " enrollment records for course: " + courseId);
            }

            // 然后删除课程
            String deleteCourseSql = "DELETE FROM courses WHERE course_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteCourseSql)) {
                stmt.setString(1, courseId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    conn.commit();
                    System.out.println("Course deleted successfully, ID: " + courseId);
                    return true;
                } else {
                    conn.rollback();
                    System.err.println("No course found with ID: " + courseId);
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to delete course ID: " + courseId);
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Failed to rollback transaction");
                    rollbackEx.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Failed to close connection");
                    closeEx.printStackTrace();
                }
            }
        }

        return false;
    }

    // 学生选课 - 添加容量检查和详细日志
    public boolean enrollStudent(String courseId, String studentId) {
        System.out.println("=== 学生选课: " + studentId + " 选择课程 " + courseId + " ===");

        Connection conn = null;
        try {
            conn = DatabaseManager.getInstance().getConnection();
            conn.setAutoCommit(false);

            // 检查课程是否存在并获取容量信息
            Course course = getCourseById(courseId);
            if (course == null) {
                System.err.println("Course not found: " + courseId);
                return false;
            }

            // 检查是否已经选过这门课
            if (isStudentEnrolled(courseId, studentId)) {
                System.err.println("Student already enrolled: " + studentId + " in course " + courseId);
                return false;
            }

            // 检查课程容量
            int currentStudents = course.getCurrentStudents();
            int maxStudents = course.getMaxStudents() != null ? course.getMaxStudents() : 50;

            System.out.println("课程容量检查 - 当前人数: " + currentStudents + ", 最大人数: " + maxStudents);

            if (currentStudents >= maxStudents) {
                System.err.println("Course is full: " + courseId + " (" + currentStudents + "/" + maxStudents + ")");
                return false;
            }

            // 插入选课记录
            String sql = "INSERT INTO enrollments (student_id, course_id, status, enrollment_date) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, studentId);
                stmt.setString(2, courseId);
                stmt.setString(3, "active");

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    conn.commit();
                    System.out.println("Student enrolled successfully: " + studentId + " in course " + courseId);
                    return true;
                } else {
                    conn.rollback();
                    System.err.println("Failed to insert enrollment record");
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("Failed to enroll student: " + studentId + " in course " + courseId);
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }

        return false;
    }

    public boolean dropCourse(String courseId, String studentId) {
        System.out.println("=== 学生退课: " + studentId + " 退选课程 " + courseId + " ===");

        // 先检查学生是否确实选择了这门课程
        if (!isStudentEnrolled(courseId, studentId)) {
            System.err.println("学生未选择此课程: " + studentId + " - " + courseId);
            return false;
        }

        // 直接删除选课记录
        String sql = "DELETE FROM enrollments WHERE course_id = ? AND student_id = ? AND status = 'active'";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            stmt.setString(2, studentId);

            System.out.println("执行退课SQL: " + sql);
            System.out.println("参数 - 课程ID: " + courseId + ", 学生ID: " + studentId);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("SQL执行结果，影响行数: " + rowsAffected);

            if (rowsAffected > 0) {
                System.out.println("学生退课成功: " + studentId + " 退选课程 " + courseId);
                return true;
            } else {
                System.err.println("退课失败 - 未找到有效的选课记录");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("执行退课SQL时发生异常: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // 检查学生是否已选择某门课程
    public boolean isStudentEnrolled(String courseId, String studentId) {
        String sql = "SELECT COUNT(*) FROM enrollments WHERE course_id = ? AND student_id = ? AND status = 'active'";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            stmt.setString(2, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boolean isEnrolled = rs.getInt(1) > 0;
                System.out.println("检查选课状态 - 学生: " + studentId + ", 课程: " + courseId + ", 已选: " + isEnrolled);
                return isEnrolled;
            }

        } catch (SQLException e) {
            System.err.println("Failed to check enrollment status");
            e.printStackTrace();
        }

        return false;
    }

    // 获取学生的课程列表
    public List<Course> getStudentCourses(String studentId) {
        List<Course> courses = new ArrayList<>();
        System.out.println("=== 获取学生课程列表，学生ID: " + studentId + " ===");

        String sql = "SELECT c.*, " +
                "(SELECT COUNT(*) FROM enrollments e WHERE e.course_id = c.course_id AND e.status = 'active') as current_students " +
                "FROM courses c " +
                "JOIN enrollments en ON c.course_id = en.course_id " +
                "WHERE en.student_id = ? AND en.status = 'active' " +
                "ORDER BY c.class_time";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Course course = mapResultSetToCourse(rs);
                courses.add(course);
                System.out.println("学生已选课程: " + course.getCourseId() + " - " + course.getCourseName());
            }

            System.out.println("学生 " + studentId + " 共选择了 " + courses.size() + " 门课程");

        } catch (SQLException e) {
            System.err.println("Failed to get student courses: " + studentId);
            e.printStackTrace();
        }

        return courses;
    }

    // 获取课程的学生名单 - 优化版本
    public List<CourseEnrollment> getCourseEnrollments(String courseId) {
        List<CourseEnrollment> enrollments = new ArrayList<>();
        System.out.println("=== 获取课程学生名单，课程ID: " + courseId + " ===");

        // 优化SQL查询，确保获取所有必要字段
        String sql = "SELECT e.enrollment_id, e.student_id, e.course_id, e.status, e.enrollment_date, " +
                "COALESCE(u.nickname, u.username) as student_name, " +
                "c.credits, c.course_name, c.teacher_name, c.classroom, c.class_time " +
                "FROM enrollments e " +
                "LEFT JOIN users u ON e.student_id = u.username " +
                "LEFT JOIN courses c ON e.course_id = c.course_id " +
                "WHERE e.course_id = ? AND e.status = 'active' " +
                "ORDER BY e.enrollment_date";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CourseEnrollment enrollment = new CourseEnrollment();

                // 基本信息
                enrollment.setEnrollmentId(rs.getInt("enrollment_id"));
                enrollment.setStudentId(rs.getString("student_id"));
                enrollment.setCourseId(rs.getString("course_id"));
                enrollment.setStatus(rs.getString("status"));

                // 时间信息
                Timestamp enrollmentDate = rs.getTimestamp("enrollment_date");
                if (enrollmentDate != null) {
                    enrollment.setEnrollmentDate(enrollmentDate.toLocalDateTime());
                }

                // 学生信息
                String studentName = rs.getString("student_name");
                if (studentName == null || studentName.trim().isEmpty()) {
                    // 如果没有找到姓名，根据ID生成显示名称
                    studentName = generateDisplayName(rs.getString("student_id"));
                }
                enrollment.setStudentName(studentName);

                // 课程信息
                enrollment.setCredits(rs.getInt("credits"));
                enrollment.setCourseName(rs.getString("course_name"));
                enrollment.setTeacherName(rs.getString("teacher_name"));
                enrollment.setClassroom(rs.getString("classroom"));
                enrollment.setClassTime(rs.getString("class_time"));

                enrollments.add(enrollment);

                System.out.println("课程学生: " + enrollment.getStudentId() + " - " + enrollment.getStudentName() +
                        " (学分: " + enrollment.getCredits() + ")");
            }

            System.out.println("课程 " + courseId + " 共有 " + enrollments.size() + " 名学生");

        } catch (SQLException e) {
            System.err.println("Failed to get course enrollments for course: " + courseId);
            e.printStackTrace();
        }

        return enrollments;
    }

    // 根据学生ID生成显示名称的辅助方法
    private String generateDisplayName(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return "未知用户";
        }

        String id = studentId.trim();
        char firstChar = id.charAt(0);

        // 根据首字母判断身份
        if (firstChar >= '0' && firstChar <= '9') {
            return "学生-" + id; // 数字开头为学生
        } else if (firstChar >= 'A' && firstChar <= 'Z') {
            return "教师-" + id; // 大写字母开头为教师
        } else if (firstChar >= 'a' && firstChar <= 'z') {
            return "管理员-" + id; // 小写字母开头为管理员
        } else {
            return "用户-" + id; // 其他情况
        }
    }

    // 映射ResultSet到Course对象
    private Course mapResultSetToCourse(ResultSet rs) throws SQLException {
        Course course = new Course();

        course.setCourseId(rs.getString("course_id"));
        course.setCourseName(rs.getString("course_name"));
        course.setTeacherId(rs.getString("teacher_id"));
        course.setTeacherName(rs.getString("teacher_name"));
        course.setCredits(rs.getInt("credits"));
        course.setSemester(rs.getString("semester"));
        course.setClassTime(rs.getString("class_time"));
        course.setClassroom(rs.getString("classroom"));
        course.setMaxStudents(rs.getInt("max_students"));

        // 安全获取 current_students
        try {
            course.setCurrentStudents(rs.getInt("current_students"));
        } catch (SQLException e) {
            // 如果获取失败，设为0
            System.err.println("获取current_students失败: " + e.getMessage());
            course.setCurrentStudents(0);
        }

        // 安全获取时间戳
        try {
            Timestamp createdTimestamp = rs.getTimestamp("created_at");
            if (createdTimestamp != null) {
                course.setCreatedAt(createdTimestamp.toLocalDateTime());
            }
        } catch (SQLException e) {
            System.err.println("获取created_at失败: " + e.getMessage());
        }

        try {
            Timestamp updatedTimestamp = rs.getTimestamp("updated_at");
            if (updatedTimestamp != null) {
                course.setUpdatedAt(updatedTimestamp.toLocalDateTime());
            }
        } catch (SQLException e) {
            System.err.println("获取updated_at失败: " + e.getMessage());
        }

        return course;
    }

    private boolean isValidTeacher(String teacherId, String teacherName) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND (nickname = ? OR username = ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, teacherId);
            stmt.setString(2, teacherName);
            stmt.setString(3, teacherName); // 兼容昵称或用户名匹配

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }

        } catch (SQLException e) {
            System.err.println("校验教师身份失败: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}

