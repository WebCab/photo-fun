package com.photofun.rest;

import java.io.Serializable;

public class Photo implements Serializable{
  private static final long serialVersionUID = -1137066479062494878L;
  
  private int id;
  private String name;
  private String url;
  private String description;

  /**
   * 
   * Instantiates Photo
   * 
   * @param name
   * @param path
   */
  public Photo(String name, String url) {
    this.name = name;
    this.url = url;
  }
  
  public Photo(){}
  public Photo(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return "Photo [name=" + name + ", url=" + url + "]";
  }
}
