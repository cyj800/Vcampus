package UI;

import model.User;
import model.UserRole;
import database.CartDAO;
import database.SecondHandDAO;
import model.SecondHandItem;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;


public class SecondHandViews {
    public static JPanel card(SecondHandItem it, User currentUser, CartDAO cartDAO) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createRaisedBevelBorder());
        card.setPreferredSize(new Dimension(200, 250));
        if (it.getImageData()!=null && it.getImageData().length>0) {
            JLabel img = new JLabel(); img.setHorizontalAlignment(SwingConstants.CENTER);
            ImageIcon icon = new ImageIcon(new ImageIcon(it.getImageData()).getImage().getScaledInstance(200,120, Image.SCALE_SMOOTH));
            img.setIcon(icon); card.add(img, BorderLayout.NORTH);
        }
        JPanel info = new JPanel(new GridLayout(0,1));
        info.add(new JLabel("商品: "+it.getName()));
        info.add(new JLabel("价格: ¥"+it.getPrice()));
        info.add(new JLabel("分类: "+it.getCategory()));
        info.add(new JLabel("卖家: "+it.getSellerName()));
        JPanel btns = new JPanel(new FlowLayout());
        JButton detail = new JButton("详情");
        btns.add(detail);
        // 非管理员可以加入购物车（与商城共享）
        if (currentUser != null && UserRole.fromUsername(currentUser.getUsername()) != UserRole.ADMIN) {
            JButton addToCart = new JButton("加入购物车");
            btns.add(addToCart);
            addToCart.addActionListener(e -> {
                try {
                    // 用负数ID区分二手，避免与商城商品ID冲突
                    int cartPid = -Math.abs(it.getId());
                    model.CartItem item = new model.CartItem(
                            currentUser.getId(),
                            cartPid,
                            it.getName(),
                            it.getPrice() != null ? it.getPrice() : BigDecimal.ZERO,
                            1
                    );
                    boolean ok = cartDAO.addToCart(item);
                    if (ok) {
                        JOptionPane.showMessageDialog(card, "已加入购物车: " + it.getName());
                    } else {
                        JOptionPane.showMessageDialog(card, "加入购物车失败，请稍后重试", "错误", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(card, "加入购物车异常: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
        detail.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            sb.append("名称: ").append(it.getName()).append('\n');
            sb.append("价格: ¥").append(it.getPrice()).append('\n');
            sb.append("分类: ").append(it.getCategory()).append('\n');
            if (it.getDescription()!=null) sb.append("描述: ").append(it.getDescription());
            JOptionPane.showMessageDialog(card, sb.toString(), "二手详情", JOptionPane.INFORMATION_MESSAGE);
        });
        card.add(info, BorderLayout.CENTER);
        card.add(btns, BorderLayout.SOUTH);
        return card;
    }

    public static JPanel rowForMine(SecondHandItem it, SecondHandDAO dao, JComponent parent) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(new JLabel("["+it.getStatus()+"] "+it.getName()+"  ¥"+it.getPrice()+"  ("+it.getCategory()+")"));
        JButton sold = new JButton("标记售出");
        JButton del = new JButton("删除");
        row.add(sold); row.add(del);
        sold.addActionListener(e -> {
            if (dao.setStatus(it.getId(), SecondHandItem.Status.SOLD)) {
                JOptionPane.showMessageDialog(parent, "已标记为售出");
                Container top = SwingUtilities.getAncestorOfClass(SecondHandMarketPanel.class, parent);
                if (top instanceof SecondHandMarketPanel) ((SecondHandMarketPanel) top).refresh();
            }
        });
        del.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(parent, "确定删除?", "确认", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
                if (dao.deleteItem(it.getId())) {
                    JOptionPane.showMessageDialog(parent, "已删除");
                    Container top = SwingUtilities.getAncestorOfClass(SecondHandMarketPanel.class, parent);
                    if (top instanceof SecondHandMarketPanel) ((SecondHandMarketPanel) top).refresh();
                }
            }
        });
        // 紧凑视觉：减少左右间距与上下留白
        row.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        return row;
    }
}
