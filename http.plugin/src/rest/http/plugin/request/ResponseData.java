package rest.http.plugin.request;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseData {
	public static final ResponseData EMPTY = new ResponseData() {
		{
			code = -1;
			duration = -1;
			body = "";
		}
	};
	
	
	
	
	public int code;
	long duration;
	
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

	public String size() {
		int sizeInO = body.getBytes(StandardCharsets.UTF_8).length;
		String qualifier = "o";
		if (sizeInO > 1024) {
			sizeInO /= 1024;
			qualifier = "ko";
		}
		if (sizeInO > 1024) {
			sizeInO /= 1024;
			qualifier = "Mo";
		}
		if (sizeInO > 1024) {
			sizeInO /= 1024;
			qualifier = "Go";
		}
		return ""+sizeInO + " " + qualifier;
	}

	public String duration() {
		int durationIn = (int) duration;
		String qualifier = "ms";
		if (durationIn > 1000) {
			durationIn /= 1000;
			qualifier = "s";
		}
		if (durationIn > 60) {
			durationIn /= 60;
			qualifier = "min";
		}
		if (durationIn > 60) {
			durationIn /= 60;
			qualifier = "h";
		}
		
		return "" + durationIn + " " + qualifier;	
	}
}
