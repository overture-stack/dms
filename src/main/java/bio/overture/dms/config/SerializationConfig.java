package bio.overture.dms.config;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

import bio.overture.dms.core.util.ObjectSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SerializationConfig {

  @Bean
  public ObjectSerializer jsonSerializer() {
    val mapper = new ObjectMapper().enable(INDENT_OUTPUT);
    return new ObjectSerializer(mapper);
  }

  @Bean
  public ObjectSerializer yamlSerializer() {
    val mapper = new ObjectMapper(new YAMLFactory()).enable(INDENT_OUTPUT);
    return new ObjectSerializer(mapper);
  }
}
