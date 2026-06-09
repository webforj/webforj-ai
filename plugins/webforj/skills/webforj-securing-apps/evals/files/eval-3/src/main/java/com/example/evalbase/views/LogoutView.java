package com.example.evalbase.views;

import com.webforj.component.Composite;
import com.webforj.component.html.elements.Div;
import com.webforj.component.icons.FeatherIcon;
import com.webforj.component.icons.IconButton;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

@Route(value = "logout", outlet = MainLayout.class)
@FrameTitle("Logout")
public class LogoutView extends Composite<Div> {
  private final Div self = getBoundComponent();

  public LogoutView() {
    IconButton logout = new IconButton(FeatherIcon.LOG_OUT.create());
    logout.onClick(e -> {
      // TODO: trigger logout
    });

    self.add(logout);
  }
}
