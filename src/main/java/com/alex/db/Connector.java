/**
 * Very simple database connector
 */
package com.alex.db;

import java.sql.*;

public class Connector {
  // is the driver class loaded?
  private static boolean driverLoaded = false;
  
  /**
   * get a Connection to the database
   * @return Connection object
   * @throws Exception if any error occurs
   */
  public synchronized static Connection getConnection() throws Exception  {
    
    if  ( !driverLoaded )  {
      try  {
        Class.forName("org.postgresql.Driver");
        driverLoaded = true;
      }  catch (ClassNotFoundException e)  {
        throw new Exception("Cannot initialize postgres jdbc driver");
      }
    }
    
    String user = "postgres";
    String passw = "postgres";
    String url = "jdbc:postgresql://localhost:5432/fgir_db";
    Connection connection = DriverManager.getConnection(url, user, passw);
    connection.setAutoCommit(false);
    return  connection;
  }
}
