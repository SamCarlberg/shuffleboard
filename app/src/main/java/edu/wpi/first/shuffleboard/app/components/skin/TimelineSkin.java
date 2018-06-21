package edu.wpi.first.shuffleboard.app.components.skin;

import edu.wpi.first.shuffleboard.app.components.Timeline;

import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import com.sun.javafx.util.Utils;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

@SuppressWarnings("JavadocMethod")
public class TimelineSkin extends BehaviorSkinBase<Timeline, TimelineBehavior> {

  private final ListChangeListener<Timeline.Marker> markerListChangeListener;

  private static final PseudoClass current = PseudoClass.getPseudoClass("current");
  private static final PseudoClass importanceLowest = PseudoClass.getPseudoClass("importance-lowest");
  private static final PseudoClass importanceLow = PseudoClass.getPseudoClass("importance-low");
  private static final PseudoClass importanceNormal = PseudoClass.getPseudoClass("importance-normal");
  private static final PseudoClass importanceHigh = PseudoClass.getPseudoClass("importance-high");
  private static final PseudoClass importanceHighest = PseudoClass.getPseudoClass("importance-highest");

  private static final Map<Timeline.Importance, PseudoClass> importanceClasses =
      new EnumMap<>(Timeline.Importance.class);

  static {
    importanceClasses.put(Timeline.Importance.LOWEST, importanceLowest);
    importanceClasses.put(Timeline.Importance.LOW, importanceLow);
    importanceClasses.put(Timeline.Importance.NORMAL, importanceNormal);
    importanceClasses.put(Timeline.Importance.HIGH, importanceHigh);
    importanceClasses.put(Timeline.Importance.HIGHEST, importanceHighest);
  }

  private Pane root;
  private final Pane track;
  private final Map<Timeline.Marker, Node> markerMap = new HashMap<>();
  private final Map<Double, Timeline.Marker> markerPositions = new LinkedHashMap<>();
  private final Label detail = new Label();
  private Timeline.Marker lastMarker = null; // NOPMD could be final - PMD doesn't understand lambdas
  private final ObjectProperty<Timeline.Marker> displayedMarker = new SimpleObjectProperty<>();
  private final MonadicBinding<Double> currentMarkerX = EasyBind.monadic(displayedMarker) // NOPMD could be local var
      .flatMap(m -> markerMap.get(m).layoutXProperty())
      .map(Number::doubleValue)
      .orElse(0.0);

  private final javafx.animation.Timeline animation = new javafx.animation.Timeline();
  private boolean tempView = false;
  private boolean hidingDetailLabel = false;
  private final DoubleBinding timelineLength;

