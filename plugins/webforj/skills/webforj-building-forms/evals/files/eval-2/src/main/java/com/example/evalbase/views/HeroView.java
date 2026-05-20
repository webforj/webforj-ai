package com.example.evalbase.views;

import com.webforj.component.Composite;
import com.webforj.component.button.Button;
import com.webforj.component.field.TextField;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.component.list.ComboBox;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

@Route(value = "hero", outlet = MainLayout.class)
@FrameTitle("Hero")
public class HeroView extends Composite<FlexLayout> {

  private final TextField nameField = new TextField("Name");
  private final ComboBox power = new ComboBox("Power");
  private final Button submit = new Button("Submit");

  public HeroView() {
    FlexLayout root = getBoundComponent();

    power.insert("Flight", "Invisibility", "Super Strength", "Telepathy");

    root.add(nameField, power, submit);
  }
}
