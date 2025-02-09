package pe.com.s2o.clinica.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

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

    public static HttpResponse sendRequest(String method, String url, String body, Map<String, String> headers) {
        HttpURLConnection connection = null;
        try {
            // Create the connection
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod(method.toUpperCase());
            connection.setDoOutput("POST".equalsIgnoreCase(method));
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000); 
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
    
    public static void main(String[] args) {
		try {
	        Map<String, String> headers = new HashMap<>();
	        headers.put("Accept", "application/json");
	        String requestBody = "{ \"title\": \"foo\", \"body\": \"bar\", \"userId\": 1 }";

	        HttpRequestUtil.HttpResponse response = HttpRequestUtil.sendRequest("GET", "https://jsonplaceholder.typicode.com/posts/1", requestBody, headers);

	        System.out.println(response.responseBody);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
