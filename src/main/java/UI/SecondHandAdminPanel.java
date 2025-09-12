package UI;

import database.SecondHandDAO;
import model.SecondHandItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SecondHandAdminPanel extends JPanel {
    private final SecondHandDAO dao = new SecondHandDAO();
    private JTable table; private DefaultTableModel model;

    public SecondHandAdminPanel() {
        setLayout(new BorderLayout());
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("刷新");
        JButton sold = new JButton("标记售出");
        JButton del = new JButton("删除");
        toolbar.add(refresh); toolbar.add(sold); toolbar.add(del);

        model = new DefaultTableModel(new String[]{"ID","名称","价格","分类","卖家","状态"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        table = new JTable(model);
        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh.addActionListener(e -> reload());
        sold.addActionListener(e -> changeStatus(SecondHandItem.Status.SOLD));
        del.addActionListener(e -> deleteSelected());
        reload();
    }

    private void reload() {
        model.setRowCount(0);
        List<SecondHandItem> list = dao.listAllForAdmin();
        for (SecondHandItem it : list) {
            model.addRow(new Object[]{it.getId(), it.getName(), it.getPrice(), it.getCategory(), it.getSellerName(), it.getStatus()});
        }
    }

    private Integer selectedId() {
        int r = table.getSelectedRow();
        if (r<0) return null;
        Object v = model.getValueAt(r,0);
        if (v instanceof Number) return ((Number) v).intValue();
        try { return Integer.parseInt(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    private void changeStatus(SecondHandItem.Status st) {
        Integer id = selectedId(); if (id==null) { JOptionPane.showMessageDialog(this,"请选择记录"); return; }
        if (dao.setStatus(id, st)) { JOptionPane.showMessageDialog(this, "已更新状态"); reload(); }
    }

    private void deleteSelected() {
        Integer id = selectedId(); if (id==null) { JOptionPane.showMessageDialog(this,"请选择记录"); return; }
        if (JOptionPane.showConfirmDialog(this, "确认删除?", "确认", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
            if (dao.deleteItem(id)) { JOptionPane.showMessageDialog(this, "已删除"); reload(); }
        }
    }
}
