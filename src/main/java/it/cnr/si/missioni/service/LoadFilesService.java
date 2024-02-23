/*
 *  Copyright (C) 2023  Consiglio Nazionale delle Ricerche
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Affero General Public License as
 *      published by the Free Software Foundation, either version 3 of the
 *      License, or (at your option) any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Affero General Public License for more details.
 *
 *      You should have received a copy of the GNU Affero General Public License
 *      along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */

package it.cnr.si.missioni.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnr.si.missioni.amq.service.ResendQueueService;
import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.cmis.MissioniCMISService;
import it.cnr.si.missioni.util.CodiciErrore;
import it.cnr.si.missioni.util.Costanti;
import it.cnr.si.missioni.util.Utility;
import it.cnr.si.missioni.util.data.*;
import it.cnr.si.missioni.util.proxy.cache.json.Services;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.config.StoragePropertyNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigInteger;

@Service
public class LoadFilesService {
    private final Logger log = LoggerFactory.getLogger(LoadFilesService.class);
    @Autowired(required = false)
    private MissioniCMISService missioniCMISService;

    @Autowired(required = false)
    private ResendQueueService resendQueueService;

    @Autowired
    private Environment env;


    @CacheEvict(value = Costanti.NOME_CACHE_FAQ, allEntries = true)
    public void evictFaq() {
    }

    @CacheEvict(value = Costanti.NOME_CACHE_DATI_UO, allEntries = true)
    public void evictDatiUo() {
    }

    @CacheEvict(value = Costanti.NOME_CACHE_SERVICES_SIGLA, allEntries = true)
    public void evictServicesForCache() {
    }

    @CacheEvict(value = Costanti.NOME_CACHE_USER_SPECIAL, allEntries = true)
    public void evictUsersSpecialForUo() {
    }

    @CacheEvict(value = Costanti.NOME_CACHE_DATI_UTENTI_PRESIDENTE_SPECIALI, allEntries = true)
    public void evictDatiUtentiPresidenteSpeciali() {
    }

    @Cacheable(value = Costanti.NOME_CACHE_DATI_UTENTI_PRESIDENTE_SPECIALI)
    public UtentiPresidenteSpeciali loadDatiUtentiPresidenteSpeciali() {
        InputStream is = getUtentiPresidenteSpeciali();
        if (is == null) {
            throw new AwesomeException(CodiciErrore.ERRGEN, "File dati delle uo non trovato.");
        }
        try {
            return new ObjectMapper().readValue(is, UtentiPresidenteSpeciali.class);
        } catch (Exception e) {
            throw new AwesomeException(CodiciErrore.ERRGEN, "Errore in fase di lettura del file JSON degli utenti presidente speciali." + Utility.getMessageException(e));
        }
    }

    @Cacheable(value = Costanti.NOME_CACHE_DATI_UO)
    public DatiUo loadDatiUo() {
        InputStream is = getUo();
        if (is == null) {
            throw new AwesomeException(CodiciErrore.ERRGEN, "File dati delle uo non trovato.");
        }
        try {
            return new ObjectMapper().readValue(is, DatiUo.class);
        } catch (Exception e) {
            throw new AwesomeException(CodiciErrore.ERRGEN, "Errore in fase di lettura del file JSON delle uo." + Utility.getMessageException(e));
        }
    }

    @Cacheable(value = Costanti.NOME_CACHE_FAQ)
    public Faq loadFaq() {
        InputStream is = getFaq();
        if (is == null) {
            throw new AwesomeException(CodiciErrore.ERRGEN, "File dati delle faq non trovato.");
        }
        try {
            return new ObjectMapper().readValue(is, Faq.class);
        } catch (Exception e) {
            throw new AwesomeException(CodiciErrore.ERRGEN, "Errore in fase di lettura del file JSON delle faq." + Utility.getMessageException(e));
        }
    }

    @Cacheable(value = Costanti.NOME_CACHE_USER_SPECIAL)
    public DataUsersSpecial loadUsersSpecialForUo() {
        log.info("loadUsersSpecialForUo");
        InputStream is = getUsersSpecial();
        if (is != null) {
            try {
                return new ObjectMapper().readValue(is, DataUsersSpecial.class);
            } catch (Exception e) {
                throw new AwesomeException(CodiciErrore.ERRGEN, "Errore in fase di lettura del file JSON dei dati degli utenti speciali per i servizi REST." + Utility.getMessageException(e));
            }
        }
        return null;
    }

