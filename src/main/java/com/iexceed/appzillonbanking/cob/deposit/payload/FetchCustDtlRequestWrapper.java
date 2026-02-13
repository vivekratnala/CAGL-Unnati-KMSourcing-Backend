package com.iexceed.appzillonbanking.cob.deposit.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FetchCustDtlRequestWrapper {
	@JsonProperty("apiRequest")
	private FetchCustDtlRequest apiRequest;
}