  public TimelineSkin(Timeline control) {
    super(control, new TimelineBehavior(control));
    root = new VBox(2);
    track = new Pane();
    track.minHeightProperty().bind(root.minHeightProperty());
    track.prefHeightProperty().bind(root.prefHeightProperty());
    track.maxHeightProperty().bind(root.maxHeightProperty());
    root.setMinHeight(20);
    root.setMaxHeight(20);
    track.getStyleClass().add("track");
    root.getChildren().add(track);
    timelineLength = control.endProperty().subtract(control.startProperty());
    markerListChangeListener = c -> {
      while (c.next()) {
        if (c.wasAdded()) {
          for (Timeline.Marker marker : c.getAddedSubList()) {
            addMarkerHandle(marker);
            markerPositions.put(marker.getPosition(), marker);
          }
        } else if (c.wasRemoved()) {
          for (Timeline.Marker marker : c.getRemoved()) {
            Node removedMarkerHandle = markerMap.remove(marker);
            track.getChildren().remove(removedMarkerHandle);
            markerPositions.remove(marker.getPosition());
          }
        }
      }
      c.getList().forEach(marker -> markerPositions.put(marker.getPosition(), marker));
    };
    for (Timeline.Marker marker : control.getMarkers()) {
      addMarkerHandle(marker);
      markerPositions.put(marker.getPosition(), marker);
    }
    control.getMarkers().addListener(markerListChangeListener);
    Path progressHandle = createProgressHandle();
    progressHandle.layoutXProperty().bind(
        control.progressProperty().divide(timelineLength).multiply(track.widthProperty()));

    control.playingProperty().addListener((__, was, is) -> {
      if (is && control.isAnimated()) {
        startAnimation();
      } else {
        animation.stop();
      }
    });
    control.animatedProperty().addListener((__, was, is) -> {
      if (is && control.isPlaying()) {
        startAnimation();
      } else {
        animation.stop();
      }
    });
    control.loopPlaybackProperty().addListener((__, was, doLoop) -> {
      animation.setCycleCount(doLoop ? -1 : 1);
      if (control.isPlaying() && control.isAnimated()) {
        animation.stop();
        animation.playFrom(progressToTime(control.getProgress()));
      }
    });

    if (control.isPlaying() && control.isAnimated()) {
      startAnimation();
    }
    displayedMarker.addListener((__, old, marker) -> {
      if (marker != null) {
        importanceClasses.forEach((p, c) -> {
          detail.pseudoClassStateChanged(c, p == marker.getImportance());
        });
      }
    });
    control.progressProperty().addListener((__, old, progress) -> {
      if (getSkinnable().getMarkers().isEmpty()) {
        return;
      }
      final double o = old.doubleValue();
      final double p = progress.doubleValue();
      markerPositions.entrySet().stream()
          .filter(e -> e.getKey() == p || (e.getKey() >= o && e.getKey() <= p))
          .map(e -> e.getValue())
          .max(Comparator.comparingDouble(Timeline.Marker::getPosition))
          .ifPresent(marker -> {
            if (lastMarker != null) {
              markerMap.get(lastMarker).pseudoClassStateChanged(current, false);
            }
            markerMap.get(marker).pseudoClassStateChanged(current, true);
            lastMarker = marker;
          });
      if (tempView) {
        return;
      }
      tempView = false;
      double adjustedTimeDelta = Math.abs(lastMarker.getPosition() - p) / control.getPlaybackSpeed();
      if (progressToTime(adjustedTimeDelta).compareTo(control.getDetailTimeout()) >= 0) {
        hideDetail();
      } else {
        displayedMarker.set(lastMarker);
        detail.setText(makeText(lastMarker));
      }
    });
    detail.getStyleClass().add("detail-label");
    detail.maxWidthProperty().bind(track.widthProperty());
    detail.setLayoutY(1);
    detail.prefHeightProperty().bind(track.heightProperty().subtract(2));
    detail.maxHeightProperty().bind(detail.prefHeightProperty());
    detail.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
    detail.textProperty().addListener((__, old, text) -> {
      if (!detail.isVisible()) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(400), detail);
        detail.setVisible(true);
        fadeTransition.setToValue(1);
        fadeTransition.setFromValue(0);
        fadeTransition.playFromStart();
      }
    });
    detail.layoutXProperty().bind(
        EasyBind.combine(
            currentMarkerX, detail.widthProperty(), track.widthProperty(),
            TimelineSkin::computeDetailLabelPosition
        ));
    detail.setOnMouseExited(__ -> {
      if (tempView) {
        hideDetail();
        tempView = false;
      }
    });
    detail.setVisible(false);
    track.getChildren().add(detail);
    track.getChildren().add(progressHandle);

    Label time = new Label();
    time.textProperty().bind(EasyBind.monadic(control.progressProperty()).map(p -> {
      return toString(progressToTime(p.doubleValue()).toMillis());
    }));
    HBox controls = createControls();
    BorderPane foot = new BorderPane();
    foot.setLeft(time);
    foot.setRight(controls);
    root.getChildren().add(foot);
    getChildren().add(root);
  }

  public void addMarkerHandle(Timeline.Marker marker) {
    Node markerHandle = createMarkerHandle(marker);
    markerHandle.layoutXProperty().bind(
        marker.positionProperty().divide(timelineLength).multiply(track.widthProperty()));
    markerMap.put(marker, markerHandle);
    track.getChildren().add(0, markerHandle);
  }

  private static double computeDetailLabelPosition(double markerX, Number labelWidth, Number trackWidth) {
    return Utils.clamp(
        0,
        markerX - labelWidth.doubleValue() / 2,
        trackWidth.doubleValue() - labelWidth.doubleValue()
    );
  }

  private void hideDetail() {
    if (detail.isVisible() && !hidingDetailLabel) {
      hidingDetailLabel = true;
      FadeTransition fadeTransition = new FadeTransition(Duration.millis(400), detail);
      fadeTransition.setToValue(0);
      fadeTransition.setFromValue(1);
      fadeTransition.setOnFinished(__ -> {
        detail.setVisible(false);
        if (lastMarker != null) {
          markerMap.get(lastMarker).pseudoClassStateChanged(current, false);
        }
        hidingDetailLabel = false;
      });
      fadeTransition.playFromStart();
    }
  }

  private static boolean isNullOrEmpty(String text) {
    return text == null || text.isEmpty() || text.chars().allMatch(Character::isWhitespace);
  }

  private HBox createControls() {
    final Timeline control = getSkinnable();
    FontAwesomeIconView prev = new FontAwesomeIconView(FontAwesomeIcon.BACKWARD);
    FontAwesomeIconView playPause = new FontAwesomeIconView(FontAwesomeIcon.PLAY);
    FontAwesomeIconView next = new FontAwesomeIconView(FontAwesomeIcon.FORWARD);
    FontAwesomeIconView loop = new FontAwesomeIconView(FontAwesomeIcon.REPEAT);
    Stream.of(prev, playPause, next, loop)
        .map(v -> v.getStyleClass())
        .forEach(c -> c.add("glyph-button"));

    Tooltip playPauseTooltip = new Tooltip();
    playPauseTooltip.textProperty().bind(
        EasyBind.monadic(control.playingProperty())
            .map(playing -> playing ? "Pause" : "Play"));

    Tooltip.install(prev, new Tooltip("Previous marker"));
    Tooltip.install(playPause, playPauseTooltip);
    Tooltip.install(next, new Tooltip("Next marker"));
    Tooltip.install(loop, new Tooltip("Repeat"));

    prev.setOnMouseClicked(__ -> getBehavior().previousMarker());
    playPause.setOnMouseClicked(__ -> getBehavior().togglePlayback());
    next.setOnMouseClicked(__ -> getBehavior().nextMarker());
    loop.setOnMouseClicked(__ -> getBehavior().toggleLoop());

    control.loopPlaybackProperty().addListener((__, was, doLoop) -> {
      loop.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), doLoop);
    });

    playPause.glyphNameProperty().bind(EasyBind.monadic(control.playingProperty())
        .map(playing -> {
          if (playing) {
            return "PAUSE";
          } else {
            return "PLAY";
          }
        }));

    HBox controls = new HBox(4, prev, playPause, next, loop);
    controls.getStyleClass().add("controls");
    return controls;
  }

  private void startAnimation() {
    Timeline control = getSkinnable();
    animation.getKeyFrames().setAll(
        new KeyFrame(Duration.ZERO, new KeyValue(control.progressProperty(), control.getStart())),
        new KeyFrame(control.getLength(), new KeyValue(control.progressProperty(), control.getEnd()))
    );
    animation.setCycleCount(control.isLoopPlayback() ? Animation.INDEFINITE : 1);
    animation.setRate(control.getPlaybackSpeed());
    animation.playFrom(progressToTime(control.getProgress()));
  }

  private String makeText(Timeline.Marker marker) {
    Duration time = progressToTime(marker.getPosition());
    String timeString = toString(time.toMillis());
    if (isNullOrEmpty(marker.getDescription())) {
      return timeString + " - " + marker.getName();
    } else {
      return timeString + " - " + marker.getName() + ": " + marker.getDescription();
    }
  }

  /**
   * Converts milliseconds to a formatted string in the format {@code HH:MM:SS.mmm}.
   *
   * @param millis the number of milliseconds
   */
  @SuppressWarnings("UnnecessaryParentheses")
  private static String toString(double millis) {
    int hh = (int) (millis / (3_600_000));
    int mm = (int) ((millis / (60_000)) % 60);
    int ss = (int) (millis / 1000) % 60;
    int mmm = (int) (millis % 1000);
    return String.format("%02d:%02d:%02d.%03d", hh, mm, ss, mmm);
  }

  private Node createMarkerHandle(Timeline.Marker marker) {
    // Diamond shape
    Path handle = new Path(
        new MoveTo(-4, 0),
        new LineTo(0, -4),
        new LineTo(4, 0),
        new LineTo(0, 4),
        new ClosePath()
    );
    handle.getStyleClass().add("marker-handle");
    handle.setOnMousePressed(__ -> {
      tempView = false;
      getBehavior().moveToMarker(marker);
      detail.setText(makeText(marker));
    });
    handle.setOnMouseEntered(__ -> {
      tempView = true;
      displayedMarker.set(marker);
      detail.setText(makeText(marker));
      detail.setOpacity(1);
      detail.setVisible(true);
      updatePseudoClasses(marker, detail);
    });
    handle.setOnMouseExited(e -> {
      if (tempView && !detail.contains(detail.screenToLocal(e.getScreenX(), e.getScreenY()))) {
        hideDetail();
        tempView = false;
      }
    });
    marker.importanceProperty().addListener(__ -> updatePseudoClasses(marker, handle));
    updatePseudoClasses(marker, handle);
    return handle;
  }

  private void updatePseudoClasses(Timeline.Marker marker, Node node) {
    importanceClasses.forEach((p, c) -> {
      node.pseudoClassStateChanged(c, p == marker.getImportance());
    });
  }

  private Path createProgressHandle() {
    Path handle = new Path(
        new MoveTo(-5, -7),
        new LineTo(5, -7),
        new LineTo(5, 2),
        new LineTo(0, 7),
        new LineTo(-5, 2),
        new ClosePath()
    );
    handle.getStyleClass().add("progress-handle");
    EventHandler<MouseEvent> resumeOnDoubleClick = e -> {
      if (e.getClickCount() == 2) {
        getBehavior().togglePlayback();
      }
    };
    makeDraggable(handle);
    handle.addEventHandler(MouseEvent.MOUSE_CLICKED, resumeOnDoubleClick);
    return handle;
  }

  private void makeDraggable(Node handle) {
    Timeline control = getSkinnable();
    handle.setOnMousePressed(__ -> control.setPlaying(false));
    handle.setOnMouseDragged(e -> {
      Point2D cur = handle.localToParent(e.getX(), e.getY());
      double dragPos = cur.getX();
      double progress = Utils.clamp(
          control.getStart(),
          (dragPos / track.getWidth()) * (control.getEnd() - control.getStart()),
          control.getEnd()
      );
      control.setProgress(progress);
    });
  }

  private Duration progressToTime(double progress) {
    Timeline control = getSkinnable();
    return control.getLength().multiply(progress / (control.getEnd() - control.getStart()));
  }

  @Override
  public void dispose() {
    getSkinnable().getMarkers().removeListener(markerListChangeListener);
    markerMap.clear();
    markerPositions.clear();
    root = null;
  }

}
