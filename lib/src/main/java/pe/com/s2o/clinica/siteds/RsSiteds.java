package pe.com.s2o.clinica.siteds;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.luxor.modulos.web.util.dev.UtilAppDate;

import pe.com.s2o.clinica.dtos.SitedsSolAutorizacionDetalleRes;
import pe.com.s2o.clinica.dtos.SitedsSolAutorizacionDto;
import pe.com.s2o.clinica.dtos.SitedsSolAutorizacionExeCarDto;
import pe.com.s2o.clinica.dtos.SitedsSolAutorizacionProEspDto;
import pe.com.s2o.clinica.dtos.SitedsSolAutorizacionRestricDto;
import pe.com.s2o.clinica.dtos.SitedsSolAutorizacionTieEspDto;
import pe.com.s2o.clinica.utils.HttpRequestUtil;
import pe.com.s2o.clinica.utils.HttpRequestUtil.HttpResponse;
import pe.com.s2o.clinica.whatsapp.GlobalConstants;
import pe.com.s2o.util.meta.SqlUtil;
import pe.com.s2o.util.rs.client.Mapo;
import pe.com.s2o.util.rs.client.UtilResponse;

import static java.util.Map.*;

/**
 * Session Bean implementation class RsSiteds
 */
@Stateless
@LocalBean
@Path("siteds/v1")
public class RsSiteds {

    public static final String coAplicativoTx = "123456";
    public static final String coEspecialidad = "";
    public static final String deCobertura = "CONSULTA AMBULATORIA";
    
    @POST
    @Path("obtenerDatosSitets")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Map<String, Object> obtenerDatosSitets(Map<String, Object> mapAseguradora){
      String codIafa = Mapo.mstring(mapAseguradora, "iafaAseguradora");
	  String apPaternoPaciente = "";
	  String apMaternoPaciente = "";
      if(GlobalConstants.CONFIG_GENERAL.getModule().equals("produccion")) {    	  
    	  apPaternoPaciente = Mapo.mstring(mapAseguradora, "apPaterno");
    	  apMaternoPaciente = Mapo.mstring(mapAseguradora, "apMaterno");
      }
      String nombrePaciente = Mapo.mstring(mapAseguradora, "nombreCompleto");
      String tipoDocPaciente = Mapo.mstring(mapAseguradora, "tipoDocumento");
      String numeroDocPaciente = Mapo.mstring(mapAseguradora, "nroDocumento");
 
      System.out.println("USUARIO: " + GlobalConstants.SITEDS_USER + ", END POINT: " +  GlobalConstants.SITEDS_BASE);
      
  	  String response = "";
  	  String inputJson = "";
  	  String idReceptor = codIafa;
      String coAfPaciente = "";
      String caPaciente = "";
      String coEsPaciente = "";
      String tiDoPaciente = "";
      String nuDoPaciente = "";
      String nuContratoPaciente = "";
      String coProducto = "";
      String coDescripcion = "";
      String nuSCTR ="";
      String coParentesco ="";
      String nuPlan = "";
      String feNacimiento ="";
      String genero = "";
      String esMarital = "";
      String tiCaContratante = "";
      String noPaContratante = "";
      String noContratante = "";
      String noMaContratante = "";
      String tiDoContratante = "";
      String idReContratante = "";
      String coReContratante = "";   
      String deProducto = ""; 
      
      SitedsSolAutorizacionDto dtoSolAuto = new SitedsSolAutorizacionDto();
      
	  HttpResponse responseData = null;
	  JsonReader jsonReader = null;
	  JsonObject jsonObject = null;  	       
	  JsonArray jsonArray = null;
	  
      inputJson = String.format("{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"caIPRESS\":\"%s\",\"noIPRESS\":\"%s\",\"tiDoIPRESS\":\"%s\",\"nuRucIPRESS\":\"%s\",\"nuRucRemitente\":\"%s\"}", 			GlobalConstants.SITEDS_NONCE, 
    		GlobalConstants.SITEDS_PASSWORD, 
    		GlobalConstants.SITEDS_USER, 
    		GlobalConstants.SITEDS_ID_REMITENTE, 
    		idReceptor, 
    		GlobalConstants.SITEDS_CA_IPRESS, 
    		GlobalConstants.SITEDS_NO_IPRESS, 
    		GlobalConstants.SITEDS_TI_DO_IPRESS, 
    		GlobalConstants.SITEDS_NU_RUC_IPRESS,
    		GlobalConstants.SITEDS_NU_RUC_IPRESS
    		  );
		try {
	        System.out.println("JSON ENTIDAD VINCULADA: " + inputJson);
			Map<String, String> headers = new HashMap<String, String>();
	        headers.put("Content-Type", "application/json; charset=utf-8");
	        headers.put("Authorization", GlobalConstants.SITEDS_TOKEN);
	        responseData  = sendPostRequest(GlobalConstants.SITEDS_ENTIDAD_VINCULADA, inputJson, headers);
	        response = responseData.getResponseBody();
	        System.out.println("RESPONSE ENTIDAD VINCULADA: " + response);
			
		} catch (Exception e) {
			throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar el servicio EntVin.");
		}
      
		if(responseData.getStatusCode() == 500) {
			throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio EntVin.");
		}
		
		if(response == null || !response.contains("respuesta\":\"Y\"")) {
			throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "El paciente no cuenta con afiliación al seguro.");
		}
      
	  	  inputJson = String.format("{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"apPaternoPaciente\":\"%s\",\"apMaternoPaciente\":\"%s\",\"noPaciente\":\"%s\",\"tiDocumento\":\"%s\",\"nuDocumento\":\"%s\",\"nuRucRemitente\":\"%s\"}", 
	  			GlobalConstants.SITEDS_NONCE,
	  			GlobalConstants.SITEDS_PASSWORD,
	  			GlobalConstants.SITEDS_USER,
	  			GlobalConstants.SITEDS_ID_REMITENTE, 
	  			idReceptor, 
	  			apPaternoPaciente, 
	  			apMaternoPaciente, 
	  			nombrePaciente, 
	  			tipoDocPaciente, 
	  			numeroDocPaciente,
	  			GlobalConstants.SITEDS_NU_RUC_IPRESS
	  	);
			try {
		        System.out.println("JSON ASEGURADO POR NOMBRE: " + inputJson);
				Map<String, String> headers = new HashMap<String, String>();
		        headers.put("Content-Type", "application/json; charset=utf-8");
		        headers.put("Authorization", GlobalConstants.SITEDS_TOKEN);
		        responseData  = sendPostRequest(GlobalConstants.SITEDS_ASEGURADO_NOMBRE, inputJson, headers);
		        response = responseData.getResponseBody();
		        System.out.println("RESPONSE ASEGURADO POR NOMBRE: " + response);
				
			} catch (Exception e) {
				throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio ConNom.");
			}
	      
			if(responseData.getStatusCode() == 500) {
				throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio ConNom.");
			}
			
        jsonReader = Json.createReader(new StringReader(response));
        jsonArray = jsonReader.readArray();
        jsonObject = null;
        
        boolean haySeguroSalud = false;
        
        boolean tieneCobertura4100 = false;
        JsonArrayBuilder nuevoDetalleCoberturaArrayBuilder = Json.createArrayBuilder();
        for (int i = 0; i < jsonArray.size(); i++) {
      	 jsonObject = jsonArray.getJsonObject(i);
      	 coProducto = jsonObject.getString("coProducto");
           coEsPaciente = jsonObject.getString("coEsPaciente");
           nuDoPaciente = jsonObject.getString("nuDoPaciente");
           coDescripcion = jsonObject.getString("coDescripcion");
           String desc = coDescripcion.toUpperCase();
           if(nuDoPaciente.equals(numeroDocPaciente)
        	        && coEsPaciente.equals("1")
        	        && !(desc.contains("SCTR")
        	            || desc.contains("SEGURO COMPLEMENTARIO DE TRABAJO DE RIESGO"))) {
                coAfPaciente = jsonObject.getString("coAfPaciente");
                caPaciente = jsonObject.getString("caPaciente");
                tiDoPaciente = jsonObject.getString("tiDoPaciente");
                nuContratoPaciente = jsonObject.getString("nuContratoPaciente");
                coDescripcion = jsonObject.getString("coDescripcion");
                nuSCTR = jsonObject.getString("nuSCTR");
                coParentesco = jsonObject.getString("coParentesco");
                nuPlan = jsonObject.getString("nuPlan");
                feNacimiento = jsonObject.getString("feNacimiento");
                genero = jsonObject.getString("genero");
                esMarital = jsonObject.getString("esMarital");
                tiCaContratante = jsonObject.getString("tiCaContratante");
                noPaContratante = jsonObject.getString("noPaContratante");
                noContratante = jsonObject.getString("noContratante");
                noMaContratante = jsonObject.getString("noMaContratante");
                tiDoContratante = jsonObject.getString("tiDoContratante");
                idReContratante = jsonObject.getString("idReContratante");
                coReContratante = jsonObject.getString("coReContratante");
                //haySeguroSalud = true;
          	 //break;
                inputJson = String.format("{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"tiDocumento\":\"%s\",\"nuDocumento\":\"%s\",\"coAfPaciente\":\"%s\",\"coProducto\":\"%s\",\"deProducto\":\"%s\",\"coEspecialidad\":\"%s\",\"coParentesco\":\"%s\",\"nuPlan\":\"%s\",\"tiCaContratante\":\"%s\",\"noPaContratante\":\"%s\",\"noContratante\":\"%s\",\"noMaContratante\":\"%s\",\"tiDoContratante\":\"%s\",\"idReContratante\":\"%s\",\"coReContratante\":\"%s\",\"nuRucRemitente\":\"%s\"}", 
                		GlobalConstants.SITEDS_NONCE,
                		GlobalConstants.SITEDS_PASSWORD,
                		GlobalConstants.SITEDS_USER, 
                		GlobalConstants.SITEDS_ID_REMITENTE, 
                		idReceptor, 
                		tiDoPaciente,
                		numeroDocPaciente, 
                		coAfPaciente, 
                		coProducto, 
                		coDescripcion, 
                		coEsPaciente, 
                		coParentesco, 
                		nuPlan, 
                		tiCaContratante, 
                		noPaContratante,
                		noContratante, 
                		noMaContratante, 
                		tiDoContratante, 
                		idReContratante, 
                		coReContratante,
                		GlobalConstants.SITEDS_NU_RUC_IPRESS
                	);
        			try {
        		        System.out.println("JSON ASEGURADO POR CODIGO: " + inputJson);
        				Map<String, String> headers = new HashMap<String, String>();
        		        headers.put("Content-Type", "application/json; charset=utf-8");
        		        headers.put("Authorization", GlobalConstants.SITEDS_TOKEN);
        		        responseData = sendPostRequest(GlobalConstants.SITEDS_ASEGURADO_CODIGO, inputJson, headers);
        		        response = responseData.getResponseBody();
        		        System.out.println("RESPONSE ASEGURADO POR CODIGO: " + response);
        				
        			} catch (Exception e) {
        	            //throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio ConCod.");
        				continue;
        			}
          	  
        			if(responseData.getStatusCode() == 500) {
        				//throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio ConCod.");
        				continue;
        			}
        			
        	        jsonReader = Json.createReader(new StringReader(response));
        	        JsonObject jsonResponse = jsonReader.readObject();
        	        JsonArray detalleCobertura = jsonResponse.getJsonArray("detalleCobertura");
        	        jsonObject = jsonResponse;
        	        if (detalleCobertura != null && !detalleCobertura.isEmpty()) {
        	            for (JsonValue coberturaValue : detalleCobertura) {
        	                JsonObject cobertura = coberturaValue.asJsonObject();
        	                if (cobertura.getString("coberturaCodigo").equals("4100")) {
        	                	nuevoDetalleCoberturaArrayBuilder.add(cobertura);
        	                    dtoSolAuto.setNuCobertura(cobertura.getString("nuCobertura"));
        	                    dtoSolAuto.setObsCobertura("");
        	                    dtoSolAuto.setMsgObs("");
        	                    dtoSolAuto.setMsgConEspeciales("");
        	                    dtoSolAuto.setNuCobPreExistencia("");
        	                    dtoSolAuto.setBeMaxInicial(cobertura.getString("beMaxInicial"));
        	                    dtoSolAuto.setCanServicio("1");
        	                    dtoSolAuto.setIdDeProducto(cobertura.getString("idProducto"));
        	                    dtoSolAuto.setCoTiCobertura(cobertura.getString("coTiCobertura"));
        	                    dtoSolAuto.setCoSubTiCobertura(cobertura.getString("coSubTiCobertura"));
        	                    dtoSolAuto.setMsgObsPre(cobertura.getString("msgConEspeciales"));
        	                    dtoSolAuto.setMsgConEspecialesPre(cobertura.getString("msgConEspeciales"));
        	                    dtoSolAuto.setCoTiMoneda(cobertura.getString("coTiMoneda"));
        	                    dtoSolAuto.setCoPagoFijo(cobertura.getString("coPagoFijo"));
        	                    dtoSolAuto.setCoCalServicio(cobertura.getString("coCalServicio"));
        	                    dtoSolAuto.setCanCalServicio(cobertura.getString("canCalServicio"));
        	                    dtoSolAuto.setCoPagoVariable(cobertura.getString("coPagoVariable"));
        	                    dtoSolAuto.setFlagCG(cobertura.getString("flagCaGarantia"));
        	                    dtoSolAuto.setDeflagCG(cobertura.getString("deflagCaGarantia"));
        	                    dtoSolAuto.setFeFinCarencia("");
        	                    if (cobertura.getString("feFinEspera") != null) {
        	                        dtoSolAuto.setFeFinCarencia(cobertura.getString("feFinEspera"));
        	                    }
        	                    dtoSolAuto.setFeFinEspera(cobertura.getString("nuCobertura"));
        	                    tieneCobertura4100 = true;
        	                    break;
        	                }
        	            }
        	        }

        	        if (tieneCobertura4100) {
        	            break;
        	        }
           }
           
        }
        
        if (!tieneCobertura4100) {
            throw UtilResponse.rsException(Response.Status.NOT_FOUND, "No hay coberturas disponibles para el seguro elegido.");
        }
        
