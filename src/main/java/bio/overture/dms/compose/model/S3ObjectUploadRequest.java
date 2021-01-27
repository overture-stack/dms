package bio.overture.dms.compose.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class S3ObjectUploadRequest {

  private final boolean autoCreateBucket;
  private final boolean overwriteData;
  @NonNull private final String bucketName;
  @NonNull private final String objectKey;
  @NonNull private final byte[] data;
}
