package it.cnr.si.missioni.service;

import it.cnr.jada.ejb.session.ComponentException;
import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.domain.custom.persistence.OrdineMissione;
import it.cnr.si.missioni.domain.custom.print.PrintOrdineMissione;
import it.cnr.si.missioni.util.DateUtils;
import it.cnr.si.missioni.util.Utility;
import it.cnr.si.missioni.util.proxy.json.object.Account;
import it.cnr.si.missioni.util.proxy.json.object.Cdr;
import it.cnr.si.missioni.util.proxy.json.object.Gae;
import it.cnr.si.missioni.util.proxy.json.object.Nazione;
import it.cnr.si.missioni.util.proxy.json.object.Progetto;
import it.cnr.si.missioni.util.proxy.json.object.UnitaOrganizzativa;
import it.cnr.si.missioni.util.proxy.json.object.Voce;
import it.cnr.si.missioni.util.proxy.json.service.AccountService;
import it.cnr.si.missioni.util.proxy.json.service.CdrService;
import it.cnr.si.missioni.util.proxy.json.service.GaeService;
import it.cnr.si.missioni.util.proxy.json.service.NazioneService;
import it.cnr.si.missioni.util.proxy.json.service.ProgettoService;
import it.cnr.si.missioni.util.proxy.json.service.UnitaOrganizzativaService;
import it.cnr.si.missioni.util.proxy.json.service.VoceService;