        /*for (int i = 0; i < jsonArray.size(); i++) {
      	 jsonObject = jsonArray.getJsonObject(i);
      	 coProducto = jsonObject.getString("coProducto");
           coEsPaciente = jsonObject.getString("coEsPaciente");
           nuDoPaciente = jsonObject.getString("nuDoPaciente");
           
           if(nuDoPaciente.equals(numeroDocPaciente) && coEsPaciente.equals("1") && !coProducto.equals("R")) {
                coAfPaciente = jsonObject.getString("coAfPaciente");
                caPaciente = jsonObject.getString("caPaciente");
                tiDoPaciente = jsonObject.getString("tiDoPaciente");
                nuContratoPaciente = jsonObject.getString("nuContratoPaciente");
                coDescripcion = jsonObject.getString("coDescripcion");
                nuSCTR = jsonObject.getString("nuSCTR");
                coParentesco = jsonObject.getString("coParentesco");
                nuPlan = jsonObject.getString("nuPlan");
                feNacimiento = jsonObject.getString("feNacimiento");
                genero = jsonObject.getString("genero");
                esMarital = jsonObject.getString("esMarital");
                tiCaContratante = jsonObject.getString("tiCaContratante");
                noPaContratante = jsonObject.getString("noPaContratante");
                noContratante = jsonObject.getString("noContratante");
                noMaContratante = jsonObject.getString("noMaContratante");
                tiDoContratante = jsonObject.getString("tiDoContratante");
                idReContratante = jsonObject.getString("idReContratante");
                coReContratante = jsonObject.getString("coReContratante");
                haySeguroSalud = true;
          	 break;
           }
           
        }
        
        if(!haySeguroSalud) {
      	  throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "No hay seguros activos para el paciente.");
        }
      
        
        inputJson = String.format("{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"tiDocumento\":\"%s\",\"nuDocumento\":\"%s\",\"coAfPaciente\":\"%s\",\"coProducto\":\"%s\",\"deProducto\":\"%s\",\"coEspecialidad\":\"%s\",\"coParentesco\":\"%s\",\"nuPlan\":\"%s\",\"tiCaContratante\":\"%s\",\"noPaContratante\":\"%s\",\"noContratante\":\"%s\",\"noMaContratante\":\"%s\",\"tiDoContratante\":\"%s\",\"idReContratante\":\"%s\",\"coReContratante\":\"%s\",\"nuRucRemitente\":\"%s\"}", 
        		GlobalConstants.SITEDS_NONCE,
        		GlobalConstants.SITEDS_PASSWORD,
        		GlobalConstants.SITEDS_USER, 
        		GlobalConstants.SITEDS_ID_REMITENTE, 
        		idReceptor, 
        		tiDoPaciente,
        		numeroDocPaciente, 
        		coAfPaciente, 
        		coProducto, 
        		coDescripcion, 
        		coEsPaciente, 
        		coParentesco, 
        		nuPlan, 
        		tiCaContratante, 
        		noPaContratante,
        		noContratante, 
        		noMaContratante, 
        		tiDoContratante, 
        		idReContratante, 
        		coReContratante,
        		GlobalConstants.SITEDS_NU_RUC_IPRESS
        	);
			try {
		        System.out.println("JSON ASEGURADO POR CODIGO: " + inputJson);
				Map<String, String> headers = new HashMap<String, String>();
		        headers.put("Content-Type", "application/json; charset=utf-8");
		        headers.put("Authorization", GlobalConstants.SITEDS_TOKEN);
		        responseData = sendPostRequest(GlobalConstants.SITEDS_ASEGURADO_CODIGO, inputJson, headers);
		        response = responseData.getResponseBody();
		        System.out.println("RESPONSE ASEGURADO POR CODIGO: " + response);
				
			} catch (Exception e) {
	            throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio ConCod.");
			}
  	  
			if(responseData.getStatusCode() == 500) {
				throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio ConCod.");
			}
			
		String fechaSolo = feNacimiento.substring(0, 10);
        LocalDate fechaNacimiento = LocalDate.parse(fechaSolo, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate fechaActual = LocalDate.now();
        Period periodo = Period.between(fechaNacimiento, fechaActual);
        Integer edad = periodo.getYears();
        
        jsonReader = Json.createReader(new StringReader(response));
        JsonValue jsonValue = jsonReader.readValue();
        jsonObject = jsonValue.asJsonObject();
        String coberturaFull = response;
        JsonArrayBuilder nuevoDetalleCoberturaArrayBuilder = Json.createArrayBuilder();
        if(jsonObject.getJsonArray("detalleCobertura").size() < 0) {
        	throw UtilResponse.rsException(Response.Status.NOT_FOUND, "No hay coberturas disponibles para el seguro elegido.");
        }
              
        for (int i = 0; i < jsonObject.getJsonArray("detalleCobertura").size(); i++) {
            JsonObject cobertura = jsonObject.getJsonArray("detalleCobertura").getJsonObject(i);
            if (cobertura.getString("coberturaCodigo").equals("4100")) {
                nuevoDetalleCoberturaArrayBuilder.add(cobertura);
                
                dtoSolAuto.setNuCobertura(cobertura.getString("nuCobertura"));
                dtoSolAuto.setObsCobertura("");
                dtoSolAuto.setMsgObs("");
                dtoSolAuto.setMsgConEspeciales("");
                dtoSolAuto.setNuCobPreExistencia("");
                dtoSolAuto.setBeMaxInicial(cobertura.getString("beMaxInicial"));
                dtoSolAuto.setCanServicio("1");
                dtoSolAuto.setIdDeProducto(cobertura.getString("idProducto"));
                dtoSolAuto.setCoTiCobertura(cobertura.getString("coTiCobertura"));
                dtoSolAuto.setCoSubTiCobertura(cobertura.getString("coSubTiCobertura"));
                dtoSolAuto.setMsgObsPre(cobertura.getString("msgConEspeciales"));
                dtoSolAuto.setMsgConEspecialesPre(cobertura.getString("msgConEspeciales"));
                dtoSolAuto.setCoTiMoneda(cobertura.getString("coTiMoneda"));
                dtoSolAuto.setCoPagoFijo(cobertura.getString("coPagoFijo"));
                dtoSolAuto.setCoCalServicio(cobertura.getString("coCalServicio"));
                dtoSolAuto.setCanCalServicio(cobertura.getString("canCalServicio"));
                dtoSolAuto.setCoPagoVariable(cobertura.getString("coPagoVariable"));
                dtoSolAuto.setFlagCG(cobertura.getString("flagCaGarantia"));
                dtoSolAuto.setDeflagCG(cobertura.getString("deflagCaGarantia"));
                dtoSolAuto.setFeFinCarencia("");
                if (cobertura.getString("feFinEspera") != null) {
                    dtoSolAuto.setFeFinCarencia(cobertura.getString("feFinEspera"));
                }
                dtoSolAuto.setFeFinEspera(cobertura.getString("nuCobertura"));
                
                break;
            }
        }*/
        
		String fechaSolo = feNacimiento.substring(0, 10);
        LocalDate fechaNacimiento = LocalDate.parse(fechaSolo, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate fechaActual = LocalDate.now();
        Period periodo = Period.between(fechaNacimiento, fechaActual);
        Integer edad = periodo.getYears();
        
        JsonObjectBuilder nuevoJsonObjectBuilder = Json.createObjectBuilder(jsonObject);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatHora = new SimpleDateFormat("HH:mm:ss");
        String horaTransaccion = formatHora.format(calendar.getTime());
        String fechaIniVigencia = this.timeConvertToIso(jsonObject.getString("feIniVigencia"));
        String fechaFinVigencia = this.timeConvertToIso(jsonObject.getString("feFinVigencia"));
        String fechaAfiliacion = this.timeConvertToIso(jsonObject.getString("feInsTitular"));
        
        nuevoJsonObjectBuilder.add("detalleCobertura", nuevoDetalleCoberturaArrayBuilder);
        nuevoJsonObjectBuilder.add("idCorrelativo", JsonValue.NULL);
        nuevoJsonObjectBuilder.add("noTransaccion", JsonValue.NULL);
        nuevoJsonObjectBuilder.add("idReceptor", JsonValue.NULL);
        nuevoJsonObjectBuilder.add("idRemitente", JsonValue.NULL);
        nuevoJsonObjectBuilder.add("nuControl", JsonValue.NULL);
        nuevoJsonObjectBuilder.add("nuControlIST", JsonValue.NULL);
        nuevoJsonObjectBuilder.add("hoTransaccion", horaTransaccion);
        nuevoJsonObjectBuilder.add("idTransaccion", "257");
        nuevoJsonObjectBuilder.add("edadPaciente", edad);      
        nuevoJsonObjectBuilder.add("descProducto", deProducto);
        nuevoJsonObjectBuilder.add("feAfiliacion", fechaAfiliacion);
        nuevoJsonObjectBuilder.add("feFinVigenciaForm", fechaFinVigencia);
        nuevoJsonObjectBuilder.add("feIniVigenciaForm", fechaIniVigencia);
        nuevoJsonObjectBuilder.add("tipoDeAtencion", Integer.valueOf(4));
        nuevoJsonObjectBuilder.add("tipoPaciente", Integer.valueOf(2));
        nuevoJsonObjectBuilder.add("coberturaDescripcion", String.valueOf("CONSULTA AMBULATORIA"));

        JsonObject nuevoJsonObject = nuevoJsonObjectBuilder.build();   


        String nroDocTitular = jsonObject.getString("nuDoPaciente");
        String coMoneda = jsonObject.getString("coMoneda");
        
        JsonArray detalleCoberturaArray = nuevoJsonObject.getJsonArray("detalleCobertura");
        JsonObject cobertura = detalleCoberturaArray.getJsonObject(0);
        
        String nuCobertura = cobertura.getString("nuCobertura");
        
        String caServicio = "1";
        String coCalServicio = cobertura.getString("coCalServicio");
        String beMaxInicial = cobertura.getString("beMaxInicial");
        String coTiCobertura = cobertura.getString("coTiCobertura");
        String coSuTiCobertura = cobertura.getString("coSubTiCobertura");
    
        
  	  Map<String, Object> mapResponse = new HashMap<String, Object>();

  	  mapResponse.put("descripcion", "Se obtuvieron los resultados");
  	  mapResponse.put("informacionObtenida", nuevoJsonObject);
  	  //mapResponse.put("informacionFormateada", mapAsegurado);
  	  
  	  //mapResponse.put("informacionCondiciones", jsonArrayCondiciones);
  	  return mapResponse;
    }
    
    
    @POST
    @Path("obtenerDatosSitetsLuxor")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public SitedsSolAutorizacionDto obtenerDatosSitetsLuxor(Map<String, Object> mapAseguradora){
      String codIafa = Mapo.mstring(mapAseguradora, "iafaAseguradora");
	  String apPaternoPaciente = "";
	  String apMaternoPaciente = "";
      if(GlobalConstants.CONFIG_GENERAL.getModule().equals("produccion")) {    	  
    	  apPaternoPaciente = Mapo.mstring(mapAseguradora, "apPaterno");
    	  apMaternoPaciente = Mapo.mstring(mapAseguradora, "apMaterno");
      }
      String nombrePaciente = Mapo.mstring(mapAseguradora, "nombreCompleto");
      String tipoDocPaciente = Mapo.mstring(mapAseguradora, "tipoDocumento");
      String numeroDocPaciente = Mapo.mstring(mapAseguradora, "nroDocumento");
      String especialidad = Mapo.mstring(mapAseguradora, "especialidad").toUpperCase();
      System.out.println("USUARIO: " + GlobalConstants.SITEDS_USER + ", END POINT: " +  GlobalConstants.SITEDS_BASE);
      
  	  String response = "";
  	  String inputJson = "";
  	  String idReceptor = codIafa;
      String coAfPaciente = "";
      String caPaciente = "";
      String coEsPaciente = "";
      String tiDoPaciente = "";
      String nuDoPaciente = "";
      String nuContratoPaciente = "";
      String coProducto = "";
      String coDescripcion = "";
      String nuSCTR ="";
      String coParentesco ="";
      String nuPlan = "";
      String feNacimiento ="";
      String genero = "";
      String esMarital = "";
      String tiCaContratante = "";
      String noPaContratante = "";
      String noContratante = "";
      String noMaContratante = "";
      String tiDoContratante = "";
      String idReContratante = "";
      String coReContratante = "";   
      String deProducto = ""; 
      
      SitedsSolAutorizacionDto dtoSolAuto = new SitedsSolAutorizacionDto();
      dtoSolAuto.setNuAutorizacion("00012123");
	  HttpResponse responseData = null;
	  JsonReader jsonReader = null;
	  JsonObject jsonObject = null;  	       
	  JsonArray jsonArray = null;
	  
      inputJson = String.format("{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"caIPRESS\":\"%s\",\"noIPRESS\":\"%s\",\"tiDoIPRESS\":\"%s\",\"nuRucIPRESS\":\"%s\",\"nuRucRemitente\":\"%s\"}", 			GlobalConstants.SITEDS_NONCE, 
    		GlobalConstants.SITEDS_PASSWORD, 
    		GlobalConstants.SITEDS_USER, 
    		GlobalConstants.SITEDS_ID_REMITENTE, 
    		idReceptor, 
    		GlobalConstants.SITEDS_CA_IPRESS, 
    		GlobalConstants.SITEDS_NO_IPRESS, 
    		GlobalConstants.SITEDS_TI_DO_IPRESS, 
    		GlobalConstants.SITEDS_NU_RUC_IPRESS,
    		GlobalConstants.SITEDS_NU_RUC_IPRESS
    		  );
		try {
	        System.out.println("JSON ENTIDAD VINCULADA: " + inputJson);
			Map<String, String> headers = new HashMap<String, String>();
	        headers.put("Content-Type", "application/json; charset=utf-8");
	        headers.put("Authorization", GlobalConstants.SITEDS_TOKEN);
	        responseData  = sendPostRequest(GlobalConstants.SITEDS_ENTIDAD_VINCULADA, inputJson, headers);
	        response = responseData.getResponseBody();
	        System.out.println("RESPONSE ENTIDAD VINCULADA: " + response);
			
		} catch (Exception e) {
			throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar el servicio EntVin.");
		}
      
		if(responseData.getStatusCode() == 500) {
			throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio EntVin.");
		}
		
		if(response == null || !response.contains("respuesta\":\"Y\"")) {
			throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "El paciente no cuenta con afiliación al seguro.");
		}
      
