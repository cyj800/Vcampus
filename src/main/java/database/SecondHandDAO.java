package database;

import database.DatabaseManager;
import model.SecondHandItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SecondHandDAO {
    public SecondHandDAO() {
        ensureTable();
    }

    public boolean addItem(SecondHandItem item) {
        ensureImageDataColumn();
        String sql = "INSERT INTO secondhand_items (name, description, price, category, image_url, image_data, seller_id, seller_name, status) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setBigDecimal(3, item.getPrice());
            ps.setString(4, item.getCategory());
            ps.setString(5, item.getImageUrl());
            if (item.getImageData()!=null) ps.setBytes(6, item.getImageData()); else ps.setNull(6, Types.BLOB);
            ps.setInt(7, item.getSellerId());
            ps.setString(8, item.getSellerName());
            ps.setString(9, item.getStatus()!=null? item.getStatus().name(): SecondHandItem.Status.ACTIVE.name());
            int n = ps.executeUpdate();
            if (n>0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) item.setId(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public SecondHandItem getById(int id) {
        String sql = "SELECT * FROM secondhand_items WHERE id=?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean updateItem(SecondHandItem item) {
        String sql = "UPDATE secondhand_items SET name=?, description=?, price=?, category=?, image_url=?, image_data=?, status=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setBigDecimal(3, item.getPrice());
            ps.setString(4, item.getCategory());
            ps.setString(5, item.getImageUrl());
            if (item.getImageData()!=null) ps.setBytes(6, item.getImageData()); else ps.setNull(6, Types.BLOB);
            ps.setString(7, item.getStatus()!=null? item.getStatus().name(): SecondHandItem.Status.ACTIVE.name());
            ps.setInt(8, item.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteItem(int id) {
        String sql = "DELETE FROM secondhand_items WHERE id=?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean setStatus(int id, SecondHandItem.Status status) {
        String sql = "UPDATE secondhand_items SET status=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<SecondHandItem> listActive() {
        List<SecondHandItem> list = new ArrayList<>();
        String sql = "SELECT * FROM secondhand_items WHERE status='ACTIVE' ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<SecondHandItem> listActiveByCategory(String category) {
        List<SecondHandItem> list = new ArrayList<>();
        String sql = "SELECT * FROM secondhand_items WHERE status='ACTIVE' AND category=? ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<SecondHandItem> searchActive(String keyword) {
        List<SecondHandItem> list = new ArrayList<>();
        String sql = "SELECT * FROM secondhand_items WHERE status='ACTIVE' AND (name LIKE ? OR description LIKE ?) ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<SecondHandItem> listBySeller(int sellerId) {
        List<SecondHandItem> list = new ArrayList<>();
        String sql = "SELECT * FROM secondhand_items WHERE seller_id=? ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<SecondHandItem> listAllForAdmin() {
        List<SecondHandItem> list = new ArrayList<>();
        String sql = "SELECT * FROM secondhand_items ORDER BY created_at DESC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private SecondHandItem map(ResultSet rs) throws SQLException {
        SecondHandItem i = new SecondHandItem();
        i.setId(rs.getInt("id"));
        i.setName(rs.getString("name"));
        i.setDescription(rs.getString("description"));
        i.setPrice(rs.getBigDecimal("price"));
        i.setCategory(rs.getString("category"));
        try { i.setImageData(rs.getBytes("image_data")); } catch (SQLException ignore) {}
        i.setImageUrl(rs.getString("image_url"));
        i.setSellerId(rs.getInt("seller_id"));
        i.setSellerName(rs.getString("seller_name"));
        try { i.setStatus(SecondHandItem.Status.valueOf(rs.getString("status"))); } catch (Exception e) { i.setStatus(SecondHandItem.Status.ACTIVE); }
        try { Timestamp c = rs.getTimestamp("created_at"); if (c!=null) i.setCreatedAt(c.toLocalDateTime()); } catch (SQLException ignore) {}
        try { Timestamp u = rs.getTimestamp("updated_at"); if (u!=null) i.setUpdatedAt(u.toLocalDateTime()); } catch (SQLException ignore) {}
        return i;
    }

    private void ensureImageDataColumn() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getColumns(null, null, "secondhand_items", "image_data")) {
                if (!rs.next()) {
                    try (Statement st = conn.createStatement()) {
                        st.executeUpdate("ALTER TABLE secondhand_items ADD COLUMN image_data LONGBLOB NULL AFTER image_url");
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void ensureTable() {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS secondhand_items (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY," +
                            "name VARCHAR(200) NOT NULL," +
                            "description TEXT," +
                            "price DECIMAL(10,2) NOT NULL," +
                            "category VARCHAR(50) NOT NULL," +
                            "image_url VARCHAR(500)," +
                            "image_data LONGBLOB NULL," +
                            "seller_id INT NOT NULL," +
                            "seller_name VARCHAR(100) NOT NULL," +
                            "status ENUM('ACTIVE','INACTIVE','SOLD') DEFAULT 'ACTIVE'," +
                            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                            "INDEX idx_category (category)," +
                            "INDEX idx_seller (seller_id)," +
                            "INDEX idx_status (status)" +
                            ") ENGINE=InnoDB COMMENT='二手市场表'"
            );
        } catch (SQLException e) { e.printStackTrace(); }
    }
}

