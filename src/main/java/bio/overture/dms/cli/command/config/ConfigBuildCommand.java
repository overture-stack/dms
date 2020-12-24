package bio.overture.dms.cli.command.config;

import static bio.overture.dms.cli.model.enums.Constants.CONFIG_FILE_NAME;
import static java.nio.file.Files.createDirectories;

import bio.overture.dms.cli.questionnaire.DmsQuestionnaire;
import bio.overture.dms.cli.terminal.Terminal;
import bio.overture.dms.cli.util.VersionProvider;
import bio.overture.dms.core.util.ObjectSerializer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Slf4j
@Command(
    name = "build",
    aliases = {"bu"},
    mixinStandardHelpOptions = true,
    versionProvider = VersionProvider.class,
    description = "Interactively build a configuration")
public class ConfigBuildCommand implements Callable<Integer> {

  private final ObjectSerializer yamlSerializer;
  private final Terminal terminal;
  private final DmsQuestionnaire dmsQuestionnaire;

  @Autowired
  public ConfigBuildCommand(
      @NonNull ObjectSerializer yamlSerializer,
      @NonNull Terminal terminal,
      @NonNull DmsQuestionnaire dmsQuestionnaire) {
    this.yamlSerializer = yamlSerializer;
    this.terminal = terminal;
    this.dmsQuestionnaire = dmsQuestionnaire;
  }

  @Option(
      names = {"--skip-answered"},
      required = false,
      description = "Skip previously answered questions, and jump to the first unanswered question")
  private boolean skipAnswered = false;

  @Option(
      names = {"--skip-system-check"},
      required = false,
      description = "Skip the system check")
  private boolean skipSystemCheck = false;

  @SneakyThrows
  private Path provisionConfigFile() {
    val userDir = Paths.get(System.getProperty("user.home"));
    val dmsDir = userDir.resolve(".dms");
    createDirectories(dmsDir);
    return dmsDir.resolve(CONFIG_FILE_NAME);
  }

  @Override
  public Integer call() throws Exception {
    terminal.printStatusLn("Starting interactive configuration");
    val configFile = provisionConfigFile();
    val dmsConfig = dmsQuestionnaire.buildDmsConfig();
    yamlSerializer.serializeToFile(dmsConfig, configFile.toFile());
    terminal.printStatusLn("Wrote config file to %s", configFile);
    return 0;
  }
}
