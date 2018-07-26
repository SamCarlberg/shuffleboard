package edu.wpi.first.shuffleboard.plugin.cameraserver;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.plugin.Description;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.plugin.Requires;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;
import edu.wpi.first.shuffleboard.api.widget.WidgetType;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.type.CameraServerDataType;
import edu.wpi.first.shuffleboard.plugin.cameraserver.source.CameraServerSourceType;
import edu.wpi.first.shuffleboard.plugin.cameraserver.source.CameraStreamAdapter;
import edu.wpi.first.shuffleboard.plugin.cameraserver.widget.CameraServerWidget;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.wpi.cscore.CameraServerJNI;

import org.opencv.core.Core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Description(
    group = "edu.wpi.first.shuffleboard",
    name = "CameraServer",
    version = "1.2.0",
    summary = "Provides sources and widgets for viewing CameraServer MJPEG streams"
)
@Requires(group = "edu.wpi.first.shuffleboard", name = "NetworkTables", minVersion = "1.0.0")
public class CameraServerPlugin extends Plugin {

  private static final Logger log = Logger.getLogger(CameraServerPlugin.class.getName());
  private final CameraStreamAdapter streamRecorder = new CameraStreamAdapter();

  @Override
  public void onLoad() {
    try {
      // Workaround. CameraServer depends on OpenCV 3.2.0, but JavaCV uses 3.4.1. The latter overrides the former,
      // so we need to revert it so that cscore will function.
      Field version = Core.class.getField("VERSION");
      Field modifiers = Field.class.getDeclaredField("modifiers");
      modifiers.setAccessible(true);
      modifiers.set(version, version.getModifiers() & ~Modifier.FINAL);
      version.setAccessible(true);
      version.set(null, "3.2.0");
      Field native_library_name = Core.class.getField("NATIVE_LIBRARY_NAME");
      modifiers.set(native_library_name, native_library_name.getModifiers() & ~Modifier.FINAL);
      native_library_name.setAccessible(true);
      native_library_name.set(null, "opencv_java320");
    } catch (ReflectiveOperationException e) {
      e.printStackTrace();
    }
    log.info("OpenCV version: " + Core.VERSION);
    log.info("OpenCV core library name: " + Core.NATIVE_LIBRARY_NAME);
    log.info("OpenCV library location: " + Core.class.getProtectionDomain().getCodeSource().getLocation());
    // Make sure the JNI is loaded. If it's not, this plugin can't work!
    // Calling a function from CameraServerJNI will extract the OpenCV JNI dependencies and load them
    CameraServerJNI.setTelemetryPeriod(1);
  }

  @Override
  public List<ComponentType> getComponents() {
    return ImmutableList.of(
        WidgetType.forAnnotatedWidget(CameraServerWidget.class)
    );
  }

  @Override
  public Map<DataType, ComponentType> getDefaultComponents() {
    return ImmutableMap.of(
        CameraServerDataType.Instance, WidgetType.forAnnotatedWidget(CameraServerWidget.class)
    );
  }

  @Override
  public List<SourceType> getSourceTypes() {
    return ImmutableList.of(
        CameraServerSourceType.INSTANCE
    );
  }

  @Override
  public List<TypeAdapter> getTypeAdapters() {
    return ImmutableList.of(
        streamRecorder
    );
  }

  @Override
  public List<DataType> getDataTypes() {
    return ImmutableList.of(
        CameraServerDataType.Instance
    );
  }

}
