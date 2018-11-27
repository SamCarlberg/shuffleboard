package edu.wpi.first.shuffleboard.app.plugin;

import edu.wpi.first.shuffleboard.api.plugin.Plugin;

public interface PluginFactory {
  Plugin create(Class<? extends Plugin> type);
}
