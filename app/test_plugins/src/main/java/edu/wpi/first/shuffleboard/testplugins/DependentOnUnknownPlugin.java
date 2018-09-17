package edu.wpi.first.shuffleboard.testplugins;

import edu.wpi.first.shuffleboard.api.plugin.Plugin;

import edu.wpi.first.desktop.plugin.Description;
import edu.wpi.first.desktop.plugin.Requires;

/**
 * A plugin that has a dependency on another, unknown, plugin.
 */
@Description(groupId = "edu.wpi.first.shuffleboard", name = "DependentOnUnknownPlugin", version = "1.0.0", summary = "")
@Requires(groupId = "???", name = "???", minVersion = "0.0.0")
public final class DependentOnUnknownPlugin extends Plugin {

}
