package it.cnr.si.missioni.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import it.cnr.jada.criterion.CriterionList;
import it.cnr.jada.ejb.session.BusyResourceException;
import it.cnr.jada.ejb.session.ComponentException;
import it.cnr.jada.ejb.session.PersistencyException;
import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.cmis.CMISRimborsoMissioneService;
import it.cnr.si.missioni.cmis.ResultFlows;
import it.cnr.si.missioni.domain.custom.persistence.DatiIstituto;
import it.cnr.si.missioni.domain.custom.persistence.OrdineMissione;
import it.cnr.si.missioni.domain.custom.persistence.OrdineMissioneAutoPropria;
import it.cnr.si.missioni.domain.custom.persistence.RimborsoMissione;
import it.cnr.si.missioni.domain.custom.persistence.RimborsoMissioneDettagli;
import it.cnr.si.missioni.repository.CRUDComponentSession;
import it.cnr.si.missioni.util.CodiciErrore;
import it.cnr.si.missioni.util.Costanti;
import it.cnr.si.missioni.util.DateUtils;
import it.cnr.si.missioni.util.SecurityUtils;
import it.cnr.si.missioni.util.Utility;
import it.cnr.si.missioni.util.data.Uo;
import it.cnr.si.missioni.util.data.UoForUsersSpecial;
import it.cnr.si.missioni.util.data.UsersSpecial;
import it.cnr.si.missioni.util.proxy.json.object.Cdr;
import it.cnr.si.missioni.util.proxy.json.object.Gae;
import it.cnr.si.missioni.util.proxy.json.object.Impegno;
import it.cnr.si.missioni.util.proxy.json.object.ImpegnoGae;
import it.cnr.si.missioni.util.proxy.json.object.Progetto;
import it.cnr.si.missioni.util.proxy.json.object.UnitaOrganizzativa;
import it.cnr.si.missioni.util.proxy.json.object.rimborso.MissioneBulk;
import it.cnr.si.missioni.util.proxy.json.service.AccountService;
import it.cnr.si.missioni.util.proxy.json.service.CdrService;
import it.cnr.si.missioni.util.proxy.json.service.GaeService;
import it.cnr.si.missioni.util.proxy.json.service.ImpegnoGaeService;
import it.cnr.si.missioni.util.proxy.json.service.ImpegnoService;
import it.cnr.si.missioni.util.proxy.json.service.NazioneService;
import it.cnr.si.missioni.util.proxy.json.service.ProgettoService;
import it.cnr.si.missioni.util.proxy.json.service.TerzoService;
import it.cnr.si.missioni.util.proxy.json.service.UnitaOrganizzativaService;
import it.cnr.si.missioni.web.filter.RimborsoMissioneFilter;
import net.bzdyl.ejb3.criteria.Order;
import net.bzdyl.ejb3.criteria.restrictions.Disjunction;
import net.bzdyl.ejb3.criteria.restrictions.Restrictions;


/**
 * Service class for managing users.
 */
@Service
public class RimborsoMissioneService {

    private final Logger log = LoggerFactory.getLogger(RimborsoMissioneService.class);

	@Autowired
	private CRUDComponentSession crudServiceBean;

	@Autowired
	private UoService uoService;

    @Autowired
    private Environment env;

	@Autowired
	private AccountService accountService;

	@Autowired
	private OrdineMissioneService ordineMissioneService;

	@Autowired
	private RimborsoMissioneDettagliService rimborsoMissioneDettagliService;

	@Autowired
	private PrintRimborsoMissioneService printRimborsoMissioneService;

	@Autowired
	UnitaOrganizzativaService unitaOrganizzativaService;
	
	@Autowired
	CdrService cdrService;
	
	@Autowired
	TerzoService terzoService;
	
	@Autowired
	CronService cronService;
	
	@Autowired
	ImpegnoGaeService impegnoGaeService;
	
	@Autowired
	ImpegnoService impegnoService;
	
	@Autowired
	NazioneService nazioneService;
	
	@Autowired
	GaeService gaeService;
	
	@Autowired
	CMISRimborsoMissioneService cmisRimborsoMissioneService; 
	
	@Autowired
	ProgettoService progettoService;
	
    @Autowired
    private DatiIstitutoService datiIstitutoService;

    @Transactional(readOnly = true)
    public RimborsoMissione getRimborsoMissione(Principal principal, Long idMissione, Boolean retrieveDetail, Boolean retrieveDataFromFlows) throws ComponentException {
    	RimborsoMissioneFilter filter = new RimborsoMissioneFilter();
    	filter.setDaId(idMissione);
    	filter.setaId(idMissione);
    	RimborsoMissione rimborsoMissione = null;
		List<RimborsoMissione> listaRimborsiMissione = getRimborsiMissione(principal, filter, false, true);
		if (listaRimborsiMissione != null && !listaRimborsiMissione.isEmpty()){
			rimborsoMissione = listaRimborsiMissione.get(0);
			if (retrieveDataFromFlows){
				if (rimborsoMissione.isStatoInviatoAlFlusso()){
	    			ResultFlows result = retrieveDataFromFlows(rimborsoMissione);
	    			if (result != null){
	    				rimborsoMissione.setStateFlows(retrieveStateFromFlows(result));
	    				rimborsoMissione.setCommentFlows(result.getComment());
	    			}
				}
			}
			if (retrieveDetail){
				retrieveDetails(principal, rimborsoMissione);
			}
		}
		return rimborsoMissione;
    }

	public void retrieveDetails(Principal principal, RimborsoMissione rimborsoMissione) throws NumberFormatException, ComponentException {
		List<RimborsoMissioneDettagli> list = rimborsoMissioneDettagliService.getRimborsoMissioneDettagli(principal, new Long(rimborsoMissione.getId().toString()));
		rimborsoMissione.setRimborsoMissioneDettagli(list);
	}

	private boolean isDevProfile(){
   		if (env.acceptsProfiles(Costanti.SPRING_PROFILE_DEVELOPMENT)) {
   			return true;
   		}
   		return false;
	}

