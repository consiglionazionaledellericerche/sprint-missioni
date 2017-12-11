package it.cnr.si.missioni.web.rest;

import com.codahale.metrics.annotation.Timed;
import it.cnr.si.missioni.service.ConfigService;
import it.cnr.si.missioni.service.CronService;
import it.cnr.si.security.AuthoritiesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;

/**
 * REST controller for managing config.
 */
@RolesAllowed({AuthoritiesConstants.ADMIN})
@RestController
@RequestMapping("/api")
public class ConfigResource {

	private final Logger log = LoggerFactory.getLogger(ConfigResource.class);

	@Autowired
	private CronService cronService;

	@Autowired
	private ConfigService configService;

	/**
	 * GET  /rest/config/refresh -> rechargeConfig
	 */
	@RequestMapping(value = "/rest/config/refresh",
			method = RequestMethod.GET)
	@Timed
	public void reloadConfig() {
		log.debug("REST request per ricaricare la configurazione da Alfresco");
		configService.reloadConfig();
	}

	/**
	 * GET  /rest/config/refreshCache -> refreshCache
	 */
	@RequestMapping(value = "/rest/config/refreshCache",
			method = RequestMethod.GET)
	@Timed
	public void refreshCache() {
		log.debug("REST request per ricaricare la configurazione da Alfresco");
		cronService.evictCache();
		cronService.loadCache();
	}

	/**
	 * GET  /rest/config/specialUsers -> rechargeSpecialUser.
	 */
	@RequestMapping(value = "/rest/config/specialUsers",
			method = RequestMethod.GET)
	@Timed
	public void reloadConfigSpecialUsers() {
		log.debug("REST request per ricaricare la configurazione di specialUser da Alfresco ");
		configService.reloadUsersSpecialForUo();
	}

	/**
	 * GET  /rest/config/datiUo -> rechargeDatiUo.
	 */
	@RequestMapping(value = "/rest/config/datiUo",
			method = RequestMethod.GET)
	@Timed
	public void reloadConfigDatiUo() {
		log.debug("REST request per ricaricare la configurazione di Dati Uo da Alfresco");
		configService.reloadDatiUo();
	}

	/**
	 * GET  /rest/config/restServicesForCache -> rechargeDatiUo.
	 */
	@RequestMapping(value = "/rest/config/restServicesForCache",
			method = RequestMethod.GET)
	@Timed
	public void reloadConfigRestServicesForCache() {
		log.debug("REST request per ricaricare la configurazione di dei Servizi Rest in Cache da Alfresco");
		configService.reloadServicesForCache();
	}
}
