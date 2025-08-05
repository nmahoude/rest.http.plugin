package rest.http.plugin.editors.syntaxcoloring.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class VariableDefinedRule implements IRule {

	private IToken token;

	public VariableDefinedRule(IToken token) {
		this.token = token;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		int currentChar = scanner.read(); // on conserver le caractère lu
		
    if (currentChar == '@' || currentChar == '=') {
			return token;
		}

		scanner.unread(); // on remet le caractère dans le scanner, on ne veut pas le consommer
    return Token.UNDEFINED;
	}

}
