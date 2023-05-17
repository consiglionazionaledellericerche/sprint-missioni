
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

package it.cnr.si.missioni.util.proxy.json.object.rimborso;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "userContext",
        "oggettoBulk"
})
public class MissioneSigla {

    @JsonProperty("userContext")
    private UserContext userContext;
    @JsonProperty("oggettoBulk")
    private MissioneBulk oggettoBulk;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * @return The userContext
     */
    @JsonProperty("userContext")
    public UserContext getUserContext() {
        return userContext;
    }

    /**
     * @param userContext The userContext
     */
    @JsonProperty("userContext")
    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }

    /**
     * @return The oggettoBulk
     */
    @JsonProperty("oggettoBulk")
    public MissioneBulk getOggettoBulk() {
        return oggettoBulk;
    }

    /**
     * @param oggettoBulk The oggettoBulk
     */
    @JsonProperty("oggettoBulk")
    public void setOggettoBulk(MissioneBulk oggettoBulk) {
        this.oggettoBulk = oggettoBulk;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
