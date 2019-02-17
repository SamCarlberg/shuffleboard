package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.api.util.Maps;
import edu.wpi.first.shuffleboard.plugin.base.data.SolenoidData;

import java.util.Map;
import java.util.function.Function;

public final class SolenoidType extends ComplexDataType<SolenoidData> {

  public static final SolenoidType Instance = new SolenoidType();

  private SolenoidType() {
    super("Solenoid", SolenoidData.class);
  }

  @Override
  public Function<Map<String, Object>, SolenoidData> fromMap() {
    return map -> new SolenoidData(
        Maps.getOrDefault(map, "Value", false),
        Maps.getOrDefault(map, ".controllable", false)
    );
  }

  @Override
  public SolenoidData getDefaultValue() {
    return new SolenoidData(false, false);
  }
}
