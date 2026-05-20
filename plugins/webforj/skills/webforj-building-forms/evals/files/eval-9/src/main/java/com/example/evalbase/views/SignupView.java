package com.example.evalbase.views;

import com.example.evalbase.models.User;
import com.webforj.component.Composite;
import com.webforj.component.button.Button;
import com.webforj.component.field.PasswordField;
import com.webforj.component.field.TextField;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.data.binding.BindingContext;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

@Route(value = "signup", outlet = MainLayout.class)
@FrameTitle("Sign Up")
public class SignupView extends Composite<FlexLayout> {

  private final TextField nameField = new TextField("Name");
  private final PasswordField passwordField = new PasswordField("Password");
  private final Button submit = new Button("Sign Up");

  private final BindingContext<User> context = new BindingContext<>(User.class);

  public SignupView() {
    FlexLayout root = getBoundComponent();

    context.bind(nameField, "name").add();

    root.add(nameField, passwordField, submit);
  }
}
