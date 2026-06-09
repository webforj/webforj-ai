package com.example.evalbase.views;

import com.example.evalbase.services.ReportService;
import com.webforj.component.Composite;
import com.webforj.component.button.Button;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.component.progressbar.ProgressBar;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "report", outlet = MainLayout.class)
@FrameTitle("Report")
public class ReportView extends Composite<FlexLayout> {

  private final ProgressBar progress = new ProgressBar(0, 0, 100);
  private final Button start = new Button("Start");

  private final ReportService reportService;

  @Autowired
  public ReportView(ReportService reportService) {
    this.reportService = reportService;

    FlexLayout root = getBoundComponent();

    start.addClickListener(e -> startReport());

    root.add(progress, start);
  }

  private void startReport() {
    // TODO: invoke the async ReportService and update `progress` as it runs.
  }
}
