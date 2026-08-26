package db;

import java.sql.Connection;
import java.sql.DriverManager;

public class db_connect {

    public static Connection connect() {
        Connection con = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/news_portal",
                    "root",
                    ""
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        return con;
    }
}