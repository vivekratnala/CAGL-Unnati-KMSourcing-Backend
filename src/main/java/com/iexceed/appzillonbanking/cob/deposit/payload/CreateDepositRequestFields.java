package com.iexceed.appzillonbanking.cob.deposit.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iexceed.appzillonbanking.cob.core.domain.ab.ApplicationMaster;
import com.iexceed.appzillonbanking.cob.core.domain.ab.DepositDtls;
import com.iexceed.appzillonbanking.cob.core.domain.ab.WorkflowDefinition;
import com.iexceed.appzillonbanking.cob.core.payload.AddressDetailsWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.ApplicationTimelineDtl;
import com.iexceed.appzillonbanking.cob.core.payload.NomineeDetailsWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.WorkFlowDetails;
import com.iexceed.appzillonbanking.cob.domain.ab.BankingFacilities;
import com.iexceed.appzillonbanking.cob.core.payload.FundAccountRequestFields;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class CreateDepositRequestFields {
	
	@ApiModelProperty(required = true, position = 1, example = "APZDEP")
	@JsonProperty("appId")
	private String appId;

	@ApiModelProperty(required = true, position = 2, example = "NEW00063")
	@JsonProperty("applicationId")
	private String applicationId;

	@ApiModelProperty(required = true, position = 3, example = "1")
	@JsonProperty("versionNum")
	private int versionNum;
	
	@ApiModelProperty(required = true, position = 4, example = "Y")
	@JsonProperty("isExistingCustomer")
	private String isExistingCustomer;		
	
	@ApiModelProperty(required = true, example = "")
	@JsonProperty("applicationMaster")
	private ApplicationMaster applicationMaster;
	
	@ApiModelProperty(required = true, example = "")
	@JsonProperty("depositDetails")
	private DepositDtls depositDetails;
	
	@ApiModelProperty(required = true, example = "")
	@JsonProperty("nomineeDetailsWrapperList")
	private List<NomineeDetailsWrapper> nomineeDetailsWrapperList;
	
	@ApiModelProperty(required = true, example = "")
	@JsonProperty("addressDetailsWrapperList")
	private List<AddressDetailsWrapper> addressDetailsWrapperList;
	
	@ApiModelProperty(required = true, example = "")
	@JsonProperty("workflow")
	private WorkFlowDetails workflow;
	
	@JsonProperty("applnWfDefinitionList")
	private List<WorkflowDefinition> applnWfDefinitionList;
	
	@JsonProperty("applicationTimelineDtl")
	private List<ApplicationTimelineDtl> applicationTimelineDtl;
	
	@JsonProperty("bankingFacilityList")
	private List<BankingFacilities> bankingFacilityList;
	
	@JsonProperty("fundDepositAccount")
	private FundAccountRequestFields fundDepositAccount;
}