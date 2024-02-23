package it.cnr.si.missioni.domain.custom;

import java.util.List;

public class ListaComuniDTO {
    private List<ComuniDTO> results;

    public ListaComuniDTO() {
    }

    public List<ComuniDTO> getResults() {
        return results;
    }

    public void setResults(List<ComuniDTO> results) {
        this.results = results;
    }
}
