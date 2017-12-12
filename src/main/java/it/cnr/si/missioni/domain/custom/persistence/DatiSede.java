package it.cnr.si.missioni.domain.custom.persistence;


import java.io.Serializable;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 * A user.
 */
@Entity
@Table(name = "DATI_SEDE")
public class DatiSede extends OggettoBulkXmlTransient implements Serializable {

	@Id
	@Column(name="ID", unique=true, nullable=false, length = 20)
    @GeneratedValue(strategy = GenerationType.AUTO)
//	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SEQUENZA")
	private Long id;

//    @JsonIgnore
    @Size(min = 0, max = 30)
    @Column(name = "CODICE_SEDE", length = 30, nullable = false)
    private String codiceSede;

    @Column(name = "DATA_INIZIO", nullable = false)
    public LocalDate dataInizio;

    @Column(name = "DATA_FINE", nullable = true)
    public LocalDate dataFine;

    @Size(min = 0, max = 200)
    @Column(name = "RESPONSABILE", length = 200, nullable = true)
    private String responsabile;

    @Size(min = 0, max = 1)
    @Column(name = "RESPONSABILE_SOLO_ITALIA", length = 1, nullable = true)
    private String responsabileSoloItalia;

    @Size(min = 0, max = 30)
    @Column(name = "SEDE_RESP_ESTERO", length = 30, nullable = true)
    private String sedeRespEstero;

	public void setId(Long id) {
		this.id = id;
	}
	@Override
	public Serializable getId() {
		return id;
	}
	public String getCodiceSede() {
		return codiceSede;
	}
	public void setCodiceSede(String codiceSede) {
		this.codiceSede = codiceSede;
	}
	public LocalDate getDataInizio() {
		return dataInizio;
	}
	public void setDataInizio(LocalDate dataInizio) {
		this.dataInizio = dataInizio;
	}
	public LocalDate getDataFine() {
		return dataFine;
	}
	public void setDataFine(LocalDate dataFine) {
		this.dataFine = dataFine;
	}
	public String getResponsabile() {
		return responsabile;
	}
	public void setResponsabile(String responsabile) {
		this.responsabile = responsabile;
	}
	public String getResponsabileSoloItalia() {
		return responsabileSoloItalia;
	}
	public void setResponsabileSoloItalia(String responsabileSoloItalia) {
		this.responsabileSoloItalia = responsabileSoloItalia;
	}
	public String getSedeRespEstero() {
		return sedeRespEstero;
	}
	public void setSedeRespEstero(String sedeRespEstero) {
		this.sedeRespEstero = sedeRespEstero;
	}
}