	  	  inputJson = String.format("{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"apPaternoPaciente\":\"%s\",\"apMaternoPaciente\":\"%s\",\"noPaciente\":\"%s\",\"tiDocumento\":\"%s\",\"nuDocumento\":\"%s\",\"nuRucRemitente\":\"%s\"}", 
	  			GlobalConstants.SITEDS_NONCE,
	  			GlobalConstants.SITEDS_PASSWORD,
	  			GlobalConstants.SITEDS_USER,
	  			GlobalConstants.SITEDS_ID_REMITENTE, 
	  			idReceptor, 
	  			apPaternoPaciente, 
	  			apMaternoPaciente, 
	  			nombrePaciente, 
	  			tipoDocPaciente, 
	  			numeroDocPaciente,
	  			GlobalConstants.SITEDS_NU_RUC_IPRESS
	  	);
			try {
		        System.out.println("JSON ASEGURADO POR NOMBRE: " + inputJson);
				Map<String, String> headers = new HashMap<String, String>();
		        headers.put("Content-Type", "application/json; charset=utf-8");
		        headers.put("Authorization", GlobalConstants.SITEDS_TOKEN);
		        responseData  = sendPostRequest(GlobalConstants.SITEDS_ASEGURADO_NOMBRE, inputJson, headers);
		        response = responseData.getResponseBody();
		        System.out.println("RESPONSE ASEGURADO POR NOMBRE: " + response);
				
			} catch (Exception e) {
				throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio ConNom.");
			}
	      
			if(responseData.getStatusCode() == 500) {
				throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio ConNom.");
			}
			
        jsonReader = Json.createReader(new StringReader(response));
        jsonArray = jsonReader.readArray();
        jsonObject = null;
        
        String numCobertura = "";
        if(especialidad.equals("PSICOLOGIA")) {
        	numCobertura = "4502";
        }else if(especialidad.equals("PSIQUIATRIA")) {
        	numCobertura = "4501";
        }else if(especialidad.equals("NUTRICION")) {
        	numCobertura = "4021";
        }else {
        	numCobertura = "4100";
        }
        
        boolean haySeguroSalud = false;
        
        boolean tieneCobertura = false;
        JsonArrayBuilder nuevoDetalleCoberturaArrayBuilder = Json.createArrayBuilder();
        for (int i = 0; i < jsonArray.size(); i++) {
      	 jsonObject = jsonArray.getJsonObject(i);
      	 coProducto = jsonObject.getString("coProducto");
           coEsPaciente = jsonObject.getString("coEsPaciente");
           nuDoPaciente = jsonObject.getString("nuDoPaciente");
           coDescripcion = jsonObject.getString("coDescripcion");
           String desc = coDescripcion.toUpperCase();
           if(nuDoPaciente.equals(numeroDocPaciente)
        	        && coEsPaciente.equals("1")
        	        && !(desc.contains("SCTR")
        	            || desc.contains("SEGURO COMPLEMENTARIO DE TRABAJO DE RIESGO")
        	            || desc.contains("SEGURO COMPLEM. TRABAJO DE RIESGO"))) {
                coAfPaciente = jsonObject.getString("coAfPaciente");
                caPaciente = jsonObject.getString("caPaciente");
                tiDoPaciente = jsonObject.getString("tiDoPaciente");
                nuContratoPaciente = jsonObject.getString("nuContratoPaciente");
                coDescripcion = jsonObject.getString("coDescripcion");
                //coDescripcion = "EPS";
                nuSCTR = jsonObject.getString("nuSCTR");
                coParentesco = jsonObject.getString("coParentesco");
                nuPlan = jsonObject.getString("nuPlan");
                feNacimiento = jsonObject.getString("feNacimiento");
                genero = jsonObject.getString("genero");
                esMarital = jsonObject.getString("esMarital");
                tiCaContratante = jsonObject.getString("tiCaContratante");
                noPaContratante = jsonObject.getString("noPaContratante");
                noContratante = jsonObject.getString("noContratante");
                noMaContratante = jsonObject.getString("noMaContratante");
                tiDoContratante = jsonObject.getString("tiDoContratante");
                idReContratante = jsonObject.getString("idReContratante");
                coReContratante = jsonObject.getString("coReContratante");
                
                //haySeguroSalud = true;
          	 //break;
                inputJson = String.format("{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"tiDocumento\":\"%s\",\"nuDocumento\":\"%s\",\"coAfPaciente\":\"%s\",\"coProducto\":\"%s\",\"deProducto\":\"%s\",\"coEspecialidad\":\"%s\",\"coParentesco\":\"%s\",\"nuPlan\":\"%s\",\"tiCaContratante\":\"%s\",\"noPaContratante\":\"%s\",\"noContratante\":\"%s\",\"noMaContratante\":\"%s\",\"tiDoContratante\":\"%s\",\"idReContratante\":\"%s\",\"coReContratante\":\"%s\",\"nuRucRemitente\":\"%s\"}", 
                		GlobalConstants.SITEDS_NONCE,
                		GlobalConstants.SITEDS_PASSWORD,
                		GlobalConstants.SITEDS_USER, 
                		GlobalConstants.SITEDS_ID_REMITENTE, 
                		idReceptor, 
                		tiDoPaciente,
                		numeroDocPaciente, 
                		coAfPaciente, 
                		coProducto, 
                		coDescripcion, 
                		coEsPaciente, 
                		coParentesco, 
                		nuPlan, 
                		tiCaContratante, 
                		noPaContratante,
                		noContratante, 
                		noMaContratante, 
                		tiDoContratante, 
                		idReContratante, 
                		coReContratante,
                		GlobalConstants.SITEDS_NU_RUC_IPRESS
                	);
        			try {
        		        System.out.println("JSON ASEGURADO POR CODIGO: " + inputJson);
        				Map<String, String> headers = new HashMap<String, String>();
        		        headers.put("Content-Type", "application/json; charset=utf-8");
        		        headers.put("Authorization", GlobalConstants.SITEDS_TOKEN);
        		        responseData = sendPostRequest(GlobalConstants.SITEDS_ASEGURADO_CODIGO, inputJson, headers);
        		        response = responseData.getResponseBody();
        		        System.out.println("RESPONSE ASEGURADO POR CODIGO: " + response);
        				
        			} catch (Exception e) {
        	            //throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio ConCod.");
        				continue;
        			}
          	  
        			if(responseData.getStatusCode() == 500) {
        				//throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio ConCod.");
        				continue;
        			}
        			
        	        jsonReader = Json.createReader(new StringReader(response));
        	        JsonObject jsonResponse = jsonReader.readObject();
        	        dtoSolAuto.setFeAfiliacion(jsonResponse.getString("feInsTitular"));
        	        JsonArray detalleCobertura = jsonResponse.getJsonArray("detalleCobertura");
        	        jsonObject = jsonResponse;
        	        if (detalleCobertura != null && !detalleCobertura.isEmpty()) {
        	            for (JsonValue coberturaValue : detalleCobertura) {
        	                JsonObject cobertura = coberturaValue.asJsonObject();
        	                if (cobertura.getString("coberturaCodigo").equals(numCobertura)) {
        	                	nuevoDetalleCoberturaArrayBuilder.add(cobertura);
        	                    dtoSolAuto.setNuCobertura(cobertura.getString("nuCobertura"));
        	                    dtoSolAuto.setObsCobertura("");
        	                    dtoSolAuto.setMsgObs("");
        	                    dtoSolAuto.setMsgConEspeciales("");
        	                    dtoSolAuto.setNuCobPreExistencia("");
        	                    dtoSolAuto.setBeMaxInicial(cobertura.getString("beMaxInicial"));
        	                    dtoSolAuto.setCanServicio("1");
        	                    dtoSolAuto.setIdDeProducto(cobertura.getString("idProducto"));
        	                    dtoSolAuto.setCoTiCobertura(cobertura.getString("coTiCobertura"));
        	                    dtoSolAuto.setCoSubTiCobertura(cobertura.getString("coSubTiCobertura"));
        	                    dtoSolAuto.setMsgObsPre(cobertura.getString("msgConEspeciales"));
        	                    dtoSolAuto.setMsgConEspecialesPre(cobertura.getString("msgConEspeciales"));
        	                    dtoSolAuto.setCoTiMoneda(cobertura.getString("coTiMoneda"));
        	                    dtoSolAuto.setCoPagoFijo(cobertura.getString("coPagoFijo"));
        	                    dtoSolAuto.setCoCalServicio(cobertura.getString("coCalServicio"));
        	                    dtoSolAuto.setCanCalServicio(cobertura.getString("canCalServicio"));
        	                    dtoSolAuto.setCoPagoVariable(cobertura.getString("coPagoVariable"));
        	                    dtoSolAuto.setFlagCG(cobertura.getString("flagCaGarantia"));
        	                    dtoSolAuto.setDeflagCG(cobertura.getString("deflagCaGarantia"));
        	                    dtoSolAuto.setFeFinCarencia("");
        	                    dtoSolAuto.setCoInRestriccion(cobertura.getString("coInRestriccion"));
        	                    if (cobertura.containsKey("feFinCarencia") && 
        	                    	    !cobertura.isNull("feFinCarencia")) {

        	                    	    dtoSolAuto.setFeFinCarencia(cobertura.getString("feFinCarencia"));
        	                    	}
        	                    dtoSolAuto.setFeFinEspera(cobertura.getString("feFinEspera"));
        	                    tieneCobertura = true;
        	                    break;
        	                }      	                
        	            }
        	            if (!tieneCobertura && "40004".equals(codIafa) && "4100".equals(numCobertura)) {
        	                String numCoberturaFallback = "4150";
        	                for (JsonValue coberturaValue : detalleCobertura) {
        	                    JsonObject cobertura = coberturaValue.asJsonObject();
        	                    if (cobertura.getString("coberturaCodigo").equals(numCoberturaFallback)) {
        	                        nuevoDetalleCoberturaArrayBuilder.add(cobertura);
        	                        dtoSolAuto.setNuCobertura(cobertura.getString("nuCobertura"));
        	                        dtoSolAuto.setObsCobertura("");
        	                        dtoSolAuto.setMsgObs("");
        	                        dtoSolAuto.setMsgConEspeciales("");
        	                        dtoSolAuto.setNuCobPreExistencia("");
        	                        dtoSolAuto.setBeMaxInicial(cobertura.getString("beMaxInicial"));
        	                        dtoSolAuto.setCanServicio("1");
        	                        dtoSolAuto.setIdDeProducto(cobertura.getString("idProducto"));
        	                        dtoSolAuto.setCoTiCobertura(cobertura.getString("coTiCobertura"));
        	                        dtoSolAuto.setCoSubTiCobertura(cobertura.getString("coSubTiCobertura"));
        	                        dtoSolAuto.setMsgObsPre(cobertura.getString("msgConEspeciales"));
        	                        dtoSolAuto.setMsgConEspecialesPre(cobertura.getString("msgConEspeciales"));
        	                        dtoSolAuto.setCoTiMoneda(cobertura.getString("coTiMoneda"));
        	                        dtoSolAuto.setCoPagoFijo(cobertura.getString("coPagoFijo"));
        	                        dtoSolAuto.setCoCalServicio(cobertura.getString("coCalServicio"));
        	                        dtoSolAuto.setCanCalServicio(cobertura.getString("canCalServicio"));
        	                        dtoSolAuto.setCoPagoVariable(cobertura.getString("coPagoVariable"));
        	                        dtoSolAuto.setFlagCG(cobertura.getString("flagCaGarantia"));
        	                        dtoSolAuto.setDeflagCG(cobertura.getString("deflagCaGarantia"));
        	                        dtoSolAuto.setFeFinCarencia("");
        	                        dtoSolAuto.setCoInRestriccion(cobertura.getString("coInRestriccion"));
        	                        if (cobertura.containsKey("feFinCarencia") && 
        	                        	    !cobertura.isNull("feFinCarencia")) {

        	                        	    dtoSolAuto.setFeFinCarencia(cobertura.getString("feFinCarencia"));
        	                        	}
        	                        dtoSolAuto.setFeFinEspera(cobertura.getString("feFinEspera"));
        	                        tieneCobertura = true;
        	                        break;
        	                    }
        	                }
        	            }
        	        }

        	        if (tieneCobertura) {
        	            break;
        	        }
           }
           
        }
        
        if (!tieneCobertura) {
            throw UtilResponse.rsException(Response.Status.NOT_FOUND, "No hay coberturas disponibles para el seguro elegido.");
        }
        
