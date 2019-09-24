package it.cnr.si.missioni.service;

import java.io.InputStream;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.OptimisticLockException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import it.cnr.jada.ejb.session.BusyResourceException;
import it.cnr.jada.ejb.session.ComponentException;
import it.cnr.jada.ejb.session.PersistencyException;
import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.cmis.CMISFileAttachment;
import it.cnr.si.missioni.cmis.CMISOrdineMissioneService;
import it.cnr.si.missioni.cmis.MimeTypes;
import it.cnr.si.missioni.cmis.MissioniCMISService;
import it.cnr.si.missioni.domain.custom.persistence.OrdineMissione;
import it.cnr.si.missioni.domain.custom.persistence.OrdineMissioneAnticipo;
import it.cnr.si.missioni.repository.CRUDComponentSession;
import it.cnr.si.missioni.repository.OrdineMissioneAnticipoRepository;
import it.cnr.si.missioni.util.CodiciErrore;
import it.cnr.si.missioni.util.Costanti;
import it.cnr.si.missioni.util.DateUtils;
import it.cnr.si.missioni.util.SecurityUtils;
import it.cnr.si.spring.storage.StorageObject;

/**
 * Service class for managing users.
 */
@Service
public class OrdineMissioneAnticipoService {

	private final Logger log = LoggerFactory.getLogger(OrdineMissioneAnticipoService.class);

	@Autowired
	private OrdineMissioneAnticipoRepository ordineMissioneAnticipoRepository;

	@Autowired
	private MissioniCMISService missioniCMISService;

	@Autowired
	private CMISOrdineMissioneService cmisOrdineMissioneService;

	@Autowired
	private PrintOrdineMissioneAnticipoService printOrdineMissioneAnticipoService;

	@Autowired
	private CRUDComponentSession crudServiceBean;

	@Transactional(readOnly = true)
	public OrdineMissioneAnticipo getAnticipo(Principal principal, Long idMissione) throws ComponentException {
		return getAnticipo(principal, idMissione, false);
	}

	@Transactional(readOnly = true)
	public OrdineMissioneAnticipo getAnticipo(Principal principal, Long idMissione, Boolean valorizzaOrdineMissione)
			throws ComponentException {
		OrdineMissione ordineMissione = (OrdineMissione) crudServiceBean.findById(principal, OrdineMissione.class,
				idMissione);

		if (ordineMissione != null) {
			return getAnticipo(ordineMissione, valorizzaOrdineMissione);
		}
		return null;
	}

	@Transactional(readOnly = true)
	public OrdineMissioneAnticipo getAnticipo(OrdineMissione ordineMissione, Boolean valorizzaOrdineMissione)
			throws ComponentException {
		if (ordineMissione != null) {
			OrdineMissioneAnticipo anticipo = ordineMissioneAnticipoRepository.getAnticipo(ordineMissione);
			if (anticipo != null && valorizzaOrdineMissione) {
				anticipo.setOrdineMissione(ordineMissione);
			}
			return anticipo;
		}
		return null;
	}

