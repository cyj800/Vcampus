package UI;

import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserDialog extends JDialog {
    private JTextField usernameField, nicknameField, emailField, departmentField, classField;
    private JComboBox<String> roleComboBox;
    private JButton saveButton, cancelButton;
    private boolean confirmed = false;
    private User user;
    private boolean isNewUser;

    public UserDialog(Window parent, String title, boolean modal, User existingUser) {
        super(parent, title, modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
        this.user = existingUser;
        this.isNewUser = (existingUser == null);

        initializeComponents();
        setupLayout();
        setupEventListeners();

        if (existingUser != null) {
            populateFields(existingUser);
        }

        setSize(400, 350);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initializeComponents() {
        Dimension fieldSize = new Dimension(200, 25);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 12);

        usernameField = new JTextField();
        usernameField.setPreferredSize(fieldSize);
        usernameField.setFont(fieldFont);
        if (!isNewUser) {
            usernameField.setEditable(false);
            usernameField.setBackground(new Color(245, 245, 245));
        }

        nicknameField = new JTextField();
        nicknameField.setPreferredSize(fieldSize);
        nicknameField.setFont(fieldFont);

        roleComboBox = new JComboBox<>(new String[]{"管理员", "教师", "学生"});
        roleComboBox.setPreferredSize(fieldSize);
        roleComboBox.setFont(fieldFont);

        emailField = new JTextField();
        emailField.setPreferredSize(fieldSize);
        emailField.setFont(fieldFont);

        departmentField = new JTextField();
        departmentField.setPreferredSize(fieldSize);
        departmentField.setFont(fieldFont);

        classField = new JTextField();
        classField.setPreferredSize(fieldSize);
        classField.setFont(fieldFont);

        saveButton = new JButton("保存");
        saveButton.setBackground(new Color(92, 184, 92));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBorderPainted(false);
        saveButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        saveButton.setPreferredSize(new Dimension(80, 30));

        cancelButton = new JButton("取消");
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBorderPainted(false);
        cancelButton.setFont(new Font("微软雅黑", Font.BOLD, 12));
        cancelButton.setPreferredSize(new Dimension(80, 30));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("微软雅黑", Font.PLAIN, 12);

        gbc.gridx = 0; gbc.gridy = 0;
        JLabel usernameLabel = new JLabel("学号:");
        usernameLabel.setFont(labelFont);
        formPanel.add(usernameLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel nicknameLabel = new JLabel("姓名:");
        nicknameLabel.setFont(labelFont);
        formPanel.add(nicknameLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(nicknameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel roleLabel = new JLabel("职责:");
        roleLabel.setFont(labelFont);
        formPanel.add(roleLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(roleComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel emailLabel = new JLabel("邮箱:");
        emailLabel.setFont(labelFont);
        formPanel.add(emailLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel departmentLabel = new JLabel("学院:");
        departmentLabel.setFont(labelFont);
        formPanel.add(departmentLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(departmentField, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel classLabel = new JLabel("班级:");
        classLabel.setFont(labelFont);
        formPanel.add(classLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(classField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        getContentPane().setBackground(Color.WHITE);
    }

    private void setupEventListeners() {
        saveButton.addActionListener(e -> saveUser());
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
    }

    private void populateFields(User user) {
        usernameField.setText(user.getUsername());
        nicknameField.setText(user.getNickname());
        emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        departmentField.setText(user.getDepartment() != null ? user.getDepartment() : "");
        classField.setText(user.getClassName() != null ? user.getClassName() : "");

        // 设置角色
        switch (user.getRoleCode()) {
            case 0: roleComboBox.setSelectedIndex(0); break;
            case 1: roleComboBox.setSelectedIndex(1); break;
            case 2: roleComboBox.setSelectedIndex(2); break;
            default: roleComboBox.setSelectedIndex(2); break;
        }
    }

    private void saveUser() {
        if (!validateInputs()) {
            return;
        }

        if (user == null) {
            user = new User();
        }

        user.setUsername(usernameField.getText().trim());
        user.setNickname(nicknameField.getText().trim());
        user.setEmail(emailField.getText().trim().isEmpty() ? null : emailField.getText().trim());
        user.setDepartment(departmentField.getText().trim().isEmpty() ? null : departmentField.getText().trim());
        user.setClassName(classField.getText().trim().isEmpty() ? null : classField.getText().trim());

        // 设置角色代码
        switch (roleComboBox.getSelectedIndex()) {
            case 0: user.setRoleCode(0); break; // 管理员
            case 1: user.setRoleCode(1); break; // 教师
            case 2: user.setRoleCode(2); break; // 学生
            default: user.setRoleCode(2); break;
        }

        confirmed = true;
        dispose();
    }

    private boolean validateInputs() {
        if (usernameField.getText().trim().isEmpty()) {
            showError("请输入用户名");
            usernameField.requestFocus();
            return false;
        }

        if (nicknameField.getText().trim().isEmpty()) {
            showError("请输入昵称");
            nicknameField.requestFocus();
            return false;
        }

        String email = emailField.getText().trim();
        if (!email.isEmpty() && !isValidEmail(email)) {
            showError("请输入有效的邮箱地址");
            emailField.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "输入错误", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public User getUser() {
        return user;
    }
}