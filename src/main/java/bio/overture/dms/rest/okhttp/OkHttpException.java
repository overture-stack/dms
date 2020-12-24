package bio.overture.dms.rest.okhttp;

import static java.lang.String.format;

import lombok.Getter;
import lombok.NonNull;
import okhttp3.Response;

public class OkHttpException extends RuntimeException {

  @Getter private final Response response;

  public OkHttpException(@NonNull Response response) {
    super();
    this.response = response;
  }

  @Override
  public String getMessage() {
    return format("OKHTTP ERROR [%s]: %s", response.code(), response.message());
  }

  public static void checkResponse(@NonNull Response response) {
    if (!response.isSuccessful()) {
      throw new OkHttpException(response);
    }
  }
}
