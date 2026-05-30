package pe.com.s2o.clinica.dtos;

import java.io.Serializable;

public class SitedsSolAutorizacionTieEspDto implements Serializable {
	private String coTiEspera;
    private String idTiEspera;
    private String deTiEspera;
    private String feFinVigenciaTiEspera;
    private String msgTiEspera;

    public SitedsSolAutorizacionTieEspDto() {
        coTiEspera = "";
        idTiEspera = "";
        deTiEspera = "";
        feFinVigenciaTiEspera = "";
        msgTiEspera = "";
    }

    public String getCoTiEspera() {
        return coTiEspera;
    }

    public void setCoTiEspera(String coTiEspera) {
        this.coTiEspera = coTiEspera;
    }

    public String getDeTiEspera() {
        return deTiEspera;
    }

    public void setDeTiEspera(String deTiEspera) {
        this.deTiEspera = deTiEspera;
    }

    public String getFeFinVigenciaTiEspera() {
        return feFinVigenciaTiEspera;
    }

    public void setFeFinVigenciaTiEspera(String feFinVigenciaTiEspera) {
        this.feFinVigenciaTiEspera = feFinVigenciaTiEspera;
    }

    public String getIdTiEspera() {
        return idTiEspera;
    }

    public void setIdTiEspera(String idTiEspera) {
        this.idTiEspera = idTiEspera;
    }

    public String getMsgTiEspera() {
        return msgTiEspera;
    }

    public void setMsgTiEspera(String msgTiEspera) {
        this.msgTiEspera = msgTiEspera;
    }
}