        /*for (int i = 0; i < jsonArray.size(); i++) {
      	 jsonObject = jsonArray.getJsonObject(i);
      	 coProducto = jsonObject.getString("coProducto");
           coEsPaciente = jsonObject.getString("coEsPaciente");
           nuDoPaciente = jsonObject.getString("nuDoPaciente");
           
           if(nuDoPaciente.equals(numeroDocPaciente) && coEsPaciente.equals("1") && !coProducto.equals("R")) {
                coAfPaciente = jsonObject.getString("coAfPaciente");
                caPaciente = jsonObject.getString("caPaciente");
                tiDoPaciente = jsonObject.getString("tiDoPaciente");
                nuContratoPaciente = jsonObject.getString("nuContratoPaciente");
                coDescripcion = jsonObject.getString("coDescripcion");
                nuSCTR = jsonObject.getString("nuSCTR");
                coParentesco = jsonObject.getString("coParentesco");
                nuPlan = jsonObject.getString("nuPlan");
                feNacimiento = jsonObject.getString("feNacimiento");
                genero = jsonObject.getString("genero");
                esMarital = jsonObject.getString("esMarital");
                tiCaContratante = jsonObject.getString("tiCaContratante");
                noPaContratante = jsonObject.getString("noPaContratante");
                noContratante = jsonObject.getString("noContratante");
                noMaContratante = jsonObject.getString("noMaContratante");
                tiDoContratante = jsonObject.getString("tiDoContratante");
                idReContratante = jsonObject.getString("idReContratante");
                coReContratante = jsonObject.getString("coReContratante");
                haySeguroSalud = true;
          	 break;
           }
           
        }
        
        if(!haySeguroSalud) {
      	  throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "No hay seguros activos para el paciente.");
        }
      
        
        inputJson = String.format("{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"tiDocumento\":\"%s\",\"nuDocumento\":\"%s\",\"coAfPaciente\":\"%s\",\"coProducto\":\"%s\",\"deProducto\":\"%s\",\"coEspecialidad\":\"%s\",\"coParentesco\":\"%s\",\"nuPlan\":\"%s\",\"tiCaContratante\":\"%s\",\"noPaContratante\":\"%s\",\"noContratante\":\"%s\",\"noMaContratante\":\"%s\",\"tiDoContratante\":\"%s\",\"idReContratante\":\"%s\",\"coReContratante\":\"%s\",\"nuRucRemitente\":\"%s\"}", 
        		GlobalConstants.SITEDS_NONCE,
        		GlobalConstants.SITEDS_PASSWORD,
        		GlobalConstants.SITEDS_USER, 
        		GlobalConstants.SITEDS_ID_REMITENTE, 
        		idReceptor, 
        		tiDoPaciente,
        		numeroDocPaciente, 
        		coAfPaciente, 
        		coProducto, 
        		coDescripcion, 
        		coEsPaciente, 
        		coParentesco, 
        		nuPlan, 
        		tiCaContratante, 
        		noPaContratante,
        		noContratante, 
        		noMaContratante, 
        		tiDoContratante, 
        		idReContratante, 
        		coReContratante,
        		GlobalConstants.SITEDS_NU_RUC_IPRESS
        	);
			try {
		        System.out.println("JSON ASEGURADO POR CODIGO: " + inputJson);
				Map<String, String> headers = new HashMap<String, String>();
		        headers.put("Content-Type", "application/json; charset=utf-8");
		        headers.put("Authorization", GlobalConstants.SITEDS_TOKEN);
		        responseData = sendPostRequest(GlobalConstants.SITEDS_ASEGURADO_CODIGO, inputJson, headers);
		        response = responseData.getResponseBody();
		        System.out.println("RESPONSE ASEGURADO POR CODIGO: " + response);
				
			} catch (Exception e) {
	            throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio ConCod.");
			}
  	  
			if(responseData.getStatusCode() == 500) {
				throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio ConCod.");
			}
			
		String fechaSolo = feNacimiento.substring(0, 10);
        LocalDate fechaNacimiento = LocalDate.parse(fechaSolo, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate fechaActual = LocalDate.now();
        Period periodo = Period.between(fechaNacimiento, fechaActual);
        Integer edad = periodo.getYears();
        
        jsonReader = Json.createReader(new StringReader(response));
        JsonValue jsonValue = jsonReader.readValue();
        jsonObject = jsonValue.asJsonObject();
        String coberturaFull = response;
        JsonArrayBuilder nuevoDetalleCoberturaArrayBuilder = Json.createArrayBuilder();
        if(jsonObject.getJsonArray("detalleCobertura").size() < 0) {
        	throw UtilResponse.rsException(Response.Status.NOT_FOUND, "No hay coberturas disponibles para el seguro elegido.");
        }
              
        for (int i = 0; i < jsonObject.getJsonArray("detalleCobertura").size(); i++) {
            JsonObject cobertura = jsonObject.getJsonArray("detalleCobertura").getJsonObject(i);
            if (cobertura.getString("coberturaCodigo").equals("4100")) {
                nuevoDetalleCoberturaArrayBuilder.add(cobertura);
                
                dtoSolAuto.setNuCobertura(cobertura.getString("nuCobertura"));
                dtoSolAuto.setObsCobertura("");
                dtoSolAuto.setMsgObs("");
                dtoSolAuto.setMsgConEspeciales("");
                dtoSolAuto.setNuCobPreExistencia("");
                dtoSolAuto.setBeMaxInicial(cobertura.getString("beMaxInicial"));
                dtoSolAuto.setCanServicio("1");
                dtoSolAuto.setIdDeProducto(cobertura.getString("idProducto"));
                dtoSolAuto.setCoTiCobertura(cobertura.getString("coTiCobertura"));
                dtoSolAuto.setCoSubTiCobertura(cobertura.getString("coSubTiCobertura"));
                dtoSolAuto.setMsgObsPre(cobertura.getString("msgConEspeciales"));
                dtoSolAuto.setMsgConEspecialesPre(cobertura.getString("msgConEspeciales"));
                dtoSolAuto.setCoTiMoneda(cobertura.getString("coTiMoneda"));
                dtoSolAuto.setCoPagoFijo(cobertura.getString("coPagoFijo"));
                dtoSolAuto.setCoCalServicio(cobertura.getString("coCalServicio"));
                dtoSolAuto.setCanCalServicio(cobertura.getString("canCalServicio"));
                dtoSolAuto.setCoPagoVariable(cobertura.getString("coPagoVariable"));
                dtoSolAuto.setFlagCG(cobertura.getString("flagCaGarantia"));
                dtoSolAuto.setDeflagCG(cobertura.getString("deflagCaGarantia"));
                dtoSolAuto.setFeFinCarencia("");
                if (cobertura.getString("feFinEspera") != null) {
                    dtoSolAuto.setFeFinCarencia(cobertura.getString("feFinEspera"));
                }
                dtoSolAuto.setFeFinEspera(cobertura.getString("nuCobertura"));
                
                break;
            }
        }*/
        
		String fechaSolo = feNacimiento.substring(0, 10);
        LocalDate fechaNacimiento = LocalDate.parse(fechaSolo, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate fechaActual = LocalDate.now();
        Period periodo = Period.between(fechaNacimiento, fechaActual);
        Integer edad = periodo.getYears();
        
        JsonObjectBuilder nuevoJsonObjectBuilder = Json.createObjectBuilder(jsonObject);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatHora = new SimpleDateFormat("HH:mm:ss");
        String horaTransaccion = formatHora.format(calendar.getTime());
        String fechaIniVigencia = this.timeConvertToIso(jsonObject.getString("feIniVigencia"));
        String fechaFinVigencia = this.timeConvertToIso(jsonObject.getString("feFinVigencia"));
        String fechaAfiliacion = this.timeConvertToIso(jsonObject.getString("feInsTitular"));
        String nuCertificado = jsonObject.getString("nuCertificado");
        String coTiPoliza = jsonObject.getString("coTiPoliza");
        coProducto = jsonObject.getString("coProducto");
        deProducto = jsonObject.getString("deProducto");
        nuPlan = jsonObject.getString("nuPlan");
        String tiPlanSalud = jsonObject.getString("tiPlanSalud");
        String coMoneda = jsonObject.getString("coMoneda");
        coParentesco = jsonObject.getString("coParentesco");
        String soBeneficio = jsonObject.getString("soBeneficio");
        String nuSoBeneficio = jsonObject.getString("nuSoBeneficio");
        feNacimiento = jsonObject.getString("feNacimiento");
        genero = jsonObject.getString("genero");
        esMarital = jsonObject.getString("esMarital");
        String feIniVigencia = jsonObject.getString("feIniVigencia");
        String feFinVigencia = jsonObject.getString("feFinVigencia");
        noPaContratante = jsonObject.getString("noPaContratante");
        noMaContratante = jsonObject.getString("noMaContratante");
        noContratante = jsonObject.getString("noContratante");
        tiDoContratante = jsonObject.getString("tiDoContratante");
        idReContratante = jsonObject.getString("idReContratante");
        coReContratante = jsonObject.getString("coReContratante");
        String caTitular = jsonObject.getString("caTitular");
        String noPaTitular = jsonObject.getString("noPaTitular");
        String noTitular = jsonObject.getString("noTitular");
        String coAfTitular = jsonObject.getString("coAfTitular");
        String noMaTitular = jsonObject.getString("noMaTitular");
        String tiDoTitular = jsonObject.getString("tiDoTitular");
        String nuDoTitular = jsonObject.getString("nuDoTitular");
        String feInsTitular = jsonObject.getString("feInsTitular");
        
        nuevoJsonObjectBuilder.add("detalleCobertura", nuevoDetalleCoberturaArrayBuilder);
        nuevoJsonObjectBuilder.add("idCorrelativo", JsonValue.NULL);
        nuevoJsonObjectBuilder.add("noTransaccion", JsonValue.NULL);
        nuevoJsonObjectBuilder.add("idReceptor", JsonValue.NULL);
        nuevoJsonObjectBuilder.add("idRemitente", JsonValue.NULL);
        nuevoJsonObjectBuilder.add("nuControl", JsonValue.NULL);
        nuevoJsonObjectBuilder.add("nuControlIST", JsonValue.NULL);
        nuevoJsonObjectBuilder.add("hoTransaccion", horaTransaccion);
        nuevoJsonObjectBuilder.add("idTransaccion", "257");
        nuevoJsonObjectBuilder.add("edadPaciente", edad);      
        nuevoJsonObjectBuilder.add("descProducto", deProducto);
        nuevoJsonObjectBuilder.add("feAfiliacion", fechaAfiliacion);
        nuevoJsonObjectBuilder.add("feFinVigenciaForm", fechaFinVigencia);
        nuevoJsonObjectBuilder.add("feIniVigenciaForm", fechaIniVigencia);
        nuevoJsonObjectBuilder.add("tipoDeAtencion", Integer.valueOf(4));
        nuevoJsonObjectBuilder.add("tipoPaciente", Integer.valueOf(2));
        nuevoJsonObjectBuilder.add("coberturaDescripcion", String.valueOf("CONSULTA AMBULATORIA"));

        JsonObject nuevoJsonObject = nuevoJsonObjectBuilder.build();   


        String nroDocTitular = jsonObject.getString("nuDoPaciente");
        //String coMoneda = jsonObject.getString("coMoneda");
        
        JsonArray detalleCoberturaArray = nuevoJsonObject.getJsonArray("detalleCobertura");
        JsonObject cobertura = detalleCoberturaArray.getJsonObject(0);
        
        String nuCobertura = cobertura.getString("nuCobertura");
        
        String caServicio = "1";
        String coCalServicio = cobertura.getString("coCalServicio");
        String beMaxInicial = cobertura.getString("beMaxInicial");
        String coTiCobertura = cobertura.getString("coTiCobertura");
        String coSuTiCobertura = cobertura.getString("coSubTiCobertura");
    
        String usrSiteds = GlobalConstants.SITEDS_USER;
        dtoSolAuto.setSitedsNonce(GlobalConstants.SITEDS_NONCE);
        dtoSolAuto.setSitedsUser(GlobalConstants.SITEDS_USER);
        dtoSolAuto.setSitedsPassword(GlobalConstants.SITEDS_PASSWORD);
        dtoSolAuto.setIdRemitente(GlobalConstants.SITEDS_ID_REMITENTE);
        dtoSolAuto.setNuRucRemitente(GlobalConstants.SITEDS_NU_RUC_IPRESS);
        dtoSolAuto.setIdReceptor(codIafa);
        dtoSolAuto.setTiCaContratante(tiCaContratante);
        dtoSolAuto.setCaPaciente("1");
        dtoSolAuto.setTiDoPaciente(tipoDocPaciente);
        dtoSolAuto.setNuDoPaciente(numeroDocPaciente);
        dtoSolAuto.setApPaternoPaciente(apPaternoPaciente);
        dtoSolAuto.setApMaternoPaciente(apMaternoPaciente);
        dtoSolAuto.setNoPaciente(nombrePaciente);

        dtoSolAuto.setCoAdmisionista(GlobalConstants.SITEDS_ADMISION);

        dtoSolAuto.setCoAfPaciente(coAfPaciente);
        dtoSolAuto.setCoEsPaciente(coEsPaciente);

        dtoSolAuto.setNuIdenEmpleador("00001");
        
        dtoSolAuto.setNuContratoPaciente(nuContratoPaciente);
        dtoSolAuto.setNuPoliza(nuContratoPaciente);
        
        dtoSolAuto.setNuCertificado(nuCertificado);
        dtoSolAuto.setCoTiPolizaAfiliacion(coTiPoliza);
        dtoSolAuto.setCoProducto(coProducto);

        dtoSolAuto.setDeProducto(coDescripcion);
        dtoSolAuto.setNuPlan(nuPlan);
        dtoSolAuto.setTiPlanSalud(tiPlanSalud);
        dtoSolAuto.setCoMoneda(coMoneda);
        dtoSolAuto.setCoParentesco(coParentesco);
        dtoSolAuto.setSoBeneficio(soBeneficio);
        dtoSolAuto.setNuSoBeneficio(nuSoBeneficio);
        dtoSolAuto.setCoEspecialidad("");
        dtoSolAuto.setCoEspecialidad("DSC");
        dtoSolAuto.setFeNacimiento(feNacimiento);
        dtoSolAuto.setGenero(genero);
        dtoSolAuto.setEsMarital(esMarital);
        dtoSolAuto.setFeIniVigencia(feIniVigencia);
        dtoSolAuto.setFeFinVigencia(feFinVigencia);
        dtoSolAuto.setEsCobertura("");
        dtoSolAuto.setNuDecAccidente("");
        dtoSolAuto.setIdInfAccidente("");
        dtoSolAuto.setDeTiAccidente("");
        dtoSolAuto.setFeOcuAccidente("");
        dtoSolAuto.setNuAtencion("");
        dtoSolAuto.setIdDerFarmacia("");
        dtoSolAuto.setTiProducto("");
        dtoSolAuto.setDeProductoDeFarmacia("");
        dtoSolAuto.setFeAtencion("");
        int nroDoc = (coReContratante != null ? coReContratante.trim().length() : 0);
        dtoSolAuto.setCaContratante((nroDoc == 8) ? "1" : "2");//Tipo  Calificador  ( 1 Persona ,2 Non-Person  )
        dtoSolAuto.setNoPaContratante(noPaContratante);
        dtoSolAuto.setNoContratante(noContratante);
        dtoSolAuto.setNoMaContratante(noMaContratante);
        dtoSolAuto.setTiDoContratante(tiDoContratante);
        dtoSolAuto.setIdReContratante(idReContratante);
        dtoSolAuto.setCoReContratante(coReContratante);
        dtoSolAuto.setCaTitular(caTitular);
        dtoSolAuto.setNoPaTitular(noPaTitular);
        dtoSolAuto.setNoTitular(noTitular);
        dtoSolAuto.setCoAfTitular(coAfTitular);
        dtoSolAuto.setNoMaTitular(noMaTitular);
        dtoSolAuto.setTiDoTitular(tiDoTitular);
        dtoSolAuto.setIdReTitular("");//Identificador  Calificador  de Referencia  (XX5   Código  para una Organización  o PIN:  4A)
        dtoSolAuto.setNuDoTitular(nuDoTitular);
        dtoSolAuto.setFeIncTitular(feInsTitular);
        /*for (RegistroAdmSitedsCobeBean det : tRegistro.getDetalleCobertura()) {
            if (det.getCodCobertura().equals(tRegistro.getCodCobSel())) {
                dtoSolAuto.setNuCobertura(det.getData().getNuCobertura());
                dtoSolAuto.setObsCobertura("");
                dtoSolAuto.setMsgObs("");
                dtoSolAuto.setMsgConEspeciales("");
                dtoSolAuto.setNuCobPreExistencia("");
                dtoSolAuto.setBeMaxInicial(det.getData().getBeMaxInicial());
                dtoSolAuto.setCanServicio("1");
                dtoSolAuto.setIdDeProducto(det.getData().getIdProducto());
                dtoSolAuto.setCoTiCobertura(det.getData().getCoTiCobertura());
                dtoSolAuto.setCoSubTiCobertura(det.getData().getCoSubTiCobertura());
                dtoSolAuto.setMsgObsPre(det.getData().getMsgObs());
                dtoSolAuto.setMsgConEspecialesPre(det.getData().getMsgConEspeciales());
                dtoSolAuto.setCoTiMoneda(det.getData().getCoTiMoneda());
                dtoSolAuto.setCoPagoFijo(det.getData().getCoPagoFijo());
                dtoSolAuto.setCoCalServicio(det.getData().getCoCalServicio());
                dtoSolAuto.setCanCalServicio(det.getData().getCanCalServicio());
                dtoSolAuto.setCoPagoVariable(det.getData().getCoPagoVariable());
                dtoSolAuto.setFlagCG(det.getData().getFlagCaGarantia());
                dtoSolAuto.setDeflagCG(det.getData().getDeflagCaGarantia());
                dtoSolAuto.setFeFinCarencia("");
                if (det.getData().getCarenciaFechaFin() != null) {
                    dtoSolAuto.setFeFinCarencia(det.getData().getCarenciaFechaFin());
                }
                dtoSolAuto.setFeFinEspera(det.getData().getFeFinEspera());
                System.out.println("COBERTURA :::::: " + det.getData().toString());
                break;
            }
        }*/
        List<SitedsSolAutorizacionProEspDto> detProEsp = new LinkedList<SitedsSolAutorizacionProEspDto>();
        List<SitedsSolAutorizacionExeCarDto> detExeCar = new LinkedList<SitedsSolAutorizacionExeCarDto>();
        List<SitedsSolAutorizacionTieEspDto> detTieEsp = new LinkedList<SitedsSolAutorizacionTieEspDto>();
        List<SitedsSolAutorizacionRestricDto> detRestric = new LinkedList<SitedsSolAutorizacionRestricDto>();
        List<SitedsSolAutorizacionDetalleRes> detDetalleRes = new LinkedList<SitedsSolAutorizacionDetalleRes>();
        SitedsSolAutorizacionProEspDto proEsp = null;
        SitedsSolAutorizacionExeCarDto exeCar = null;
        SitedsSolAutorizacionTieEspDto tieEsp = null;
        SitedsSolAutorizacionRestricDto restric = null;
        SitedsSolAutorizacionDetalleRes detalleRes = null;
        proEsp = new SitedsSolAutorizacionProEspDto();
        proEsp.setCoInProcedimiento("1");
        detProEsp.add(0, proEsp);
        exeCar = new SitedsSolAutorizacionExeCarDto();
        exeCar.setCoExCarencia("1");
        detExeCar.add(0, exeCar);
        tieEsp = new SitedsSolAutorizacionTieEspDto();
        tieEsp.setCoTiEspera("1");
        detTieEsp.add(0, tieEsp);
        for (int i = 0; i < 5; i++) {
            restric = new SitedsSolAutorizacionRestricDto();
            detRestric.add(0, restric);
        }
       
        
        for (int i = 0; i < 5; i++) {
        	detalleRes = new SitedsSolAutorizacionDetalleRes();
        	detDetalleRes.add(0, detalleRes);
        }
        
        
        /*if (registro.getProcEsp() != null && registro.getProcEsp().getDetalleCopagoDife() != null && !registro.getProcEsp().getDetalleCopagoDife().isEmpty()
            && (codIafa.equals("20002") || codIafa.equals("40004"))) {//20002: PACIFICO EPS, 40004: PACIFICO VIDA
            detProEsp = new LinkedList<SitedsSolAutorizacionProEspDto>();
            detExeCar = new LinkedList<SitedsSolAutorizacionExeCarDto>();
            detTieEsp = new LinkedList<SitedsSolAutorizacionTieEspDto>();
            //PR:preexistencia, EX:exclusiones, CA:carencias, CM:observaciones, EN:enfermedad
            for (RegistroAdmSitedsCdifBean det : registro.getProcEsp().getDetalleCopagoDife()) {
                proEsp = new SitedsSolAutorizacionProEspDto();
                proEsp.setCaConAmbulatoria(det.getRegistro().getTiNuDias());
                proEsp.setCoInProcedimiento(det.getRegistro().getCoInProcedimiento());
                proEsp.setCoTiProConAmbulatoria(det.getRegistro().getCoProcedimiento());
                proEsp.setFrConAmbulatoria(det.getRegistro().getNuFrecuencia());
                proEsp.setGeConAmbulatoria(det.getRegistro().getCoSexo());
                proEsp.setImDeducible(det.getRegistro().getImDeducible());
                proEsp.setMsgConAmbulatoria(det.getRegistro().getTeMsgObservacion());
                proEsp.setNuPlanConAmbulatoria(registro.getNuPlan());
                proEsp.setPoConAmbulatoria(det.getRegistro().getPoCuExDecimal());
                detProEsp.add(proEsp);
            }
        }*/
        
        dtoSolAuto.setDetalleProEsp(detProEsp);
        dtoSolAuto.setDetalleTieEsp(detTieEsp);
        dtoSolAuto.setDetalleExeCar(detExeCar);
        dtoSolAuto.setDetalleRestric(detRestric);
        dtoSolAuto.setDetalleRes(detDetalleRes);
        dtoSolAuto.setCaRegafi("");
        dtoSolAuto.setNoPaRegafi("");
        dtoSolAuto.setNoRegafi("");
        dtoSolAuto.setCoAfRegafi("");
        dtoSolAuto.setNoMaRegafi("");
        dtoSolAuto.setTiDoRegafi("");
        dtoSolAuto.setNuDoRegafi("");
        dtoSolAuto.setFeNaRegafi("");
        dtoSolAuto.setGeRegafi("");
        dtoSolAuto.setCoPaisRegafi("");
        dtoSolAuto.setIdReRegafi("");
        
  	  Map<String, Object> mapResponse = new HashMap<String, Object>();

  	  mapResponse.put("descripcion", "Se obtuvieron los resultados");
  	  mapResponse.put("informacionObtenida", nuevoJsonObject);
  	  //mapResponse.put("informacionFormateada", mapAsegurado);
  	  
  	  //mapResponse.put("informacionCondiciones", jsonArrayCondiciones);
  	  return dtoSolAuto;
    }
    
    
    
