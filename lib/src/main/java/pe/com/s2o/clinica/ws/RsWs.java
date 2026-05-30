package pe.com.s2o.clinica.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import pe.com.s2o.clinica.utils.HttpRequestUtil;
import pe.com.s2o.clinica.utils.HttpRequestUtil.HttpResponse;
import pe.com.s2o.clinica.whatsapp.GlobalConstants;


@ServerEndpoint(value = "/chat", configurator = FilterWsConfig.class)
public class RsWs {
	
    private static final Set<Session> clients = new CopyOnWriteArraySet<>();
    
    @OnOpen
    public void onOpen(Session session) {
        clients.add(session);
        System.out.println("WebSocket conectado: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        clients.remove(session);
        System.out.println("WebSocket desconectado: " + session.getId());
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error en WebSocket: " + throwable.getMessage());
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Mensaje recibido del cliente: " + message);
        try (JsonReader jsonReader = Json.createReader(new StringReader(message))) {
            javax.json.JsonObject jsonObject = jsonReader.readObject();
            String to = jsonObject.getString("to", "");
            String body = jsonObject.getString("message", "");

            if (to.trim().isEmpty() || body.trim().isEmpty()) {
                session.getBasicRemote().sendText(Json.createObjectBuilder()
                    .add("direction", "error")
                    .add("message", "Debe enviar to y message")
                    .build()
                    .toString());
                return;
            }

            String messageId = sendToWhatsApp(to, body);
            if (messageId != null) {
                notifyMessageSent(to, body, String.valueOf(Instant.now().toEpochMilli()), messageId);
            } else {
                session.getBasicRemote().sendText(Json.createObjectBuilder()
                    .add("direction", "error")
                    .add("to", to)
                    .add("message", "No se pudo enviar el mensaje a WhatsApp")
                    .build()
                    .toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String sendToWhatsApp(String to, String message) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://graph.facebook.com/v20.0/" + GlobalConstants.PHONE_NUMBER_ID + "/messages");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + GlobalConstants.API_ACCESS_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            javax.json.JsonObject payload = Json.createObjectBuilder()
                .add("messaging_product", "whatsapp")
                .add("recipient_type", "individual")
                .add("to", to)
                .add("type", "text")
                .add("text", Json.createObjectBuilder()
                    .add("preview_url", false)
                    .add("body", message))
                .build();

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            InputStream is = responseCode < HttpURLConnection.HTTP_BAD_REQUEST
                ? conn.getInputStream()
                : conn.getErrorStream();

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            System.out.println("Respuesta Meta WhatsApp: " + response.toString());

            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
                return null;
            }

            try (JsonReader jsonReader = Json.createReader(new StringReader(response.toString()))) {
                javax.json.JsonObject jsonObject = jsonReader.readObject();
                JsonArray messages = jsonObject.getJsonArray("messages");
                if (messages != null && !messages.isEmpty()) {
                    return messages.getJsonObject(0).getString("id", null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return null;
    }
    
    public static void broadcastMessage(String from, String message, String type, String timestamp, String messageId) {
        broadcastMessage(from, message, type, timestamp, messageId, null);
    }

    public static void broadcastMessage(String from, String message, String type, String timestamp, String messageId, javax.json.JsonObject sourceMessage) {
        javax.json.JsonObjectBuilder jsonMessageBuilder = Json.createObjectBuilder()
            .add("from", from)
            .add("message", message)
            .add("type", type)
            .add("messageId", messageId)
            .add("timestamp", timestamp)
            .add("direction", "incoming"); // incoming = del usuario, outgoing = enviado por nosotros

        addSourceMetadata(jsonMessageBuilder, type, sourceMessage);

        String messageStr = jsonMessageBuilder.build().toString();

        synchronized(clients) {
            for (Session client : clients) {
                if (client.isOpen()) {
                    try {
                        client.getBasicRemote().sendText(messageStr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void addSourceMetadata(javax.json.JsonObjectBuilder jsonMessageBuilder, String type, javax.json.JsonObject sourceMessage) {
        if (sourceMessage == null) {
            return;
        }

        try {
            if ("image".equals(type) || "video".equals(type) || "audio".equals(type) || "document".equals(type) || "sticker".equals(type)) {
                javax.json.JsonObject media = sourceMessage.getJsonObject(type);
                if (media != null) {
                    addStringIfPresent(jsonMessageBuilder, "mediaId", media, "id");
                    addStringIfPresent(jsonMessageBuilder, "mimeType", media, "mime_type");
                    addStringIfPresent(jsonMessageBuilder, "caption", media, "caption");
                    addStringIfPresent(jsonMessageBuilder, "fileName", media, "filename");
                    addStringIfPresent(jsonMessageBuilder, "sha256", media, "sha256");
                    //addStringIfPresent(jsonMessageBuilder, "mediaUrl", media, "url");
                }
                return;
            }

            if ("location".equals(type)) {
                javax.json.JsonObject location = sourceMessage.getJsonObject("location");
                if (location != null) {
                    if (location.containsKey("latitude")) {
                        jsonMessageBuilder.add("latitude", location.getJsonNumber("latitude"));
                    }
                    if (location.containsKey("longitude")) {
                        jsonMessageBuilder.add("longitude", location.getJsonNumber("longitude"));
                    }
                    addStringIfPresent(jsonMessageBuilder, "address", location, "address");
                    addStringIfPresent(jsonMessageBuilder, "name", location, "name");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addStringIfPresent(javax.json.JsonObjectBuilder jsonMessageBuilder, String targetKey, javax.json.JsonObject source, String sourceKey) {
        if (source.containsKey(sourceKey) && !source.isNull(sourceKey)) {
            jsonMessageBuilder.add(targetKey, source.getString(sourceKey, ""));
        }
    }

    /*public static void broadcastMessage(String from, String message, String type, String timestamp, String messageId) {
        javax.json.JsonObject jsonMessage = Json.createObjectBuilder()
            .add("from", from)
            .add("message", message)
            .add("type", type)
            .add("messageId", messageId)
            .add("timestamp", timestamp)
            .add("direction", "incoming") // incoming = del usuario, outgoing = enviado por nosotros
            .build();
        
        String messageStr = jsonMessage.toString();
        
        synchronized(clients) {
            for (Session client : clients) {
                if (client.isOpen()) {
                    try {
                        client.getBasicRemote().sendText(messageStr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }*/
   
    
    public static void notifyMessageSent(String to, String message, String timestamp, String messageId) {
    	javax.json.JsonObject jsonMessage = Json.createObjectBuilder()
            .add("to", to)
            .add("message", message)
            .add("type", "text")
            .add("messageId", messageId)
            .add("timestamp", timestamp)
            .add("direction", "outgoing")
            .build();
        
        String messageStr = jsonMessage.toString();
        
        synchronized(clients) {
            for (Session client : clients) {
                if (client.isOpen()) {
                    try {
                        client.getBasicRemote().sendText(messageStr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    public boolean obtenerClientesActivos() {
    	return !clients.isEmpty();
    	
    }
}
