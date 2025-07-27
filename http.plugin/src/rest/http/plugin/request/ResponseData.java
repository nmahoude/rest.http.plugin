package rest.http.plugin.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseData {

	public int code;
	public long duration;
	public int size;
	
	public String body;
	public Map<String, List<String>> headers = new HashMap<>();
	public Map<String, String> metadata = new HashMap<>();

	public String getBody() {
		return body;
	}

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	public int getCode() {
		return code;
	}
}
