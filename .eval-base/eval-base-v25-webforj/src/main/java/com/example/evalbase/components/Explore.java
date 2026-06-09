package com.example.evalbase.components;

import com.webforj.component.Composite;
import com.webforj.component.html.elements.Anchor;
import com.webforj.component.html.elements.H3;
import com.webforj.component.html.elements.Img;
import com.webforj.component.layout.flexlayout.FlexAlignment;
import com.webforj.component.layout.flexlayout.FlexDirection;
import com.webforj.component.layout.flexlayout.FlexLayout;

public class Explore extends Composite<FlexLayout> {
  private FlexLayout self = getBoundComponent();

  public Explore(String title) {
    self.addClassName("explore-component");
    self.setStyle("margin", "1em auto");
    self.setDirection(FlexDirection.COLUMN);
    self.setAlignment(FlexAlignment.CENTER);
    self.setMaxWidth(300);
    self.setSpacing(".3em");

    Img img = new Img(String.format("ws://explore/%s.svg", title), "mailbox");
    img.setMaxWidth(250);

    Anchor anchor = new Anchor("https://docs.webforj.com/docs/components/overview", "Explore UI Components");
    anchor.setTarget("_blank");

    self.add(img, new H3(title), anchor);
  }
}
