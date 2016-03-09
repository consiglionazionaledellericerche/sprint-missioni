package it.cnr.si.missioni.util.proxy;

import it.cnr.si.missioni.util.proxy.json.object.CommonJsonRest;
import it.cnr.si.missioni.util.proxy.json.object.RestServiceBean;

import java.io.Serializable;

public class ResultProxy implements Serializable {
    private String type;
    private int status;
    private String body;
	CommonJsonRest<RestServiceBean> commonJsonResponse;
    
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public CommonJsonRest<RestServiceBean> getCommonJsonResponse() {
		return commonJsonResponse;
	}
	public void setCommonJsonResponse(
			CommonJsonRest<RestServiceBean> commonJsonResponse) {
		this.commonJsonResponse = commonJsonResponse;
	}

}
