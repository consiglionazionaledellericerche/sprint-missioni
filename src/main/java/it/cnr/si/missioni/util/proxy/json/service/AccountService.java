package it.cnr.si.missioni.util.proxy.json.service;

import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.service.ConfigService;
import it.cnr.si.missioni.service.ProxyService;
import it.cnr.si.missioni.util.CodiciErrore;
import it.cnr.si.missioni.util.Costanti;
import it.cnr.si.missioni.util.Utility;
import it.cnr.si.missioni.util.data.UoForUsersSpecial;
import it.cnr.si.missioni.util.data.UsersSpecial;
import it.cnr.si.missioni.util.proxy.ResultProxy;
import it.cnr.si.missioni.util.proxy.json.object.Account;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AccountService {
	@Autowired
    private ProxyService proxyService;

	@Autowired
    private ConfigService configService;

	public UsersSpecial getUoForUsersSpecial(String uid){
		if (configService.getDataUsersSpecial() != null && configService.getDataUsersSpecial().getUsersSpecials() != null ){
			for (Iterator<UsersSpecial> iteratorUsers = configService.getDataUsersSpecial().getUsersSpecials().iterator(); iteratorUsers.hasNext();){
				UsersSpecial user = iteratorUsers.next();
				if (user.getUid() != null && user.getUid().equals(uid)){
					return user;
				}
			}
		}
		return null;
	}
	
	public String manageResponseForAccountRest(String uid, 
			String body) {
		Account account = getAccount(body);
		if (account != null){
			return getResponseAccount(uid, account);
		}
		return null;
	}

	private String getResponseAccount(String uid, Account account) {
		UsersSpecial user = getUoForUsersSpecial(uid);
		return createResponseForAccountRest(account, user);
	}

	public String createResponseForAccountRest(Account account, UsersSpecial user) {
		if (user != null){
			account.setAllUoForUsersSpecial(user.getAll());
			account.setUoForUsersSpecial(user.getUoForUsersSpecials());
			return getBodyAccount(account);
		}
		return null;
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
		if (loadSpecialUserData){
			String resp = manageResponseForAccountRest(currentLogin, risposta);
			if (resp != null){
				return resp;
			}
		}
		return risposta;
	}

	public Boolean isUserSpecialEnableToValidateOrder(String user, String uo){
		if (uo == null){
			throw new AwesomeException(CodiciErrore.ERRGEN, "UO non indicata.");
		}
		UsersSpecial userSpecial = getUoForUsersSpecial(user);
		if (userSpecial.getAll() == null || !userSpecial.getAll().equals("S")){
			if (userSpecial.getUoForUsersSpecials() != null && !userSpecial.getUoForUsersSpecials().isEmpty()){
				List<String> listaUoUtente = new ArrayList<String>();
		    	for (UoForUsersSpecial uoForUsersSpecial : userSpecial.getUoForUsersSpecials()){
		    		if (uo.equals(uoForUsersSpecial.getCodice_uo()) && Utility.nvl(uoForUsersSpecial.getOrdine_da_validare()).equals("S")){
		    			return true;
		    		}
		    	}
			}
		} else {
			return true;
		}
		return false;
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

}
