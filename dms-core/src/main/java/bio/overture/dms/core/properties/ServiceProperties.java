package bio.overture.dms.core.properties;

import static bio.overture.dms.core.properties.env.EnvVars.generateEnvVarMap;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public interface ServiceProperties {
  default Map<String, String> getEnvironment()
      throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
    return generateEnvVarMap(this);
  }
}
