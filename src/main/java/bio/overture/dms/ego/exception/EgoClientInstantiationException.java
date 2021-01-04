package bio.overture.dms.ego.exception;

public class EgoClientInstantiationException extends RuntimeException {

  public EgoClientInstantiationException(String message) {
    super(message);
  }

  public EgoClientInstantiationException(String message, Throwable cause) {
    super(message, cause);
  }
}
