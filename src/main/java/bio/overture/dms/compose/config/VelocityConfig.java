package bio.overture.dms.compose.config;

import java.util.Properties;
import lombok.val;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VelocityConfig {

  @Bean
  public VelocityEngine velocityEngine() {
    val props = new Properties();
    props.put("resource.loader", "class");
    props.put(
        "class.resource.loader.class",
        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    return new VelocityEngine(props);
  }
}
