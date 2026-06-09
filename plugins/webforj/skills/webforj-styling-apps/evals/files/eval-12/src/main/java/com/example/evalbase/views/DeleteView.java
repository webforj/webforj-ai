package com.example.evalbase.views;

import com.webforj.component.Composite;
import com.webforj.component.button.Button;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

@Route(value = "delete", outlet = MainLayout.class)
@FrameTitle("Delete Account")
public class DeleteView extends Composite<FlexLayout> {

  public DeleteView() {
    FlexLayout root = getBoundComponent();

    Button delete = new Button("Delete");
    delete.addClickListener(e -> {
      // delete account logic
    });

    root.add(delete);
  }
}
