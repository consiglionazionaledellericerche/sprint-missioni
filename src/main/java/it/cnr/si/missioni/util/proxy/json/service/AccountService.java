package it.cnr.si.missioni.util.proxy.json.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.cmis.MissioniCMISService;
import it.cnr.si.missioni.service.ConfigService;
import it.cnr.si.missioni.service.ProxyService;
import it.cnr.si.missioni.service.UoService;
import it.cnr.si.missioni.util.CodiciErrore;
import it.cnr.si.missioni.util.Costanti;
import it.cnr.si.missioni.util.Utility;
import it.cnr.si.missioni.util.data.Uo;
import it.cnr.si.missioni.util.data.UoForUsersSpecial;
import it.cnr.si.missioni.util.data.UsersSpecial;
import it.cnr.si.missioni.util.proxy.ResultProxy;
import it.cnr.si.missioni.util.proxy.cache.CallCache;
import it.cnr.si.missioni.util.proxy.json.object.Account;
import it.cnr.si.missioni.util.proxy.json.object.DatiDirettore;

@Service
public class AccountService {
	private transient static final Log logger = LogFactory.getLog(AccountService.class);

	@Autowired
    private ProxyService proxyService;

	@Autowired
    private ConfigService configService;

	@Autowired
    private TerzoService terzoService;

	@Autowired
    private InquadramentoService inquadramentoService;

	@Autowired
    private UoService uoService;

	public UsersSpecial getUoForUsersSpecial(String uid){
		if (configService.getDataUsersSpecial() != null && configService.getDataUsersSpecial().getUsersSpecials() != null ){
			for (Iterator<UsersSpecial> iteratorUsers = configService.getDataUsersSpecial().getUsersSpecials().iterator(); iteratorUsers.hasNext();){
				UsersSpecial user = iteratorUsers.next();
				if (user.getUid() != null && user.getUid().equalsIgnoreCase(uid)){
					return user;
				}
			}
		}
		return null;
	}
	
	public List<UsersSpecial> getUserSpecialForUo(String uo, Boolean isPerValidazione){
		List<UsersSpecial> listaUtenti = new ArrayList<UsersSpecial>();
		if (configService.getDataUsersSpecial() != null && configService.getDataUsersSpecial().getUsersSpecials() != null ){
			for (Iterator<UsersSpecial> iteratorUsers = configService.getDataUsersSpecial().getUsersSpecials().iterator(); iteratorUsers.hasNext();){
				UsersSpecial user = iteratorUsers.next();
				if (isUtenteAbilitatoUo(user.getUoForUsersSpecials(),uo, isPerValidazione)){
					logger.info("User special to be able: "+user.getUid());
					listaUtenti.add(user);
				}
			}
		}
		return listaUtenti;
	}
	
	public Boolean isUserSpecialEnableToValidateOrder(String user, String uo){
		if (uo == null){
			throw new AwesomeException(CodiciErrore.ERRGEN, "UO non indicata.");
		}
		Uo datiUo = uoService.recuperoUoSigla(uo);
		if (datiUo != null && Utility.nvl(datiUo.getOrdineDaValidare(),"N").equals("N")){
			return true;
		}
		UsersSpecial userSpecial = getUoForUsersSpecial(user);
		if (userSpecial != null){
			if (userSpecial.getAll() == null || !userSpecial.getAll().equals("S")){
				if (userSpecial.getUoForUsersSpecials() != null && !userSpecial.getUoForUsersSpecials().isEmpty()){
			    	for (UoForUsersSpecial uoForUsersSpecial : userSpecial.getUoForUsersSpecials()){
			    		if (uo.equals(getUoSigla(uoForUsersSpecial)) && Utility.nvl(uoForUsersSpecial.getOrdine_da_validare()).equals("S")){
			    			return true;
			    		}
			    	}
				}
			} else if (userSpecial.getAll().equals("S")){
				return true;
			}
		}
		return false;
	}
	
	public List<UsersSpecial> getUserSpecialForUoPerValidazione(String uo){
		return getUserSpecialForUo(uo, true);
	}
	
	public Boolean isUtenteAbilitatoUo(List<UoForUsersSpecial> listUo, String uo, Boolean isPerValidazione){
		for (Iterator<UoForUsersSpecial> iteratorUo = listUo.iterator(); iteratorUo.hasNext();){
			UoForUsersSpecial uoForUsersSpecial = iteratorUo.next();
			if (uoForUsersSpecial.getCodice_uo() != null && getUoSigla(uoForUsersSpecial).equals(uo)){
				if (isPerValidazione){
					if (uoForUsersSpecial.getOrdine_da_validare().equals("S")){
						return true;
					} else {
						return false;
					}
				} else {
					return true;
				}
			}
		}
		return false;
	}
	
	public String manageResponseForAccountRest(String body) {
		Account account = getAccount(body);
		if (account != null){
			return getResponseAccount(null, account, Boolean.FALSE);
		}
		return null;
	}

