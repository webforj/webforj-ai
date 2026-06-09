package com.example.evalbase.views;

import com.webforj.component.Composite;
import com.webforj.component.button.Button;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.component.field.TextField;
import com.webforj.component.list.ComboBox;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

@Route(value = "search", outlet = MainLayout.class)
@FrameTitle("Search")
public class SearchView extends Composite<FlexLayout> {

  private final TextField query = new TextField("Query");
  private final ComboBox<String> category = new ComboBox<>("Category");
  private final Button submit = new Button("Search");

  public SearchView() {
    FlexLayout root = getBoundComponent();

    category.insert("All", "Products", "Customers", "Invoices");

    root.add(query, category, submit);
  }
}
