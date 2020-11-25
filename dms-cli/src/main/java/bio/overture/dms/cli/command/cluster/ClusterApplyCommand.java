package bio.overture.dms.cli.command.cluster;

import bio.overture.dms.cli.util.VersionProvider;
import bio.overture.dms.infra.config.JacksonConfig;
import bio.overture.dms.infra.docker.DCGraphGenerator;
import bio.overture.dms.infra.docker.DockerService;
import bio.overture.dms.infra.job.DockerComposer;
import bio.overture.dms.infra.service.DCReader;
import bio.overture.dms.infra.service.DeployInfoService;
import com.github.dockerjava.api.DockerClient;
import lombok.val;
import org.javers.core.JaversBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static bio.overture.dms.infra.util.FileUtils.readResourcePath;

@Command(
    name = "apply",
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Deploy a spec to the cluster" )
public class ClusterApplyCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
      return 0;
    }
}
