package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.sources.recording.Recording;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.app.components.Timeline;
import edu.wpi.first.shuffleboard.app.sources.recording.Playback;

import org.fxmisc.easybind.EasyBind;

import java.util.Objects;

import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class PlaybackController {

  @FXML
  private Pane root;
  @FXML
  private Button recordButton;
  @FXML
  private HBox playbackControls;
  @FXML
  private Button playPauseButton;
  @FXML
  private Timeline timeline;

  private final ImageView recordIcon = new ImageView("/edu/wpi/first/shuffleboard/app/icons/icons8-Record-16.png");
  private final ImageView stopIcon = new ImageView("/edu/wpi/first/shuffleboard/app/icons/icons8-Stop-16.png");

  private final Property<Number> frameProperty =
      EasyBind.monadic(Playback.currentPlaybackProperty())
          .selectProperty(Playback::frameProperty);

  private final Property<Boolean> loopingProperty =
      EasyBind.monadic(Playback.currentPlaybackProperty())
          .selectProperty(Playback::loopingProperty);

  @FXML
  private void initialize() {
    playbackControls.disableProperty().bind(
        EasyBind.map(Playback.currentPlaybackProperty(), Objects::isNull));
    recordButton.graphicProperty().bind(
        EasyBind.map(Recorder.getInstance().runningProperty(), running -> running ? stopIcon : recordIcon));

    timeline.endProperty().bind(EasyBind.monadic(Playback.currentPlaybackProperty()).map(Playback::getMaxFrameNum));
    timeline.lengthProperty().bind(
        EasyBind.monadic(Playback.currentPlaybackProperty())
            .map(Playback::getRecording)
            .map(Recording::getLength)
            .map(Duration::millis)
            .orElse(Duration.UNKNOWN));

    Playback.currentPlaybackProperty().addListener((__, old, playback) -> {
      timeline.getMarkers().clear();
      timeline.getMarkers().addAll(
          new Timeline.Marker("Start of recording", "", 0, Timeline.Importance.HIGHEST),
          new Timeline.Marker("End of recording", "", playback.getMaxFrameNum(), Timeline.Importance.HIGHEST)
      );
    });

    timeline.progressProperty().addListener((__, old, progress) -> {
      if (!timeline.isPlaying()) {
        Playback.getCurrentPlayback().ifPresent(playback -> playback.setFrame(progress.intValue()));
      }
    });

    timeline.playingProperty().addListener((__, was, is) -> {
      if (is) {
        Playback.getCurrentPlayback().ifPresent(Playback::unpause);
      } else {
        Playback.getCurrentPlayback().ifPresent(Playback::pause);
      }
    });

    frameProperty.addListener((__, prev, frame) -> {
      FxUtils.runOnFxThread(() -> {
        Playback playback = Playback.getCurrentPlayback().orElse(null);
        if (frame == null || playback == null) {
          timeline.setProgress(0);
        } else {
          timeline.setProgress(playback.getFrame());
        }
      });
    });

    timeline.loopPlaybackProperty().bindBidirectional(loopingProperty);
  }

  @FXML
  void previousFrame() {
    Playback.getCurrentPlayback().ifPresent(Playback::previousFrame);
  }

  @FXML
  void nextFrame() {
    Playback.getCurrentPlayback().ifPresent(Playback::nextFrame);
  }

  @FXML
  void togglePlayPause() {
    Playback.getCurrentPlayback().ifPresent(playback -> playback.setPaused(!playback.isPaused()));
  }

  @FXML
  void stopPlayback() {
    Playback.getCurrentPlayback().ifPresent(Playback::stop);
  }

  @FXML
  void toggleRecord() {
    Recorder recorder = Recorder.getInstance();
    if (recorder.isRunning()) {
      recorder.stop();
    } else {
      recorder.start();
    }
  }

}
