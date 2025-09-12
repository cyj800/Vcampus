package UI;

import database.ProductDAO;
import database.CartDAO;
import model.Product;
import model.CartItem;
import model.User;
import model.UserRole;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.math.BigDecimal;

public class CampusStoreUI extends JFrame {
    private User currentUser;
    private ProductDAO productDAO;
    private CartDAO cartDAO;

    // UI组件
    private JPanel mainPanel;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel productPanel;
    private JTextField searchField;
    private JComboBox<String> categoryComboBox;
    private JLabel cartCountLabel;
    private JButton cartButton;
    private JScrollPane productScrollPane;

    // 分类数据
    private final String[] categories = {
            "全部商品", "数码产品", "文具用品", "教材书籍",
            "服装配饰", "日用品", "食品饮料"
    };

    public CampusStoreUI(User user) {
        this.currentUser = user;
        this.productDAO = new ProductDAO();
        this.cartDAO = new CartDAO();

        // 根据用户角色决定显示界面
        UserRole role = UserRole.fromUsername(user.getUsername());
        if (role == UserRole.ADMIN) {
            // 管理员显示商品管理界面
            showAdminInterface();
        } else {
            // 学生和教师显示购买界面
            showCustomerInterface();
        }
    }

    private void showAdminInterface() {
        // 关闭当前窗口并打开管理界面
        SwingUtilities.invokeLater(() -> {
            dispose();
            new ProductManagementUI(currentUser).setVisible(true);
        });
    }

    private void showCustomerInterface() {
        initializeComponents();
        setupLayout();
        addEventListeners();
        loadProducts();
        updateCartCount();

        setTitle("校园商店 - " + currentUser.getNickname());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout());
        leftPanel = new JPanel();
        rightPanel = new JPanel(new BorderLayout());
        productPanel = new JPanel(new GridLayout(0, 3, 10, 10));

        searchField = new JTextField(20);
        categoryComboBox = new JComboBox<>(categories);
        cartCountLabel = new JLabel("购物车 (0)");
        cartButton = new JButton("查看购物车");

