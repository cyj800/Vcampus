package UI;

import database.ProductDAO;
import database.CartDAO;
import model.Product;
import model.CartItem;
//import CartDialog;
//import ProductDetailsDialog;
import model.User;
import model.UserRole;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.math.BigDecimal;

public class StoreContentPanel extends JPanel {
    private User currentUser;
    private final ProductDAO productDAO;
    private final CartDAO cartDAO;
    private final MainInterface parentFrame;

    // 顶部/通用 UI
    private JPanel headerPanel;
    private JPanel mainContentPanel;
    private JLabel titleLabel;
    // 客户界面持有引用，便于交互和刷新
    private JPanel productGridPanel;
    private JTextField searchField;
    private JComboBox<String> customerCategoryComboBox;
    private JLabel cartCountLabel;

    // 管理员商品管理组件
    private JTable productTable;
    private DefaultTableModel productTableModel;
    private JTextField nameField;
    private JTextField priceField;
    private JTextField stockField;
    private JComboBox<String> adminCategoryComboBox;
    private JTextArea descriptionArea;
    private JTextField imageUrlField;
    // 新增：图片样例上传与预览（BLOB）
    private JButton imageChooseButton;
    private JLabel imagePreviewLabel;
    private byte[] currentImageData;
    private Integer selectedProductId; // 当前选中商品 ID
    private JComboBox<String> adminStatusFilterComboBox; // 管理端状态筛选

    // 分类数据
    private final String[] categories = {
            "全部商品", "数码产品", "文具用品", "教材书籍",
            "服装配饰", "日用品", "食品饮料"
    };

    public StoreContentPanel(User user, MainInterface parent) {
        this.currentUser = user;
        this.parentFrame = parent;
        this.productDAO = new ProductDAO();
        this.cartDAO = new CartDAO();

        setLayout(new BorderLayout());

        // 根据用户角色决定显示界面
        UserRole role = UserRole.fromUsername(user.getUsername());
        if (role == UserRole.ADMIN) {
            createAdminInterface();
        } else {
            createCustomerInterface();
        }
    }

    private void createAdminInterface() {
        createHeader("商品管理系统");
        createAdminContent();
    }

    private void createCustomerInterface() {
        createHeader("校园商店");
        createCustomerContent();
    }

    private void createHeader(String title) {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEtchedBorder());
        headerPanel.setPreferredSize(new Dimension(0, 60));

