package bio.overture.dms.domain.compose;

import static bio.overture.dms.util.Exceptions.checkArgument;
import static java.lang.Long.parseLong;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import lombok.val;

// Durations specified as {value}{us/ms/s/m/h} and can combine multiple value+unit pairs
public class DurationNsDeserializer extends JsonDeserializer<Long> {

  @Override
  public Long deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    val root = ctxt.readTree(p);
    val arr = root.asText().split("\\d+|[a-z]+");
    checkArgument(arr.length > 0, "At least 1 duration must be defined");
    checkArgument(
        arr.length % 2 > 0, "All values must have an associated unit: ns, us, ms, s, m, or h");
    Duration total = Duration.of(0, ChronoUnit.NANOS);
    for (int i = 0; i < arr.length; i += 2) {
      val value = parseLong(arr[i]);
      val unit = arr[i + 1];
      TemporalUnit temporalUnit = null;
      if (unit.equals("ns")) {
        temporalUnit = ChronoUnit.NANOS;
      } else if (unit.equals("us")) {
        temporalUnit = ChronoUnit.MICROS;
      } else if (unit.equals("ms")) {
        temporalUnit = ChronoUnit.MILLIS;
      } else if (unit.equals("s")) {
        temporalUnit = ChronoUnit.SECONDS;
      } else if (unit.equals("m")) {
        temporalUnit = ChronoUnit.MINUTES;
      } else if (unit.equals("h")) {
        temporalUnit = ChronoUnit.HOURS;
      } else {
        throw new IllegalStateException(
            "Undefined duration unit. Please use one of: [ns, us, ms, s, m, h]");
      }

      val dur = Duration.of(value, temporalUnit);
      total = total.plus(dur);
    }
    return total.toNanos();
  }
}
