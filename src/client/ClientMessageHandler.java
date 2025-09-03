package client;

import org.json.simple.JSONObject;

import java.io.ObjectInputStream;

public class ClientMessageHandler implements Runnable {
    private ObjectInputStream input;
    private ClientNetwork.LoginCallback loginCallback;
    private ClientNetwork.RegisterCallback registerCallback;

    public ClientMessageHandler(ObjectInputStream input) {
        this.input = input;
    }

    public void setLoginCallback(ClientNetwork.LoginCallback callback) {
        this.loginCallback = callback;
    }

    public void setRegisterCallback(ClientNetwork.RegisterCallback callback) {
        this.registerCallback = callback;
    }

    @Override
    public void run() {
        try {
            while (true) {
                JSONObject message = (JSONObject) input.readObject();
                handleMessage(message);
            }
        } catch (Exception e) {
            System.out.println("Connection lost: " + e.getMessage());
        }
    }

    private void handleMessage(JSONObject message) {
        String type = (String) message.get("type");

        switch (type) {
            case "login_result":
                if (loginCallback != null) {
                    String status = (String) message.get("status");
                    boolean success = "success".equals(status);
                    String msg = (String) message.get("message");
                    String username = (String) message.get("username");
                    String nickname = (String) message.get("nickname");
                    loginCallback.onLoginResult(success, msg, username, nickname);
                }
                break;

            case "register_result":
                if (registerCallback != null) {
                    String msg = (String) message.get("message");
                    boolean success = msg.contains("successful");
                    registerCallback.onRegisterResult(success, msg);
                }
                break;

            case "chat":
                // 处理聊天消息
                String sender = (String) message.get("sender");
                String msg = (String) message.get("msg");
                System.out.println("[" + sender + "]: " + msg);
                break;

            case "error":
                String errorMsg = (String) message.get("message");
                System.out.println("Server Error: " + errorMsg);
                break;
        }
    }
}