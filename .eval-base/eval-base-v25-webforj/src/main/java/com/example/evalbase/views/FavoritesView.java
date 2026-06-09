package com.example.evalbase.views;

import com.example.evalbase.components.Explore;

import com.webforj.component.Composite;
import com.webforj.component.layout.flexlayout.FlexAlignment;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

@Route(value = "/favorites", outlet = MainLayout.class)
@FrameTitle("Favorites")
public class FavoritesView extends Composite<FlexLayout> {
  private FlexLayout self = getBoundComponent();

  public FavoritesView() {
    self.setHeight("100%");
    self.setAlignment(FlexAlignment.CENTER);
    self.add(new Explore("Favorites"));
  }
}
