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

package it.cnr.si.missioni.service;

import it.cnr.jada.ejb.session.ComponentException;
import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.domain.custom.persistence.RimborsoMissione;
import it.cnr.si.missioni.domain.custom.persistence.RimborsoMissioneDettagli;
import it.cnr.si.missioni.domain.custom.print.PrintRimborsoMissione;
import it.cnr.si.missioni.domain.custom.print.PrintRimborsoMissioneDettagli;
import it.cnr.si.missioni.util.Costanti;
import it.cnr.si.missioni.util.DateUtils;
import it.cnr.si.missioni.util.Utility;
import it.cnr.si.missioni.util.proxy.json.object.*;
import it.cnr.si.missioni.util.proxy.json.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
public class PrintRimborsoMissioneService {
    private final Logger log = LoggerFactory.getLogger(PrintRimborsoMissioneService.class);
    @Autowired
    RimborsoImpegniService rimborsoImpegniService;
    @Autowired
    private Environment env;
    @Autowired
    private PrintService printService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private NazioneService nazioneService;

    @Autowired
    private UnitaOrganizzativaService unitaOrganizzativaService;

    @Autowired
    private CdrService cdrService;

    @Autowired
    private GaeService gaeService;

    @Autowired
    private ProgettoService progettoService;

    @Autowired
    private VoceService voceService;

