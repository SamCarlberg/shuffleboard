package edu.wpi.first.shuffleboard.plugin.base.data;

import edu.wpi.first.shuffleboard.api.data.ComplexData;

import java.util.Map;

public final class SolenoidData extends ComplexData<SolenoidData> {

  private final boolean powered;
  private final boolean controllable;

  public SolenoidData(boolean powered, boolean controllable) {
    this.powered = powered;
    this.controllable = controllable;
  }

  public boolean isPowered() {
    return powered;
  }

  public boolean isControllable() {
    return controllable;
  }

  @Override
  public Map<String, Object> asMap() {
    return Map.of(
        "Value", powered,
        ".controllable", controllable
    );
  }
}
