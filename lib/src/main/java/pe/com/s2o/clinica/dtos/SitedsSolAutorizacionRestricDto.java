package pe.com.s2o.clinica.dtos;

import java.io.Serializable;

public class SitedsSolAutorizacionRestricDto implements Serializable {
	private String cie10Restricciones;
    private String idRestricciones;
    private String obsRestricciones;
    private String deRestricciones;
    private String msgRestricciones;
    private String monTopeRestricciones;
    private String feFinEsperaRestricciones;

    public SitedsSolAutorizacionRestricDto() {
        
        cie10Restricciones = "";
        idRestricciones = "";
        obsRestricciones = "";
        deRestricciones = "";
        msgRestricciones = "";
        monTopeRestricciones = "";
        feFinEsperaRestricciones = "";
    }

    public String getCie10Restricciones() {
        return cie10Restricciones;
    }

    public void setCie10Restricciones(String cie10Restricciones) {
        this.cie10Restricciones = cie10Restricciones;
    }

    public String getDeRestricciones() {
        return deRestricciones;
    }

    public void setDeRestricciones(String deRestricciones) {
        this.deRestricciones = deRestricciones;
    }

    public String getFeFinEsperaRestricciones() {
        return feFinEsperaRestricciones;
    }

    public void setFeFinEsperaRestricciones(String feFinEsperaRestricciones) {
        this.feFinEsperaRestricciones = feFinEsperaRestricciones;
    }

    public String getIdRestricciones() {
        return idRestricciones;
    }

    public void setIdRestricciones(String idRestricciones) {
        this.idRestricciones = idRestricciones;
    }

    public String getMonTopeRestricciones() {
        return monTopeRestricciones;
    }

    public void setMonTopeRestricciones(String monTopeRestricciones) {
        this.monTopeRestricciones = monTopeRestricciones;
    }

    public String getMsgRestricciones() {
        return msgRestricciones;
    }

    public void setMsgRestricciones(String msgRestricciones) {
        this.msgRestricciones = msgRestricciones;
    }

    public String getObsRestricciones() {
        return obsRestricciones;
    }

    public void setObsRestricciones(String obsRestricciones) {
        this.obsRestricciones = obsRestricciones;
    }
}
