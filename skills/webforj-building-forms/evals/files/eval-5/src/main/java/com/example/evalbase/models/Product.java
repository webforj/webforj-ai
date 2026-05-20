package com.example.evalbase.models;

public class Product {

  private String name;
  private double price;

  public Product() {}

  public Product(String name, double price) {
    this.name = name;
    this.price = price;
  }

  public String getName() { return name; }
  public void setName(String v) { this.name = v; }
  public double getPrice() { return price; }
  public void setPrice(double v) { this.price = v; }
}
