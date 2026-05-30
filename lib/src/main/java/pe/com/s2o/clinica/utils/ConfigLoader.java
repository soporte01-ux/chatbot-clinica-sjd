package pe.com.s2o.clinica.utils;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import pe.com.s2o.clinica.dtos.ConfigDto;

public class ConfigLoader {
    private static final String CONFIG_PATH = "config.json";

    public static ConfigDto loadConfig() {
    	Gson gson = new Gson();
        try (InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_PATH)) {
            if (inputStream == null) {
                throw new RuntimeException("No se encontró el archivo config.json en resources");
            }
            return gson.fromJson(new InputStreamReader(inputStream), ConfigDto.class);
        } catch (JsonSyntaxException | JsonIOException e) {
            throw new RuntimeException("Error al parsear el JSON: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar la configuración: " + e.getMessage());
        }
    }
}
