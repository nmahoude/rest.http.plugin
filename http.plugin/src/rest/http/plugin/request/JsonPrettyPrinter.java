package rest.http.plugin.request;

import org.json.JSONObject;

public class JsonPrettyPrinter {
	public static String prettyPrint(String jsonString) {
		try {
			JSONObject json = new JSONObject(jsonString);
			String prettyJson = json.toString(2); // indentation à 4 espaces
			return prettyJson;
		} catch (Exception e) {
			// Si le JSON n'est pas valide, on retourne la chaîne d'origine
			return jsonString;
		}
	}

}
