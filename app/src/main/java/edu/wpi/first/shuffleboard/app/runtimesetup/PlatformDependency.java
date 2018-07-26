package edu.wpi.first.shuffleboard.app.runtimesetup;

import edu.wpi.first.shuffleboard.api.util.SystemProperties;

import java.util.function.BiFunction;

public final class PlatformDependency implements Dependency {

  private final String group;
  private final String name;
  private final String version;
  private final BiFunction<String, String, String> classifierGenerator;

  public PlatformDependency(String group, String name, String version, BiFunction<String, String, String> classifierGenerator) {
    this.group = group;
    this.name = name;
    this.version = version;
    this.classifierGenerator = classifierGenerator;
  }

  @Override
  public String getGroup() {
    return group;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public String getClassifier() {
    return classifierGenerator.apply(SystemProperties.OS_NAME, SystemProperties.OS_ARCH);
  }
}