    public List<RimborsoMissione> getRimborsiMissioneForValidateFlows(Principal principal, RimborsoMissioneFilter filter,  Boolean isServiceRest) throws AwesomeException, ComponentException, Exception {
    	if (isDevProfile()){
        	cronService.verificaFlussoEComunicaDatiRimborsoSigla(principal);
    	}
    	List<RimborsoMissione> lista = getRimborsiMissione(principal, filter, isServiceRest, true);
    	if (lista != null){
    		List<RimborsoMissione> listaNew = new ArrayList<RimborsoMissione>();
    		for (RimborsoMissione rimborsoMissione : lista){
    			if (rimborsoMissione.isStatoInviatoAlFlusso() && !rimborsoMissione.isMissioneDaValidare()){
    				ResultFlows result = retrieveDataFromFlows(rimborsoMissione);
    				if (result != null){
    					RimborsoMissione rimborsoMissioneDaAggiornare = (RimborsoMissione)crudServiceBean.findById(principal, RimborsoMissione.class, rimborsoMissione.getId());
    					if (result.isApprovato()){
    						aggiornaRimborsoMissioneApprovato(principal, rimborsoMissioneDaAggiornare);
    						rimborsoMissione.setStatoFlussoRitornoHome(Costanti.STATO_APPROVATO_PER_HOME);
    						listaNew.add(rimborsoMissione);
    					} else if (result.isStateReject()){
    						rimborsoMissione.setCommentFlows(result.getComment());
    						rimborsoMissione.setStateFlows(retrieveStateFromFlows(result));
    						aggiornaRimborsoMissioneRespinto(principal, result, rimborsoMissioneDaAggiornare);
    						rimborsoMissione.setStatoFlussoRitornoHome(Costanti.STATO_RESPINTO_PER_HOME);
    						listaNew.add(rimborsoMissione);
    					} else if (result.isAnnullato()){
    						aggiornaRimborsoMissioneAnnullato(principal, rimborsoMissioneDaAggiornare);
    						rimborsoMissione.setStatoFlussoRitornoHome(Costanti.STATO_ANNULLATO_PER_HOME);
    						listaNew.add(rimborsoMissione);
    					} else {
    						rimborsoMissione.setStatoFlussoRitornoHome(Costanti.STATO_DA_AUTORIZZARE_PER_HOME);
    						listaNew.add(rimborsoMissione);
    					}
    				}
    			} else {
    				if (rimborsoMissione.isMissioneDaValidare() && rimborsoMissione.isMissioneConfermata()){
    					rimborsoMissione.setStatoFlussoRitornoHome(Costanti.STATO_DA_VALIDARE_PER_HOME);
    					listaNew.add(rimborsoMissione);
    				} else if (rimborsoMissione.isMissioneInserita()){
    					rimborsoMissione.setStatoFlussoRitornoHome(Costanti.STATO_DA_CONFERMARE_PER_HOME);
    					listaNew.add(rimborsoMissione);
    				}
    			}
    		}
    		return listaNew;
    	}
    	return lista;
    }

	public void aggiornaRimborsoMissioneRespinto(Principal principal, ResultFlows result,
			RimborsoMissione rimborsoMissioneDaAggiornare) throws Exception {
		aggiornaValidazione(rimborsoMissioneDaAggiornare);
		rimborsoMissioneDaAggiornare.setCommentFlows(result.getComment());
		rimborsoMissioneDaAggiornare.setStateFlows(retrieveStateFromFlows(result));
		rimborsoMissioneDaAggiornare.setStato(Costanti.STATO_INSERITO);
		updateRimborsoMissione(principal, rimborsoMissioneDaAggiornare, true);
	}

	public void aggiornaRimborsoMissioneAnnullato(Principal principal, RimborsoMissione rimborsoMissioneDaAggiornare)
			throws Exception {
		rimborsoMissioneDaAggiornare.setStatoFlusso(Costanti.STATO_ANNULLATO);
		updateRimborsoMissione(principal, rimborsoMissioneDaAggiornare, true);
	}

	public RimborsoMissione aggiornaRimborsoMissioneApprovato(Principal principal, RimborsoMissione rimborsoMissioneDaAggiornare)
			throws Exception {
		rimborsoMissioneDaAggiornare.setStatoInvioSigla(Costanti.STATO_INVIO_SIGLA_DA_COMUNICARE);
		rimborsoMissioneDaAggiornare.setStatoFlusso(Costanti.STATO_APPROVATO_FLUSSO);
		rimborsoMissioneDaAggiornare.setStato(Costanti.STATO_DEFINITIVO);
		return updateRimborsoMissione(principal, rimborsoMissioneDaAggiornare, true);
	}

	public RimborsoMissione aggiornaRimborsoMissioneComunicata(Principal principal, RimborsoMissione rimborsoMissioneDaAggiornare, MissioneBulk missioneBulk)
			throws Exception {
		rimborsoMissioneDaAggiornare.setEsercizioSigla(missioneBulk.getEsercizio());
		rimborsoMissioneDaAggiornare.setPgMissioneSigla(missioneBulk.getPgMissione());
		rimborsoMissioneDaAggiornare.setCdCdsSigla(missioneBulk.getCdCds());
		rimborsoMissioneDaAggiornare.setCdUoSigla(missioneBulk.getCdUnitaOrganizzativa());
		rimborsoMissioneDaAggiornare.setStatoInvioSigla(Costanti.STATO_INVIO_SIGLA_COMUNICATA);
		return updateRimborsoMissione(principal, rimborsoMissioneDaAggiornare, true);
	}

