package it.cnr.si.missioni.service;

import it.cnr.jada.criterion.CriterionList;
import it.cnr.jada.ejb.session.BusyResourceException;
import it.cnr.jada.ejb.session.ComponentException;
import it.cnr.jada.ejb.session.PersistencyException;
import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.cmis.CMISOrdineMissioneService;
import it.cnr.si.missioni.cmis.ResultFlows;
import it.cnr.si.missioni.domain.custom.persistence.DatiIstituto;
import it.cnr.si.missioni.domain.custom.persistence.OrdineMissione;
import it.cnr.si.missioni.repository.CRUDComponentSession;
import it.cnr.si.missioni.util.CodiciErrore;
import it.cnr.si.missioni.util.Costanti;
import it.cnr.si.missioni.util.Utility;
import it.cnr.si.missioni.util.data.UoForUsersSpecial;
import it.cnr.si.missioni.util.data.UsersSpecial;
import it.cnr.si.missioni.util.proxy.json.object.Cdr;
import it.cnr.si.missioni.util.proxy.json.object.Gae;
import it.cnr.si.missioni.util.proxy.json.object.Impegno;
import it.cnr.si.missioni.util.proxy.json.object.ImpegnoGae;
import it.cnr.si.missioni.util.proxy.json.object.Progetto;
import it.cnr.si.missioni.util.proxy.json.object.UnitaOrganizzativa;
import it.cnr.si.missioni.util.proxy.json.service.AccountService;
import it.cnr.si.missioni.util.proxy.json.service.CdrService;
import it.cnr.si.missioni.util.proxy.json.service.GaeService;
import it.cnr.si.missioni.util.proxy.json.service.ImpegnoGaeService;
import it.cnr.si.missioni.util.proxy.json.service.ImpegnoService;
import it.cnr.si.missioni.util.proxy.json.service.ProgettoService;
import it.cnr.si.missioni.util.proxy.json.service.UnitaOrganizzativaService;
import it.cnr.si.missioni.web.filter.OrdineMissioneFilter;
import it.cnr.si.missioni.util.SecurityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.OptimisticLockException;

import net.bzdyl.ejb3.criteria.Order;
import net.bzdyl.ejb3.criteria.restrictions.Restrictions;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


/**
 * Service class for managing users.
 */
@Service
public class OrdineMissioneService {

    private final Logger log = LoggerFactory.getLogger(OrdineMissioneService.class);

    @Inject
    private DatiIstitutoService datiIstitutoService;

    @Autowired
    private PrintOrdineMissioneService printOrdineMissioneService;

    @Autowired
    private CMISOrdineMissioneService cmisOrdineMissioneService;

    @Inject
    private UnitaOrganizzativaService unitaOrganizzativaService;

    @Inject
    private CdrService cdrService;
    
    @Inject
    private GaeService gaeService;
    
    @Inject
    private ProgettoService progettoService;
    
    @Inject
    private ImpegnoService impegnoService;
    
    @Inject
    private ImpegnoGaeService impegnoGaeService;

    @Inject
    private OrdineMissioneAnticipoService ordineMissioneAnticipoService;

    @Inject
    private OrdineMissioneAutoPropriaService ordineMissioneAutoPropriaService;

    @Autowired
    private ConfigService configService;

	@Inject
	private CRUDComponentSession crudServiceBean;

	@Autowired
	private AccountService accountService;
	
    @Transactional(readOnly = true)
    public OrdineMissione getOrdineMissione(Principal principal, Long idMissione, Boolean retrieveDataFromFlows) throws ComponentException {
    	OrdineMissioneFilter filter = new OrdineMissioneFilter();
    	filter.setDaId(idMissione);
    	filter.setaId(idMissione);
    	OrdineMissione ordineMissione = null;
		List<OrdineMissione> listaOrdiniMissione = getOrdiniMissione(principal, filter, false, true);
		if (listaOrdiniMissione != null && !listaOrdiniMissione.isEmpty()){
			ordineMissione = listaOrdiniMissione.get(0);
			if (retrieveDataFromFlows){
				if (ordineMissione.isStatoInviatoAlFlusso()){
	    			ResultFlows result = retrieveDataFromFlows(ordineMissione);
	    			ordineMissione.setStateFlows(retrieveStateFromFlows(result));
	    			ordineMissione.setCommentFlows(result.getComment());
				}
			}
			
		}
		return ordineMissione;
    }

