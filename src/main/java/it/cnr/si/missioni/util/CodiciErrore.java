package it.cnr.si.missioni.util;
/**
 * 
 * @author ISED Interfaccia che dichiara gli errori e i rispettivi codici
 * 
 * 
 */
public interface CodiciErrore {
	final static int ATT_DEBUG_LEVEL = 1; // valori possibili 1 e 2
	final static int OK = 0;
	final static int ERRGEN = 1;
	final static int ERRSQL = 2;
	final static int ERR_RICERCA_DATI_ISTITUTO = 3;
	final static int SEDE_NON_TROVATA = 4;
	final static int DIPENDENTE_NON_TROVATO = 5;
	final static String ERR_AGGIORNAMENTO_DATI_ISTITUTO = "ERR_AGGIORNAMENTO_DATI_ISTITUTO";
	final static String INVALID_REQUEST = "INVALID_REQUEST";
	final static String TARGA_GIA_INSERITA = "TARGA_ALREADY_EXISTS";
	final static String CAMPO_OBBLIGATORIO = "Campo Obbligatorio";
	final static String DATI_INCONGRUENTI = "DATI_INCONGRUENTI";
	final static String ERR_DATE_INCONGRUENTI = "ERR_DATE_INCONGRUENTI";
	public final static String[] text = {
			"OK",
			"ERRGEN",
			"ERRSQL",
			"DATI_NON_TROVATI",
			"SEDE_NON_TROVATA",
			"DIPENDENTE_NON_TROVATO",
			"ERRDIPNABILASS",
			"ERRASSSOVRAPP",
			"ERRASSNONCONS31IV_X",
			"ERRINASSFUORILIMASSSEDE",
			"ERRFINEASSFUORILIMASSSEDE",
			"ERRASSNONCONS31",
			"ERRASSNONCONS37I_III",
			"ERRASSNONCONS37IV_X",
			"ERRDECORRENZA",
			"ERRDECORRENZANULLA",
			"ERRSTRAORFUORILIM",
			"ERRDIPNABILCOMPET",
			"ERRSTRAORCOMPLFUORILIM",
			"ERRSEDEDISAGIATA",
			"ERRDUPLICAZIONESTRAO",
			"ERRREPERIBILITAINDIVIDUALE",
			"ERRCODNONVAL",
			"ERRSUPERATOTETTOREPERMENS",
			"ERRSUPERATOTETTOSTRAOSEDE",
			"ERRDIPENDENTINONELABORATI",
			"ATTESTATONONVALIDATO",
			"ATTESTATOGIAVALIDATO",
			"ERRPASSWORDVALIDAZIONEMANCANTE",
			"ERRPASSWORDLOGINMANCANTE",
			"ERRPASSWORDVALIDAZIONESCORRETTA",
			"ERRINSTRALCIODAHOST",
			"ERRINREGISTRASUHOST",
			"WARNINSTRALCIODAHOST",
			"WARNINREGISTRASUHOST",
			"ATTESTATO GIA VALIDATO",
			"SUCCESSO",
			"DATI_SQL_INESISTENTI",
			"SEDE_SQL_INESISTENTE",
			"ORARIO_SEDE_INESISTENTE",
			"ERRREGISTRAZIONEDUPLICATA",
			"NONEXISTENTERRCODE",
			"ERROVERFLOWTABELLA",
			"ERRGENERICOSQL",
			"ERRSQLNONDISPONIBILE",
			"ERRTERMINAZANOMALA",
			"WARNING-TIMEOUT",
			"ERRASSNONCONS31CL9801",
			"ERRASSNONCONS37CL9801",
			"ERRASSCONSSOLOLIVIAIIICL9801",
			"ERRASSCONSSOLOLIVIVAIXCL9801",
			"ERRFAREPRIMALOSTRALCIO",
			"ATTENZIONESTRALCIOGIAESEGUITO",
			"ATTENZIONE_REGISTRAZIONE_GIA_ESEGUITA_IN_PRECEDENZA",
			"ERRORE_COMPETENZA_NULLA",
			"ATTENZIONE: GIORNI COMPETENZE SUPERIORI A GIORNI PRESENZE <BR> (assegnati giorni presenza effettivi)",
			"ATTENZIONE: ESEGUIRE CONTROLLO TETTO", "ERRSTRALCIOINCORSO",
			"ERRREGISTRAZIONEINCORSO",
			"ERRORESISTEMASOVRACCARICO_RIPROVARE_PIU_TARDI", "ERRORE_SQL_TCAS",
			"ERRORE_SQL_ORACLE", "DB_SOVRACCARICO",
			"ERR_SQLDS_CANNOT_ROLLBACK", "OPERAZIONE_DISABILITATA",
			"REGISTRAZIONE_INCOMPLETA", "LDAP_NON_DISPONIBILE",
			"ERRORE NOME UTENTE O PASSWORD ERRATI",
			"ERRORE UTENTE NON ABILITATO", "ERRASSNONCONS203",
			"ERRORE NEL REPORT", "ERRORE PASSWORD SCADUTA",
			"ERRORE QUANTITA' BUONI PASTO",
			"ATTESTATO BUONI PASTO NON MODIFICABILE" };
}