    @Transactional(propagation = Propagation.REQUIRED)
    public RimborsoMissione updateRimborsoMissione(Principal principal, RimborsoMissione rimborsoMissione)  throws AwesomeException, 
    ComponentException, Exception{
    	return updateRimborsoMissione(principal, rimborsoMissione, false);
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    private RimborsoMissione updateRimborsoMissione(Principal principal, RimborsoMissione rimborsoMissione, Boolean fromFlows)  throws AwesomeException, 
    ComponentException, Exception{
    	return updateRimborsoMissione(principal, rimborsoMissione, fromFlows, false);
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public RimborsoMissione updateRimborsoMissione (Principal principal, RimborsoMissione rimborsoMissione, Boolean fromFlows, Boolean confirm)  throws AwesomeException, 
    ComponentException, Exception {

    	RimborsoMissione rimborsoMissioneDB = (RimborsoMissione)crudServiceBean.findById(principal, RimborsoMissione.class, rimborsoMissione.getId());

		if (rimborsoMissioneDB==null){
			throw new AwesomeException(CodiciErrore.ERRGEN, "Rimborso Missione da aggiornare inesistente.");
		}
		
		if (rimborsoMissioneDB.isMissioneConfermata() && !fromFlows && !Utility.nvl(rimborsoMissione.getDaValidazione(), "N").equals("D")){
			if (rimborsoMissioneDB.isStatoFlussoApprovato()){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Non è possibile modificare il rimborso della missione. E' già stato approvato.");
			}
			if (!rimborsoMissioneDB.isMissioneDaValidare()){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Non è possibile modificare il rimborso della missione. E' già stato avviato il flusso di approvazione.");
			}
		}
		
		if (Utility.nvl(rimborsoMissione.getDaValidazione(), "N").equals("S")){
			if (!rimborsoMissioneDB.getStato().equals(Costanti.STATO_CONFERMATO)){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Rimborso missione non confermato.");
			}
			if (!rimborsoMissioneDB.isMissioneDaValidare()){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Rimborso missione già validato.");
			}
			if (!accountService.isUserSpecialEnableToValidateOrder(principal.getName(), rimborsoMissioneDB.getUoRich())){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Utente non abilitato a validare i rimborsi di missione.");
			}
			
			if (!confirm){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Operazione non possibile. Non è possibile modificare un rimborso di missione durante la fase di validazione. Rieseguire la ricerca.");
			}
			rimborsoMissioneDB.setValidato("S");
		} else if (Utility.nvl(rimborsoMissione.getDaValidazione(), "N").equals("D")){
			if (rimborsoMissione.getEsercizioOriginaleObbligazione() == null || rimborsoMissione.getPgObbligazione() == null ){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Per rendere definitivo il rimborso della missione è necessario valorizzare l'impegno.");
			}
			if (!StringUtils.isEmpty(rimborsoMissione.getGae())){
				rimborsoMissioneDB.setGae(rimborsoMissione.getGae());
			}
			if (!StringUtils.isEmpty(rimborsoMissione.getVoce())){
				rimborsoMissioneDB.setVoce(rimborsoMissione.getVoce());
			}
			rimborsoMissioneDB.setEsercizioOriginaleObbligazione(rimborsoMissione.getEsercizioOriginaleObbligazione());
			rimborsoMissioneDB.setPgObbligazione(rimborsoMissione.getPgObbligazione());
			rimborsoMissioneDB.setStato(Costanti.STATO_DEFINITIVO);
		} else if (Utility.nvl(rimborsoMissione.getDaValidazione(), "N").equals("R")){
			if (rimborsoMissioneDB.isStatoNonInviatoAlFlusso() || rimborsoMissioneDB.isMissioneDaValidare()) {
				rimborsoMissioneDB.setStato(Costanti.STATO_INSERITO);
			} else {
				throw new AwesomeException(CodiciErrore.ERRGEN, "Non è possibile sbloccare un rimborso missione se è stato già inviato al flusso.");
			}
		} else {
			rimborsoMissioneDB.setStato(rimborsoMissione.getStato());
			rimborsoMissioneDB.setStatoFlusso(rimborsoMissione.getStatoFlusso());
			rimborsoMissioneDB.setCdrSpesa(rimborsoMissione.getCdrSpesa());
			rimborsoMissioneDB.setCdsSpesa(rimborsoMissione.getCdsSpesa());
			rimborsoMissioneDB.setUoSpesa(rimborsoMissione.getUoSpesa());
			rimborsoMissioneDB.setCdsCompetenza(rimborsoMissione.getCdsCompetenza());
			rimborsoMissioneDB.setUoCompetenza(rimborsoMissione.getUoCompetenza());
			rimborsoMissioneDB.setDomicilioFiscaleRich(rimborsoMissione.getDomicilioFiscaleRich());
			rimborsoMissioneDB.setDataInizioMissione(rimborsoMissione.getDataInizioMissione());
			rimborsoMissioneDB.setDataFineMissione(rimborsoMissione.getDataFineMissione());
			rimborsoMissioneDB.setDestinazione(rimborsoMissione.getDestinazione());
			rimborsoMissioneDB.setGae(rimborsoMissione.getGae());
			rimborsoMissioneDB.setNote(rimborsoMissione.getNote());
			if (confirm){
	    		DatiIstituto istituto = datiIstitutoService.getDatiIstituto(rimborsoMissione.getCdsSpesa(), rimborsoMissione.getAnno());
	    		if (istituto.isAttivaGestioneResponsabileModulo()){
	    			if (StringUtils.isEmpty(rimborsoMissioneDB.getPgProgetto())){
	    				throw new AwesomeException(CodiciErrore.ERRGEN, "E' necessario indicare il Progetto.");
	    			}
    			}
				aggiornaValidazione(rimborsoMissioneDB);
			} else {
				rimborsoMissioneDB.setValidato(rimborsoMissione.getValidato());
			}
			rimborsoMissioneDB.setOggetto(rimborsoMissione.getOggetto());
			rimborsoMissioneDB.setTipoMissione(rimborsoMissione.getTipoMissione());
			rimborsoMissioneDB.setVoce(rimborsoMissione.getVoce());
			rimborsoMissioneDB.setTrattamento(rimborsoMissione.getTrattamento());
			rimborsoMissioneDB.setNazione(rimborsoMissione.getNazione());

			rimborsoMissioneDB.setNoteUtilizzoTaxiNoleggio(rimborsoMissione.getNoteUtilizzoTaxiNoleggio());
			rimborsoMissioneDB.setUtilizzoAutoNoleggio(rimborsoMissione.getUtilizzoAutoNoleggio());
			rimborsoMissioneDB.setUtilizzoTaxi(rimborsoMissione.getUtilizzoTaxi());
			rimborsoMissioneDB.setPgProgetto(rimborsoMissione.getPgProgetto());
			rimborsoMissioneDB.setPgProgetto(rimborsoMissione.getPgProgetto());
			rimborsoMissioneDB.setEsercizioOriginaleObbligazione(rimborsoMissione.getEsercizioOriginaleObbligazione());
			rimborsoMissioneDB.setPgObbligazione(rimborsoMissione.getPgObbligazione());

			rimborsoMissioneDB.setDataInizioEstero(rimborsoMissione.getDataInizioEstero());
			rimborsoMissioneDB.setDataFineEstero(rimborsoMissione.getDataFineEstero());
			rimborsoMissioneDB.setCdTerzoSigla(rimborsoMissione.getCdTerzoSigla());
			rimborsoMissioneDB.setModpag(rimborsoMissione.getModpag());
			rimborsoMissioneDB.setIban(rimborsoMissione.getIban());
			rimborsoMissioneDB.setPgBanca(rimborsoMissione.getPgBanca());
			rimborsoMissioneDB.setTipoPagamento(rimborsoMissione.getTipoPagamento());
			rimborsoMissioneDB.setAnticipoRicevuto(rimborsoMissione.getAnticipoRicevuto());
			rimborsoMissioneDB.setAnticipoAnnoMandato(rimborsoMissione.getAnticipoAnnoMandato());
			rimborsoMissioneDB.setAnticipoNumeroMandato(rimborsoMissione.getAnticipoNumeroMandato());
			rimborsoMissioneDB.setAnticipoImporto(rimborsoMissione.getAnticipoImporto());
			rimborsoMissioneDB.setAltreSpeseAntDescrizione(rimborsoMissione.getAltreSpeseAntDescrizione());
			rimborsoMissioneDB.setAltreSpeseAntImporto(rimborsoMissione.getAltreSpeseAntImporto());
			rimborsoMissioneDB.setSpeseTerziImporto(rimborsoMissione.getSpeseTerziImporto());
			rimborsoMissioneDB.setSpeseTerziRicevute(rimborsoMissione.getSpeseTerziRicevute());
//			rimborsoMissioneDB.setOrdineMissione(rimborsoMissione.getOrdineMissione());
			rimborsoMissioneDB.setInquadramento(rimborsoMissione.getInquadramento());
			rimborsoMissioneDB.setCdCdsSigla(rimborsoMissione.getCdCdsSigla());
			rimborsoMissioneDB.setCdUoSigla(rimborsoMissione.getCdUoSigla());
			rimborsoMissioneDB.setEsercizioSigla(rimborsoMissione.getEsercizioSigla());
			rimborsoMissioneDB.setPgMissioneSigla(rimborsoMissione.getPgMissioneSigla());
			rimborsoMissioneDB.setStatoInvioSigla(rimborsoMissione.getStatoInvioSigla());
			rimborsoMissioneDB.setCdTipoRapporto(rimborsoMissione.getCdTipoRapporto());
//			rimborsoMissioneDB.setNoteDifferenzeOrdine(rimborsoMissione.getNoteDifferenzeOrdine());
		}
		
		
    	if (confirm){
    		rimborsoMissioneDB.setStato(Costanti.STATO_CONFERMATO);
    	} 

    	rimborsoMissioneDB.setToBeUpdated();
    	retrieveDetails(principal, rimborsoMissioneDB);
		if (confirm){
			if (assenzaDettagli(rimborsoMissioneDB)){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Non è possibile confermare un rimborso missione senza aver indicato nessun dettaglio di spesa.");
			}
		}
//		//effettuo controlli di validazione operazione CRUD
    	if (!Utility.nvl(rimborsoMissione.getDaValidazione(), "N").equals("R") && !fromFlows){
    		validaCRUD(principal, rimborsoMissioneDB);
    	}

		controlloCongruenzaTestataDettagli(rimborsoMissioneDB);
    	if (confirm && !rimborsoMissioneDB.isMissioneDaValidare()){
    		cmisRimborsoMissioneService.avviaFlusso((Principal) SecurityUtils.getCurrentUser(), rimborsoMissioneDB);
    	}
    	rimborsoMissioneDB.setRimborsoMissioneDettagli(null);
    	rimborsoMissioneDB = (RimborsoMissione)crudServiceBean.modificaConBulk(principal, rimborsoMissioneDB);
    	
    	log.debug("Updated Information for Rimborso Missione: {}", rimborsoMissioneDB);

    	return rimborsoMissioneDB;
    }

	private void controlloCongruenzaTestataDettagli(RimborsoMissione rimborsoMissione) {
		if (rimborsoMissione.getRimborsoMissioneDettagli() != null && !rimborsoMissione.getRimborsoMissioneDettagli().isEmpty() ){
			for (RimborsoMissioneDettagli dettaglio : rimborsoMissione.getRimborsoMissioneDettagli()){
				if (dettaglio.getDataSpesa().isAfter(DateUtils.truncate(rimborsoMissione.getDataFineMissione()))){
					throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": La Data di Fine Missione non può essere precedente alla data di una spesa indicata nei dettagli.");
				}
				if (dettaglio.getDataSpesa().isBefore(DateUtils.truncate(rimborsoMissione.getDataInizioMissione()))){
					throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": La Data di Inizio Missione non può essere successiva alla data di una spesa indicata nei dettagli.");
				}
			}
		}
	}

