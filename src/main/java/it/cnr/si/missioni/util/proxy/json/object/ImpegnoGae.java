/*
 *  Copyright (C) 2023  Consiglio Nazionale delle Ricerche
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Affero General Public License as
 *      published by the Free Software Foundation, either version 3 of the
 *      License, or (at your option) any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Affero General Public License for more details.
 *
 *      You should have received a copy of the GNU Affero General Public License
 *      along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package it.cnr.si.missioni.util.proxy.json.object;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.cnr.si.missioni.util.Utility;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ImpegnoGae extends RestServiceBean implements Serializable {

    @JsonProperty("cdCds")
    private String cdCds;
    @JsonProperty("esercizio")
    private Integer esercizio;
    @JsonProperty("cdUnitaOrganizzativa")
    private String cdUnitaOrganizzativa;
    @JsonProperty("esercizioOriginale")
    private Integer esercizioOriginale;
    @JsonProperty("cdCdsOrigine")
    private String cdCdsOrigine;
    @JsonProperty("pgObbligazione")
    private Long pgObbligazione;
    @JsonProperty("dsObbligazione")
    private String dsObbligazione;
    @JsonProperty("cdUoOrigine")
    private String cdUoOrigine;
    @JsonProperty("cdElementoVoce")
    private String cdElementoVoce;
    @JsonProperty("tiAppartenenza")
    private String tiAppartenenza;
    @JsonProperty("tiGestione")
    private String tiGestione;
    @JsonProperty("flPgiro")
    private Boolean flPgiro;
    @JsonProperty("imScadenzaComp")
    private BigDecimal imScadenzaComp;
    @JsonProperty("imScadenzaRes")
    private BigDecimal imScadenzaRes;
    @JsonProperty("imAssociatoDocAmmComp")
    private BigDecimal imAssociatoDocAmmComp;
    @JsonProperty("imAssociatoDocAmmRes")
    private BigDecimal imAssociatoDocAmmRes;
    @JsonProperty("imPagatoComp")
    private BigDecimal imPagatoComp;
    @JsonProperty("imPagatoRes")
    private BigDecimal imPagatoRes;
    @JsonProperty("cdLineaAttivita")
    private String cdLineaAttivita;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The cdCds
     */
    @JsonProperty("cdCds")
    public String getCdCds() {
        return cdCds;
    }

    /**
     * @param cdCds The cdCds
     */
    @JsonProperty("cdCds")
    public void setCdCds(String cdCds) {
        this.cdCds = cdCds;
    }

    /**
     * @return The esercizio
     */
    @JsonProperty("esercizio")
    public Integer getEsercizio() {
        return esercizio;
    }

    /**
     * @param esercizio The esercizio
     */
    @JsonProperty("esercizio")
    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }

    /**
     * @return The cdUnitaOrganizzativa
     */
    @JsonProperty("cdUnitaOrganizzativa")
    public String getCdUnitaOrganizzativa() {
        return cdUnitaOrganizzativa;
    }

    /**
     * @param cdUnitaOrganizzativa The cdUnitaOrganizzativa
     */
    @JsonProperty("cdUnitaOrganizzativa")
    public void setCdUnitaOrganizzativa(String cdUnitaOrganizzativa) {
        this.cdUnitaOrganizzativa = cdUnitaOrganizzativa;
    }

    /**
     * @return The cdUnitaOrganizzativa
     */
    @JsonProperty("cdLineaAttivita")
    public String getCdLineaAttivita() {
        return cdLineaAttivita;
    }

    /**
     * @param cdUnitaOrganizzativa The cdUnitaOrganizzativa
     */
    @JsonProperty("cdLineaAttivita")
    public void setCdLineaAttivita(String cdLineaAttivita) {
        this.cdLineaAttivita = cdLineaAttivita;
    }

    /**
     * @return The esercizioOriginale
     */
    @JsonProperty("esercizioOriginale")
    public Integer getEsercizioOriginale() {
        return esercizioOriginale;
    }

    /**
     * @param esercizioOriginale The esercizioOriginale
     */
    @JsonProperty("esercizioOriginale")
    public void setEsercizioOriginale(Integer esercizioOriginale) {
        this.esercizioOriginale = esercizioOriginale;
    }

    /**
     * @return The cdCdsOrigine
     */
    @JsonProperty("cdCdsOrigine")
    public String getCdCdsOrigine() {
        return cdCdsOrigine;
    }

    /**
     * @param cdCdsOrigine The cdCdsOrigine
     */
    @JsonProperty("cdCdsOrigine")
    public void setCdCdsOrigine(String cdCdsOrigine) {
        this.cdCdsOrigine = cdCdsOrigine;
    }

    /**
     * @return The pgObbligazione
     */
    @JsonProperty("pgObbligazione")
    public Long getPgObbligazione() {
        return pgObbligazione;
    }

    /**
     * @param pgObbligazione The pgObbligazione
     */
    @JsonProperty("pgObbligazione")
    public void setPgObbligazione(Long pgObbligazione) {
        this.pgObbligazione = pgObbligazione;
    }

    /**
     * @return The dsObbligazione
     */
    @JsonProperty("dsObbligazione")
    public String getDsObbligazione() {
        return dsObbligazione;
    }

    /**
     * @param dsObbligazione The dsObbligazione
     */
    @JsonProperty("dsObbligazione")
    public void setDsObbligazione(String dsObbligazione) {
        this.dsObbligazione = dsObbligazione;
    }

    /**
     * @return The cdUoOrigine
     */
    @JsonProperty("cdUoOrigine")
    public String getCdUoOrigine() {
        return cdUoOrigine;
    }

    /**
     * @param cdUoOrigine The cdUoOrigine
     */
    @JsonProperty("cdUoOrigine")
    public void setCdUoOrigine(String cdUoOrigine) {
        this.cdUoOrigine = cdUoOrigine;
    }

    /**
     * @return The cdElementoVoce
     */
    @JsonProperty("cdElementoVoce")
    public String getCdElementoVoce() {
        return cdElementoVoce;
    }

    /**
     * @param cdElementoVoce The cdElementoVoce
     */
    @JsonProperty("cdElementoVoce")
    public void setCdElementoVoce(String cdElementoVoce) {
        this.cdElementoVoce = cdElementoVoce;
    }

    /**
     * @return The tiAppartenenza
     */
    @JsonProperty("tiAppartenenza")
    public String getTiAppartenenza() {
        return tiAppartenenza;
    }

    /**
     * @param tiAppartenenza The tiAppartenenza
     */
    @JsonProperty("tiAppartenenza")
    public void setTiAppartenenza(String tiAppartenenza) {
        this.tiAppartenenza = tiAppartenenza;
    }

    /**
     * @return The tiGestione
     */
    @JsonProperty("tiGestione")
    public String getTiGestione() {
        return tiGestione;
    }

    /**
     * @param tiGestione The tiGestione
     */
    @JsonProperty("tiGestione")
    public void setTiGestione(String tiGestione) {
        this.tiGestione = tiGestione;
    }

    /**
     * @return The flPgiro
     */
    @JsonProperty("flPgiro")
    public Boolean getFlPgiro() {
        return flPgiro;
    }

    /**
     * @param flPgiro The flPgiro
     */
    @JsonProperty("flPgiro")
    public void setFlPgiro(Boolean flPgiro) {
        this.flPgiro = flPgiro;
    }

    /**
     * @return The imScadenzaComp
     */
    @JsonProperty("imScadenzaComp")
    public BigDecimal getImScadenzaComp() {
        return imScadenzaComp;
    }

    /**
     * @param imScadenzaComp The imScadenzaComp
     */
    @JsonProperty("imScadenzaComp")
    public void setImScadenzaComp(BigDecimal imScadenzaComp) {
        this.imScadenzaComp = imScadenzaComp;
    }

    /**
     * @return The imScadenzaRes
     */
    @JsonProperty("imScadenzaRes")
    public BigDecimal getImScadenzaRes() {
        return imScadenzaRes;
    }

    /**
     * @param imScadenzaRes The imScadenzaRes
     */
    @JsonProperty("imScadenzaRes")
    public void setImScadenzaRes(BigDecimal imScadenzaRes) {
        this.imScadenzaRes = imScadenzaRes;
    }

    /**
     * @return The imAssociatoDocAmmComp
     */
    @JsonProperty("imAssociatoDocAmmComp")
    public BigDecimal getImAssociatoDocAmmComp() {
        return imAssociatoDocAmmComp;
    }

    /**
     * @param imAssociatoDocAmmComp The imAssociatoDocAmmComp
     */
    @JsonProperty("imAssociatoDocAmmComp")
    public void setImAssociatoDocAmmComp(BigDecimal imAssociatoDocAmmComp) {
        this.imAssociatoDocAmmComp = imAssociatoDocAmmComp;
    }

    /**
     * @return The imAssociatoDocAmmRes
     */
    @JsonProperty("imAssociatoDocAmmRes")
    public BigDecimal getImAssociatoDocAmmRes() {
        return imAssociatoDocAmmRes;
    }

    /**
     * @param imAssociatoDocAmmRes The imAssociatoDocAmmRes
     */
    @JsonProperty("imAssociatoDocAmmRes")
    public void setImAssociatoDocAmmRes(BigDecimal imAssociatoDocAmmRes) {
        this.imAssociatoDocAmmRes = imAssociatoDocAmmRes;
    }

    /**
     * @return The imPagatoComp
     */
    @JsonProperty("imPagatoComp")
    public BigDecimal getImPagatoComp() {
        return imPagatoComp;
    }

    /**
     * @param imPagatoComp The imPagatoComp
     */
    @JsonProperty("imPagatoComp")
    public void setImPagatoComp(BigDecimal imPagatoComp) {
        this.imPagatoComp = imPagatoComp;
    }

    /**
     * @return The imPagatoRes
     */
    @JsonProperty("imPagatoRes")
    public BigDecimal getImPagatoRes() {
        return imPagatoRes;
    }

    /**
     * @param imPagatoRes The imPagatoRes
     */
    @JsonProperty("imPagatoRes")
    public void setImPagatoRes(BigDecimal imPagatoRes) {
        this.imPagatoRes = imPagatoRes;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public BigDecimal getDisponibilitaImpegno() {
        if (getEsercizio().compareTo(getEsercizioOriginale()) == 0) {
            return Utility.nvl(getImScadenzaComp().subtract(getImAssociatoDocAmmComp()));
        } else {
            return Utility.nvl(getImScadenzaRes().subtract(getImAssociatoDocAmmRes()));
        }
    }
}