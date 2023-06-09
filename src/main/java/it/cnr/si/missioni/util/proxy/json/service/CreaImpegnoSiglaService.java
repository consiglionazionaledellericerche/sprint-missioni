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

package it.cnr.si.missioni.util.proxy.json.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnr.jada.ejb.session.ComponentException;
import it.cnr.si.missioni.cmis.CMISOrdineMissioneService;
import it.cnr.si.missioni.cmis.CMISRimborsoMissioneService;
import it.cnr.si.missioni.domain.custom.persistence.DatiIstituto;
import it.cnr.si.missioni.domain.custom.persistence.OrdineMissione;
import it.cnr.si.missioni.domain.custom.persistence.RimborsoMissione;
import it.cnr.si.missioni.domain.custom.persistence.RimborsoMissioneDettagli;
import it.cnr.si.missioni.repository.CRUDComponentSession;
import it.cnr.si.missioni.service.RimborsoMissioneService;
import it.cnr.si.missioni.util.Costanti;
import it.cnr.si.missioni.util.DateUtils;
import it.cnr.si.missioni.util.Utility;
import it.cnr.si.missioni.util.proxy.json.object.impegno.ObbligazioneBulk;
import it.cnr.si.missioni.util.proxy.json.object.rimborso.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CreaImpegnoSiglaService {
    private final Logger log = LoggerFactory.getLogger(CreaImpegnoSiglaService.class);
    @Autowired
    private CommonService commonService;

    @Autowired
    private CRUDComponentSession crudServiceBean;

    @Autowired
    private RimborsoMissioneService rimborsoMissioneService;

    @Autowired
    private CMISRimborsoMissioneService cmisRimborsoMissioneService;

    @Autowired
    private CMISOrdineMissioneService cmisOrdineMissioneService;

    @Autowired
    private AccountService accountService;

    @Transactional(propagation = Propagation.REQUIRED)
    public ObbligazioneBulk creaImpegnoSigla(OrdineMissione ordine, DatiIstituto datiIstituto) throws Exception {
//		if (!ordine.isTrattamentoAlternativoMissione() && Utility.nvl(ordine.getImportoPresunto()).compareTo(BigDecimal.ZERO) > 0){
//			ObbligazioneBulk obbligazione = new ObbligazioneBulk();
//			obbligazione.setCds(ordine.getCdsSpesa());
//			obbligazione.setEsercizio(recuperoAnno());
//
//			if (datiIstituto.getTerzoImpDefault() != null){
//				obbligazione.setCdTerzo(datiIstituto.getTerzoImpDefault().intValue());
//			}
//			obbligazione.setUo(ordine.getUoSpesa());
//			obbligazione.setCdElementoVoce(ordine.getVoce());
//			obbligazione.setDtRegistrazione(DateUtils.getDateAsString(ZonedDateTime.now(), DateUtils.PATTERN_DATE));
//			obbligazione.setEsercizioOriginale(recuperoAnno());
//			obbligazione.setTipoDocAmm(Costanti.TIPO_DOCUMENTO_OBBLIGAZIONI_SIGLA);
//			
//			oggettoBulk.setDtFineMissione(DateUtils.getDateAsString(rimborsoApprovato.getDataFineMissione(), DateUtils.PATTERN_DATETIME_WITH_TIMEZONE));
//			oggettoBulk.setDtInizioMissione(DateUtils.getDateAsString(rimborsoApprovato.getDataInizioMissione(), DateUtils.PATTERN_DATETIME_WITH_TIMEZONE));
//			oggettoBulk.setPgMissioneFromGeMis(rimborsoApprovato.getNumero());
//			oggettoBulk.setFlAssociatoCompenso(false);
//			if (rimborsoApprovato.isMissioneEstera()){
//				oggettoBulk.setFlComuneAltro(false);
//				oggettoBulk.setFlComuneEstero(true);
//			} else {
//				oggettoBulk.setFlComuneAltro(true);
//				oggettoBulk.setFlComuneEstero(false);
//			}
//			oggettoBulk.setFlComuneProprio(false);
//			oggettoBulk.setImDiariaLorda(BigDecimal.ZERO);
//			oggettoBulk.setImDiariaNetto(BigDecimal.ZERO);
//			oggettoBulk.setImQuotaEsente(BigDecimal.ZERO);
//			oggettoBulk.setImRimborso(BigDecimal.ZERO);
//			oggettoBulk.setImSpese(Utility.nvl(rimborsoApprovato.getTotaleRimborsoSenzaSpeseAnticipate()));
//			oggettoBulk.setImSpeseAnticipate(Utility.nvl(rimborsoApprovato.getTotaleSpeseAnticipate()));
//			oggettoBulk.setImTotaleMissione(rimborsoApprovato.getTotaleRimborsoSenzaSpeseAnticipate());
//			oggettoBulk.setImportoDaRimborsare(Utility.nvl(rimborsoApprovato.getTotaleRimborsoSenzaSpeseAnticipate()).subtract(Utility.nvl(rimborsoApprovato.getAnticipoImporto())));
//			if (!rimborsoApprovato.isTrattamentoAlternativoMissione()){
//				oggettoBulk.setImLordoPercepiente(Utility.nvl(rimborsoApprovato.getTotaleRimborsoSenzaSpeseAnticipate()));
//				oggettoBulk.setImNettoPecepiente(Utility.nvl(rimborsoApprovato.getTotaleRimborsoSenzaSpeseAnticipate()));
//			} else {
//				oggettoBulk.setImLordoPercepiente(BigDecimal.ZERO);
//				oggettoBulk.setImNettoPecepiente(BigDecimal.ZERO);
//			}
//			if (rimborsoApprovato.getAnticipoAnnoMandato() != null){
//				oggettoBulk.setEsercizioAnticipoGeMis(rimborsoApprovato.getAnticipoAnnoMandato());
//			}
//			if (rimborsoApprovato.getAnticipoNumeroMandato() != null){
//				oggettoBulk.setPgAnticipoGeMis(rimborsoApprovato.getAnticipoNumeroMandato());
//				oggettoBulk.setCdsAnticipoGeMis(rimborsoApprovato.getCdsSpesa());
//			}
//			oggettoBulk.setIdRimborsoMissione(new Long (rimborsoApprovato.getId().toString()));
//			oggettoBulk.setIdFlusso(rimborsoApprovato.getIdFlusso());
//			Account account = accountService.loadAccountFromRest(rimborsoApprovato.getUid());
//			oggettoBulk.setCognome(account.getCognome());
//			oggettoBulk.setNome(account.getNome());
//			impostaDescrizioneMissione(rimborsoApprovato, oggettoBulk);
//			oggettoBulk.setCodice_fiscale(account.getCodice_fiscale());
//			Folder folder = cmisRimborsoMissioneService.recuperoFolderRimborsoMissione(rimborsoApprovato);
//			if (folder != null){
//				oggettoBulk.setIdFolderRimborsoMissione(folder.getPropertyValue(PropertyIds.OBJECT_ID));
//			}
//			if (rimborsoApprovato.getOrdineMissione() != null && rimborsoApprovato.getOrdineMissione().getId() != null){
//				OrdineMissione ordineMissione = (OrdineMissione)crudServiceBean.findById( OrdineMissione.class, rimborsoApprovato.getOrdineMissione().getId());
//				if (ordineMissione != null){
//					if (ordineMissione.getIdFlusso() != null){
//						oggettoBulk.setIdFlussoOrdineMissione(ordineMissione.getIdFlusso());
//					}
//					Folder folderOrdine = cmisOrdineMissioneService.recuperoFolderOrdineMissione(ordineMissione);
//					if (folderOrdine != null){
//						oggettoBulk.setIdFolderOrdineMissione(folderOrdine.getPropertyValue(PropertyIds.OBJECT_ID));
//					}
//				}
//			}
//			if (!rimborsoApprovato.isTrattamentoAlternativoMissione()){
//				if (StringUtils.hasLength(rimborsoApprovato.getCdCdsObbligazione())){
//					oggettoBulk.setCdsObblGeMis(rimborsoApprovato.getCdCdsObbligazione());
//				}
//				if (rimborsoApprovato.getEsercizioObbligazione() != null){
//					oggettoBulk.setEsercizioObblGeMis(rimborsoApprovato.getEsercizioObbligazione());
//				}
//				if (rimborsoApprovato.getEsercizioOriginaleObbligazione() != null){
//					oggettoBulk.setEsercizioOriObblGeMis(rimborsoApprovato.getEsercizioOriginaleObbligazione());
//				}
//				if (rimborsoApprovato.getPgObbligazione() != null){
//					oggettoBulk.setPgObblGeMis(rimborsoApprovato.getPgObbligazione());
//				}
//				if (rimborsoApprovato.getGae() != null){
//					oggettoBulk.setGaeGeMis(rimborsoApprovato.getGae());
//				}
//				if (rimborsoApprovato.getCdrSpesa() != null){
//					oggettoBulk.setCdrGeMis(rimborsoApprovato.getCdrSpesa());
//				}
//				if (rimborsoApprovato.getVoce() != null){
//					oggettoBulk.setVoceGeMis(rimborsoApprovato.getVoce());
//				}
//			}
//			impostaModalitaPagamento(rimborsoApprovato, oggettoBulk);
//
//			impostaInquadramento(rimborsoApprovato, oggettoBulk);
//			impostaTappe(rimborsoApprovato, oggettoBulk);
//
//			TipoRapporto tipoRapporto = new TipoRapporto();
//			if (StringUtils.hasLength(rimborsoApprovato.getMatricola())){
//				tipoRapporto.setCdTipoRapporto("DIP");
//				oggettoBulk.setTiAnagrafico("D");
//			} else {
//				tipoRapporto.setCdTipoRapporto(rimborsoApprovato.getCdTipoRapporto());
//				oggettoBulk.setTiAnagrafico("A");
//			}
//
//			oggettoBulk.setTipoRapporto(tipoRapporto);
//
//			if (rimborsoApprovato.getRimborsoMissioneDettagli() != null && !rimborsoApprovato.getRimborsoMissioneDettagli().isEmpty()){
//				List<SpeseMissioneColl> speseMissioneColl = new ArrayList<SpeseMissioneColl>();
//
//				for (RimborsoMissioneDettagli dettaglio : rimborsoApprovato.getRimborsoMissioneDettagli()){
//					SpeseMissioneColl spesaMissione = new SpeseMissioneColl();
//					spesaMissione.setCdTiSpesa(dettaglio.getCdTiSpesa());
//					spesaMissione.setTiCdTiSpesa(dettaglio.getTiCdTiSpesa());
//					spesaMissione.setDsTiSpesa(dettaglio.getDsTiSpesa());
//					String dataTappa = recuperoDataTappa(oggettoBulk.getTappeMissioneColl(), dettaglio);
//					if (dataTappa != null){
//						spesaMissione.setDtInizioTappa(dataTappa);
//					} else {
//						throw new AwesomeException(CodiciErrore.ERRGEN, "Per il dettaglio spesa "+ dettaglio.getDsTiSpesa()+" del "+ DateUtils.getDefaultDateAsString(dettaglio.getDataSpesa())+ " del rimborso missione con id "+ rimborsoApprovato.getId() + " della uo "+rimborsoApprovato.getUoRich()+", anno "+rimborsoApprovato.getAnno()+", numero "+rimborsoApprovato.getNumero()+"  non esiste una tappa utile. Possibile incongruenza con le date di inizio e di fine missione.");
//					}
//
//					spesaMissione.setFlDiariaManuale(false);
//					spesaMissione.setFlSpesaAnticipata(dettaglio.getFlSpesaAnticipata().equals("S") ? true : false);
//					spesaMissione.setImBaseMaggiorazione(BigDecimal.ZERO);
//					spesaMissione.setImDiariaLorda(BigDecimal.ZERO);
//					spesaMissione.setImDiariaNetto(BigDecimal.ZERO);
//					spesaMissione.setImMaggiorazione(BigDecimal.ZERO);
//					spesaMissione.setImMaggiorazioneEuro(BigDecimal.ZERO);
//					spesaMissione.setImQuotaEsente(BigDecimal.ZERO);
//					spesaMissione.setPercentualeMaggiorazione(BigDecimal.ZERO);
//					spesaMissione.setImRimborso(BigDecimal.ZERO);
//					spesaMissione.setImSpesaMax(Costanti.IMPORTO_SPESA_MAX_DEFAULT);
//					spesaMissione.setImSpesaMaxDivisa(Costanti.IMPORTO_SPESA_MAX_DEFAULT);
//					spesaMissione.setImSpesaDivisa(dettaglio.getImportoDivisa());
//					spesaMissione.setImSpesaEuro(dettaglio.getImportoEuro());
//					spesaMissione.setImTotaleSpesa(dettaglio.getImportoEuro());
//					spesaMissione.setCdDivisaSpesa(Costanti.CODICE_DIVISA_DEFAULT_SIGLA);
//					spesaMissione.setCambioSpesa(BigDecimal.ONE);
//					spesaMissione.setPgRiga(dettaglio.getRiga().intValue());
//					String idFolderDettaglio = cmisRimborsoMissioneService.getNodeRefFolderDettaglioRimborso(new Long (dettaglio.getId().toString()));
//					if (idFolderDettaglio != null){
//						spesaMissione.setIdFolderDettagliGemis(idFolderDettaglio);
//						spesaMissione.setDsGiustificativo("DETTAGLIO DA GESTIONE AUTOMATICA MISSIONI");
//						spesaMissione.setIdGiustificativo(dettaglio.getRiga().toString());
//					} else {
//						if (dettaglio.isGiustificativoObbligatorio()){
//							if (dettaglio.getDsNoGiustificativo() != null){
//								spesaMissione.setDsNoGiustificativo(dettaglio.getDsNoGiustificativo());
//							} else {
//								throw new AwesomeException(CodiciErrore.ERRGEN, "Per il dettaglio spesa "+ dettaglio.getDsTiSpesa()+" del "+ DateUtils.getDefaultDateAsString(dettaglio.getDataSpesa())+ " del rimborso missione con id "+ rimborsoApprovato.getId() + " della uo "+rimborsoApprovato.getUoRich()+", anno "+rimborsoApprovato.getAnno()+", numero "+rimborsoApprovato.getNumero()+" è obbligatorio allegare almeno un giustificativo.");
//							}
//						}
//					}
//					if (dettaglio.isDettaglioPasto()){
//						spesaMissione.setCdTiPasto(dettaglio.getCdTiPasto());
//					}
//					if (dettaglio.isDettaglioIndennitaKm()){
//						spesaMissione.setTiAuto("P");
//						spesaMissione.setChilometri(dettaglio.getKmPercorsi());
//						spesaMissione.setLocalitaSpostamento(dettaglio.getLocalitaSpostamento());
//					} else {
//						spesaMissione.setChilometri(new Long(0));
//					}
//					if (dettaglio.getDsSpesa() != null){
//						if (dettaglio.getDsSpesa().length() > 100){
//							spesaMissione.setDsSpesa(dettaglio.getDsSpesa().substring(0, 100));
//						} else {
//							spesaMissione.setDsSpesa(dettaglio.getDsSpesa());
//						}
//					}
//					spesaMissione.setTiSpesaDiaria("S");
//					speseMissioneColl.add(spesaMissione);
//				}
//				oggettoBulk.setSpeseMissioneColl(speseMissioneColl);
//			}
//			oggettoBulk.setStatoCoan("N");
//			oggettoBulk.setStatoCofi("I");
//			oggettoBulk.setStatoCoge("N");
//			oggettoBulk.setStatoLiquidazione("SOSP");
//			oggettoBulk.setStatoPagamentoFondoEco("N");
//
//			oggettoBulk.setTiAssociatoManrev("N");
//			oggettoBulk.setTiIstituzCommerc("I");
//			oggettoBulk.setTiProvvisorioDefinitivo("P");
//			missioneSigla.setOggettoBulk(oggettoBulk);
//			MissioneBulk missioneBulk = comunica(oggettoBulk);
//			if (missioneBulk != null){
//				rimborsoMissioneService.aggiornaRimborsoMissioneComunicata(rimborsoApprovato, missioneBulk);
//			}
//			return missioneBulk;
//		}
        return null;
    }

    private int recuperoAnno() {
        return ZonedDateTime.now().getYear();
    }

    private void impostaDescrizioneMissione(RimborsoMissione rimborsoApprovato, MissioneBulk oggettoBulk) {
        String descrizioneMissione = "Missione a " + rimborsoApprovato.getDestinazione() + " del " + DateUtils.getDefaultDateAsString(rimborsoApprovato.getDataInizioMissione()) + " di " + Utility.nvl(oggettoBulk.getCognome()) + " " + Utility.nvl(oggettoBulk.getNome()) + " - " + rimborsoApprovato.getOggetto();
        if (descrizioneMissione.length() > 300) {
            descrizioneMissione = rimborsoApprovato.getDestinazione() + " del " + DateUtils.getDefaultDateAsString(rimborsoApprovato.getDataInizioMissione()) + " di " + Utility.nvl(oggettoBulk.getCognome()) + " " + Utility.nvl(oggettoBulk.getNome()) + " - " + rimborsoApprovato.getOggetto();
            if (descrizioneMissione.length() > 300) {
                descrizioneMissione = rimborsoApprovato.getDestinazione() + " del " + DateUtils.getDefaultDateAsString(rimborsoApprovato.getDataInizioMissione()) + " " + rimborsoApprovato.getOggetto();
                if (descrizioneMissione.length() > 300) {
                    descrizioneMissione = rimborsoApprovato.getOggetto();
                    if (descrizioneMissione.length() > 300) {
                        descrizioneMissione = descrizioneMissione.substring(0, 300);
                    }
                }
            }
        }
        oggettoBulk.setDsMissione(descrizioneMissione);
    }

    public MissioneBulk comunica(MissioneBulk missione) {
        if (missione != null) {
            String app = Costanti.APP_SIGLA;
            String url = Costanti.REST_COMUNICA_RIMBORSO_SIGLA;
            String body = prepareBody(missione);

            String risposta = commonService.process(body, app, url, true, HttpMethod.PUT);
            try {
                ObjectMapper mapper = new ObjectMapper();
                MissioneBulk missioneBulk = mapper.readValue(risposta, MissioneBulk.class);
                return missioneBulk;
            } catch (Exception ex) {
                throw new ComponentException("Errore nella lettura del file di risposta.", ex);
            }
        }
        return null;
    }

    public String prepareBody(MissioneBulk missione) {
        String body = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            body = mapper.writeValueAsString(missione);
            return body;
        } catch (Exception ex) {
            throw new ComponentException("Errore nella manipolazione del file JSON per la preparazione del body della richiesta REST (" + Utility.getMessageException(ex) + ").", ex);
        }
    }

    private String recuperoDataTappa(List<TappeMissioneColl> tappe, RimborsoMissioneDettagli dettaglio) {
        for (TappeMissioneColl tappa : tappe) {
            if (dettaglio.getDataSpesa().isEqual(DateUtils.parseLocalDate(tappa.getDtInizioTappa().substring(0, 10), DateUtils.PATTERN_DATE_FOR_DOCUMENTALE))) {
                return tappa.getDtInizioTappa();
            }
        }
        return null;
    }

    private void impostaTappe(RimborsoMissione rimborsoApprovato, MissioneBulk oggettoBulk)
            throws ComponentException {
        List<TappeMissioneColl> tappeMissioneColl = new ArrayList<TappeMissioneColl>();
        TappeMissioneColl tappa = new TappeMissioneColl();
        impostaDivisaTappa(tappa);
        tappa.setFlNoDiaria(true);
        tappa.setFlRimborso(rimborsoApprovato.isTrattamentoAlternativoMissione());
        tappa.setFlComuneAltro(oggettoBulk.getFlComuneAltro());
        tappa.setFlComuneEstero(oggettoBulk.getFlComuneEstero());
        tappa.setFlComuneProprio(oggettoBulk.getFlComuneProprio());
        tappa.setFlAlloggioGratuito(false);
        tappa.setFlNavigazione(false);
        tappa.setFlVittoAlloggioGratuito(false);
        tappa.setFlVittoGratuito(false);
        impostaNazioneRimborso(rimborsoApprovato, tappa);
        ZonedDateTime dataInizio = null;
        ZonedDateTime dataFine = null;
        if (!rimborsoApprovato.isMissioneEstera() || !rimborsoApprovato.isTrattamentoAlternativoMissione()) {
            dataInizio = rimborsoApprovato.getDataInizioMissione();
            dataFine = rimborsoApprovato.getDataFineMissione();
        } else {
            dataInizio = rimborsoApprovato.getDataInizioEstero();
            dataFine = rimborsoApprovato.getDataFineEstero();

        }
        tappeMissioneColl = impostaTappeDaDate(dataInizio, dataFine, tappa, tappeMissioneColl);
//			if (DateUtils.truncate(rimborsoApprovato.getDataInizioMissione()).compareTo(DateUtils.truncate(rimborsoApprovato.getDataInizioEstero())) == 0 && 
//					DateUtils.truncate(rimborsoApprovato.getDataFineMissione()).compareTo(DateUtils.truncate(rimborsoApprovato.getDataFineEstero())) == 0){
//				impostaNazioneRimborso(rimborsoApprovato, tappa);
//				tappeMissioneColl = impostaTappeDaDate(rimborsoApprovato.getDataInizioMissione(), rimborsoApprovato.getDataFineMissione(), tappa, tappeMissioneColl);
//			} else {
//				impostaNazioneRimborso(rimborsoApprovato, tappa);
//				tappeMissioneColl = impostaTappeDaDate(rimborsoApprovato.getDataInizioEstero(), rimborsoApprovato.getDataFineEstero(), tappa, tappeMissioneColl, rimborsoApprovato.getDataInizioMissione(), rimborsoApprovato.getDataFineMissione());
//			}
//		}
        oggettoBulk.setTappeMissioneColl(tappeMissioneColl);
    }

    private List<TappeMissioneColl> impostaTappeDaDate(ZonedDateTime daData, ZonedDateTime aData, TappeMissioneColl tappa, List<TappeMissioneColl> tappeMissioneColl) throws ComponentException {
        return impostaTappeDaDate(daData, aData, tappa, tappeMissioneColl, null, null);
    }

    private List<TappeMissioneColl> impostaTappeDaDate(ZonedDateTime daData, ZonedDateTime aData, TappeMissioneColl tappa, List<TappeMissioneColl> tappeMissioneColl, ZonedDateTime dataInizioMissione, ZonedDateTime dataFineMissione) throws ComponentException {
        ZonedDateTime ultimaDataInizioUsata = null;
        if (dataInizioMissione != null && !DateUtils.truncate(daData).equals(DateUtils.truncate(dataInizioMissione))) {
            for (ZonedDateTime data = dataInizioMissione; DateUtils.truncate(data).isBefore(DateUtils.truncate(daData)); data = data.plusDays(1)) {
                ultimaDataInizioUsata = data;
                TappeMissioneColl newDayTappa;
                try {
                    newDayTappa = (TappeMissioneColl) tappa.clone();
                } catch (CloneNotSupportedException e) {
                    log.error("Errore", e);
                    throw new ComponentException("Errore nel clone.", e);
                }
                impostaNazione(Costanti.NAZIONE_ITALIA_SIGLA, newDayTappa);
                ZonedDateTime dataFine = data.plusDays(1);
                if (dataFine.isAfter(dataFineMissione)) {
                    dataFine = dataFineMissione;
                }
                impostaDateTappa(data, dataFine, newDayTappa);
                tappeMissioneColl.add(newDayTappa);
            }
        }
        if (ultimaDataInizioUsata != null) {
            daData = ultimaDataInizioUsata.plusDays(1);
        }
        for (ZonedDateTime data = daData; DateUtils.truncate(data).isBefore(DateUtils.truncate(aData)) || DateUtils.truncate(data).isEqual(DateUtils.truncate(aData)); data = data.plusDays(1)) {
            ultimaDataInizioUsata = data;
            TappeMissioneColl newDayTappa;
            try {
                newDayTappa = (TappeMissioneColl) tappa.clone();
            } catch (CloneNotSupportedException e) {
                log.error("Errore", e);
                throw new ComponentException("Errore nel clone.", e);
            }
            ZonedDateTime dataFine = data.plusDays(1);
            if (dataFine.isAfter(aData)) {
                if (dataFineMissione == null) {
                    dataFine = aData;
                } else {
                    if (dataFine.isAfter(dataFineMissione)) {
                        dataFine = dataFineMissione;
                    }
                }
            }
            impostaDateTappa(data, dataFine, newDayTappa);
            tappeMissioneColl.add(newDayTappa);
        }
//		if (dataFineMissione != null && !DateUtils.truncate(aData).equals(DateUtils.truncate(dataFineMissione))){
//			ultimaDataInizioUsata = ultimaDataInizioUsata.plusDays(1);
//			impostaNazione(Costanti.NAZIONE_ITALIA_SIGLA, tappa);
//			for (ZonedDateTime data = ultimaDataInizioUsata; DateUtils.truncate(data).isBefore(DateUtils.truncate(dataFineMissione)) || DateUtils.truncate(data).isEqual(DateUtils.truncate(dataFineMissione)); data = data.plusDays(1))
//			{
//				ZonedDateTime dataInizio = data;
//				if (dataInizio.isAfter(dataFineMissione)){
//					dataInizio = dataFineMissione;
//				}
//				TappeMissioneColl newDayTappa;
//				try {
//					newDayTappa = (TappeMissioneColl)tappa.clone();
//				} catch (CloneNotSupportedException e) {
//					log.error("Errore",e);
//					throw new ComponentException("Errore nel clone.",e);
//				}
//				ZonedDateTime dataFine = dataInizio.plusDays(1);
//				if (dataFine.isAfter(dataFineMissione)){
//					dataFine = dataFineMissione;
//				}
//				impostaDateTappa(dataInizio, dataFine, newDayTappa);
//				tappeMissioneColl.add(newDayTappa);
//			}
//		}


        return tappeMissioneColl;
    }

    private void impostaDateTappa(ZonedDateTime dataInizio, ZonedDateTime dataFine, TappeMissioneColl tappa) {
        tappa.setDtInizioTappa(DateUtils.getDateAsString(dataInizio, DateUtils.PATTERN_DATETIME_WITH_TIMEZONE));
        tappa.setDtFineTappa(DateUtils.getDateAsString(dataFine, DateUtils.PATTERN_DATETIME_WITH_TIMEZONE));
    }

    private void impostaNazioneRimborso(RimborsoMissione rimborsoApprovato, TappeMissioneColl tappa) {
        impostaNazione(rimborsoApprovato.getNazione(), tappa);
    }

    private void impostaNazione(Long idNazione, TappeMissioneColl tappa) {
        Nazione nazione = new Nazione();
        nazione.setPgNazione(idNazione.intValue());
        tappa.setNazione(nazione);
    }

    private void impostaDivisaTappa(TappeMissioneColl tappa) {
        DivisaTappa divisa = new DivisaTappa();
        divisa.setCdDivisa(Costanti.CODICE_DIVISA_DEFAULT_SIGLA);
        tappa.setDivisaTappa(divisa);
        tappa.setCambioTappa(1);
    }

    private void impostaInquadramento(RimborsoMissione rimborsoApprovato, MissioneBulk oggettoBulk) {
        if (rimborsoApprovato.getInquadramento() != null) {
            RifInquadramento rifInquadramento = new RifInquadramento();
            rifInquadramento.setPgRifInquadramento(rimborsoApprovato.getInquadramento().intValue());
            oggettoBulk.setRifInquadramento(rifInquadramento);
        }
    }

    private void impostaModalitaPagamento(RimborsoMissione rimborsoApprovato, MissioneBulk oggettoBulk) {
        ModalitaPagamento modalitaPagamento = new ModalitaPagamento();
        modalitaPagamento.setCdModalitaPag(rimborsoApprovato.getModpag());
        oggettoBulk.setModalitaPagamento(modalitaPagamento);
    }

    private void impostaBanca(RimborsoMissione rimborsoApprovato, MissioneBulk oggettoBulk) {
        Banca banca = new Banca();
        banca.setPgBanca(rimborsoApprovato.getPgBanca());
        oggettoBulk.setBanca(banca);
    }

    private void impostaUserContext(RimborsoMissione rimborsoApprovato,
                                    MissioneSigla missioneSigla) {
        UserContext userContext = new UserContext();
        userContext.setCdCds(rimborsoApprovato.getCdsSpesa());
//f		userContext.setUser(principal.getName());
        missioneSigla.setUserContext(userContext);
    }
}
