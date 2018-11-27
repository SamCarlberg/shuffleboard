package edu.wpi.first.shuffleboard.app.tab;

import edu.wpi.first.shuffleboard.api.tab.TabInfo;
import edu.wpi.first.shuffleboard.api.util.Registry;

import com.google.inject.Inject;

import java.util.Objects;

public final class TabInfoRegistry extends Registry<TabInfo> {

  @Inject
  private static TabInfoRegistry defaultInstance;

  public static TabInfoRegistry getDefault() {
    return defaultInstance;
  }

  @Override
  public void register(TabInfo item) {
    Objects.requireNonNull(item, "item");
    if (isRegistered(item)) {
      throw new IllegalArgumentException("Item is already registered: " + item);
    }
    addItem(item);
  }

  @Override
  public void unregister(TabInfo item) {
    removeItem(item);
  }

}
