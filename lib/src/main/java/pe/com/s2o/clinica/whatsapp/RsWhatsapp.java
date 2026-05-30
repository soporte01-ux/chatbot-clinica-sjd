package pe.com.s2o.clinica.whatsapp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.luxor.modulos.web.util.dev.UtilAppDate;
import com.luxor.modulos.web.util.dev.UtilAppEncrypt;
import com.luxor.modulos.web.util.dev.UtilAppString;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
/*import pe.com.s2o.clinica.citas.rs.RequestData;
import pe.com.s2o.clinica.citas.rs.config.ConfigRsCitasV1;
import pe.com.s2o.clinica.citas.util.SqlUtilData;*/
import pe.com.s2o.clinica.ws.RsWs;
import pe.com.s2o.util.rs.client.UtilResponse;
import pe.com.s2o.clinica.dtos.SitedsSolAutorizacionDto;
import pe.com.s2o.clinica.dtos.SitedsSolAutorizacionExeCarDto;
import pe.com.s2o.clinica.dtos.SitedsSolAutorizacionProEspDto;
import pe.com.s2o.clinica.dtos.SitedsSolAutorizacionRestricDto;
import pe.com.s2o.clinica.dtos.SitedsSolAutorizacionTieEspDto;
import pe.com.s2o.clinica.siteds.RsSiteds;
import pe.com.s2o.clinica.utils.HttpRequestUtil;
import pe.com.s2o.clinica.utils.HttpRequestUtil.HttpResponse;

/**
 * Session Bean implementation class RsWhatsapp
 */
@Path("/whatsapp")
@Stateless
@LocalBean
public class RsWhatsapp {
	
	private static final ConcurrentHashMap<String, ConversationSession> userConversationState = new ConcurrentHashMap<>();
    private static final long SESSION_TIMEOUT_MINUTES = 15 * 60;   
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	public static Integer codOrg = 1;
	private static final List<String> IAFAS_AVAILABLE = Arrays.asList(
		    "20002", "20001", "40007", "20004", "40004", "40005"
		);
	private static final String SECRET_KEY_ENCRYPT = "OaTtkWdvFvGwgeHh";
    private static final String TYPE_ENCRYPT = "AES";
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
	
	
    public RsWhatsapp() {
    	scheduler.scheduleAtFixedRate(this::cleanExpiredSessions, 1, 1, TimeUnit.MINUTES);
    }

