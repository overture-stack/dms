package bio.overture.dms.cli.config;

import bio.overture.dms.cli.command.DmsCommand;
import bio.overture.dms.cli.util.CommandListRenderer;
import bio.overture.dms.cli.util.ProjectBanner;
import bio.overture.dms.infra.util.JsonProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.NonNull;
import lombok.val;
import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import picocli.CommandLine;
import bio.overture.dms.cli.question.QuestionFactory;

import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_COMMAND_LIST;
import static picocli.CommandLine.Model.UsageMessageSpec.SECTION_KEY_HEADER_HEADING;

@Configuration
public class CliConfig {

  public static final String APPLICATION_NAME = "DMS";

  private final CommandLine.IFactory factory;    // auto-configured to inject PicocliSpringFactory
  private final DmsCommand dmsCommand;

  @Autowired
  public CliConfig( @NonNull CommandLine.IFactory factory,
      @NonNull DmsCommand dmsCommand) {
    this.factory = factory;
    this.dmsCommand = dmsCommand;
  }

  @Bean
  public TextTerminal<?> textTerminal(){
    return TextIoFactory.getTextTerminal();
  }

  @Bean
  public TextIO textIO(TextTerminal<?> textTerminal){
    return new TextIO(textTerminal);
  }

  @Bean
  public JsonProcessor jsonProcessor(){
    val mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    return new JsonProcessor(mapper);
  }

  @Bean
  public JsonProcessor yamlProcessor(){
    val mapper = new ObjectMapper(new YAMLFactory()).enable(SerializationFeature.INDENT_OUTPUT);
    return new JsonProcessor(mapper);
  }

  @Bean
  public QuestionFactory questionFactory(TextIO textIO){
    return new QuestionFactory(textIO);
  }

  @Bean
  public CommandLine commandLine(){
    val cmd = new CommandLine(dmsCommand, factory);
    val banner = new ProjectBanner(APPLICATION_NAME,"@|bold,green ", "|@" );
    cmd.getHelpSectionMap().put(SECTION_KEY_COMMAND_LIST, new CommandListRenderer());
    addBannerToHelp(cmd, banner);
    return cmd;
  }

  private void addBannerToHelp(CommandLine cmd, ProjectBanner banner){
    cmd.getHelpSectionMap().put(SECTION_KEY_HEADER_HEADING,
        help -> help.createHeading(banner.generateBannerText()));
    if (!cmd.getSubcommands().isEmpty()){
      cmd.getSubcommands().values().forEach(x -> addBannerToHelp(x, banner));
    }
  }

}
