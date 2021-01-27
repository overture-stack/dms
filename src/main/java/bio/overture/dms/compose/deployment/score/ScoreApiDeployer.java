package bio.overture.dms.compose.deployment.score;

import bio.overture.dms.compose.deployment.ServiceDeployer;
import bio.overture.dms.compose.deployment.ego.EgoHelper;
import bio.overture.dms.compose.model.S3ObjectUploadRequest;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.EgoConfig;
import bio.overture.dms.core.model.dmsconfig.ScoreConfig;
import bio.overture.dms.core.model.dmsconfig.ScoreConfig.ScoreApiConfig;
import bio.overture.dms.core.model.dmsconfig.ScoreConfig.ScoreS3Config;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import bio.overture.dms.swarm.properties.DockerProperties;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;

import java.net.URI;

import static bio.overture.dms.compose.deployment.SimpleProvisionService.createSimpleProvisionService;
import static bio.overture.dms.compose.deployment.score.s3.S3ServiceFactory.buildS3Service;
import static bio.overture.dms.compose.model.ComposeServiceResources.MINIO_API;
import static bio.overture.dms.compose.model.ComposeServiceResources.SCORE_API;
import static bio.overture.dms.compose.model.Constants.DMS_ADMIN_GROUP_NAME;
import static bio.overture.dms.core.model.enums.ClusterRunModes.LOCAL;
import static bio.overture.dms.core.model.enums.ClusterRunModes.PRODUCTION;
import static java.lang.String.format;
import static software.amazon.awssdk.regions.Region.US_EAST_1;

@Slf4j
@Component
public class ScoreApiDeployer {


  /** Constants */
  private static final String SCORE_POLICY_NAME = "SCORE";
  private static final String HELIOGRAPH_CONTENT = "DMS Score Heliograph Object";
  private static final Region DEFAULT_S3_REGION = US_EAST_1;

  // NOTE: the DEFAULT_OBJECT_PATH is baked into the score-api.yaml.vm
  private static final String DEFAULT_OBJECT_PATH = "data";
  // NOTE: the DEFAULT_OBJECT_SENTINEL is baked into the score-api.yaml.vm
  private static final String DEFAULT_OBJECT_SENTINEL = "heliograph";
  private static final int MINIO_API_CONTAINER_PORT = 9000;

  /** Dependencies */
  private final ServiceDeployer serviceDeployer;
  private final EgoHelper egoHelper;
  private final DockerProperties dockerProperties;

  @Autowired
  public ScoreApiDeployer(@NonNull ServiceDeployer serviceDeployer, @NonNull EgoHelper egoHelper,
      @NonNull DockerProperties dockerProperties) {
    this.serviceDeployer = serviceDeployer;
    this.egoHelper = egoHelper;
    this.dockerProperties = dockerProperties;
  }

  public void deploy(@NonNull DmsConfig dmsConfig) {
    egoHelper.waitForEgoApiHealthy(dmsConfig.getClusterRunMode(), dmsConfig.getEgo());
    serviceDeployer.deploy(dmsConfig, SCORE_API, true);
    provision(dmsConfig);
  }

  private void provision(DmsConfig dmsConfig) {
    buildEgoScoreProvisioner(dmsConfig.getEgo(), dmsConfig.getScore().getApi()).run();
    provisionS3Buckets(dmsConfig.getClusterRunMode(), dmsConfig.getScore());
  }

  private void provisionS3Buckets(ClusterRunModes clusterRunMode, ScoreConfig scoreConfig) {
    // TODO: May want to create interactive question for this in the future. For now, autocreate
    // buckets if minio is run
    val autoCreateBuckets = clusterRunMode == LOCAL;
    val endpointUri = resolveS3Endpoint(clusterRunMode, scoreConfig.getS3());
    val region = resolveS3Region(scoreConfig.getS3());
    val s3Service = buildS3Service(scoreConfig.getS3(), endpointUri, region);
    s3Service.provisionBucket(scoreConfig.getApi().getStateBucket(), autoCreateBuckets);
    s3Service.uploadData(
        S3ObjectUploadRequest.builder()
            .autoCreateBucket(autoCreateBuckets)
            .bucketName(scoreConfig.getApi().getObjectBucket())
            .objectKey(resolveObjectKey())
            .data(HELIOGRAPH_CONTENT.getBytes())
            .build());
  }

  @SneakyThrows
  private URI resolveS3Endpoint(
      ClusterRunModes clusterRunMode, ScoreS3Config scoreS3Config) {
    if (clusterRunMode == PRODUCTION) {
      return scoreS3Config.getUrl().toURI();
    } else if (clusterRunMode == LOCAL) {
      return resolveMinioContainerUri(scoreS3Config);
    } else {
      throw new IllegalStateException(
          format(
              "The clusterRunMode '%s' is unknown and cannot be processed", clusterRunMode.name()));
    }
  }

  @SneakyThrows
  private URI resolveMinioContainerUri(ScoreS3Config scoreS3Config) {
    if(dockerProperties.getRunAs()){
      return new URI("http://" + MINIO_API.toString() + ":" + MINIO_API_CONTAINER_PORT);
    } else {
      return scoreS3Config.getUrl().toURI();
    }
  }

  private EgoScoreProvisioner buildEgoScoreProvisioner(
      EgoConfig egoConfig, ScoreApiConfig scoreApiConfig) {
    val egoService = egoHelper.buildEgoService(egoConfig);
    val simpleProvisionService = createSimpleProvisionService(egoService);
    return EgoScoreProvisioner.builder()
        .simpleProvisionService(simpleProvisionService)
        .dmsGroupName(DMS_ADMIN_GROUP_NAME)
        .scorePolicyName(SCORE_POLICY_NAME)
        .scoreAppCredential(scoreApiConfig.getScoreAppCredential())
        .build();
  }

  private static Region resolveS3Region(ScoreS3Config scoreS3Config){
    if (scoreS3Config.isS3RegionDefined()){
      return Region.of(scoreS3Config.getS3Region());
    } else {
      return DEFAULT_S3_REGION;
    }
  }

  private static String resolveObjectKey() {
    return DEFAULT_OBJECT_PATH + "/" + DEFAULT_OBJECT_SENTINEL;
  }

}
