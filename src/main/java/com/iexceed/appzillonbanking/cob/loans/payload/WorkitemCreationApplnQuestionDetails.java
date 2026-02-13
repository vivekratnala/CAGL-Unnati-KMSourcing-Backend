package com.iexceed.appzillonbanking.cob.loans.payload;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class WorkitemCreationApplnQuestionDetails {
	
	@JacksonXmlProperty(localName = "PID")
	private String pid;
    
	@JacksonXmlProperty(localName = "Questions")
	private String questions;
    
	@JacksonXmlProperty(localName = "Options")
	private String options;
    
	@JacksonXmlProperty(localName = "Remarks")
	private String remarks;
    
	@JacksonXmlProperty(localName = "insertionOrderId")
	private String insertionOrderId;
    
	@JacksonXmlProperty(localName = "custPrefix")
	private String custPrefix;

}
