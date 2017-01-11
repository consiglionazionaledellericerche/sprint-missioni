package it.cnr.si.missioni.service;

import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.util.CodiciErrore;
import it.cnr.si.missioni.util.Costanti;
import it.cnr.si.missioni.util.DateUtils;
import it.cnr.si.missioni.util.Utility;
import it.cnr.si.missioni.util.proxy.ResultProxy;
import it.cnr.si.missioni.util.proxy.cache.CallCache;
import it.cnr.si.missioni.util.proxy.json.JSONBody;
import it.cnr.si.missioni.util.proxy.json.object.CommonJsonRest;
import it.cnr.si.missioni.util.proxy.json.object.sigla.Context;
import it.cnr.si.missioni.web.rest.ProxyResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for proxy to other application.
 * <p/>
 * <p>
 * </p>
 */
@Service
public class ProxyService implements EnvironmentAware{
    private final Logger log = LoggerFactory.getLogger(ProxyService.class);

	private RelaxedPropertyResolver propertyResolver;

    private Environment environment;
    
    private Map<String, RestTemplate> restTemplateMap;

    @Autowired
    CacheManager cacheManager;
    
    @Cacheable(value=Costanti.NOME_CACHE_PROXY)
    public ResultProxy processInCache(CallCache callCache)  throws AwesomeException{
    	log.debug("Process in Cache 2: "+callCache.toString());
    	ResultProxy resultProxyForCache = process(callCache.getHttpMethod(), callCache.getBody(), callCache.getApp(), callCache.getUrl(), callCache.getQueryString(), callCache.getAuthorization());
		return resultProxyForCache(callCache, resultProxyForCache);
    }

    public ResultProxy process(CallCache callCache)  throws AwesomeException{
    	log.debug("Process in Cache 1: "+callCache.toString());
    	ResultProxy resultProxyForCache = process(callCache.getHttpMethod(), callCache.getBody(), callCache.getApp(), callCache.getUrl(), callCache.getQueryString(), callCache.getAuthorization());
		return resultProxyForCache(callCache, resultProxyForCache);
    }

	private ResultProxy resultProxyForCache(CallCache callCache, ResultProxy resultProxy) throws AwesomeException{
		if (!StringUtils.isEmpty(callCache.getClasseJson())){
			Class<?> clazzJson= null;
			try {
				clazzJson = Class.forName(callCache.getClasseJson());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new AwesomeException(CodiciErrore.ERRGEN, Utility.getMessageException(e));
			}
//			if (callCache.getUrl().equals("ConsProgettiAction.json")){
//				Path p = Paths.get("c:\\app\\prg.txt");
//				byte data[] = resultProxy.getBody().getBytes();
//				try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(p))){
//					out.write(data);
//				} catch (Exception e) {
//					log.debug("errore" + e.getMessage());
//				}
//			}
			CommonJsonRest commonJson = null;
			try {
				commonJson = (CommonJsonRest)new ObjectMapper().readValue(resultProxy.getBody(), clazzJson);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new AwesomeException(CodiciErrore.ERRGEN, Utility.getMessageException(e));
			}
	    	resultProxy.setCommonJsonResponse(commonJson);
	    	resultProxy.setBody("");
		}
    	return resultProxy;
	}
    
