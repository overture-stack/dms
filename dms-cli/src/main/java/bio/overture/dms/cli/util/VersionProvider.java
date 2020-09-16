package bio.overture.dms.cli.util;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IVersionProvider;

@Component
public class VersionProvider implements IVersionProvider {

  private final BuildProperties buildProperties;

  @Autowired
  public VersionProvider(@NonNull BuildProperties buildProperties) {
    this.buildProperties = buildProperties;
  }

  @Override
  public String[] getVersion() throws Exception {
    return new String[] { buildProperties.getVersion() };
  }
}
