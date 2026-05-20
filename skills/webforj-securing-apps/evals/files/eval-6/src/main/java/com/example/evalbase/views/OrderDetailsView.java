package com.example.evalbase.views;

import com.webforj.component.Composite;
import com.webforj.component.html.elements.Div;
import com.webforj.component.html.elements.H2;
import com.webforj.component.html.elements.Paragraph;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

import jakarta.annotation.security.RolesAllowed;

@Route(value = "/users/:userId/edit", outlet = MainLayout.class)
@FrameTitle("Edit Profile")
@RolesAllowed("USER")
public class OrderDetailsView extends Composite<Div> {
  private final Div self = getBoundComponent();

  private String userId;

  public OrderDetailsView() {
    self.add(new H2("Edit Profile"));
    self.add(new Paragraph("Edit your account details."));
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
}
