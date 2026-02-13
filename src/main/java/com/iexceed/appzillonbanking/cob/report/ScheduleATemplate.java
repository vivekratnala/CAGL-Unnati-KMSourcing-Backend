package com.iexceed.appzillonbanking.cob.report;

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.component.ComponentBuilder;
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder;
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder;
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder;
import net.sf.dynamicreports.report.builder.style.FontBuilder;
import net.sf.dynamicreports.report.builder.style.ReportStyleBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.constant.HorizontalImageAlignment;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.Markup;
import net.sf.dynamicreports.report.constant.PageOrientation;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.constant.VerticalAlignment;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.iexceed.appzillonbanking.cob.core.domain.ab.AddressDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.ApplicationMaster;
import com.iexceed.appzillonbanking.cob.core.domain.ab.BCMPIIncomeDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.BCMPILoanObligations;
import com.iexceed.appzillonbanking.cob.core.domain.ab.BCMPIOtherDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.BCMPIStageVerification;
import com.iexceed.appzillonbanking.cob.core.domain.ab.BankDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.CustomerDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.DeviationRATracker;
import com.iexceed.appzillonbanking.cob.core.domain.ab.ExistingLoanDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.InsuranceDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.LoanDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.NomineeDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.OccupationDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.ProductDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.SanctionMaster;
import com.iexceed.appzillonbanking.cob.core.payload.Address;
import com.iexceed.appzillonbanking.cob.core.payload.AddressDetailsPayload;
import com.iexceed.appzillonbanking.cob.core.payload.AddressDetailsWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.BankDetailsPayload;
import com.iexceed.appzillonbanking.cob.core.payload.BankDetailsWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.CibilDetailsPayload;
import com.iexceed.appzillonbanking.cob.core.payload.CibilDetailsWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.CustomerDetailsPayload;
import com.iexceed.appzillonbanking.cob.core.payload.ExistingLoanDetailsWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.InsuranceDetailsWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.LoanDetailsPayload;
import com.iexceed.appzillonbanking.cob.core.payload.NomineeDetailsWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.OccupationDetailsWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.RepaymentSchedule;
import com.iexceed.appzillonbanking.cob.core.payload.Response;
import com.iexceed.appzillonbanking.cob.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.cob.core.repository.ab.ApplicationMasterRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.BankDetailsRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.ProductDetailsrepository;
import com.iexceed.appzillonbanking.cob.core.utils.AppStatus;
import com.iexceed.appzillonbanking.cob.core.utils.CobFlagsProperties;
import com.iexceed.appzillonbanking.cob.core.utils.CommonUtils;
import com.iexceed.appzillonbanking.cob.core.utils.Constants;
import com.iexceed.appzillonbanking.cob.core.utils.ResponseCodes;
import com.iexceed.appzillonbanking.cob.loans.payload.ApplyLoanRequestFields;
import com.iexceed.appzillonbanking.cob.loans.payload.BCMPIIncomeDetailsWrapper;
import com.iexceed.appzillonbanking.cob.loans.payload.BCMPIOtherDetailsWrapper;
import com.iexceed.appzillonbanking.cob.loans.payload.FetchAppRequest;
import com.iexceed.appzillonbanking.cob.loans.payload.FetchAppRequestFields;
import com.iexceed.appzillonbanking.cob.loans.payload.LoanObligationsWrapper;
import com.iexceed.appzillonbanking.cob.loans.service.LoanService;
import com.iexceed.appzillonbanking.cob.payload.CustomerDataFields;
import com.iexceed.appzillonbanking.cob.payload.FetchDeleteUserFields;
import com.iexceed.appzillonbanking.cob.payload.FetchDeleteUserRequest;
import com.iexceed.appzillonbanking.cob.service.COBService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;



public class ScheduleATemplate {
	private static final Logger logger = LogManager.getLogger(ScheduleATemplate.class); 
	private StyleBuilder borderedStyle, boldText, boldCenteredStyle, boldTextWithBorder, boldLeftStyle , rightStyle, leftStyle;

	static String space = "\u00a0\u00a0\u00a0";
	
	private String applicantCustId="";
	private String coApplicantCustId ="";
	private String applicantName = "";
	private String coApplicantName = "";
	private CustomerDetails applicantCustDtls = null;
	private CustomerDetails coApplicantCustDtls = null;
	String appltGender ="";
	String coAppltGender ="";
	String interestRate = "";
	
