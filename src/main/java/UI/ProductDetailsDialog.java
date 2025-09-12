package UI;

import model.Product;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProductDetailsDialog extends JDialog {
    private Product product;

    public ProductDetailsDialog(JFrame parent, Product product) {
        super(parent, "商品详情", true);
        this.product = product;

        initializeComponents();
        setSize(500, 400);
        setLocationRelativeTo(parent);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // 内容面板（可滚动）
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        // 顶部图片（如有）
        if (product.getImageData() != null && product.getImageData().length > 0) {
            JLabel img = new JLabel();
            img.setHorizontalAlignment(SwingConstants.CENTER);
            ImageIcon icon = getScaledIcon(product.getImageData(), 460, 260);
            if (icon != null) img.setIcon(icon);
            img.setAlignmentX(Component.CENTER_ALIGNMENT);
            img.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
            content.add(img);
        }

        // 主要信息面板
        JPanel mainInfoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // 商品名称
        gbc.gridx = 0; gbc.gridy = 0;
        mainInfoPanel.add(new JLabel("商品名称:"), gbc);
        gbc.gridx = 1;
        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 16f));
        mainInfoPanel.add(nameLabel, gbc);

        // 价格
        gbc.gridx = 0; gbc.gridy = 1;
        mainInfoPanel.add(new JLabel("价格:"), gbc);
        gbc.gridx = 1;
        JLabel priceLabel = new JLabel("￥" + product.getPrice());
        priceLabel.setFont(priceLabel.getFont().deriveFont(Font.BOLD, 16f));
        priceLabel.setForeground(Color.RED);
        mainInfoPanel.add(priceLabel, gbc);

        // 分类
        gbc.gridx = 0; gbc.gridy = 2;
        mainInfoPanel.add(new JLabel("分类:"), gbc);
        gbc.gridx = 1;
        mainInfoPanel.add(new JLabel(product.getCategory()), gbc);

        // 库存
        gbc.gridx = 0; gbc.gridy = 3;
        mainInfoPanel.add(new JLabel("库存:"), gbc);
        gbc.gridx = 1;
        JLabel stockLabel = new JLabel(String.valueOf(product.getStock()));
        if (product.getStock() <= 0) {
            stockLabel.setForeground(Color.RED);
            stockLabel.setText(product.getStock() + " (缺货)");
        } else if (product.getStock() <= 10) {
            stockLabel.setForeground(Color.ORANGE);
            stockLabel.setText(product.getStock() + " (库存偏低)");
        }
        mainInfoPanel.add(stockLabel, gbc);

        // 销量
        gbc.gridx = 0; gbc.gridy = 4;
        mainInfoPanel.add(new JLabel("销量:"), gbc);
        gbc.gridx = 1;
        mainInfoPanel.add(new JLabel(String.valueOf(product.getSalesCount())), gbc);

        // 商家ID
        gbc.gridx = 0; gbc.gridy = 5;
        mainInfoPanel.add(new JLabel("商家ID:"), gbc);
        gbc.gridx = 1;
        mainInfoPanel.add(new JLabel(String.valueOf(product.getSellerId())), gbc);

        // 状态
        gbc.gridx = 0; gbc.gridy = 6;
        mainInfoPanel.add(new JLabel("状态:"), gbc);
        gbc.gridx = 1;
        JLabel statusLabel = new JLabel(getStatusText(product.getStatus()));
        switch (product.getStatus()) {
            case ACTIVE:
                statusLabel.setForeground(Color.GREEN);
                break;
            case INACTIVE:
                statusLabel.setForeground(Color.GRAY);
                break;
            case OUT_OF_STOCK:
                statusLabel.setForeground(Color.RED);
                break;
        }
        mainInfoPanel.add(statusLabel, gbc);

        // 将主信息与描述加入内容面板（描述与上方标签左侧对齐）
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            GridBagConstraints gbcDesc = new GridBagConstraints();
            gbcDesc.insets = new Insets(10, 10, 10, 10);
            gbcDesc.anchor = GridBagConstraints.NORTHWEST;

            // 左侧标签
            gbcDesc.gridx = 0; gbcDesc.gridy = 7;
            mainInfoPanel.add(new JLabel("描述:"), gbcDesc);

            // 右侧文本（无边框，自动换行）
            gbcDesc.gridx = 1; gbcDesc.gridy = 7;
            gbcDesc.fill = GridBagConstraints.HORIZONTAL;
            gbcDesc.weightx = 1.0;
            JTextArea descAreaPlain = new JTextArea(product.getDescription());
            descAreaPlain.setEditable(false);
            descAreaPlain.setOpaque(false);
            descAreaPlain.setBorder(null);
            descAreaPlain.setLineWrap(true);
            descAreaPlain.setWrapStyleWord(true);
            descAreaPlain.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            descAreaPlain.setRows(5); // 限高，整体滚动由外层 JScrollPane 承担
            mainInfoPanel.add(descAreaPlain, gbcDesc);
        }
        content.add(mainInfoPanel);

        // 内容外包滚动容器
        JScrollPane sp = new JScrollPane(content);
        sp.getVerticalScrollBar().setUnitIncrement(32);
        add(sp, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private String getStatusText(Product.ProductStatus status) {
        switch (status) {
            case ACTIVE:
                return "正常销售";
            case INACTIVE:
                return "暂停销售";
            case OUT_OF_STOCK:
                return "缺货";
            default:
                return "未知";
        }
    }

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
