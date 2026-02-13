package com.iexceed.appzillonbanking.cob.loans.payload;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveDeviationRaApplicationsReqFields {

    @JsonProperty("applicationId")
    private String applicationId;

    @JsonProperty("recordId")
    private String recordId;

    @JsonProperty("recordType")
    private String recordType;

    @JsonProperty("remarks")
    private String remarks;

    @JsonProperty("role")
    private String role;
}
