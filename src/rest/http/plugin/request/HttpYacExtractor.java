package rest.http.plugin.request;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class HttpYacExtractor {
	Map<String, String> variables = new HashMap<>();
	
	public RequestData extract(IDocument document, int start) throws BadLocationException {
		Builder builder = HttpRequest.newBuilder();
		RequestData requestData = new RequestData();
		
		int totalLines = document.getNumberOfLines();
		int line = start;

		String httpMethod = "GET";
		URI httpUri = null;
		
		// 1. Find method and URL
		while (line < totalLines) {
			String content = document.get(document.getLineOffset(line), document.getLineLength(line)).trim();
			if (content.isEmpty() || content.startsWith("#")) {
				line++;
				continue;
			}
			if (content.startsWith("###")) {
				requestData.request = builder.build();
				return requestData; // End of request
			}
			// First non-empty, non-comment line: method and URL
			String[] parts = content.split(" ", 2);
			if (parts.length == 2) {
				httpMethod = parts[0].trim();
				httpUri = java.net.URI.create(replaceVariables(parts[1].trim()));
				builder.method(httpMethod, HttpRequest.BodyPublishers.noBody());
				builder.uri(httpUri);
			}
			line++;
			break;
		}

		// 2. Headers (until first empty line)
		while (line < totalLines) {
			String content = document.get(document.getLineOffset(line), document.getLineLength(line)).trim();
			if (content.isEmpty()) {
				line++;
				break;
			}
			if (content.startsWith("###")) {
				break;
			}
			if (!content.startsWith("#")) {
				String [] headerParts = content.split(":", 2);
				builder.header(headerParts[0].trim(), headerParts.length > 1 ? headerParts[1].trim() : "");
			}
			line++;
		}

		// 3. Body (until ### or end)
		StringBuilder body = new StringBuilder();
		while (line < totalLines) {
			String content = document.get(document.getLineOffset(line), document.getLineLength(line));
			if (content.trim().startsWith("###")) {
				break;
			}
			body.append(content);
			line++;
		}
		builder.method(httpMethod, HttpRequest.BodyPublishers.ofString(body.toString()));
		requestData.request = builder.build();
		requestData.body = body.toString();
		return requestData;
	}

	private String replaceVariables(String content) {
		for (Map.Entry<String, String> entry : variables.entrySet()) {
			String key = "{{" + entry.getKey()+"}}";
			String value = entry.getValue();
			content = content.replace(key, value);
		}
		return content;
	}

	public void refreshDoc(IDocument document) {
		int totalLines = document.getNumberOfLines();
		int current = 0;
		
		variables.clear();
		while (current < totalLines) {
			try {
				String content = document.get(document.getLineOffset(current), document.getLineLength(current)).trim();
				if (content.startsWith("@")) {
					// it's a variable
					String[] parts = content.split("=", 2);
					variables.put(parts[0].substring(1).trim(), parts.length > 1 ? parts[1].trim() : "");
				}
				
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			current++;
		}
		
	}
}