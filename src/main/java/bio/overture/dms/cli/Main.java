package bio.overture.dms.cli;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.BindOptions;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Mount;
import com.github.dockerjava.api.model.MountType;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient.Request;
import com.github.dockerjava.transport.DockerHttpClient.Response;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static bio.overture.dms.cli.Main.DockerService.resolveRepoTag;
import static java.lang.String.format;
import static java.nio.file.Files.readString;
import static java.util.Arrays.stream;

@Slf4j
public class Main {

  @SneakyThrows
  public static void main2(String[] args){
    val ex = Config.builder()
        .firstName("Robert")
        .lastName("Tisma")
        .age(5)
        .build();
    for (val field : Config.class.getDeclaredFields()){
      if (field.isAnnotationPresent(Question.class)){
        val question = field.getDeclaredAnnotation(Question.class).value();
        val getterName = "get"+field.getName().substring(0,1).toUpperCase()+field.getName().substring(1);
        val methodRef = Config.class.getDeclaredMethod(getterName);
        val value = methodRef.invoke(ex);
        log.info(question+" ----  "+value);

      }

    }
    log.info("sdf");
  }

  @RequiredArgsConstructor
  public static class DockerService {
    @NonNull private final DockerClient client;

    public void ping(){
      client.pingCmd().exec();
    }

    private Optional<Network> findNetwork(String networkName){
      return client.listNetworksCmd()
          .exec()
          .stream()
          .filter(x -> x.getName().equals(networkName))
          .findFirst();
    }

    public Network getNetwork(String networkName){
      return findNetwork(networkName)
          .orElseGet(() ->{
              client.createNetworkCmd()
                  .withName(networkName)
                  .exec();
              return findNetwork(networkName)
                  .orElseThrow(() -> new IllegalStateException(format("could not create network \"%s\"", networkName)));
              }
          );
    }

    @SneakyThrows
    private void pullImage(String repo, String tag){
      client.pullImageCmd(repo).withTag(tag)
          .start()
          .awaitCompletion();
    }

    private Optional<Image> findImage(String repo, String tag){
      return client.listImagesCmd()
          .exec()
          .stream()
          .filter(x -> matchTag(x.getRepoTags(), repo, tag))
          .findFirst();
    }

    private Image readImage(String repo, String tag){
      return findImage(repo,tag)
          .orElseThrow(() ->
              new IllegalStateException(format("The repo \"%s\" with tag \"%s\" does not exist", repo, tag)));
    }

    public Image getImage(String repo, String tag){
      pullImage(repo, tag);
      return readImage(repo, tag);
    }

    public void deleteImage(String repo, String tag){
      val image = readImage(repo, tag);
      client.removeImageCmd(image.getId()).exec();
    }

    public static String resolveRepoTag(String repo, String tag){
      return repo+":"+tag;
    }

    private static boolean matchTag(String[] repoTags, String inputRepo, String inputTag){
      return stream(repoTags).anyMatch(actualRepoTag -> actualRepoTag.equals(resolveRepoTag(inputRepo,inputTag)));
    }
  }

  private static Path readResourcePath(String filename) throws IOException, URISyntaxException {
    val is = Main.class.getClassLoader().getResource(filename);
    if (is == null){
      throw new IllegalArgumentException(format("The resource \"%s\" was not found", filename));
    }
    val path = Paths.get(is.toURI());
    if (!Files.exists(path)){
      throw new IllegalArgumentException(format("The resource \"%s\" was not found", filename));
    }
    return path;
  }

