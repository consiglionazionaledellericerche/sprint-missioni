package it.cnr.si.missioni.util.proxy.json.service;

import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.domain.custom.persistence.OrdineMissione;
import it.cnr.si.missioni.util.CodiciErrore;
import it.cnr.si.missioni.util.Costanti;
import it.cnr.si.missioni.util.Utility;
import it.cnr.si.missioni.util.proxy.json.JSONClause;
import it.cnr.si.missioni.util.proxy.json.object.Nazione;
import it.cnr.si.missioni.util.proxy.json.object.NazioneJson;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NazioneService {
	@Inject
    private CommonService commonService;

	public Nazione loadNazione(OrdineMissione ordineMissione) throws AwesomeException {
		if (ordineMissione.getNazione() != null){
			List<JSONClause> clauses = prepareJSONClause(ordineMissione);
			String app = Costanti.APP_SIGLA;
			String url = Costanti.REST_NAZIONE;
			String risposta = commonService.process(clauses, app, url);

			try {
				ObjectMapper mapper = new ObjectMapper();
				NazioneJson nazioneJson = mapper.readValue(risposta, NazioneJson.class);
				if (nazioneJson != null){
					List<Nazione> lista = nazioneJson.getElements();
					if (lista != null && !lista.isEmpty()){
						return lista.get(0);
					}
				}
			} catch (Exception ex) {
				throw new AwesomeException(CodiciErrore.ERRGEN, "Errore nella lettura del file JSON per le nazioni ("+Utility.getMessageException(ex)+").");
			}
		}
		return null;
	}

	public List<JSONClause> prepareJSONClause(OrdineMissione ordineMissione) {
		JSONClause clause = new JSONClause();
		clause.setFieldName("pg_nazione");
		clause.setFieldValue(ordineMissione.getNazione());
		clause.setCondition("AND");
		clause.setOperator("=");
		List<JSONClause> clauses = new ArrayList<JSONClause>();
		clauses.add(clause);
		return clauses;
	}

}