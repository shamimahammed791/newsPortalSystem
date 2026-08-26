package dashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import db.db_connect;

public class dashboard extends JFrame {

    private JLabel newsCountLabel;
    private JLabel categoryCountLabel;
    private JLabel usersCountLabel;

    public dashboard() {
        setTitle("News Portal Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle screenBounds = ge.getMaximumWindowBounds();
        setBounds(screenBounds);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setLayout(new BorderLayout());

        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(25,25,112));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, getHeight()));
        add(sidebar, BorderLayout.WEST);

        JButton dashboardBtn = createSidebarButton("Dashboard");
        sidebar.add(Box.createVerticalStrut(40));
        sidebar.add(dashboardBtn);
        sidebar.add(Box.createVerticalStrut(15));

        JButton portalBtn = createSidebarButton("View News Portal");
        sidebar.add(portalBtn);
        sidebar.add(Box.createVerticalStrut(15));

        JButton newsBtn = createSidebarButton("News \u25BC");
        sidebar.add(newsBtn);

        JPanel newsSubMenuPanel = new JPanel();
        newsSubMenuPanel.setBackground(new Color(25,25,112));
        newsSubMenuPanel.setLayout(new BoxLayout(newsSubMenuPanel, BoxLayout.Y_AXIS));
        newsSubMenuPanel.setVisible(false);
        sidebar.add(newsSubMenuPanel);

        JButton addNewsBtn = createSidebarSubButton("Add News");
        JButton viewNewsBtn = createSidebarSubButton("View News");
        JButton categoryBtn = createSidebarSubButton("Categories");

        newsSubMenuPanel.add(Box.createVerticalStrut(10));
        newsSubMenuPanel.add(addNewsBtn);
        newsSubMenuPanel.add(Box.createVerticalStrut(10));
        newsSubMenuPanel.add(viewNewsBtn);
        newsSubMenuPanel.add(Box.createVerticalStrut(10));
        newsSubMenuPanel.add(categoryBtn);
        sidebar.add(Box.createVerticalStrut(20));

        JButton usersBtn = createSidebarButton("Users");
        sidebar.add(usersBtn);
        sidebar.add(Box.createVerticalStrut(15));

        JButton logoutBtn = createSidebarButton("Logout");
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalGlue());

        portalBtn.addActionListener(e -> new news.publicNews().setVisible(true));
        newsBtn.addActionListener(e -> newsSubMenuPanel.setVisible(!newsSubMenuPanel.isVisible()));
        addNewsBtn.addActionListener(e -> new news.addNews().setVisible(true));
        viewNewsBtn.addActionListener(e -> new news.viewNews().setVisible(true));
        categoryBtn.addActionListener(e -> new category.categoryForm().setVisible(true));
        usersBtn.addActionListener(e -> new user.userForm().setVisible(true));
        logoutBtn.addActionListener(e -> {
            this.dispose();
            new login.loginForm().setVisible(true);
        });

        JPanel mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        JPanel cardsPanel = new JPanel(new GridLayout(1,4,30,0));
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(50, 30, 30, 30));
        mainPanel.add(cardsPanel, BorderLayout.NORTH);

        newsCountLabel = new JLabel();
        JPanel panelNews = createCard("Total News", new Color(60,179,113), newsCountLabel);
        panelNews.setPreferredSize(new Dimension(200, 160));
        panelNews.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new news.viewNews().setVisible(true);
            }
        });
        cardsPanel.add(panelNews);

        categoryCountLabel = new JLabel();
        JPanel panelCategory = createCard("Total Categories", new Color(255,165,0), categoryCountLabel);
        panelCategory.setPreferredSize(new Dimension(200, 160));
        panelCategory.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new category.categoryForm().setVisible(true);
            }
        });
        cardsPanel.add(panelCategory);

        usersCountLabel = new JLabel();
        JPanel panelUsers = createCard("Total Users", new Color(220,20,60), usersCountLabel);
        panelUsers.setPreferredSize(new Dimension(200, 160));
        panelUsers.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new user.userForm().setVisible(true);
            }
        });
        cardsPanel.add(panelUsers);

        JPanel panelPortal = new JPanel();
        panelPortal.setBackground(new Color(70,130,180));
        panelPortal.setLayout(new BorderLayout());

        JLabel portalLabel = new JLabel("View All News", JLabel.CENTER);
        portalLabel.setForeground(Color.WHITE);
        portalLabel.setFont(new Font("Arial",Font.BOLD,26));

        panelPortal.add(portalLabel, BorderLayout.CENTER);

        panelPortal.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new news.publicNews().setVisible(true);
            }
        });

        cardsPanel.add(panelPortal);

        refreshCounts();
        new javax.swing.Timer(3000, e -> refreshCounts()).start();

        setVisible(true);
    }

    private JButton createSidebarButton(String text){
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(180, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0,0,128));
        btn.setFocusPainted(false);
        return btn;
    }

    private JButton createSidebarSubButton(String text){
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(160, 35));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0,0,128));
        btn.setFocusPainted(false);
        return btn;
    }

    private JPanel createCard(String title, Color color, JLabel countLabel){
        JPanel panel = new JPanel();
        panel.setBackground(color);
        panel.setLayout(new GridLayout(2,1));

        JLabel l1 = new JLabel(title, JLabel.CENTER);
        l1.setForeground(Color.WHITE);
        l1.setFont(new Font("Arial",Font.BOLD,24));
        panel.add(l1);

        countLabel.setHorizontalAlignment(JLabel.CENTER);
        countLabel.setForeground(Color.WHITE);
        countLabel.setFont(new Font("Arial",Font.BOLD,42));
        panel.add(countLabel);

        return panel;
    }

    public void refreshCounts(){
        newsCountLabel.setText(String.valueOf(getCount("news")));
        categoryCountLabel.setText(String.valueOf(getCount("category")));
        usersCountLabel.setText(String.valueOf(getCount("users")));
    }

    private int getCount(String table){
        int count = 0;
        try(Connection con = db_connect.connect()){
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM "+table);
            if(rs.next()) count = rs.getInt(1);
        } catch(Exception e){ e.printStackTrace(); }
        return count;
    }

    public static void main(String[] args) {
        new dashboard();
    }
}