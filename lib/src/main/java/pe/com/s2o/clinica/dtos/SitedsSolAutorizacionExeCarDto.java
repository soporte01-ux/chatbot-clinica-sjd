package pe.com.s2o.clinica.dtos;

import java.io.Serializable;

public class SitedsSolAutorizacionExeCarDto implements Serializable {
	private String coExCarencia;
    private String idExCarencia;
    private String deExCarencia;
    private String msgExCarencia;

    public SitedsSolAutorizacionExeCarDto() {
        coExCarencia = "";
        idExCarencia = "";
        deExCarencia = "";
        msgExCarencia = "";
    }

    public String getCoExCarencia() {
        return coExCarencia;
    }

    public void setCoExCarencia(String coExCarencia) {
        this.coExCarencia = coExCarencia;
    }

    public String getDeExCarencia() {
        return deExCarencia;
    }

    public void setDeExCarencia(String deExCarencia) {
        this.deExCarencia = deExCarencia;
    }

    public String getIdExCarencia() {
        return idExCarencia;
    }

    public void setIdExCarencia(String idExCarencia) {
        this.idExCarencia = idExCarencia;
    }

    public String getMsgExCarencia() {
        return msgExCarencia;
    }

    public void setMsgExCarencia(String msgExCarencia) {
        this.msgExCarencia = msgExCarencia;
    }
}