  @SneakyThrows
  public static void main(String[] args){
    val config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
    val httpClient = new ApacheDockerHttpClient.Builder()
        .dockerHost(config.getDockerHost())
        .sslConfig(config.getSSLConfig())
        .build();

    val request = Request.builder()
        .method(Request.Method.GET)
        .path("/_ping")
        .build();

    try (Response response = httpClient.execute(request)) {
      assert response.getStatusCode() == 200;
      assert String.join("\n", IOUtils.readLines(response.getBody(), Charset.defaultCharset())).equals("OK");
    }

    DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
    val docker= new DockerService(dockerClient);

    // Network
    val networkName = "RobTestNetwork";
    val network = docker.getNetwork(networkName);

    // Postgres
    val postgreRepo = "postgres";
    val postgresTag = "11.1";
    docker.pullImage(postgreRepo, postgresTag);
    val initPath = readResourcePath("./assets/ego-init/").toAbsolutePath().toString();

    val postgresContainer = dockerClient.createContainerCmd(resolveRepoTag(postgreRepo, postgresTag))
        .withName("ego-db")
        .withEnv("POSTGRES_DB=ego", "POSTGRES_PASSWORD=password")
        .withExposedPorts(ExposedPort.tcp(5432))
        .withHostConfig(
            HostConfig.newHostConfig()
                .withMounts(List.of(new Mount()
                    .withType(MountType.BIND)
                    .withReadOnly(true)
                    .withSource(initPath)
                    .withTarget("/docker-entrypoint-initdb.d")
                ))
                .withPortBindings(new PortBinding(Ports.Binding.bindPort(8432), ExposedPort.tcp(5432)))
        )
        .exec();


    dockerClient.connectToNetworkCmd()
        .withContainerId(postgresContainer.getId())
        .withNetworkId(network.getId())
        .exec();

    dockerClient.startContainerCmd(postgresContainer.getId()).exec();


    // Ego
    val egoRepo = "overture/ego";
    val egoTag = "3.1.0";
    docker.pullImage(egoRepo, egoTag);
    val egoContainer = dockerClient.createContainerCmd(resolveRepoTag(egoRepo, egoTag))
        .withName("ego-server")
        .withEnv("SERVER_PORT=8080",
            "SPRING_DATASOURCE_URL=jdbc:postgresql://ego-db:5432/ego?stringtype=unspecified",
            "SPRING_DATASOURCE_USERNAME=postgres",
            "SPRING_DATASOURCE_PASSWORD=password",
            "SPRING_FLYWAY_ENABLED=true",
            "SPRING_FLYWAY_LOCATIONS=classpath:flyway/sql,classpath:db/migration",
            "SPRING_PROFILES_ACTIVE=demo")
        .withExposedPorts(ExposedPort.tcp(8080))
        .withHostConfig(
            HostConfig.newHostConfig()
                .withPortBindings(new PortBinding(Ports.Binding.bindPort(8080), ExposedPort.tcp(8080))))
        .withCmd("java -jar /srv/ego/install/ego.jar")
        .exec();

    dockerClient.connectToNetworkCmd()
        .withContainerId(egoContainer.getId())
        .withNetworkId(network.getId())
        .exec();

    dockerClient.startContainerCmd(egoContainer.getId()).exec();

    //Cleanup
    dockerClient.killContainerCmd(egoContainer.getId()).exec();
    dockerClient.killContainerCmd(postgresContainer.getId()).exec();
    dockerClient.removeContainerCmd(egoContainer.getId()).exec();
    dockerClient.removeContainerCmd(postgresContainer.getId()).exec();
    dockerClient.removeNetworkCmd(network.getId()).exec();
    dockerClient.removeImageCmd(resolveRepoTag(egoRepo, egoTag)).exec();
    dockerClient.removeImageCmd(resolveRepoTag(postgreRepo, postgresTag)).exec();

    log.info("sdf");

  }


  public static class QuestionDTO<A,T> {

    @NonNull private final String text;
    @NonNull private final Class<T> fieldType;
    @NonNull private final Method getterMethod;
    @NonNull private final Method setterMethod;

    @Builder
    public QuestionDTO(@NonNull String text, @NonNull Class<T> fieldType,
        @NonNull Method getterMethod, @NonNull Method setterMethod) {
      this.text = text;
      this.fieldType = fieldType;
      this.getterMethod = getterMethod;
      this.setterMethod = setterMethod;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public T getValue(A object){
      return (T)getterMethod.invoke(object);
    }

    @SneakyThrows
    public void setValue(A object, T value){
      setterMethod.invoke(object, value);
    }

  }
}
