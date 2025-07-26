package rest.http.plugin.request;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestData {
	public static final RequestData EMPTY = new RequestData();
	private final static List<String> localCalls = List.of("localhost", "127.0.0.1", "::1");

	
	public String url;
	public String method;
	public Map<String, List<String>> headers = new HashMap<>();
	public String body;
	public boolean http2 = false;
	public boolean noProxy = false;


	public URL url() throws MalformedURLException {
		return new URL(url);
	}

	public boolean useProxy() throws MalformedURLException {
    if (localCalls.contains(url().getHost())) return false; // Ne pas utiliser de proxy pour les appels locaux
    return !noProxy;
	}

}
