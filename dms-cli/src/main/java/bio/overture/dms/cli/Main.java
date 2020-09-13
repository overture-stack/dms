package bio.overture.dms.cli;

import bio.overture.dms.cli.command.DmsCommand;
import bio.overture.dms.cli.util.ProjectBanner;
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
import picocli.CommandLine.IFactory;

import java.io.IOException;

import static bio.overture.dms.cli.config.CliConfig.APPLICATION_NAME;
import static org.springframework.boot.SpringApplication.exit;

@SpringBootApplication
public class Main implements CommandLineRunner, ExitCodeGenerator {

  /**
   * Dependencies
   */
  private final CommandLine commandLine;

  /**
   * State
   */
  private int exitCode;

  @Autowired
  public Main(@NonNull CommandLine commandLine) {
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
    // let Spring instantiate and inject dependencies
    val app = new SpringApplication(Main.class);
    app.setBanner(new ProjectBanner(APPLICATION_NAME));
    app.setBannerMode(Banner.Mode.CONSOLE);
    System.exit(exit(app.run(args)));
  }
}
