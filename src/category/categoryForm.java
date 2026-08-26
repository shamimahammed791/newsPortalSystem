package category;

import db.db_connect;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class categoryForm extends JFrame {

    private JTextField txtName;
    private JButton btnAdd, btnDelete;
    private JTable table;
    private DefaultTableModel model;

    public categoryForm() {
        setTitle("Category Management");
        setSize(650,420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        JPanel mainPanel = new JPanel(new BorderLayout(10,10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        add(mainPanel);

        // Input Panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
        JLabel lblName = new JLabel("Category Name:");
        lblName.setFont(new Font("Segoe UI",Font.BOLD,14));
        txtName = new JTextField(20);
        txtName.setFont(new Font("Segoe UI",Font.PLAIN,14));
        btnAdd = new JButton("Add Category");
        btnAdd.setFont(new Font("Segoe UI",Font.BOLD,14));
        btnAdd.setPreferredSize(new Dimension(140,35));
        inputPanel.add(lblName); inputPanel.add(txtName); inputPanel.add(btnAdd);
        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(new Object[]{"Serial","Category Name"},0){
            @Override
            public boolean isCellEditable(int row,int col){
                return col==1; // only category name editable
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI",Font.PLAIN,14));
        table.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,14));
        table.getTableHeader().setBackground(new Color(220,220,220));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);

        JScrollPane scroll = new JScrollPane(table);
        mainPanel.add(scroll, BorderLayout.CENTER);

        // Delete Panel
        JPanel deletePanel = new JPanel();
        btnDelete = new JButton("Delete Selected");
        btnDelete.setFont(new Font("Segoe UI",Font.BOLD,14));
        btnDelete.setPreferredSize(new Dimension(160,40));
        deletePanel.add(btnDelete);
        mainPanel.add(deletePanel, BorderLayout.SOUTH);

        // Load categories
        loadCategories();

        // Add category
        btnAdd.addActionListener(e -> {
            String name = txtName.getText().trim();
            if(name.isEmpty()){
                JOptionPane.showMessageDialog(this,"Please enter category name");
                return;
            }
            try(Connection con = db_connect.connect()){
                PreparedStatement ps = con.prepareStatement("INSERT INTO category(name) VALUES(?)");
                ps.setString(1,name);
                ps.executeUpdate();
                txtName.setText("");
                loadCategories();
            }catch(SQLException ex){
                JOptionPane.showMessageDialog(this,"Error adding category: "+ex.getMessage());
            }
        });

        // Delete category
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if(row==-1) { JOptionPane.showMessageDialog(this,"Select a category to delete"); return;}
            int serial = (int) model.getValueAt(row,0); // get serial
            int confirm = JOptionPane.showConfirmDialog(this,"Are you sure?");
            if(confirm==JOptionPane.YES_OPTION){
                try(Connection con = db_connect.connect()){
                    Statement st = con.createStatement();
                    ResultSet rs = st.executeQuery("SELECT id FROM category ORDER BY id DESC");
                    int idToDelete = -1;
                    int count = 1;
                    while(rs.next()){
                        if(count == serial){
                            idToDelete = rs.getInt("id");
                            break;
                        }
                        count++;
                    }
                    if(idToDelete != -1){
                        PreparedStatement ps = con.prepareStatement("DELETE FROM category WHERE id=?");
                        ps.setInt(1,idToDelete); ps.executeUpdate();
                        loadCategories();
                    }
                }catch(SQLException ex){
                    JOptionPane.showMessageDialog(this,"Error deleting category: "+ex.getMessage());
                }
            }
        });

        // Inline edit listener
        table.getModel().addTableModelListener(e -> {
            if(e.getType() == javax.swing.event.TableModelEvent.UPDATE){
                int row = e.getFirstRow();
                int col = e.getColumn();
                if(col==1){ // Category Name column
                    int serial = (int) table.getValueAt(row,0);
                    String newName = (String) table.getValueAt(row,1);
                    try(Connection con = db_connect.connect()){
                        Statement st = con.createStatement();
                        ResultSet rs = st.executeQuery("SELECT id FROM category ORDER BY id DESC");
                        int idToUpdate = -1;
                        int count = 1;
                        while(rs.next()){
                            if(count == serial){
                                idToUpdate = rs.getInt("id");
                                break;
                            }
                            count++;
                        }
                        if(idToUpdate != -1){
                            updateCategory(idToUpdate,newName);
                        }
                    }catch(SQLException ex){ JOptionPane.showMessageDialog(this,"Error updating category: "+ex.getMessage());}
                }
            }
        });

        table.setSurrendersFocusOnKeystroke(true);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    private void loadCategories(){
        model.setRowCount(0);
        try(Connection con = db_connect.connect()){
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM category ORDER BY id DESC");
            int serial = 1;
            while(rs.next()){
                model.addRow(new Object[]{serial++,rs.getString("name")});
            }
        }catch(SQLException ex){ JOptionPane.showMessageDialog(this,"Error loading categories: "+ex.getMessage()); }
    }

    private void updateCategory(int id,String name){
        try(Connection con = db_connect.connect()){
            PreparedStatement ps = con.prepareStatement("UPDATE category SET name=? WHERE id=?");
            ps.setString(1,name); ps.setInt(2,id); ps.executeUpdate();
        }catch(SQLException ex){ JOptionPane.showMessageDialog(this,"Error updating category: "+ex.getMessage()); }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new categoryForm().setVisible(true));
    }
}