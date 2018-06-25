/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.User;



/**
 *
 * @author dang toan thang d13cn5
 */
public class DAO {
    private String url = "jdbc:mysql://localhost/test";
    private String userName = "root";
    private String pass = "root";
    private Connection con;
    
    public DAO(){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(url, userName, pass);
        } catch (SQLException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public boolean checkUser(User user){
        try {
            Statement sm = con.createStatement();
            ResultSet rs = sm.executeQuery("select * from users where userName='"+user.getUserName()+"' and password='"+user.getPassword()+"'");
            if(rs.next()) return true;
        } catch (SQLException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
