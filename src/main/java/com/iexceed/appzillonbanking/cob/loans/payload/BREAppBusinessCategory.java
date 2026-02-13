package com.iexceed.appzillonbanking.cob.loans.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BREAppBusinessCategory {
    @JsonProperty("Kirana")
    private BREKirana kirana;

    @JsonProperty("Dairy")
    private BREDairy dairy;

    @JsonProperty("Tailoring")
    private BRETailoring tailoring;

    @JsonProperty("Agriculture")
    private BREAgriculture agriculture;

    @JsonProperty("Restaurant_eatery")
    private BRERestaurantEatery restaurantEatery;

    @JsonProperty("Barber")
    private BREBarber barber;

    @JsonProperty("Animal_Husbandry")
    private BREAnimalHusbandry animalHusbandry;

    @JsonProperty("Cloth_shop")
    private BREClothShop clothShop;
}
