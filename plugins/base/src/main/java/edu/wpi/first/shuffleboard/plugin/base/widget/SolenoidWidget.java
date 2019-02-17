package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.components.Piston;
import edu.wpi.first.shuffleboard.plugin.base.data.SolenoidData;

import org.controlsfx.glyphfont.Glyph;
import org.fxmisc.easybind.EasyBind;

import java.util.List;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

@Description(name = "Solenoid", dataTypes = SolenoidData.class)
@ParametrizedController("SolenoidWidget.fxml")
public final class SolenoidWidget extends SimpleAnnotatedWidget<SolenoidData> {

  @FXML
  private Pane root;
  @FXML
  private Piston piston;
  @FXML
  private Label powerLabel;
  @FXML
  private Glyph powerIcon;

  @FXML
  private void initialize() {
    // Combine two properties so that the piston will always move if either one changes
    piston.positionProperty().bind(
        EasyBind.combine(dataOrDefault, piston.poweredPositionProperty(),
            SolenoidWidget::positionForState)
    );

    powerLabel.textProperty().bind(dataOrDefault.map(SolenoidData::isPowered).map(powered -> {
      if (powered) {
        return "Energized";
      } else {
        return "Unenergized";
      }
    }));

    powerIcon.textFillProperty().bind(dataOrDefault.map(SolenoidData::isPowered).map(powered -> {
      if (powered) {
        return Color.YELLOW;
      } else {
        return Color.DARKGRAY;
      }
    }));
  }

  @Override
  public Pane getView() {
    return root;
  }

  @Override
  public List<Group> getSettings() {
    return List.of(
        Group.of("Piston",
            Setting.of("Energized position", "The state of the piston when the solenoid is energized", piston.poweredPositionProperty(), Piston.Position.class),
            Setting.of("Animated", "Animate the motion of the piston", piston.animatedProperty(), Boolean.class)
        )
    );
  }

  private static Piston.Position positionForState(SolenoidData data, Piston.Position poweredPosition) {
    if (data.isPowered()) {
      return poweredPosition;
    } else {
      return poweredPosition.reverse();
    }
  }

}
