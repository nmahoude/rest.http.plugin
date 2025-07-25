package rest.http.plugin.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestData {
	public String url;
	public String method;
	public Map<String, List<String>> headers = new HashMap<>();
	public String body;
	
}
