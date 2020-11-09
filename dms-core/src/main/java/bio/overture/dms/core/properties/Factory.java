package bio.overture.dms.core.properties;

import bio.overture.dms.core.properties.env.EnvProcessor;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MemberUsageScanner;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.io.File;

import static java.util.Objects.isNull;

@Slf4j
public class Factory {

  public static EnvProcessor buildEnvProcessor(){
    return new EnvProcessor(buildReflections());
  }

  private static boolean isInProductionMode(){
    return !isNull(System.getProperty("prod"));
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

}
