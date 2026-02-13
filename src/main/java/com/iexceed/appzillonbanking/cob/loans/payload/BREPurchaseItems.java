package com.iexceed.appzillonbanking.cob.loans.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class BREPurchaseItems {
    @JsonProperty("purchase_item_names")
    private List<String> purchaseItemNames = new ArrayList<>();

    @JsonProperty("purchase_item_prices")
    private List<Integer> purchaseItemPrices = new ArrayList<>();
}
