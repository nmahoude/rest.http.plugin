package rest.http.plugin.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestData {
	public static final RequestData EMPTY = new RequestData();

	
	public String url;
	public String method;
	public Map<String, List<String>> headers = new HashMap<>();
	public String body;
	public boolean http2 = false;
	public boolean noProxy = false;

}
