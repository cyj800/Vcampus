package UI;

import model.User;
import database.SecondHandDAO;
import model.SecondHandItem;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.function.Consumer;

public class SecondHandPublishDialog extends JDialog {
    private final User user;
    private final SecondHandDAO dao;
    private final Runnable onSuccess;

    private JTextField nameField;
    private JTextField priceField;
    private JComboBox<String> categoryCombo;
    private JTextArea descArea;
    private JLabel imagePreview;
    private byte[] imageData;

    private final String[] categories = {
            "数码产品", "文具用品", "教材书籍", "服装配饰", "日用品", "食品饮料"
    };

    public SecondHandPublishDialog(Window owner, User user, SecondHandDAO dao, Runnable onSuccess) {
        super(owner, "发布二手物品", ModalityType.APPLICATION_MODAL);
        this.user = user; this.dao = dao; this.onSuccess = onSuccess;
        setContentPane(buildUI());
        setSize(520, 420);
        setLocationRelativeTo(owner);
    }

    private JPanel buildUI() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        nameField = new JTextField(18);
        priceField = new JTextField(18);
        categoryCombo = new JComboBox<>(categories);
        descArea = new JTextArea(5, 18);
        descArea.setLineWrap(true); descArea.setWrapStyleWord(true);
        imagePreview = new JLabel(); imagePreview.setPreferredSize(new Dimension(160, 110));

        int y=0;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("名称:"), gbc);
        gbc.gridx=1; form.add(nameField, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("价格:"), gbc);
        gbc.gridx=1; form.add(priceField, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("分类:"), gbc);
        gbc.gridx=1; form.add(categoryCombo, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; gbc.anchor=GridBagConstraints.NORTHWEST; form.add(new JLabel("描述:"), gbc);
        gbc.gridx=1; gbc.fill=GridBagConstraints.BOTH; form.add(new JScrollPane(descArea), gbc); y++;
        gbc.gridx=0; gbc.gridy=y; gbc.anchor=GridBagConstraints.WEST; gbc.fill=GridBagConstraints.HORIZONTAL; form.add(new JLabel("图片样例:"), gbc);
        JPanel imgRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton choose = new JButton("选择图片...");
        choose.addActionListener(e -> onChooseImage());
        imgRow.add(choose); imgRow.add(Box.createHorizontalStrut(8)); imgRow.add(imagePreview);
        gbc.gridx=1; form.add(imgRow, gbc);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("发布");
        JButton cancel = new JButton("取消");
        btns.add(ok); btns.add(cancel);
        ok.addActionListener(e -> onPublish());
        cancel.addActionListener(e -> dispose());

        p.add(form, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    private void onChooseImage() {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try {
                imageData = java.nio.file.Files.readAllBytes(f.toPath());
                ImageIcon icon = new ImageIcon(new ImageIcon(imageData).getImage().getScaledInstance(160,110, Image.SCALE_SMOOTH));
                imagePreview.setIcon(icon);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "读取图片失败:"+ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onPublish() {
        try {
            if (nameField.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this, "请输入名称"); return; }
            BigDecimal price = new BigDecimal(priceField.getText().trim());

            SecondHandItem item = new SecondHandItem();
            item.setName(nameField.getText().trim());
            item.setPrice(price);
            item.setCategory(String.valueOf(categoryCombo.getSelectedItem()));
            item.setDescription(descArea.getText().trim());
            item.setImageData(imageData);
            item.setSellerId(user.getId());
            item.setSellerName(user.getNickname()!=null && !user.getNickname().isEmpty()? user.getNickname(): user.getUsername());
            item.setStatus(SecondHandItem.Status.ACTIVE);

            if (dao.addItem(item)) {
                JOptionPane.showMessageDialog(this, "发布成功");
                if (onSuccess != null) onSuccess.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "发布失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "发布异常: "+ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
