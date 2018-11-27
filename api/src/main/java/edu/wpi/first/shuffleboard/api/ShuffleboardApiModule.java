package edu.wpi.first.shuffleboard.api;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.sources.Sources;
import edu.wpi.first.shuffleboard.api.sources.recording.Converters;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.theme.Themes;
import edu.wpi.first.shuffleboard.api.widget.Components;

import com.google.inject.AbstractModule;

/**
 * Guice module for bindings to Shuffleboard APIs.
 */
public final class ShuffleboardApiModule extends AbstractModule {
  @Override
  protected void configure() {
    // Registries
    bind(DataTypes.class).asEagerSingleton();
    bind(Themes.class).toInstance(Themes.createDefault());
    bind(Components.class).asEagerSingleton();
    bind(SourceTypes.class).asEagerSingleton();
    bind(Sources.class).asEagerSingleton();
    bind(Converters.class).asEagerSingleton();

    bind(Recorder.class).asEagerSingleton();

    requestStaticInjection(
        DataTypes.class,
        Themes.class,
        Components.class,
        SourceTypes.class,
        Sources.class,
        Converters.class,
        Recorder.class
    );
  }
}
