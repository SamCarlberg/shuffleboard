package edu.wpi.first.shuffleboard.app.runtimesetup;

import edu.wpi.first.shuffleboard.api.util.SystemProperties;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class DependencyInstaller {

  private final List<? extends RemoteRepository> repositories;
  private final List<? extends Dependency> dependencies;
  private final File cacheDir;
  private final RepositorySystem system = newRepositorySystem();
  private final DefaultRepositorySystemSession session = newRepositorySystemSession(system);

  public DependencyInstaller(List<? extends RemoteRepository> repositories, List<? extends Dependency> dependencies, File cacheDir) {
    this.repositories = repositories;
    this.dependencies = dependencies;
    this.cacheDir = cacheDir;
  }

  public DefaultRepositorySystemSession getSession() {
    return session;
  }

  public void resolve() throws DependencyResolutionException, ArtifactResolutionException, IOException {
    System.out.println("Resolving artifacts");
    if (!cacheDir.exists() || !cacheDir.isDirectory()) {
      cacheDir.mkdirs();
    }
    ArrayList<RemoteRepository> repositories = new ArrayList<>(this.repositories);
    for (Dependency dependency : dependencies) {
      Artifact artifact;
      if (dependency.getClassifier() == null) {
        artifact = new DefaultArtifact(dependency.generateCoords());
      } else {
        artifact = new DefaultArtifact(dependency.getGroup(), dependency.getName(), dependency.getClassifier(), "jar", dependency.getVersion());
      }
      CollectRequest collectRequest = new CollectRequest();
      collectRequest.setRoot(new org.eclipse.aether.graph.Dependency(artifact, null, false));
      collectRequest.setRepositories(repositories);

      ArtifactRequest artifactRequest = new ArtifactRequest(artifact, repositories, null);
      DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);

      ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
      DependencyResult dependencyResult = system.resolveDependencies(session, dependencyRequest);

      File artifactFile = artifactResult.getArtifact().getFile();

      Files.copy(artifactFile.toPath(), new File(cacheDir, artifactFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
      for (ArtifactResult result : dependencyResult.getArtifactResults()) {
        File file = result.getArtifact().getFile();
        Files.copy(file.toPath(), new File(cacheDir, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
    }
  }

  public static RepositorySystem newRepositorySystem() {
    /*
     * Aether's components implement org.eclipse.aether.spi.locator.Service to ease manual wiring and using the
     * prepopulated DefaultServiceLocator, we only need to register the repository connector and transporter
     * factories.
     */
    DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
    locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
    locator.addService(TransporterFactory.class, FileTransporterFactory.class);
    locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

    locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
      @Override
      public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
        exception.printStackTrace();
      }
    });

    return locator.getService(RepositorySystem.class);
  }


  public static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
    DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

    LocalRepository localRepo = new LocalRepository(SystemProperties.USER_HOME + "/.m2/repository");
    session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));

    // uncomment to generate dirty trees
    // session.setDependencyGraphTransformer( null );

    return session;
  }


}
