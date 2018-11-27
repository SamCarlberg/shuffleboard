package edu.wpi.first.shuffleboard.app.dialogs;

import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.LazyInit;
import edu.wpi.first.shuffleboard.app.ConvertRecordingPaneController;
import edu.wpi.first.shuffleboard.app.StageProvider;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;

import com.google.inject.Inject;

import org.fxmisc.easybind.EasyBind;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Dialog for allowing users to convert data recordings from the binary format to other formats.
 */
public final class ExportRecordingDialog {

  private final AppPreferences appPreferences;
  private final StageProvider stageProvider;

  private boolean initialized = false;
  private final LazyInit<Pane> pane = LazyInit.of(() -> FxUtils.load(ConvertRecordingPaneController.class));
  private Stage stage;

  @Inject
  public ExportRecordingDialog(AppPreferences appPreferences, StageProvider stageProvider) {
    this.appPreferences = appPreferences;
    this.stageProvider = stageProvider;
  }

  private void setup() {
    stage = new Stage();
    stage.setTitle("Export Recording Files");
    stage.setScene(new Scene(pane.get()));
    stage.setResizable(false);
    FxUtils.bind(stage.getScene().getStylesheets(), EasyBind.map(appPreferences.themeProperty(), Theme::getStyleSheets));
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.initOwner(stageProvider.getPrimaryStage());
    initialized = true;
  }

  /**
   * Shows the dialog.
   */
  public void show() {
    if (!initialized) {
      setup();
    }
    stage.show();
  }

}
