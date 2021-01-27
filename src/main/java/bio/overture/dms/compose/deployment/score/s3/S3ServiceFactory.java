package bio.overture.dms.compose.deployment.score.s3;

import static lombok.AccessLevel.PRIVATE;
import static software.amazon.awssdk.core.client.config.SdkAdvancedClientOption.SIGNER;
import static software.amazon.awssdk.core.retry.RetryMode.STANDARD;
import static software.amazon.awssdk.regions.Region.US_EAST_1;

import bio.overture.dms.core.model.dmsconfig.ScoreConfig.ScoreS3Config;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.signer.AwsS3V4Signer;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@NoArgsConstructor(access = PRIVATE)
public class S3ServiceFactory {

  private static final Duration API_CALL_TIMEOUT_DURATION = Duration.ofMillis(15000);
  public static S3Service buildS3Service(
      @NonNull ScoreS3Config scoreS3Config, @NonNull URI s3EndpointUri, Region region) {
    val s3 = buildS3Client(scoreS3Config, s3EndpointUri, region);
    return new S3Service(s3);
  }

  @SneakyThrows
  private static S3Client buildS3Client(ScoreS3Config scoreS3Config, URI s3EndpointUri, Region region) {
    val b = S3Client.builder();
    b.region(region);
    b.endpointOverride(s3EndpointUri);
    b.credentialsProvider(buildAwsCredentialsProvider(scoreS3Config));
    b.overrideConfiguration(buildS3Configuration(API_CALL_TIMEOUT_DURATION));
    return b.build();
  }

  private static ClientOverrideConfiguration buildS3Configuration(Duration callTimout) {
    return ClientOverrideConfiguration.builder()
        .apiCallTimeout(callTimout)
        .retryPolicy(STANDARD)
        .advancedOptions(Map.of(SIGNER, AwsS3V4Signer.create()))
        .build();
  }

  private static AwsCredentialsProvider buildAwsCredentialsProvider(ScoreS3Config scoreS3Config) {
    return StaticCredentialsProvider.create(
        AwsBasicCredentials.create(scoreS3Config.getAccessKey(), scoreS3Config.getSecretKey()));
  }
}
