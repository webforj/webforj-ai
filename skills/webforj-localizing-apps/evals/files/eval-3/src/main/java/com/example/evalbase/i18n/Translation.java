package com.example.evalbase.i18n;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "translations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"message_key", "locale"}))
public class Translation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "message_key", nullable = false)
  private String key;

  @Column(name = "locale", nullable = false, length = 16)
  private String locale;

  @Column(name = "message_value", nullable = false, length = 2000)
  private String value;

  protected Translation() {
  }

  public Translation(String key, String locale, String value) {
    this.key = key;
    this.locale = locale;
    this.value = value;
  }

  public Long getId() {
    return id;
  }

  public String getKey() {
    return key;
  }

  public String getLocale() {
    return locale;
  }

  public String getValue() {
    return value;
  }
}
