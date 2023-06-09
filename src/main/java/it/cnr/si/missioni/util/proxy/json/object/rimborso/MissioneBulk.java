
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

package it.cnr.si.missioni.util.proxy.json.object.rimborso;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "esercizio",
        "cd_cds",
        "cd_unita_organizzativa",
        "cd_terzo",
        "ds_missione",
        "im_spese",
        "im_diaria_netto",
        "im_totale_missione",
        "im_lordo_percepiente",
        "im_netto_pecepiente",
        "im_spese_anticipate",
        "im_diaria_lorda",
        "im_quota_esente",
        "im_rimborso",
        "ti_istituz_commerc",
        "fl_comune_proprio",
        "fl_comune_altro",
        "fl_comune_estero",
        "fl_associato_compenso",
        "stato_coge",
        "stato_cofi",
        "stato_coan",
        "stato_pagamento_fondo_eco",
        "stato_liquidazione",
        "ti_provvisorio_definitivo",
        "ti_associato_manrev",
        "dt_inizio_missione",
        "dt_fine_missione",
        "ti_anagrafico",
        "tipo_rapporto",
        "banca",
        "rif_inquadramento",
        "modalita_pagamento",
        "tappeMissioneColl",
        "missioneRigaColl",
        "speseMissioneColl",
        "annoMandatoAnticipo",
        "numeroMandatoAnticipo",
        "importoMandatoAnticipo",
        "idRimborsoMissione",
        "idFlusso",
        "idFlussoOrdineMissione",
        "idFolderRimborsoMissione",
        "idFolderOrdineMissione",
        "tam",
        "cdCdsObbligazione",
        "esercizioObbligazione",
        "esercizioOriObbligazione",
        "pgObbligazione",
        "gae",
        "cognome",
        "nome",
        "codice_fiscale",
        "importoDaRimborsare"})
public class MissioneBulk implements Cloneable, Serializable {

