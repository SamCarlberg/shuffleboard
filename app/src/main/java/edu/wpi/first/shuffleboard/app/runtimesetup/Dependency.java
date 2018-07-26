package edu.wpi.first.shuffleboard.app.runtimesetup;

public interface Dependency {

  String getGroup();

  String getName();

  String getVersion();

  String getClassifier();

  default String generateCoords() {
    StringBuilder builder = new StringBuilder()
        .append(getGroup()).append(':')
        .append(getName()).append(':')
        .append(getVersion());

    String classifier = getClassifier();
    if (classifier != null && !classifier.isEmpty()) {
      builder.append(':').append(classifier);
      return builder.toString();
    }
    return builder.toString();
  }

}