        productScrollPane = new JScrollPane(productPanel);
        productScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        setContentPane(mainPanel);
    }

    private void setupLayout() {
        // 左侧搜索和分类面板
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createTitledBorder("商品搜索"));
        leftPanel.setPreferredSize(new Dimension(250, 0));

        // 搜索区域
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchPanel.add(new JLabel("搜索:"));
        searchPanel.add(searchField);
        JButton searchButton = new JButton("搜索");
        searchPanel.add(searchButton);

        // 分类区域
        JPanel categoryPanel = new JPanel(new FlowLayout());
        categoryPanel.add(new JLabel("分类:"));
        categoryPanel.add(categoryComboBox);

        // 购物车区域
        JPanel cartPanel = new JPanel(new FlowLayout());
        cartPanel.add(cartCountLabel);
        cartPanel.add(cartButton);

        leftPanel.add(searchPanel);
        leftPanel.add(categoryPanel);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(cartPanel);
        leftPanel.add(Box.createVerticalGlue());

        // 右侧商品展示区域
        rightPanel.setBorder(BorderFactory.createTitledBorder("商品列表"));
        rightPanel.add(productScrollPane, BorderLayout.CENTER);

        // 主面板布局
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
    }

    private void addEventListeners() {
        // 搜索按钮
        JButton searchButton = (JButton) ((JPanel) leftPanel.getComponent(0)).getComponent(2);
        searchButton.addActionListener(e -> searchProducts());

        // 搜索框回车
        searchField.addActionListener(e -> searchProducts());

        // 分类选择
        categoryComboBox.addActionListener(e -> filterByCategory());

        // 购物车按钮
        cartButton.addActionListener(e -> openCartDialog());
    }

    private void loadProducts() {
        List<Product> products = productDAO.getAllProducts();
        displayProducts(products);
    }

    private void searchProducts() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadProducts();
        } else {
            List<Product> products = productDAO.searchProducts(keyword);
            displayProducts(products);
        }
    }

    private void filterByCategory() {
        String selectedCategory = (String) categoryComboBox.getSelectedItem();
        if ("全部商品".equals(selectedCategory)) {
            loadProducts();
        } else {
            List<Product> products = productDAO.getProductsByCategory(selectedCategory);
            displayProducts(products);
        }
    }

    private void displayProducts(List<Product> products) {
        productPanel.removeAll();

        for (Product product : products) {
            JPanel productCard = createProductCard(product);
            productPanel.add(productCard);
        }

        productPanel.revalidate();
        productPanel.repaint();
    }

    private JPanel createProductCard(Product product) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createEtchedBorder());
        card.setPreferredSize(new Dimension(250, 300));

        // 商品信息面板
        JPanel infoPanel = new JPanel(new GridLayout(0, 1));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel nameLabel = new JLabel("<html><b>" + product.getName() + "</b></html>");
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel priceLabel = new JLabel("￥" + product.getPrice());
        priceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        priceLabel.setForeground(Color.RED);
        priceLabel.setFont(priceLabel.getFont().deriveFont(Font.BOLD, 16f));

        JLabel stockLabel = new JLabel("库存: " + product.getStock());
        stockLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel salesLabel = new JLabel("销量: " + product.getSalesCount());
        salesLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel categoryLabel = new JLabel("[" + product.getCategory() + "]");
        categoryLabel.setHorizontalAlignment(SwingConstants.CENTER);
        categoryLabel.setForeground(Color.GRAY);

        // 描述文本（限制长度）
        String description = product.getDescription();
        if (description != null && description.length() > 50) {
            description = description.substring(0, 47) + "...";
        }
        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);

        infoPanel.add(nameLabel);
        infoPanel.add(priceLabel);
        infoPanel.add(stockLabel);
        infoPanel.add(salesLabel);
        infoPanel.add(categoryLabel);
        infoPanel.add(descLabel);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addToCartButton = new JButton("加入购物车");
        JButton viewDetailsButton = new JButton("查看详情");

        // 检查库存
        if (product.getStock() <= 0) {
            addToCartButton.setEnabled(false);
            addToCartButton.setText("缺货");
        }

        addToCartButton.addActionListener(e -> addToCart(product));
        viewDetailsButton.addActionListener(e -> viewProductDetails(product));

        buttonPanel.add(addToCartButton);
        buttonPanel.add(viewDetailsButton);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    private void addToCart(Product product) {
        if (product.getStock() <= 0) {
            JOptionPane.showMessageDialog(this, "商品库存不足！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CartItem cartItem = new CartItem(
                currentUser.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                1
        );

        boolean success = cartDAO.addToCart(cartItem);
        if (success) {
            JOptionPane.showMessageDialog(this, "已添加到购物车！", "成功", JOptionPane.INFORMATION_MESSAGE);
            updateCartCount();
        } else {
            JOptionPane.showMessageDialog(this, "添加失败，请稍后再试！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewProductDetails(Product product) {
        ProductDetailsDialog dialog = new ProductDetailsDialog(this, product);
        dialog.setVisible(true);
    }

    private void updateCartCount() {
        List<CartItem> cartItems = cartDAO.getCartByUserId(currentUser.getId());
        int totalItems = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        cartCountLabel.setText("购物车 (" + totalItems + ")");
    }

    private void openCartDialog() {
        CartDialog dialog = new CartDialog(this, currentUser, cartDAO);
        dialog.setVisible(true);
        updateCartCount(); // 对话框关闭后更新购物车数量
    }

    // 刷新商品列表（供外部调用）
    public void refreshProducts() {
        filterByCategory();
        updateCartCount();
    }

    // 主方法用于测试
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 创建测试用户
            model.User testUser = new model.User();
            testUser.setId(2);
            testUser.setUsername("student");
            testUser.setNickname("学生");

            new CampusStoreUI(testUser).setVisible(true);
        });
    }
}
