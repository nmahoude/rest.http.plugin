package rest.http.plugin.yac;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import rest.http.plugin.request.RequestData;

public class YacBlock {
	public int startingLine;
	public int endLine;

	public int verbLine;
	public String content;
	RequestData data = new RequestData();
	
	boolean validRequest = false;
	
	public YacBlock(int start, int end, String content) {

		this.startingLine = start;
		this.endLine = end;
		this.content = content;
	}
	
	void init(Map<String, String> variables) {
		if (content == null || content== null || content.isEmpty()) {
			error();
			return;
  	}
	  
		String[] lines = content.split("\\r?\\n");
	  if (lines.length == 0) {
	  	error();
	  	return;
	  }
	  
	  // Passer toutes les lignes vides et les commentaires
	  int current = 0;
	  while (current < lines.length && (lines[current].trim().isEmpty() || lines[current].trim().startsWith("#") || lines[current].trim().startsWith("/"))) {
	    String line = replaceVariables(lines[current], variables);
	    if (line.contains("@no-proxy")) {
	    	data.noProxy = true;
	    }
	    if (line.contains("@proxy")) {
	    	data.noProxy = false;
	    }
	  	current++;
	  }
	  if (current >= lines.length) {
	  	error();
	  	return;
	  }
	  
	  String firstLine = replaceVariables(lines[current], variables);
	  if (firstLine.isEmpty()) {
	  	error();
	  	return;
	  }

	  extractMethodAndUrl(data, firstLine);
	  if (!validRequest) {
	  	error(data);
	  	return;
	  }
	  verbLine = current;
	  current++;
	  
	  Map<String, List<String>> headers = new java.util.HashMap<>();
		for (; current < lines.length; ) {
      String line = replaceVariables(lines[current++], variables);;
			if (line.isEmpty()) {
				break; // Skip empty lines and comments
			}
      int idx = line.indexOf(":");
      if (idx > 0) {
          String key = line.substring(0, idx).trim();
          String value = line.substring(idx + 1).trim();
          headers.putIfAbsent(key, new ArrayList<>());
          headers.get(key).add(value);
      }
	  }
		data.headers = headers;
	  
	  StringBuilder body = new StringBuilder();
	  for (; current < lines.length; current++) {
      String line = replaceVariables(lines[current], variables);
      body.append(line).append("\n");
	  }
	  data.body = body.toString().trim();
	}

	private String replaceVariables(String content, Map<String, String> variables) {
		for (Map.Entry<String, String> entry : variables.entrySet()) {
			String key = "{{" + entry.getKey()+ "}}";
			String value = entry.getValue();
			content = content.replace(key, value);
		}
		return content;
	}

	public RequestData request() {
		return this.data;
	}
	
	private void extractMethodAndUrl(RequestData requestData, String firstLine) {
	  String[] parts = firstLine.split(" ", 3);
	  if (parts.length == 3) {
	  	// On a un verbe, une url et un protocole
	  	requestData.method = parts[0].trim();
	  	requestData.url = parts[1].trim();
	  	if (parts[2].trim().equalsIgnoreCase("HTTP/2") || parts[2].trim().equalsIgnoreCase("HTTP/2.0")) {
	  		requestData.http2 = true;
	  	} else {
	  		requestData.http2 = false;
	  	}
	  }
	  else if (parts.length < 2) {
	  	if (parts[0].trim().startsWith("http")) {
		  	requestData.method = "GET";
		  	requestData.url = parts[0].trim();
	  	} else {
	  		error(requestData);
	  		return;
	  	}
	  } else {
	  	requestData.method = parts[0];
	  	requestData.url = parts[1];
	  }
	  
	  validRequest = true;
	}


	private void error() {
		error(this.data);
	}

	private void error(RequestData requestData) {
		error(requestData, "malformed block");
	}
	private void error(RequestData requestData, String message) {
		this.validRequest = false;
		requestData.method = "GET";
		requestData.url = message;
	}

	public boolean isValidRequest() {
		return validRequest;
	}

	public Map<String, String> extractVariables() {
		Map<String, String> variables = new java.util.HashMap<>();
		
		String[] lines = content.split("\\r?\\n");
	  if (lines.length == 0) {
	  	return Collections.emptyMap();
	  }
	  
	  // Passer toutes les lignes en revue
	  for (String line : lines) {
	  	if (line.trim().startsWith("@")) {
	  		// extract variable as yac format
	  		String[] parts = line.trim().split("=", 2);
	  		if (parts.length == 2) {
	  			String key = parts[0].trim().substring(1); // remove @
	  			String value = parts[1].trim();
	  			if (!key.isEmpty() && !value.isEmpty()) {
	  				variables.put(key, value);
	  			}
	  		} else if (parts.length == 1) {
	  			// just a variable name, no value
	  			String key = parts[0].trim();
	  			if (!key.isEmpty()) {
	  				variables.put(key, "");
	  			}
	  		}
	  	}
	  }
	  return variables;
	}
}
