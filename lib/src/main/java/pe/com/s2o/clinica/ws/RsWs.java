package pe.com.s2o.clinica.ws;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import pe.com.s2o.clinica.whatsapp.GlobalConstants;


@ServerEndpoint(value = "/chat", configurator = FilterWsConfig.class)
public class RsWs {
    // Mapa para almacenar las sesiones por número de teléfono
    private static final Map<String, List<String>> messageHistory = new ConcurrentHashMap<>();
    private static final Set<Session> clients = new CopyOnWriteArraySet<>();
    private static final Map<Session, String> activeConversations = new ConcurrentHashMap<>();
    
    private static final Set<String> closedConversations = new CopyOnWriteArraySet<>();

    private static final TokenValidator tokenValidator = new TokenValidator();
    
    public void markConversationClosed(String phoneNumber) {
        closedConversations.add(phoneNumber);
        messageHistory.remove(phoneNumber);
    }

    public boolean isConversationClosed(String phoneNumber) {
        return closedConversations.contains(phoneNumber);
    }

    public void clearConversationClosed(String phoneNumber) {
        closedConversations.remove(phoneNumber);
    }
    
    @OnOpen
    public void onOpen(Session session) {
        clients.add(session);
        sendActiveConversationsList(session);
        System.out.println("Conexión abierta con el cliente: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {

        	
        	if (message.startsWith("IMAGE:")) {
        	    String[] parts = message.split(":", 3);
        	    if (parts.length == 3) {
        	        String to = parts[1].trim();
        	        String imageUrl = parts[2].trim();
        	        saveMessage(to, "SENT_IMAGE: " + imageUrl);
        	        sendImageToWhatsApp(to, imageUrl);
        	    }
        	    return;
        	}
        	
            if (message.startsWith("CLOSE_CONVERSATION:")) {
                String phoneNumber = activeConversations.get(session);

                if (phoneNumber != null) {
                	sendEndedMessageToWhatsApp(phoneNumber);
                    markConversationClosed(phoneNumber);
                    activeConversations.remove(session);
                    session.getBasicRemote().sendText("CONVERSATION_CLOSED:" + phoneNumber);
                    for (Session client : clients) {
                        sendActiveConversationsList(client);
                    }
                }
                return;
            }
            
        	
            if (message.startsWith("SELECT:")) {
                String phoneNumber = message.substring(7);
                activeConversations.put(session, phoneNumber);
                sendConversationHistory(session, phoneNumber);
                return;
            }

            if (message.equals("GET_CONVERSATIONS")) {
                sendActiveConversationsList(session);
                return;
            }

            String[] parts = message.split(":", 2);
            if (parts.length == 2) {
                String to = parts[0].trim();
                String body = parts[1].trim();
                saveMessage(to, "SENT: " + body);
                sendMessageToWhatsApp(to, body);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Set<String> getClosedConversations() {
        return new HashSet<>(closedConversations);
    }
    
    public void handleIncomingWhatsAppMessage(String from, String message) {
    	
    	if(message.contains("NEW_SESSION:")) {
    		closedConversations.remove(from);
    		message = message.split(":")[1];
    	}
    	
        if (isConversationClosed(from)) {
            System.out.println("Conversación cerrada para: " + from);
            return;
        }
        
    	System.out.println("Recibiendo mensaje de WhatsApp - From: " + from + ", Message: " + message);
        
        saveMessage(from, "RECEIVED: " + message);
        System.out.println("Número de clientes conectados: " + clients.size());
        
        for (Session client : clients) {
            try {
                String activeConversation = activeConversations.get(client);
                System.out.println("Cliente " + client.getId() + " está viendo conversación: " + activeConversation);
                if (from.equals(activeConversation)) {
                    client.getBasicRemote().sendText("MESSAGE:" + from + ":" + message);
                }else {
                    client.getBasicRemote().sendText("NEW_MESSAGE:" + from + ":" + message);
                }
                sendActiveConversationsList(client);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }

    public static void saveMessage(String phoneNumber, String message) {
        messageHistory.computeIfAbsent(phoneNumber, k -> new ArrayList<>()).add(message);
    }

    private void sendConversationHistory(Session session, String phoneNumber) throws IOException {
        List<String> history = messageHistory.getOrDefault(phoneNumber, new ArrayList<>());
        session.getBasicRemote().sendText("HISTORY:" + phoneNumber + ":" + String.join("|", history));
    }

    private void sendActiveConversationsList(Session session) {
        try {
            String conversationsList = String.join(",", messageHistory.keySet());
            System.out.println(conversationsList);
            session.getBasicRemote().sendText("CONVERSATIONS:" + conversationsList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean obtenerClientesActivos() {
    	return !clients.isEmpty();
    	
    }
    

    @OnClose
    public void onClose(Session session) {
        clients.remove(session);
        activeConversations.remove(session);
        System.out.println("Conexión cerrada con el cliente: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

    private void sendMessageToWhatsApp(String to, String body) {
        String requestBody = "{\n" +
                "    \"messaging_product\": \"whatsapp\",\n" +
                "    \"recipient_type\": \"individual\",\n" +
                "    \"to\": \"" + to + "\",\n" +
                "    \"type\": \"text\",\n" +
                "    \"text\": {\n" +
                "        \"preview_url\": false,\n" +
                "        \"body\": \"" + body + "\"\n" +
                "    }\n" +
                "}";
        sendHttpRequest(requestBody);
    }

    public void sendImageToWhatsApp(String to, String imageUrl) {
        try {
            String apiUrl = "https://graph.facebook.com/v20.0/" + GlobalConstants.PHONE_NUMBER_ID + "/messages";

            String jsonPayload = "{"
                + "\"messaging_product\": \"whatsapp\","
                + "\"to\": \"" + to + "\","
                + "\"type\": \"image\","
                + "\"image\": {\"link\": \"" + imageUrl + "\"}"
                + "}";

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + GlobalConstants.API_ACCESS_TOKEN);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("WhatsApp Response Code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("✅ Imagen enviada correctamente a " + to);
            } else {
                System.out.println("❌ Error al enviar la imagen: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEndedMessageToWhatsApp(String to) {
		String requestBody = "{\r\n"
				+ "    \"messaging_product\": \"whatsapp\",\r\n"
				+ "    \"recipient_type\": \"individual\",\r\n"
				+ "    \"to\": \""+to+"\",\r\n"
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
				+ "                        \"title\": \"🔁 Retornar\"\r\n"
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
    
    private void sendHttpRequest(String requestBody) {
        try {
            URL url = new URL("https://graph.facebook.com/v20.0/" + GlobalConstants.PHONE_NUMBER_ID + "/messages");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + GlobalConstants.API_ACCESS_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes());
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            System.out.println(responseCode == HttpURLConnection.HTTP_OK ? 
                "Mensaje enviado con éxito" : 
                "Error al enviar mensaje: " + responseCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}