package com.example.evalbase.views;

import java.util.Set;

import com.example.evalbase.components.DrawerHeader;

import com.webforj.component.Component;
import com.webforj.component.Composite;
import com.webforj.component.html.elements.H1;
import com.webforj.component.icons.FeatherIcon;
import com.webforj.component.icons.TablerIcon;
import com.webforj.component.layout.applayout.AppDrawerToggle;
import com.webforj.component.layout.applayout.AppLayout;
import com.webforj.component.layout.appnav.AppNav;
import com.webforj.component.layout.appnav.AppNavItem;
import com.webforj.component.layout.toolbar.Toolbar;
import com.webforj.dispatcher.ListenerRegistration;
import com.webforj.router.Router;
import com.webforj.router.annotation.FrameTitle;
import com.webforj.router.annotation.Route;
import com.webforj.router.event.NavigateEvent;

@Route
public class MainLayout extends Composite<AppLayout> {
  private AppLayout self = getBoundComponent();
  private H1 title = new H1();
  private ListenerRegistration<NavigateEvent> navigateRegistration;

  public MainLayout() {
    setHeader();
    setDrawer();
    navigateRegistration = Router.getCurrent().onNavigate(this::onNavigate);
  }

  private void setHeader() {
    self.setDrawerHeaderVisible(true);

    self.addToDrawerTitle(new DrawerHeader());

    Toolbar toolbar = new Toolbar();
    toolbar.addToStart(new AppDrawerToggle());
    toolbar.addToTitle(title);

    self.addToHeader(toolbar);
  }

  private void setDrawer() {

    AppNav appNav = new AppNav();
    appNav.addItem(new AppNavItem("Inbox", InboxView.class, TablerIcon.create("inbox")));
    appNav.addItem(new AppNavItem("Outbox", OutboxView.class, TablerIcon.create("send-2")));
    appNav.addItem(new AppNavItem("Favorites", FavoritesView.class, TablerIcon.create("heart")));
    appNav.addItem(new AppNavItem("Archived", ArchivedView.class, TablerIcon.create("archive")));
    appNav.addItem(new AppNavItem("Trash", TrashView.class, TablerIcon.create("trash")));
    appNav.addItem(new AppNavItem("Spam", SpamView.class, TablerIcon.create("alert-hexagon")));

    self.addToDrawer(appNav);
  }

  @Override
  protected void onDidDestroy() {
    if (navigateRegistration != null) {
      navigateRegistration.remove();
    }
  }

  private void onNavigate(NavigateEvent ev) {
    Set<Component> components = ev.getContext().getAllComponents();
    Component view = components.stream().filter(c -> c.getClass().getSimpleName().endsWith("View")).findFirst()
        .orElse(null);

    if (view != null) {
      FrameTitle frameTitle = view.getClass().getAnnotation(FrameTitle.class);
      title.setText(frameTitle != null ? frameTitle.value() : "");
    }
  }
}
