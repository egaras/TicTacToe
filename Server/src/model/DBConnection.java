/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.sql.Driver;

import java.sql.*;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ehab
 */
public class DBConnection {
    Connection con;
    ResultSet rs;
    public Connection Connection(){
    
          try {
              rs=null;
              DriverManager.registerDriver(new com.mysql.jdbc.Driver());
              con = DriverManager.getConnection("jdbc:mysql://localhost/TicTacToeDB", "root", "root");
          } catch (SQLException ex) {
              Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
          }
   
    return con;
    }
    
    /**
     *
     * @param con
     */
    public void CloseConnection(Connection con){
          try {
              con.close();
          } catch (SQLException ex) {
              Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
          }
    
    }
    
    
}