package it.cnr.si.missioni.service;

import java.security.Principal;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.OptimisticLockException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.cnr.jada.ejb.session.BusyResourceException;
import it.cnr.jada.ejb.session.ComponentException;
import it.cnr.jada.ejb.session.PersistencyException;
import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.cmis.CMISRimborsoMissioneService;
import it.cnr.si.missioni.cmis.MissioniCMISService;
import it.cnr.si.missioni.domain.custom.persistence.RimborsoMissione;
import it.cnr.si.missioni.domain.custom.persistence.RimborsoMissioneDettagli;
import it.cnr.si.missioni.repository.CRUDComponentSession;
import it.cnr.si.missioni.repository.RimborsoMissioneDettagliRepository;
import it.cnr.si.missioni.util.CodiciErrore;
import it.cnr.si.missioni.util.Costanti;

/**
 * Service class for managing users.
 */
@Service
public class RimborsoMissioneDettagliService {

    private final Logger log = LoggerFactory.getLogger(RimborsoMissioneDettagliService.class);

    @Autowired
    private RimborsoMissioneDettagliRepository rimborsoMissioneDettagliRepository;

//    @Autowired
//    private PrintOrdineMissioneAutoPropriaService printOrdineMissioneAutoPropriaService;

    @Autowired
    private MissioniCMISService missioniCMISService;

    @Autowired
    private CMISRimborsoMissioneService cmisRimborsoMissioneService;

	@Inject
	private CRUDComponentSession crudServiceBean;


    @Transactional(readOnly = true)
    public List<RimborsoMissioneDettagli> getRimborsoMissioneDettagli(Principal principal, Long idRimborsoMissione) throws ComponentException {
    	RimborsoMissione rimborsoMissione = (RimborsoMissione)crudServiceBean.findById(principal, RimborsoMissione.class, idRimborsoMissione);
		
		if (rimborsoMissione!= null){
			List<RimborsoMissioneDettagli> lista = rimborsoMissioneDettagliRepository.getRimborsoMissioneDettagli(rimborsoMissione);
			return lista;
		}
		return null;
    }

    private void validaCRUD(Principal principal, RimborsoMissioneDettagli rimborsoMissioneDettagli) {
//    	if (StringUtils.isEmpty(ordineMissioneAutoPropria.getCartaCircolazione()) ||
//    			StringUtils.isEmpty(ordineMissioneAutoPropria.getTarga())  ||
//    			StringUtils.isEmpty(ordineMissioneAutoPropria.getPolizzaAssicurativa())  ||
//    			StringUtils.isEmpty(ordineMissioneAutoPropria.getMarca())  ||
//    			StringUtils.isEmpty(ordineMissioneAutoPropria.getModello()) ){
//			throw new AwesomeException(CodiciErrore.ERRGEN, "Dati dell'auto propria non esistenti o incompleti.");
//    	}
//    	if (StringUtils.isEmpty(ordineMissioneAutoPropria.getDataRilascioPatente()) ||
//    			StringUtils.isEmpty(ordineMissioneAutoPropria.getDataScadenzaPatente())  ||
//    			StringUtils.isEmpty(ordineMissioneAutoPropria.getEntePatente())  ||
//    			StringUtils.isEmpty(ordineMissioneAutoPropria.getNumeroPatente())){
//			throw new AwesomeException(CodiciErrore.ERRGEN, "Dati della patente non esistenti o incompleti.");
//    	}
	}

	@Transactional(propagation = Propagation.REQUIRED)
    public RimborsoMissioneDettagli createRimborsoMissioneDettagli(Principal principal, RimborsoMissioneDettagli rimborsoMissioneDettagli)  throws AwesomeException, 
    ComponentException, OptimisticLockException, OptimisticLockException, PersistencyException, BusyResourceException {
		rimborsoMissioneDettagli.setUid(principal.getName());
		rimborsoMissioneDettagli.setUser(principal.getName());
		rimborsoMissioneDettagli.setStato(Costanti.STATO_INSERITO);
		RimborsoMissione rimborsoMissione = (RimborsoMissione)crudServiceBean.findById(principal, RimborsoMissione.class, rimborsoMissioneDettagli.getRimborsoMissione().getId());
		rimborsoMissioneDettagli.setRimborsoMissione(rimborsoMissione);
    	Long maxRiga = rimborsoMissioneDettagliRepository.getMaxRigaDettaglio(rimborsoMissione);
    	if (maxRiga == null ){
    		maxRiga = new Long(0);
    	}
    	maxRiga = maxRiga + 1;
    	rimborsoMissioneDettagli.setRiga(maxRiga);
    	rimborsoMissioneDettagli.setToBeCreated();
		validaCRUD(principal, rimborsoMissioneDettagli);
		rimborsoMissioneDettagli = (RimborsoMissioneDettagli)crudServiceBean.creaConBulk(principal, rimborsoMissioneDettagli);
//    	autoPropriaRepository.save(autoPropria);
    	log.debug("Created Information for RimborsoMissioneDettagli: {}", rimborsoMissioneDettagli);
    	return rimborsoMissioneDettagli;
    }


