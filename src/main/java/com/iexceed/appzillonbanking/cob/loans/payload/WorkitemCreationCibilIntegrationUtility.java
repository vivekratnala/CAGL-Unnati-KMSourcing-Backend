package com.iexceed.appzillonbanking.cob.loans.payload;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WorkitemCreationCibilIntegrationUtility {
	
	@JacksonXmlProperty(localName = "custPrefix")
	private String custPrefix;
    
	@JacksonXmlProperty(localName = "PID")
	private String pid;
    
	@JacksonXmlProperty(localName = "CustomerType")
	private String customerType;
    
	@JacksonXmlProperty(localName = "ServiceType")
	private String serviceType;
    
	@JacksonXmlProperty(localName = "Description")
	private String description;
    
	@JacksonXmlProperty(localName = "INSERTEDON")
	private String insertedOn;
    
	@JacksonXmlProperty(localName = "DG_Reference_FID")
	private String dgReferenceFid;

}
