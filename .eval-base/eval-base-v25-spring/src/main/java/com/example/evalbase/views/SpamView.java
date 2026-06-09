package com.example.evalbase.views;

import com.example.evalbase.components.Explore;

import com.webforj.component.Composite;
import com.webforj.component.layout.flexlayout.FlexAlignment;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

@Route(value = "/spam", outlet = MainLayout.class)
@FrameTitle("Spam")
public class SpamView extends Composite<FlexLayout> {
  private FlexLayout self = getBoundComponent();

  public SpamView() {
    self.setHeight("100%");
    self.setAlignment(FlexAlignment.CENTER);
    self.add(new Explore("Spam"));
  }
}
