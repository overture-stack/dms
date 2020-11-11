package bio.overture.dms.docker.spec;

import bio.overture.dms.docker.env.EnvProcessor;
import bio.overture.dms.docker.reflection.Reflector;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MemberUsageScanner;
import org.reflections.scanners.MethodAnnotationsScanner;

import static bio.overture.dms.docker.env.EnvProcessor.createEnvProcessor;
import static bio.overture.dms.docker.reflection.Reflector.createReflector;
import static java.util.Objects.isNull;

@Slf4j
public class Factory {

  public static EnvProcessor buildEnvProcessor(){
    return createEnvProcessor(buildReflector());
  }

  public static Reflector buildReflector(){
    return createReflector(buildReflections());
  }

  private static Reflections buildReflections(){
    if (isInProductionMode()){
      log.info("[REFLECTION_FACTORY]: Prod mode enabled. Using reflections.xml manifest generated at build time");
      return Reflections.collect();
    } else {
      log.info("[REFLECTION_FACTORY]: Prod mode NOT ENABLED. The {} package will be scanned and indexed by the Reflections lib", Factory.class.getPackage());
      return new Reflections(Factory.class.getPackageName(),
          new MemberUsageScanner(),
          new FieldAnnotationsScanner(),
          new MethodAnnotationsScanner());
    }
  }

  private static boolean isInProductionMode(){
    return !isNull(System.getProperty("prod"));
  }

}
