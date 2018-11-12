package it.cnr.si.missioni.web.rest;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.cnr.si.missioni.domain.custom.ExternalProblem;
import it.cnr.si.missioni.service.HelpdeskService;
import it.cnr.si.missioni.util.JSONResponseEntity;
import it.cnr.si.security.AuthoritiesConstants;

@RolesAllowed({AuthoritiesConstants.USER})
@RestController
@RequestMapping("/api")
public class HelpdeskResource {
	@Autowired
	private HelpdeskService helpdeskService;
	private final Logger log = LoggerFactory.getLogger(HelpdeskResource.class);

	@RequestMapping(value = "/rest/helpdesk/sendWithAttachment", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)	
	public ResponseEntity sendWithAttachment(HttpServletRequest req, @RequestParam("file") MultipartFile uploadedMultipartFile) {
		log.debug("HelpdeskResource:send");
		ExternalProblem hd = new ExternalProblem();
		Long id = null;
		if (StringUtils.hasLength(req.getParameter("idSegnalazione"))){
			id = new Long (req.getParameter("idSegnalazione"));
			hd.setIdSegnalazione(id);
			hd.setNota(req.getParameter("nota"));
		} else {
			hd.setTitolo(req.getParameter("titolo"));
			hd.setDescrizione(req.getParameter("descrizione"));
			hd.setCategoria(new Integer(req.getParameter("categoria")));
			hd.setCategoriaDescrizione(req.getParameter("categoriaDescrizione"));
		}
		id = helpdeskService.newProblem(hd);

		helpdeskService.addAttachments(id, uploadedMultipartFile);
		
		return JSONResponseEntity.ok();
	}

	@RequestMapping(value = "/rest/helpdesk/sendWithoutAttachment", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)	
	public ResponseEntity sendWithoutAttachment(HttpServletRequest req, @RequestBody ExternalProblem hdDataModel) {
		log.debug("HelpdeskResource:send");
		helpdeskService.newProblem(hdDataModel);
		return JSONResponseEntity.ok();	
	}
}
