package pe.com.s2o.clinica.siteds;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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

    @POST
    @Path("obtenerDatosSitets")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Map<String, Object> obtenerDatosSitets(Map<String, Object> mapAseguradora){
      String codIafa = Mapo.mstring(mapAseguradora, "iafaAseguradora");
      //String apPaternoPaciente = Mapo.mstring(mapAseguradora, "apPaterno");
      //String apMaternoPaciente = Mapo.mstring(mapAseguradora, "apMaterno");
      String apPaternoPaciente = "";
      String apMaternoPaciente = "";
      String nombrePaciente = Mapo.mstring(mapAseguradora, "nombreCompleto");
      String tipoDocPaciente = Mapo.mstring(mapAseguradora, "tipoDocumento");
      String numeroDocPaciente = Mapo.mstring(mapAseguradora, "nroDocumento");
 
      System.out.println("USUARIO: " + GlobalSitedsConstants.SITEDS_USER + ", END POINT: " +  GlobalSitedsConstants.SITEDS_BASE);
      
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
      String coAplicativoTx = "123456";
	  HttpResponse responseData = null;
	  JsonReader jsonReader = null;
	  JsonObject jsonObject = null;  	       
	  JsonArray jsonArray = null;
	  
      inputJson = String.format("{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"caIPRESS\":\"%s\",\"noIPRESS\":\"%s\",\"tiDoIPRESS\":\"%s\",\"nuRucIPRESS\":\"%s\"}", 			GlobalSitedsConstants.SITEDS_NONCE, 
    		GlobalSitedsConstants.SITEDS_PASSWORD, 
    		GlobalSitedsConstants.SITEDS_USER, 
    		GlobalSitedsConstants.SITEDS_ID_REMITENTE, 
    		idReceptor, 
    		GlobalSitedsConstants.SITEDS_CA_IPRESS, 
    		GlobalSitedsConstants.SITEDS_NO_IPRESS, 
    		GlobalSitedsConstants.SITEDS_TI_DO_IPRESS, 
    		GlobalSitedsConstants.SITEDS_NU_RUC_IPRESS
    		  );
		try {
	        System.out.println("JSON ENTIDAD VINCULADA: " + inputJson);
			Map<String, String> headers = new HashMap<String, String>();
	        headers.put("Content-Type", "application/json; charset=utf-8");
	        headers.put("Authorization", GlobalSitedsConstants.SITEDS_TOKEN);
	        responseData  = HttpRequestUtil.sendRequest("POST", GlobalSitedsConstants.SITEDS_ENTIDAD_VINCULADA, inputJson, headers);
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
      
	  	  inputJson = String.format("{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"apPaternoPaciente\":\"%s\",\"apMaternoPaciente\":\"%s\",\"noPaciente\":\"%s\",\"tiDocumento\":\"%s\",\"nuDocumento\":\"%s\"}", 
	  			GlobalSitedsConstants.SITEDS_NONCE,
	  			GlobalSitedsConstants.SITEDS_PASSWORD,
	  			GlobalSitedsConstants.SITEDS_USER,
	  			GlobalSitedsConstants.SITEDS_ID_REMITENTE, 
	  			idReceptor, 
	  			apPaternoPaciente, 
	  			apMaternoPaciente, 
	  			nombrePaciente, 
	  			tipoDocPaciente, 
	  			numeroDocPaciente
	  	);
			try {
		        System.out.println("JSON ASEGURADO POR NOMBRE: " + inputJson);
				Map<String, String> headers = new HashMap<String, String>();
		        headers.put("Content-Type", "application/json; charset=utf-8");
		        headers.put("Authorization", GlobalSitedsConstants.SITEDS_TOKEN);
		        responseData  = HttpRequestUtil.sendRequest("POST", GlobalSitedsConstants.SITEDS_ASEGURADO_NOMBRE, inputJson, headers);
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
        for (int i = 0; i < jsonArray.size(); i++) {
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
      
        
        inputJson = String.format("{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"tiDocumento\":\"%s\",\"nuDocumento\":\"%s\",\"coAfPaciente\":\"%s\",\"coProducto\":\"%s\",\"deProducto\":\"%s\",\"coEspecialidad\":\"%s\",\"coParentesco\":\"%s\",\"nuPlan\":\"%s\",\"tiCaContratante\":\"%s\",\"noPaContratante\":\"%s\",\"noContratante\":\"%s\",\"noMaContratante\":\"%s\",\"tiDoContratante\":\"%s\",\"idReContratante\":\"%s\",\"coReContratante\":\"%s\"}", 
        		GlobalSitedsConstants.SITEDS_NONCE,
        		GlobalSitedsConstants.SITEDS_PASSWORD,
        		GlobalSitedsConstants.SITEDS_USER, 
        		GlobalSitedsConstants.SITEDS_ID_REMITENTE, 
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
        		coReContratante
        	);
			try {
		        System.out.println("JSON ASEGURADO POR CODIGO: " + inputJson);
				Map<String, String> headers = new HashMap<String, String>();
		        headers.put("Content-Type", "application/json; charset=utf-8");
		        headers.put("Authorization", GlobalSitedsConstants.SITEDS_TOKEN);
		        responseData = sendPostRequest(GlobalSitedsConstants.SITEDS_ASEGURADO_CODIGO, inputJson, Map.of(
	                    "Content-Type", "application/json; charset=utf-8",
	                    "Authorization", GlobalSitedsConstants.SITEDS_TOKEN // Reemplaza con tu token real
	                ));
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
                break;
            }
        }
        
        JsonObjectBuilder nuevoJsonObjectBuilder = Json.createObjectBuilder(jsonObject);
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatHora = new SimpleDateFormat("HH:mm:ss");
        String horaTransaccion = formatHora.format(calendar.getTime());
        nuevoJsonObjectBuilder.add("detalleCobertura", nuevoDetalleCoberturaArrayBuilder);
        nuevoJsonObjectBuilder.add("idCorrelativo", jsonValue.NULL);
        nuevoJsonObjectBuilder.add("noTransaccion", jsonValue.NULL);
        nuevoJsonObjectBuilder.add("idReceptor", jsonValue.NULL);
        nuevoJsonObjectBuilder.add("idRemitente", jsonValue.NULL);
        nuevoJsonObjectBuilder.add("nuControl", jsonValue.NULL);
        nuevoJsonObjectBuilder.add("nuControlIST", jsonValue.NULL);
        nuevoJsonObjectBuilder.add("hoTransaccion", horaTransaccion);
        nuevoJsonObjectBuilder.add("idTransaccion", "271");
        JsonObject nuevoJsonObject = nuevoJsonObjectBuilder.build();   

        String fechaIniVigencia = this.timeConvertToIso(jsonObject.getString("feIniVigencia"));
        String fechaFinVigencia = this.timeConvertToIso(jsonObject.getString("feFinVigencia"));
        String fechaAfiliacion = this.timeConvertToIso(jsonObject.getString("feInsTitular"));
        String nroDocTitular = jsonObject.getString("nuDoPaciente");
        String coMoneda = jsonObject.getString("coMoneda");
        
        JsonArray detalleCoberturaArray = nuevoJsonObject.getJsonArray("detalleCobertura");
        JsonObject cobertura = detalleCoberturaArray.getJsonObject(0);
        
        String nuCobertura = cobertura.getString("nuCobertura");
        String deCobertura = "CONSULTA AMBULATORIA";
        String caServicio = "1";
        String coCalServicio = cobertura.getString("coCalServicio");
        String beMaxInicial = cobertura.getString("beMaxInicial");
        String coTiCobertura = cobertura.getString("coTiCobertura");
        String coSuTiCobertura = cobertura.getString("coSubTiCobertura");
        String coEspecialidad = "";
        
        
        inputJson = String.format("{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"tiDocumento\":\"%s\",\"nuDocumento\":\"%s\",\"coAfPaciente\":\"%s\",\"coProducto\":\"%s\",\"deProducto\":\"%s\",\"coParentesco\":\"%s\",\"nuPlan\":\"%s\",\"tiCaContratante\":\"%s\",\"noPaContratante\":\"%s\",\"noContratante\":\"%s\",\"noMaContratante\":\"%s\",\"tiDoContratante\":\"%s\",\"idReContratante\":\"%s\",\"coReContratante\":\"%s\"}", 
        		GlobalSitedsConstants.SITEDS_NONCE,
        		GlobalSitedsConstants.SITEDS_PASSWORD,
        		GlobalSitedsConstants.SITEDS_USER, 
        		GlobalSitedsConstants.SITEDS_ID_REMITENTE, 
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
 				 coReContratante
 			);
			try {
		        System.out.println("JSON OBSERVACIONES: " + inputJson);
				Map<String, String> headers = new HashMap<String, String>();
		        headers.put("Content-Type", "application/json; charset=utf-8");
		        headers.put("Authorization", GlobalSitedsConstants.SITEDS_TOKEN);
		        //responseData  = HttpRequestUtil.sendRequest("POST", GlobalSitedsConstants.SITEDS_OBSERVACIONES, inputJson, headers);
		        
		        responseData = sendPostRequest(GlobalSitedsConstants.SITEDS_OBSERVACIONES, inputJson, Map.of(
	                    "Content-Type", "application/json; charset=utf-8",
	                    "Authorization", GlobalSitedsConstants.SITEDS_TOKEN
	                ));
		        
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
  	  
  	  String obsAsegurado = jsonObject.isNull("teMsgLibre1") ? null : jsonObject.getString("teMsgLibre1").trim().isEmpty() ? jsonObject.getString("teMsgLibre1").trim() + " " + jsonObject.getString("rptObs").trim() : 							!jsonObject.isNull("rptObs") ? jsonObject.getString("rptObs").trim() : "";
  	  String obsAdicional = jsonObject.isNull("teMsgLibre2") ? null : jsonObject.getString("teMsgLibre2").trim().isEmpty() ? "" : jsonObject.getString("teMsgLibre2") ;	  
  	  
  	inputJson = String.format("{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"tiDocumento\":\"%s\",\"nuDocumento\":\"%s\",\"coAfPaciente\":\"%s\",\"coProducto\":\"%s\",\"deProducto\":\"%s\",\"nuPlan\":\"%s\",\"tiCaContratante\":\"%s\",\"noPaContratante\":\"%s\",\"noContratante\":\"%s\",\"noMaContratante\":\"%s\",\"tiDoContratante\":\"%s\",\"idReContratante\":\"%s\",\"coReContratante\":\"%s\",\"nuCobertura\":\"%s\",\"deCobertura\":\"%s\",\"caServicio\":\"%s\",\"coCalservicio\":\"%s\",\"beMaxInicial\":\"%s\",\"coTiCobertura\":\"%s\",\"coSuTiCobertura\":\"%s\",\"coAplicativoTx\":\"%s\",\"coEspecialidad\":\"%s\",\"coParentesco\":\"%s\"}", 
  				GlobalSitedsConstants.SITEDS_NONCE,
    			GlobalSitedsConstants.SITEDS_PASSWORD,
    			GlobalSitedsConstants.SITEDS_USER, 
    			GlobalSitedsConstants.SITEDS_ID_REMITENTE, 
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
				 coParentesco
			);
			try {
		        System.out.println("JSON PROCEDIMIENTOS: " + inputJson);
				Map<String, String> headers = new HashMap<String, String>();
		        headers.put("Content-Type", "application/json; charset=utf-8");
		        headers.put("Authorization", GlobalSitedsConstants.SITEDS_TOKEN);
		        //responseData  = HttpRequestUtil.sendRequest("POST", GlobalSitedsConstants.SITEDS_PROCEDIMIENTOS, inputJson, headers);
		        
		        responseData = sendPostRequest(GlobalSitedsConstants.SITEDS_PROCEDIMIENTOS, inputJson, Map.of(
	                    "Content-Type", "application/json; charset=utf-8",
	                    "Authorization", GlobalSitedsConstants.SITEDS_TOKEN
	                ));
		        
		        response = responseData.getResponseBody();
		        System.out.println("RESPONSE PROCEDIMIENTOS: " + response);
				
			} catch (Exception e) {
				throw UtilResponse.rsException(Response.Status.BAD_REQUEST, "Error al consultar servicio qlProc.");
			}  	
			
			if(responseData.getStatusCode() == 500) {
				throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar servicio qlProc.");
			}
  	  
  	  Map<String, Object> mapDatosProcedimientos = new HashMap<String, Object>();
  	  List<Map<String, Object>> lstCpDiferen = new LinkedList<Map<String,Object>>();
  	  List<Map<String, Object>> lstExCarencia = new LinkedList<Map<String,Object>>();
  	  List<Map<String, Object>> lstTiEspera = new LinkedList<Map<String,Object>>();
        String fechaVigFin = "";
        jsonReader = Json.createReader(new StringReader(response));
        JsonArray aseguradoProcedimientos = jsonReader.readArray();
        
        for (int i = 0; i < aseguradoProcedimientos.size(); i++) {
      	  JsonObject procesos = aseguradoProcedimientos.getJsonObject(i);
      	  if(!procesos.getString("coProcedimiento").trim().isEmpty()) {
    			Map<String, Object> regCpDif = new HashMap<String, Object>();
    			regCpDif.put("identificador", procesos.getString("coProcedimiento"));
    			regCpDif.put("procedimiento", "");
    			regCpDif.put("genero", procesos.getString("coSexo").trim().isEmpty() ? "F/M" : procesos.getString("coSexo"));
    			regCpDif.put("cpFijo", procesos.getString("imDeducible") + " " + ("") + " POR ATENCIÓN");
    			regCpDif.put("deducible", procesos.getString("imDeducible"));
    			regCpDif.put("cpVariable", procesos.getString("poCuExDecimal") + " %");
    			regCpDif.put("coAseguro", procesos.getString("poCuExDecimal"));
    			regCpDif.put("frecuencia", procesos.getString("nuFrecuencia").trim().isEmpty() ? "0" : procesos.getString("nuFrecuencia"));
    			regCpDif.put("tiempo", procesos.getString("tiNuDias").trim().isEmpty() ? "0" : procesos.getString("tiNuDias"));
    			regCpDif.put("observacion", procesos.getString("teMsgObservacion"));
    			lstCpDiferen.add(regCpDif);
    			
    		}
    		fechaVigFin = procesos.getString("feFinVigencia");
    		
    		if(!procesos.getString("coExCarencia").trim().isEmpty()) {
    			Map<String, Object> regExCar = new HashMap<String, Object>();
    			regExCar.put("tipo", "CA");
    			regExCar.put("identificador", procesos.getString("coExCarencia"));
    			regExCar.put("descripcion", procesos.getString("deExCarencia"));
    			regExCar.put("observacion", procesos.getString("teMsgExCarencia"));
    			if(fechaVigFin != null && fechaVigFin.trim().length() == 8) {
    				regExCar.put("fechaVigFin", UtilAppDate.convertStringToDate(fechaVigFin, "yyyyMMdd"));
    			}
    			lstExCarencia.add(regExCar);
    		}
    		if(!procesos.getString("coTiEspera").trim().isEmpty()) {
    			Map<String, Object> regTiEsp = new HashMap<String, Object>();
    			regTiEsp.put("tipo", "TE");
    			regTiEsp.put("identificador", procesos.getString("coTiEspera"));
    			regTiEsp.put("descripcion", procesos.getString("deTiEspera"));
    			regTiEsp.put("observacion", procesos.getString("teMsgTiEspera"));
    			if(fechaVigFin != null && fechaVigFin.trim().length() == 8) {
    				regTiEsp.put("fechaVigFin", UtilAppDate.convertStringToDate(fechaVigFin, "yyyyMMdd"));
    			}
    			lstTiEspera.add(regTiEsp);
    		}
  	}
        
  	  mapDatosProcedimientos.put("detalleCoPagoDife", lstCpDiferen);
        if (lstExCarencia != null && !lstExCarencia.isEmpty()) {
            Map<String, Object> procCond = new HashMap<String, Object>();
            procCond.put("tipo","CA");
            procCond.put("descripcion","EXCEPCIÓN A LA CARENCIA");
            procCond.put("detalleCondicion", lstExCarencia);
            mapDatosProcedimientos.put("detalleProcEspRes", procCond);
        }
        if (lstTiEspera != null && !lstTiEspera.isEmpty()) {
      	  Map<String, Object> procCond = new HashMap<String, Object>();
            procCond.put("tipo","TE");
            procCond.put("descripcion","TIEMPO DE ESPERA");
            procCond.put("detalleCondicion",lstTiEspera);
            mapDatosProcedimientos.put("detalleProcEspRes", procCond);
        }
  	  
        
        Map<String, Object> mapAsegurado = new HashMap<String, Object>();
        mapAsegurado.put("tipoDeAtencion", Integer.valueOf(4));
        mapAsegurado.put("tipoPaciente", Integer.valueOf(2));
        mapAsegurado.put("coberturaDescripcion", String.valueOf("CONSULTA AMBULATORIA"));
        mapAsegurado.put("edadPaciente", edad);      
        mapAsegurado.put("descProducto", deProducto);
        mapAsegurado.put("feFinVigencia", fechaFinVigencia);
        mapAsegurado.put("feIniVigencia", fechaIniVigencia);
        mapAsegurado.put("obsAsegurado",obsAsegurado);
        mapAsegurado.put("obsAseguradoAdicional",obsAdicional);
        mapAsegurado.put("feAfiliacion", fechaAfiliacion);
        mapAsegurado.put("procedimientosCobertura", mapDatosProcedimientos);
        //mapAsegurado.put("fullCoberturaPaciente", coberturaFull);
        
  	  Map<String, Object> mapResponse = new HashMap<String, Object>();

  	  mapResponse.put("descripcion", "Se obtuvieron los resultados");
  	  mapResponse.put("informacionObtenida", nuevoJsonObject);
  	  mapResponse.put("informacionFormateada", mapAsegurado);
  	  
  	  return mapResponse;
    }
    
    @POST
    @Path("autorizacionSiteds")
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Map<String, Object> autorizacionSiteds(Map<String, Object> mapIn){
      Map<String, Object> mapResponse = new HashMap<String, Object>();
      Map<String, Object> mapInfoSeguroPersona = new HashMap<String, Object>();
      List<Map<String, Object>> detalleCobertura = new ArrayList<Map<String,Object>>();
      Map<String, Object> mapInfo = new HashMap<String, Object>();
      mapInfoSeguroPersona = (Map<String, Object>) mapIn.get("informacionPersonaSeguro");
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
      String edadPaciente = null;
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
      String jsonAsegurado = null;
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
	  	jsonAsegurado = (String) mapInfoSeguroPersona.get("fullCoberturaPaciente");
	  	cConsulta = 100.00;
	  	dscCobertura = (String) mapInfoSeguroPersona.get("coberturaDescripcion").toString();
	  	edadPaciente = mapInfoSeguroPersona.get("edadPaciente").toString();
	  	fechaIniVigencia = (String) mapInfoSeguroPersona.get("feIniVigencia").toString();
	  	fechaFinVigencia = (String) mapInfoSeguroPersona.get("feFinVigencia").toString();
	  	dscProducto = (String) mapInfoSeguroPersona.get("descProducto").toString();
	  	fechaAfiliacion = (String) mapInfoSeguroPersona.get("feAfiliacion").toString();
	  	observacion = "";
	  	observacionAsegurado = mapInfoSeguroPersona.get("obsAsegurado").toString().trim().isEmpty() ? "0" : mapInfoSeguroPersona.get("obsAsegurado").toString() ;
	  	observacionAsegAdicional = (String) mapInfoSeguroPersona.get("obsAseguradoAdicional").toString();
	  	direcMedico = "Dpto:              Provincia:             Distrito:                 Direccion:";
	  	
	  	
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
	  	String coAdmisionista = GlobalSitedsConstants.SITEDS_ADMISION;
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
	  	String jsonTemplate = "{\"sitedsNonce\":\"%s\",\"sitedsPassword\":\"%s\",\"sitedsUser\":\"%s\",\"idRemitente\":\"%s\",\"idReceptor\":\"%s\",\"apPaternoPaciente\":\"%s\",\"apMaternoPaciente\":\"%s\",\"noPaciente\":\"%s\",\"tiDoPaciente\":\"%s\",\"nuDoPaciente\":\"%s\",\"caPaciente\":\"%s\",\"coAfPaciente\":\"%s\",\"coEsPaciente\":\"%s\",\"coAdmisionista\":\"%s\",\"nuIdenEmpleador\":\"%s\",\"nuContratoPaciente\":\"%s\",\"nuPoliza\":\"%s\",\"nuCertificado\":\"%s\",\"coTiPolizaAfiliacion\":\"%s\",\"coProducto\":\"%s\",\"deProducto\":\"%s\",\"nuPlan\":\"%s\",\"tiPlanSalud\":\"%s\",\"coMoneda\":\"%s\",\"coParentesco\":\"%s\",\"soBeneficio\":\"%s\",\"nuSoBeneficio\":\"%s\",\"coEspecialidad\":\"%s\",\"feNacimiento\":\"%s\",\"genero\":\"%s\",\"esMarital\":\"%s\",\"feIniVigencia\":\"%s\",\"feFinVigencia\":\"%s\",\"esCobertura\":\"%s\",\"nuDecAccidente\":\"%s\",\"idInfAccidente\":\"%s\",\"deTiAccidente\":\"%s\",\"feAfiliacion\":\"%s\",\"feOcuAccidente\":\"%s\",\"nuAtencion\":\"%s\",\"idDerFarmacia\":\"%s\",\"tiProducto\":\"%s\",\"deProductoDeFarmacia\":\"%s\",\"feAtencion\":\"%s\",\"caContratante\":\"%s\",\"noPaContratante\":\"%s\",\"noMaContratante\":\"%s\",\"noContratante\":\"%s\",\"tiDoContratante\":\"%s\",\"idReContratante\":\"%s\",\"coReContratante\":\"%s\",\"caTitular\":\"%s\",\"noPaTitular\":\"%s\",\"noTitular\":\"%s\",\"coAfTitular\":\"%s\",\"noMaTitular\":\"%s\",\"tiDoTitular\":\"%s\",\"idReTitular\":\"%s\",\"nuDoTitular\":\"%s\",\"feIncTitular\":\"%s\",\"nuCobertura\":\"%s\",\"obsCobertura\":\"%s\",\"msgObs\":\"%s\",\"msgConEspeciales\":\"%s\",\"nuCobPreExistencia\":\"%s\",\"beMaxInicial\":\"%s\",\"canServicio\":\"%s\",\"idDeProducto\":\"%s\",\"coTiCobertura\":\"%s\",\"coSubTiCobertura\":\"%s\",\"msgObsPre\":\"%s\",\"msgConEspecialesPre\":\"%s\",\"coTiMoneda\":\"%s\",\"coPagoFijo\":\"%s\",\"coCalServicio\":\"%s\",\"canCalServicio\":\"%s\",\"coPagoVariable\":\"%s\",\"flagCG\":\"%s\",\"deflagCG\":\"%s\",\"feFinCarencia\":\"%s\",\"feFinEspera\":\"%s\",\"caRegafi\":\"%s\",\"noPaRegafi\":\"%s\",\"noRegafi\":\"%s\",\"coAfRegafi\":\"%s\",\"noMaRegafi\":\"%s\",\"tiDoRegafi\":\"%s\",\"nuDoRegafi\":\"%s\",\"feNaRegafi\":\"%s\",\"geRegafi\":\"%s\",\"coPaisRegafi\":\"%s\",\"idReRegafi\":\"%s\",\"detalleProEsp\":%s,\"detalleTieEsp\":%s,\"detalleExeCar\":%s,\"detalleRes\":%s}";
	    String inputJson = String.format(jsonTemplate, GlobalSitedsConstants.SITEDS_NONCE, GlobalSitedsConstants.SITEDS_PASSWORD, GlobalSitedsConstants.SITEDS_USER, GlobalSitedsConstants.SITEDS_ID_REMITENTE, idReceptor, apPaternoPaciente, apMaternoPaciente, noPaciente, tiDoPaciente, nuDoPaciente, caPaciente, coAfPaciente, coEsPaciente, coAdmisionista, nuIdEmpleador, nuContratoPaciente, nuPoliza, nuCertificado, coTiPoliza, coProducto, deProducto, nuPlan, tiPlanSalud, coMoneda, coParentesco, soBeneficio, nuSoBeneficio, coEspecialidad, feNacimiento, genero, esMarital, feIniVigencia, feFinVigencia,esCobertura, nuDecAccidente, idInfAccidente, deTiAccidente, feAfiliacion, feOcuAccidente, nuAtencion, idDerFarmacia, tiProducto, deProductoDeFarmacia, feAtencion, caContrantante, noPaContratante, noMaContratante, noContratante, tiDoContratante, idReContratante, coReContratante, caTitular, noPaTitular, noTitular, coAfTitular, noMaTitular, tiDoTitular, idReTitular, nuDoTitular, feInsTitular, nuCobertura, obsCobertura, msgObs, msgConEspeciales, nuCobPreExistencia, beMaxInicial, canServicio, idProducto, coTiCobertura, coSubTiCobertura, msgObsPre, msgConEspecialesPre, coTiMoneda, coPagoFijo, coCalServicio, canCalServicio, coPagoVariable, flagCG, detFlagCG, feFinCarencia, feFinEspera, caRegafi, noPaRegafi, noRegafi, coAfRegafi, noMaRegafi, tiDoRegafi, nuDoRegafi, feNaRegafi, geRegafi, coPaisRegafi, idReRegafi, detalleProEsp, detalleTieEsp, detalleExeCar, detalleRestric);
	      
	  		try {
	  	        System.out.println("JSON AUTORIZACION: " + inputJson);
	  			Map<String, String> headers = new HashMap<String, String>();
	  	        headers.put("Content-Type", "application/json; charset=utf-8");
	  	        headers.put("Authorization", GlobalSitedsConstants.SITEDS_TOKEN);
	  	        //responseData  = HttpRequestUtil.sendRequest("POST", GlobalSitedsConstants.SITEDS_AUTORIZACION, inputJson, headers);
	  	      responseData = sendPostRequest(GlobalSitedsConstants.SITEDS_AUTORIZACION, inputJson, Map.of(
	                    "Content-Type", "application/json; charset=utf-8",
	                    "Authorization", GlobalSitedsConstants.SITEDS_TOKEN
	                ));
	  	        response = responseData.getResponseBody();
	  	        System.out.println("RESPONSE AUTORIZACION: " + response);
	  			
	  		} catch (Exception e) {
	  			throw UtilResponse.rsException(Response.Status.UNAUTHORIZED, "Error al consultar el servicio SoliAutorizacion.");
	  		}
	  	
	  	JsonReader jsonReader = Json.createReader(new StringReader(response));
	  	JsonObject jsonObject = jsonReader.readObject();
	  	tipoAutorizacion = "01";
	  	nroAutorizacion = jsonObject.getString("nuAutorizacion");
	  	mapResponse.put("tiAutorizacion", tipoAutorizacion);
	  	mapResponse.put("nroAutorizacion", nroAutorizacion);	  	
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
		        headers.put("Authorization", GlobalSitedsConstants.SITEDS_TOKEN);
		        System.out.println("HEADERS: " + headers);
		        /*responseData  = HttpRequestUtil.sendRequest("POST", GlobalSitedsConstants.SITEDS_ASEGURADO_CODIGO, inputJson, headers);
		        responseData = sendPostRequest(GlobalSitedsConstants.SITEDS_ASEGURADO_CODIGO, inputJson, Map.of(
	                    "Content-Type", "application/json; charset=utf-8",
	                    "Authorization", GlobalSitedsConstants.SITEDS_TOKEN // Reemplaza con tu token real
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
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Configurar la conexión
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);

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
                responseBody = new String(is.readAllBytes(), "UTF-8");
            }
        } else { // Respuesta con error
            try (InputStream es = connection.getErrorStream()) {
                responseBody = new String(es.readAllBytes(), "UTF-8");
            }
        }

        // Devolver el resultado
        return new HttpResponse(Integer.valueOf(responseCode), responseBody, "");
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
