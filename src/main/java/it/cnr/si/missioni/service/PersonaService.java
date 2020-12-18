package it.cnr.si.missioni.service;

import it.cnr.si.missioni.util.Costanti;
import it.cnr.si.missioni.util.proxy.json.object.Account;
import it.cnr.si.missioni.util.proxy.json.service.AccountService;
import it.cnr.si.service.AceService;
import it.cnr.si.service.SiperService;
import it.cnr.si.service.dto.anagrafica.UserInfoDto;
import it.cnr.si.service.dto.anagrafica.scritture.BossDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PersonaService {
    @Autowired
    private SiperService siperService;

    @Autowired
    AceService aceService;

    private transient static final Log logger = LogFactory.getLog(AccountService.class);

    @Cacheable(value = Costanti.NOME_CACHE_DATI_PERSONE)
    public UserInfoDto getAccountFromSiper(String currentLogin) {
        logger.info("getAccountFromSiper: "+ currentLogin);
        UserInfoDto userInfoDto = siperService.getUserInfoByUsername(currentLogin);
        return userInfoDto;
    }

    @Cacheable(value = Costanti.NOME_CACHE_DATI_PERSONE)
    public String getDirettore(String username) {
        BossDto direttore = aceService.findResponsabileStruttura(username);
        if (direttore != null){
            return direttore.getUtente().getUsername();
        }
        return "";
    }
}
