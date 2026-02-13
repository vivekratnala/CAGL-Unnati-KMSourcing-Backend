package com.iexceed.appzillonbanking.cob.loans.payload;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WorkitemCreationCustOtherSourceDetails {
	
	@JacksonXmlProperty(localName = "custPrefix")
	private String custPrefix;
    
	@JacksonXmlProperty(localName = "insertionOrderId")
	private String insertionOrderId;
    
	@JacksonXmlProperty(localName = "Cust_Name_list")
	private String custNameList;
    
	@JacksonXmlProperty(localName = "Customer_Type")
	private String customerType;
    
	@JacksonXmlProperty(localName = "Other_Src_of_Income")
	private String otherSrcOfIncome;
    
	@JacksonXmlProperty(localName = "Other_Src_Annual_Income")
	private String otherSrcAnnualIncome;
    
	@JacksonXmlProperty(localName = "fid")
	private String fid;

}
