package com.example.evalbase.models;

public class User {

  private String name;
  private String password;

  public User() {}

  public User(String name, String password) {
    this.name = name;
    this.password = password;
  }

  public String getName() { return name; }
  public void setName(String v) { this.name = v; }
  public String getPassword() { return password; }
  public void setPassword(String v) { this.password = v; }
}
