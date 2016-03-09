package it.cnr.si.missioni.cmis;

import it.cnr.jada.ejb.session.ComponentException;
import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.cmis.flows.FlowResubmitType;
import it.cnr.si.missioni.domain.custom.persistence.DatiIstituto;
import it.cnr.si.missioni.domain.custom.persistence.OrdineMissione;
import it.cnr.si.missioni.domain.custom.persistence.OrdineMissioneAnticipo;
import it.cnr.si.missioni.domain.custom.persistence.OrdineMissioneAutoPropria;
import it.cnr.si.missioni.service.DatiIstitutoService;
import it.cnr.si.missioni.service.OrdineMissioneAnticipoService;
import it.cnr.si.missioni.service.OrdineMissioneAutoPropriaService;
import it.cnr.si.missioni.service.PrintOrdineMissioneService;
import it.cnr.si.missioni.service.UoService;
import it.cnr.si.missioni.util.CodiciErrore;
import it.cnr.si.missioni.util.Costanti;
import it.cnr.si.missioni.util.DateUtils;
import it.cnr.si.missioni.util.Utility;
import it.cnr.si.missioni.util.data.Uo;
import it.cnr.si.missioni.util.proxy.json.object.Account;
import it.cnr.si.missioni.util.proxy.json.object.Gae;
import it.cnr.si.missioni.util.proxy.json.object.Impegno;
import it.cnr.si.missioni.util.proxy.json.object.ImpegnoGae;
import it.cnr.si.missioni.util.proxy.json.object.Progetto;
import it.cnr.si.missioni.util.proxy.json.object.UnitaOrganizzativa;
import it.cnr.si.missioni.util.proxy.json.object.Voce;
import it.cnr.si.missioni.util.proxy.json.service.AccountService;
import it.cnr.si.missioni.util.proxy.json.service.GaeService;
import it.cnr.si.missioni.util.proxy.json.service.ImpegnoGaeService;
import it.cnr.si.missioni.util.proxy.json.service.ImpegnoService;
import it.cnr.si.missioni.util.proxy.json.service.ProgettoService;
import it.cnr.si.missioni.util.proxy.json.service.UnitaOrganizzativaService;
import it.cnr.si.missioni.util.proxy.json.service.VoceService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.bindings.spi.http.Response;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CMISOrdineMissioneService {
	public static final String 
			PROPERTY_TIPOLOGIA_DOC = "wfcnr:tipologiaDOC",
			PROPERTY_TIPOLOGIA_DOC_MISSIONI = "cnrmissioni:tipologiaDocumentoMissione";

	@Autowired
	private DatiIstitutoService datiIstitutoService;

	@Autowired
	private PrintOrdineMissioneService printOrdineMissioneService;

	@Autowired
	private UnitaOrganizzativaService unitaOrganizzativaService;

	@Autowired
	private GaeService gaeService;

	@Autowired
	private ProgettoService progettoService;

	@Autowired
	private ImpegnoService impegnoService;

	@Autowired
	private ImpegnoGaeService impegnoGaeService;

	@Autowired
	private VoceService voceService;

	@Autowired
	private UoService uoService;

	@Autowired
	private OrdineMissioneAnticipoService ordineMissioneAnticipoService;

	@Autowired
	private OrdineMissioneAutoPropriaService ordineMissioneAutoPropriaService;

	@Autowired
	private MissioniCMISService missioniCMISService;

	@Autowired
	private AccountService accountService;

	public CMISOrdineMissione create(Principal principal, OrdineMissione ordineMissione) throws ComponentException{
		CMISOrdineMissione cmisOrdineMissione = new CMISOrdineMissione();
		caricaDatiDerivati(principal, ordineMissione);
		OrdineMissioneAnticipo anticipo = null;
		OrdineMissioneAutoPropria autoPropria = null;
		if (ordineMissione != null){
			anticipo = ordineMissioneAnticipoService.getAnticipo(principal, new Long(ordineMissione.getId().toString()));
			if (anticipo != null){
				ordineMissioneAnticipoService.updateAnticipo(principal, anticipo, true);
				ordineMissione.setRichiestaAnticipo("S");
			} else {
				ordineMissione.setRichiestaAnticipo("N");
			}
			autoPropria = ordineMissioneAutoPropriaService.getAutoPropria(principal, new Long(ordineMissione.getId().toString()));
			if (autoPropria != null){
				ordineMissione.setUtilizzoAutoPropria("S");
			} else {
				ordineMissione.setUtilizzoAutoPropria("N");
			}
		}

		String username = principal.getName();
		
		Account account = accountService.loadAccountFromRest(ordineMissione.getUid());
		Progetto progetto = progettoService.loadModulo(ordineMissione.getPgProgetto(), ordineMissione.getAnno(), null);
		Voce voce = voceService.loadVoce(ordineMissione);
		Gae gae = gaeService.loadGae(ordineMissione);
		UnitaOrganizzativa uoCompetenza = null;
		if (ordineMissione.getUoCompetenza() != null){
			uoCompetenza = unitaOrganizzativaService.loadUo(ordineMissione.getUoCompetenza(), null, ordineMissione.getAnno());
		}
		UnitaOrganizzativa uoSpesa = unitaOrganizzativaService.loadUo(ordineMissione.getUoSpesa(), null, ordineMissione.getAnno());
		UnitaOrganizzativa uoRich = unitaOrganizzativaService.loadUo(ordineMissione.getUoRich(), null, ordineMissione.getAnno());
		String descrImpegno = ""; 
		BigDecimal dispImpegno = null;
		if (ordineMissione.getPgObbligazione() != null){
			if (gae != null){
				ImpegnoGae impegnoGae = impegnoGaeService.loadImpegno(ordineMissione);
				descrImpegno = impegnoGae.getDsObbligazione();
				dispImpegno = impegnoGae.getDisponibilitaImpegno();
			} else {
				Impegno impegno = impegnoService.loadImpegno(ordineMissione);
				descrImpegno = impegno.getDsObbligazione();
				dispImpegno = impegno.getDisponibilitaImpegno();
			}
		}

		//		String userNameFirmatario = ldapService.getUid(attestato.getSede().getMatricolaDirettoreSede());

		String uoCompetenzaPerFlusso = Utility.replace(ordineMissione.getUoCompetenza(), ".", "");
		String uoSpesaPerFlusso = Utility.replace(ordineMissione.getUoSpesa(), ".", "");
		String uoRichPerFlusso = Utility.replace(ordineMissione.getUoRich(), ".", "");
		
		String userNameFirmatario = accountService.getDirector(uoRichPerFlusso);

		Uo uoDatiSpesa = uoService.recuperoUo(uoSpesaPerFlusso);
		String userNameFirmatarioSpesa = null;
		if (uoDatiSpesa != null && uoDatiSpesa.getFirmaSpesa() != null && uoDatiSpesa.getFirmaSpesa().equals("N")){
			if (uoCompetenzaPerFlusso != null){
				userNameFirmatarioSpesa = accountService.getDirector(uoCompetenzaPerFlusso);
			} else {
				userNameFirmatarioSpesa = userNameFirmatario;
			}
		} else {
			userNameFirmatarioSpesa = accountService.getDirector(uoSpesaPerFlusso);
		}
		

//		String nodeRefFirmatario = missioniCMISService.recuperoNodeRefUtente(userNameFirmatario);

		GregorianCalendar dataScadenzaFlusso = new GregorianCalendar();
		dataScadenzaFlusso.setTime(DateUtils.getCurrentTime());
		dataScadenzaFlusso.add(Calendar.DATE, 14);
		dataScadenzaFlusso.add(Calendar.MONTH, 1);


		cmisOrdineMissione.setAnno(ordineMissione.getAnno().toString());
		cmisOrdineMissione.setNumero(ordineMissione.getNumero().toString());
		cmisOrdineMissione.setAnticipo(ordineMissione.getRichiestaAnticipo().equals("S") ? "true" : "false");
		cmisOrdineMissione.setAutoPropriaFlag(ordineMissione.getUtilizzoAutoPropria().equals("S") ? "true" : "false");
		cmisOrdineMissione.setCapitolo(ordineMissione.getVoce());
		cmisOrdineMissione.setDescrizioneCapitolo(voce == null ? "" : voce.getDs_elemento_voce());
		cmisOrdineMissione.setDescrizioneGae(gae == null ? "" : gae.getDs_linea_attivita());
		cmisOrdineMissione.setDescrizioneImpegno(descrImpegno);
		cmisOrdineMissione.setDescrizioneModulo(progetto == null ? "" : progetto.getDs_progetto());
		cmisOrdineMissione.setDescrizioneUoOrdine(uoRich == null ? "" : uoRich.getDs_unita_organizzativa());
		cmisOrdineMissione.setDescrizioneUoSpesa(uoSpesa == null ? "" : uoSpesa.getDs_unita_organizzativa());
		cmisOrdineMissione.setDescrizioneUoCompetenza(uoCompetenza == null ? "" : uoCompetenza.getDs_unita_organizzativa());
		cmisOrdineMissione.setDisponibilita(dispImpegno);
		cmisOrdineMissione.setGae(gae == null ? "" : gae.getCd_linea_attivita());
		cmisOrdineMissione.setImpegnoAnnoCompetenza(ordineMissione.getEsercizioObbligazione() == null ? null : new Long(ordineMissione.getEsercizioObbligazione()));
		cmisOrdineMissione.setImpegnoAnnoResiduo(ordineMissione.getEsercizioOriginaleObbligazione() == null ? null : new Long(ordineMissione.getEsercizioOriginaleObbligazione()));
		cmisOrdineMissione.setImpegnoNumero(ordineMissione.getPgObbligazione());
		cmisOrdineMissione.setImportoMissione(ordineMissione.getImportoPresunto() == null ? null : ordineMissione.getImportoPresunto());
		cmisOrdineMissione.setModulo(progetto == null ? "" : progetto.getCd_progetto());
		cmisOrdineMissione.setNoleggioFlag(ordineMissione.getUtilizzoAutoNoleggio().equals("S") ? "true" : "false");
		cmisOrdineMissione.setNote(ordineMissione.getNote() == null ? "" : ordineMissione.getNote());
		cmisOrdineMissione.setOggetto(ordineMissione.getOggetto());
		cmisOrdineMissione.setPriorita(ordineMissione.getPriorita());
		cmisOrdineMissione.setTaxiFlag(ordineMissione.getUtilizzoTaxi().equals("S") ? "true" : "false");
		cmisOrdineMissione.setUoOrdine(uoRichPerFlusso);
		cmisOrdineMissione.setUoSpesa(uoSpesaPerFlusso);
		cmisOrdineMissione.setUoCompetenza(uoCompetenzaPerFlusso == null ? "" : uoCompetenzaPerFlusso);
		cmisOrdineMissione.setUserNameFirmatarioSpesa(userNameFirmatarioSpesa);
		cmisOrdineMissione.setUserNamePrimoFirmatario(userNameFirmatario);
		cmisOrdineMissione.setUserNameResponsabileModulo("");
		cmisOrdineMissione.setUsernameRichiedente(username);
		cmisOrdineMissione.setUsernameUtenteOrdine(ordineMissione.getUid());
		cmisOrdineMissione.setValidazioneModulo("false");
		cmisOrdineMissione.setValidazioneSpesa(impostaValidazioneSpesa(userNameFirmatario, userNameFirmatarioSpesa));
		cmisOrdineMissione.setWfDescription("Ordine di Missione n. "+ordineMissione.getNumero()+" di "+account.getCognome() + " "+account.getNome());
		cmisOrdineMissione.setWfDueDate(DateUtils.getDateAsString(dataScadenzaFlusso.getTime(), DateUtils.PATTERN_DATE_FOR_DOCUMENTALE));
		cmisOrdineMissione.setDestinazione(ordineMissione.getDestinazione());
		cmisOrdineMissione.setMissioneEsteraFlag(ordineMissione.getTipoMissione().equals("E") ? "true" : "false");
		cmisOrdineMissione.setDataInizioMissione(DateUtils.getDateAsString(ordineMissione.getDataInizioMissione(), DateUtils.PATTERN_DATETIME_NO_SEC_FOR_DOCUMENTALE));
		cmisOrdineMissione.setDataFineMissione(DateUtils.getDateAsString(ordineMissione.getDataFineMissione(), DateUtils.PATTERN_DATETIME_NO_SEC_FOR_DOCUMENTALE));

		return cmisOrdineMissione;
	}
	
	private String impostaValidazioneSpesa(String userNameFirmatario, String userNameFirmatarioSpesa){
		if (userNameFirmatario != null && userNameFirmatarioSpesa != null && userNameFirmatario.equals(userNameFirmatarioSpesa)){
			return "false";
		}
		return "true";
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
	public Document salvaStampaOrdineMissioneSuCMIS(Principal principal, byte[] stampa, OrdineMissione ordineMissione) throws ComponentException {
		CMISOrdineMissione cmisOrdineMissione = create(principal, ordineMissione);
		return salvaStampaOrdineMissioneSuCMIS(principal, stampa, ordineMissione, cmisOrdineMissione);
	}
	
	public CmisPath createFolderOrdineMissione(OrdineMissione ordineMissione){
		CmisPath cmisPath = missioniCMISService.getBasePath();
		cmisPath = missioniCMISService.createFolderIfNotPresent(cmisPath, ordineMissione.getUoRich());
		cmisPath = missioniCMISService.createFolderIfNotPresent(cmisPath, "Ordini di Missione");
		cmisPath = missioniCMISService.createFolderIfNotPresent(cmisPath, "Anno "+ordineMissione.getAnno());
		cmisPath = createLastFolderIfNotPresent(cmisPath, ordineMissione);
		return cmisPath;
	}

	public CmisPath createLastFolderIfNotPresent(CmisPath cmisPath, OrdineMissione ordineMissione){
		Map<String, Object> metadataProperties = new HashMap<String, Object>();
		String name = ordineMissione.constructCMISNomeFile();
		String folderName = name;
		folderName = missioniCMISService.sanitizeFolderName(folderName);
		metadataProperties.put(PropertyIds.OBJECT_TYPE_ID, OrdineMissione.CMIS_PROPERTY_MAIN);
		metadataProperties.put(MissioniCMISService.PROPERTY_DESCRIPTION, missioniCMISService.sanitizeFilename(name));
		metadataProperties.put(PropertyIds.NAME, missioniCMISService.sanitizeFilename(ordineMissione.constructCMISNomeFile()));
		metadataProperties.put(MissioniCMISService.PROPERTY_TITLE, missioniCMISService.sanitizeFilename(name));
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_NAME_NUMERO, ordineMissione.getNumero());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_NAME_ANNO, ordineMissione.getAnno());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_NAME_ID, ordineMissione.getId());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_NAME_UID, ordineMissione.getUid());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_NAME_MODULO, ordineMissione.getModulo());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_NAME_OGGETTO, ordineMissione.getOggetto());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_NAME_DESTINAZIONE, ordineMissione.getDestinazione());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_NAME_NOTE, ordineMissione.getNote());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_NAME_DATA_INIZIO, ordineMissione.getDataInizioMissione());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_NAME_DATA_FINE, ordineMissione.getDataFineMissione());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_NAME_DATA_INSERIMENTO, ordineMissione.getDataInserimento());
		List<String> aspectsToAdd = new ArrayList<String>();
		aspectsToAdd.add(MissioniCMISService.ASPECT_TITLED);
		cmisPath = missioniCMISService.createFolderIfNotPresent(cmisPath, metadataProperties, aspectsToAdd, folderName);
		return cmisPath;
	}
	
	
	private Document salvaStampaOrdineMissioneSuCMIS(Principal principal,
			byte[] stampa, OrdineMissione ordineMissione,
			CMISOrdineMissione cmisOrdineMissione) {
		InputStream streamStampa = new ByteArrayInputStream(stampa);
		CmisPath cmisPath = createFolderOrdineMissione(ordineMissione);
		Map<String, Object> metadataProperties = createMetadataForFileOrdineMissione(principal.getName(), cmisOrdineMissione);
		try{
			Document node = missioniCMISService.restoreSimpleDocument(
					metadataProperties,
					streamStampa,
					MimeTypes.PDF.mimetype(),
					ordineMissione.getFileName(), 
					cmisPath);
			missioniCMISService.addAspect(node, CMISOrdineMissioneAspect.ORDINE_MISSIONE_ATTACHMENT_ORDINE.value());
			missioniCMISService.makeVersionable(node);
			return node;
		} catch (Exception e) {
			if (e.getCause() instanceof CmisConstraintException)
				throw new AwesomeException(CodiciErrore.ERRGEN, "CMIS - File ["+ordineMissione.getFileName()+"] già presente o non completo di tutte le proprietà obbligatorie. Inserimento non possibile!");
			throw new AwesomeException(CodiciErrore.ERRGEN, "CMIS - Errore nella registrazione del file XML sul Documentale (" + Utility.getMessageException(e) + ")");
		}
	}

	private String recuperoUidDirettoreUo(String codiceUo){
		Uo uo = uoService.recuperoUo(codiceUo);
		return recuperoUidDirettoreUo(codiceUo, uo);
	}

	private String recuperoUidDirettoreUo(String codiceUo, Uo uo) {
		if (uo != null && uo.getCodiceUo() != null && uo.getCodiceUo().equals(codiceUo)){
			return uo.getUidDirettore();
		}
		return null;
	}

	public Map<String, Object> createMetadataForFileOrdineMissione(String currentLogin, CMISOrdineMissione cmisOrdineMissione){
		Map<String, Object> metadataProperties = new HashMap<String, Object>();
		metadataProperties.put(PropertyIds.OBJECT_TYPE_ID, OrdineMissione.CMIS_PROPERTY_ATTACHMENT_DOCUMENT);
		metadataProperties.put(MissioniCMISService.PROPERTY_DESCRIPTION, missioniCMISService.sanitizeFilename("Ordine Missione - anno "+cmisOrdineMissione.getAnno()+" numero "+cmisOrdineMissione.getNumero()));
		metadataProperties.put(MissioniCMISService.PROPERTY_TITLE, missioniCMISService.sanitizeFilename("Ordine di Missione"));
		metadataProperties.put(MissioniCMISService.PROPERTY_AUTHOR, currentLogin);
		metadataProperties.put(PROPERTY_TIPOLOGIA_DOC, OrdineMissione.CMIS_PROPERTY_NAME_DOC_ORDINE);
		metadataProperties.put(PROPERTY_TIPOLOGIA_DOC_MISSIONI, OrdineMissione.CMIS_PROPERTY_NAME_TIPODOC_ORDINE);

		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_ANNO_IMPEGNO_COMP, cmisOrdineMissione.getImpegnoAnnoCompetenza());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_ANNO_IMPEGNO_RES, cmisOrdineMissione.getImpegnoAnnoResiduo());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_ANTICIPO, cmisOrdineMissione.getAnticipo().equals("true"));
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_AUTO_PROPRIA, cmisOrdineMissione.getAutoPropriaFlag().equals("true"));
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_CAPITOLO, cmisOrdineMissione.getCapitolo());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_DESCRIZIONE, cmisOrdineMissione.getOggetto());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_DESCRIZIONE_CAPITOLO, cmisOrdineMissione.getDescrizioneCapitolo());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_DESCRIZIONE_GAE, cmisOrdineMissione.getDescrizioneGae());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_DESCRIZIONE_IMPEGNO, cmisOrdineMissione.getDescrizioneImpegno());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_DESCRIZIONE_MODULO, cmisOrdineMissione.getDescrizioneModulo());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_DESCRIZIONE_UO_ORDINE, cmisOrdineMissione.getDescrizioneUoOrdine());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_DESCRIZIONE_UO_SPESA, cmisOrdineMissione.getDescrizioneUoSpesa());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_DISPONIBILITA_IMPEGNO, cmisOrdineMissione.getDisponibilita());
//		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_DUE_DATE, cmisOrdineMissione.getWfDueDate());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_GAE, cmisOrdineMissione.getGae());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_IMPORTO_MISSIONE, cmisOrdineMissione.getImportoMissione());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_MODULO, cmisOrdineMissione.getModulo());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_NOLEGGIO, cmisOrdineMissione.getNoleggioFlag().equals("true"));
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_NOTE, cmisOrdineMissione.getNote());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_NUMERO_IMPEGNO, cmisOrdineMissione.getImpegnoNumero());
//		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_PRIORITY, cmisOrdineMissione.getPriorita());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_TAXI, cmisOrdineMissione.getTaxiFlag().equals("true"));
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_DESTINAZIONE, cmisOrdineMissione.getDestinazione());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_ESTERA_FLAG, cmisOrdineMissione.getMissioneEsteraFlag().equals("true"));
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_DATA_INIZIO_MISSIONE, cmisOrdineMissione.getDataInizioMissione());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_DATA_FINE_MISSIONE, cmisOrdineMissione.getDataFineMissione());
		
		
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_UO_ORDINE, cmisOrdineMissione.getUoOrdine());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_UO_SPESA, cmisOrdineMissione.getUoSpesa());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_USERNAME_FIRMA_SPESA, cmisOrdineMissione.getUserNameFirmatarioSpesa());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_USERNAME_FIRMA_UO, cmisOrdineMissione.getUserNamePrimoFirmatario());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_USERNAME_ORDINE, cmisOrdineMissione.getUsernameUtenteOrdine());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_USERNAME_RESPONSABILE_MODULO, "");
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_USERNAME_RICHIEDENTE, cmisOrdineMissione.getUsernameRichiedente());
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_VALIDAZIONE_MODULO, false);
		metadataProperties.put(OrdineMissione.CMIS_PROPERTY_FLOW_VALIDAZIONE_SPESA, cmisOrdineMissione.getValidazioneSpesa().equals("true"));
		return metadataProperties;
	}
	
	public void avviaFlusso(Principal principal, OrdineMissione ordineMissione) throws ComponentException {
		String username = principal.getName();
		byte[] stampa = printOrdineMissioneService.printOrdineMissione(ordineMissione, username);
		CMISOrdineMissione cmisOrdineMissione = create(principal, ordineMissione);
		Document documento = salvaStampaOrdineMissioneSuCMIS(principal, stampa, ordineMissione, cmisOrdineMissione);
		OrdineMissioneAnticipo anticipo = ordineMissioneAnticipoService.getAnticipo(principal, new Long(ordineMissione.getId().toString()));
//		if (anticipo != null){
//			ordineMissioneAnticipoService.updateAnticipo(principal, anticipo, true);
//			ordineMissione.setRichiestaAnticipo("S");
//		} else {
//			ordineMissione.setRichiestaAnticipo("N");
//		}
		OrdineMissioneAutoPropria autoPropria = ordineMissioneAutoPropriaService.getAutoPropria(principal, new Long(ordineMissione.getId().toString()));
//		if (autoPropria != null){
//			ordineMissione.setUtilizzoAutoPropria("S");
//		} else {
//			ordineMissione.setUtilizzoAutoPropria("N");
//		}
		Document documentoAnticipo = null;
		if (anticipo != null){
			anticipo.setOrdineMissione(ordineMissione);
			documentoAnticipo = ordineMissioneAnticipoService.creaDocumentoAnticipo(username, anticipo);
		}
		
		Document documentoAutoPropria = null;
		if (autoPropria != null){
			autoPropria.setOrdineMissione(ordineMissione);
			documentoAutoPropria = ordineMissioneAutoPropriaService.creaDocumentoRichiestaAutoPropria(username, autoPropria);
		}

		if (ordineMissione.isStatoNonInviatoAlFlusso()){
			String nodeRefFirmatario = missioniCMISService.recuperoNodeRefUtente(cmisOrdineMissione.getUserNamePrimoFirmatario());

			StringWriter stringWriter = new StringWriter();
			JsonFactory jsonFactory = new JsonFactory();
			ObjectMapper mapper = new ObjectMapper(jsonFactory); 
			try {
				 JsonGenerator jGenerator = jsonFactory.createJsonGenerator(stringWriter);
				 jGenerator.writeStartObject();
				 jGenerator.writeStringField("assoc_bpm_assignee_added" , nodeRefFirmatario);
				 jGenerator.writeStringField("assoc_bpm_assignee_removed" , "");
				 StringBuffer nodeRefs = new StringBuffer();
				 aggiungiDocumento(documento, nodeRefs);
				 aggiungiDocumento(documentoAnticipo, nodeRefs);
				 aggiungiDocumento(documentoAutoPropria, nodeRefs);
				 
				 jGenerator.writeStringField("assoc_packageItems_added" , nodeRefs.toString());
				 jGenerator.writeStringField("assoc_packageItems_removed" , "");
				 jGenerator.writeStringField("prop_bpm_comment" , "");
				 jGenerator.writeStringField("prop_cnrmissioni_descrizioneOrdine" , cmisOrdineMissione.getOggetto());
				 jGenerator.writeStringField("prop_cnrmissioni_note" , cmisOrdineMissione.getNote());
				 jGenerator.writeStringField("prop_bpm_workflowDescription" , cmisOrdineMissione.getWfDescription());
				 jGenerator.writeStringField("prop_bpm_sendEMailNotifications" , "false");
				 jGenerator.writeStringField("prop_bpm_workflowDueDate" , cmisOrdineMissione.getWfDueDate());
				 jGenerator.writeStringField("prop_bpm_percentComplete" , "0");
				 jGenerator.writeStringField("prop_bpm_status" , "Not Yet Started");
				 jGenerator.writeStringField("prop_bpm_workflowPriority" , cmisOrdineMissione.getPriorita());
				 jGenerator.writeStringField("prop_wfcnr_groupName" , "GENERICO");
				 jGenerator.writeStringField("prop_wfcnr_wfCounterIndex" , "");
				 jGenerator.writeStringField("prop_wfcnr_wfCounterId" , "");
				 jGenerator.writeStringField("prop_wfcnr_wfCounterAnno" , "");
				 jGenerator.writeStringField("prop_cnrmissioni_validazioneSpesaFlag" , cmisOrdineMissione.getValidazioneSpesa());
				 jGenerator.writeStringField("prop_cnrmissioni_missioneConAnticipoFlag" , cmisOrdineMissione.getAnticipo());
				 jGenerator.writeStringField("prop_cnrmissioni_validazioneModuloFlag" , "false");
				 jGenerator.writeStringField("prop_cnrmissioni_userNameUtenteOrdineMissione" , cmisOrdineMissione.getUsernameUtenteOrdine());
				 jGenerator.writeStringField("prop_cnrmissioni_userNameRichiedente" , cmisOrdineMissione.getUsernameRichiedente());
				 jGenerator.writeStringField("prop_cnrmissioni_userNameResponsabileModulo" , cmisOrdineMissione.getUserNameResponsabileModulo());
				 jGenerator.writeStringField("prop_cnrmissioni_userNamePrimoFirmatario" , cmisOrdineMissione.getUserNamePrimoFirmatario());
				 jGenerator.writeStringField("prop_cnrmissioni_userNameFirmatarioSpesa" , cmisOrdineMissione.getUserNameFirmatarioSpesa());
				 jGenerator.writeStringField("prop_cnrmissioni_uoOrdine" , cmisOrdineMissione.getUoOrdine());
				 jGenerator.writeStringField("prop_cnrmissioni_descrizioneUoOrdine" , cmisOrdineMissione.getDescrizioneUoOrdine());
				 jGenerator.writeStringField("prop_cnrmissioni_uoSpesa" , cmisOrdineMissione.getUoSpesa());
				 jGenerator.writeStringField("prop_cnrmissioni_descrizioneUoSpesa" , cmisOrdineMissione.getDescrizioneUoSpesa());
				 jGenerator.writeStringField("prop_cnrmissioni_uoCompetenza" , cmisOrdineMissione.getUoCompetenza());
				 jGenerator.writeStringField("prop_cnrmissioni_descrizioneUoCompetenza" , cmisOrdineMissione.getDescrizioneUoCompetenza());
				 jGenerator.writeStringField("prop_cnrmissioni_autoPropriaFlag" , cmisOrdineMissione.getAutoPropriaFlag());
				 jGenerator.writeStringField("prop_cnrmissioni_noleggioFlag" , cmisOrdineMissione.getNoleggioFlag());
				 jGenerator.writeStringField("prop_cnrmissioni_taxiFlag" , cmisOrdineMissione.getTaxiFlag());
				 jGenerator.writeStringField("prop_cnrmissioni_capitolo" , cmisOrdineMissione.getCapitolo());
				 jGenerator.writeStringField("prop_cnrmissioni_descrizioneCapitolo" , cmisOrdineMissione.getDescrizioneCapitolo());
				 jGenerator.writeStringField("prop_cnrmissioni_modulo" , cmisOrdineMissione.getModulo());
				 jGenerator.writeStringField("prop_cnrmissioni_descrizioneModulo" , cmisOrdineMissione.getDescrizioneModulo());
				 jGenerator.writeStringField("prop_cnrmissioni_gae" , cmisOrdineMissione.getGae());
				 jGenerator.writeStringField("prop_cnrmissioni_descrizioneGae" , cmisOrdineMissione.getDescrizioneGae());
				 jGenerator.writeStringField("prop_cnrmissioni_impegnoAnnoResiduo" , cmisOrdineMissione.getImpegnoAnnoResiduo() == null ? "": cmisOrdineMissione.getImpegnoAnnoResiduo().toString());
				 jGenerator.writeStringField("prop_cnrmissioni_impegnoAnnoCompetenza" , cmisOrdineMissione.getImpegnoAnnoCompetenza() == null ? "": cmisOrdineMissione.getImpegnoAnnoCompetenza().toString());
				 jGenerator.writeStringField("prop_cnrmissioni_impegnoNumero" , cmisOrdineMissione.getImpegnoNumero() == null ? "": cmisOrdineMissione.getImpegnoNumero().toString());
				 jGenerator.writeStringField("prop_cnrmissioni_descrizioneImpegno" , cmisOrdineMissione.getDescrizioneImpegno());
				 jGenerator.writeStringField("prop_cnrmissioni_importoMissione" , cmisOrdineMissione.getImportoMissione() == null ? "": cmisOrdineMissione.getImportoMissione().toString());
				 jGenerator.writeStringField("prop_cnrmissioni_disponibilita" , cmisOrdineMissione.getDisponibilita() == null ? "": cmisOrdineMissione.getDisponibilita().toString());
				 jGenerator.writeStringField("prop_cnrmissioni_missioneEsteraFlag" , cmisOrdineMissione.getMissioneEsteraFlag());
				 jGenerator.writeStringField("prop_cnrmissioni_destinazione" , cmisOrdineMissione.getDestinazione());
				 jGenerator.writeStringField("prop_cnrmissioni_dataInizioMissione" , cmisOrdineMissione.getDataInizioMissione());
				 jGenerator.writeStringField("prop_cnrmissioni_dataFineMissione" , cmisOrdineMissione.getDataFineMissione());
				jGenerator.writeEndObject();
				jGenerator.close();
			} catch (IOException e) {
				throw new AwesomeException(CodiciErrore.ERRGEN, "Errore in fase avvio flusso documentale. Errore: "+e);
			}
			
			try {
				Response responsePost = missioniCMISService.startFlowOrdineMissione(stringWriter);
				TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
				HashMap<String,Object> mapRichiedente = mapper.readValue(responsePost.getStream(), typeRef); 
				String idFlusso = null;
				
				String text = mapRichiedente.get("persistedObject").toString();
				String patternString1 = "id=(activiti\\$[0-9]+)";

				Pattern pattern = Pattern.compile(patternString1);
				Matcher matcher = pattern.matcher(text);
				if (matcher.find())
					idFlusso = matcher.group(1);
				ordineMissione.setIdFlusso(idFlusso);
	        	ordineMissione.setStatoFlusso(Costanti.STATO_INVIATO_FLUSSO);
				if (anticipo != null){
					anticipo.setIdFlusso(idFlusso);
				}
			} catch (AwesomeException e) {
				throw e;
			} catch (Exception e) {
				throw new AwesomeException(CodiciErrore.ERRGEN, "Errore in fase avvio flusso documentale. Errore: " + Utility.getMessageException(e) + ".");
			}
		} else {
			if (ordineMissione.isStatoInviatoAlFlusso() && !StringUtils.isEmpty(ordineMissione.getIdFlusso())){
				StringBuffer nodeRefs = new StringBuffer();
				aggiungiDocumento(documentoAutoPropria, nodeRefs);
				avanzaFlusso(ordineMissione, nodeRefs);
			} else {
				throw new AwesomeException(CodiciErrore.ERRGEN, "Anomalia nei dati. Stato di invio al flusso non valido.");
			}
		}
	}
	private void aggiungiDocumento(Document documentoAnticipo,
			StringBuffer nodeRefs) {
		if (documentoAnticipo != null){
			if (nodeRefs.length() > 0){
				 nodeRefs.append(",");
			}
			 nodeRefs.append((String)documentoAnticipo.getPropertyValue("alfcmis:nodeRef"));
		 }
	}

	public ContentStream getContentStreamOrdineMissione(OrdineMissione ordineMissione) throws Exception{
		String id = getNodeRefOrdineMissione(ordineMissione);
		if (id != null){
			return missioniCMISService.recuperoContentFileFromObjectID(id);
		}
		return null;
	}
	
	public String getNodeRefOrdineMissione(OrdineMissione ordineMissione) throws ComponentException{
		List<String> ids = new ArrayList<String>();
		Folder node = recuperoFolderOrdineMissione(ordineMissione);
		if (node == null){
			throw new AwesomeException(CodiciErrore.ERRGEN, "Non esistono documenti collegati all'Ordine di Missione. ID Ordine di Missione:"+ordineMissione.getId()+", Anno:"+ordineMissione.getAnno()+", Numero:"+ordineMissione.getNumero());
		}
		String folder = (String) node.getPropertyValue(PropertyIds.OBJECT_ID); 
		StringBuffer query = new StringBuffer("select doc.cmis:objectId from cmis:document doc ");
		query.append(" join missioni_ordine_attachment:ordine ordine on doc.cmis:objectId = ordine.cmis:objectId ");
		query.append(" where IN_FOLDER(doc, '").append(folder).append("')");
		ItemIterable<QueryResult> results = missioniCMISService.search(query);
		if (results.getTotalNumItems() == 0)
			return null;
		else if (results.getTotalNumItems() > 1){
			throw new AwesomeException(CodiciErrore.ERRGEN, "Errore di sistema, esistono sul documentale piu' files ordini di missione aventi l'ID :"+ ordineMissione.getId()+", Anno:"+ordineMissione.getAnno()+", Numero:"+ordineMissione.getNumero());
		} else {
			for (QueryResult nodeFile : results) {
				String file = nodeFile.getPropertyValueById(PropertyIds.OBJECT_ID);
				return file;
			}
		}
		return null;
	}
	
	public Folder recuperoFolderOrdineMissione(OrdineMissione ordineMissione)throws ComponentException{
		StringBuffer query = new StringBuffer("select ord.cmis:objectId from missioni_ordine:main as ord ");
		query.append(" where ord.missioni_ordine:id = ").append(ordineMissione.getId());

		ItemIterable<QueryResult> resultsFolder = missioniCMISService.search(query);
		if (resultsFolder.getTotalNumItems() == 0)
			return null;
		else if (resultsFolder.getTotalNumItems() > 1){
			throw new AwesomeException(CodiciErrore.ERRGEN, "Errore di sistema, esistono sul documentale piu' ordini di missione aventi l'ID :"+ ordineMissione.getId()+", Anno:"+ordineMissione.getAnno()+", Numero:"+ordineMissione.getNumero());
		} else {
			for (QueryResult queryResult : resultsFolder) {
				return (Folder) missioniCMISService.getNodeByNodeRef((String) queryResult.getPropertyValueById(PropertyIds.OBJECT_ID));
			}
		}
		return null;
	}
	
    @Transactional(readOnly = true)
    public void uploadAllegatoOrdineMissione(Principal principal, OrdineMissione ordineMissione, InputStream uploadedAllegatoInputStream) throws AwesomeException, ComponentException {
    	String fileName = "Allegato1.pdf";
    	CmisPath cmisPath = createFolderOrdineMissione(ordineMissione);
    	Map<String, Object> metadataProperties = new HashMap<String, Object>();
    	try{
    		Document node = missioniCMISService.restoreSimpleDocument(
    				metadataProperties,
    				uploadedAllegatoInputStream,
    				MimeTypes.PDF.mimetype(),
    				fileName, 
    				cmisPath);
    		missioniCMISService.addAspect(node, CMISOrdineMissioneAspect.ORDINE_MISSIONE_ATTACHMENT_ALLEGATI.value());
    		missioniCMISService.makeVersionable(node);
    	} catch (Exception e) {
    		if (e.getCause() instanceof CmisConstraintException)
    			throw new AwesomeException("CMIS - File ["+fileName+"] già presente o non completo di tutte le proprietà obbligatorie. Inserimento non possibile!");
    		throw new AwesomeException("CMIS - Errore nella registrazione del file sul Documentale (" + Utility.getMessageException(e) + ")");
    	}
    }

	private void avanzaFlusso(OrdineMissione ordineMissione, StringBuffer nodeRefs) throws AwesomeException {
    	avanzaFlusso(ordineMissione, nodeRefs, FlowResubmitType.RESTART_FLOW);
    }

    public void annullaFlusso(OrdineMissione ordineMissione) throws AwesomeException {
    	avanzaFlusso(ordineMissione, null, FlowResubmitType.ABORT_FLOW);
		ordineMissione.setStatoFlusso(Costanti.STATO_ANNULLATO);
    }

    private void avanzaFlusso(OrdineMissione ordineMissione, StringBuffer nodeRefs, FlowResubmitType step) throws AwesomeException {
    	try {
    		restartFlowOrdineMissione(ordineMissione, nodeRefs, step);
    	} catch (AwesomeException e) {
    		throw e;
    	}
    }
	public ResultFlows getFlowsOrdineMissione(String idFlusso)throws AwesomeException{
		String fieldStato = "wfcnr:statoFlusso"; 
		String fieldTaskId = "wfcnr:taskId"; 
		String fieldComment = "cnrmissioni:commento"; 
		QueryResult result = recuperoFlusso(idFlusso);
		if (result != null){
			ResultFlows flows = new ResultFlows();
			flows.setState((String) result.getPropertyValueById(fieldStato));
			flows.setComment((String) result.getPropertyValueById(fieldComment));
			flows.setTaskId((String) result.getPropertyValueById(fieldTaskId));
			return flows;
		}
		return null;
	}

	public void restartFlowOrdineMissione(OrdineMissione ordineMissione, StringBuffer nodeRefs, FlowResubmitType step) throws AwesomeException {
    	ResultFlows result = getFlowsOrdineMissione(ordineMissione.getIdFlusso());
    	if (!StringUtils.isEmpty(result.getTaskId())){
    		if (step.equals(FlowResubmitType.ABORT_FLOW.operation())){
        		StringWriter stringWriter = createJsonForAbortFlowOrdineMissione();
        		missioniCMISService.abortFlowOrdineMissione(stringWriter, result);
    		} else {
    			StringWriter stringWriter = createJsonForRestartFlowOrdineMissione(nodeRefs);
        		missioniCMISService.restartFlowOrdineMissione(stringWriter, result);
    		}
    	} else {
    		throw new AwesomeException(CodiciErrore.ERRGEN, "Anomalia nei dati. Task Id del flusso non trovato.");
    	}
    }

	public QueryResult recuperoFlusso(String idFlusso)throws AwesomeException{
		StringBuffer query = new StringBuffer("select parametriFlusso.*, flussoMissioni.* from cmis:document as t ");
		query.append( "inner join wfcnr:parametriFlusso as parametriFlusso on t.cmis:objectId = parametriFlusso.cmis:objectId ");
		query.append(" inner join cnrmissioni:parametriFlussoMissioni as flussoMissioni on t.cmis:objectId = flussoMissioni.cmis:objectId ");
		query.append(" where parametriFlusso.wfcnr:wfInstanceId = '").append(idFlusso).append("'");

		ItemIterable<QueryResult> resultsFolder = missioniCMISService.search(query);
		if (resultsFolder.getTotalNumItems() == 0){
			return null;
		} else {
			for (QueryResult queryResult : resultsFolder) {
				return queryResult;
			}
		}
		return null;
	}

	private StringWriter createJsonForRestartFlowOrdineMissione(StringBuffer nodeRefs) {
		StringWriter stringWriter = new StringWriter();
		try {
			JsonFactory jsonFactory = new JsonFactory();
			JsonGenerator jGenerator = jsonFactory.createJsonGenerator(stringWriter);
			jGenerator.writeStartObject();
			if (nodeRefs != null){
				jGenerator.writeStringField("assoc_packageItems_added" , nodeRefs.toString());
			}
			jGenerator.writeStringField("prop_bpm_comment" , "AVANZAMENTO");
			jGenerator.writeStringField("prop_wfcnr_reviewOutcome" , FlowResubmitType.RESTART_FLOW.operation());
			jGenerator.writeStringField("prop_transitions" , "Next");
			jGenerator.writeEndObject();
			jGenerator.close();
		} catch (IOException e) {
			throw new AwesomeException(CodiciErrore.ERRGEN, "Errore in fase di scrittura dei file del flusso per l'avanzamento del documentale. Errore: "+e);
		}
		return stringWriter;
	}

	private StringWriter createJsonForAbortFlowOrdineMissione() {
		StringWriter stringWriter = new StringWriter();
		try {
			JsonFactory jsonFactory = new JsonFactory();
			JsonGenerator jGenerator = jsonFactory.createJsonGenerator(stringWriter);
			jGenerator.writeStartObject();
			jGenerator.writeStringField("bpm_comment" , "AVANZAMENTO");
			jGenerator.writeStringField("wfcnr_reviewOutcome" , FlowResubmitType.ABORT_FLOW.operation());
			jGenerator.writeEndObject();
			jGenerator.close();
		} catch (IOException e) {
			throw new AwesomeException(CodiciErrore.ERRGEN, "Errore in fase di scrittura dei file del flusso per l'avanzamento del documentale. Errore: "+e);
		}
		return stringWriter;
	}
	public Map<String, Object> createMetadataForFileOrdineMissioneAnticipo(String currentLogin, OrdineMissioneAnticipo anticipo){
		Map<String, Object> metadataProperties = new HashMap<String, Object>();
		metadataProperties.put(PropertyIds.OBJECT_TYPE_ID, OrdineMissione.CMIS_PROPERTY_ATTACHMENT_DOCUMENT);
		metadataProperties.put(MissioniCMISService.PROPERTY_DESCRIPTION, missioniCMISService.sanitizeFilename("Anticipo per l'Ordine Missione - anno "+anticipo.getOrdineMissione().getAnno()+" numero "+anticipo.getOrdineMissione().getNumero()));
		metadataProperties.put(MissioniCMISService.PROPERTY_TITLE, missioniCMISService.sanitizeFilename("Anticipo Ordine di Missione"));
		metadataProperties.put(MissioniCMISService.PROPERTY_AUTHOR, currentLogin);
		metadataProperties.put(PROPERTY_TIPOLOGIA_DOC, OrdineMissioneAnticipo.CMIS_PROPERTY_NAME_DOC_ANTICIPO);
		metadataProperties.put(PROPERTY_TIPOLOGIA_DOC_MISSIONI, OrdineMissioneAnticipo.CMIS_PROPERTY_NAME_TIPODOC_ANTICIPO);
		return metadataProperties;
	}
	
	public Map<String, Object> createMetadataForFileOrdineMissioneAutoPropria(String currentLogin, OrdineMissioneAutoPropria autoPropria){
		Map<String, Object> metadataProperties = new HashMap<String, Object>();
		metadataProperties.put(PropertyIds.OBJECT_TYPE_ID, OrdineMissione.CMIS_PROPERTY_ATTACHMENT_DOCUMENT);
		metadataProperties.put(MissioniCMISService.PROPERTY_DESCRIPTION, missioniCMISService.sanitizeFilename("Richiesta Uso Auto Propria per l'Ordine Missione - anno "+autoPropria.getOrdineMissione().getAnno()+" numero "+autoPropria.getOrdineMissione().getNumero()));
		metadataProperties.put(MissioniCMISService.PROPERTY_TITLE, missioniCMISService.sanitizeFilename("Richiesta Uso Auto Propria Ordine di Missione"));
		metadataProperties.put(MissioniCMISService.PROPERTY_AUTHOR, currentLogin);
		metadataProperties.put(PROPERTY_TIPOLOGIA_DOC, OrdineMissioneAutoPropria.CMIS_PROPERTY_NAME_DOC_AUTO_PROPRIA);
		metadataProperties.put(PROPERTY_TIPOLOGIA_DOC_MISSIONI, OrdineMissioneAutoPropria.CMIS_PROPERTY_NAME_TIPODOC_AUTO_PROPRIA);
		return metadataProperties;
	}
	
	public Map<String, Object> createMetadataForFileOrdineMissioneAllegati(String currentLogin, OrdineMissioneAutoPropria autoPropria){
		Map<String, Object> metadataProperties = new HashMap<String, Object>();
		metadataProperties.put(PropertyIds.OBJECT_TYPE_ID, OrdineMissione.CMIS_PROPERTY_ATTACHMENT_DOCUMENT);
		metadataProperties.put(MissioniCMISService.PROPERTY_DESCRIPTION, missioniCMISService.sanitizeFilename("Allegato all'Ordine Missione - anno "+autoPropria.getOrdineMissione().getAnno()+" numero "+autoPropria.getOrdineMissione().getNumero()));
		metadataProperties.put(MissioniCMISService.PROPERTY_TITLE, missioniCMISService.sanitizeFilename("Allegato Ordine di Missione"));
		metadataProperties.put(MissioniCMISService.PROPERTY_AUTHOR, currentLogin);
		metadataProperties.put(PROPERTY_TIPOLOGIA_DOC, OrdineMissione.CMIS_PROPERTY_NAME_DOC_ALLEGATO);
		metadataProperties.put(PROPERTY_TIPOLOGIA_DOC_MISSIONI, OrdineMissione.CMIS_PROPERTY_NAME_DOC_ALLEGATO);
		return metadataProperties;
	}
}