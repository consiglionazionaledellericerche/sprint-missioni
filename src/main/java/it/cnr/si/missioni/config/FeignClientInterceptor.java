package it.cnr.si.missioni.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.keycloak.adapters.OidcKeycloakAccount;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private static final String AUTHORIZATION_HEADER="Authorization";
    private static final String TOKEN_TYPE = "Bearer";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getDetails() instanceof OidcKeycloakAccount) {
            OidcKeycloakAccount details = (OidcKeycloakAccount) authentication.getDetails();
            requestTemplate.header(AUTHORIZATION_HEADER, String.format("%s %s", TOKEN_TYPE, details.getKeycloakSecurityContext().getTokenString()));
        }
    }
}
