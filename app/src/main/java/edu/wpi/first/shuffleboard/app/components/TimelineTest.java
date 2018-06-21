package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.theme.Themes;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TimelineTest extends Application {

  @Override
  public void start(Stage stage) {
    Timeline timeline = new Timeline();
    Timeline.Marker start = new Timeline.Marker("Match start", "", 0, Timeline.Importance.HIGHEST);
    Timeline.Marker teleopStart = new Timeline.Marker("Teleop start", "", 15, Timeline.Importance.HIGH);
    Timeline.Marker endgameStart = new Timeline.Marker("Endgame start", "", 105, Timeline.Importance.NORMAL);
    Timeline.Marker climb = new Timeline.Marker("Climb", "", 130, Timeline.Importance.LOW);
    Timeline.Marker end = new Timeline.Marker("Match end", "", 135, Timeline.Importance.HIGHEST);
    timeline.getMarkers().addAll(start, teleopStart, endgameStart, climb, end);
    timeline.setLength(Duration.seconds(135));
    timeline.setStart(0);
    timeline.setEnd(135);

    timeline.setDetailTimeout(Duration.seconds(5));
    timeline.setPlaybackSpeed(5);
    timeline.setPlaying(true);
    timeline.setAnimated(true);

    StackPane root = new StackPane(timeline);
    root.getStylesheets().addAll(Themes.MIDNIGHT.getStyleSheets());
    root.setPadding(new Insets(32));
    stage.setScene(new Scene(root, 320, 64));
    stage.setMinHeight(64);
    stage.setMinWidth(128);
    stage.setTitle("Timeline Control Test");
    stage.show();
  }

}
