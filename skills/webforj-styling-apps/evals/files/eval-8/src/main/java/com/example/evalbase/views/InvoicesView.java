package com.example.evalbase.views;

import com.example.evalbase.models.Invoice;
import com.webforj.component.Composite;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.component.table.Table;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

import java.time.LocalDate;
import java.util.List;

@Route(value = "invoices", outlet = MainLayout.class)
@FrameTitle("Invoices")
public class InvoicesView extends Composite<FlexLayout> {

  private final Table<Invoice> table = new Table<>();

  public InvoicesView() {
    FlexLayout root = getBoundComponent();

    table.addColumn("Invoice #", Invoice::getInvoiceNumber);
    table.addColumn("Customer", Invoice::getCustomer);
    table.addColumn("Amount", inv -> String.format("$%.2f", inv.getAmount()));
    table.addColumn("Due", inv -> inv.getDueDate() == null ? "" : inv.getDueDate().toString());
    table.addColumn("Paid", inv -> inv.isPaid() ? "Yes" : "No");

    table.setItems(List.of(
        new Invoice("INV-001", "Acme Co.", 1250.00, LocalDate.now().minusDays(15), false),
        new Invoice("INV-002", "Beta LLC", 850.50, LocalDate.now().plusDays(5), false),
        new Invoice("INV-003", "Gamma Inc.", 2400.00, LocalDate.now().minusDays(45), false),
        new Invoice("INV-004", "Delta Corp.", 600.00, LocalDate.now().plusDays(10), true),
        new Invoice("INV-005", "Epsilon Ltd.", 3200.75, LocalDate.now().minusDays(2), false)
    ));

    root.add(table);
  }

  public Table<Invoice> getTable() {
    return table;
  }
}