	public ScheduleATemplate() {
		 
		borderedStyle = stl.style(stl.penThin()).setPadding(5);
		boldTextWithBorder = stl.style(stl.penThin()).setPadding(5).bold();
		boldCenteredStyle = stl.style().bold().setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
		boldText = stl.style().bold();
		boldLeftStyle = stl.style().bold().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
		
//		StyleBuilder headerStyle = stl.style().setFontSize(20).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER).bold();
	    rightStyle = stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT);
	    leftStyle = stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
	    
	    
	}

	public String generatePdfForDbKit(JSONObject keysForContent, String filePath, CustomerDataFields custmrDataFields, String productName, List<RepaymentSchedule> repaymentList, String language) throws DRException, IOException {
			
		/* Basic Application Details */
		CustomerDetailsPayload payload1 = null;
		CustomerDetailsPayload payload2 =null;
		Gson gsonObj = new Gson();
		for(CustomerDetails custDtl : custmrDataFields.getCustomerDetailsList()) {
			logger.debug("customer Type : " + custDtl.getCustomerType());
			if(custDtl.getCustomerType().equalsIgnoreCase("Applicant")) { 
				 applicantCustId = String.valueOf(custDtl.getCustDtlId());
				 logger.debug("applicantCustId : " + applicantCustId);
				 applicantName = custDtl.getCustomerName();
				 
				payload1  = gsonObj.fromJson(custDtl.getPayloadColumn(), CustomerDetailsPayload.class);
				logger.debug("custApplicantPayload :" + payload1);
				applicantCustDtls = custDtl;
				appltGender = payload1.getGender();
			}else if(custDtl.getCustomerType().equalsIgnoreCase("Co-App")) {
				coApplicantCustId = String.valueOf(custDtl.getCustDtlId());
				coApplicantName = custDtl.getCustomerName();
				logger.debug("coApplicantCustId : " + coApplicantCustId);
				payload2  = gsonObj.fromJson(custDtl.getPayloadColumn(), CustomerDetailsPayload.class);
				logger.debug("custCo-ApplicantPayload :" + payload2);
				coAppltGender = payload2.getGender();
				coApplicantCustDtls = custDtl;
			}
		}
		Gson gsonObj2 = new Gson();
		
		CibilDetailsPayload cibilPayloadCoApp = null;
		int totalInsurance = 0;
		for (CibilDetailsWrapper cibilDetailsWrapper : custmrDataFields.getCibilDetailsWrapperList()) {
			String custId = cibilDetailsWrapper.getCibilDetails().getCustDtlId().toString();
//		String customerType = applicant ? applicantCustId : coApplicantCustId;
			
			logger.debug("CreditDetailsPayload Payload : " + cibilPayloadCoApp);
			if (custId.equals(coApplicantCustId)) {
				cibilPayloadCoApp = gsonObj.fromJson(
						cibilDetailsWrapper.getCibilDetails().getPayloadColumn(), CibilDetailsPayload.class);	
				

				totalInsurance = toFindSum(cibilPayloadCoApp.getInsuranceChargeJoint(),
						cibilPayloadCoApp.getInsuranceChargeMember(), cibilPayloadCoApp.getInsuranceChargeSpouse());
//				int netOffAmt = toFindSum(outstandingAmount, cibilPayloadCoApp.getStampDutyCharge(),
//						String.valueOf(totalInsurance), cibilPayloadCoApp.getProcessingFees());
//				int postNetOff = toFindDifference(cibilPayloadCoApp.getEligibleAmt(),String.valueOf(netOffAmt));
//				int postNetOff2 = Integer.parseInt(bmLoanAmount)-netOffAmt;
			}
		
		}
		
//		BigDecimal sactionAmtDb = custmrDataFields.getLoanDetails().getSanctionedLoanAmount();
////		String sactionAmt = String.valueOf(sactionAmtDb == null ? "" : sactionAmtDb.toPlainString());
//		Long outstandingAmountL = 0L;
//		 try {
//			 outstandingAmountL = Long.parseLong(outstandingAmount);
//		    } catch (NumberFormatException e) {
//		        // Log the exception if needed
//		    	outstandingAmountL = 0L; // Or any default value you prefer
//		    }
//		 
//		String outStdgAmtInWords = convert(outstandingAmountL);
//		long amount = sactionAmtDb == null ? 0L : sactionAmtDb.longValue();
//		String snAmtInWords = convert(amount);
		
		int noOfEPIs = repaymentList.size();
		logger.debug("Number of Records: " + noOfEPIs);
		
//		String repymtStartDate = "";
		String emi = "";
		String firstEmi = "";
		BigDecimal totalInterest = BigDecimal.ZERO;
		if( repaymentList.size() > 0) {
			firstEmi = repaymentList.get(0).getTotalDue();
			emi = repaymentList.get(2).getTotalDue(); 
			
//			String startDate = repaymentList.get(0).getDate();	
//			try {
//				repymtStartDate = CommonUtils.dateFormat2(startDate);
//			}catch (Exception e) {
//				logger.debug("error while date format " + e);
//			}	

			for (RepaymentSchedule schedule : repaymentList) {
				if (schedule.getInterest() != null && !schedule.getInterest().isEmpty()) {
					try {
						BigDecimal interest1 = new BigDecimal(schedule.getInterest());
						totalInterest = totalInterest.add(interest1);
					} catch (NumberFormatException e) {
						logger.error("Invalid interest value: " + e);
					}
				}
			}
		}
		
		
		JasperReportBuilder report = new JasperReportBuilder();
		JasperReportBuilder subReport = new JasperReportBuilder();
		JasperReportBuilder subReport1 = new JasperReportBuilder();
		JasperReportBuilder subReport2 = new JasperReportBuilder();
		JasperReportBuilder subReport3 = new JasperReportBuilder();
		JasperReportBuilder subReport4 = new JasperReportBuilder();
		JasperReportBuilder subReport6 = new JasperReportBuilder();
		JasperReportBuilder subReport7 = new JasperReportBuilder();
		JasperReportBuilder subReport8 = new JasperReportBuilder();
		JasperReportBuilder subReport9 = new JasperReportBuilder();
		
		JRDataSource emptyDataSource = new JREmptyDataSource(1);

		subReport.setDataSource(emptyDataSource);
		subReport1.setDataSource(emptyDataSource);
		subReport2.setDataSource(emptyDataSource);
		subReport3.setDataSource(emptyDataSource);
		subReport4.setDataSource(emptyDataSource);
		
		subReport6.setDataSource(emptyDataSource);
		subReport7.setDataSource(emptyDataSource);
		subReport8.setDataSource(emptyDataSource);
		subReport9.setDataSource(emptyDataSource);
		
		report.setPageFormat(PageType.A4, PageOrientation.PORTRAIT).setPageMargin(DynamicReports.margin(30));

		/* Basic Application Details */
		
		subReport.title(cmp.text(keysForContent.getString("applicationName")).setMarkup(Markup.HTML).setStyle(boldCenteredStyle.setFontSize(14)));
		subReport.title(cmp.text(""));
		subReport.setDataSource(emptyDataSource);
		subReport1.title(getParticularsAndDetailsForScheduleA(keysForContent, custmrDataFields)).title(cmp.text(""));
		subReport1.setDataSource(emptyDataSource);
		subReport2.title(getBorrowerDetailsForScheduleA(keysForContent, custmrDataFields, payload1)).title(cmp.text(""));
		subReport2.setDataSource(emptyDataSource);
		subReport3.title(getLoanDetailsForScheduleA(keysForContent, custmrDataFields, cibilPayloadCoApp, productName)).title(cmp.text(""));
		subReport3.setDataSource(emptyDataSource);
		subReport4.title(getLoanAmortizationForScheduleA(keysForContent, custmrDataFields, cibilPayloadCoApp, noOfEPIs, firstEmi, emi)).title(cmp.text(""));
		subReport4.setDataSource(emptyDataSource);
		subReport6.title(getDetailChargesForScheduleA(keysForContent, cibilPayloadCoApp)).title(cmp.text(""));
		subReport6.setDataSource(emptyDataSource);
		subReport7.title(getKfsSummaryDetails(keysForContent, cibilPayloadCoApp, custmrDataFields, productName, totalInsurance, noOfEPIs, emi, totalInterest)).title(cmp.text(""));
		subReport7.setDataSource(emptyDataSource);	
		subReport8.title(cmp.text(keysForContent.getString("confirmation1")).setMarkup(Markup.HTML));
		subReport8.setDataSource(emptyDataSource);
		subReport9.title(cmp.text(keysForContent.getString("confirmation2")).setMarkup(Markup.HTML));
		subReport9.setDataSource(emptyDataSource);

//		report.setPageFormat(PageType.A4, PageOrientation.PORTRAIT).setPageMargin(DynamicReports.margin(30));
		
		report
		    .setPageFormat(PageType.A4, PageOrientation.PORTRAIT)
		    .setPageMargin(DynamicReports.margin(30))
	//	    .pageFooter(cmp.pageNumber().setStyle(leftStyle))
		    .pageFooter(
		    	    cmp.verticalList(Signatory(keysForContent))
		    	        .setFixedHeight(60)
		    	)
		    
//		    .pageFooter(Signatory(keysForContent))
		    .setDataSource(new JREmptyDataSource(1)) 
		   
		    .detail(
				    cmp.verticalList(
				    	cmp.subreport(subReport),	
				        cmp.subreport(subReport1),
				        cmp.subreport(subReport2),
				        cmp.subreport(subReport3),
				        cmp.subreport(subReport4),
				        cmp.subreport(subReport6),
				        cmp.subreport(subReport7),
				        cmp.subreport(subReport8),
				        cmp.subreport(subReport9)
//				        cmp.verticalGap(15)
	
				    )
				);
		
//	        FileOutputStream fos = new FileOutputStream(filePath);
//	        try {
//	            report.toPdf(fos);
//	        } catch (DRException e) {
//	            
//	        }
//	        fos.close();
//	        byte[] inputfile = Files.readAllBytes(Paths.get(filePath));
//	        byte[] encodedBytes = Base64.getEncoder().encode(inputfile);
//	        return new String(encodedBytes);
		
       try {
	            String[] newVrnclrLanguageArr = Constants.NEW_VERNCLR_LANGUAGES.split(",");
	            logger.debug("inputLanguage " + language);
	            
	            boolean isValidLanguage = Arrays.stream(newVrnclrLanguageArr)
	                    .anyMatch(lang -> lang.equalsIgnoreCase(language));

	            if (isValidLanguage) {
	                // Generate HTML in-memory, no file creation
	                ByteArrayOutputStream htmlOut = new ByteArrayOutputStream();
	                report.toHtml(htmlOut);
	                
	                // Return raw HTML instead of Base64
	                return htmlOut.toString(StandardCharsets.UTF_8.name());
	            } else {
	                // Normal PDF flow (write to disk)
	                try (FileOutputStream fos = new FileOutputStream(filePath)) {
	                    report.toPdf(fos);
	                }

	                byte[] inputfile = Files.readAllBytes(Paths.get(filePath));
	                byte[] encodedBytes = Base64.getEncoder().encode(inputfile);
	                return new String(encodedBytes);
	            }

	        } catch (Exception e) {
	            throw e;
	        }		

	}

	private TextFieldBuilder<String> createTextField(String label) {
		return cmp.text(label).setMarkup(Markup.HTML).setStyle(boldCenteredStyle);
	}

	public static String getTodayData() {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		String strDate = formatter.format(date);
		return strDate;
	}

	public static String getYearandMonth() {
		Date today = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
		return dayOfMonth + "," + month + "," + year;

	}

	public static String dateFormatyyyymmddtoddmmyyyy(String DOB) {
		String ds1 = DOB;
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy");
		String ds2 = null;
		try {
			ds2 = sdf2.format(sdf1.parse(ds1));
		} catch (ParseException e) {
			
			
		}
		System.out.println(ds2);
		return ds2;
	}

	/*public static String formatCurrency(String amount) {
		double d = Double.parseDouble(amount);
		//DecimalFormat f = new DecimalFormat("#,##,##0.00");
		return f.format(d);
	}*/

