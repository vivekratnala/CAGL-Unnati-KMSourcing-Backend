package com.iexceed.appzillonbanking.cob.deposit.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FetchDeleteUserRequestWrapper {

	@JsonProperty("apiRequest")
	private FetchDeleteUserRequest fetchDeleteUserRequest;

}