    @Transactional(readOnly = true)
    public OrdineMissione getOrdineMissione(Principal principal, Long idMissione) throws ComponentException {
		return getOrdineMissione(principal, idMissione, false);
    }

    @Transactional(propagation = Propagation.REQUIRED)
	private void caricaDatiDerivati(Principal principal, OrdineMissione ordineMissione) throws ComponentException {
		if (ordineMissione != null){
			DatiIstituto dati = datiIstitutoService.getDatiIstituto(ordineMissione.getCdsSpesa(), ordineMissione.getAnno());
			if (dati == null){
				dati = datiIstitutoService.creaDatiIstituto(principal, ordineMissione.getCdsSpesa(), ordineMissione.getAnno());
			}
			ordineMissione.setDatiIstituto(dati);
			if (ordineMissione.getDatiIstituto() == null){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Errore. Non esistono i dati per istituto per il codice "+ordineMissione.getCdsSpesa()+" nell'anno "+ordineMissione.getAnno());
			}
		}
	}

    @Transactional(readOnly = true)
   	public byte[] printOrdineMissione(Authentication auth, Long idMissione) throws AwesomeException, ComponentException {
    	String username = SecurityUtils.getCurrentUserLogin();
    	Principal principal = (Principal)auth;
    	OrdineMissione ordineMissione = getOrdineMissione(principal, idMissione);
    	byte[] printOrdineMissione = null;
    	if (ordineMissione.isStatoInviatoAlFlusso()){
    		InputStream is = null;
			try {
				is = cmisOrdineMissioneService.getStreamOrdineMissione(ordineMissione);
			} catch (Exception e) {
				throw new AwesomeException(CodiciErrore.ERRGEN, "Errore nel recupero dello stream del file sul documentale (" + Utility.getMessageException(e) + ")");
			}
    		if (is != null){
        		try {
					printOrdineMissione = IOUtils.toByteArray(is);
				} catch (IOException e) {
					throw new AwesomeException(CodiciErrore.ERRGEN, "Errore nella conversione dello stream in byte del file (" + Utility.getMessageException(e) + ")");
				}
    		}
    	} else {
    		printOrdineMissione = printOrdineMissioneService.printOrdineMissione(ordineMissione, username);
    		if (ordineMissione.isMissioneInserita()){
    			cmisOrdineMissioneService.salvaStampaOrdineMissioneSuCMIS(principal, printOrdineMissione, ordineMissione);
    		}
    	}
		return printOrdineMissione;
    }
    
    @Transactional(readOnly = true)
	public String jsonForPrintOrdineMissione(Principal principal, Long idMissione) throws AwesomeException, ComponentException {
    	OrdineMissione ordineMissione = getOrdineMissione(principal, idMissione);
    	return printOrdineMissioneService.createJsonPrintOrdineMissione(ordineMissione, principal.getName());
    }

