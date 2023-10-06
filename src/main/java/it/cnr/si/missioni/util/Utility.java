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

package it.cnr.si.missioni.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.si.missioni.cmis.MimeTypes;
import it.cnr.si.missioni.util.proxy.json.object.sigla.ErrorRestSigla;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utility {

    public static final java.math.BigDecimal ZERO = new java.math.BigDecimal(0);
    private static final Logger log = LoggerFactory.getLogger(Utility.class);

    public static boolean equalsNull(Object object1, Object object2) {
        if (object1 == null && object2 == null)
            return true;
        else if ((object1 == null && object2 != null) || (object1 != null && object2 == null))
            return false;
        else if (object1 != null && object2 != null)
            return object1.equals(object2);
        return false;
    }

    /**
     * Restituisce true se i due oggetti sono uguali o sono entrambi null
     * false altrimenti
     */
    public static boolean equalsBulkNull(OggettoBulk object1, OggettoBulk object2) {
        if (object1 == null && object2 == null)
            return true;
        else if ((object1 == null && object2 != null) || (object1 != null && object2 == null))
            return false;
        else if (object1 != null && object2 != null)
            return object1.equalsByPrimaryKey(object2);
        return false;
    }

    public static BigDecimal nvl(BigDecimal imp) {
        if (imp != null)
            return imp.setScale(2);
        return ZERO;
    }

    public static String nvl(String str) {
        if (str != null)
            return str;
        return "";
    }

    public static String nvl(String str, String anotherValue) {
        if (str != null)
            return str;
        return anotherValue;
    }

    /**
     * Restituisce una Stringa ottenuta sostituendo
     * nella stringa sorgente alla stringa pattern la stringa replace,
     * se la stringa pattern non è presente restituisce la stringa sorgente
     */
    public static String replace(String source, String pattern, String replace) {
        if (source != null) {
            final int len = pattern.length();
            StringBuffer sb = new StringBuffer();
            int found = -1;
            int start = 0;

            while ((found = source.indexOf(pattern, start)) != -1) {
                sb.append(source, start, found);
                sb.append(replace);
                start = found + len;
            }

            sb.append(source.substring(start));
            return sb.toString();
        } else
            return null;
    }


    public static String lpad(double d, int size, char pad) {
        return lpad(Double.toString(d), size, pad);
    }

    public static String lpad(long l, int size, char pad) {
        return lpad(Long.toString(l), size, pad);
    }

    public static String lpad(String s, int size, char pad) {
        StringBuilder builder = new StringBuilder();
        while (builder.length() + s.length() < size) {
            builder.append(pad);
        }
        builder.append(s);
        return builder.toString();
    }

    public static Date getDateWithoutHours(Date data) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static ZonedDateTime getDateWithoutHours(ZonedDateTime data) {
        return data.truncatedTo(ChronoUnit.DAYS);
    }

    public static String numberFormat(BigDecimal importo) {
        if (importo != null) {
            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.ITALIAN);
            DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
            symbols.setGroupingSeparator('.');
            formatter.setDecimalFormatSymbols(symbols);
            formatter.setMaximumFractionDigits(2);
            formatter.setMinimumFractionDigits(2);
            return formatter.format((importo.setScale(2)).doubleValue());
        } else {
            return "";
        }
    }

    public static String getMessageException(Throwable e) {
        String obj = e.getLocalizedMessage() == null ? (e.getCause() == null ? ExceptionUtils.getStackTrace(e) : e.getCause().toString()) : e.getLocalizedMessage();
        log.debug("Errore", e);

        ErrorRestSigla errorRest = null;
        try {
            Class<?> classJson = ErrorRestSigla.class;
            errorRest = (ErrorRestSigla) new ObjectMapper().readValue(obj, classJson);
            return errorRest.getError();
        } catch (IOException ex) {
            log.info("Non Rest Generico SIGLA");
            try {
                JSONObject json = new JSONObject(obj);
                String message = json.getString("userMessage");
                if (message != null) {
                    return message;
                }
            } catch (JSONException e1) {
                log.info("Non userMessage SIGLA");
                try {
                    JSONObject json = new JSONObject(obj);
                    String message = json.getString("originalMessage");
                    if (message != null) {
                        return message;
                    }
                } catch (JSONException e2) {
                    log.info("Non originalMessage SIGLA");
                    return obj;
                }
            }
            return obj;
        }
    }

    public static MimeTypes getMimeType(String contentType) {
        for (MimeTypes m : MimeTypes.values()) {
            if (m.mimetype().equals(contentType)) {
                return m;
            }
        }
        return null;
    }

    public static String getUoSigla(String uo) {
        if (uo.contains(".")) {
            return uo;
        }
        return uo.substring(0, 3) + "." + uo.substring(3, 6);
    }

    public static String getUoSiper(String uo) {
        return Utility.replace(uo, ".", "");
    }

}
