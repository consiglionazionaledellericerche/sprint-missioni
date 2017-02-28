
package it.cnr.si.missioni.util.proxy.json.object.rimborso;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pg_nazione"
})
public class Nazione implements Serializable{

    @JsonProperty("pg_nazione")
    private Integer pgNazione;
    @JsonIgnore
    private Map<String, Serializable> additionalProperties = new HashMap<String, Serializable>();

    /**
     * 
     * @return
     *     The pgNazione
     */
    @JsonProperty("pg_nazione")
    public Integer getPgNazione() {
        return pgNazione;
    }

    /**
     * 
     * @param pgNazione
     *     The pg_nazione
     */
    @JsonProperty("pg_nazione")
    public void setPgNazione(Integer pgNazione) {
        this.pgNazione = pgNazione;
    }

    @JsonAnyGetter
    public Map<String, Serializable> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Serializable value) {
        this.additionalProperties.put(name, value);
    }

}
