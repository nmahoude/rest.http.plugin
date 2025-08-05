package rest.http.plugin.editors.syntaxcoloring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import rest.http.plugin.editors.syntaxcoloring.rules.VariableDefinedRule;
import rest.http.plugin.editors.syntaxcoloring.rules.VariableUsedRule;

public class HttpScanner extends RuleBasedScanner {

  private ColorManager colorManager;

	public HttpScanner(ColorManager colorManager) {
      this.colorManager = colorManager;
      
      Token variableToken = new Token(new TextAttribute(new Color(Display.getCurrent(), 200, 0, 0)));
      
      
      Color methodFrontColor = colorManager.getColor(new RGB(255, 255, 255));
			IToken getMethodToken = new Token(new TextAttribute(methodFrontColor, colorManager.getColor(new RGB(171, 149, 222)), SWT.BOLD));
			IToken postMethodToken = new Token(new TextAttribute(methodFrontColor, colorManager.getColor(new RGB(174, 206, 166)), SWT.BOLD));
			IToken putMethodToken = new Token(new TextAttribute(methodFrontColor, colorManager.getColor(new RGB(220, 191, 127)), SWT.BOLD));
			IToken delMethodToken = new Token(new TextAttribute(methodFrontColor, colorManager.getColor(new RGB(237, 168, 163)), SWT.BOLD));
			IToken patchMethodToken = new Token(new TextAttribute(methodFrontColor, colorManager.getColor(new RGB(163, 237, 211)), SWT.BOLD));
      
      
			IToken methodToken = new Token(new TextAttribute(colorManager.getColor(new RGB(127, 0, 85)), null, SWT.BOLD));
      IToken httpToken = new Token(new TextAttribute(colorManager.getColor(new RGB(50, 150, 50))));
      IToken urlToken = new Token(new TextAttribute(colorManager.getColor(new RGB(42, 0, 1))));

      IToken commentToken = new Token(new TextAttribute(colorManager.getColor(new RGB(0, 99, 0)), null, SWT.ITALIC));
      IToken separatorToken = new Token(new TextAttribute(colorManager.getColor(new RGB(255, 0, 255)), null, SWT.ITALIC));
      
      List<IRule> rules = new ArrayList<>();

      // separator
      rules.add(new EndOfLineRule("###", separatorToken));

      // http
      rules.add(new EndOfLineRule("http://", httpToken));
      rules.add(new EndOfLineRule("https://", httpToken));

      
      // Commentaires
      rules.add(new EndOfLineRule("//", commentToken));
      rules.add(new EndOfLineRule("#", commentToken));
      
      // Règle pour détecter GET, POST, PUT...
      WordRule wordRule = new WordRule(new IWordDetector() {
          @Override
          public boolean isWordStart(char c) {
              return Character.isLetter(c);
          }

          @Override
          public boolean isWordPart(char c) {
              return Character.isLetter(c);
          }
      });

      wordRule.addWord("GET", getMethodToken);
      wordRule.addWord("POST", postMethodToken);
      wordRule.addWord("PUT", putMethodToken);
      wordRule.addWord("DELETE", delMethodToken);
      wordRule.addWord("PATCH", patchMethodToken);
      
      rules.add(wordRule);

      rules.add(new VariableDefinedRule(variableToken)); // Règle pour les variables
      rules.add(new VariableUsedRule(variableToken)); // Règle pour les variables utilisées
      
      rulesForJson(rules);
      
      setRules(rules.toArray(new IRule[0]));
  }

	private void rulesForJson(List<IRule> rules) {
		// Couleurs issues du thème
		    Color keyColor = getColorFromThemeOrDefault("json.key", new RGB(127, 0, 85)); // violet
		    Color stringColor = getColorFromThemeOrDefault("json.string", new RGB(42, 0, 255)); // bleu
		    Color numberColor = getColorFromThemeOrDefault("json.number", new RGB(0, 128, 0)); // vert

		    IToken keyToken = new Token(new TextAttribute(keyColor, null, SWT.BOLD));
		    IToken stringToken = new Token(new TextAttribute(stringColor));
		    IToken numberToken = new Token(new TextAttribute(numberColor));

		    // 1. Clé JSON : "clé":
		    rules.add(new PatternRule("\"", "\":", keyToken, '\\', false));

		    // 2. Valeur string : "..."
		    rules.add(new PatternRule(":", "\"", stringToken, '\\', false)); // naïf, mais fonctionne pour valeur string

		    // 3. Nombre : regex
		    rules.add(new RegexRule(":\\s*(\\d+)", numberToken));

		    // 4. Booléens / null
		    rules.add(new WordRule(new JSONWordDetector(), Token.UNDEFINED));
		    WordRule boolRule = new WordRule(new JSONWordDetector(), Token.UNDEFINED);
		    boolRule.addWord("true", new Token(new TextAttribute(numberColor)));
		    boolRule.addWord("false", new Token(new TextAttribute(numberColor)));
		    boolRule.addWord("null", new Token(new TextAttribute(numberColor)));
		    rules.add(boolRule);
	}

	private Color getColorFromThemeOrDefault(String key, RGB fallbackRGB) {
    ColorRegistry registry = PlatformUI.getWorkbench()
        .getThemeManager()
        .getCurrentTheme()
        .getColorRegistry();
    if (registry.hasValueFor(key)) {
        return registry.get(key);
    } else {
        return new Color(Display.getCurrent(), fallbackRGB);
    }
}
}