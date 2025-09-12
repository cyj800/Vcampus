package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    private int id;
    private String orderNumber;        // 订单号
    private int userId;               // 用户ID
    private String userName;          // 用户名
    private BigDecimal totalAmount;   // 订单总金额
    private OrderStatus status;       // 订单状态
    private PaymentMethod paymentMethod; // 支付方式
    private PaymentStatus paymentStatus; // 支付状态
    private DeliveryMethod deliveryMethod; // 收货方式
    private String deliveryAddress;   // 收货地址/自提点
    private String contactPhone;     // 联系电话
    private String remarks;          // 订单备注
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
    private LocalDateTime paymentTime; // 支付时间
    private LocalDateTime deliveryTime; // 发货/自提时间
    private LocalDateTime completedTime; // 完成时间

    // 订单状态枚举
    public enum OrderStatus {
        PENDING_PAYMENT("待付款"),
        PENDING_DELIVERY("待发货"),
        PENDING_PICKUP("待自提"),
        DELIVERED("已发货"),
        COMPLETED("已完成"),
        CANCELLED("已取消"),
        REFUNDED("已退款");

        private String description;

        OrderStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 支付方式枚举
    public enum PaymentMethod {
        CAMPUS_CARD("校园卡"),
        WECHAT("微信支付"),
        ALIPAY("支付宝"),
        BANK_CARD("银行卡");

        private String description;

        PaymentMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 支付状态枚举
    public enum PaymentStatus {
        UNPAID("未支付"),
        PAID("已支付"),
        REFUNDING("退款中"),
        REFUNDED("已退款");

        private String description;

        PaymentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 收货方式枚举
    public enum DeliveryMethod {
        PICKUP("自提"),
        DELIVERY("配送上门");

        private String description;

        DeliveryMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 构造函数
    public Order() {}

    public Order(int userId, String userName, BigDecimal totalAmount) {
        this.userId = userId;
        this.userName = userName;
        this.totalAmount = totalAmount;
        this.orderNumber = generateOrderNumber();
        this.status = OrderStatus.PENDING_PAYMENT;
        this.paymentStatus = PaymentStatus.UNPAID;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getter 和 Setter 方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public DeliveryMethod getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(DeliveryMethod deliveryMethod) { this.deliveryMethod = deliveryMethod; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }

    public LocalDateTime getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(LocalDateTime deliveryTime) { this.deliveryTime = deliveryTime; }

    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }

    // 业务方法
    private String generateOrderNumber() {
        return "OR" + System.currentTimeMillis();
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING_PAYMENT || status == OrderStatus.PENDING_DELIVERY;
    }

    public boolean canBePaid() {
        return status == OrderStatus.PENDING_PAYMENT && paymentStatus == PaymentStatus.UNPAID;
    }

    public void markAsPaid(PaymentMethod method) {
        this.paymentMethod = method;
        this.paymentStatus = PaymentStatus.PAID;
        this.paymentTime = LocalDateTime.now();

        if (deliveryMethod == DeliveryMethod.PICKUP) {
            this.status = OrderStatus.PENDING_PICKUP;
        } else {
            this.status = OrderStatus.PENDING_DELIVERY;
        }
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderNumber='" + orderNumber + '\'' +
                ", userName='" + userName + '\'' +
                ", totalAmount=" + totalAmount +
                ", status=" + status.getDescription() +
                ", paymentStatus=" + paymentStatus.getDescription() +
                ", createdAt=" + createdAt +
                '}';
    }
}