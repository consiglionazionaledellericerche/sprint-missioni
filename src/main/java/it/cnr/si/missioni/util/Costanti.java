package it.cnr.si.missioni.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @author ISED Interfaccia che dichiara gli errori e i rispettivi codici
 * 
 * 
 */
public class Costanti {
	public final static String REST_CDS =  "ConsCdSAction.json";
	public final static String REST_CDR =  "ConsCdRAction.json";
	public final static String REST_UO =  "ConsUnitaOrganizzativaAction.json";
	public final static String REST_NAZIONE =  "ConsNazioneAction.json";
	public final static String REST_MODULO =  "ConsProgettiAction.json";
	public final static String REST_GAE =  "ConsGAEAction.json";
	public final static String REST_VOCE =  "ConsCapitoloAction.json";
	public final static String REST_IMPEGNO =  "ConsImpegnoAction.json";
	public final static String REST_IMPEGNO_GAE = "ConsImpegnoGaeAction.json";
	public final static String REST_ACCOUNT = "json/userinfo/";
	public final static String REST_UO_DIRECTOR = "json/sedi";
	public final static String NOME_CACHE_PROXY = "cacheProxy";
	public final static String APP_SIGLA = "SIGLA";
	public final static String APP_SIPER = "SIPER";
	public final static String HEADER_FOR_PROXY_AUTHORIZATION = "x-proxy-authorization";
	public final static String STATO_ANNULLATO = "ANN";
	public final static String STATO_INSERITO = "INS";
	public final static String STATO_DEFINITIVO = "DEF";
	public final static String STATO_NON_INVIATO_FLUSSO = "INS";
	public final static String STATO_INVIATO_FLUSSO = "INV";
	public final static String STATO_APPROVATO_FLUSSO = "APP";
	public final static String STATO_CONFERMATO = "CON";
	public final static String MISSIONE_ITALIANA = "I";
	public final static String MISSIONE_ESTERA = "E";
	public final static String PRIORITA_CRITICA = "1";
	public final static String PRIORITA_IMPORTANTE = "3";
	public final static String PRIORITA_MEDIA = "5";
	public final static String TAM = "T";
	public final static String RIMBORSO_DOCUMENTATO = "R";
	public final static String RESIDENZA_DOMICILIO = "R";
	public final static String SEDE_LAVORO = "S";
	public final static String STATO_FIRMATO_FROM_CMIS = "FIRMATO";
	public final static String STATO_ANNULLATO_FROM_CMIS = "ANNULLATO";
	public final static String STATO_RESPINTO_UO_FROM_CMIS = "RESPINTO UO";
	public final static String STATO_RESPINTO_SPESA_FROM_CMIS = "RESPINTO SPESA";
	public final static String STATO_FIRMA_UO_FROM_CMIS = "FIRMA UO";
	public final static String STATO_FIRMA_SPESA_FROM_CMIS = "FIRMA SPESA";
	public final static int DEFAULT_VALUE_MAX_ITEM_FOR_PAGE_CACHE = 1000000;
    public static final String SPRING_PROFILE_DEVELOPMENT = "dev";
    public static final String SPRING_PROFILE_PRODUCTION = "prod";
    public static final String SYSTEM_ACCOUNT = "system";
	public final static String STATO_APPROVATO_PER_HOME = "A";
	public final static String STATO_RESPINTO_PER_HOME = "R";
	public final static String STATO_ANNULLATO_PER_HOME = "N";
	public final static String STATO_DA_AUTORIZZARE_PER_HOME = "D";
	public final static String STATO_DA_VALIDARE_PER_HOME = "V";
	public final static String STATO_DA_CONFERMARE_PER_HOME = "C";
	public final static String CDS_SAC = "000";
	public final static String UO_SAC_PROGETTI = "000.000";
	
	public final static Map<String, String> PRIORITA;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put(PRIORITA_CRITICA, "CRITICA");
        aMap.put(PRIORITA_IMPORTANTE, "IMPORTANTE");
        aMap.put(PRIORITA_MEDIA, "MEDIA");
        PRIORITA = Collections.unmodifiableMap(aMap);
    }

    public final static Map<String, String> TRATTAMENTO;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put(TAM, "Trattamento Alternativo di Missione");
        aMap.put(RIMBORSO_DOCUMENTATO, "Rimborso Documentato");
        TRATTAMENTO = Collections.unmodifiableMap(aMap);
    }

    public final static Map<String, String> PARTENZA_DA;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put(RESIDENZA_DOMICILIO, "Residenza/Domicilio Fiscale");
        aMap.put(SEDE_LAVORO, "Sede di Lavoro");
        PARTENZA_DA = Collections.unmodifiableMap(aMap);
    }

    public final static Map<String, String> STATO;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put(STATO_ANNULLATO, "Annullato");
        aMap.put(STATO_CONFERMATO, "Confermato");
        aMap.put(STATO_INSERITO, "Inserito");
        aMap.put(STATO_DEFINITIVO, "Definitivo");
        STATO = Collections.unmodifiableMap(aMap);
    }

    public final static Map<String, String> TIPO_MISSIONE;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put(MISSIONE_ITALIANA, "ITALIA");
        aMap.put(MISSIONE_ESTERA, "ESTERO");
        TIPO_MISSIONE = Collections.unmodifiableMap(aMap);
    }

    public final static Map<String, String> SI_NO;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put("S", "Si");
        aMap.put("N", "No");
        SI_NO = Collections.unmodifiableMap(aMap);
    }

    public final static Map<String, String> STATO_FLUSSO;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put(STATO_INVIATO_FLUSSO, "Inviato");
        aMap.put(STATO_INSERITO, "Non Inviato");
        aMap.put(STATO_ANNULLATO, "Annullato");
        aMap.put(STATO_APPROVATO_FLUSSO, "Approvato");
        STATO_FLUSSO = Collections.unmodifiableMap(aMap);
    }

    public final static Map<String, String> STATO_FLUSSO_FROM_CMIS;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put(STATO_ANNULLATO_FROM_CMIS, "Annullato");
        aMap.put(STATO_FIRMA_SPESA_FROM_CMIS, "Alla Firma Uo Spesa");
        aMap.put(STATO_FIRMA_UO_FROM_CMIS, "Alla Firma Uo");
        aMap.put(STATO_FIRMATO_FROM_CMIS, "Approvato");
        aMap.put(STATO_RESPINTO_SPESA_FROM_CMIS, "Respinto da Uo Spesa");
        aMap.put(STATO_RESPINTO_UO_FROM_CMIS, "Respinto da Uo");
        STATO_FLUSSO_FROM_CMIS = Collections.unmodifiableMap(aMap);
    }

}
