
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
    "cd_tipo_rapporto"
})
public class TipoRapporto implements Serializable {

    @JsonProperty("cd_tipo_rapporto")
    private String cdTipoRapporto;
    @JsonIgnore
    private Map<String, Serializable> additionalProperties = new HashMap<String, Serializable>();

    /**
     * 
     * @return
     *     The cdTipoRapporto
     */
    @JsonProperty("cd_tipo_rapporto")
    public String getCdTipoRapporto() {
        return cdTipoRapporto;
    }

    /**
     * 
     * @param cdTipoRapporto
     *     The cd_tipo_rapporto
     */
    @JsonProperty("cd_tipo_rapporto")
    public void setCdTipoRapporto(String cdTipoRapporto) {
        this.cdTipoRapporto = cdTipoRapporto;
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
