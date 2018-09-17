package edu.wpi.first.shuffleboard.api.plugin;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.sources.recording.Converters;
import edu.wpi.first.shuffleboard.api.theme.Themes;
import edu.wpi.first.shuffleboard.api.widget.Components;

public final class ShuffleboardContext {

  private final Components components;
  private final Converters converters;
  private final DataTypes dataTypes;
  private final SourceTypes sourceTypes;
  private final Themes themes;

  /**
   * Creates a shuffleboard context container.
   */
  public ShuffleboardContext(Components components,
                             Converters converters,
                             DataTypes dataTypes,
                             SourceTypes sourceTypes,
                             Themes themes) {
    this.components = components;
    this.converters = converters;
    this.dataTypes = dataTypes;
    this.sourceTypes = sourceTypes;
    this.themes = themes;
  }

  public Components getComponents() {
    return components;
  }

  public Converters getConverters() {
    return converters;
  }

  public DataTypes getDataTypes() {
    return dataTypes;
  }

  public SourceTypes getSourceTypes() {
    return sourceTypes;
  }

  public Themes getThemes() {
    return themes;
  }
}
