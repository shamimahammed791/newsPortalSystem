package news;

import db.db_connect;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;
import javax.imageio.ImageIO;

public class publicNews extends JFrame {

    private JPanel newsContainer;

    public publicNews() {
        setTitle("Latest News");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        newsContainer = new JPanel();
        newsContainer.setLayout(new BoxLayout(newsContainer, BoxLayout.Y_AXIS));
        newsContainer.setBackground(Color.WHITE);
        newsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(newsContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);

        loadNews();
    }

    private void loadNews() {
        try (Connection con = db_connect.connect()) {
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT n.id, n.title, n.description, n.image, c.name category " +
                            "FROM news n JOIN category c ON n.category_id=c.id " +
                            "ORDER BY n.id DESC"
            );

            while (rs.next()) {
                String title = rs.getString("title");
                String description = rs.getString("description");
                String category = rs.getString("category");
                String imageName = rs.getString("image");

                JPanel card = createNewsCard(title, description, category, imageName);
                newsContainer.add(card);
                newsContainer.add(Box.createRigidArea(new Dimension(0, 12)));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading news: " + e.getMessage());
        }
    }

    private JPanel createNewsCard(String title, String description, String category, String imageName) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblTitle);

        card.add(Box.createRigidArea(new Dimension(0, 6)));

        if (imageName != null && !imageName.isEmpty()) {
            ImageIcon icon = loadImageFromDesktop(imageName);
            if (icon != null) {
                JLabel imgLabel = new JLabel(icon);
                imgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(imgLabel);
                card.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        JLabel lblCategory = new JLabel("Category: " + category);
        lblCategory.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblCategory.setForeground(Color.GRAY);
        lblCategory.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblCategory);

        card.add(Box.createRigidArea(new Dimension(0, 8)));

        JTextArea txtDesc = new JTextArea(description);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        txtDesc.setEditable(false);
        txtDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDesc.setBackground(Color.WHITE);
        txtDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDesc.setBorder(null);

        card.add(txtDesc);

        return card;
    }

    private ImageIcon loadImageFromDesktop(String imageName) {
        try {
            String path = System.getProperty("user.home") + "/Desktop/newsPortalSystem/news_images/" + imageName;
            File imgFile = new File(path);
            if (imgFile.exists()) {
                Image img = ImageIO.read(imgFile).getScaledInstance(500, 280, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new publicNews().setVisible(true));
    }
}