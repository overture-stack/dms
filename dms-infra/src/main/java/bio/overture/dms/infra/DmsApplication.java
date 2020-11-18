package bio.overture.dms.infra;

import static bio.overture.dms.infra.docker.DockerService.resolveRepoTag;
import static bio.overture.dms.infra.util.FileUtils.readResourcePath;
import static java.lang.String.format;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Arrays.stream;

import bio.overture.dms.infra.docker.DockerService;
import bio.overture.dms.infra.docker.model.DockerImage;
import bio.overture.dms.infra.env.EnvProcessor;
import bio.overture.dms.infra.reflection.Reflector;
import bio.overture.dms.infra.util.FileUtils;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.reflections.Reflections;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class DmsApplication {

  @SneakyThrows
  public static void main(String[] args) {

    Path scratchPath = Paths.get("scratch");
    String networkName;
    if (args.length > 1) {
      scratchPath = Paths.get(args[0]);
      networkName = args[1];
    } else if (args.length > 0) {
      networkName = args[0];
    } else {
      throw new IllegalArgumentException(
          "must have either 2 (scratch dir and networkname) or 1 arg (just networkname)");
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
    val envProcessor = EnvProcessor.createEnvProcessor(Reflector.createReflector(Reflections.collect()));
    val docker = new DockerService(dockerClient, envProcessor);

    // Network
    val network = docker.getNetwork(networkName);

    // Postgres
    val postgreRepo = "postgres";
    val postgresTag = "11.1";
    docker.pullImage(DockerImage.builder()
        .repositoryName(postgreRepo)
        .tag(postgresTag)
        .build());
    val resource = readResourcePath("/assets/ego-init/init.sql");
    val postgresMountSrc = scratchPath.resolve("assets/ego-init/").toAbsolutePath();
    val initFile = postgresMountSrc.resolve("init.sql");
    Files.createDirectories(postgresMountSrc);
    Files.copy(resource.getInputStream(), initFile, REPLACE_EXISTING);

    String thisContainerId = null;
    if (dockerMode != null) {
      thisContainerId =
          Files.readAllLines(Paths.get("/proc/self/cgroup")).get(0).replaceAll(".*\\/", "");
      log.info(
          "LiNEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEeeeeeEEEEEEEEEEEEEEEEEEEE:  containerId  "
              + thisContainerId);
    }

    val postgresContainer =
        dockerClient
            .createContainerCmd(resolveRepoTag(postgreRepo, postgresTag))
            .withName("ego-db")
            .withEnv("POSTGRES_DB=ego", "POSTGRES_PASSWORD=password")
            .withExposedPorts(ExposedPort.tcp(5432))
            .withHostConfig(
                HostConfig.newHostConfig()
                    .withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(8432), ExposedPort.tcp(5432))))
            .exec();

    dockerClient
        .connectToNetworkCmd()
        .withContainerId(postgresContainer.getId())
        .withNetworkId(network.getId())
        .exec();

    /**
     * Instead of mounting a docker continer, just create the container and copy the files that are
     * needed on itital boot. In this case, its the init.sql file. You could alternatively just exec
     * execute the script, but that means you have to check if it was already initialized, which the
     * previous method already does.
     */
    dockerClient
        .copyArchiveToContainerCmd(postgresContainer.getId())
        .withHostResource(initFile.toAbsolutePath().toString())
        .withRemotePath("/docker-entrypoint-initdb.d")
        .exec();

    dockerClient.startContainerCmd(postgresContainer.getId()).exec();

    // Ego
    val egoRepo = "overture/ego";
    val egoTag = "3.1.0";
    docker.pullImage(DockerImage.builder()
        .repositoryName(egoRepo)
        .tag(egoTag)
        .build());
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

}
