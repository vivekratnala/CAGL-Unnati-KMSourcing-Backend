package com.iexceed.appzillonbanking.cob.loans.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoterFrontOcrRequestWrapper {

	@Override
	public String toString() {
		return "VoterFrontOcrRequestWrapper [apiRequest=" + apiRequest + "]";
	}

	@JsonProperty("apiRequest")
	private VoterFrontOcrRequest apiRequest;
}
