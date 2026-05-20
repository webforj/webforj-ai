package com.example.evalbase.models;

import java.time.LocalDate;

public class Invoice {

  private String invoiceNumber;
  private String customer;
  private double amount;
  private LocalDate dueDate;
  private boolean paid;

  public Invoice() {}

  public Invoice(String invoiceNumber, String customer, double amount, LocalDate dueDate, boolean paid) {
    this.invoiceNumber = invoiceNumber;
    this.customer = customer;
    this.amount = amount;
    this.dueDate = dueDate;
    this.paid = paid;
  }

  public boolean isOverdue() {
    return !paid && dueDate != null && dueDate.isBefore(LocalDate.now());
  }

  public String getInvoiceNumber() { return invoiceNumber; }
  public void setInvoiceNumber(String v) { this.invoiceNumber = v; }
  public String getCustomer() { return customer; }
  public void setCustomer(String v) { this.customer = v; }
  public double getAmount() { return amount; }
  public void setAmount(double v) { this.amount = v; }
  public LocalDate getDueDate() { return dueDate; }
  public void setDueDate(LocalDate v) { this.dueDate = v; }
  public boolean isPaid() { return paid; }
  public void setPaid(boolean v) { this.paid = v; }
}
