package rest.http.plugin.editors.annotations;

import org.eclipse.jface.text.source.Annotation;

public class InlineTextAnnotation extends Annotation {
	public static final String TYPE = "rest.http.plugin.inlineText";

  private final String message;

  public InlineTextAnnotation(String message) {
      super(TYPE, false, message);
      this.message = message;
  }

  public String getMessage() {
      return message;
  }
}
