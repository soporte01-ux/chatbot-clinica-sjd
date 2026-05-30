package pe.com.s2o.clinica.dtos;

public class WhatsAppBotConfig {
    private String bot;
    private String phone_id;
    private String access_token;

    // Getters y setters
    public String getBot() { return bot; }
    public void setBot(String bot) { this.bot = bot; }

    public String getPhone_id() { return phone_id; }
    public void setPhone_id(String phone_id) { this.phone_id = phone_id; }

    public String getAccess_token() { return access_token; }
    public void setAccess_token(String access_token) { this.access_token = access_token; }
}
