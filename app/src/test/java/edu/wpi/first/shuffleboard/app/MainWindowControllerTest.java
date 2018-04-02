package edu.wpi.first.shuffleboard.app;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainWindowControllerTest extends ApplicationTest {

  private MainWindowController controller;

  @Override
  public void start(Stage stage) throws Exception {
    FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource("MainWindow.fxml"));
    Pane root = loader.load();
    controller = loader.getController();
    stage.setScene(new Scene(root));
    stage.show();
  }

  @Test
  public void testLoadInvalidFile() throws IOException {
    File file = Files.createTempFile("no-tabs", ".json").toFile();
    InputStream noTabs = getClass().getResourceAsStream("/no-tabs.json");
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(noTabs, "UTF-8"))) {
      List<String> lines = reader.lines().collect(Collectors.toList());
      Files.write(file.toPath(), lines);
    }
    Assertions.assertThrows(IOException.class, () -> controller.load(file));
  }

}
