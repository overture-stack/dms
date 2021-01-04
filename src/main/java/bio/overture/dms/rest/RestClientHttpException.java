package bio.overture.dms.rest;

import lombok.Getter;

public class RestClientHttpException extends RuntimeException {

  @Getter private final int httpStatusCode;

  public RestClientHttpException(int httpStatusCode) {
    this.httpStatusCode = httpStatusCode;
  }

  public RestClientHttpException(String message, int httpStatusCode) {
    super(message);
    this.httpStatusCode = httpStatusCode;
  }

  public RestClientHttpException(String message, Throwable cause, int httpStatusCode) {
    super(message, cause);
    this.httpStatusCode = httpStatusCode;
  }

  public RestClientHttpException(Throwable cause, int httpStatusCode) {
    super(cause);
    this.httpStatusCode = httpStatusCode;
  }

  public RestClientHttpException(
      String message,
      Throwable cause,
      boolean enableSuppression,
      boolean writableStackTrace,
      int httpStatusCode) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.httpStatusCode = httpStatusCode;
  }

  public boolean isClientError() {
    return httpStatusCode >= 400 && httpStatusCode < 500;
  }

  public boolean isServerError() {
    return httpStatusCode >= 500 && httpStatusCode < 600;
  }

  public boolean isError() {
    return isClientError() || isServerError();
  }

  public boolean isUnauthorizedError() {
    return httpStatusCode == 401;
  }

  public boolean isForbiddenError() {
    return httpStatusCode == 403;
  }
}
