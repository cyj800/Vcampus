//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package UI;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import model.UserRole;

public class Library_interface {
    public JPanel createLibraryPanel(String username) {
        UserRole userRole = UserRole.fromUsername(username);
        switch (userRole) {
            case ADMIN:
                return this.createAdminPanel(username);
            case TEACHER:
                return this.createTeacherPanel(username);
            case STUDENT:
            default:
                UserLibraryPanel studentService = new UserLibraryPanel(username);
                return studentService.createStudentPanel(username);
        }
    }

    private JPanel createTeacherPanel(String username) {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setFont(new Font("微软雅黑", 0, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        StringBuilder content = new StringBuilder();
        content.append("=== \ud83d\udc68\u200d\ud83c\udfeb 教师图书馆功能 ===\n\n");
        content.append("欢迎你，").append(username).append("老师！\n\n");
        content.append("\ud83d\udccb 可用功能:\n");
        content.append("  \ud83d\udd0d 1. 图书查询\n");
        content.append("  \ud83d\udcd6 2. 借阅管理\n");
        content.append("  \ud83d\udd52 3. 预约服务\n");
        content.append("  \ud83d\udcbb 4. 电子资源访问\n");
        content.append("  \ud83c\udf93 5. 学术资源推荐\n");
        content.append("  \ud83d\udc64 6. 个人中心\n\n");
        contentArea.setText(content.toString());
        JScrollPane scrollPane = new JScrollPane(contentArea);
        panel.add(scrollPane, "Center");
        return panel;
    }

    private JPanel createAdminPanel(String username) {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setFont(new Font("微软雅黑", 0, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        StringBuilder content = new StringBuilder();
        content.append("=== \ud83d\udc51 管理员图书馆功能 ===\n\n");
        content.append("欢迎你，管理员！\n\n");
        content.append("\ud83d\udccb 可用功能:\n");
        content.append("  \ud83d\udcda 1. 图书管理\n");
        content.append("  \ud83d\udc65 2. 用户管理\n");
        content.append("  \ud83d\udcca 3. 借阅记录查询\n");
        content.append("  ⚙️ 4. 系统配置\n");
        content.append("  \ud83d\udcc8 5. 统计报表\n");
        content.append("  \ud83d\uded2 6. 图书采购\n\n");
        contentArea.setText(content.toString());
        JScrollPane scrollPane = new JScrollPane(contentArea);
        panel.add(scrollPane, "Center");
        return panel;
    }
}
