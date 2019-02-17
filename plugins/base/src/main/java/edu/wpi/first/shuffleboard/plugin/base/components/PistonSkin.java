package edu.wpi.first.shuffleboard.plugin.base.components;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Control;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

/**
 * A skin for the {@link Piston} control.
 */
final class PistonSkin extends SkinBase<Piston> {

  private Pane root = new Pane();

  private Timeline motionTimeline;
  private final Rectangle shaft;

  private static final double RETRACTED_POSITION = 20.0;
  private static final double EXTENDED_POSITION = 112.5;

  /**
   * Constructor for all SkinBase instances.
   *
   * @param control The control for which this Skin should attach to.
   */
  protected PistonSkin(Piston control) {
    super(control);
    control.setMouseTransparent(true);
    root.setMouseTransparent(true);

    root.setMaxWidth(Control.USE_PREF_SIZE);
    root.setMaxHeight(Control.USE_PREF_SIZE);
    getChildren().setAll(root);

    Rectangle cylinder = new Rectangle(0, 0, 120.0, 40.0);

    cylinder.getStyleClass().setAll("piston-cylinder");

    Rectangle head = new Rectangle(cylinder.getX() + cylinder.getWidth(), 10.0, 15.0, 20.0);
    head.getStyleClass().setAll("piston-head");

    shaft = new Rectangle(RETRACTED_POSITION, 15.0, cylinder.getWidth(), 10.0);
    shaft.getStyleClass().setAll("piston-shaft");

    Rectangle end = new Rectangle(30.0, 20.0);
    end.xProperty().bind(shaft.xProperty().add(shaft.widthProperty()));
    end.setY(10.0);
    end.getStyleClass().setAll("piston-connector");

    shaft.setX(shaftLengthForPositon(control.getPosition()));

    Rectangle piston = new Rectangle();
    piston.setWidth(10.0);
    piston.setHeight(37.5);
    piston.xProperty().bind(shaft.xProperty().subtract(piston.widthProperty()));
    piston.yProperty().bind(cylinder.yProperty().add(1.25));
    piston.getStyleClass().add("piston-piston");

    Rectangle innerAir = new Rectangle();
    innerAir.yProperty().bind(piston.yProperty());
    innerAir.heightProperty().bind(piston.heightProperty());
    innerAir.xProperty().bind(EasyBind.combine(control.poweredPositionProperty(), shaft.xProperty(), (pos, x) -> {
      if (pos == Piston.Position.EXTENDED) {
        return 2;
      } else {
        return x;
      }
    }));
    innerAir.widthProperty().bind(EasyBind.combine(control.poweredPositionProperty(), piston.xProperty(), (pos, x) -> {
      if (pos == Piston.Position.EXTENDED) {
        return x;
      } else {
        return cylinder.getWidth() - 2 - x.doubleValue() - piston.getWidth();
      }
    }));
    innerAir.getStyleClass().add("air");

    control.positionProperty().addListener((__, was, position) -> {
      if (control.isAnimated()) {
        playAnimation(position);
      } else {
        shaft.setX(shaftLengthForPositon(position));
      }
    });

    Pane airIndicators = new Pane();
    airIndicators.setLayoutX(24);
    airIndicators.layoutXProperty().bind(
        EasyBind.monadic(control.poweredPositionProperty())
            .map(position -> {
              switch (position) {
                case RETRACTED:
                  return 96;
                case EXTENDED:
                  return 8;
                default:
                  return 8;
              }
            }));
    airIndicators.setManaged(false);
    List<Shape> firstRow = new ArrayList<>();
    List<Shape> secondRow = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      Shape indicator = generateAirIndicator();
      indicator.setLayoutX(4 * i);
      indicator.setLayoutY(0);
      firstRow.add(indicator);
      airIndicators.getChildren().add(indicator);
    }
    for (int i = 0; i < 4; i++) {
      Shape indicator = generateAirIndicator();
      indicator.setLayoutX(4 * i);
      indicator.setLayoutY(4);
      secondRow.add(indicator);
      airIndicators.getChildren().add(indicator);
    }
    for (int i = 0; i < 4; i++) {
      Shape indicator = generateAirIndicator();
      indicator.setLayoutX(4 * i);
      indicator.setLayoutY(8);
      airIndicators.getChildren().add(indicator);
    }
    airIndicators.getChildren().forEach(n -> n.getStyleClass().add("air-indicator"));

