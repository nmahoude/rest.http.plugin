package rest.http.plugin.request;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.SSLSession;

public class RequestExecutor {
	HttpClient client = HttpClient.newHttpClient();
	
	public HttpResponse<?> execute(RequestData requestData) {
		try {
			// Perform the actual HTTP call
			HttpResponse<?> result = client.send(requestData.request, BodyHandlers.ofString());
			return result;

		} catch (IOException | InterruptedException e) {
			// Create a fake HttpResponse in case of error
			return new HttpResponse<String>() {
				@Override
				public int statusCode() { return 500; }
				@Override
				public String body() { return "Error executing request: " + e.getMessage(); }
				@Override
				public HttpRequest request() { return requestData.request; }
				@Override
				public Optional<HttpResponse<String>> previousResponse() { return Optional.empty(); }
				@Override
				public HttpHeaders headers() { return HttpHeaders.of(Map.of(), (k, v) -> true); }
				@Override
				public URI uri() { return requestData.request.uri(); }
				@Override
				public Version version() { return Version.HTTP_1_1; }
				@Override
				public Optional<SSLSession> sslSession() {
					return Optional.empty();
				}
			};
		}
	}
}