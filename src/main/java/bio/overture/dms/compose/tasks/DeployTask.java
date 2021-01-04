package bio.overture.dms.compose.tasks;

@Deprecated
public interface DeployTask {

  /**
   * The name of the peripheral deployment task, which should match the name of service in the
   * statically stored *.yaml.vm file. For instance, the name of a deployment task for
   * ego-api.yaml.vm, would be ego-api.
   */
  String getTaskName();
}
