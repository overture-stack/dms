package bio.overture.dms.docker.env.visitor;

import java.util.Map;

/**
 * Defines the behaviour for processing an environment variable and its value with the env map
 */
public interface EnvFieldVisitor {

  void visit(Map<String, String> envMap, String envName, Object fieldValue);
}
