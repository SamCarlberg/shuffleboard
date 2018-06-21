package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.app.components.skin.TimelineSkin;

import javafx.beans.NamedArg;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.Duration;

import static edu.wpi.first.shuffleboard.app.components.Timeline.Importance.LOWEST;

/**
 * A JavaFX control that allows users to move through a timeline and navigate to special events.
 */
public class Timeline extends Control {

  private static final String DEFAULT_STYLECLASS = "timeline";

  private final ObservableList<Marker> markers = FXCollections.observableArrayList();
  private final DoubleProperty progress = new SimpleDoubleProperty(this, "progress", 0);
  private final DoubleProperty start = new SimpleDoubleProperty(this, "start", 0);
  private final DoubleProperty end = new SimpleDoubleProperty(this, "end", 1);
  private final ObjectProperty<Duration> length = new SimpleObjectProperty<>(this, "length", Duration.UNKNOWN);
  private final ObjectProperty<Duration> detailTimeout =
      new SimpleObjectProperty<>(this, "detailTimeout", Duration.INDEFINITE);
  private final DoubleProperty playbackSpeed = new SimpleDoubleProperty(this, "playbackSpeed", 1);
  private final BooleanProperty playing = new SimpleBooleanProperty(this, "playing", false);
  private final BooleanProperty loopPlayback = new SimpleBooleanProperty(this, "loopPlayback", false);
  private final BooleanProperty animated = new SimpleBooleanProperty(this, "animated", false);

  public Timeline() {
    this(0, 1, 0);
  }

  public Timeline(@NamedArg("start") double start,
                  @NamedArg("end") double end) {
    this(start, end, start);
  }

  /**
   * Creates a new timeline control.
   *
   * @param start    the start of the timeline
   * @param end      the end of the timeline
   * @param progress the current progress of the timeline
   */
  public Timeline(@NamedArg("start") double start,
                  @NamedArg("end") double end,
                  @NamedArg("progress") double progress) {
    setStart(start);
    setEnd(end);
    setProgress(progress);
    getStyleClass().add(DEFAULT_STYLECLASS);
  }

  /**
   * Gets the list of markers on this timeline.
   */
  public ObservableList<Marker> getMarkers() {
    return markers;
  }

  /**
   * Gets the current progress of the timeline, in the range
   * {@code [}{@link #getStart() start}{@code , }{@link #getEnd() end}{@code ]}.
   */
  public double getProgress() {
    return progress.get();
  }

  public DoubleProperty progressProperty() {
    return progress;
  }

  /**
   * Sets the current progress of the timeline.
   */
  public void setProgress(double progress) {
    this.progress.set(progress);
  }

  /**
   * Gets the starting point of the timeline.
   */
  public double getStart() {
    return start.get();
  }

  public DoubleProperty startProperty() {
    return start;
  }

  /**
   * Sets the starting point of the timeline.
   */
  public void setStart(double start) {
    this.start.set(start);
  }

  /**
   * Gets the end point of the timeline.
   */
  public double getEnd() {
    return end.get();
  }

  public DoubleProperty endProperty() {
    return end;
  }

  /**
   * Sets the end point of the timeline.
   */
  public void setEnd(double end) {
    this.end.set(end);
  }

  /**
   * Gets the length of time that this timeline spans.
   */
  public Duration getLength() {
    return length.get();
  }

  public ObjectProperty<Duration> lengthProperty() {
    return length;
  }

  /**
   * Sets the length of time that this timeline spans.
   */
  public void setLength(Duration length) {
    this.length.set(length);
  }

  /**
   * Gets the length of time that the detail label is visible for when the timeline is playing.
   *
   * <p>Default value: {@link Duration#INDEFINITE}.</p>
   */
  public Duration getDetailTimeout() {
    return detailTimeout.get();
  }

  public ObjectProperty<Duration> detailTimeoutProperty() {
    return detailTimeout;
  }

  /**
   * Sets the length of time that the detail label is visible for when the timeline is playing.
   */
  public void setDetailTimeout(Duration detailTimeout) {
    this.detailTimeout.set(detailTimeout);
  }

