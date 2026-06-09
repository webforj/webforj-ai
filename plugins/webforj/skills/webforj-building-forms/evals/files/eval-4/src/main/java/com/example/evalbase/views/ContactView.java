package com.example.evalbase.views;

import com.example.evalbase.models.Contact;
import com.webforj.component.Composite;
import com.webforj.component.button.Button;
import com.webforj.component.field.TextField;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.data.binding.BindingContext;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

@Route(value = "contact", outlet = MainLayout.class)
@FrameTitle("Contact")
public class ContactView extends Composite<FlexLayout> {

  private final TextField name = new TextField("Name");
  private final TextField email = new TextField("Email");
  private final Button submit = new Button("Save");

  private final BindingContext<Contact> context = BindingContext.of(this, Contact.class, true);

  public ContactView() {
    FlexLayout root = getBoundComponent();

    root.add(name, email, submit);
  }
}
