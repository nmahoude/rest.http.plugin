package rest.http.plugin.editors.annotations;

import org.eclipse.jface.text.source.Annotation;

public class HttpExecuteAnnotation extends Annotation {
	public static final String TYPE = "rest.http.plugin.editors.HttpFileEditor";

  public HttpExecuteAnnotation() {
      super(TYPE, false, "Exécuter la requête");
  }
}
