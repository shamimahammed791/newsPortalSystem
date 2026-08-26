package news;

import db.db_connect;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class editNews extends JFrame {

    private int newsId;
    private viewNews parent;
    private JTextField txtTitle;
    private JTextArea txtDescription;
    private JComboBox<String> comboCategory;
    private JButton btnUpdate;
    private JLabel imagePathLabel;
    private JLabel imagePreview;
    private File selectedImage;
    private String existingImageName;

    public editNews(int id) {
        this(id, null);
    }

    public editNews(int id, viewNews parent) {
        this.newsId = id;
        this.parent = parent;

        setTitle("Edit News");
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

        btnUpdate = new JButton("Update News");
        btnUpdate.setBounds(150,480,150,35);
        btnUpdate.setBackground(new Color(0,120,215));
        btnUpdate.setForeground(Color.WHITE);
        add(btnUpdate);

        loadCategories();
        loadNewsData();

        btnUpload.addActionListener(e -> chooseImage());
        btnUpdate.addActionListener(e -> updateNews());
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        int option = chooser.showOpenDialog(this);
        if(option==JFileChooser.APPROVE_OPTION){
            selectedImage = chooser.getSelectedFile();
            imagePathLabel.setText(selectedImage.getName());

            ImageIcon icon = new ImageIcon(selectedImage.getAbsolutePath());
            Image img = icon.getImage().getScaledInstance(200,150,Image.SCALE_SMOOTH);
            imagePreview.setIcon(new ImageIcon(img));
        }
    }

    private void loadCategories() {
        try(Connection con = db_connect.connect()){
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT name FROM category ORDER BY name");
            while(rs.next()) comboCategory.addItem(rs.getString("name"));
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private int getCategoryIdByName(String name) {
        try(Connection con = db_connect.connect()){
            PreparedStatement ps = con.prepareStatement("SELECT id FROM category WHERE name=?");
            ps.setString(1,name);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getInt("id");
        } catch(Exception ex){ ex.printStackTrace(); }
        return -1;
    }

    private void loadNewsData() {
        try(Connection con = db_connect.connect()){
            PreparedStatement ps = con.prepareStatement("SELECT * FROM news WHERE id=?");
            ps.setInt(1, newsId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                txtTitle.setText(rs.getString("title"));
                txtDescription.setText(rs.getString("description"));

                existingImageName = rs.getString("image");
                if(existingImageName!=null && !existingImageName.isEmpty()){
                    File imgFile = new File(System.getProperty("user.home")+"/Desktop/newsPortalSystem/news_images/"+existingImageName);
                    if(imgFile.exists()){
                        ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
                        Image img = icon.getImage().getScaledInstance(200,150,Image.SCALE_SMOOTH);
                        imagePreview.setIcon(new ImageIcon(img));
                        imagePathLabel.setText(existingImageName);
                    }
                }

                String catName = getCategoryNameById(rs.getInt("category_id"));
                comboCategory.setSelectedItem(catName);
            }
        } catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Error loading news data: "+ex.getMessage());
        }
    }

    private String getCategoryNameById(int catId) {
        try(Connection con = db_connect.connect()){
            PreparedStatement ps = con.prepareStatement("SELECT name FROM category WHERE id=?");
            ps.setInt(1,catId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getString("name");
        } catch(Exception ex){ ex.printStackTrace(); }
        return "";
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9\\-]", "_");
    }

    private void updateNews() {
        String title = txtTitle.getText().trim();
        String description = txtDescription.getText().trim();
        String category = (String) comboCategory.getSelectedItem();

        if(title.isEmpty() || description.isEmpty() || category==null){
            JOptionPane.showMessageDialog(this,"All fields are required");
            return;
        }

        int catId = getCategoryIdByName(category);

        try(Connection con = db_connect.connect()){

            String folderPath = System.getProperty("user.home")+"/Desktop/newsPortalSystem/news_images/";

            String imageName = existingImageName;

            if(selectedImage!=null){
                String extension = "";
                int i = selectedImage.getName().lastIndexOf('.');
                if(i>0) extension = selectedImage.getName().substring(i);

                imageName = sanitizeFileName(title)+extension;

                File folder = new File(folderPath);
                if(!folder.exists()) folder.mkdirs();

                File dest = new File(folderPath+imageName);
                Files.copy(selectedImage.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE news SET title=?, description=?, category_id=?, image=? WHERE id=?"
            );

            ps.setString(1,title);
            ps.setString(2,description);
            ps.setInt(3,catId);
            ps.setString(4,imageName);
            ps.setInt(5,newsId);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,"News updated successfully");
            if(parent != null) parent.loadNews(); // refresh viewNews table
            dispose();

        } catch(Exception ex){
            JOptionPane.showMessageDialog(this, "Error updating news: "+ex.getMessage());
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new editNews(1).setVisible(true));
    }
}