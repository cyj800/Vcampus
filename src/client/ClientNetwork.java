package client;

import org.json.simple.JSONObject;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public class ClientNetwork {
    private static Socket socket;
    private static ObjectOutputStream objectOutputStream;
    private static ObjectInputStream objectInputStream;
    private static boolean isConnected = false;
    private static ClientMessageHandler messageHandler;

    public interface LoginCallback {
        void onLoginResult(boolean success, String message, String username, String nickname);
    }

    public interface RegisterCallback {
        void onRegisterResult(boolean success, String message);
    }

    public static boolean connectToServer(String serverIP, int port) {
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
            new Thread(messageHandler).start();

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

    public static void login(String username, String password, LoginCallback callback) {
        if (!isConnected) {
            callback.onLoginResult(false, "Not connected to server", null, null);
            return;
        }

        try {
            JSONObject request = new JSONObject();
            request.put("type", "login");
            request.put("username", username);
            request.put("password", password);

            // 设置回调
            messageHandler.setLoginCallback(callback);

            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
        } catch (Exception e) {
            callback.onLoginResult(false, "Network error: " + e.getMessage(), null, null);
        }
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
}