package bio.overture.dms;

import static org.springframework.boot.SpringApplication.exit;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

@Slf4j
@SpringBootApplication
public class SpringMain implements CommandLineRunner, ExitCodeGenerator {
  private static final String INFRA_MODE_SWITCH = "--infra-mode";

  /** Dependencies */
  private final CommandLine commandLine;

  /** State */
  private int exitCode;

  @Autowired
  public SpringMain(@NonNull CommandLine commandLine) {
    this.commandLine = commandLine;
  }

  @Override
  public void run(String... args) throws Exception {
    exitCode = commandLine.execute(args);
  }

  @Override
  public int getExitCode() {
    return exitCode;
  }

  public static void main(String[] args) {
    // TODO: do somethign with the bootstrap objects
    //    val bootstrap = new BootstrapCommand();
    //    val cli = new CommandLine(bootstrap);
    //    cli.execute(args);

    // let Spring instantiate and inject dependencies
    try {
      val app = new SpringApplication(SpringMain.class);
      app.setBannerMode(Banner.Mode.OFF);
      //    app.setAdditionalProfiles(<profiles>); //dynamically set spring profiles, if need to
      System.exit(exit(app.run(args)));
    } catch (Throwable t) {
      System.out.println(t.getMessage());
    }
  }
}
