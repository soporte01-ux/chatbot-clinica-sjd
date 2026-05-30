package pe.com.s2o.clinica.ws;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import pe.com.s2o.clinica.utils.HttpRequestUtil;


public class TokenValidator {

	//private static final String API_VALIDATION_URL = "http://localhost:8085/clinica-tokens/rs/tokenval/v1/validacion"; // Configura tu URL
	private static final String API_VALIDATION_URL = "https://melany.clinicarequipa.com.pe:4200/clinica-app/clinica/api/auth/token/validate";
	
    private final Client client;

    public TokenValidator() {
        this.client = ClientBuilder.newClient();
    }

    public boolean validateToken(String token) {
        // Primero validamos que sea un JWT válido
        /*if (!isValidJWTFormat(token)) {
            return false;
        }*/

        // Luego validamos contra la API
        return validateTokenWithAPI(token);
    }

    /*private boolean isValidJWTFormat(String token) {
        try {
            byte[] keyBytes = JWT_SECRET.getBytes();
            
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                .build()
                .parseClaimsJws(token)
                .getBody();
                
            // Validar que el token no haya expirado
            if (claims.getExpiration().before(new Date())) {
                return false;
            }
            
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Error validando JWT: " + e.getMessage());
            return false;
        }
    }*/

    public static class UserResponse implements Serializable {
        private Double appCliId;
        private String cuenta;
        private String nombre;
        private String email;

        // Getters
        public Double getAppCliId() { return appCliId; }
        public String getCuenta() { return cuenta; }
        public String getNombre() { return nombre; }
        public String getEmail() { return email; }
    }
    

    private boolean validateTokenWithAPI(String token) {
    	String response = "";
    	try {
    		HttpRequestUtil.disableSslValidation();
            URL url = new URL(API_VALIDATION_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Establecer el método de la solicitud
            connection.setRequestMethod("POST");

            // Establecer los encabezados necesarios
            connection.setRequestProperty("Content-Type", "text/plain");  // Si el cuerpo es texto plano
            connection.addRequestProperty("Authorization", "Bearer " + token);  // Usar "Bearer" si la API lo requiere

            // Habilitar la opción de salida para el cuerpo de la solicitud
            connection.setDoOutput(true);

            // El token se enviará como texto plano en el cuerpo
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = token.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Obtener el código de respuesta
            int responseCode = connection.getResponseCode();
            InputStream inputStream;

            // Verificamos si el código de respuesta es un error
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }

            // Leer la respuesta
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"))) {
                StringBuilder responseBuilder = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    responseBuilder.append(responseLine.trim());
                }
                response = responseBuilder.toString();
            }

            System.out.println("RESPONSE VALIDACIÓN TOKEN: " + response);
            
            if(responseCode == HttpURLConnection.HTTP_OK && response.contains("Token válido")) {            	
            	return true;
            }
            
            return false;
        	
        } catch (Exception e) {
            e.printStackTrace();
            response = "{\"error\": \"Error al obtener los datos del paciente\"}";
            return false;
        }
    	
    }
    
    // Método para cerrar el cliente cuando ya no se necesite
    public void close() {
        if (client != null) {
            client.close();
        }
    }

}