    public ResultProxy process(HttpMethod httpMethod, JSONBody jsonBody, String app, String url, String queryString, String authorization) {
    	return process(httpMethod, jsonBody, app, url, queryString, authorization, false);
    }
    public ResultProxy process(HttpMethod httpMethod, JSONBody jsonBody, String app, String url, String queryString, String authorization, Boolean restContextHeader) {
        log.info("REST request from app ", app);
        String appUrl = propertyResolver.getProperty(app + ".url");
        if (appUrl == null) {
        	log.error("Cannot find properties for app: " + app + " Current profile are: ", Arrays.toString(environment.getActiveProfiles()));
        	throw new ApplicationContextException("Cannot find properties for app: " + app);
        }
        log.info("proxy url is: " + appUrl);  
        try {
        	HttpHeaders headers = new HttpHeaders();
        	String username = propertyResolver.getProperty(app + ".username"), 
        			password = propertyResolver.getProperty(app + ".password");
        	if (username != null && password != null) {
        		String plainCreds = username.concat(":").concat(password);
        		byte[] plainCredsBytes = plainCreds.getBytes();
        		byte[] base64CredsBytes = Base64.encode(plainCredsBytes);
        		String base64Creds = new String(base64CredsBytes);
        		headers.add("Authorization", "Basic " + base64Creds);
        	} else {
        		headers.add("Authorization", authorization);
        	}
        	headers.setContentType(MediaType.APPLICATION_JSON);
        	
        	if (restContextHeader){
        		addContextToHeader(app, headers);
        	}
        	
    		String body = null;
        	try {
        		ObjectMapper mapper = new ObjectMapper();
        		body = mapper.writeValueAsString(jsonBody);
        	} catch (Exception ex) {
        		throw new AwesomeException(CodiciErrore.ERRGEN, "Errore nella manipolazione del file JSON per la preparazione del body della richiesta REST ("+Utility.getMessageException(ex)+").");
        	}
        	String proxyURL = appUrl.concat(url);
    		if (queryString != null){
    			String valueToDelete = ProxyResource.PROXY_URL+"="+url;
    			int numberCharacter = valueToDelete.length(); 
    			String newValue = queryString;
    			if (queryString.startsWith(valueToDelete)){
    				newValue = queryString.substring(numberCharacter);
    			}
    			proxyURL = proxyURL.concat("?").concat(newValue);        	
    		}
        	HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);  
            ResponseEntity<String> result = getRestTemplate(app).
            		exchange(proxyURL, httpMethod, requestEntity, String.class);
            ResultProxy resultProxy = new ResultProxy();
            resultProxy.setBody(result.getBody());
            resultProxy.setType(result.getHeaders().getContentType().getType());
            resultProxy.setStatus(result.getStatusCode());
            log.debug("Response for url : " + proxyURL, resultProxy);
            return resultProxy;
        } catch (HttpClientErrorException _ex) {
        	String errResponse = _ex.getResponseBodyAsString();
        	log.error(_ex.getMessage(), _ex.getResponseBodyAsString());
        	throw new ApplicationContextException(errResponse);
        }    	
    }

	private void addContextToHeader(String app, HttpHeaders headers) {
		Context context = getDefaultContext(app);
		if (context != null){
			headers.add("X-sigla-cd-cds", context.getCd_cds());
			headers.add("X-sigla-cd-unita-organizzativa", context.getCd_unita_organizzativa());
			headers.add("X-sigla-cd-cdr", context.getCd_cdr());
			int anno = DateUtils.getCurrentYear();
			headers.add("X-sigla-esercizio", new Integer(anno).toString());
		}
	}

	private RestTemplate getRestTemplate(String app) {
    	if (!restTemplateMap.containsKey(app))
    		restTemplateMap.put(app, new RestTemplate());    		
    	return restTemplateMap.get(app);    	
    }
	
	@Override
	public void setEnvironment(Environment environment) {
        this.environment = environment;
        this.propertyResolver = new RelaxedPropertyResolver(environment, "spring.proxy.");
        this.restTemplateMap = new HashMap<String, RestTemplate>();
	}

	public Context getDefaultContext(String app){
		String uoContext = propertyResolver.getProperty(app + ".context.cd_unita_organizzativa");
		Context context = new Context();
    	if (!StringUtils.isEmpty(uoContext)){
    		context.setCd_unita_organizzativa(uoContext);
    		if (StringUtils.isEmpty(context.getCd_cds())){
    			context.setCd_cds(propertyResolver.getProperty(app + ".context.cd_cds"));
    		}
    		if (StringUtils.isEmpty(context.getCd_cdr())){
    			context.setCd_cdr(propertyResolver.getProperty(app + ".context.cd_cdr"));
    		}
    	}
    	return context;
	}
	public JSONBody inizializzaJson() {
		JSONBody jBody = new JSONBody();
		jBody.setActivePage(0);
		jBody.setMaxItemsPerPage(Costanti.DEFAULT_VALUE_MAX_ITEM_FOR_PAGE_CACHE);
		return jBody;
	}
}