private ComponentBuilder<?, ?> createSingleHorizontalListForScheduleA(String Key) {
		
		HorizontalListBuilder horizontalList = cmp.horizontalList();		
//		horizontalList.add(cmp.text(Key).setStyle(boldTextWithBorder).setWidth(100));

		 horizontalList.add(cmp.text(Key).setMarkup(Markup.HTML)
			        .setStyle(stl.style(boldTextWithBorder).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER))
			        .setWidth(100));
		 
		return horizontalList;

	}

	private ComponentBuilder<?, ?> createTwoHorizontalListForScheduleAHeader(String Key, String value) {
		
		HorizontalListBuilder horizontalList = cmp.horizontalList();
		
		horizontalList.add(cmp.text(Key).setMarkup(Markup.HTML).setStyle(stl.style(boldTextWithBorder).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)).setWidth(40));
		horizontalList.add(cmp.text(value).setMarkup(Markup.HTML).setStyle(stl.style(boldTextWithBorder).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)).setWidth(60));
	
		return horizontalList;
	}

    private ComponentBuilder<?, ?> Signatory1(JSONObject keysForContent) {
        VerticalListBuilder verticalList = cmp.verticalList();
        String blank = " .............";
        verticalList.add(cmp.horizontalList(
                        cmp.text(keysForContent.getString("applicantNameAndSign") + ": " + applicantName+" &"+blank)
                                .setMarkup(Markup.HTML).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)),
                        cmp.text(keysForContent.getString("authorizedSignatory") + ": " + blank).setMarkup(Markup.HTML).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT))
                )
        );
        verticalList.add(cmp.horizontalList(
                        cmp.text(keysForContent.getString("coapplicantNameAndSign") + ": " + coApplicantName+" &"+blank)
                                .setMarkup(Markup.HTML).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))
                )
        );
        return verticalList;
    }

    private ComponentBuilder<?, ?> Signatory(JSONObject keysForContent) {
        VerticalListBuilder verticalList = cmp.verticalList();
        String blank = " .........";
        verticalList.add(cmp.horizontalList(
                cmp.text(keysForContent.getString("applicantNameAndSign") + ": " + applicantName+" &"+blank)
                        .setMarkup(Markup.HTML).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT)))
        );
        verticalList.add(cmp.horizontalList(
                        cmp.text(keysForContent.getString("coapplicantNameAndSign") + ": " + coApplicantName+" &"+blank)
                                .setMarkup(Markup.HTML).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.LEFT))
                )
        );
        verticalList.add(cmp.horizontalList(
                cmp.text(keysForContent.getString("authorizedSignatory") + ": " + blank).setMarkup(Markup.HTML).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT)))
        );

        return verticalList;
    }
	
	private ComponentBuilder<?, ?> createTwoHorizontalList(String Key, String value, ReportStyleBuilder style) {

		HorizontalListBuilder horizontalList = cmp.horizontalList();

		horizontalList.add(cmp.text(Key).setMarkup(Markup.HTML).setStyle(style));
		horizontalList.add(cmp.text(value).setMarkup(Markup.HTML).setStyle(style));

		return horizontalList;
	}
	private ComponentBuilder<?, ?> createTwoHorizontalListForScheduleA(String Key, String value) {
		
		HorizontalListBuilder horizontalList = cmp.horizontalList();

		// for (int i = 0; i < columns; i++) {

//		horizontalList.add(cmp.text(Key).setStyle(borderedStyle).setStyle(boldTextWithBorder).setWidth(25));
//		horizontalList.add(cmp.text(value).setStyle(borderedStyle).setWidth(75));
		
		horizontalList.add(cmp.text(Key).setMarkup(Markup.HTML).setStyle(borderedStyle).setWidth(40));
		horizontalList.add(cmp.text(value).setMarkup(Markup.HTML).setStyle(borderedStyle).setWidth(60));
		// horizontalList.add(createVerticalList("6","5"));

		// }

		return horizontalList;

	}