    private PrintRimborsoMissione getPrintRimborsoMissione(RimborsoMissione rimborsoMissione, String currentLogin) throws AwesomeException, ComponentException {
        Account account = accountService.loadAccountFromUsername(rimborsoMissione.getUid());
        Nazione nazione = nazioneService.loadNazione(rimborsoMissione.getNazione());
        LocalDate data = LocalDate.now();
        int anno = data.getYear();
        Progetto progetto = progettoService.loadModulo(rimborsoMissione.getPgProgetto(), anno, rimborsoMissione.getUoSpesa());
        Voce voce = voceService.loadVoce(rimborsoMissione);
        Gae gae = gaeService.loadGae(rimborsoMissione);
        PrintRimborsoMissione printRimborsoMissione = new PrintRimborsoMissione();
        printRimborsoMissione.setAnno(rimborsoMissione.getAnno());
        printRimborsoMissione.setNumero(rimborsoMissione.getNumero());
        printRimborsoMissione.setCodiceFiscaleRich(account.getCodice_fiscale());
        printRimborsoMissione.setComuneResidenzaRich(Utility.nvl(rimborsoMissione.getComuneResidenzaRich()));
        if (account.getData_nascita() != null) {
            Date dataNas = DateUtils.parseDate(account.getData_nascita().substring(0, 10), "yyyy-MM-dd");
            printRimborsoMissione.setDataDiNascitaRich(DateUtils.getDateAsString(dataNas, DateUtils.PATTERN_DATE));
        }
        printRimborsoMissione.setDatoreLavoroRich(rimborsoMissione.getDatoreLavoroRich());
        printRimborsoMissione.setDomicilioFiscaleRich(Utility.nvl(rimborsoMissione.getDomicilioFiscaleRich()));
        printRimborsoMissione.setIndirizzoResidenzaRich(Utility.nvl(rimborsoMissione.getIndirizzoResidenzaRich()));
        printRimborsoMissione.setLivelloRich(rimborsoMissione.getLivelloRich() == null ? "" : rimborsoMissione.getLivelloRich());
        printRimborsoMissione.setLuogoDiNascitaRich(account.getComune_nascita());
        printRimborsoMissione.setMatricolaRich(account.getMatricola() != null ? account.getMatricola().toString() : "");
        printRimborsoMissione.setCognomeRich(account.getCognome());
        printRimborsoMissione.setNomeRich(account.getNome());
        printRimborsoMissione.setQualificaRich(rimborsoMissione.getQualificaRich() == null ? "" : rimborsoMissione.getQualificaRich());

        printRimborsoMissione.setDataFineMissione(DateUtils.getDateAsString(rimborsoMissione.getDataFineMissione(), DateUtils.PATTERN_DATETIME_NO_SEC));
        printRimborsoMissione.setDataInizioMissione(DateUtils.getDateAsString(rimborsoMissione.getDataInizioMissione(), DateUtils.PATTERN_DATETIME_NO_SEC));
        printRimborsoMissione.setDataInserimento(DateUtils.getDateAsString(rimborsoMissione.getDataInserimento(), DateUtils.PATTERN_DATE));
        printRimborsoMissione.setOggetto(rimborsoMissione.getOggetto());
        printRimborsoMissione.setDestinazione(rimborsoMissione.getDestinazione());
        printRimborsoMissione.setAltreSpese(rimborsoMissione.getAltreSpeseAntDescrizione() == null ? "" : rimborsoMissione.getAltreSpeseAntDescrizione());
        printRimborsoMissione.setAltreSpeseImporto(Utility.numberFormat(rimborsoMissione.getAltreSpeseAntImporto()));
        if (progetto != null) {
            printRimborsoMissione.setModulo(progetto.getCd_progetto() + " " + progetto.getDs_progetto());
        } else {
            printRimborsoMissione.setModulo("");
        }
        if (voce != null) {
            printRimborsoMissione.setVoce(voce.getCd_elemento_voce() + " " + voce.getDs_elemento_voce());
        } else {
            printRimborsoMissione.setVoce("");
        }
        if (gae != null) {
            printRimborsoMissione.setGae(gae.getCd_linea_attivita() + " " + gae.getDs_linea_attivita());
        } else {
            printRimborsoMissione.setGae("");
        }
        printRimborsoMissione.setUoSpesa(caricaUo(rimborsoMissione.getUoSpesa(), rimborsoMissione.getAnno()));
        printRimborsoMissione.setCdrSpesa(caricaCdr(rimborsoMissione.getCdrSpesa()));
/* INIZIO REM PER MULTI IMPEGNO
    	List<RimborsoImpegni> lista = rimborsoImpegniService.getRimborsoImpegni(new Long(rimborsoMissione.getId().toString()));
    	if (lista != null && !lista.isEmpty()){
    		String impegni = null;
    		for (RimborsoImpegni rimborsoImpegni : lista){
    			if (impegni != null){
    				impegni+="   ";
    			} else {
    				impegni = "";
    			}
    			impegni += rimborsoImpegni.getEsercizioOriginaleObbligazione()+"-"+rimborsoImpegni.getPgObbligazione();
    		}
        	printRimborsoMissione.setImpegni(impegni);
    	} 
    	FINE REM PER MULTI IMPEGNO */
        if (rimborsoMissione.getPgObbligazione() != null && rimborsoMissione.getEsercizioOriginaleObbligazione() != null) {
// INIZIO AGGIUNTA MULTI IMPEGNI
            String impegni = rimborsoMissione.getEsercizioOriginaleObbligazione() + "-" + rimborsoMissione.getPgObbligazione();
            printRimborsoMissione.setImpegni(impegni);
// FINE AGGIUNTA MULTI IMPEGNI
            printRimborsoMissione.setPgObbligazione(rimborsoMissione.getPgObbligazione().toString());
            printRimborsoMissione.setEsercizioOriginaleObbligazione(rimborsoMissione.getEsercizioOriginaleObbligazione().toString());
        } else {
            printRimborsoMissione.setPgObbligazione("");
            printRimborsoMissione.setEsercizioOriginaleObbligazione("");
        }
        if (rimborsoMissione.getAnticipoAnnoMandato() != null) {
            printRimborsoMissione.setAnnoMandato(rimborsoMissione.getAnticipoAnnoMandato().toString());
        } else {
            printRimborsoMissione.setAnnoMandato("");
        }
        printRimborsoMissione.setAnticipo(rimborsoMissione.getAnticipoRicevuto());
        if (rimborsoMissione.getCdTerzoSigla() != null) {
            printRimborsoMissione.setCdTerzo(rimborsoMissione.getCdTerzoSigla().toString());
        } else {
            printRimborsoMissione.setCdTerzo("");
        }
        if (rimborsoMissione.getDataFineEstero() != null) {
            printRimborsoMissione.setDataFineEstero(DateUtils.getDateAsString(rimborsoMissione.getDataFineEstero(), DateUtils.PATTERN_DATETIME_NO_SEC));
        } else {
            printRimborsoMissione.setDataFineEstero("");
        }
        if (rimborsoMissione.getDataInizioEstero() != null) {
            printRimborsoMissione.setDataInizioEstero(DateUtils.getDateAsString(rimborsoMissione.getDataInizioEstero(), DateUtils.PATTERN_DATETIME_NO_SEC));
        } else {
            printRimborsoMissione.setDataInizioEstero("");
        }
        printRimborsoMissione.setDataInizioMissione(DateUtils.getDateAsString(rimborsoMissione.getDataInizioMissione(), DateUtils.PATTERN_DATETIME_NO_SEC));
        printRimborsoMissione.setDataFineMissione(DateUtils.getDateAsString(rimborsoMissione.getDataFineMissione(), DateUtils.PATTERN_DATETIME_NO_SEC));
        if (rimborsoMissione.getIban() != null) {
            printRimborsoMissione.setIban(rimborsoMissione.getIban());
        } else {
            printRimborsoMissione.setIban("");
        }
        if (rimborsoMissione.getAnticipoImporto() != null) {
            printRimborsoMissione.setImportoMandato(Utility.numberFormat(rimborsoMissione.getAnticipoImporto()));
        } else {
            printRimborsoMissione.setImportoMandato("");
        }
        printRimborsoMissione.setItaliaEstero(rimborsoMissione.decodeTipoMissione());
        printRimborsoMissione.setTipoMissione(rimborsoMissione.decodeTipoMissione());
        printRimborsoMissione.setModpag(rimborsoMissione.getModpag());
        if (rimborsoMissione.isMissioneEstera()) {
            printRimborsoMissione.setNazione(nazione.getDs_nazione());
        } else {
            printRimborsoMissione.setNazione("");
        }
        if (rimborsoMissione.getAnticipoNumeroMandato() != null) {
            printRimborsoMissione.setNumeroMandato(rimborsoMissione.getAnticipoNumeroMandato().toString());
        } else {
            printRimborsoMissione.setNumeroMandato("");
        }
        printRimborsoMissione.setSpeseTerzi(rimborsoMissione.getSpeseTerziRicevute());
        printRimborsoMissione.setImporto0(rimborsoMissione.getRimborso0());
        if (rimborsoMissione.getSpeseTerziImporto() != null) {
            printRimborsoMissione.setSpeseTerziImporto(Utility.numberFormat(rimborsoMissione.getSpeseTerziImporto()));
        } else {
            printRimborsoMissione.setSpeseTerziImporto("");
        }
        printRimborsoMissione.setTrattamento(rimborsoMissione.decodeTrattamento());
        BigDecimal totMissione = BigDecimal.ZERO;
        if (rimborsoMissione.getRimborsoMissioneDettagli() != null && !rimborsoMissione.getRimborsoMissioneDettagli().isEmpty()) {
            List<PrintRimborsoMissioneDettagli> listDettagliPrint = new ArrayList<PrintRimborsoMissioneDettagli>();
            for (Iterator<RimborsoMissioneDettagli> iterator = rimborsoMissione.getRimborsoMissioneDettagli().iterator(); iterator.hasNext(); ) {
                RimborsoMissioneDettagli dettagli = iterator.next();
                PrintRimborsoMissioneDettagli dettagliPrint = new PrintRimborsoMissioneDettagli();
                String dsSpesa = "";
                if (dettagli.getDsSpesa() != null) {
                    dsSpesa = dettagli.getDsTiSpesa() + " - " + dettagli.getDsSpesa();
                } else {
                    dsSpesa = dettagli.getDsTiSpesa();
                }
                if (Utility.nvl(dettagli.getTiCdTiSpesa()).equals("P")) {
                    dsSpesa = dsSpesa + " - " + dettagli.getCdTiPasto();
                }
                if (dettagli.isSpesaAnticipata()) {
                    dsSpesa = dsSpesa + " - SPESA ANTICIPATA";
                } else {
                    totMissione = totMissione.add(dettagli.getImportoEuro());
                }
                dettagliPrint.setDsSpesa(dsSpesa);
                dettagliPrint.setData(DateUtils.getDateAsString(dettagli.getDataSpesa(), DateUtils.PATTERN_DATE));
                if (dettagli.getImportoEuro() != null) {
                    dettagliPrint.setImporto(Utility.numberFormat(dettagli.getImportoEuro()));
                } else {
                    dettagliPrint.setImporto("");
                }
                if (dettagli.getKmPercorsi() != null) {
                    dettagliPrint.setKmPercorsi(dettagli.getKmPercorsi().toString());
                } else {
                    dettagliPrint.setKmPercorsi("");
                }
                listDettagliPrint.add(dettagliPrint);
            }
            printRimborsoMissione.setPrintDettagliSpeseRimborsoMissione(listDettagliPrint);
        }
        printRimborsoMissione.setUtilizzoAutoNoleggio(rimborsoMissione.decodeUtilizzoAutoNoleggio());
        printRimborsoMissione.setUtilizzoTaxi(rimborsoMissione.decodeUtilizzoTaxi());
        printRimborsoMissione.setUtilizzoAutoServizio(rimborsoMissione.decodeUtilizzoAutoServizio());
        printRimborsoMissione.setPersonaleAlSeguito(rimborsoMissione.decodePersonaleAlSeguito());
        printRimborsoMissione.setNoteUtilizzoTaxiNoleggio(Utility.nvl(rimborsoMissione.getNoteUtilizzoTaxiNoleggio()));
        printRimborsoMissione.setNote(Utility.nvl(rimborsoMissione.getNote()));
        printRimborsoMissione.setTotMissione(Utility.numberFormat(totMissione));
        printRimborsoMissione.setCup(rimborsoMissione.getCup() == null ? "" : rimborsoMissione.getCup());
        return printRimborsoMissione;
    }

