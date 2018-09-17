package edu.wpi.first.shuffleboard.testplugins;

import edu.wpi.first.shuffleboard.api.plugin.Plugin;

import edu.wpi.first.desktop.plugin.Description;
import edu.wpi.first.desktop.plugin.Requires;

/**
 * A plugin that depends on another plugin.
 */
@Description(groupId = "edu.wpi.first.shuffleboard", name = "DependentPlugin", version = "1.0.0", summary = "")
@Requires(groupId = "edu.wpi.first.shuffleboard", name = "BasicPlugin", minVersion = "1.0.0")
public final class DependentPlugin extends Plugin {
}
