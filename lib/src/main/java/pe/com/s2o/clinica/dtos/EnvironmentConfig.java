package pe.com.s2o.clinica.dtos;

public class EnvironmentConfig {
    private String type;
    private String frontend;
    private String backend;
    private WhatsAppBotConfig whatsapp_bot;
    private SiteDSConfig siteds;

    // Getters y setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFrontend() { return frontend; }
    public void setFrontend(String frontend) { this.frontend = frontend; }

    public String getBackend() { return backend; }
    public void setBackend(String backend) { this.backend = backend; }

    public WhatsAppBotConfig getWhatsapp_bot() { return whatsapp_bot; }
    public void setWhatsapp_bot(WhatsAppBotConfig whatsapp_bot) { this.whatsapp_bot = whatsapp_bot; }

    public SiteDSConfig getSiteds() { return siteds; }
    public void setSiteds(SiteDSConfig siteds) { this.siteds = siteds; }
}
