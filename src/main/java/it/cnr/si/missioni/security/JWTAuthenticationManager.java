package it.cnr.si.missioni.security;

import feign.FeignException;
import it.cnr.si.security.AuthoritiesConstants;
import it.cnr.si.service.AceService;
import it.cnr.si.service.AuthService;
import it.cnr.si.service.SiperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Primary
public class JWTAuthenticationManager implements AuthenticationManager {

    private final Logger log = LoggerFactory.getLogger(JWTAuthenticationManager.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private AceService aceService;

    @Autowired
    private SiperService siperService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String principal = (String) authentication.getPrincipal();
        String credentials = (String) authentication.getCredentials();

        authService.getToken(principal, credentials);

        List<GrantedAuthority> authorities = new ArrayList<>();
        try {
            authorities = aceService.ruoliAttivi(principal).stream()
                    .filter(ruolo -> ruolo.getContesto().getSigla().equals("missioni-app"))
                    .map(a -> new SimpleGrantedAuthority(a.getSigla()))
                    .collect(Collectors.toList());
        } catch (FeignException e) {
            log.info(e.getMessage() + " for user: "+ "\"" +  principal + "\"");
        }

        if (authorities.stream().filter(auth -> auth.getAuthority().equals("supervisore@missioni")).count() > 0){
            authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN));
        }

        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN));

        User utente = new User(principal.toLowerCase(), credentials, authorities);

        return new UsernamePasswordAuthenticationToken(utente, authentication, authorities);

    }

}
