package bio.overture.dms.core.properties.service;

import static bio.overture.dms.core.properties.service.env.EnvVars.generateEnvVarMap;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public interface ServiceProperties {
  String getName();

  default Map<String, String> getEnvironment()
      throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    return generateEnvVarMap(this);
  }
}