    @JsonProperty("esercizio")
    private Integer esercizio;
    @JsonProperty("cd_cds")
    private String cdCds;
    @JsonProperty("cd_unita_organizzativa")
    private String cdUnitaOrganizzativa;
    @JsonProperty("pg_missione")
    private Long pgMissione;
    @JsonProperty("cd_terzo")
    private Integer cdTerzo;
    @JsonProperty("ds_missione")
    private String dsMissione;
    @JsonProperty("im_spese")
    private BigDecimal imSpese;
    @JsonProperty("im_diaria_netto")
    private BigDecimal imDiariaNetto;
    @JsonProperty("importoDaRimborsare")
    private BigDecimal importoDaRimborsare;
    @JsonProperty("im_totale_missione")
    private BigDecimal imTotaleMissione;
    @JsonProperty("im_lordo_percepiente")
    private BigDecimal imLordoPercepiente;
    @JsonProperty("im_netto_pecepiente")
    private BigDecimal imNettoPecepiente;
    @JsonProperty("im_spese_anticipate")
    private BigDecimal imSpeseAnticipate;
    @JsonProperty("im_diaria_lorda")
    private BigDecimal imDiariaLorda;
    @JsonProperty("im_quota_esente")
    private BigDecimal imQuotaEsente;
    @JsonProperty("im_rimborso")
    private BigDecimal imRimborso;
    @JsonProperty("ti_istituz_commerc")
    private String tiIstituzCommerc;
    @JsonProperty("fl_comune_proprio")
    private Boolean flComuneProprio;
    @JsonProperty("fl_comune_altro")
    private Boolean flComuneAltro;
    @JsonProperty("fl_comune_estero")
    private Boolean flComuneEstero;
    @JsonProperty("fl_associato_compenso")
    private Boolean flAssociatoCompenso;
    @JsonProperty("stato_coge")
    private String statoCoge;
    @JsonProperty("stato_cofi")
    private String statoCofi;
    @JsonProperty("stato_coan")
    private String statoCoan;
    @JsonProperty("stato_pagamento_fondo_eco")
    private String statoPagamentoFondoEco;
    @JsonProperty("stato_liquidazione")
    private String statoLiquidazione;
    @JsonProperty("ti_provvisorio_definitivo")
    private String tiProvvisorioDefinitivo;
    @JsonProperty("ti_associato_manrev")
    private String tiAssociatoManrev;
    @JsonProperty("dt_inizio_missione")
    private String dtInizioMissione;
    @JsonProperty("dt_fine_missione")
    private String dtFineMissione;
    @JsonProperty("ti_anagrafico")
    private String tiAnagrafico;
    @JsonProperty("annoMandatoAnticipo")
    private Integer annoMandatoAnticipo;
    @JsonProperty("numeroMandatoAnticipo")
    private Long numeroMandatoAnticipo;
    @JsonProperty("idRimborsoMissione")
    private Long idRimborsoMissione;
    @JsonProperty("idFlusso")
    private String idFlusso;
    @JsonProperty("idFlussoOrdineMissione")
    private String idFlussoOrdineMissione;
    @JsonProperty("idFolderRimborsoMissione")
    private String idFolderRimborsoMissione;
    @JsonProperty("idFolderOrdineMissione")
    private String idFolderOrdineMissione;
    @JsonProperty("tam")
    private String tam;
    @JsonProperty("cdsObblGeMis")
    private String cdsObblGeMis;
    @JsonProperty("esercizioObblGeMis")
    private Integer esercizioObblGeMis;
    @JsonProperty("esercizioOriObblGeMis")
    private Integer esercizioOriObblGeMis;
    @JsonProperty("pgMissioneFromGeMis")
    private Long pgMissioneFromGeMis;
    @JsonProperty("pgObblGeMis")
    private Long pgObblGeMis;
    @JsonProperty("gaeGeMis")
    private String gaeGeMis;
    @JsonProperty("cdrGeMis")
    private String cdrGeMis;
    @JsonProperty("voceGeMis")
    private String voceGeMis;
    @JsonProperty("tipo_rapporto")
    private TipoRapporto tipoRapporto;
    @JsonProperty("banca")
    private Banca banca;
    @JsonProperty("rif_inquadramento")
    private RifInquadramento rifInquadramento;
    @JsonProperty("modalita_pagamento")
    private ModalitaPagamento modalitaPagamento;
    // inizio rem aggiunta per multi impegno
//    @JsonProperty("missioneRigaColl")
//    private List<MissioneRigaColl> missioneRigaColl = new ArrayList<>();
    // fine aggiunta per multi impegno
    @JsonProperty("tappeMissioneColl")
    private List<TappeMissioneColl> tappeMissioneColl = null;
    @JsonProperty("speseMissioneColl")
    private List<SpeseMissioneColl> speseMissioneColl = null;
    @JsonProperty("cdsAnticipoGeMis")
    private String cdsAnticipoGeMis;
    @JsonProperty("esercizioAnticipoGeMis")
    private Integer esercizioAnticipoGeMis;
    @JsonProperty("pgAnticipoGeMis")
    private Long pgAnticipoGeMis;
    @JsonProperty("cognome")
    private String cognome;
    @JsonProperty("nome")
    private String nome;
    @JsonProperty("codice_fiscale")
    private String codice_fiscale;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

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
     * @return The cdCds
     */
    @JsonProperty("cd_cds")
    public String getCdCds() {
        return cdCds;
    }

    /**
     * @param cdCds The cd_cds
     */
    @JsonProperty("cd_cds")
    public void setCdCds(String cdCds) {
        this.cdCds = cdCds;
    }

    /**
     * @return The cdUnitaOrganizzativa
     */
    @JsonProperty("cd_unita_organizzativa")
    public String getCdUnitaOrganizzativa() {
        return cdUnitaOrganizzativa;
    }

