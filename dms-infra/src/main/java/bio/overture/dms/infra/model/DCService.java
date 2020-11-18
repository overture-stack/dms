package bio.overture.dms.infra.model;

import bio.overture.dms.core.Joiner;
import bio.overture.dms.core.Splitter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.val;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import static bio.overture.dms.infra.util.JsonProcessor.getFieldNames;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;

@Data
public class DCService {

  @JsonIgnore
  private String serviceName;

  private String image;

  @JsonDeserialize(using = StringEqualsDCMapDeserializer.class)
  private Map<String, String> environment = new TreeMap<>();

  private Set<String> expose;

  @JsonDeserialize(using = IntegerColonDCMapDeserializer.class)
  private Map<Integer, Integer> ports;

  @JsonProperty("depends_on")
  private Set<String> dependsOn;

  @JsonDeserialize(using = StringColonDCMapDeserializer.class)
  private Map<String, String> volumes;

  /**
   * Deserializers
   */
  public static class StringEqualsDCMapDeserializer extends AbstractDCMapDeserializer<String,String> {
    @Override protected Splitter getSplitter() {
      return Splitter.EQUALS;
    }

    @Override protected Joiner getJoiner() {
      return Joiner.EQUALS;
    }

    @Override protected Function<String, String> getKeyConversionFunction() {
      return identity();
    }

    @Override protected Function<String, String> getValueConversionFunction() {
      return identity();
    }
  }

  public static class IntegerColonDCMapDeserializer extends AbstractColonDCMapDeserializer<Integer, Integer> {

    @Override protected Function<String, Integer> getKeyConversionFunction() {
      return Integer::parseInt;
    }

    @Override protected Function<String, Integer> getValueConversionFunction() {
      return Integer::parseInt;
    }
  }


  public static class StringColonDCMapDeserializer extends AbstractColonDCMapDeserializer<String, String> {

    @Override
    protected Function<String, String> getKeyConversionFunction() {
      return identity();
    }

    @Override protected Function<String, String> getValueConversionFunction() {
      return identity();
    }
  }

  public static abstract class AbstractColonDCMapDeserializer<K,V> extends AbstractDCMapDeserializer<K,V> {
    @Override protected Splitter getSplitter() {
      return Splitter.COLON;
    }

    @Override protected Joiner getJoiner() {
      return Joiner.COLON;
    }
  }

  public static abstract class AbstractDCMapDeserializer<K,V> extends JsonDeserializer<Map<K, V>> {

    protected abstract Splitter getSplitter();
    protected abstract Joiner getJoiner();

    protected abstract Function<String,K> getKeyConversionFunction();
    protected abstract Function<String,V> getValueConversionFunction();

    @Override
    public Map<K, V> deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      val root = ctxt.readTree(p);
      if (root instanceof ObjectNode){
        return processObjectNode(root);
      } else if (root instanceof ArrayNode){
        return processArrayNode(root, p);
      } else {
        throw new IllegalStateException(format("Could not process json since not ArrayNode nor ObjectNode: %s", p.getCurrentLocation() ));
      }
    }


    private Map<K, V> processObjectNode(JsonNode root){
      return getFieldNames(root).stream()
          .collect(toUnmodifiableMap(x -> getKeyConversionFunction().apply(x), y -> getValueConversionFunction().apply(root.path(y).asText())));
    }

    private Map<K, V> processArrayNode(JsonNode root, JsonParser p){
      val arrayNode = (ArrayNode)root;
      val map = new HashMap<K, V>();
      arrayNode.elements().forEachRemaining(item -> {
            val input = item.asText();
            // make this a strategy
            val split = getSplitter().split(input,true);
            if (split.size() < 2){
              throw new IllegalStateException(format("Could not process value '%s' at: %s", input, p.getCurrentLocation()));
            }
            val key = getKeyConversionFunction().apply(split.get(0));
            val value = getValueConversionFunction().apply(getJoiner().join(split.stream().skip(1)));
            map.put(key, value);
          }
      );
      return Map.copyOf(map);
    }
  }
}
