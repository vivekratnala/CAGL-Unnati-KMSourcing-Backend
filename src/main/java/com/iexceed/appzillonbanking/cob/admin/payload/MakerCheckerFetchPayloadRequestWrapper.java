package com.iexceed.appzillonbanking.cob.admin.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MakerCheckerFetchPayloadRequestWrapper {

	@JsonProperty("makerCheckerPayloadReq")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private MakerCheckerFetchPayloadRequest makerCheckerPayloadRequest;

	public MakerCheckerFetchPayloadRequest getMakerCheckerPayloadRequest() {
		return makerCheckerPayloadRequest;
	}

	public void setMakerCheckerPayloadRequest(MakerCheckerFetchPayloadRequest makerCheckerPayloadRequest) {
		this.makerCheckerPayloadRequest = makerCheckerPayloadRequest;
	}

	@Override
	public String toString() {
		return "MakerCheckerPayloadRequestWrapper [makerCheckerPayloadRequest=" + makerCheckerPayloadRequest + "]";
	}
	
	
	
}