	private String manageResponseForAccountRest(String uid, 
			String body, Boolean loadSpecialUserData) {
		Account account = getAccount(body);
		if (account != null){
			return getResponseAccount(uid, account, loadSpecialUserData);
		}
		return null;
	}

	public String manageResponseForAccountRest(String uid, 
			String body) {
		Account account = getAccount(body);
		if (account != null){
			return getResponseAccount(uid, account, true);
		}
		return null;
	}

	private String getResponseAccount(String uid, Account account, Boolean loadSpecialUserData) {
		UsersSpecial user = null;
		if (loadSpecialUserData){
			user = getUoForUsersSpecial(uid);
		}
		return createResponseForAccountRest(account, user);
	}

	public String createResponseForAccountRest(Account account, UsersSpecial user) {
		if (user != null){
			account.setAllUoForUsersSpecial(user.getAll());
			account.setUoForUsersSpecial(user.getUoForUsersSpecials());
		}
// Da richiamare come servizio REST a parte per il rimborso
//		if (account.getCodiceFiscale() != null){
//			Terzo terzo = terzoService.loadTerzo(account.getCodiceFiscale(), null);
//			if (terzo != null){
//				account.setCdTerzoSigla(terzo.getCd_terzo().toString());
//				account.setInquadramenti(inquadramentoService.loadInquadramento(terzo.getCd_anag()));
//				
//			}
//		}
		return getBodyAccount(account);
	}

	public Account loadAccountFromRest(String currentLogin){
		return loadAccountFromRest(currentLogin, false);
	}
	
	public Account loadAccountFromRest(String currentLogin, Boolean loadSpecialUserData){

		String risposta = getAccount(currentLogin, loadSpecialUserData);
		return getAccount(risposta);
	}

	public String getAccount(String currentLogin, Boolean loadSpecialUserData) {
		ResultProxy result = proxyService.process(HttpMethod.GET, null, Costanti.APP_SIPER, Costanti.REST_ACCOUNT+currentLogin, "proxyURL="+Costanti.REST_ACCOUNT+currentLogin, null);
		String risposta = result.getBody();
		String resp = manageResponseForAccountRest(currentLogin, risposta, loadSpecialUserData);
		if (resp != null){
			return resp;
		}
		return risposta;
	}

	public String getDirector(String uo) {
		CallCache callCache = new CallCache(HttpMethod.GET, null, Costanti.APP_SIPER, Costanti.REST_UO_DIRECTOR, "titCa="+uo+"&userinfo=true&ruolo=resp", null, null);
		ResultProxy result = proxyService.processInCache(callCache);
		String risposta = result.getBody();
		try {
			ObjectMapper mapper = new ObjectMapper();
			DatiDirettore [] lista = mapper.readValue(risposta, DatiDirettore[].class);
			if (lista != null && lista.length > 0){
				return lista[0].getUid();
			} else if (lista == null || lista.length == 0){
				throw new AwesomeException(CodiciErrore.ERRGEN, "Non è stato possibile recuperare il direttore per la uo." + uo);
			}
		} catch (Exception ex) {
			throw new AwesomeException(CodiciErrore.ERRGEN, "Errore nella lettura del file JSON per i Direttori ("+Utility.getMessageException(ex)+").");
		}
		return risposta;
	}

	public Boolean isUserSpecialEnableToFinalizeOrder(String user, String uo){
		if (uo == null){
			throw new AwesomeException(CodiciErrore.ERRGEN, "UO non indicata.");
		}
		UsersSpecial userSpecial = getUoForUsersSpecial(user);
		if (userSpecial.getAll() == null || !userSpecial.getAll().equals("S")){
			if (userSpecial.getUoForUsersSpecials() != null && !userSpecial.getUoForUsersSpecials().isEmpty()){
		    	for (UoForUsersSpecial uoForUsersSpecial : userSpecial.getUoForUsersSpecials()){
		    		if (uo.equals(getUoSigla(uoForUsersSpecial)) && Utility.nvl(uoForUsersSpecial.getRendi_definitivo()).equals("S")){
		    			return true;
		    		}
		    	}
			}
		} else {
			return true;
		}
		return false;
	}
	
	private String getUoSigla(UoForUsersSpecial uo) {
		return uo.getCodice_uo().substring(0,3)+"."+uo.getCodice_uo().substring(3,6);
	}

	public Account getAccount(String risposta) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Account account = mapper.readValue(risposta, Account.class);
			return account;
		} catch (Exception ex) {
			throw new AwesomeException(CodiciErrore.ERRGEN, "Errore nella creazione dell'oggetto JSON dei dati dell'Account dalla REST ("+Utility.getMessageException(ex)+").");
		}
	}

	public String getBodyAccount(Account account) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			String risposta = mapper.writeValueAsString(account);
			return risposta;
		} catch (Exception ex) {
			throw new AwesomeException(CodiciErrore.ERRGEN, "Errore nella generazione del body della response dall'oggetto JSON dei dati dell'Account ("+Utility.getMessageException(ex)+").");
		}
	}

    public String getEmail(String user){
		Account utente = loadAccountFromRest(user);
		return utente.getEmailComunicazioni();
    }

}
