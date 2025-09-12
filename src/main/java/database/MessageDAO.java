package database;

import model.Message;
import model.UserRole;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    // 发送消息
    public boolean sendMessage(Message message) {
        String sql = "INSERT INTO messages (sender_id, sender_name, receiver_type, receiver_id, receiver_name, " +
                "message_title, message_content, message_type, is_read, send_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, message.getSenderId());
            stmt.setString(2, message.getSenderName());
            stmt.setString(3, message.getReceiverType());
            stmt.setString(4, message.getReceiverId());
            stmt.setString(5, message.getReceiverName());
            stmt.setString(6, message.getMessageTitle());
            stmt.setString(7, message.getMessageContent());
            stmt.setString(8, message.getMessageType());
            stmt.setBoolean(9, message.isRead());
            stmt.setTimestamp(10, Timestamp.valueOf(message.getSendTime()));

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    message.setMessageId(generatedKeys.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 获取用户的个人消息
    public List<Message> getPersonalMessages(String userId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE receiver_id = ? AND receiver_type = 'individual' " +
                "ORDER BY send_time DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }

    // 获取发送给所有人的消息
    public List<Message> getBroadcastMessages() {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE receiver_type = 'all' ORDER BY send_time DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }

    // 获取发送给特定角色的消息
    public List<Message> getRoleMessages(String userId, UserRole userRole) {
        List<Message> messages = new ArrayList<>();
        String roleCondition = "";

        switch (userRole) {
            case TEACHER:
                roleCondition = "receiver_type = 'teachers'";
                break;
            case STUDENT:
                roleCondition = "receiver_type = 'students'";
                break;
            default:
                return messages;
        }

        String sql = "SELECT * FROM messages WHERE " + roleCondition + " ORDER BY send_time DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }

    // 获取发送给特定课程的消息
    public List<Message> getCourseMessages(String courseId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE receiver_type = 'course' AND receiver_id = ? " +
                "ORDER BY send_time DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }

    // 获取用户发送的消息
    public List<Message> getSentMessages(String userId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE sender_id = ? ORDER BY send_time DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messages;
    }

    // 获取用户的所有相关消息（接收的）
    public List<Message> getAllReceivedMessages(String userId, UserRole userRole) {
        List<Message> messages = new ArrayList<>();

        // 个人消息
        messages.addAll(getPersonalMessages(userId));

        // 广播消息
        messages.addAll(getBroadcastMessages());

        // 角色消息
        messages.addAll(getRoleMessages(userId, userRole));

        // 按时间排序
        messages.sort((m1, m2) -> m2.getSendTime().compareTo(m1.getSendTime()));

        return messages;
    }

    // 标记消息为已读
    public boolean markAsRead(int messageId) {
        String sql = "UPDATE messages SET is_read = TRUE, read_time = ? WHERE message_id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, messageId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 获取未读消息数量
    public int getUnreadMessageCount(String userId, UserRole userRole) {
        int count = 0;
        String sql = "SELECT COUNT(*) FROM messages WHERE is_read = FALSE AND (" +
                "receiver_id = ? OR " +
                "receiver_type = 'all' OR " +
                "receiver_type = ?" +
                ")";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setString(2, userRole == UserRole.TEACHER ? "teachers" : "students");

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return count;
    }

    // 获取特定课程的所有学生
    public List<String> getStudentsByCourse(String courseId) {
        List<String> students = new ArrayList<>();
        String sql = "SELECT student_id FROM enrollments WHERE course_id = ? AND status = 'active'";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, courseId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                students.add(rs.getString("student_id"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;
    }

    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setMessageId(rs.getInt("message_id"));
        message.setSenderId(rs.getString("sender_id"));
        message.setSenderName(rs.getString("sender_name"));
        message.setReceiverType(rs.getString("receiver_type"));
        message.setReceiverId(rs.getString("receiver_id"));
        message.setReceiverName(rs.getString("receiver_name"));
        message.setMessageTitle(rs.getString("message_title"));
        message.setMessageContent(rs.getString("message_content"));
        message.setMessageType(rs.getString("message_type"));
        message.setRead(rs.getBoolean("is_read"));
        message.setSendTime(rs.getTimestamp("send_time").toLocalDateTime());
        Timestamp readTime = rs.getTimestamp("read_time");
        if (readTime != null) {
            message.setReadTime(readTime.toLocalDateTime());
        }
        return message;
    }
}