    private String caricaUo(String cdUo, Integer anno) {
        if (cdUo != null) {
            UnitaOrganizzativa uo = unitaOrganizzativaService.loadUo(cdUo, null, anno);
            return uo.getCd_unita_organizzativa() + " " + uo.getDs_unita_organizzativa();
        }
        return "";
    }

    private String caricaCdr(String cdCdr) {
        if (cdCdr != null) {
            Cdr cdr = cdrService.loadCdr(cdCdr, null);
            return cdr.getCd_centro_responsabilita() + " " + cdr.getDs_cdr();
        }
        return "";
    }

    public byte[] printRimborsoMissione(RimborsoMissione rimborsoMissione, String currentLogin) throws AwesomeException, ComponentException {
        String myJson = createJsonPrintRimborsoMissione(rimborsoMissione, currentLogin);
        String nomeStampa = "";
        if (env != null && env.getProperty("spring.print." + Costanti.NOME_STAMPA_RIMBORSO) != null) {
            nomeStampa = env.getProperty("spring.print." + Costanti.NOME_STAMPA_RIMBORSO);
        } else {
            throw new ComponentException("Configurare il nome stampa del rimborso");
        }

        return printService.print(myJson, nomeStampa, rimborsoMissione.getId());
    }

    public String createJsonPrintRimborsoMissione(RimborsoMissione rimborsoMissione, String currentLogin) throws ComponentException {
        PrintRimborsoMissione printRimborsoMissione = getPrintRimborsoMissione(rimborsoMissione, currentLogin);
        return printService.createJsonForPrint(printRimborsoMissione);
    }

}
