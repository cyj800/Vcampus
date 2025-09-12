package client;

import org.json.simple.JSONObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import model.Course;
import model.CourseEnrollment;
import org.json.simple.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientNetwork {
    private static Socket socket;
    private static ObjectOutputStream objectOutputStream;
    private static ObjectInputStream objectInputStream;
    private static boolean isConnected = false;
    private static ClientMessageHandler messageHandler;

    private static ExecutorService networkThreadPool = Executors.newFixedThreadPool(10);
    private static ExecutorService messageHandlingThreadPool = Executors.newSingleThreadExecutor();

    public interface LoginCallback {
        void onLoginResult(boolean success, String message, String username, String nickname);
    }

    public interface RegisterCallback {
        void onRegisterResult(boolean success, String message);
    }

    // 课程相关回调接口
    public interface CourseCallback {
        void onCourseResult(boolean success, String message, List<Course> courses);
    }

    public interface CourseActionCallback {
        void onActionResult(boolean success, String message);
    }

    public interface CourseStudentsCallback {
        void onStudentsResult(boolean success, List<CourseEnrollment> students);
    }

    // 学期列表回调接口
    public interface SemesterCallback {
        void onSemesterResult(boolean success, List<String> semesters);
    }

    public static boolean connectToServer(String serverIP, int port) {
        System.out.println("connectToServer 调用线程: " + Thread.currentThread().getName() +
                " (ID: " + Thread.currentThread().getId() + ")");
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            socket = new Socket(serverIP, port);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            isConnected = true;

            // 启动消息监听线程
            messageHandler = new ClientMessageHandler(objectInputStream);
//            new Thread(messageHandler).start();
            messageHandlingThreadPool.execute(messageHandler);
            messageHandlingThreadPool.execute(messageHandler);
            System.out.println("消息处理任务已提交到线程池");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            isConnected = false;
            return false;
        }
    }

    public static boolean isConnected() {
        return isConnected;
    }

    //    public static void login(String username, String password, LoginCallback callback) {
