package bio.overture.dms.compose.config;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import bio.overture.dms.core.util.ObjectSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.JsonValueSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.val;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static java.util.Objects.nonNull;

@Configuration
public class VelocityConfig {

  @Bean
  public VelocityEngine velocityEngine() {
    val props = new Properties();
    props.put("runtime.references.strict", "true");
    props.put("resource.loader", "class");
    props.put(
        "class.resource.loader.class",
        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    return new VelocityEngine(props);
  }

  /**
   * This is a special instance of an ObjectSerializer, tweaked to serialize URL objects as an
   * Object and not a String. Usually, URL objects are serialized as strings,
   * but for Velocity templating, we want to be able to access the properties of
   * a URL object, and this cannot be done if its serialized to a string.
   */
  @Bean
  public ObjectSerializer velocitySerializer(){
    val module = new SimpleModule().addSerializer(URL.class, new CustomUrlSerializer());
    val mapper = new ObjectMapper()
        .registerModule(module)
        .enable(INDENT_OUTPUT);
    return new ObjectSerializer(mapper);
  }

  /**
   * By default, when Jackson converts an object to a Map, it will convert URLs to a string.
   * While this makes sense for most use-cases, for Velocity templating it make sense to convert
   * a URL to a Map so that the different components of the URL can be accessed individually.
   * If the string is required for the velocity template, then by calling `$url.full`,
   * the fully rendered URL string will be used.
   */
  public static class CustomUrlSerializer extends JsonSerializer<URL>{

    @Override
    public void serialize(URL value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      gen.writeStartObject();
      if (nonNull(value)){
        gen.writeStringField("protocol", value.getProtocol());
        gen.writeStringField("host", value.getHost());
        gen.writeNumberField("port", value.getPort());
        gen.writeStringField("path", value.getPath());
        gen.writeStringField("query", value.getQuery());
        gen.writeStringField("file", value.getFile());
        gen.writeStringField("full", value.toString());
      }
      gen.writeEndObject();
    }
  }
}
