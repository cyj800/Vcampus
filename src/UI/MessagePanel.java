package UI;

import model.Message;
import model.UserRole;
import model.Course;
import service.MessageService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessagePanel extends JPanel {
    private UserRole currentUserRole;
    private String currentUserId;
    private String currentNickname;

    private JTabbedPane tabbedPane;
    private JTable inboxTable;
    private JTable sentTable;
    private DefaultTableModel inboxModel;
    private DefaultTableModel sentModel;
    private JButton refreshButton;
    private JLabel userInfoLabel;
    private JLabel unreadLabel;

    private MessageService messageService;
    private AtomicBoolean isLoading = new AtomicBoolean(false);

    public MessagePanel(UserRole role, String userId, String nickname) {
        this.currentUserRole = role;
        this.currentUserId = userId;
        this.currentNickname = nickname;
        this.messageService = new MessageService();

        initializeComponents();
        setupLayout();
        addEventListeners();
        loadData();
    }

    private void initializeComponents() {
        // 标签页
        tabbedPane = new JTabbedPane();

        // 收件箱表格
        String[] inboxColumns = {"发件人", "标题", "时间", "状态"};
        inboxModel = new DefaultTableModel(inboxColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        inboxTable = new JTable(inboxModel);
        inboxTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        inboxTable.getTableHeader().setReorderingAllowed(false);
        inboxTable.setRowHeight(25);

        // 发件箱表格
        String[] sentColumns = {"收件人", "标题", "时间"};
        sentModel = new DefaultTableModel(sentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        sentTable = new JTable(sentModel);
        sentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sentTable.getTableHeader().setReorderingAllowed(false);
        sentTable.setRowHeight(25);

        // 按钮
        refreshButton = new JButton("刷新");
        JButton sendButton = new JButton("发送消息");
        sendButton.addActionListener(e -> showSendMessageDialog());

        // 标签
        userInfoLabel = new JLabel();
        updateUserInfoLabel();

        unreadLabel = new JLabel();
        updateUnreadCount();
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // 顶部面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(userInfoLabel);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(refreshButton);
        topPanel.add(new JButton("发送消息") {{
            addActionListener(e -> showSendMessageDialog());
        }});
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(unreadLabel);

        // 收件箱面板
        JPanel inboxPanel = new JPanel(new BorderLayout());
        JScrollPane inboxScrollPane = new JScrollPane(inboxTable);
        inboxScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        inboxPanel.add(inboxScrollPane, BorderLayout.CENTER);

        // 发件箱面板
        JPanel sentPanel = new JPanel(new BorderLayout());
        JScrollPane sentScrollPane = new JScrollPane(sentTable);
        sentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sentPanel.add(sentScrollPane, BorderLayout.CENTER);

        // 添加标签页
        tabbedPane.addTab("收件箱", inboxPanel);
        tabbedPane.addTab("发件箱", sentPanel);

        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void addEventListeners() {
        refreshButton.addActionListener(e -> loadData());

        // 收件箱双击查看消息
        inboxTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = inboxTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        showInboxMessage(selectedRow);
                    }
                }
            }
        });

        // 发件箱双击查看消息
        sentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = sentTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        showSentMessage(selectedRow);
                    }
                }
            }
        });
    }

    private void loadData() {
        if (!isLoading.compareAndSet(false, true)) {
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private List<Message> inboxMessages;
            private List<Message> sentMessages;

            @Override
            protected Void doInBackground() throws Exception {
                inboxMessages = messageService.getReceivedMessages(currentUserId, currentUserRole);
                sentMessages = messageService.getSentMessages(currentUserId);
                return null;
            }

            @Override
            protected void done() {
                try {
                    updateInboxTable(inboxMessages);
                    updateSentTable(sentMessages);
                    updateUnreadCount();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MessagePanel.this,
                            "加载消息失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                } finally {
                    isLoading.set(false);
                }
            }
        };
        worker.execute();
    }

    private void updateInboxTable(List<Message> messages) {
        SwingUtilities.invokeLater(() -> {
            inboxModel.setRowCount(0);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (Message message : messages) {
                Object[] row = {
                        message.getSenderName() + " (" + message.getSenderId() + ")",
                        message.getMessageTitle(),
                        message.getSendTime().format(formatter),
                        message.isRead() ? "已读" : "未读"
                };
                inboxModel.addRow(row);
            }
        });
    }

    private void updateSentTable(List<Message> messages) {
        SwingUtilities.invokeLater(() -> {
            sentModel.setRowCount(0);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (Message message : messages) {
                Object[] row = {
                        message.getReceiverName() != null ? message.getReceiverName() : "未知",
                        message.getMessageTitle(),
                        message.getSendTime().format(formatter)
                };
                sentModel.addRow(row);
            }
        });
    }

    private void updateUserInfoLabel() {
        String roleInfo = String.format("消息中心 - 当前用户: %s (%s)", currentNickname, currentUserRole.getRoleName());
        userInfoLabel.setText(roleInfo);
        userInfoLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        userInfoLabel.setForeground(getRoleColor(currentUserRole));
    }

    private void updateUnreadCount() {
        SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return messageService.getUnreadMessageCount(currentUserId, currentUserRole);
            }

            @Override
            protected void done() {
                try {
                    Integer count = get();
                    unreadLabel.setText("未读消息: " + count);
                    unreadLabel.setForeground(count > 0 ? Color.RED : Color.BLACK);
                    unreadLabel.setFont(new Font("微软雅黑", Font.BOLD, 12));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void showInboxMessage(int rowIndex) {
        SwingWorker<List<Message>, Void> worker = new SwingWorker<List<Message>, Void>() {
            @Override
            protected List<Message> doInBackground() throws Exception {
                return messageService.getReceivedMessages(currentUserId, currentUserRole);
            }

            @Override
            protected void done() {
                try {
                    List<Message> messages = get();
                    if (rowIndex < messages.size()) {
                        Message message = messages.get(rowIndex);

                        // 标记为已读
                        if (!message.isRead()) {
                            messageService.markAsRead(message.getMessageId());
                        }

                        showMessageDialog(message, true);

                        // 刷新界面
                        loadData();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void showSentMessage(int rowIndex) {
        SwingWorker<List<Message>, Void> worker = new SwingWorker<List<Message>, Void>() {
            @Override
            protected List<Message> doInBackground() throws Exception {
                return messageService.getSentMessages(currentUserId);
            }

            @Override
            protected void done() {
                try {
                    List<Message> messages = get();
                    if (rowIndex < messages.size()) {
                        Message message = messages.get(rowIndex);
                        showMessageDialog(message, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void showMessageDialog(Message message, boolean isInbox) {
        JDialog messageDialog = new JDialog();
        messageDialog.setTitle("查看消息");
        messageDialog.setModal(true);
        messageDialog.setSize(600, 400);
        messageDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());

        // 消息头部信息
        JPanel headerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        headerPanel.add(new JLabel("发件人:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(message.getSenderName() + " (" + message.getSenderId() + ")"), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        headerPanel.add(new JLabel("时间:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel(message.getSendTime().toString()), gbc);

        if (!isInbox) {
            gbc.gridx = 0; gbc.gridy = 2;
            headerPanel.add(new JLabel("收件人:"), gbc);
            gbc.gridx = 1;
            headerPanel.add(new JLabel(message.getReceiverName() != null ? message.getReceiverName() : "未知"), gbc);
        }

        gbc.gridx = 0; gbc.gridy = 3;
        headerPanel.add(new JLabel("标题:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(new JLabel("<html><b>" + message.getMessageTitle() + "</b></html>"), gbc);

        // 消息内容
        JTextArea contentArea = new JTextArea(message.getMessageContent());
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JScrollPane contentScrollPane = new JScrollPane(contentArea);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentScrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> messageDialog.dispose());
        buttonPanel.add(closeButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        messageDialog.add(panel);
        messageDialog.setVisible(true);
    }

    private void showSendMessageDialog() {
        JDialog sendDialog = new JDialog();
        sendDialog.setTitle("发送消息");
        sendDialog.setModal(true);
        sendDialog.setSize(500, 500);
        sendDialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 消息类型选择
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("消息类型:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> typeComboBox = new JComboBox<>();
        typeComboBox.addItem("个人消息");
        if (currentUserRole == UserRole.ADMIN || currentUserRole == UserRole.TEACHER) {
            typeComboBox.addItem("广播消息");
            typeComboBox.addItem("角色消息");
        }
        if (currentUserRole == UserRole.TEACHER) {
            typeComboBox.addItem("课程消息");
        }
        panel.add(typeComboBox, gbc);

        // 接收者面板（动态变化）
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("接收者:"), gbc);
        gbc.gridx = 1;

        JPanel receiverPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(receiverPanel, gbc);

        // 标题
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("标题:"), gbc);
        gbc.gridx = 1;
        JTextField titleField = new JTextField(20);
        panel.add(titleField, gbc);

        // 内容
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("内容:"), gbc);
        gbc.gridx = 1; gbc.gridheight = 2;
        JTextArea contentArea = new JTextArea(8, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(contentArea);
        panel.add(contentScrollPane, gbc);

        // 按钮面板
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton sendButton = new JButton("发送");
        JButton cancelButton = new JButton("取消");
        buttonPanel.add(sendButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, gbc);

        // 动态更新接收者选择面板
        typeComboBox.addActionListener(e -> updateReceiverPanel(receiverPanel, typeComboBox, sendDialog));

        // 初始化接收者面板
        updateReceiverPanel(receiverPanel, typeComboBox, sendDialog);

        // 发送按钮事件
        sendButton.addActionListener(e -> {
            String messageType = (String) typeComboBox.getSelectedItem();
            String title = titleField.getText().trim();
            String content = contentArea.getText().trim();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(sendDialog, "请输入消息标题", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (content.isEmpty()) {
                JOptionPane.showMessageDialog(sendDialog, "请输入消息内容", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            sendMessage(messageType, receiverPanel, title, content, sendDialog);
        });

        cancelButton.addActionListener(e -> sendDialog.dispose());

        sendDialog.add(panel);
        sendDialog.setVisible(true);
    }

    private void updateReceiverPanel(JPanel receiverPanel, JComboBox<String> typeComboBox, JDialog parentDialog) {
        receiverPanel.removeAll();

        String selectedType = (String) typeComboBox.getSelectedItem();

        switch (selectedType) {
            case "个人消息":
                // 个人消息 - 选择用户
                JTextField userField = new JTextField(15);
                receiverPanel.add(new JLabel("用户名:"));
                receiverPanel.add(userField);
                break;

            case "广播消息":
                // 广播消息 - 发送给所有人
                receiverPanel.add(new JLabel("发送给所有用户"));
                break;

            case "角色消息":
                // 角色消息 - 选择角色
                JComboBox<String> roleComboBox = new JComboBox<>();
                roleComboBox.addItem("教师");
                roleComboBox.addItem("学生");
                receiverPanel.add(roleComboBox);
                break;

            case "课程消息":
                // 课程消息 - 选择课程
                JComboBox<Course> courseComboBox = new JComboBox<>();
                courseComboBox.setPreferredSize(new Dimension(150, 25));

                // 异步加载课程数据
                SwingWorker<List<Course>, Void> courseWorker = new SwingWorker<List<Course>, Void>() {
                    @Override
                    protected List<Course> doInBackground() throws Exception {
                        if (currentUserRole == UserRole.TEACHER) {
                            return messageService.getTeacherCourses(currentUserId);
                        } else {
                            return messageService.getAllCourses();
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            List<Course> courses = get();
                            courseComboBox.removeAllItems();
                            for (Course course : courses) {
                                courseComboBox.addItem(course);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                courseWorker.execute();

                receiverPanel.add(courseComboBox);
                break;
        }

        receiverPanel.revalidate();
        receiverPanel.repaint();
    }

    private void sendMessage(String messageType, JPanel receiverPanel, String title, String content, JDialog dialog) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                switch (messageType) {
                    case "个人消息":
                        JTextField userField = (JTextField) receiverPanel.getComponent(1);
                        String receiverId = userField.getText().trim();
                        // 这里应该验证用户是否存在
                        return messageService.sendPersonalMessage(currentUserId, currentNickname,
                                receiverId, receiverId, title, content);

                    case "广播消息":
                        return messageService.sendBroadcastMessage(currentUserId, currentNickname, title, content);

                    case "角色消息":
                        JComboBox<String> roleComboBox = (JComboBox<String>) receiverPanel.getComponent(0);
                        String role = (String) roleComboBox.getSelectedItem();
                        UserRole userRole = "教师".equals(role) ? UserRole.TEACHER : UserRole.STUDENT;
                        return messageService.sendRoleMessage(currentUserId, currentNickname, userRole, title, content);

                    case "课程消息":
                        JComboBox<Course> courseComboBox = (JComboBox<Course>) receiverPanel.getComponent(0);
                        Course selectedCourse = (Course) courseComboBox.getSelectedItem();
                        if (selectedCourse != null) {
                            return messageService.sendCourseMessage(currentUserId, currentNickname,
                                    selectedCourse.getCourseId(), selectedCourse.getCourseName(),
                                    title, content);
                        }
                        return false;
                }
                return false;
            }

            @Override
            protected void done() {
                try {
                    Boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(dialog, "消息发送成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        loadData(); // 刷新消息列表
                    } else {
                        JOptionPane.showMessageDialog(dialog, "消息发送失败", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "消息发送失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private Color getRoleColor(UserRole role) {
        switch (role) {
            case ADMIN:
                return new Color(255, 100, 100);
            case TEACHER:
                return new Color(100, 150, 255);
            case STUDENT:
                return new Color(100, 200, 100);
            default:
                return Color.BLACK;
        }
    }
}