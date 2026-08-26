package user;

import db.db_connect;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import javax.swing.border.TitledBorder;
import java.awt.Font;

public class userForm extends JFrame {

    private JTextField txtUsername, txtEmail, txtContact;
    private JPasswordField txtPassword;
    private JButton btnAdd, btnDelete;
    private JTable table;
    private DefaultTableModel model;

    public userForm() {

        setTitle("User Management");
        setSize(750,520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(5,2,10,10));
        TitledBorder border = BorderFactory.createTitledBorder("Add User");
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 22));
        inputPanel.setBorder(border);
        inputPanel.setPreferredSize(new Dimension(350,220));

        inputPanel.add(new JLabel("Username"));
        txtUsername = new JTextField();
        inputPanel.add(txtUsername);

        inputPanel.add(new JLabel("Email"));
        txtEmail = new JTextField();
        inputPanel.add(txtEmail);

        inputPanel.add(new JLabel("Contact Number"));
        txtContact = new JTextField();
        inputPanel.add(txtContact);

        inputPanel.add(new JLabel("Password"));
        txtPassword = new JPasswordField();
        inputPanel.add(txtPassword);

        btnAdd = new JButton("Add User");
        inputPanel.add(new JLabel());
        inputPanel.add(btnAdd);

        JPanel formWrapper = new JPanel();
        formWrapper.add(inputPanel);
        centerPanel.add(formWrapper, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Serial","Username","Email","Contact"},0){
            @Override
            public boolean isCellEditable(int row,int col){
                return col == 2 || col == 3;
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI",Font.PLAIN,14));
        table.setSelectionBackground(new Color(0,120,215));
        table.setGridColor(new Color(220,220,220));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI",Font.BOLD,14));
        header.setBackground(new Color(221, 221, 221));
        header.setForeground(Color.BLACK);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
        centerPanel.add(scroll, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        btnDelete = new JButton("Delete Selected");
        btnDelete.setBackground(new Color(220,53,69));
        btnDelete.setForeground(Color.WHITE);
        bottom.add(btnDelete);
        add(bottom, BorderLayout.SOUTH);

        loadUsers();

        btnAdd.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String email = txtEmail.getText().trim();
            String contact = txtContact.getText().trim();
            String password = new String(txtPassword.getPassword());

            if(username.isEmpty() || email.isEmpty() || contact.isEmpty() || password.isEmpty()){
                JOptionPane.showMessageDialog(this,"All fields required");
                return;
            }

            try(Connection con = db_connect.connect()){
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO users(username,email,contact,password) VALUES(?,?,?,?)"
                );
                ps.setString(1,username);
                ps.setString(2,email);
                ps.setString(3,contact);
                ps.setString(4,password);
                ps.executeUpdate();

                txtUsername.setText("");
                txtEmail.setText("");
                txtContact.setText("");
                txtPassword.setText("");

                loadUsers();

            }catch(Exception ex){
                JOptionPane.showMessageDialog(this,"Error adding user: "+ex.getMessage());
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row==-1){ JOptionPane.showMessageDialog(this,"Select user"); return; }
            String username = (String) model.getValueAt(row,1);
            int confirm = JOptionPane.showConfirmDialog(this,"Are you sure?");
            if(confirm==JOptionPane.YES_OPTION){
                try(Connection con = db_connect.connect()){
                    PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE username=?");
                    ps.setString(1,username);
                    ps.executeUpdate();
                    loadUsers();
                }catch(Exception ex){ JOptionPane.showMessageDialog(this,"Error deleting user: "+ex.getMessage()); }
            }
        });

        model.addTableModelListener(e -> {
            if(e.getType() == javax.swing.event.TableModelEvent.UPDATE){
                int row = e.getFirstRow();
                int col = e.getColumn();
                if(col==2 || col==3){
                    String username = (String) model.getValueAt(row,1);
                    String email = (String) model.getValueAt(row,2);
                    String contact = (String) model.getValueAt(row,3);
                    updateUser(username,email,contact);
                }
            }
        });

    }

    private void loadUsers(){
        model.setRowCount(0);
        try(Connection con = db_connect.connect()){
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT username,email,contact FROM users ORDER BY id DESC");
            int serial = 1;
            while(rs.next()){
                model.addRow(new Object[]{
                        serial++,
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("contact")
                });
            }
        }catch(Exception ex){ JOptionPane.showMessageDialog(this,"Error loading users: "+ex.getMessage()); }
    }

    private void updateUser(String username,String email,String contact){
        try(Connection con = db_connect.connect()){
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE users SET email=?, contact=? WHERE username=?"
            );
            ps.setString(1,email);
            ps.setString(2,contact);
            ps.setString(3,username);
            ps.executeUpdate();
        }catch(SQLException ex){ JOptionPane.showMessageDialog(this,"Error updating user: "+ex.getMessage()); }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new userForm().setVisible(true));
    }
}