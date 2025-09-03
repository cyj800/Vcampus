package UI;

import client.ClientNetwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Login extends JFrame {
    private JPanel rootpanel;
    private JButton loginButton;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel VcampusLabel;
    private JButton registerButton;
    private JLabel statusLabel;
    private JButton connectButton;

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8888;

    public Login() {
        initializeComponents();
        setupLayout();
        addEventListeners();

        setTitle("校园平台 - 登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        // 添加窗口关闭监听器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ClientNetwork.disconnect();
                System.exit(0);
            }
        });
    }

    private void initializeComponents() {
        rootpanel = new JPanel();
        loginButton = new JButton("登录");
        usernameField = new JTextField(15);
        passwordField = new JPasswordField(15);
        VcampusLabel = new JLabel("校园平台登录");
        registerButton = new JButton("注册");
        statusLabel = new JLabel("请连接服务器");
        connectButton = new JButton("连接服务器");

        // 设置标题样式
        VcampusLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        VcampusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // 设置初始状态
        statusLabel.setForeground(Color.RED);
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);
    }

    private void setupLayout() {
        rootpanel.setLayout(new BorderLayout());
        setContentPane(rootpanel);

        // 北部 - 标题
        rootpanel.add(VcampusLabel, BorderLayout.NORTH);

        // 中部 - 输入区域
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // 用户名行
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(new JLabel("用户名:"), gbc);
        gbc.gridx = 1;
        centerPanel.add(usernameField, gbc);

        // 密码行
        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(new JLabel("密码:"), gbc);
        gbc.gridx = 1;
        centerPanel.add(passwordField, gbc);

        // 状态行
        gbc.gridx = 0; gbc.gridy = 2;
        centerPanel.add(new JLabel("状态:"), gbc);
        gbc.gridx = 1;
        centerPanel.add(statusLabel, gbc);

        rootpanel.add(centerPanel, BorderLayout.CENTER);

        // 南部 - 按钮区域
        JPanel southPanel = new JPanel(new FlowLayout());
        southPanel.add(connectButton);
        southPanel.add(loginButton);
        southPanel.add(registerButton);
        rootpanel.add(southPanel, BorderLayout.SOUTH);
    }

    private void addEventListeners() {
        // 连接服务器按钮
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });

        // 登录按钮
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        // 注册按钮
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openRegisterDialog();
            }
        });

        // 密码框回车事件
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (loginButton.isEnabled()) {
                    performLogin();
                }
            }
        });
    }

    private void connectToServer() {
        statusLabel.setText("正在连接服务器...");
        statusLabel.setForeground(Color.BLUE);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return ClientNetwork.connectToServer(SERVER_IP, SERVER_PORT);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        statusLabel.setText("服务器连接成功");
                        statusLabel.setForeground(Color.GREEN);
                        loginButton.setEnabled(true);
                        registerButton.setEnabled(true);
                        connectButton.setEnabled(false);
                    } else {
                        statusLabel.setText("服务器连接失败");
                        statusLabel.setForeground(Color.RED);
                    }
                } catch (Exception e) {
                    statusLabel.setText("连接错误: " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                }
            }
        }.execute();
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入用户名和密码", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        statusLabel.setText("正在登录...");
        statusLabel.setForeground(Color.BLUE);
        loginButton.setEnabled(false);

        ClientNetwork.login(username, password, new ClientNetwork.LoginCallback() {
            @Override
            public void onLoginResult(boolean success, String message, String username, String nickname) {
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        statusLabel.setText("登录成功");
                        statusLabel.setForeground(Color.GREEN);
                        JOptionPane.showMessageDialog(Login.this, "欢迎 " + nickname + "!", "登录成功", JOptionPane.INFORMATION_MESSAGE);

                        // 打开主界面
                        openMainInterface(username, nickname);
                    } else {
                        statusLabel.setText("登录失败");
                        statusLabel.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(Login.this, message, "登录失败", JOptionPane.ERROR_MESSAGE);
                        loginButton.setEnabled(true);
                    }
                });
            }
        });
    }

    private void openRegisterDialog() {
        RegisterDialog registerDialog = new RegisterDialog(this);
        registerDialog.setVisible(true);
    }

    private void openMainInterface(String username, String nickname) {
        // 隐藏登录窗口
        this.dispose();

        // 打开主界面
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainInterface(username, nickname).setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new Login().setVisible(true);
            }
        });
    }
}