	private Boolean assenzaDettagli(RimborsoMissione rimborsoMissione) throws ComponentException {
		if (rimborsoMissione.getRimborsoMissioneDettagli() == null || rimborsoMissione.getRimborsoMissioneDettagli().isEmpty() ){
			return true;
		} else {
			cmisRimborsoMissioneService.controlloEsitenzaGiustificativoDettaglio(rimborsoMissione);
		}
		return false;
	}

    @Transactional(propagation = Propagation.REQUIRED)
	public void deleteRimborsoMissione(Principal principal, Long idRimborsoMissione) throws AwesomeException, ComponentException, OptimisticLockException, PersistencyException, BusyResourceException {
    	RimborsoMissione rimborsoMissione = (RimborsoMissione)crudServiceBean.findById(principal, RimborsoMissione.class, idRimborsoMissione);
		if (rimborsoMissione != null){
			controlloOperazioniCRUDDaGui(rimborsoMissione);
			rimborsoMissioneDettagliService.cancellaRimborsoMissioneDettagli(principal, rimborsoMissione, false);
			rimborsoMissione.setStato(Costanti.STATO_ANNULLATO);
			rimborsoMissione.setToBeUpdated();
			if (rimborsoMissione.isStatoInviatoAlFlusso() && !StringUtils.isEmpty(rimborsoMissione.getIdFlusso())){
				cmisRimborsoMissioneService.annullaFlusso(rimborsoMissione);
			}
			crudServiceBean.modificaConBulk(principal, rimborsoMissione);
		}
	}

	public void controlloOperazioniCRUDDaGui(RimborsoMissione rimborsoMissione) {
		if (!rimborsoMissione.isMissioneInserita()){
			throw new AwesomeException(CodiciErrore.ERRGEN, "Non è possibile effettuare l'operazione su un Rimborso che non si trova in uno stato "+Costanti.STATO.get(Costanti.STATO_INSERITO));
		}
	}
	
	public String retrieveStateFromFlows(ResultFlows result) {
		return result.getState();
	}

	public ResultFlows retrieveDataFromFlows(RimborsoMissione rimborsoMissione)
			throws ComponentException {
		ResultFlows result = cmisRimborsoMissioneService.getFlowsRimborsoMissione(rimborsoMissione.getIdFlusso());
		return result;
	}

    @Transactional(readOnly = true)
    public RimborsoMissione getRimborsoMissione(Principal principal, Long idMissione, Boolean retrieveDetail) throws ComponentException {
		return getRimborsoMissione(principal, idMissione, retrieveDetail, false);
    }

    @Transactional(readOnly = true)
    public List<RimborsoMissione> getRimborsoMissione(Principal principal, RimborsoMissioneFilter filter, Boolean isServiceRest) throws ComponentException {
		return getRimborsiMissione(principal, filter, isServiceRest, false);
    }

    @Transactional(readOnly = true)
    public List<RimborsoMissione> getRimborsiMissione(Principal principal, RimborsoMissioneFilter filter, Boolean isServiceRest) throws ComponentException {
		return getRimborsiMissione(principal, filter, isServiceRest, false);
    }


