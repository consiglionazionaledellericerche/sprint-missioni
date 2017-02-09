package it.cnr.si.missioni.web.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;

import it.cnr.jada.ejb.session.BusyResourceException;
import it.cnr.jada.ejb.session.ComponentException;
import it.cnr.jada.ejb.session.PersistencyException;
import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.cmis.CMISFileAttachment;
import it.cnr.si.missioni.cmis.MimeTypes;
import it.cnr.si.missioni.domain.custom.persistence.OrdineMissioneAnticipo;
import it.cnr.si.missioni.service.OrdineMissioneAnticipoService;
import it.cnr.si.missioni.util.JSONResponseEntity;
import it.cnr.si.missioni.util.SecurityUtils;
import it.cnr.si.missioni.util.Utility;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/app")
public class OrdineMissioneAnticipoResource {

    private final Logger log = LoggerFactory.getLogger(OrdineMissioneAnticipoResource.class);


    @Autowired
    private TokenStore tokenStore;

	@Inject
    private OrdineMissioneAnticipoService ordineMissioneAnticipoService;

    /**
     * GET  /rest/ordineMissione -> get Ordine di missione byId
     */
    @RequestMapping(value = "/rest/ordineMissione/anticipo/get",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> getAnticipo(HttpServletRequest request,
    		@RequestParam(value = "idMissione") Long idMissione) {
        log.debug("REST request per visualizzare i dati dell'auto propria dell'Ordine di Missione" );
        try {
            OrdineMissioneAnticipo ordineMissioneAnticipo = ordineMissioneAnticipoService.getAnticipo((Principal) SecurityUtils.getCurrentUser(), idMissione, true);
            return JSONResponseEntity.ok(ordineMissioneAnticipo);
        } catch (ComponentException e) {
			log.error("ERRORE getAnticipo",e);
            return JSONResponseEntity.badRequest(Utility.getMessageException(e));
		} 
    }

    @RequestMapping(value = "/rest/ordineMissione/anticipo/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> createAnticipoOrdineMissione(@RequestBody OrdineMissioneAnticipo ordineMissioneAnticipo, HttpServletRequest request,
                                             HttpServletResponse response) {
    	if (ordineMissioneAnticipo.getId() == null){
            try {
                ordineMissioneAnticipo = ordineMissioneAnticipoService.createAnticipo((Principal) SecurityUtils.getCurrentUser(), ordineMissioneAnticipo);
    		} catch (Exception e) {
    			log.error("ERRORE createAnticipoOrdineMissione",e);
                return JSONResponseEntity.badRequest(Utility.getMessageException(e));
    		}
            return JSONResponseEntity.ok(ordineMissioneAnticipo);
    	} else {
    		String error = "Id anticipo già valorizzato";
			log.error("ERRORE createAnticipoOrdineMissione",error);
    	    return JSONResponseEntity.badRequest(error);
    	}
    }

    @RequestMapping(value = "/rest/ordineMissione/anticipo/modify",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> modifyAnticipoOrdineMissione(@RequestBody OrdineMissioneAnticipo ordineMissioneAnticipo, HttpServletRequest request,
                                             HttpServletResponse response) {
    	if (ordineMissioneAnticipo.getId() != null){
            try {
            	ordineMissioneAnticipo = ordineMissioneAnticipoService.updateAnticipo((Principal) SecurityUtils.getCurrentUser(), ordineMissioneAnticipo);
    		} catch (AwesomeException|ComponentException|OptimisticLockException|PersistencyException|BusyResourceException e) {
    			log.error("ERRORE modifyAnticipoOrdineMissione",e);
                return JSONResponseEntity.badRequest(Utility.getMessageException(e));
    		}
            return JSONResponseEntity.ok(ordineMissioneAnticipo);
    	} else {
    		String error = "ID Anticipo dell'Ordine di missione non trovato";
			log.error("ERRORE modifyAnticipoOrdineMissione",error);
    	    return JSONResponseEntity.badRequest(error);
    	}
    }


    @RequestMapping(value = "/rest/ordineMissione/anticipo/{ids}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> deleteAnticipo(@PathVariable Long ids, HttpServletRequest request) {
		try {
			ordineMissioneAnticipoService.deleteAnticipo((Principal) SecurityUtils.getCurrentUser(), ids);
            return JSONResponseEntity.ok();
		} catch (AwesomeException e) {
			log.error("ERRORE deleteAnticipo",e);
			return JSONResponseEntity.getResponse(HttpStatus.BAD_REQUEST, Utility.getMessageException(e));
		} catch (ComponentException|OptimisticLockException|PersistencyException|BusyResourceException e) {
			log.error("ERRORE deleteAnticipo",e);
            return JSONResponseEntity.badRequest(Utility.getMessageException(e));
		}
	}

    @RequestMapping(value = "/rest/public/printOrdineMissioneAnticipo",
            method = RequestMethod.GET)
    @Timed
    public @ResponseBody void printOrdineMissioneAnticipo(HttpServletRequest request,
    		@RequestParam(value = "idMissione") String idMissione, @RequestParam(value = "token") String token, HttpServletResponse res) {
        log.debug("REST request per la stampa dell'Ordine di Missione " );
        
        if (!StringUtils.isEmpty(idMissione)){
        	try {
        		Long idMissioneLong = new Long (idMissione); 
        		OAuth2Authentication auth = tokenStore.readAuthentication(token);
        		if (auth != null){
        			Map<String, byte[]> map = ordineMissioneAnticipoService.printOrdineMissioneAnticipo(auth, idMissioneLong);
        			if (map != null){
        				res.setContentType("application/pdf");
        				try {
        					String headerValue = "attachment";
        					for (String key : map.keySet()) {
        						headerValue += "; filename=\"" + key + "\"";


        						res.setHeader("Content-Disposition", headerValue);
        						OutputStream outputStream = res.getOutputStream();
        						InputStream inputStream = new ByteArrayInputStream(map.get(key));

        						IOUtils.copy(inputStream, outputStream);

        						outputStream.flush();

        						inputStream.close();
        						outputStream.close();       	

        					}
        				} catch (IOException e) {
        					log.error("ERRORE printOrdineMissioneAnticipo",e);
        					throw new RuntimeException(Utility.getMessageException(e));
        				} 
        			}
    			}
        	} catch (ComponentException e) {
    			log.error("ERRORE printOrdineMissioneAnticipo",e);
        		throw new RuntimeException(Utility.getMessageException(e));
        	} 
        }
    }

    @RequestMapping(value = "/rest/ordineMissione/anticipo/print/json",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> jsonForPrintOrdineMissioneAnticipo(HttpServletRequest request,
    		@RequestParam(value = "idMissione") Long idMissione) {
        log.debug("REST request per il json della stampa dell'anticipo dell'Ordine di Missione " );
        try {
        	String json = ordineMissioneAnticipoService.jsonForPrintOrdineMissione((Principal) SecurityUtils.getCurrentUser(), idMissione);
        	return JSONResponseEntity.ok(json);
        } catch (ComponentException e) {
			log.error("ERRORE jsonForPrintOrdineMissioneAnticipo",e);
        	return JSONResponseEntity.badRequest(Utility.getMessageException(e));
		} 
    }

    @RequestMapping(value = "/rest/ordineMissione/anticipo",
            method = RequestMethod.PUT,
            params = {"confirm"}, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> confirmOrdineMissioneAnticipo(@RequestBody OrdineMissioneAnticipo ordineMissioneAnticipo, @RequestParam(value = "confirm") Boolean confirm, 
    		HttpServletRequest request, HttpServletResponse response) {
    	if (ordineMissioneAnticipo.getId() != null){
            try {
				ordineMissioneAnticipo = ordineMissioneAnticipoService.updateAnticipo((Principal) SecurityUtils.getCurrentUser(), ordineMissioneAnticipo, confirm);
    		} catch (AwesomeException e) {
    			log.error("ERRORE confirmOrdineMissioneAnticipo",e);
    			return JSONResponseEntity.getResponse(HttpStatus.BAD_REQUEST, Utility.getMessageException(e));
    		} catch (ComponentException|OptimisticLockException e) {
    			log.error("ERRORE confirmOrdineMissioneAnticipo",e);
                return JSONResponseEntity.badRequest(Utility.getMessageException(e));
    		}
        	return JSONResponseEntity.ok(ordineMissioneAnticipo);
    	} else {
    		String error = "ID Anticipo non trovato";
			log.error("ERRORE confirmOrdineMissioneAnticipo",error);
            return JSONResponseEntity.badRequest(error);
    	}
    }

    @RequestMapping(value = "/rest/ordineMissione/anticipo/viewAttachments/{idAnticipo}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> getAttachments(HttpServletRequest request,
    		@PathVariable Long idAnticipo) {
        log.debug("REST request per visualizzare gli allegati dell'anticipo" );
        try {
            List<CMISFileAttachment> lista = ordineMissioneAnticipoService.getAttachments((Principal) SecurityUtils.getCurrentUser(), idAnticipo);
            return JSONResponseEntity.ok(lista);
		} catch (ComponentException e) {
			log.error("getAttachments", e);
            return JSONResponseEntity.badRequest(Utility.getMessageException(e));
		} 
    }

    @RequestMapping(value = "/rest/ordineMissione/anticipo/uploadAllegati/{idAnticipo}",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Timed
    public ResponseEntity<?> uploadAllegatiAnticipo(@PathVariable Long idAnticipo, HttpServletRequest req, @RequestParam("file") MultipartFile file) {
        log.debug("REST request per l'upload di allegati dell'anticipo" );
        if (idAnticipo != null){
            	try {
            		if (file != null && file.getContentType() != null){
            			MimeTypes mimeTypes = Utility.getMimeType(file.getContentType());
            			if (mimeTypes == null){
                			return new ResponseEntity<String>("Il tipo di file selezionato: "+file.getContentType()+ " non è valido.", HttpStatus.BAD_REQUEST);
            			} else {
        					CMISFileAttachment cmisFileAttachment = ordineMissioneAnticipoService.uploadAllegato((Principal) SecurityUtils.getCurrentUser(), idAnticipo, file.getInputStream(), file.getOriginalFilename(), mimeTypes);
        	                if (cmisFileAttachment != null){
        	                    return JSONResponseEntity.ok(cmisFileAttachment);
        	                } else {
        	                	String error = "Non è stato possibile salvare il file.";
        	        			log.error("uploadAllegatiAnticipo", error);
        	                    return JSONResponseEntity.badRequest(error);
        	                }
            			}
            		}else {
	                	String error = "File vuoto o con tipo non specificato.";
	        			log.error("uploadAllegatiAnticipo", error);
	                    return JSONResponseEntity.badRequest(error);
            		}
            	} catch (ComponentException | AwesomeException | IOException e1) {
        			log.error("uploadAllegatiAnticipo", e1);
                    return JSONResponseEntity.badRequest(Utility.getMessageException(e1));
				}
    	} else {
        	String error = "Id Dettaglio non valorizzato.";
			log.error("uploadAllegatiAnticipo", error);
            return JSONResponseEntity.badRequest(error);
    	}
    }


}
