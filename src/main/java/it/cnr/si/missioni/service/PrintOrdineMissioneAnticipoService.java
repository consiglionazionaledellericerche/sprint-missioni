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

import it.cnr.jada.ejb.session.ComponentException;
import it.cnr.si.missioni.awesome.exception.AwesomeException;
import it.cnr.si.missioni.domain.custom.persistence.OrdineMissione;
import it.cnr.si.missioni.domain.custom.persistence.OrdineMissioneAnticipo;
import it.cnr.si.missioni.domain.custom.print.PrintOrdineMissioneAnticipo;
import it.cnr.si.missioni.util.Costanti;
import it.cnr.si.missioni.util.DateUtils;
import it.cnr.si.missioni.util.Utility;
import it.cnr.si.missioni.util.proxy.json.object.Account;
import it.cnr.si.missioni.util.proxy.json.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PrintOrdineMissioneAnticipoService {
    private final Logger log = LoggerFactory.getLogger(PrintOrdineMissioneAnticipoService.class);

    @Autowired
    private Environment env;

    @Autowired
    private PrintService printService;

    @Autowired
    private AccountService accountService;

    //    private PrintOrdineMissione getPrintOrdineMissione(OrdineMissione ordineMissione) throws AwesomeException, ComponentException {
    private PrintOrdineMissioneAnticipo getPrintOrdineMissioneAnticipo(OrdineMissioneAnticipo ordineMissioneAnticipo, String currentLogin) throws AwesomeException, ComponentException {
        OrdineMissione ordineMissione = ordineMissioneAnticipo.getOrdineMissione();
        Account account = accountService.loadAccountFromUsername(ordineMissione.getUid());
        PrintOrdineMissioneAnticipo printOrdineMissioneAnticipo = new PrintOrdineMissioneAnticipo();
        printOrdineMissioneAnticipo.setAnno(ordineMissione.getAnno());
        printOrdineMissioneAnticipo.setNumero(ordineMissione.getNumero());
        printOrdineMissioneAnticipo.setCodiceFiscaleRich(account.getCodice_fiscale());
        printOrdineMissioneAnticipo.setComuneResidenzaRich(Utility.nvl(ordineMissione.getComuneResidenzaRich()));
        if (account.getData_nascita() != null) {
            Date dataNas = DateUtils.parseDate(account.getData_nascita().substring(0, 10), "yyyy-MM-dd");
            printOrdineMissioneAnticipo.setDataDiNascitaRich(DateUtils.getDateAsString(dataNas, DateUtils.PATTERN_DATE));
        }
        printOrdineMissioneAnticipo.setDataFineMissione(DateUtils.getDateAsString(ordineMissione.getDataFineMissione(), DateUtils.PATTERN_DATETIME_NO_SEC));
        printOrdineMissioneAnticipo.setDataInizioMissione(DateUtils.getDateAsString(ordineMissione.getDataInizioMissione(), DateUtils.PATTERN_DATETIME_NO_SEC));
        printOrdineMissioneAnticipo.setDataInserimento(DateUtils.getDateAsString(ordineMissione.getDataInserimento(), DateUtils.PATTERN_DATE));
        printOrdineMissioneAnticipo.setDatoreLavoroRich(ordineMissione.getDatoreLavoroRich());

        printOrdineMissioneAnticipo.setDomicilioFiscaleRich(Utility.nvl(ordineMissione.getDomicilioFiscaleRich()));
        printOrdineMissioneAnticipo.setImportoPresunto(Utility.numberFormat(ordineMissione.getImportoPresunto()));
        printOrdineMissioneAnticipo.setIndirizzoResidenzaRich(Utility.nvl(ordineMissione.getIndirizzoResidenzaRich()));
        printOrdineMissioneAnticipo.setLivelloRich(ordineMissione.getLivelloRich() == null ? "" : ordineMissione.getLivelloRich());
        printOrdineMissioneAnticipo.setLuogoDiNascitaRich(account.getComune_nascita());
        printOrdineMissioneAnticipo.setMatricolaRich(account.getMatricola() != null ? account.getMatricola().toString() : "");
        printOrdineMissioneAnticipo.setCognomeRich(account.getCognome());
        printOrdineMissioneAnticipo.setNomeRich(account.getNome());
        printOrdineMissioneAnticipo.setNote(Utility.nvl(ordineMissioneAnticipo.getNote()));
        printOrdineMissioneAnticipo.setQualificaRich(ordineMissione.getQualificaRich() == null ? "" : ordineMissione.getQualificaRich());
        printOrdineMissioneAnticipo.setOggetto(ordineMissione.getOggetto());
        printOrdineMissioneAnticipo.setImportoAnticipo(Utility.numberFormat(ordineMissioneAnticipo.getImporto()));
        printOrdineMissioneAnticipo.setDataAnticipo(DateUtils.getDateAsString(ordineMissioneAnticipo.getDataRichiesta(), DateUtils.PATTERN_DATE));
        printOrdineMissioneAnticipo.setStato(ordineMissioneAnticipo.getDecodeStato());
        return printOrdineMissioneAnticipo;
    }

    public byte[] printOrdineMissioneAnticipo(OrdineMissioneAnticipo ordineMissioneAnticipo, String currentLogin) throws AwesomeException, ComponentException {
        String myJson = createJsonPrintOrdineMissioneAnticipo(ordineMissioneAnticipo, currentLogin);
        String nomeStampa = "";
        if (env != null && env.getProperty("spring.print." + Costanti.NOME_STAMPA_ANTICIPO) != null) {
            nomeStampa = env.getProperty("spring.print." + Costanti.NOME_STAMPA_ANTICIPO);
        } else {
            throw new ComponentException("Configurare il nome stampa dell'anticipo");
        }
        return printService.print(myJson, nomeStampa, ordineMissioneAnticipo.getId());
    }

    public String createJsonPrintOrdineMissioneAnticipo(OrdineMissioneAnticipo ordineMissioneAnticipo, String currentLogin) throws ComponentException {
        PrintOrdineMissioneAnticipo printOrdineMissioneAnticipo = getPrintOrdineMissioneAnticipo(ordineMissioneAnticipo, currentLogin);
        return printService.createJsonForPrint(printOrdineMissioneAnticipo);
    }

}
