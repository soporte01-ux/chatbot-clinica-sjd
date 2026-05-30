package pe.com.s2o.clinica.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import pe.com.s2o.clinica.dtos.SitedsSolAutorizacionRestricDto;

public class HttpRequestUtil {
    public static class HttpResponse {
        private int statusCode;
        private String responseBody;
        private String errorMessage;

        public HttpResponse(int statusCode, String responseBody, String errorMessage) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
            this.errorMessage = errorMessage;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String toString() {
            return "HttpResponse{" +
                    "statusCode=" + statusCode +
                    ", responseBody='" + responseBody + '\'' +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        }
    }
    
    public static HttpResponse sendRequestChatUp(String method, String url, String body, Map<String, String> headers) {
        HttpURLConnection connection = null;
        try {
        	disableSslValidation();
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod(method.toUpperCase());
            connection.setDoOutput("POST".equalsIgnoreCase(method));
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000); 
            // Set headers
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    connection.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            // Write body if POST
            if (body != null && !body.isEmpty() && "POST".equalsIgnoreCase(method)) {
                try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
                    writer.write(body);
                    writer.flush();
                }
            }

            // Read response
            int statusCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    statusCode >= 200 && statusCode < 300 ? connection.getInputStream() : connection.getErrorStream()
            ));
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line);
            }
            reader.close();

            return new HttpResponse(statusCode, responseBody.toString(), null);

        } catch (Exception e) {
            return new HttpResponse(-1, null, e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    

    public static HttpResponse sendRequest(String method, String url, String body, Map<String, String> headers) {
        HttpURLConnection connection = null;
        try {
        	disableSslValidation();
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod(method.toUpperCase());
            connection.setDoOutput("POST".equalsIgnoreCase(method));
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000); 
            // Set headers
            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    connection.setRequestProperty(header.getKey(), header.getValue());
                }
            }

            // Write body if POST
            if (body != null && !body.isEmpty() && "POST".equalsIgnoreCase(method)) {
                try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                    outputStream.writeBytes(body);
                    outputStream.flush();
                }
            }

            // Read response
            int statusCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    statusCode >= 200 && statusCode < 300 ? connection.getInputStream() : connection.getErrorStream()
            ));
            StringBuilder responseBody = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line);
            }
            reader.close();

            return new HttpResponse(statusCode, responseBody.toString(), null);

        } catch (Exception e) {
            return new HttpResponse(-1, null, e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    
    public static void disableSslValidation() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
		/*try {
	        Map<String, String> headers = new HashMap<>();
	        headers.put("Accept", "application/json");
	        String requestBody = "{ \"title\": \"foo\", \"body\": \"bar\", \"userId\": 1 }";

	        HttpRequestUtil.HttpResponse response = HttpRequestUtil.sendRequest("GET", "https://jsonplaceholder.typicode.com/posts/1", requestBody, headers);

	        System.out.println(response.responseBody);
		}
		catch (Exception e) {
			e.printStackTrace();
		}*/
        List<SitedsSolAutorizacionRestricDto> detRestric = new LinkedList<SitedsSolAutorizacionRestricDto>();
        SitedsSolAutorizacionRestricDto restric = null;
        for (int i = 0; i < 5; i++) {
            restric = new SitedsSolAutorizacionRestricDto();
            detRestric.add(0, restric);
        }
        
        System.out.println(detRestric.toString());
        
	}
}