    @Transactional(readOnly = true)
    public List<RimborsoMissione> getRimborsiMissione(Principal principal, RimborsoMissioneFilter filter, Boolean isServiceRest, Boolean isForValidateFlows) throws ComponentException {
		CriterionList criterionList = new CriterionList();
		List<RimborsoMissione> rimborsoMissioneList=null;
		String aliasRimborsoMissione = "Rimborso";
		if (filter != null){
			if (filter.getAnno() != null){
				criterionList.add(Restrictions.eq("anno", filter.getAnno()));
			}
			if (filter.getDaId() != null){
				criterionList.add(Restrictions.ge("id", filter.getDaId()));
			}
			if (filter.getStato() != null){
				criterionList.add(Restrictions.eq("stato", filter.getStato()));
			}
			if (filter.getStatoFlusso() != null){
				criterionList.add(Restrictions.eq("statoFlusso", filter.getStatoFlusso()));
			}
			if (filter.getValidato() != null){
				criterionList.add(Restrictions.eq("validato", filter.getValidato()));
			}
			if (filter.getListaStatiMissione() != null && !filter.getListaStatiMissione().isEmpty()){
				criterionList.add(Restrictions.in("stato", filter.getListaStatiMissione()));
			}
			
			if (filter.getaId() != null){
				criterionList.add(Restrictions.le("id", filter.getaId()));
			}
			if (filter.getDaNumero() != null){
				criterionList.add(Restrictions.ge("numero", filter.getDaNumero()));
			}
			if (filter.getaNumero() != null){
				criterionList.add(Restrictions.le("numero", filter.getaNumero()));
			}
			if (filter.getDaData() != null){
				criterionList.add(Restrictions.ge("dataInserimento", filter.getDaData()));
			}
			if (filter.getaData() != null){
				criterionList.add(Restrictions.le("dataInserimento", filter.getaData()));
			}
			if (filter.getCdsRich() != null){
				criterionList.add(Restrictions.eq("cdsRich", filter.getCdsRich()));
			}
			if (filter.getUoRich() != null){
				criterionList.add(Restrictions.eq("uoRich", filter.getUoRich()));
			}
			if (filter.getAnnoOrdine() != null){
				criterionList.add(Restrictions.eq(aliasRimborsoMissione+".anno", filter.getAnnoOrdine()));
			}
			if (filter.getDaNumeroOrdine() != null){
				criterionList.add(Restrictions.ge(aliasRimborsoMissione+".numero", filter.getDaNumeroOrdine()));
			}
			if (filter.getaNumeroOrdine() != null){
				criterionList.add(Restrictions.le(aliasRimborsoMissione+".numero", filter.getaNumeroOrdine()));
			}
			if (filter.getStatoInvioSigla() != null){
				criterionList.add(Restrictions.eq("statoInvioSigla", filter.getStatoInvioSigla()));
			}
		}
		if (filter != null && Utility.nvl(filter.getToFinal(), "N").equals("S")){
			if (StringUtils.isEmpty(filter.getUoRich())){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Non è stata selezionata la uo per rendere definitivi il rimborso della missione.");
			}
			UsersSpecial userSpecial = accountService.getUoForUsersSpecial(principal.getName());
			boolean uoAbilitata = false;
			if (userSpecial != null){
				if (userSpecial.getAll() == null || !userSpecial.getAll().equals("S")){
					if (userSpecial.getUoForUsersSpecials() != null && !userSpecial.getUoForUsersSpecials().isEmpty()){
				    	for (UoForUsersSpecial uoUser : userSpecial.getUoForUsersSpecials()){
				    		if (uoService.getUoSigla(uoUser).equals(filter.getUoRich())){
				    			uoAbilitata = true;
				    			if (!Utility.nvl(uoUser.getRendi_definitivo(),"N").equals("S")){
									throw new AwesomeException(CodiciErrore.ERRGEN, "L'utente non è abilitato a rendere definitivi rimborsi di missione.");
					    		}
				    		}
				    	} 
					}
				}
			}
			if (!uoAbilitata){
				throw new AwesomeException(CodiciErrore.ERRGEN, "L'utente non è abilitato a rendere definitivi rimborsi di missione.");
			}
			criterionList.add(Restrictions.eq("statoFlusso", Costanti.STATO_APPROVATO_FLUSSO));
			criterionList.add(Restrictions.eq("stato", Costanti.STATO_CONFERMATO));
			criterionList.add(Restrictions.eq("validato", "S"));
			rimborsoMissioneList = crudServiceBean.findByProjection(principal, RimborsoMissione.class, RimborsoMissione.getProjectionForElencoMissioni(), criterionList, true, Order.asc("dataInserimento"));
			return rimborsoMissioneList;
			
		} else {
			if (!isForValidateFlows){
				if (!StringUtils.isEmpty(filter.getUser())){
					criterionList.add(Restrictions.eq("uid", filter.getUser()));
				} else {
					criterionList.add(Restrictions.eq("uid", principal.getName()));
				}
			} else {
				UsersSpecial userSpecial = accountService.getUoForUsersSpecial(principal.getName());
				if (userSpecial != null){
					if (userSpecial.getAll() == null || !userSpecial.getAll().equals("S")){
						if (userSpecial.getUoForUsersSpecials() != null && !userSpecial.getUoForUsersSpecials().isEmpty()){
							boolean esisteUoConValidazioneConUserNonAbilitato = false;
							Disjunction condizioneOr = Restrictions.disjunction();
							List<String> listaUoUtente = new ArrayList<String>();
					    	for (UoForUsersSpecial uoUser : userSpecial.getUoForUsersSpecials()){
					    		Uo uo = uoService.recuperoUo(uoUser.getCodice_uo());
					    		if (uo != null){
						    		listaUoUtente.add(uoService.getUoSigla(uoUser));
						    		if (Utility.nvl(uo.getOrdineDaValidare(),"N").equals("S")){
						    			if (Utility.nvl(uoUser.getOrdine_da_validare(),"N").equals("S")){
							    			condizioneOr.add(Restrictions.eq("uoRich", uoService.getUoSigla(uoUser)));
							    		} else {
							    			esisteUoConValidazioneConUserNonAbilitato = true;
							    			condizioneOr.add(Restrictions.conjunction().add(Restrictions.eq("uoRich", uoService.getUoSigla(uoUser))).add(Restrictions.eq("validato", "S")));
						    			}
						    		}
					    		}
					    	}
					    	if (esisteUoConValidazioneConUserNonAbilitato){
						    	criterionList.add(condizioneOr);
					    	} else {
						    	criterionList.add(Restrictions.in("uoRich", listaUoUtente));
					    	}
						} else {
							criterionList.add(Restrictions.eq("uid", principal.getName()));
						}
					}
				} else {
					criterionList.add(Restrictions.eq("uid", principal.getName()));
				}
			}
			criterionList.add(Restrictions.not(Restrictions.eq("stato", Costanti.STATO_ANNULLATO)));

			if (isServiceRest) {
				if (isForValidateFlows){
					List<String> listaStatiFlusso = new ArrayList<String>();
					listaStatiFlusso.add(Costanti.STATO_INVIATO_FLUSSO);
					listaStatiFlusso.add(Costanti.STATO_NON_INVIATO_FLUSSO);
					criterionList.add(Restrictions.disjunction().add(Restrictions.disjunction().add(Restrictions.in("statoFlusso", listaStatiFlusso)).add(Restrictions.eq("stato", Costanti.STATO_INSERITO))));
				}
				rimborsoMissioneList = crudServiceBean.findByProjection(principal, RimborsoMissione.class, RimborsoMissione.getProjectionForElencoMissioni(), criterionList, true, Order.asc("dataInserimento"));
			} else
				rimborsoMissioneList = crudServiceBean.findByCriterion(principal, RimborsoMissione.class, criterionList, Order.asc("dataInserimento"));
			return rimborsoMissioneList;
		}
    }

    
    @Transactional(propagation = Propagation.REQUIRED)
    public RimborsoMissione createRimborsoMissione(Principal principal, RimborsoMissione rimborsoMissione)  throws Exception {
    	controlloDatiObbligatoriDaGUI(rimborsoMissione);
    	inizializzaCampiPerInserimento(principal, rimborsoMissione);
		validaCRUD(principal, rimborsoMissione);
		rimborsoMissione = (RimborsoMissione)crudServiceBean.creaConBulk(principal, rimborsoMissione);
    	log.info("Creato Rimborso Missione", rimborsoMissione.getId());
    	return rimborsoMissione;
    }

