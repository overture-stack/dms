package bio.overture.dms.domain.compose;

public enum EndpointModes {
  VIP,
  DNSRR;

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
