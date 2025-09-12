package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CartItem {
    private int id;
    private int userId;              // 用户ID
    private int productId;           // 商品ID
    private String productName;      // 商品名称
    private BigDecimal productPrice; // 商品价格
    private int quantity;            // 数量
    private LocalDateTime addedAt;   // 添加时间
    private boolean selected;        // 是否选中结算

    // 构造函数
    public CartItem() {}

    public CartItem(int userId, int productId, String productName, BigDecimal productPrice, int quantity) {
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.addedAt = LocalDateTime.now();
        this.selected = true;
    }

    // Getter 和 Setter 方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public BigDecimal getProductPrice() { return productPrice; }
    public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    // 业务方法
    public BigDecimal getTotalPrice() {
        return productPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        if (this.quantity > amount) {
            this.quantity -= amount;
        } else {
            this.quantity = 1; // 最少保持1个
        }
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + productPrice +
                ", totalPrice=" + getTotalPrice() +
                ", selected=" + selected +
                '}';
    }
}