    /**
     * @param cdUnitaOrganizzativa The cd_unita_organizzativa
     */
    @JsonProperty("cd_unita_organizzativa")
    public void setCdUnitaOrganizzativa(String cdUnitaOrganizzativa) {
        this.cdUnitaOrganizzativa = cdUnitaOrganizzativa;
    }

    /**
     * @return The cdTerzo
     */
    @JsonProperty("cd_terzo")
    public Integer getCdTerzo() {
        return cdTerzo;
    }

    /**
     * @param cdTerzo The cd_terzo
     */
    @JsonProperty("cd_terzo")
    public void setCdTerzo(Integer cdTerzo) {
        this.cdTerzo = cdTerzo;
    }

    /**
     * @return The dsMissione
     */
    @JsonProperty("ds_missione")
    public String getDsMissione() {
        return dsMissione;
    }

    /**
     * @param dsMissione The ds_missione
     */
    @JsonProperty("ds_missione")
    public void setDsMissione(String dsMissione) {
        this.dsMissione = dsMissione;
    }

    /**
     * @return The imSpese
     */
    @JsonProperty("im_spese")
    public BigDecimal getImSpese() {
        return imSpese;
    }

    /**
     * @param imSpese The im_spese
     */
    @JsonProperty("im_spese")
    public void setImSpese(BigDecimal imSpese) {
        this.imSpese = imSpese;
    }

    /**
     * @return The imDiariaNetto
     */
    @JsonProperty("im_diaria_netto")
    public BigDecimal getImDiariaNetto() {
        return imDiariaNetto;
    }

    /**
     * @param imDiariaNetto The im_diaria_netto
     */
    @JsonProperty("im_diaria_netto")
    public void setImDiariaNetto(BigDecimal imDiariaNetto) {
        this.imDiariaNetto = imDiariaNetto;
    }

    /**
     * @return The imTotaleMissione
     */
    @JsonProperty("im_totale_missione")
    public BigDecimal getImTotaleMissione() {
        return imTotaleMissione;
    }

    /**
     * @param imTotaleMissione The im_totale_missione
     */
    @JsonProperty("im_totale_missione")
    public void setImTotaleMissione(BigDecimal imTotaleMissione) {
        this.imTotaleMissione = imTotaleMissione;
    }

    @JsonProperty("importoDaRimborsare")
    public BigDecimal getImportoDaRimborsare() {
        return importoDaRimborsare;
    }

    @JsonProperty("importoDaRimborsare")
    public void setImportoDaRimborsare(BigDecimal importoDaRimborsare) {
        this.importoDaRimborsare = importoDaRimborsare;
    }

    /**
     * @return The imLordoPercepiente
     */
    @JsonProperty("im_lordo_percepiente")
    public BigDecimal getImLordoPercepiente() {
        return imLordoPercepiente;
    }

    /**
     * @param imLordoPercepiente The im_lordo_percepiente
     */
    @JsonProperty("im_lordo_percepiente")
    public void setImLordoPercepiente(BigDecimal imLordoPercepiente) {
        this.imLordoPercepiente = imLordoPercepiente;
    }

    /**
     * @return The imNettoPecepiente
     */
    @JsonProperty("im_netto_pecepiente")
    public BigDecimal getImNettoPecepiente() {
        return imNettoPecepiente;
    }

    /**
     * @param imNettoPecepiente The im_netto_pecepiente
     */
    @JsonProperty("im_netto_pecepiente")
    public void setImNettoPecepiente(BigDecimal imNettoPecepiente) {
        this.imNettoPecepiente = imNettoPecepiente;
    }

    /**
     * @return The imSpeseAnticipate
     */
    @JsonProperty("im_spese_anticipate")
    public BigDecimal getImSpeseAnticipate() {
        return imSpeseAnticipate;
    }

    /**
     * @param imSpeseAnticipate The im_spese_anticipate
     */
    @JsonProperty("im_spese_anticipate")
    public void setImSpeseAnticipate(BigDecimal imSpeseAnticipate) {
        this.imSpeseAnticipate = imSpeseAnticipate;
    }

