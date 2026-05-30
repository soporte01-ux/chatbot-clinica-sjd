package pe.com.s2o.clinica.dtos;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;


public class SitedsSolAutorizacionDto implements Serializable {
	private String sitedsNonce;
    private String sitedsPassword;
    private String sitedsUser;
    private String idRemitente;
    private String nuRucRemitente;
    private String idReceptor;

    private String apPaternoPaciente;
    private String apMaternoPaciente;
    private String noPaciente;
    private String tiDoPaciente;
    private String nuDoPaciente;

    private String caPaciente;
    private String coAfPaciente;
    private String coEsPaciente;
    private String coAdmisionista;
    private String nuIdenEmpleador;
    private String nuContratoPaciente;
    private String nuPoliza;
    private String nuCertificado;
    private String coTiPolizaAfiliacion;
    private String coProducto;
    private String deProducto;
    private String nuPlan;
    private String tiPlanSalud;
    private String coMoneda;
    private String coParentesco;
    private String soBeneficio;
    private String nuSoBeneficio;
    private String coEspecialidad;
    private String feNacimiento;
    private String genero;
    private String esMarital;
    private String feIniVigencia;
    private String feFinVigencia;

    private String esCobertura;
    private String nuDecAccidente;
    private String idInfAccidente;
    private String deTiAccidente;
    private String feAfiliacion;
    private String feOcuAccidente;
    private String nuAtencion;
    private String nuAutorizacion;
    private String idDerFarmacia;
    private String tiProducto;
    private String deProductoDeFarmacia;
    private String feAtencion;

    private String caContratante;
    private String noPaContratante;
    private String noMaContratante;
    private String noContratante;
    private String tiDoContratante;
    private String idReContratante;
    private String coReContratante;
    private String tiCaContratante;
    
    private String caTitular;
    private String noPaTitular;
    private String noTitular;
    private String coAfTitular;
    private String noMaTitular;
    private String tiDoTitular;
    private String idReTitular;
    private String nuDoTitular;
    private String feIncTitular;

    private String obsCobertura;
    private String msgObs;
    private String msgConEspeciales;
    private String nuCobPreExistencia;
    private String nuCobertura;
    private String coInRestriccion;
    private String beMaxInicial;
    private String canServicio;
    private String idDeProducto;
    private String coTiCobertura;
    private String coSubTiCobertura;
    private String msgObsPre;
    private String msgConEspecialesPre;
    private String coTiMoneda;
    private String coPagoFijo;
    private String coCalServicio;
    private String canCalServicio;
    private String coPagoVariable;
    private String flagCG;
    private String deflagCG;
    private String feFinCarencia;
    private String feFinEspera;

    private String caRegafi;
    private String noPaRegafi;
    private String noRegafi;
    private String coAfRegafi;
    private String noMaRegafi;
    private String tiDoRegafi;
    private String nuDoRegafi;
    private String feNaRegafi;
    private String geRegafi;
    private String coPaisRegafi;

    private String idReRegafi;

    private List<SitedsSolAutorizacionProEspDto> detalleProEsp;
    private List<SitedsSolAutorizacionExeCarDto> detalleExeCar;
    private List<SitedsSolAutorizacionTieEspDto> detalleTieEsp;
    private List<SitedsSolAutorizacionRestricDto> detalleRestric;
    private List<SitedsSolAutorizacionDetalleRes> detalleRes;

    public SitedsSolAutorizacionDto() {

        detalleProEsp = new LinkedList<SitedsSolAutorizacionProEspDto>();
        detalleExeCar = new LinkedList<SitedsSolAutorizacionExeCarDto>();
        detalleTieEsp = new LinkedList<SitedsSolAutorizacionTieEspDto>();
        detalleRestric = new LinkedList<SitedsSolAutorizacionRestricDto>();
        detalleRes = new LinkedList<SitedsSolAutorizacionDetalleRes>();
    }
    
    public String getNuAutorizacion() {
		return nuAutorizacion;
	}

	public void setNuAutorizacion(String nuAutorizacion) {
		this.nuAutorizacion = nuAutorizacion;
	}

	public String getNuRucRemitente() {
        return nuRucRemitente;
    }

    public void setNuRucRemitente(String nuRucRemitente) {
        this.nuRucRemitente = nuRucRemitente;
    }

    public String getCaRegafi() {
        return caRegafi;
    }

    public void setCaRegafi(String caRegafi) {
        this.caRegafi = caRegafi;
    }

    public String getCoAfRegafi() {
        return coAfRegafi;
    }