import java.util.Date;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PrintOrdineMissioneService {
    private final Logger log = LoggerFactory.getLogger(PrintOrdineMissioneService.class);

    @Autowired
    private PrintService printService;

    @Inject
    private UnitaOrganizzativaService unitaOrganizzativaService;

    @Inject
    private CdrService cdrService;

    @Inject
    private NazioneService nazioneService;

    @Inject
    private GaeService gaeService;

    @Inject
    private ProgettoService progettoService;

    @Inject
    private AccountService accountService;

    @Inject
    private VoceService voceService;
    
//    private PrintOrdineMissione getPrintOrdineMissione(Principal principal, OrdineMissione ordineMissione) throws AwesomeException, ComponentException {
    private PrintOrdineMissione getPrintOrdineMissione(OrdineMissione ordineMissione, String currentLogin) throws AwesomeException, ComponentException {
		Account account = accountService.loadAccountFromRest(ordineMissione.getUid());
		Progetto progetto = progettoService.loadModulo(ordineMissione.getPgProgetto(), ordineMissione.getAnno(), null);
		Voce voce = voceService.loadVoce(ordineMissione);
		Nazione nazione = nazioneService.loadNazione(ordineMissione);
		Gae gae = gaeService.loadGae(ordineMissione);
    	PrintOrdineMissione printOrdineMissione = new PrintOrdineMissione();
    	printOrdineMissione.setAnno(ordineMissione.getAnno());
    	printOrdineMissione.setCdrRich(caricaCdr(ordineMissione.getCdrRich()));
    	printOrdineMissione.setCdrSpesa(caricaCdr(ordineMissione.getCdrSpesa()));
    	printOrdineMissione.setCdsRich(ordineMissione.getCdsRich());
    	printOrdineMissione.setCdsSpesa(ordineMissione.getCdsSpesa());
    	printOrdineMissione.setCodiceFiscaleRich(account.getCodiceFiscale());
    	printOrdineMissione.setComuneResidenzaRich(ordineMissione.getComuneResidenzaRich());
    	if (account.getDataNascita() != null){
    		Date dataNas = DateUtils.parseDate(account.getDataNascita().substring(0, 10),"yyyy-MM-dd");
    		printOrdineMissione.setDataDiNascitaRich(DateUtils.getDateAsString(dataNas, DateUtils.PATTERN_DATE));
    	}
    	printOrdineMissione.setDataFineMissione(DateUtils.getDateAsString(ordineMissione.getDataFineMissione(), DateUtils.PATTERN_DATETIME_NO_SEC));
    	printOrdineMissione.setDataInizioMissione(DateUtils.getDateAsString(ordineMissione.getDataInizioMissione(), DateUtils.PATTERN_DATETIME_NO_SEC));
    	printOrdineMissione.setDataInserimento(DateUtils.getDateAsString(ordineMissione.getDataInserimento(), DateUtils.PATTERN_DATE));
    	printOrdineMissione.setDatoreLavoroRich(ordineMissione.getDatoreLavoroRich());
    	printOrdineMissione.setDestinazione(ordineMissione.getDestinazione());

    	if (ordineMissione.getDistanzaDallaSede() != null){
        	printOrdineMissione.setDistanzaDallaSede(ordineMissione.getDistanzaDallaSede().toString());
    	} else {
        	printOrdineMissione.setDistanzaDallaSede("");
    	}
    	printOrdineMissione.setDomicilioFiscaleRich(Utility.nvl(ordineMissione.getDomicilioFiscaleRich()));
       	printOrdineMissione.setImportoPresunto(Utility.numberFormat(ordineMissione.getImportoPresunto()));

       	printOrdineMissione.setIndirizzoResidenzaRich(ordineMissione.getIndirizzoResidenzaRich());
    	printOrdineMissione.setLivelloRich(ordineMissione.getLivelloRich() == null ? "" : ordineMissione.getLivelloRich().toString());
    	printOrdineMissione.setLuogoDiNascitaRich(account.getComuneNascita());
    	printOrdineMissione.setMatricolaRich(account.getMatricola());
    	if (progetto != null){
        	printOrdineMissione.setModulo(progetto.getCd_progetto()+" "+progetto.getDs_progetto());
    	} else {
        	printOrdineMissione.setModulo("");
    	}
    	if (ordineMissione.isMissioneEstera()){
        	printOrdineMissione.setNazione(nazione.getDs_nazione());
    	} else {
    		printOrdineMissione.setNazione("");
    	}
		printOrdineMissione.setItaliaEstero(ordineMissione.decodeTipoMissione());
		printOrdineMissione.setTrattamento(ordineMissione.decodeTrattamento());
    	if (voce != null){
        	printOrdineMissione.setVoce(voce.getCd_elemento_voce()+" "+voce.getDs_elemento_voce());
    	} else {
        	printOrdineMissione.setVoce("");
    	}
    	if (gae != null){
        	printOrdineMissione.setGae(gae.getCd_linea_attivita()+" "+gae.getDs_linea_attivita());
    	} else {
        	printOrdineMissione.setGae("");
    	}
    	printOrdineMissione.setCognomeRich(account.getCognome());
    	printOrdineMissione.setNomeRich(account.getNome());
    	printOrdineMissione.setNote(Utility.nvl(ordineMissione.getNote()));
    	printOrdineMissione.setNumero(ordineMissione.getNumero());
    	if (ordineMissione.isMissioneConGiorniDivervi()){
        	printOrdineMissione.setObbligoRientro(ordineMissione.decodeObbligoRientro());
    	} else {
        	printOrdineMissione.setObbligoRientro("");
    	}
    	printOrdineMissione.setUoRich(caricaUo(ordineMissione.getUoRich(), ordineMissione.getAnno()));
    	printOrdineMissione.setUoSpesa(caricaUo(ordineMissione.getUoSpesa(), ordineMissione.getAnno()));
    	printOrdineMissione.setTipoMissione(ordineMissione.getTipoMissione());
    	printOrdineMissione.setQualificaRich(ordineMissione.getQualificaRich() == null ? "" : ordineMissione.getQualificaRich());
    	printOrdineMissione.setPriorita(ordineMissione.decodePriorita());
    	printOrdineMissione.setPartenzaDa(ordineMissione.decodePartenzaDa());
    	if (ordineMissione.getPgObbligazione() != null && ordineMissione.getEsercizioOriginaleObbligazione() != null){
        	printOrdineMissione.setPgObbligazione(ordineMissione.getPgObbligazione().toString());
        	printOrdineMissione.setEsercizioOriginaleObbligazione(ordineMissione.getEsercizioOriginaleObbligazione().toString());
    	} else {
        	printOrdineMissione.setPgObbligazione("");
        	printOrdineMissione.setEsercizioOriginaleObbligazione("");
    	}
    	printOrdineMissione.setOggetto(ordineMissione.getOggetto());
    	printOrdineMissione.setUtilizzoAutoNoleggio(ordineMissione.decodeUtilizzoAutoNoleggio());
    	printOrdineMissione.setUtilizzoTaxi(ordineMissione.decodeUtilizzoTaxi());
    	printOrdineMissione.setNoteUtilizzoTaxiNoleggio(Utility.nvl(ordineMissione.getNoteUtilizzoTaxiNoleggio()));
    	return printOrdineMissione; 
    }

	private String caricaUo(String cdUo, Integer anno) {
    	if (cdUo != null){
    		UnitaOrganizzativa uo = unitaOrganizzativaService.loadUo(cdUo, null, anno);
    		return uo.getCd_unita_organizzativa() + " "+ uo.getDs_unita_organizzativa();
    	}
   		return "";
	}
    
	private String caricaCdr(String cdCdr) {
    	if (cdCdr != null){
    		Cdr cdr = cdrService.loadCdr(cdCdr, null);
    		return cdr.getCd_centro_responsabilita() + " "+ cdr.getDs_cdr();
    	}
   		return "";
	}
	
	public byte[] printOrdineMissione(OrdineMissione ordineMissione, String currentLogin) throws AwesomeException, ComponentException {
		String myJson = createJsonPrintOrdineMissione(ordineMissione, currentLogin);
		return printService.print(myJson, "OrdineMissione.jasper");
	}

	public String createJsonPrintOrdineMissione(OrdineMissione ordineMissione, String currentLogin) throws ComponentException {
		PrintOrdineMissione printOrdineMissione = getPrintOrdineMissione(ordineMissione, currentLogin);
		return printService.createJsonForPrint(printOrdineMissione);
	}

}