    @Transactional(readOnly = true)
    private void inizializzaCampiPerInserimento(Principal principal,
    		RimborsoMissione rimborsoMissione) throws ComponentException,
    		PersistencyException, BusyResourceException {
    	rimborsoMissione.setUidInsert(principal.getName());
    	rimborsoMissione.setUser(principal.getName());
    	Integer anno = recuperoAnno(rimborsoMissione);
    	rimborsoMissione.setAnno(anno);
    	rimborsoMissione.setNumero(datiIstitutoService.getNextPG(principal, rimborsoMissione.getCdsRich(), anno , Costanti.TIPO_RIMBORSO_MISSIONE));
    	if (StringUtils.isEmpty(rimborsoMissione.getTrattamento())){
    		rimborsoMissione.setTrattamento("R");
    	}
    	if (StringUtils.isEmpty(rimborsoMissione.getUtilizzoAutoNoleggio())){
    		rimborsoMissione.setUtilizzoAutoNoleggio("N");
    	}
    	if (StringUtils.isEmpty(rimborsoMissione.getUtilizzoTaxi())){
    		rimborsoMissione.setUtilizzoTaxi("N");
    	}
    	if (StringUtils.isEmpty(rimborsoMissione.getAnticipoRicevuto())){
    		rimborsoMissione.setAnticipoRicevuto("N");
    	}
    	
    	if (StringUtils.isEmpty(rimborsoMissione.getSpeseTerziRicevute())){
    		rimborsoMissione.setSpeseTerziRicevute("N");
    	}
    	
    	aggiornaValidazione(rimborsoMissione);
    	
    	rimborsoMissione.setStato(Costanti.STATO_INSERITO);
    	rimborsoMissione.setStatoFlusso(Costanti.STATO_INSERITO);
    	if (rimborsoMissione.getOrdineMissione() != null){
        	OrdineMissione ordineMissione = (OrdineMissione)crudServiceBean.findById(principal, OrdineMissione.class, rimborsoMissione.getOrdineMissione().getId());
        	if (ordineMissione != null){
        		rimborsoMissione.setOrdineMissione(ordineMissione);
        	} else {
				throw new AwesomeException(CodiciErrore.ERRGEN, "L'ordine di missione con ID: "+rimborsoMissione.getOrdineMissione().getId()+" non esiste");
        	}
    	}
    	rimborsoMissione.setToBeCreated();
    }

    private OrdineMissioneAutoPropria getAutoPropriaOrdineMissione(Principal principal, RimborsoMissione rimborsoMissione){
    	if (rimborsoMissione.getOrdineMissione() != null){
        	OrdineMissione ordineMissione = (OrdineMissione)crudServiceBean.findById(principal, OrdineMissione.class, rimborsoMissione.getOrdineMissione().getId());
        	if (ordineMissione != null){
        		return ordineMissioneService.getAutoPropria(ordineMissione);
        	}
    	}
    	return null;
    }
    
    private Integer recuperoAnno(RimborsoMissione rimborsoMissione) {
		if (rimborsoMissione.getDataInserimento() == null){
			rimborsoMissione.setDataInserimento(LocalDate.now());
		}
		Integer anno = 	rimborsoMissione.getDataInserimento().getYear();
		return anno;
	}