    DoubleProperty firstRowOpacity = new SimpleDoubleProperty(0);
    DoubleProperty secondRowOpacity = new SimpleDoubleProperty(0.5);
    firstRow.forEach(s -> s.opacityProperty().bind(firstRowOpacity));
    secondRow.forEach(s -> s.opacityProperty().bind(secondRowOpacity));

    // Row 1 starts at -24, 0%   opacity
    // Row 2 starts at -16, 50%  opacity
    // Row 3 starts at  -8, 100% opacity
    // Row 1 ends   at -16, 50%  opacity
    // Row 2 ends   at  -8, 100% opacity
    // Row 3 ends   at   0, 100% opacity
    Timeline airTimeline = new Timeline(
        new KeyFrame(
            Duration.ZERO,
            new KeyValue(airIndicators.layoutYProperty(), -8),
            new KeyValue(firstRowOpacity, 0),
            new KeyValue(secondRowOpacity, 0.5)
        ),
        new KeyFrame(
            Duration.millis(300),
            new KeyValue(airIndicators.layoutYProperty(), -4),
            new KeyValue(firstRowOpacity, 0.5),
            new KeyValue(secondRowOpacity, 1)
        )
    );
    airTimeline.setCycleCount(Animation.INDEFINITE);

    if (control.getPosition() == control.getPoweredPosition() && control.isAnimated()) {
      airTimeline.playFromStart();
    }

    MonadicBinding<Boolean> showAir1 = EasyBind.combine(control.positionProperty(), control.poweredPositionProperty(), Objects::equals);
    airIndicators.visibleProperty().bind(
        EasyBind.combine(control.animatedProperty(), showAir1,
            (animated, showAir) -> animated && showAir));
    airIndicators.visibleProperty().addListener((__, wasShowingAir, doShowAir) -> {
      if (doShowAir) {
        airTimeline.playFromStart();
      } else {
        airTimeline.stop();
      }
    });

    root.getChildren().addAll(airIndicators, cylinder, innerAir, shaft, piston, head, end);
  }

  @Override
  public void dispose() {
    super.dispose();
    if (motionTimeline != null) {
      motionTimeline.stop();
    }
    root = null;
  }

  private void playAnimation(Piston.Position position) {
    Piston control = getSkinnable();
    Duration currentTime = null;
    if (motionTimeline != null) {
      currentTime = motionTimeline.getCurrentTime();
      motionTimeline.stop();
    }
    motionTimeline = new Timeline();
    motionTimeline.setRate(1);
    motionTimeline.setAutoReverse(false);
    motionTimeline.setCycleCount(1);
    motionTimeline.getKeyFrames().add(new KeyFrame(Duration.ZERO, new KeyValue(shaft.xProperty(), shaft.getX())));
    motionTimeline.getKeyFrames().add(new KeyFrame(control.getAnimationDuration(), new KeyValue(shaft.xProperty(), shaftLengthForPositon(position))));
    if (currentTime == null) {
      motionTimeline.playFromStart();
    } else {
      motionTimeline.playFrom(control.getAnimationDuration().subtract(currentTime));
    }
  }

  private static double shaftLengthForPositon(Piston.Position position) {
    switch (position) {
      case EXTENDED:
        return EXTENDED_POSITION;
      case RETRACTED:
        return RETRACTED_POSITION;
      default:
        return RETRACTED_POSITION;
    }
  }

  private static Shape generateAirIndicator() {
    Shape indicator = new Circle(1);
    indicator.getStyleClass().add("air");
    return indicator;
  }

}
