package com.iexceed.appzillonbanking.cob.loans.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ToString
public class BREKirana {
    @ApiModelProperty(required = false, example = "Stationary")
    @JsonProperty("type_of_shop")
    private String typeOfShop = "Stationary";

    @ApiModelProperty(required = false, example = "happy shop")
    @JsonProperty("name_of_shop")
    private String nameOfShop = "happy shop";

    @ApiModelProperty(required = false, example = "Market Stall")
    @JsonProperty("shop_Setup")
    private String shopSetup = "Market Stall";

    @ApiModelProperty(required = false, example = "Market Area")
    @JsonProperty("surrounding_area_classification")
    private String surroundingAreaClassification = "Market Area";

    @ApiModelProperty(required = false, example = "Local Retail Market")
    @JsonProperty("market_classification")
    private String marketClassification = "Local Retail Market";

    @ApiModelProperty(required = false, example = "shop")
    @JsonProperty("business_location")
    private String businessLocation = "shop";

    @ApiModelProperty(required = false, example = "75.5")
    @JsonProperty("area_of_shop_sqft")
    private double areaOfShopSqft = 75.5;

    @ApiModelProperty(required = false, example = "100")
    @JsonProperty("area_of_godown")
    private int areaOfGodown = 100;

    @ApiModelProperty(required = false, example = "High")
    @JsonProperty("occupancy_level_of_shop")
    private String occupancyLevelOfShop = "High";

    @ApiModelProperty(required = false, example = "low")
    @JsonProperty("occupancy_level_of_godown")
    private String occupancyLevelOfGodown = "low";

    @JsonProperty("prominent_Sku_items")
    private List<String> prominentSkuItems = new ArrayList<>(Arrays.asList("books", "tiffin box", "water bottle"));

    @JsonProperty("Storage_of_bulk_items")
    private List<String> storageOfBulkItems = new ArrayList<>(Arrays.asList("Decorative Stuffs"));

    @JsonProperty("inventory_purchase_bill_records")
    private BREPurchaseItems inventoryPurchaseBillrecords ;

    @ApiModelProperty(required = false, example = "6")
    @JsonProperty("customer_Footfall_or_visible_crowd")
    private int customerFootfallOrVisibleCrowd = 6;

    @ApiModelProperty(required = false, example = "cash")
    @JsonProperty("payment_methods_visible")
    private String paymentMethodsVisible = "cash";

    @ApiModelProperty(required = false, example = "new")
    @JsonProperty("condition_of_shop")
    private String conditionOfShop = "new";

    @ApiModelProperty(required = false, example = "Yes")
    @JsonProperty("presence_of_fridge_or_freezer")
    private String presenceOfFridgeOrFreezer = "Yes";

    @ApiModelProperty(required = false, example = "Powder gali , mumbai")
    @JsonProperty("address_of_the_kirana")
    private String addressOfTheKirana = "Powder gali , mumbai";
}