    public void setCoAfRegafi(String coAfRegafi) {
        this.coAfRegafi = coAfRegafi;
    }

    public String getCoPaisRegafi() {
        return coPaisRegafi;
    }

    public void setCoPaisRegafi(String coPaisRegafi) {
        this.coPaisRegafi = coPaisRegafi;
    }

    public String getFeNaRegafi() {
        return feNaRegafi;
    }

    public void setFeNaRegafi(String feNaRegafi) {
        this.feNaRegafi = feNaRegafi;
    }

    public String getGeRegafi() {
        return geRegafi;
    }

    public void setGeRegafi(String geRegafi) {
        this.geRegafi = geRegafi;
    }

    public String getNoMaRegafi() {
        return noMaRegafi;
    }

    public void setNoMaRegafi(String noMaRegafi) {
        this.noMaRegafi = noMaRegafi;
    }

    public String getNoPaRegafi() {
        return noPaRegafi;
    }

    public void setNoPaRegafi(String noPaRegafi) {
        this.noPaRegafi = noPaRegafi;
    }

    public String getNoRegafi() {
        return noRegafi;
    }

    public void setNoRegafi(String noRegafi) {
        this.noRegafi = noRegafi;
    }

    public String getNuDoRegafi() {
        return nuDoRegafi;
    }

    public void setNuDoRegafi(String nuDoRegafi) {
        this.nuDoRegafi = nuDoRegafi;
    }

    public String getTiDoRegafi() {
        return tiDoRegafi;
    }

    public void setTiDoRegafi(String tiDoRegafi) {
        this.tiDoRegafi = tiDoRegafi;
    }

    public String getMsgConEspeciales() {
        return msgConEspeciales;
    }

    public void setMsgConEspeciales(String msgConEspeciales) {
        this.msgConEspeciales = msgConEspeciales;
    }

    public String getMsgObs() {
        return msgObs;
    }

    public void setMsgObs(String msgObs) {
        this.msgObs = msgObs;
    }

    public String getNuCobPreExistencia() {
        return nuCobPreExistencia;
    }

    public void setNuCobPreExistencia(String nuCobPreExistencia) {
        this.nuCobPreExistencia = nuCobPreExistencia;
    }

    public String getObsCobertura() {
        return obsCobertura;
    }

    public void setObsCobertura(String obsCobertura) {
        this.obsCobertura = obsCobertura;
    }

    public String getDeProductoDeFarmacia() {
        return deProductoDeFarmacia;
    }

    public void setDeProductoDeFarmacia(String deProductoDeFarmacia) {
        this.deProductoDeFarmacia = deProductoDeFarmacia;
    }

    public String getDeTiAccidente() {
        return deTiAccidente;
    }

    public void setDeTiAccidente(String deTiAccidente) {
        this.deTiAccidente = deTiAccidente;
    }

    public String getFeAfiliacion() {
        return feAfiliacion;
    }

    public void setFeAfiliacion(String feAfiliacion) {
        this.feAfiliacion = feAfiliacion;
    }

    public String getFeAtencion() {
        return feAtencion;
    }

    public void setFeAtencion(String feAtencion) {
        this.feAtencion = feAtencion;
    }

    public String getFeOcuAccidente() {
        return feOcuAccidente;
    }

    public void setFeOcuAccidente(String feOcuAccidente) {
        this.feOcuAccidente = feOcuAccidente;
    }

    public String getIdDerFarmacia() {
        return idDerFarmacia;
    }

    public void setIdDerFarmacia(String idDerFarmacia) {
        this.idDerFarmacia = idDerFarmacia;
    }

    public String getIdInfAccidente() {
        return idInfAccidente;
    }

    public void setIdInfAccidente(String idInfAccidente) {
        this.idInfAccidente = idInfAccidente;
    }

    public String getNuAtencion() {
        return nuAtencion;
    }

    public void setNuAtencion(String nuAtencion) {
        this.nuAtencion = nuAtencion;
    }

    public String getNuDecAccidente() {
        return nuDecAccidente;
    }

    public void setNuDecAccidente(String nuDecAccidente) {
        this.nuDecAccidente = nuDecAccidente;
    }

    public String getTiProducto() {
        return tiProducto;
    }

    public void setTiProducto(String tiProducto) {
        this.tiProducto = tiProducto;
    }

    public String getEsCobertura() {
        return esCobertura;
    }

    public void setEsCobertura(String esCobertura) {
        this.esCobertura = esCobertura;
    }

    public List<SitedsSolAutorizacionExeCarDto> getDetalleExeCar() {
        return detalleExeCar;
    }

