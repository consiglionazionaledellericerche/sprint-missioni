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

package it.cnr.si.missioni.domain.custom.print;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PrintMissione {
    private Integer anno;

    private Long numero;

    private String dataInserimento;

    private String cognomeRich;

    private String nomeRich;

    private String matricolaRich;

    private String codiceFiscaleRich;

    private String luogoDiNascitaRich;

    private String dataDiNascitaRich;

    private String comuneResidenzaRich;

    private String indirizzoResidenzaRich;

    private String domicilioFiscaleRich;

    private String datoreLavoroRich;

    private String qualificaRich;

    private String livelloRich;

    private String oggetto;

    private String italiaEstero;

    private String destinazione;

    private String nazione;

    private String tipoMissione;

    private String trattamento;

    private String dataInizioMissione;

    private String dataFineMissione;

    private String voce;

    private String gae;

    private String cdrRich;

    private String uoRich;

    private String cdrSpesa;

    private String uoSpesa;

    private String modulo;

    private String cdsRich;

    private String cdsSpesa;

    private String cup;

    private String pgObbligazione;

    private String esercizioOriginaleObbligazione;

    private String utilizzoTaxi;

    private String utilizzoAutoNoleggio;

    private String utilizzoAutoServizio;

    private String personaleAlSeguito;

    private String noteUtilizzoTaxiNoleggio;

    public Integer getAnno() {
        return anno;
    }

    public void setAnno(Integer anno) {
        this.anno = anno;
    }

    public Long getNumero() {
        return numero;
    }

    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public String getDataInserimento() {
        return dataInserimento;
    }

    public void setDataInserimento(String dataInserimento) {
        this.dataInserimento = dataInserimento;
    }

    public String getMatricolaRich() {
        return matricolaRich;
    }

    public void setMatricolaRich(String matricolaRich) {
        this.matricolaRich = matricolaRich;
    }

    public String getCodiceFiscaleRich() {
        return codiceFiscaleRich;
    }

    public void setCodiceFiscaleRich(String codiceFiscaleRich) {
        this.codiceFiscaleRich = codiceFiscaleRich;
    }

    public String getLuogoDiNascitaRich() {
        return luogoDiNascitaRich;
    }

    public void setLuogoDiNascitaRich(String luogoDiNascitaRich) {
        this.luogoDiNascitaRich = luogoDiNascitaRich;
    }

    public String getDataDiNascitaRich() {
        return dataDiNascitaRich;
    }

    public void setDataDiNascitaRich(String dataDiNascitaRich) {
        this.dataDiNascitaRich = dataDiNascitaRich;
    }

    public String getComuneResidenzaRich() {
        return comuneResidenzaRich;
    }

    public void setComuneResidenzaRich(String comuneResidenzaRich) {
        this.comuneResidenzaRich = comuneResidenzaRich;
    }

    public String getIndirizzoResidenzaRich() {
        return indirizzoResidenzaRich;
    }

    public void setIndirizzoResidenzaRich(String indirizzoResidenzaRich) {
        this.indirizzoResidenzaRich = indirizzoResidenzaRich;
    }

    public String getDomicilioFiscaleRich() {
        return domicilioFiscaleRich;
    }

    public void setDomicilioFiscaleRich(String domicilioFiscaleRich) {
        this.domicilioFiscaleRich = domicilioFiscaleRich;
    }

    public String getDatoreLavoroRich() {
        return datoreLavoroRich;
    }

    public void setDatoreLavoroRich(String datoreLavoroRich) {
        this.datoreLavoroRich = datoreLavoroRich;
    }

    public String getQualificaRich() {
        return qualificaRich;
    }

    public void setQualificaRich(String qualificaRich) {
        this.qualificaRich = qualificaRich;
    }

    public String getLivelloRich() {
        return livelloRich;
    }

    public void setLivelloRich(String livelloRich) {
        this.livelloRich = livelloRich;
    }

    public String getOggetto() {
        return oggetto;
    }

    public void setOggetto(String oggetto) {
        this.oggetto = oggetto;
    }

    public String getDestinazione() {
        return destinazione;
    }

    public void setDestinazione(String destinazione) {
        this.destinazione = destinazione;
    }

    public String getNazione() {
        return nazione;
    }

    public void setNazione(String nazione) {
        this.nazione = nazione;
    }

    public String getTipoMissione() {
        return tipoMissione;
    }

    public void setTipoMissione(String tipoMissione) {
        this.tipoMissione = tipoMissione;
    }

    public String getTrattamento() {
        return trattamento;
    }

    public void setTrattamento(String trattamento) {
        this.trattamento = trattamento;
    }

    public String getDataInizioMissione() {
        return dataInizioMissione;
    }

    public void setDataInizioMissione(String dataInizioMissione) {
        this.dataInizioMissione = dataInizioMissione;
    }

    public String getDataFineMissione() {
        return dataFineMissione;
    }

    public void setDataFineMissione(String dataFineMissione) {
        this.dataFineMissione = dataFineMissione;
    }

    public String getVoce() {
        return voce;
    }

    public void setVoce(String voce) {
        this.voce = voce;
    }

    public String getGae() {
        return gae;
    }

    public void setGae(String gae) {
        this.gae = gae;
    }

    public String getCdrRich() {
        return cdrRich;
    }

    public void setCdrRich(String cdrRich) {
        this.cdrRich = cdrRich;
    }

    public String getUoRich() {
        return uoRich;
    }

    public void setUoRich(String uoRich) {
        this.uoRich = uoRich;
    }

    public String getCdrSpesa() {
        return cdrSpesa;
    }

    public void setCdrSpesa(String cdrSpesa) {
        this.cdrSpesa = cdrSpesa;
    }

    public String getUoSpesa() {
        return uoSpesa;
    }

    public void setUoSpesa(String uoSpesa) {
        this.uoSpesa = uoSpesa;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getCdsRich() {
        return cdsRich;
    }

    public void setCdsRich(String cdsRich) {
        this.cdsRich = cdsRich;
    }

    public String getCdsSpesa() {
        return cdsSpesa;
    }

    public void setCdsSpesa(String cdsSpesa) {
        this.cdsSpesa = cdsSpesa;
    }

    public String getCognomeRich() {
        return cognomeRich;
    }

    public void setCognomeRich(String cognomeRich) {
        this.cognomeRich = cognomeRich;
    }

    public String getNomeRich() {
        return nomeRich;
    }

    public void setNomeRich(String nomeRich) {
        this.nomeRich = nomeRich;
    }

    public String getItaliaEstero() {
        return italiaEstero;
    }

    public void setItaliaEstero(String italiaEstero) {
        this.italiaEstero = italiaEstero;
    }

    public String getCup() {
        return cup;
    }

    public void setCup(String cup) {
        this.cup = cup;
    }

    public String getPgObbligazione() {
        return pgObbligazione;
    }

    public void setPgObbligazione(String pgObbligazione) {
        this.pgObbligazione = pgObbligazione;
    }

    public String getEsercizioOriginaleObbligazione() {
        return esercizioOriginaleObbligazione;
    }

    public void setEsercizioOriginaleObbligazione(String esercizioOriginaleObbligazione) {
        this.esercizioOriginaleObbligazione = esercizioOriginaleObbligazione;
    }

    public String getUtilizzoTaxi() {
        return utilizzoTaxi;
    }

    public void setUtilizzoTaxi(String utilizzoTaxi) {
        this.utilizzoTaxi = utilizzoTaxi;
    }

    public String getUtilizzoAutoNoleggio() {
        return utilizzoAutoNoleggio;
    }

    public void setUtilizzoAutoNoleggio(String utilizzoAutoNoleggio) {
        this.utilizzoAutoNoleggio = utilizzoAutoNoleggio;
    }

    public String getUtilizzoAutoServizio() {
        return utilizzoAutoServizio;
    }

    public void setUtilizzoAutoServizio(String utilizzoAutoServizio) {
        this.utilizzoAutoServizio = utilizzoAutoServizio;
    }

    public String getPersonaleAlSeguito() {
        return personaleAlSeguito;
    }

    public void setPersonaleAlSeguito(String personaleAlSeguito) {
        this.personaleAlSeguito = personaleAlSeguito;
    }

    public String getNoteUtilizzoTaxiNoleggio() {
        return noteUtilizzoTaxiNoleggio;
    }

    public void setNoteUtilizzoTaxiNoleggio(String noteUtilizzoTaxiNoleggio) {
        this.noteUtilizzoTaxiNoleggio = noteUtilizzoTaxiNoleggio;
    }

}
