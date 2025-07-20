package rest.http.plugin.editors.syntaxcoloring;

import org.eclipse.jface.text.rules.IWordDetector;

public class JSONWordDetector implements IWordDetector {

	@Override
  public boolean isWordStart(char c) {
      return Character.isLetter(c);
  }

  @Override
  public boolean isWordPart(char c) {
      return Character.isLetter(c);
  }

}
