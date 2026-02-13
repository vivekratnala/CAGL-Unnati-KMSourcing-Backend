package com.iexceed.appzillonbanking.cob.loans.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AdharRedactOcrRequestFields {

	@JsonProperty("imageUrl")
	private String imageUrl;

	@JsonProperty("clientRefId")
	private String clientRefId;

	@JsonProperty("maskAadhaarNumber")
	private String maskAadhaarNumber;

	@JsonProperty("outputmaskedAadhaar")
	private String outputmaskedAadhaar;

	@JsonProperty("returnMaskedImageAsBase")
	private String returnMaskedImageAsBase;

	@JsonProperty("isBlackWhiteCheck")
	private String isBlackWhiteCheck;

}
