package bio.overture.dms.infra.converter;

import static bio.overture.dms.core.util.RandomGenerator.createRandomGenerator;
import static java.lang.String.format;
import static java.util.Objects.isNull;

import bio.overture.dms.core.util.Nullable;
import bio.overture.dms.core.util.RandomGenerator;
import bio.overture.dms.infra.docker.model.DockerContainer;
import bio.overture.dms.infra.docker.model.DockerImage;
import bio.overture.dms.infra.properties.service.FlywayProperties;
import bio.overture.dms.infra.properties.service.PostgresServiceProperties;
import bio.overture.dms.infra.properties.service.ego.ClientDatabaseProperties;
import bio.overture.dms.infra.properties.service.ego.EgoApiServiceProperties;
import bio.overture.dms.infra.properties.service.ego.RefreshTokenProperties;
import bio.overture.dms.infra.properties.service.ego.SSOProperties;
import bio.overture.dms.infra.properties.service.ego.SwaggerProperties;
import bio.overture.dms.infra.spec.EgoSpec;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class EgoContainerConverter implements Converter<EgoSpec, List<DockerContainer<?>>> {

  /** Constants */
  private static final RandomGenerator RANDOM_GENERATOR =
      createRandomGenerator(EgoContainerConverter.class.getSimpleName());

  /** Config */
  @NonNull private final String networkName;

  @Override
  public List<DockerContainer<?>> convert(EgoSpec input) {
    val dbDockerContainer = buildDbDockerContainer(networkName, input);
    val apiServiceProperty = buildApiServiceProperties(input, dbDockerContainer);
    val apiDockerContainer = buildApiDockerContainer(networkName, apiServiceProperty);
    return List.of(dbDockerContainer, apiDockerContainer);
  }

  private static DockerContainer<PostgresServiceProperties> buildDbDockerContainer(
      @NonNull String networkName, @NonNull EgoSpec egoSpec) {
    val dbServiceProperties = buildEgoDBServiceProperty(egoSpec);
    return DockerContainer.<PostgresServiceProperties>builder()
        .name("ego-db")
        .network(networkName)
        .dockerImage(DockerImage.builder().repositoryName("postgres").tag("11").build())
        .exposedPort(5432)
        .serviceProperties(dbServiceProperties)
        .build();
  }

  private static PostgresServiceProperties buildEgoDBServiceProperty(@NonNull EgoSpec egoSpec) {
    val databasePassword =
        egoSpec.isDatabasePasswordDefined()
            ? egoSpec.getDatabasePassword()
            : RANDOM_GENERATOR.generateRandomAsciiString(25);
    return PostgresServiceProperties.builder()
        .dbName("ego")
        .username("postgres")
        .password(databasePassword)
        .build();
  }

  private static EgoApiServiceProperties buildApiServiceProperties(
      EgoSpec egoSpec, DockerContainer<PostgresServiceProperties> dbDockerContainer) {
    return EgoApiServiceProperties.builder()
        .serverPort(8080)
        .springProfilesActive("auth,grpc,jwt")
        .googleClient(
            isNull(egoSpec.getSso()) ? null : convertToSSOProperties(egoSpec.getSso().getGoogle()))
        .githubClient(
            isNull(egoSpec.getSso()) ? null : convertToSSOProperties(egoSpec.getSso().getGithub()))
        .linkedinClient(
            isNull(egoSpec.getSso())
                ? null
                : convertToSSOProperties(egoSpec.getSso().getLinkedin()))
        .facebookClient(
            isNull(egoSpec.getSso())
                ? null
                : convertToSSOProperties(egoSpec.getSso().getFacebook()))
        .apiTokenDurationDays(egoSpec.getApiTokenDurationDays())
        .jwtDurationMs(egoSpec.getJwtDurationMS())
        .refreshToken(
            RefreshTokenProperties.builder()
                .cookieIsSecure(true)
                .domain(egoSpec.getHost())
                .durationMs(egoSpec.getRefreshTokenDurationMS())
                .build())
        .db(buildClientDatabaseProperties(dbDockerContainer))
        .flyway(
            FlywayProperties.builder()
                .enabled(true)
                .locations("classpath:flyway/sql,classpath:db/migration")
                .build())
        .swagger(SwaggerProperties.builder().baseUrl("/").host(egoSpec.getHost()).build())
        .build();
  }

  private static ClientDatabaseProperties buildClientDatabaseProperties(
      @NonNull DockerContainer<PostgresServiceProperties> dbDockerContainer) {
    val prop = dbDockerContainer.getServiceProperties();
    InvalidSpecException.checkInvalidSpec(
        dbDockerContainer.getExposedPorts().size() == 1,
        "The db docker container for the ego context can only have 1 exposed port, but instead has {}",
        dbDockerContainer.getExposedPorts().size());
    val port = dbDockerContainer.getExposedPorts().stream().findFirst().get();
    val url =
        format(
            "jdbc:postgresql://%s:%s/%s?stringtype=unspecified",
            dbDockerContainer.getName(), port, prop.getDbName());
    return ClientDatabaseProperties.builder()
        .url(url)
        .username(prop.getUsername())
        .password(prop.getPassword())
        .build();
  }

  private static SSOProperties convertToSSOProperties(@Nullable EgoSpec.SSOClientSpec spec) {
    if (!isNull(spec)) {
      return SSOProperties.builder()
          .clientId(spec.getClientId())
          .clientSecret(spec.getClientSecret())
          .preEstablishedRedirectUri(spec.getPreEstablishedRedirectUri())
          .build();
    }
    return null;
  }

  private static DockerContainer<EgoApiServiceProperties> buildApiDockerContainer(
      String networkName, EgoApiServiceProperties apiServiceProperties) {
    return DockerContainer.<EgoApiServiceProperties>builder()
        .name("ego-api")
        .network(networkName)
        .dockerImage(
            DockerImage.builder()
                .accountName("overture")
                .repositoryName("ego")
                .tag("3.3.0")
                .build())
        .exposedPort(8080)
        .serviceProperties(apiServiceProperties)
        .build();
  }
}
