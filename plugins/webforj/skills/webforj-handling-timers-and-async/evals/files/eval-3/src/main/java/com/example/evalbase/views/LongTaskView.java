package com.example.evalbase.views;

import com.webforj.component.Composite;
import com.webforj.component.button.Button;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.component.progressbar.ProgressBar;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

@Route(value = "long-task", outlet = MainLayout.class)
@FrameTitle("Long Task")
public class LongTaskView extends Composite<FlexLayout> {

  private final ProgressBar progress = new ProgressBar(0, 0, 100);
  private final Button start = new Button("Start");

  public LongTaskView() {
    FlexLayout root = getBoundComponent();

    start.addClickListener(e -> runLongTask());

    root.add(progress, start);
  }

  private void runLongTask() {
    // TODO: run the ~10s CPU-bound calculation off the UI thread
    // and update `progress` from 0 to 100 as it advances.
  }
}
