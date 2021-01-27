package bio.overture.dms.compose.deployment.score.s3;

import static bio.overture.dms.core.util.Exceptions.checkState;

import bio.overture.dms.compose.model.S3ObjectUploadRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Slf4j
@RequiredArgsConstructor
public class S3Service {

  @NonNull private final S3Client s3;

  /** Provisions the request bucket followed by uploading data to the specified object key path */
  @SneakyThrows
  public PutObjectResponse uploadData(@NonNull S3ObjectUploadRequest request) {
    provisionBucket(request.getBucketName(), request.isAutoCreateBucket());
    log.info(
        "Uploading data to bucket '{}' with objectKey '{}'",
        request.getBucketName(),
        request.getObjectKey());
    val resp = putObject(request.getBucketName(), request.getObjectKey(), request.getData());
    log.info("- Done uploading data to bucket");
    return resp;
  }

  /**
   * Creates a bucket if it does not exist and autoCreateBucket is true. If autoCreateBucket is
   * false, an error is thrown. If the bucket already exists, then bucket creation is skipped.
   *
   * @param bucketName to create
   * @param autoCreateBucket indicated whether the bucket should be created if it does not exist.
   */
  public void provisionBucket(@NonNull String bucketName, boolean autoCreateBucket) {
    if (!autoCreateBucket) {
      checkBucketExists(bucketName);
    } else if (!isBucketExist(bucketName)) {
      log.info("Creating non-existent bucket '{}'", bucketName);
      createBucket(bucketName);
      log.info("-  Done");
    } else {
      log.info("The bucket '{}' already exists, skipping bucket creation.", bucketName);
    }
  }

  private PutObjectResponse putObject(String bucketName, String objectKey, byte[] data) {
    return s3.putObject(
        PutObjectRequest.builder().bucket(bucketName).key(objectKey).build(),
        RequestBody.fromBytes(data));
  }

  private boolean isBucketExist(@NonNull String bucketName) {
    return s3.listBuckets().buckets().stream()
        .map(Bucket::name)
        .anyMatch(x -> x.equals(bucketName));
  }

  private void checkBucketExists(String bucketName) {
    checkState(isBucketExist(bucketName), "The bucket '%s' does not exist", bucketName);
  }

  private void createBucket(@NonNull String bucketName) {
    s3.createBucket(x -> x.bucket(bucketName));
  }
}
