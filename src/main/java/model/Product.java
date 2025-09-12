package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private int id;
    private String name;                // 商品名称
    private String description;         // 商品描述
    private BigDecimal price;          // 商品价格
    private int stock;                 // 库存数量
    private String category;           // 商品分类
    private String imageUrl;           // 商品图片URL（兼容旧字段）
    private byte[] imageData;          // 商品图片二进制（新）
    private int sellerId;             // 商家ID
    private String sellerName;        // 商家名称
    private ProductStatus status;     // 商品状态
    private LocalDateTime createdAt;  // 创建时间
    private LocalDateTime updatedAt;  // 更新时间
    private int salesCount;          // 销售数量
    private double rating;           // 评分

    // 商品状态枚举
    public enum ProductStatus {
        ACTIVE("上架"),
        INACTIVE("下架"),
        OUT_OF_STOCK("缺货");

        private String description;

        ProductStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 构造函数
    public Product() {}

    public Product(String name, String description, BigDecimal price, int stock, String category, int sellerId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.sellerId = sellerId;
        this.status = ProductStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.salesCount = 0;
        this.rating = 5.0;
    }

    // Getter 和 Setter 方法
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public void setId(long id) { this.id = (int) id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) {
        this.stock = stock;
        // 自动更新商品状态
        if (stock <= 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        } else if (this.status == ProductStatus.OUT_OF_STOCK) {
            this.status = ProductStatus.ACTIVE;
        }
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public byte[] getImageData() { return imageData; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getSalesCount() { return salesCount; }
    public void setSalesCount(int salesCount) { this.salesCount = salesCount; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    // 业务方法
    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE && stock > 0;
    }

    public void increaseSalesCount(int count) {
        this.salesCount += count;
    }

    public void decreaseStock(int count) {
        if (this.stock >= count) {
            this.stock -= count;
            if (this.stock == 0) {
                this.status = ProductStatus.OUT_OF_STOCK;
            }
        }
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stock=" + stock +
                ", category='" + category + '\'' +
                ", status=" + status.getDescription() +
                ", salesCount=" + salesCount +
                ", rating=" + rating +
                '}';
    }
}