    @Cacheable(value = Costanti.NOME_CACHE_SERVICES_SIGLA)
    public Services loadServicesForCache() {
        InputStream is = getServicesForCache();
        if (is != null) {
            try {
                return new ObjectMapper().readValue(is, Services.class);
            } catch (Exception e) {
                throw new AwesomeException(CodiciErrore.ERRGEN, "Errore in fase di lettura del file JSON dei servizi REST." + Utility.getMessageException(e));
            }
        } else {
            return null;
        }
    }

    private InputStream getUsersSpecial() {
        InputStream is = null;
        String fileName = getFileNameFromUsersSpecial();
        StorageObject node = missioniCMISService.getStorageObjectByPath(missioniCMISService.getBasePath().getPathConfig() + "/" + missioniCMISService.sanitizeFilename(fileName));

        if (node == null || (node.<BigInteger>getPropertyValue(StoragePropertyNames.CONTENT_STREAM_LENGTH.value())).compareTo(BigInteger.ZERO) == 0) {
            is = this.getClass().getResourceAsStream("/it/cnr/missioni/sourceData/" + fileName);
        } else {
            is = missioniCMISService.getResource(node);
        }
        return is;
    }

    private InputStream getUo() {
        String fileName = getFileNameFromDatiUo();
        return recuperoFile(fileName);
    }

    private InputStream getUtentiPresidenteSpeciali() {
        String fileName = getFileNameFromUtentiPresidenteSpeciali();
        return recuperoFile(fileName);
    }

    protected InputStream recuperoFile(String fileName) {
        InputStream is;
        StorageObject node = missioniCMISService.getStorageObjectByPath(missioniCMISService.getBasePath().getPathConfig() + "/" + missioniCMISService.sanitizeFilename(fileName));
        if (node == null || (node.<BigInteger>getPropertyValue(StoragePropertyNames.CONTENT_STREAM_LENGTH.value())).compareTo(BigInteger.ZERO) == 0) {
            is = this.getClass().getResourceAsStream("/it/cnr/missioni/sourceData/" + fileName);
        } else {
            is = missioniCMISService.getResource(node);
        }
        return is;
    }

    private InputStream getFaq() {
        String fileName = getFileNameFromFaq();
        return recuperoFile(fileName);
    }

    private String getFileNameFromFaq() {
        return "faq.json";
    }

    private String getFileNameFromDatiUo() {
        if (env.acceptsProfiles(Costanti.SPRING_PROFILE_DEVELOPMENT)) {
            return "datiUo.json";
        } else {
            return "datiUo.json";
        }
    }

    private String getFileNameFromUtentiPresidenteSpeciali() {
        return "utentiPresidenteSpeciali.json";
    }

    private String getFileNameFromRestServices() {
//   		if (env.acceptsProfiles(Costanti.SPRING_PROFILE_DEVELOPMENT)) {
        return "restServicesDev.json";
//   		} else {
//   			return "restServices.json";
//   		}
    }

    private String getFileNameFromUsersSpecial() {
        if (env.acceptsProfiles(Costanti.SPRING_PROFILE_DEVELOPMENT)) {
            return "usersSpecialForUo.json";
        } else {
            return "usersSpecialForUo.json";
        }
    }

    private InputStream getServicesForCache() {
        InputStream is = null;

//    	if (env != null && Boolean.valueOf(env.getProperty("cache."+"init_cache")).equals(true)) {
        String fileName = getFileNameFromRestServices();
        is = recuperoFile(fileName);
//    	}
        return is;
    }

    public void resendQueue() {
        DataQueue queue = loadDataForQueue();
        resendQueueService.resendQueue(queue);
    }

    public DataQueue loadDataForQueue() {
        InputStream is = getQueue();
        if (is == null) {
            throw new AwesomeException(CodiciErrore.ERRGEN, "File dati delle uo non trovato.");
        }
        try {
            return new ObjectMapper().readValue(is, DataQueue.class);
        } catch (Exception e) {
            throw new AwesomeException(CodiciErrore.ERRGEN, "Errore in fase di lettura del file JSON delle uo." + Utility.getMessageException(e));
        }
    }

    private String getFileNameFromQueue() {
        if (env.acceptsProfiles(Costanti.SPRING_PROFILE_DEVELOPMENT)) {
            return "queueDev.json";
        } else {
            return "queue.json";
        }
    }

    private InputStream getQueue() {
        String fileName = getFileNameFromQueue();
        log.info("Nome File: " + fileName);
        return recuperoFile(fileName);
    }

}