// For Schedule A	
private ComponentBuilder<?, ?> getParticularsAndDetailsForScheduleA(JSONObject keysForContent, CustomerDataFields custmrDataFields) {
		
		VerticalListBuilder verticalList = cmp.verticalList();

		verticalList.add(createTwoHorizontalListForScheduleAHeader(keysForContent.getString("particulars"), keysForContent.getString("details")));
		verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("agreementDate"), CommonUtils.getCurDate()));
		verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("agreementPlace"), custmrDataFields.getApplicationMaster().getBranchName()));
		verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("branchDetails"), custmrDataFields.getApplicationMaster().getBranchName()));
		verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("loanAccountNumber"), custmrDataFields.getLoanDetails().getT24LoanId()));
		
		return verticalList;	
	}
	
private ComponentBuilder<?, ?> getBorrowerDetailsForScheduleA(JSONObject keysForContent, CustomerDataFields custmrDataFields, CustomerDetailsPayload payload1) {
	
	VerticalListBuilder verticalList = cmp.verticalList();
	
	JSONObject addressDetailsObj = getAllAddressDeatils(custmrDataFields);
	String fromAddressAppnt = addressDetailsObj.getString("presentAddressApplicant");
	String fromAddressCoAppnt = addressDetailsObj.getString("presentAddressCoApplicant");

	verticalList.add(createSingleHorizontalListForScheduleA(keysForContent.getString("borrowerDetails")).setStyle(boldCenteredStyle));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("borrowerName"), applicantName));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("borrowerAge"), payload1.getAge()));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("borrowerAddress"), fromAddressAppnt));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("coapplicantName"), coApplicantName));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("coapplicantAddress"), fromAddressCoAppnt));
	
	return verticalList;	
}

