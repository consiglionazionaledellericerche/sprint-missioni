package it.cnr.si.missioni.amq.service;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.cnr.si.missioni.service.ConfigService;
import it.cnr.si.missioni.service.MailService;

@Profile("!showcase")
@RabbitListener(queues = "${spring.rabbitmq.coda-cambio-username}", ackMode = "NONE")
@Configuration
public class CambioUsernameConsumer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CambioUsernameConsumer.class);
    
    @Inject
    private ConfigService configService;
    @Inject
    private MailService mailService;
    private ObjectMapper om = new ObjectMapper();

    
    @RabbitHandler
    public void process(@Payload byte[] byteMessage) throws IOException {
        
        String stringMessage = "parsingError"; // try to get the input string, else get a parsing error
        try {stringMessage = new String(byteMessage);} catch (Exception ex) {/* no action*/}
        
        mailService.sendEmailError("Errore nella lettura del messaggio cambioUsername", stringMessage, false, true);
        LOGGER.info("Ricevuto Messaggio cambio username: "+ stringMessage);
        
        MessaggioCambioUsername message;
        try {
            message = om.readValue(byteMessage, MessaggioCambioUsername.class);
            Set<String> listaModifiche = configService.rinominaUtente(message.getOldUsername(), message.getNewUsername());
            LOGGER.info("RinominaUtente per il messaggio "+ stringMessage +" eseguito con successo "+
                        listaModifiche.stream().collect(Collectors.joining(", ")));
        } catch (IOException e) {
            LOGGER.error("Errore nella lettura del messaggio cambioUsername: "+ stringMessage, e);
            mailService.sendEmailError("Errore nella lettura del messaggio cambioUsername", stringMessage, false, true);
            throw e;
        }        
    }
    
    @SuppressWarnings("unused")
    private static class MessaggioCambioUsername {
        private String oldUsername;
        private String newUsername;
        public String getOldUsername() {
            return oldUsername;
        }
        public void setOldUsername(String oldUsername) {
            this.oldUsername = oldUsername;
        }
        public String getNewUsername() {
            return newUsername;
        }
        public void setNewUsername(String newUsername) {
            this.newUsername = newUsername;
        }
    }

}
