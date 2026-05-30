package pe.com.s2o.clinica.whatsapp;

import pe.com.s2o.clinica.dtos.ConfigDto;
import pe.com.s2o.clinica.dtos.EnvironmentConfig;
import pe.com.s2o.clinica.utils.ConfigLoader;

public class GlobalConstants {
	
	//DESARROLLO
	/*
	public static final String API_BASE_CLINICA = "http://localhost:8080";
	public static final String PHONE_NUMBER_ID = "463180813536065";
	public static final String API_BASE_NIUBIZ = "http://localhost:4200";
	public static final String API_BASE_CLINICA_BOT = "http://localhost:8085/lib";
	public static final String API_ACCESS_TOKEN  = "EAAHAX9FaMMoBO7aUq7V6HsZC3gkHMl5BxC9NiTxon57LyO3IzrSG6xp9jUUQ4PWgGlIjb5UllybHXmiZCq8TKdiexgLssxP81HQo0kxNea9f6Sd3SJUZAIT4ctFgqFPZAtGgcvndM4FBu3rdkPkZBj6VQwgEqTei8gTkqCvCmrZC7NtxIhCURf0j5qgDUPmrtTdQZDZD";
	*/
	
	//PRODUCCION
	
	public static final ConfigDto CONFIG_GENERAL = ConfigLoader.loadConfig();
    private static final EnvironmentConfig ENV = CONFIG_GENERAL.getConfig()
            .stream()
            .filter(env -> env.getType().equals(CONFIG_GENERAL.getModule()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No se encontró el ambiente configurado"));
    
	/*public static final String API_BASE_CLINICA = "https://melany.s2o.pe/clinica-app";
	public static final String PHONE_NUMBER_ID = "594026173787037";
	public static final String API_BASE_NIUBIZ = "https://melany.sofia.net.pe";
	public static final String API_BASE_CLINICA_BOT = "https://sofia.s2o.pe/clinica-aqp";
	public static final String API_ACCESS_TOKEN = "EAAIgsizmjpwBOyyPZCeWdDiga4mZCNc9kciTyRy3pn6IVgJ5y2Ro7ebswoZAOAGBq9rZBXhrxbZAZArEYxZCiBcgsVPlIGPIWYOByTHGCZCj1vKAezWpZBZCLtya7gRbaSfIAxKYBljwkq7rOivMuYM589SsnrrMnCpoqhptYnYCg4iYSZAbiTGG8bEGiwCUcjZASX6rzQZDZD";
	public static final String SITEDS_BASE = "http://200.39.148.101:8190";
	public static final String SITEDS_USER = "testv2";
	public static final String SITEDS_PASSWORD = "UBq03oOvlvDOPVKEMACWRXAmvwFTjn8cj6tnI4zR8vLNIf6q04uEao78Uj+tt/PGIyisCQl6iqz0FzClGWlK/M3Qzw8T76Obs5TPC3yhHIEGxm4Ozq1ytgWrt+WopMZaPmItAfuM5Ff7/uCuRUTNzcwRgoqvU57ALBEpqMZwlAc=|704uuu49a7l2ul62au46798ss7694s4a3asu083a3lul54sl41u2s277sl53s357|Y1+LIyGpLP9QTZLQau0J0w==";
	public static final String SITEDS_NONCE = "UzjoeaTqa2iZnJtBksmQ1g==";
	public static final String SITEDS_NO_IPRESS = "CLINICA SAN JUAN DE DIOS";
	public static final String SITEDS_TI_DO_IPRESS = "6";
	public static final String SITEDS_NU_RUC_IPRESS = "20162580672";
	public static final String SITEDS_CA_IPRESS = "2";
	public static final String SITEDS_ID_REMITENTE = "00015730";
	public static final String SITEDS_TOKEN = "1tFTQwISn4c=";
	public static final String SITEDS_ADMISION = "29288258";*/
	
	public static final String API_BASE_CLINICA = ENV.getBackend();
	public static final String PHONE_NUMBER_ID = ENV.getWhatsapp_bot().getPhone_id();
	public static final String API_BASE_NIUBIZ = ENV.getFrontend();
	public static final String API_BASE_CLINICA_BOT = ENV.getWhatsapp_bot().getBot();
	public static final String API_ACCESS_TOKEN = ENV.getWhatsapp_bot().getAccess_token();
	public static final String SITEDS_BASE = ENV.getSiteds().getApi();
	public static final String SITEDS_USER = ENV.getSiteds().getUser();
	public static final String SITEDS_PASSWORD = ENV.getSiteds().getPassword();
	public static final String SITEDS_NONCE = ENV.getSiteds().getNonce();
	public static final String SITEDS_NO_IPRESS = ENV.getSiteds().getNo_ipress();
	public static final String SITEDS_TI_DO_IPRESS = ENV.getSiteds().getType_doc_ipress();
	public static final String SITEDS_NU_RUC_IPRESS = ENV.getSiteds().getNu_ruc_ipress();
	public static final String SITEDS_CA_IPRESS = ENV.getSiteds().getCa_ipress();
	public static final String SITEDS_ID_REMITENTE = ENV.getSiteds().getId_remitente();
	public static final String SITEDS_TOKEN = CONFIG_GENERAL.getSiteds_token();
	public static final String SITEDS_ADMISION = ENV.getSiteds().getAdmition();
	
	
	//PRODUCCION-CLINICA-DOCKER-PROPIO
	
	/*
	public static final String API_BASE_CLINICA = "https://clinica.s2o.pe/clinica-app";
	public static final String PHONE_NUMBER_ID = "463180813536065";
	public static final String API_BASE_NIUBIZ = "https://clinica.s2o.pe";
	public static final String API_BASE_CLINICA_BOT = "https://sofia.s2o.pe/clinica-aqp-test";
	public static final String API_ACCESS_TOKEN  = "EAAHAX9FaMMoBO7aUq7V6HsZC3gkHMl5BxC9NiTxon57LyO3IzrSG6xp9jUUQ4PWgGlIjb5UllybHXmiZCq8TKdiexgLssxP81HQo0kxNea9f6Sd3SJUZAIT4ctFgqFPZAtGgcvndM4FBu3rdkPkZBj6VQwgEqTei8gTkqCvCmrZC7NtxIhCURf0j5qgDUPmrtTdQZDZD";
	*/
	
	public static final String API_VERIFY_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MzY4MzU2MTgsImV4cCI6MTc2ODM3MTYxOCwiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoianJvY2tldEBleGFtcGxlLmNvbSIsIkdpdmVuTmFtZSI6IkpvaG5ueSIsIlN1cm5hbWUiOiJSb2NrZXQiLCJFbWFpbCI6Impyb2NrZXRAZXhhbXBsZS5jb20iLCJSb2xlIjpbIk1hbmFnZXIiLCJQcm9qZWN0IEFkbWluaXN0cmF0b3IiXX0.NGXtephaYXdLxxPHHKK1GTvX9xF0d1E3YWyYCiLN4D0";
	public static final String API_SITEDS_CONSULTA_DOCUMENTO = "http://200.39.148.126:8180/sofia-ws-common/rs/afl/qoAfiliado";
	public static final String API_LOGIN_CLINICA = "http://200.39.148.126:8180/clinica-tokens-desarrollo/rs/token/v1/autenticacionWsp";
	public static final String API_REGISTRO_PERSONA = "http://200.39.148.126:8180/clinica-tokens-desarrollo/rs/token/v1/crearUsuarioWsp";
	public static final String API_ACTUALIZAR_EMAIL = "http://200.39.148.126:8180/clinica-tokens-desarrollo/rs/token/v1/actualizarCorreo";
	public static final String API_ACTUALIZAR_DATOS = "http://200.39.148.126:8180/clinica-tokens-desarrollo/rs/token/v1/actualizarCampos";
	public static final String API_ESPECIALIDADES = API_BASE_CLINICA + "/rs/citas/v1/listar-especialidad";
	public static final String API_ASEGURADORAS = API_BASE_CLINICA + "/rs/citas/v1/listar-seguros";
	public static final String API_OBTENER_PREVENTA = API_BASE_CLINICA + "/rs/citas/v1/generar-codigo-preventa";
	public static final String API_HORARIOS_ESPECIALIDAD = API_BASE_CLINICA + "/rs/citas/v1/listar-fechas-por-especialidad-ws";
	public static final String API_HORARIOS_DET_ESPECIALIDAD = API_BASE_CLINICA + "/rs/citas/v1/listar-horario-medico-especialidad";
	public static final String API_CONSULTA_PRECIO = API_BASE_CLINICA + "/rs/citas/v1/listar-precios-por-negespecialidad";
	public static final String API_PAGO_NIUBIZ = API_BASE_NIUBIZ + "/niubiz";
	public static final String API_URL_RECORTER = API_BASE_CLINICA_BOT + "/rs/recorter/shorten"; 
	public static final String API_URL_RECORTER_RESPONSE = API_BASE_CLINICA_BOT; 
	public static final String API_REGISTRAR_CITA_SEGURO = API_BASE_CLINICA + "/rs/citas/v1/guardar-cita"; 
	public static final String API_PRE_RESERVA_CITA = API_BASE_CLINICA + "/erp/cita/reservar/preReserva"; 
	public static final String API_ANULAR_PRE_RESERVA = API_BASE_CLINICA + "/rs/citas/v1/anular-cita-programada"; 
	public static final String API_OBTENER_IAFAS = API_BASE_CLINICA + "/rs/citas/v1/listar-seguros"; 
	
	//public static final String API_WEBHOOCK_SOPORTE = "https://ayako-overfree-foolishly.ngrok-free.dev/webhook"; 
	public static final String API_WEBHOOCK_SOPORTE = "https://vps4-back.sjd.pe/webhook"; 
	
	public static final String SITEDS_ENTIDAD_VINCULADA = SITEDS_BASE + "/sofia-ws-common/rs/srs/qoEntidadVinculada";
	public static final String SITEDS_ASEGURADO_NOMBRE = SITEDS_BASE + "/sofia-ws-common/rs/srs/qlAseguradoPorNombre";
	public static final String SITEDS_ASEGURADO_CODIGO = SITEDS_BASE + "/sofia-ws-common/rs/srs/qoAseguradoPorCodigo";
	public static final String SITEDS_ASEGURADO_CONDICIONES_MEDICAS = SITEDS_BASE + "/sofia-ws-common/rs/srs/qlCondicionMedica";
	public static final String SITEDS_OBSERVACIONES = SITEDS_BASE + "/sofia-ws-common/rs/srs/qoObservaciones";
	public static final String SITEDS_PROCEDIMIENTOS = SITEDS_BASE + "/sofia-ws-common/rs/srs/qlProcedimientos";
	public static final String SITEDS_AUTORIZACION = SITEDS_BASE + "/sofia-ws-common/rs/srs/qoSolicitudAutorizacion";
	public static final String SITEDS_ACREDITACION = SITEDS_BASE + "/sofia-ws-common/rs/srs/qoAcreditacion";
	public static final String SITEDS_ENVIARRECIBIR = SITEDS_BASE + "/sofia-ws-common/rs/cola/qoEnviarRecibir";
	public static final String API_DEFAULT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJub21icmUiOiJDaXRhcyBDbGllbnRlIEFwcCIsImVtYWlsIjoiZGV2MmRldi5zb2Z0d2FyZUBnbWFpbC5jb20iLCJwZXJzb25hX2NvZCI6ODc4NjgxLCJhcHBDbGlJZCI6MSwic3ViIjoiYXBwY2xpY2l0YXMxIiwianRpIjoiNDAwZjJhNmItYWQ2OS00ZDEwLWIxZTYtNWUyMGUxN2YyNGIyIiwiaWF0IjoxNjc2NjQyMzQ1LCJleHAiOjE2NzY2NzgzNDV9.5LtDRydmHzs64YxbN3OlewT1fLQf3x3hag4zdF7JoaU";
	public static final String API_DEFAULT_TOKEN_RESERVAS = "eyJhbGciOiJIUzI1NiJ9.eyJub21icmUiOiJKdWFuIE11cmlsbG8gTWVuZGV6IiwiZW1haWwiOiJkZXYyZGV2LnNvZnR3YXJlQGdtYWlsLmNvbSIsImFwcENsaUlkIjoxLCJzdWIiOiJ1c3VhYmMzIiwianRpIjoiNzlhNWI3YmUtMGM3Yi00Nzk3LWJhZGYtNzg3Njk5YzVkYTAwIiwiaWF0IjoxNjc2MzMyMDM1LCJleHAiOjE2NzYzNjgwMzV9.uUJ9Apn0WTDzlRRzlctCfqG22rX-msF4pNPiTpoo8BU";
}
