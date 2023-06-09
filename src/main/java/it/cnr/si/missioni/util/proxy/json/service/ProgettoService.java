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

package it.cnr.si.missioni.util.proxy.json.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.util.CodiciErrore;
import it.cnr.si.missioni.util.Costanti;
import it.cnr.si.missioni.util.Utility;
import it.cnr.si.missioni.util.proxy.json.JSONClause;
import it.cnr.si.missioni.util.proxy.json.object.Progetto;
import it.cnr.si.missioni.util.proxy.json.object.ProgettoJson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProgettoService {
    @Autowired
    private CommonService commonService;

    public Progetto loadModulo(Long pgProgetto, Integer anno, String uo) throws AwesomeException {

        if (pgProgetto != null) {
            if (uo != null && uo.startsWith(Costanti.CDS_SAC)) {
                uo = Costanti.UO_SAC_PROGETTI;
            }
            List<JSONClause> clauses = prepareJSONClause(pgProgetto);
            List<JSONClause> clausesFixedToAdd = prepareJSONClauseToAdd(anno, uo);
            String app = Costanti.APP_SIGLA;
            String url = Costanti.REST_MODULO;
            String risposta = commonService.process(clauses, app, url, clausesFixedToAdd);

            try {
                ObjectMapper mapper = new ObjectMapper();
                ProgettoJson progettoJson = mapper.readValue(risposta, ProgettoJson.class);
                if (progettoJson != null) {
                    List<Progetto> lista = progettoJson.getElements();
                    if (lista != null && !lista.isEmpty()) {
                        return lista.get(0);
                    }
                }
            } catch (Exception ex) {
                throw new AwesomeException(CodiciErrore.ERRGEN, "Errore nella lettura del file JSON per i moduli (" + Utility.getMessageException(ex) + ").");
            }
        }
        return null;
    }

    public List<JSONClause> prepareJSONClauseToAdd(Integer anno, String uo) {
        JSONClause clause = new JSONClause();
        clause.setFieldName("esercizio");
        clause.setFieldValue(anno);
        clause.setCondition("AND");
        clause.setOperator("=");
        List<JSONClause> clauses = new ArrayList<JSONClause>();
        clauses.add(clause);
        if (!StringUtils.isEmpty(uo)) {
            clause = new JSONClause();
            clause.setFieldName("cd_unita_organizzativa");
            clause.setFieldValue(uo);
            clause.setCondition("AND");
            clause.setOperator("=");
            clauses.add(clause);
        }
        return clauses;
    }

    public List<JSONClause> prepareJSONClause(Long pgProgetto) {
        JSONClause clause = new JSONClause();
        clause.setFieldName("pg_progetto");
        clause.setFieldValue(pgProgetto);
        clause.setCondition("AND");
        clause.setOperator("=");
        List<JSONClause> clauses = new ArrayList<JSONClause>();
        clauses.add(clause);
        return clauses;
    }

}
