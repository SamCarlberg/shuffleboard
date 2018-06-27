package edu.wpi.first.shuffleboard.app.components.skin;

import edu.wpi.first.shuffleboard.app.components.Timeline;

import com.github.samcarlberg.fxbehaviors.BehaviorBase;
import com.github.samcarlberg.fxbehaviors.InputBindings;
import com.github.samcarlberg.fxbehaviors.KeyBinding;

import java.util.Comparator;
import java.util.function.BiFunction;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;

public class TimelineBehavior extends BehaviorBase<Timeline, TimelineBehavior> {

  private static final InputBindings<TimelineBehavior> bindings = InputBindings.of(
      KeyBinding.<TimelineBehavior>builder()
          .withKey(KeyCode.LEFT)
          .withKey(KeyCode.KP_LEFT)
          .withAction(TimelineBehavior::previousMarker)
          .build(),
      KeyBinding.<TimelineBehavior>builder()
          .withKey(KeyCode.RIGHT)
          .withKey(KeyCode.KP_RIGHT)
          .withAction(TimelineBehavior::nextMarker)
          .build(),
      KeyBinding.<TimelineBehavior>builder()
          .withKey(KeyCode.HOME)
          .withKey(KeyCode.LEFT, KeyCombination.CONTROL_DOWN)
          .withAction(TimelineBehavior::firstMarker)
          .build(),
      KeyBinding.<TimelineBehavior>builder()
          .withKey(KeyCode.END)
          .withKey(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN)
          .withAction(TimelineBehavior::lastMarker)
          .build(),
      KeyBinding.<TimelineBehavior>builder()
          .withKey(KeyCode.SPACE)
          .withAction(TimelineBehavior::togglePlayback)
          .build()
  );

  public TimelineBehavior(Timeline control) {
    super(control, bindings);
  }

  /**
   * Gives the control focus. Should be called by the skin when the control is pressed on with a mouse button.
   */
  public void focus() {
    Timeline timeline = getControl();
    if (!timeline.isFocused()) {
      timeline.requestFocus();
    }
  }

  /**
   * Sets the control's progress to the closest marker prior to the current progress value. Does nothing if there is no
   * such marker.
   */
  public void previousMarker() {
    Timeline.Marker closest = closest(1, (a, b) -> a > b);
    if (closest != null) {
      moveToMarker(closest);
    }
  }

  /**
   * Sets the control's progress to the closest marker after to the current progress value. Does nothing if there is no
   * such marker.
   */
  public void nextMarker() {
    Timeline.Marker closest = closest(-1, (a, b) -> a < b);
    if (closest != null) {
      moveToMarker(closest);
    }
  }

  /**
   * Moves the timeline's progress to the given marker and stops its playback.
   *
   * @param marker the marker to move to
   */
  public void moveToMarker(Timeline.Marker marker) {
    Timeline control = getControl();
    control.setPlaying(false);
    control.setProgress(marker.getPosition());
  }

  private Timeline.Marker closest(double defaultValue, BiFunction<Double, Double, Boolean> comparator) {
    Timeline control = getControl();
    Timeline.Marker closest = null;
    for (Timeline.Marker marker : control.getMarkers()) {
      double diff = marker.getPosition() - control.getProgress();
      if (comparator.apply(diff, 0.0) || diff == 0) {
        // Either the current control or one ahead of the current position; either way, it's not a marker we want
        continue;
      }
      double lastDiff = closest == null ? defaultValue : closest.getPosition() - control.getProgress();
      if (closest == null || comparator.apply(diff, lastDiff)) {
        closest = marker;
      }
    }
    return closest;
  }

  /**
   * Sets the control's progress to the first marker on the timeline.
   */
  public void firstMarker() {
    getControl().getMarkers()
        .stream()
        .min(Comparator.comparingDouble(m -> m.getPosition()))
        .ifPresent(this::moveToMarker);
  }

  /**
   * Sets the control's progress to the last marker on the timeline.
   */
  public void lastMarker() {
    getControl().getMarkers()
        .stream()
        .max(Comparator.comparingDouble(m -> m.getPosition()))
        .ifPresent(this::moveToMarker);
  }

  /**
   * Toggles the control's {@link Timeline#playingProperty() playing property}.
   */
  public void togglePlayback() {
    Timeline control = getControl();
    control.setPlaying(!getControl().isPlaying());
  }

  /**
   * Toggles the control's {@link Timeline#loopPlaybackProperty() loop playback property}.
   */
  public void toggleLoop() {
    Timeline control = getControl();
    boolean doLoop = !control.isLoopPlayback();
    control.setLoopPlayback(doLoop);
    if (doLoop && control.getProgress() == control.getEnd()) {
      control.setPlaying(false);
      control.setPlaying(true);
      control.setProgress(control.getStart());
    }
  }

}
