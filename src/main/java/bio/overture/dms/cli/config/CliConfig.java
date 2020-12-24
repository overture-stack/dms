package bio.overture.dms.cli.config;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_HEADER_HEADING;

import bio.overture.dms.cli.command.DmsCommand;
import bio.overture.dms.cli.util.CommandListRenderer;
import bio.overture.dms.cli.util.ProjectBanner;
import lombok.NonNull;
import lombok.val;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextTerminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import picocli.CommandLine;

@Configuration
public class CliConfig {

  public static final String APPLICATION_NAME = "DMS";

  private final CommandLine.IFactory factory; // auto-configured to inject PicocliSpringFactory
  private final DmsCommand dmsCommand;
  private final TextTerminal<?> textTerminal;

  @Autowired
  public CliConfig(
      @NonNull CommandLine.IFactory factory,
      @NonNull DmsCommand dmsCommand,
      @NonNull TextTerminal<?> textTerminal) {
    this.factory = factory;
    this.dmsCommand = dmsCommand;
    this.textTerminal = textTerminal;
  }

  @Bean
  public TextIO textIO() {
    return new TextIO(textTerminal);
  }

  @Bean
  public CommandLine commandLine() {
    val cmd = new CommandLine(dmsCommand, factory);
    val banner = new ProjectBanner(APPLICATION_NAME, "@|bold,green ", "|@");
    cmd.getHelpSectionMap().put(SECTION_KEY_COMMAND_LIST, new CommandListRenderer());
    addBannerToHelp(cmd, banner);
    return cmd;
  }

  private void addBannerToHelp(CommandLine cmd, ProjectBanner banner) {
    cmd.getHelpSectionMap()
        .put(SECTION_KEY_HEADER_HEADING, help -> help.createHeading(banner.generateBannerText()));
    if (!cmd.getSubcommands().isEmpty()) {
      cmd.getSubcommands().values().forEach(x -> addBannerToHelp(x, banner));
    }
  }
}
