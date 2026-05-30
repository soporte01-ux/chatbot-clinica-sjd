package pe.com.s2o.clinica.dtos;

import java.util.List;

public class ConfigDto {
	private String module;
    private String whatsapp_verify_token;
    private String siteds_token;
    private List<EnvironmentConfig> config;

    // Getters y setters
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getWhatsapp_verify_token() { return whatsapp_verify_token; }
    public void setWhatsapp_verify_token(String whatsapp_verify_token) { this.whatsapp_verify_token = whatsapp_verify_token; }

    public String getSiteds_token() { return siteds_token; }
    public void setSiteds_token(String siteds_token) { this.siteds_token = siteds_token; }

    public List<EnvironmentConfig> getConfig() { return config; }
    public void setConfig(List<EnvironmentConfig> config) { this.config = config; }
}
