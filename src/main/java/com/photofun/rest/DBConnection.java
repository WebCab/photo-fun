package com.photofun.rest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DBConnection {
  private String url;
  private String user;
  private String pwd;
  private static DBConnection instance = null;

  public DBConnection() {
    String driver = "com.mysql.jdbc.Driver";
    try {
      url = "jdbc:mysql://localhost/photofun?user=root";
      ResourceBundle bundle = ResourceBundle.getBundle("config");
      driver = bundle.getString("jdbc.driver");
      Class.forName(driver);
      url = bundle.getString("jdbc.url");
      user = bundle.getString("jdbc.user");
      pwd = bundle.getString("jdbc.pwd");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Connection getConnection() throws SQLException {
    if (instance == null) {
      instance = new DBConnection();
    }
    try {
      return DriverManager.getConnection(instance.url, instance.user, instance.pwd);
    } catch (SQLException e) {
      throw e;
    }
  }

  public static void close(Connection connection) {
    try {
      if (connection != null) {
        connection.close();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
