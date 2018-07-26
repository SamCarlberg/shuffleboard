package edu.wpi.first.shuffleboard.app.runtimesetup;

public final class StandardDependency implements Dependency {

  private final String group;
  private final String name;
  private final String version;
  private final String classifier;

  public static StandardDependency from(String coordinates) {
    String[] parts = coordinates.split(":");
    if (parts.length < 3) {
      return null;
    }
    return new StandardDependency(parts[0], parts[1], parts[2], parts.length == 4 ? parts[3] : null);
  }

  public StandardDependency(String group, String name, String version) {
    this(group, name, version, null);
  }

  public StandardDependency(String group, String name, String version, String classifier) {
    this.group = group;
    this.name = name;
    this.version = version;
    this.classifier = classifier;
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
    return classifier;
  }
}
