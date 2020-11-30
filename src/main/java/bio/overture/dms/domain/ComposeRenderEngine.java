package bio.overture.dms.domain;

public interface ComposeRenderEngine<S extends DMSSpec, C extends ComposeObject> {

  C render(S spec);
}
