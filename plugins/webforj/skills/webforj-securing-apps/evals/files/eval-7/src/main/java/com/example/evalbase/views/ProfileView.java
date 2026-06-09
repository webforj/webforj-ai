package com.example.evalbase.views;

import com.webforj.component.Composite;
import com.webforj.component.button.Button;
import com.webforj.component.html.elements.Div;
import com.webforj.component.html.elements.H2;
import com.webforj.component.html.elements.Paragraph;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

@Route(value = "profile", outlet = MainLayout.class)
@FrameTitle("Profile")
public class ProfileView extends Composite<Div> {
  private final Div self = getBoundComponent();

  public ProfileView() {
    H2 heading = new H2("Welcome");
    Paragraph greeting = new Paragraph("Your dashboard.");

    self.add(heading, greeting);

    Button adminPanel = new Button("Admin Panel");
    adminPanel.addClickListener(e -> {
      // open admin panel
    });
    self.add(adminPanel);
  }
}
