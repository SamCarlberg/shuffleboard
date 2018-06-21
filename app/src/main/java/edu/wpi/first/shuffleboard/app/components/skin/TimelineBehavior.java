package edu.wpi.first.shuffleboard.app.components.skin;

import edu.wpi.first.shuffleboard.app.components.Timeline;

import com.google.common.collect.ImmutableList;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

import javafx.scene.input.KeyCode;

public class TimelineBehavior extends BehaviorBase<Timeline> {

  private static final String PREV_MARKER = "PreviousMarker";
  private static final String NEXT_MARKER = "NextMarker";
  private static final String FIRST_MARKER = "FirstMarker";
  private static final String LAST_MARKER = "LastMarker";
  private static final String TOGGLE_PLAYING = "TogglePlaying";

  private static final List<KeyBinding> keyBindings = ImmutableList.of(
      new KeyBinding(KeyCode.LEFT, PREV_MARKER),
      new KeyBinding(KeyCode.KP_LEFT, PREV_MARKER),
      new KeyBinding(KeyCode.RIGHT, NEXT_MARKER),
      new KeyBinding(KeyCode.KP_RIGHT, NEXT_MARKER),
      new KeyBinding(KeyCode.HOME, FIRST_MARKER),
      new KeyBinding(KeyCode.LEFT, FIRST_MARKER).ctrl(),
      new KeyBinding(KeyCode.END, LAST_MARKER),
      new KeyBinding(KeyCode.RIGHT, LAST_MARKER).ctrl(),
      new KeyBinding(KeyCode.SPACE, TOGGLE_PLAYING)
  );

  public TimelineBehavior(Timeline control) {
    super(control, keyBindings);
  }

  @Override
  protected void callAction(String name) {
    switch (name) {
      case PREV_MARKER:
        previousMarker();
        break;
      case NEXT_MARKER:
        nextMarker();
        break;
      case FIRST_MARKER:
        firstMarker();
        break;
      case LAST_MARKER:
        lastMarker();
        break;
      case TOGGLE_PLAYING:
        togglePlayback();
        break;
      default:
        super.callAction(name);
    }
  }

  public void previousMarker() {
    Timeline.Marker closest = closest(1, (a, b) -> a > b);
    if (closest != null) {
      moveToMarker(closest);
    }
  }

  public void nextMarker() {
    Timeline.Marker closest = closest(-1, (a, b) -> a < b);
    if (closest != null) {
      moveToMarker(closest);
    }
  }

  private void moveToMarker(Timeline.Marker closest) {
    Timeline control = getControl();
    control.setPlaying(false);
    control.setProgress(closest.getPosition());
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

  public void firstMarker() {
    getControl().getMarkers()
        .stream()
        .min(Comparator.comparingDouble(m -> m.getPosition()))
        .ifPresent(this::moveToMarker);
  }

  public void lastMarker() {
    getControl().getMarkers()
        .stream()
        .max(Comparator.comparingDouble(m -> m.getPosition()))
        .ifPresent(this::moveToMarker);
  }

  public void togglePlayback() {
    Timeline control = getControl();
    control.setPlaying(!getControl().isPlaying());
  }

  public void toggleLoop() {
    Timeline control = getControl();
    control.setLoopPlayback(!control.isLoopPlayback());
  }

}
