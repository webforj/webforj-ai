package com.example.evalbase.views;

import com.webforj.component.Composite;
import com.webforj.component.html.elements.Paragraph;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

@Route(value = "stock", outlet = MainLayout.class)
@FrameTitle("Stock")
public class StockView extends Composite<FlexLayout> {

  private final Paragraph quote = new Paragraph("--");

  public StockView() {
    FlexLayout root = getBoundComponent();
    root.add(quote);
  }

  private void refreshQuote() {
    // TODO: fetch the latest quote and update the paragraph
  }
}
