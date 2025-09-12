package UI;

import database.ProductDAO;
import database.CartDAO;
import model.CartItem;
import model.Order;
import model.OrderItem;
import model.Product;
import model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

public class OrderDialog extends JDialog {
    private User currentUser;
    private List<CartItem> cartItems;
    private ProductDAO productDAO;
    private CartDAO cartDAO;
    private boolean orderCreated = false;

    private JTextArea orderSummaryArea;
    private JTextField deliveryAddressField;
    private JTextField phoneField;
    private JComboBox<String> paymentMethodCombo;
    private JLabel totalAmountLabel;
    private JButton createOrderButton;

    public OrderDialog(JDialog parent, User user, List<CartItem> cartItems) {
        super(parent, "创建订单", true);
        this.currentUser = user;
        this.cartItems = cartItems;
        this.productDAO = new ProductDAO();
        this.cartDAO = new CartDAO();

        initializeComponents();
        setupLayout();
        addEventListeners();
        populateOrderSummary();

        setSize(600, 700);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        orderSummaryArea = new JTextArea(10, 40);
        orderSummaryArea.setEditable(false);
        orderSummaryArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        deliveryAddressField = new JTextField(30);
        phoneField = new JTextField(15);

        String[] paymentMethods = {"校园卡支付", "支付宝", "微信支付", "银行卡"};
        paymentMethodCombo = new JComboBox<>(paymentMethods);

        totalAmountLabel = new JLabel();
        totalAmountLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        totalAmountLabel.setForeground(Color.RED);

        createOrderButton = new JButton("确认下单");
        createOrderButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        createOrderButton.setBackground(new Color(255, 165, 0));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // 标题
        JLabel titleLabel = new JLabel("确认订单信息", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        // 主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // 订单商品摘要
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(new JLabel("订单商品:"), gbc);

        gbc.gridy = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1; gbc.weighty = 0.4;
        JScrollPane summaryScrollPane = new JScrollPane(orderSummaryArea);
        summaryScrollPane.setBorder(BorderFactory.createEtchedBorder());
        mainPanel.add(summaryScrollPane, gbc);

        // 收货信息
        gbc.gridy = 2; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        mainPanel.add(new JLabel("收货地址:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        mainPanel.add(deliveryAddressField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        mainPanel.add(new JLabel("联系电话:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        mainPanel.add(phoneField, gbc);

        // 支付方式
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        mainPanel.add(new JLabel("支付方式:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1;
        mainPanel.add(paymentMethodCombo, gbc);

        // 订单金额
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(totalAmountLabel, gbc);

        // 底部按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(createOrderButton);

        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        add(titleLabel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addEventListeners() {
        createOrderButton.addActionListener(e -> createOrder());
    }

    private void populateOrderSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("商品详情:\n");
        summary.append(String.format("%-30s %8s %6s %10s\n", "商品名称", "单价", "数量", "小计"));
        summary.append("------------------------------------------------\n");

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            BigDecimal subtotal = item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            summary.append(String.format("%-30s %8.2f %6d %10.2f\n",
                    truncateString(item.getProductName(), 30),
                    item.getProductPrice().doubleValue(),
                    item.getQuantity(),
                    subtotal.doubleValue()));
            totalAmount = totalAmount.add(subtotal);
        }

        summary.append("------------------------------------------------\n");
        summary.append(String.format("共 %d 件商品", cartItems.size()));

        orderSummaryArea.setText(summary.toString());
        totalAmountLabel.setText("订单总金额: ￥" + totalAmount);
    }

    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    private void createOrder() {
        // 验证输入
        String deliveryAddress = deliveryAddressField.getText().trim();
        String phone = phoneField.getText().trim();
        String paymentMethod = (String) paymentMethodCombo.getSelectedItem();

        if (deliveryAddress.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入收货地址！", "输入错误", JOptionPane.WARNING_MESSAGE);
            deliveryAddressField.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入联系电话！", "输入错误", JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return;
        }

        // 验证手机号格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            JOptionPane.showMessageDialog(this, "请输入正确的手机号格式！", "输入错误", JOptionPane.WARNING_MESSAGE);
            phoneField.requestFocus();
            return;
        }

        // 最后确认
        int result = JOptionPane.showConfirmDialog(this,
                "确认创建订单吗？\n\n收货地址: " + deliveryAddress +
                        "\n联系电话: " + phone +
                        "\n支付方式: " + paymentMethod,
                "确认下单",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        // 禁用按钮防止重复提交
        createOrderButton.setEnabled(false);
        createOrderButton.setText("处理中...");

        // 在后台线程中处理订单创建
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return processOrder(deliveryAddress, phone, paymentMethod);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        orderCreated = true;
                        JOptionPane.showMessageDialog(OrderDialog.this,
                                "订单创建成功！\n系统将为您安排配送。",
                                "订单成功",
                                JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(OrderDialog.this,
                                "订单创建失败，请稍后再试！",
                                "订单失败",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(OrderDialog.this,
                            "订单处理出现异常：" + e.getMessage(),
                            "系统错误",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    createOrderButton.setEnabled(true);
                    createOrderButton.setText("确认下单");
                }
            }
        };

        worker.execute();
    }

    private boolean processOrder(String deliveryAddress, String phone, String paymentMethod) {
        try {
            // 计算总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (CartItem item : cartItems) {
                totalAmount = totalAmount.add(item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            // 创建订单
            Order order = new Order(
                    currentUser.getId(),
                    currentUser.getNickname(),
                    totalAmount
            );

            // 设置订单的额外信息
            order.setDeliveryAddress(deliveryAddress);
            order.setContactPhone(phone);

            // 转换支付方式字符串为枚举
            Order.PaymentMethod paymentMethodEnum;
            switch (paymentMethod) {
                case "校园卡支付":
                    paymentMethodEnum = Order.PaymentMethod.CAMPUS_CARD;
                    break;
                case "微信支付":
                    paymentMethodEnum = Order.PaymentMethod.WECHAT;
                    break;
                case "支付宝":
                    paymentMethodEnum = Order.PaymentMethod.ALIPAY;
                    break;
                case "银行卡":
                    paymentMethodEnum = Order.PaymentMethod.BANK_CARD;
                    break;
                default:
                    paymentMethodEnum = Order.PaymentMethod.CAMPUS_CARD;
            }
            order.setPaymentMethod(paymentMethodEnum);

            // 创建订单项
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem cartItem : cartItems) {
                OrderItem orderItem = new OrderItem(
                        0, // 订单ID暂时为0，创建订单后会设置
                        cartItem.getProductId(),
                        cartItem.getProductName(),
                        cartItem.getProductPrice(),
                        cartItem.getQuantity()
                );
                orderItems.add(orderItem);
            }

            // 这里应该调用OrderDAO来创建订单
            // 由于暂时没有OrderDAO，这里返回true模拟成功
            // TODO: 实现OrderDAO并调用createOrder方法

            // 模拟订单创建成功，清空购物车
            Thread.sleep(1000); // 模拟处理时间

            // 清空购物车
            boolean cartCleared = cartDAO.clearCart(currentUser.getId());

            // 减少商品库存
            for (CartItem item : cartItems) {
                Product product = productDAO.getProductById(item.getProductId());
                if (product != null) {
                    int newStock = product.getStock() - item.getQuantity();
                    int newSalesCount = product.getSalesCount() + item.getQuantity();
                    // 这里应该调用productDAO的更新方法
                    // TODO: 实现库存和销量更新
                }
            }

            return cartCleared;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isOrderCreated() {
        return orderCreated;
    }
}
