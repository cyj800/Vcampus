package UI;

import database.CartDAO;
import database.ProductDAO;
import model.CartItem;
import model.Product;
import model.User;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;

public class CartDialog extends JDialog {
    private User currentUser;
    private CartDAO cartDAO;
    private ProductDAO productDAO;
    private List<CartItem> cartItems;

    private JTable cartTable;
    private CartTableModel tableModel;
    private JLabel totalLabel;
    private JButton checkoutButton;
    private JButton clearCartButton;

    public CartDialog(JFrame parent, User user, CartDAO cartDAO) {
        super(parent, "购物车", true);
        this.currentUser = user;
        this.cartDAO = cartDAO;
        this.productDAO = new ProductDAO();

        loadCartItems();
        initializeComponents();
        setupLayout();
        addEventListeners();
        updateTotal();

        setSize(800, 600);
        setLocationRelativeTo(parent);
    }

    private void loadCartItems() {
        cartItems = cartDAO.getCartByUserId(currentUser.getId());
    }

    private void initializeComponents() {
        tableModel = new CartTableModel();
        cartTable = new JTable(tableModel);

        // 设置表格样式
        cartTable.setRowHeight(30);
        cartTable.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        // 设置列宽
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(200); // 商品名称
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(100); // 单价
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // 数量
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(100); // 小计
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // 操作

        // 价格列右对齐
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        cartTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        cartTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

        // 数量列居中
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        cartTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

        totalLabel = new JLabel("总计: ￥0.00");
        totalLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        totalLabel.setForeground(Color.RED);

        checkoutButton = new JButton("结算");
        checkoutButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        clearCartButton = new JButton("清空购物车");

        // 如果购物车为空，禁用按钮
        if (cartItems.isEmpty()) {
            checkoutButton.setEnabled(false);
            clearCartButton.setEnabled(false);
        }
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // 顶部标题
        JLabel titleLabel = new JLabel("我的购物车", SwingConstants.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // 表格面板
        JScrollPane scrollPane = new JScrollPane(cartTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("购物车商品"));

        // 底部面板
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // 总计面板
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.add(totalLabel);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(clearCartButton);
        buttonPanel.add(checkoutButton);

        JButton closeButton = new JButton("关闭");
        buttonPanel.add(closeButton);

        bottomPanel.add(totalPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addEventListeners() {
        // 结算按钮
        checkoutButton.addActionListener(e -> checkout());

        // 清空购物车按钮
        clearCartButton.addActionListener(e -> clearCart());

        // 关闭按钮
        JPanel buttonPanel = (JPanel) ((JPanel) ((BorderLayout) getContentPane().getLayout())
                .getLayoutComponent(BorderLayout.SOUTH)).getComponent(1);
        JButton closeButton = (JButton) buttonPanel.getComponent(2);
        closeButton.addActionListener(e -> dispose());
    }

    private void updateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            total = total.add(item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        totalLabel.setText("总计: ￥" + total);
    }

    private void checkout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "购物车为空！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 检查库存（仅商城商品：productId>0；二手商品用负ID标识不校验库存）
        for (CartItem item : cartItems) {
            if (item.getProductId() > 0) {
                Product product = productDAO.getProductById(item.getProductId());
                if (product == null || product.getStock() < item.getQuantity()) {
                    JOptionPane.showMessageDialog(this,
                            "商品 " + item.getProductName() + " 库存不足！",
                            "库存不足", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }

        // 创建订单对话框
        OrderDialog orderDialog = new OrderDialog(this, currentUser, cartItems);
        orderDialog.setVisible(true);

        // 如果订单创建成功，刷新购物车
        if (orderDialog.isOrderCreated()) {
            loadCartItems();
            tableModel.fireTableDataChanged();
            updateTotal();

            if (cartItems.isEmpty()) {
                checkoutButton.setEnabled(false);
                clearCartButton.setEnabled(false);
            }
        }
    }

    private void clearCart() {
        int result = JOptionPane.showConfirmDialog(this,
                "确定要清空购物车吗？", "确认",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            boolean success = cartDAO.clearCart(currentUser.getId());
            if (success) {
                cartItems.clear();
                tableModel.fireTableDataChanged();
                updateTotal();
                checkoutButton.setEnabled(false);
                clearCartButton.setEnabled(false);
                JOptionPane.showMessageDialog(this, "购物车已清空！", "成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "清空失败，请稍后再试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeItem(int rowIndex) {
        CartItem item = cartItems.get(rowIndex);
        boolean success = cartDAO.removeFromCart(item.getId());
        if (success) {
            cartItems.remove(rowIndex);
            tableModel.fireTableRowsDeleted(rowIndex, rowIndex);
            updateTotal();

            if (cartItems.isEmpty()) {
                checkoutButton.setEnabled(false);
                clearCartButton.setEnabled(false);
            }
        } else {
            JOptionPane.showMessageDialog(this, "删除失败，请稍后再试！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateQuantity(int rowIndex, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(rowIndex);
            return;
        }

        CartItem item = cartItems.get(rowIndex);

        // 检查库存（商城商品才检查）
        if (item.getProductId() > 0) {
            Product product = productDAO.getProductById(item.getProductId());
            if (product == null || product.getStock() < newQuantity) {
                JOptionPane.showMessageDialog(this, "库存不足！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        boolean success = cartDAO.updateQuantity(item.getId(), newQuantity);
        if (success) {
            item.setQuantity(newQuantity);
            tableModel.fireTableCellUpdated(rowIndex, 2);
            tableModel.fireTableCellUpdated(rowIndex, 3);
            updateTotal();
        } else {
            JOptionPane.showMessageDialog(this, "更新失败，请稍后再试！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 购物车表格模型
    private class CartTableModel extends AbstractTableModel {
        private final String[] columnNames = {"商品名称", "单价", "数量", "小计", "操作"};

        @Override
        public int getRowCount() {
            return cartItems.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            CartItem item = cartItems.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return item.getProductName();
                case 1:
                    return "￥" + item.getProductPrice();
                case 2:
                    return item.getQuantity();
                case 3:
                    BigDecimal subtotal = item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return "￥" + subtotal;
                case 4:
                    return "操作";
                default:
                    return "";
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 2 || columnIndex == 4; // 数量和操作列可编辑
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (columnIndex == 2) {
                try {
                    int newQuantity = Integer.parseInt(value.toString());
                    updateQuantity(rowIndex, newQuantity);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(CartDialog.this, "请输入有效的数量！", "输入错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
