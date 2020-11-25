package bio.overture.dms.infra.config;

import bio.overture.dms.infra.util.JsonProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

  @Bean
  public JsonProcessor jsonProcessor() {
    val mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    return new JsonProcessor(mapper);
  }

  @Bean
  public JsonProcessor yamlProcessor() {
    val mapper = new ObjectMapper(new YAMLFactory()).enable(SerializationFeature.INDENT_OUTPUT);
    return new JsonProcessor(mapper);
  }
}
