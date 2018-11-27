package edu.wpi.first.shuffleboard.app.dialogs;

import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.LazyInit;
import edu.wpi.first.shuffleboard.app.PluginPaneController;
import edu.wpi.first.shuffleboard.app.StageProvider;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;

import com.google.inject.Inject;

import org.fxmisc.easybind.EasyBind;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog for plugins controller.
 */
public final class PluginDialog {

  private final AppPreferences appPreferences;
  private final StageProvider stageProvider;

  private boolean initialized = false;

  // Lazy init to avoid unnecessary loading if the dialog is never used
  private final LazyInit<Pane> pane = LazyInit.of(() -> FxUtils.load(PluginPaneController.class));
  private Stage stage;

  @Inject
  public PluginDialog(AppPreferences appPreferences, StageProvider stageProvider) {
    this.appPreferences = appPreferences;
    this.stageProvider = stageProvider;
  }

  private void setup() {
    initialized = true;
    stage = new Stage();
    stage.initModality(Modality.WINDOW_MODAL);
    stage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        stage.close();
      }
    });
    stage.setScene(new Scene(pane.get()));
    stage.sizeToScene();
    stage.setMinWidth(675);
    stage.setMinHeight(325);
    stage.setTitle("Loaded Plugins");
    stage.initOwner(stageProvider.getPrimaryStage());
    FxUtils.bind(stage.getScene().getStylesheets(), EasyBind.map(appPreferences.themeProperty(), Theme::getStyleSheets));
  }

  /**
   * Shows the plugin dialog.
   */
  public void show() {
    if (!initialized) {
      setup();
    }
    stage.show();
  }

}
