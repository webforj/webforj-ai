package com.example.evalbase.models;

public class Hero {

  private String name;
  private String power;

  public Hero() {}

  public Hero(String name, String power) {
    this.name = name;
    this.power = power;
  }

  public String getName() { return name; }
  public void setName(String v) { this.name = v; }
  public String getPower() { return power; }
  public void setPower(String v) { this.power = v; }
}
