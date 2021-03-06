package bio.overture.dms.cli.config;

import static bio.overture.dms.cli.model.Constants.GuidesURLS.HELP_HEADER_GUIDE_URLS;
import static picocli.CommandLine.Model.UsageMessageSpec.*;

import bio.overture.dms.cli.command.DmsCommand;
import bio.overture.dms.cli.util.CommandListRenderer;
import bio.overture.dms.cli.util.ProjectBanner;
import lombok.NonNull;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import picocli.CommandLine;

@Configuration
public class CliConfig {

  public static final String APPLICATION_NAME = "DMS";

  private final CommandLine.IFactory factory; // auto-configured to inject PicocliSpringFactory
  private final DmsCommand dmsCommand;

  @Autowired
  public CliConfig(@NonNull CommandLine.IFactory factory, @NonNull DmsCommand dmsCommand) {
    this.factory = factory;
    this.dmsCommand = dmsCommand;
  }

  @Bean
  public CommandLine commandLine() {
    val cmd = new CommandLine(dmsCommand, factory);
    val banner = new ProjectBanner(APPLICATION_NAME, "@|bold,green ", "|@");
    cmd.getHelpSectionMap().put(SECTION_KEY_HEADER, (x) -> HELP_HEADER_GUIDE_URLS);
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
