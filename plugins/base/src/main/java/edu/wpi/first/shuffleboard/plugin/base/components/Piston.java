package edu.wpi.first.shuffleboard.plugin.base.components;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.Duration;

/**
 * A JavaFX object that draws a pneumatic piston.
 */
public final class Piston extends Control {

  private final Property<Position> position = new SimpleObjectProperty<>(Position.RETRACTED);
  private final Property<Position> poweredPosition = new SimpleObjectProperty<>(Position.EXTENDED);
  private final BooleanProperty animated = new SimpleBooleanProperty(true);
  private final Property<Duration> animationDuration = new SimpleObjectProperty<>(Duration.millis(150));

  public enum Position {
    EXTENDED {
      @Override
      public Position reverse() {
        return RETRACTED;
      }
    },
    RETRACTED {
      @Override
      public Position reverse() {
        return EXTENDED;
      }
    };

    public abstract Position reverse();
  }

  public Piston() {
    setMaxWidth(USE_PREF_SIZE);
    setMaxHeight(USE_PREF_SIZE);
    getStyleClass().add("piston");
  }

  @Override
  protected Skin<?> createDefaultSkin() {
    return new PistonSkin(this);
  }

  @Override
  public String getUserAgentStylesheet() {
    return Piston.class.getResource("/edu/wpi/first/shuffleboard/plugin/base/widget/piston.css").toExternalForm();
  }

  public Position getPosition() {
    return position.getValue();
  }

  public Property<Position> positionProperty() {
    return position;
  }

  public void setPosition(Position position) {
    this.position.setValue(position);
  }

  public Position getPoweredPosition() {
    return poweredPosition.getValue();
  }

  public Property<Position> poweredPositionProperty() {
    return poweredPosition;
  }

  public void setPoweredPosition(Position poweredPosition) {
    this.poweredPosition.setValue(poweredPosition);
  }

  public boolean isAnimated() {
    return animated.get();
  }

  public BooleanProperty animatedProperty() {
    return animated;
  }

  public void setAnimated(boolean animated) {
    this.animated.set(animated);
  }

  public Duration getAnimationDuration() {
    return animationDuration.getValue();
  }

  public Property<Duration> animationDurationProperty() {
    return animationDuration;
  }

  public void setAnimationDuration(Duration animationDuration) {
    this.animationDuration.setValue(animationDuration);
  }
}