    /**
     * @return The imDiariaLorda
     */
    @JsonProperty("im_diaria_lorda")
    public BigDecimal getImDiariaLorda() {
        return imDiariaLorda;
    }

    /**
     * @param imDiariaLorda The im_diaria_lorda
     */
    @JsonProperty("im_diaria_lorda")
    public void setImDiariaLorda(BigDecimal imDiariaLorda) {
        this.imDiariaLorda = imDiariaLorda;
    }

    /**
     * @return The imQuotaEsente
     */
    @JsonProperty("im_quota_esente")
    public BigDecimal getImQuotaEsente() {
        return imQuotaEsente;
    }

    /**
     * @param imQuotaEsente The im_quota_esente
     */
    @JsonProperty("im_quota_esente")
    public void setImQuotaEsente(BigDecimal imQuotaEsente) {
        this.imQuotaEsente = imQuotaEsente;
    }

    /**
     * @return The imRimborso
     */
    @JsonProperty("im_rimborso")
    public BigDecimal getImRimborso() {
        return imRimborso;
    }

    /**
     * @param imRimborso The im_rimborso
     */
    @JsonProperty("im_rimborso")
    public void setImRimborso(BigDecimal imRimborso) {
        this.imRimborso = imRimborso;
    }

    /**
     * @return The tiIstituzCommerc
     */
    @JsonProperty("ti_istituz_commerc")
    public String getTiIstituzCommerc() {
        return tiIstituzCommerc;
    }

    /**
     * @param tiIstituzCommerc The ti_istituz_commerc
     */
    @JsonProperty("ti_istituz_commerc")
    public void setTiIstituzCommerc(String tiIstituzCommerc) {
        this.tiIstituzCommerc = tiIstituzCommerc;
    }

    /**
     * @return The flComuneProprio
     */
    @JsonProperty("fl_comune_proprio")
    public Boolean getFlComuneProprio() {
        return flComuneProprio;
    }

    /**
     * @param flComuneProprio The fl_comune_proprio
     */
    @JsonProperty("fl_comune_proprio")
    public void setFlComuneProprio(Boolean flComuneProprio) {
        this.flComuneProprio = flComuneProprio;
    }

    /**
     * @return The flComuneAltro
     */
    @JsonProperty("fl_comune_altro")
    public Boolean getFlComuneAltro() {
        return flComuneAltro;
    }

    /**
     * @param flComuneAltro The fl_comune_altro
     */
    @JsonProperty("fl_comune_altro")
    public void setFlComuneAltro(Boolean flComuneAltro) {
        this.flComuneAltro = flComuneAltro;
    }

    /**
     * @return The flComuneEstero
     */
    @JsonProperty("fl_comune_estero")
    public Boolean getFlComuneEstero() {
        return flComuneEstero;
    }

    /**
     * @param flComuneEstero The fl_comune_estero
     */
    @JsonProperty("fl_comune_estero")
    public void setFlComuneEstero(Boolean flComuneEstero) {
        this.flComuneEstero = flComuneEstero;
    }

    /**
     * @return The flAssociatoCompenso
     */
    @JsonProperty("fl_associato_compenso")
    public Boolean getFlAssociatoCompenso() {
        return flAssociatoCompenso;
    }

    /**
     * @param flAssociatoCompenso The fl_associato_compenso
     */
    @JsonProperty("fl_associato_compenso")
    public void setFlAssociatoCompenso(Boolean flAssociatoCompenso) {
        this.flAssociatoCompenso = flAssociatoCompenso;
    }

    /**
     * @return The statoCoge
     */
    @JsonProperty("stato_coge")
    public String getStatoCoge() {
        return statoCoge;
    }

    /**
     * @param statoCoge The stato_coge
     */
    @JsonProperty("stato_coge")
    public void setStatoCoge(String statoCoge) {
        this.statoCoge = statoCoge;
    }

