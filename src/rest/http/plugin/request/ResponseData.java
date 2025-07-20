package rest.http.plugin.request;

public class ResponseData {

	public int code;
	public String body;
	public String headers;

	public String getBody() {
		return body;
	}

	public String getHeaders() {
		return headers;
	}

	public int getCode() {
		return code;
	}
}
