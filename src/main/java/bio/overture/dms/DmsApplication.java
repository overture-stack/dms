package bio.overture.dms;

import static bio.overture.dms.DmsApplication.DockerService.resolveRepoTag;
import static java.lang.String.format;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Arrays.stream;

import bio.overture.dms.cli.Main;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.BindOptions;
import com.github.dockerjava.api.model.BindPropagation;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.TmpfsOptions;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.api.model.VolumeOptions;
import com.github.dockerjava.api.model.VolumesFrom;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.CopyOption;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;

@Slf4j
@SpringBootApplication
public class DmsApplication {

  @SneakyThrows
  public static void main(String[] args) {

    Path scratchPath = Paths.get("scratch");
    String networkName;
    if (args.length>1){
      scratchPath = Paths.get(args[0]);
      networkName = args[1];
    } else if (args.length > 0){
      networkName = args[0];
    } else {
      throw new IllegalArgumentException("must have either 2 (scratch dir and networkname) or 1 arg (just networkname)");
    }

    val dockerMode = System.getenv("DOCKER_MODE");
    val config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

    val httpClient =
        new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .build();

    val request =
        DockerHttpClient.Request.builder()
            .method(DockerHttpClient.Request.Method.GET)
            .path("/_ping")
            .build();

    try (DockerHttpClient.Response response = httpClient.execute(request)) {
      assert response.getStatusCode() == 200;
      assert String.join("\n", IOUtils.readLines(response.getBody(), Charset.defaultCharset()))
          .equals("OK");
    }

    DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
    val docker = new DmsApplication.DockerService(dockerClient);

    // Network
    val network = docker.getNetwork(networkName);

    // Postgres
    val postgreRepo = "postgres";
    val postgresTag = "11.1";
    docker.pullImage(postgreRepo, postgresTag);
    val resource =
        DmsApplication.readResourcePath("/assets/ego-init/init.sql");
    val postgresMountSrc = scratchPath.resolve("ego-init/").toAbsolutePath();
    Files.createDirectories(postgresMountSrc);
    Files.copy(resource.getInputStream(), postgresMountSrc.resolve("init.sql"), REPLACE_EXISTING);

    String thisContainerId = null;
    if (dockerMode != null){
      thisContainerId = Files.readAllLines(Paths.get("/proc/self/cgroup")).get(0).replaceAll(".*\\/", "");
      log.info("LiNEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEeeeeeEEEEEEEEEEEEEEEEEEEE:  containerId  "+thisContainerId);
    }

    /**
     * Issue: created containers also mount docker socket which is no good
     */
    val postgresContainerConfig =
        dockerClient
            .createContainerCmd(resolveRepoTag(postgreRepo, postgresTag))
            .withName("ego-db")
            .withEnv("POSTGRES_DB=ego", "POSTGRES_PASSWORD=password")
            .withExposedPorts(ExposedPort.tcp(5432));
    if (thisContainerId != null){
      postgresContainerConfig
          .withHostConfig(HostConfig.newHostConfig()
              .withPortBindings(
                  new PortBinding(Ports.Binding.bindPort(8432), ExposedPort.tcp(5432))));
    } else {
      postgresContainerConfig
            .withHostConfig(
          HostConfig.newHostConfig()
              .withMounts(
                  List.of(
                      new Mount()
                          .withType(MountType.BIND)
                          .withReadOnly(true)
                          .withSource(postgresMountSrc.toString())
                          .withTarget("/docker-entrypoint-initdb.d")))
              .withPortBindings(
                  new PortBinding(Ports.Binding.bindPort(8432), ExposedPort.tcp(5432))));
    }
    val postgresContainer = postgresContainerConfig.exec();

    dockerClient
        .connectToNetworkCmd()
        .withContainerId(postgresContainer.getId())
        .withNetworkId(network.getId())
        .exec();

    dockerClient.startContainerCmd(postgresContainer.getId()).exec();

    // Ego
    val egoRepo = "overture/ego";
    val egoTag = "3.1.0";
    docker.pullImage(egoRepo, egoTag);
    val egoContainer =
        dockerClient
            .createContainerCmd(resolveRepoTag(egoRepo, egoTag))
            .withName("ego-server")
            .withEnv(
                "SERVER_PORT=8080",
                "SPRING_DATASOURCE_URL=jdbc:postgresql://ego-db:5432/ego?stringtype=unspecified",
                "SPRING_DATASOURCE_USERNAME=postgres",
                "SPRING_DATASOURCE_PASSWORD=password",
                "SPRING_FLYWAY_ENABLED=true",
                "SPRING_FLYWAY_LOCATIONS=classpath:flyway/sql,classpath:db/migration",
                "SPRING_PROFILES_ACTIVE=demo")
            .withExposedPorts(ExposedPort.tcp(8080))
            .withHostConfig(
                HostConfig.newHostConfig()
                    .withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(8080), ExposedPort.tcp(8080))))
            .withCmd("java -jar /srv/ego/install/ego.jar")
            .exec();

    dockerClient
        .connectToNetworkCmd()
        .withContainerId(egoContainer.getId())
        .withNetworkId(network.getId())
        .exec();

    dockerClient.startContainerCmd(egoContainer.getId()).exec();

    // Cleanup
    //    dockerClient.killContainerCmd(egoContainer.getId()).exec();
    //    dockerClient.killContainerCmd(postgresContainer.getId()).exec();
    //    dockerClient.removeContainerCmd(egoContainer.getId()).exec();
    //    dockerClient.removeContainerCmd(postgresContainer.getId()).exec();
    //    dockerClient.removeNetworkCmd(network.getId()).exec();
    //    dockerClient.removeImageCmd(resolveRepoTag(egoRepo, egoTag)).exec();
    //    dockerClient.removeImageCmd(resolveRepoTag(postgreRepo, postgresTag)).exec();

    log.info("sdf");
  }

  public static void main2(String[] args) {
    SpringApplication.run(DmsApplication.class, args);
  }

  public static Resource readResourcePath(String filename) throws IOException, URISyntaxException {
    val resource = new DefaultResourceLoader().getResource(filename);
    if (!resource.exists()) {
      throw new IllegalArgumentException(format("The resource \"%s\" was not found", filename));
    }
    return resource;
  }

  @RequiredArgsConstructor
  public static class DockerService {
    @NonNull private final DockerClient client;

    public void ping() {
      client.pingCmd().exec();
    }

    private Optional<Network> findNetwork(String networkName) {
      return client.listNetworksCmd().exec().stream()
          .filter(x -> x.getName().equals(networkName))
          .findFirst();
    }

    public Network getNetwork(String networkName) {
      return findNetwork(networkName)
          .orElseGet(
              () -> {
                client.createNetworkCmd().withName(networkName).exec();
                return findNetwork(networkName)
                    .orElseThrow(
                        () ->
                            new IllegalStateException(
                                format("could not create network \"%s\"", networkName)));
              });
    }

    @SneakyThrows
    private void pullImage(String repo, String tag) {
      client.pullImageCmd(repo).withTag(tag).start().awaitCompletion();
    }

    private Optional<Image> findImage(String repo, String tag) {
      return client.listImagesCmd().exec().stream()
          .filter(x -> matchTag(x.getRepoTags(), repo, tag))
          .findFirst();
    }

    private Image readImage(String repo, String tag) {
      return findImage(repo, tag)
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      format("The repo \"%s\" with tag \"%s\" does not exist", repo, tag)));
    }

    public Image getImage(String repo, String tag) {
      pullImage(repo, tag);
      return readImage(repo, tag);
    }

    public void deleteImage(String repo, String tag) {
      val image = readImage(repo, tag);
      client.removeImageCmd(image.getId()).exec();
    }

    public static String resolveRepoTag(String repo, String tag) {
      return repo + ":" + tag;
    }

    private static boolean matchTag(String[] repoTags, String inputRepo, String inputTag) {
      return stream(repoTags)
          .anyMatch(actualRepoTag -> actualRepoTag.equals(resolveRepoTag(inputRepo, inputTag)));
    }
  }
}
