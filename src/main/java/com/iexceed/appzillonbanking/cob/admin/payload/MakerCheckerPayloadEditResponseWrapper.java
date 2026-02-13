package com.iexceed.appzillonbanking.cob.admin.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iexceed.appzillonbanking.cob.core.payload.ResponseHeader;

public class MakerCheckerPayloadEditResponseWrapper {

	@JsonProperty("ResponseHeader")
	private ResponseHeader responseHeader;

	@JsonProperty("makerCheckerPayloadRes")
	private MakerCheckerPayloadEditResponse makerCheckerPayloadResponse;

	public MakerCheckerPayloadEditResponse getMakerCheckerPayloadResponse() {
		return makerCheckerPayloadResponse;
	}

	public void setMakerCheckerPayloadResponse(MakerCheckerPayloadEditResponse makerCheckerPayloadResponse) {
		this.makerCheckerPayloadResponse = makerCheckerPayloadResponse;
	}

	public ResponseHeader getResponseHeader() {
		return responseHeader;
	}

	public void setResponseHeader(ResponseHeader responseHeader) {
		this.responseHeader = responseHeader;
	}

}
