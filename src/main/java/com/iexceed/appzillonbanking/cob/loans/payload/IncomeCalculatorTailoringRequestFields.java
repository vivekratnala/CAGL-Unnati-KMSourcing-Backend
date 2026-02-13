package com.iexceed.appzillonbanking.cob.loans.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IncomeCalculatorTailoringRequestFields {

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("businessLocation")
	private String businessLocation;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("businessPremise")
	private String businessPremise;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("numOfPowerTailoringMachine")
	private int numOfPowerTailoringMachine;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("numOfManualTailoringMachine")
	private int numOfManualTailoringMachine;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("isApplicantATailor")
	private String isApplicantATailor;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("isCoApplicantPartOfTailorBusiness")
	private String isCoApplicantPartOfTailorBusiness;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("numOfSalariedTailors")
	private int numOfSalariedTailors;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("workType")
	private String workType;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("stitchingFor")
	private String stitchingFor;

}
