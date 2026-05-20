package com.example.evalbase.models;

public class Order {

  private String orderNumber;
  private String orderDate;
  private double total;

  public Order() {}

  public Order(String orderNumber, String orderDate, double total) {
    this.orderNumber = orderNumber;
    this.orderDate = orderDate;
    this.total = total;
  }

  public String getOrderNumber() { return orderNumber; }
  public void setOrderNumber(String v) { this.orderNumber = v; }
  public String getOrderDate() { return orderDate; }
  public void setOrderDate(String v) { this.orderDate = v; }
  public double getTotal() { return total; }
  public void setTotal(double v) { this.total = v; }
}