    /**
     * @return The statoCofi
     */
    @JsonProperty("stato_cofi")
    public String getStatoCofi() {
        return statoCofi;
    }

    /**
     * @param statoCofi The stato_cofi
     */
    @JsonProperty("stato_cofi")
    public void setStatoCofi(String statoCofi) {
        this.statoCofi = statoCofi;
    }

    /**
     * @return The statoCoan
     */
    @JsonProperty("stato_coan")
    public String getStatoCoan() {
        return statoCoan;
    }

    /**
     * @param statoCoan The stato_coan
     */
    @JsonProperty("stato_coan")
    public void setStatoCoan(String statoCoan) {
        this.statoCoan = statoCoan;
    }

    /**
     * @return The statoPagamentoFondoEco
     */
    @JsonProperty("stato_pagamento_fondo_eco")
    public String getStatoPagamentoFondoEco() {
        return statoPagamentoFondoEco;
    }

    /**
     * @param statoPagamentoFondoEco The stato_pagamento_fondo_eco
     */
    @JsonProperty("stato_pagamento_fondo_eco")
    public void setStatoPagamentoFondoEco(String statoPagamentoFondoEco) {
        this.statoPagamentoFondoEco = statoPagamentoFondoEco;
    }

    /**
     * @return The statoLiquidazione
     */
    @JsonProperty("stato_liquidazione")
    public String getStatoLiquidazione() {
        return statoLiquidazione;
    }

    /**
     * @param statoLiquidazione The stato_liquidazione
     */
    @JsonProperty("stato_liquidazione")
    public void setStatoLiquidazione(String statoLiquidazione) {
        this.statoLiquidazione = statoLiquidazione;
    }

    /**
     * @return The tiProvvisorioDefinitivo
     */
    @JsonProperty("ti_provvisorio_definitivo")
    public String getTiProvvisorioDefinitivo() {
        return tiProvvisorioDefinitivo;
    }

    /**
     * @param tiProvvisorioDefinitivo The ti_provvisorio_definitivo
     */
    @JsonProperty("ti_provvisorio_definitivo")
    public void setTiProvvisorioDefinitivo(String tiProvvisorioDefinitivo) {
        this.tiProvvisorioDefinitivo = tiProvvisorioDefinitivo;
    }

    /**
     * @return The tiAssociatoManrev
     */
    @JsonProperty("ti_associato_manrev")
    public String getTiAssociatoManrev() {
        return tiAssociatoManrev;
    }

    /**
     * @param tiAssociatoManrev The ti_associato_manrev
     */
    @JsonProperty("ti_associato_manrev")
    public void setTiAssociatoManrev(String tiAssociatoManrev) {
        this.tiAssociatoManrev = tiAssociatoManrev;
    }

    /**
     * @return The dtInizioMissione
     */
    @JsonProperty("dt_inizio_missione")
    public String getDtInizioMissione() {
        return dtInizioMissione;
    }

    /**
     * @param dtInizioMissione The dt_inizio_missione
     */
    @JsonProperty("dt_inizio_missione")
    public void setDtInizioMissione(String dtInizioMissione) {
        this.dtInizioMissione = dtInizioMissione;
    }

    /**
     * @return The dtFineMissione
     */
    @JsonProperty("dt_fine_missione")
    public String getDtFineMissione() {
        return dtFineMissione;
    }

    /**
     * @param dtFineMissione The dt_fine_missione
     */
    @JsonProperty("dt_fine_missione")
    public void setDtFineMissione(String dtFineMissione) {
        this.dtFineMissione = dtFineMissione;
    }

    /**
     * @return The tiAnagrafico
     */
    @JsonProperty("ti_anagrafico")
    public String getTiAnagrafico() {
        return tiAnagrafico;
    }

