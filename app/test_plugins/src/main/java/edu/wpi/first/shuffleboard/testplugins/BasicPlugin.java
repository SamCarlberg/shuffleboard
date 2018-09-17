package edu.wpi.first.shuffleboard.testplugins;

import edu.wpi.first.shuffleboard.api.plugin.Plugin;

import edu.wpi.first.desktop.plugin.Description;

/**
 * A basic plugin with no dependencies.
 */
@Description(
    groupId = "edu.wpi.first.shuffleboard",
    name = "BasicPlugin",
    version = "1.0.0",
    summary = "A basic plugin for testing"
)
public final class BasicPlugin extends Plugin {

}
