package com.example.evalbase.views;

import com.example.evalbase.models.Product;
import com.webforj.component.Composite;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.component.table.Table;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

import java.util.List;

@Route(value = "products", outlet = MainLayout.class)
@FrameTitle("Products")
public class ProductsView extends Composite<FlexLayout> {

  private final Table<Product> table = new Table<>();

  public ProductsView() {
    FlexLayout root = getBoundComponent();

    table.addColumn("Name", Product::getName);
    table.addColumn("price", Product::getPrice);

    table.setItems(List.of(
        new Product("Widget", 1234567.89),
        new Product("Gadget", 24.50),
        new Product("Sprocket", 999.00)
    ));

    root.add(table);
  }

  public Table<Product> getTable() {
    return table;
  }
}
