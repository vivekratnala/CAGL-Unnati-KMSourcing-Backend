package com.iexceed.appzillonbanking.cob.core.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoanDetailsPayload {
	
	@JsonProperty("loanPurpose")
	private String loanPurpose;

	@JsonProperty("modeOfSecurity")
	private String modeOfSecurity;
	
	@JsonProperty("subCategory")
	private String subCategory;

	@JsonProperty("modeOfDisbursement")
	private String modeOfDisbursement;
	
	@JsonProperty("languageForCommunction")
	private String languageForCommunction;
	
	@JsonProperty("language")
	private String language;
	
	@JsonProperty("frequencyOfRepayment")
	private String frequencyOfRepayment;
	
}