	@Transactional(readOnly = true)
	public OrdineMissioneAnticipo getAnticipo(Long idOrdineMissione)
			throws ComponentException {
		OrdineMissioneAnticipo anticipo = ordineMissioneAnticipoRepository.getAnticipo(idOrdineMissione);
		return anticipo;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public OrdineMissioneAnticipo createAnticipo(Principal principal, OrdineMissioneAnticipo ordineMissioneAnticipo)
			throws AwesomeException, ComponentException, OptimisticLockException, OptimisticLockException,
			PersistencyException, BusyResourceException {
		ordineMissioneAnticipo.setUid(principal.getName());
		ordineMissioneAnticipo.setUser(principal.getName());
		OrdineMissione ordineMissione = (OrdineMissione) crudServiceBean.findById(principal, OrdineMissione.class,
				ordineMissioneAnticipo.getOrdineMissione().getId());
		ordineMissioneAnticipo.setOrdineMissione(ordineMissione);
		ordineMissioneAnticipo.setStato(Costanti.STATO_INSERITO);
		ordineMissioneAnticipo.setStatoFlusso(Costanti.STATO_INSERITO);
		ordineMissioneAnticipo.setDataRichiesta(DateUtils.getCurrentTime());
		ordineMissioneAnticipo.setToBeCreated();
		ordineMissioneAnticipo = (OrdineMissioneAnticipo) crudServiceBean.creaConBulk(principal,
				ordineMissioneAnticipo);
		log.debug("Created Information for OrdineMissioneAutoPropria: {}", ordineMissioneAnticipo);
		return ordineMissioneAnticipo;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public OrdineMissioneAnticipo updateAnticipo(Principal principal, OrdineMissioneAnticipo ordineMissioneAnticipo)
			throws AwesomeException, ComponentException, OptimisticLockException, OptimisticLockException,
			PersistencyException, BusyResourceException {
		return updateAnticipo(principal, ordineMissioneAnticipo, false);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public OrdineMissioneAnticipo updateAnticipo(Principal principal, OrdineMissioneAnticipo ordineMissioneAnticipo,
			Boolean confirm) throws AwesomeException, ComponentException {

		OrdineMissioneAnticipo ordineMissioneAnticipoDB = (OrdineMissioneAnticipo) crudServiceBean.findById(principal,
				OrdineMissioneAnticipo.class, ordineMissioneAnticipo.getId());

		if (ordineMissioneAnticipoDB == null)
			throw new AwesomeException(CodiciErrore.ERRGEN,
					"Anticipo di Ordine di Missione da aggiornare inesistente.");

		if (confirm) {
			if (ordineMissioneAnticipoDB.isAnticipoConfermato()) {
				throw new AwesomeException(CodiciErrore.ERRGEN,
						"Non è possibile rendere definitivo l'anticipo. E' già stato avviato il flusso di approvazione.");
			} else if (ordineMissioneAnticipoDB.getOrdineMissione().isMissioneInserita()) {
				throw new AwesomeException(CodiciErrore.ERRGEN,
						"Non è possibile rendere definitivo l'anticipo. E' necessario prima avviare il flusso di approvazione per il relativo ordine di missione.");
			}
			ordineMissioneAnticipoDB.setStato(Costanti.STATO_CONFERMATO);
			ordineMissioneAnticipoDB.setStatoFlusso(Costanti.STATO_INVIATO_FLUSSO);
		} else {
			ordineMissioneAnticipoDB.setStato(ordineMissioneAnticipo.getStato());
			ordineMissioneAnticipoDB.setStatoFlusso(ordineMissioneAnticipo.getStatoFlusso());
			ordineMissioneAnticipoDB.setImporto(ordineMissioneAnticipo.getImporto());
			ordineMissioneAnticipoDB.setNote(ordineMissioneAnticipo.getNote());
		}

		ordineMissioneAnticipoDB.setToBeUpdated();

		if (confirm) {
			avviaFlusso((Principal) SecurityUtils.getCurrentUser(), ordineMissioneAnticipoDB);
		}

		ordineMissioneAnticipoDB = (OrdineMissioneAnticipo) crudServiceBean.modificaConBulk(principal,
				ordineMissioneAnticipoDB);
		log.debug("Updated Information for Anticipo Ordine di Missione: {}", ordineMissioneAnticipoDB);

		return ordineMissioneAnticipo;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void avviaFlusso(Principal principal, OrdineMissioneAnticipo ordineMissioneAnticipo)
			throws AwesomeException, ComponentException {

	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteAnticipo(Principal principal, OrdineMissione ordineMissione)
			throws AwesomeException, ComponentException {
		OrdineMissioneAnticipo anticipo = ordineMissioneAnticipoRepository.getAnticipo(ordineMissione);
		if (anticipo != null && anticipo.getId() != null) {
			cancellaOrdineMissioneAnticipo(principal, anticipo);
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteAnticipo(Principal principal, Long idAnticipoOrdineMissione) throws AwesomeException,
			ComponentException, OptimisticLockException, PersistencyException, BusyResourceException {
		OrdineMissioneAnticipo ordineMissioneAnticipo = (OrdineMissioneAnticipo) crudServiceBean.findById(principal,
				OrdineMissioneAnticipo.class, idAnticipoOrdineMissione);
		if (ordineMissioneAnticipo != null) {
			cancellaOrdineMissioneAnticipo(principal, ordineMissioneAnticipo);
		}
	}

	private void cancellaOrdineMissioneAnticipo(Principal principal, OrdineMissioneAnticipo ordineMissioneAnticipo)
			throws ComponentException {
		if (ordineMissioneAnticipo.isStatoNonInviatoAlFlusso()){
			ordineMissioneAnticipo.setToBeUpdated();
			ordineMissioneAnticipo.setStato(Costanti.STATO_ANNULLATO);
			crudServiceBean.modificaConBulk(principal, ordineMissioneAnticipo);
			OrdineMissione ordineMissione = (OrdineMissione) crudServiceBean.findById(principal, OrdineMissione.class, ordineMissioneAnticipo.getOrdineMissione().getId());

			List<StorageObject> listaAllegati = cmisOrdineMissioneService.getAttachmentsAnticipo(ordineMissione);
			if (listaAllegati != null){
				for (StorageObject object : listaAllegati){
					if (ordineMissione != null && StringUtils.hasLength(ordineMissione.getIdFlusso())){
						StorageObject folderOrdineMissione = cmisOrdineMissioneService.recuperoFolderOrdineMissione(ordineMissione);
						missioniCMISService.eliminaFilePresenteNelFlusso(principal, object.getKey(), folderOrdineMissione);
					} else {
		        		missioniCMISService.deleteNode(object.getKey());
					}
				}
			}

		} else {
			throw new AwesomeException(CodiciErrore.ERRGEN,
					"Non è possibile cancellare l'anticipo. E' già stato inviato al flusso per l'approvazione.");
		}
	}

	@Transactional(readOnly = true)
	public Map<String, byte[]> printOrdineMissioneAnticipo(Authentication auth, Long idMissione)
			throws AwesomeException, ComponentException {
		String username = SecurityUtils.getCurrentUserLogin();
		Principal principal = (Principal) auth;
		OrdineMissioneAnticipo ordineMissioneAnticipo = getAnticipo(principal, idMissione, true);
		OrdineMissione ordineMissione = ordineMissioneAnticipo.getOrdineMissione();
		Map<String, byte[]> map = new HashMap<String, byte[]>();
		byte[] printOrdineMissione = null;
		String fileName = null;
    	if ((ordineMissione.isStatoInviatoAlFlusso()  && (!ordineMissione.isMissioneInserita() || !ordineMissione.isMissioneDaValidare())) || (ordineMissione.isStatoFlussoApprovato())){
    		map = cmisOrdineMissioneService.getFileOrdineMissioneAnticipo(ordineMissioneAnticipo);
		} else {
			fileName = "OrdineMissioneAnticipo" + idMissione + ".pdf";
			printOrdineMissione = printAnticipo(username, ordineMissioneAnticipo);
			if (ordineMissioneAnticipo.isAnticipoInserito()) {
				cmisOrdineMissioneService.salvaStampaAnticipoSuCMIS(username, printOrdineMissione, ordineMissioneAnticipo);
			}
			map.put(fileName, printOrdineMissione);
		}
		return map;
	}

	private byte[] printAnticipo(String username, OrdineMissioneAnticipo ordineMissioneAnticipo)
			throws ComponentException {
		byte[] printOrdineMissione = printOrdineMissioneAnticipoService
				.printOrdineMissioneAnticipo(ordineMissioneAnticipo, username);
		return printOrdineMissione;
	}

	@Transactional(readOnly = true)
	public String jsonForPrintOrdineMissione(Principal principal, Long idMissione)
			throws AwesomeException, ComponentException {
		OrdineMissioneAnticipo ordineMissioneAnticipo = getAnticipo(principal, idMissione, true);
		return printOrdineMissioneAnticipoService.createJsonPrintOrdineMissioneAnticipo(ordineMissioneAnticipo,
				principal.getName());
	}

	@Transactional(readOnly = true)
	public List<CMISFileAttachment> getAttachments(Principal principal, Long idAnticipo)
			throws ComponentException {
		if (idAnticipo != null) {
			OrdineMissioneAnticipo ordineMissioneAnticipo = (OrdineMissioneAnticipo) crudServiceBean.findById(principal,
					OrdineMissioneAnticipo.class, idAnticipo);
			OrdineMissione ordineMissione = (OrdineMissione) crudServiceBean.findById(principal, OrdineMissione.class,
					ordineMissioneAnticipo.getOrdineMissione().getId());
			List<CMISFileAttachment> lista = cmisOrdineMissioneService.getAttachmentsAnticipo(ordineMissione, idAnticipo);
			return lista;
		}
		return null;
	}

	@Transactional(readOnly = true)
	public CMISFileAttachment uploadAllegato(Principal principal, Long idAnticipo,
			InputStream inputStream, String name, MimeTypes mimeTypes) throws ComponentException {
		OrdineMissioneAnticipo ordineMissioneAnticipo = (OrdineMissioneAnticipo) crudServiceBean.findById(principal,
				OrdineMissioneAnticipo.class, idAnticipo);
		OrdineMissione ordineMissione = (OrdineMissione) crudServiceBean.findById(principal, OrdineMissione.class,
				ordineMissioneAnticipo.getOrdineMissione().getId());
		if (ordineMissione != null) {
			return cmisOrdineMissioneService.uploadAttachmentAnticipo(principal, ordineMissione,idAnticipo,
					inputStream, name, mimeTypes);
		}
		return null;
	}
}
