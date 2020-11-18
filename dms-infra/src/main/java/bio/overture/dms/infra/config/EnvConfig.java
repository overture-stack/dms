package bio.overture.dms.infra.config;

import bio.overture.dms.infra.env.EnvProcessor;
import bio.overture.dms.infra.reflection.Reflector;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MemberUsageScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static bio.overture.dms.infra.env.EnvProcessor.createEnvProcessor;
import static bio.overture.dms.infra.reflection.Reflector.createReflector;
import static java.util.Objects.isNull;

@Slf4j
@Configuration
public class EnvConfig {

  private static final String REFLECTION_PATH = "bio.overture.dms.infra";

  @Bean
  public EnvProcessor buildEnvProcessor(@Autowired Reflector reflector){
    return createEnvProcessor(reflector);
  }

  @Bean
  public Reflector buildReflector(@Autowired Reflections reflections){
    return createReflector(buildReflections());
  }

  @Bean
  public Reflections buildReflections(){
    if (isInProductionMode()){
      log.info("[REFLECTION_FACTORY]: Prod mode enabled. Using reflections.xml manifest generated at build time");
      return Reflections.collect();
    } else {
      log.info("[REFLECTION_FACTORY]: Prod mode NOT ENABLED. The {} package will be scanned and indexed by the Reflections lib", REFLECTION_PATH);
      return new Reflections(REFLECTION_PATH,
          new MemberUsageScanner(),
          new FieldAnnotationsScanner(),
          new MethodAnnotationsScanner());
    }
  }

  private static boolean isInProductionMode(){
    return !isNull(System.getProperty("prod"));
  }
}