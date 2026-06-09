package com.example.evalbase.views;

import com.webforj.component.Composite;
import com.webforj.component.html.elements.Div;
import com.webforj.component.html.elements.H2;
import com.webforj.component.html.elements.Paragraph;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

@Route(value = "reports/advanced", outlet = MainLayout.class)
@FrameTitle("Advanced Reports")
public class ReportsView extends Composite<Div> {
  private final Div self = getBoundComponent();

  public ReportsView() {
    self.add(new H2("Advanced Reports"));
    self.add(new Paragraph("Detailed analytics and reporting."));
  }
}
