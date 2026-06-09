package com.example.evalbase.views;

import com.webforj.component.Composite;
import com.webforj.component.html.elements.Div;
import com.webforj.component.html.elements.Paragraph;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

@Route(value = "about", outlet = MainLayout.class)
@FrameTitle("About")
public class AboutView extends Composite<Div> {
  private final Div self = getBoundComponent();

  public AboutView() {
    self.add(new Paragraph("About this application."));
  }
}