    @Transactional(propagation = Propagation.REQUIRED)
	public void cancellaRimborsoMissioneDettagli(Principal principal,
			RimborsoMissione rimborsoMissione)
			throws ComponentException {
		List<RimborsoMissioneDettagli> listaRimborsoMissioneDettagli = rimborsoMissioneDettagliRepository.getRimborsoMissioneDettagli(rimborsoMissione);
		if (listaRimborsoMissioneDettagli != null && !listaRimborsoMissioneDettagli.isEmpty()){
			for (Iterator<RimborsoMissioneDettagli> iterator = listaRimborsoMissioneDettagli.iterator(); iterator.hasNext();){
				RimborsoMissioneDettagli dettaglio = iterator.next();
				cancellaRimborsoMissioneDettagli(principal, dettaglio);
		    }
		}
	}
	
    @Transactional(propagation = Propagation.REQUIRED)
	public void deleteRimborsoMissioneDettagli(Principal principal, Long idRimborsoMissioneDettagli) throws AwesomeException, ComponentException, OptimisticLockException, PersistencyException, BusyResourceException {
    	RimborsoMissioneDettagli rimborsoMissioneDettagli = (RimborsoMissioneDettagli)crudServiceBean.findById(principal, RimborsoMissioneDettagli.class, idRimborsoMissioneDettagli);

		//effettuo controlli di validazione operazione CRUD
		if (rimborsoMissioneDettagli != null){
			cancellaRimborsoMissioneDettagli(principal, rimborsoMissioneDettagli);
		}
	}

    @Transactional(propagation = Propagation.REQUIRED)
	private void cancellaRimborsoMissioneDettagli(Principal principal, RimborsoMissioneDettagli rimborsoMissioneDettagli) throws ComponentException {
    	rimborsoMissioneDettagli.setToBeUpdated();
    	rimborsoMissioneDettagli.setStato(Costanti.STATO_ANNULLATO);
		crudServiceBean.modificaConBulk(principal, rimborsoMissioneDettagli);
	}

    @Transactional(propagation = Propagation.REQUIRED)
    public RimborsoMissioneDettagli updateRimborsoMissioneDettagli(Principal principal, RimborsoMissioneDettagli rimborsoMissioneDettagli)  throws AwesomeException, 
    ComponentException, OptimisticLockException, OptimisticLockException, PersistencyException, BusyResourceException {

    	RimborsoMissioneDettagli rimborsoMissioneDettagliDB = (RimborsoMissioneDettagli)crudServiceBean.findById(principal, RimborsoMissioneDettagli.class, rimborsoMissioneDettagli.getId());

		if (rimborsoMissioneDettagliDB==null)
			throw new AwesomeException(CodiciErrore.ERRGEN, "Dettaglio Rimborso Missione da aggiornare inesistente.");
		
		rimborsoMissioneDettagliDB.setCdTiPasto(rimborsoMissioneDettagli.getCdTiPasto());
		rimborsoMissioneDettagliDB.setCdTiSpesa(rimborsoMissioneDettagli.getCdTiSpesa());
		rimborsoMissioneDettagliDB.setDataSpesa(rimborsoMissioneDettagli.getDataSpesa());
		rimborsoMissioneDettagliDB.setDsSpesa(rimborsoMissioneDettagli.getDsSpesa());
		rimborsoMissioneDettagliDB.setTiSpesaDiaria(rimborsoMissioneDettagli.getTiSpesaDiaria());
		rimborsoMissioneDettagliDB.setDsTiSpesa(rimborsoMissioneDettagli.getDsTiSpesa());
		rimborsoMissioneDettagliDB.setCambio(rimborsoMissioneDettagli.getCambio());
		rimborsoMissioneDettagliDB.setCdDivisa(rimborsoMissioneDettagli.getCdDivisa());
		rimborsoMissioneDettagliDB.setImporto(rimborsoMissioneDettagli.getImporto());
		rimborsoMissioneDettagliDB.setImportoEuro(rimborsoMissioneDettagli.getImportoEuro());
		rimborsoMissioneDettagliDB.setImportoDivisa(rimborsoMissioneDettagli.getImportoDivisa());
		
		rimborsoMissioneDettagliDB.setToBeUpdated();


		rimborsoMissioneDettagliDB = (RimborsoMissioneDettagli)crudServiceBean.modificaConBulk(principal, rimborsoMissioneDettagliDB);
    	
    	log.debug("Updated Information for Dettaglio Rimborso Missione: {}", rimborsoMissioneDettagliDB);
    	return rimborsoMissioneDettagli;
    }
    
}
