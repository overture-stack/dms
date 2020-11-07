package bio.overture.dms.docker;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

import bio.overture.dms.core.properties.song.SongServiceProperties;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SongEnum {
  SERVER_PORT(SongServiceProperties::getServerPort);

  private final Function<SongServiceProperties, Object> getter;

  private String getValue(SongServiceProperties p) {
    return getter.apply(p).toString();
  }

  public static Map<String, String> generateEnvVars(SongServiceProperties properties) {
    return stream(SongEnum.values()).collect(toMap(Enum::name, x -> x.getValue(properties)));
  }
}