//        if (!isConnected) {
//            callback.onLoginResult(false, "Not connected to server", null, null);
//            return;
//        }
//
//        try {
//            JSONObject request = new JSONObject();
//            request.put("type", "login");
//            request.put("username", username);
//            request.put("password", password);
//
//            // 设置回调
//            messageHandler.setLoginCallback(callback);
//
//            objectOutputStream.writeObject(request);
//            objectOutputStream.flush();
//        } catch (Exception e) {
//            callback.onLoginResult(false, "Network error: " + e.getMessage(), null, null);
//        }
//    }
    public static void login(String username, String password, LoginCallback callback) {
        System.out.println("=== ClientNetwork.login() 开始 ===");
        System.out.println("传入的用户名: '" + username + "'");
        System.out.println("传入的密码: '" + password + "'");
        System.out.println("连接状态: " + isConnected);

        if (!isConnected) {
            System.out.println("✗ 未连接到服务器");
            callback.onLoginResult(false, "Not connected to server", null, null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "login");
            request.put("username", username);
            request.put("password", password);

            System.out.println("构造的请求JSON: " + request.toJSONString());

            // 设置回调
            messageHandler.setLoginCallback(callback);

            System.out.println("发送登录请求到服务器...");
            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
            System.out.println("✓ 登录请求已发送");

        } catch (Exception e) {
            System.out.println("✗ 网络异常: " + e.getMessage());
            e.printStackTrace();
            callback.onLoginResult(false, "Network error: " + e.getMessage(), null, null);
        }
        System.out.println("=== ClientNetwork.login() 结束 ===");
    }

    public static void register(String username, String password, String email, String nickname, RegisterCallback callback) {
        if (!isConnected) {
            callback.onRegisterResult(false, "Not connected to server");
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "register");
            request.put("username", username);
            request.put("password", password);
            request.put("email", email);
            request.put("nickname", nickname);

            // 设置回调
            messageHandler.setRegisterCallback(callback);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
        } catch (Exception e) {
            callback.onRegisterResult(false, "Network error: " + e.getMessage());
        }
    }


    // 获取所有课程
    public static void getAllCourses(CourseCallback callback) {
        if (!isConnected) {
            callback.onCourseResult(false, "Not connected to server", null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "course");
            request.put("action", "get_all_courses");

            messageHandler.setCourseCallback(callback);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
        } catch (Exception e) {
            callback.onCourseResult(false, "Network error: " + e.getMessage(), null);
        }
    }

    // 按课程号搜索课程
    public static void searchCoursesByCourseId(String courseId, CourseCallback callback) {
        if (!isConnected) {
            callback.onCourseResult(false, "Not connected to server", null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "course");
            request.put("action", "search_courses_by_id");
            request.put("course_id", courseId);

            messageHandler.setCourseCallback(callback);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
        } catch (Exception e) {
            callback.onCourseResult(false, "Network error: " + e.getMessage(), null);
        }
    }

    // 按学期筛选课程
    public static void getCoursesBySemester(String semester, CourseCallback callback) {
        if (!isConnected) {
            callback.onCourseResult(false, "Not connected to server", null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "course");
            request.put("action", "get_courses_by_semester");
            request.put("semester", semester);

            messageHandler.setCourseCallback(callback);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
        } catch (Exception e) {
            callback.onCourseResult(false, "Network error: " + e.getMessage(), null);
        }
    }

    // 获取所有学期
    public static void getAllSemesters(SemesterCallback callback) {
        if (!isConnected) {
            callback.onSemesterResult(false, null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "course");
            request.put("action", "get_all_semesters");

            messageHandler.setSemesterCallback(callback);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
        } catch (Exception e) {
            callback.onSemesterResult(false, null);
        }
    }

    // 获取学生课程
    public static void getStudentCourses(String studentId, CourseCallback callback) {
        if (!isConnected) {
            callback.onCourseResult(false, "Not connected to server", null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "course");
            request.put("action", "get_student_courses");
            if (studentId != null) {
                request.put("student_id", studentId);
            }

            messageHandler.setCourseCallback(callback);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
        } catch (Exception e) {
            callback.onCourseResult(false, "Network error: " + e.getMessage(), null);
        }
    }

    // 获取教师课程
    public static void getTeacherCourses(String teacherId, CourseCallback callback) {
        if (!isConnected) {
            callback.onCourseResult(false, "Not connected to server", null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "course");
            request.put("action", "get_teacher_courses");
            if (teacherId != null) {
                request.put("teacher_id", teacherId);
            }

            messageHandler.setCourseCallback(callback);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
        } catch (Exception e) {
            callback.onCourseResult(false, "Network error: " + e.getMessage(), null);
        }
    }

    // 选课 - 使用回调映射
    public static void enrollCourse(String courseId, CourseActionCallback callback) {
        System.out.println("=== ClientNetwork.enrollCourse 开始 ===");
        System.out.println("课程ID: " + courseId);
        System.out.println("连接状态: " + isConnected);

        if (!isConnected) {
            callback.onActionResult(false, "Not connected to server");
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "course");
            request.put("action", "enroll_course");
            request.put("course_id", courseId);

            // 使用回调映射
            System.out.println("设置选课回调...");
            messageHandler.setActionCallback("enroll_course", callback);

            System.out.println("发送选课请求: " + request.toJSONString());
            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
            System.out.println("选课请求已发送");

        } catch (Exception e) {
            System.err.println("发送选课请求异常: " + e.getMessage());
            e.printStackTrace();
            callback.onActionResult(false, "Network error: " + e.getMessage());
        }
        System.out.println("=== ClientNetwork.enrollCourse 结束 ===");
    }

    // 退课 - 使用回调映射
    public static void dropCourse(String courseId, CourseActionCallback callback) {
        System.out.println("=== ClientNetwork.dropCourse 开始 ===");
        System.out.println("课程ID: " + courseId);
        System.out.println("连接状态: " + isConnected);

        if (!isConnected) {
            callback.onActionResult(false, "Not connected to server");
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "course");
            request.put("action", "drop_course");
            request.put("course_id", courseId);

            // 使用回调映射 - 核心改进
            System.out.println("设置退课回调...");
            messageHandler.setActionCallback("drop_course", callback);

            System.out.println("发送退课请求: " + request.toJSONString());
            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
            System.out.println("退课请求已发送");

        } catch (Exception e) {
            System.err.println("发送退课请求异常: " + e.getMessage());
            e.printStackTrace();
            callback.onActionResult(false, "Network error: " + e.getMessage());
        }
        System.out.println("=== ClientNetwork.dropCourse 结束 ===");
    }

    // 创建课程 - 使用回调映射
    public static void createCourse(Course course, CourseActionCallback callback) {
        System.out.println("=== ClientNetwork.createCourse 开始 ===");

        if (!isConnected) {
            callback.onActionResult(false, "Not connected to server");
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "course");
            request.put("action", "create_course");
            request.put("course_id", course.getCourseId());
            request.put("course_name", course.getCourseName());
            request.put("teacher_id", course.getTeacherId());
            request.put("teacher_name", course.getTeacherName());
            request.put("credits", course.getCredits());
            request.put("max_students", course.getMaxStudents());
            request.put("semester", course.getSemester());
            request.put("class_time", course.getClassTime());
            request.put("classroom", course.getClassroom());

            // 使用回调映射
            System.out.println("设置创建课程回调...");
            messageHandler.setActionCallback("create_course", callback);

            System.out.println("发送创建课程请求: " + request.toJSONString());
            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
            System.out.println("创建课程请求已发送");

        } catch (Exception e) {
            System.err.println("发送创建课程请求异常: " + e.getMessage());
            e.printStackTrace();
            callback.onActionResult(false, "Network error: " + e.getMessage());
        }
        System.out.println("=== ClientNetwork.createCourse 结束 ===");
    }

    // 更新课程 - 使用回调映射
    public static void updateCourse(Course course, CourseActionCallback callback) {
        System.out.println("=== ClientNetwork.updateCourse 开始 ===");

        if (!isConnected) {
            callback.onActionResult(false, "Not connected to server");
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "course");
            request.put("action", "update_course");
            request.put("course_id", course.getCourseId());
            request.put("course_name", course.getCourseName());
            request.put("teacher_id", course.getTeacherId());
            request.put("teacher_name", course.getTeacherName());
            request.put("credits", course.getCredits());
            request.put("max_students", course.getMaxStudents());
            request.put("semester", course.getSemester());
            request.put("class_time", course.getClassTime());
            request.put("classroom", course.getClassroom());

            // 使用回调映射
            System.out.println("设置更新课程回调...");
            messageHandler.setActionCallback("update_course", callback);

            System.out.println("发送更新课程请求: " + request.toJSONString());
            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
            System.out.println("更新课程请求已发送");

        } catch (Exception e) {
            System.err.println("发送更新课程请求异常: " + e.getMessage());
            e.printStackTrace();
            callback.onActionResult(false, "Network error: " + e.getMessage());
        }
        System.out.println("=== ClientNetwork.updateCourse 结束 ===");
    }

    // 删除课程 - 使用回调映射
    public static void deleteCourse(String courseId, CourseActionCallback callback) {
        System.out.println("=== ClientNetwork.deleteCourse 开始 ===");
        System.out.println("课程ID: " + courseId);

        if (!isConnected) {
            callback.onActionResult(false, "Not connected to server");
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "course");
            request.put("action", "delete_course");
            request.put("course_id", courseId);

            // 使用回调映射
            System.out.println("设置删除课程回调...");
            messageHandler.setActionCallback("delete_course", callback);

            System.out.println("发送删除课程请求: " + request.toJSONString());
            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
            System.out.println("删除课程请求已发送");

        } catch (Exception e) {
            System.err.println("发送删除课程请求异常: " + e.getMessage());
            e.printStackTrace();
            callback.onActionResult(false, "Network error: " + e.getMessage());
        }
        System.out.println("=== ClientNetwork.deleteCourse 结束 ===");
    }

    // 获取课程学生名单
    public static void getCourseStudents(String courseId, CourseStudentsCallback callback) {
        if (!isConnected) {
            callback.onStudentsResult(false, null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "course");
            request.put("action", "get_course_students");
            request.put("course_id", courseId);

            messageHandler.setCourseStudentsCallback(callback);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
        } catch (Exception e) {
            callback.onStudentsResult(false, null);
        }
    }

    public static void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                JSONObject logoutRequest = new JSONObject();
                logoutRequest.put("type", "logout");
                objectOutputStream.writeObject(logoutRequest);
                objectOutputStream.flush();

                socket.close();
            }
            isConnected = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 替换 getCoursesByTeacher 方法
    public static void getCoursesByTeacher(String teacherId, CourseCallback callback) {
        if (!isConnected) {
            callback.onCourseResult(false, "Not connected to server", null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "course");
            request.put("action", "get_teacher_courses");
            if (teacherId != null) {
                request.put("teacher_id", teacherId);
            }

            messageHandler.setCourseCallback(callback);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
        } catch (Exception e) {
            callback.onCourseResult(false, "Network error: " + e.getMessage(), null);
        }
    }

    // 替换 getCourseEnrollments 方法
    public static void getCourseEnrollments(String courseId, EnrollmentCallback callback) {
        if (!isConnected) {
            callback.onEnrollmentResult(false, "Not connected to server", null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "course");
            request.put("action", "get_course_enrollments");
            request.put("course_id", courseId);

            // 需要在 messageHandler 中添加对应的回调设置方法
            messageHandler.setEnrollmentCallback(callback);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
        } catch (Exception e) {
            callback.onEnrollmentResult(false, "Network error: " + e.getMessage(), null);
        }
    }


    // 学生名单回调接口
    public interface EnrollmentCallback {
        void onEnrollmentResult(boolean success, String message, List<CourseEnrollment> enrollments);
    }

    public static void shutdown() {
        if (networkThreadPool != null && !networkThreadPool.isShutdown()) {
            networkThreadPool.shutdown();
        }
        if (messageHandlingThreadPool != null && !messageHandlingThreadPool.isShutdown()) {
            messageHandlingThreadPool.shutdown();
        }
    }


    // ==================== 作业管理相关网络方法 ====================

    public interface AssignmentCallback {
        void onResult(boolean success, String message, Object data);
    }

    public interface SubmissionCallback {
        void onResult(boolean success, String message, Object data);
    }

    public interface AppealCallback {
        void onResult(boolean success, String message, Object data);
    }

    /**
     * 创建作业
     */
    public static void createAssignment(String courseId, String title, String description,
                                        String deadline, String submitType, String allowedFileTypes,
                                        Integer maxFileSize, String teacherId, AssignmentCallback callback) {
        if (!isConnected) {
            callback.onResult(false, "未连接到服务器", null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "create_assignment");
            request.put("course_id", courseId);
            request.put("title", title);
            request.put("description", description);
            request.put("deadline", deadline);
            request.put("submit_type", submitType);
            request.put("allowed_file_types", allowedFileTypes);
            request.put("teacher_id", teacherId);

            if (maxFileSize != null) {
                request.put("max_file_size", maxFileSize);
            }

            // 注意：这里需要设置回调处理，可以扩展消息处理器来处理
            objectOutputStream.writeObject(request);
            objectOutputStream.flush();

        } catch (Exception e) {
            callback.onResult(false, "网络错误: " + e.getMessage(), null);
        }
    }

    /**
     * 获取作业列表
     */
    public static void getAssignments(String userType, String userId, String courseId, AssignmentCallback callback) {
        if (!isConnected) {
            callback.onResult(false, "未连接到服务器", null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "get_assignments");
            request.put("user_type", userType);
            request.put("user_id", userId);
            if (courseId != null) {
                request.put("course_id", courseId);
            }

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();

        } catch (Exception e) {
            callback.onResult(false, "网络错误: " + e.getMessage(), null);
        }
    }

    /**
     * 提交作业
     */
    public static void submitAssignment(int assignmentId, String studentId, String content,
                                        SubmissionCallback callback) {
        if (!isConnected) {
            callback.onResult(false, "未连接到服务器", null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "submit_assignment");
            request.put("assignment_id", assignmentId);
            request.put("student_id", studentId);
            request.put("content", content);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();

        } catch (Exception e) {
            callback.onResult(false, "网络错误: " + e.getMessage(), null);
        }
    }

    /**
     * 获取提交记录
     */
    public static void getSubmissions(String userType, String userId, Integer assignmentId,
                                      SubmissionCallback callback) {
        if (!isConnected) {
            callback.onResult(false, "未连接到服务器", null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "get_submissions");
            request.put("user_type", userType);
            request.put("user_id", userId);
            if (assignmentId != null) {
                request.put("assignment_id", assignmentId);
            }

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();

        } catch (Exception e) {
            callback.onResult(false, "网络错误: " + e.getMessage(), null);
        }
    }

    /**
     * 批改作业
     */
    public static void gradeSubmission(int submissionId, int score, String feedback,
                                       String graderId, SubmissionCallback callback) {
        if (!isConnected) {
            callback.onResult(false, "未连接到服务器", null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "grade_submission");
            request.put("submission_id", submissionId);
            request.put("score", score);
            request.put("feedback", feedback);
            request.put("grader_id", graderId);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();

        } catch (Exception e) {
            callback.onResult(false, "网络错误: " + e.getMessage(), null);
        }
    }

    /**
     * 创建申诉
     */
    public static void createAppeal(int submissionId, String studentId, String reason,
                                    AppealCallback callback) {
        if (!isConnected) {
            callback.onResult(false, "未连接到服务器", null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "create_appeal");
            request.put("submission_id", submissionId);
            request.put("student_id", studentId);
            request.put("reason", reason);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();

        } catch (Exception e) {
            callback.onResult(false, "网络错误: " + e.getMessage(), null);
        }
    }

    /**
     * 获取待处理申诉
     */
    public static void getPendingAppeals(AppealCallback callback) {
        if (!isConnected) {
            callback.onResult(false, "未连接到服务器", null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "get_pending_appeals");

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();

        } catch (Exception e) {
            callback.onResult(false, "网络错误: " + e.getMessage(), null);
        }
    }

    // ==================== 文件传输相关网络方法 ====================

    public interface FileDownloadCallback {
        void onDownloadStart(String fileName, long fileSize);
        void onDownloadProgress(long downloadedBytes, long totalBytes);
        void onDownloadComplete(byte[] fileData, String fileName);
        void onDownloadError(String errorMessage);
    }

    /**
     * 下载文件
     */
    public static void downloadFile(int submissionId, FileDownloadCallback callback) {
        if (!isConnected) {
            callback.onDownloadError("未连接到服务器");
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "download_file");
            // 确保使用正确的数字类型
            request.put("submission_id", (long) submissionId); // 使用Long类型

            // 设置文件下载回调
            getMessageHandler().setFileDownloadCallback(callback);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();

        } catch (Exception e) {
            callback.onDownloadError("网络错误: " + e.getMessage());
        }
    }

    // 添加获取消息处理器的方法
    private static ClientMessageHandler getMessageHandler() {
        return messageHandler;
    }
}