package database;

import database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Product;
import model.Product.ProductStatus;

public class ProductDAO {

    // 添加商品
    public boolean addProduct(Product product) {
        ensureImageDataColumn();
        String sql = "INSERT INTO products (name, description, price, stock, category, image_url, image_data, seller_id, seller_name, status, sales_count, rating) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setString(5, product.getCategory());
            stmt.setString(6, product.getImageUrl() != null ? product.getImageUrl() : "");
                if (product.getImageData() != null) {
                stmt.setBytes(7, product.getImageData());
            } else {
                stmt.setNull(7, java.sql.Types.BLOB);
            }
            // 使用传入的商家信息（若未提供则回退为管理员默认）
            int sellerId = product.getSellerId() != 0 ? product.getSellerId() : 1;
            String sellerName = (product.getSellerName() != null && !product.getSellerName().isEmpty()) ? product.getSellerName() : "系统管理员";
            stmt.setInt(8, sellerId);
            stmt.setString(9, sellerName);
            stmt.setString(10, ProductStatus.ACTIVE.name()); // 默认状态为上架
            stmt.setInt(11, 0); // 默认销量为0
            stmt.setDouble(12, 0.0); // 默认评分为0

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    product.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 获取所有商品
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE status = 'ACTIVE' ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    // 按分类获取商品
    public List<Product> getProductsByCategory(String category) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category = ? AND status = 'ACTIVE' ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    // 搜索商品
    public List<Product> searchProducts(String keyword) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE (name LIKE ? OR description LIKE ?) AND status = 'ACTIVE' ORDER BY sales_count DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    // 根据ID获取商品
    public Product getProductById(long id) {
        String sql = "SELECT * FROM products WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 更新商品信息
    public boolean updateProduct(Product product) {
        ensureImageDataColumn();
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, stock = ?, category = ?, image_url = ?, image_data = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getStock());
            stmt.setString(5, product.getCategory());
            stmt.setString(6, product.getImageUrl());
            if (product.getImageData() != null) {
                stmt.setBytes(7, product.getImageData());
            } else {
                stmt.setNull(7, java.sql.Types.BLOB);
            }
            stmt.setLong(8, product.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 删除商品（软删除）
    public boolean deleteProduct(long productId) {
        String sql = "UPDATE products SET status = 'INACTIVE', updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 删除商品（重载方法）
    public boolean deleteProduct(int productId) {
        return deleteProduct((long) productId);
    }

    // 获取所有商品（包括非活动状态的，供管理员使用）
    public List<Product> getAllProductsForAdmin() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY created_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    // 根据ID获取商品
    public Product getProductById(int id) {
        return getProductById((long) id);
    }

    // 更新库存
    public boolean updateStock(int productId, int newStock) {
        String sql = "UPDATE products SET stock = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newStock);
            stmt.setInt(2, productId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 增加销量
    public boolean increaseSalesCount(int productId, int count) {
        String sql = "UPDATE products SET sales_count = sales_count + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, count);
            stmt.setInt(2, productId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 获取热门商品
    public List<Product> getPopularProducts(int limit) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE status = 'ACTIVE' ORDER BY sales_count DESC LIMIT ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    // 获取所有商品分类
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM products WHERE status = 'ACTIVE' ORDER BY category";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    // 设置商品状态（上架/下架）
    public boolean setProductStatus(int productId, ProductStatus status) {
        String sql = "UPDATE products SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 物理删除（不可恢复）
    public boolean hardDeleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 结果集映射到商品对象
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStock(rs.getInt("stock"));
        product.setCategory(rs.getString("category"));
        product.setImageUrl(rs.getString("image_url"));
        try { product.setImageData(rs.getBytes("image_data")); } catch (SQLException ignore) {}
        product.setSellerId(rs.getInt("seller_id"));
        product.setSellerName(rs.getString("seller_name"));
        product.setStatus(ProductStatus.valueOf(rs.getString("status")));
        product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        product.setSalesCount(rs.getInt("sales_count"));
        product.setRating(rs.getDouble("rating"));
        return product;
    }

    // 确保 image_data 列存在（LONGBLOB），若不存在则自动添加
    private void ensureImageDataColumn() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, "products", "image_data")) {
                if (!rs.next()) {
                    try (Statement st = conn.createStatement()) {
                        st.executeUpdate("ALTER TABLE products ADD COLUMN image_data LONGBLOB NULL AFTER image_url");
                    }
                }
            }
        } catch (SQLException e) {
            // 不能因迁移失败中断主要流程，打印日志即可
            e.printStackTrace();
        }
    }
}