    public List<OrdineMissione> getOrdiniMissioneForValidateFlows(Principal principal, OrdineMissioneFilter filter,  Boolean isServiceRest) throws AwesomeException, ComponentException, Exception {
    	List<OrdineMissione> lista = getOrdiniMissione(principal, filter, isServiceRest, true);
    	if (lista != null){
        	List<OrdineMissione> listaNew = new ArrayList<OrdineMissione>();
    		for (OrdineMissione ordineMissione : lista){
    			if (ordineMissione.isStatoInviatoAlFlusso()){
        			ResultFlows result = retrieveDataFromFlows(ordineMissione);
        			if (result != null){
    			    	OrdineMissione ordineMissioneDaAggiornare = (OrdineMissione)crudServiceBean.findById(principal, OrdineMissione.class, ordineMissione.getId());
        				if (result.isApprovato()){
        					ordineMissioneDaAggiornare.setStatoFlusso(Costanti.STATO_APPROVATO_FLUSSO);
        					updateOrdineMissione(principal, ordineMissioneDaAggiornare, true);
        					ordineMissione.setStatoFlussoRitornoHome(Costanti.STATO_APPROVATO_PER_HOME);
        					listaNew.add(ordineMissione);
        				} else if (result.isStateReject()){
        					ordineMissione.setCommentFlows(result.getComment());
        					ordineMissione.setStateFlows(retrieveStateFromFlows(result));

        					ordineMissioneDaAggiornare.setCommentFlows(result.getComment());
        					ordineMissioneDaAggiornare.setStateFlows(retrieveStateFromFlows(result));
        					ordineMissioneDaAggiornare.setStato(Costanti.STATO_INSERITO);
        					updateOrdineMissione(principal, ordineMissioneDaAggiornare, true);
        					ordineMissione.setStatoFlussoRitornoHome(Costanti.STATO_RESPINTO_PER_HOME);
        					listaNew.add(ordineMissione);
        				} else if (result.isAnnullato()){
        					ordineMissioneDaAggiornare.setStatoFlusso(Costanti.STATO_ANNULLATO);
        					updateOrdineMissione(principal, ordineMissioneDaAggiornare, true);
        					ordineMissione.setStatoFlussoRitornoHome(Costanti.STATO_ANNULLATO_PER_HOME);
        					listaNew.add(ordineMissione);
        				} else {
        					ordineMissione.setStatoFlussoRitornoHome(Costanti.STATO_DA_AUTORIZZARE_PER_HOME);
        					listaNew.add(ordineMissione);
        				}
        			}
    			} else {
    				if (ordineMissione.isMissioneInserita()){
    					ordineMissione.setStatoFlussoRitornoHome(Costanti.STATO_DA_CONFERMARE_PER_HOME);
    					listaNew.add(ordineMissione);
    				}
    			}
    		}
    		return listaNew;
    	}
    	return lista;
    }

	public String retrieveStateFromFlows(ResultFlows result) {
		return Costanti.STATO_FLUSSO_FROM_CMIS.get(result.getState());
	}

	public ResultFlows retrieveDataFromFlows(OrdineMissione ordineMissione)
			throws ComponentException {
		ResultFlows result = cmisOrdineMissioneService.getFlowsOrdineMissione(ordineMissione.getIdFlusso());
		return result;
	}

    @Transactional(readOnly = true)
    public void uploadAllegatoOrdineMissione(Principal principal, Long idMissione, InputStream uploadedAllegatoInputStream) throws AwesomeException, ComponentException {
    	OrdineMissione ordineMissione = getOrdineMissione(principal, idMissione);
    	cmisOrdineMissioneService.uploadAllegatoOrdineMissione(principal, ordineMissione, uploadedAllegatoInputStream);
    }

    @Transactional(readOnly = true)
    public List<OrdineMissione> getOrdiniMissione(Principal principal, OrdineMissioneFilter filter, Boolean isServiceRest) throws ComponentException {
		return getOrdiniMissione(principal, filter, isServiceRest, false);
    }

    @Transactional(readOnly = true)
    public List<OrdineMissione> getOrdiniMissione(Principal principal, OrdineMissioneFilter filter, Boolean isServiceRest, Boolean isForValidateFlows) throws ComponentException {
		CriterionList criterionList = new CriterionList();
		if (filter != null){
			if (filter.getAnno() != null){
				criterionList.add(Restrictions.eq("anno", filter.getAnno()));
			}
			if (filter.getDaId() != null){
				criterionList.add(Restrictions.ge("id", filter.getDaId()));
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
		}
		Boolean isUserSpecial = false;
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
						isUserSpecial = true;
						List<String> listaUoUtente = new ArrayList<String>();
				    	for (UoForUsersSpecial uo : userSpecial.getUoForUsersSpecials()){
				    		listaUoUtente.add(uo.getCodice_uo().substring(0,3)+"."+uo.getCodice_uo().substring(3,6));
				    	}
						criterionList.add(Restrictions.in("uoRich", listaUoUtente));
					} else {
						criterionList.add(Restrictions.eq("uid", principal.getName()));
					}
				}
			} else {
				criterionList.add(Restrictions.eq("uid", principal.getName()));
			}
		}
		criterionList.add(Restrictions.not(Restrictions.eq("stato", Costanti.STATO_ANNULLATO)));

		List<OrdineMissione> ordineMissioneList=null;
		if (isServiceRest) {
			if (isForValidateFlows){
				criterionList.add(Restrictions.conjunction().add(Restrictions.disjunction().add(Restrictions.eq("statoFlusso", Costanti.STATO_INVIATO_FLUSSO)).add(Restrictions.disjunction().add(Restrictions.eq("stato", Costanti.STATO_INSERITO)))));
			}
			ordineMissioneList = crudServiceBean.findByProjection(principal, OrdineMissione.class, OrdineMissione.getProjectionForElencoMissioni(), criterionList, true, Order.asc("dataInserimento"));
		} else
			ordineMissioneList = crudServiceBean.findByCriterion(principal, OrdineMissione.class, criterionList, Order.asc("dataInserimento"));

		return ordineMissioneList;
    }