    public void setDetalleExeCar(List<SitedsSolAutorizacionExeCarDto> detalleExeCar) {
        this.detalleExeCar = detalleExeCar;
    }

    public List<SitedsSolAutorizacionProEspDto> getDetalleProEsp() {
        return detalleProEsp;
    }

    public void setDetalleProEsp(List<SitedsSolAutorizacionProEspDto> detalleProEsp) {
        this.detalleProEsp = detalleProEsp;
    }

    public List<SitedsSolAutorizacionRestricDto> getDetalleRestric() {
        return detalleRestric;
    }

    public void setDetalleRestric(List<SitedsSolAutorizacionRestricDto> detalleRestric) {
        this.detalleRestric = detalleRestric;
    }

    public List<SitedsSolAutorizacionTieEspDto> getDetalleTieEsp() {
        return detalleTieEsp;
    }

    public void setDetalleTieEsp(List<SitedsSolAutorizacionTieEspDto> detalleTieEsp) {
        this.detalleTieEsp = detalleTieEsp;
    }
      
    
    public List<SitedsSolAutorizacionDetalleRes> getDetalleRes() {
        return detalleRes;
    }

    public void setDetalleRes(List<SitedsSolAutorizacionDetalleRes> detalleRes) {
        this.detalleRes = detalleRes;
    }
    
    

    public String getApMaternoPaciente() {
        return apMaternoPaciente;
    }

    public void setApMaternoPaciente(String apMaternoPaciente) {
        this.apMaternoPaciente = apMaternoPaciente;
    }

    public String getApPaternoPaciente() {
        return apPaternoPaciente;
    }

    public void setApPaternoPaciente(String apPaternoPaciente) {
        this.apPaternoPaciente = apPaternoPaciente;
    }

    public String getBeMaxInicial() {
        return beMaxInicial;
    }

    public void setBeMaxInicial(String beMaxInicial) {
        this.beMaxInicial = beMaxInicial;
    }

    public String getCaContratante() {
        return caContratante;
    }

    public void setCaContratante(String caContratante) {
        this.caContratante = caContratante;
    }

    public String getCaPaciente() {
        return caPaciente;
    }

    public void setCaPaciente(String caPaciente) {
        this.caPaciente = caPaciente;
    }

    public String getCaTitular() {
        return caTitular;
    }

    public void setCaTitular(String caTitular) {
        this.caTitular = caTitular;
    }

    public String getCanCalServicio() {
        return canCalServicio;
    }

    public void setCanCalServicio(String canCalServicio) {
        this.canCalServicio = canCalServicio;
    }

    public String getCanServicio() {
        return canServicio;
    }

    public void setCanServicio(String canServicio) {
        this.canServicio = canServicio;
    }

    public String getCoAdmisionista() {
        return coAdmisionista;
    }

    public void setCoAdmisionista(String coAdmisionista) {
        this.coAdmisionista = coAdmisionista;
    }

    public String getCoAfPaciente() {
        return coAfPaciente;
    }

    public void setCoAfPaciente(String coAfPaciente) {
        this.coAfPaciente = coAfPaciente;
    }

    public String getCoAfTitular() {
        return coAfTitular;
    }

    public void setCoAfTitular(String coAfTitular) {
        this.coAfTitular = coAfTitular;
    }

    public String getCoCalServicio() {
        return coCalServicio;
    }

    public void setCoCalServicio(String coCalServicio) {
        this.coCalServicio = coCalServicio;
    }

    public String getCoEsPaciente() {
        return coEsPaciente;
    }

    public void setCoEsPaciente(String coEsPaciente) {
        this.coEsPaciente = coEsPaciente;
    }

    public String getCoEspecialidad() {
        return coEspecialidad;
    }

    public void setCoEspecialidad(String coEspecialidad) {
        this.coEspecialidad = coEspecialidad;
    }

    public String getCoMoneda() {
        return coMoneda;
    }

    public void setCoMoneda(String coMoneda) {
        this.coMoneda = coMoneda;
    }

    public String getCoPagoFijo() {
        return coPagoFijo;
    }

    public void setCoPagoFijo(String coPagoFijo) {
        this.coPagoFijo = coPagoFijo;
    }

    public String getCoPagoVariable() {
        return coPagoVariable;
    }

    public void setCoPagoVariable(String coPagoVariable) {
        this.coPagoVariable = coPagoVariable;
    }

    public String getCoParentesco() {
        return coParentesco;
    }

    public void setCoParentesco(String coParentesco) {
        this.coParentesco = coParentesco;
    }

