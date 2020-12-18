package bio.overture.dms.compose.model.stack;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * NOTE: Originally, the idea was to create a domain model (i.e infrastructure independent)
 * representing deployments, and then translating them into infrastructure dependant models/configs
 * (i.e swarm config, manual docker container deployment, docker-compose or k8s configs), however
 * creating the conversion methods between domain-config and infra-config were extremely tedious,
 * and seemed over engineered.
 *
 * <p>Instead, a middle ground was chosen, where the model incorporates both domain and infra
 * related concepts. Not the best representation, however its cleaner, easier to maintain and less
 * error prone.
 */
@Data
@JsonInclude(NON_NULL)
public class ComposeStack {
  private List<ComposeService> services = new ArrayList<>();
}
