package com.example.evalbase.views;

import com.webforj.component.Composite;
import com.webforj.component.login.Login;
import com.webforj.router.annotation.Route;

import com.webforj.router.security.annotation.AnonymousAccess;

@Route("/login")
@AnonymousAccess
public class LoginView extends Composite<Login> {
  private final Login self = getBoundComponent();

  public LoginView() {
    // TODO: handle login submission
    self.whenAttached().thenAccept(c -> self.open());
  }
}
