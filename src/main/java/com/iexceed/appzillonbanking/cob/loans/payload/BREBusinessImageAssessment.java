package com.iexceed.appzillonbanking.cob.loans.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BREBusinessImageAssessment {
    @JsonProperty("App_business_category")
    private BREAppBusinessCategory appBusinessCategory;

    @JsonProperty("Co_App_business_category")
    private BRECoAppBusinessCategory coAppBusinessCategory;
}
