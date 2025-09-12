package UI;

import model.User;
import model.UserRole;
import database.CartDAO;
import database.SecondHandDAO;
import model.SecondHandItem;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SecondHandMarketPanel extends JPanel {
    private final User currentUser;
    private final SecondHandDAO dao = new SecondHandDAO();
    private final CartDAO cartDAO = new CartDAO();

    // 顾客视图
    private JPanel gridPanel;
    private JTextField searchField;
    private JComboBox<String> categoryCombo;

    // 卖家视图（学生/老师）
    private JButton publishButton;
    private JPanel myListPanel;

    private final String[] categories = {
            "全部", "数码产品", "文具用品", "教材书籍", "服装配饰", "日用品", "食品饮料"
    };

    public SecondHandMarketPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        reloadAll();
    }

    private JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        JLabel title = new JLabel("二手市场", SwingConstants.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 18));
        p.add(title, BorderLayout.CENTER);
        JButton backBtn = new JButton("返回商城");
        backBtn.setToolTipText("返回校园商店");
        p.add(backBtn, BorderLayout.WEST);
        backBtn.addActionListener(e -> {
            // 找到外层 StoreContentPanel 并让其重建商城界面
            java.awt.Component c = this;
            while (c != null && !(c instanceof UI.StoreContentPanel)) c = c.getParent();
            if (c instanceof UI.StoreContentPanel) {
                UI.StoreContentPanel store = (UI.StoreContentPanel) c;
                store.removeAll();
                JPanel recreated = storeCreateShoppingPanel(store);
                store.setLayout(new BorderLayout());
                store.add(recreated, BorderLayout.CENTER);
                store.revalidate(); store.repaint();
            } else {
                // 兜底：关闭当前并提示
                JOptionPane.showMessageDialog(this, "已返回商店，可通过左侧‘校园商店’按钮再次进入");
            }
        });
        return p;
    }

    // 友好地调用 StoreContentPanel 的 createShoppingPanel（包可见辅助）
    private JPanel storeCreateShoppingPanel(UI.StoreContentPanel storePanel) {
        try {
            java.lang.reflect.Method m = UI.StoreContentPanel.class.getDeclaredMethod("createShoppingPanel");
            m.setAccessible(true);
            return (JPanel) m.invoke(storePanel);
        } catch (Exception e) {
            JPanel fallback = new JPanel(new BorderLayout());
            fallback.add(new JLabel("返回失败，请点击左侧‘校园商店’重进", SwingConstants.CENTER), BorderLayout.CENTER);
            return fallback;
        }
    }

    private JComponent buildBody() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.75);

        // 左侧：公开列表
        JPanel left = new JPanel(new BorderLayout());
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(16);
        JButton searchBtn = new JButton("搜索");
        categoryCombo = new JComboBox<>(categories);
        filter.add(new JLabel("搜索:"));
        filter.add(searchField);
        filter.add(searchBtn);
        filter.add(new JLabel("分类:"));
        filter.add(categoryCombo);
        left.add(filter, BorderLayout.NORTH);

        gridPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.getVerticalScrollBar().setUnitIncrement(48);
        left.add(scroll, BorderLayout.CENTER);

        // 右侧：我的发布（仅学生/老师）
        JPanel right = new JPanel(new BorderLayout());
        JPanel head = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        head.add(new JLabel("我的发布"));
        publishButton = new JButton("发布二手");
        head.add(publishButton);
        right.add(head, BorderLayout.NORTH);
        myListPanel = new JPanel();
        myListPanel.setLayout(new BoxLayout(myListPanel, BoxLayout.Y_AXIS));
        myListPanel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        right.add(new JScrollPane(myListPanel), BorderLayout.CENTER);

        split.setLeftComponent(left);
        split.setRightComponent(right);

        // 权限：管理员不显示发布按钮
        UserRole role = UserRole.fromUsername(currentUser.getUsername());
        publishButton.setVisible(role != UserRole.ADMIN);

        // 事件
        searchBtn.addActionListener(e -> reloadPublic());
        categoryCombo.addActionListener(e -> reloadPublic());
        publishButton.addActionListener(e -> openPublishDialog());

        return split;
    }

    private void reloadAll() {
        reloadPublic();
        reloadMine();
    }

    public void refresh() {
        reloadAll();
    }

    private void reloadPublic() {
        gridPanel.removeAll();
        String keyword = searchField.getText().trim();
        String cat = String.valueOf(categoryCombo.getSelectedItem());
        List<SecondHandItem> items;
        if (!keyword.isEmpty()) {
            items = dao.searchActive(keyword);
        } else if (!"全部".equals(cat)) {
            items = dao.listActiveByCategory(cat);
        } else {
            items = dao.listActive();
        }
        for (SecondHandItem it : items) {
            gridPanel.add(SecondHandViews.card(it, currentUser, cartDAO));
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void reloadMine() {
        myListPanel.removeAll();
        List<SecondHandItem> mine = dao.listBySeller(currentUser.getId());
        for (SecondHandItem it : mine) {
            myListPanel.add(SecondHandViews.rowForMine(it, dao, this));
            myListPanel.add(Box.createVerticalStrut(2)); // 紧凑分隔
        }
        myListPanel.revalidate();
        myListPanel.repaint();
    }

    private void openPublishDialog() {
        SecondHandPublishDialog dialog = new SecondHandPublishDialog(SwingUtilities.getWindowAncestor(this), currentUser, dao, this::reloadAll);
        dialog.setVisible(true);
    }
}
