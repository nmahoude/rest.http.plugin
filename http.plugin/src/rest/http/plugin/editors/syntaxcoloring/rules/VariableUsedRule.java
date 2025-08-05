package rest.http.plugin.editors.syntaxcoloring.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class VariableUsedRule implements IRule {

	private IToken token;

	public VariableUsedRule(IToken token) {
		this.token = token;
	}

	@Override
	public IToken evaluate(ICharacterScanner scanner) {
		int currentChar = scanner.read(); // on conserver le caractère lu
		if (currentChar != '{' && currentChar != '}') {
			scanner.unread(); // on remet le caractère dans le scanner, on ne veut pas le consommer
			return Token.UNDEFINED; // si ce n'est pas une accolade, on ne traite pas
		}
		
		// verifier qu'il y a la même avant ou après
		int nextChar = scanner.read(); // lire le caractère suivant
		scanner.unread(); // on se remet tout de suite sur le caractère lu initial
		if (nextChar == currentChar) {
			return token; // si c'est la même accolade, on retourne le token
		}

		scanner.unread(); // remettre le caractère lu dans le scanner
		scanner.unread(); // remettre le caractère lu dans le scanner
		int previousChar = scanner.read(); // lire le caractère suivant
		scanner.read(); // on relit le caractère suivant pour avancer dans le scanner
		if (previousChar == currentChar) {
			return token; // si c'est la même accolade, on retourne le token
		}
		
		scanner.unread(); // remettre le caractère lu dans le scanner
    return Token.UNDEFINED;
	}

}
