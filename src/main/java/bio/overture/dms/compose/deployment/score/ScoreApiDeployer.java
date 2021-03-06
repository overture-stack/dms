package bio.overture.dms.compose.deployment.score;

import static bio.overture.dms.compose.deployment.score.s3.S3ServiceFactory.buildS3Service;
import static bio.overture.dms.compose.model.ComposeServiceResources.*;
import static software.amazon.awssdk.regions.Region.US_EAST_1;

import bio.overture.dms.compose.deployment.ServiceDeployer;
import bio.overture.dms.compose.deployment.ego.EgoHelper;
import bio.overture.dms.compose.model.S3ObjectUploadRequest;
import bio.overture.dms.core.Messenger;
import bio.overture.dms.core.model.dmsconfig.DmsConfig;
import bio.overture.dms.core.model.dmsconfig.ScoreConfig;
import bio.overture.dms.core.model.dmsconfig.ScoreConfig.ScoreS3Config;
import bio.overture.dms.core.model.enums.ClusterRunModes;
import bio.overture.dms.swarm.properties.DockerProperties;
import java.net.URI;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;

@Slf4j
@Component
public class ScoreApiDeployer {

  /** Constants */
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

  private final Messenger messenger;

  @Autowired
  public ScoreApiDeployer(
      @NonNull ServiceDeployer serviceDeployer,
      @NonNull EgoHelper egoHelper,
      @NonNull DockerProperties dockerProperties,
      @NonNull Messenger messenger) {
    this.serviceDeployer = serviceDeployer;
    this.egoHelper = egoHelper;
    this.dockerProperties = dockerProperties;
    this.messenger = messenger;
  }

  public void deploy(@NonNull DmsConfig dmsConfig) {
    egoHelper.waitForEgoApiHealthy(dmsConfig.getClusterRunMode(), dmsConfig.getEgo());
    //    messenger.send("⏳ Provisioning needed data for '%s' ", SCORE_API.toString());
    serviceDeployer.deploy(dmsConfig, SCORE_API, true);
    provision(dmsConfig);
    //    messenger.send("✔️ Provisioning for '%s' completed", SCORE_API.toString());
    messenger.send(
        "\uD83C\uDFC1️ Deployment for service %s finished successfully", SCORE_API.toString());
  }

  private void provision(DmsConfig dmsConfig) {
    provisionS3Buckets(dmsConfig.getClusterRunMode(), dmsConfig.getScore());
  }

  private void provisionS3Buckets(ClusterRunModes clusterRunMode, ScoreConfig scoreConfig) {
    // TODO: May want to create interactive question for this in the future. For now, autocreate
    // buckets if minio is run
    val autoCreateBuckets = scoreConfig.getS3().isUseMinio();
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
            .overwriteData(false)
            .build());
  }

  @SneakyThrows
  private URI resolveS3Endpoint(ClusterRunModes clusterRunMode, ScoreS3Config scoreS3Config) {
    return resolveS3LocalUrl(scoreS3Config);
  }

  @SneakyThrows
  private URI resolveS3LocalUrl(ScoreS3Config scoreS3Config) {
    if (dockerProperties.getRunAs() && scoreS3Config.isUseMinio()) {
      return new URI("http://" + MINIO_API.toString() + ":" + MINIO_API_CONTAINER_PORT);
    }

    // this is needed to allow dms to create buckets in local dev mode
    // the proxy will override the host header which will alter the url signature
    // we need to create the buckets while bypassing the proxy since we are outside
    // the docker network
    if (scoreS3Config.isUseMinio() && !dockerProperties.getRunAs()) {
      return new URI("http://localhost:" + scoreS3Config.getHostPort());
    }

    // no minio
    return scoreS3Config.getUrl().toURI();
  }

  private static Region resolveS3Region(ScoreS3Config scoreS3Config) {
    if (scoreS3Config.isS3RegionDefined()) {
      return Region.of(scoreS3Config.getS3Region());
    } else {
      return DEFAULT_S3_REGION;
    }
  }

  private static String resolveObjectKey() {
    return DEFAULT_OBJECT_PATH + "/" + DEFAULT_OBJECT_SENTINEL;
  }
}
