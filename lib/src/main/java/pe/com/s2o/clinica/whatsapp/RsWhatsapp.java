package pe.com.s2o.clinica.whatsapp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.gson.Gson;
import com.luxor.modulos.web.util.dev.UtilAppDate;
import com.luxor.modulos.web.util.dev.UtilAppEncrypt;
import com.luxor.modulos.web.util.dev.UtilAppString;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
/*import pe.com.s2o.clinica.citas.rs.RequestData;
import pe.com.s2o.clinica.citas.rs.config.ConfigRsCitasV1;
import pe.com.s2o.clinica.citas.util.SqlUtilData;*/
import pe.com.s2o.clinica.ws.RsWs;
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
    private static final long SESSION_TIMEOUT_MINUTES = 10 * 60;   
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	public static Integer codOrg = 1;
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
                    	//String body = message.getJsonObject("text").getString("body");                        	    
                    	System.out.println("---------------------INICIO BODY CONTENIDO-------------------");
                        /*RsWs wsInstance = new RsWs();
                        wsInstance.handleIncomingWhatsAppMessage(from, body);*/
                    	bodyContentMessage(type, message, from);
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
    
    public static void main(String[] args) {
    	
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
        boolean conversationEnded;
        
        ConversationSession(Map<String, Object> state) {
            this.state = state;
            this.lastActivity = Instant.now();
            this.conversationEnded = false;
        }

        void endConversation() {
            this.conversationEnded = true; // Marcar que la conversación terminó
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
    
    public void bodyContentMessage(String type, JsonObject message, String from) {
    	ConversationSession session = getUserSessionFrom(from);
    	try {
    		 Map<String, Object> conversationState = session.state;
            System.out.println("Estado antes de procesar mensaje: " + conversationState);  
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
                	case "encargado":
                		proccessHablarEncargado(from, userMessage, conversationState);
                		break;
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
            }else if (type.equals("interactive")) {
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
                        	/*if(conversationState.get("codeVenta") != null) {
                        		String codeVenta = conversationState.get("codeVenta").toString();
                        		eliminarVentaWsp(codeVenta);
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
                    	if(conversationState.get("codeVenta") != null) {
                    		String codeVenta = conversationState.get("codeVenta").toString();
                    		//eliminarVentaWsp(codeVenta);
                    	}
                    	sendTerminoDeSesion(from, session);
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
                    case "contactar_soporte":
	                	if(handleEmptyConversationIsFull(from, conversationState)) {
	                		conversationState.put("userDocumentType","");
	                		proccessEncargado(from, buttonId, conversationState);
	                	}
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
    
    private void processRegDocumento(String from, JsonObject interactive ,Map<String, Object> conversationState) {
    	try {
            String tipoDocumento = interactive.getJsonObject("button_reply").getString("title").toUpperCase();
            conversationState.put("userDocumentType", tipoDocumento);
            String mensaje = "*¡Perfecto!* 🎉\\n\\n"
                    + "Ahora, por favor, *ingresa tu número de " + tipoDocumento + "* o el *Número DNI* de la persona  que deseas generar la cita para continuar.\\n\\n"
                    + "🔢 Asegúrate de que sea el número correcto y completo.\\n\\n"
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
            int opcion = Integer.parseInt(userMessage) - 1;
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
                    InDocumento = 2;
                } else {
                    sendMessage(from, "❌ Longitud incorrecta para " + tipoDocumento + ". Por favor, ingresa el número correcto.");
                }
                
                if (InDocumento != 0) {
                    String mensaje = "*Estamos validando tus datos...* 🔍\\r\\n"
                            + "\\r\\n"
                            + "Por favor, espera un momento mientras procesamos la información. ⏳\\r\\n"
                            + "\\r\\n"
                            + "Te notificaremos en breve. ¡Gracias por tu paciencia!";
                    sendMessage(from, mensaje);
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
                    		} else {
                    		    conversationState.put("modulo", "actualizar_correo");
                    		    sendMessage(from, "¡No encontramos un *correo electrónico* asociado a tu cuenta! Por favor, ingresa uno para continuar.");
                    		}
                    	}
                    }
                    catch(Exception ex) {
                    	ex.printStackTrace();
                    	sendMessage(from, "❌ ¡El documento ingresado no se encuentra registrado en el servicio o no se pudo consultar la información, contactate con el area de soporte!");
                    }
                }
            } else {
                sendMessage(from, "❌ ¡Ingresa correctamente tu número de documento!");
            }
        } else {
            sendMessage(from, "❌ Por favor, selecciona primero el tipo de documento.");
        }
    }
    
    private void proccessEncargado(String from, String userMessage, Map<String, Object> conversationState) {
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
    }
    
    private void proccessHablarEncargado(String from, String userMessage, Map<String, Object> conversationState) {
    	try {			
    		RsWs wsInstance = new RsWs();
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
    			return;
    		}
    		wsInstance.handleIncomingWhatsAppMessage(from, userMessage);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private void proccessActualizarCorreo(String from, String userMessage, Map<String, Object> conversationState) {
    	try {
    		HttpResponse responseData = null;
    		JsonReader jsonReader = null;
    		JsonObject jsonObject = null;  	        
    		String token = (String) conversationState.get("token");
    		String nombrePersona = (String) conversationState.get("nombrePersona");
    		Integer idPersona = Integer.valueOf(conversationState.get("codPersona").toString());
  	        String inputJson = String.format("{\"email\":\"%s\",\"idPersona\":%s}", userMessage, idPersona);
	        Map<String, String> headers = new HashMap<String, String>();
	        headers.put("Content-Type", "application/json; charset=utf-8");
	        headers.put("Authorization", "Bearer " + token);
	        responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_ACTUALIZAR_EMAIL, inputJson, headers);
	        String response = responseData.getResponseBody();
	        if(response.contains("Se ha actualizado el correo exitosamente.")) {
		        conversationState.put("modulo", "elegir_modulo");
		        String nombre = capitalizeFirstLetter(nombrePersona);
		        sendButtonsModulos(from, nombre);
	        }
	        else {
	        	sendMessage(from, "¡No se ha podido registrar el correo electronico, intentalo nuevamente más tarde!");
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
		Map<String, Object> mapReserva = (Map<String, Object>) mapDatos.get("preReserva");
		Map<String, Object> mapReservaDatos = (Map<String, Object>) mapReserva.get("datosReserva");
		if(codPlan.intValue() != 1) {			
			Map<String, Object> informacionSeguro = (Map<String, Object>) mapDatos.get("informacionObtenida");
			Map<String, Object> informacionPersona = (Map<String, Object>) mapDatos.get("informacionFormateada");
			String nuAutorizacion = "";
			try {
				String inputJson = String.format("{\"codIafa\":\"%s\",\"informacionSeguro\":%s,\"informacionPersonaSeguro\":%s}", codIafa, informacionSeguro, informacionPersona);
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("Content-Type", "application/json; charset=utf-8");
				System.out.println("INPUT JSON AUTORIZACION WSP: " + inputJson);
				responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_BASE_CLINICA_BOT + "/rs/siteds/v1/autorizacionSiteds", inputJson, headers);
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
		}
		
		
    	String token = mapDatos.get("token").toString();
    	long unixTimeSeconds = Instant.now().getEpochSecond();   
    	//String jsonPago = (String) mapDatos.get("jsonReserva");
    	String jsonPago = jsonInput(mapReserva);
    	String link =  GlobalConstants.API_PAGO_NIUBIZ + "?paymentInfo="+ URLEncoder.encode(encrypt(jsonPago)) + "&sessionToken=" + token + "&currentUnixTime=" + unixTimeSeconds;
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
    			+ "            \"text\": \"🤗 Se ha generado el siguiente link de pago: \\n\\n 👉 "+ link +"\\n\\nRealiza el pago y serás reedirgido para ver el estado de tu reserva.\"\r\n"
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
		Map<String, Object> informacionSeguro = (Map<String, Object>) mapDatos.get("informacionObtenida");
		Map<String, Object> informacionPersona = (Map<String, Object>) mapDatos.get("informacionFormateada");
		Map<String, Object> mapReserva = (Map<String, Object>) mapDatos.get("preReserva");
		Map<String, Object> mapReservaDatos = (Map<String, Object>) mapReserva.get("datosReserva");
		String codIafa = String.valueOf(seguro.get("codIafa").toString());
		String nuAutorizacion = "";
		try {
			String inputJson = String.format("{\"codIafa\":\"%s\",\"informacionSeguro\":%s,\"informacionPersonaSeguro\":%s}", codIafa, informacionSeguro, informacionPersona);
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "application/json; charset=utf-8");
			System.out.println("INPUT JSON AUTORIZACION WSP: " + inputJson);
			//responseData  = HttpRequestUtil.sendRequest("POST", END_POINT_URL + "/rs/siteds/v1/autorizacionSiteds", inputJson, headers);
	        responseData = RsSiteds.sendPostRequest(GlobalConstants.API_BASE_CLINICA_BOT + "/rs/siteds/v1/autorizacionSiteds", inputJson, Map.of(
                    "Content-Type", "application/json; charset=utf-8"
                ));
			response = responseData.getResponseBody();
			if(response.contains("\"nroAutorizacion\":null")) {
				sendMessageFinalizar(to, "No se pudo obtener el codigo de autorización, comunicate con un administrador.");
				return;
			}
			jsonReader = Json.createReader(new StringReader(response));
			jsonObject = jsonReader.readObject();
			
			nuAutorizacion = jsonObject.getString("nroAutorizacion");
		} catch (Exception e) {
			e.printStackTrace();
			sendMessageFinalizar(to, "Hubo un error al obtener la autorización del seguro.");
			return;
		}
		
		mapReservaDatos.put("autorizacionsiteds", nuAutorizacion);
		mapReservaDatos.put("objsiteds", mapDatos.get("informacionObtenida"));
		mapReservaDatos.put("objsitedsobservacion", mapDatos.get("informacionFormateada"));
		mapReserva.put("datosReserva", mapReservaDatos);
		String jsonPago = jsonInput(mapReserva);
		System.out.println("INPUT JSON RESERVA PAGO COMPLETO: " + jsonPago);
		try {
	        Map<String, String> headers = new HashMap<String, String>();
	        headers.put("Content-Type", "application/json; charset=utf-8");
	        headers.put("Authorization", "Bearer " + token);	        
	        responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_REGISTRAR_CITA_SEGURO, jsonPago, headers);
	        response = responseData.getResponseBody();
			
		} catch (Exception e) {
			sendMessageFinalizar(to, "😓 No se ha podido obtener el precio de la consulta para esta especialidad, intentalo nuevamente.");
			return;
		} 
		
	  	 if(response.contains("success\":true")) {
	  		 sendMessage(to, "📝 ¡Cita registrada con éxito! 🎉\\r\\n"
	  		 		+ "Te esperamos en la fecha y hora acordadas. ⏰👨‍⚕️\\r\\n"
	  		 		+ "Si necesitas realizar algún cambio, no dudes en contactarnos. 📞");
	  		 
	    		Map<String, Object> especialidad = (Map<String, Object>) mapDatos.get("especialidad");
	    		Map<String, Object> especialidadHorario = (Map<String, Object>) mapDatos.get("especialidad_horario");
	    		Map<String, Object> especialidadFecha = (Map<String, Object>) mapDatos.get("especialidad_fecha");
	    		
	    		double precioIGV = 0;
	    		String nombrePersona = mapDatos.get("perNombre1").toString() + " "
	    				+ obtenerPrimeraLetra(mapDatos.get("perNombre2").toString()) + " "
	    				+ mapDatos.get("perApePaterno").toString() + " " + obtenerPrimeraLetra(mapDatos.get("perApeMaterno").toString());
	    		String nombreProfesional = especialidadFecha.get("profNombre").toString().replace("\"", "");;
	    		String nroDocumento = mapDatos.get("nroDocumento").toString().replace("\"", "");;
	    		String especialidadHorarioElegido = especialidadHorario.get("Hora").toString().replace("\"", "");
	    		String turnoPosible = especialidadFecha.get("turno").toString().replace("\"", "");;
	    		String especialidadFechaElegido = especialidadFecha.get("fechaDisponible").toString().replace("Z", "");
	    		especialidadFechaElegido = especialidadFechaElegido.replace("\"", "");
	    		String especialidadElegida = especialidad.get("descripcion").toString().replace("\"", "");
	    		String seguroElegido = seguro.get("descripcionSeguro").toString().replace("\"", "");
	  		 
	  		 
	    		String informacionCita = "😄 ¡Genial, *"+ capitalizeFirstLetter(nombrePersona) +"*, estas a un paso de registrar tu cita!\\r\\n"
	    				+ "\\r\\n"
	    				+ "🩺 Especialidad: *"+ capitalizeFirstLetter(especialidadElegida) +"*\\r\\n"
	    				+ "📅 Fecha: *"+ formaterDate(especialidadFechaElegido) +"*\\r\\n"
	    				+ "🕒 Hora: *"+ formaterDatePMAM(especialidadHorarioElegido) +"*\\r\\n"
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
	  	 }	 
	  	userConversationState.remove(to);
	  	sesion.endConversation();
    }

    public void sendTerminoDeSesion(String to, ConversationSession sesion) {
    	sendMessage(to, "💬 ¡Gracias por usar nuestro servicio! 🙌\\r\\n"
    			+ "Esperamos haber sido de ayuda. Si necesitas algo más, no dudes en escribirnos. ¡Te esperamos pronto! 😊\\r\\n"
    			+ "🌟 ¡Que tengas un excelente día! 🌟\\n\\n Clínica Arequipa tu clínica 💙");
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
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    

    
    public void sendInformacionCita(String to, String token, Map<String, Object> mapDatos) {
		try {		
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
    		Integer codPlan = Integer.valueOf(seguro.get("codAseguradora").toString());
    		//Integer codEsp = Integer.valueOf(especialidad.get("codEsp").toString());
    		String nombrePersona = mapDatos.get("perNombre1").toString() + " "
    				+ obtenerPrimeraLetra(mapDatos.get("perNombre2").toString()) + " "
    				+ mapDatos.get("perApePaterno").toString() + " " + obtenerPrimeraLetra(mapDatos.get("perApeMaterno").toString());
    		String nombreProfesional = especialidadFecha.get("profNombre").toString().replace("\"", "");;
    		String nroDocumento = mapDatos.get("nroDocumento").toString().replace("\"", "");;
    		String especialidadHorarioElegido = especialidadHorario.get("Hora").toString().replace("\"", "");
    		String turnoPosible = especialidadFecha.get("turno").toString().replace("\"", "");;
    		String especialidadFechaElegido = especialidadFecha.get("fechaDisponible").toString().replace("Z", "");
    		especialidadFechaElegido = especialidadFechaElegido.replace("\"", "");
    		String especialidadElegida = especialidad.get("descripcion").toString().replace("\"", "");
    		String seguroElegido = seguro.get("descripcionSeguro").toString().replace("\"", "");;
    		String codHorario = String.valueOf(especialidadFecha.get("codHora").toString());
    		String nombreCompleto = mapDatos.get("perNombre1").toString() + " " + mapDatos.get("perNombre2").toString();
    		String perApePaterno = mapDatos.get("perApePaterno").toString();
    		String perApeMaterno = mapDatos.get("perApeMaterno").toString();
    		String tipoDocumento = mapDatos.get("tipoDocumento").toString();
    		Integer codPersona = Integer.valueOf(mapDatos.get("codPersona").toString());
    		
    		Integer codAseguradora = Integer.valueOf(seguro.get("idEmprestadoraSeguro").toString());
    		String fechaDisponible = String.valueOf(especialidadFecha.get("fechaDisponible").toString());
        	String fechaFormateada = fechaDisponible + "T" + especialidadHorarioElegido.replace("\"", "") + ":00";
        	Double duracion = Double.valueOf(especialidad.get("tiempoPromedioAtencion").toString());
        	Integer codPaciente = Integer.valueOf(mapDatos.get("codPersona").toString());
        	Integer codProfesional = Integer.valueOf(especialidadFecha.get("codProf").toString());
        	Integer codEspecialidad = Integer.valueOf(especialidad.get("idEspecialidad").toString());
        	Integer tipoPaciente = Integer.valueOf(seguro.get("tipoPaciente").toString());
        	String codIafa = String.valueOf(seguro.get("codIafa").toString());
        	String correo = (String) mapDatos.get("correo");
        	
    		try {
    	        Map<String, String> headers = new HashMap<String, String>();
    	        headers.put("Content-Type", "application/json; charset=utf-8");
    	        headers.put("Authorization", "Bearer " + token);
    	        String inputJson = String.format("{\"sucursal\":\"0001\",\"metodoConsulta\":\"CONS\",\"tipoPaciente\":3,\"idPaciente\":%s,\"idHorario\":\"%s\"}", codPersona, codHorario);
    	        responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_CONSULTA_PRECIO, inputJson, headers);	
    	        response = responseData.getResponseBody();
    			
    		} catch (Exception e) {
    			sendMessageFinalizar(to, "😓 No se ha podido obtener el precio de la consulta para esta especialidad, intentalo nuevamente.");
    			return;
    		}  		
    	
    		mapDatos.put("informacionObtenida", "");
    		mapDatos.put("informacionFormateada", "");
    		
    		jsonReader = Json.createReader(new StringReader(response));
    		jsonObject = jsonReader.readObject();
    		double precio = jsonObject.getJsonNumber("montoTotal").doubleValue();
    		precioIGV = precio;

        	Map<String, Object> mapPago = new HashMap<String, Object>();
        	Map<String, Object> mapDatosReserva = new HashMap<String, Object>();
        	mapDatosReserva.put("idhorario", codHorario);
        	mapDatosReserva.put("fechacita", fechaFormateada);
        	mapDatosReserva.put("duracionpromedio", duracion);
        	mapDatosReserva.put("duracionreal", duracion);
        	mapDatosReserva.put("idpaciente", codPaciente);
        	mapDatosReserva.put("idmedico", codProfesional);
        	mapDatosReserva.put("idespecialidad", codEspecialidad);
        	mapDatosReserva.put("email", correo);
        	mapDatosReserva.put("pertipoparentesco", "");
        	mapDatosReserva.put("sucursal","0001");
        	mapDatosReserva.put("esSiteds", tipoPaciente.equals(3) ? "N" : "F");
        	mapDatosReserva.put("metodoconsulta", "CONS");
        	mapDatosReserva.put("tipopaciente", tipoPaciente);
    		
    		if(codPlan.intValue() != 1) {
    			sendMessage(to, "📅✨ Por favor, espera un momento mientras obtenemos la información. 🕒😊");
        		try {
        	        Map<String, String> headers = new HashMap<String, String>();
        	        headers.put("Content-Type", "application/json; charset=utf-8");
        	        headers.put("Authorization", "Bearer " + token);
        	        String inputJson = String.format("{\"iafaAseguradora\":\"%s\",\"apPaterno\":\"%s\",\"apMaterno\":\"%s\",\"nombreCompleto\":\"%s\",\"tipoDocumento\":\"%s\",\"nroDocumento\":\"%s\"}", codIafa.replace("40007", "20001"), perApePaterno, perApeMaterno, nombreCompleto, tipoDocumento, nroDocumento);
        	        responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_BASE_CLINICA_BOT + "/rs/siteds/v1/obtenerDatosSitets", inputJson, headers);
        	        response = responseData.getResponseBody();
        			
        		} catch (Exception e) {
        			sendMessageFinalizar(to, "Su seguro no cuenta para hacer uso de *consulta ambulatoria* o no lo encontramos habilitado, intente con otra opción.");
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
        		
        		JsonReader jsonReaderSiteds = Json.createReader(new StringReader(response));
        		JsonObject jsonObjectSiteds = jsonReaderSiteds.readObject();
        		JsonObject jsonInfoObtenida = jsonObjectSiteds.getJsonObject("informacionObtenida");
        		JsonObject jsonInfoObtenidaFormateada = jsonObjectSiteds.getJsonObject("informacionFormateada");
        		mapDatos.put("informacionObtenida", jsonInfoObtenida);
        		mapDatos.put("informacionFormateada", jsonInfoObtenidaFormateada);
        		JsonArray detalleCoberturaArray = jsonInfoObtenida.getJsonArray("detalleCobertura");
        		JsonObject jsonInfoCobertura = detalleCoberturaArray.getJsonObject(0);      		
        		precioIGV = jsonInfoCobertura.getJsonNumber("deducible").doubleValue();
        		
        		
        		Map<String, Object> informacionSeguro = (Map<String, Object>) mapDatos.get("informacionObtenida");
        		List<Map<String, Object>> coberturas = (List<Map<String, Object>>) informacionSeguro.get("detalleCobertura");
        		Map<String, Object> cobertura4100 = coberturas.get(0);
        		String coPagoFijo = String.valueOf(cobertura4100.get("coPagoFijo").toString()).replace("\"", "");
        		String coPagoVariable = String.valueOf(cobertura4100.get("coPagoVariable").toString()).replace("\"", "");
        		String coProducto = String.valueOf(informacionSeguro.get("coProducto").toString()).replace("\"", "");
        		mapDatosReserva.put("idaseguradora", codAseguradora);
        		mapDatosReserva.put("codigoiafas", codIafa);
        		mapDatosReserva.put("copagofarmacia", "55");
        		mapDatosReserva.put("copagofijo", coPagoFijo);
        		mapDatosReserva.put("copagovariable", coPagoVariable);
        		mapDatosReserva.put("coproducto", coProducto);

    		}
    		mapDatos.put("tarifaConsulta", precioIGV);
    		String precioReserva = String.valueOf(precioIGV);
        	mapPago.put("amount", precioReserva);
        	mapPago.put("currency", "PEN");
        	mapPago.put("datosReserva", mapDatosReserva);
        	//String jsonPago = jsonInput(mapPago);
        	mapDatos.put("preReserva", mapPago);
    		String informacionCita = "😄 ¡Genial, *"+ capitalizeFirstLetter(nombrePersona) +"*, estas a un paso de registrar tu cita!\\r\\n"
    				+ "\\r\\n"
    				+ "🩺 Especialidad: *"+ capitalizeFirstLetter(especialidadElegida) +"*\\r\\n"
    				+ "📅 Fecha: *"+ formaterDate(especialidadFechaElegido) +"*\\r\\n"
    				+ "🕒 Hora: *"+ formaterDatePMAM(especialidadHorarioElegido) +"*\\r\\n"
    				+ "👨‍⚕️ Médico: *"+ capitalizeFirstLetter(nombreProfesional) +"*\\r\\n"
    				+ "💬 Tipo de Consulta: *Presencial*\\r\\n"
    				+ "🆔 Documento: *"+ nroDocumento +"*\\r\\n"
    				+ "🔢 Turno: *"+ turnoPosible +"*\\r\\n"
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
	        headers.put("Authorization", "Bearer " + token);
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
	
        
        String mensaje = "";
        String siConvenio = "";
        String noConvenio = "";
        int flag = 1;

        siConvenio = "*1.* Particular 🏥\\n";

        // Crear una nueva instancia de mapSeguro y seguroOpciones para el primer seguro
        Map<String, Object> mapSeguro = new HashMap<>();
        mapSeguro.put("codIafa", 1);
        mapSeguro.put("descripcionSeguro", "Particular");
        mapSeguro.put("codAseguradora", 1);
        mapSeguro.put("tipoPaciente", 3);
        mapSeguro.put("idCodigo", 1);
        mapSeguro.put("idEmprestadoraSeguro", "1");

        Map<String, Object> seguroOpciones = new HashMap<>();
        seguroOpciones.put(String.valueOf(flag), mapSeguro);
        lstSeguros.add(seguroOpciones);

        for (JsonObject afiliacion : filteredAfiliaciones) {	
        	boolean foundMatch = false;
    	    for (Iterator iterator = jsonArray.iterator(); iterator.hasNext();) {
    			JsonValue jsonValue = (JsonValue) iterator.next();
    			JsonObject jsonObjectAse = jsonValue.asJsonObject();
    			String codIafas = afiliacion.getString("coIafas");
    			System.out.println("Valor del json: " + jsonValue);
    			String codIafasSeguro = jsonObjectAse.getString("codigoIafas");
    			if(codIafas.equals(codIafasSeguro)) {
        			mapSeguro = new HashMap<String, Object>();
        			seguroOpciones= new HashMap<String, Object>();
        			siConvenio += "*" + (flag + 1) + ".* " + afiliacion.getString("deIafas") + " 🏥\\n";
        			mapSeguro.put("codIafa", codIafasSeguro.replace("40007", "20001"));
        			mapSeguro.put("descripcionSeguro", afiliacion.getString("deIafas"));
        			mapSeguro.put("codAseguradora", jsonObjectAse.getJsonNumber("codigo"));
        			mapSeguro.put("idEmprestadoraSeguro", jsonObjectAse.getString("idEmpresaAseguradora"));
        			mapSeguro.put("tipoPaciente", jsonObjectAse.getJsonNumber("tipoPaciente"));
        			mapSeguro.put("idCodigo", jsonObjectAse.getJsonNumber("idCodigo"));
        			seguroOpciones.put(String.valueOf((flag + 1)), mapSeguro);
        			lstSeguros.add(seguroOpciones);
        			flag++;
        			foundMatch = true;
        			//break;
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
        
        String requestBody = "{\r\n"
        		+ "    \"messaging_product\": \"whatsapp\",\r\n"
        		+ "    \"recipient_type\": \"individual\",\r\n"
        		+ "    \"to\": \""+ to +"\",\r\n"
        		+ "    \"type\": \"interactive\",\r\n"
        		+ "    \"interactive\": {\r\n"
        		+ "        \"type\": \"button\",\r\n"
        		+ "        \"body\": {\r\n"
        		+ "            \"text\": \"A continuación, te mostramos tu *lista de seguros afiliados a nuestra clínica*.\\n\\n"+ mensajeConvenios +"Por favor, elige el número correspondiente al seguro del cual dispongas, para continuar al ultimo paso. 😊\"\r\n"
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
    	        headers.put("Authorization", "Bearer " + token);
    	        responseData  = HttpRequestUtil.sendRequest("GET", GlobalConstants.API_ESPECIALIDADES, null, headers);
    	        response = responseData.getResponseBody();
    			
    		} catch (Exception e) {
    			sendMessageFinalizar(to, "😓 No se ha podido obtener las especialidades en este momento, intentalo nuevamente mas tarde.");
    			return;
    		}
    		jsonReader = Json.createReader(new StringReader(response));
    		jsonObject = jsonReader.readObject();
    		jsonArray = jsonObject.getJsonArray("data");
    		
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
    			especialidad += "*" + (i + 1) + ".*" + " " + capitalizeFirstLetter(jsonObject.getString("descripcion")) + "\\n";
    		    especialidadBuilder.append("*").append(i + 1).append(".* ")
                .append(capitalizeFirstLetter(jsonObject.getString("descripcion")))
                .append("\\n");
    			mapEspecialidad.put(String.valueOf((i + 1)), jsonObject);
    			lstEspecialidades.add(mapEspecialidad);
    		    if (count == 15 || i == jsonArray.size() - 1) {
    		    	sendMessage(to, especialidadBuilder.toString());
    		        especialidadBuilder.setLength(0);
    		        count = 0;
    		    }
    		}
    		
    		mapConversation.put("lstEspecialidades", lstEspecialidades);
    		sendMessageContinuar(to, "Elige alguna de nuestras especialidades para registrar una cita. 😊");
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
    		String fecha = "";
    		Integer codMedico = Integer.valueOf(especialidadFechas.get("codProf").toString());
    		String codHorario = String.valueOf(especialidadFechas.get("codHora").toString());
    		fecha = especialidadFechas.get("fechaDisponible").toString().replace("Z", "");
    		fecha = fecha.replace("\"", "");
    		LocalDate fechaActual = LocalDate.parse(fecha);
    		
    		int dia = fechaActual.getDayOfMonth();
    		int mes = fechaActual.getMonthValue();
    		int anio = fechaActual.getYear();
    		System.out.println("Día: " + dia);
    		System.out.println("Mes: " + mes);
    		System.out.println("Año: " + anio);
            String mesFormateado = String.format("%02d", mes);
            String diaFormateado = String.format("%02d", dia);
    		try {
    			String inputJson = String.format("{\"idespecialidad\":\"\",\"medico\":%s,\"anio\":\"%s\",\"mes\":\"%s\",\"dia\":\"%s\",\"idHorario\":\"%s\"}", codMedico, anio, mesFormateado, diaFormateado, codHorario);
    	        System.out.println("JSON HORARIOS: " + inputJson);
    			Map<String, String> headers = new HashMap<String, String>();
    	        headers.put("Content-Type", "application/json; charset=utf-8");
    	        headers.put("Authorization", "Bearer " + token);
    	        responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_HORARIOS_DET_ESPECIALIDAD, inputJson, headers);
    	        response = responseData.getResponseBody();
    	        System.out.println("RESPONSE HORARIOS: " + inputJson);
    			
    		} catch (Exception e) {
    			sendMessageFinalizar(to, "Ha ocurrido un error al intentar obtener las horas disponibles  del medicos, intentalo nuevamente volviendo al paso anterior.");
    			return;
    		}
    		
    		if(response.contains("El medico no tiene horas disponibles para ese dia")) 
    		{
    			sendMessageContinuar(to, "El medico no tiene horas disponibles para ese día.");
    			return;
    		}
    		
    		jsonReader = Json.createReader(new StringReader(response));
    		jsonObject = jsonReader.readObject();
    		jsonArray = jsonObject.getJsonArray("dias");
    		String horarios = "";
    		int contadorDisponibles = 0;
    		int flag = 0;
    		for (int i = 0; i < jsonArray.size(); i++) {
    			Map<String, Object> mapHorariosPadre = new HashMap<String, Object>();
    		    JsonObject jsonObjectPrincipal = jsonArray.getJsonObject(i);
    		    if (jsonObjectPrincipal.getBoolean("Disponible")) {
    		    	horarios += "*" + (flag + 1) + ".* " + formaterDatePMAM(jsonObjectPrincipal.getString("Hora")) + "\\n";
    		    	mapHorariosPadre.put(String.valueOf((flag + 1)), jsonObjectPrincipal);
    		    	lstEspecialisadesHorarios.add(mapHorariosPadre);
    		    	flag++;
    		        contadorDisponibles++;
    		        if (contadorDisponibles >= 5) {
    		            break;
    		        }
    		    }
    		}
    		
    		if(lstEspecialisadesHorarios.size() == 0) {
        		sendMessageContinuar(to, "😓 La fecha seleccionado no cuenta con horarios disponibles para este día, puedes volver al paso anterior para poder elegir otro.");
        		return;
    		}   		
    		mapDatos.put("lstHorarios", lstEspecialisadesHorarios);
    		sendMessage(to, horarios);  	
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
            System.out.println("Día: " + dia);
            System.out.println("Mes: " + mes);
            System.out.println("Año: " + anio);
            
    		Map<String, Object> especialidad = (Map<String, Object>) mapDatos.get("especialidad");
    		Integer especialidadId = Integer.valueOf(especialidad.get("idEspecialidad").toString());
    		String especialidadDesc = String.valueOf(especialidad.get("descripcion"));
    		List<Map<String, Object>> lstEspecialidadesFechas = new ArrayList<>();
    		try {
    	        Map<String, String> headers = new HashMap<String, String>();
    	        headers.put("Content-Type", "application/json; charset=utf-8");
    	        headers.put("Authorization", "Bearer " + token);
    	        String inputJson = String.format("{\"idEspecialidad\":%s,\"anio\":\"%s\",\"mes\":\"%s\",\"dia\":%s,\"sucursal\":\"\"}", especialidadId, anio, mes, dia);
    	        responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_HORARIOS_ESPECIALIDAD, inputJson, headers);
    	        response = responseData.getResponseBody();
    			
    		} catch (Exception e) {
    			sendMessageFinalizar(to, "😓 No se ha podido obtener los horarios para esta especialidad en este momento, intentalo nuevamente mas tarde.");
    			return;
    		}
    		
    		if(response.contains("No existe horario para esta Especialidad")) {
    			sendMessageFinalizar(to, "No hay medicos con horarios disponibles para esta especialidad, finaliza e intenta con otra especialidad.");
    			return;
    		}
    		
    		jsonReader = Json.createReader(new StringReader(response));
    		jsonArray = jsonReader.readArray();
    		
    		if(jsonArray.size() == 0) {
    			sendMessageContinuar(to, "😓 La especialidad seleccionada no cuenta con horarios disponibles por el momento.");
        		return;
    		}
    		
    		
    		String especialidadFecha = "";
    		Map<String, Object> mapHorariosPadre = new HashMap<String, Object>();
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
    			+ "                            \"title\": \"👩‍⚕️ Reservar Cita\",\r\n"
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
    			+ "                            \"title\": \"🔍 Consulta de Horarios\",\r\n"
    			+ "                            \"description\": \"Consultar información de horarios disponibles.\"\r\n"
    			+ "                        },\r\n"
    			+ "                    ]\r\n"
    			+ "                },\r\n"
    			+ "                {\r\n"
    			+ "                    \"title\": \"🧑‍💻 Soporte\",\r\n"
    			+ "                    \"rows\": [\r\n"
    			+ "                        {\r\n"
    			+ "                            \"id\": \"contactar_soporte\",\r\n"
    			+ "                            \"title\": \"📞 Otras Consultas\",\r\n"
    			+ "                            \"description\": \"Podrás comunicarte con un encargado para consultar tus dudas.\"\r\n"
    			+ "                        }\r\n"
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
        		+ "            \"text\": \"¡Bienvenido a Clínica Arequipa! 👋\"\r\n"
        		+ "        },\r\n"
        		+ "        \"body\": {\r\n"
        		+ "            \"text\": \"¡Hola, soy Melany! tu asistente virtual 🤗\\nPara continuar, debes aceptar los términos y condiciones del uso de nuestro servicio.\\n\\nhttps://clinicarequipa.com.pe/politicas\\n\\n¿Deseas continuar? \"\r\n"
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
       
    public void sendHttpRequest(String requestBody) {
        try {
            URL url = new URL("https://graph.facebook.com/v20.0/" + GlobalConstants.PHONE_NUMBER_ID + "/messages");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + GlobalConstants.API_ACCESS_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");
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
  		    
  		    inputJson = "{\"documento\":\""+ nroDoc +"\"}";
  	        
  	    	try {
  	        	Map<String, String> headers = new HashMap<String, String>();
  	        	headers.put("Content-Type", "application/json; charset=utf-8");
  	        	responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_LOGIN_CLINICA, inputJson, headers);
  	        }
  	    	catch(Exception ex) {
  	    		mapResponse.put("excepcion", "Ha ocurrido un error al ingresar con el número de documento.");
  	    		return mapResponse;
  	    	}
  		    
  	    	
  	    	
  	        jsonReader = Json.createReader(new StringReader(responseData.getResponseBody()));
  	        jsonObject = jsonReader.readObject();
  	        String message  = jsonObject.getString("message");
  	        if(message.contains("Autenticación Exitosa")) {
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
  	        	return mapResponse;
  	        }
  	        else if (message.contains("Número de documento inexistente")) {  	        	
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
  	        	String tipoDocumento = tipoDoc == 1 ? "D" : tipoDoc == 2 ? "X" : "P";
  	        	inputJson = String.format("{\"apellidomaterno\":\"%s\",\"apellidopaterno\":\"%s\",\"celular\":\"%s\",\"correoelectronico\":\"%s\",\"departamento\":\"%s\",\"documento\":\"%s\",\"fechanacimiento\":\"%s\",\"nombres\":\"%s\",\"origen\":\"%s\",\"pais\":\"%s\",\"persona\":null,\"provincia\":\"%s\",\"sexo\":\"%s\",\"tipodocumento\":\"%s\"}", 
  	        			apPaterno, apMaterno, extraerNumeroTelefonico(numPersona), "", departamento, nroDoc, fechaFormateada, noPersona, origen, pais, provincia, genero, tipoDocumento);
  	        	
  	        	try {
  	  	        	Map<String, String> headers = new HashMap<String, String>();
  	  	        	headers.put("Content-Type", "application/json; charset=utf-8");
  	  	        	responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_REGISTRO_PERSONA, inputJson, headers);
  	        	}
  	        	catch(Exception ex) 
  	        	{
  	        		mapResponse.put("excepcion", "No se pudo registrar a la persona, intentalo de nuevo más tarde, o comunicate con un encargado.");
  	        		return mapResponse;
  	        	}
  	        	
  	  	        jsonReader = Json.createReader(new StringReader(responseData.getResponseBody()));
  	  	        jsonObject = jsonReader.readObject();
  	  	        
  	        	mapPersonaDetail.put("perCod", jsonObject.getInt("persona"));
  	        	nombres = extraerNombres(jsonObject.getString("nombres"));
  	  	        nombre1 = nombres[0];
  	  	        nombre2 = nombres[1];  
  	  	        mapPersonaDetail.put("perNombre1", nombre1);
  	  	    	mapPersonaDetail.put("perNombre2", nombre2);
  	  	    	mapPersonaDetail.put("perApePaterno", jsonObject.getString("apellidopaterno"));
  	        	mapPersonaDetail.put("perApeMaterno", jsonObject.getString("apellidomaterno"));
  	        	mapPersonaDetail.put("nroDocumento", jsonObject.getString("documento"));
  	        	mapResponse.put("token", jsonObject.getString("token"));
  	        	mapResponse.put("personaInfo", mapPersonaDetail);
  	        	String correo  = jsonObject.isNull("correoelectronico") ? "" : jsonObject.getString("correoelectronico");
  	        	mapResponse.put("correo", correo);
  	        	mapResponse.put("modo_inicio", "registro");  	
  	        	
  	  		    inputJson = "{\"documento\":"+ nroDoc +"}";
  	  	        
  	  	    	try {
  	  	        	Map<String, String> headers = new HashMap<String, String>();
  	  	        	headers.put("Content-Type", "application/json; charset=utf-8");
  	  	        	responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.API_LOGIN_CLINICA, inputJson, headers);
  	  	        }
  	  	    	catch(Exception ex) {
  	  	    		mapResponse.put("excepcion", "Ha ocurrido un error al ingresar con el número de documento.");
  	  	    		return mapResponse;
  	  	    	}
  	  	    	
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
	  
    public String jsonInput(Map<String, Object> mapData) {
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
    }
    
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
    
	private static final Integer COD_APP_CITAS = 2;
    
}