    @GET
    @Path("/webhookCli")
    public Response verifyWebhook(@QueryParam("hub.mode") String mode, @QueryParam("hub.challenge") String challenge, @QueryParam("hub.verify_token") String verifyToken) {
    	
    	if ("subscribe".equals(mode) && GlobalConstants.API_VERIFY_TOKEN.equals(verifyToken)) {
    		return Response.ok(challenge).build();
    	} else {
    		return Response.status(Response.Status.FORBIDDEN).build();
    	}
    }
    
    
    @POST
    @Path("/webhookSjd")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response recibirWebhook(Map<String, Object> data) {
        try {
        	Gson gson = new Gson();
        	String jsonWsp = gson.toJson(data);
            System.out.println("📩 Webhook recibido:");
            System.out.println("Mensaje: " + data);
            String respuesta = "";
            String phone_number = (String) data.get("phone_number");
            if (phone_number != null && userConversationState.containsKey(phone_number)) {
                ConversationSession session = userConversationState.get(phone_number);
                sendMessageFinalizar(phone_number, "👨‍💼 El encargado ha culminado la sesión, si deseas volver a comunicarte o realizar otra acción puedes iniciar una nueva escribiendo un mensaje.");
                session.endConversation();
                userConversationState.remove(phone_number);
                respuesta = "ok";
            }
            else {
            	respuesta = "No hay una sesión activa para el paciente.";
            }
            
            return Response.ok("{\"status\":\""+ respuesta +"\"}").build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al procesar el webhook"))
                    .build();
        }
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/imagenWsp")
    @Produces({"image/png", "image/jpeg", "image/webp"})
    public Response getImageFromLinkWithToken(Map<String, String> request) {
        try {
            String imageUrl = request.get("url");

            if (imageUrl == null || imageUrl.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Debe proporcionar una URL de imagen válida.")
                        .build();
            }

            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + GlobalConstants.API_ACCESS_TOKEN);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Token no autorizado o inválido.")
                        .build();
            } else if (responseCode != HttpURLConnection.HTTP_OK) {
                return Response.status(Response.Status.BAD_GATEWAY)
                        .entity("Error al acceder a la imagen: Código " + responseCode)
                        .build();
            }

            String contentType = connection.getContentType();

            if (contentType == null ||
                !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"))) {
                return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
                        .entity("Solo se permiten imágenes JPG, PNG o WEBP.")
                        .build();
            }

            InputStream imageStream = connection.getInputStream();
            return Response.ok(imageStream, contentType).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error al procesar la imagen.")
                    .build();
        }
    }

    @GET
    @Path("/media/{mediaId}")
    public Response getMediaFromMeta(@PathParam("mediaId") String mediaId) {
        HttpURLConnection metadataConnection = null;
        HttpURLConnection mediaConnection = null;

        try {
            if (mediaId == null || mediaId.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Debe proporcionar un mediaId valido.")
                        .build();
            }

            URL metadataUrl = new URL("https://graph.facebook.com/v20.0/" + mediaId);
            metadataConnection = (HttpURLConnection) metadataUrl.openConnection();
            metadataConnection.setRequestMethod("GET");
            metadataConnection.setRequestProperty("Authorization", "Bearer " + GlobalConstants.API_ACCESS_TOKEN);
            metadataConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            int metadataResponseCode = metadataConnection.getResponseCode();
            InputStream metadataStream = metadataResponseCode < HttpURLConnection.HTTP_BAD_REQUEST
                    ? metadataConnection.getInputStream()
                    : metadataConnection.getErrorStream();

            StringBuilder metadataResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(metadataStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    metadataResponse.append(line);
                }
            }

            if (metadataResponseCode != HttpURLConnection.HTTP_OK) {
                return Response.status(Response.Status.BAD_GATEWAY)
                        .entity("Error obteniendo metadata de media: " + metadataResponse.toString())
                        .build();
            }

            String mediaUrl;
            try (JsonReader reader = Json.createReader(new StringReader(metadataResponse.toString()))) {
                JsonObject metadata = reader.readObject();
                mediaUrl = metadata.getString("url", "");
            }

            if (mediaUrl == null || mediaUrl.trim().isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Meta no devolvio URL para el mediaId solicitado.")
                        .build();
            }

            URL url = new URL(mediaUrl);
            mediaConnection = (HttpURLConnection) url.openConnection();
            mediaConnection.setRequestMethod("GET");
            mediaConnection.setRequestProperty("Authorization", "Bearer " + GlobalConstants.API_ACCESS_TOKEN);

            int mediaResponseCode = mediaConnection.getResponseCode();
            if (mediaResponseCode != HttpURLConnection.HTTP_OK) {
                return Response.status(Response.Status.BAD_GATEWAY)
                        .entity("Error descargando media desde Meta. Codigo: " + mediaResponseCode)
                        .build();
            }

            String contentType = mediaConnection.getContentType();
            if (contentType == null || contentType.trim().isEmpty()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM;
            }

            InputStream mediaStream = mediaConnection.getInputStream();
            return Response.ok(mediaStream, contentType)
                    .header("Cache-Control", "private, max-age=300")
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error procesando media de WhatsApp.")
                    .build();
        } finally {
            if (metadataConnection != null) {
                metadataConnection.disconnect();
            }
        }
    }
     
    @POST
    @Path("/webhookCli")
    @Consumes("application/json")
    public Response receiveWebhook(String payload) {
    	System.out.println("PAYLAOD BOT MENSAJE: " + payload);
        try (JsonReader jsonReader = Json.createReader(new StringReader(payload))) {
            JsonObject jsonObject = jsonReader.readObject();
            JsonArray entries = jsonObject.getJsonArray("entry");
            if (entries != null && !entries.isEmpty()) {
                JsonObject entry = entries.getJsonObject(0);  // Puedes ajustar el índice según la estructura del payload
                JsonArray changes = entry.getJsonArray("changes");
                if (changes != null && !changes.isEmpty()) {
                    JsonObject change = changes.getJsonObject(0); // Ajusta el índice según la estructura del payload
                    JsonObject value = change.getJsonObject("value");
                    JsonArray messages = value.getJsonArray("messages");
                    if (messages != null && !messages.isEmpty()) {
                        JsonObject message = messages.getJsonObject(0); // Ajusta el índice según la estructura del payload
                    	String from = message.getString("from");
                    	String type = message.getString("type");
                    	//String body = extractMessageBody(type, message);
                    	String timestamp = message.getString("timestamp");
                    	String messageId = message.getString("id");
                    	//String body = message.getJsonObject("text").getString("body");                        	    
                    	System.out.println("---------------------INICIO BODY CONTENIDO-------------------");
                        /*RsWs wsInstance = new RsWs();
                        wsInstance.handleIncomingWhatsAppMessage(from, body);*/
                    	
	                    	//RsWs.broadcastMessage(from, body, type, timestamp, messageId, message);
                    	
                    	bodyContentMessage(type, message, from, payload);
                    	System.out.println("---------------------FINAL BODY CONTENIDO--------------------");
                    }
                }
            }
            return Response.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).entity("Error procesando el evento").build();
        }
    }
    
    
    @POST
    @Path("/send-message")
    @Consumes("application/json")
    @Produces("application/json")
    public Response sendMessage(JsonObject payload) {
        try {
            String to = payload.getString("to");
            String message = payload.getString("message");
            String messageId = sendToWhatsApp(to, message);

            if (messageId != null) {
                String timestamp = String.valueOf(Instant.now().toEpochMilli());
                RsWs.notifyMessageSent(to, message, timestamp, messageId);

                return Response.ok(Json.createObjectBuilder()
                        .add("success", true)
                        .add("message", "Mensaje enviado correctamente")
                        .add("message_id", messageId)
                        .build())
                    .build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Json.createObjectBuilder()
                        .add("success", false)
                        .add("message", "Error al enviar mensaje")
                        .build())
                    .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Error: " + e.getMessage())
                    .build())
                .build();
        }
    }
    
    private String sendToWhatsApp(String to, String message) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://graph.facebook.com/v20.0/" + GlobalConstants.PHONE_NUMBER_ID + "/messages");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + GlobalConstants.API_ACCESS_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // Crear el cuerpo JSON
            JsonObject payload = Json.createObjectBuilder()
                .add("messaging_product", "whatsapp")
                .add("to", to)
                .add("type", "text")
                .add("text", Json.createObjectBuilder()
                    .add("body", message))
                .build();

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode < HttpURLConnection.HTTP_BAD_REQUEST)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            // Leer la respuesta
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            System.out.println("📩 Respuesta Meta: " + response);

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                // Parsear JSON para obtener el ID
                try (JsonReader reader = Json.createReader(new StringReader(response.toString()))) {
                    JsonObject jsonResponse = reader.readObject();
                    if (jsonResponse.containsKey("messages")) {
                        JsonArray messages = jsonResponse.getJsonArray("messages");
                        if (!messages.isEmpty()) {
                            JsonObject msg = messages.getJsonObject(0);
                            String messageId = msg.getString("id");
                            return messageId; // ✅ Devolvemos el ID del mensaje
                        }
                    }
                }
            } else {
                System.err.println("❌ Error al enviar mensaje. Código: " + responseCode);
                System.err.println("Respuesta: " + response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return null; // ❌ si algo falla
    }
    
    private String extractMessageBody(String type, JsonObject message) {
        try {
            switch (type) {
                case "text":
                    return message.getJsonObject("text").getString("body");
                case "image":
                    return extractMediaCaption(message, "image", "[Imagen recibida]");
                case "video":
                    return extractMediaCaption(message, "video", "[Video recibido]");
                case "audio":
                    return "[Audio recibido]";
                case "document":
                    JsonObject document = message.getJsonObject("document");
                    return document.getString("filename", "[Documento recibido]");
                case "sticker":
                    return "[Sticker recibido]";
                case "location":
                    JsonObject location = message.getJsonObject("location");
                    return location.getString("name", location.getString("address", "[Ubicación recibida]"));
                case "interactive":
                    return extractInteractiveBody(message);
                default:
                    return "[Mensaje de tipo: " + type + "]";
            }
        } catch (Exception e) {
            return "[Error extrayendo mensaje]";
        }
    }

    private String extractMediaCaption(JsonObject message, String mediaType, String fallback) {
        JsonObject media = message.getJsonObject(mediaType);
        return media.getString("caption", fallback);
    }

    private String extractInteractiveBody(JsonObject message) {
        JsonObject interactive = message.getJsonObject("interactive");
        String interactiveType = interactive.getString("type", "");

        if ("button_reply".equals(interactiveType)) {
            JsonObject buttonReply = interactive.getJsonObject("button_reply");
            return buttonReply.getString("title", buttonReply.getString("id", "[Respuesta de botón]"));
        }

        if ("list_reply".equals(interactiveType)) {
            JsonObject listReply = interactive.getJsonObject("list_reply");
            return listReply.getString("title", listReply.getString("id", "[Respuesta de lista]"));
        }

        return "[Mensaje interactivo]";
    }
    
    public static void main(String[] args) {
    	HttpResponse responseData = null;
    	String response = "";
    	/*String token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJXaGF0c2FwcCIsInJvbCI6IlJPTEVfVVNFUiIsImVtYWlsIjoiSkVBTkZSQU5DT0NBQkFOQUgxMjMzQEdNQUlMLkNPTSIsImNvZFBlcnNvbmEiOjM4ODExNiwibm9tYnJlUGVyc29uYSI6IkNBQkFOQSBIVUFZSFVBIEpFQU4gRlJBTkNPIiwiZ2VuZXJvIjoiTSIsImlhdCI6MTc0OTU4MjQ4MSwiZXhwIjoxNzUzMTgyNDgxfQ.znsC89jJ8vXKKm0IavL3r565Nkgiht5xkOHmlu-4bnep2xAnFpNGlbp999DHk2rnhuYSDXbq255DC5qTyDxrsA";
    	Integer idCita = 496941;
        System.out.println("TOKEN: " + token + " | IDCITA: " + idCita);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "Bearer " + token);
        responseData  = HttpRequestUtil.sendRequest("GET",GlobalConstants.API_ANULAR_PRE_RESERVA + "?idCita=" + idCita, null, headers);
        response = responseData.getResponseBody();*/
        LocalDate fechaActual = LocalDate.now();

        int dia = fechaActual.getDayOfMonth();
        int mes = fechaActual.getMonthValue();
        int anio = fechaActual.getYear();
        System.out.println("Día: " + dia);
        System.out.println("Mes: " + mes);
        System.out.println("Año: " + anio);
        String fechaActualStr = String.format("%s-%s-%s00:00", anio, mes, dia); 
		Integer especialidadId = 8;
		String especialidadDesc = "CARDIOLOGIA";
		List<Map<String, Object>> lstEspecialidadesFechas = new ArrayList<>();
		try {
	        Map<String, String> headers = new HashMap<String, String>();
	        headers.put("Content-Type", "application/json; charset=utf-8");
	        headers.put("Authorization", GlobalConstants.API_DEFAULT_TOKEN_RESERVAS);
	        //String inputJson = String.format("{\"idEspecialidad\":%s,\"anio\":\"%s\",\"mes\":\"%s\",\"dia\":%s,\"sucursal\":\"\"}", especialidadId, anio, mes, dia);
	        responseData  = HttpRequestUtil.sendRequest("GET", GlobalConstants.API_OBTENER_PREVENTA, null, headers);
	        response = responseData.getResponseBody();
			
		} catch (Exception e) {
			e.printStackTrace();
			//sendMessageFinalizar(to, "😓 No se ha podido obtener los horarios para esta especialidad en este momento, intentalo nuevamente mas tarde.");
			return;
		}
		
		System.out.println("RESPONSE FECHAS: " + response);
    }
    
    private void updateUserSession(String from, ConversationSession session) {
        session.updateLastActivity();
    }
    
    private ConversationSession getUserSessionFrom(String from) {
        return userConversationState.computeIfAbsent(from, k -> new ConversationSession(new ConcurrentHashMap<>()));
    }
    
    /* CONTROL DE ACTIVIDAD DE MENSAJES */
    

    private static class ConversationSession {
        Map<String, Object> state;
        Instant lastActivity;
        Instant conversationStartTime;
        boolean conversationEnded;
        
        ConversationSession(Map<String, Object> state) {
            this.state = state;
            this.lastActivity = Instant.now();
            this.conversationStartTime = this.lastActivity;
            this.conversationEnded = false;
        }

        void endConversation() {
            this.conversationEnded = true; // Marcar que la conversación terminó
        }
        
        Instant getConversationStartTime() {
            return conversationStartTime;
        }
        
        boolean isConversationEnded() {
            return this.conversationEnded;
        } 
        
        void updateLastActivity() {
            this.lastActivity = Instant.now();
        }

        boolean isExpired() {
            return Instant.now().isAfter(lastActivity.plusSeconds(SESSION_TIMEOUT_MINUTES));
        }
    }
    
    private void cleanExpiredSessions() {
        userConversationState.entrySet().removeIf(entry -> {
            ConversationSession session = entry.getValue();
            
            if (session.isExpired() && !session.isConversationEnded()) {
                sendSessionExpiredMessage(entry.getKey());
                Map<String, Object> conversationState = session.state;
            	if(conversationState.get("idCitaPreReserva") != null) {
            		String idCitaPre = conversationState.get("idCitaPreReserva").toString();
            		String token = (String) conversationState.get("token");
            		eliminarVentaWsp(idCitaPre, token, entry.getKey());
            	}
                session.endConversation();
                return true;
            }
            return session.isConversationEnded();
        });
        if (userConversationState.isEmpty()) {
            shutdown();
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
    
    private void sendSessionExpiredMessage(String to) {
        String message = "Tu sesión ha expirado debido a inactividad. Por favor, inicia una nueva conversación si necesitas ayuda.";
        sendMessage(to, message);
    }
		
    /* CONTENIDO DEL MENSAJE PARA ENVIAR AL USUARIO */
    
    public void bodyContentMessage(String type, JsonObject message, String from, String fullMessage) {
    	ConversationSession session = getUserSessionFrom(from);
    	try {
    		 Map<String, Object> conversationState = session.state;
    		 long inicio = session.getConversationStartTime().getEpochSecond();;
    		 conversationState.put("inicioConversacion", inicio);
            System.out.println("Estado antes de procesar mensaje: " + conversationState);  
            String moduleSelected = (String) conversationState.getOrDefault("modulo", "");
            if(moduleSelected.equals("encargado")) {
            	proccessEncargadoWebhoock(from, fullMessage, conversationState);
            }
            else {
                if(type.equals("text")) {
                    String userMessage = message.getJsonObject("text").getString("body");
                    System.out.println("Mensaje Enviado: " + userMessage);
                    Boolean acceptedDefault = (Boolean) conversationState.getOrDefault("accepted", false);
                    if (acceptedDefault) {
                    	
                        if (userMessage.trim().equalsIgnoreCase("retornar")) {
                            handlePreviousStep(from, conversationState);
                            return;
                        }
                    	
                    	String currentModule = (String) conversationState.getOrDefault("modulo", "");
                    	switch(currentModule) {
                    	case "reservar_cita":
                    		processReservaCita(from, userMessage, conversationState);
                    		break;
                    	case "consulta_fecha":
                    		processReservaCita(from, userMessage, conversationState);
                    		break;
                    	case "consulta_horarios":
                    		processSubReservaFecha(from, userMessage, conversationState);
                    		break;
                    	case "sub_reserva_fecha":
                    		processSubReservaFecha(from, userMessage, conversationState);
                    		break;
                    	case "vista_horarios":
                    		break;
                    	case "sub_reserva_horario":
                    		processSubReservaHorario(from, userMessage, conversationState);
                    		break;
                    	case "sub_reserva_seguro":
                    		processSubReservaSeguro(from, userMessage, conversationState);
                    		break;
                    	case "citas_programadas":
                    	case "historial_citas":
                    		processHistorialCita(from, userMessage, conversationState);
                    		break;
                    	case "obtener_mes":
                    		//processObtenerCitas(from, userMessage, conversationState);
                    		break;
                    	case "registrar_otro_cliente":
                    		//processNuevoCliente(from, userMessage, conversationState);
                    		break;
                    	/*case "encargado":
                    		proccessHablarEncargado(from, userMessage, conversationState, null);
                    		break;*/
                    	case "actualizar_correo":
                    		proccessActualizarCorreo(from, userMessage, conversationState);
                    		break;
                    	default:
                    		processDocumentInput(from, userMessage, conversationState);
                    		break;
                    	}
                    } else {
                        sendAcceptTermins(from);
                    }
                           
                }
                /*else if (type.equals("image")) {
                	String currentModule = (String) conversationState.getOrDefault("modulo", "");
                    JsonObject imageObject = message.getJsonObject("image");
                    String mediaId = imageObject.getString("id");
                    String mimeType = imageObject.getString("mime_type");
                    String messageImgInfo = "IMAGE_USER:" + mediaId + "|" + mimeType;
                	switch(currentModule) {
                	case "encargado":
                		proccessHablarEncargado(from, messageImgInfo, conversationState, null);
                		break;
                	default:
                		sendMessage(from, "Las imagenes enviadas solo son vistas por el encargado, comunicate con uno para poder resolver tu inconveniente.");
                		break;
                	}
                }*/           
                
                else if (type.equals("interactive")) {
                    JsonObject interactive = message.getJsonObject("interactive");
                    String typeInteractive = interactive.getString("type");
                    
                    if (typeInteractive.equals("button_reply")) {
                        String buttonId = interactive.getJsonObject("button_reply").getString("id");
                        switch(buttonId) {
                        case "accept_terms":
                            conversationState.put("accepted", true);
                            sendTypeDocumet(from);
                            break;
                        case "decline_terms":
                        	sendDeclinarTerminos(from, session);
                        	break;
                        case "doc_dni":
                        case "doc_ce":
                        case "doc_pasaporte":
                        	if(handleEmptyConversationIsFull(from, conversationState)) {
                        		processRegDocumento(from, interactive, conversationState);
                        	}
                            break;
                        case "registrar_cita_seguro":
                        	if(handleEmptyConversationIsFull(from, conversationState)) {
                            	sendRegistroCita(from, conversationState, session);
                        	}
                        	break;
                        case "registrar_cita":
                        	if(handleEmptyConversationIsFull(from, conversationState)) {
                        		//sendBFRequerido(from);
                        		sendPagoLink(from, conversationState);
                        	}
                        	break;
                        case "factura":
                        	if(handleEmptyConversationIsFull(from, conversationState)) {
                        		conversationState.put("tipoDocEmision", buttonId);
                        		sendDocTypeDocEmiBo(from);
                        		//sendFormatoNuevoCliente(from, conversationState);
                        	}
                        	break;
                        case "boleta":
                        	if(handleEmptyConversationIsFull(from, conversationState)) {
                        		conversationState.put("tipoDocEmision", buttonId);
                        		conversationState.put("otroCliente", false);
                            	sendDetallesBFAndOtroUsuario(from, conversationState);
                        	}
                        	break;
                        case "otro_cliente":
                        	if(handleEmptyConversationIsFull(from, conversationState)) {
                        		sendDocTypeDocEmiBo(from);
                            	//sendFormatoNuevoCliente(from, conversationState);
                        	}
                        	break;
                        case "continuar_pago":
                        	if(handleEmptyConversationIsFull(from, conversationState)) {
                        		sendPagoLink(from, conversationState);
                        	}
                        	break;
                        case "retornar":
                        	if(handleEmptyConversationIsFull(from, conversationState)) {
                              	/*if(conversationState.get("idCitaPreReserva") != null) {
                            		String idCitaPre = conversationState.get("idCitaPreReserva").toString();
                            		//eliminarVentaWsp(codeVenta);
                            	}*/
                        		handlePreviousStep(from, conversationState);
                        	}
                        	break;
                        case "realizar_venta":
                        	if(handleEmptyConversationIsFull(from, conversationState)) {
                        		/*boolean pago = validarPago(from, conversationState, session);
                        		if(pago) {
                        			sendRegistroCita(from, conversationState, session);
                        		}*/
                        	}
                        	break;
                        case "terminar_sesion":
                        	if(conversationState.get("idCitaPreReserva") != null) {
                        		String idCitaPre = conversationState.get("idCitaPreReserva").toString();
                        		String token = (String) conversationState.get("token");
                        		eliminarVentaWsp(idCitaPre, token, from);
                        	}
                        	sendTerminoDeSesion(from, session);
                        	break;
                    	case "contactar_soporte":
    	                	if(handleEmptyConversationIsFull(from, conversationState)) {
    	                		conversationState.put("userDocumentType","");
    	                		proccessEncargadoWebhoock(from, buttonId, conversationState);
    	                	}
    	                	/*if(handleEmptyConversationIsFull(from, conversationState)) {
    	                		conversationState.put("userDocumentType","");
    	                		proccessHablarEncargado(from, "Consulta Normal", conversationState, null);
    	                	}*/
    	                	break;
                        }
                    }
                    else if (typeInteractive.equals("list_reply")) {
                    	String buttonId = interactive.getJsonObject("list_reply").getString("id");
                    	switch (buttonId) {
                        case "reservar_cita":
                        	if(handleEmptyConversationIsFull(from, conversationState)) {
                            	conversationState.put("userDocumentType","");
                            	processSendListaEspecialidad(from, buttonId, conversationState);
                        	}
                        	break;
                        case "chequeos_previos":
                        	if(handleEmptyConversationIsFull(from, conversationState)) {
                            	conversationState.put("userDocumentType","");
                            	sendMessageFinalizar(from, "Consulta aquí nuestros paquetes disponibles, celular 958958114.");
                            	return;
                        	}
                        	break;
                        case "citas_programadas":
                        	if(handleEmptyConversationIsFull(from, conversationState)) {
                        		conversationState.put("userDocumentType","");
                        		conversationState.put("tipoCita","pagados");
                        		processHistorialCitas(from, buttonId, conversationState);
                        	}
                        	break;
                        case "historial_citas":
                        	if(handleEmptyConversationIsFull(from, conversationState)) {
                        		conversationState.put("userDocumentType","");
                        		conversationState.put("tipoCita","general");
                        		processHistorialCitas(from, buttonId, conversationState);
                        	}
                        	break;
                        case "info_horarios":
    	                	if(handleEmptyConversationIsFull(from, conversationState)) {
    	                		conversationState.put("userDocumentType","");
    	                		processSendListaEspecialidad(from, buttonId, conversationState);
    	                	}
                        	break;
                        case "promociones_clinica":
    	                	if(handleEmptyConversationIsFull(from, conversationState)) {
    	                		conversationState.put("userDocumentType","");
    	                		processPromocionesClinica(from, buttonId, conversationState);
    	                	}
                        	break;
                        case "retornar":
                        	if(handleEmptyConversationIsFull(from, conversationState)) {
                              	/*if(conversationState.get("idCitaPreReserva") != null) {
                            		String idCitaPre = conversationState.get("idCitaPreReserva").toString();
                            		//eliminarVentaWsp(codeVenta);
                            	}*/
                        		handlePreviousStep(from, conversationState);
                        	}
                        	break;
                        case "terminar_sesion":
                        	if(conversationState.get("idCitaPreReserva") != null) {
                        		String idCitaPre = conversationState.get("idCitaPreReserva").toString();
                        		String token = (String) conversationState.get("token");
                        		eliminarVentaWsp(idCitaPre, token, from);
                        	}
                        	sendTerminoDeSesion(from, session);
                        	break;
                        case "contactar_soporte":
    	                	if(handleEmptyConversationIsFull(from, conversationState)) {
    	                		conversationState.put("userDocumentType","");
    	                		proccessEncargadoWebhoock(from, buttonId, conversationState);
    	                	}
    	                	/*if(handleEmptyConversationIsFull(from, conversationState)) {
    	                		conversationState.put("userDocumentType","");
    	                		proccessHablarEncargado(from, "Consulta Normal", conversationState, null);
    	                	}*/
                        	break;
                        case "DNI":
                        case "RUC":
                        case "PASAPORTE":
                        case "CE":
    	                	if(handleEmptyConversationIsFull(from, conversationState)) {
    	                		conversationState.put("docEmisionTipoGeneral", buttonId);
    	                		sendFormatoNuevoCliente(from, conversationState);
    	                		//procces(from, buttonId, conversationState);
    	                	}
                        	break;
    					}
                    }
                }
            }

            System.out.println("Estado después de procesar mensaje: " + conversationState);
            if (userConversationState.containsKey(from)) {
                updateUserSession(from, session);
            }
    	}catch(Exception ex) {
    		ex.printStackTrace();
    	}
    }
    
    private boolean handleEmptyConversationIsFull(String from, Map<String, Object> conversationState) {
    	if(!conversationState.isEmpty()) {
    		return true;
    	}
		sendAcceptTermins(from);
		return false;
    }
    
    private void handlePreviousStep(String from, Map<String, Object> conversationState) {
        String currentModule = (String) conversationState.getOrDefault("modulo", "");
        String token = (String) conversationState.get("token");
        switch(currentModule) {
	        case "elegir_modulo":
	        	sendMessage(from, "🔁 Retornando a la selección de documento.");
	        	conversationState.put("modulo", "accept_terms");
	            conversationState.put("accepted", true);
	            sendTypeDocumet(from);
	        	break;
        	case "encargado":
        	case "sin_encargado":
	        case "citas_programadas":
	        case "historial_citas":
	        case "reservar_cita":
	        case "consulta_fecha":
	    		String nombrePersona = conversationState.get("perNombre1").toString() + " "
	    				+ obtenerPrimeraLetra(conversationState.get("perNombre2").toString()) + " "
	    				+ conversationState.get("perApePaterno").toString() + " " + obtenerPrimeraLetra(conversationState.get("perApeMaterno").toString());
	    		String nombre = capitalizeFirstLetter(nombrePersona);
	        	conversationState.put("modulo", "elegir_modulo");
	        	sendButtonsModulos(from, nombre);
	        	break;
            case "sub_reserva_horario":
            	conversationState.put("modulo", "sub_reserva_fecha");
                sendMessage(from, "🔁 Retornando a la selección de fecha.");
                sendListFechasXMedico(from, token, conversationState, true);
                break;
            case "vista_horarios":
            	conversationState.put("modulo", "consulta_horarios");
                sendMessage(from, "🔁 Retornando a la selección de fecha.");
                //sendListFechas(from, token, conversationState);
                break;
            case "consulta_horarios":
            	conversationState.put("modulo", "consulta_fecha");
            	sendMessage(from, "🔁 Retornando a la selección de especialidad.");
            	sendListEspecialidad(from, token, conversationState);
            	break;
            case "sub_reserva_fecha":
            	conversationState.put("modulo", "reservar_cita");
                sendMessage(from, "🔁 Retornando a la selección de especialidad.");
                sendListEspecialidad(from, token, conversationState);
                break;
            case "sub_reserva_seguro":
            	conversationState.put("modulo", "sub_reserva_horario");
                sendMessage(from, "🔁 Retornando a la selección de horario.");
                sendListHorariosXDisponibles(from, token, conversationState);
                break;
            case "obtener_mes":
            	conversationState.put("modulo", "historial_citas");
                sendMessage(from, "🔁 Retornando a obtener año.");
    			String requestBody = "{\r\n"
        				+ "    \"messaging_product\": \"whatsapp\",\r\n"
        				+ "    \"recipient_type\": \"individual\",\r\n"
        				+ "    \"to\": \""+from+"\",\r\n"
        				+ "    \"type\": \"interactive\",\r\n"
        				+ "    \"interactive\": {\r\n"
        				+ "        \"type\": \"button\",\r\n"
        				+ "        \"body\": {\r\n"
        				+ "            \"text\": \"¡Excelente! 😊 Por favor, indícanos el año del cual te gustaría recibir tu historial. ¡Estamos aquí para ayudarte! (Ejm: 2024)\"\r\n"
        				+ "        },\r\n"
        				+ "        \"action\": {\r\n"
        				+ "            \"buttons\": [\r\n"
        				+ "                {\r\n"
        				+ "                    \"type\": \"reply\",\r\n"
        				+ "                    \"reply\": {\r\n"
        				+ "                        \"id\": \"retornar\",\r\n"
        				+ "                        \"title\": \"🔁 Atrás\"\r\n"
        				+ "                    }\r\n"
        				+ "                },\r\n"
        				+ "                {\r\n"
        				+ "                    \"type\": \"reply\",\r\n"
        				+ "                    \"reply\": {\r\n"
        				+ "                        \"id\": \"terminar_sesion\",\r\n"
        				+ "                        \"title\": \"🔚 Finalizar\"\r\n"
        				+ "                    }\r\n"
        				+ "                }\r\n"
        				+ "            ]\r\n"
        				+ "        }\r\n"
        				+ "    }\r\n"
        				+ "}";
    			sendHttpRequest(requestBody);
                break;
            case "obtener_citas_general":
            	conversationState.put("modulo", "obtener_mes");
                sendMessage(from, "🔁 Retornando a seleccionar mes.");
                sendListMeses(from, conversationState);
                break;
            default:
            	sendMessage(from, "No se puede volver más atrás.");
                break;
        }
    }
    
    
    private void processSendListaEspecialidad(String from, String buttonId ,Map<String, Object> conversationState) {
    	try {							
    		if ("reservar_cita".equals(buttonId) || "info_horarios".equals(buttonId)) {
    			System.out.println("MODULO ESPECIALIDAD:" + conversationState.get("modulo").toString());
    			if("info_horarios".equals(buttonId)) {
    				conversationState.put("modulo", "consulta_fecha");
    			}
    			else {    				
    				conversationState.put("modulo", "reservar_cita");
    			}
    			String token = (String) conversationState.get("token");
    			sendListEspecialidad(from, token, conversationState);
    		}
		} catch (Exception e) {
			e.printStackTrace();
			sendMessage(from, "❌ Error al enviar la lista de especialidades. Por favor, intenta de nuevo.");
		}
    }
    
    private void processPromocionesClinica(String from, String buttonId ,Map<String, Object> conversationState) {
    	try {
    		String mensaje = "✨ *Promociones para ti* ✨\\n\\n"
    		        + "Conoce las *promociones y campañas de salud* de la *Clínica San Juan de Dios Arequipa* 🏥💙\\n\\n"
    		        + "🎁 Más información aquí:\\n"
    		        + "👉 https://www.sanjuandediosarequipa.com/promociones-y-publicidad/\\n\\n"
    		        + "¡Te esperamos! 😊";
            sendMessageFinalizar(from, mensaje);
            return;
		} catch (Exception e) {
            e.printStackTrace();
            sendMessage(from, "❌ Error al procesar tu documento. Por favor, intenta de nuevo.");
		}

    }
    
    private void processRegDocumento(String from, JsonObject interactive ,Map<String, Object> conversationState) {
    	try {
            String tipoDocumento = interactive.getJsonObject("button_reply").getString("title").toUpperCase();
            conversationState.put("userDocumentType", tipoDocumento);
            String mensaje = "*¡Perfecto!* 🎉\\n\\n"
                    + "Ahora, por favor, para continuar *ingresa el número de " + tipoDocumento + "* del paciente a generar la cita.\\n\\n"
                    + "🔢 Para asegurarnos de que todo salga bien, revisa si el número de documento es el correcto y completo.\\n\\n"
                    + "¡Gracias!";
            sendMessage(from, mensaje);
		} catch (Exception e) {
            e.printStackTrace();
            sendMessage(from, "❌ Error al procesar tu documento. Por favor, intenta de nuevo.");
		}

    }
    
    private void processReservaCita(String from, String userMessage, Map<String, Object> conversationState) {
        try {
            int opcion = Integer.parseInt(userMessage) - 1;
            List<Map<String, Object>> lstEsp = (List<Map<String, Object>>) conversationState.get("lstEspecialidades");
            if (lstEsp != null && opcion >= 0 && opcion < lstEsp.size()) {
                Map<String, Object> mapEspecialidad = (Map<String, Object>) lstEsp.get(opcion).get(userMessage);
                conversationState.put("especialidad", mapEspecialidad);
                boolean vistaCompleta = false;
    			if(conversationState.get("modulo").toString().equals("consulta_fecha")) {
    				conversationState.put("modulo", "consulta_horarios");
    				vistaCompleta = false;
    			}
    			else {    				
    				conversationState.put("modulo", "sub_reserva_fecha");
    				vistaCompleta = true;
    			}
    			System.out.println("MODULO RESERVA CITA:" + conversationState.get("modulo").toString());
                String token = (String) conversationState.get("token");
                sendListFechasXMedico(from, token, conversationState, vistaCompleta);
            } else {
                sendMessage(from, "❌ Opción no válida. Por favor, selecciona una opción de la lista.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(from, "❌ Error al procesar tu selección. Por favor, intenta de nuevo.");
        }
    }
    
    private void processSubReservaFecha(String from, String userMessage, Map<String, Object> conversationState) {
        try {
            int opcion = Integer.parseInt(userMessage) - 1;
            List<Map<String, Object>> lstEspFecha = (List<Map<String, Object>>) conversationState.get("especialidadesFechas");
            if (lstEspFecha != null && opcion >= 0 && opcion < lstEspFecha.size()) {        	
            	Map<String, Object> mapEspecialidadFecha = (Map<String, Object>) lstEspFecha.get(opcion).get(userMessage);
            	conversationState.put("especialidad_fecha", mapEspecialidadFecha);
            	String token = (String) conversationState.get("token");
    			if(conversationState.get("modulo").toString().equals("consulta_horarios")) {
    				conversationState.put("modulo", "vista_horarios");
    				//sendListHorariosVista(from, token, conversationState);
    			}
    			else {    				
    				conversationState.put("modulo", "sub_reserva_horario");
    				sendListHorariosXDisponibles(from, token, conversationState);
    			}
    			System.out.println("MODULO RESERVA HORARIO:" + conversationState.get("modulo").toString());
            }else {
            	sendMessage(from, "❌ Opción no válida. Por favor, selecciona una opción de la lista.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(from, "❌ Error al procesar la fecha seleccionada. Por favor, intenta de nuevo.");
        }
    }
  
    private void processSubReservaHorario(String from, String userMessage, Map<String, Object> conversationState) {
        try {
            int opcion = Integer.parseInt(userMessage) - 1;
            List<Map<String, Object>> lstEspHorario = (List<Map<String, Object>>) conversationState.get("lstHorarios");
            if (lstEspHorario != null && opcion >= 0 && opcion < lstEspHorario.size()) {           	
            	Map<String, Object> mapEspecialidadHorario = (Map<String, Object>) lstEspHorario.get(opcion).get(userMessage);
            	conversationState.put("especialidad_horario", mapEspecialidadHorario);
            	conversationState.put("modulo", "sub_reserva_seguro");
            	String nroDoc = (String) conversationState.get("nroDocumento");
            	Integer inDocumento = (Integer) conversationState.get("tipoDocumento");
            	String token = (String) conversationState.get("token");
            	sendSeguros(from, token, COD_APP_CITAS, nroDoc, inDocumento, conversationState);
            }else {
            	sendMessage(from, "❌ Opción no válida. Por favor, selecciona una opción de la lista.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(from, "❌ Error al procesar el horario seleccionado. Por favor, intenta de nuevo.");
        }
    }
    
    /*private void processObtenerCitas(String from, String userMessage, Map<String, Object> conversationState) {
        try {
            int opcion = Integer.parseInt(userMessage) - 1;
            List<Map<String, Object>> lstMeses = (List<Map<String, Object>>) conversationState.get("lstMeses");
            if (lstMeses != null && opcion >= 0 && opcion < lstMeses.size()) {           	
            	Map<String, Object> mapMes = (Map<String, Object>) lstMeses.get(opcion).get(userMessage);
            	conversationState.put("mesSeleccionado", mapMes);
            	conversationState.put("modulo", "obtener_citas_general");
            	Integer codPersona = Integer.valueOf(conversationState.get("codPersona").toString());
            	String mes = (String) mapMes.get("numero");
            	String token = (String) conversationState.get("token");
            	String year = (String) conversationState.get("year");
            	String tipoHistorial = (String) conversationState.get("tipoCita");
            	sendCitas(from, codPersona, token, mes, year, tipoHistorial, conversationState);
            }else {
            	sendMessage(from, "❌ Opción no válida. Por favor, selecciona una opción de la lista.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(from, "❌ Error al procesar el mes seleccionado. Por favor, intenta de nuevo.");
        }
    }*/
    
    private void processSubReservaSeguro(String from, String userMessage, Map<String, Object> conversationState) {
        try {
           /* int opcion = Integer.parseInt(userMessage) - 1;
            List<Map<String, Object>> lstSeguros = (List<Map<String, Object>>) conversationState.get("lstSeguros");
            if (lstSeguros != null && opcion >= 0 && opcion < lstSeguros.size()) {             	
            	Map<String, Object> mapSeguro = (Map<String, Object>) lstSeguros.get(opcion).get(userMessage);
            	System.out.println("SEGURO SELECCIONADO: " + mapSeguro);
            	conversationState.put("especialidad_seguro", mapSeguro);
            	String token = (String) conversationState.get("token");
            	sendInformacionCita(from, token, conversationState);
            }
            else {
            	sendMessage(from, "❌ Opción no válida. Por favor, selecciona una opción de la lista.");
            }*/
        	String claveSeleccionada = userMessage;
        	List<Map<String, Object>> lstSeguros = (List<Map<String, Object>>) conversationState.get("lstSeguros");
        	Map<String, Object> seguroSeleccionado = null;

        	for (Map<String, Object> item : lstSeguros) {
        	    if (item.containsKey(claveSeleccionada)) {
        	        seguroSeleccionado = (Map<String, Object>) item.get(claveSeleccionada);
        	        break;
        	    }
        	}

        	if (seguroSeleccionado != null) {
        	    System.out.println("SEGURO SELECCIONADO: " + seguroSeleccionado);
        	    conversationState.put("especialidad_seguro", seguroSeleccionado);
        	    String token = (String) conversationState.get("token");
        	    int opcionElegida = Integer.parseInt(userMessage);
        	    if (opcionElegida >= 9) {
        	        proccessHablarEncargado(from, "Seguro por Soporte", conversationState, seguroSeleccionado);
        	    } else {
        	        sendInformacionCita(from, token, conversationState);
        	    }
        	} else {
        	    sendMessage(from, "❌ Opción no válida. Por favor, selecciona una opción de la lista.");
        	}
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(from, "❌ Error al procesar el seguro seleccionado. Por favor, intenta de nuevo.");
        }
    }
    
    
    private void processHistorialCitas(String from, String buttonId ,Map<String, Object> conversationState) {
    	try {							
    		if ("historial_citas".equals(buttonId) || "citas_programadas".equals(buttonId)) {
    			conversationState.put("modulo", "historial_citas");
    			String requestBody = "{\r\n"
        				+ "    \"messaging_product\": \"whatsapp\",\r\n"
        				+ "    \"recipient_type\": \"individual\",\r\n"
        				+ "    \"to\": \""+from+"\",\r\n"
        				+ "    \"type\": \"interactive\",\r\n"
        				+ "    \"interactive\": {\r\n"
        				+ "        \"type\": \"button\",\r\n"
        				+ "        \"body\": {\r\n"
        				+ "            \"text\": \"¡Excelente! 😊 Por favor, indícanos el año del cual te gustaría recibir tu historial. ¡Estamos aquí para ayudarte! (Ejm: 2024)\"\r\n"
        				+ "        },\r\n"
        				+ "        \"action\": {\r\n"
        				+ "            \"buttons\": [\r\n"
        				+ "                {\r\n"
        				+ "                    \"type\": \"reply\",\r\n"
        				+ "                    \"reply\": {\r\n"
        				+ "                        \"id\": \"retornar\",\r\n"
        				+ "                        \"title\": \"🔁 Atrás\"\r\n"
        				+ "                    }\r\n"
        				+ "                },\r\n"
        				+ "                {\r\n"
        				+ "                    \"type\": \"reply\",\r\n"
        				+ "                    \"reply\": {\r\n"
        				+ "                        \"id\": \"terminar_sesion\",\r\n"
        				+ "                        \"title\": \"🔚 Finalizar\"\r\n"
        				+ "                    }\r\n"
        				+ "                }\r\n"
        				+ "            ]\r\n"
        				+ "        }\r\n"
        				+ "    }\r\n"
        				+ "}";
    			sendHttpRequest(requestBody);
    		}
		} catch (Exception e) {
			e.printStackTrace();
			sendMessage(from, "❌ Error al enviar el mensaje. Por favor, intenta de nuevo.");
		}
    }
    
    private void processHistorialCita(String from, String userMessage ,Map<String, Object> conversationState) {
    	try {							
    		if (userMessage.matches("\\b(201[0-9]|202[0-4])\\b")) {
    			conversationState.put("year", userMessage);
    			conversationState.put("modulo", "obtener_mes");
    			sendListMeses(from, conversationState);
    		}
		} catch (Exception e) {
			e.printStackTrace();
			sendMessage(from, "❌ Error al enviar los meses. Por favor, intenta de nuevo.");
		}
    }
    
    private void processDocumentInput(String from, String userMessage, Map<String, Object> conversationState) {
        String tipoDocumento = (String) conversationState.getOrDefault("userDocumentType", "");
        if (!tipoDocumento.isEmpty()) {
        	System.out.println("PROCESO DE DOCUMENTO");
            if (userMessage.matches("\\d+") || ("PASAPORTE".equalsIgnoreCase(tipoDocumento) && userMessage.matches("[A-Z]\\d+"))) {
                int longitud = userMessage.trim().length();                     
                int InDocumento = 0;
                if (tipoDocumento.contains("DNI") && longitud == 8) {
                    InDocumento = 1;
                } else if (tipoDocumento.contains("PASAPORTE")) {
                    InDocumento = 3;
                } else if (tipoDocumento.contains("C.E.")) {
                    InDocumento = 4;
                } else {
                    sendMessage(from, "❌ Longitud incorrecta para " + tipoDocumento + ". Por favor, ingresa el número correcto.");
                }
                
                if (InDocumento != 0) {
                    String mensaje = "*Estamos validando tus datos...* 🔍\\r\\n"
                            + "\\r\\n"
                            + "Por favor, espera un momento mientras procesamos la información. ⏳\\r\\n"
                            + "\\r\\n"
                            + "Te notificaremos en breve. ¡Gracias por tu paciencia!";
                    //sendMessage(from, mensaje);
                    try {                           	
                    	Map<String, Object> mapDatosPersona = wspAutenticacion(userMessage, InDocumento, COD_APP_CITAS, from);
                    	System.out.println(mapDatosPersona);
                    	if (mapDatosPersona.get("excepcion") != null) {
                    		mensaje = (String) mapDatosPersona.get("excepcion");
                    		sendMessage(from, mensaje);
                    		return;
                    	} else {
                    		Map<String, Object> mapDatosInfoPersona = (Map<String, Object>) mapDatosPersona.get("personaInfo");
                    		String nombrePersona = mapDatosInfoPersona.get("perNombre1").toString() + " "
                    				+ obtenerPrimeraLetra(mapDatosInfoPersona.get("perNombre2").toString()) + " "
                    				+ mapDatosInfoPersona.get("perApePaterno").toString() + " " + obtenerPrimeraLetra(mapDatosInfoPersona.get("perApeMaterno").toString());
                    		String nombre = capitalizeFirstLetter(nombrePersona);                 	
                    		conversationState.put("token", mapDatosPersona.get("token").toString());
                    		conversationState.put("perNombre1", mapDatosInfoPersona.get("perNombre1").toString());
                    		conversationState.put("perNombre2", mapDatosInfoPersona.get("perNombre2").toString());
                    		conversationState.put("perApePaterno", mapDatosInfoPersona.get("perApePaterno").toString());
                    		conversationState.put("perApeMaterno", mapDatosInfoPersona.get("perApeMaterno").toString());
                    		conversationState.put("codPersona", mapDatosInfoPersona.get("perCod").toString());
                    		conversationState.put("nroDocumento", mapDatosInfoPersona.get("nroDocumento").toString());                 
                    		conversationState.put("tipoDocumento", InDocumento);
                    		conversationState.put("nombrePersona", nombrePersona);
                    		if ("login".equals(mapDatosPersona.get("modo_inicio"))) {  
                    		    String correo = (String) mapDatosPersona.get("correo");
                    		    
                    		    if (correo == null || correo.isEmpty()) {
                    		        conversationState.put("modulo", "actualizar_correo");
                    		        sendMessage(from, "¡No encontramos un *correo electrónico* asociado a tu cuenta! Por favor, ingresa uno para continuar.");
                    		    } else {                    				
                    		    	conversationState.put("correo", correo);
                    		        conversationState.put("modulo", "elegir_modulo");
                    		        sendButtonsModulos(from, nombre);
                    		    }
                		    	/*conversationState.put("correo", correo);
                		        conversationState.put("modulo", "elegir_modulo");
                		        sendButtonsModulos(from, nombre);*/
                    		} else {
                    		    conversationState.put("modulo", "actualizar_correo");
                    		    sendMessage(from, "¡No encontramos un *correo electrónico* asociado a tu cuenta! Por favor, ingresa uno para continuar.");
                    		}
                    	}
                    }
                    catch(Exception ex) {
                    	ex.printStackTrace();
                    	sendMessage(from, "❌ ¡El documento ingresado no se encuentra registrado en el servicio o no se pudo consultar la información, contactate con el area de Atención al Cliente!");
                    	return;
                    }
                }
            } else {
                sendMessage(from, "❌ ¡Ingresa correctamente tu número de documento!");
            }
        } else {
            sendMessage(from, "❌ Por favor, selecciona primero el tipo de documento.");
        }
    }
    
    /*private void proccessEncargado(String from, String userMessage, Map<String, Object> conversationState) {
    	try {
    		RsWs wsInstance = new RsWs();
    		if(!wsInstance.obtenerClientesActivos()) {
    			String requestBody = "{\r\n"
    					+ "    \"messaging_product\": \"whatsapp\",\r\n"
    					+ "    \"recipient_type\": \"individual\",\r\n"
    					+ "    \"to\": \""+from+"\",\r\n"
    					+ "    \"type\": \"interactive\",\r\n"
    					+ "    \"interactive\": {\r\n"
    					+ "        \"type\": \"button\",\r\n"
    					+ "        \"body\": {\r\n"
    					+ "            \"text\": \"¡En estos momentos no hay un encargado conectado para antenderte 😓, intentalo nuevamente más tarde 🙏!\"\r\n"
    					+ "        },\r\n"
    					+ "        \"action\": {\r\n"
    					+ "            \"buttons\": [\r\n"
    					+ "                {\r\n"
    					+ "                    \"type\": \"reply\",\r\n"
    					+ "                    \"reply\": {\r\n"
    					+ "                        \"id\": \"retornar\",\r\n"
    					+ "                        \"title\": \"🔁 Atrás\"\r\n"
    					+ "                    }\r\n"
    					+ "                },\r\n"
    					+ "                {\r\n"
    					+ "                    \"type\": \"reply\",\r\n"
    					+ "                    \"reply\": {\r\n"
    					+ "                        \"id\": \"terminar_sesion\",\r\n"
    					+ "                        \"title\": \"🔚 Finalizar\"\r\n"
    					+ "                    }\r\n"
    					+ "                }\r\n"
    					+ "            ]\r\n"
    					+ "        }\r\n"
    					+ "    }\r\n"
    					+ "}";
    			sendHttpRequest(requestBody);
                conversationState.put("modulo", "sin_encargado");
    			return;
    		}
			sendMessage(from, "¡Hola! 😊\\r\\n"
					+ "\\r\\n"
					+ "Gracias por ponerte en contacto con nosotros. Un encargado se comunicará contigo en breve. ¡Agradecemos tu paciencia! 📲✨");
			
    		String nombrePersona = conversationState.get("perNombre1").toString() + " "
    				+ conversationState.get("perNombre2").toString() + " "
    				+ conversationState.get("perApePaterno").toString() + " " + conversationState.get("perApeMaterno").toString();
    		String nombre = capitalizeFirstLetter(nombrePersona);
            String recurso = "NEW_SESSION:¡Hola! Soy "+ nombre.toUpperCase() +", y quiero comunicarme contigo para resolver algunas dudas. ¿En qué momento podrías atenderme? 😁👍";
            wsInstance.handleIncomingWhatsAppMessage(from, recurso);
            conversationState.put("modulo", "encargado");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }*/
    
    private void proccessEncargadoWebhoock(String from, String userMessage, Map<String, Object> conversationState) {
    	try {
			/*sendMessage(from, "¡Hola! 😊\\r\\n"
					+ "\\r\\n"
					+ "Gracias por ponerte en contacto con nosotros. Un encargado se comunicará contigo en breve. ¡Agradecemos tu paciencia! 📲✨");*/
    		String currentModule = (String) conversationState.getOrDefault("modulo", "");
    		if(currentModule.equals("encargado")) {
        		HttpResponse responseData = null;
        		JsonReader jsonReader = null;
        		JsonObject jsonObject = null;  	        
    	        Map<String, String> headers = new HashMap<String, String>();
    	        headers.put("Content-Type", "application/json; charset=utf-8");
    	       //headers.put("Authorization", "MY_TOKEN");
    	        headers.put("Authorization", "123456");
    	        responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_WEBHOOCK_SOPORTE, userMessage, headers);
    	        String response = responseData.getResponseBody();
    	        System.out.println("RESPONSE MENSAJE ENVIADO POR EL SOPORTE: " + response);
    		}
    		else {
    			/*sendMessage(from, "¡Hola! 😊\\r\\n"
    					+ "\\r\\n"
    					+ "Indica cual es tu consulta para que nuestro encargado pueda atenderte con mayor facilidad. 📲✨");*/
        		HttpResponse responseData = null;
        		JsonReader jsonReader = null;
        		JsonObject jsonObject = null;  	        
        		String token = (String) conversationState.get("token");
        		String nombrePersona = (String) conversationState.get("nombrePersona");
        		String nroDocumento = (String) conversationState.get("nroDocumento");
        		Integer idPersona = Integer.valueOf(conversationState.get("codPersona").toString());
        		String comentario = "";
        		comentario = String.format("✨ *Estimado(a) %s*, Hemos recibido y atendido su solicitud. Un asesor continuará con su *atención personalizada* 🤝. 📌 Recuerde que su *DNI: %s* será necesario para brindarle una atención óptima.⏳ *Espere un momento, por favor...*", nombrePersona, nroDocumento);
      	        //String inputJson = String.format("{\"email\":\"%s\",\"idPersona\":%s}", userMessage, idPersona);
        		String numberPhone = from.substring(0, 2) + " " + from.substring(2);
        		String payload = String.format(
        			    "{\"mensaje\":\"%s\",\"phone_number\":\"%s\"}",
        			    comentario, numberPhone
        			);
    	        System.out.println("PAYLOAD CHATUP: " + payload);

    	        Map<String, String> headers = new HashMap<String, String>();
    	        headers.put("Content-Type", "application/json; charset=utf-8");
    	        //headers.put("Authorization", "Bearer " + GlobalConstants.ACCESS_TOKEN_CHATUP);
    	        responseData  = HttpRequestUtil.sendRequestChatUp("POST", "https://vps4-back.sjd.pe/api/mensaje/enviar", payload, headers);
    	        System.out.println("RESPONSE CHATUP: " + responseData.toString());
    	        conversationState.put("modulo", "encargado");
    	        if(responseData.getStatusCode() == 200) {
    	        	//sendMessageFinalizar(from, "¡Nos comunicaremos en breve contigo para una atención personalizada, si no llegas a recibir algun mensaje por parte de nosotros, comunicate a este número *958 749 595*!");
    	        	System.out.println("DE HA ENVIADO EL MENSAJE AL ASESOR.");
    	        	return;
    	        }else {
    	        	//sendMessageFinalizar(from, "¡Si no llegas a recibir algun mensaje por parte de nosotros, comunicate a este número *958 749 595*!");
    	        	System.out.println("HUBO UN ERROR AL ENVIAR EL MENSAJE AL ASESOR.");
    	        	return;
    	        }
    	        
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    
    private void proccessHablarEncargado(String from, String userMessage, Map<String, Object> conversationState, Map<String, Object> mapSeguro) {
    	try {			
    		/*RsWs wsInstance = new RsWs();
    		if(wsInstance.isConversationClosed(from)) {
    			String requestBody = "{\r\n"
    					+ "    \"messaging_product\": \"whatsapp\",\r\n"
    					+ "    \"recipient_type\": \"individual\",\r\n"
    					+ "    \"to\": \""+from+"\",\r\n"
    					+ "    \"type\": \"interactive\",\r\n"
    					+ "    \"interactive\": {\r\n"
    					+ "        \"type\": \"button\",\r\n"
    					+ "        \"body\": {\r\n"
    					+ "            \"text\": \"¡La sesión con el encargado ha finalizado! estas de vuelta con Sofia, puedes hacer lo siguiente 👇\"\r\n"
    					+ "        },\r\n"
    					+ "        \"action\": {\r\n"
    					+ "            \"buttons\": [\r\n"
    					+ "                {\r\n"
    					+ "                    \"type\": \"reply\",\r\n"
    					+ "                    \"reply\": {\r\n"
    					+ "                        \"id\": \"retornar\",\r\n"
    					+ "                        \"title\": \"🔁 Atrás\"\r\n"
    					+ "                    }\r\n"
    					+ "                },\r\n"
    					+ "                {\r\n"
    					+ "                    \"type\": \"reply\",\r\n"
    					+ "                    \"reply\": {\r\n"
    					+ "                        \"id\": \"terminar_sesion\",\r\n"
    					+ "                        \"title\": \"🔚 Finalizar\"\r\n"
    					+ "                    }\r\n"
    					+ "                }\r\n"
    					+ "            ]\r\n"
    					+ "        }\r\n"
    					+ "    }\r\n"
    					+ "}";
    			sendHttpRequest(requestBody);
    			return;
    		}
    		wsInstance.handleIncomingWhatsAppMessage(from, userMessage);*/
    		HttpResponse responseData = null;
    		JsonReader jsonReader = null;
    		JsonObject jsonObject = null;  	        
    		String token = (String) conversationState.get("token");
    		String nombrePersona = (String) conversationState.get("nombrePersona");
    		String nroDocumento = (String) conversationState.get("nroDocumento");
    		Integer idPersona = Integer.valueOf(conversationState.get("codPersona").toString());
    		String comentario = "";
    		if(userMessage.contains("Seguro por Soporte")) {
    			String seguro = (String) mapSeguro.get("descripcionSeguro");
    			String iafa = (String) mapSeguro.get("codIafa");
    			 comentario = String.format("✨ *Estimado(a) %s*, Hemos recibido y atendido su solicitud. Un asesor continuará con su *atención personalizada* 🤝. 📌 Recuerde que su *Código de Seguro: %s - %s* y su *DNI: %s* serán necesarios para brindarle una atención óptima.⏳ *Espere un momento, por favor...*", nombrePersona, seguro, iafa , nroDocumento);
    		}else {
   			 	comentario = String.format("✨ *Estimado(a) %s*, Hemos recibido y atendido su solicitud. Un asesor continuará con su *atención personalizada* 🤝. 📌 Recuerde que su *DNI: %s* será necesario para brindarle una atención óptima.⏳ *Espere un momento, por favor...*", nombrePersona, nroDocumento);
    		}
  	        //String inputJson = String.format("{\"email\":\"%s\",\"idPersona\":%s}", userMessage, idPersona);
    		String numberPhone = from.substring(0, 2) + " " + from.substring(2);
    		String payload = String.format(
    			    "{\"mensaje\":\"%s\",\"phone_number\":\"%s\"}",
    			    comentario, numberPhone
    			);
	        System.out.println("PAYLOAD CHATUP: " + payload);

	        Map<String, String> headers = new HashMap<String, String>();
	        headers.put("Content-Type", "application/json; charset=utf-8");
	        //headers.put("Authorization", "Bearer " + GlobalConstants.ACCESS_TOKEN_CHATUP);
	        responseData  = HttpRequestUtil.sendRequestChatUp("POST", "https://vps4-back.sjd.pe/api/mensaje/enviar", payload, headers);
	        System.out.println("RESPONSE CHATUP: " + responseData.toString());
	        if(responseData.getStatusCode() == 200) {
	        	sendMessageFinalizar(from, "¡Nos comunicaremos en breve contigo para una atención personalizada, si no llegas a recibir algun mensaje por parte de nosotros, comunicate a este número *958 749 595*!");
	        	return;
	        }else {
	        	sendMessageFinalizar(from, "¡Si no llegas a recibir algun mensaje por parte de nosotros, comunicate a este número *958 749 595*!");
	        	return;
	        }
	        /*String response = responseData.getResponseBody();
	        if(response.contains("Se ha actualizado el correo exitosamente.")) {
		        conversationState.put("modulo", "elegir_modulo");
		        conversationState.put("correo", userMessage);
		        String nombre = capitalizeFirstLetter(nombrePersona);
		        sendButtonsModulos(from, nombre);
	        }
	        else {
	        	sendMessage(from, "¡No se ha podido registrar el correo electronico, intentalo nuevamente más tarde!");
	        	return;
	        }*/
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private void proccessActualizarCorreo(String from, String userMessage, Map<String, Object> conversationState) {
    	try {
    		
            if (!isValidEmail(userMessage)) {
                sendMessage(from, "❌ El correo ingresado no es válido. Asegúrate de que tenga el formato correcto (ej: ejemplo@correo.com).");
                return;
            }
    		
    		HttpResponse responseData = null;
    		JsonReader jsonReader = null;
    		JsonObject jsonObject = null;  	        
    		String token = (String) conversationState.get("token");
    		String nombrePersona = (String) conversationState.get("nombrePersona");
    		Integer idPersona = Integer.valueOf(conversationState.get("codPersona").toString());
    		
  	        String inputJson = String.format("{\"email\":\"%s\",\"idPersona\":%s}", userMessage, idPersona);
	        Map<String, String> headers = new HashMap<String, String>();
	        headers.put("Content-Type", "application/json; charset=utf-8");
	        headers.put("Authorization", GlobalConstants.API_DEFAULT_TOKEN);
	        responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_ACTUALIZAR_EMAIL, inputJson, headers);
	        String response = responseData.getResponseBody();
	        if(response.contains("Se ha actualizado el correo exitosamente.")) {
		        conversationState.put("modulo", "elegir_modulo");
		        conversationState.put("correo", userMessage);
		        String nombre = capitalizeFirstLetter(nombrePersona);
		        sendButtonsModulos(from, nombre);
	        }
	        else {
	        	sendMessage(from, "¡No se ha podido registrar el correo electronico, intentalo nuevamente más tarde!");
	        	return;
	        }
    	}catch (Exception e) {
    		e.printStackTrace();
			sendMessage(from, "¡No se ha podido registrar el correo electronico, intentalo nuevamente!");
		}
    }
    
    /* ENVIO DE BOTONES, TEXTOS, REGISTROS AL USUARIO*/
    
    public void sendPagoLink(String to, Map<String, Object> mapDatos ) {	
    	
		String response = "";
		HttpResponse responseData = null;
		JsonReader jsonReader = null;
		JsonObject jsonObject = null;  	       
		JsonArray jsonArray = null;
		Map<String, Object> seguro = (Map<String, Object>) mapDatos.get("especialidad_seguro");
		String codIafa = String.valueOf(seguro.get("codIafa").toString());
		Integer codPlan = Integer.valueOf(seguro.get("codAseguradora").toString());
		//Integer idCitaPreReserva = Integer.valueOf(mapDatos.get("idCitaPreReserva").toString());
		//Map<String, Object> mapReserva = (Map<String, Object>) mapDatos.get("preReserva");
		String mapReserva = (String) mapDatos.get("preReserva");
		//Map<String, Object> mapReservaDatos = (Map<String, Object>) mapReserva.get("datosReserva");
		//mapReservaDatos.put("idcita", idCitaPreReserva);
		/*if(codPlan.intValue() != 1) {			
			Map<String, Object> informacionSeguro = (Map<String, Object>) mapDatos.get("informacionObtenida");
			Map<String, Object> informacionPersona = (Map<String, Object>) mapDatos.get("informacionFormateada");
			String nuAutorizacion = "";
			try {
				String inputJson = String.format("{\"codIafa\":\"%s\",\"informacionSeguro\":%s,\"informacionPersonaSeguro\":%s}", codIafa, informacionSeguro, informacionPersona);
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("Content-Type", "application/json; charset=utf-8");
				System.out.println("INPUT JSON AUTORIZACION WSP: " + inputJson);
				responseData  = RsSiteds.sendPostRequest( GlobalConstants.API_BASE_CLINICA_BOT + "/rs/siteds/v1/autorizacionSiteds", inputJson, headers);
				response = responseData.getResponseBody();
				if(response.contains("\"nroAutorizacion\":null")) {
					sendMessageFinalizar(to, "No se pudo obtener el codigo de autorización, comunicate con un administrador.");
					return;
				}
				jsonReader = Json.createReader(new StringReader(response));
				jsonObject = jsonReader.readObject();
				
				nuAutorizacion = jsonObject.getString("nroAutorizacion");
			} catch (Exception e) {
				sendMessageFinalizar(to, "Hubo un error al obtener la autorización del seguro.");
				return;
			}
			
			mapReservaDatos.put("autorizacionsiteds", nuAutorizacion);
			mapReservaDatos.put("objsiteds", mapDatos.get("informacionObtenida"));
			mapReservaDatos.put("objsitedsobservacion", mapDatos.get("informacionFormateada"));
			mapReserva.put("datosReserva", mapReservaDatos);
		}*/
		
		
    	String token = mapDatos.get("token").toString();
    	long unixTimeSeconds = (long) mapDatos.get("inicioConversacion"); 
    	long nowSeconds = Instant.now().getEpochSecond();
    	long transcurridos = nowSeconds - unixTimeSeconds;
    	long restantes = SESSION_TIMEOUT_MINUTES - transcurridos;
    	long minutosRestantes = 0;
    	if (restantes > 0) {
    	    minutosRestantes = restantes / 60;
    	    long segundosRestantes = restantes % 60;
    	    System.out.println("Te quedan " + minutosRestantes + " minutos y " + segundosRestantes + " segundos para realizar tu pago.");
    	} else {
    	    System.out.println("El tiempo para realizar el pago ha expirado.");
    	}
    	//String jsonPago = (String) mapDatos.get("jsonReserva");
    	//String jsonPago = jsonInput(mapReserva);
    	System.out.println("JSON RESERVA PARA PAGO LINK: " + mapReserva);
    	String link =  GlobalConstants.API_PAGO_NIUBIZ + "?paymentInfo="+ URLEncoder.encode(encrypt(mapReserva)) + "&sessionToken=" + token + "&currentUnixTime=" + unixTimeSeconds;
    	System.out.println("LINK: " + link);
    	link = obtenerUrlAcortada(to, link);
    	String requestBody = "{\r\n"
    			+ "    \"messaging_product\": \"whatsapp\",\r\n"
    			+ "    \"recipient_type\": \"individual\",\r\n"
    			+ "    \"to\": \""+ to +"\",\r\n"
    			+ "    \"type\": \"interactive\",\r\n"
    			+ "    \"interactive\": {\r\n"
    			+ "        \"type\": \"button\",\r\n"
    			+ "        \"body\": {\r\n"
    			+ "            \"text\": \"🤗 Se ha generado el siguiente link de pago: \\n\\n 👉 "+ link +"\\n\\nRealiza el pago y serás reedirgido para ver el estado de tu reserva.\\n\\nTienes exactamente *" + minutosRestantes + " minutos* para realizar el pago.\"\r\n"
    			+ "        },\r\n"
    			+ "        \"action\": {\r\n"
    			+ "            \"buttons\": [\r\n"
    			/*+ "                {\r\n"
    			+ "                    \"type\": \"reply\",\r\n"
    			+ "                    \"reply\": {\r\n"
    			+ "                        \"id\": \"realizar_venta\",\r\n"
    			+ "                        \"title\": \"👨‍⚕️ Listar Especialidades\"\r\n"
    			+ "                    }\r\n"
    			+ "                },\r\n"*/
    			+ "                {\r\n"
    			+ "                    \"type\": \"reply\",\r\n"
    			+ "                    \"reply\": {\r\n"
    			+ "                        \"id\": \"terminar_sesion\",\r\n"
    			+ "                        \"title\": \"❌ Finalizar\"\r\n"
    			+ "                    }\r\n"
    			+ "                }\r\n"
    			+ "            ]\r\n"
    			+ "        }\r\n"
    			+ "    }\r\n"
    			+ "}";
    	sendHttpRequest(requestBody);
    }
    
    public void sendRegistroCita(String to, Map<String, Object> mapDatos, ConversationSession sesion) {  	
    	sendMessage(to, "📅✨ ¡Estamos registrando tu cita! Por favor, espera un momento mientras procesamos la información. 🕒😊");
    	String token = mapDatos.get("token").toString();
    	String response = "";
		HttpResponse responseData = null;
		JsonReader jsonReader = null;
		JsonObject jsonObject = null;  	       
		JsonArray jsonArray = null;
		Map<String, Object> seguro = (Map<String, Object>) mapDatos.get("especialidad_seguro");
		Map<String, Object> especialidadHorario = (Map<String, Object>) mapDatos.get("especialidad_horario");
		Map<String, Object> fechaEspecialidad = (Map<String, Object>) mapDatos.get("especialidad_fecha");
		Map<String, Object> especialidad = (Map<String, Object>) mapDatos.get("especialidad");
		//Map<String, Object> informacionSeguro = (Map<String, Object>) mapDatos.get("seguroRegistro");
		//Map<String, Object> informacionPersona = (Map<String, Object>) mapDatos.get("informacionFormateada");
		//Map<String, Object> mapReserva = (Map<String, Object>) mapDatos.get("preReserva");
		//Map<String, Object> mapReservaDatos = (Map<String, Object>) mapReserva.get("datosReserva");
		//Integer idCitaPreReserva = Integer.valueOf(mapDatos.get("idCitaPreReserva").toString());
		String codIafa = String.valueOf(seguro.get("codIafa").toString());
		String nombreCompleto = mapDatos.get("perNombre1").toString() + " " + mapDatos.get("perNombre2").toString();
		String perApePaterno = mapDatos.get("perApePaterno").toString();
		String perApeMaterno = mapDatos.get("perApeMaterno").toString();
		String tipoDocumento = mapDatos.get("tipoDocumento").toString();
		Integer idEspecialidad = Integer.parseInt(especialidad.get("tabId").toString());
		Integer codPersona = Integer.parseInt(mapDatos.get("codPersona").toString());
		String nroDocumento = mapDatos.get("nroDocumento").toString().replace("\"", "");
		String nombreCompletoPersona = (String) mapDatos.get("perApePaterno") + " " + (String) mapDatos.get("perApeMaterno") + ", " +  (String) mapDatos.get("perNombre1") + " " +  (String) mapDatos.get("perNombre2");
		String correo = (String) mapDatos.get("correo");
		String numero = extraerNumeroTelefonico(to);
		
		String fechaEscogida = (String) especialidadHorario.get("hora");
		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		LocalDateTime dateTime = LocalDateTime.parse(fechaEscogida, inputFormatter);
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss");
		String fechaFormateadaElegida = dateTime.format(outputFormatter);
		/*Gson gson = new Gson();
		String jsonPayload = "";
		Integer codProfesional = Integer.parseInt(fechaEspecialidad.get("codProf").toString());
		Integer codAseguradora = Integer.parseInt(seguro.get("codAseguradora").toString());
		//String nuAutorizacion = "";
		try {
	        Map<String, String> headers = new HashMap<String, String>();
	        headers.put("Content-Type", "application/json; charset=utf-8");
	        System.out.println("API CONSULTA: " + GlobalConstants.API_BASE_CLINICA_BOT + "/rs/siteds/v1/obtenerDatosSitetsLuxor");
	        String inputJson = String.format("{\"iafaAseguradora\":\"%s\",\"apPaterno\":\"%s\",\"apMaterno\":\"%s\",\"nombreCompleto\":\"%s\",\"tipoDocumento\":\"%s\",\"nroDocumento\":\"%s\"}", codIafa.replace("40007", "20001"), perApePaterno, perApeMaterno, nombreCompleto, tipoDocumento, nroDocumento);
	        System.out.println("JSON CONSULTA SITEDS: " + inputJson);
	        responseData  = RsSiteds.sendPostRequest(GlobalConstants.API_BASE_CLINICA_BOT + "/rs/siteds/v1/obtenerDatosSitetsLuxor", inputJson, headers);
	        response = responseData.getResponseBody();
	        //SitedsSolAutorizacionDto dto = gson.fromJson(response, SitedsSolAutorizacionDto.class);
	        //jsonPayload = gson.toJson(dto);
		} 
		catch (SocketException e) {
			sendMessage(to, "Se ha excedido el tiempo de espera, reintentando...");
			return;
		}
		
		catch (Exception e) {
			sendMessageFinalizar(to, "Su seguro no cuenta para hacer uso de *consulta ambulatoria* o no lo encontramos habilitado, intente con otra opción.");
			return;
		}


		String payload = String.format(
			    "{\"perId\":%s,\"perNombre\":\"%s\",\"perCorreo\":\"%s\",\"celular\":\"%s\",\"empId\":\"%s\",\"tabEspecialidad\":%s,\"medicoId\":%s,\"codigoSeguro\":%s,\"codigoNegociacion\":%s,\"fechaProgramacion\":\"%s\",\"peticionSited\":%s,\"respuestaSited\":{\"status\":\"ok\"},\"respuestaExCarencia\":{\"inConProc271Detalles\":\"ok\"},\"respuestaPreEx\":{\"inConMed271Detalles\":\"ok\"},\"respuestaProcEspecial\":{\"inConProc271Detalles\":\"ok\"},\"respuestaTiEspera\":{\"inConProc271Detalles\":\"ok\"}}",
			    codPersona, nombreCompletoPersona, correo, numero, codAseguradora, idEspecialidad, codProfesional, codAseguradora, 86, fechaFormateadaElegida, response
			);		*/
		String payload = (String) mapDatos.get("seguroRegistro");
		System.out.println("INPUT JSON RESERVA PAGO COMPLETO: " + payload);
		try {
	        Map<String, String> headers = new HashMap<String, String>();
	        headers.put("Content-Type", "application/json; charset=utf-8");
	        headers.put("Authorization", GlobalConstants.API_DEFAULT_TOKEN_RESERVAS);	        
	        responseData  = RsSiteds.sendPostRequest(GlobalConstants.API_REGISTRAR_CITA_SEGURO, payload, headers);
	        response = responseData.getResponseBody();
	        System.out.println("RESPONSE RESERVA PAGO COMPLETO: " + response);

		} catch (Exception e) {
			sendMessageFinalizar(to, "😓 No se ha podido registrar la cita, intentalo nuevamente o comunicate con alguien de Atención al Cliente.");
			return;
		} 
		
        JsonReader reader = Json.createReader(new StringReader(response));
        JsonArray jsonArrayPre = reader.readArray();
        JsonObject objetoPreReserva = jsonArrayPre.getJsonObject(0);
        if (objetoPreReserva.containsKey("message")) {
            String message = objetoPreReserva.getString("message");
			sendMessageFinalizar(to, message);
			return;
        }
        String idCita = objetoPreReserva.getString("idCita");
        String codPreventa = objetoPreReserva.getString("codPreventa");
        String codVenta = objetoPreReserva.getString("codVenta");
        
        if(codVenta.trim().equals("")) {
	        sendMessage(to, "⚠️ Lo sentimos, no pudimos registrar tu cita. 😕\\r\\n"
	                + "Por favor, intenta nuevamente más tarde o contáctanos para obtener ayuda. 📞\\r\\n"
	                + "¡Estamos aquí para asistirte! 😊");
		    return;
        }
        else {
        	sendMessage(to, "📝 ¡Cita registrada con éxito! 🎉\\r\\n"
	                + "Te esperamos en la fecha y hora acordadas. ⏰👨‍⚕️\\r\\n"
	                + "Si necesitas realizar algún cambio, no dudes en contactarnos. 📞");

	        Map<String, Object> especialidadFecha = (Map<String, Object>) mapDatos.get("especialidad_fecha");

	        double precioIGV = 0;
	        String nombrePersona = mapDatos.get("perNombre1").toString() + " "
	                + obtenerPrimeraLetra(mapDatos.get("perNombre2").toString()) + " "
	                + mapDatos.get("perApePaterno").toString() + " "
	                + obtenerPrimeraLetra(mapDatos.get("perApeMaterno").toString());
	        String nombreProfesional = especialidadFecha.get("profNombre").toString().replace("\"", "");

	        String especialidadHorarioElegido = especialidadHorario.get("hora").toString().replace("\"", "");
	        DateTimeFormatter formatoEntrada = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	        DateTimeFormatter formatoSalida = DateTimeFormatter.ofPattern("hh:mm a");
	        LocalDateTime fechaHora = LocalDateTime.parse(especialidadHorarioElegido, formatoEntrada);
	        String horaFormateadaElegida = fechaHora.format(formatoSalida);

	        //String turnoPosible = especialidadFecha.get("turno").toString().replace("\"", "");
	        //String especialidadFechaElegido = especialidadHorario.get("fechaDisponible").toString().replace("Z", "").replace("\"", "");
	        
    		String especialidadFechaElegido = especialidadHorario.get("fechaDisponible").toString().replace("Z", "");
    		especialidadFechaElegido = especialidadFechaElegido.replace("\"", "");
	        String especialidadElegida = especialidad.get("tabNombre").toString().replace("\"", "");
	        String seguroElegido = seguro.get("descripcionSeguro").toString().replace("\"", "");

	        String informacionCita = "😄 ¡Genial, *" + capitalizeFirstLetter(nombrePersona) + "*, se ha registrado tu cita correctamente!\\r\\n"
	                + "\\r\\n"
	                + "🩺 Especialidad: *" + capitalizeFirstLetter(especialidadElegida) + "*\\r\\n"
	                + "📅 Fecha: *" + formaterDate(especialidadFechaElegido) + "*\\r\\n"
	                + "🕒 Hora: *" + horaFormateadaElegida + "*\\r\\n"
	                + "👨‍⚕️ Médico: *" + capitalizeFirstLetter(nombreProfesional) + "*\\r\\n"
	                + "💬 Tipo de Consulta: *Presencial*\\r\\n"
	                + "🆔 Documento: *" + nroDocumento + "*\\r\\n"
	                //+ "🔢 Turno: *" + turnoPosible + "*\\r\\n"
	                + "🏥 Seguro: *" + seguroElegido + "*\\r\\n"
	                + "💲 Precio: *S/. " + precioIGV + "*\\r\\n\\r\\n";

	        sendMessage(to, informacionCita);
	        String mensajeFinal = "Señor(a), le informamos que su boleta de venta será enviada a su correo electrónico dentro de las dos horas posteriores a la realización de su trámite. Le recomendamos revisar también la bandeja de spam o correo no deseado.\\n"
	        		+ "En caso no la reciba dentro de ese plazo, puede comunicarse con nosotros para brindarle el apoyo correspondiente. Muchas gracias.";
	        sendMessage(to, mensajeFinal);
        }
		/*try {
		    if (response == null || response.trim().isEmpty()) {
		        throw new RuntimeException("La respuesta está vacía");
		    }

		    String codigo = response.trim();

		    //if (idCita > 0) 
		    
		    if (codigo.matches("\\d{10,}")) {
		        // ✅ Flujo exitoso
		        sendMessage(to, "📝 ¡Cita registrada con éxito! 🎉\\r\\n"
		                + "Te esperamos en la fecha y hora acordadas. ⏰👨‍⚕️\\r\\n"
		                + "Si necesitas realizar algún cambio, no dudes en contactarnos. 📞");

		        Map<String, Object> especialidadFecha = (Map<String, Object>) mapDatos.get("especialidad_fecha");

		        double precioIGV = 0;
		        String nombrePersona = mapDatos.get("perNombre1").toString() + " "
		                + obtenerPrimeraLetra(mapDatos.get("perNombre2").toString()) + " "
		                + mapDatos.get("perApePaterno").toString() + " "
		                + obtenerPrimeraLetra(mapDatos.get("perApeMaterno").toString());
		        String nombreProfesional = especialidadFecha.get("profNombre").toString().replace("\"", "");

		        String especialidadHorarioElegido = especialidadHorario.get("hora").toString().replace("\"", "");
		        DateTimeFormatter formatoEntrada = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		        DateTimeFormatter formatoSalida = DateTimeFormatter.ofPattern("hh:mm a");
		        LocalDateTime fechaHora = LocalDateTime.parse(especialidadHorarioElegido, formatoEntrada);
		        String horaFormateadaElegida = fechaHora.format(formatoSalida);

		        //String turnoPosible = especialidadFecha.get("turno").toString().replace("\"", "");
		        String especialidadFechaElegido = especialidadFecha.get("fechaDisponible").toString().replace("Z", "").replace("\"", "");
		        String especialidadElegida = especialidad.get("tabNombre").toString().replace("\"", "");
		        String seguroElegido = seguro.get("descripcionSeguro").toString().replace("\"", "");

		        String informacionCita = "😄 ¡Genial, *" + capitalizeFirstLetter(nombrePersona) + "*, se ha registrado tu cita correctamente!\\r\\n"
		                + "\\r\\n"
		                + "🩺 Especialidad: *" + capitalizeFirstLetter(especialidadElegida) + "*\\r\\n"
		                + "📅 Fecha: *" + formaterDate(especialidadFechaElegido) + "*\\r\\n"
		                + "🕒 Hora: *" + horaFormateadaElegida + "*\\r\\n"
		                + "👨‍⚕️ Médico: *" + capitalizeFirstLetter(nombreProfesional) + "*\\r\\n"
		                + "💬 Tipo de Consulta: *Presencial*\\r\\n"
		                + "🆔 Documento: *" + nroDocumento + "*\\r\\n"
		                //+ "🔢 Turno: *" + turnoPosible + "*\\r\\n"
		                + "🏥 Seguro: *" + seguroElegido + "*\\r\\n"
		                + "💲 Precio: *S/. " + precioIGV + "*\\r\\n\\r\\n";

		        sendMessage(to, informacionCita);
		    } else {
		        // ⚠️ Por si acaso, si el número es <= 0
		        sendMessage(to, "⚠️ Lo sentimos, no pudimos registrar tu cita. 😕\\r\\n"
		                + "Por favor, intenta nuevamente más tarde o contáctanos para obtener ayuda. 📞\\r\\n"
		                + "¡Estamos aquí para asistirte! 😊");
		    }
		} catch (NumberFormatException ex) {
		    // ❌ El response no es un número, lo tratamos como JSON con mensaje de error
		    JsonObject json = Json.createReader(new StringReader(response)).readObject();
		    String mensajeError = json.containsKey("message") ? json.getString("message") : "No se pudo registrar tu cita.";
		    
		    sendMessage(to, "⚠️ Lo sentimos, no pudimos registrar tu cita. 😕\\r\\n"
		            + mensajeError + "\\r\\n"
		            + "Por favor, intenta nuevamente más tarde o contáctanos para obtener ayuda. 📞\\r\\n"
		            + "¡Estamos aquí para asistirte! 😊");
		}*/
		/*
	  	 if(response.contains("success\":true")) {
	  		 sendMessage(to, "📝 ¡Cita registrada con éxito! 🎉\\r\\n"
	  		 		+ "Te esperamos en la fecha y hora acordadas. ⏰👨‍⚕️\\r\\n"
	  		 		+ "Si necesitas realizar algún cambio, no dudes en contactarnos. 📞");
	  		 
	    		//Map<String, Object> especialidad = (Map<String, Object>) mapDatos.get("especialidad");
	    		//Map<String, Object> especialidadHorario = (Map<String, Object>) mapDatos.get("especialidad_horario");
	    		Map<String, Object> especialidadFecha = (Map<String, Object>) mapDatos.get("especialidad_fecha");
	    		
	    		double precioIGV = 0;
	    		String nombrePersona = mapDatos.get("perNombre1").toString() + " "
	    				+ obtenerPrimeraLetra(mapDatos.get("perNombre2").toString()) + " "
	    				+ mapDatos.get("perApePaterno").toString() + " " + obtenerPrimeraLetra(mapDatos.get("perApeMaterno").toString());
	    		String nombreProfesional = especialidadFecha.get("profNombre").toString().replace("\"", "");
	    		
	    		String especialidadHorarioElegido = especialidadHorario.get("hora").toString().replace("\"", "");
	    		DateTimeFormatter formatoEntrada = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	    		DateTimeFormatter formatoSalida = DateTimeFormatter.ofPattern("hh:mm a"); // con AM/PM
	    		LocalDateTime fechaHora = LocalDateTime.parse(especialidadHorarioElegido, formatoEntrada);
	    		String horaFormateadaElegida = fechaHora.format(formatoSalida);
	    		
	    		String turnoPosible = especialidadFecha.get("turno").toString().replace("\"", "");;
	    		String especialidadFechaElegido = especialidadFecha.get("fechaDisponible").toString().replace("Z", "");
	    		especialidadFechaElegido = especialidadFechaElegido.replace("\"", "");
	    		String especialidadElegida = especialidad.get("tabNombre").toString().replace("\"", "");
	    		String seguroElegido = seguro.get("descripcionSeguro").toString().replace("\"", "");
	  		 
	  		 
	    		String informacionCita = "😄 ¡Genial, *"+ capitalizeFirstLetter(nombrePersona) +"*, se ha registrado tu cita correctamente!\\r\\n"
	    				+ "\\r\\n"
	    				+ "🩺 Especialidad: *"+ capitalizeFirstLetter(especialidadElegida) +"*\\r\\n"
	    				+ "📅 Fecha: *"+ formaterDate(especialidadFechaElegido) +"*\\r\\n"
	    				+ "🕒 Hora: *"+ horaFormateadaElegida +"*\\r\\n"
	    				+ "👨‍⚕️ Médico: *"+ capitalizeFirstLetter(nombreProfesional) +"*\\r\\n"
	    				+ "💬 Tipo de Consulta: *Presencial*\\r\\n"
	    				+ "🆔 Documento: *"+ nroDocumento +"*\\r\\n"
	    				+ "🔢 Turno: *"+ turnoPosible +"*\\r\\n"
	    				+ "🏥 Seguro: *"+ seguroElegido +"*\\r\\n"
	    				+ "💲 Precio: *S/. "+ precioIGV +"*\\r\\n\\r\\n";
	    		sendMessage(to, informacionCita);

	  	 }else {
	  		 sendMessage(to, "⚠️ Lo sentimos, no pudimos registrar tu cita. 😕\\r\\n"
	  		 		+ "Por favor, intenta nuevamente más tarde o contáctanos para obtener ayuda. 📞\\r\\n"
	  		 		+ "¡Estamos aquí para asistirte! 😊");		 
	  	 }	 */
	  	userConversationState.remove(to);
	  	sesion.endConversation();
    }

    public void sendTerminoDeSesion(String to, ConversationSession sesion) {
    	sendMessage(to, "💬 ¡Gracias por usar nuestro servicio! 🙌\\r\\n"
    			+ "Esperamos haber sido de ayuda. Si necesitas algo más, no dudes en escribirnos. ¡Te esperamos pronto! 😊\\r\\n"
    			+ "🌟 ¡Que tengas un excelente día! 🌟");
    	userConversationState.remove(to);
    	sesion.endConversation();
    }
    
    public void sendDeclinarTerminos(String to, ConversationSession sesion) {
    	sendMessage(to, "⚠️ ¡Ups! Parece que aún no has aceptado nuestros términos y condiciones. 📄\\r\\n"
    			+ "\\r\\n"
    			+ "✅ Por favor, revisa y acepta los términos para continuar. ¡Es un pequeño paso para seguir adelante! 🚀");
    	userConversationState.remove(to);
    	sesion.endConversation();
    }
    
    public void sendModuloProduccion(String to, ConversationSession sesion) {
    	sendMessage(to, "🛠️ Estamos trabajando duro para traerlo pronto. ¡Gracias por tu paciencia!");
    	userConversationState.remove(to);
    	sesion.endConversation();
    }
    
    public void sendListMeses(String to,  Map<String, Object> mapDatos) {
    	try {
    		String meses = "";
    		List<Map<String, Object>> lstMeses = new ArrayList<>();
    		for (int i = 1; i <= 12; i++) {
    		    Map<String, Object> mes = new HashMap<>();
    		    Map<String, Object> mesOption = new HashMap<>();
    		    mes.put("numero", String.format("%02d", i));
    		    mes.put("nombre", getNombreMes(i));
    		    meses += "*" + i + ".* " + getNombreMes(i) + "\\n";
    		    mesOption.put(String.valueOf(i), mes);
    		    lstMeses.add(mesOption);
    		}
    		mapDatos.put("lstMeses", lstMeses);
    		String requestBody = "{\r\n"
    				+ "    \"messaging_product\": \"whatsapp\",\r\n"
    				+ "    \"recipient_type\": \"individual\",\r\n"
    				+ "    \"to\": \""+ to +"\",\r\n"
    				+ "    \"type\": \"interactive\",\r\n"
    				+ "    \"interactive\": {\r\n"
    				+ "        \"type\": \"button\",\r\n"
    				+ "        \"body\": {\r\n"
    				+ "            \"text\": \"A continuación, te mostramos la *lista de meses*.\\n\\n"+ meses +"\\nPor favor, elige el mes del cual deseas obtener las citas registradas, escribe *retornar* para volver a digitar el año. 🗓️\"\r\n"
    				+ "        },\r\n"
    				+ "        \"action\": {\r\n"
    				+ "            \"buttons\": [\r\n"
    				+ "                {\r\n"
    				+ "                    \"type\": \"reply\",\r\n"
    				+ "                    \"reply\": {\r\n"
    				+ "                        \"id\": \"retornar\",\r\n"
    				+ "                        \"title\": \"🔁 Atrás\"\r\n"
    				+ "                    }\r\n"
    				+ "                },\r\n"
    				+ "                {\r\n"
    				+ "                    \"type\": \"reply\",\r\n"
    				+ "                    \"reply\": {\r\n"
    				+ "                        \"id\": \"terminar_sesion\",\r\n"
    				+ "                        \"title\": \"🔚 Finalizar\"\r\n"
    				+ "                    }\r\n"
    				+ "                }\r\n"
    				+ "            ]\r\n"
    				+ "        }\r\n"
    				+ "    }\r\n"
    				+ "}";
    		sendHttpRequest(requestBody);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    

    
    public void sendInformacionCita(String to, String token, Map<String, Object> mapDatos) {
		try {		
	        if (mapDatos.containsKey("consultando_siteds") && (Boolean) mapDatos.get("consultando_siteds")) {
	            return;
	        }
	        if(mapDatos.containsKey("siteds_obtenido") && (Boolean) mapDatos.get("siteds_obtenido")) {
	        	return;
	        }
	        mapDatos.put("consultando_siteds", true);
    		String response = "";
    		HttpResponse responseData = null;
    		JsonReader jsonReader = null;
    		JsonObject jsonObject = null;  	       
    		JsonArray jsonArray = null;
    		Map<String, Object> seguro = (Map<String, Object>) mapDatos.get("especialidad_seguro");
    		Map<String, Object> especialidad = (Map<String, Object>) mapDatos.get("especialidad");
    		Map<String, Object> especialidadHorario = (Map<String, Object>) mapDatos.get("especialidad_horario");
    		Map<String, Object> especialidadFecha = (Map<String, Object>) mapDatos.get("especialidad_fecha");
    		double precioIGV = 0;
    		Integer consId = Integer.parseInt(especialidadHorario.get("consId").toString());
    		Integer turnoId = Integer.parseInt(especialidadHorario.get("turnoId").toString());
    		Integer codPlan = Integer.valueOf(seguro.get("codAseguradora").toString());
    		//Integer codEsp = Integer.valueOf(especialidad.get("codEsp").toString());
    		String nombrePersona = mapDatos.get("perNombre1").toString() + " "
    				+ obtenerPrimeraLetra(mapDatos.get("perNombre2").toString()) + " "
    				+ mapDatos.get("perApePaterno").toString() + " " + obtenerPrimeraLetra(mapDatos.get("perApeMaterno").toString());
    		String nombreProfesional = especialidadFecha.get("profNombre").toString().replace("\"", "");
    		String nroDocumento = mapDatos.get("nroDocumento").toString().replace("\"", "");
    		
    		String especialidadHorarioElegido = especialidadHorario.get("hora").toString().replace("\"", "");
    		DateTimeFormatter formatoEntrada = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    		DateTimeFormatter formatoSalida = DateTimeFormatter.ofPattern("hh:mm a"); // con AM/PM
    		LocalDateTime fechaHora = LocalDateTime.parse(especialidadHorarioElegido, formatoEntrada);
    		String horaFormateadaElegida = fechaHora.format(formatoSalida);
    		
    		//String turnoPosible = especialidadFecha.get("turno").toString().replace("\"", "");;
    		String especialidadFechaElegido = especialidadHorario.get("fechaDisponible").toString().replace("Z", "");
    		especialidadFechaElegido = especialidadFechaElegido.replace("\"", "");
    		String especialidadElegida = especialidad.get("tabNombre").toString().replace("\"", "");
    		Integer especialidadId = Integer.valueOf(especialidad.get("tabId").toString().replace("\"", ""));
    		String seguroElegido = seguro.get("descripcionSeguro").toString().replace("\"", "");
    		//String codHorario = String.valueOf(especialidadFecha.get("codHora").toString());
    		String nombreCompleto = mapDatos.get("perNombre1").toString() + " " + mapDatos.get("perNombre2").toString();
    		String perApePaterno = mapDatos.get("perApePaterno").toString();
    		String perApeMaterno = mapDatos.get("perApeMaterno").toString();
    		String tipoDocumento = mapDatos.get("tipoDocumento").toString();
    		//Integer codPersona = Integer.valueOf(mapDatos.get("codPersona").toString());
    		
    		//Integer codAseguradora = Integer.valueOf(seguro.get("idEmprestadoraSeguro").toString());
    		//String fechaDisponible = String.valueOf(especialidadFecha.get("fechaDisponible").toString());
        	//String fechaFormateada = fechaDisponible + "T" + especialidadHorarioElegido.replace("\"", "") + ":00";
        	//Double duracion = Double.valueOf(especialidad.get("tiempoPromedioAtencion").toString());
        	//Integer codPaciente = Integer.valueOf(mapDatos.get("codPersona").toString());
        	//Integer codProfesional = Integer.valueOf(especialidadFecha.get("codProf").toString());
        	//Integer codEspecialidad = Integer.valueOf(especialidad.get("idEspecialidad").toString());
        	//Integer tipoPaciente = Integer.valueOf(seguro.get("tipoPaciente").toString());
    		
    		
    		//Integer idEspecialidad = Integer.parseInt(especialidad.get("tabId").toString());
    		Integer codPersona = Integer.parseInt(mapDatos.get("codPersona").toString());
    		String nombreCompletoPersona = (String) mapDatos.get("perApePaterno") + " " + (String) mapDatos.get("perApeMaterno") + ", " +  (String) mapDatos.get("perNombre1") + " " +  (String) mapDatos.get("perNombre2");
    		String correo = (String) mapDatos.get("correo");
    		String numero = extraerNumeroTelefonico(to);
    		
    		String fechaEscogida = (String) especialidadHorario.get("hora");
    		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    		LocalDateTime dateTime = LocalDateTime.parse(fechaEscogida, inputFormatter);
    		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm:ss");
    		String fechaFormateadaElegida = dateTime.format(outputFormatter);
    		Gson gson = new Gson();
    		String jsonPayload = "";
    		Integer codProfesional = Integer.parseInt(especialidadFecha.get("codProf").toString());
    		Integer codAseguradora = Integer.parseInt(seguro.get("codAseguradora").toString());
    		
    		
        	String codIafa = String.valueOf(seguro.get("codIafa").toString());
        	//String correo = (String) mapDatos.get("correo");
        	
    		try {
    	        Map<String, String> headers = new HashMap<String, String>();
    	        headers.put("Content-Type", "application/json; charset=utf-8");
    	        headers.put("Authorization", GlobalConstants.API_DEFAULT_TOKEN_RESERVAS);
    	        responseData  = HttpRequestUtil.sendRequest("GET", GlobalConstants.API_CONSULTA_PRECIO + "?tabNegociacion=" + 1 + "&tabEspecialidad=" + especialidadId, null, headers);	
    	        response = responseData.getResponseBody();
    			
    		} catch (Exception e) {
    			sendMessageFinalizar(to, "😓 No se ha podido obtener el precio de la consulta para esta especialidad, intentalo nuevamente.");
    			return;
    		}  		
    	
    		mapDatos.put("informacionObtenida", "");
    		//mapDatos.put("informacionFormateada", "");
    		
    		jsonReader = Json.createReader(new StringReader(response));
    		jsonArray = jsonReader.readArray();
    		JsonObject primerElemento = jsonArray.getJsonObject(0);
    		String precioString = primerElemento.getString("precioNegociadoConIgv");
    		double precio = Double.parseDouble(precioString);
    		precio = Math.round(precio * 100.0) / 100.0;
    		precioIGV = precio;

        	Map<String, Object> mapPago = new HashMap<String, Object>();
        	Map<String, Object> mapDatosReserva = new HashMap<String, Object>();
        	mapDatosReserva.put("perId", codPersona);
        	mapDatosReserva.put("perNombre", nombreCompletoPersona);
        	mapDatosReserva.put("perCorreo", correo);
        	mapDatosReserva.put("celular", numero);
        	mapDatosReserva.put("empId", codAseguradora);
        	mapDatosReserva.put("turnoId", turnoId);
        	mapDatosReserva.put("tabEspecialidad", especialidadId);
        	mapDatosReserva.put("medicoId", codProfesional);
        	mapDatosReserva.put("consId", consId);
        	mapDatosReserva.put("codModalidad", 3);
        	mapDatosReserva.put("codigoSeguro", codAseguradora);
        	mapDatosReserva.put("codigoNegociacion", 1);
        	mapDatosReserva.put("fechaProgramacion",fechaFormateadaElegida);
        	
        	Map<String, Object> respuestaSited = new HashMap<>();
        	respuestaSited.put("status", "ok");

        	Map<String, Object> respuestaExCarencia = new HashMap<>();
        	respuestaExCarencia.put("inConProc271Detalles", "ok");

        	Map<String, Object> respuestaPreEx = new HashMap<>();
        	respuestaPreEx.put("inConMed271Detalles", "ok");

        	Map<String, Object> respuestaProcEspecial = new HashMap<>();
        	respuestaProcEspecial.put("inConProc271Detalles", "ok");

        	Map<String, Object> respuestaTiEspera = new HashMap<>();
        	respuestaTiEspera.put("inConProc271Detalles", "ok");

        	mapDatosReserva.put("respuestaSited", respuestaSited);
        	mapDatosReserva.put("respuestaExCarencia", respuestaExCarencia);
        	mapDatosReserva.put("respuestaPreEx", respuestaPreEx);
        	mapDatosReserva.put("respuestaProcEspecial", respuestaProcEspecial);
        	mapDatosReserva.put("respuestaTiEspera", respuestaTiEspera);

    		if(codPlan.intValue() != 1) {
    			sendMessage(to, "📅✨ Por favor, espera un momento mientras obtenemos la información. 🕒😊");
        		try {
        	        Map<String, String> headers = new HashMap<String, String>();
        	        headers.put("Content-Type", "application/json; charset=utf-8");
        	        System.out.println("API CONSULTA: " + GlobalConstants.API_BASE_CLINICA_BOT + "/rs/siteds/v1/obtenerDatosSitetsLuxor");
        	        String inputJson = String.format("{\"iafaAseguradora\":\"%s\",\"apPaterno\":\"%s\",\"apMaterno\":\"%s\",\"nombreCompleto\":\"%s\",\"tipoDocumento\":\"%s\",\"nroDocumento\":\"%s\",\"especialidad\":\"%s\"}", codIafa.replace("40007", "20001"), perApePaterno, perApeMaterno, nombreCompleto, tipoDocumento, nroDocumento, especialidadElegida);
        	        System.out.println("JSON CONSULTA SITEDS: " + inputJson);
        	        responseData  = RsSiteds.sendPostRequest(GlobalConstants.API_BASE_CLINICA_BOT + "/rs/siteds/v1/obtenerDatosSitetsLuxor", inputJson, headers);
        	        response = responseData.getResponseBody();
        			
        		} 
        
        		catch (SocketException e) {
        			sendMessage(to, "Se ha excedido el tiempo de espera, reintentando...");
        			return;
        		}
        		
        		catch (Exception e) {
        			e.printStackTrace();
        			sendMessageFinalizar(to, "No logramos obtener la información de su seguro, intentelo nuevamente.");
        			return;
        		}
        		
        		System.out.println("RESPONSE SEGURO INFORMACIÓN: " + response);
        		if(response.contains("Error al consultar servicio ConNom.")) {
        			sendMessage(to, "Su seguro no cuenta para hacer uso de *consulta ambulatoria* o no lo encontramos habilitado, intente con otra opción.");
        			return;
        		}
        		
        		if(response.contains("El paciente no cuenta con afiliación al seguro.")) {
        			sendMessage(to, "El paciente no cuenta con afiliación al seguro, intente con otra opción.");
        			return;
        		}
        		
        		if(response.contains("Error al consultar servicio ConCod.")) {
        			sendMessage(to, "Ocurrio un error al intentar consultar las coberturas, intentelo de nuevo más tarde.");
        			return;
        		}
        		
        		if(response.contains("No hay seguros activos para el paciente.")) {
        			sendMessageFinalizar(to, "No hay seguros activos para el paciente, elige otra opción o finaliza.");
        			return;
        		}
        		mapDatos.put("siteds_obtenido", true);		
        		SitedsSolAutorizacionDto dto = gson.fromJson(response, SitedsSolAutorizacionDto.class);
        		com.google.gson.JsonObject json = gson.toJsonTree(dto).getAsJsonObject();
        		System.out.println("JSON SITEDS SOL AUTORIZACION: " + response);
        		json.remove("nuAutorizacion");
        		json.remove("tiCaContratante");
        		json.remove("coInRestriccion");
        		json.remove("detalleRestric");
        		String jsonFinal = gson.toJson(json);
        		System.out.println("JSON FINAL PARA PEDIR AUTORIZACION: " + jsonFinal);
        		
        		try {
        	        Map<String, String> headers = new HashMap<String, String>();
        	        headers.put("Content-Type", "application/json; charset=utf-8");
        	        headers.put("Authorization", GlobalConstants.API_DEFAULT_TOKEN_RESERVAS);
        	        System.out.println("API CONSULTA: " + GlobalConstants.API_OBTENER_IAFAS + "?empresaCod=" + dto.getCoReContratante());
        	        responseData  = HttpRequestUtil.sendRequest("GET", GlobalConstants.API_OBTENER_IAFAS + "?empresaCod=" + dto.getCoReContratante(), null, headers);
        	        response = responseData.getResponseBody();
        	        System.out.println("RESPONSE IAFAS: " + response);
        	        JsonReader reader = Json.createReader(new StringReader(response));
        	        JsonArray jsonArrayPre = reader.readArray();
        	        int negociacion = 0;
            	    for (Iterator iterator = jsonArrayPre.iterator(); iterator.hasNext();) {
            	    	JsonValue jsonValue = (JsonValue) iterator.next();
            	    	JsonObject jsonObjectAse = jsonValue.asJsonObject();
            	    	String negociacionStr = jsonObjectAse.get("negociacion").toString().replace("\"", "");
            	    	negociacion = Integer.parseInt(negociacionStr);
            	    	if(jsonObjectAse.getString("codigoIafa").equals(codIafa)) {
            	    		mapDatosReserva.put("codigoNegociacion", negociacion);
            	    	}
            	    }
            	    if(negociacion == 0) {
            			sendMessageSoporte(to, "Para poder hacer uso del seguro comunicate con el área de Atención al Cliente.");
            			return;
            	    }
        		} 

        		catch (Exception e) {
        			e.printStackTrace();
        			sendMessageFinalizar(to, "No se ha podido consultar la negociación del seguro.");
        			return;
        		}
        		
        		
        		precioIGV = Double.parseDouble(dto.getCoPagoFijo());
        		
    	  		try {
    	  	        System.out.println("JSON AUTORIZACION: " + jsonFinal);
    	  			Map<String, String> headers = new HashMap<String, String>();
    	  	        headers.put("Content-Type", "application/json; charset=utf-8");
    	  	        headers.put("Authorization", GlobalConstants.SITEDS_TOKEN);
    	  	        //responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.SITEDS_AUTORIZACION, inputJson, headers);
    	  	      responseData = RsSiteds.sendPostRequest(GlobalConstants.SITEDS_AUTORIZACION, jsonFinal, headers);
    	  	        response = responseData.getResponseBody();
    	  	        System.out.println("RESPONSE AUTORIZACION: " + response);
    	  			
    	  		} catch (Exception e) {
    	  			//throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar el servicio SoliAutorizacion.");
    	  			sendMessageFinalizar(to, "Error al consultar servicio de autorización.");
    	  			return;
    	  		}
    	  	
    			if(responseData.getStatusCode() == 500) {
    				//throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio SoliAutorizacion.");
    	  			sendMessageFinalizar(to, "Error al consultar servicio.");
    	  			return;
    			}
    			
    			
    			jsonReader = Json.createReader(new StringReader(response));
    		  	jsonObject = jsonReader.readObject();
    		  	String tipoAutorizacion = "01";
    		  	String nroAutorizacion = jsonObject.getString("nuAutorizacion");
        		
    		  	dto.setNuAutorizacion(nroAutorizacion);
    		  	
        		mapDatosReserva.put("peticionSited", dto);	    		
    		}
    		mapDatos.put("consultando_siteds", false);
    		//String jsonPreReserva = jsonInput(mapDatosReserva);
    		
    		/*try {
    			Map<String, String> headers = new HashMap<String, String>();
    			headers.put("Content-Type", "application/json; charset=utf-8");
    			headers.put("Authorization", "Bearer " + token);
    			responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_PRE_RESERVA_CITA, jsonPreReserva, headers);
    			response = responseData.getResponseBody();
    		} 
    		catch(Exception ex) {
    			ex.printStackTrace();
    			sendMessage(to, "Húbo un error al pre reservar la cita, intentalo nuevamente o comunicate con un encargado.");
    			return;
    		}*/
    		
    		//System.out.println("JSON PRE RESERVA: " + jsonPreReserva);
    		/*System.out.println("RESPONSE PRE RESERVA: " + response);
    		
    		jsonReader = Json.createReader(new StringReader(response));
    		jsonObject = jsonReader.readObject();
    		
    		
    		if(response.contains("Lo sentimos el usuario ya tiene una cita para este dia")) {
    			sendMessage(to, "Lo sentimos el usuario ya tiene una cita para esta hora.");
    			return;
    		}
    		
    		Integer idCita = null;
    		
    		if (jsonObject.containsKey("idcita")) {
    			if (!jsonObject.isNull("idcita")) {
    				idCita = jsonObject.getInt("idcita");
    				if (idCita == null || idCita <= 0) {
    					sendMessage(to, "No se ha podido hacer un pre reserva de la cita, comunicate con un encargado.");
    					return;
    				}
    			} else {
    				sendMessage(to, "No se ha podido hacer un pre reserva de la cita, comunicate con un encargado.");
    				return;
    			}
    		} else {
    			sendMessage(to, "No se ha podido hacer un pre reserva de la cita, comunicate con un encargado.");
    			return;
    		}    	  
    		mapDatos.put("idCitaPreReserva", idCita);*/
    		/*if(precioIGV > 0) {
    		}*/
    	
    		String jsonSeguroRegistro = new Gson().toJson(mapDatosReserva);
    		
    		if(precioIGV > 0) {
    			System.out.println("INPUT JSON PRE RESERVA: " + jsonSeguroRegistro);
    			try {
    		        Map<String, String> headers = new HashMap<String, String>();
    		        headers.put("Content-Type", "application/json; charset=utf-8");
    		        headers.put("Authorization", GlobalConstants.API_DEFAULT_TOKEN_RESERVAS);	        
    		        responseData  = RsSiteds.sendPostRequest(GlobalConstants.API_REGISTRAR_CITA_SEGURO, jsonSeguroRegistro, headers);
    		        response = responseData.getResponseBody();
    		        System.out.println("RESPONSE RESERVA PRE RESERVA: " + response);

    			} catch (Exception e) {
    				sendMessageFinalizar(to, "😓 No se ha podido registrar la cita, intentalo nuevamente o comunicate con alguien de Atención al Cliente.");
    				return;
    			} 
    			
    	        JsonReader reader = Json.createReader(new StringReader(response));
    	        JsonArray jsonArrayPre = reader.readArray();
    	        JsonObject objetoPreReserva = jsonArrayPre.getJsonObject(0);
    	        if (objetoPreReserva.containsKey("message")) {
    	            String message = objetoPreReserva.getString("message");
    				sendMessageFinalizar(to, message);
    				return;
    	        }
    	        String idCita = objetoPreReserva.getString("idCita");
    	        String codPreventa = objetoPreReserva.getString("codPreventa");
    	        mapDatosReserva.put("codPreventa", codPreventa);
    	        mapDatos.put("idCitaPreReserva", idCita);
    		}
    		
    		mapDatos.put("tarifaConsulta", precioIGV);
    		String precioReserva = String.valueOf(precioIGV);
        	mapPago.put("amount", precioReserva);
        	mapPago.put("currency", "PEN");
        	mapPago.put("datosReserva", mapDatosReserva);
        	mapDatos.put("seguroRegistro",jsonSeguroRegistro);
        	String jsonPreReserva = new Gson().toJson(mapPago);
        	mapDatos.put("preReserva", jsonPreReserva);
        	System.out.println("INFORMACION CITA JSON PARA ENVIAR: " + jsonPreReserva);
    		
    		
    		String informacionCita = "😄 ¡Genial, *"+ capitalizeFirstLetter(nombrePersona) +"*, estas a un paso de registrar tu cita!\\r\\n"
    				+ "\\r\\n"
    				+ "🩺 Especialidad: *"+ capitalizeFirstLetter(especialidadElegida) +"*\\r\\n"
    				+ "📅 Fecha: *"+ formaterDate(especialidadFechaElegido) +"*\\r\\n"
    				+ "🕒 Hora: *"+ horaFormateadaElegida +"*\\r\\n"
    				+ "👨‍⚕️ Médico: *"+ capitalizeFirstLetter(nombreProfesional) +"*\\r\\n"
    				+ "💬 Tipo de Consulta: *Presencial*\\r\\n"
    				+ "🆔 Documento: *"+ nroDocumento +"*\\r\\n"
    				+ "🏥 Seguro: *"+ seguroElegido +"*\\r\\n"
    				+ "💲 Precio: *S/. "+ precioIGV +"*\\r\\n\\r\\n";
    		String proceso = "";
    	   		
    		if(precioIGV > 0) {
    			informacionCita += "💳 Recuerda: La cita será confirmada una vez que realices el pago.";
    			proceso = "registrar_cita";
    		}else {
    			proceso = "registrar_cita_seguro";
    		}
    		String requestBody = "{\r\n"
    				+ "    \"messaging_product\": \"whatsapp\",\r\n"
    				+ "    \"recipient_type\": \"individual\",\r\n"
    				+ "    \"to\": \""+ to +"\",\r\n"
    				+ "    \"type\": \"interactive\",\r\n"
    				+ "    \"interactive\": {\r\n"
    				+ "        \"type\": \"button\",\r\n"
    				+ "        \"body\": {\r\n"
    				+ "            \"text\": \""+ informacionCita +"\"\r\n"
    				+ "        },\r\n"
    				+ "        \"action\": {\r\n"
    				+ "            \"buttons\": [\r\n"
    				+ "                {\r\n"
    				+ "                    \"type\": \"reply\",\r\n"
    				+ "                    \"reply\": {\r\n"
    				+ "                        \"id\": \""+ proceso +"\",\r\n"
    				+ "                        \"title\": \"➡️ Continuar\"\r\n"
    				+ "                    }\r\n"
    				+ "                },\r\n"
    				+ "                {\r\n"
    				+ "                    \"type\": \"reply\",\r\n"
    				+ "                    \"reply\": {\r\n"
    				+ "                        \"id\": \"terminar_sesion\",\r\n"
    				+ "                        \"title\": \"❌ Cancelar\"\r\n"
    				+ "                    }\r\n"
    				+ "                }\r\n"
    				+ "            ]\r\n"
    				+ "        }\r\n"
    				+ "    }\r\n"
    				+ "}";
    		sendHttpRequest(requestBody);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void sendSeguros(String to, String token, int appClient, String nroDoc, int tipoDoc, Map<String, Object> mapData) {
  	    List<Map<String, Object>> lstSeguros = new ArrayList<>();
      	String response = "";   
	    String admision = "29288258";
	    String nroDocumento = "20162580672";
	    Map<String, Object> mapResponse = new HashMap<String, Object>();
		HttpResponse responseData = null;
		JsonReader jsonReader = null;
		JsonObject jsonObject = null;  	       
		JsonArray jsonArray = null;
		try {
	        Map<String, String> headers = new HashMap<String, String>();
	        headers.put("Content-Type", "application/json; charset=utf-8");
	        headers.put("Authorization", "1tFTQwISn4c=");
	        String inputJson = "{\"doConsultante\":\""+ admision  +"\",\"idInstitucion\":\""+nroDocumento+"\",\"nuDocumento\":\""+ nroDoc +"\",\"tiDocumento\":\""+ String.valueOf(tipoDoc).replace("4", "2").replace("7", "3")+"\"}";
	        responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_SITEDS_CONSULTA_DOCUMENTO, inputJson, headers);
	        response = responseData.getResponseBody();
			
		} catch (Exception e) {
			sendMessageFinalizar(to, "😓 No se ha podido obtener los seguros del paciente, intentalo nuevamente más tarde.");
			return;
		} 
		
	    jsonReader = Json.createReader(new StringReader(response));
	    jsonObject = jsonReader.readObject();
	    String coError = jsonObject.getString("coError");
	    if(coError.equals("1002")) {
	    	sendMessage(to, "No se ha encontrado datos para el número de documento brindado.");
	        //throw UtilResponse.rsException(Status.CONFLICT, "No se han encontrado datos para este documento, revisa los datos correctamente y vuelve a intentarlo.");
	    }
	    
	    JsonArray afiliaciones;

	    if (jsonObject.containsKey("Afiliaciones") && 
	    	    jsonObject.get("Afiliaciones").getValueType() == JsonValue.ValueType.OBJECT) {

	    	    JsonObject afiliacionesObject = jsonObject.getJsonObject("Afiliaciones");

	    	    if (afiliacionesObject.containsKey("afiliacion") &&
	    	        afiliacionesObject.get("afiliacion").getValueType() == JsonValue.ValueType.ARRAY) {

	    	        afiliaciones = afiliacionesObject.getJsonArray("afiliacion");
	    	    } else {
	    	        afiliaciones = Json.createArrayBuilder().build(); // Arreglo vacío
	    	    }
	    	} else {
	    	    afiliaciones = Json.createArrayBuilder().build(); // Arreglo vacío
	    	}
	    
	    Set<String> deIafasSet = new HashSet<String>();
        List<JsonObject> filteredAfiliaciones = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate currentDate = LocalDate.now();
        
        for (int i = 0; i < afiliaciones.size(); i++) {
            JsonObject afiliacion = afiliaciones.getJsonObject(i);
            String deIafas = afiliacion.getString("deIafas");
            String tiPlanSalud = afiliacion.getString("tiPlanSalud");
            String feFinAfiliacion = afiliacion.getString("feFinAfiliacion").trim();
            
            // Filtrar si es SCTR o EsSalud
            if ("SCTR".equals(afiliacion.getString("dePlanSalud")) || "EsSalud".equals(deIafas) || "SIS".equals(deIafas)) {
                continue;
            }
            
            // Filtrar por fecha de fin de afiliación
            if (!feFinAfiliacion.isEmpty()) {
                LocalDate finAfiliacionDate = LocalDate.parse(feFinAfiliacion, formatter);
                if (finAfiliacionDate.isBefore(currentDate)) {
                    continue;
                }
            }
            
            // Eliminar duplicados por "deIafas"
            if (!deIafasSet.contains(deIafas)) {
                deIafasSet.add(deIafas);
                filteredAfiliaciones.add(afiliacion);
            }
        }
        
		try {
	        Map<String, String> headers = new HashMap<String, String>();
	        headers.put("Content-Type", "application/json; charset=utf-8");
	        headers.put("Authorization", GlobalConstants.API_DEFAULT_TOKEN_RESERVAS);
	        responseData  = HttpRequestUtil.sendRequest("GET", GlobalConstants.API_ASEGURADORAS, null, headers);
	        response = responseData.getResponseBody();
			
		} catch (Exception e) {
			sendMessageFinalizar(to, "😓 No se ha podido obtener los seguros de nuestro servicio, intentalo nuevamente más tarde.");
			return;
		}
		
		jsonReader = Json.createReader(new StringReader(response));
		jsonArray = jsonReader.readArray();
		
		if(jsonArray.isEmpty()) {
			sendMessageFinalizar(to, "😓 No hay seguros registrados en nuestro servicio, contacta a un administrador.");
			return;
		}
	
        
		try {
	        Map<String, String> headers = new HashMap<String, String>();
	        headers.put("Content-Type", "application/json; charset=utf-8");
	        headers.put("Authorization", GlobalConstants.API_DEFAULT_TOKEN_RESERVAS);
	        System.out.println("API CONSULTA: " + GlobalConstants.API_OBTENER_IAFAS);
	        responseData  =HttpRequestUtil.sendRequest("GET", GlobalConstants.API_OBTENER_IAFAS, null, headers);
	        response = responseData.getResponseBody();
	        System.out.println("RESPONSE IAFAS: " + GlobalConstants.API_OBTENER_IAFAS);

		} 

		catch (Exception e) {
			sendMessageFinalizar(to, "No se han podido obtener los seguros de la clinica.");
			return;
		}
		
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
        List<Map<String, Object>> seguros = gson.fromJson(response, listType);

        List<String> iafas = seguros.stream()
                .map(s -> (String) s.get("codigoIafa"))
                .collect(Collectors.toList());
		
        String mensaje = "";
        String siConvenio = "";
        String noConvenio = "";
        String conSoporte = "";
        int flag = 1;
        int soporteFlag = 9; 
        siConvenio = "*1.* Particular 🏥\\n";
        Map<String, Object> mapSeguro = new HashMap<>();
        mapSeguro.put("codIafa", 1);
        mapSeguro.put("descripcionSeguro", "Particular");
        mapSeguro.put("codAseguradora", 1);

        Map<String, Object> seguroOpciones = new HashMap<>();
        seguroOpciones.put(String.valueOf(flag), mapSeguro);
        lstSeguros.add(seguroOpciones);

        for (JsonObject afiliacion : filteredAfiliaciones) {	
        	boolean foundMatch = false;
    	    for (Iterator iterator = jsonArray.iterator(); iterator.hasNext();) {
    			JsonValue jsonValue = (JsonValue) iterator.next();
    			JsonObject jsonObjectAse = jsonValue.asJsonObject();
    			String codIafas = afiliacion.getString("coIafas").replace("40003", "40004");
    			System.out.println("Valor del json: " + jsonValue);
    			String codIafasSeguro = jsonObjectAse.getString("codigoIafa");
    			if(codIafas.equals(codIafasSeguro)) {
    				if(iafas.contains(codIafas)) {    					
    					mapSeguro = new HashMap<String, Object>();
    					seguroOpciones= new HashMap<String, Object>();
    					siConvenio += "*" + (flag + 1) + ".* " + afiliacion.getString("deIafas") + " 🏥\\n";
    					mapSeguro.put("codIafa", codIafasSeguro);
    					mapSeguro.put("descripcionSeguro", afiliacion.getString("deIafas"));
    					mapSeguro.put("codAseguradora", jsonObjectAse.getJsonNumber("codigoSeguro"));
    					seguroOpciones.put(String.valueOf((flag + 1)), mapSeguro);
    					lstSeguros.add(seguroOpciones);
    					flag++;
    				}
    				/*else {
    	                conSoporte += "⚠️ " + afiliacion.getString("deIafas") + " (Contacta a soporte) 🏥\\n";
    				}*/
    				else {
    	                // No disponible: requiere contacto a soporte
    	                mapSeguro = new HashMap<>();
    	                seguroOpciones = new HashMap<>();

    	                conSoporte += "*" + soporteFlag + ".* " + afiliacion.getString("deIafas") + " (Contactar A. Cliente) 🏥\\n";

    	                mapSeguro.put("codIafa", codIafasSeguro);
    					mapSeguro.put("descripcionSeguro", afiliacion.getString("deIafas"));
    					mapSeguro.put("codAseguradora", jsonObjectAse.getJsonNumber("codigoSeguro"));
    	                seguroOpciones.put(String.valueOf(soporteFlag), mapSeguro);
    	                lstSeguros.add(seguroOpciones);
    	                soporteFlag++;
    	            }
    				foundMatch = true;
        		}
        		
    		}
    	    
    	    if(!foundMatch) {
    	    	noConvenio += afiliacion.getString("deIafas") + " 🏥\\n";
    	    }       	
        }
        
        String mensajeConvenios = "Seguros disponibles: ✅\\n\\n" + siConvenio + "\\n\\n";
        mapData.put("lstSeguros", lstSeguros);
        if(!noConvenio.isEmpty()) {
        	mensajeConvenios += "Seguros sin convenio: ❌\\n\\n" + noConvenio + "\\n\\n";
        }
        if (!conSoporte.isEmpty()) {
            mensajeConvenios += "Seguros con A. Cliente: ⚠️\\n\\n" + conSoporte + "\\n";
        }
        
        String requestBody = "{\r\n"
        		+ "    \"messaging_product\": \"whatsapp\",\r\n"
        		+ "    \"recipient_type\": \"individual\",\r\n"
        		+ "    \"to\": \""+ to +"\",\r\n"
        		+ "    \"type\": \"interactive\",\r\n"
        		+ "    \"interactive\": {\r\n"
        		+ "        \"type\": \"button\",\r\n"
        		+ "        \"body\": {\r\n"
        		+ "            \"text\": \"A continuación, te mostramos tu *lista de seguros afiliados a nuestra clínica*.\\n\\n"+ mensajeConvenios +"Porfavor, élige si tu atención es de manera *Particular* o *Seguro*, según el número correspondiente para continuar con el último paso. 😊\"\r\n"
        		+ "        },\r\n"
        		+ "        \"action\": {\r\n"
        		+ "            \"buttons\": [\r\n"
        		+ "                {\r\n"
        		+ "                    \"type\": \"reply\",\r\n"
        		+ "                    \"reply\": {\r\n"
        		+ "                        \"id\": \"retornar\",\r\n"
        		+ "                        \"title\": \"🔁 Atrás\"\r\n"
        		+ "                    }\r\n"
        		+ "                },\r\n"
				+ "                {\r\n"
				+ "                    \"type\": \"reply\",\r\n"
				+ "                    \"reply\": {\r\n"
				+ "                        \"id\": \"contactar_soporte\",\r\n"
				+ "                        \"title\": \"🧑‍💻 Ayuda\"\r\n"
				+ "                    }\r\n"
				+ "                },\r\n"
        		+ "                {\r\n"
        		+ "                    \"type\": \"reply\",\r\n"
        		+ "                    \"reply\": {\r\n"
        		+ "                        \"id\": \"terminar_sesion\",\r\n"
        		+ "                        \"title\": \"🔚 Finalizar\"\r\n"
        		+ "                    }\r\n"
        		+ "                }\r\n"
        		+ "            ]\r\n"
        		+ "        }\r\n"
        		+ "    }\r\n"
        		+ "}";
        sendHttpRequest(requestBody);
    }
       
    public void sendMessage(String to, String message) {
        String requestBody = "{\r\n"
        		+ "    \"messaging_product\": \"whatsapp\",    \r\n"
        		+ "    \"recipient_type\": \"individual\",\r\n"
        		+ "    \"to\": \""+ to +"\",\r\n"
        		+ "    \"type\": \"text\",\r\n"
        		+ "    \"text\": {\r\n"
        		+ "        \"body\": \""+ message +"\"\r\n"
        		+ "    }\r\n"
        		+ "}";

        sendHttpRequest(requestBody);
    }
    
    public void sendListEspecialidad(String to, String token, Map<String, Object> mapConversation) {
    	try {  		
    		String response = "";
    		List<Map<String, Object>> lstEspecialidades = new ArrayList<>();
    		HttpResponse responseData = null;
    		JsonReader jsonReader = null;
    		JsonObject jsonObject = null;  	       
    		JsonArray jsonArray = null;
    		try {
    	        Map<String, String> headers = new HashMap<String, String>();
    	        headers.put("Content-Type", "application/json; charset=utf-8");
    	        headers.put("Authorization",GlobalConstants.API_DEFAULT_TOKEN_RESERVAS);
    	        responseData  = HttpRequestUtil.sendRequest("GET", GlobalConstants.API_ESPECIALIDADES, null, headers);
    	        response = responseData.getResponseBody();
    			
    		} catch (Exception e) {
    			sendMessageFinalizar(to, "😓 No se ha podido obtener las especialidades en este momento, intentalo nuevamente mas tarde.");
    			return;
    		}
    		
  	    	System.out.println("RESPONSE CODE: " + responseData.getStatusCode());
  	    	System.out.println("ERROR MESSAGE: " + responseData.getErrorMessage());
  	    	System.out.println("RESPONSE ESPECIALIDADES: " + responseData.getResponseBody() + ", FULL API: " + GlobalConstants.API_LOGIN_CLINICA);
    		
    		jsonReader = Json.createReader(new StringReader(response));
    		//jsonObject = jsonReader.readObject();
    		jsonArray = jsonReader.readArray();
    		
    		if(jsonArray.size() == 0) {
        		sendMessageFinalizar(to, "😓 No hay especialidades habilitadas por el momento.");
        		return;
    		}
    		int count = 0;
    		StringBuilder especialidadBuilder = new StringBuilder();
    		String especialidad = "";
    		for (int i = 0; i < jsonArray.size(); i++) {
    			Map<String, Object> mapEspecialidad = new HashMap<String, Object>();
    			jsonObject = jsonArray.getJsonObject(i);
    			especialidad += "*" + (i + 1) + ".*" + " " + capitalizeFirstLetter(jsonObject.getString("tabNombre")) + "\\n";
    		    especialidadBuilder.append("*").append(i + 1).append(".* ")
                .append(capitalizeFirstLetter(jsonObject.getString("tabNombre")))
                .append("\\n");
    			mapEspecialidad.put(String.valueOf((i + 1)), jsonObject);
    			lstEspecialidades.add(mapEspecialidad);
    			count++;
    		    if (count == 15 || i == jsonArray.size() - 1) {
    		    	sendMessage(to, especialidadBuilder.toString());
    		        especialidadBuilder.setLength(0);
    		        count = 0;
    		    }
    		}
    		
    		mapConversation.put("lstEspecialidades", lstEspecialidades);
    		sendMessageContinuar(to, "Digita el número de opcion de la especialidad. 🥼🙌😊");
    	}
    	catch(Exception ex){
    		ex.printStackTrace();
    	}
    }
    
    public String obtenerUrlAcortada(String to, String link) {
    	String response = "";
  	  	HttpResponse responseData = null;
		try {
			String inputJson = String.format("{\"url\":\"%s\"}", link);
	        System.out.println("JSON ACORTADOR: " + inputJson);
			Map<String, String> headers = new HashMap<String, String>();
	        headers.put("Content-Type", "application/json; charset=utf-8");
	        responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_URL_RECORTER, inputJson, headers);
	        response = responseData.getResponseBody();
	        System.out.println("RESPONSE ACORTADOR: " + inputJson);
	        return response;
			
		} catch (Exception e) {
			e.printStackTrace();
			sendMessageFinalizar(to, "Ha ocurrido un error al obtener el link del pago.");
			return null;
		}
    }
    
    public void sendListHorariosXDisponibles(String to, String token, Map<String, Object> mapDatos) {
    	try {		
    		String response = "";
    		HttpResponse responseData = null;
    		JsonReader jsonReader = null;
    		JsonObject jsonObject = null;  	       
    		JsonArray jsonArray = null;
    		Map<String, Object> especialidadFechas = (Map<String, Object>) mapDatos.get("especialidad_fecha");
    		List<Map<String, Object>> lstEspecialisadesHorarios = new ArrayList<Map<String,Object>>();
    		lstEspecialisadesHorarios.clear();
    		String fecha = "";
    		Integer codMedico = Integer.valueOf(especialidadFechas.get("codProf").toString());
    		Integer codEspecialidad = Integer.valueOf(especialidadFechas.get("codEspecialidad").toString());
    		//String codHorario = String.valueOf(especialidadFechas.get("codHora").toString());
    		fecha = especialidadFechas.get("fechaDisponible").toString().replace("Z", "");
    		fecha = fecha.replace("\"", "") + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    		/*LocalDate fechaActual = LocalDate.parse(fecha);
    		
    		int dia = fechaActual.getDayOfMonth();
    		int mes = fechaActual.getMonthValue();
    		int anio = fechaActual.getYear();
    		System.out.println("Día: " + dia);
    		System.out.println("Mes: " + mes);
    		System.out.println("Año: " + anio);
            String mesFormateado = String.format("%02d", mes);
            String diaFormateado = String.format("%02d", dia);*/
    		String urlData = GlobalConstants.API_HORARIOS_DET_ESPECIALIDAD + "?tabEspecialidad="+ codEspecialidad +"&medId="+ codMedico +"&fecha=" + fecha;
    		System.out.println("API OBTENER HORARIOS ESPECIALIDADES: " + urlData);

    		try {
    			//String inputJson = String.format("{\"idespecialidad\":\"\",\"medico\":%s,\"anio\":\"%s\",\"mes\":\"%s\",\"dia\":\"%s\",\"idHorario\":\"%s\"}", codMedico, anio, mesFormateado, diaFormateado, codHorario);
    	        //System.out.println("JSON HORARIOS: " + inputJson);
    			Map<String, String> headers = new HashMap<String, String>();
    	        headers.put("Content-Type", "application/json; charset=utf-8");
    	        headers.put("Authorization", GlobalConstants.API_DEFAULT_TOKEN_RESERVAS);
    	        responseData  = HttpRequestUtil.sendRequest("GET", urlData, null, headers);
    	        response = responseData.getResponseBody();
    	        //System.out.println("RESPONSE HORARIOS: " + inputJson);
    			
    		} catch (Exception e) {
    			sendMessageFinalizar(to, "Ha ocurrido un error al intentar obtener las horas disponibles  del medicos, intentalo nuevamente volviendo al paso anterior.");
    			return;
    		}
    		
    		if(response.contains("El medico no tiene horas disponibles para ese dia")) 
    		{
    			sendMessageContinuar(to, "El medico no tiene horas disponibles para ese día.");
    			return;
    		}
    		
    		System.out.println("RESPONSE HORARIOS DISPONIBLES X MEDICO: " + response);
    		
    		jsonReader = Json.createReader(new StringReader(response));
    		jsonArray = jsonReader.readArray();

    		Map<String, List<JsonObject>> horariosPorFecha = new LinkedHashMap<>();
    		DateTimeFormatter formatoEntrada = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    		DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");

    		int totalMostrados = 0;
    		int flag = 0;

    		// Agrupar por fecha
    		for (int i = 0; i < jsonArray.size(); i++) {
    		    JsonObject medicoObj = jsonArray.getJsonObject(i);
    		    JsonArray horarios = medicoObj.getJsonArray("HORARIOS");

    		    for (int j = 0; j < horarios.size(); j++) {
    		        JsonObject horarioObj = horarios.getJsonObject(j);
    		        String fechaHora = horarioObj.getString("fecha");
    		        String fechaAgru = fechaHora.split(" ")[0]; // Solo la fecha

    		        horariosPorFecha.computeIfAbsent(fechaAgru, k -> new ArrayList<>()).add(horarioObj);
    		    }
    		}

    		for (Map.Entry<String, List<JsonObject>> entry : horariosPorFecha.entrySet()) {
    		    if (totalMostrados >= 5) break;

    		    String fechaAgru = entry.getKey();
    		    List<JsonObject> horariosDia = entry.getValue();

    		    // Duración
    		    long duracionMin = 0;
    		    if (horariosDia.size() >= 2) {
    		        LocalDateTime inicio = LocalDateTime.parse(horariosDia.get(0).getString("fecha"), formatoEntrada);
    		        LocalDateTime siguiente = LocalDateTime.parse(horariosDia.get(1).getString("fecha"), formatoEntrada);
    		        duracionMin = Duration.between(inicio, siguiente).toMinutes();
    		    }

    		    StringBuilder mensajeFecha = new StringBuilder();
    		    mensajeFecha.append("Fecha: *").append(formaterDate(fechaAgru)).append("* - Duración: *").append(duracionMin).append(" min*\\n");

    		    for (JsonObject h : horariosDia) {
    		        if (totalMostrados >= 5) break;

    		        LocalDateTime hora = LocalDateTime.parse(h.getString("fecha"), formatoEntrada);
    		        String horaFormateada = hora.format(formatoHora);

    		        flag++;
    		        mensajeFecha.append("    *").append(flag).append(".* ").append(horaFormateada).append("\\n");

    		        Map<String, Object> mapHorario = new HashMap<>();
    		        mapHorario.put("fechaDisponible", fechaAgru);
    		        mapHorario.put("hora", h.getString("fecha"));
    		        mapHorario.put("turnoId", h.getInt("turnoId"));
    		        mapHorario.put("consId", h.getInt("consId"));
    		        lstEspecialisadesHorarios.add(Map.of(String.valueOf(flag), mapHorario));

    		        totalMostrados++;
    		    }

    		    sendMessage(to, mensajeFecha.toString());
    		}
    		
    		if(lstEspecialisadesHorarios.size() == 0) {
        		sendMessageContinuar(to, "😓 La fecha seleccionado no cuenta con horarios disponibles para este día, puedes volver al paso anterior para poder elegir otro.");
        		mapDatos.remove("lstHorarios");
        		mapDatos.remove("especialidadesFechas");
        		return;
    		}   		
    		mapDatos.put("lstHorarios", lstEspecialisadesHorarios);
    		//sendMessage(to, horarios);  	
    		sendMessageContinuar(to, "Ahora puedes visualizar la *lista de horarios disponibles*. 👨‍💼\\n\\nPor favor, elige el número correspondiente a la hora que desees 😊");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
       
    public void sendListFechasXMedico(String to, String token, Map<String, Object> mapDatos, boolean vista) {
    	try {
    		String response = "";
    		HttpResponse responseData = null;
    		JsonReader jsonReader = null;
    		JsonObject jsonObject = null;  	       
    		JsonArray jsonArray = null;
            LocalDate fechaActual = LocalDate.now();
            int dia = fechaActual.getDayOfMonth();
            int mes = fechaActual.getMonthValue();
            int anio = fechaActual.getYear();
            
            ZonedDateTime fechaHoraPeru = ZonedDateTime.now(ZoneId.of("America/Lima"));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            String horaActual = fechaHoraPeru.format(formatter);
            
            System.out.println("Día: " + dia);
            System.out.println("Mes: " + mes);
            System.out.println("Año: " + anio);
            
            String fechaActualStr = String.format("%04d-%02d-%02d" + horaActual, anio, mes, dia); 
    		Map<String, Object> especialidad = (Map<String, Object>) mapDatos.get("especialidad");
    		Integer especialidadId = Integer.valueOf(
    			    especialidad.get("tabId").toString().replace("\"", "")
    				);
    		String especialidadDesc = String.valueOf(especialidad.get("tabNombre"));
    		List<Map<String, Object>> lstEspecialidadesFechas = new ArrayList<>();
    		lstEspecialidadesFechas.clear();
    		System.out.println("FECHA ACTUAL STR ENVIADO: " + fechaActualStr);
    		try {
    	        Map<String, String> headers = new HashMap<String, String>();
    	        headers.put("Content-Type", "application/json; charset=utf-8");
    	        headers.put("Authorization", GlobalConstants.API_DEFAULT_TOKEN_RESERVAS);
    	        //String inputJson = String.format("{\"idEspecialidad\":%s,\"anio\":\"%s\",\"mes\":\"%s\",\"dia\":%s,\"sucursal\":\"\"}", especialidadId, anio, mes, dia);
    	        responseData  = HttpRequestUtil.sendRequest("GET", GlobalConstants.API_HORARIOS_ESPECIALIDAD + "?tabEspecialidad=" + especialidadId + "&fecha=" + fechaActualStr , null, headers);
    	        System.out.println("FECHAS OBTENIDAD PARA LA ESPECIALIDAD: " + response);
    	        response = responseData.getResponseBody();
    			
    		} catch (Exception e) {
    			sendMessageFinalizar(to, "😓 No se ha podido obtener los horarios para esta especialidad en este momento, intentalo nuevamente mas tarde.");
    			return;
    		}
    		
    		if(response == null) {
    			sendMessageContinuar(to, "😓 No se ha podido obtener los horarios para esta especialidad en este momento, intentalo nuevamente mas tarde.");
    			return;
    		}
    		
    		if(response.contains("No existe horario para esta Especialidad")) {
    			sendMessageContinuar(to, "No hay medicos con horarios disponibles para esta especialidad, finaliza e intenta con otra especialidad.");
    			return;
    		}
    		
    		jsonReader = Json.createReader(new StringReader(response));
    		jsonArray = jsonReader.readArray();
    		
    		if(jsonArray.size() == 0) {
    			sendMessageContinuar(to, "😓 La especialidad seleccionada no cuenta con horarios disponibles por el momento.");
        		return;
    		}
    		
    		System.out.println("FECHAS DE LA ESPECIALIDAD: " + response);
    		String especialidadFecha = "";
    		/*Map<String, Object> mapHorariosPadre = new HashMap<String, Object>();
    		int flag = 0;
    		for (int i = 0; i < jsonArray.size(); i++) {
    			JsonObject jsonPadre = jsonArray.getJsonObject(i);
    			especialidadFecha = "Dr(a): *" + jsonPadre.getString("medico") + "*\\n"
    					+ "Duración: *" + jsonPadre.getJsonNumber("tiempoPromedioAtencion").doubleValue() + " min.* 🕛\\n";
    			JsonArray jsonArrayFechas = jsonPadre.getJsonArray("dias");
    			jsonArrayFechas = jsonArrayFechas.getJsonArray(0);
    			if(jsonArrayFechas.size() > 0) {    				
    				for(int j = 0; j < jsonArrayFechas.size() && j < 5; j++) {
    					JsonObject jsonHijo = jsonArrayFechas.getJsonObject(j);
    					JsonArray jsonArraySubHijo = jsonHijo.getJsonArray("horarios");
    					JsonObject jsonSubHijo = jsonArraySubHijo.getJsonObject(0);
    					if(j == 0) {    						
    						especialidadFecha += "Horario: *" + formaterTimeAQP(jsonSubHijo.getString("horaInicio")) + " - " + formaterTimeAQP(jsonSubHijo.getString("horaFin")) + "* ⌛\\n\\n";
    					}
    					if(vista) {    						
    						Map<String, Object> mapHorarios = new HashMap<String, Object>();
    						especialidadFecha += "*" + (flag + 1) + ".* " + formaterDate(jsonHijo.getString("fecha")) + "\\n";
    						mapHorarios.put("fechaDisponible", jsonHijo.getString("fecha"));
    						mapHorarios.put("turno", Integer.valueOf(1));
    						mapHorarios.put("codHora", jsonHijo.getString("idHorario"));
    						mapHorarios.put("codProf", jsonPadre.getInt("idMedico"));
    						mapHorarios.put("profNombre", jsonPadre.getString("medico"));
    						mapHorarios.put("duracionCita", jsonPadre.getJsonNumber("tiempoPromedioAtencion").doubleValue());
    						mapHorariosPadre.put(String.valueOf((flag + 1)), mapHorarios);
    						lstEspecialidadesFechas.add(mapHorariosPadre);
    						flag++;
    					}
    				}
    			}
    			sendMessage(to, especialidadFecha);
    		}*/
    		int flag = 0;

    		for (int i = 0; i < jsonArray.size(); i++) {
    		    JsonObject jsonMedico = jsonArray.getJsonObject(i);
    		    String nombreMedico = "Dr(a): *" + jsonMedico.getString("medNombres") + " " + jsonMedico.getString("medPaterno") + " " + jsonMedico.getString("medMaterno") + "*\\n";
    		    especialidadFecha = nombreMedico;
    		    JsonArray fechasArray = jsonMedico.getJsonArray("fechas");

    		    for (int j = 0; j < fechasArray.size() && j < 5; j++) {
    		        JsonObject fechaObj = fechasArray.getJsonObject(j);
    		        String fecha = fechaObj.getString("fecha");

    		        Map<String, Object> mapHorarios = new HashMap<>();
    		        mapHorarios.put("fechaDisponible", fecha);
    		        mapHorarios.put("codProf", jsonMedico.getInt("medId"));
    		        mapHorarios.put("codEspecialidad", jsonMedico.getInt("tabEspecialidad"));
    		        mapHorarios.put("profNombre", jsonMedico.getString("medNombres") + " " + jsonMedico.getString("medPaterno") + " " + jsonMedico.getString("medMaterno"));
    		        especialidadFecha += "*" + (flag + 1) + ".* " + formaterDate(fecha);
    		        if(fechaObj.getInt("disponible") == 0) {
    		        	especialidadFecha += " (No Disponible)";
    		        }
    		        especialidadFecha += "\\n";
    		        Map<String, Object> mapHorariosPadre = new HashMap<>();
    		        mapHorariosPadre.put(String.valueOf(flag + 1), mapHorarios);
    		        lstEspecialidadesFechas.add(mapHorariosPadre);
    		        flag++;
    		    }

    		    sendMessage(to, especialidadFecha);
    		}
    		mapDatos.put("especialidadesFechas", lstEspecialidadesFechas);
    		sendMessageContinuar(to, "Ahora puedes visualizar la *lista de fechas disponibles/horarios* y los medicos asignados para especialidad de *" + especialidadDesc.replace("\"", "") + "*. 👨‍💼\\n\\nPor favor, elige el número correspondiente de la fecha que desees. 😊");
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void sendButtonsModulos(String to, String message) {
    	
    	String requestBody = "{\r\n"
    			+ "    \"messaging_product\": \"whatsapp\",\r\n"
    			+ "    \"recipient_type\": \"individual\",\r\n"
    			+ "    \"to\": \""+ to +"\",\r\n"
    			+ "    \"type\": \"interactive\",\r\n"
    			+ "    \"interactive\": {\r\n"
    			+ "        \"type\": \"list\",\r\n"
    			+ "        \"header\": {\r\n"
    			+ "            \"type\": \"text\",\r\n"
    			+ "            \"text\": \"¡Validación exitosa! ✅\"\r\n"
    			+ "        },\r\n"
    			+ "        \"body\": {\r\n"
    			+ "            \"text\": \"¡Hola *"+ message +"*! 👋\\n\\nAhora puedes continuar utilizando nuestros servicios. ¡Gracias por confiar en nosotros! 😊\\n\\n\"\r\n"
    			+ "        },\r\n"
    			+ "        \"footer\": {\r\n"
    			+ "            \"text\": \"Elige una opción para continuar 👇\"\r\n"
    			+ "        },\r\n"
    			+ "        \"action\": {\r\n"
    			+ "            \"button\": \"Módulos Disponibles\",\r\n"
    			+ "            \"sections\": [\r\n"
    			+ "                {\r\n"
    			+ "                    \"title\": \"👨‍⚕️ Principal\",\r\n"
    			+ "                    \"rows\": [\r\n"
    			+ "                        {\r\n"
    			+ "                            \"id\": \"reservar_cita\",\r\n"
    			+ "                            \"title\": \"👩‍⚕️ Agendar Cita\",\r\n"
    			+ "                            \"description\": \"Podrás reservar tu cita para cualquier especialidad y medico.\"\r\n"
    			+ "                        },\r\n"
    			/*+ "                        {\r\n"
    			+ "                            \"id\": \"citas_programadas\",\r\n"
    			+ "                            \"title\": \"🗓️ Citas Programadas\",\r\n"
    			+ "                            \"description\": \"Obtendrás las ultimas citas programadas que tengas.\"\r\n"
    			+ "                        },\r\n"
    			+ "                        {\r\n"
    			+ "                            \"id\": \"historial_citas\",\r\n"
    			+ "                            \"title\": \"📋 Historial de Citas\",\r\n"
    			+ "                            \"description\": \"Obtendrás las 3 ultimas citas que has tenido.\"\r\n"
    			+ "                        },\r\n"*/
    			+ "                        {\r\n"
    			+ "                            \"id\": \"info_horarios\",\r\n"
    			+ "                            \"title\": \"🔍 Horarios Disponibles\",\r\n"
    			+ "                            \"description\": \"Consultar información de horarios disponibles.\"\r\n"
    			+ "                        },\r\n"
    			+ "                        {\r\n"
    			+ "                            \"id\": \"chequeos_previos\",\r\n"
    			+ "                            \"title\": \"📋 Chequeos Preventivos\",\r\n"
    			+ "                            \"description\": \"Consulta sobre chequeos preventivos.\"\r\n"
    			+ "                        },\r\n"
    			+ "                    ]\r\n"
    			+ "                },\r\n"
    			+ "                {\r\n"
    			+ "                    \"title\": \"🧑‍💻 Otras Consultas\",\r\n"
    			+ "                    \"rows\": [\r\n"
    			+ "                        {\r\n"
    			+ "                            \"id\": \"contactar_soporte\",\r\n"
    			+ "                            \"title\": \"📞 Atención al Cliente\",\r\n"
    			+ "                            \"description\": \"Atención: L-V 8:00 a 17:00, S 8:00 a 13:00.\"\r\n"
    			+ "                        },\r\n"
    			+ "                        {\r\n"
    			+ "                            \"id\": \"promociones_clinica\",\r\n"
    			+ "                            \"title\": \"🌟 Promociones\",\r\n"
    			+ "                            \"description\": \"Publicidad de promociones.\"\r\n"
    			+ "                        },\r\n"
    			+ "                        {\r\n"
    			+ "                            \"id\": \"retornar\",\r\n"
    			+ "                            \"title\": \"🔄 Cambiar Documento\",\r\n"
    			+ "                            \"description\": \"Cambiar tipo de documento ingresado.\"\r\n"
    			+ "                        },\r\n"
    			+ "                        {\r\n"
    			+ "                            \"id\": \"terminar_sesion\",\r\n"
    			+ "                            \"title\": \"❌ Cerrar Sesión\",\r\n"
    			+ "                            \"description\": \"Cerrará la sesión actual.\"\r\n"
    			+ "                        },\r\n"
    			+ "                    ]\r\n"
    			+ "                }\r\n"
    			+ "            ]\r\n"
    			+ "        }\r\n"
    			+ "    }\r\n"
    			+ "}";

        sendHttpRequest(requestBody);
    }
    
    public void sendAcceptTermins(String to) {
        String requestBody = "{\r\n"
        		+ "    \"messaging_product\": \"whatsapp\",\r\n"
        		+ "    \"recipient_type\": \"individual\",\r\n"
        		+ "    \"to\": \""+ to +"\",\r\n"
        		+ "    \"type\": \"interactive\",\r\n"
        		+ "    \"interactive\": {\r\n"
        		+ "        \"type\": \"button\",\r\n"
        		+ "        \"header\": {\r\n"
        		+ "            \"type\": \"text\",\r\n"
        		+ "            \"text\": \"¡Bienvenido a Orden Hospitalaria Clínica San Juan De Dios! 👋\"\r\n"
        		+ "        },\r\n"
        		+ "        \"body\": {\r\n"
        		+ "            \"text\": \"¡Hola, soy *Juan D'*! tu asistente virtual 🤗\\nPara continuar, debes aceptar los términos y condiciones del uso de nuestro servicio.\\n\\nhttps://www.sanjuandediosarequipa.com/terminos-condiciones/\\n\\n¿Deseas continuar? \"\r\n"
        		+ "        },\r\n"
        		+ "        \"action\": {\r\n"
        		+ "            \"buttons\": [\r\n"
        		+ "                {\r\n"
        		+ "                    \"type\": \"reply\",\r\n"
        		+ "                    \"reply\": {\r\n"
        		+ "                        \"id\": \"accept_terms\",\r\n"
        		+ "                        \"title\": \"✔️ Aceptar\"\r\n"
        		+ "                    }\r\n"
        		+ "                },\r\n"
        		+ "                {\r\n"
        		+ "                    \"type\": \"reply\",\r\n"
        		+ "                    \"reply\": {\r\n"
        		+ "                        \"id\": \"decline_terms\",\r\n"
        		+ "                        \"title\": \"❌ Declinar\"\r\n"
        		+ "                    }\r\n"
        		+ "                }\r\n"
        		+ "            ]\r\n"
        		+ "        }\r\n"
        		+ "    }\r\n"
        		+ "}";

        sendHttpRequest(requestBody);
    }    
    
    public void sendTypeDocumet(String to) {
        String requestBody = "{\r\n"
        		+ "    \"messaging_product\": \"whatsapp\",\r\n"
        		+ "    \"recipient_type\": \"individual\",\r\n"
        		+ "    \"to\": \""+ to +"\",\r\n"
        		+ "    \"type\": \"interactive\",\r\n"
        		+ "    \"interactive\": {\r\n"
        		+ "        \"type\": \"button\",\r\n"
        		+ "        \"header\": {\r\n"
        		+ "            \"type\": \"text\",\r\n"
        		+ "            \"text\": \"¡Genial! 😉\"\r\n"
        		+ "        },\r\n"
        		+ "        \"body\": {\r\n"
        		+ "            \"text\": \"Para brindarte una mejor atención, por favor elige el *tipo de documento* con el que deseas identificarte 👇\"\r\n"
        		+ "        },\r\n"
        		+ "        \"action\": {\r\n"
        		+ "            \"buttons\": [\r\n"
        		+ "                {\r\n"
        		+ "                    \"type\": \"reply\",\r\n"
        		+ "                    \"reply\": {\r\n"
        		+ "                        \"id\": \"doc_dni\",\r\n"
        		+ "                        \"title\": \"👨‍💼 DNI\"\r\n"
        		+ "                    }\r\n"
        		+ "                },\r\n"
        		+ "                {\r\n"
        		+ "                    \"type\": \"reply\",\r\n"
        		+ "                    \"reply\": {\r\n"
        		+ "                        \"id\": \"doc_ce\",\r\n"
        		+ "                        \"title\": \"🆔 C.E.\"\r\n"
        		+ "                    }\r\n"
        		+ "                },\r\n"
        		+ "                {\r\n"
        		+ "                    \"type\": \"reply\",\r\n"
        		+ "                    \"reply\": {\r\n"
        		+ "                        \"id\": \"doc_pasaporte\",\r\n"
        		+ "                        \"title\": \"🛂 Pasaporte\"\r\n"
        		+ "                    }\r\n"
        		+ "                }\r\n"
        		+ "            ]\r\n"
        		+ "        }\r\n"
        		+ "    }\r\n"
        		+ "}";

        sendHttpRequest(requestBody);
    }  
    
    public void sendBFRequerido(String to) {
    	String requestBody = "{\r\n"
    			+ "    \"messaging_product\": \"whatsapp\",\r\n"
    			+ "    \"recipient_type\": \"individual\",\r\n"
    			+ "    \"to\": \""+ to +"\",\r\n"
    			+ "    \"type\": \"interactive\",\r\n"
    			+ "    \"interactive\": {\r\n"
    			+ "        \"type\": \"button\",\r\n"
    			+ "        \"body\": {\r\n"
    			+ "            \"text\": \"🤗 A continuación elige si deseas factura o boleta para tu reserva. 👇\"\r\n"
    			+ "        },\r\n"
    			+ "        \"action\": {\r\n"
    			+ "            \"buttons\": [\r\n"
    			+ "                {\r\n"
    			+ "                    \"type\": \"reply\",\r\n"
    			+ "                    \"reply\": {\r\n"
    			+ "                        \"id\": \"boleta\",\r\n"
    			+ "                        \"title\": \"🧾 Boleta\"\r\n"
    			+ "                    }\r\n"
    			+ "                },\r\n"
    			+ "                {\r\n"
    			+ "                    \"type\": \"reply\",\r\n"
    			+ "                    \"reply\": {\r\n"
    			+ "                        \"id\": \"factura\",\r\n"
    			+ "                        \"title\": \"📇 Factura\"\r\n"
    			+ "                    }\r\n"
    			+ "                }\r\n"
    			+ "            ]\r\n"
    			+ "        }\r\n"
    			+ "    }\r\n"
    			+ "}";

        sendHttpRequest(requestBody);
    }
    
    public void sendDocTypeDocEmiBo(String to) {
    	String requestBody = "{\r\n"
    			+ "    \"messaging_product\": \"whatsapp\",\r\n"
    			+ "    \"recipient_type\": \"individual\",\r\n"
    			+ "    \"to\": \""+ to +"\",\r\n"
    			+ "    \"type\": \"interactive\",\r\n"
    			+ "    \"interactive\": {\r\n"
    			+ "        \"type\": \"list\",\r\n"
    			+ "        \"header\": {\r\n"
    			+ "            \"type\": \"text\",\r\n"
    			+ "            \"text\": \"¡Elección de documento! 🆔\"\r\n"
    			+ "        },\r\n"
    			+ "        \"body\": {\r\n"
    			+ "            \"text\": \"Para realizar la boleta a otro nombre se requiere de un tipo de documento para su emisión, gracias.😊\\n\\n\"\r\n"
    			+ "        },\r\n"
    			+ "        \"footer\": {\r\n"
    			+ "            \"text\": \"Elige una opción para continuar 👇\"\r\n"
    			+ "        },\r\n"
    			+ "        \"action\": {\r\n"
    			+ "            \"button\": \"Tipos de Documento\",\r\n"
    			+ "            \"sections\": [\r\n"
    			+ "                {\r\n"
    			+ "                    \"title\": \"🆔 Documentos\",\r\n"
    			+ "                    \"rows\": [\r\n"
    			+ "                        {\r\n"
    			+ "                            \"id\": \"DNI\",\r\n"
    			+ "                            \"title\": \"👨‍💼 D.N.I\",\r\n"
    			+ "                            \"description\": \"Emitir documento de emisión con D.N.I.\"\r\n"
    			+ "                        },\r\n"
    			+ "                        {\r\n"
    			+ "                            \"id\": \"CE\",\r\n"
    			+ "                            \"title\": \"🆔 Carnet de Extranjería\",\r\n"
    			+ "                            \"description\": \"Emitir documento de emisión con C.E.\"\r\n"
    			+ "                        },\r\n"
    			+ "                        {\r\n"
    			+ "                            \"id\": \"PASAPORTE\",\r\n"
    			+ "                            \"title\": \"🛂 Pasaporte\",\r\n"
    			+ "                            \"description\": \"Emitir documento de emisión con Pasaporte\"\r\n"
    			+ "                        },\r\n"
    			+ "                        {\r\n"
    			+ "                            \"id\": \"RUC\",\r\n"
    			+ "                            \"title\": \"🚹 R.U.C.\",\r\n"
    			+ "                            \"description\": \"Emitir documento de emisión con R.U.C.\"\r\n"
    			+ "                        },\r\n"
    			+ "                    ]\r\n"
    			+ "                },\r\n"
    			+ "            ]\r\n"
    			+ "        }\r\n"
    			+ "    }\r\n"
    			+ "}";

        sendHttpRequest(requestBody);
    }

    public void sendDetallesBFAndOtroUsuario(String to, Map<String, Object> mapConversacion) {
    	boolean otroUsuario = Boolean.valueOf(mapConversacion.get("otroCliente").toString());
    	String nroDocumento = "";
    	String nombreCompleto = "";
    	if(otroUsuario) {
    		nroDocumento = mapConversacion.get("nroDocCliente").toString();
    		nombreCompleto = mapConversacion.get("clienteNombre").toString();
    	}else {
    		Integer codCliente = Integer.valueOf(mapConversacion.get("codPersona").toString());
    		mapConversacion.put("codCliente", codCliente);
    		mapConversacion.put("tipoDocCliente", Integer.valueOf(mapConversacion.get("tipoDocumento").toString()));
    		nroDocumento = mapConversacion.get("nroDocumento").toString();    		
    		nombreCompleto = mapConversacion.get("perNombre1").toString() + " " + obtenerPrimeraLetra(mapConversacion.get("perNombre2").toString()) + " " + mapConversacion.get("perApePaterno").toString() + " " + obtenerPrimeraLetra(mapConversacion.get("perApeMaterno").toString());
    	}
    	String requestBody = "{\r\n"
    			+ "    \"messaging_product\": \"whatsapp\",\r\n"
    			+ "    \"recipient_type\": \"individual\",\r\n"
    			+ "    \"to\": \""+ to +"\",\r\n"
    			+ "    \"type\": \"interactive\",\r\n"
    			+ "    \"interactive\": {\r\n"
    			+ "        \"type\": \"button\",\r\n"
    			+ "        \"body\": {\r\n"
    			+ "            \"text\": \"La "+ mapConversacion.get("tipoDocEmision").toString() +" saldrá con los siguientes datos: \\n\\n *Número de Documento: " + nroDocumento + "*\\n *Cliente: " + nombreCompleto + "*\\n\\n Si la "+ mapConversacion.get("tipoDocEmision").toString() +" será para otro cliente, presiona el botón *Otro Cliente*. 👇\"\r\n"
    			+ "        },\r\n"
    			+ "        \"action\": {\r\n"
    			+ "            \"buttons\": [\r\n"
    			+ "                {\r\n"
    			+ "                    \"type\": \"reply\",\r\n"
    			+ "                    \"reply\": {\r\n"
    			+ "                        \"id\": \"otro_cliente\",\r\n"
    			+ "                        \"title\": \"🤵 Otro Cliente\"\r\n"
    			+ "                    }\r\n"
    			+ "                },\r\n"
    			+ "                {\r\n"
    			+ "                    \"type\": \"reply\",\r\n"
    			+ "                    \"reply\": {\r\n"
    			+ "                        \"id\": \"continuar_pago\",\r\n"
    			+ "                        \"title\": \"➡️ Continuar\"\r\n"
    			+ "                    }\r\n"
    			+ "                }\r\n"	
    			+ "            ]\r\n"
    			+ "        }\r\n"
    			+ "    }\r\n"
    			+ "}";
    	
        sendHttpRequest(requestBody);
    }
    
    public void sendFormatoNuevoCliente(String to, Map<String, Object> mapConversacion) {
    	mapConversacion.put("modulo", "registrar_otro_cliente");
    	sendMessage(to, "💁 Envía el *número de documento* de la persona/empresa para asignar la boleta/factura.");
    }
       
    /*public void sendHttpRequest(String requestBody) {
        try {
            URL url = new URL("https://graph.facebook.com/v20.0/" + GlobalConstants.PHONE_NUMBER_ID + "/messages");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + GlobalConstants.API_ACCESS_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(requestBody.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Mensaje enviado con éxito");
            } else {
            	System.out.println(requestBody);
                System.out.println("Error al enviar mensaje: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
    
    public String sendHttpRequest(String requestBody) {
        HttpURLConnection conn = null;
        String result = null;
        try {
            URL url = new URL("https://graph.facebook.com/v20.0/" + GlobalConstants.PHONE_NUMBER_ID + "/messages");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + GlobalConstants.API_ACCESS_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // Siempre usa UTF-8 explícito
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input);
                os.flush();
            }

            int responseCode = conn.getResponseCode();

            InputStream is = (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) ?
                    conn.getInputStream() : conn.getErrorStream();

            // Leer la respuesta
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
            }

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                System.out.println("✅ Mensaje enviado con éxito");
                result = response.toString();
            } else {
                System.out.println("❌ Error al enviar mensaje: " + responseCode);
                System.out.println("🔍 Response: " + response.toString());
                result = response.toString();
            }

        } catch (Exception e) {
            System.err.println("🚨 Excepción al enviar mensaje:");
            e.printStackTrace();
            result = e.getMessage();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            
        }
        return result;
    }
	
    public void sendMessageFinalizar(String to,String mensaje) {
		String requestBody = "{\r\n"
				+ "    \"messaging_product\": \"whatsapp\",\r\n"
				+ "    \"recipient_type\": \"individual\",\r\n"
				+ "    \"to\": \""+to+"\",\r\n"
				+ "    \"type\": \"interactive\",\r\n"
				+ "    \"interactive\": {\r\n"
				+ "        \"type\": \"button\",\r\n"
				+ "        \"body\": {\r\n"
				+ "            \"text\": \""+ mensaje +"\"\r\n"
				+ "        },\r\n"
				+ "        \"action\": {\r\n"
				+ "            \"buttons\": [\r\n"
				+ "                {\r\n"
				+ "                    \"type\": \"reply\",\r\n"
				+ "                    \"reply\": {\r\n"
				+ "                        \"id\": \"terminar_sesion\",\r\n"
				+ "                        \"title\": \"🔚 Finalizar\"\r\n"
				+ "                    }\r\n"
				+ "                }\r\n"
				+ "            ]\r\n"
				+ "        }\r\n"
				+ "    }\r\n"
				+ "}";
		sendHttpRequest(requestBody);
    }
    
    public void sendMessageSoporte(String to,String mensaje) {
		String requestBody = "{\r\n"
				+ "    \"messaging_product\": \"whatsapp\",\r\n"
				+ "    \"recipient_type\": \"individual\",\r\n"
				+ "    \"to\": \""+to+"\",\r\n"
				+ "    \"type\": \"interactive\",\r\n"
				+ "    \"interactive\": {\r\n"
				+ "        \"type\": \"button\",\r\n"
				+ "        \"body\": {\r\n"
				+ "            \"text\": \""+ mensaje +"\"\r\n"
				+ "        },\r\n"
				+ "        \"action\": {\r\n"
				+ "            \"buttons\": [\r\n"
				+ "                {\r\n"
				+ "                    \"type\": \"reply\",\r\n"
				+ "                    \"reply\": {\r\n"
				+ "                        \"id\": \"contactar_soporte\",\r\n"
				+ "                        \"title\": \"🧑‍💻 A. Cliente\"\r\n"
				+ "                    }\r\n"
				+ "                }\r\n"
				+ "            ]\r\n"
				+ "        }\r\n"
				+ "    }\r\n"
				+ "}";
		sendHttpRequest(requestBody);
    }
    
    public void sendMessageContinuar(String to,String mensaje) {
		String requestBody = "{\r\n"
				+ "    \"messaging_product\": \"whatsapp\",\r\n"
				+ "    \"recipient_type\": \"individual\",\r\n"
				+ "    \"to\": \""+to+"\",\r\n"
				+ "    \"type\": \"interactive\",\r\n"
				+ "    \"interactive\": {\r\n"
				+ "        \"type\": \"button\",\r\n"
				+ "        \"body\": {\r\n"
				+ "            \"text\": \""+ mensaje +"\"\r\n"
				+ "        },\r\n"
				+ "        \"action\": {\r\n"
				+ "            \"buttons\": [\r\n"
				+ "                {\r\n"
				+ "                    \"type\": \"reply\",\r\n"
				+ "                    \"reply\": {\r\n"
				+ "                        \"id\": \"retornar\",\r\n"
				+ "                        \"title\": \"🔁 Atrás\"\r\n"
				+ "                    }\r\n"
				+ "                },\r\n"
				+ "                {\r\n"
				+ "                    \"type\": \"reply\",\r\n"
				+ "                    \"reply\": {\r\n"
				+ "                        \"id\": \"contactar_soporte\",\r\n"
				+ "                        \"title\": \"🧑‍💻 Ayuda\"\r\n"
				+ "                    }\r\n"
				+ "                },\r\n"
				+ "                {\r\n"
				+ "                    \"type\": \"reply\",\r\n"
				+ "                    \"reply\": {\r\n"
				+ "                        \"id\": \"terminar_sesion\",\r\n"
				+ "                        \"title\": \"🔚 Finalizar\"\r\n"
				+ "                    }\r\n"
				+ "                }\r\n"
				+ "            ]\r\n"
				+ "        }\r\n"
				+ "    }\r\n"
				+ "}";
		sendHttpRequest(requestBody);
    }

	public int eliminarVentaWsp (String idCita, String token, String to){
		try {
			
			String response = "";
			HttpResponse responseData = null;
			JsonReader jsonReader = null;
			JsonObject jsonObject = null;  	       
			JsonArray jsonArray = null;
	        System.out.println("TOKEN: " + token + " | IDCITA: " + idCita);
	        Map<String, String> headers = new HashMap<String, String>();
	        headers.put("Authorization", GlobalConstants.API_DEFAULT_TOKEN_RESERVAS);
	        headers.put("Content-Type", "application/json; charset=utf-8");
	        String inputJson = String.format("{\"solId\":%s}", idCita);
	        responseData  = HttpRequestUtil.sendRequest("POST",GlobalConstants.API_ANULAR_PRE_RESERVA, inputJson, headers);
	        response = responseData.getResponseBody();
	        System.out.println("RESPONSE DATA ELIMINAR PRE RESERVA: " + responseData.toString() + " | FULL API: " + GlobalConstants.API_ANULAR_PRE_RESERVA);
	        if(response.equals(1)) {	       
	        	System.out.println("CITA ELIMINADA EXISTOSAMENTE.");
	        	sendSessionExpiredMessage(to);
	        	return 1;
	        }
	        if(response.equals(2)) {
	        	sendMessage(to, "¡Pago realizado con éxito! Le informamos que su boleta de venta será enviada a su correo electrónico dentro de las dos horas posteriores a la realización de su trámite. Le recomendamos revisar también la bandeja de spam o correo no deseado.\\n"
	        			+ "En caso no la reciba dentro de ese plazo, puede comunicarse con nosotros para brindarle el apoyo correspondiente. Muchas gracias.");
	        	return 1;
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		sendSessionExpiredMessage(to); 
		return 0;
	}
    
    public static String generarSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: Algoritmo de hash no encontrado.", e);
        }
    }
    
    private static boolean validarDocumento(String tipo, String numero) {
        switch (tipo) {
            case "DNI":
                return numero.matches("\\d{8}"); // DNI debe tener 8 dígitos
            case "RUC":
                return numero.matches("\\d{11}"); // RUC debe tener 11 dígitos
            case "CE":
            case "PASAPORTE":
                return numero.matches("\\d+"); // CE o PAS solo deben ser números (sin longitud específica)
            default:
                return false;
        }
    } 
     
    /* AUTENTICACION Y FUNCIONES PARA CADENAS, FECHAS, ETC. */
    
	
    public Map<String, Object> wspAutenticacion(String nroDoc, int tipoDoc, int appClient, String numPersona){
		Map<String, Object> mapResponse = null;
		try {
			mapResponse = new HashMap<String, Object>();
  	    	HttpResponse responseData = null;
  	        JsonReader jsonReader = null;
  	        JsonObject jsonObject = null;
			Map<String, Object> mapPersonaDetail = new HashMap<String, Object>(); 
  	        String apPaterno = "";
  	        String apMaterno = "";
  	        String noPersona = "";
  	        String feNacimiento = "";
  	        String deSexo = "";
  	        String genero = "";
  	        String[] nombres = null;
  	        String nombre1 = "";
  	        String nombre2 = "";  	      
  	        String pais = "";
  	        String origen = "AQP";
  	        String provincia = "";
  	        String celular = "";
  	        String departamento = "";
  	        
  	        
  		    String admision = "29288258";
  		    String nroDocumento = "20162580672";
  		    String inputJson = "";
  		    
  		    inputJson = "{\"documento\": \""+ nroDoc +"\", \"appCliId\": 3}";
  	        
  	    	try {
  	        	Map<String, String> headers = new HashMap<String, String>();
  	        	headers.put("Content-Type", "application/json; charset=utf-8");
  	        	headers.put("Authorization", GlobalConstants.API_DEFAULT_TOKEN);
  	        	responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_LOGIN_CLINICA, inputJson, headers);
  	        }
  	    	catch(Exception ex) {
  	    		mapResponse.put("excepcion", "Ha ocurrido un error al ingresar con el número de documento.");
  	    		return mapResponse;
  	    	}
  		    
  	    	System.out.println("RESPONSE CODE: " + responseData.getStatusCode());
  	    	System.out.println("ERROR MESSAGE: " + responseData.getErrorMessage());
  	    	System.out.println("RESPONSE LOGIN: " + responseData.getResponseBody() + ", FULL API: " + GlobalConstants.API_LOGIN_CLINICA);
  	    	
  	    	
  	        jsonReader = Json.createReader(new StringReader(responseData.getResponseBody()));
  	        jsonObject = jsonReader.readObject();
	  	    String message = jsonObject.containsKey("message")
	  	    	    ? jsonObject.getString("message")
	  	    	    : jsonObject.containsKey("descripcion")
	  	    	        ? jsonObject.getString("descripcion")
	  	    	        : "Mensaje no disponible";
  	        if(message.contains("exitosa.")) {
  	        	JsonObject jsonInfoPersona = jsonObject.getJsonObject("personaInfo");
  	        	mapPersonaDetail.put("perCod", jsonInfoPersona.getInt("persona"));
  	        	nombres = extraerNombres(jsonInfoPersona.getString("nombres"));
  	  	        nombre1 = nombres[0];
  	  	        nombre2 = nombres[1];  
  	  	        mapPersonaDetail.put("perNombre1", nombre1);
  	  	    	mapPersonaDetail.put("perNombre2", nombre2);
  	  	    	mapPersonaDetail.put("perApePaterno", jsonInfoPersona.getString("apellidopaterno"));
  	        	mapPersonaDetail.put("perApeMaterno", jsonInfoPersona.getString("apellidomaterno"));
  	        	mapPersonaDetail.put("nroDocumento", jsonInfoPersona.getString("documento"));
  	        	mapResponse.put("token", jsonObject.getString("token"));
  	        	String correo  = jsonInfoPersona.isNull("correoelectronico") ? "" : jsonInfoPersona.getString("correoelectronico");
  	        	mapResponse.put("correo", correo);
  	        	mapResponse.put("personaInfo", mapPersonaDetail);
  	        	mapResponse.put("modo_inicio", "login");  	 
  	        	String codigoPersona = String.valueOf(jsonInfoPersona.getInt("persona"));
  	        	String generoPersona = null;
  	        	String fechaNac = null;

  	        	if (jsonInfoPersona.containsKey("genero")
  	        	        && !jsonInfoPersona.isNull("genero")) {
  	        	    generoPersona = jsonInfoPersona.getString("genero").trim();
  	        	}

  	        	if (jsonInfoPersona.containsKey("nacimiento")
  	        	        && !jsonInfoPersona.isNull("nacimiento")) {
  	        	    fechaNac = jsonInfoPersona.getString("nacimiento").trim();
  	        	}

  	        	if (generoPersona == null || generoPersona.isEmpty()
  	        	        || fechaNac == null || fechaNac.isEmpty()) {
  	        	    Map<String, Object> actualizarDatos =
  	        	            updatePersonaDates(nroDoc, tipoDoc, codigoPersona);
  	        	    if((boolean) actualizarDatos.get("success")) {
  	        	    	System.out.println("Se han actualizado los campos asignados.");
  	        	    }else {
  	        	    	System.out.println("No se pudo actualizar los campos genero y fecha de nacimiento.");
  	        	    }
  	        	}	  	        
	        		return mapResponse;
  	        }
  	        else if (message.contains("No se ha encontrado ha ninguna persona con") || message.contains("La persona no esta registrada como paciente con historia")) {  	        	
  	        	inputJson = "{\"doConsultante\":\""+ admision  +"\",\"idInstitucion\":\""+nroDocumento+"\",\"nuDocumento\":\""+ nroDoc +"\",\"tiDocumento\":\""+ String.valueOf(tipoDoc).replace("4", "2").replace("7", "3")+"\"}";	
  	        	try {
  	        		Map<String, String> headers = new HashMap<String, String>();
  	        		headers.put("Content-Type", "application/json; charset=utf-8");
  	        		headers.put("Authorization", "1tFTQwISn4c=");
  	        		responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_SITEDS_CONSULTA_DOCUMENTO, inputJson, headers);
  	        	}
  	        	catch(Exception ex) {
  	        		mapResponse.put("excepcion", "No se pudo obtener los datos de la persona.");
  	        		return mapResponse;
  	        	}
  	        	jsonReader = Json.createReader(new StringReader(responseData.getResponseBody()));
  	        	jsonObject = jsonReader.readObject();
  	        	String coError = jsonObject.getString("coError");
  	        	if(coError.equals("1002")) {
  	        		mapResponse.put("excepcion", "No se han encontrado datos para este documento, revisa los datos correctamente y vuelve a intentarlo.");
  	        		return mapResponse;
  	        	}
  	        	apPaterno = jsonObject.getString("apPaterno");
  	        	apMaterno = jsonObject.isNull("apMaterno") ? "" : jsonObject.getString("apMaterno");
  	        	noPersona = jsonObject.getString("noPersona");
  	        	feNacimiento = jsonObject.getString("feNacimiento");
  	        	deSexo = jsonObject.getString("deSexo");
  	        	genero = deSexo.equals("2") ? "F" : "M";
  	        	nombres = extraerNombres(noPersona);
  	        	nombre1 = nombres[0];
  	        	nombre2 = nombres[1];   	
  	        	departamento = jsonObject.getString("coDepartamento");
  	        	provincia = jsonObject.getString("coProvincia");
  	        	pais = jsonObject.getString("coPaisEmisor");
  	        	String fechaFormateada = feNacimiento.substring(0, 4) + "-" +
  	        			feNacimiento.substring(4, 6) + "-" +
  	        			feNacimiento.substring(6);
  	        	//String tipoDocumento = tipoDoc == 1 ? "D" : tipoDoc == 2 ? "X" : "P";
  	        	inputJson = String.format("{\"apellidomaterno\":\"%s\",\"apellidopaterno\":\"%s\",\"celular\":\"%s\",\"correoelectronico\":\"%s\",\"departamento\":\"%s\",\"documento\":\"%s\",\"fechanacimiento\":\"%s\",\"nombres\":\"%s\",\"origen\":\"%s\",\"pais\":\"%s\",\"persona\":null,\"provincia\":\"%s\",\"sexo\":\"%s\",\"tipodocumento\":%s}", 
  	        			apMaterno, apPaterno, extraerNumeroTelefonico(numPersona), "", departamento, nroDoc, fechaFormateada, noPersona, origen, pais, provincia, genero, tipoDoc);
  	        	
  	        	try {
  	  	  	    	System.out.println("JSON REGISTRO PERSONA: " + inputJson);
  	  	        	Map<String, String> headers = new HashMap<String, String>();
  	  	        	headers.put("Content-Type", "application/json; charset=utf-8");
  	  	        	headers.put("Authorization", GlobalConstants.API_DEFAULT_TOKEN);
  	  	        	responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_REGISTRO_PERSONA, inputJson, headers);
  	        	}
  	        	catch(Exception ex) 
  	        	{
  	        		ex.printStackTrace();
  	        		mapResponse.put("excepcion", "No se pudo registrar a la persona, intentalo de nuevo más tarde, o comunicate con un encargado.");
  	        		return mapResponse;
  	        	}
  	        	
  	        	System.out.println("JSON RESPONSE PERSONA CREAD: " + responseData.getResponseBody() + " CON CODIGO: " + responseData.getStatusCode());
  	        	
  	  	        jsonReader = Json.createReader(new StringReader(responseData.getResponseBody()));
  	  	        JsonObject jsonObjectPer = jsonReader.readObject();
  	  	        jsonObject = jsonObjectPer.getJsonObject("personaInfo");
  	        	mapPersonaDetail.put("perCod", jsonObject.getInt("persona"));
  	        	nombres = extraerNombres(jsonObject.getString("nombres"));
  	  	        nombre1 = nombres[0];
  	  	        nombre2 = nombres[1];  
  	  	        mapPersonaDetail.put("perNombre1", nombre1);
  	  	    	mapPersonaDetail.put("perNombre2", nombre2);
  	  	    	mapPersonaDetail.put("perApePaterno", jsonObject.getString("apellidopaterno"));
  	        	mapPersonaDetail.put("perApeMaterno", jsonObject.getString("apellidomaterno"));
  	        	mapPersonaDetail.put("nroDocumento", jsonObject.getString("documento"));
  	        	mapResponse.put("personaInfo", mapPersonaDetail);
  	        	String correo  = jsonObject.isNull("correoelectronico") ? "" : jsonObject.getString("correoelectronico");
  	        	mapResponse.put("correo", correo);
  	        	mapResponse.put("modo_inicio", "registro");  	
  	        	
  	        	inputJson = "{\"documento\": \""+ nroDoc +"\", \"appCliId\": 3}";
  	        	
  	  	    	try {
  	  	    	System.out.println("JSON LOGIN: " + inputJson);
  	  	        	Map<String, String> headers = new HashMap<String, String>();
  	  	        	headers.put("Content-Type", "application/json; charset=utf-8");
  	  	        	headers.put("Authorization", GlobalConstants.API_DEFAULT_TOKEN);
  	  	        	//responseData  = RsSiteds.sendPostRequest(GlobalConstants.API_LOGIN_CLINICA, inputJson, headers);
  	  	        	responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_LOGIN_CLINICA, inputJson, headers);
  	  	        }
  	  	    	catch(Exception ex) {
  	  	    		mapResponse.put("excepcion", "Ha ocurrido un error al ingresar con el número de documento.");
  	  	    		return mapResponse;
  	  	    	}
  	  	    	
  	        	System.out.println("JSON RESPONSE LOGIN: " + responseData.getResponseBody() + " CON CODIGO: " + responseData.getStatusCode());

  	  	    	
  	  	        jsonReader = Json.createReader(new StringReader(responseData.getResponseBody()));
  	  	        jsonObject = jsonReader.readObject();
  	  	        
  	  	        mapResponse.put("token", jsonObject.getString("token"));
  	        	return mapResponse;
  	        	
  	        }
  	        mapResponse.put("excepcion", "Ha ocurrido un obtener al conectarse a nuestros registros, contacta con un encargado.");
	  	    return mapResponse;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
    }

    public Map<String, Object> updatePersonaDates(String nroDoc, Integer tipoDoc, String codigoPersona) {
    	Map<String, Object> mapResponse = new HashMap<String, Object>();
    	try {
    	    HttpResponse responseData = null;
      	    JsonReader jsonReader = null;
      	    JsonObject jsonObject = null;
    		String admision = "29288258";
    		String nroDocumento = "20162580672";
    		String inputJson = "";
          	inputJson = "{\"doConsultante\":\""+ admision  +"\",\"idInstitucion\":\""+nroDocumento+"\",\"nuDocumento\":\""+ nroDoc +"\",\"tiDocumento\":\""+ String.valueOf(tipoDoc).replace("4", "2").replace("7", "3")+"\"}";	
          	try {
          		Map<String, String> headers = new HashMap<String, String>();
          		headers.put("Content-Type", "application/json; charset=utf-8");
          		headers.put("Authorization", "1tFTQwISn4c=");
          		responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_SITEDS_CONSULTA_DOCUMENTO, inputJson, headers);
          	}
          	catch(Exception ex) {
          		return mapResponse;
          	}
          	jsonReader = Json.createReader(new StringReader(responseData.getResponseBody()));
          	jsonObject = jsonReader.readObject();
          	String coError = jsonObject.getString("coError");
          	if(coError.equals("1002")) {
          		mapResponse.put("excepcion", "No se han encontrado datos para este documento, revisa los datos correctamente y vuelve a intentarlo.");
          		mapResponse.put("success", false);
          		return mapResponse;
          	}
          	
          	String apPaterno = jsonObject.getString("apPaterno");
	        String apMaterno = jsonObject.isNull("apMaterno") ? "" : jsonObject.getString("apMaterno");
	        String noPersona = jsonObject.getString("noPersona");
	        String feNacimiento = jsonObject.getString("feNacimiento");
	        String deSexo = jsonObject.getString("deSexo");
	        String genero = deSexo.equals("2") ? "F" : "M";
	        String fechaFormateada = feNacimiento.substring(0, 4) + "-" +
  	        			feNacimiento.substring(4, 6) + "-" +
  	        			feNacimiento.substring(6);
	        
          	inputJson = "{\"codigoPersona\":\""+ codigoPersona  +"\",\"genero\":\""+ genero  +"\",\"nacimiento\":\""+fechaFormateada+"\"}";	
          	try {
          		Map<String, String> headers = new HashMap<String, String>();
          		headers.put("Content-Type", "application/json; charset=utf-8");
          		headers.put("Authorization", GlobalConstants.API_DEFAULT_TOKEN);
          		responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_ACTUALIZAR_DATOS, inputJson, headers);
          	}
          	catch(Exception ex) {
          		return mapResponse;
          	}
          	jsonReader = Json.createReader(new StringReader(responseData.getResponseBody()));
          	jsonObject = jsonReader.readObject();
          	Boolean success = jsonObject.containsKey("success") ? jsonObject.getBoolean("success") : false;
          	if(success) {
      	        mapResponse.put("success", true);
    	  	    return mapResponse;
          	}else {
      	        mapResponse.put("success", false);
    	  	    return mapResponse;
          	}
          	
		} catch (Exception e) {
			e.printStackTrace();
  	        mapResponse.put("excepcion", "Ha ocurrido un obtener al conectarse a nuestros registros, contacta con un encargado.");
  	        mapResponse.put("success", false);
	  	    return mapResponse;
		}
    }
    
    
    public String extraerNumeroTelefonico(String numero) {
    	return numero.startsWith("51") && numero.length() > 2 ? numero.substring(2) : "Número no válido";
    }
    
    public String[] extraerNombres(String noPersona) {
        String[] partes = noPersona.trim().split("\\s+");
        String nombre1 = partes.length > 0 ? partes[0] : "";
        String nombre2 = partes.length > 1 ? partes[1] : "";
        return new String[]{nombre1, nombre2};
    }
    
    public String obtenerPrimeraLetra(String noPersona) {
        if (noPersona == null || noPersona.trim().isEmpty()) {
            return "";
        }
        return noPersona.substring(0, 1) + ".";
    }
    
    public String formaterDatePMAM(String hora) {
    	String horaNueva = UtilAppString.completeStringLeft(hora, 5, "0");
    	LocalTime time = LocalTime.parse(horaNueva, DateTimeFormatter.ofPattern("HH:mm"));
    	String sufijo = time.getHour() < 12 ? "a.m." : "p.m.";
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    	String horaConSufijo = time.format(formatter) + " " + sufijo;
    	return horaConSufijo;
    }
    
    public String formaterTimeAQP(String horario) {
    	 LocalTime hora = LocalTime.parse(horario);
         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
         String horaFormateada = hora.format(formatter);
         return horaFormateada;
    }
    
    public String formaterDate(String dateString) {
    	LocalDate date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    	String day = String.valueOf(date.getDayOfMonth());
    	String month = date.getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es"));
    	String year = String.valueOf(date.getYear());
    	String formattedDate = String.format("%s de %s del %s", day, month, year);	
    	return formattedDate;
    }
    
    public String capitalizeFirstLetter(String str) {
        String[] words = str.split(" ");
        StringBuilder capitalizedText = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                capitalizedText.append(Character.toUpperCase(word.charAt(0)))
                               .append(word.substring(1).toLowerCase())
                               .append(" ");
            }
        }
        return capitalizedText.toString().trim();
    }
    
    public String getNombreMes(int mes) {
        String[] nombresMeses = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        };
        return nombresMeses[mes - 1];
    }
    
    public String formatearFechaCita(String fecha) {
        SimpleDateFormat formatoEntrada = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatoSalida = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date fechaDate = formatoEntrada.parse(fecha);
            return formatoSalida.format(fechaDate);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
   /* public String obtenerCodigoPreventa() throws Exception {
      	//String inputJson = "{\"doConsultante\":\""+ admision  +"\",\"idInstitucion\":\""+nroDocumento+"\",\"nuDocumento\":\""+ nroDoc +"\",\"tiDocumento\":\""+ String.valueOf(tipoDoc).replace("4", "2").replace("7", "3")+"\"}";	
	    HttpResponse responseData = null;
  	    JsonReader jsonReader = null;
  	    JsonObject jsonObject = null;
    	try {
      		Map<String, String> headers = new HashMap<String, String>();
      		headers.put("Content-Type", "application/json; charset=utf-8");
      		headers.put("Authorization", GlobalConstants.API_DEFAULT_TOKEN_RESERVAS);
      		responseData  = HttpRequestUtil.sendRequest("GET", GlobalConstants.API_OBTENER_PREVENTA, null, headers);
      		return responseData.getResponseBody();
      	}
      	catch(Exception ex) {
      		System.out.println("No se pudo obtener los datos de la persona.");
      		return null;
      	}
    }*/
    
    public String encrypt(String data) {
    	try {
    		SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY_ENCRYPT.getBytes(), TYPE_ENCRYPT);
    		Cipher cipher = Cipher.getInstance(ALGORITHM);
    		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    		
    		byte[] encryptedData = cipher.doFinal(data.getBytes("UTF-8"));
    		return Base64.getEncoder().encodeToString(encryptedData);			
		} catch (Exception e) {
			return data;
		}
    }

    public String decrypt(String encryptedData)  {
    	try {			
    		SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY_ENCRYPT.getBytes(), TYPE_ENCRYPT);
    		Cipher cipher = Cipher.getInstance(ALGORITHM);
    		cipher.init(Cipher.DECRYPT_MODE, secretKey);
    		
    		byte[] decodedData = Base64.getDecoder().decode(encryptedData);
    		byte[] originalData = cipher.doFinal(decodedData);
    		
    		return new String(originalData, "UTF-8");
		} catch (Exception e) {
			return encryptedData;
		}
    }
    
    
    public static String jsonInput(Map<String, Object> mapData) {
    	com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
    	Gson gson = new Gson();
        for (Map.Entry<String, Object> entry : mapData.entrySet()) {
            Object value = entry.getValue();

            if (value instanceof String) {
                String strVal = (String) value;

                // Si parece ser un JSON válido lo parseamos
                if ((strVal.trim().startsWith("{") && strVal.trim().endsWith("}")) ||
                    (strVal.trim().startsWith("[") && strVal.trim().endsWith("]"))) {
                    try {
                        JsonElement jsonElement = JsonParser.parseString(strVal);
                        jsonObject.add(entry.getKey(), jsonElement);
                        continue;
                    } catch (Exception e) {
                        // no era JSON real, se guarda como string normal
                    }
                }

                jsonObject.addProperty(entry.getKey(), strVal);

            } else {
                // para Map anidado u otros objetos
                JsonElement jsonElement = gson.toJsonTree(value);
                jsonObject.add(entry.getKey(), jsonElement);
            }
        }

        return gson.toJson(jsonObject);
    }
	  
    /*public String jsonInput(Map<String, Object> mapData) {
        StringBuilder jsonBuilder = new StringBuilder("{");

        for (Map.Entry<String, Object> entry : mapData.entrySet()) {
            jsonBuilder.append("\"")
                       .append(entry.getKey())
                       .append("\":");

            if (entry.getValue() instanceof String) {
                jsonBuilder.append("\"")
                           .append(entry.getValue())
                           .append("\"");
            } else if (entry.getValue() instanceof Map) {
                jsonBuilder.append(mapToJson((Map<String, Object>) entry.getValue()));
            } else {
                jsonBuilder.append(entry.getValue());
            }

            jsonBuilder.append(",");
        }

        // Eliminar la última coma
        if (jsonBuilder.charAt(jsonBuilder.length() - 1) == ',') {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
        }

        jsonBuilder.append("}");

        return jsonBuilder.toString();
    }*/
    
    public static String mapToJson(Map<String, Object> map) {
        StringBuilder jsonBuilder = new StringBuilder("{");

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            jsonBuilder.append("\"")
                       .append(entry.getKey())
                       .append("\":");

            if (entry.getValue() instanceof String) {
                jsonBuilder.append("\"")
                           .append(entry.getValue())
                           .append("\"");
            } else if (entry.getValue() instanceof Map) {
                jsonBuilder.append(mapToJson((Map<String, Object>) entry.getValue()));
            } else if (entry.getValue() instanceof List) {
                jsonBuilder.append(listToJson((List<Object>) entry.getValue()));
            } else {
                jsonBuilder.append(entry.getValue());
            }

            jsonBuilder.append(",");
        }

        if (jsonBuilder.charAt(jsonBuilder.length() - 1) == ',') {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
        }

        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }
    
    public static String listToJson(List<Object> list) {
        StringBuilder jsonBuilder = new StringBuilder("[");

        for (Object item : list) {
            if (item instanceof String) {
                jsonBuilder.append("\"")
                           .append(item)
                           .append("\"");
            } else if (item instanceof Map) {
                jsonBuilder.append(mapToJson((Map<String, Object>) item));
            } else {
                jsonBuilder.append(item);
            }
            jsonBuilder.append(",");
        }

        if (jsonBuilder.charAt(jsonBuilder.length() - 1) == ',') {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
        }

        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    }
    
	private static final Integer COD_APP_CITAS = 3;
    
}

