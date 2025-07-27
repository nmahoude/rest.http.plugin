package rest.http.plugin.request;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonPrettyPrinter {
	public static String prettyPrint(String jsonString) {
		try {
			JSONObject json = new JSONObject(jsonString);
			String prettyJson = json.toString(2); // indentation 
			return prettyJson;
		} catch (Exception e) {
			// on essaye avec un array
			try {
				JSONArray json = new JSONArray(jsonString);
				String prettyJson = json.toString(2); // indentation 
				return prettyJson;
			} catch (Exception e2) {
				// Si le JSON n'est pas valide, on retourne la chaîne d'origine
				return jsonString;
			}
		}
	}

}
