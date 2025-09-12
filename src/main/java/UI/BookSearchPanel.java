//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package UI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class BookSearchPanel extends JPanel {
    private JButton backButton;
    private JTextField searchField;
    private JTable table;
    private Image backgroundImage;

    public BookSearchPanel(Runnable returnToMainCallback) {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        try {
            this.backgroundImage = ImageIO.read(this.getClass().getResource("/images/book_back.png"));
        } catch (IOException e) {
            e.printStackTrace();
            this.setBackground(new Color(245, 245, 245));
        }

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setOpaque(false);
        JPanel topPanel = this.createTopPanel(returnToMainCallback);
        contentPanel.add(topPanel, "North");
        JPanel searchPanel = this.createSearchPanel();
        contentPanel.add(searchPanel, "Center");
        JScrollPane tablePanel = this.createTablePanel();
        contentPanel.add(tablePanel, "South");
        this.add(contentPanel, "Center");
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.backgroundImage != null) {
            g.drawImage(this.backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }

    }

    private JPanel createTopPanel(Runnable returnToMainCallback) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        this.backButton = new JButton("返回主界面");
        this.backButton.setFont(new Font("微软雅黑", 0, 14));
        this.backButton.addActionListener((e) -> returnToMainCallback.run());
        JLabel titleLabel = new JLabel("馆藏查询");
        titleLabel.setFont(new Font("微软雅黑", 1, 24));
        titleLabel.setForeground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = (double)1.0F;
        gbc.anchor = 17;
        panel.add(this.backButton, gbc);
        gbc.gridx = 1;
        gbc.weightx = (double)3.0F;
        gbc.anchor = 10;
        panel.add(titleLabel, gbc);
        gbc.gridx = 2;
        gbc.weightx = (double)1.0F;
        gbc.anchor = 13;
        panel.add(Box.createHorizontalStrut(this.backButton.getPreferredSize().width), gbc);
        return panel;
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(1, 10, 10));
        panel.setOpaque(false);
        this.searchField = new JTextField(60);
        this.searchField.setFont(new Font("微软雅黑", 0, 16));
        this.searchField.setPreferredSize(new Dimension(500, 35));
        JButton searchButton = new JButton("搜索图书");
        searchButton.setFont(new Font("微软雅黑", 0, 16));
        searchButton.setPreferredSize(new Dimension(120, 35));
        searchButton.addActionListener(this::performSearch);
        panel.add(this.searchField);
        panel.add(searchButton);
        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = new String[]{"书名", "作者", "出版社", "出版年份", "状态"};
        Object[][] data = new Object[][]{{"Java编程思想", "Bruce Eckel", "机械工业出版社", "2007", "可借"}, {"深入理解计算机系统", "Randal E. Bryant", "机械工业出版社", "2016", "可借"}, {"算法导论", "Thomas H. Cormen", "机械工业出版社", "2013", "已借出"}, {"设计模式", "Erich Gamma", "机械工业出版社", "2010", "可借"}, {"计算机组成与设计", "David A. Patterson", "机械工业出版社", "2019", "可借"}};
        this.table = new JTable(data, columnNames);
        this.table.setFont(new Font("微软雅黑", 0, 16));
        this.table.setRowHeight(30);
        this.table.setBackground(new Color(255, 255, 255, 200));
        this.table.setForeground(Color.BLACK);
        this.table.setOpaque(true);
        JScrollPane scrollPane = new JScrollPane(this.table);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        return scrollPane;
    }

    private void performSearch(ActionEvent e) {
        String keyword = this.searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入搜索关键词啊小傻瓜");
        } else {
            JOptionPane.showMessageDialog(this, "搜索关键词: " + keyword);
        }
    }

    public String getSearchKeyword() {
        return this.searchField.getText().trim();
    }

    public void setTableData(Object[][] data, String[] columnNames) {
        this.table.setModel(new DefaultTableModel(data, columnNames));
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("图书查询系统");
        frame.setDefaultCloseOperation(3);
        frame.setSize(1200, 600);
        frame.setLocationRelativeTo((Component)null);
        Runnable returnToMainCallback = () -> {
            JOptionPane.showMessageDialog(frame, "返回主界面");
            frame.dispose();
        };
        BookSearchPanel searchPanel = new BookSearchPanel(returnToMainCallback);
        frame.add(searchPanel);
        frame.setVisible(true);
    }
}
