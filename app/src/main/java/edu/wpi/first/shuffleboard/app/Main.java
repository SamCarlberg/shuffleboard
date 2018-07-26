package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.SystemProperties;
import edu.wpi.first.shuffleboard.app.runtimesetup.Dependency;
import edu.wpi.first.shuffleboard.app.runtimesetup.DependencyInstaller;
import edu.wpi.first.shuffleboard.app.runtimesetup.PlatformDependency;
import edu.wpi.first.shuffleboard.app.runtimesetup.StandardDependency;

import com.sun.javafx.application.LauncherImpl;

import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

  private static final Logger logger = Logger.getLogger("Shuffleboard Launcher");

  private static final File cacheDir = new File(SystemProperties.USER_HOME + "/Shuffleboard/libs");

  public static ClassLoader classLoader;
  private static Class<ShuffleboardPreloader> preloaderClass;
  private static Class<Shuffleboard> appClass;

  private Application app;

  private static final BiFunction<String, String, String> wpilibClassifier = (osName, osArch) -> {
    String lower = osName.toLowerCase();
    boolean x86_64 = osArch.contains("64");
    if (lower.startsWith("windows")) {
      if (x86_64) {
        return "windowsx86-64";
      } else {
        return "windowsx86";
      }
    } else if (lower.startsWith("mac")) {
      if (x86_64) {
        return "osxx86_64";
      } else {
        throw new UnsupportedOperationException("No binaries available for 32-bit macOS");
      }
    } else {
      if (x86_64) {
        return "linuxx86_64";
      } else {
        return "linuxx86";
      }
    }
  };

  private static final List<PlatformDependency> platformDependencies = Arrays.asList(
      new PlatformDependency("org.opencv", "opencv-jni", "3.2.0", wpilibClassifier),
      new PlatformDependency("edu.wpi.first.cscore", "cscore-jni", "2018.4.1-20180630034724-1073-gebd41fe", wpilibClassifier),
      javacppNatives("opencv", "3.4.1"),
      javacppNatives("ffmpeg", "3.4.2"),
      javacppNatives("flandmark", "1.07"),
      javacppNatives("flycapture", "2.11.3.121"),
      javacppNatives("libfreenect2", "0.2.0"),
      javacppNatives("libfreenect", "0.5.3"),
      javacppNatives("librealsense", "1.12.1"),
      javacppNatives("videoinput", "0.200")
  );

  private static PlatformDependency javacppNatives(String name, String version) {
    return new PlatformDependency("org.bytedeco.javacpp-presets", name, version + "-1.4.1", (osName, osArch) -> {
      String lower = osName.toLowerCase();
      boolean x86_64 = osArch.contains("64");
      if (lower.startsWith("windows")) {
        if (x86_64) {
          return "windows-x86_64";
        } else {
          return "windows-x86";
        }
      } else if (lower.startsWith("mac")) {
        if (x86_64) {
          return "macosx-x86_64";
        } else {
          throw new UnsupportedOperationException("No binaries available for 32-bit macOS");
        }
      } else {
        if (x86_64) {
          return "linux-x86_64";
        } else {
          return "linux-x86";
        }
      }
    });
  }

  public static void main(String[] args) throws Exception {
    Loggers.setupLoggers();
    LauncherImpl.launchApplication(Main.class, ShuffleboardPreloader.class, args);
  }

  @Override
  public void init() throws Exception {
    logger.info("Generating runtime classpath and configuring classloader");
    final long startTime = System.nanoTime();
    cacheDir.mkdirs();
    List<StandardDependency> standardDependencies = new ArrayList<>();
    for (String dep : Stream.of("api", "app", "base", "cameraserver", "networktables", "powerup")
        .map(it -> "/project-deps-" + it + ".txt")
        .collect(Collectors.toList())) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(Main.class.getResourceAsStream(dep)))) {
        reader.lines()
            .distinct()
            .map(StandardDependency::from)
            .filter(Objects::nonNull)
            .forEach(standardDependencies::add);
      }
    }
    List<Dependency> allDependencies = new ArrayList<>(standardDependencies);
    allDependencies.addAll(platformDependencies);
    DependencyInstaller dependencyInstaller = new DependencyInstaller(
        Arrays.asList(
            new RemoteRepository.Builder("Maven Central", "default", "http://repo1.maven.org/maven2").build(),
            new RemoteRepository.Builder("WPILib Release", "default", "http://first.wpi.edu/FRC/roborio/maven/release").build(),
            new RemoteRepository.Builder("WPILib Development", "default", "http://first.wpi.edu/FRC/roborio/maven/development").build(),
            new RemoteRepository.Builder("Sam Carlberg", "default", "https://dl.bintray.com/samcarlberg/maven-artifacts/").build()
        ),
        allDependencies,
        cacheDir
    );
    dependencyInstaller.getSession().setTransferListener(new AbstractTransferListener() {
      @Override
      public void transferStarted(TransferEvent event) {
        notifyPreloader(new ShuffleboardPreloader.StateNotification("Downloading " + event.getResource().getFile().getName(), 0));
      }

      @Override
      public void transferProgressed(TransferEvent event) {
        notifyPreloader(new ShuffleboardPreloader.StateNotification("Downloading " + event.getResource().getFile().getName(), (double) event.getTransferredBytes() / event.getResource().getContentLength()));
      }
    });
    dependencyInstaller.getSession().setRepositoryListener(new AbstractRepositoryListener() {
      @Override
      public void artifactResolving(RepositoryEvent event) {
        notifyPreloader(new ShuffleboardPreloader.StateNotification("Resolving " + event.getArtifact(), 0));
      }

      @Override
      public void artifactResolved(RepositoryEvent event) {
        notifyPreloader(new ShuffleboardPreloader.StateNotification("Resolving " + event.getArtifact(), 1));
      }
    });
    notifyPreloader(new ShuffleboardPreloader.StateNotification("Resolving dependencies", 0));
    long start = System.nanoTime();
    dependencyInstaller.resolve();
    long end = System.nanoTime();
    logger.info(String.format("Took %.2f ms to resolve dependencies", (end - start) / 1e6));
    notifyPreloader(new ShuffleboardPreloader.StateNotification("Loading dependencies", 0.95));

    List<URL> urls = Files.list(cacheDir.toPath())
        .filter(p -> p.toAbsolutePath().toString().endsWith(".jar"))
        .map(Main::pathToUrl)
        .collect(Collectors.toList());
    logger.info("Additional classpath entries: " + urls);

    // Note: this hack does NOT work above Java 8
    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
    addURL.setAccessible(true);
    for (URL url : urls) {
      addURL.invoke(sysLoader, url);
    }
    classLoader = sysLoader;

    start = System.nanoTime();
    preloaderClass = (Class<ShuffleboardPreloader>) classLoader.loadClass("edu.wpi.first.shuffleboard.app.ShuffleboardPreloader");
    appClass = (Class<Shuffleboard>) classLoader.loadClass("edu.wpi.first.shuffleboard.app.Shuffleboard");

    app = appClass.newInstance();
    end = System.nanoTime();
    logger.info(String.format("Took %.2f ms to load the preloader and application classes", (end - start) / 1e6));

    logger.info(String.format("Launching Shuffleboard (took %.2f ms to generate classpath)", (System.nanoTime() - startTime) / 1e6));
    app.init();
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    app.start(primaryStage);
  }

  private static URL pathToUrl(Path path) {
    try {
      return path.toAbsolutePath().toUri().toURL();
    } catch (MalformedURLException e) {
      throw new IllegalStateException(e);
    }
  }

}
