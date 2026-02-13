package com.iexceed.appzillonbanking.cob.loans.payload;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WorkitemCreationRequestInsuranceDtls {
	
	@ApiModelProperty(required = true, example = "")
	@JsonProperty("customerType")
	private String customerType;
	
	@ApiModelProperty(required = true, example = "")
	@JsonProperty("customerId")
	private String customerId;
	
	@ApiModelProperty(required = true, example = "")
	@JsonProperty("insuranceFor")
	private String insuranceFor;
	
	@ApiModelProperty(required = true, example = "")
	@JsonProperty("nomineeInsurance")
	private String nomineeInsurance;
	
	@ApiModelProperty(required = true, example = "")
	@JsonProperty("nomineeDob")
	private String nomineeDob;
	
	@ApiModelProperty(required = true, example = "")
	@JsonProperty("nomineeAge")
	private String nomineeAge;
	
	@ApiModelProperty(required = true, example = "")
	@JsonProperty("nomineeRelationship")
	private String nomineeRelationship;

}
