package it.cnr.si.missioni.web.filter;

public class RimborsoMissioneFilter extends MissioneFilter {
	private Integer annoOrdine;
	private Long daNumeroOrdine;
	private Long aNumeroOrdine;
	private String statoInvioSigla;
	public Integer getAnnoOrdine() {
		return annoOrdine;
	}
	public void setAnnoOrdine(Integer annoOrdine) {
		this.annoOrdine = annoOrdine;
	}
	public Long getDaNumeroOrdine() {
		return daNumeroOrdine;
	}
	public void setDaNumeroOrdine(Long daNumeroOrdine) {
		this.daNumeroOrdine = daNumeroOrdine;
	}
	public Long getaNumeroOrdine() {
		return aNumeroOrdine;
	}
	public void setaNumeroOrdine(Long aNumeroOrdine) {
		this.aNumeroOrdine = aNumeroOrdine;
	}
	public String getStatoInvioSigla() {
		return statoInvioSigla;
	}
	public void setStatoInvioSigla(String statoInvioSigla) {
		this.statoInvioSigla = statoInvioSigla;
	}
}