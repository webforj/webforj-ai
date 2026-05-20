package com.example.evalbase.views;

import com.webforj.component.Composite;
import com.webforj.component.field.TextField;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

@Route(value = "search", outlet = MainLayout.class)
@FrameTitle("Search")
public class SearchView extends Composite<FlexLayout> {

  private final TextField query = new TextField("Search");
  private final FlexLayout results = new FlexLayout();

  public SearchView() {
    FlexLayout root = getBoundComponent();

    query.addModifyListener(e -> {
      // TODO: trigger a debounced search using the current query value
    });

    root.add(query, results);
  }

  private void search(String term) {
    // TODO: run the server query and populate the results panel
  }
}