private ComponentBuilder<?, ?> getLoanDetailsForScheduleA(JSONObject keysForContent,  CustomerDataFields custmrDataFields, CibilDetailsPayload cibilPayloadCoApp, String productDetail) {
	VerticalListBuilder verticalList = cmp.verticalList();
	try {
		Gson gsonObj = new Gson();
		LoanDetailsPayload payload = gsonObj.fromJson(custmrDataFields.getLoanDetails().getPayloadColumn(),
				LoanDetailsPayload.class);
		logger.debug("LoanDetailsPayload : " + payload);
	
//		custmrDataFields.getLoanDetails().getBmRecommendedLoanAmount();
		
		String interest = (cibilPayloadCoApp.getRoi() == null) ? "" : cibilPayloadCoApp.getRoi().toString();
		logger.debug("Interest rate fro loan details2:"+ interest);
		
		verticalList.add(createSingleHorizontalListForScheduleA(keysForContent.getString("loanDetails")).setStyle(boldCenteredStyle));
		verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("loanType"), productDetail));
		verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("loanAmount"), "Rs."+CommonUtils.amountFormat(String.valueOf(custmrDataFields.getLoanDetails().getLoanAmount()))+"/-")); 
		verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("loanInterestRate"), CommonUtils.amountFormat(interest) + " %"));
		verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("loanPurpose"), payload.getLoanPurpose()));
				
	} catch (Exception e) {
		logger.error("error - getLoanDetails");
		logger.error(e.getMessage());
	}
	logger.debug("LoanDetails added");

	return verticalList;	
}

private ComponentBuilder<?, ?> getLoanAmortizationForScheduleA(JSONObject keysForContent, CustomerDataFields custmrDataFields, CibilDetailsPayload cibilPayloadCoApp, int noOfEPIs, String firstEmi, String emi) {
	
	VerticalListBuilder verticalList = cmp.verticalList();

	verticalList.add(createSingleHorizontalListForScheduleA(keysForContent.getString("loanAmortization")).setStyle(boldCenteredStyle));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("tenure"), cibilPayloadCoApp.getFinalTenure()));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("installments"), String.valueOf(noOfEPIs)));
//	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("installmentAmount"),
//			keysForContent.getString("firstInstallment")+ ": "+ "\n" + keysForContent.getString("lastInstallmentAmount")+": ")); 
//	
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("installmentAmount"),
			keysForContent.getString("firstInstallment")+ ": "+ "Rs." +CommonUtils.amountFormat(firstEmi) +"/-" + "<br>" + keysForContent.getString("equatedInstallment")+": " +"Rs."+CommonUtils.amountFormat(emi) +"/-")); 
			
	//equatedInstallment
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("repaymentFrequency"), cibilPayloadCoApp.getRepaymentFrequency()));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("repaymentMode"), "Cash/Cashless")); //Cash/Cashless
//	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("firstInstallment"), ""));
//	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("lastInstallmentAmount"), ""));
	
	return verticalList;	
}

private ComponentBuilder<?, ?> getDetailChargesForScheduleA(JSONObject keysForContent, CibilDetailsPayload cibilPayloadCoApp) {
	
	VerticalListBuilder verticalList = cmp.verticalList();
	
	verticalList.add(createSingleHorizontalListForScheduleA(keysForContent.getString("chargesDetails")).setStyle(boldCenteredStyle));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("processingCharges"), "Rs."+CommonUtils.amountFormat(cibilPayloadCoApp.getProcessingFees())+ "/-"));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("stampDutyCharges"), "Rs."+CommonUtils.amountFormat("0")+ "/-")); // as of now  - 0 //conditionally fertch based on state for NESL 
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("latePaymentCharges"), "NA"));
//	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("firstInstallment"), ""));
//	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("lastInstallmentAmount"), ""));
	
	return verticalList;	
}

private ComponentBuilder<?, ?> getKfsSummaryDetails(JSONObject keysForContent, CibilDetailsPayload cibilPayloadCoApp, CustomerDataFields custmrDataFields, String productName, int totalInsurance, int noOfEPIs, String emi, BigDecimal totalInterest) {
	
	VerticalListBuilder verticalList = cmp.verticalList();
	String interest = String.valueOf(cibilPayloadCoApp.getRoi() == null ? "": cibilPayloadCoApp.getRoi().toString());
	String apr = String.valueOf(cibilPayloadCoApp.getEir() == null ? "": cibilPayloadCoApp.getEir().toString()); 
	
	BigDecimal sactionAmtDb = custmrDataFields.getLoanDetails().getSanctionedLoanAmount();
	String sactionAmt = String.valueOf(sactionAmtDb == null ? "" : sactionAmtDb.toPlainString());
	
	verticalList.add(createSingleHorizontalListForScheduleA(keysForContent.getString("module2")).setStyle(boldCenteredStyle));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("loanProductName"), productName));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("loanAmountSanctioned"), "Rs. "+ CommonUtils.amountFormat(sactionAmt) + "/-"));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("repaymentFrequency"), cibilPayloadCoApp.getRepaymentFrequency()));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("numberOfInstallments"), String.valueOf(noOfEPIs)));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("installmentAmoun"),  "Rs. "+ CommonUtils.amountFormat(emi)+ "/-"));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("rateOfInterest"), CommonUtils.amountFormat(interest) + " %"));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("totalInterest"),  "Rs. "+ CommonUtils.amountFormat(String.valueOf(totalInterest))+ "/-"));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("processingFees"),"Rs. "+ CommonUtils.amountFormat(cibilPayloadCoApp.getProcessingFees())+ "/-"));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("insurancePremium"), "Rs. " + CommonUtils.amountFormat(String.valueOf(totalInsurance))+ "/-"));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("stampDutyCharges"), "Rs. "+ CommonUtils.amountFormat("0")+ "/-"));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("latePaymentCharges2"), "NA"));
	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("annualPercentageRate"), apr));
