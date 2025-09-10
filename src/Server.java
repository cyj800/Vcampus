
import model.User;
import model.Course;
import model.CourseEnrollment;
import database.UserDAO;
import database.CourseDAO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Server {
    private static UserDAO userDAO = new UserDAO();
    private static CourseDAO courseDAO = new CourseDAO(); // 添加CourseDAO
    private static ConcurrentHashMap<String, Socket> onlineUsers = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try {
            System.out.println("Server starting...");
            ServerSocket serverSocket = new ServerSocket(8888);
            System.out.println("Server started on port 8888");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress());
                new Thread(new ServerHandler(socket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class ServerHandler implements Runnable {
        private Socket socket;
        private ObjectInputStream input;
        private ObjectOutputStream output;
        private String currentUsername;
        private String currentUserRole; // 添加用户角色

        public ServerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                input = new ObjectInputStream(socket.getInputStream());
                output = new ObjectOutputStream(socket.getOutputStream());

                while (true) {
                    JSONObject request = (JSONObject) input.readObject();
                    handleRequest(request);
                }
            } catch (Exception e) {
                System.out.println("Client disconnected: " + currentUsername);
                if (currentUsername != null) {
                    onlineUsers.remove(currentUsername);
                }
                try {
                    socket.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void handleRequest(JSONObject request) {
            try {
                String type = (String) request.get("type");

                switch (type) {
                    case "register":
                        handleRegister(request);
                        break;
                    case "login":
                        handleLogin(request);
                        break;
                    case "logout":
                        handleLogout();
                        break;
                    case "chat":
                        handleChat(request);
                        break;
                    case "heart":
                        // 心跳包，不做处理
                        break;
                    case "course":
                        handleCourseRequest(request);
                    default:
                        sendResponse("error", "Unknown request type: " + type);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse("error", "Server error: " + e.getMessage());
            }
        }

        private void handleRegister(JSONObject request) {
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            String email = (String) request.get("email");
            String nickname = (String) request.get("nickname");

            // 检查用户名是否已存在
            if (userDAO.isUsernameExists(username)) {
                JSONObject response = new JSONObject();
                response.put("type", "register_result");
                response.put("success", false);
                response.put("message", "Username already exists");
                sendJSONResponse(response);
                return;
            }

            User user = new User(username, password, email, nickname);
            boolean success = userDAO.registerUser(user);

            JSONObject response = new JSONObject();
            response.put("type", "register_result");
            response.put("success", success);
            response.put("message", success ? "Registration successful" : "Registration failed");
            sendJSONResponse(response);
        }

        private void handleLogin(JSONObject request) {
            String username = (String) request.get("username");
            String password = (String) request.get("password");

            User user = userDAO.loginUser(username, password);

            try {
                JSONObject response = new JSONObject();
                response.put("type", "login_result");
                if (user != null) {
                    currentUsername = username;
                    // 保存用户角色
                    onlineUsers.put(username, socket);
                    response.put("status", "success");
                    response.put("username", user.getUsername());
                    response.put("nickname", user.getNickname());
                    response.put("role", user.getRole()); // 添加角色信息
                    response.put("message", "Login successful");
                } else {
                    response.put("status", "fail");
                    response.put("message", "Invalid username or password");
                }
                output.writeObject(response);
                output.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private void handleCourseRequest(JSONObject request) {
            String action = (String) request.get("action");

            switch (action) {
                case "get_all_courses":
                    handleGetAllCourses();
                    break;
                case "search_courses_by_id":
                    handleSearchCoursesByCourseId(request);
                    break;

                case "get_student_courses":
                    handleGetStudentCourses(request);
                    break;
                case "get_teacher_courses":
                    handleGetTeacherCourses(request);
                    break;
                case "enroll_course":
                    handleEnrollCourse(request);
                    break;
                case "drop_course":
                    handleDropCourse(request);
                    break;
                case "create_course":
                    handleCreateCourse(request);
                    break;
                case "update_course":
                    handleUpdateCourse(request);
                    break;
                case "delete_course":
                    handleDeleteCourse(request);
                    break;
                case "get_course_students":
                    handleGetCourseStudents(request);
                    break;
                default:
                    sendCourseResponse(action, false, "Unknown course action: " + action, null);
            }
        }
        private JSONObject courseToJson(Course course) throws Exception {
            System.out.println("=== courseToJson 开始: " + course.getCourseId() + " ===");

            JSONObject courseObj = new JSONObject();

            try {
                // 使用下划线命名以匹配客户端期望
                courseObj.put("course_id", course.getCourseId() != null ? course.getCourseId() : "");
                courseObj.put("course_name", course.getCourseName() != null ? course.getCourseName() : "");
                courseObj.put("teacher_id", course.getTeacherId() != null ? course.getTeacherId() : "");
                courseObj.put("teacher_name", course.getTeacherName() != null ? course.getTeacherName() : "");
                courseObj.put("credits", course.getCredits());
                courseObj.put("semester", course.getSemester() != null ? course.getSemester() : "");
                courseObj.put("class_time", course.getClassTime() != null ? course.getClassTime() : "");
                courseObj.put("classroom", course.getClassroom() != null ? course.getClassroom() : "");
                courseObj.put("max_students", course.getMaxStudents());
                courseObj.put("current_students", course.getCurrentStudents());

                // 暂时不包含时间字段，避免序列化问题
                // if (course.getCreatedAt() != null) {
                //     courseObj.put("created_at", course.getCreatedAt().toString());
                // }
                // if (course.getUpdatedAt() != null) {
                //     courseObj.put("updated_at", course.getUpdatedAt().toString());
                // }

                System.out.println("courseToJson 完成");
                return courseObj;

            } catch (Exception e) {
                System.err.println("courseToJson 过程中出错: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }

        private void handleGetAllCourses() {
            System.out.println("=== 处理获取所有课程请求 ===");

            try {
                List<Course> courses = courseDAO.getAllCourses();
                System.out.println("从数据库获取到 " + courses.size() + " 门课程");

                JSONArray coursesArray = new JSONArray();

                for (int i = 0; i < courses.size(); i++) {
                    Course course = courses.get(i);
                    try {
                        System.out.println("开始转换第 " + i + " 门课程: " + course.getCourseId());
                        JSONObject courseObj = courseToJson(course);
                        coursesArray.add(courseObj);
                        System.out.println("成功转换第 " + i + " 门课程");
                    } catch (Exception e) {
                        System.err.println("转换第 " + i + " 门课程时出错: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                // 修改响应格式以匹配客户端期望
                JSONObject response = new JSONObject();
                response.put("type", "course_response");    // 改为 course_response
                response.put("action", "get_all_courses");  // 添加 action 字段
                response.put("success", true);
                response.put("message", "获取课程列表成功");
                response.put("courses", coursesArray);

                System.out.println("准备发送响应，包含 " + coursesArray.size() + " 门课程");
                sendJSONResponse(response);
                System.out.println("响应发送完成");

            } catch (Exception e) {
                System.err.println("处理获取所有课程请求时出错: " + e.getMessage());
                e.printStackTrace();

                JSONObject errorResponse = new JSONObject();
                errorResponse.put("type", "course_response");    // 改为 course_response
                errorResponse.put("action", "get_all_courses");  // 添加 action 字段
                errorResponse.put("success", false);
                errorResponse.put("message", "服务器错误: " + e.getMessage());
                sendJSONResponse(errorResponse);
            }
        }

        private void handleSearchCoursesByCourseId(JSONObject request) {
            try {
                String courseId = (String) request.get("course_id");
                List<Course> courses = courseDAO.searchCoursesByCourseId(courseId);
                JSONArray coursesArray = convertCoursesToJSON(courses);
                sendCourseResponse("search_courses_by_id", true, "Success", coursesArray);
            } catch (Exception e) {
                sendCourseResponse("search_courses_by_id", false, "Search failed: " + e.getMessage(), null);
            }
        }




        private void handleGetStudentCourses(JSONObject request) {
            try {
                String studentId = (String) request.get("student_id");
                if (studentId == null) {
                    studentId = currentUsername; // 使用当前登录用户
                }
                List<Course> courses = courseDAO.getStudentCourses(studentId);
                JSONArray coursesArray = convertCoursesToJSON(courses);
                sendCourseResponse("get_student_courses", true, "Success", coursesArray);
            } catch (Exception e) {
                sendCourseResponse("get_student_courses", false, "Failed to get student courses: " + e.getMessage(), null);
            }
        }

        private void handleGetTeacherCourses(JSONObject request) {
            try {
                String teacherId = (String) request.get("teacher_id");
                if (teacherId == null) {
                    teacherId = currentUsername; // 使用当前登录用户
                }
                List<Course> courses = courseDAO.getCoursesByTeacher(teacherId);
                JSONArray coursesArray = convertCoursesToJSON(courses);
                sendCourseResponse("get_teacher_courses", true, "Success", coursesArray);
            } catch (Exception e) {
                sendCourseResponse("get_teacher_courses", false, "Failed to get teacher courses: " + e.getMessage(), null);
            }
        }

        private void handleEnrollCourse(JSONObject request) {
            try {
                String courseId = (String) request.get("course_id");
                boolean success = courseDAO.enrollStudent(courseId, currentUsername);
                String message = success ? "Enrollment successful" : "Enrollment failed";
                sendCourseResponse("enroll_course", success, message, null);
            } catch (Exception e) {
                sendCourseResponse("enroll_course", false, "Enrollment error: " + e.getMessage(), null);
            }
        }

        private void handleDropCourse(JSONObject request) {
            try {
                String courseId = (String) request.get("course_id");
                boolean success = courseDAO.dropCourse(courseId, currentUsername);
                String message = success ? "Drop course successful" : "Drop course failed";
                sendCourseResponse("drop_course", success, message, null);
            } catch (Exception e) {
                sendCourseResponse("drop_course", false, "Drop course error: " + e.getMessage(), null);
            }
        }

        private void handleCreateCourse(JSONObject request) {
            try {
                Course course = new Course();
                course.setCourseId((String) request.get("course_id"));
                course.setCourseName((String) request.get("course_name"));
                course.setTeacherId((String) request.get("teacher_id"));
                course.setTeacherName((String) request.get("teacher_name"));

                // 处理 credits
                Object creditsObj = request.get("credits");
                if (creditsObj != null) {
                    course.setCredits(convertToInteger(creditsObj));
                }

                // 处理 max_students
                Object maxStudentsObj = request.get("max_students");
                if (maxStudentsObj != null) {
                    course.setMaxStudents(convertToInteger(maxStudentsObj));
                }

                course.setSemester((String) request.get("semester"));
                course.setClassTime((String) request.get("class_time"));
                course.setClassroom((String) request.get("classroom"));

                boolean success = courseDAO.createCourse(course);
                String message = success ? "Course created successfully" : "Failed to create course";
                sendCourseResponse("create_course", success, message, null);
            } catch (Exception e) {
                sendCourseResponse("create_course", false, "Create course error: " + e.getMessage(), null);
            }
        }

        // 自定义方法，将 Object 转换为 Integer
        private Integer convertToInteger(Object obj) {
            if (obj instanceof Integer) {
                return (Integer) obj;
            } else if (obj instanceof Long) {
                return ((Long) obj).intValue();
            } else {
                // 如果是其他类型，尝试解析为数字，这里可以根据实际情况调整
                try {
                    return Integer.parseInt(obj.toString());
                } catch (NumberFormatException e) {
                    // 转换失败，返回默认值，也可以抛异常
                    return 0;
                }
            }
        }

        private void handleUpdateCourse(JSONObject request) {
            try {
                Course course = new Course();
                course.setCourseId((String) request.get("course_id"));
                course.setCourseName((String) request.get("course_name"));
                course.setTeacherId((String) request.get("teacher_id"));
                course.setTeacherName((String) request.get("teacher_name"));

                // 使用安全的类型转换方法
                Object creditsObj = request.get("credits");
                if (creditsObj != null) {
                    course.setCredits(convertToInteger(creditsObj));
                }

                Object maxStudentsObj = request.get("max_students");
                if (maxStudentsObj != null) {
                    course.setMaxStudents(convertToInteger(maxStudentsObj));
                }

                course.setSemester((String) request.get("semester"));
                course.setClassTime((String) request.get("class_time"));
                course.setClassroom((String) request.get("classroom"));

                boolean success = courseDAO.updateCourse(course);
                String message = success ? "Course updated successfully" : "Failed to update course";
                sendCourseResponse("update_course", success, message, null);
            } catch (Exception e) {
                sendCourseResponse("update_course", false, "Update course error: " + e.getMessage(), null);
            }
        }

        private void handleDeleteCourse(JSONObject request) {
            try {


                String courseId = (String) request.get("course_id");
                boolean success = courseDAO.deleteCourse(courseId);
                String message = success ? "Course deleted successfully" : "Failed to delete course";
                sendCourseResponse("delete_course", success, message, null);
            } catch (Exception e) {
                sendCourseResponse("delete_course", false, "Delete course error: " + e.getMessage(), null);
            }
        }

        private void handleGetCourseStudents(JSONObject request) {
            try {
                String courseId = (String) request.get("course_id");
                List<CourseEnrollment> students = courseDAO.getCourseEnrollments(courseId);
                JSONArray studentsArray = convertStudentsToJSON(students);

                JSONObject response = new JSONObject();
                response.put("type", "course_response");
                response.put("action", "get_course_students");
                response.put("success", true);
                response.put("students", studentsArray);
                sendJSONResponse(response);
            } catch (Exception e) {
                JSONObject response = new JSONObject();
                response.put("type", "course_response");
                response.put("action", "get_course_students");
                response.put("success", false);
                response.put("message", "Failed to get course students: " + e.getMessage());
                sendJSONResponse(response);
            }
        }


        // 转换课程列表为JSON数组
        private JSONArray convertCoursesToJSON(List<Course> courses) {
            JSONArray coursesArray = new JSONArray();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            for (Course course : courses) {
                JSONObject courseObj = new JSONObject();
                courseObj.put("course_id", course.getCourseId());           // 改为下划线
                courseObj.put("course_name", course.getCourseName());       // 改为下划线
                courseObj.put("teacher_id", course.getTeacherId());         // 改为下划线
                courseObj.put("teacher_name", course.getTeacherName());     // 改为下划线
                courseObj.put("credits", course.getCredits());
                courseObj.put("max_students", course.getMaxStudents());     // 改为下划线
                courseObj.put("current_students", course.getCurrentStudents()); // 改为下划线
                courseObj.put("semester", course.getSemester());
                courseObj.put("class_time", course.getClassTime());         // 改为下划线
                courseObj.put("classroom", course.getClassroom());

                if (course.getCreatedAt() != null) {
                    courseObj.put("created_at", course.getCreatedAt().format(formatter)); // 改为下划线
                }
                if (course.getUpdatedAt() != null) {
                    courseObj.put("updated_at", course.getUpdatedAt().format(formatter)); // 改为下划线
                }

                coursesArray.add(courseObj);
            }

            return coursesArray;
        }


        // 转换学生列表为JSON数组
        private JSONArray convertStudentsToJSON(List<CourseEnrollment> students) {
            JSONArray studentsArray = new JSONArray();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            for (CourseEnrollment student : students) {
                JSONObject studentObj = new JSONObject();
                studentObj.put("enrollment_id", student.getEnrollmentId());
                studentObj.put("student_id", student.getStudentId());
                studentObj.put("course_id", student.getCourseId());
                studentObj.put("status", student.getStatus());
                studentObj.put("student_name", student.getStudentName());
                studentObj.put("credits", student.getCredits());

                if (student.getEnrollmentDate() != null) {
                    studentObj.put("enrollment_date", student.getEnrollmentDate().format(formatter));
                }

                studentsArray.add(studentObj);
            }

            return studentsArray;
        }
        private void handleLogout() {
            if (currentUsername != null) {
                onlineUsers.remove(currentUsername);
                currentUsername = null;
                sendResponse("logout_result", "Logout successful");
            }
        }

        private void handleChat(JSONObject request) {
            String message = (String) request.get("msg");
            String sender = currentUsername != null ? currentUsername : "Unknown";

            // 广播消息给所有在线用户
            JSONObject broadcast = new JSONObject();
            broadcast.put("type", "chat");
            broadcast.put("sender", sender);
            broadcast.put("msg", message);
            broadcast.put("timestamp", System.currentTimeMillis());

            for (Socket clientSocket : onlineUsers.values()) {
                try {
                    if (!clientSocket.equals(socket)) {
                        ObjectOutputStream clientOutput = new ObjectOutputStream(clientSocket.getOutputStream());
                        clientOutput.writeObject(broadcast);
                        clientOutput.flush();
                    }
                } catch (Exception e) {
                    // 移除断开连接的客户端
                    onlineUsers.values().remove(clientSocket);
                }
            }

            // 回复发送者
            sendResponse("chat_result", "Message sent");
        }
        // 发送课程响应
        private void sendCourseResponse(String action, boolean success, String message, JSONArray data) {
            try {
                JSONObject response = new JSONObject();
                response.put("type", "course_response");
                response.put("action", action);
                response.put("success", success);
                response.put("message", message);
                if (data != null) {
                    response.put("courses", data);
                }
                output.writeObject(response);
                output.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 发送JSON响应
        private void sendJSONResponse(JSONObject response) {
            try {
                output.writeObject(response);
                output.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private void sendResponse(String type, String message) {
            try {
                JSONObject response = new JSONObject();
                response.put("type", type);
                response.put("message", message);
                output.writeObject(response);
                output.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}