    /**
     * @param tiAnagrafico The ti_anagrafico
     */
    @JsonProperty("ti_anagrafico")
    public void setTiAnagrafico(String tiAnagrafico) {
        this.tiAnagrafico = tiAnagrafico;
    }

    /**
     * @return The tipoRapporto
     */
    @JsonProperty("tipo_rapporto")
    public TipoRapporto getTipoRapporto() {
        return tipoRapporto;
    }

    /**
     * @param tipoRapporto The tipo_rapporto
     */
    @JsonProperty("tipo_rapporto")
    public void setTipoRapporto(TipoRapporto tipoRapporto) {
        this.tipoRapporto = tipoRapporto;
    }

    /**
     * @return The banca
     */
    @JsonProperty("banca")
    public Banca getBanca() {
        return banca;
    }

    /**
     * @param banca The banca
     */
    @JsonProperty("banca")
    public void setBanca(Banca banca) {
        this.banca = banca;
    }

    /**
     * @return The rifInquadramento
     */
    @JsonProperty("rif_inquadramento")
    public RifInquadramento getRifInquadramento() {
        return rifInquadramento;
    }

    /**
     * @param rifInquadramento The rif_inquadramento
     */
    @JsonProperty("rif_inquadramento")
    public void setRifInquadramento(RifInquadramento rifInquadramento) {
        this.rifInquadramento = rifInquadramento;
    }

    /**
     * @return The modalitaPagamento
     */
    @JsonProperty("modalita_pagamento")
    public ModalitaPagamento getModalitaPagamento() {
        return modalitaPagamento;
    }

    /**
     * @param modalitaPagamento The modalita_pagamento
     */
    @JsonProperty("modalita_pagamento")
    public void setModalitaPagamento(ModalitaPagamento modalitaPagamento) {
        this.modalitaPagamento = modalitaPagamento;
    }

    /**
     * @return The tappeMissioneColl
     */
    @JsonProperty("tappeMissioneColl")
    public List<TappeMissioneColl> getTappeMissioneColl() {
        return tappeMissioneColl;
    }

    /**
     * @param tappeMissioneColl The tappeMissioneColl
     */
    @JsonProperty("tappeMissioneColl")
    public void setTappeMissioneColl(List<TappeMissioneColl> tappeMissioneColl) {
        this.tappeMissioneColl = tappeMissioneColl;
    }

    /**
     * @return The speseMissioneColl
     */
    @JsonProperty("speseMissioneColl")
    public List<SpeseMissioneColl> getSpeseMissioneColl() {
        return speseMissioneColl;
    }

