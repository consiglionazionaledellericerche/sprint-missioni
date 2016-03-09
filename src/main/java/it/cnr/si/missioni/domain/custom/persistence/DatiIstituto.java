package it.cnr.si.missioni.domain.custom.persistence;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.springframework.util.StringUtils;

/**
 * A user.
 */
@Entity
@Table(name = "DATI_ISTITUTO")
public class DatiIstituto extends OggettoBulkXmlTransient implements Serializable {

	@Id
	@Column(name="ID", unique=true, nullable=false, length = 20)
    @GeneratedValue(strategy = GenerationType.AUTO)
//	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="SEQUENZA")
	private Long id;

//    @JsonIgnore
    @Size(min = 0, max = 20)
    @Column(name = "ISTITUTO", length = 20, nullable = false)
    private String istituto;

    @Column(name = "ANNO", length = 4, nullable = false)
    private Integer anno;

    @Column(name = "PROGRESSIVO_ORDINE", length = 20, nullable = true)
    private Long progressivoOrdine;

    @Size(min = 0, max = 250)
    @Column(name = "DESCR_ISTITUTO", length = 250, nullable = false)
    private String descrIstituto;

    @Size(min = 0, max = 1)
    @Column(name = "GESTIONE_RESP_MODULO", length = 1, nullable = false)
    private String gestioneRespModulo;

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

	public String getIstituto() {
		return istituto;
	}

	public void setIstituto(String istituto) {
		this.istituto = istituto;
	}

	public Integer getAnno() {
		return anno;
	}

	public void setAnno(Integer anno) {
		this.anno = anno;
	}

	public Long getProgressivoOrdine() {
		return progressivoOrdine;
	}

	public void setProgressivoOrdine(Long progressivoOrdine) {
		this.progressivoOrdine = progressivoOrdine;
	}

	public String getDescrIstituto() {
		return descrIstituto;
	}

	public void setDescrIstituto(String descrIstituto) {
		this.descrIstituto = descrIstituto;
	}

	public String getGestioneRespModulo() {
		return gestioneRespModulo;
	}

	public void setGestioneRespModulo(String gestioneRespModulo) {
		this.gestioneRespModulo = gestioneRespModulo;
	}

	@Transient
    public Boolean isAttivaGestioneResponsabileModulo() {
		if (!StringUtils.isEmpty(getGestioneRespModulo())){
        	if (getGestioneRespModulo().equals("S")){
        		return true;
        	} 
    	}
    	return false;
    }
}