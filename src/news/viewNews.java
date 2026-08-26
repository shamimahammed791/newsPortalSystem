package news;

import db.db_connect;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;

public class viewNews extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private JButton btnEdit, btnDelete;
    private ArrayList<Integer> newsIds = new ArrayList<>();

    public viewNews() {
        setTitle("View News");
        setSize(1000,550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout(10,10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        JLabel title = new JLabel("News List");
        title.setFont(new Font("Segoe UI",Font.BOLD,20));
        mainPanel.add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"Serial","Title","Description","Category","Image"},0){
            public boolean isCellEditable(int r,int c){return false;}
        };

        table = new JTable(model){
            public Class getColumnClass(int column){
                if(column==4) return ImageIcon.class;
                return Object.class;
            }
        };
        table.setRowHeight(80);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setPreferredSize(new Dimension(table.getColumnModel().getTotalColumnWidth(), 40));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center);

        JScrollPane scroll = new JScrollPane(table);
        mainPanel.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER,20,10));

        btnEdit = new JButton("Edit Selected");
        btnEdit.setPreferredSize(new Dimension(160,40));

        btnDelete = new JButton("Delete Selected");
        btnDelete.setPreferredSize(new Dimension(160,40));

        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainPanel);

        loadNews();

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row==-1){
                JOptionPane.showMessageDialog(this,"Select news to edit");
                return;
            }
            int id = newsIds.get(row);
            editNews edit = new editNews(id, this);
            edit.setVisible(true);
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row==-1){
                JOptionPane.showMessageDialog(this,"Select news to delete");
                return;
            }
            int id = newsIds.get(row);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure to delete this news?");
            if(confirm==JOptionPane.YES_OPTION){
                try(Connection con = db_connect.connect()){
                    PreparedStatement ps =
                            con.prepareStatement("DELETE FROM news WHERE id=?");
                    ps.setInt(1,id);
                    ps.executeUpdate();
                    loadNews();
                }catch(Exception ex){
                    JOptionPane.showMessageDialog(this,
                            "Error deleting news: "+ex.getMessage());
                }
            }
        });
    }

    public void loadNews(){
        model.setRowCount(0);
        newsIds.clear();
        try(Connection con = db_connect.connect()){
            String sql =
                    "SELECT n.id,n.title,n.description,c.name as category,n.image "+
                            "FROM news n LEFT JOIN category c "+
                            "ON n.category_id=c.id ORDER BY n.id DESC";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            int serial=1;
            while(rs.next()){
                ImageIcon icon = null;
                String imageName = rs.getString("image");
                if(imageName!=null && !imageName.isEmpty()){
                    File imgFile = new File(System.getProperty("user.home")+"/Desktop/newsPortalSystem/news_images/"+imageName);
                    if(imgFile.exists()){
                        ImageIcon tmpIcon = new ImageIcon(imgFile.getAbsolutePath());
                        Image scaled = tmpIcon.getImage().getScaledInstance(100,70,Image.SCALE_SMOOTH);
                        icon = new ImageIcon(scaled);
                    }
                }
                model.addRow(new Object[]{
                        serial++,
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("category"),
                        icon
                });
                newsIds.add(rs.getInt("id"));
            }
        }catch(Exception ex){
            JOptionPane.showMessageDialog(this,
                    "Error loading news: "+ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new viewNews().setVisible(true));
    }
}