    @POST
    @Path("obtenerProcesos")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Map<String, Object> obtenerAutorizacion(Map<String, Object> mapAseguradora){
  	  HttpResponse responseData = null;
  	  JsonReader jsonReader = null;
  	  JsonObject jsonObject = null;  	       
  	  JsonArray jsonArray = null;
  	  String inputJson = "";
  	  String response = "";
  	  String tiDoPaciente = Mapo.mstring(mapAseguradora, "tiDoPaciente");
  	  String nuDoPaciente = Mapo.mstring(mapAseguradora, "nuDoPaciente");
  	  String coAfPaciente = Mapo.mstring(mapAseguradora, "coAfPaciente");
  	  String coProducto = Mapo.mstring(mapAseguradora, "coProducto");
  	  String deProducto = Mapo.mstring(mapAseguradora, "deProducto");
  	  String nuPlan = Mapo.mstring(mapAseguradora, "nuPlan");
  	  String tiCaContratante = Mapo.mstring(mapAseguradora, "tiCaContratante");
  	  String noPaContratante = Mapo.mstring(mapAseguradora, "noPaContratante");
  	  String noContratante = Mapo.mstring(mapAseguradora, "noContratante");
  	  String noMaContratante = Mapo.mstring(mapAseguradora, "noMaContratante");
  	  String tiDoContratante = Mapo.mstring(mapAseguradora, "tiDoContratante");
  	  String idReContratante = Mapo.mstring(mapAseguradora, "idReContratante");
  	  String coReContratante = Mapo.mstring(mapAseguradora, "coReContratante");
  	  String coParentesco = Mapo.mstring(mapAseguradora, "coParentesco");
  	  String codIafa = Mapo.mstring(mapAseguradora, "codIafa");
  	String idReceptor = codIafa;
  	String nuCobertura = Mapo.mstring(mapAseguradora, "nuCobertura");
  	String caServicio = Mapo.mstring(mapAseguradora, "caServicio");
  	String coCalServicio = Mapo.mstring(mapAseguradora, "coCalServicio");
  	String beMaxInicial = Mapo.mstring(mapAseguradora, "beMaxInicial");
  	String coTiCobertura = Mapo.mstring(mapAseguradora, "coTiCobertura");
  	String coSuTiCobertura = Mapo.mstring(mapAseguradora, "coSuTiCobertura");
  	  
  	  inputJson = String.format("{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"tiDocumento\":\"%s\",\"nuDocumento\":\"%s\",\"coAfPaciente\":\"%s\",\"coProducto\":\"%s\",\"deProducto\":\"%s\",\"nuPlan\":\"%s\",\"tiCaContratante\":\"%s\",\"noPaContratante\":\"%s\",\"noContratante\":\"%s\",\"noMaContratante\":\"%s\",\"tiDoContratante\":\"%s\",\"idReContratante\":\"%s\",\"coReContratante\":\"%s\",\"nuRucRemitente\":\"%s\"}", 
        		GlobalConstants.SITEDS_NONCE,
        		GlobalConstants.SITEDS_PASSWORD,
        		GlobalConstants.SITEDS_USER, 
        		GlobalConstants.SITEDS_ID_REMITENTE, 
 				 idReceptor, 
 				 tiDoPaciente, 
 				 nuDoPaciente, 
 				 coAfPaciente, 
 				 coProducto, 
 				 deProducto,
 				 nuPlan, 
 				 tiCaContratante, 
 				 noPaContratante, 
 				 noContratante, 
 				 noMaContratante, 
 				 tiDoContratante, 
 				 idReContratante, 
 				 coReContratante,
 				 GlobalConstants.SITEDS_NU_RUC_IPRESS
 			);
			try {
		        System.out.println("JSON CONDICIONES MEDICAS: " + inputJson);
				Map<String, String> headers = new HashMap<String, String>();
		        headers.put("Content-Type", "application/json; charset=utf-8");
		        headers.put("Authorization", GlobalConstants.SITEDS_TOKEN);
		        //responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.SITEDS_OBSERVACIONES, inputJson, headers);
		        
		        responseData = sendPostRequest(GlobalConstants.SITEDS_ASEGURADO_CONDICIONES_MEDICAS, inputJson, headers);
		        
		        response = responseData.getResponseBody();
		        System.out.println("RESPONSE CONDICIONES MEDICAS: " + response);
				
			} catch (Exception e) {
	            throw UtilResponse.rsException(Response.Status.BAD_REQUEST, "Error al consultar servicio qlCondicionMedica.");
			}
			
  	  
			if(responseData.getStatusCode() == 500) {
				throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio qlCondicionMedica.");
			}
			
			  jsonReader = Json.createReader(new StringReader(response));
			  JsonArray jsonArrayCondiciones = jsonReader.readArray();
		  	  //jsonObject = jsonReader.readObject();
		  	  
		  	  
        
        inputJson = String.format("{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"tiDocumento\":\"%s\",\"nuDocumento\":\"%s\",\"coAfPaciente\":\"%s\",\"coProducto\":\"%s\",\"deProducto\":\"%s\",\"coParentesco\":\"%s\",\"nuPlan\":\"%s\",\"tiCaContratante\":\"%s\",\"noPaContratante\":\"%s\",\"noContratante\":\"%s\",\"noMaContratante\":\"%s\",\"tiDoContratante\":\"%s\",\"idReContratante\":\"%s\",\"coReContratante\":\"%s\",\"nuRucRemitente\":\"%s\"}", 
        		GlobalConstants.SITEDS_NONCE,
        		GlobalConstants.SITEDS_PASSWORD,
        		GlobalConstants.SITEDS_USER, 
        		GlobalConstants.SITEDS_ID_REMITENTE, 
 				 idReceptor, 
 				 tiDoPaciente, 
 				 nuDoPaciente, 
 				 coAfPaciente, 
 				 coProducto, 
 				 deProducto, 
 				 coParentesco, 
 				 nuPlan, 
 				 tiCaContratante, 
 				 noPaContratante, 
 				 noContratante, 
 				 noMaContratante, 
 				 tiDoContratante, 
 				 idReContratante, 
 				 coReContratante,
 				GlobalConstants.SITEDS_NU_RUC_IPRESS
 			);
			try {
		        System.out.println("JSON OBSERVACIONES: " + inputJson);
				Map<String, String> headers = new HashMap<String, String>();
		        headers.put("Content-Type", "application/json; charset=utf-8");
		        headers.put("Authorization", GlobalConstants.SITEDS_TOKEN);
		        //responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.SITEDS_OBSERVACIONES, inputJson, headers);
		        
		        responseData = sendPostRequest(GlobalConstants.SITEDS_OBSERVACIONES, inputJson, headers);
		        
		        response = responseData.getResponseBody();
		        System.out.println("RESPONSE OBSERVACIONES: " + response);
				
			} catch (Exception e) {
	            throw UtilResponse.rsException(Response.Status.BAD_REQUEST, "Error al consultar servicio qoObserva.");
			}
			
  	  
			if(responseData.getStatusCode() == 500) {
				throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio qoObserva.");
			}
			
  	  jsonReader = Json.createReader(new StringReader(response));
  	  jsonObject = jsonReader.readObject();
  	  
  	  //String obsAsegurado = jsonObject.isNull("teMsgLibre1") ? null : jsonObject.getString("teMsgLibre1").trim().isEmpty() ? jsonObject.getString("teMsgLibre1").trim() + " " + jsonObject.getString("rptObs").trim() : 							!jsonObject.isNull("rptObs") ? jsonObject.getString("rptObs").trim() : "";
  	  //String obsAdicional = jsonObject.isNull("teMsgLibre2") ? null : jsonObject.getString("teMsgLibre2").trim().isEmpty() ? "" : jsonObject.getString("teMsgLibre2") ;	  
	  	String teMsgLibre1 = jsonObject.isNull("teMsgLibre1") ? "" : jsonObject.getString("teMsgLibre1").trim();
	  	String rptObs = jsonObject.isNull("rptObs") ? "" : jsonObject.getString("rptObs").trim();
	  	String obsAsegurado = teMsgLibre1.isEmpty() ? rptObs : teMsgLibre1 + " " + rptObs;
	  	String obsAdicional = jsonObject.isNull("teMsgLibre2") ? "" : jsonObject.getString("teMsgLibre2").trim();
	  	obsAsegurado = limpiarTexto(obsAsegurado);
	  	obsAdicional = limpiarTexto(obsAdicional);
  	  response = "";
  	  if(codIafa.equals("40004") || codIafa.equals("20002")) { 		  
  		  inputJson = String.format("{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"tiDocumento\":\"%s\",\"nuDocumento\":\"%s\",\"coAfPaciente\":\"%s\",\"coProducto\":\"%s\",\"deProducto\":\"%s\",\"nuPlan\":\"%s\",\"tiCaContratante\":\"%s\",\"noPaContratante\":\"%s\",\"noContratante\":\"%s\",\"noMaContratante\":\"%s\",\"tiDoContratante\":\"%s\",\"idReContratante\":\"%s\",\"coReContratante\":\"%s\",\"nuCobertura\":\"%s\",\"deCobertura\":\"%s\",\"caServicio\":\"%s\",\"coCalservicio\":\"%s\",\"beMaxInicial\":\"%s\",\"coTiCobertura\":\"%s\",\"coSuTiCobertura\":\"%s\",\"coAplicativoTx\":\"%s\",\"coEspecialidad\":\"%s\",\"coParentesco\":\"%s\",\"nuRucRemitente\":\"%s\"}",
  				  GlobalConstants.SITEDS_NONCE,
  				  GlobalConstants.SITEDS_PASSWORD,
  				  GlobalConstants.SITEDS_USER, 
  				  GlobalConstants.SITEDS_ID_REMITENTE, 
  				  idReceptor, 
  				  tiDoPaciente, 
  				  nuDoPaciente, 
  				  coAfPaciente, 
  				  coProducto, 
  				  deProducto, 
  				  nuPlan, 
  				  tiCaContratante, 
  				  noPaContratante, 
  				  noContratante, 
  				  noMaContratante, 
  				  tiDoContratante, 
  				  idReContratante, 
  				  coReContratante, 
  				  nuCobertura, 
  				  deCobertura, 
  				  caServicio, 
  				  coCalServicio, 
  				  beMaxInicial, 
  				  coTiCobertura, 
  				  coSuTiCobertura, 
  				  coAplicativoTx, 
  				  coEspecialidad, 
  				  coParentesco,
  				  GlobalConstants.SITEDS_NU_RUC_IPRESS 				  
  				  );
  		  try {
  			  System.out.println("JSON PROCEDIMIENTOS: " + inputJson);
  			  Map<String, String> headers = new HashMap<String, String>();
  			  headers.put("Content-Type", "application/json; charset=utf-8");
  			  headers.put("Authorization", GlobalConstants.SITEDS_TOKEN);
  			  //responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.SITEDS_PROCEDIMIENTOS, inputJson, headers);
  			  
  			  responseData = sendPostRequest(GlobalConstants.SITEDS_PROCEDIMIENTOS, inputJson, headers);
  			  
  			  response = responseData.getResponseBody();
  			  System.out.println("RESPONSE PROCEDIMIENTOS: " + response);
  			  
  		  } catch (Exception e) {
  			  response = null;
  			  //throw UtilResponse.rsException(Response.Status.BAD_REQUEST, "Error al consultar servicio qlProc.");
  		  }  	
  		  
  		  if(responseData.getStatusCode() == 500) {
  			  response = null;
  			  //throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio qlProc.");
  		  }
  	  }
  	  
  	  
	  	Map<String, Object> mapDatosProcedimientos = new HashMap<>();
	  	List<Map<String, Object>> lstCpDiferen = new LinkedList<>();
	  	List<Map<String, Object>> lstExCarencia = new LinkedList<>();
	  	List<Map<String, Object>> lstTiEspera = new LinkedList<>();
	  	String fechaVigFin = "";
	
	  	// Validar si response no es vacío ni nulo
	  	if (response != null && !response.trim().isEmpty()) {
	  	    try (JsonReader jsonReader1 = Json.createReader(new StringReader(response))) {
	  	        JsonArray aseguradoProcedimientos = jsonReader1.readArray();
	
	  	        for (int i = 0; i < aseguradoProcedimientos.size(); i++) {
	  	            JsonObject procesos = aseguradoProcedimientos.getJsonObject(i);
	
	  	            if (!procesos.getString("coProcedimiento").trim().isEmpty()) {
	  	                Map<String, Object> regCpDif = new HashMap<>();
	  	                regCpDif.put("identificador", procesos.getString("coProcedimiento"));
	  	                regCpDif.put("procedimiento", "");
	  	                regCpDif.put("genero", procesos.getString("coSexo").trim().isEmpty() ? "F/M" : procesos.getString("coSexo"));
	  	                regCpDif.put("cpFijo", procesos.getString("imDeducible") + " POR ATENCIÓN");
	  	                regCpDif.put("deducible", procesos.getString("imDeducible"));
	  	                regCpDif.put("cpVariable", procesos.getString("poCuExDecimal") + " %");
	  	                regCpDif.put("coAseguro", procesos.getString("poCuExDecimal"));
	  	                regCpDif.put("frecuencia", procesos.getString("nuFrecuencia").trim().isEmpty() ? "0" : procesos.getString("nuFrecuencia"));
	  	                regCpDif.put("tiempo", procesos.getString("tiNuDias").trim().isEmpty() ? "0" : procesos.getString("tiNuDias"));
	  	                regCpDif.put("observacion", procesos.getString("teMsgObservacion"));
	  	                lstCpDiferen.add(regCpDif);
	  	            }
	
	  	            fechaVigFin = procesos.getString("feFinVigencia");
	
	  	            if (!procesos.getString("coExCarencia").trim().isEmpty()) {
	  	                Map<String, Object> regExCar = new HashMap<>();
	  	                regExCar.put("tipo", "CA");
	  	                regExCar.put("identificador", procesos.getString("coExCarencia"));
	  	                regExCar.put("descripcion", procesos.getString("deExCarencia"));
	  	                regExCar.put("observacion", procesos.getString("teMsgExCarencia"));
	  	                if (fechaVigFin != null && fechaVigFin.trim().length() == 8) {
	  	                    regExCar.put("fechaVigFin", UtilAppDate.convertStringToDate(fechaVigFin, "yyyyMMdd"));
	  	                }
	  	                lstExCarencia.add(regExCar);
	  	            }
	
	  	            if (!procesos.getString("coTiEspera").trim().isEmpty()) {
	  	                Map<String, Object> regTiEsp = new HashMap<>();
	  	                regTiEsp.put("tipo", "TE");
	  	                regTiEsp.put("identificador", procesos.getString("coTiEspera"));
	  	                regTiEsp.put("descripcion", procesos.getString("deTiEspera"));
	  	                regTiEsp.put("observacion", procesos.getString("teMsgTiEspera"));
	  	                if (fechaVigFin != null && fechaVigFin.trim().length() == 8) {
	  	                    regTiEsp.put("fechaVigFin", UtilAppDate.convertStringToDate(fechaVigFin, "yyyyMMdd"));
	  	                }
	  	                lstTiEspera.add(regTiEsp);
	  	            }
	  	        }
	  	    } catch (Exception e) {
	  	        System.err.println("Error al parsear JSON de procedimientos: " + e.getMessage());
	  	        // También puedes lanzar una excepción personalizada si lo deseas
	  	    }
	  	} else {
	  	    System.out.println("No se recibió respuesta de procedimientos.");
	  	}
	
	  	// Agregar datos a map
	  	mapDatosProcedimientos.put("detalleCoPagoDife", lstCpDiferen);
	
	  	if (!lstExCarencia.isEmpty()) {
	  	    Map<String, Object> procCond = new HashMap<>();
	  	    procCond.put("tipo", "CA");
	  	    procCond.put("descripcion", "EXCEPCIÓN A LA CARENCIA");
	  	    procCond.put("detalleCondicion", lstExCarencia);
	  	    mapDatosProcedimientos.put("detalleProcEspRes", procCond);
	  	}
	
	  	if (!lstTiEspera.isEmpty()) {
	  	    Map<String, Object> procCond = new HashMap<>();
	  	    procCond.put("tipo", "TE");
	  	    procCond.put("descripcion", "TIEMPO DE ESPERA");
	  	    procCond.put("detalleCondicion", lstTiEspera);
	  	    mapDatosProcedimientos.put("detalleProcEspRes", procCond);
	  	}
  	  
      
        
        Map<String, Object> mapAsegurado = new HashMap<String, Object>();
        mapAsegurado.put("obsAsegurado",obsAsegurado);
        mapAsegurado.put("obsAseguradoAdicional",obsAdicional);
        mapAsegurado.put("procedimientosCobertura", mapDatosProcedimientos);
  	  return mapAsegurado;
    }
    
    
    @POST
    @Path("autorizacionSiteds")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Map<String, Object> autorizacionSiteds(Map<String, Object> mapIn){
      Map<String, Object> mapResponse = new HashMap<String, Object>();
      //Map<String, Object> mapInfoSeguroPersona = new HashMap<String, Object>();
      List<Map<String, Object>> detalleCobertura = new ArrayList<Map<String,Object>>();
      Map<String, Object> mapInfo = new HashMap<String, Object>();
      mapInfo = (Map<String, Object>) mapIn.get("informacionSeguro");
      detalleCobertura = (List<Map<String, Object>>) mapInfo.get("detalleCobertura");
      Map<String, Object> mapCobertura = detalleCobertura.get(0);
      String codIafa = String.valueOf(mapIn.get("codIafa").toString());
      Integer codEmpresa = 1;
      Integer codASeguro = 1;
      Integer tipoAtencion = 4;
      String codCobertura = null;
      String dscCobertura = null;
      Double deducible = null;
      Double coaSeguro = 0.0;
      String nroAsegurado = null;
      //String edadPaciente = null;
      String fechaIniVigencia = null;
      String fechaFinVigencia = null;
      String nroSOrigen = null;
      String nroAccidente = null;
      String nroContrato = null;
      String nroCertificado = null;
      String restriccion = null;
      String finCarencia = null;
      String obsConDesp = null;
      String observacion = null;
      String observacionAsegurado = null;
      String observacionAsegAdicional = null;
      String nroPlan = null;
      String nomTitular = null;
      String nomEntidad = null;
      String direcMedico = null;
      //String jsonAsegurado = null;
      String fechaAfiliacion = null;
      Double cConsulta = null;
      String tipoAutorizacion = null;
      String tipoAutorizacion2 = null;
      String nroAutorizacion = null;
      String nroAutorizacion2 = null;
      String codPlanSalud = "0";
      String codParentesco = "1";
      String codProducto = null;
      String dscProducto = null;
      Integer codEntidad = 1;
      Integer codTitularPersona = null;
      Integer codAfiliacion = 1;
      Integer codECivil = -1;
      Integer tipoMoneda = 1;
      Integer fSiteds = 0;
      String descripcionEspecialidad = "";
      HttpResponse responseData = null;
      
	  	nroSOrigen = "";
	  	nroAccidente = "";
	  	//jsonAsegurado = (String) mapInfoSeguroPersona.get("fullCoberturaPaciente");
	  	cConsulta = 100.00;
	  	dscCobertura = (String) mapInfo.get("coberturaDescripcion").toString();
	  	//edadPaciente = mapInfoSeguroPersona.get("edadPaciente").toString();
	  	fechaIniVigencia = (String) mapInfo.get("feIniVigencia").toString();
	  	fechaFinVigencia = (String) mapInfo.get("feFinVigencia").toString();
	  	dscProducto = (String) mapInfo.get("descProducto").toString();
	  	fechaAfiliacion = (String) mapInfo.get("feAfiliacion").toString();
	  	observacion = "";
	  	observacionAsegurado = mapInfo.get("obsAsegurado").toString().trim().isEmpty() ? "0" : mapInfo.get("obsAsegurado").toString() ;
	  	observacionAsegAdicional = (String) mapInfo.get("obsAseguradoAdicional").toString();
	  	//direcMedico = "Dpto:              Provincia:             Distrito:                 Direccion:";
	  	
	  	
	  	nomTitular = mapInfo.get("noPaTitular").toString() + " " +  mapInfo.get("noMaTitular").toString() + " " + mapInfo.get("noTitular").toString();
	  	nomEntidad = mapInfo.get("noPaContratante").toString();
	  	nroAsegurado = mapInfo.get("coAfPaciente").toString();
	  	codParentesco = mapInfo.get("coParentesco").toString();
	  	nroContrato = mapInfo.get("nuContratoPaciente").toString().trim().isEmpty() ? mapInfo.get("nuPoliza").toString() : mapInfo.get("nuContratoPaciente").toString() ;
	  	nroCertificado = mapInfo.get("nuCertificado").toString();
	  	nroPlan = mapInfo.get("nuPlan").toString();
	  	codPlanSalud = mapInfo.get("tiPlanSalud").toString();
	  	codProducto = mapInfo.get("coProducto").toString();
	  	codAfiliacion = Integer.valueOf(mapInfo.get("coTiPoliza").toString());
	  	codECivil = Integer.valueOf(mapInfo.get("esMarital").toString().isEmpty() ? "0" : mapInfo.get("esMarital").toString());
	  	tipoMoneda = Integer.valueOf(mapInfo.get("coMoneda").toString());
	  	
	  	codCobertura = mapCobertura.get("coberturaCodigo").toString();
	  	restriccion = mapCobertura.get("coInRestriccion").toString();
	  	deducible = Double.valueOf(mapCobertura.get("deducible").toString());
	  	coaSeguro = Double.valueOf(mapCobertura.get("coaseguro").toString()); //coSubTiCobertura
	  	
	  	String caPaciente = "1";
	  	String idReceptor = codIafa;
	  	String tiDoPaciente = mapInfo.get("tiDoPaciente").toString();
	  	String nuDoPaciente = mapInfo.get("nuDoPaciente").toString();
	  	String apPaternoPaciente = mapInfo.get("apPaternoPaciente").toString();
	  	String apMaternoPaciente = mapInfo.get("apMaternoPaciente").toString();
	  	String noPaciente = mapInfo.get("noPaciente").toString();
	  	String coAdmisionista = GlobalConstants.SITEDS_ADMISION;
	  	String coAfPaciente = mapInfo.get("coAfPaciente").toString();
	  	String coEsPaciente = mapInfo.get("coEsPaciente").toString();
	  	String nuIdEmpleador = "00001";
	  	String nuContratoPaciente = mapInfo.get("nuContratoPaciente").toString();
	  	String nuPoliza = mapInfo.get("nuPoliza").toString();
	  	String nuCertificado = mapInfo.get("nuCertificado").toString();
	  	String coTiPoliza = mapInfo.get("coTiPoliza").toString();
	  	String coProducto = mapInfo.get("coProducto").toString();
	  	String deProducto = mapInfo.get("deProducto").toString();
	  	String nuPlan = mapInfo.get("nuPlan").toString();
	  	String tiPlanSalud = mapInfo.get("tiPlanSalud").toString();
	  	String coMoneda = mapInfo.get("coMoneda").toString();
	  	String coParentesco = mapInfo.get("coParentesco").toString();
	  	String soBeneficio = mapInfo.get("soBeneficio").toString();
	  	String nuSoBeneficio = mapInfo.get("nuSoBeneficio").toString();
	  	String coEspecialidad =  "";	
	  	descripcionEspecialidad = "";	
	  	String feNacimiento = mapInfo.get("feNacimiento").toString();
	  	String genero = mapInfo.get("genero").toString();
	  	String esMarital = mapInfo.get("esMarital").toString();
	  	String feIniVigencia = mapInfo.get("feIniVigencia").toString();
	  	String feFinVigencia = mapInfo.get("feFinVigencia") != null ?mapInfo.get("feFinVigencia").toString() : "" ;
	  	String esCobertura = "";
	  	String nuDecAccidente = "";
	  	String idInfAccidente = "";
	  	String deTiAccidente = "";
	  	String feAfiliacion = "";
	  	String feOcuAccidente = "";
	  	String nuAtencion = "";
	  	String idDerFarmacia = "";
	  	String tiProducto = "";
	  	String deProductoDeFarmacia = "";
	  	String feAtencion = "";
	  	int nroDoc = (mapInfo.get("coReContratante") != null ? mapInfo.get("coReContratante").toString().trim().length() : 0);
	  	String caContrantante = (nroDoc == 8) ? "1" : "2";
	  	String noPaContratante = mapInfo.get("noPaContratante").toString();
	  	String noMaContratante = mapInfo.get("noMaContratante").toString();
	  	String noContratante = mapInfo.get("noContratante").toString();
	  	String tiDoContratante = mapInfo.get("tiDoContratante").toString();
	  	String idReContratante = mapInfo.get("idReContratante").toString();
	  	String coReContratante = mapInfo.get("coReContratante").toString();
	  	String caTitular = mapInfo.get("caTitular").toString();
	  	String noPaTitular = mapInfo.get("noPaTitular").toString();
	  	String coAfTitular = mapInfo.get("coAfTitular").toString();
	  	String noMaTitular = mapInfo.get("noMaTitular").toString();
	  	String tiDoTitular = mapInfo.get("tiDoTitular").toString();
	  	String noTitular =  mapInfo.get("noTitular").toString();
	  	String idReTitular = "";
	  	String nuDoTitular = mapInfo.get("nuDoTitular").toString();
	  	String feInsTitular = mapInfo.get("feInsTitular").toString();
	  	
	  	String nuCobertura = mapCobertura.get("coberturaCodigo").toString();
	  	String obsCobertura = "";
	  	String msgObs = "";
	  	String msgConEspeciales= "";
	  	String nuCobPreExistencia = "";
	  	String beMaxInicial = mapCobertura.get("beMaxInicial").toString();
	  	String canServicio = "1";
	  	String idProducto = mapCobertura.get("idProducto").toString();
	  	String coTiCobertura = mapCobertura.get("coTiCobertura").toString();
	  	String coSubTiCobertura = mapCobertura.get("coSubTiCobertura").toString();
	  	String msgObsPre = mapCobertura.get("msgObs").toString();
	  	String msgConEspecialesPre = mapCobertura.get("msgConEspeciales").toString();
	  	String coTiMoneda = mapCobertura.get("coTiMoneda").toString();
	  	String coPagoFijo = mapCobertura.get("coPagoFijo").toString();
	  	String coCalServicio = mapCobertura.get("coCalServicio").toString();
	  	String canCalServicio = mapCobertura.get("canCalServicio").toString();
	  	String coPagoVariable = mapCobertura.get("coPagoVariable").toString();
	  	String flagCG = mapCobertura.get("flagCaGarantia").toString();
	  	String detFlagCG = mapCobertura.get("deflagCaGarantia").toString();
	  	String feFinCarencia = mapCobertura.get("carenciaFechaFin") != null ? mapCobertura.get("carenciaFechaFin").toString() : "";
	  	String feFinEspera = mapCobertura.get("feFinEspera").toString();
	  	String detalleProEsp = "[{\"coInProcedimiento\":\"1\",\"coTiProConAmbulatoria\":\"\",\"nuPlanConAmbulatoria\":\"\",\"imDeducible\":\"\",\"poConAmbulatoria\":\"\",\"frConAmbulatoria\":\"\",\"geConAmbulatoria\":\"\",\"caConAmbulatoria\":\"\",\"msgConAmbulatoria\":\"\"}]";
	  	String detalleTieEsp = "[{\"coTiEspera\":\"1\",\"idTiEspera\":\"\",\"deTiEspera\":\"\",\"feFinVigenciaTiEspera\":\"\",\"msgTiEspera\":\"\"}]";
	  	String detalleExeCar = "[{\"coExCarencia\":\"1\",\"idExCarencia\":\"\",\"deExCarencia\":\"\",\"msgExCarencia\":\"\"}]";
	  	String detalleRestric = "[{\"cie10Restricciones\":\"\",\"idRestricciones\":\"\",\"obsRestricciones\":\"\",\"deRestricciones\":\"\",\"msgRestricciones\":\"\",\"monTopeRestricciones\":\"\",\"feFinEsperaRestricciones\":\"\"},{\"cie10Restricciones\":\"\",\"idRestricciones\":\"\",\"obsRestricciones\":\"\",\"deRestricciones\":\"\",\"msgRestricciones\":\"\",\"monTopeRestricciones\":\"\",\"feFinEsperaRestricciones\":\"\"},{\"cie10Restricciones\":\"\",\"idRestricciones\":\"\",\"obsRestricciones\":\"\",\"deRestricciones\":\"\",\"msgRestricciones\":\"\",\"monTopeRestricciones\":\"\",\"feFinEsperaRestricciones\":\"\"},{\"cie10Restricciones\":\"\",\"idRestricciones\":\"\",\"obsRestricciones\":\"\",\"deRestricciones\":\"\",\"msgRestricciones\":\"\",\"monTopeRestricciones\":\"\",\"feFinEsperaRestricciones\":\"\"},{\"cie10Restricciones\":\"\",\"idRestricciones\":\"\",\"obsRestricciones\":\"\",\"deRestricciones\":\"\",\"msgRestricciones\":\"\",\"monTopeRestricciones\":\"\",\"feFinEsperaRestricciones\":\"\"}]";
	  	String caRegafi = "";
	  	String noPaRegafi = "";
	  	String noRegafi = "";
	  	String coAfRegafi = "";
	  	String noMaRegafi = "";
	  	String tiDoRegafi = "";
	  	String nuDoRegafi = "";
	  	String feNaRegafi = "";
	  	String geRegafi = "";
	  	String coPaisRegafi = "";
	  	String idReRegafi = "";
	  	String response = "";
	  	String jsonTemplate = "{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"apPaternoPaciente\":\"%s\",\"apMaternoPaciente\":\"%s\",\"noPaciente\":\"%s\",\"tiDoPaciente\":\"%s\",\"nuDoPaciente\":\"%s\",\"caPaciente\":\"%s\",\"coAfPaciente\":\"%s\",\"coEsPaciente\":\"%s\",\"coAdmisionista\":\"%s\",\"nuIdenEmpleador\":\"%s\",\"nuContratoPaciente\":\"%s\",\"nuPoliza\":\"%s\",\"nuCertificado\":\"%s\",\"coTiPolizaAfiliacion\":\"%s\",\"coProducto\":\"%s\",\"deProducto\":\"%s\",\"nuPlan\":\"%s\",\"tiPlanSalud\":\"%s\",\"coMoneda\":\"%s\",\"coParentesco\":\"%s\",\"soBeneficio\":\"%s\",\"nuSoBeneficio\":\"%s\",\"coEspecialidad\":\"%s\",\"feNacimiento\":\"%s\",\"genero\":\"%s\",\"esMarital\":\"%s\",\"feIniVigencia\":\"%s\",\"feFinVigencia\":\"%s\",\"esCobertura\":\"%s\",\"nuDecAccidente\":\"%s\",\"idInfAccidente\":\"%s\",\"deTiAccidente\":\"%s\",\"feAfiliacion\":\"%s\",\"feOcuAccidente\":\"%s\",\"nuAtencion\":\"%s\",\"idDerFarmacia\":\"%s\",\"tiProducto\":\"%s\",\"deProductoDeFarmacia\":\"%s\",\"feAtencion\":\"%s\",\"caContratante\":\"%s\",\"noPaContratante\":\"%s\",\"noMaContratante\":\"%s\",\"noContratante\":\"%s\",\"tiDoContratante\":\"%s\",\"idReContratante\":\"%s\",\"coReContratante\":\"%s\",\"caTitular\":\"%s\",\"noPaTitular\":\"%s\",\"noTitular\":\"%s\",\"coAfTitular\":\"%s\",\"noMaTitular\":\"%s\",\"tiDoTitular\":\"%s\",\"idReTitular\":\"%s\",\"nuDoTitular\":\"%s\",\"feIncTitular\":\"%s\",\"nuCobertura\":\"%s\",\"obsCobertura\":\"%s\",\"msgObs\":\"%s\",\"msgConEspeciales\":\"%s\",\"nuCobPreExistencia\":\"%s\",\"beMaxInicial\":\"%s\",\"canServicio\":\"%s\",\"idDeProducto\":\"%s\",\"coTiCobertura\":\"%s\",\"coSubTiCobertura\":\"%s\",\"msgObsPre\":\"%s\",\"msgConEspecialesPre\":\"%s\",\"coTiMoneda\":\"%s\",\"coPagoFijo\":\"%s\",\"coCalServicio\":\"%s\",\"canCalServicio\":\"%s\",\"coPagoVariable\":\"%s\",\"flagCG\":\"%s\",\"deflagCG\":\"%s\",\"feFinCarencia\":\"%s\",\"feFinEspera\":\"%s\",\"caRegafi\":\"%s\",\"noPaRegafi\":\"%s\",\"noRegafi\":\"%s\",\"coAfRegafi\":\"%s\",\"noMaRegafi\":\"%s\",\"tiDoRegafi\":\"%s\",\"nuDoRegafi\":\"%s\",\"feNaRegafi\":\"%s\",\"geRegafi\":\"%s\",\"coPaisRegafi\":\"%s\",\"idReRegafi\":\"%s\",\"detalleProEsp\":%s,\"detalleTieEsp\":%s,\"detalleExeCar\":%s,\"detalleRes\":%s,\"nuRucRemitente\":\"%s\"}";
	    String inputJson = String.format(jsonTemplate, 
	    		GlobalConstants.SITEDS_NONCE, 
	    		GlobalConstants.SITEDS_PASSWORD, 
	    		GlobalConstants.SITEDS_USER, 
	    		GlobalConstants.SITEDS_ID_REMITENTE, 
	    		idReceptor, 
	    		apPaternoPaciente, 
	    		apMaternoPaciente, 
	    		noPaciente, 
	    		tiDoPaciente, 
	    		nuDoPaciente, 
	    		caPaciente, 
	    		coAfPaciente, 
	    		coEsPaciente, 
	    		coAdmisionista, 
	    		nuIdEmpleador, 
	    		nuContratoPaciente, 
	    		nuPoliza, 
	    		nuCertificado, 
	    		coTiPoliza, 
	    		coProducto, 
	    		deProducto, 
	    		nuPlan, 
	    		tiPlanSalud, 
	    		coMoneda, 
	    		coParentesco, 
	    		soBeneficio, 
	    		nuSoBeneficio, 
	    		coEspecialidad, 
	    		feNacimiento, 
	    		genero, 
	    		esMarital, 
	    		feIniVigencia, 
	    		feFinVigencia,
	    		esCobertura, 
	    		nuDecAccidente, 
	    		idInfAccidente, 
	    		deTiAccidente, 
	    		feAfiliacion, 
	    		feOcuAccidente, 
	    		nuAtencion, 
	    		idDerFarmacia, 
	    		tiProducto, 
	    		deProductoDeFarmacia, 
	    		feAtencion, 
	    		caContrantante, 
	    		noPaContratante, 
	    		noMaContratante, 
	    		noContratante, 
	    		tiDoContratante, 
	    		idReContratante, 
	    		coReContratante, 
	    		caTitular, 
	    		noPaTitular, 
	    		noTitular, 
	    		coAfTitular, 
	    		noMaTitular, 
	    		tiDoTitular, 
	    		idReTitular, 
	    		nuDoTitular, 
	    		feInsTitular, 
	    		nuCobertura, 
	    		obsCobertura, 
	    		msgObs, 
	    		msgConEspeciales, 
	    		nuCobPreExistencia, 
	    		beMaxInicial, 
	    		canServicio, 
	    		idProducto, 
	    		coTiCobertura, 
	    		coSubTiCobertura, 
	    		msgObsPre, 
	    		msgConEspecialesPre, 
	    		coTiMoneda, 
	    		coPagoFijo, 
	    		coCalServicio, 
	    		canCalServicio, 
	    		coPagoVariable, 
	    		flagCG, 
	    		detFlagCG, 
	    		feFinCarencia, 
	    		feFinEspera, 
	    		caRegafi, 
	    		noPaRegafi, 
	    		noRegafi, 
	    		coAfRegafi, 
	    		noMaRegafi, 
	    		tiDoRegafi, 
	    		nuDoRegafi, 
	    		feNaRegafi, 
	    		geRegafi, 
	    		coPaisRegafi, 
	    		idReRegafi, 
	    		detalleProEsp, 
	    		detalleTieEsp, 
	    		detalleExeCar, 
	    		detalleRestric, 
	    		GlobalConstants.SITEDS_NU_RUC_IPRESS
	    		);
	      
	  		try {
	  	        System.out.println("JSON AUTORIZACION: " + inputJson);
	  			Map<String, String> headers = new HashMap<String, String>();
	  	        headers.put("Content-Type", "application/json; charset=utf-8");
	  	        headers.put("Authorization", GlobalConstants.SITEDS_TOKEN);
	  	        //responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.SITEDS_AUTORIZACION, inputJson, headers);
	  	      responseData = sendPostRequest(GlobalConstants.SITEDS_AUTORIZACION, inputJson, headers);
	  	        response = responseData.getResponseBody();
	  	        System.out.println("RESPONSE AUTORIZACION: " + response);
	  			
	  		} catch (Exception e) {
	  			throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar el servicio SoliAutorizacion.");
	  		}
	  	
			if(responseData.getStatusCode() == 500) {
				throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio SoliAutorizacion.");
			}
	  		
	  	JsonReader jsonReader = Json.createReader(new StringReader(response));
	  	JsonObject jsonObject = jsonReader.readObject();
	  	tipoAutorizacion = "01";
	  	nroAutorizacion = jsonObject.getString("nuAutorizacion");
	  	System.out.println("NUMERO AUTORIZACION: " + nroAutorizacion);
	  	mapResponse.put("tiAutorizacion", tipoAutorizacion);
	  	mapResponse.put("nroAutorizacion", nroAutorizacion);
	  	LocalDateTime fechaHoraActual = LocalDateTime.now();
        DateTimeFormatter formatoSalida = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	  	String noTransaccion = null;
	  	String feTransaccion = fechaHoraActual.format(formatoSalida);
	  	String nuRucRemitente = GlobalConstants.SITEDS_NU_RUC_IPRESS;
	  	String caReceptor = null;
	  	String deCobertura = "CONSULTA AMBULATORIA";
	  	String caResponsableAut = "2";
	  	String noPaResponsableAut= GlobalConstants.SITEDS_USER;
	  	String noResponsableAut= GlobalConstants.SITEDS_USER;
	  	String noMaResponsableAut= GlobalConstants.SITEDS_USER;
	  	String tiDoResponsableAut= "1";
	  	String nuDoResponsableAut= GlobalConstants.SITEDS_USER;
	  	String nuControl = "";
	  	String nuControlST = "";
	  	nuContratoPaciente = nuContratoPaciente.trim().isEmpty() ? nuPoliza : nuContratoPaciente ;
	  	formatoSalida = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	  	String feHoTransaccion = fechaHoraActual.format(formatoSalida);
	  	String hoTransaccion = fechaHoraActual.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
	    inputJson = String.format("{\"sitedsNonce\":\"%s\","
	    		+ "\"sitedsPassword\":\"%s\","
	    		+ "\"sitedsUser\":\"%s\","
	    		+ "\"idRemitente\":\"%s\","
	    		+ "\"idReceptor\":\"%s\","
	    		+ "\"tiDoPaciente\":\"%s\","
	    		+ "\"nuDoPaciente\":\"%s\","
	    		+ "\"coAfPaciente\":\"%s\","
	    		+ "\"coProducto\":\"%s\","
	    		+ "\"noTransaccion\":\"%s\","
	    		+ "\"feTransaccion\":\"%s\","
	    		+ "\"nuRucRemitente\":\"%s\","
	    		+ "\"caReceptor\":\"%s\","
	    		+ "\"caPaciente\":\"%s\","
	    		+ "\"coEsPaciente\":\"%s\","
	    		+ "\"nuContratoPaciente\":\"%s\","
	    		+ "\"coTiPolizaAfiliacion\":\"%s\","
	    		+ "\"nuPlan\":\"%s\","
	    		+ "\"coParentesco\":\"%s\","
	    		+ "\"feNacimiento\":\"%s\","
	    		+ "\"genero\":\"%s\","
	    		+ "\"feIniVigencia\":\"%s\","
	    		+ "\"nuCobertura\":\"%s\","
	    		+ "\"deCobertura\":\"%s\","
	    		+ "\"caContratante\":\"%s\","
	    		+ "\"tiDoContratante\":\"%s\","
	    		+ "\"idReContratante\":\"%s\","
	    		+ "\"rucContratante\":\"%s\","
	    		+ "\"coAfiliadoTitular\":\"%s\","
	    		+ "\"caResponsableAut\":\"%s\","
	    		+ "\"noPaResponsableAut\":\"%s\","
	    		+ "\"noResponsableAut\":\"%s\","
	    		+ "\"noMaResponsableAut\":\"%s\","
	    		+ "\"tiDoResponsableAut\":\"%s\","
	    		+ "\"nuDoResponsableAut\":\"%s\","
	    		+ "\"nuAutorizacion\":\"%s\","
	    		+ "\"feHoAutorizacion\":\"%s\","
	    		+ "\"beMaxInicial\":\"%s\","
	    		+ "\"coPagoFijo\":\"%s\","
	    		+ "\"coPagoVariable\":\"%s\","
	    		+ "\"flagCartaGarantia\":\"%s\","
	    		+ "\"deFlagCartaGarantia\":\"%s\","
	    		+ "\"nuControl\":\"%s\","
	    		+ "\"nuRucRemitente\":\"%s\","
	    		+ "\"nuControlST\":\"%s\","
	    		+ "\"hoTransaccion\":\"%s\","
	    		+ "\"apPaternoPaciente\":\"%s\","
	    		+ "\"apMaternoPaciente\":\"%s\","
	    		+ "\"noPaciente\":\"%s\"}",
	    		
	    		GlobalConstants.SITEDS_NONCE,
	    		GlobalConstants.SITEDS_PASSWORD, 
	    		GlobalConstants.SITEDS_USER, 
	    		GlobalConstants.SITEDS_ID_REMITENTE,
	    		idReceptor,
	    		tiDoPaciente,
	    		nuDoPaciente,
	    		coAfPaciente,
	    		coProducto,
	    		noTransaccion,
	    		feTransaccion,
	    		nuRucRemitente,
	    		caReceptor,
	    		caPaciente,
	    		coEsPaciente,
	    		nuContratoPaciente,
	    		coTiPoliza,
	    		nuPlan,
	    		coParentesco,
	    		feNacimiento,
	    		genero,
	    		feIniVigencia,
	    		nuCobertura,
	    		deCobertura,
	    		caContrantante,
	    		tiDoContratante,
	    		idReContratante,
	    		coReContratante,
	    		coAfTitular,
	    		caResponsableAut,
	    		noPaResponsableAut,
	    		noResponsableAut,
	    		noMaResponsableAut,
	    		tiDoResponsableAut,
	    		nuDoResponsableAut,
	    		nroAutorizacion,
	    		feHoTransaccion,
	    		beMaxInicial,
	    		coPagoFijo,
	    		coPagoVariable,
	    		flagCG,
	    		detFlagCG,
	    		nuControl,
	    		GlobalConstants.SITEDS_NU_RUC_IPRESS,
	    		nuControlST,
	    		hoTransaccion,
	    		apPaternoPaciente,
	    		apMaternoPaciente,
	    		noPaciente
	    );
	      
	  		try {
	  	        System.out.println("JSON ACREDITACION: " + inputJson);
	  			Map<String, String> headers = new HashMap<String, String>();
	  	        headers.put("Content-Type", "application/json; charset=utf-8");
	  	        headers.put("Authorization", GlobalConstants.SITEDS_TOKEN);
	  	        //responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.SITEDS_AUTORIZACIONß, inputJson, headers);
	  	      responseData = sendPostRequest(GlobalConstants.SITEDS_ACREDITACION, inputJson, headers);
	  	        response = responseData.getResponseBody();
	  	        System.out.println("RESPONSE ACREDITACION: " + response);
	  			
	  		} catch (Exception e) {
	  			throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar el servicio Acreditacion.");
	  		}
	  		
			if(responseData.getStatusCode() == 500) {
				throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio Acreditacion.");
			}
			
			
		  	/*jsonReader = Json.createReader(new StringReader(response));
		  	jsonObject = jsonReader.readObject();
		  	
		  	String trama = jsonObject.getString("peticion");
			
			String host = "app3.susalud.gob.pe";
			String channel= "CH.SUSALUD.IPRESS_LA";
			String port= "1430";
			String queueManager= "QM.999.998.AC";
			String queueIn= "QL.995.AC.002.3.IN";
			String queueOut= "QL.995.AC.002.3.OUT";
			String jmsType= "mcd://XMLNSC/[set]";
			String property= "JMS_IBM_Character_Set";
			
		    inputJson = String.format("{\"host\":\"%s\",\"channel\":\"%s\",\"port\":\"%s\",\"queueManager\":\"%s\",\"queueIn\":\"%s\",\"queueOut\":\"%s\",\"jmsType\":\"%s\",\"property\":\"%s\",\"mensaje\":\"<?xml version='1.0' encoding='utf-8'?><RegistroAutRequest xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xmlns:xsd='http://www.w3.org/2001/XMLSchema' xmlns='http://www.susalud.gob.pe/acreditacion/RegistroAutRequest.xsd'><txNombre xmlns=''>271_LOGACRE_INSERT</txNombre><codRemitente xmlns=''>%s</codRemitente><txPeticion xmlns=''>%s</txPeticion></RegistroAutRequest>\"}",
		    		host,
		    		channel,
		    		port,
		    		queueManager,
		    		queueIn,
		    		queueOut,
		    		jmsType,
		    		property,
		    		GlobalConstants.SITEDS_ID_REMITENTE,
		    		trama
		    );
		      
		  		try {
		  	        System.out.println("JSON ENVIAR RECIBIR : " + inputJson);
		  			Map<String, String> headers = new HashMap<String, String>();
		  	        headers.put("Content-Type", "application/json; charset=utf-8");
		  	        headers.put("Authorization", GlobalConstants.SITEDS_TOKEN);
		  	        //responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.SITEDS_AUTORIZACION, inputJson, headers);
		  	        responseData = sendPostRequest(GlobalConstants.SITEDS_ENVIARRECIBIR, inputJson, headers);
		  	        response = responseData.getResponseBody();
		  	        System.out.println("RESPONSE ENVIAR RECIBIR: " + response);
		  			
		  		} catch (Exception e) {
		  			throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar el servicio SoliAutorizacion.");
		  		}
		  	
				if(responseData.getStatusCode() == 500) {
					throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio SoliAutorizacion.");
				}*/
	  	
  	  return mapResponse;
    }
    
    
    public static void main(String[] args) {
    	
    	HttpResponse responseData = null;
    	String response = "";
        String inputJson = "{\r\n"
        		+ "	\"sitedsNonce\": \"UzjoeaTqa2iZnJtBksmQ1g==\",\r\n"
        		+ "	\"sitedsPassword\": \"UBq03oOvlvDOPVKEMACWRXAmvwFTjn8cj6tnI4zR8vLNIf6q04uEao78Uj+tt/PGIyisCQl6iqz0FzClGWlK/M3Qzw8T76Obs5TPC3yhHIEGxm4Ozq1ytgWrt+WopMZaPmItAfuM5Ff7/uCuRUTNzcwRgoqvU57ALBEpqMZwlAc=|704uuu49a7l2ul62au46798ss7694s4a3asu083a3lul54sl41u2s277sl53s357|Y1+LIyGpLP9QTZLQau0J0w==\",\r\n"
        		+ "	\"sitedsUser\": \"testv2\",\r\n"
        		+ "	\"idRemitente\": \"00015730\",\r\n"
        		+ "	\"idReceptor\": \"20001\",\r\n"
        		+ "	\"tiDocumento\": \"1\",\r\n"
        		+ "	\"nuDocumento\": \"40581340\",\r\n"
        		+ "	\"coAfPaciente\": \"3675355\",\r\n"
        		+ "	\"coProducto\": \"01\",\r\n"
        		+ "	\"deProducto\": \"AMC REGULAR\",\r\n"
        		+ "	\"coEspecialidad\": \"1\",\r\n"
        		+ "	\"coParentesco\": \"1\",\r\n"
        		+ "	\"nuPlan\": \"111378\",\r\n"
        		+ "	\"tiCaContratante\": \"2\",\r\n"
        		+ "	\"noPaContratante\": \"COMPAÑIA MINERA ANTAPACCAY S.A.\",\r\n"
        		+ "	\"noContratante\": \"COMPAÑIA MINERA ANTAPACCAY S.A.\",\r\n"
        		+ "	\"noMaContratante\": \"COMPAÑIA MINERA ANTAPACCAY S.A.\",\r\n"
        		+ "	\"tiDoContratante\": \"8\",\r\n"
        		+ "	\"idReContratante\": \"XX5\",\r\n"
        		+ "	\"coReContratante\": \"20114915026\"\r\n"
        		+ "}";
			try {
		        System.out.println("JSON ASEGURADO POR CODIGO: " + inputJson);
				Map<String, String> headers = new HashMap<String, String>();
		        headers.put("Content-Type", "application/json; charset=utf-8");
		        headers.put("Authorization", GlobalConstants.SITEDS_TOKEN);
		        System.out.println("HEADERS: " + headers);
		        /*responseData  = HttpRequestUtil.sendRequest("POST", GlobalConstants.SITEDS_ASEGURADO_CODIGO, inputJson, headers);
		        responseData = sendPostRequest(GlobalConstants.SITEDS_ASEGURADO_CODIGO, inputJson, Map.of(
	                    "Content-Type", "application/json; charset=utf-8",
	                    "Authorization", GlobalConstants.SITEDS_TOKEN // Reemplaza con tu token real
	                ));
		        response = responseData.getResponseBody();
		        System.out.println("RESPONSE ASEGURADO POR CODIGO: " + response);*/
				
			} catch (Exception e) {
	            throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio ConCod.");
			}
  	  
			if(responseData.getStatusCode() == 500) {
				throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio ConCod.");
			}
	}
    
