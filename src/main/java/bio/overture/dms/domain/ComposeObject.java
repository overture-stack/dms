package bio.overture.dms.domain;

import java.util.Set;

public interface ComposeObject<I extends ComposeItem> {

  String getVersion();

  Set<I> getComposeItems();
}
