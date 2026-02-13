package com.iexceed.appzillonbanking.cob.loans.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WorkitemCreationRequestOccupationDtls {

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("customerType")
	private String customerType;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("bussEmpEntityType")
	private String bussEmpEntityType;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("natureOfBussEmp")
	private String natureOfBussEmp;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("bussEmpActivity")
	private String bussEmpActivity;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("streetVendor")
	private String streetVendor;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("bussEmpStartDate")
	private String bussEmpStartDate;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("bussEmpVintage")
	private String bussEmpVintage;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("bussAddressProof")
	private String bussAddressProof;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("nameOfOrgEmployer")
	private String nameOfOrgEmployer;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("empProof")
	private String empProof;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("bussPremiseOwnership")
	private String bussPremiseOwnership;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("modeOfIncome")
	private String modeOfIncome;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("freqOfIncome")
	private String freqOfIncome;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("annualIncome")
	private String annualIncome;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("otherSrcOfIncome")
	private String otherSrcOfIncome;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("otherSrcAnnualIncome")
	private String otherSrcAnnualIncome;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("customerName")
	private String customerName;

}