  /**
   * Gets the ratio at which playback should run.
   *
   * <p>Default value: {@code 1}.</p>
   */
  public double getPlaybackSpeed() {
    return playbackSpeed.get();
  }

  public DoubleProperty playbackSpeedProperty() {
    return playbackSpeed;
  }

  /**
   * Sets the ratio at which playback should run. Values greater than {@code 1} will make playback run faster than real
   * life, and values less than {@code 1} will make playback run slower.
   */
  public void setPlaybackSpeed(double playbackSpeed) {
    this.playbackSpeed.set(playbackSpeed);
  }

  /**
   * Checks if the timeline is currently auto-playing.
   */
  public boolean isPlaying() {
    return playing.get();
  }

  public BooleanProperty playingProperty() {
    return playing;
  }

  /**
   * Sets the timeline to automatically play.
   */
  public void setPlaying(boolean playing) {
    this.playing.set(playing);
  }

  /**
   * Checks if playback should repeat from the beginning when it reaches the end of the timeline.
   */
  public boolean isLoopPlayback() {
    return loopPlayback.get();
  }

  public BooleanProperty loopPlaybackProperty() {
    return loopPlayback;
  }

  /**
   * Sets playback to automatically loop back to the beginning of the timeline when it reaches the end.
   */
  public void setLoopPlayback(boolean loopPlayback) {
    this.loopPlayback.set(loopPlayback);
  }

  public boolean isAnimated() {
    return animated.get();
  }

  public BooleanProperty animatedProperty() {
    return animated;
  }

  public void setAnimated(boolean animated) {
    this.animated.set(animated);
  }

  /**
   * Marks a specific event on a timeline. An event has a position on a timeline in the range
   * {@code [}{@link #getStart() start}{@code , }{@link #getEnd() end}{@code ]}, as well as a name and optional
   * description, and an importance value.
   */
  public static final class Marker {
    private final StringProperty name = new SimpleStringProperty(this, "name", "");
    private final StringProperty description = new SimpleStringProperty(this, "description", "");
    private final DoubleProperty position = new SimpleDoubleProperty(this, "position", 0);
    private final ObjectProperty<Importance> importance = new SimpleObjectProperty<>(this, "importance", LOWEST);

    public Marker() {
      this(null, null, 0, LOWEST);
    }

    public Marker(@NamedArg("position") double position) {
      this(null, null, position, LOWEST);
    }

    public Marker(@NamedArg("name") String name, @NamedArg("description") String description) {
      this(name, description, 0, LOWEST);
    }

    /**
     * Creates a new marker for an event on a timeline.
     *
     * @param name        the name of the event
     * @param description an optional detailed description of the event
     * @param position    the position of the event along the timeline
     * @param importance  the importance of the event
     */
    public Marker(@NamedArg("name") String name,
                  @NamedArg("description") String description,
                  @NamedArg("position") double position,
                  @NamedArg("importance") Importance importance) {
      setName(name);
      setDescription(description);
      setPosition(position);
      setImportance(importance);
    }

    public String getName() {
      return name.get();
    }

    public StringProperty nameProperty() {
      return name;
    }

    public void setName(String name) {
      this.name.set(name);
    }

    public String getDescription() {
      return description.get();
    }

    public StringProperty descriptionProperty() {
      return description;
    }

    public void setDescription(String description) {
      this.description.set(description);
    }

    public double getPosition() {
      return position.get();
    }

    public DoubleProperty positionProperty() {
      return position;
    }

    public void setPosition(double position) {
      this.position.set(position);
    }

    public Importance getImportance() {
      return importance.get();
    }

    public ObjectProperty<Importance> importanceProperty() {
      return importance;
    }

    public void setImportance(Importance importance) {
      this.importance.set(importance);
    }
  }

  /**
   * Represents the importance of an event.
   */
  public enum Importance {
    LOWEST,
    LOW,
    NORMAL,
    HIGH,
    HIGHEST
  }

  @Override
  public Skin<Timeline> createDefaultSkin() {
    return new TimelineSkin(this);
  }

  @Override
  public String getUserAgentStylesheet() {
    return Timeline.class.getResource("Timeline.css").toExternalForm();
  }

}
