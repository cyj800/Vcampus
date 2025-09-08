package service;

import database.MessageDAO;
import model.Message;
import model.UserRole;
import model.Course;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MessageService {
    private MessageDAO messageDAO;
    private GradeService gradeService; // 用于获取课程信息

    public MessageService() {
        this.messageDAO = new MessageDAO();
        this.gradeService = new GradeService();
    }

    // 发送个人消息
    public boolean sendPersonalMessage(String senderId, String senderName,
                                       String receiverId, String receiverName,
                                       String title, String content) {
        Message message = new Message(senderId, senderName, title, content);
        message.setReceiverType("individual");
        message.setReceiverId(receiverId);
        message.setReceiverName(receiverName);
        return messageDAO.sendMessage(message);
    }

    // 发送广播消息（所有人）
    public boolean sendBroadcastMessage(String senderId, String senderName,
                                        String title, String content) {
        Message message = new Message(senderId, senderName, title, content);
        message.setReceiverType("all");
        message.setReceiverName("所有人");
        return messageDAO.sendMessage(message);
    }

    // 发送角色消息
    public boolean sendRoleMessage(String senderId, String senderName,
                                   UserRole role, String title, String content) {
        Message message = new Message(senderId, senderName, title, content);
        message.setReceiverType(role == UserRole.TEACHER ? "teachers" : "students");
        message.setReceiverName(role == UserRole.TEACHER ? "所有教师" : "所有学生");
        return messageDAO.sendMessage(message);
    }

    // 发送课程消息
    public boolean sendCourseMessage(String senderId, String senderName,
                                     String courseId, String courseName,
                                     String title, String content) {
        Message message = new Message(senderId, senderName, title, content);
        message.setReceiverType("course");
        message.setReceiverId(courseId);
        message.setReceiverName("课程[" + courseName + "]所有学生");
        return messageDAO.sendMessage(message);
    }

    // 获取用户的所有接收消息
    public List<Message> getReceivedMessages(String userId, UserRole userRole) {
        return messageDAO.getAllReceivedMessages(userId, userRole);
    }

    // 获取用户发送的消息
    public List<Message> getSentMessages(String userId) {
        return messageDAO.getSentMessages(userId);
    }

    // 标记消息为已读
    public boolean markAsRead(int messageId) {
        return messageDAO.markAsRead(messageId);
    }

    // 获取未读消息数量
    public int getUnreadMessageCount(String userId, UserRole userRole) {
        return messageDAO.getUnreadMessageCount(userId, userRole);
    }

    // 获取教师教授的课程
    public List<Course> getTeacherCourses(String teacherId) {
        return gradeService.getCoursesByTeacher(teacherId);
    }

    // 获取所有课程（管理员用）
    public List<Course> getAllCourses() {
        return gradeService.getAllCourses();
    }

    // 获取课程的所有学生
    public List<String> getStudentsByCourse(String courseId) {
        return messageDAO.getStudentsByCourse(courseId);
    }
}