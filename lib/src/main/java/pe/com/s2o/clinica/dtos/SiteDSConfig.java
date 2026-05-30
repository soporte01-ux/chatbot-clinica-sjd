package pe.com.s2o.clinica.dtos;

public class SiteDSConfig {
    private String api;
    private String user;
    private String password;
    private String nonce;
    private String no_ipress;
    private String type_doc_ipress;
    private String nu_ruc_ipress;
    private String ca_ipress;
    private String id_remitente;
    private String admition;

    // Getters y setters
    public String getApi() { return api; }
    public void setApi(String api) { this.api = api; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }

    public String getNo_ipress() { return no_ipress; }
    public void setNo_ipress(String no_ipress) { this.no_ipress = no_ipress; }

    public String getType_doc_ipress() { return type_doc_ipress; }
    public void setType_doc_ipress(String type_doc_ipress) { this.type_doc_ipress = type_doc_ipress; }

    public String getNu_ruc_ipress() { return nu_ruc_ipress; }
    public void setNu_ruc_ipress(String nu_ruc_ipress) { this.nu_ruc_ipress = nu_ruc_ipress; }

    public String getCa_ipress() { return ca_ipress; }
    public void setCa_ipress(String ca_ipress) { this.ca_ipress = ca_ipress; }

    public String getId_remitente() { return id_remitente; }
    public void setId_remitente(String id_remitente) { this.id_remitente = id_remitente; }

    public String getAdmition() { return admition; }
    public void setAdmition(String admition) { this.admition = admition; }
}
