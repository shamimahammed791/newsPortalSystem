package news;

import db.db_connect;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class addNews extends JFrame {

    private JTextField txtTitle;
    private JTextArea txtDescription;
    private JComboBox<String> comboCategory;
    private JLabel imagePathLabel;
    private JLabel imagePreview;
    private File selectedImage;

    public addNews() {

        setTitle("Add News");
        setSize(650,600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        JLabel lblTitle = new JLabel("Title");
        lblTitle.setBounds(40,30,100,25);
        add(lblTitle);

        txtTitle = new JTextField();
        txtTitle.setBounds(150,30,400,30);
        add(txtTitle);

        JLabel lblDescription = new JLabel("Description");
        lblDescription.setBounds(40,80,100,25);
        add(lblDescription);

        txtDescription = new JTextArea();
        JScrollPane scroll = new JScrollPane(txtDescription);
        scroll.setBounds(150,80,400,120);
        add(scroll);

        JLabel lblCategory = new JLabel("Category");
        lblCategory.setBounds(40,220,100,25);
        add(lblCategory);

        comboCategory = new JComboBox<>();
        comboCategory.setBounds(150,220,200,30);
        add(comboCategory);

        JLabel lblImage = new JLabel("Image");
        lblImage.setBounds(40,270,100,25);
        add(lblImage);

        JButton btnUpload = new JButton("Upload Image");
        btnUpload.setBounds(150,270,150,30);
        add(btnUpload);

        imagePathLabel = new JLabel("No Image Selected");
        imagePathLabel.setBounds(310,270,250,30);
        add(imagePathLabel);

        imagePreview = new JLabel();
        imagePreview.setBounds(150,310,200,150);
        imagePreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(imagePreview);

        JButton btnAdd = new JButton("Add News");
        btnAdd.setBounds(150,480,150,35);
        btnAdd.setBackground(new Color(0,120,215));
        btnAdd.setForeground(Color.WHITE);
        add(btnAdd);

        loadCategories();

        btnUpload.addActionListener(e -> chooseImage());
        btnAdd.addActionListener(e -> addNews());
    }

    private void chooseImage(){

        JFileChooser chooser = new JFileChooser();
        int option = chooser.showOpenDialog(this);

        if(option == JFileChooser.APPROVE_OPTION){

            selectedImage = chooser.getSelectedFile();
            imagePathLabel.setText(selectedImage.getName());

            ImageIcon icon = new ImageIcon(selectedImage.getAbsolutePath());
            Image img = icon.getImage().getScaledInstance(200,150,Image.SCALE_SMOOTH);
            imagePreview.setIcon(new ImageIcon(img));
        }
    }

    private void loadCategories(){

        try(Connection con = db_connect.connect()){

            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM category ORDER BY name");

            while(rs.next()){
                comboCategory.addItem(rs.getString("name"));
            }

        }catch(Exception ex){
            JOptionPane.showMessageDialog(this,ex.getMessage());
        }
    }

    private int getCategoryId(String name){

        try(Connection con = db_connect.connect()){

            PreparedStatement ps =
                    con.prepareStatement("SELECT id FROM category WHERE name=?");

            ps.setString(1,name);

            ResultSet rs = ps.executeQuery();

            if(rs.next()) return rs.getInt("id");

        }catch(Exception ex){
            ex.printStackTrace();
        }

        return -1;
    }

    private String sanitizeFileName(String name){
        return name.replaceAll("[^a-zA-Z0-9\\-]", "_");
    }

    private void addNews(){

        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        String category = (String) comboCategory.getSelectedItem();

        if(title.isEmpty() || description.isEmpty() || selectedImage == null){

            JOptionPane.showMessageDialog(this,"Fill all fields and select image");
            return;
        }

        int categoryId = getCategoryId(category);

        try(Connection con = db_connect.connect()){

            String folderPath = System.getProperty("user.home") + "/Desktop/NewsPortalSystem/news_images/";
            File folder = new File(folderPath);

            if(!folder.exists()) folder.mkdirs();

            String extension = "";
            String fileName = selectedImage.getName();

            int i = fileName.lastIndexOf('.');
            if(i > 0) extension = fileName.substring(i);

            String imageName = sanitizeFileName(title) + extension;

            File dest = new File(folderPath + imageName);

            Files.copy(selectedImage.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO news(title,description,category_id,image,created_at) VALUES(?,?,?,?,NOW())"
            );

            ps.setString(1,title);
            ps.setString(2,description);
            ps.setInt(3,categoryId);
            ps.setString(4,imageName);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,"News Added Successfully");

            txtTitle.setText("");
            txtDescription.setText("");
            imagePreview.setIcon(null);
            imagePathLabel.setText("No Image Selected");
            selectedImage = null;

        }catch(Exception ex){

            JOptionPane.showMessageDialog(this,ex.getMessage());
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new addNews().setVisible(true));
    }
}