package it.cnr.si.missioni.domain.custom.persistence;


import it.cnr.si.missioni.util.Costanti;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.springframework.util.StringUtils;

/**
 * A user.
 */
@Entity
@Table(name = "RIMBORSO_MISSIONE_DETTAGLI")
public class RimborsoMissioneDettagli extends OggettoBulkXmlTransient implements Serializable {

	@Id
	@Column(name="ID", unique=true, nullable=false, length = 20)
    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

    @Size(min = 0, max = 256)
    @Column(name = "UID", length = 256, nullable = false)
    private String uid;

    @Column(name = "IMPORTO", length = 28, nullable = true)
    private BigDecimal importo;

    @Size(min = 0, max = 1000)
    @Column(name = "NOTE", length = 3, nullable = true)
    private String note;

    @Column(name = "RIGA", length = 50, nullable = false)
    private Long riga;

    @Type(type = "java.util.Date")
    @Column(name = "DATA_SPESA", nullable = false)
    private Date dataSpesa;

    @ManyToOne
	@JoinColumn(name="ID_RIMBORSO_MISSIONE", nullable=false)
	private RimborsoMissione rimborsoMissione;

    @Size(min = 0, max = 3)
    @Column(name = "STATO", length = 3, nullable = false)
    private String stato;

    @Size(min = 0, max = 1)
    @Column(name = "TI_SPESA_DIARIA", length = 1, nullable = false)
    private String tiSpesaDiaria;

    @Size(min = 0, max = 20)
    @Column(name = "CD_TI_SPESA", length = 20, nullable = true)
    private String cdTiSpesa;

    @Size(min = 0, max = 250)
    @Column(name = "DS_TI_SPESA", length = 250, nullable = true)
    private String dsTiSpesa;

    @Size(min = 0, max = 250)
    @Column(name = "DS_SPESA", length = 250, nullable = true)
    private String dsSpesa;

    @Size(min = 0, max = 20)
    @Column(name = "CD_TI_PASTO", length = 20, nullable = true)
    private String cdTiPasto;

    @Column(name = "KM_PERCORSI", length = 10, nullable = true)
    private Long kmPercorsi;

    @Size(min = 0, max = 10)
    @Column(name = "CD_DIVISA", length = 10, nullable = true)
    private String cdDivisa;

    @Column(name = "IMPORTO_DIVISA", length = 28, nullable = true)
    private BigDecimal importoDivisa;

    @Column(name = "IMPORTO_EURO", length = 28, nullable = true)
    private BigDecimal importoEuro;

    @Column(name = "CAMBIO", length = 19, nullable = true)
    private BigDecimal cambio;

	public void setId(Long id) {
		this.id = id;
	}

	//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((id == null) ? 0 : id.hashCode());
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (!super.equals(obj))
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		AutoPropria other = (AutoPropria) obj;
//		if (id == null) {
//			if (other.id != null)
//				return false;
//		} else if (!id.equals(other.id))
//			return false;
//		return true;
//	}
//
	@Override
	public Serializable getId() {
		return id;
	}

    public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getStato() {
		return stato;
	}

	public void setStato(String stato) {
		this.stato = stato;
	}

	public BigDecimal getImporto() {
		return importo;
	}

	public void setImporto(BigDecimal importo) {
		this.importo = importo;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	@Transient
	public String getDecodeStato() {
		if (!StringUtils.isEmpty(getStato())){
			return Costanti.STATO.get(getStato());
		}
		return "";
	}
	
	@Transient
    public Boolean isAnticipoConfermato() {
		if (!StringUtils.isEmpty(getStato())){
        	if (getStato().equals(Costanti.STATO_CONFERMATO)){
        		return true;
        	} 
    	}
    	return false;
    }

	@Transient
    public Boolean isAnticipoInserito() {
		if (!StringUtils.isEmpty(getStato())){
        	if (getStato().equals(Costanti.STATO_INSERITO)){
        		return true;
        	} 
    	}
    	return false;
    }

	@Transient
    public String getFileName() {
		return "AnticipoOrdineMissione"+getId()+".pdf";
	}

	public Long getRiga() {
		return riga;
	}

	public void setRiga(Long riga) {
		this.riga = riga;
	}

	public Date getDataSpesa() {
		return dataSpesa;
	}

	public void setDataSpesa(Date dataSpesa) {
		this.dataSpesa = dataSpesa;
	}

	public RimborsoMissione getRimborsoMissione() {
		return rimborsoMissione;
	}

	public void setRimborsoMissione(RimborsoMissione rimborsoMissione) {
		this.rimborsoMissione = rimborsoMissione;
	}

	public String getTiSpesaDiaria() {
		return tiSpesaDiaria;
	}

	public void setTiSpesaDiaria(String tiSpesaDiaria) {
		this.tiSpesaDiaria = tiSpesaDiaria;
	}

	public String getCdTiSpesa() {
		return cdTiSpesa;
	}

	public void setCdTiSpesa(String cdTiSpesa) {
		this.cdTiSpesa = cdTiSpesa;
	}

	public String getDsTiSpesa() {
		return dsTiSpesa;
	}

	public void setDsTiSpesa(String dsTiSpesa) {
		this.dsTiSpesa = dsTiSpesa;
	}

	public String getDsSpesa() {
		return dsSpesa;
	}

	public void setDsSpesa(String dsSpesa) {
		this.dsSpesa = dsSpesa;
	}

	public String getCdTiPasto() {
		return cdTiPasto;
	}

	public void setCdTiPasto(String cdTiPasto) {
		this.cdTiPasto = cdTiPasto;
	}

	public Long getKmPercorsi() {
		return kmPercorsi;
	}

	public void setKmPercorsi(Long kmPercorsi) {
		this.kmPercorsi = kmPercorsi;
	}

	public String getCdDivisa() {
		return cdDivisa;
	}

	public void setCdDivisa(String cdDivisa) {
		this.cdDivisa = cdDivisa;
	}

	public BigDecimal getImportoDivisa() {
		return importoDivisa;
	}

	public void setImportoDivisa(BigDecimal importoDivisa) {
		this.importoDivisa = importoDivisa;
	}

	public BigDecimal getImportoEuro() {
		return importoEuro;
	}

	public void setImportoEuro(BigDecimal importoEuro) {
		this.importoEuro = importoEuro;
	}

	public BigDecimal getCambio() {
		return cambio;
	}

	public void setCambio(BigDecimal cambio) {
		this.cambio = cambio;
	}
	

}