    public String getCoProducto() {
        return coProducto;
    }

    public void setCoProducto(String coProducto) {
        this.coProducto = coProducto;
    }

    public String getCoReContratante() {
        return coReContratante;
    }

    public void setCoReContratante(String coReContratante) {
        this.coReContratante = coReContratante;
    }

    public String getCoSubTiCobertura() {
        return coSubTiCobertura;
    }

    public void setCoSubTiCobertura(String coSubTiCobertura) {
        this.coSubTiCobertura = coSubTiCobertura;
    }

    public String getCoTiCobertura() {
        return coTiCobertura;
    }

    public void setCoTiCobertura(String coTiCobertura) {
        this.coTiCobertura = coTiCobertura;
    }

    public String getCoTiMoneda() {
        return coTiMoneda;
    }

    public void setCoTiMoneda(String coTiMoneda) {
        this.coTiMoneda = coTiMoneda;
    }

    public String getCoTiPolizaAfiliacion() {
        return coTiPolizaAfiliacion;
    }

    public void setCoTiPolizaAfiliacion(String coTiPolizaAfiliacion) {
        this.coTiPolizaAfiliacion = coTiPolizaAfiliacion;
    }

    public String getDeProducto() {
        return deProducto;
    }

    public void setDeProducto(String deProducto) {
        this.deProducto = deProducto;
    }

    public String getDeflagCG() {
        return deflagCG;
    }

    public void setDeflagCG(String deflagCG) {
        this.deflagCG = deflagCG;
    }

    public String getEsMarital() {
        return esMarital;
    }

    public void setEsMarital(String esMarital) {
        this.esMarital = esMarital;
    }

    public String getFeFinCarencia() {
        return feFinCarencia;
    }

    public void setFeFinCarencia(String feFinCarencia) {
        this.feFinCarencia = feFinCarencia;
    }

    public String getFeFinEspera() {
        return feFinEspera;
    }

    public void setFeFinEspera(String feFinEspera) {
        this.feFinEspera = feFinEspera;
    }

    public String getFeFinVigencia() {
        return feFinVigencia;
    }

    public void setFeFinVigencia(String feFinVigencia) {
        this.feFinVigencia = feFinVigencia;
    }

    public String getFeIncTitular() {
        return feIncTitular;
    }

    public void setFeIncTitular(String feIncTitular) {
        this.feIncTitular = feIncTitular;
    }

    public String getFeIniVigencia() {
        return feIniVigencia;
    }

    public void setFeIniVigencia(String feIniVigencia) {
        this.feIniVigencia = feIniVigencia;
    }

    public String getFeNacimiento() {
        return feNacimiento;
    }

    public void setFeNacimiento(String feNacimiento) {
        this.feNacimiento = feNacimiento;
    }

    public String getFlagCG() {
        return flagCG;
    }

