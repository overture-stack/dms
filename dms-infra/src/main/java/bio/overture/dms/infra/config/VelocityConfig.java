package bio.overture.dms.infra.config;

import lombok.val;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

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
