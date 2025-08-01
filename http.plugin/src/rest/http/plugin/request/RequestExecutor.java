package rest.http.plugin.request;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import rest.http.plugin.Activator;
import rest.http.plugin.HttpPreferencePage;

public class RequestExecutor {
	
	
	public ResponseData execute(RequestData requestData) {
		try {
      ResponseData result = new ResponseData();

			String proxyHost = Activator.getDefault().getPreferenceStore().getString(HttpPreferencePage.SOCKS_PROXY_HOST_ID);
			int proxyPort = Activator.getDefault().getPreferenceStore().getInt(HttpPreferencePage.SOCKS_PROXY_PORT_ID);
			
      disableSSLVerification();
      
      
      URL url = requestData.url();
      HttpURLConnection connection;
      
      boolean useProxy = requestData.useProxy();
      if (useProxy && (proxyHost == null || proxyHost.isEmpty() || proxyPort <= 0)) {
      	useProxy = false; // Pas de proxy configuré
      	result.metadata.put("proxy", "not configured");
      }
      
      if (useProxy) {
      	Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));
				connection = (HttpURLConnection) url.openConnection(proxy);
      } else {
				connection = (HttpURLConnection) url.openConnection();
      }
      
      connection.setRequestMethod(requestData.method.trim());
      connection.setDoOutput(true);
      connection.setConnectTimeout(1000);
      connection.setReadTimeout(30_000);

      // headers
      for (Map.Entry<String, List<String>> header : requestData.headers.entrySet()) {
  				for (String value : header.getValue()) {
  					connection.setRequestProperty(header.getKey(), value);
  				}
			}
      
      // body
      if (requestData.body != null && !requestData.body.isEmpty()) {
				connection.setRequestProperty("Content-Type", "application/json");
				try (OutputStream os = connection.getOutputStream()) {
					byte[] input = requestData.body.getBytes(StandardCharsets.UTF_8);
					os.write(input);
				}
			}
      
      // Http call
      long startTime = System.currentTimeMillis();
      result.code = connection.getResponseCode();
      long endTime = System.currentTimeMillis();
      result.duration = endTime - startTime;
      
      for (int i = 1;; i++) {
        String headerKey = connection.getHeaderFieldKey(i);
        String headerValue = connection.getHeaderField(i);
        if (headerKey == null && headerValue == null) break;
        
        result.headers.putIfAbsent(headerKey, new ArrayList<>());
        result.headers.get(headerKey).add(headerValue);
        
        System.out.println(headerKey + ": " + headerValue);
      }
      
      // get body
      InputStream inputStream;
      if (result.code >= 200 && result.code < 400) {
        	inputStream = connection.getInputStream(); // succès
      } else {
      	inputStream = connection.getErrorStream(); // erreur (ex: 404, 500)
      }

      if (inputStream != null) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
            result.body = JsonPrettyPrinter.prettyPrint(response.toString());
        }
	    } else {
	    	result.body = ""; // parfois aucune réponse
	    }
      System.err.println("Response body: " + result.body);
      
			return result;
			
		} catch (Exception e) {
      ResponseData result = new ResponseData();
			result.code = 500; // Erreur interne
			result.body = e.getMessage();
			return result;
		}
	}
	
	private static void disableSSLVerification() throws Exception {
    // Ne rien vérifier côté client
    TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) {}
            public void checkServerTrusted(X509Certificate[] chain, String authType) {}
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
        }
    };

    SSLContext sc = SSLContext.getInstance("TLS");
    sc.init(null, trustAllCerts, new SecureRandom());

    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

    // Ignorer le nom de domaine (hostname) aussi
    HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
}
}