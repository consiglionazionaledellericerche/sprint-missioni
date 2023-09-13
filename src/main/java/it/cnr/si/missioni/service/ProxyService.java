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
import it.cnr.jada.ejb.session.ComponentException;
import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.domain.custom.ExternalProblem;
import it.cnr.si.missioni.util.Costanti;
import it.cnr.si.missioni.util.DateUtils;
import it.cnr.si.missioni.util.FileMessageResource;
import it.cnr.si.missioni.util.Utility;
import it.cnr.si.missioni.util.proxy.ResultProxy;
import it.cnr.si.missioni.util.proxy.cache.CallCache;
import it.cnr.si.missioni.util.proxy.json.JSONBody;
import it.cnr.si.missioni.util.proxy.json.object.CommonJsonRest;
import it.cnr.si.missioni.util.proxy.json.object.sigla.Context;
import it.cnr.si.missioni.web.rest.ProxyResource;
import org.keycloak.admin.client.Keycloak;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for proxy to other application.
 * <p/>
 * <p>
 * </p>
 */
@Service
public class ProxyService implements EnvironmentAware {
    private final Logger log = LoggerFactory.getLogger(ProxyService.class);
    @Autowired
    CacheManager cacheManager;
    private Environment environment;
    private Map<String, RestTemplate> restTemplateMap;
    @Autowired(required = false)
    private Keycloak keycloak;

    @Cacheable(value = Costanti.NOME_CACHE_PROXY)
    public ResultProxy processInCache(CallCache callCache) throws AwesomeException {
        log.debug("Process in Cache 2: " + callCache.toString());
        ResultProxy resultProxyForCache = process(callCache.getHttpMethod(), callCache.getBody(), callCache.getApp(), callCache.getUrl(), callCache.getQueryString(), callCache.getAuthorization());
        log.debug("Response Cache 2: " + resultProxyForCache.toString());
        return resultProxyForCache(callCache, resultProxyForCache);
    }

    public ResultProxy process(CallCache callCache) throws AwesomeException {
        log.debug("Process in Cache 1: " + callCache.toString());
        ResultProxy resultProxyForCache = process(callCache.getHttpMethod(), callCache.getBody(), callCache.getApp(), callCache.getUrl(), callCache.getQueryString(), callCache.getAuthorization());
        return resultProxyForCache(callCache, resultProxyForCache);
    }

