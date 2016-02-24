package it.cnr.si.missioni.util.data;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
	"codice_uo",
	"ordine_da_validare"
})
public class UoForUsersSpecial {

	@JsonProperty("codice_uo")
	private String codice_uo;

	@JsonProperty("ordine_da_validare")
	private String ordine_da_validare;

	/**
	 *
	 * @return
	 * The codice_uo
	 */
	@JsonProperty("codice_uo")
	public String getCodice_uo() {
		return codice_uo;
	}

	/**
	 *
	 * @param codice_uo
	 * The codice_uo
	 */
	@JsonProperty("codice_uo")
	public void setCodice_uo(String codice_uo) {
		this.codice_uo = codice_uo;
	}

	@JsonProperty("ordine_da_validare")
	public String getOrdine_da_validare() {
		return ordine_da_validare;
	}

	@JsonProperty("ordine_da_validare")
	public void setOrdine_da_validare(String ordine_da_validare) {
		this.ordine_da_validare = ordine_da_validare;
	}

}