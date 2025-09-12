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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class PaperSearchPanel extends JPanel {
    private JButton backButton;
    private JTextField searchField;
    private JTable table;

    public PaperSearchPanel(Runnable returnToMainCallback) {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        this.setBackground(new Color(245, 245, 245));
        JPanel topPanel = this.createTopPanel(returnToMainCallback);
        this.add(topPanel, "North");
        JPanel searchPanel = this.createSearchPanel();
        this.add(searchPanel, "Center");
        JScrollPane tablePanel = this.createTablePanel();
        this.add(tablePanel, "South");
    }

    private JPanel createTopPanel(Runnable returnToMainCallback) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        this.backButton = new JButton("返回主界面");
        this.backButton.setFont(new Font("微软雅黑", 0, 14));
        this.backButton.addActionListener((e) -> returnToMainCallback.run());
        JLabel titleLabel = new JLabel("文献检索");
        titleLabel.setFont(new Font("微软雅黑", 1, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = (double)1.0F;
        gbc.anchor = 17;
        panel.add(this.backButton, gbc);
        gbc.gridx = 1;
        gbc.weightx = (double)2.0F;
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
        this.searchField.setPreferredSize(new Dimension(600, 35));
        JButton searchButton = new JButton("搜索文献");
        searchButton.setFont(new Font("微软雅黑", 0, 16));
        searchButton.setPreferredSize(new Dimension(120, 35));
        searchButton.addActionListener(this::performSearch);
        panel.add(this.searchField);
        panel.add(searchButton);
        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] columnNames = new String[]{"标题", "作者", "期刊/会议", "年份", "摘要"};
        Object[][] data = new Object[][]{{"深度学习在自然语言处理中的应用", "李华等人", "计算机学报", "2020", "本文研究了深度学习在NLP中的应用..."}, {"基于人工智能的图像识别技术", "张三", "人工智能学报", "2021", "探讨了AI在图像识别领域的最新进展..."}, {"区块链技术研究综述", "王五等人", "计算机研究与发展", "2019", "对区块链技术的发展进行了全面综述..."}, {"云计算安全机制研究", "赵六", "软件学报", "2022", "分析了云计算环境下的安全问题和解决方案..."}, {"大数据分析在医疗领域的应用", "刘七", "数据挖掘", "2018", "研究如何利用大数据分析改善医疗诊断..."}};
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };
        this.table = new JTable(model);
        this.table.setFont(new Font("微软雅黑", 0, 14));
        this.table.setRowHeight(30);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(0);
        centerRenderer.setVerticalAlignment(0);

        for(int i = 0; i < this.table.getColumnCount(); ++i) {
            this.table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JTableHeader header = this.table.getTableHeader();
        header.setFont(new Font("微软雅黑", 1, 14));
        header.setDefaultRenderer(centerRenderer);
        return new JScrollPane(this.table);
    }

    private void performSearch(ActionEvent e) {
        String keyword = this.searchField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入搜索关键词");
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
        JFrame frame = new JFrame("文献检索系统");
        frame.setDefaultCloseOperation(3);
        frame.setSize(1200, 600);
        frame.setLocationRelativeTo((Component)null);
        Runnable returnToMainCallback = () -> {
            JOptionPane.showMessageDialog(frame, "即将返回主界面");
            frame.dispose();
        };
        PaperSearchPanel searchPanel = new PaperSearchPanel(returnToMainCallback);
        frame.add(searchPanel);
        frame.setVisible(true);
    }
}