//	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("firstInstallment"), ""));
//	verticalList.add(createTwoHorizontalListForScheduleA(keysForContent.getString("lastInstallmentAmount"), ""));
	
	return verticalList;	
}



//	public static void main(String[] args) throws Exception {
//		String languagePath ="";
//		///condition for language
//		String lang = "English";
//		if(lang.equals("English")){ // English
////			languagePath="C:\\Users\\rs.karthik\\Desktop\\Vishal\\GenTemplates & JSON keys\\ScheduleA\\jsonKeysForEnglishScheduleA"
////					+ ".json";
//			languagePath="C:\\Users\\ashoka.raja\\Desktop\\Vishal\\GenTemplates & JSON keys\\ScheduleA\\jsonKeysForEnglishScheduleA.json";
//		}else if(lang.equals("Kannada")){ //Kannada
//			//languagePath="C:\\Users\\vishal.a\\Downloads\\LOANAPPLICATION (1)\\LOANAPPLICATION\\ScheduleA\\jsonKeysForKannadaScheduleA.txt";
//			languagePath="C:\\Users\\ashoka.raja\\Desktop\\Vishal\\GenTemplates & JSON keys\\ScheduleA\\jsonKeysForKannadaScheduleA.json";
//		}else if(lang.equals("Hindi")){ //Hindi
//			//languagePath="C:\\Users\\vishal.a\\Downloads\\LOANAPPLICATION (1)\\LOANAPPLICATION\\ScheduleA\\jsonKeysForHindiScheduleA.txt";
//			languagePath="C:\\Users\\ashoka.raja\\Desktop\\Vishal\\GenTemplates & JSON keys\\ScheduleA\\jsonKeysForHindiScheduleA.json";
//		}else if(lang.equals("Marathi")){ //Marathi
//			//languagePath="C:\\Users\\vishal.a\\Downloads\\LOANAPPLICATION (1)\\LOANAPPLICATION\\ScheduleA\\jsonKeysForMarathiScheduleA.txt";
//			languagePath="C:\\Users\\ashoka.raja\\Desktop\\Vishal\\GenTemplates & JSON keys\\ScheduleA\\jsonKeysForMarathiScheduleA.json";
//		}else if(lang.equals("Tamil")){ //Tamil
////			languagePath="C:\\Users\\vishal.a\\Downloads\\LOANAPPLICATION (1)\\LOANAPPLICATION\\ScheduleA\\jsonKeysForTamilScheduleA"
////					+ ".json";
//			languagePath="C:\\Users\\ashoka.raja\\Desktop\\Vishal\\GenTemplates & JSON keys\\ScheduleA\\jsonKeysForTamilScheduleA.json";
//		}else if(lang.equals("Malayalam")){ //Malayalam
//			languagePath="C:\\Users\\vishal.a\\Downloads\\LOANAPPLICATION (1)\\LOANAPPLICATION\\ScheduleA\\jsonKeysForMalayalamScheduleA.txt";
//		}		
//		
//			JSONObject keysForContent = new JSONObject(new ScheduleATemplate()
//				.getContentFromFile(languagePath))
//				.getJSONObject("keysForContent");
//				
//			String base64 = new ScheduleATemplate().generatePdf(keysForContent);	
//	}
	
	 private JSONObject getAllAddressDeatils(CustomerDataFields req) {
 		logger.debug("Entry - getAllAddressDetails method");
 		JSONObject jsnObj = null;
 		try {
 			Gson gsonObj = new Gson();
 			
 			List<Address> applicantAddrPayLoadLst = null;
 			List<Address> coApplicantAddrPayLoadLst = null;
 			
 			List<Address> applicantOccupnAddrPayLoadLst = null;
 			List<Address> coApplicantOccupnAddrPayLoadLst = null;
 			
 			for(AddressDetails addr : req.getAddressDetailsWrapperList().get(0).getAddressDetailsList()) {
 				if(addr.getAddressType().equalsIgnoreCase("Personal") && 
 						(String.valueOf(addr.getCustDtlId()).equals(applicantCustId))){
 					AddressDetailsPayload applicantPayload  = gsonObj.fromJson(addr.getPayloadColumn(), AddressDetailsPayload.class);
 					applicantAddrPayLoadLst = applicantPayload.getAddressList();
 					logger.debug("PersonalAddLstApplicant :" + applicantAddrPayLoadLst);				
 				}else if(addr.getAddressType().equalsIgnoreCase("Personal") && 
 						(String.valueOf(addr.getCustDtlId()).equals(coApplicantCustId))){
 					AddressDetailsPayload coApplicantPayload  = gsonObj.fromJson(addr.getPayloadColumn(), AddressDetailsPayload.class);
 					coApplicantAddrPayLoadLst = coApplicantPayload.getAddressList();
 					logger.debug("PersonalAddLstCo-aaplicant :" + coApplicantAddrPayLoadLst);	
 				}
 				
 				//occupation Address
 				if(addr.getAddressType().equalsIgnoreCase("Occupation") && 
 						(String.valueOf(addr.getCustDtlId()).equals(applicantCustId))){
 					AddressDetailsPayload applicantPayload  = gsonObj.fromJson(addr.getPayloadColumn(), AddressDetailsPayload.class);
 					applicantOccupnAddrPayLoadLst = applicantPayload.getAddressList();
 					logger.debug("PersonalAddLstApplicant - Occupation :"+ applicantOccupnAddrPayLoadLst);
 				} else if(addr.getAddressType().equalsIgnoreCase("Occupation") && 
 						(String.valueOf(addr.getCustDtlId()).equals(coApplicantCustId))){
 					AddressDetailsPayload coApplicantPayload  = gsonObj.fromJson(addr.getPayloadColumn(), AddressDetailsPayload.class);
 					coApplicantOccupnAddrPayLoadLst = coApplicantPayload.getAddressList();
 					logger.debug("PersonalAddLstCoApplicant - Occupation :"+ coApplicantOccupnAddrPayLoadLst);
 				}
 			}
 			
 			// Address
 			String presentAddressApplicant = "";
 			String permanetAddressApplicant = "";

 			String presentAddressCoApplicant = "";
 			String permanetAddressCoApplicant = "";

 			// other Deatils
 			String presentResidenceOwnership = "";
 			String presentAddressYears = "";
 			String presntCityYears = "";
 			String presentResidenceAddressProof = "";
 			String presentResidenceType = "";
 			String presentResidenceSize = "--";
 			
 			String presentResidenceOwnershipCo = "";
 			String presentAddressYearsCo = "";
 			String presntCityYearsCo = "";
 			String presentResidenceAddressProofCo = "";
 			String presentResidenceTypeCo = "";
 			String presentResidenceSizeCo = "--";
 			
 			//Occupation Address
 			String occpnAddrApplicant = "";
 			String occpnAddrCoApplicant = "";
 			
 			
 			//Applicant Address
 			for (Address addr : applicantAddrPayLoadLst) {
 				if (addr.getAddressType().equalsIgnoreCase("present")) {
// 					presentAddressApplicant = addr.getAddressLine1() + addr.getAddressLine2() + addr.getAddressLine3()
// 							+ addr.getArea() +  addr.getLandMark() + addr.getCity() + addr.getDistrict() + addr.getState()
// 							+ addr.getCountry() + addr.getPinCode();
 					
 					presentAddressApplicant = getFullAddress(addr);
 					logger.debug("Present Address - Applicant : " + presentAddressApplicant);	
 					presentResidenceOwnership = addr.getResidenceOwnership();
 					presentAddressYears = addr.getResidenceAddressSince();
 					presntCityYears = addr.getResidenceCitySince();
 					presentResidenceAddressProof = addr.getCurrentAddressProof();
 					presentResidenceType = addr.getHouseType();
 				} else if (addr.getAddressType().equalsIgnoreCase("Permanent")) {
// 					permanetAddressApplicant = addr.getAddressLine1() + addr.getAddressLine2() + addr.getAddressLine3()
// 							+ addr.getArea()  + addr.getLandMark() + addr.getCity() + addr.getDistrict() + addr.getState()
// 							+ addr.getCountry() + addr.getPinCode();
 					permanetAddressApplicant = getFullAddress(addr);
 					logger.debug("Permanent Address - Applicant :" + permanetAddressApplicant);
 				} 		
 			}
 				
 			//Co-Applicant Address
 			if(coApplicantAddrPayLoadLst !=null) {
 				for(Address addr: coApplicantAddrPayLoadLst){
 					if(addr.getAddressType().equalsIgnoreCase("present")) {
// 						presentAddressCoApplicant = addr.getAddressLine1() + addr.getAddressLine2() + addr.getAddressLine3() + addr.getArea() + addr.getLandMark() + addr.getCity() + addr.getDistrict()+ addr.getState() + addr.getCountry() + addr.getPinCode();
 						presentAddressCoApplicant = getFullAddress(addr);
 						logger.debug("Present Address - Co-applicant :" + presentAddressCoApplicant);
 						presentResidenceOwnershipCo = addr.getResidenceOwnership();
 						presentAddressYearsCo = addr.getResidenceAddressSince();
 						presntCityYearsCo = addr.getResidenceCitySince();
 						presentResidenceAddressProofCo = addr.getCurrentAddressProof();
 						presentResidenceTypeCo = addr.getHouseType();
 					}else if(addr.getAddressType().equalsIgnoreCase("Permanent")) {
// 						permanetAddressCoApplicant = addr.getAddressLine1() + addr.getAddressLine2() + addr.getAddressLine3() + addr.getArea() + addr.getLandMark() + addr.getCity() + addr.getDistrict()+ addr.getState() + addr.getCountry() + addr.getPinCode();
 						permanetAddressCoApplicant = getFullAddress(addr);
 						logger.debug("permanent Address - Co-applicant. :" + permanetAddressCoApplicant);
 					}
 				}
 			}
 				
 			//Occupation Address
 			Address ocupnAddr = null;
 			Address ocupnAddrCo = null;
 			if (applicantOccupnAddrPayLoadLst != null && !applicantOccupnAddrPayLoadLst.isEmpty()) {
 			    ocupnAddr = applicantOccupnAddrPayLoadLst.get(0);
 			}
 			if (coApplicantOccupnAddrPayLoadLst != null && !coApplicantOccupnAddrPayLoadLst.isEmpty()) {
 				ocupnAddrCo = coApplicantOccupnAddrPayLoadLst.get(0);
 			}
// 			if(addr.getAddressType().equalsIgnoreCase("Office")) {
 				occpnAddrApplicant = getFullAddress(ocupnAddr);
 				occpnAddrCoApplicant = getFullAddress(ocupnAddrCo);
 			logger.debug("Ocupation Address Applicnt : " + occpnAddrApplicant);	
 			logger.debug("Ocupation Address Co-Applicnt : " + occpnAddrCoApplicant);	
// 				ocupnAddr.getAddressLine1() + ocupnAddr.getAddressLine2() + ocupnAddr.getAddressLine3()
// 				+ ocupnAddr.getArea() +  ocupnAddr.getLandMark() + ocupnAddr.getCity() + ocupnAddr.getDistrict() + ocupnAddr.getState()
// 				+ ocupnAddr.getCountry() + ocupnAddr.getPinCode();
 					
// 			}else if(addr.getAddressType().equalsIgnoreCase("Office")) {
// 				occpnAddrCoApplicant = ocupnAddrCo.getAddressLine1() + ocupnAddrCo.getAddressLine2() + ocupnAddrCo.getAddressLine3()
// 				+ ocupnAddrCo.getArea() +  ocupnAddrCo.getLandMark() + ocupnAddrCo.getCity() + ocupnAddrCo.getDistrict() + ocupnAddrCo.getState()
// 				+ ocupnAddrCo.getCountry() + ocupnAddrCo.getPinCode();
// 				}
 			
 				
 			jsnObj = new JSONObject();
 			jsnObj.put("presentAddressApplicant", presentAddressApplicant);
 			jsnObj.put("permanetAddressApplicant", permanetAddressApplicant);
 			jsnObj.put("presentAddressCoApplicant", presentAddressCoApplicant);
 			jsnObj.put("permanetAddressCoApplicant", permanetAddressCoApplicant);
 			
 			// other Deatils
 			jsnObj.put("presentResidenceOwnership", presentResidenceOwnership);
 			jsnObj.put("presentAddressYears", presentAddressYears);
 			jsnObj.put("presntCityYears", presntCityYears);
 			jsnObj.put("presentResidenceAddressProof", presentResidenceAddressProof);
 			jsnObj.put("presentResidenceType", presentResidenceType);
 			jsnObj.put("presentResidenceSize", presentResidenceSize);
 			
 			jsnObj.put("presentResidenceOwnershipCo", presentResidenceOwnershipCo);
 			jsnObj.put("presentAddressYearsCo", presentAddressYearsCo);
 			jsnObj.put("presntCityYearsCo", presntCityYearsCo);
 			jsnObj.put("presentResidenceAddressProofCo", presentResidenceAddressProofCo);
 			jsnObj.put("presentResidenceTypeCo", presentResidenceTypeCo);
 			jsnObj.put("presentResidenceSizeCo", presentResidenceSizeCo);
 			
 			//Occupation Address
 			jsnObj.put("occpnAddrApplicant", occpnAddrApplicant);
 			jsnObj.put("occpnAddrCoApplicant", occpnAddrCoApplicant);
 			
 			
 		
 		}catch (Exception e) {
 			logger.error("error - getAllAddressDeatils Method");
 			logger.error(e.getMessage());	
 		}
 		logger.debug("Exit - getAllAddressDetails method completed.");
 		return jsnObj;
 	}
 	
 	
 	private static String getFullAddress(Address address) {
 	    // Retrieve individual components from the Address object
 		if (address == null) {
 		    return "";
 		}
 	    String[] addressArr = {
 	        address.getAddressLine1(),
 	        address.getAddressLine2(),
 	        address.getAddressLine3(),
 	        address.getArea(),
 	        address.getLandMark(),
 	        address.getDistrict(),
 	        address.getCity(),
 	        address.getState(),
 	        address.getCountry(),
 	        address.getPinCode()
 	        
 	    };

 	    // Construct the full address
 	    StringBuilder totalAddress = new StringBuilder();
 	    for (String part : addressArr) {
 	        if (part != null && !part.isEmpty()) { // Ensure non-null and non-empty
 	            totalAddress.append(part).append(", ");
 	        }
 	    }

 	    // Remove trailing comma, if any
 	    if (totalAddress.length() > 0 && totalAddress.toString().endsWith(", ")) {
 	        totalAddress.setLength(totalAddress.length() - 2);
 	    }

 	    return totalAddress.toString();
 	}
	
 	 private static final String[] units = {
		        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
		        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen",
		        "Sixteen", "Seventeen", "Eighteen", "Nineteen"
		    };

		    private static final String[] tens = {
		        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
		    };

		    public static String convert(long number) {
		        if (number == 0) return "Zero";
		        return convertNumber(number).trim();
		    }

		    private static String convertNumber(long number) {
		        if (number < 20) return units[(int) number];
		        if (number < 100) return tens[(int) number / 10] + " " + units[(int) number % 10];
		        if (number < 1000) return units[(int) number / 100] + " Hundred " + convertNumber(number % 100);
		        if (number < 100000) return convertNumber(number / 1000) + " Thousand " + convertNumber(number % 1000);
		        if (number < 10000000) return convertNumber(number / 100000) + " Lakh " + convertNumber(number % 100000);
		        return convertNumber(number / 10000000) + " Crore " + convertNumber(number % 10000000);
		    }
	
			private int toFindSum(String... value) {
				int totalInsurance = Stream.of(
				        value
				).map(val -> {
				    try {
				        return Integer.parseInt(Optional.ofNullable(val).orElse("0"));
				    } catch (NumberFormatException e) {
				        return 0;
				    }
				}).reduce(0, Integer::sum);
				return totalInsurance;
			}
			
			private int toFindDifference(String val1, String val2) {
			    try {
			        int num1 = Integer.parseInt(Optional.ofNullable(val1).orElse("0").replaceAll(",", ""));
			        int num2 = Integer.parseInt(Optional.ofNullable(val2).orElse("0").replaceAll(",", ""));
			        return num1 - num2;
			    } catch (NumberFormatException e) {
			        return 0;
			    }
			}
}
