package UI;

import client.ClientNetwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JTextField nicknameField;
    private JButton registerButton;
    private JButton cancelButton;
    private JLabel statusLabel;

    public RegisterDialog(Frame parent) {
        super(parent, "用户注册", true);
        initializeComponents();
        addEventListeners();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // 创建输入面板
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // 用户名
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("用户名:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        inputPanel.add(usernameField, gbc);

        // 密码
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("密码:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        inputPanel.add(passwordField, gbc);

        // 邮箱
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("邮箱:"), gbc);
        gbc.gridx = 1;
        emailField = new JTextField(15);
        inputPanel.add(emailField, gbc);

        // 昵称
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("昵称:"), gbc);
        gbc.gridx = 1;
        nicknameField = new JTextField(15);
        inputPanel.add(nicknameField, gbc);

        // 状态标签
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        statusLabel = new JLabel(" ");
        inputPanel.add(statusLabel, gbc);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        registerButton = new JButton("注册");
        cancelButton = new JButton("取消");
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addEventListeners() {
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performRegister();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        // 密码框回车注册
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performRegister();
            }
        });
    }

    private void performRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String email = emailField.getText().trim();
        String nickname = nicknameField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || nickname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写所有字段", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        statusLabel.setText("正在注册...");
        registerButton.setEnabled(false);

        ClientNetwork.register(username, password, email, nickname, new ClientNetwork.RegisterCallback() {
            @Override
            public void onRegisterResult(boolean success, String message) {
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        statusLabel.setText("注册成功");
                        statusLabel.setForeground(Color.GREEN);
                        JOptionPane.showMessageDialog(RegisterDialog.this, message, "注册成功", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        statusLabel.setText("注册失败");
                        statusLabel.setForeground(Color.RED);
                        JOptionPane.showMessageDialog(RegisterDialog.this, message, "注册失败", JOptionPane.ERROR_MESSAGE);
                        registerButton.setEnabled(true);
                    }
                });
            }
        });
    }
}