    public static HttpResponse sendPostRequest(String urlString, String inputJson, Map<String, String> headers) throws Exception {
    	HttpRequestUtil.disableSslValidation();
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {          
            // Configurar la conexión
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(50000);
            connection.setReadTimeout(40000);
            // Agregar encabezados
            for (Map.Entry<String, String> header : headers.entrySet()) {
                connection.setRequestProperty(header.getKey(), header.getValue());
            }

            // Enviar datos JSON
            try (OutputStream os = connection.getOutputStream()) {
                os.write(inputJson.getBytes("UTF-8"));
                os.flush();
            }

            // Leer respuesta
            int responseCode = connection.getResponseCode();
            String responseBody = "";

            if (responseCode >= 200 && responseCode < 300) { // Respuesta exitosa
                try (InputStream is = connection.getInputStream()) {
                    // Alternativa compatible con Java 8
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) != -1) {
                        result.write(buffer, 0, length);
                    }
                    responseBody = new String(result.toByteArray(), "UTF-8");
                }
            }else { // Respuesta con error
                try (InputStream is = connection.getInputStream()) {
                    // Alternativa compatible con Java 8
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) != -1) {
                        result.write(buffer, 0, length);
                    }
                    responseBody = new String(result.toByteArray(), "UTF-8");
                }
            }

            // Devolver el resultado
            return new HttpResponse(Integer.valueOf(responseCode), responseBody, "");
		} 
        catch (SocketTimeoutException e) {
            throw new Exception("Tiempo de espera excedido al conectar con el servicio. Por favor, inténtelo más tarde.", e);
        }
        finally {
            if (connection != null) {
                connection.disconnect();  // Asegurar que la conexión se cierre
            }
		}

    }
    
    public static String limpiarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return texto
        	.replaceAll("[\\r\\n\\t]+", " ")  
        	.replaceAll("^[\\-\\s\\t]+", "")
        	.replaceAll("[\\-\\s\\t]+", " ")
        	.replaceAll("\\s+", " ") 
            .replaceAll("[\\r\\n]+", " ")   // elimina saltos de línea
            .replace("\"", "")              // elimina comillas dobles
            .replace("'", "")
            .trim();  
    }
    
    private String timeConvertToIso(String fechaObtenida) {
  	  if(fechaObtenida.trim().isEmpty() || fechaObtenida.equals("00000000")) {
  		  
  		  return "";
  	  }
        DateTimeFormatter formatoOriginal = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate fecha = LocalDate.parse(fechaObtenida, formatoOriginal);
        DateTimeFormatter formatoNuevo = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaFormateada = fecha.format(formatoNuevo);
        return fechaFormateada;
    }
    
}
