package com.example.evalbase.views;

import com.webforj.component.Composite;
import com.webforj.component.html.elements.Div;
import com.webforj.component.html.elements.H2;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

@Route(value = "billing", outlet = MainLayout.class)
@FrameTitle("Billing")
public class BillingView extends Composite<Div> {
  private final Div self = getBoundComponent();

  public BillingView() {
    self.add(new H2("Billing"));
  }
}
