package login;

import db.db_connect;
import javax.swing.*;
import java.sql.*;
import java.awt.*;

public class loginForm extends JFrame {

    private JTextField username;
    private JPasswordField password;
    private JButton loginBtn;

    public loginForm() {
        setTitle("Admin Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new GridBagLayout()); // centers panel automatically

        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(400,200));
        panel.setLayout(null);

        JLabel heading = new JLabel("News Portal Login Form");
        heading.setBounds(0,10,400,30);
        heading.setHorizontalAlignment(JLabel.CENTER);
        heading.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 25));
        panel.add(heading);

        JLabel uLabel = new JLabel("Username:");
        uLabel.setBounds(30,60,100,30);
        panel.add(uLabel);

        username = new JTextField();
        username.setBounds(130,60,200,30);
        panel.add(username);

        JLabel pLabel = new JLabel("Password:");
        pLabel.setBounds(30,100,100,30);
        panel.add(pLabel);

        password = new JPasswordField();
        password.setBounds(130,100,200,30);
        panel.add(password);

        loginBtn = new JButton("Login");
        loginBtn.setBounds(130,140,100,30);
        panel.add(loginBtn);

        add(panel);

        loginBtn.addActionListener(e -> login());
    }

    private void login() {
        String user = username.getText();
        String pass = String.valueOf(password.getPassword());

        try (Connection con = db_connect.connect()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM users WHERE username=? AND password=?");
            ps.setString(1, user);
            ps.setString(2, pass);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) {
                JOptionPane.showMessageDialog(this,"Login Successful");
                new dashboard.dashboard().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this,"Incorrect Username or Password");
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new loginForm().setVisible(true);
    }
}