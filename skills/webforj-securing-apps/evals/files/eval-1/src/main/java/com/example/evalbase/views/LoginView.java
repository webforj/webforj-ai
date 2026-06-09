package com.example.evalbase.views;

import com.webforj.component.Composite;
import com.webforj.component.login.Login;
import com.webforj.router.annotation.Route;

@Route("/login")
public class LoginView extends Composite<Login> {
  private final Login self = getBoundComponent();

  public LoginView() {
    whenAttached().thenAccept(c -> self.open());
  }
}