    private void controlloDatiObbligatoriDaGUI(RimborsoMissione rimborsoMissione){
		if (rimborsoMissione != null){
			if (!StringUtils.isEmpty(rimborsoMissione.getMatricola())){
				if (StringUtils.isEmpty(rimborsoMissione.getInquadramento()))
					throw new AwesomeException(CodiciErrore.ERRGEN, "Non è stato possibile recuperare il valore dell'Inquadramento SIGLA. Verificare i dati");
			} else {
				
			}
			if (StringUtils.isEmpty(rimborsoMissione.getCdsRich())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Cds Richiedente");
			} else if (StringUtils.isEmpty(rimborsoMissione.getCdsSpesa())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Cds Spesa");
			} else if (StringUtils.isEmpty(rimborsoMissione.getUoRich())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Uo Richiedente");
			} else if (StringUtils.isEmpty(rimborsoMissione.getUoSpesa())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Uo Spesa");
			} else if (StringUtils.isEmpty(rimborsoMissione.getDataInizioMissione())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Data Inizio Missione");
			} else if (StringUtils.isEmpty(rimborsoMissione.getDataFineMissione())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Data Fine Missione");
			} else if (StringUtils.isEmpty(rimborsoMissione.getDatoreLavoroRich())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Datore di Lavoro Richiedente");
			} else if (StringUtils.isEmpty(rimborsoMissione.getDestinazione())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Destinazione");
			} else if (StringUtils.isEmpty(rimborsoMissione.getOggetto())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Oggetto");
			} else if (StringUtils.isEmpty(rimborsoMissione.getTipoMissione())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Tipo Missione");
			} else if (StringUtils.isEmpty(rimborsoMissione.getModpag())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Modalità di Pagamento");
			} else if (StringUtils.isEmpty(rimborsoMissione.getAnticipoRicevuto())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Anticipo Ricevuto");
			} else if (StringUtils.isEmpty(rimborsoMissione.getSpeseTerziRicevute())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Altri anticipi ricevuti");
			} 
			if (StringUtils.isEmpty(rimborsoMissione.getInquadramento())){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Per la data della missione indicata non è stato possibile recuperare l'inquadramento.");
			} 
			if (rimborsoMissione.isMissioneEstera()){
				if (StringUtils.isEmpty(rimborsoMissione.getNazione())){
					throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Nazione");
				} 
				if (StringUtils.isEmpty(rimborsoMissione.getTrattamento())){
					throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Trattamento");
				} 
				if (rimborsoMissione.getDataInizioEstero() == null){
					throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Data Inizio Attraversamento Frontiera");
				}
				if (rimborsoMissione.getDataFineEstero() == null){
					throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Data Fine Attraversamento Frontiera");
				}
			} else {
				if (rimborsoMissione.getDataInizioEstero() != null){
					throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": La Data Inizio Attraversamento Frontiera può essere valorizzata solo nel caso di missione estera");
				}
				if (rimborsoMissione.getDataFineEstero() != null){
					throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": La Data Fine Attraversamento Frontiera può essere valorizzata solo nel caso di missione estera");
				}
			}
			if (rimborsoMissione.isMissioneDipendente()){
				if (StringUtils.isEmpty(rimborsoMissione.getComuneResidenzaRich())){
					throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Comune di Residenza del Richiedente");
				} else if (StringUtils.isEmpty(rimborsoMissione.getIndirizzoResidenzaRich())){
					throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Indirizzo di Residenza del Richiedente");
				}
			}
		}
    }

	private void aggiornaValidazione(RimborsoMissione rimborsoMissione) {
		Uo uo = uoService.recuperoUoSigla(rimborsoMissione.getUoRich());
		if (Utility.nvl(uo.getOrdineDaValidare(),"N").equals("N")){
			rimborsoMissione.setValidato("S");
		} else {
			rimborsoMissione.setValidato("N");
		}
	}

    @Transactional(propagation = Propagation.REQUIRED)
	private void validaCRUD(Principal principal, RimborsoMissione rimborsoMissione) throws AwesomeException {
		if (rimborsoMissione != null){
			controlloCampiObbligatori(rimborsoMissione); 
			controlloCongruenzaDatiInseriti(principal, rimborsoMissione);
			controlloDatiFinanziari(principal, rimborsoMissione);
		}
	}

    @Transactional(propagation = Propagation.REQUIRED)
	private void controlloDatiFinanziari(Principal principal, RimborsoMissione rimborsoMissione) throws AwesomeException {
    	UnitaOrganizzativa uo = unitaOrganizzativaService.loadUo(rimborsoMissione.getUoSpesa(), rimborsoMissione.getCdsSpesa(), rimborsoMissione.getAnno());
    	if (uo == null){
    		throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": La UO "+ rimborsoMissione.getUoSpesa() + " non è corretta rispetto al CDS "+rimborsoMissione.getCdsSpesa());
    	}
		if (!StringUtils.isEmpty(rimborsoMissione.getCdrSpesa())){
			Cdr cdr = cdrService.loadCdr(rimborsoMissione.getCdrSpesa(), rimborsoMissione.getUoSpesa());
			if (cdr == null){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": Il CDR "+ rimborsoMissione.getCdrSpesa() + " non è corretto rispetto alla UO "+rimborsoMissione.getUoSpesa());
			}
		}
		if (!StringUtils.isEmpty(rimborsoMissione.getPgProgetto())){
			Progetto progetto = progettoService.loadModulo(rimborsoMissione.getPgProgetto(), rimborsoMissione.getAnno(), rimborsoMissione.getUoSpesa());
			if (progetto == null){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": Il modulo indicato non è corretto rispetto alla UO "+rimborsoMissione.getUoSpesa());
			}
		}
		if (!StringUtils.isEmpty(rimborsoMissione.getGae())){
			if (StringUtils.isEmpty(rimborsoMissione.getCdrSpesa())){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Non è possibile indicare la GAE senza il centro di responsabilità");
			}
			Gae gae = gaeService.loadGae(rimborsoMissione);
			if (gae == null){
				throw new AwesomeException(CodiciErrore.ERRGEN, "La GAE "+ rimborsoMissione.getGae()+" indicata non esiste");
			} else {
				boolean progettoCdrIndicato = false;
				if (!StringUtils.isEmpty(rimborsoMissione.getPgProgetto()) && !StringUtils.isEmpty(gae.getPg_progetto())){
					progettoCdrIndicato = true;
					if (gae.getPg_progetto().compareTo(rimborsoMissione.getPgProgetto()) != 0){
						throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": La GAE indicata "+ rimborsoMissione.getGae()+" non corrisponde al modulo indicato.");
					}
				}
				if (!StringUtils.isEmpty(rimborsoMissione.getCdrSpesa())){
					progettoCdrIndicato = true;
					if (!gae.getCd_centro_responsabilita().equals(rimborsoMissione.getCdrSpesa()) ){
						throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": La GAE indicata "+ rimborsoMissione.getGae()+" non corrisponde con il CDR "+rimborsoMissione.getCdrSpesa() +" indicato.");
					}
				}
				if (!progettoCdrIndicato){
					throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": Non è possibile indicare solo La GAE senza il modulo o il CDR.");
				}
			}
		}

		if (!StringUtils.isEmpty(rimborsoMissione.getPgObbligazione())){
			if (StringUtils.isEmpty(rimborsoMissione.getEsercizioOriginaleObbligazione())){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Oltre al numero dell'impegno è necessario indicare anche l'anno dell'impegno");
			}
			if (!StringUtils.isEmpty(rimborsoMissione.getGae())){
				ImpegnoGae impegnoGae = impegnoGaeService.loadImpegno(rimborsoMissione);
				if (impegnoGae == null){
					throw new AwesomeException(CodiciErrore.ERRGEN, "L'impegno indicato "+ rimborsoMissione.getEsercizioOriginaleObbligazione() + "-" + rimborsoMissione.getPgObbligazione() +" non corrisponde con la GAE "+ rimborsoMissione.getGae()+" indicata oppure non esiste");
				} else {
					if (!StringUtils.isEmpty(rimborsoMissione.getVoce())){
						if (!impegnoGae.getCdElementoVoce().equals(rimborsoMissione.getVoce())){
							throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": L'impegno indicato "+rimborsoMissione.getEsercizioOriginaleObbligazione() + "-" + rimborsoMissione.getPgObbligazione() +" non corrisponde con la voce di Bilancio indicata."+rimborsoMissione.getVoce());
						}
					} else {
						rimborsoMissione.setVoce(impegnoGae.getCdElementoVoce());
					}
					rimborsoMissione.setCdCdsObbligazione(impegnoGae.getCdCds());
					rimborsoMissione.setEsercizioObbligazione(impegnoGae.getEsercizio());
				}
			} else {
				Impegno impegno = impegnoService.loadImpegno(rimborsoMissione);
				if (impegno == null){
					throw new AwesomeException(CodiciErrore.ERRGEN, "L'impegno indicato "+ rimborsoMissione.getEsercizioOriginaleObbligazione() + "-" + rimborsoMissione.getPgObbligazione() +" non esiste");
				} else {
					if (!StringUtils.isEmpty(rimborsoMissione.getVoce())){
						if (!impegno.getCdElementoVoce().equals(rimborsoMissione.getVoce())){
							throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": L'impegno indicato "+rimborsoMissione.getEsercizioOriginaleObbligazione() + "-" + rimborsoMissione.getPgObbligazione() +" non corrisponde con la voce di Bilancio indicata."+rimborsoMissione.getVoce());
						}
					} else {
						rimborsoMissione.setVoce(impegno.getCdElementoVoce());
					}
					rimborsoMissione.setCdCdsObbligazione(impegno.getCdCds());
					rimborsoMissione.setEsercizioObbligazione(impegno.getEsercizio());
				}
			}
		} else {
//			if (!StringUtils.isEmpty(rimborsoMissione.getEsercizioOriginaleObbligazione())){
//				throw new AwesomeException(CodiciErrore.ERRGEN, "Oltre all'anno dell'impegno è necessario indicare anche il numero dell'impegno");
//			}
			rimborsoMissione.setCdCdsObbligazione(null);
			rimborsoMissione.setEsercizioObbligazione(null);
		}
    
    }
	
    private void controlloCongruenzaDatiInseriti(Principal principal, RimborsoMissione rimborsoMissione) throws AwesomeException {
		if (rimborsoMissione.getDataFineMissione().isBefore(rimborsoMissione.getDataInizioMissione())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.ERR_DATE_INCONGRUENTI+": La data di fine missione non può essere precedente alla data di inizio missione");
		}
		if (!StringUtils.isEmpty(rimborsoMissione.getNoteUtilizzoTaxiNoleggio())){
			if (rimborsoMissione.getUtilizzoTaxi().equals("N") && rimborsoMissione.getUtilizzoAutoNoleggio().equals("N")){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.ERR_DATE_INCONGRUENTI+": Non è possibile indicare le note all'utilizzo del taxi o dell'auto a noleggio se non si è scelto il loro utilizzo");
			}
		}
        if (rimborsoMissione.getUtilizzoAutoNoleggio() != null && rimborsoMissione.getUtilizzoAutoNoleggio().equals("S") && 
                getAutoPropriaOrdineMissione(principal, rimborsoMissione) != null ){
                throw new AwesomeException(CodiciErrore.ERRGEN, "L'ordine di missione prevede l'utilizo dell'auto propria. Non è possibile indicare l'utilizzo dell'auto a noleggio.");
        } 
		if ((Utility.nvl(rimborsoMissione.getUtilizzoAutoNoleggio()).equals("S") || Utility.nvl(rimborsoMissione.getUtilizzoTaxi()).equals("S")) && StringUtils.isEmpty(rimborsoMissione.getNoteUtilizzoTaxiNoleggio())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": E' obbligatorio indicare le note all'utilizzo del taxi o dell'auto a noleggio se si è scelto il loro utilizzo");
		}
		if (rimborsoMissione.isMissioneEstera()) {
			if (rimborsoMissione.getDataInizioEstero().isBefore(rimborsoMissione.getDataInizioMissione())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.ERR_DATE_INCONGRUENTI+": La data di inizio attraversamento frontiera non può essere precedente alla data di inizio missione");
			}
			if (rimborsoMissione.getDataFineMissione().isBefore(rimborsoMissione.getDataFineEstero())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.ERR_DATE_INCONGRUENTI+": La data di fine attraversamento frontiera non può essere successiva alla data di fine missione");
			}
			if (rimborsoMissione.getDataFineEstero().isBefore(rimborsoMissione.getDataInizioEstero())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.ERR_DATE_INCONGRUENTI+": La data di fine attraversamento frontiera non può essere precedente alla data di inizio attraversamento frontiera");
			}
		}
		if (StringUtils.isEmpty(rimborsoMissione.getIdFlusso()) &&  rimborsoMissione.isStatoInviatoAlFlusso()){
			throw new AwesomeException(CodiciErrore.ERRGEN, "Non è possibile avere lo stato Inviato al flusso e non avere l'ID del flusso");
		} 
		if (!rimborsoMissione.isMissioneEstera()){
			rimborsoMissione.setNazione(new Long("1"));
		}
	}
	
	private void controlloCampiObbligatori(RimborsoMissione rimborsoMissione) {
		if (!rimborsoMissione.isToBeCreated()){
			controlloDatiObbligatoriDaGUI(rimborsoMissione);
		}
		if (rimborsoMissione.getCdTerzoSigla() == null){
			throw new AwesomeException(CodiciErrore.ERRGEN, "Non è stato recuperato il codice terzo SIGLA, verificare il rimborso.");
		}
		if (StringUtils.isEmpty(rimborsoMissione.getAnno())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Anno");
		} else if (StringUtils.isEmpty(rimborsoMissione.getUid())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Utente");
		} else if (StringUtils.isEmpty(rimborsoMissione.getUtilizzoTaxi())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Utilizzo del Taxi");
		} else if (StringUtils.isEmpty(rimborsoMissione.getUtilizzoAutoNoleggio())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Utilizzo auto a noleggio");
		} else if (StringUtils.isEmpty(rimborsoMissione.getStato())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Stato");
		} else if (StringUtils.isEmpty(rimborsoMissione.getValidato())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Validato");
		} else if (StringUtils.isEmpty(rimborsoMissione.getNumero())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Numero");
		}
	}

	@Transactional(readOnly = true)
   	public Map<String, byte[]> printRimborsoMissione(Authentication auth, Long idMissione) throws AwesomeException, ComponentException {
    	Principal principal = (Principal)auth;
    	RimborsoMissione rimborsoMissione = getRimborsoMissione(principal, idMissione, true);
    	byte[] printRimborsoMissione = null;
    	String fileName = null;
    	if (!rimborsoMissione.isStatoNonInviatoAlFlusso()){
    		ContentStream content = null;
			try {
				content = cmisRimborsoMissioneService.getContentStreamRimborsoMissione(rimborsoMissione);
			} catch (Exception e1) {
				throw new ComponentException("Errore nel recupero del contenuto del file sul documentale (" + Utility.getMessageException(e1) + ")",e1);
			}
    		if (content != null){
        		fileName = content.getFileName();
        		InputStream is = null;
    			try {
    				is = content.getStream();
    			} catch (Exception e) {
    				throw new ComponentException("Errore nel recupero dello stream del file sul documentale (" + Utility.getMessageException(e) + ")",e);
    			}
        		if (is != null){
            		try {
    					printRimborsoMissione = IOUtils.toByteArray(is);
    				} catch (IOException e) {
    					throw new ComponentException("Errore nella conversione dello stream in byte del file (" + Utility.getMessageException(e) + ")",e);
    				}
        		}
    		} else {
				throw new AwesomeException(CodiciErrore.ERRGEN, "Errore nel recupero del contenuto del file sul documentale");
    		}
    		Map<String, byte[]> map = new HashMap<String, byte[]>();
    		map.put(fileName, printRimborsoMissione);
    		return map;
    	} else {
    		return stampaRimborso(principal, rimborsoMissione);
    	}
    }

	public Map<String, byte[]> stampaRimborso(Principal principal, RimborsoMissione rimborsoMissione)
			throws ComponentException {
		byte[] printRimborsoMissione;
		String fileName;
		retrieveDetails(principal, rimborsoMissione);
		if (assenzaDettagli(rimborsoMissione)){
			throw new AwesomeException(CodiciErrore.ERRGEN, "Non è possibile stampare un rimborso missione senza aver indicato nessun dettaglio di spesa.");
		}
		fileName = "RimborsoMissione"+rimborsoMissione.getId()+".pdf";
		printRimborsoMissione = printRimborsoMissioneService.printRimborsoMissione(rimborsoMissione, principal.getName());
		if (rimborsoMissione.isMissioneInserita()){
			cmisRimborsoMissioneService.salvaStampaRimborsoMissioneSuCMIS(principal, printRimborsoMissione, rimborsoMissione);
		}
		Map<String, byte[]> map = new HashMap<String, byte[]>();
		map.put(fileName, printRimborsoMissione);
		return map;
	}
}
