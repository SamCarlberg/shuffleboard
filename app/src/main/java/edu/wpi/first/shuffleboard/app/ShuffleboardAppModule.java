package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.app.plugin.PluginCache;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;
import edu.wpi.first.shuffleboard.app.tab.TabInfoRegistry;

import com.google.inject.AbstractModule;

import java.io.IOException;

public class ShuffleboardAppModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(PluginCache.class).toProvider(() -> {
      try {
        return new PluginCache(Storage.getPluginCache());
      } catch (IOException e) {
        return new PluginCache(null);
      }
    });

    requestStaticInjection(
        PluginLoader.class,
        PluginCache.class,
        TabInfoRegistry.class,
        AppPreferences.class
    );
  }
}
