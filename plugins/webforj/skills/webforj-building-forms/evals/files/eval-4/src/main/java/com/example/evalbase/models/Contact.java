package com.example.evalbase.models;

public class Contact {

  private String name;
  private String email;
  private String phone;

  public Contact() {}

  public Contact(String name, String email, String phone) {
    this.name = name;
    this.email = email;
    this.phone = phone;
  }

  public String getName() { return name; }
  public void setName(String v) { this.name = v; }
  public String getEmail() { return email; }
  public void setEmail(String v) { this.email = v; }
  public String getPhone() { return phone; }
  public void setPhone(String v) { this.phone = v; }
}
