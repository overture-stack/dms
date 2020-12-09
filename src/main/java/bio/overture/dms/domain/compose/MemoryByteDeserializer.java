package bio.overture.dms.domain.compose;

import static bio.overture.dms.util.Exceptions.checkArgument;
import static java.lang.Long.parseLong;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import lombok.val;

public class MemoryByteDeserializer extends JsonDeserializer<Long> {

  private static final long K_MULTIPLIER = 1024;
  private static final long M_MULTIPLIER = K_MULTIPLIER * 1024;
  private static final long G_MULTIPLIER = M_MULTIPLIER * 1024;
  private static final long T_MULTIPLIER = G_MULTIPLIER * 1024;

  @Override
  public Long deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    val root = ctxt.readTree(p);
    val arr = root.asText().split("\\d+|[a-z]+");
    checkArgument(
        arr.length > 0 && arr.length <= 2,
        "Must define a single byte value, or a value and one of the units: [b, k/kb, m/mb, g/gb, t/tb]");
    val value = parseLong(arr[0].trim());
    String unit = "b";
    if (arr.length > 1) {
      unit = arr[1].trim().toLowerCase();
    }
    if (unit.equals("b")) {
      return value;
    } else if (unit.equals("k") || unit.equals("kb")) {
      return value * K_MULTIPLIER;
    } else if (unit.equals("m") || unit.equals("mb")) {
      return value * M_MULTIPLIER;
    } else if (unit.equals("g") || unit.equals("gb")) {
      return value * G_MULTIPLIER;
    } else if (unit.equals("t") || unit.equals("tb")) {
      return value * T_MULTIPLIER;
    } else {
      throw new IllegalStateException(
          "Undefined memory value. Please use one of: [b, k/kb, m/mb, g/gb, t/tb]");
    }
  }
}
