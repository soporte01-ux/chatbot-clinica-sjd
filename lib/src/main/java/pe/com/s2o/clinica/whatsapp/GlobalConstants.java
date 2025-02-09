package pe.com.s2o.clinica.whatsapp;

public class GlobalConstants {
	
	//DESARROLLO
	/*
	public static final String API_BASE_CLINICA = "http://localhost:8080";
	public static final String PHONE_NUMBER_ID = "594026173787037";
	public static final String API_BASE_NIUBIZ = "http://localhost:4200";
	public static final String API_BASE_CLINICA_BOT = "http://localhost:8085/lib";
	*/
	//PRODUCCION
	
	public static final String API_BASE_CLINICA = "https://melany.s2o.pe/clinica-app";
	public static final String PHONE_NUMBER_ID = "594026173787037";
	public static final String API_BASE_NIUBIZ = "https://melany.sofia.net.pe";
	public static final String API_BASE_CLINICA_BOT = "https://sofia.s2o.pe/clinica-aqp";
	
	public static final String API_ACCESS_TOKEN = "EAAIgsizmjpwBOyyPZCeWdDiga4mZCNc9kciTyRy3pn6IVgJ5y2Ro7ebswoZAOAGBq9rZBXhrxbZAZArEYxZCiBcgsVPlIGPIWYOByTHGCZCj1vKAezWpZBZCLtya7gRbaSfIAxKYBljwkq7rOivMuYM589SsnrrMnCpoqhptYnYCg4iYSZAbiTGG8bEGiwCUcjZASX6rzQZDZD";
	public static final String API_VERIFY_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MzY4MzU2MTgsImV4cCI6MTc2ODM3MTYxOCwiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoianJvY2tldEBleGFtcGxlLmNvbSIsIkdpdmVuTmFtZSI6IkpvaG5ueSIsIlN1cm5hbWUiOiJSb2NrZXQiLCJFbWFpbCI6Impyb2NrZXRAZXhhbXBsZS5jb20iLCJSb2xlIjpbIk1hbmFnZXIiLCJQcm9qZWN0IEFkbWluaXN0cmF0b3IiXX0.NGXtephaYXdLxxPHHKK1GTvX9xF0d1E3YWyYCiLN4D0";
	public static final String API_SITEDS_CONSULTA_DOCUMENTO = "http://200.39.148.101:8180/sofia-ws-common/rs/afl/qoAfiliado";
	public static final String API_LOGIN_CLINICA = API_BASE_CLINICA + "/clinica/api/auth/token";
	public static final String API_REGISTRO_PERSONA = API_BASE_CLINICA + "/erp/cita/persona/registro";
	public static final String API_ACTUALIZAR_EMAIL = API_BASE_CLINICA + "/erp/cita/persona/actualizarEmail";
	public static final String API_ESPECIALIDADES = API_BASE_CLINICA + "/erp/cita/especialidad/all/especialidad";
	public static final String API_ASEGURADORAS = API_BASE_CLINICA + "/erp/cita/especialidad/all/aseguradoras";
	public static final String API_HORARIOS_ESPECIALIDAD = API_BASE_CLINICA + "/erp/cita/especialidad/all/medicosxhorariosmov";
	public static final String API_HORARIOS_DET_ESPECIALIDAD = API_BASE_CLINICA + "/erp/cita/especialidad/all/medicosxhorasdisponible";
	public static final String API_CONSULTA_PRECIO = API_BASE_CLINICA + "/erp/cita/financiadores/obtenerprecioconsulta";
	public static final String API_PAGO_NIUBIZ = API_BASE_NIUBIZ + "/niubiz";
	public static final String API_URL_RECORTER = API_BASE_CLINICA_BOT + "/rs/recorter/shorten"; 
	public static final String API_URL_RECORTER_RESPONSE = API_BASE_CLINICA_BOT; 
	public static final String API_REGISTRAR_CITA_SEGURO = API_BASE_CLINICA + "/erp/cita/pagos/segurocompleto"; 
}