    /**
     * @param speseMissioneColl The speseMissioneColl
     */
    @JsonProperty("speseMissioneColl")
    public void setSpeseMissioneColl(List<SpeseMissioneColl> speseMissioneColl) {
        this.speseMissioneColl = speseMissioneColl;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public Integer getAnnoMandatoAnticipo() {
        return annoMandatoAnticipo;
    }

    public void setAnnoMandatoAnticipo(Integer annoMandatoAnticipo) {
        this.annoMandatoAnticipo = annoMandatoAnticipo;
    }

    public Long getNumeroMandatoAnticipo() {
        return numeroMandatoAnticipo;
    }

    public void setNumeroMandatoAnticipo(Long numeroMandatoAnticipo) {
        this.numeroMandatoAnticipo = numeroMandatoAnticipo;
    }

    public Long getIdRimborsoMissione() {
        return idRimborsoMissione;
    }

    public void setIdRimborsoMissione(Long idRimborsoMissione) {
        this.idRimborsoMissione = idRimborsoMissione;
    }

    public String getIdFlusso() {
        return idFlusso;
    }

    public void setIdFlusso(String idFlusso) {
        this.idFlusso = idFlusso;
    }

    public String getTam() {
        return tam;
    }

    public void setTam(String tam) {
        this.tam = tam;
    }

    public Long getPgMissione() {
        return pgMissione;
    }

    public void setPgMissione(Long pgMissione) {
        this.pgMissione = pgMissione;
    }

    public String getCdsObblGeMis() {
        return cdsObblGeMis;
    }

    public void setCdsObblGeMis(String cdsObblGeMis) {
        this.cdsObblGeMis = cdsObblGeMis;
    }

    public Integer getEsercizioObblGeMis() {
        return esercizioObblGeMis;
    }

    public void setEsercizioObblGeMis(Integer esercizioObblGeMis) {
        this.esercizioObblGeMis = esercizioObblGeMis;
    }

    public Integer getEsercizioOriObblGeMis() {
        return esercizioOriObblGeMis;
    }

    public void setEsercizioOriObblGeMis(Integer esercizioOriObblGeMis) {
        this.esercizioOriObblGeMis = esercizioOriObblGeMis;
    }

    public Long getPgObblGeMis() {
        return pgObblGeMis;
    }

    public void setPgObblGeMis(Long pgObblGeMis) {
        this.pgObblGeMis = pgObblGeMis;
    }

    public String getGaeGeMis() {
        return gaeGeMis;
    }

    public void setGaeGeMis(String gaeGeMis) {
        this.gaeGeMis = gaeGeMis;
    }

    public String getIdFlussoOrdineMissione() {
        return idFlussoOrdineMissione;
    }

    public void setIdFlussoOrdineMissione(String idFlussoOrdineMissione) {
        this.idFlussoOrdineMissione = idFlussoOrdineMissione;
    }

    public String getIdFolderRimborsoMissione() {
        return idFolderRimborsoMissione;
    }

    public void setIdFolderRimborsoMissione(String idFolderRimborsoMissione) {
        this.idFolderRimborsoMissione = idFolderRimborsoMissione;
    }

    public String getIdFolderOrdineMissione() {
        return idFolderOrdineMissione;
    }

    public void setIdFolderOrdineMissione(String idFolderOrdineMissione) {
        this.idFolderOrdineMissione = idFolderOrdineMissione;
    }

    public String getCdsAnticipoGeMis() {
        return cdsAnticipoGeMis;
    }

    public void setCdsAnticipoGeMis(String cdsAnticipoGeMis) {
        this.cdsAnticipoGeMis = cdsAnticipoGeMis;
    }

    public Integer getEsercizioAnticipoGeMis() {
        return esercizioAnticipoGeMis;
    }

    public void setEsercizioAnticipoGeMis(Integer esercizioAnticipoGeMis) {
        this.esercizioAnticipoGeMis = esercizioAnticipoGeMis;
    }

    public Long getPgAnticipoGeMis() {
        return pgAnticipoGeMis;
    }

    public void setPgAnticipoGeMis(Long pgAnticipoGeMis) {
        this.pgAnticipoGeMis = pgAnticipoGeMis;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodice_fiscale() {
        return codice_fiscale;
    }

    public void setCodice_fiscale(String codice_fiscale) {
        this.codice_fiscale = codice_fiscale;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Long getPgMissioneFromGeMis() {
        return pgMissioneFromGeMis;
    }

    public void setPgMissioneFromGeMis(Long pgMissioneFromGeMis) {
        this.pgMissioneFromGeMis = pgMissioneFromGeMis;
    }

    public String getCdrGeMis() {
        return cdrGeMis;
    }

    public void setCdrGeMis(String cdrGeMis) {
        this.cdrGeMis = cdrGeMis;
    }

    public String getVoceGeMis() {
        return voceGeMis;
    }

    public void setVoceGeMis(String voceGeMis) {
        this.voceGeMis = voceGeMis;
    }

    // inizio rem per multi impegno
//	public List<MissioneRigaColl> getMissioneRigaColl() {
//		return missioneRigaColl;
//	}
//
//	public void setMissioneRigaColl(List<MissioneRigaColl> missioneRigaColl) {
//		this.missioneRigaColl = missioneRigaColl;
//	}
    // fine rem per multi impegno

}
