package it.cnr.si.missioni.service;

import java.io.IOException;
import java.time.ZonedDateTime;

import it.cnr.si.missioni.repository.CRUDComponentSession;
import it.cnr.si.service.SecurityService;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.domain.custom.ExternalProblem;
import it.cnr.si.missioni.util.CodiciErrore;
import it.cnr.si.missioni.util.Costanti;
import it.cnr.si.missioni.util.DateUtils;
import it.cnr.si.missioni.util.SecurityUtils;
import it.cnr.si.missioni.util.proxy.ResultProxy;
import it.cnr.si.missioni.util.proxy.json.object.Account;
import it.cnr.si.missioni.util.proxy.json.service.AccountService;

@Service
public class HelpdeskService {
    private final Logger log = LoggerFactory.getLogger(HelpdeskService.class);

    @Autowired
    ProxyService proxyService;
    
    @Value("${spring.proxy.OIL.url}")
    private static String helpdeskUrl;

    @Value("${spring.proxy.OIL.instance}")
    private static String instance;

    @Value("${spring.proxy.OIL.newProblem}")
    private static String newProblem;

	@Autowired
	private AccountService accountService;

	@Autowired
	private SecurityService securityService;

	public Long newProblem(ExternalProblem hd) throws ServiceException {

		Account account = accountService.loadAccount(true);
		if (account.getUoForUsersSpecials() == null || account.getUoForUsersSpecials().isEmpty()){
			throw new AwesomeException(CodiciErrore.ERRGEN, "Errore. Helpdesk non autorizzato");
		}
		
		hd.setFirstName(account.getNome());
		hd.setFamilyName(account.getCognome());
		hd.setEmail(account.getEmail_comunicazioni());
		hd.setConfirmRequested("y");
		String url = Costanti.REST_OIL_NEW_PROBLEM;

		if (hd.getIdSegnalazione() != null){
			hd.setStato(0);
			ResultProxy result = proxyService.process(HttpMethod.POST, hd, Costanti.APP_HELPDESK, url, null, null, false);

		} else {
			String descrizione = hd.getDescrizione() + System.getProperty("line.separator")+System.getProperty("line.separator")+hd.getFirstName()+
					" "+hd.getFamilyName()+"  Email: "+hd.getEmail()+"  Data: "+DateUtils.getDateAsString(ZonedDateTime.now(), DateUtils.PATTERN_DATETIME);

			hd.setDescrizione(descrizione);

			//			, String app, String url, Boolean value, ) {
			ResultProxy result = proxyService.process(HttpMethod.PUT, hd, Costanti.APP_HELPDESK, url, null, null, false);
			return new Long( result.getBody());
			
		}
		return null;
	}
	
	public void addAttachments(long id, MultipartFile uploadedMultipartFile) throws ServiceException {

		String url = Costanti.REST_OIL_NEW_PROBLEM+"/"+id;
		try {
			ResultProxy result = proxyService.processWithFile(HttpMethod.POST, null, Costanti.APP_HELPDESK, url, null, null, uploadedMultipartFile);
		} catch (IOException e) {
			e.printStackTrace();
			throw new AwesomeException("Errore per l'allegato "+e);
		}
			
	}
		
}
