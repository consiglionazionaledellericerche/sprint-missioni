'use strict';

/* Constants */

missioniApp.constant('USER_ROLES', {
        'all': '*',
        'admin': 'ROLE_ADMIN',
        'user': 'ROLE_USER'
    });

missioniApp.constant('TIPO_PAGAMENTO', {
        'ALTRO': 'A',
        'BANCA_ITALIA': 'I',
        'POSTA': 'P',
        'BANCA': 'B',
        'BANCA_ESTERA': 'N',
        'QUIETANZA': 'Q'
    });

missioniApp.constant('APP_FOR_REST', {
        'SIGLA': 'SIGLA',
        'SIPER': 'SIPER',
        'OIL': 'OIL'
    });

missioniApp.constant('URL_REST', {
        'PROXY': 'proxy/',
        'STANDARD': 'api/proxy/'
    });
missioniApp.constant('SIPER_REST', {
        'PERSONS_FOR_UO': 'json/sedi/',
        'GET_PERSON': 'json/userinfo/'
    });

missioniApp.constant('OIL_REST', {
        'ISTANZA': 'HDSiper',
        'CATEGORIE': 'catg/HDSiper/119',
        'NEW_PROBLEM': 'pest/'
    });

missioniApp.constant('SIGLA_REST', {
        'CDS': 'ConsCdSAction.json',
        'CDR': 'ConsCdRAction.json',
        'UO': 'ConsUnitaOrganizzativaAction.json',
        'NAZIONE': 'ConsNazioneAction.json',
        'MODULO': 'ConsProgettiAction.json',
        'GAE': 'ConsGAEAction.json',
        'VOCE': 'ConsCapitoloAction.json',
        'IMPEGNO': 'ConsImpegnoAction.json',
        'IMPEGNO_GAE': 'ConsImpegnoGaeAction.json',
        'TERZO': 'ConsTerzoAction.json',
        'INQUADRAMENTO': 'ConsInquadramentoAction.json',
        'MANDATO': 'ConsMandatoRevRestAction.json',
        'DIVISA': 'restservice/servizirest/getDivisa',
        'DATI_DIVISA': 'ConsDivisaAction.json',
        'TIPO_PASTO': 'ConsMissioneTipoPastoAction.json',
        'TIPO_SPESA': 'ConsMissioneTipoSpesaAction.json',
        'BANCA': 'ConsBancaAction.json',
        'TERZO_PER_COMPENSO': 'ConsTerzoPerCompensoAction.json',
        'RIMBORSO_KM': 'ConsMissioneRimborsoKmAction.json',
        'VALIDA_RIGA_RIMBORSO': 'restapi/missioni/validaMassimaleSpesa',
        'MOD_PAGAMENTO': 'ConsModalitaPagamentoAction.json',
        'MANDATO_MISSIONE_SIGLA': 'ConsRiepilogoSiopeMandatiRestAction.json'
    });

missioniApp.constant('COSTANTI', {
        'UTENTE_SPECIALE': 'app.missioni',
        'FORMATO_DATA': 'dd/MM/yyyy',
        'CDS_SAC': 'ASR',
        'ANNO_PARTENZA': 2017,
        'UO_STANDARD_SAC': 'ASR.000',
        'DEFAULT_VALUE_MAX_ITEM_FOR_PAGE_SIGLA_REST':4999,
        'DEFAULT_MAX_FILE_SIZE':1024000
    });

/*
Languages codes are ISO_639-1 codes, see http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes
They are written in English to avoid character encoding issues (not a perfect solution)
missioniApp.constant('LANGUAGES', {
        'en': 'English',
        'it': 'Italiano'
    });
*/
missioniApp.constant('LANGUAGES', {
        'it': 'Italiano'
    });