    private ResultProxy resultProxyForCache(CallCache callCache, ResultProxy resultProxy) throws AwesomeException {
        if (!StringUtils.isEmpty(callCache.getClasseJson())) {
            Class<?> clazzJson = null;
            try {
                clazzJson = Class.forName(callCache.getClasseJson());
            } catch (ClassNotFoundException e) {
                log.error("Errore", e);
                throw new ComponentException(Utility.getMessageException(e), e);
            }
            CommonJsonRest commonJson = null;
            try {
                commonJson = (CommonJsonRest) new ObjectMapper().readValue(resultProxy.getBody(), clazzJson);
            } catch (IOException e) {
                log.error("Errore", e);
                throw new ComponentException(Utility.getMessageException(e), e);
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
        log.debug("REST request from app {}", app);
        String body = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            body = mapper.writeValueAsString(jsonBody);
        } catch (Exception ex) {
            throw new ComponentException("Errore nella manipolazione del file JSON per la preparazione del body della richiesta REST (" + Utility.getMessageException(ex) + ").", ex);
        }
        return process(httpMethod, body, app, url, queryString, authorization, restContextHeader);
    }

    public ResultProxy process(HttpMethod httpMethod, String body, String app, String url, String queryString, String authorization, Boolean restContextHeader) {
        HttpHeaders headers = impostaAutenticazione(app, authorization);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        if (restContextHeader) {
            addContextToHeader(app, headers);
        }

        String proxyURL = impostaUrl(app, url, queryString);
        HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
        log.debug("Url: " + proxyURL);
        log.debug("Header: " + headers);
        log.debug("Body: " + body);
        try {
            ResponseEntity<String> result = getRestTemplate(app).
                    exchange(proxyURL, httpMethod, requestEntity, String.class);
            ResultProxy resultProxy = new ResultProxy();
            resultProxy.setBody(result.getBody());
            if (result.getHeaders() != null && result.getHeaders().getContentType() != null) {
                resultProxy.setType(result.getHeaders().getContentType().getType());
            }
            resultProxy.setStatus(result.getStatusCode());
            log.debug("Response for url : " + proxyURL, resultProxy);
            return resultProxy;
        } catch (HttpClientErrorException _ex) {
            String errResponse = _ex.getResponseBodyAsString();
            log.error(_ex.getMessage(), _ex.getResponseBodyAsString());
            throw new ApplicationContextException(errResponse, _ex);
        } catch (HttpServerErrorException _ex) {
            String errResponse = _ex.getResponseBodyAsString();
            log.error(_ex.getMessage(), _ex.getResponseBodyAsString());
            if (_ex.getStatusCode().compareTo(HttpStatus.SERVICE_UNAVAILABLE) == 0) {
                throw new ComponentException(app + " temporaneamente non disponibile");
            }
            throw new ApplicationContextException(errResponse, _ex);
        } catch (Exception _ex) {
            log.error(_ex.getMessage(), _ex.getLocalizedMessage());
            throw new ApplicationContextException("Servizio REST " + proxyURL + " Eccezione: " + _ex.getLocalizedMessage(), _ex);
        }
    }

    public ResultProxy processWithFile(HttpMethod httpMethod, String body, String app, String url, String queryString, String authorization, MultipartFile uploadedMultipartFile) throws IOException {
        MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add(ExternalProblem.ALLEGATO, new FileMessageResource(uploadedMultipartFile.getBytes(), uploadedMultipartFile.getOriginalFilename()));
        HttpHeaders headers = impostaAutenticazione(app, authorization);

        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

        String proxyURL = impostaUrl(app, url, queryString);

        log.debug("Url: " + proxyURL);
        log.debug("Header: " + headers);
        log.debug("Body: " + body);
        try {
            ResponseEntity<String> result = getRestTemplate(app).
                    exchange(proxyURL, httpMethod, requestEntity, String.class);
            ResultProxy resultProxy = new ResultProxy();
            resultProxy.setBody(result.getBody());
            resultProxy.setStatus(result.getStatusCode());
            log.debug("Response for url : " + proxyURL, resultProxy);
            return resultProxy;
        } catch (HttpClientErrorException _ex) {
            String errResponse = _ex.getResponseBodyAsString();
            log.error(_ex.getMessage(), _ex.getResponseBodyAsString());
            throw new ApplicationContextException(errResponse, _ex);
        } catch (HttpServerErrorException _ex) {
            String errResponse = _ex.getResponseBodyAsString();
            log.error(_ex.getMessage(), _ex.getResponseBodyAsString());
            throw new ApplicationContextException(errResponse, _ex);
        } catch (Exception _ex) {
            log.error(_ex.getMessage(), _ex.getLocalizedMessage());
            throw new ApplicationContextException("Servizio REST " + proxyURL + " Eccezione: " + _ex.getLocalizedMessage(), _ex);
        }
    }

    public String impostaUrl(String app, String url, String queryString) {
        String appUrl = environment.getProperty("spring.proxy." + app + ".url");
        String proxyURL = null;
        if (appUrl == null) {
            log.error("Cannot find properties for app: " + app + " Current profile are: ", Arrays.toString(environment.getActiveProfiles()));
            throw new ApplicationContextException("Cannot find properties for app: " + app);
        }
        log.debug("proxy url is: " + appUrl);
        proxyURL = appUrl.concat(url);
        if (queryString != null) {
            String valueToDelete = ProxyResource.PROXY_URL + "=" + url;
            int numberCharacter = valueToDelete.length();
            String newValue = queryString;
            if (queryString.startsWith(valueToDelete)) {
                newValue = queryString.substring(numberCharacter);
            }
            proxyURL = proxyURL.concat("?").concat(newValue);
        }
        return proxyURL;
    }

    public HttpHeaders impostaAutenticazione(String app, String authorization) {
        HttpHeaders headers = new HttpHeaders();
        String username = environment.getProperty("spring.proxy." + app + ".username"),
                password = environment.getProperty("spring.proxy." + app + ".password");
        final Boolean clientCredentials = environment.getProperty("spring.proxy." + app + ".client_credentials", Boolean.class, Boolean.FALSE);
        if (clientCredentials) {
            final String token = keycloak.tokenManager().getAccessToken().getToken();
            log.debug("Token: {}", token);
            headers.add("Authorization", "Bearer " + token);
        } else {
            if (username != null && password != null) {
                String plainCreds = username.concat(":").concat(password);
                byte[] plainCredsBytes = plainCreds.getBytes();
                byte[] base64CredsBytes = Base64.encode(plainCredsBytes);
                String base64Creds = new String(base64CredsBytes);
                headers.add("Authorization", "Basic " + base64Creds);
            } else {
                headers.add("Authorization", authorization);
            }
        }
        return headers;
    }

    private void addContextToHeader(String app, HttpHeaders headers) {
        Context context = getDefaultContext(app);
        if (context != null) {
            headers.add("X-sigla-cd-cds", context.getCd_cds());
            headers.add("X-sigla-cd-unita-organizzativa", context.getCd_unita_organizzativa());
            headers.add("X-sigla-cd-cdr", context.getCd_cdr());
            int anno = DateUtils.getCurrentYear();
            headers.add("X-sigla-esercizio", Integer.valueOf(anno).toString());
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
        this.restTemplateMap = new HashMap<String, RestTemplate>();
    }

    public Context getDefaultContext(String app) {
        String uoContext = environment.getProperty("spring.proxy." + app + ".context.cd_unita_organizzativa");
        Context context = new Context();
        if (!StringUtils.isEmpty(uoContext)) {
            context.setCd_unita_organizzativa(uoContext);
            if (StringUtils.isEmpty(context.getCd_cds())) {
                context.setCd_cds(environment.getProperty("spring.proxy." + app + ".context.cd_cds"));
            }
            if (StringUtils.isEmpty(context.getCd_cdr())) {
                context.setCd_cdr(environment.getProperty("spring.proxy." + app + ".context.cd_cdr"));
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