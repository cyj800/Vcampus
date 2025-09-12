//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package UI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class UserLibraryPanel {
    private HoverButton book_Button;
    private HoverButton paper_Button;
    private HoverButton db_Button;
    private HoverButton add_Button;
    private JTextField search_TextField = new JTextField(30);
    private JButton searchButton;
    private JPanel buttonPanel;
    private JScrollPane tableScrollPane = new JScrollPane();
    private ScheduledExecutorService animationExecutor;
    private String currentUserId;
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private JPanel mainPanel;
    private BookSearchPanel bookSearchPanel;
    private PaperSearchPanel paperSearchPanel;

    public UserLibraryPanel(String userId) {
        this.currentUserId = userId;
        this.animationExecutor = Executors.newSingleThreadScheduledExecutor();
        this.cardLayout = new CardLayout();
        this.mainContainer = new JPanel(this.cardLayout);
        this.book_Button = new HoverButton("图书查询", new Color(70, 130, 180), this.createScaledImageIcon("images/红_小.png", 150, 220), this.createScaledImageIcon("images/红_大.png", 300, 220));
        this.paper_Button = new HoverButton("文献检索", new Color(60, 179, 113), this.createScaledImageIcon("images/蓝_小.png", 150, 220), this.createScaledImageIcon("images/蓝_大.png", 300, 220));
        this.db_Button = new HoverButton("数据库搜索", new Color(218, 165, 32), this.createScaledImageIcon("images/黄_小.png", 150, 220), this.createScaledImageIcon("images/黄_大.png", 300, 220));
        this.add_Button = new HoverButton("联系我们", new Color(205, 92, 92), this.createScaledImageIcon("images/绿_小.png", 150, 220), this.createScaledImageIcon("images/绿_大.png", 300, 220));
        this.setupButton(this.book_Button);
        this.setupButton(this.paper_Button);
        this.setupButton(this.db_Button);
        this.setupButton(this.add_Button);
        this.searchButton = new JButton("搜索");
        this.searchButton.setFont(new Font("微软雅黑", 0, 16));
        this.searchButton.setBackground(new Color(70, 130, 180));
        this.searchButton.setForeground(Color.WHITE);
        this.searchButton.addActionListener((e) -> this.performSearch());
        this.setupSearchField();
        this.book_Button.addActionListener((e) -> this.showBookSearchPanel());
        this.paper_Button.addActionListener((e) -> this.showPaperSearchPanel());
        this.db_Button.addActionListener((e) -> this.showDatabaseLinks());
        this.add_Button.addActionListener((e) -> JOptionPane.showMessageDialog((Component)null, "请联系管理员: 孤独根号三@library.com"));
        this.book_Button.expand();
        final AnimationController controller = new AnimationController(new HoverButton[]{this.book_Button, this.paper_Button, this.db_Button, this.add_Button});
        MouseAdapter sharedAdapter = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                HoverButton source = (HoverButton)e.getSource();
                controller.animateTo(source);
            }
        };
        this.book_Button.addMouseListener(sharedAdapter);
        this.paper_Button.addMouseListener(sharedAdapter);
        this.db_Button.addMouseListener(sharedAdapter);
        this.add_Button.addMouseListener(sharedAdapter);
        this.createPanels();
        this.mainContainer.add(this.mainPanel, "Main");
        this.mainContainer.add(this.bookSearchPanel, "BookSearch");
        this.mainContainer.add(this.paperSearchPanel, "PaperSearch");
        this.cardLayout.show(this.mainContainer, "Main");
    }

    private void createPanels() {
        this.mainPanel = this.createStudentPanel("用户");
        this.bookSearchPanel = new BookSearchPanel(this::returnToMainPanel);
        this.paperSearchPanel = new PaperSearchPanel(this::returnToMainPanel);
    }

    private void showBookSearchPanel() {
        this.cardLayout.show(this.mainContainer, "BookSearch");
    }

    private void showPaperSearchPanel() {
        this.cardLayout.show(this.mainContainer, "PaperSearch");
    }

    private void returnToMainPanel() {
        this.cardLayout.show(this.mainContainer, "Main");
    }

    private void showBookSearch() {
        this.search_TextField.setVisible(true);
        this.search_TextField.setText("");
        this.search_TextField.setToolTipText("请输入图书名称、作者或分类...");
        this.search_TextField.requestFocus();
        this.tableScrollPane.setVisible(false);
    }

    private void showPaperSearch() {
        this.search_TextField.setVisible(true);
        this.search_TextField.setText("");
        this.search_TextField.setToolTipText("请输入文献关键词...");
        this.search_TextField.requestFocus();
        this.tableScrollPane.setVisible(false);
    }

    private void showDatabaseLinks() {
        this.search_TextField.setVisible(false);
        this.tableScrollPane.setVisible(false);
        JPanel dbPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        dbPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel("常用学术数据库链接:");
        title.setFont(new Font("微软雅黑", 1, 18));
        dbPanel.add(title);
        this.addHyperlink(dbPanel, "1. CNKI中国知网", "https://www.cnki.net");
        this.addHyperlink(dbPanel, "2. IEEE Xplore", "https://ieeexplore.ieee.org");
        this.addHyperlink(dbPanel, "3. Springer Link", "https://link.springer.com");
        JOptionPane.showMessageDialog((Component)null, dbPanel, "数据库链接", -1);
    }

    private void performSearch() {
        String keyword = this.search_TextField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog((Component)null, "请输入搜索关键词");
        } else {
            JOptionPane.showMessageDialog((Component)null, "搜索关键词: " + keyword);
        }
    }

    private void addHyperlink(JPanel panel, String text, final String url) {
        JLabel linkLabel = new JLabel(text);
        linkLabel.setFont(new Font("微软雅黑", 0, 16));
        linkLabel.setForeground(Color.BLUE.darker());
        linkLabel.setCursor(Cursor.getPredefinedCursor(12));
        linkLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(URI.create(url));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog((Component)null, "无法打开链接: " + ex.getMessage());
                }

            }
        });
        panel.add(linkLabel);
    }

    private ImageIcon createScaledImageIcon(String imagePath, int width, int height) {
        URL imgURL = this.getClass().getClassLoader().getResource(imagePath);
        if (imgURL != null) {
            ImageIcon originalIcon = new ImageIcon(imgURL);
            Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, 4);
            return new ImageIcon(scaledImage);
        } else {
            BufferedImage img = new BufferedImage(width, height, 2);
            Graphics2D g2d = img.createGraphics();
            GradientPaint gradient = new GradientPaint(0.0F, 0.0F, Color.LIGHT_GRAY, (float)width, (float)height, Color.DARK_GRAY);
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, width, height);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("微软雅黑", 1, 12));
            String text = "失败";
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            g2d.drawString(text, (width - textWidth) / 2, height / 2);
            g2d.dispose();
            return new ImageIcon(img);
        }
    }

    private void setupButton(HoverButton button) {
        button.setPreferredSize(new Dimension(150, 220));
        button.setFont(new Font("微软雅黑", 1, 16));
        button.setForeground(Color.WHITE);
        button.setHorizontalTextPosition(0);
        button.setVerticalTextPosition(3);
        button.setIconTextGap(5);
    }

    private void setupSearchField() {
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        this.search_TextField.setFont(new Font("微软雅黑", 0, 16));
        this.search_TextField.setBackground(new Color(240, 240, 240));
        this.search_TextField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        this.search_TextField.setVisible(true);
        this.search_TextField.addActionListener((e) -> this.performSearch());
        this.searchButton.setPreferredSize(new Dimension(80, 30));
        searchPanel.add(this.search_TextField, "Center");
        searchPanel.add(this.searchButton, "East");
    }

    public JPanel createStudentPanel(String username) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        panel.setBackground(new Color(245, 245, 245));
        JLabel titleLabel = new JLabel("欢迎使用图书馆服务系统, " + username + "!");
        titleLabel.setFont(new Font("微软雅黑", 1, 20));
        titleLabel.setHorizontalAlignment(0);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, "North");
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.1;
        centerPanel.add(Box.createVerticalGlue(), gbc);
        gbc.gridy = 1;
        gbc.weighty = (double)0.0F;
        this.buttonPanel = new JPanel();
        this.buttonPanel.setOpaque(false);
        this.buttonPanel.setLayout(new BoxLayout(this.buttonPanel, 0));
        this.buttonPanel.add(Box.createHorizontalGlue());
        this.buttonPanel.add(this.book_Button);
        this.buttonPanel.add(Box.createHorizontalStrut(20));
        this.buttonPanel.add(this.paper_Button);
        this.buttonPanel.add(Box.createHorizontalStrut(20));
        this.buttonPanel.add(this.db_Button);
        this.buttonPanel.add(Box.createHorizontalStrut(20));
        this.buttonPanel.add(this.add_Button);
        this.buttonPanel.add(Box.createHorizontalGlue());
        centerPanel.add(this.buttonPanel, gbc);
        gbc.gridy = 2;
        gbc.weightx = (double)1.0F;
        gbc.fill = 2;
        gbc.insets = new Insets(30, 150, 0, 72);
        this.setupSearchField();
        JPanel searchPanel = (JPanel)this.search_TextField.getParent();
        centerPanel.add(searchPanel, gbc);
        gbc.gridy = 3;
        gbc.weighty = 0.4;
        centerPanel.add(Box.createVerticalGlue(), gbc);
        panel.add(centerPanel, "Center");
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        tablePanel.add(this.tableScrollPane, "Center");
        panel.add(tablePanel, "South");
        return panel;
    }

    public JPanel getMainContainer() {
        return this.mainContainer;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("图书馆服务系统");
            frame.setDefaultCloseOperation(3);
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo((Component)null);
            UserLibraryPanel service = new UserLibraryPanel("user123");
            frame.add(service.getMainContainer());
            frame.setVisible(true);
        });
    }

    class AnimationController {
        private final HoverButton[] buttons;
        private HoverButton currentExpanded;

        public AnimationController(HoverButton... buttons) {
            this.buttons = buttons;
            UserLibraryPanel.this.animationExecutor.scheduleAtFixedRate(this::updateAnimation, 0L, 16L, TimeUnit.MILLISECONDS);
        }

        public void animateTo(HoverButton target) {
            if (this.currentExpanded != null && this.currentExpanded != target) {
                this.currentExpanded.shrink();
            }

            this.currentExpanded = target;
            target.expand();
        }

        private void updateAnimation() {
            boolean needsRepaint = false;

            for(HoverButton btn : this.buttons) {
                if (btn.updateAnimationState()) {
                    needsRepaint = true;
                }
            }

            if (needsRepaint && UserLibraryPanel.this.buttonPanel != null) {
                SwingUtilities.invokeLater(() -> {
                    UserLibraryPanel.this.buttonPanel.revalidate();
                    UserLibraryPanel.this.buttonPanel.repaint();
                });
            }

        }
    }

    class HoverButton extends JButton {
        private final Color baseColor;
        private final ImageIcon normalIcon;
        private final ImageIcon expandedIcon;
        private volatile float currentWidth = 150.0F;
        private volatile int targetWidth = 150;
        private volatile boolean isExpanded = false;
        private BufferedImage bufferImage;
        private final Object bufferLock = new Object();

        public HoverButton(String text, Color color, ImageIcon normalIcon, ImageIcon expandedIcon) {
            super(text);
            this.baseColor = color;
            this.normalIcon = normalIcon;
            this.expandedIcon = expandedIcon;
            this.setContentAreaFilled(false);
            this.setBorderPainted(false);
            this.setFocusPainted(false);
            this.setBackground(this.baseColor);
            this.setIcon(normalIcon);
        }

        public boolean updateAnimationState() {
            if (Math.abs(this.currentWidth - (float)this.targetWidth) < 0.5F) {
                this.currentWidth = (float)this.targetWidth;
                return false;
            } else {
                this.currentWidth += ((float)this.targetWidth - this.currentWidth) * 0.08F;
                return true;
            }
        }

        public void expand() {
            if (!this.isExpanded) {
                this.isExpanded = true;
                this.targetWidth = 300;
                this.setIcon(this.expandedIcon);
            }
        }

        public void shrink() {
            if (this.isExpanded) {
                this.isExpanded = false;
                this.targetWidth = 150;
                this.setIcon(this.normalIcon);
            }
        }

        public Dimension getPreferredSize() {
            return new Dimension((int)this.currentWidth, 220);
        }

        protected void paintComponent(Graphics g) {
            synchronized(this.bufferLock) {
                if (this.bufferImage == null || this.bufferImage.getWidth() != this.getWidth() || this.bufferImage.getHeight() != this.getHeight()) {
                    this.bufferImage = new BufferedImage(this.getWidth(), this.getHeight(), 2);
                }

                Graphics2D g2d = this.bufferImage.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                if (this.getIcon() != null) {
                    Image img = ((ImageIcon)this.getIcon()).getImage();
                    g2d.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
                }

                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D textBounds = fm.getStringBounds(this.getText(), g2d);
                int textX = (this.getWidth() - (int)textBounds.getWidth()) / 2;
                int textY = this.getHeight() - 10;
                g2d.setColor(new Color(0, 0, 0, 100));
                g2d.drawString(this.getText(), textX + 1, textY + 1);
                g2d.setColor(this.getForeground());
                g2d.drawString(this.getText(), textX, textY);
                g2d.dispose();
                g.drawImage(this.bufferImage, 0, 0, (ImageObserver)null);
            }
        }
    }
}
