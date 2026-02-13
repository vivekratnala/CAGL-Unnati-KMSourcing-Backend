package com.iexceed.appzillonbanking.cob.loans.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IncomeCalculatorRentalIncomeRequestFields {

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("state")
	private String state;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("typeOfBuilding")
	private String typeOfBuilding;

	@ApiModelProperty(required = true, example = "")
	@JsonProperty("rentalIncome")
	private int rentalIncome;

}
