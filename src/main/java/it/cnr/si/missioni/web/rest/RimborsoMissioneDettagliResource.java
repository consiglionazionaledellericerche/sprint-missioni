package it.cnr.si.missioni.web.rest;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.OptimisticLockException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.codahale.metrics.annotation.Timed;

import it.cnr.jada.ejb.session.BusyResourceException;
import it.cnr.jada.ejb.session.ComponentException;
import it.cnr.jada.ejb.session.PersistencyException;
import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.cmis.CMISFileAttachment;
import it.cnr.si.missioni.cmis.MimeTypes;
import it.cnr.si.missioni.domain.custom.persistence.RimborsoMissioneDettagli;
import it.cnr.si.missioni.service.RimborsoMissioneDettagliService;
import it.cnr.si.missioni.util.CodiciErrore;
import it.cnr.si.missioni.util.SecurityUtils;
import it.cnr.si.missioni.util.Utility;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/app")
public class RimborsoMissioneDettagliResource {

    private final Logger log = LoggerFactory.getLogger(RimborsoMissioneDettagliResource.class);

    @Autowired
    private RimborsoMissioneDettagliService rimborsoMissioneDettagliService;

    @RequestMapping(value = "/rest/rimborsoMissione/dettagli/get",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<RimborsoMissioneDettagli>> getDettagli(HttpServletRequest request,
    		@RequestParam(value = "idRimborsoMissione") Long idRimborsoMissione) {
        log.debug("REST request per visualizzare i dettagli del Rimborso della Missione" );
        try {
            List<RimborsoMissioneDettagli> dettagli = rimborsoMissioneDettagliService.getRimborsoMissioneDettagli((Principal) SecurityUtils.getCurrentUser(), idRimborsoMissione);
            return new ResponseEntity<>(
            		dettagli,
                    HttpStatus.OK);
		} catch (ComponentException e) {
			List<RimborsoMissioneDettagli> listaVuota = new ArrayList<RimborsoMissioneDettagli>();
			return new ResponseEntity<>(
                    listaVuota,
                    HttpStatus.BAD_REQUEST);
		} 
    }

    @RequestMapping(value = "/rest/rimborsoMissione/dettagli/modify",
            method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> modifyDettaglio(@RequestBody RimborsoMissioneDettagli dettaglio, HttpServletRequest request,
                                             HttpServletResponse response) {
    	if (dettaglio.getId() != null){
            try {
            	dettaglio = rimborsoMissioneDettagliService.updateRimborsoMissioneDettagli((Principal) SecurityUtils.getCurrentUser(), dettaglio);
    		} catch (AwesomeException|ComponentException|OptimisticLockException|PersistencyException|BusyResourceException e) {
    			return new ResponseEntity<String>(Utility.getMessageException(e), HttpStatus.INTERNAL_SERVER_ERROR);
    		}
            return new ResponseEntity<>(dettaglio, HttpStatus.OK);
    	} else {
    	      return new ResponseEntity<String>(CodiciErrore.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    	}
    }

    @RequestMapping(value = "/rest/rimborsoMissione/dettagli/create",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> createDettaglio(@RequestBody RimborsoMissioneDettagli dettaglio, HttpServletRequest request,
                                             HttpServletResponse response) {
    	if (dettaglio.getId() == null){
            try {
            	dettaglio = rimborsoMissioneDettagliService.createRimborsoMissioneDettagli((Principal) SecurityUtils.getCurrentUser(), dettaglio);
    		} catch (AwesomeException e) {
    			return e.getResponse();
    		} catch (ComponentException e) {
    			return new ResponseEntity<String>(Utility.getMessageException(e), HttpStatus.BAD_REQUEST);
    		} catch (OptimisticLockException e) {
    			return new ResponseEntity<String>(Utility.getMessageException(e), HttpStatus.BAD_REQUEST);
    		} catch (PersistencyException e) {
    			return new ResponseEntity<String>(Utility.getMessageException(e), HttpStatus.BAD_REQUEST);
    		} catch (BusyResourceException e) {
    			return new ResponseEntity<String>(Utility.getMessageException(e), HttpStatus.BAD_REQUEST);
    		}
            return new ResponseEntity<>(dettaglio, HttpStatus.CREATED);
    	} else {
    	      return new ResponseEntity<String>(CodiciErrore.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    	}
    }

    @RequestMapping(value = "/rest/rimborsoMissione/dettagli/{ids}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<?> deleteSpostamento(@PathVariable Long ids, HttpServletRequest request) {
		try {
			rimborsoMissioneDettagliService.deleteRimborsoMissioneDettagli((Principal) SecurityUtils.getCurrentUser(), ids);
            return new ResponseEntity<>(HttpStatus.OK);
		} catch (AwesomeException e) {
			return e.getResponse();
		} catch (ComponentException e) {
			return new ResponseEntity<String>(Utility.getMessageException(e), HttpStatus.BAD_REQUEST);
		} catch (OptimisticLockException e) {
			return new ResponseEntity<String>(Utility.getMessageException(e), HttpStatus.BAD_REQUEST);
		} catch (PersistencyException e) {
			return new ResponseEntity<String>(Utility.getMessageException(e), HttpStatus.BAD_REQUEST);
		} catch (BusyResourceException e) {
			return new ResponseEntity<String>(Utility.getMessageException(e), HttpStatus.BAD_REQUEST);
		}
	}

    @RequestMapping(value = "/rest/rimborsoMissione/dettagli/viewAttachments/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<List<CMISFileAttachment>> getAttachments(HttpServletRequest request,
    		@PathVariable Long idRimborsoMissione) {
        log.debug("REST request per visualizzare gli allegati dei dettagli del Rimborso della Missione" );
        try {
            List<CMISFileAttachment> lista = rimborsoMissioneDettagliService.getAttachments((Principal) SecurityUtils.getCurrentUser(), idRimborsoMissione);
            return new ResponseEntity<>(
            		lista,
                    HttpStatus.OK);
		} catch (ComponentException e) {
			List<CMISFileAttachment> listaVuota = new ArrayList<CMISFileAttachment>();
			return new ResponseEntity<>(
                    listaVuota,
                    HttpStatus.BAD_REQUEST);
		} 
    }

//    @RequestMapping(value = "/rest/ordineMissione/uploadAllegati/{idOrdineMissione}",
//            method = RequestMethod.POST,
//            produces = MediaType.APPLICATION_JSON_VALUE,
//            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @Timed
//    public ResponseEntity<?> uploadAllegatiOrdineMissione(@PathVariable Long idOrdineMissione, HttpServletRequest req, @RequestParam("file") MultipartFile file) {
//        log.debug("REST request per l'upload di allegati all'Ordine di Missione " );
//        if (idOrdineMissione != null){
//            	try {
//            		if (!Utility.isTypeOk(file.getContentType())){
//    	    			return new ResponseEntity<String>("Il tipo di file selezionato: "+file.getContentType()+ " non è valido.", HttpStatus.BAD_REQUEST);
//            		}
//					ordineMissioneService.uploadAllegatoOrdineMissione((Principal) SecurityUtils.getCurrentUser(), idOrdineMissione, file.getInputStream(), file.getName(), file.getContentType());
//					MimeTypes.valueOf(file.getContentType());
//            	} catch (ComponentException | AwesomeException | IOException e1) {
//	    			return new ResponseEntity<String>(e1.getMessage(), HttpStatus.BAD_REQUEST);
//				}
//            try {
//                return new ResponseEntity<>(
//                		null,
//                        HttpStatus.OK);
//    		} catch (ComponentException e) {
//    			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
//    		} 
//    	} else {
//  	      return new ResponseEntity<String>(CodiciErrore.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
//    	}
//    }
}
