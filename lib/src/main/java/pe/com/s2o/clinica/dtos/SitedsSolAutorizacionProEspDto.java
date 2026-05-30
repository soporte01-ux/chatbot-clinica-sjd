package pe.com.s2o.clinica.dtos;

import java.io.Serializable;

public class SitedsSolAutorizacionProEspDto implements Serializable {
	private String coInProcedimiento;
    private String coTiProConAmbulatoria;
    private String nuPlanConAmbulatoria;
    private String imDeducible;
    private String poConAmbulatoria;
    private String frConAmbulatoria;
    private String geConAmbulatoria;
    private String caConAmbulatoria;
    private String msgConAmbulatoria;

    public SitedsSolAutorizacionProEspDto() {
        coInProcedimiento = "";
        coTiProConAmbulatoria = "";
        nuPlanConAmbulatoria = "";
        imDeducible = "";
        poConAmbulatoria = "";
        frConAmbulatoria = "";
        geConAmbulatoria = "";
        caConAmbulatoria = "";
        msgConAmbulatoria = "";
    }

    public String getCaConAmbulatoria() {
        return caConAmbulatoria;
    }

    public void setCaConAmbulatoria(String caConAmbulatoria) {
        this.caConAmbulatoria = caConAmbulatoria;
    }

    public String getCoInProcedimiento() {
        return coInProcedimiento;
    }

    public void setCoInProcedimiento(String coInProcedimiento) {
        this.coInProcedimiento = coInProcedimiento;
    }

    public String getCoTiProConAmbulatoria() {
        return coTiProConAmbulatoria;
    }

    public void setCoTiProConAmbulatoria(String coTiProConAmbulatoria) {
        this.coTiProConAmbulatoria = coTiProConAmbulatoria;
    }

    public String getFrConAmbulatoria() {
        return frConAmbulatoria;
    }

    public void setFrConAmbulatoria(String frConAmbulatoria) {
        this.frConAmbulatoria = frConAmbulatoria;
    }

    public String getGeConAmbulatoria() {
        return geConAmbulatoria;
    }

    public void setGeConAmbulatoria(String geConAmbulatoria) {
        this.geConAmbulatoria = geConAmbulatoria;
    }

    public String getImDeducible() {
        return imDeducible;
    }

    public void setImDeducible(String imDeducible) {
        this.imDeducible = imDeducible;
    }

    public String getMsgConAmbulatoria() {
        return msgConAmbulatoria;
    }

    public void setMsgConAmbulatoria(String msgConAmbulatoria) {
        this.msgConAmbulatoria = msgConAmbulatoria;
    }

    public String getNuPlanConAmbulatoria() {
        return nuPlanConAmbulatoria;
    }

    public void setNuPlanConAmbulatoria(String nuPlanConAmbulatoria) {
        this.nuPlanConAmbulatoria = nuPlanConAmbulatoria;
    }

    public String getPoConAmbulatoria() {
        return poConAmbulatoria;
    }

    public void setPoConAmbulatoria(String poConAmbulatoria) {
        this.poConAmbulatoria = poConAmbulatoria;
    }
}
