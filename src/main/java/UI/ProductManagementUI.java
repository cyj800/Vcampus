package UI;

import database.ProductDAO;
import model.Product;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品管理界面 - 仅限管理员使用
 */
public class ProductManagementUI extends JFrame {
    private User currentUser;
    private ProductDAO productDAO;

    // UI组件
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField nameField;
    private JTextField priceField;
    private JTextField stockField;
    private JTextArea descriptionArea;
    private JComboBox<String> categoryComboBox;
    private JTextField imageUrlField;

    // 表格列名
    private final String[] columnNames = {
            "ID", "商品名称", "价格", "库存", "分类", "状态"
    };

    // 分类数据
    private final String[] categories = {
            "数码产品", "文具用品", "教材书籍",
            "服装配饰", "日用品", "食品饮料"
    };

    public ProductManagementUI(User user) {
        this.currentUser = user;
        this.productDAO = new ProductDAO();

        initializeComponents();
        setupLayout();
        addEventListeners();
        loadProducts();

        setTitle("商品管理系统 - " + user.getNickname());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    private void initializeComponents() {
        // 初始化表格
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可直接编辑
            }
        };
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 初始化输入组件
        nameField = new JTextField(20);
        priceField = new JTextField(10);
        stockField = new JTextField(10);
        descriptionArea = new JTextArea(3, 20);
        categoryComboBox = new JComboBox<>(categories);
        imageUrlField = new JTextField(20);

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // 顶部工具栏
        JPanel toolbarPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("添加商品");
        JButton updateButton = new JButton("更新商品");
        JButton deleteButton = new JButton("删除商品");
        JButton refreshButton = new JButton("刷新列表");

        addButton.setActionCommand("ADD");
        updateButton.setActionCommand("UPDATE");
        deleteButton.setActionCommand("DELETE");
        refreshButton.setActionCommand("REFRESH");

        toolbarPanel.add(addButton);
        toolbarPanel.add(updateButton);
        toolbarPanel.add(deleteButton);
        toolbarPanel.add(refreshButton);

        // 中央表格区域
        JScrollPane tableScrollPane = new JScrollPane(productTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("商品列表"));

        // 右侧商品信息编辑区域
        JPanel editPanel = new JPanel(new GridBagLayout());
        editPanel.setBorder(BorderFactory.createTitledBorder("商品信息"));
        editPanel.setPreferredSize(new Dimension(300, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // 商品名称
        gbc.gridx = 0; gbc.gridy = 0;
        editPanel.add(new JLabel("商品名称:"), gbc);
        gbc.gridx = 1;
        editPanel.add(nameField, gbc);

        // 价格
        gbc.gridx = 0; gbc.gridy = 1;
        editPanel.add(new JLabel("价格:"), gbc);
        gbc.gridx = 1;
        editPanel.add(priceField, gbc);

        // 库存
        gbc.gridx = 0; gbc.gridy = 2;
        editPanel.add(new JLabel("库存:"), gbc);
        gbc.gridx = 1;
        editPanel.add(stockField, gbc);

        // 分类
        gbc.gridx = 0; gbc.gridy = 3;
        editPanel.add(new JLabel("分类:"), gbc);
        gbc.gridx = 1;
        editPanel.add(categoryComboBox, gbc);

        // 图片URL
        gbc.gridx = 0; gbc.gridy = 4;
        editPanel.add(new JLabel("图片URL:"), gbc);
        gbc.gridx = 1;
        editPanel.add(imageUrlField, gbc);

        // 描述
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        editPanel.add(new JLabel("描述:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        editPanel.add(new JScrollPane(descriptionArea), gbc);

        // 添加事件监听器
        ActionListener buttonListener = this::handleButtonAction;
        addButton.addActionListener(buttonListener);
        updateButton.addActionListener(buttonListener);
        deleteButton.addActionListener(buttonListener);
        refreshButton.addActionListener(buttonListener);

        // 表格选择监听器
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedProduct();
            }
        });

        // 布局
        add(toolbarPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(editPanel, BorderLayout.EAST);
    }

    private void addEventListeners() {
        // 已在setupLayout中添加
    }

    private void handleButtonAction(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "ADD":
                addProduct();
                break;
            case "UPDATE":
                updateProduct();
                break;
            case "DELETE":
                deleteProduct();
                break;
            case "REFRESH":
                loadProducts();
                clearFields();
                break;
        }
    }

    private void addProduct() {
        if (validateFields()) {
            try {
                Product product = createProductFromFields();
                if (productDAO.addProduct(product)) {
                    JOptionPane.showMessageDialog(this, "商品添加成功！");
                    loadProducts();
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "商品添加失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "添加商品时出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要更新的商品！");
            return;
        }

        if (validateFields()) {
            try {
                Integer productId = (Integer) tableModel.getValueAt(selectedRow, 0);
                Product product = createProductFromFields();
                product.setId(productId.intValue());

                if (productDAO.updateProduct(product)) {
                    JOptionPane.showMessageDialog(this, "商品更新成功！");
                    loadProducts();
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "商品更新失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "更新商品时出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请先选择要删除的商品！");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除这个商品吗？", "确认删除", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Integer productId = (Integer) tableModel.getValueAt(selectedRow, 0);
                if (productDAO.deleteProduct(productId.intValue())) {
                    JOptionPane.showMessageDialog(this, "商品删除成功！");
                    loadProducts();
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "商品删除失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "删除商品时出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadProducts() {
        try {
            List<Product> products = productDAO.getAllProductsForAdmin();
            tableModel.setRowCount(0); // 清空表格

            for (Product product : products) {
                Object[] row = {
                        product.getId(),
                        product.getName(),
                        "¥" + product.getPrice(),
                        product.getStock(),
                        product.getCategory(),
                        product.getStock() > 0 ? "有库存" : "缺货"
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载商品列表失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow != -1) {
            try {
                Integer productId = (Integer) tableModel.getValueAt(selectedRow, 0);
                Product product = productDAO.getProductById(productId.intValue());

                if (product != null) {
                    nameField.setText(product.getName());
                    priceField.setText(product.getPrice().toString());
                    stockField.setText(String.valueOf(product.getStock()));
                    categoryComboBox.setSelectedItem(product.getCategory());
                    descriptionArea.setText(product.getDescription());
                    imageUrlField.setText(product.getImageUrl() != null ? product.getImageUrl() : "");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "加载商品详情失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入商品名称！");
            return false;
        }

        try {
            BigDecimal price = new BigDecimal(priceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                JOptionPane.showMessageDialog(this, "价格必须大于0！");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的价格！");
            return false;
        }

        try {
            int stock = Integer.parseInt(stockField.getText().trim());
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "库存不能为负数！");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的库存数量！");
            return false;
        }

        return true;
    }

    private Product createProductFromFields() {
        Product product = new Product();
        product.setName(nameField.getText().trim());
        product.setPrice(new BigDecimal(priceField.getText().trim()));
        product.setStock(Integer.parseInt(stockField.getText().trim()));
        product.setCategory((String) categoryComboBox.getSelectedItem());
        product.setDescription(descriptionArea.getText().trim());

        String imageUrl = imageUrlField.getText().trim();
        if (!imageUrl.isEmpty()) {
            product.setImageUrl(imageUrl);
        }

        return product;
    }

    private void clearFields() {
        nameField.setText("");
        priceField.setText("");
        stockField.setText("");
        descriptionArea.setText("");
        categoryComboBox.setSelectedIndex(0);
        imageUrlField.setText("");
    }
}
