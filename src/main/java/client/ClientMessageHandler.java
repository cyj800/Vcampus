package client;

import org.json.simple.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import model.Course;
import model.CourseEnrollment;
import org.json.simple.JSONArray;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ClientMessageHandler implements Runnable {
    private ObjectInputStream input;
    private ClientNetwork.LoginCallback loginCallback;
    private ClientNetwork.RegisterCallback registerCallback;


    // 课程相关回调
    private ClientNetwork.CourseCallback courseCallback;
    private ClientNetwork.CourseStudentsCallback courseStudentsCallback;
    private ClientNetwork.SemesterCallback semesterCallback;

    // 使用Map管理不同操作的回调 - 核心改进
    private Map<String, ClientNetwork.CourseActionCallback> actionCallbacks = new ConcurrentHashMap<>();
    // 在 ClientMessageHandler 类中添加
    // 正确的引用 - 使用完整的类名
    private ClientNetwork.EnrollmentCallback enrollmentCallback;

    public void setEnrollmentCallback(ClientNetwork.EnrollmentCallback callback) {
        this.enrollmentCallback = callback;
    }


    // 在消息处理方法中添加对应的处理逻辑
    public ClientMessageHandler(ObjectInputStream input) {
        this.input = input;
    }

    public void setLoginCallback(ClientNetwork.LoginCallback callback) {
        this.loginCallback = callback;
    }

    public void setRegisterCallback(ClientNetwork.RegisterCallback callback) {
        this.registerCallback = callback;
    }

    // 设置课程回调方法
    public void setCourseCallback(ClientNetwork.CourseCallback callback) {
        this.courseCallback = callback;
    }

    public void setCourseStudentsCallback(ClientNetwork.CourseStudentsCallback callback) {
        this.courseStudentsCallback = callback;
    }

    // 设置学期回调
    public void setSemesterCallback(ClientNetwork.SemesterCallback callback) {
        this.semesterCallback = callback;
    }

    // 新增：设置操作回调的方法
    public void setActionCallback(String action, ClientNetwork.CourseActionCallback callback) {
        System.out.println("设置操作回调 - 操作: " + action + ", 回调: " + (callback != null ? "非空" : "空"));
        actionCallbacks.put(action, callback);
    }

    // 新增：移除操作回调的方法
    public void removeActionCallback(String action) {
        actionCallbacks.remove(action);
    }

    // 新增：清空所有操作回调
    public void clearActionCallbacks() {
        actionCallbacks.clear();
    }

    private ClientNetwork.FileDownloadCallback fileDownloadCallback;
    private ByteArrayOutputStream fileBuffer;
    private long totalFileSize;
    private long downloadedBytes;
    private String currentFileName;

    public void setFileDownloadCallback(ClientNetwork.FileDownloadCallback callback) {
        this.fileDownloadCallback = callback;
    }


    @Override
    public void run() {
        System.out.println("消息处理器线程启动: " + Thread.currentThread().getName() +
                " (ID: " + Thread.currentThread().getId() + ")");
        try {
            while (true) {
                JSONObject message = (JSONObject) input.readObject();
                System.out.println("接收到消息的线程: " + Thread.currentThread().getName() +
                        " (ID: " + Thread.currentThread().getId() + ")");
                handleMessage(message);
            }
        } catch (Exception e) {
            System.out.println("Connection lost: " + e.getMessage());
        }
        System.out.println("消息处理器线程结束: " + Thread.currentThread().getName());
    }


//    private void handleMessage(JSONObject message) {
//        String type = (String) message.get("type");
//
//        switch (type) {
//            case "login_result":
//                if (loginCallback != null) {
//                    String status = (String) message.get("status");
//                    boolean success = "success".equals(status);
//                    String msg = (String) message.get("message");
//                    String username = (String) message.get("username");
//                    String nickname = (String) message.get("nickname");
//                    loginCallback.onLoginResult(success, msg, username, nickname);
//                }
//                break;
//
//            case "register_result":
//                if (registerCallback != null) {
//                    String msg = (String) message.get("message");
//                    boolean success = msg.contains("successful");
//                    registerCallback.onRegisterResult(success, msg);
//                }
//                break;
//
//            case "chat":
//                // 处理聊天消息
//                String sender = (String) message.get("sender");
//                String msg = (String) message.get("msg");
//                System.out.println("[" + sender + "]: " + msg);
//                break;
//
//            case "error":
//                String errorMsg = (String) message.get("message");
//                System.out.println("Server Error: " + errorMsg);
//                break;
//        }
//    }

    private void handleMessage(JSONObject message) {
        try {
            String type = (String) message.get("type");
            System.out.println("=== 收到服务器消息，类型: " + type + " ===");
            System.out.println("完整消息内容: " + message.toJSONString());

            switch (type) {
                case "login_result":
                    handleLoginResult(message);
                    break;

                case "register_result":
                    handleRegisterResult(message);
                    break;

                case "course_response":
                    handleCourseResponse(message);
                    break;

                case "chat":
                    handleChatMessage(message);
                    break;

                case "error":
                    handleErrorMessage(message);
                    break;
                case "create_assignment_result":
                    handleAssignmentResult(message);
                    break;
                case "get_assignments_result":
                    handleAssignmentsResult(message);
                    break;
                case "submit_assignment_result":
                    handleSubmissionResult(message);
                    break;
                case "get_submissions_result":
                    handleSubmissionsResult(message);
                    break;
                case "grade_submission_result":
                    handleSubmissionResult(message);
                    break;
                case "create_appeal_result":
                    handleAppealResult(message);
                    break;
                case "get_pending_appeals_result":
                    handlePendingAppealsResult(message);
                    break;

                // 文件传输相关case
                case "file_download_start":
                    handleFileDownloadStart(message);
                    break;
                case "file_chunk":
                    handleFileChunk(message);
                    break;
                case "file_download_complete":
                    handleFileDownloadComplete(message);
                    break;
                case "file_download_error":
                    handleFileDownloadError(message);
                    break;
                default:
                    System.out.println("未知消息类型: " + type);
            }
        } catch (Exception e) {
            System.err.println("处理消息时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 处理登录结果
    private void handleLoginResult(JSONObject message) {
        if (loginCallback != null) {
            String status = (String) message.get("status");
            boolean success = "success".equals(status);
            String msg = (String) message.get("message");

            System.out.println("登录结果 - 状态: " + status + ", 消息: " + msg);

            // 只有当登录成功时才尝试获取用户名和昵称
            String username = success ? (String) message.get("username") : null;
            String nickname = success ? (String) message.get("nickname") : null;

            if (success) {
                System.out.println("登录成功 - 用户名: " + username + ", 昵称: " + nickname);
            }

            loginCallback.onLoginResult(success, msg, username, nickname);
        }
    }

    // 处理注册结果
    private void handleRegisterResult(JSONObject message) {
        if (registerCallback != null) {
            Boolean success = (Boolean) message.get("success");
            String msg = (String) message.get("message");

            System.out.println("注册结果 - 成功: " + success + ", 消息: " + msg);

            registerCallback.onRegisterResult(success != null ? success : false, msg);
        }
    }

    // 处理聊天消息
    private void handleChatMessage(JSONObject message) {
        String sender = (String) message.get("sender");
        String msg = (String) message.get("msg");
        System.out.println("[聊天] " + sender + ": " + msg);
    }

    // 处理错误消息
    private void handleErrorMessage(JSONObject message) {
        String errorMsg = (String) message.get("message");
        System.out.println("服务器错误: " + errorMsg);
    }

    // 处理课程响应 - 核心改进
    private void handleCourseResponse(JSONObject message) {
        String action = (String) message.get("action");
        Boolean success = (Boolean) message.get("success");
        String msg = (String) message.get("message");

        System.out.println("课程响应 - 操作: " + action + ", 成功: " + success + ", 消息: " + msg);
        System.out.println("当前活跃的操作回调: " + actionCallbacks.keySet());

        switch (action) {
            case "get_all_courses":
                handleGetAllCoursesResponse(message, success, msg);
                break;

            case "get_student_courses":
                handleGetStudentCoursesResponse(message, success, msg);
                break;

            case "get_teacher_courses":
                handleGetTeacherCoursesResponse(message, success, msg);
                break;

            case "search_courses_by_id":
                handleSearchCoursesResponse(message, success, msg);
                break;

            case "get_courses_by_semester":
                handleGetCoursesBySemesterResponse(message, success, msg);
                break;

            case "get_all_semesters":
                handleGetAllSemestersResponse(message, success, msg);
                break;

            case "get_course_students":
            case "get_course_enrollments":
                handleGetCourseStudentsResponse(message, success, msg);
                break;

            // 使用回调映射处理操作响应
            case "enroll_course":
            case "drop_course":
            case "create_course":
            case "update_course":
            case "delete_course":
                handleActionResponse(action, success, msg);
                break;

            default:
                System.out.println("未知的课程操作: " + action);
        }
    }

    // 新增：统一处理操作响应的方法
    private void handleActionResponse(String action, Boolean success, String msg) {
        System.out.println("=== 处理操作响应: " + action + " ===");

        ClientNetwork.CourseActionCallback callback = actionCallbacks.get(action);
        if (callback != null) {
            boolean isSuccess = success != null ? success : false;
            System.out.println("找到回调，执行结果 - 成功: " + isSuccess + ", 消息: " + msg);

            try {
                callback.onActionResult(isSuccess, msg);
                System.out.println("回调执行完成");
            } catch (Exception e) {
                System.err.println("执行回调时发生异常: " + e.getMessage());
                e.printStackTrace();
            }

            // 使用后移除回调，防止内存泄漏和回调混乱
            actionCallbacks.remove(action);
            System.out.println("已移除操作回调: " + action);
        } else {
            System.err.println("警告：未找到操作 " + action + " 的回调！");
            System.err.println("当前可用的回调: " + actionCallbacks.keySet());
        }
    }

    // 处理获取所有课程响应
    private void handleGetAllCoursesResponse(JSONObject message, Boolean success, String msg) {
        if (courseCallback != null) {
            List<Course> courses = null;
            if (success != null && success) {
                courses = parseCourses((JSONArray) message.get("courses"));
                System.out.println("成功获取所有课程，共 " + (courses != null ? courses.size() : 0) + " 门");
            } else {
                System.out.println("获取所有课程失败: " + msg);
            }
            courseCallback.onCourseResult(success != null ? success : false, msg, courses);
        }
    }

    // 处理获取学生课程响应
    private void handleGetStudentCoursesResponse(JSONObject message, Boolean success, String msg) {
        if (courseCallback != null) {
            List<Course> courses = null;
            if (success != null && success) {
                courses = parseCourses((JSONArray) message.get("courses"));
                System.out.println("成功获取学生课程，共 " + (courses != null ? courses.size() : 0) + " 门");
            } else {
                System.out.println("获取学生课程失败: " + msg);
            }
            courseCallback.onCourseResult(success != null ? success : false, msg, courses);
        }
    }

    // 处理获取教师课程响应
    private void handleGetTeacherCoursesResponse(JSONObject message, Boolean success, String msg) {
        if (courseCallback != null) {
            List<Course> courses = null;
            if (success != null && success) {
                courses = parseCourses((JSONArray) message.get("courses"));
                System.out.println("成功获取教师课程，共 " + (courses != null ? courses.size() : 0) + " 门");
            } else {
                System.out.println("获取教师课程失败: " + msg);
            }
            courseCallback.onCourseResult(success != null ? success : false, msg, courses);
        }
    }

    // 处理搜索课程响应
    private void handleSearchCoursesResponse(JSONObject message, Boolean success, String msg) {
        if (courseCallback != null) {
            List<Course> courses = null;
            if (success != null && success) {
                courses = parseCourses((JSONArray) message.get("courses"));
                System.out.println("课程搜索完成，找到 " + (courses != null ? courses.size() : 0) + " 门课程");
            } else {
                System.out.println("课程搜索失败: " + msg);
            }
            courseCallback.onCourseResult(success != null ? success : false, msg, courses);
        }
    }

    // 处理按学期筛选课程响应
    private void handleGetCoursesBySemesterResponse(JSONObject message, Boolean success, String msg) {
        if (courseCallback != null) {
            List<Course> courses = null;
            if (success != null && success) {
                courses = parseCourses((JSONArray) message.get("courses"));
                System.out.println("学期筛选完成，找到 " + (courses != null ? courses.size() : 0) + " 门课程");
            } else {
                System.out.println("按学期筛选课程失败: " + msg);
            }
            courseCallback.onCourseResult(success != null ? success : false, msg, courses);
        }
    }

    // 处理获取所有学期响应
    private void handleGetAllSemestersResponse(JSONObject message, Boolean success, String msg) {
        if (semesterCallback != null) {
            List<String> semesters = null;
            if (success != null && success) {
                semesters = parseSemesters((JSONArray) message.get("semesters"));
                System.out.println("成功获取学期列表，共 " + (semesters != null ? semesters.size() : 0) + " 个学期");
            } else {
                System.out.println("获取学期列表失败: " + msg);
            }
            semesterCallback.onSemesterResult(success != null ? success : false, semesters);
        }
    }

    // 处理获取课程学生名单响应
    private void handleGetCourseStudentsResponse(JSONObject message, Boolean success, String msg) {
        if (courseStudentsCallback != null) {
            List<CourseEnrollment> students = null;
            if (success != null && success) {
                students = parseStudents((JSONArray) message.get("students"));
                System.out.println("成功获取课程学生名单，共 " + (students != null ? students.size() : 0) + " 名学生");
            } else {
                System.out.println("获取课程学生名单失败: " + msg);
            }
            courseStudentsCallback.onStudentsResult(success != null ? success : false, students);
        }
    }

    // 解析课程JSON数组 - 增强版本
    private List<Course> parseCourses(JSONArray coursesArray) {
        System.out.println("=== 开始解析课程数据 ===");
        System.out.println("课程数组长度: " + (coursesArray != null ? coursesArray.size() : 0));

        List<Course> courses = new ArrayList<>();
        if (coursesArray != null) {
            for (int i = 0; i < coursesArray.size(); i++) {
                try {
                    JSONObject courseObj = (JSONObject) coursesArray.get(i);
                    Course course = parseSingleCourse(courseObj, i);
                    if (course != null) {
                        courses.add(course);
                    }
                } catch (Exception e) {
                    System.err.println("解析第 " + i + " 门课程时出错: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        System.out.println("总共成功解析了 " + courses.size() + " 门课程");
        System.out.println("=== 课程数据解析完成 ===");
        return courses;
    }

    // 解析单个课程对象
    private Course parseSingleCourse(JSONObject courseObj, int index) {
        try {
            System.out.println("解析第 " + index + " 门课程...");

            Course course = new Course();

            // 基本信息
            course.setCourseId((String) courseObj.get("course_id"));
            course.setCourseName((String) courseObj.get("course_name"));
            course.setTeacherId((String) courseObj.get("teacher_id"));
            course.setTeacherName((String) courseObj.get("teacher_name"));

            // 数字字段 - 使用安全转换
            course.setCredits(safeIntegerConvert(courseObj.get("credits")));
            course.setMaxStudents(safeIntegerConvert(courseObj.get("max_students")));
            course.setCurrentStudents(safeIntegerConvert(courseObj.get("current_students")));

            // 其他信息
            course.setSemester((String) courseObj.get("semester"));
            course.setClassTime((String) courseObj.get("class_time"));
            course.setClassroom((String) courseObj.get("classroom"));

            // 时间字段
            parseTimeFields(course, courseObj);

            System.out.println("成功解析课程: " + course.getCourseId() + " - " + course.getCourseName() +
                    " (当前人数: " + course.getCurrentStudents() + "/" + course.getMaxStudents() + ")");

            return course;

        } catch (Exception e) {
            System.err.println("解析课程对象失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // 解析时间字段
    private void parseTimeFields(Course course, JSONObject courseObj) {
        // 解析创建时间
        String createdAtStr = (String) courseObj.get("created_at");
        if (createdAtStr != null && !createdAtStr.trim().isEmpty()) {
            try {
                course.setCreatedAt(LocalDateTime.parse(createdAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } catch (Exception e) {
                System.err.println("解析created_at失败: " + createdAtStr + " - " + e.getMessage());
            }
        }

        // 解析更新时间
        String updatedAtStr = (String) courseObj.get("updated_at");
        if (updatedAtStr != null && !updatedAtStr.trim().isEmpty()) {
            try {
                course.setUpdatedAt(LocalDateTime.parse(updatedAtStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } catch (Exception e) {
                System.err.println("解析updated_at失败: " + updatedAtStr + " - " + e.getMessage());
            }
        }
    }

    // 安全的整数转换方法 - 增强版本
    private int safeIntegerConvert(Object obj) {
        if (obj == null) {
            return 0;
        }

        try {
            if (obj instanceof Integer) {
                return (Integer) obj;
            } else if (obj instanceof Long) {
                return ((Long) obj).intValue();
            } else if (obj instanceof Number) {
                return ((Number) obj).intValue();
            } else if (obj instanceof String) {
                String str = ((String) obj).trim();
                if (str.isEmpty()) {
                    return 0;
                }
                return Integer.parseInt(str);
            } else {
                return Integer.parseInt(obj.toString());
            }
        } catch (NumberFormatException e) {
            System.err.println("无法转换为整数: " + obj + " (类型: " + obj.getClass().getSimpleName() + ")");
            return 0;
        }
    }

    // 解析学期列表
    private List<String> parseSemesters(JSONArray semestersArray) {
        System.out.println("=== 开始解析学期数据 ===");

        List<String> semesters = new ArrayList<>();
        if (semestersArray != null) {
            for (Object obj : semestersArray) {
                if (obj != null) {
                    String semester = obj.toString().trim();
                    if (!semester.isEmpty()) {
                        semesters.add(semester);
                        System.out.println("解析学期: " + semester);
                    }
                }
            }
        }

        System.out.println("总共解析了 " + semesters.size() + " 个学期");
        System.out.println("=== 学期数据解析完成 ===");
        return semesters;
    }

    // 解析学生选课记录JSON数组
    private List<CourseEnrollment> parseStudents(JSONArray studentsArray) {
        System.out.println("=== 开始解析学生选课记录 ===");

        List<CourseEnrollment> students = new ArrayList<>();
        if (studentsArray != null) {
            for (int i = 0; i < studentsArray.size(); i++) {
                try {
                    JSONObject studentObj = (JSONObject) studentsArray.get(i);
                    CourseEnrollment enrollment = parseSingleEnrollment(studentObj, i);
                    if (enrollment != null) {
                        students.add(enrollment);
                    }
                } catch (Exception e) {
                    System.err.println("解析第 " + i + " 条选课记录时出错: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        System.out.println("总共解析了 " + students.size() + " 条选课记录");
        System.out.println("=== 学生选课记录解析完成 ===");
        return students;
    }

    // 解析单个选课记录
    private CourseEnrollment parseSingleEnrollment(JSONObject studentObj, int index) {
        try {
            CourseEnrollment enrollment = new CourseEnrollment();

            // 基本信息
            enrollment.setEnrollmentId(safeIntegerConvert(studentObj.get("enrollment_id")));
            enrollment.setStudentId((String) studentObj.get("student_id"));
            enrollment.setCourseId((String) studentObj.get("course_id"));
            enrollment.setStatus((String) studentObj.get("status"));

            // 解析选课时间
            String enrollmentDateStr = (String) studentObj.get("enrollment_date");
            if (enrollmentDateStr != null && !enrollmentDateStr.trim().isEmpty()) {
                try {
                    enrollment.setEnrollmentDate(LocalDateTime.parse(enrollmentDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                } catch (Exception e) {
                    System.err.println("解析enrollment_date失败: " + enrollmentDateStr + " - " + e.getMessage());
                }
            }

            // 可选的附加字段
            enrollment.setStudentName((String) studentObj.get("student_name"));
            enrollment.setCourseName((String) studentObj.get("course_name"));
            enrollment.setTeacherName((String) studentObj.get("teacher_name"));
            enrollment.setClassroom((String) studentObj.get("classroom"));
            enrollment.setClassTime((String) studentObj.get("class_time"));
            enrollment.setCredits(safeIntegerConvert(studentObj.get("credits")));

            System.out.println("成功解析选课记录: 学生 " + enrollment.getStudentId() +
                    " 选择课程 " + enrollment.getCourseId());

            return enrollment;

        } catch (Exception e) {
            System.err.println("解析选课记录失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    private void handleAssignmentResult(JSONObject message) {
        String status = (String) message.get("status");
        boolean success = "success".equals(status);
        String msg = (String) message.get("message");

        // 这里需要根据具体使用场景设置回调
        System.out.println("Assignment result: " + msg);
    }

    private void handleAssignmentsResult(JSONObject message) {
        String status = (String) message.get("status");
        if ("success".equals(status)) {
            JSONArray assignmentsArray = (JSONArray) message.get("assignments");
            // 处理作业列表数据
            System.out.println("Received " + assignmentsArray.size() + " assignments");
            // 这里应该通知UI层更新作业列表
        }
    }

    private void handleSubmissionResult(JSONObject message) {
        String status = (String) message.get("status");
        boolean success = "success".equals(status);
        String msg = (String) message.get("message");

        System.out.println("Submission result: " + msg);
    }

    private void handleSubmissionsResult(JSONObject message) {
        String status = (String) message.get("status");
        if ("success".equals(status)) {
            JSONArray submissionsArray = (JSONArray) message.get("submissions");
            // 处理提交记录列表数据
            System.out.println("Received " + submissionsArray.size() + " submissions");
            // 这里应该通知UI层更新提交列表
        }
    }

    private void handleAppealResult(JSONObject message) {
        String status = (String) message.get("status");
        boolean success = "success".equals(status);
        String msg = (String) message.get("message");

        System.out.println("Appeal result: " + msg);
    }

    private void handlePendingAppealsResult(JSONObject message) {
        String status = (String) message.get("status");
        if ("success".equals(status)) {
            JSONArray appealsArray = (JSONArray) message.get("appeals");
            // 处理待处理申诉列表数据
            System.out.println("Received " + appealsArray.size() + " pending appeals");
            // 这里应该通知UI层更新申诉列表
        }
    }

    // ==================== 文件传输相关处理方法 ====================

    private void handleFileDownloadStart(JSONObject message) {
        if (fileDownloadCallback != null) {
            try {
                currentFileName = (String) message.get("file_name");
                totalFileSize = ((Long) message.get("file_size")).longValue();
                downloadedBytes = 0;

                // 初始化文件缓冲区
                fileBuffer = new ByteArrayOutputStream();

                fileDownloadCallback.onDownloadStart(currentFileName, totalFileSize);
            } catch (Exception e) {
                fileDownloadCallback.onDownloadError("处理文件开始消息失败: " + e.getMessage());
            }
        }
    }

    private void handleFileChunk(JSONObject message) {
        if (fileDownloadCallback != null && fileBuffer != null) {
            try {
                byte[] data = (byte[]) message.get("data");
                // 修复类型转换问题
                Object sizeObj = message.get("size");
                int size = convertToInteger(sizeObj); // 使用安全转换方法

                if (data != null && data.length >= size) {
                    // 写入数据到缓冲区
                    fileBuffer.write(data, 0, size);
                    downloadedBytes += size;

                    // 通知进度更新
                    fileDownloadCallback.onDownloadProgress(downloadedBytes, totalFileSize);
                }
            } catch (Exception e) {
                fileDownloadCallback.onDownloadError("处理文件块失败: " + e.getMessage());
            }
        }
    }

    // 添加安全的整数转换方法
    private int convertToInteger(Object obj) {
        if (obj == null) {
            return 0;
        }

        try {
            if (obj instanceof Integer) {
                return (Integer) obj;
            } else if (obj instanceof Long) {
                return ((Long) obj).intValue();
            } else if (obj instanceof Number) {
                return ((Number) obj).intValue();
            } else {
                return Integer.parseInt(obj.toString());
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void handleFileDownloadComplete(JSONObject message) {
        if (fileDownloadCallback != null && fileBuffer != null) {
            try {
                // 获取完整的文件数据
                byte[] fileData = fileBuffer.toByteArray();

                // 通知下载完成
                fileDownloadCallback.onDownloadComplete(fileData, currentFileName);

                // 清理资源
                fileBuffer = null;
                currentFileName = null;
                totalFileSize = 0;
                downloadedBytes = 0;

            } catch (Exception e) {
                fileDownloadCallback.onDownloadError("处理文件完成消息失败: " + e.getMessage());
            }
        }
    }

    private void handleFileDownloadError(JSONObject message) {
        if (fileDownloadCallback != null) {
            try {
                String errorMessage = (String) message.get("message");
                fileDownloadCallback.onDownloadError(errorMessage);

                // 清理资源
                fileBuffer = null;
                currentFileName = null;
                totalFileSize = 0;
                downloadedBytes = 0;

            } catch (Exception e) {
                fileDownloadCallback.onDownloadError("处理文件错误消息失败: " + e.getMessage());
            }
        }
    }
}