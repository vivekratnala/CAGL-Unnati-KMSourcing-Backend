package com.iexceed.appzillonbanking.cob.loans.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IncomeCalculatorAgricultureRequestFields {

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("state")
	private String state;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("cropType")
	private String cropType;

	@ApiModelProperty(required = false, example = "")
	@JsonProperty("crop")
	private String crop;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("maxProductionPerAcre")
	private String maxProductionPerAcre;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("totalAcres")
	private String totalAcres;
}
