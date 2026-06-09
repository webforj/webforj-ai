package com.example.evalbase.views;

import java.util.List;
import java.util.Locale;

import com.webforj.App;
import com.webforj.component.Composite;
import com.webforj.component.button.Button;
import com.webforj.component.html.elements.H1;
import com.webforj.component.html.elements.Paragraph;
import com.webforj.component.layout.flexlayout.FlexLayout;
import com.webforj.component.list.ComboBox;
import com.webforj.component.list.ListItem;
import com.webforj.concern.HasTranslation;
import com.webforj.i18n.LocaleObserver;
import com.webforj.i18n.event.LocaleEvent;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;

@Route(value = "welcome", outlet = MainLayout.class)
@FrameTitle("Welcome")
public class WelcomeView extends Composite<FlexLayout>
    implements HasTranslation, LocaleObserver {

  private final FlexLayout self = getBoundComponent();
  private final H1 title = new H1();
  private final Paragraph subtitle = new Paragraph();
  private final Button save = new Button();
  private final Button cancel = new Button();
  private final ComboBox languageSwitcher = new ComboBox();

  public WelcomeView() {
    languageSwitcher.insert(List.of(
        new ListItem("en", "English"),
        new ListItem("de", "Deutsch")));
    languageSwitcher.onSelect(e -> {
      ListItem selected = e.getSelectedItem();
      if (selected != null) {
        App.setLocale(Locale.forLanguageTag(selected.getKey().toString()));
      }
    });

    title.setText(t("welcome.title"));
    subtitle.setText(t("welcome.subtitle"));
    save.setText(t("button.save"));
    cancel.setText(t("button.cancel"));

    self.add(languageSwitcher, title, subtitle, save, cancel);
  }

  @Override
  public void onLocaleChange(LocaleEvent event) {
    title.setText(t("welcome.title"));
    subtitle.setText(t("welcome.subtitle"));
    save.setText(t("button.save"));
    cancel.setText(t("button.cancel"));
  }
}