    public void setFlagCG(String flagCG) {
        this.flagCG = flagCG;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getIdDeProducto() {
        return idDeProducto;
    }

    public void setIdDeProducto(String idDeProducto) {
        this.idDeProducto = idDeProducto;
    }

    public String getIdReContratante() {
        return idReContratante;
    }

    public void setIdReContratante(String idReContratante) {
        this.idReContratante = idReContratante;
    }

    public String getIdReRegafi() {
        return idReRegafi;
    }

    public void setIdReRegafi(String idReRegafi) {
        this.idReRegafi = idReRegafi;
    }

    public String getIdReTitular() {
        return idReTitular;
    }

    public void setIdReTitular(String idReTitular) {
        this.idReTitular = idReTitular;
    }

    public String getIdReceptor() {
        return idReceptor;
    }

    public void setIdReceptor(String idReceptor) {
        this.idReceptor = idReceptor;
    }

    public String getIdRemitente() {
        return idRemitente;
    }

    public void setIdRemitente(String idRemitente) {
        this.idRemitente = idRemitente;
    }

    public String getMsgConEspecialesPre() {
        return msgConEspecialesPre;
    }

    public void setMsgConEspecialesPre(String msgConEspecialesPre) {
        this.msgConEspecialesPre = msgConEspecialesPre;
    }

    public String getMsgObsPre() {
        return msgObsPre;
    }

    public void setMsgObsPre(String msgObsPre) {
        this.msgObsPre = msgObsPre;
    }

    public String getNoContratante() {
        return noContratante;
    }

    public void setNoContratante(String noContratante) {
        this.noContratante = noContratante;
    }

    public String getNoMaContratante() {
        return noMaContratante;
    }

    public void setNoMaContratante(String noMaContratante) {
        this.noMaContratante = noMaContratante;
    }

    public String getNoMaTitular() {
        return noMaTitular;
    }

    public void setNoMaTitular(String noMaTitular) {
        this.noMaTitular = noMaTitular;
    }

    public String getNoPaContratante() {
        return noPaContratante;
    }

    public void setNoPaContratante(String noPaContratante) {
        this.noPaContratante = noPaContratante;
    }

    public String getNoPaTitular() {
        return noPaTitular;
    }

    public void setNoPaTitular(String noPaTitular) {
        this.noPaTitular = noPaTitular;
    }

    public String getNoPaciente() {
        return noPaciente;
    }

    public void setNoPaciente(String noPaciente) {
        this.noPaciente = noPaciente;
    }

    public String getNoTitular() {
        return noTitular;
    }

    public void setNoTitular(String noTitular) {
        this.noTitular = noTitular;
    }

    public String getNuCertificado() {
        return nuCertificado;
    }

    public void setNuCertificado(String nuCertificado) {
        this.nuCertificado = nuCertificado;
    }

    public String getNuCobertura() {
        return nuCobertura;
    }

    public void setNuCobertura(String nuCobertura) {
        this.nuCobertura = nuCobertura;
    }

    public String getNuContratoPaciente() {
        return nuContratoPaciente;
    }

    public void setNuContratoPaciente(String nuContratoPaciente) {
        this.nuContratoPaciente = nuContratoPaciente;
    }

    public String getNuDoTitular() {
        return nuDoTitular;
    }

    public void setNuDoTitular(String nuDoTitular) {
        this.nuDoTitular = nuDoTitular;
    }

    public String getNuIdenEmpleador() {
        return nuIdenEmpleador;
    }

    public void setNuIdenEmpleador(String nuIdenEmpleador) {
        this.nuIdenEmpleador = nuIdenEmpleador;
    }

    public String getNuPlan() {
        return nuPlan;
    }

    public void setNuPlan(String nuPlan) {
        this.nuPlan = nuPlan;
    }

    public String getNuPoliza() {
        return nuPoliza;
    }

    public void setNuPoliza(String nuPoliza) {
        this.nuPoliza = nuPoliza;
    }

    public String getNuSoBeneficio() {
        return nuSoBeneficio;
    }

    public void setNuSoBeneficio(String nuSoBeneficio) {
        this.nuSoBeneficio = nuSoBeneficio;
    }

    public String getSitedsNonce() {
        return sitedsNonce;
    }

    public void setSitedsNonce(String sitedsNonce) {
        this.sitedsNonce = sitedsNonce;
    }

    public String getSitedsPassword() {
        return sitedsPassword;
    }

    public void setSitedsPassword(String sitedsPassword) {
        this.sitedsPassword = sitedsPassword;
    }

    public String getSitedsUser() {
        return sitedsUser;
    }

    public void setSitedsUser(String sitedsUser) {
        this.sitedsUser = sitedsUser;
    }

    public String getSoBeneficio() {
        return soBeneficio;
    }

    public void setSoBeneficio(String soBeneficio) {
        this.soBeneficio = soBeneficio;
    }

    public String getTiDoContratante() {
        return tiDoContratante;
    }

    public void setTiDoContratante(String tiDoContratante) {
        this.tiDoContratante = tiDoContratante;
    }

    public String getTiDoTitular() {
        return tiDoTitular;
    }

    public void setTiDoTitular(String tiDoTitular) {
        this.tiDoTitular = tiDoTitular;
    }

    public String getTiPlanSalud() {
        return tiPlanSalud;
    }

    public void setTiPlanSalud(String tiPlanSalud) {
        this.tiPlanSalud = tiPlanSalud;
    }

    public String getNuDoPaciente() {
        return nuDoPaciente;
    }

    public void setNuDoPaciente(String nuDoPaciente) {
        this.nuDoPaciente = nuDoPaciente;
    }

    public String getTiDoPaciente() {
        return tiDoPaciente;
    }

    public void setTiDoPaciente(String tiDoPaciente) {
        this.tiDoPaciente = tiDoPaciente;
    }

	public String getTiCaContratante() {
		return tiCaContratante;
	}

	public void setTiCaContratante(String tiCaContratante) {
		this.tiCaContratante = tiCaContratante;
	}

	public String getCoInRestriccion() {
		return coInRestriccion;
	}

	public void setCoInRestriccion(String coInRestriccion) {
		this.coInRestriccion = coInRestriccion;
	}
}
