package bio.overture.dms.docker;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toMap;

import bio.overture.dms.core.spec.song.SongServiceSpec;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SongEnum {
  SERVER_PORT(SongServiceSpec::getServerPort);

  private final Function<SongServiceSpec, Object> getter;

  private String getValue(SongServiceSpec p) {
    return getter.apply(p).toString();
  }

  public static Map<String, String> generateEnvVars(SongServiceSpec properties) {
    return stream(SongEnum.values()).collect(toMap(Enum::name, x -> x.getValue(properties)));
  }
}