        // 返回按钮
        // 标题
        titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // 用户信息
        JLabel userLabel = new JLabel("欢迎，" + currentUser.getNickname());
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));

        // 不再添加返回按钮，应用户要求去除
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(userLabel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void createAdminContent() {
        mainContentPanel = createProductManagementPanel();
        add(mainContentPanel, BorderLayout.CENTER);
    }

    private void createCustomerContent() {
        mainContentPanel = createShoppingPanel();
        add(mainContentPanel, BorderLayout.CENTER);
    }

    private JPanel createProductManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 工具栏
        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("添加");
        JButton updateButton = new JButton("更新");
        JButton publishButton = new JButton("上架");
        JButton unpublishButton = new JButton("下架");
        JButton deleteButton = new JButton("删除(物理)");
        JButton refreshButton = new JButton("刷新");
        JButton secondHandAdminBtn = new JButton("二手管理");
        toolbarPanel.add(addButton);
        toolbarPanel.add(updateButton);
        toolbarPanel.add(publishButton);
        toolbarPanel.add(unpublishButton);
        toolbarPanel.add(deleteButton);
        toolbarPanel.add(refreshButton);
        toolbarPanel.add(Box.createHorizontalStrut(12));
        toolbarPanel.add(secondHandAdminBtn);

        // 管理端状态筛选
        toolbarPanel.add(Box.createHorizontalStrut(15));
        toolbarPanel.add(new JLabel("筛选:"));
        adminStatusFilterComboBox = new JComboBox<>(new String[]{"全部", "仅上架", "仅下架", "仅缺货"});
        toolbarPanel.add(adminStatusFilterComboBox);

        // 商品表格
        String[] columnNames = {"ID", "商品名称", "价格", "库存", "分类", "状态"};
        productTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        productTable = new JTable(productTableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScrollPane = new JScrollPane(productTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("商品列表"));

        // 右侧编辑面板
        JPanel editPanel = createProductEditPanel();
        editPanel.setPreferredSize(new Dimension(310, 0));

        // 事件
        addButton.addActionListener(e -> handleAddProduct());
        updateButton.addActionListener(e -> handleUpdateProduct());
        publishButton.addActionListener(e -> handleSetStatus(true));
        unpublishButton.addActionListener(e -> handleSetStatus(false));
        deleteButton.addActionListener(e -> handleHardDeleteProduct());
        refreshButton.addActionListener(e -> reloadProducts());
        adminStatusFilterComboBox.addActionListener(e -> reloadProducts());
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });

        // 初次加载
        reloadProducts();

        panel.add(toolbarPanel, BorderLayout.NORTH);
        panel.add(tableScrollPane, BorderLayout.CENTER);
        panel.add(editPanel, BorderLayout.EAST);

        // 进入二手管理（嵌入式）
        secondHandAdminBtn.addActionListener(e -> {
            JPanel adminWrap = new JPanel(new BorderLayout());
            JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton backBtn = new JButton("返回商品管理");
            top.add(backBtn);
            top.add(new JLabel("二手市场管理"));
            adminWrap.add(top, BorderLayout.NORTH);
            SecondHandAdminPanel shAdmin = new SecondHandAdminPanel();
            adminWrap.add(shAdmin, BorderLayout.CENTER);

            // 替换中心内容
            remove(panel);
            mainContentPanel = adminWrap;
            add(mainContentPanel, BorderLayout.CENTER);
            revalidate(); repaint();

            backBtn.addActionListener(ev -> {
                remove(adminWrap);
                mainContentPanel = createProductManagementPanel();
                add(mainContentPanel, BorderLayout.CENTER);
                revalidate(); repaint();
            });
        });
        return panel;
    }

    JPanel createShoppingPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 顶部切换（整合到商店系统内部，视觉更简洁）
        JPanel topSwitcher = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton toSecondHandBtn = new JButton("二手商店");
        toSecondHandBtn.setToolTipText("进入二手商店");
        topSwitcher.add(new JLabel("商城"));
        topSwitcher.add(new JSeparator(SwingConstants.VERTICAL));
        topSwitcher.add(toSecondHandBtn);
        panel.add(topSwitcher, BorderLayout.NORTH);

        // 左侧搜索和分类面板
        JPanel leftPanel = createSearchPanel();
        leftPanel.setPreferredSize(new Dimension(250, 0));

        // 右侧商品展示区域
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("商品列表"));

        // 恢复为网格布局（3列）
        productGridPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        JScrollPane productScrollPane = new JScrollPane(productGridPanel);
        productScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // 提高滚动灵敏度
        productScrollPane.setWheelScrollingEnabled(true);
        productScrollPane.getVerticalScrollBar().setUnitIncrement(48);
        productScrollPane.getHorizontalScrollBar().setUnitIncrement(48);

        rightPanel.add(productScrollPane, BorderLayout.CENTER);

        // 加载商品
        loadProductsToGrid();

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);

        // 进入二手商店（整合式入口）
        toSecondHandBtn.addActionListener(e -> {
            panel.remove(leftPanel);
            panel.remove(rightPanel);
            SecondHandMarketPanel sh = new SecondHandMarketPanel(currentUser);
            panel.add(sh, BorderLayout.CENTER);
            panel.revalidate(); panel.repaint();
        });

        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("商品搜索"));

        // 搜索区域
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(15);
        JButton searchButton = new JButton("搜索");
        searchPanel.add(new JLabel("搜索:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // 分类区域
        JPanel categoryPanel = new JPanel(new FlowLayout());
        customerCategoryComboBox = new JComboBox<>(categories);
        categoryPanel.add(new JLabel("分类:"));
        categoryPanel.add(customerCategoryComboBox);

        // 购物车区域
        JPanel cartPanel = new JPanel(new FlowLayout());
        cartCountLabel = new JLabel("购物车 (0)");
        JButton cartButton = new JButton("查看购物车");
        cartPanel.add(cartCountLabel);
        cartPanel.add(cartButton);

        // 事件：搜索、分类过滤、查看购物车
        searchButton.addActionListener(e -> loadProductsToGrid());
        customerCategoryComboBox.addActionListener(e -> loadProductsToGrid());
        cartButton.addActionListener(e -> new CartDialog(parentFrame, currentUser, cartDAO).setVisible(true));

        panel.add(searchPanel);
        panel.add(categoryPanel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(cartPanel);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createProductEditPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("商品信息"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(4,4,4,4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 商品名称
        gbc.gridx=0; gbc.gridy=0; panel.add(new JLabel("名称:"), gbc);
        nameField = new JTextField(16); gbc.gridx=1; panel.add(nameField, gbc);

        // 价格
        gbc.gridx=0; gbc.gridy=1; panel.add(new JLabel("价格:"), gbc);
        priceField = new JTextField(16); gbc.gridx=1; panel.add(priceField, gbc);

        // 库存
        gbc.gridx=0; gbc.gridy=2; panel.add(new JLabel("库存:"), gbc);
        stockField = new JTextField(16); gbc.gridx=1; panel.add(stockField, gbc);

        // 分类
        gbc.gridx=0; gbc.gridy=3; panel.add(new JLabel("分类:"), gbc);
        adminCategoryComboBox = new JComboBox<>(new String[]{"数码产品","文具用品","教材书籍","服装配饰","日用品","食品饮料"});
        gbc.gridx=1; panel.add(adminCategoryComboBox, gbc);

        // 图片样例（本地上传，存数据库）
        gbc.gridx=0; gbc.gridy=4; panel.add(new JLabel("图片样例:"), gbc);
        JPanel imgRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        imageChooseButton = new JButton("选择图片...");
        imagePreviewLabel = new JLabel();
        imagePreviewLabel.setPreferredSize(new Dimension(140, 90));
        imagePreviewLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        imgRow.add(imageChooseButton);
        imgRow.add(Box.createHorizontalStrut(8));
        imgRow.add(imagePreviewLabel);
        gbc.gridx=1; panel.add(imgRow, gbc);
        // 隐藏 URL 文本框（兼容旧数据）
        imageUrlField = new JTextField(16);
        imageUrlField.setVisible(false);

        // 描述
        gbc.gridx=0; gbc.gridy=5; gbc.anchor=GridBagConstraints.NORTHWEST; panel.add(new JLabel("描述:"), gbc);
        descriptionArea = new JTextArea(4,16);
        descriptionArea.setLineWrap(true); descriptionArea.setWrapStyleWord(true);
        gbc.gridx=1; gbc.fill=GridBagConstraints.BOTH; panel.add(new JScrollPane(descriptionArea), gbc);

        // 选择图片事件
        imageChooseButton.addActionListener(e -> onChooseImage());

        // 占位拉伸
        gbc.gridy=6; gbc.gridx=0; gbc.gridwidth=2; gbc.weighty=1; gbc.fill=GridBagConstraints.VERTICAL;
        panel.add(Box.createVerticalGlue(), gbc);
        return panel;
    }

    private void loadProductsToTable(DefaultTableModel tableModel) {
        try {
            // 管理员端显示所有商品（包含下架），以便查看和再次上架
            List<Product> all = productDAO.getAllProductsForAdmin();
            String filter = adminStatusFilterComboBox != null && adminStatusFilterComboBox.getSelectedItem()!=null
                    ? adminStatusFilterComboBox.getSelectedItem().toString() : "全部";

            tableModel.setRowCount(0);
            for (Product p : all) {
                if ("仅上架".equals(filter)) {
                    if (p.getStatus() == null || p.getStatus() != model.Product.ProductStatus.ACTIVE) continue;
                } else if ("仅下架".equals(filter)) {
                    if (p.getStatus() == null || p.getStatus() != model.Product.ProductStatus.INACTIVE) continue;
                } else if ("仅缺货".equals(filter)) {
                    // 缺货优先以库存判断
                    if (p.getStock() > 0) continue;
                }

                String statusText;
                if (p.getStatus() == model.Product.ProductStatus.INACTIVE) {
                    statusText = "下架";
                } else if (p.getStock() <= 0 || p.getStatus() == model.Product.ProductStatus.OUT_OF_STOCK) {
                    statusText = "缺货";
                } else {
                    statusText = "上架";
                }

                tableModel.addRow(new Object[]{
                        p.getId(),
                        p.getName(),
                        "¥" + p.getPrice(),
                        p.getStock(),
                        p.getCategory(),
                        statusText
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "加载商品列表失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProductsToGrid() {
        try {
            String keyword = searchField != null ? searchField.getText().trim() : "";
            String selectedCategory = (customerCategoryComboBox != null && customerCategoryComboBox.getSelectedItem()!=null)
                    ? customerCategoryComboBox.getSelectedItem().toString() : "全部商品";

            List<Product> products;
            if (keyword != null && !keyword.isEmpty()) {
                products = productDAO.searchProducts(keyword);
            } else if (!"全部商品".equals(selectedCategory)) {
                products = productDAO.getProductsByCategory(selectedCategory);
            } else {
                products = productDAO.getAllProducts();
            }

            productGridPanel.removeAll();

            for (Product product : products) {
                JPanel productCard = createProductCard(product);
                productGridPanel.add(productCard);
            }

            productGridPanel.revalidate();
            productGridPanel.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载商品失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createProductCard(Product product) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createRaisedBevelBorder());
        card.setPreferredSize(new Dimension(200, 250));

        // 顶部图片（如果有）
        if (product.getImageData() != null && product.getImageData().length > 0) {
            JLabel img = new JLabel();
            img.setHorizontalAlignment(SwingConstants.CENTER);
            ImageIcon icon = getScaledIcon(product.getImageData(), 200, 120);
            if (icon != null) img.setIcon(icon);
            card.add(img, BorderLayout.NORTH);
        }

        // 商品信息
        JPanel infoPanel = new JPanel(new GridLayout(4, 1));
        infoPanel.add(new JLabel("商品: " + product.getName()));
        infoPanel.add(new JLabel("价格: ¥" + product.getPrice()));
        infoPanel.add(new JLabel("库存: " + product.getStock()));
        infoPanel.add(new JLabel("分类: " + product.getCategory()));

        // 操作按钮
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addToCartButton = new JButton("加入购物车");
        JButton detailButton = new JButton("查看详情");

        addToCartButton.addActionListener(e -> addToCart(product));
        detailButton.addActionListener(e -> showProductDetail(product));

        buttonPanel.add(addToCartButton);
        buttonPanel.add(detailButton);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }

    // ===== 管理员操作逻辑 =====
    private boolean validateAdminForm() {
        if (nameField.getText().trim().isEmpty()) {JOptionPane.showMessageDialog(this,"请输入名称");return false;}
        if (priceField.getText().trim().isEmpty()) {JOptionPane.showMessageDialog(this,"请输入价格");return false;}
        try { new BigDecimal(priceField.getText().trim()); } catch (Exception ex) { JOptionPane.showMessageDialog(this,"价格格式错误"); return false; }
        if (stockField.getText().trim().isEmpty()) {JOptionPane.showMessageDialog(this,"请输入库存");return false;}
        try { Integer.parseInt(stockField.getText().trim()); } catch (Exception ex) { JOptionPane.showMessageDialog(this,"库存格式错误"); return false; }
        return true;
    }

    private Product buildProductFromForm() {
        Product p = new Product();
        p.setName(nameField.getText().trim());
        p.setPrice(new BigDecimal(priceField.getText().trim()));
        p.setStock(Integer.parseInt(stockField.getText().trim()));
        p.setCategory((String) adminCategoryComboBox.getSelectedItem());
        p.setDescription(descriptionArea.getText().trim());
        // 设置商家为当前管理员
        if (currentUser != null) {
            p.setSellerId(currentUser.getId());
            String sellerName = currentUser.getNickname() != null && !currentUser.getNickname().isEmpty()
                    ? currentUser.getNickname() : currentUser.getUsername();
            p.setSellerName(sellerName);
        }
        if (currentImageData != null && currentImageData.length > 0) {
            p.setImageData(currentImageData);
        } else {
            String img = imageUrlField != null ? imageUrlField.getText().trim() : null;
            if (img != null && !img.isEmpty()) p.setImageUrl(img);
        }
        return p;
    }

    private void clearAdminForm() {
        selectedProductId = null;
        nameField.setText(""); priceField.setText(""); stockField.setText(""); descriptionArea.setText(""); if(imageUrlField!=null) imageUrlField.setText(""); adminCategoryComboBox.setSelectedIndex(0);
        currentImageData = null;
        if (imagePreviewLabel != null) imagePreviewLabel.setIcon(null);
    }

    private void fillFormFromSelection() {
        int row = productTable.getSelectedRow();
        if (row == -1) return;
        Object idVal = productTableModel.getValueAt(row,0);
        if (idVal instanceof Number) {
            selectedProductId = ((Number) idVal).intValue();
        } else {
            try { selectedProductId = Integer.parseInt(String.valueOf(idVal)); } catch (Exception ignore) { selectedProductId = null; }
        }
        nameField.setText(String.valueOf(productTableModel.getValueAt(row,1)));
        String priceText = String.valueOf(productTableModel.getValueAt(row,2)).replace("¥"," ").trim();
        priceField.setText(priceText);
        stockField.setText(String.valueOf(productTableModel.getValueAt(row,3)));
        adminCategoryComboBox.setSelectedItem(productTableModel.getValueAt(row,4));
        // 详情需要再查询 DB 以获取 description / image
        try {
            Product p = productDAO.getProductById(selectedProductId);
            if (p != null) {
                descriptionArea.setText(p.getDescription()!=null? p.getDescription():"");
                if (imageUrlField != null) imageUrlField.setText(p.getImageUrl()!=null? p.getImageUrl():"");
                currentImageData = p.getImageData();
                if (imagePreviewLabel != null) {
                    if (currentImageData != null && currentImageData.length > 0) {
                        ImageIcon icon = getScaledIcon(currentImageData, 140, 90);
                        if (icon != null) imagePreviewLabel.setIcon(icon);
                    } else {
                        imagePreviewLabel.setIcon(null);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private void handleAddProduct() {
        if (!validateAdminForm()) return;
        try {
            Product p = buildProductFromForm();
            if (productDAO.addProduct(p)) {
                reloadProducts();
                clearAdminForm();
                JOptionPane.showMessageDialog(this,"添加成功");
            } else JOptionPane.showMessageDialog(this,"添加失败","错误",JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this,"添加异常: "+ex.getMessage()); }
    }

    private void handleUpdateProduct() {
        if (selectedProductId == null) { JOptionPane.showMessageDialog(this,"请先选择商品"); return; }
        if (!validateAdminForm()) return;
        try {
            Product p = buildProductFromForm();
            p.setId(selectedProductId);
            if (productDAO.updateProduct(p)) {
                reloadProducts();
                JOptionPane.showMessageDialog(this,"更新成功");
            } else JOptionPane.showMessageDialog(this,"更新失败","错误",JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this,"更新异常: "+ex.getMessage()); }
    }

    private void handleHardDeleteProduct() {
        if (selectedProductId == null) { JOptionPane.showMessageDialog(this,"请先选择商品"); return; }
        int c = JOptionPane.showConfirmDialog(this,"该操作不可恢复，确定物理删除该商品?","确认",JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            if (productDAO.hardDeleteProduct(selectedProductId)) {
                reloadProducts();
                clearAdminForm();
                JOptionPane.showMessageDialog(this,"已物理删除");
            } else JOptionPane.showMessageDialog(this,"删除失败","错误",JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this,"删除异常: "+ex.getMessage()); }
    }

    private void handleSetStatus(boolean toActive) {
        if (selectedProductId == null) { JOptionPane.showMessageDialog(this,"请先选择商品"); return; }
        try {
            model.Product.ProductStatus status = toActive ? model.Product.ProductStatus.ACTIVE : model.Product.ProductStatus.INACTIVE;
            if (productDAO.setProductStatus(selectedProductId, status)) {
                reloadProducts();
                JOptionPane.showMessageDialog(this, (toActive?"已上架":"已下架"));
            } else JOptionPane.showMessageDialog(this,"操作失败","错误",JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) { JOptionPane.showMessageDialog(this,"操作异常: "+ex.getMessage()); }
    }

    private void reloadProducts() { loadProductsToTable(productTableModel); }

    private void addToCart(Product product) {
        try {
            if (product == null) return;
            if (product.getStock() <= 0) {
                JOptionPane.showMessageDialog(this, "该商品已缺货，无法加入购物车", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            CartItem item = new CartItem(currentUser.getId(), product.getId(), product.getName(), product.getPrice(), 1);
            boolean ok = cartDAO.addToCart(item);
            if (ok) {
                updateCartCount();
                JOptionPane.showMessageDialog(this, "已加入购物车: " + product.getName());
            } else {
                JOptionPane.showMessageDialog(this, "加入购物车失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "加入购物车异常: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showProductDetail(Product product) {
        try {
            new ProductDetailsDialog(parentFrame, product).setVisible(true);
        } catch (Exception ex) {
            // 回退到简单对话框
            String detail = "商品名称: " + product.getName() + "\n" +
                    "价格: ¥" + product.getPrice() + "\n" +
                    "库存: " + product.getStock() + "\n" +
                    "分类: " + product.getCategory() + "\n" +
                    (product.getDescription() != null ? ("描述: " + product.getDescription() + "\n") : "");
            JOptionPane.showMessageDialog(this, detail, "商品详情", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void updateCartCount() {
        if (cartCountLabel == null) return;
        try {
            int count = cartDAO.getCartByUserId(currentUser.getId()).size();
            cartCountLabel.setText("购物车 (" + count + ")");
        } catch (Exception ignore) {}
    }

    // 选择图片文件并预览
    private void onChooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择图片样例");
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            java.io.File file = chooser.getSelectedFile();
            try {
                byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
                currentImageData = data;
                ImageIcon icon = getScaledIcon(data, 140, 90);
                if (icon != null && imagePreviewLabel != null) imagePreviewLabel.setIcon(icon);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "读取图片失败: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 简单的缩放工具
    private ImageIcon getScaledIcon(byte[] data, int w, int h) {
        try {
            ImageIcon icon = new ImageIcon(data);
            Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }
}

