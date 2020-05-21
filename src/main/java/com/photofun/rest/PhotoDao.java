package com.photofun.rest;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class PhotoDao {
  
  static String consumerKey = null;
  static String consumerSecret = null;
  static String accessToken = null;
  static String accessTokenSecret = null;
  
  public Photo findById(int id) {
    String sql = buildGetByIdQuery(id);
    return findOne(sql);
  }

  public Photo findByName(String photoName) {
    String sql = buildGetByNameQuery(photoName);
    return findOne(sql);
  }

  public Photo save(Photo photo) {
    String sql = buildSaveQuery(photo);
    runExecuteUpdateQuery(sql);
    return photo;
  }

  public Photo update(Photo photo) {
    String sql = buildUpdateQuery(photo.getName(), photo.getUrl(),
        photo.getId());
    runExecuteUpdateQuery(sql);
    return photo;
  }

  public Photo delete(Photo photo) {
    String sql = buildDeleteQuery(photo.getId());
    runExecuteUpdateQuery(sql);
    return photo;
  }

  private int runExecuteUpdateQuery(String sql) {
    Connection c = null;
    int recordsUpdated = 0;
    try {
      c = DBConnection.getConnection();
      Statement s = c.createStatement();
      recordsUpdated = s.executeUpdate(sql);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      DBConnection.close(c);
    }
    return recordsUpdated;
  }
  
  public Photo getLastPhoto(){
    String sql = buildGetLastPhotoQuery();
    return findOne(sql);
  }
  
  private Photo findOne(String sql) {
    Connection c = null;
    Photo photo = null;
    try {
      c = DBConnection.getConnection();
      Statement s = c.createStatement();
      ResultSet rs = s.executeQuery(sql);
      while (rs.next()) {
        photo = processRow(rs);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      DBConnection.close(c);
    }

    return photo;
  }
  
  private int countPhotos() {
    Connection c = null;
    int count = 0;
    try {
      c = DBConnection.getConnection();
      Statement s = c.createStatement();
      ResultSet rs = s.executeQuery("SELECT COUNT(*) AS count FROM photos");
      while (rs.next()) {
        count = rs.getInt("count");
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      DBConnection.close(c);
    }

    return count;
  }

  public List<Photo> findN(int from, int to) {
    List<Photo> list = new ArrayList<Photo>();
    int count = countPhotos();
    if(from >= count){
      from = 0;
    }
    Connection c = null;
    String sql = "SELECT * FROM photos ORDER BY id DESC LIMIT " + from + ", " + to;
    try {
      c = DBConnection.getConnection();
      Statement s = c.createStatement();
      ResultSet rs = s.executeQuery(sql);
      while (rs.next()) {
        list.add(processRow(rs));
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } finally {
      DBConnection.close(c);
    }
    return list;
  }
  
  public Photo sharePhoto(String photoUrl, String statusMessage){
    Photo sharedPhoto = null;
    try{
      
      if(consumerKey == null){
        
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        consumerKey = bundle.getString("twitter.consumerKey");
        consumerSecret = bundle.getString("twitter.consumerSecret");
        accessToken = bundle.getString("twitter.accessToken");
        accessTokenSecret = bundle.getString("twitter.accessTokenSecret");
        
      }
      
      ConfigurationBuilder twitterConfigBuilder = new ConfigurationBuilder();
      twitterConfigBuilder.setDebugEnabled(true);
      twitterConfigBuilder.setOAuthConsumerKey(consumerKey);
      twitterConfigBuilder.setOAuthConsumerSecret(consumerSecret);
      twitterConfigBuilder.setOAuthAccessToken(accessToken);
      twitterConfigBuilder.setOAuthAccessTokenSecret(accessTokenSecret);
  
      Twitter twitter = new TwitterFactory(twitterConfigBuilder.build()).getInstance();
      //////
      URL fileUrl = new URL(photoUrl); 
      String tDir = System.getProperty("catalina.base")
          + File.separator + "webapps" + File.separator + "photofun" + File.separator;
      String ext = FilenameUtils.getExtension(photoUrl);
      String path = tDir + "tmp" + ext; 
      File file = new File(path); 
      file.deleteOnExit(); 
      FileUtils.copyURLToFile(fileUrl, file); 
  
      StatusUpdate status = new StatusUpdate(statusMessage);
      status.setMedia(file); // set the image to be uploaded here.
      twitter.updateStatus(status);
      
      sharedPhoto =  new Photo("shared photo", photoUrl);
      sharedPhoto.setDescription(statusMessage);
    } catch(Exception e){
      e.printStackTrace();
      sharedPhoto = new Photo(null, null);
    }
    
    return sharedPhoto;
  }
  public Photo processRow(ResultSet rs) throws SQLException {
    Photo photo = new Photo(rs.getString("name"), rs.getString("path"));
    photo.setDescription(rs.getString("description"));
    photo.setId(rs.getInt("id"));
    return photo;
  }

  public static String buildSaveQuery(Photo photo) {
    return "INSERT INTO photos (name, path, description) VALUES('" + photo.getName()
        + "', '" + photo.getUrl() + "', '"+photo.getDescription()+"')";
  }

  public static String buildDeleteQuery(int photoId) {
    return "DELETE FROM photos WHERE id=" + photoId;
  }

  public static String buildUpdateQuery(String photoName, String photoPath, int photoId) {
    return "UPDATE photos SET name='" + photoName + "', path='" + photoPath
        + "' WHERE id=" + photoId;
  }
  
  public static String buildGetLastPhotoQuery(){
    return "SELECT * FROM photos ORDER BY id DESC LIMIT 1";
  }
  public static String buildGetByIdQuery(int photoId) {
    return "SELECT * FROM photos WHERE id=" + photoId + " LIMIT 1";
  }

  public static String buildGetByNameQuery(String photoName) {
    return "SELECT * FROM photos WHERE name='" + photoName
        + "' LIMIT 1";
  }
}
