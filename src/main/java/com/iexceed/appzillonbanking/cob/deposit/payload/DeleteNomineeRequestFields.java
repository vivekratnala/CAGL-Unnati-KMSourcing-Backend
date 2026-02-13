package com.iexceed.appzillonbanking.cob.deposit.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class DeleteNomineeRequestFields {

	@ApiModelProperty(required = true, position = 4, example = "APZCOB")
	@JsonProperty("appId")
	private String appId;
	
	@ApiModelProperty(required = true, position = 3, example = "16185889475433589")
	@JsonProperty("applicationId")
	private String applicationId;
	
	@ApiModelProperty(required = true, position = 2, example = "1")
	@JsonProperty("versionNum")
	private int versionNum;
	
	//Below are to send to external service
	
	@ApiModelProperty(required = true, example = "22114451")
	@JsonProperty("customerId")
	private String customerId;	
	
	@ApiModelProperty(required = true, example = "16190967474451390")
	@JsonProperty("nomineeId")
	private BigDecimal nomineeId;
}