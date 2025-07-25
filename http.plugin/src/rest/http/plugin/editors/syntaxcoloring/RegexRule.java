package rest.http.plugin.editors.syntaxcoloring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class RegexRule implements IRule {
  private final Pattern pattern;
  private final IToken token;

  public RegexRule(String regex, IToken token) {
      this.pattern = Pattern.compile(regex);
      this.token = token;
  }

  @Override
  public IToken evaluate(ICharacterScanner scanner) {
      StringBuilder buffer = new StringBuilder();
      int c;

      while ((c = scanner.read()) != ICharacterScanner.EOF) {
          buffer.append((char) c);
          Matcher matcher = pattern.matcher(buffer);
          if (matcher.find()) {
              return token;
          }
          if (!pattern.pattern().startsWith(buffer.toString())) {
              break;
          }
      }

      // rewind
      for (int i = buffer.length(); i > 0; i--) {
          scanner.unread();
      }
      return Token.UNDEFINED;
  }
}