//    @Transactional(readOnly = true)
//    public AutoPropria getAutoPropria(String targa) {
//        return autoPropriaRepository.getAutoPropria(SecurityUtils.getCurrentLogin(), targa);
//    }
//
    @Transactional(propagation = Propagation.REQUIRED)
    public OrdineMissione createOrdineMissione(Principal principal, OrdineMissione ordineMissione)  throws AwesomeException, 
    ComponentException, OptimisticLockException, OptimisticLockException, PersistencyException, BusyResourceException {
    	controlloDatiObbligatoriDaGUI(ordineMissione);
    	inizializzaCampiPerInserimento(principal, ordineMissione);
		validaCRUD(principal, ordineMissione);
		ordineMissione = (OrdineMissione)crudServiceBean.creaConBulk(principal, ordineMissione);
//    	autoPropriaRepository.save(autoPropria);
    	log.debug("Created Information for User: {}", ordineMissione);
    	return ordineMissione;
    }


    @Transactional(readOnly = true)
    private void inizializzaCampiPerInserimento(Principal principal,
    		OrdineMissione ordineMissione) throws ComponentException,
    		PersistencyException, BusyResourceException {
    	ordineMissione.setUidInsert(principal.getName());
    	ordineMissione.setUser(principal.getName());
    	Integer anno = recuperoAnno(ordineMissione);
    	ordineMissione.setAnno(anno);
    	ordineMissione.setNumero(datiIstitutoService.getNextPG(principal, ordineMissione.getCdsRich(), anno ));
    	if (StringUtils.isEmpty(ordineMissione.getTrattamento())){
    		ordineMissione.setTrattamento("R");
    	}
    	if (StringUtils.isEmpty(ordineMissione.getObbligoRientro())){
    		ordineMissione.setObbligoRientro("S");
    	}
    	if (StringUtils.isEmpty(ordineMissione.getUtilizzoAutoNoleggio())){
    		ordineMissione.setUtilizzoAutoNoleggio("N");
    	}
    	if (StringUtils.isEmpty(ordineMissione.getUtilizzoTaxi())){
    		ordineMissione.setUtilizzoTaxi("N");
    	}
    	ordineMissione.setStato(Costanti.STATO_INSERITO);
    	ordineMissione.setStatoFlusso(Costanti.STATO_INSERITO);
    	ordineMissione.setToBeCreated();
    }

	private Integer recuperoAnno(OrdineMissione ordineMissione) {
		if (ordineMissione.getDataInserimento() == null){
			ordineMissione.setDataInserimento(new Date());
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(ordineMissione.getDataInserimento());
		Integer anno = 	calendar.get(Calendar.YEAR);
		return anno;
	}

    @Transactional(propagation = Propagation.REQUIRED)
    public OrdineMissione updateOrdineMissione(Principal principal, OrdineMissione ordineMissione)  throws AwesomeException, 
    ComponentException, Exception{
    	return updateOrdineMissione(principal, ordineMissione, false);
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    private OrdineMissione updateOrdineMissione(Principal principal, OrdineMissione ordineMissione, Boolean fromFlows)  throws AwesomeException, 
    ComponentException, Exception{
    	return updateOrdineMissione(principal, ordineMissione, fromFlows, false);
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public OrdineMissione updateOrdineMissione(Principal principal, OrdineMissione ordineMissione, Boolean fromFlows, Boolean confirm)  throws AwesomeException, 
    ComponentException, Exception {

    	OrdineMissione ordineMissioneDB = (OrdineMissione)crudServiceBean.findById(principal, OrdineMissione.class, ordineMissione.getId());

		if (ordineMissioneDB==null){
			throw new AwesomeException(CodiciErrore.ERRGEN, "Ordine di Missione da aggiornare inesistente.");
		}
		
		if (ordineMissioneDB.isMissioneConfermata() && !fromFlows){
			if (ordineMissioneDB.isStatoFlussoApprovato()){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Non è possibile modificare l'ordine di missione. E' già stato approvato.");
			}
			throw new AwesomeException(CodiciErrore.ERRGEN, "Non è possibile modificare l'ordine di missione. E' già stato avviato il flusso di approvazione.");
		}
		ordineMissioneDB.setStato(ordineMissione.getStato());
		ordineMissioneDB.setStatoFlusso(ordineMissione.getStatoFlusso());
		ordineMissioneDB.setCdrSpesa(ordineMissione.getCdrSpesa());
		ordineMissioneDB.setCdsSpesa(ordineMissione.getCdsSpesa());
		ordineMissioneDB.setUoSpesa(ordineMissione.getUoSpesa());
		ordineMissioneDB.setDomicilioFiscaleRich(ordineMissione.getDomicilioFiscaleRich());
		ordineMissioneDB.setDataInizioMissione(ordineMissione.getDataInizioMissione());
		ordineMissioneDB.setDataFineMissione(ordineMissione.getDataFineMissione());
		ordineMissioneDB.setDestinazione(ordineMissione.getDestinazione());
		ordineMissioneDB.setDistanzaDallaSede(ordineMissione.getDistanzaDallaSede());
		ordineMissioneDB.setGae(ordineMissione.getGae());
		ordineMissioneDB.setImportoPresunto(ordineMissione.getImportoPresunto());
		ordineMissioneDB.setModulo(ordineMissione.getModulo());
		ordineMissioneDB.setNote(ordineMissione.getNote());
		ordineMissioneDB.setObbligoRientro(ordineMissione.getObbligoRientro());
		ordineMissioneDB.setOggetto(ordineMissione.getOggetto());
		ordineMissioneDB.setPartenzaDa(ordineMissione.getPartenzaDa());
		ordineMissioneDB.setPriorita(ordineMissione.getPriorita());
		ordineMissioneDB.setTipoMissione(ordineMissione.getTipoMissione());
		ordineMissioneDB.setVoce(ordineMissione.getVoce());
		ordineMissioneDB.setTrattamento(ordineMissione.getTrattamento());
		ordineMissioneDB.setNazione(ordineMissione.getNazione());

		ordineMissioneDB.setNoteUtilizzoTaxiNoleggio(ordineMissione.getNoteUtilizzoTaxiNoleggio());
		ordineMissioneDB.setUtilizzoAutoNoleggio(ordineMissione.getUtilizzoAutoNoleggio());
		ordineMissioneDB.setUtilizzoTaxi(ordineMissione.getUtilizzoTaxi());
		ordineMissioneDB.setPgProgetto(ordineMissione.getPgProgetto());
		ordineMissioneDB.setPgProgetto(ordineMissione.getPgProgetto());
		ordineMissioneDB.setEsercizioOriginaleObbligazione(ordineMissione.getEsercizioOriginaleObbligazione());
		ordineMissioneDB.setPgObbligazione(ordineMissione.getPgObbligazione());
		
    	if (confirm){
        	ordineMissioneDB.setStato(Costanti.STATO_CONFERMATO);
    	} 

    	ordineMissioneDB.setToBeUpdated();

//		//effettuo controlli di validazione operazione CRUD
		validaCRUD(principal, ordineMissioneDB);

    	if (confirm){
    		cmisOrdineMissioneService.avviaFlusso((Principal) SecurityUtils.getCurrentUser(), ordineMissioneDB);
    	}
		ordineMissioneDB = (OrdineMissione)crudServiceBean.modificaConBulk(principal, ordineMissioneDB);
    	
//    	autoPropriaRepository.save(autoPropria);
    	log.debug("Updated Information for Ordine di Missione: {}", ordineMissioneDB);

    	return ordineMissione;
    }

    @Transactional(readOnly = true)
	public void salvaStampaOrdineMissioneSuCMIS(Principal principal, Long idMissione, byte[] stampa) throws AwesomeException, ComponentException {
    	OrdineMissione ordineMissione = getOrdineMissione(principal, idMissione);
		cmisOrdineMissioneService.salvaStampaOrdineMissioneSuCMIS(principal, stampa, ordineMissione);
    }

    @Transactional(propagation = Propagation.REQUIRED)
	public void deleteOrdineMissione(Principal principal, Long idOrdineMissione) throws AwesomeException, ComponentException, OptimisticLockException, PersistencyException, BusyResourceException {
		OrdineMissione ordineMissione = (OrdineMissione)crudServiceBean.findById(principal, OrdineMissione.class, idOrdineMissione);
		if (ordineMissione != null){
			if (!ordineMissione.isMissioneInserita()){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Non è possibile cancellare un ordine di missione che non si trova in uno stato "+Costanti.STATO.get(Costanti.STATO_INSERITO));
			}
			ordineMissioneAnticipoService.deleteAnticipo(principal, ordineMissione);
			ordineMissioneAutoPropriaService.deleteAutoPropria(principal, ordineMissione);
			//effettuo controlli di validazione operazione CRUD
			ordineMissione.setStato(Costanti.STATO_ANNULLATO);
			ordineMissione.setToBeUpdated();
			if (ordineMissione.isStatoInviatoAlFlusso() && !StringUtils.isEmpty(ordineMissione.getIdFlusso())){
				cmisOrdineMissioneService.annullaFlusso(ordineMissione);
			}
			crudServiceBean.modificaConBulk(principal, ordineMissione);
		}
	}
	
    private void controlloDatiObbligatoriDaGUI(OrdineMissione ordineMissione){
		if (ordineMissione != null){
			if (StringUtils.isEmpty(ordineMissione.getCdsRich())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Cds Richiedente");
			} else if (StringUtils.isEmpty(ordineMissione.getCdsSpesa())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Cds Spesa");
			} else if (StringUtils.isEmpty(ordineMissione.getUoRich())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Uo Richiedente");
			} else if (StringUtils.isEmpty(ordineMissione.getUoSpesa())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Uo Spesa");
			} else if (StringUtils.isEmpty(ordineMissione.getDataInizioMissione())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Data Inizio Missione");
			} else if (StringUtils.isEmpty(ordineMissione.getDataFineMissione())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Data Fine Missione");
			} else if (StringUtils.isEmpty(ordineMissione.getDataInserimento())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Data Inserimento");
			} else if (StringUtils.isEmpty(ordineMissione.getDatoreLavoroRich())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Datore di Lavoro Richiedente");
			} else if (StringUtils.isEmpty(ordineMissione.getDestinazione())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Destinazione");
			} else if (StringUtils.isEmpty(ordineMissione.getComuneResidenzaRich())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Comune di Residenza del Richiedente");
			} else if (StringUtils.isEmpty(ordineMissione.getIndirizzoResidenzaRich())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Indirizzo di Residenza del Richiedente");
			} else if (StringUtils.isEmpty(ordineMissione.getOggetto())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Oggetto");
			} else if (StringUtils.isEmpty(ordineMissione.getPriorita())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Priorità");
			} else if (StringUtils.isEmpty(ordineMissione.getTipoMissione())){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Tipo Missione");
			} 
			if (ordineMissione.isMissioneEstera()){
				if (StringUtils.isEmpty(ordineMissione.getNazione())){
					throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Nazione");
				} 
				if (StringUtils.isEmpty(ordineMissione.getTrattamento())){
					throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Trattamento");
				} 
			}

			if (ordineMissione.isMissioneConGiorniDivervi()){
				if (StringUtils.isEmpty(ordineMissione.getObbligoRientro())){
					throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Obbligo di Rientro");
				}
			} 
		}
    }
	private void controlloCongruenzaDatiInseriti(Principal principal, OrdineMissione ordineMissione) throws AwesomeException {
		if (ordineMissione.getDataFineMissione().before(ordineMissione.getDataFineMissione())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.ERR_DATE_INCONGRUENTI+": La data di fine missione non può essere precedente alla data di inizio missione");
		}
		if (!StringUtils.isEmpty(ordineMissione.getNoteUtilizzoTaxiNoleggio())){
			if (ordineMissione.getUtilizzoTaxi().equals("N") && ordineMissione.getUtilizzoAutoNoleggio().equals("N")){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.ERR_DATE_INCONGRUENTI+": Non è possibile indicare le note all'utilizzo del tazi o dell'auto a noleggio se non si è scelto il loro utilizzo");
			}
		}
		if (StringUtils.isEmpty(ordineMissione.getIdFlusso()) &&  ordineMissione.isStatoInviatoAlFlusso()){
			throw new AwesomeException(CodiciErrore.ERRGEN, "Non è possibile avere lo stato Inviato al flusso e non avere l'ID del flusso");
		} 
	}
	
    @Transactional(propagation = Propagation.REQUIRED)
	private void controlloDatiFinanziari(Principal principal, OrdineMissione ordineMissione) throws AwesomeException {
    	UnitaOrganizzativa uo = unitaOrganizzativaService.loadUo(ordineMissione.getUoSpesa(), ordineMissione.getCdsSpesa(), ordineMissione.getAnno());
    	if (uo == null){
    		throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": La UO "+ ordineMissione.getUoSpesa() + " non è corretta rispetto al CDS "+ordineMissione.getCdsSpesa());
    	}
		if (!StringUtils.isEmpty(ordineMissione.getCdrSpesa())){
			Cdr cdr = cdrService.loadCdr(ordineMissione.getCdrSpesa(), ordineMissione.getUoSpesa());
			if (cdr == null){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": Il CDR "+ ordineMissione.getCdrSpesa() + " non è corretto rispetto alla UO "+ordineMissione.getUoSpesa());
			}
		}
		if (!StringUtils.isEmpty(ordineMissione.getPgProgetto())){
			Progetto progetto = progettoService.loadModulo(ordineMissione.getPgProgetto(), ordineMissione.getAnno(), ordineMissione.getUoSpesa());
			if (progetto == null){
				throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": Il modulo indicato non è corretto rispetto alla UO "+ordineMissione.getUoSpesa());
			}
		}
		if (!StringUtils.isEmpty(ordineMissione.getGae())){
			if (StringUtils.isEmpty(ordineMissione.getCdrSpesa())){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Non è possibile indicare la GAE senza il centro di responsabilità");
			}
			Gae gae = gaeService.loadGae(ordineMissione);
			if (gae == null){
				throw new AwesomeException(CodiciErrore.ERRGEN, "La GAE "+ ordineMissione.getGae()+" indicata non esiste");
			} else {
				boolean progettoCdrIndicato = false;
				if (!StringUtils.isEmpty(ordineMissione.getPgProgetto()) && !StringUtils.isEmpty(gae.getPg_progetto())){
					progettoCdrIndicato = true;
					if (gae.getPg_progetto().compareTo(ordineMissione.getPgProgetto()) != 0){
						throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": La GAE indicata "+ ordineMissione.getGae()+" non corrisponde al modulo indicato.");
					}
				}
				if (!StringUtils.isEmpty(ordineMissione.getCdrSpesa())){
					progettoCdrIndicato = true;
					if (!gae.getCd_centro_responsabilita().equals(ordineMissione.getCdrSpesa()) ){
						throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": La GAE indicata "+ ordineMissione.getGae()+" non corrisponde con il CDR "+ordineMissione.getCdrSpesa() +" indicato.");
					}
				}
				if (!progettoCdrIndicato){
					throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": Non è possibile indicare solo La GAE senza il modulo o il CDR.");
				}
			}
		}

		if (!StringUtils.isEmpty(ordineMissione.getPgObbligazione())){
			if (StringUtils.isEmpty(ordineMissione.getEsercizioOriginaleObbligazione())){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Oltre al numero dell'impegno è necessario indicare anche l'anno dell'impegno");
			}
			if (!StringUtils.isEmpty(ordineMissione.getGae())){
				ImpegnoGae impegnoGae = impegnoGaeService.loadImpegno(ordineMissione);
				if (impegnoGae == null){
					throw new AwesomeException(CodiciErrore.ERRGEN, "L'impegno indicato "+ ordineMissione.getEsercizioOriginaleObbligazione() + "-" + ordineMissione.getPgObbligazione() +" non corrisponde con la GAE "+ ordineMissione.getGae()+" indicata oppure non esiste");
				} else {
					if (!StringUtils.isEmpty(ordineMissione.getVoce())){
						if (!impegnoGae.getCdElementoVoce().equals(ordineMissione.getVoce())){
							throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": L'impegno indicato "+ordineMissione.getEsercizioOriginaleObbligazione() + "-" + ordineMissione.getPgObbligazione() +" non corrisponde con la voce di Bilancio indicata."+ordineMissione.getVoce());
						}
					}
					ordineMissione.setCdCdsObbligazione(impegnoGae.getCdCds());
					ordineMissione.setEsercizioObbligazione(impegnoGae.getEsercizio());
				}
			} else {
				Impegno impegno = impegnoService.loadImpegno(ordineMissione);
				if (impegno == null){
					throw new AwesomeException(CodiciErrore.ERRGEN, "L'impegno indicato "+ ordineMissione.getEsercizioOriginaleObbligazione() + "-" + ordineMissione.getPgObbligazione() +" non esiste");
				} else {
					if (!StringUtils.isEmpty(ordineMissione.getVoce())){
						if (!impegno.getCdElementoVoce().equals(ordineMissione.getVoce())){
							throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.DATI_INCONGRUENTI+": L'impegno indicato "+ordineMissione.getEsercizioOriginaleObbligazione() + "-" + ordineMissione.getPgObbligazione() +" non corrisponde con la voce di Bilancio indicata."+ordineMissione.getVoce());
						}
					}
					ordineMissione.setCdCdsObbligazione(impegno.getCdCds());
					ordineMissione.setEsercizioObbligazione(impegno.getEsercizio());
				}
			}
		} else {
			if (!StringUtils.isEmpty(ordineMissione.getEsercizioOriginaleObbligazione())){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Oltre all'anno dell'impegno è necessario indicare anche il numero dell'impegno");
			}
			ordineMissione.setCdCdsObbligazione(null);
			ordineMissione.setEsercizioObbligazione(null);
		}
    
    }
	
    @Transactional(propagation = Propagation.REQUIRED)
	private void validaCRUD(Principal principal, OrdineMissione ordineMissione) throws AwesomeException {
		if (ordineMissione != null){
			controlloCampiObbligatori(ordineMissione); 
			controlloCongruenzaDatiInseriti(principal, ordineMissione);
			controlloDatiFinanziari(principal, ordineMissione);
		}
	}
    
	private void controlloCampiObbligatori(OrdineMissione ordineMissione) {
		if (!ordineMissione.isToBeCreated()){
			controlloDatiObbligatoriDaGUI(ordineMissione);
		}
		if (StringUtils.isEmpty(ordineMissione.getAnno())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Anno");
		} else if (StringUtils.isEmpty(ordineMissione.getObbligoRientro())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Obbligo di Rientro");
		} else if (StringUtils.isEmpty(ordineMissione.getUid())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Utente");
		} else if (StringUtils.isEmpty(ordineMissione.getUtilizzoTaxi())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Utilizzo del Taxi");
		} else if (StringUtils.isEmpty(ordineMissione.getUtilizzoAutoNoleggio())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Utilizzo auto a noleggio");
		} else if (StringUtils.isEmpty(ordineMissione.getStato())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Stato");
		} else if (StringUtils.isEmpty(ordineMissione.getNumero())){
			throw new AwesomeException(CodiciErrore.ERRGEN, CodiciErrore.CAMPO_OBBLIGATORIO+": Numero");
		}
	}
}
