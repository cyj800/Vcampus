
import model.User;
import database.UserDAO;
import org.json.simple.JSONObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;


public class Server {
    private static UserDAO userDAO = new UserDAO();
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
                sendResponse("register_result", "Username already exists");
                return;
            }

            User user = new User(username, password, email, nickname);
            boolean success = userDAO.registerUser(user);

            if (success) {
                sendResponse("register_result", "Registration successful");
            } else {
                sendResponse("register_result", "Registration failed");
            }
        }

        private void handleLogin(JSONObject request) {
            String username = (String) request.get("username");
            String password = (String) request.get("password");

            User user = userDAO.loginUser(username, password);

            if (user != null) {
                try{
                    currentUsername = username;
                    onlineUsers.put(username, socket);
                    JSONObject response = new JSONObject();
                    response.put("type", "login_result");
                    response.put("status", "success");
                    response.put("username", user.getUsername());
                    response.put("nickname", user.getNickname());
                    response.put("message", "Login successful");
                    output.writeObject(response);
                    output.flush();
                }catch(Exception e){
                    e.printStackTrace();
                }

            } else {
                sendResponse("login_result", "Invalid username or password");
            }
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