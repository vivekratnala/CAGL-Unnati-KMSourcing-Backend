package com.iexceed.appzillonbanking.cob.loans.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class BREVisibleServicesBoard {
    @ApiModelProperty(required = false, example = "[\"Haircut\", \"Shaving\", \"Hair Color\"]")
    @JsonProperty("services_names")
    private List<String> servicesNames = new ArrayList<>();

    @ApiModelProperty(required = false, example = "[100, 50, 500]")
    @JsonProperty("services_prices")
    private List<Integer> servicesPrices = new ArrayList<>();
}
