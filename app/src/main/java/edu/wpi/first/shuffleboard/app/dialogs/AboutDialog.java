package edu.wpi.first.shuffleboard.app.dialogs;

import edu.wpi.first.shuffleboard.api.components.ShuffleboardDialog;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.LazyInit;
import edu.wpi.first.shuffleboard.app.AboutDialogController;
import edu.wpi.first.shuffleboard.app.Shuffleboard;
import edu.wpi.first.shuffleboard.app.StageProvider;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;

import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.scene.layout.Pane;

/**
 * Dialog for displaying information about shuffleboard.
 */
public final class AboutDialog {

  private final LazyInit<Pane> pane = LazyInit.of(() -> FxUtils.load(AboutDialogController.class));
  private final AppPreferences appPreferences;
  private final StageProvider stageProvider;

  @Inject
  public AboutDialog(AppPreferences appPreferences, StageProvider stageProvider) {
    this.appPreferences = appPreferences;
    this.stageProvider = stageProvider;
  }

  /**
   * Shows the about dialog.
   */
  public void show() {
    ShuffleboardDialog dialog = new ShuffleboardDialog(pane.get(), true);
    dialog.setHeaderText("WPILib Shuffleboard");
    dialog.setSubheaderText(Shuffleboard.getVersion());
    dialog.getDialogPane().getStylesheets().setAll(appPreferences.getTheme().getStyleSheets());
    Platform.runLater(dialog.getDialogPane()::requestFocus);
    dialog.initOwner(stageProvider.getPrimaryStage());
    dialog.showAndWait();
  }

}
