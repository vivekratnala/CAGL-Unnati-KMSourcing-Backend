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
public class DrivingLicenseOcrRequestFields {
	
	@JsonProperty("base64String")
	private String base64String;

	@JsonProperty("mimetype")
	private String mimetype;

	@JsonProperty("ttl")
	private String ttl;

}
