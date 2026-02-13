package com.iexceed.appzillonbanking.cob.loans.service;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import com.iexceed.appzillonbanking.cob.loans.payload.*;
import com.iexceed.appzillonbanking.cob.loans.payload.BIPRequestWrapper.BIPMasterRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.*;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.iexceed.appzillonbanking.cob.core.domain.ab.AddressDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.AddressDetailsHistory;
import com.iexceed.appzillonbanking.cob.core.domain.ab.ApiExecutionLog;
import com.iexceed.appzillonbanking.cob.core.domain.ab.ApplicationDocuments;
import com.iexceed.appzillonbanking.cob.core.domain.ab.ApplicationDocumentsHistory;
import com.iexceed.appzillonbanking.cob.core.domain.ab.ApplicationMaster;
import com.iexceed.appzillonbanking.cob.core.domain.ab.ApplicationMasterHistory;
import com.iexceed.appzillonbanking.cob.core.domain.ab.ApplicationWorkflow;
import com.iexceed.appzillonbanking.cob.core.domain.ab.BCMPIIncomeDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.BCMPILoanObligations;
import com.iexceed.appzillonbanking.cob.core.domain.ab.BCMPIOtherDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.BCMPIStageVerification;
import com.iexceed.appzillonbanking.cob.core.domain.ab.BankDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.CibilDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.CibilDetailsHistory;
import com.iexceed.appzillonbanking.cob.core.domain.ab.CustomerDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.CustomerDetailsHistory;
import com.iexceed.appzillonbanking.cob.core.domain.ab.DBKITStageVerification;
import com.iexceed.appzillonbanking.cob.core.domain.ab.ExistingGLLoanDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.ExistingLoanDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.InsuranceDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.InsuranceDetailsHistory;
import com.iexceed.appzillonbanking.cob.core.domain.ab.LeadDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.LoanDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.LoanHisDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.OccupationDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.OccupationDetailsHistory;
import com.iexceed.appzillonbanking.cob.core.domain.ab.RenewalLeadDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.RenewalLeadOccpInsDetails;
import com.iexceed.appzillonbanking.cob.core.domain.ab.RpcStageVerification;
import com.iexceed.appzillonbanking.cob.core.domain.ab.SourcingResponseTracker;
import com.iexceed.appzillonbanking.cob.core.domain.ab.Udhyam;
import com.iexceed.appzillonbanking.cob.core.domain.ab.WorkflowDefinition;
import com.iexceed.appzillonbanking.cob.core.payload.Address;
import com.iexceed.appzillonbanking.cob.core.payload.AddressDetailsPayload;
import com.iexceed.appzillonbanking.cob.core.payload.AddressDetailsWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.ApplicationDocumentsPayload;
import com.iexceed.appzillonbanking.cob.core.payload.ApplicationDocumentsWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.BankDetailsPayload;
import com.iexceed.appzillonbanking.cob.core.payload.BankDetailsWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.CheckApplicationRes;
import com.iexceed.appzillonbanking.cob.core.payload.CibilDetailsPayload;
import com.iexceed.appzillonbanking.cob.core.payload.CibilDetailsWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.CustomerDetailsPayload;
import com.iexceed.appzillonbanking.cob.core.payload.CustomerIdentificationCasa;
import com.iexceed.appzillonbanking.cob.core.payload.CustomerIdentificationLoan;
import com.iexceed.appzillonbanking.cob.core.payload.ExistingLoanDetailsWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.Header;
import com.iexceed.appzillonbanking.cob.core.payload.InsuranceDetailsPayload;
import com.iexceed.appzillonbanking.cob.core.payload.InsuranceDetailsWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.LoanDetailsPayload;
import com.iexceed.appzillonbanking.cob.core.payload.OccupationDetailsPayload;
import com.iexceed.appzillonbanking.cob.core.payload.OccupationDetailsWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.PopulateapplnWFRequest;
import com.iexceed.appzillonbanking.cob.core.payload.PopulateapplnWFRequestFields;
import com.iexceed.appzillonbanking.cob.core.payload.Response;
import com.iexceed.appzillonbanking.cob.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.cob.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.cob.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.WorkFlowDetails;
import com.iexceed.appzillonbanking.cob.core.repository.ab.AddressDetailsHisRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.AddressDetailsRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.ApiExecutionLogRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.ApplicationDocumentsHisRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.ApplicationDocumentsRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.ApplicationMasterHisRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.ApplicationMasterRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.ApplicationWorkflowRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.BCMPIIncomeDetailsRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.BCMPILoanObligationsRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.BCMPIOtherDetailsRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.BCMPIStageVerificationRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.BankDetailsRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.CibilDetailsHisRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.CibilDetailsRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.CustomerDetailsHisRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.CustomerDetailsRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.DBKITStageVerificationRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.ExistingGLLoanDetailsRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.ExistingLoanDetailsRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.InsuranceDetailsHisRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.InsuranceDetailsRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.LeadDetailsRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.LoanDtlsHisRepo;
import com.iexceed.appzillonbanking.cob.core.repository.ab.LoanDtlsRepo;
import com.iexceed.appzillonbanking.cob.core.repository.ab.OccupationDetailsHisRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.OccupationDetailsRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.RenewalLeadDetailsRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.RenewalLeadOccpInsDetailsRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.RpcStageVerificationRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.SourcingResponseTrackerRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.UdhyamRepository;
import com.iexceed.appzillonbanking.cob.core.repository.ab.WorkflowDefinitionRepository;
import com.iexceed.appzillonbanking.cob.core.services.CommonParamService;
import com.iexceed.appzillonbanking.cob.core.services.InterfaceAdapter;
import com.iexceed.appzillonbanking.cob.core.services.ResponseParser;
import com.iexceed.appzillonbanking.cob.core.services.SoapInterfaceParser;
import com.iexceed.appzillonbanking.cob.core.utils.AdapterUtil;
import com.iexceed.appzillonbanking.cob.core.utils.AppStatus;
import com.iexceed.appzillonbanking.cob.core.utils.CobFlagsProperties;
import com.iexceed.appzillonbanking.cob.core.utils.CodeTypes;
import com.iexceed.appzillonbanking.cob.core.utils.CommonUtils;
import com.iexceed.appzillonbanking.cob.core.utils.Constants;
import com.iexceed.appzillonbanking.cob.core.utils.FallbackUtils;
import com.iexceed.appzillonbanking.cob.core.utils.Products;
import com.iexceed.appzillonbanking.cob.core.utils.ResponseCodes;
import com.iexceed.appzillonbanking.cob.core.utils.WorkflowStatus;
import com.iexceed.appzillonbanking.cob.domain.ab.LovMaster;
import com.iexceed.appzillonbanking.cob.domain.ab.RoleAccessMap;
//import com.iexceed.appzillonbanking.cob.loans.payload.DmsRequestExt;
import com.iexceed.appzillonbanking.cob.loans.payload.UploadLoanRequestFields.DBKITResponse;
import com.iexceed.appzillonbanking.cob.loans.report.LoanReport;
import com.iexceed.appzillonbanking.cob.loans.repository.user.TbUserRepository;
import com.iexceed.appzillonbanking.cob.nesl.domain.ab.Enach;
import com.iexceed.appzillonbanking.cob.nesl.repository.ab.EnachRepository;
import com.iexceed.appzillonbanking.cob.payload.CreateModifyUserRequest;
import com.iexceed.appzillonbanking.cob.payload.CreateModifyUserRequestWrapper;
import com.iexceed.appzillonbanking.cob.payload.CustomerDataFields;
import com.iexceed.appzillonbanking.cob.payload.FetchDeleteUserFields;
import com.iexceed.appzillonbanking.cob.payload.FetchDeleteUserRequest;
import com.iexceed.appzillonbanking.cob.payload.LoanCreationReqFields;
import com.iexceed.appzillonbanking.cob.payload.LoanFetchReqFields;
import com.iexceed.appzillonbanking.cob.payload.UpdateApplicantsCountRequest;
import com.iexceed.appzillonbanking.cob.payload.UpdateApplicantsCountRequestFields;
import com.iexceed.appzillonbanking.cob.payload.UploadDocumentRequestFields;
import com.iexceed.appzillonbanking.cob.report.LoanApplication;
import com.iexceed.appzillonbanking.cob.repository.ab.LovMasterRepository;
import com.iexceed.appzillonbanking.cob.repository.ab.WhitelistedBranchesRepository;
import com.iexceed.appzillonbanking.cob.rest.CustomerOnBoardingAPI;
import com.iexceed.appzillonbanking.cob.service.COBService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Service
public class LoanService {

	private static final Logger logger = LogManager.getLogger(LoanService.class);

	@Autowired
	private AdapterUtil adapterUtil;

	@Autowired
	private ApplicationMasterRepository applicationMasterRepo;

	@Autowired
	private ApplicationMasterHisRepository applicationMasterHisRepo;

	@Autowired
	private ApplicationWorkflowRepository applnWfRepository;

	@Autowired
	private WorkflowDefinitionRepository wfDefnLoanRepo;

	@Autowired
	private LoanDtlsRepo loanDtlsRepo;

	@Autowired
	private LoanDtlsHisRepo loanDtlsHisRepo;

	@Autowired
	private ApplicationDocumentsRepository appLoanDocsRepository;

	@Autowired
	private ApplicationDocumentsHisRepository appLoanDocsHisRepository;

	@Autowired
	private InterfaceAdapter interfaceAdapter;

	@Autowired
	private CustomerDetailsRepository custDtlRepo;

	@Autowired
	private CustomerDetailsHisRepository custDtlHisRepo;

	@Autowired
	private AddressDetailsRepository addressDtlRepo;

	@Autowired
	private ExistingLoanDetailsRepository existingLoanDtlRepo;

	@Autowired
	private AddressDetailsHisRepository addressDtlHisRepo;

	@Autowired
	private OccupationDetailsRepository occupationDtlRepo;

	@Autowired
	private InsuranceDetailsRepository insuranceDtlRepo;

	@Autowired
	private InsuranceDetailsHisRepository insuranceDtlHisRepo;

	@Autowired
	private BankDetailsRepository bankDtlRepo;

	@Autowired
	private CibilDetailsRepository cibilDtlRepo;

	@Autowired
	private CibilDetailsHisRepository cibilDtlHisRepo;

	@Autowired
	private OccupationDetailsHisRepository occupationDtlHisRepo;

	@Autowired
	private LeadDetailsRepository leadDtlsRepo;

	@Autowired
	private RenewalLeadDetailsRepository renewalLeadDtlsRepo;

	@Autowired
	private CommonParamService commonParamService;

	@Autowired
	private LoanReport report;

	@Autowired
	private CustomerOnBoardingAPI casaApi;

	@Autowired
	private WorkflowDefinitionRepository wfDefnRepoLn;

	@Autowired
	private COBService cobService;

	@Autowired
	private RenewalLeadOccpInsDetailsRepository renewalLeadOccpInsDetailsRepo;

	@Autowired
	private ExistingGLLoanDetailsRepository existingGLLoanDetailsRepo;

	@Autowired
	private RpcStageVerificationRepository rpcStgVerificationRepo;

	@Autowired
	private BCMPIIncomeDetailsRepository bcmpiIncomeDetailsRepo;

	@Autowired
	private BCMPIStageVerificationRepository bcmpiStageVerificationRepository;

	@Autowired
	private BCMPILoanObligationsRepository bcmpiLoanObligationsRepo;

	@Autowired
	private BCMPIOtherDetailsRepository bcmpiOtherDetailsRepo;

	@Autowired
	private LovMasterRepository lovMasterRepository;

	@Autowired
	private UdhyamRepository udhyamRepository;

	@Autowired
	private SourcingResponseTrackerRepository sourcingResponseTrackerRepo;

	@Autowired
	private EnachRepository enachRepository;

	@Autowired
	private WhitelistedBranchesRepository whitelistedBranchesRepository;

	@Autowired
	private WebClient webClient;

	@Autowired
	DBKITStageVerificationRepository dbkitStageVerificationRepository;

	@Autowired
	private ApiExecutionLogRepository logRepository;

	@Autowired
	TbUserRepository tbUserRepository;

	private String versionHm = "versionHm";
	private String headerHm = "headerHm";
	private String applicationIDHm = "applicationIDHm";
	private String propHm = "propHm";
	private int loanDetailsLovId = 29;

	private String requestLog;

	public String getRequestLog() {
		return requestLog;
	}

	public void setRequestLog(String requestLog) {
		this.requestLog = requestLog;
	}

	DateTimeFormatter localDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	SimpleDateFormat outFormat = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat inFormat = new SimpleDateFormat("dd/MM/yyyy");

	public Mono<Response> applyLoan(HashMap<String, String> hm2, ApplyLoanRequest applyLoanRequest,
									boolean isSelfOnBoardingAppId, boolean isSelfOnBoardingHeaderAppId, Properties prop, JSONArray array) {
		Header header = CommonUtils.obtainHeader(hm2.get("reqAppId"), hm2.get("interfaceId"), hm2.get("userId"),
				hm2.get("masterTxnRefNo"), hm2.get("deviceId"));
		ApplyLoanRequestFields requestObj = applyLoanRequest.getRequestObj();
		ApplicationMaster applicationMaster = requestObj.getApplicationMaster();
		ResponseBody responseBody = new ResponseBody();
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		responseHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());
		CustomerIdentificationLoan customerIdentification = new CustomerIdentificationLoan();
		String applicationID;
		// Previously it was set to 0. Change done on 25/02/2024
		int version = Constants.INITIAL_VERSION_NO;
		boolean isThisLastStage = commonParamService
				.isThisLastStage(applicationMaster.getCurrentScreenId().split("~")[0], array);
		boolean isAccountCreationisNextStage = false;
		// BigDecimal custDtlId = commonParamService.getCustDtlId(applicationMaster);
		BigDecimal custDtlId;
		Optional<ApplicationMaster> masterData = applicationMasterRepo.findByAppIdAndWorkitemNo(requestObj.getAppId(),
				applicationMaster.getWorkitemNo());

		if (!masterData.isPresent()) {
			applicationID = CommonUtils.generateRandomNumStr();
			// custDtlId =
			// commonParamService.generateCustDtlId(applicationID,requestObj.getCustomerDetailsList().get(0).getCustomerType());
			version = Constants.INITIAL_VERSION_NO; // initial creation of loan application version number should be 1.
			populateAppMasterAndApplnwf(requestObj, applicationID, version, customerIdentification,
					isSelfOnBoardingHeaderAppId, prop);
			/*
			 * commonParamService.populateCustomerDtlsIfNotPresent(requestObj.
			 * getApplicationMaster(), applicationID, custDtlId, version,
			 * requestObj.getAppId());
			 */
		} else { // this ID should be created once only.
			applicationID = masterData.get().getApplicationId();
			// custDtlId =
			// commonParamService.generateCustDtlId(applicationID,requestObj.getCustomerDetailsList().get(0).getCustomerType());
			Optional<ApplicationMaster> appMasterForVersionCheck = applicationMasterRepo
					.findTopByAppIdAndApplicationIdOrderByVersionNumDesc(requestObj.getAppId(), applicationID);
			if (appMasterForVersionCheck.isPresent()) {
				ApplicationMaster appMaster = appMasterForVersionCheck.get();
				customerIdentification.setRelatedApplicationId(appMaster.getRelatedApplicationId());
				if (AppStatus.INPROGRESS.getValue().equalsIgnoreCase(appMaster.getApplicationStatus())
						|| AppStatus.APPROVED.getValue().equalsIgnoreCase(appMaster.getApplicationStatus())
						|| AppStatus.PUSHBACK.getValue().equalsIgnoreCase(appMaster.getApplicationStatus()) || AppStatus.IPUSHBACK.getValue().equalsIgnoreCase(appMaster.getApplicationStatus())
						|| AppStatus.PENDING.getValue().equalsIgnoreCase(appMaster.getApplicationStatus())) {
					// Taking version number always from db as part of VAPT too.
					// If application is in INPROGRESS status,subsequent tables should have same
					// version number.
					version = appMaster.getVersionNum();
				}
				if (Objects.equals(appMaster.getApplicantsCount(), applicationMaster.getCustDtlSlNum())) {
					isAccountCreationisNextStage = commonParamService
							.isAccountCreationisNextStage(applicationMaster.getCurrentScreenId().split("~")[0], array);
				}
			}
		}
		String[] currentScreenIdArray = requestObj.getApplicationMaster().getCurrentScreenId().split("~");
		commonParamService.updateCurrentStageInMaster(requestObj.getApplicationMaster(), currentScreenIdArray, version,
				requestObj.getAppId(), requestObj.getApplicationId());
		String custType = (requestObj.getApplicationMaster().getCustDtlSlNum() <= 1) ? Constants.APPLICANT
				: Constants.COAPPLICANT;
		custDtlId = commonParamService.generateCustDtlId(applicationID, custType);
		switch (currentScreenIdArray[0]) {
			case Constants.CUST_VERIFICATION:
				if (requestObj.getApplicationMaster().getCustomerId() == null
						&& "N".equalsIgnoreCase(requestObj.getIsExistingCustomer())) {

					CreateModifyUserRequestWrapper createUserRequestWrapper = new CreateModifyUserRequestWrapper();
					CreateModifyUserRequest createUserRequest = new CreateModifyUserRequest();
					CustomerDataFields createUserRequestFields = new CustomerDataFields();
					ApplicationMaster appMasterCasa = new ApplicationMaster();
					BeanUtils.copyProperties(applicationMaster, appMasterCasa);
					appMasterCasa.setCustDtlSlNum(1); // By default creating single holder casa account for Loans.
					appMasterCasa.setApplicantsCount(requestObj.getApplicationMaster().getApplicantsCount());
					appMasterCasa.setProductCode(prop.getProperty(CobFlagsProperties.DEFAULT_CASA_PRODUCTLN.getKey()));
					appMasterCasa.setProductGroupCode(prop.getProperty(CobFlagsProperties.DEFAULT_CASA_GRP.getKey()));
					appMasterCasa.setRelatedApplicationId(applicationID);
					appMasterCasa.setApplicationId(null); // set this to null so that new application Id will be created in
					// createApplication method.
					appMasterCasa.setCustDtlId(null); // set this to null so that new custDtlId will be created in
					// createApplication method.
					createUserRequestFields.setIsExistingCustomer(requestObj.getIsExistingCustomer());
					createUserRequestFields.setApplicationMaster(appMasterCasa);
					createUserRequestFields.setWorkflow(null); // Workflow is not required for this casa sub application.
					createUserRequestFields.setAppId(requestObj.getAppId());
					if (null != requestObj.getBankingFacilityList()) {
						createUserRequestFields.setBankingFacilityList(requestObj.getBankingFacilityList());
					}
					createUserRequest.setAppId(applyLoanRequest.getAppId());
					createUserRequest.setInterfaceName(applyLoanRequest.getInterfaceName());
					createUserRequest.setUserId(applyLoanRequest.getUserId());
					createUserRequest.setRequestObj(createUserRequestFields);
					createUserRequestWrapper.setCreateModifyUserRequest(createUserRequest);
					Mono<ResponseEntity<ResponseWrapper>> responseMono = casaApi.createApplication(createUserRequestWrapper,
							hm2.get("reqAppId"), hm2.get("interfaceId"), hm2.get("userId"), hm2.get("masterTxnRefNo"),
							hm2.get("deviceId"));
					final int versionFinal = version;
					return responseMono.flatMap(res -> {
						if (ResponseCodes.SUCCESS.getKey()
								.equalsIgnoreCase(res.getBody().getApiResponse().getResponseHeader().getResponseCode())) {
							String casaResStr = res.getBody().getApiResponse().getResponseBody().getResponseObj();
							JSONObject casaResjson = new JSONObject(casaResStr);
							CustomerIdentificationCasa casaCustIdentification = new CustomerIdentificationCasa();
							casaCustIdentification.setApplicationId(casaResjson.getString("applicationId"));
							casaCustIdentification.setCustDtlId(casaResjson.getString("custDtlId"));
							casaCustIdentification.setVersionNum(casaResjson.getInt("versionNum"));
							if (casaResjson.has("bankFacilityList")) {
								JSONArray bankFacilityArr = casaResjson.getJSONArray("bankFacilityList");
								List<String> bankFacilityList = new ArrayList<>();
								for (Object obj : bankFacilityArr) {
									bankFacilityList.add((String) obj);
								}
								casaCustIdentification.setBankFacilityList(bankFacilityList);
							}
							customerIdentification.setCasaCustomerIdentification(casaCustIdentification);
						} else {
							logger.debug("CASA creation failed for loan with error code"
									+ res.getBody().getApiResponse().getResponseHeader().getResponseCode());
							logger.debug("CASA creation failed for loan with error message"
									+ res.getBody().getApiResponse().getResponseBody().getResponseObj());
							responseHeader.setResponseCode(ResponseCodes.CASA_CREATION_FAIL.getKey());
						}

						updateCustomerDtlInMaster(requestObj, versionFinal, applicationID, customerIdentification);
						updateCustIdAndBranchInMaster(requestObj, versionFinal);
						populateOrUpdateLoanDtls(requestObj, versionFinal, applicationID, customerIdentification,
								Constants.LOAN_DETAILS); // populateOrUpdateLoanDtls method call is required here. Because
						// it is possible to have first screen as loan details and
						// second screen as customer verification.
						Gson gson = new Gson();
						String responseStr = gson.toJson(customerIdentification);
						responseBody.setResponseObj(responseStr);
						response.setResponseBody(responseBody);
						response.setResponseHeader(responseHeader);
						return Mono.just(response);
					});
				}
				break;
			case Constants.CUSTOMER_DETAILS:

				populateCustomerDtls(requestObj, customerIdentification, applicationID, custDtlId, version);
				populateAddressDtls(requestObj, customerIdentification, applicationID, custDtlId, version,
						Constants.CUSTOMER_DETAILS, custType);
				UpdateApplicantsCountRequest updateApplicantsCountRequest = new UpdateApplicantsCountRequest();
				UpdateApplicantsCountRequestFields updateApplicantsCountRequestFields = new UpdateApplicantsCountRequestFields();
				updateApplicantsCountRequestFields.setAppId(requestObj.getAppId());
				updateApplicantsCountRequestFields.setApplicationId(applicationID);
				updateApplicantsCountRequestFields.setVersionNum(version);
				updateApplicantsCountRequestFields
						.setApplicantsCount(requestObj.getApplicationMaster().getApplicantsCount());
				updateApplicantsCountRequest.setRequestObj(updateApplicantsCountRequestFields);
				cobService.updateApplicantsCount(updateApplicantsCountRequest);
				/*
				 * commonParamService.updatePanInMaster(requestObj.getApplicationMaster(),
				 * version, requestObj.getAppId(), requestObj.getApplicationId());
				 */
				populateExistingLoanDtls(requestObj, customerIdentification, applicationID, custDtlId, version,
						Constants.CUSTOMER_DETAILS);
				// replicate data from leads table for renewal
				if (!masterData.isPresent()
						&& applicationMaster.getProductCode().equalsIgnoreCase(Constants.RENEWAL_LOAN_PRODUCT_CODE)) {
					logger.debug("inside replicate application");
					replicateApplicationDetails(requestObj, customerIdentification, applicationID, custDtlId, version);
				}
				if (Constants.COAPPLICANT.equalsIgnoreCase(custType)) {
					updateCoApplicantId(requestObj, applicationID, requestObj.getAppId());
				}
				if(null != requestObj.getQueryResponse()){
					updateQueryResponse(requestObj, applicationID);
				}

				break;
			case Constants.OCCUPATION_DETAILS:
				List<String> disabledFields = new ArrayList<>();
				for (CustomerDetails cd : requestObj.getCustomerDetailsList()) {
					if (cd != null && cd.getPayload() != null && cd.getPayload().getIsDisabled() != null) {
						disabledFields.add(cd.getPayload().getIsDisabled().toUpperCase());
					}
				}
				if(disabledFields.contains("Y")||disabledFields.contains("YES")){
					populateCustomerDtlsForDisabled(requestObj,applicationID);
				}else {
					logger.debug("No disabled fields found with value Y or YES");
				}
				populateApplicationDocs(requestObj, customerIdentification, applicationID, version);
				populateOccupationdtls(requestObj, customerIdentification, applicationID, custDtlId, version, custType);
				populateAddressDtls(requestObj, customerIdentification, applicationID, custDtlId, version,
						Constants.OCCUPATION_DETAILS, custType);
				populateCoAppOccupationAddressdtls(requestObj, applicationID, custDtlId, version);
				populateInsuranceDtls(requestObj, customerIdentification, applicationID, custDtlId, version, custType);
				if (requestObj.getApplicationMaster().getCustDtlSlNum() <= 1)
					populateBankDtls(requestObj, customerIdentification, applicationID, custDtlId, version);

				if(null != requestObj.getQueryResponse()){
					updateQueryResponse(requestObj, applicationID);
				}
				break;
			case Constants.LOAN_DETAILS:
				if (requestObj.getApplicationMaster().getCustDtlSlNum() <= 1)
					populateOrUpdateLoanDtls(requestObj, version, applicationID, customerIdentification,
							Constants.LOAN_DETAILS); // This is required if first screen is customer verification and second
				// screen is loan details
				// populateCibilDtls(requestObj, customerIdentification, applicationID,
				// custDtlId, version); //A// have to comment
				populateApplicationDocs(requestObj, customerIdentification, applicationID, version);

				if(null != requestObj.getQueryResponse()){
					updateQueryResponse(requestObj, applicationID);
				}
				break;
			case Constants.EMI_DETAILS:
				populateOrUpdateLoanDtls(requestObj, version, applicationID, customerIdentification, Constants.EMI_DETAILS);
				break;
			case Constants.LOAN_CR_DETAILS:
				populateOrUpdateLoanDtls(requestObj, version, applicationID, customerIdentification,
						Constants.LOAN_CR_DETAILS);
				break;
			case Constants.UPLOAD_DOCUMENTS:
//			populateApplicationDocs(requestObj, customerIdentification, applicationID, version);
				if(null != requestObj.getQueryResponse()){
					updateQueryResponse(requestObj, applicationID);
				}
				break;
			case Constants.TERMS_AND_CONDITIONS:
				updateDeclarationFlagInMaster(requestObj, version, applicationID, customerIdentification);
				break;
			case Constants.CONFIRMATION:
				// No action to do specifically for CONFIRMATION. Appropriate actions are taken
				// based on return value of isAccountCreationisNextStage() and
				// isThisLastStage().
				customerIdentification.setApplicationId(applicationID);
				customerIdentification.setVersionNum(version);
				break;
			default:
				logger.error("INVALID current screen ID");
				// call all the above methods at once if you need to insert all data at once at
				// the last screen (CONFIRMATION)
				break;
		}
		logger.error("customerIdentification 1 : " + customerIdentification + "  --  " + isThisLastStage);

		if (isThisLastStage) {
			Set<String> cbDetailsMissing = cbReportExists(applicationID, applyLoanRequest.getAppId(), version, prop);
			if (!cbDetailsMissing.isEmpty()) {
				String formattedMissing = String.join(", ", cbDetailsMissing);
				responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
				responseHeader.setResponseMessage(ResponseCodes.FAILURE.getValue());
				responseBody.setResponseObj("CB Report Missing for : " + formattedMissing);
				response.setResponseBody(responseBody);
				response.setResponseHeader(responseHeader);
				return Mono.just(response);
			}
			updateConfirmFlagInMaster(requestObj, version, applicationID, customerIdentification, prop,
					isSelfOnBoardingAppId, isSelfOnBoardingHeaderAppId);
		}
		if (isAccountCreationisNextStage) {
			HashMap<String, Object> hm = new HashMap<>(); // HM is used to keep number of arguments less than 8 as per
			// sonarqube
			hm.put(versionHm, version);
			hm.put(propHm, prop);
			hm.put(headerHm, header);
			hm.put(applicationIDHm, applicationID);
			return accountCreationStageOperations(hm, applicationMaster, requestObj, isSelfOnBoardingAppId,
					applyLoanRequest, customerIdentification, isSelfOnBoardingHeaderAppId);
		}

		Gson gson = new Gson();
		String responseStr = gson.toJson(customerIdentification);
		responseBody.setResponseObj(responseStr);
		response.setResponseBody(responseBody);
		response.setResponseHeader(responseHeader);
		return Mono.just(response);
	}

	private void updateQueryResponse(ApplyLoanRequestFields requestObj, String applicationID) {
		logger.debug("Entering updateQueryResponse for applicationID: {}", applicationID);
		JsonNode queryResponse = requestObj.getQueryResponse();
		ObjectMapper mapper = new ObjectMapper();
		String queryResponseString = "";
		try {
			queryResponseString = mapper.writeValueAsString(queryResponse);
			logger.debug("Converted queryResponse to string: {}", queryResponseString);
		} catch (JsonProcessingException e) {
			logger.error("Error converting query response to string: ", e);
		}

		Optional<SourcingResponseTracker> sourcingResponseTrackerOpt = sourcingResponseTrackerRepo.findById(applicationID);
		if (sourcingResponseTrackerOpt.isPresent()) {
			SourcingResponseTracker sourcingResponseTracker = sourcingResponseTrackerOpt.get();
			logger.debug("Found existing SourcingResponseTracker for applicationID: {}", applicationID);
			sourcingResponseTracker.setResponse(queryResponseString);
			sourcingResponseTrackerRepo.save(sourcingResponseTracker);
			logger.debug("Updated SourcingResponseTracker with new response for applicationID: {}", applicationID);
		} else {
			SourcingResponseTracker sourcingResponseTracker = new SourcingResponseTracker();
			sourcingResponseTracker.setApplicationId(applicationID);
			sourcingResponseTracker.setResponse(queryResponseString);
			sourcingResponseTracker.setCreatedAt(LocalDateTime.now());
			sourcingResponseTracker.setStage(""); // empty for now
			sourcingResponseTrackerRepo.save(sourcingResponseTracker);
			logger.debug("Created new SourcingResponseTracker for applicationID: {}", applicationID);
		}
		logger.debug("Exiting updateQueryResponse for applicationID: {}", applicationID);
	}

	public Set<String> cbReportExists(String applicationId, String appId, int versionNum, Properties prop) {
		Set<String> cbReportMissing = new HashSet<>();
		Gson gson = new Gson();
		String fileLocation = prop.getProperty(CobFlagsProperties.FILE_UPLOAD.getKey()) + "/"
				+ appId + Constants.LOANPATH + applicationId + "/";

		logger.debug("Checking CB reports for applicationId: {}, appId: {}, versionNum: {}", applicationId, appId, versionNum);
		logger.debug("file location for CB reports: {}", fileLocation);

		Optional<List<CibilDetails>> cibilDetailsOpt = cibilDtlRepo.findByApplicationIdAndAppIdAndVersionNum(applicationId, appId, versionNum);
		if (cibilDetailsOpt.isPresent()) {
			List<CibilDetails> cibilDetailsList = cibilDetailsOpt.get();
			logger.debug("Found {} CIBIL details for the application.", cibilDetailsList.size());

			for (CibilDetails cibilDetail : cibilDetailsList) {
				CibilDetailsPayload cibilDetailsPayload = gson.fromJson(cibilDetail.getPayloadColumn(), CibilDetailsPayload.class);
				String cbLoanId = cibilDetailsPayload.getCbLoanId();
				String applicantType = ""; // Initialize with a default value
				if(!cibilDetailsPayload.getBureauName().isEmpty() && cibilDetailsPayload.getBureauName().equalsIgnoreCase("BRE")){
					if (cbLoanId.startsWith("A")) {
						applicantType = Constants.APPLICANT;
					} else if (cbLoanId.startsWith("C")) {
						applicantType = Constants.COAPPLICANT_STRING;
					}

					File file = new File(fileLocation + cbLoanId + Constants.PDF_EXTENSION);
					if (!file.exists() && !applicantType.isEmpty()) { // Ensure applicantType is valid
						logger.warn("CB report missing for applicantType: {}, cbLoanId: {}", applicantType, cbLoanId);
						cbReportMissing.add(applicantType);
					} else {
						logger.debug("CB report exists for applicantType: {}, cbLoanId: {}", applicantType, cbLoanId);
					}
				}
			}
		} else {
			logger.warn("No CIBIL details found for applicationId: {}, appId: {}, versionNum: {}", applicationId, appId, versionNum);
			cbReportMissing.add(Constants.APPLICANT);
			cbReportMissing.add(Constants.COAPPLICANT_STRING);
		}

		logger.debug("CB report missing types: {}", cbReportMissing);
		return cbReportMissing;
	}

	private void updateCoApplicantId(ApplyLoanRequestFields requestObj, String applicationID, String appId) {
		ObjectMapper objectMapper = new ObjectMapper();
		Gson gson = new Gson();
		int customerIndex = 1;
		try {
			List<CustomerDetails> customerDetailsList = requestObj.getCustomerDetailsList();
			for (CustomerDetails customerDtl : customerDetailsList) {
				customerIndex = customerDtl.getPayload().getCustomerIndex();
			}
			Optional<CustomerDetails> customerDetails = custDtlRepo
					.findByApplicationIdAndAppIdAndCustomerType(applicationID, appId, Constants.APPLICANT);
			if (customerDetails.isPresent()) {
				CustomerDetails customerDetail = customerDetails.get();
				CustomerDetailsPayload customerDetailPayload = objectMapper.readValue(customerDetail.getPayloadColumn(),
						CustomerDetailsPayload.class);
				customerDetailPayload.setCustomerIndex(customerIndex);

				customerDetail.setPayloadColumn(gson.toJson(customerDetailPayload));
				logger.warn("customerDetail.toString() to be updated: " + customerDetail.toString());
				custDtlRepo.save(customerDetail);
			}
		} catch (Exception e) {
			logger.error("Error : ", e.getMessage());
		}

	}

	private void replicateApplicationDetails(ApplyLoanRequestFields requestObj,
											 CustomerIdentificationLoan customerIdentification, String applicationID, BigDecimal custDtlId,
											 int version) {
		Optional<RenewalLeadOccpInsDetails> occInsDetails = renewalLeadOccpInsDetailsRepo
				.findByPid(requestObj.getApplicationMaster().getWorkitemNo());
		logger.debug("app id :occInsDetails " + occInsDetails.toString());
		Optional<RenewalLeadDetails> renewalLeadData = renewalLeadDtlsRepo
				.findByPid(requestObj.getApplicationMaster().getWorkitemNo());
		logger.debug("app id :renewalLeadData  " + renewalLeadData.toString());
		logger.debug("inside replicate application occInsDetails.isPresent() --" + occInsDetails.isPresent());
		logger.debug("inside replicate application renewalLeadData.isPresent() --" + renewalLeadData.isPresent());
		if (occInsDetails.isPresent() && renewalLeadData.isPresent()) {
			RenewalLeadOccpInsDetails occInsDetail = occInsDetails.get();
			logger.debug("app id :occInsDetail  " + occInsDetail.toString());
			RenewalLeadDetails renewalLeadDetail = renewalLeadData.get();
			logger.debug("app id :renewalLeadDetail  " + renewalLeadDetail.toString());
			BigDecimal coAppCustDtlId = populateRenewalCustomerDtls(renewalLeadDetail, occInsDetail, requestObj,
					customerIdentification, applicationID, custDtlId, version);
			logger.debug("app id :coAppCustDtlId  " + coAppCustDtlId.toString());
			populateRenewalAddressDtls(renewalLeadDetail, occInsDetail, requestObj, customerIdentification,
					applicationID, custDtlId, coAppCustDtlId, version, Constants.CUSTOMER_DETAILS);
			populateRenewalOccupationdtls(renewalLeadDetail, occInsDetail, requestObj, customerIdentification,
					applicationID, custDtlId, coAppCustDtlId, version);
			populateRenewalInsuranceDtls(renewalLeadDetail, occInsDetail, requestObj, customerIdentification,
					applicationID, custDtlId, coAppCustDtlId, version);
		}
	}

	public Mono<Response> applyRejectLoan(ApplyLoanRequest applyLoanRequest, boolean isSelfOnBoardingHeaderAppId,
										  Properties prop) {
		ApplyLoanRequestFields requestObj = applyLoanRequest.getRequestObj();
		ApplicationMaster applicationMaster = requestObj.getApplicationMaster();
		ResponseBody responseBody = new ResponseBody();
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		responseHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());
		CustomerIdentificationLoan customerIdentification = new CustomerIdentificationLoan();
		String applicationID = CommonUtils.generateRandomNumStr();
		Optional<ApplicationMaster> masterData = applicationMasterRepo.findByAppIdAndWorkitemNo(requestObj.getAppId(),
				applicationMaster.getWorkitemNo());
		if (masterData.isPresent()) {
			applicationID = masterData.get().getApplicationId();
		}
		int version = Constants.INITIAL_VERSION_NO;
		String custType = (requestObj.getApplicationMaster().getCustDtlSlNum() <= 1) ? Constants.APPLICANT
				: Constants.COAPPLICANT;
		BigDecimal custDtlId = commonParamService.generateCustDtlId(applicationID, custType);
		populateAppMasterAndApplnwf(requestObj, applicationID, version, customerIdentification,
				isSelfOnBoardingHeaderAppId, prop);
		commonParamService.populateCustomerDtlsIfNotPresent(requestObj.getApplicationMaster(), applicationID, custDtlId,
				version, requestObj.getAppId());

		String[] currentScreenIdArray = requestObj.getApplicationMaster().getCurrentScreenId().split("~");
		commonParamService.updateCurrentStageInMaster(requestObj.getApplicationMaster(), currentScreenIdArray, version,
				requestObj.getAppId(), requestObj.getApplicationId());

		populateCustomerDtls(requestObj, customerIdentification, applicationID, custDtlId, version);
		populateAddressDtls(requestObj, customerIdentification, applicationID, custDtlId, version,
				Constants.CUSTOMER_DETAILS, custType);

		UpdateApplicantsCountRequest updateApplicantsCountRequest = new UpdateApplicantsCountRequest();
		UpdateApplicantsCountRequestFields updateApplicantsCountRequestFields = new UpdateApplicantsCountRequestFields();
		updateApplicantsCountRequestFields.setAppId(requestObj.getAppId());
		updateApplicantsCountRequestFields.setApplicationId(applicationID);
		updateApplicantsCountRequestFields.setVersionNum(version);
		updateApplicantsCountRequestFields.setApplicantsCount(requestObj.getApplicationMaster().getApplicantsCount());
		updateApplicantsCountRequest.setRequestObj(updateApplicantsCountRequestFields);
		cobService.updateApplicantsCount(updateApplicantsCountRequest);

		populateExistingLoanDtls(requestObj, customerIdentification, applicationID, custDtlId, version,
				Constants.CUSTOMER_DETAILS);

		Mono<Response> rejectRespBlock = callRejectApplication(applyLoanRequest.getAppId(),
				applyLoanRequest.getUserId(), prop, applicationID, custDtlId, applicationMaster.getRemarks());
		Response rejectResp = rejectRespBlock.block();
		if (null != rejectResp) {
			if (rejectResp.getResponseHeader().getResponseCode().equals("0")) {
				Gson gson = new Gson();
				String responseStr = gson.toJson(customerIdentification);
				responseBody.setResponseObj(responseStr);
				response.setResponseBody(responseBody);
				response.setResponseHeader(responseHeader);
			} else {
				response.setResponseBody(rejectResp.getResponseBody());
				response.setResponseHeader(rejectResp.getResponseHeader());
			}
		} else {
			response = CommonUtils.formFailResponse(ResponseCodes.REJECT_RES_NOT_VALID.getValue(),
					ResponseCodes.REJECT_RES_NOT_VALID.getKey());
		}
		return Mono.just(response);
	}

	private Mono<Response> callRejectApplication(String appId, String userId, Properties prop, String applicationId,
												 BigDecimal custDtlId, String remarks) {
		Mono<Response> response = Mono.empty();
		String roleId = commonParamService.fetchRoleId(appId, userId);
		RoleAccessMap objDb = cobService.fetchRoleAccessMapObj(appId, roleId);
		if (Constants.ACCESS_PERMISSION_VIEWONLY.equalsIgnoreCase(objDb.getAccessPermission())) {
			response = CommonUtils.formFailResponseMono(ResponseCodes.VAPT_ISSUE_PERMISSION.getValue(),
					ResponseCodes.VAPT_ISSUE_PERMISSION.getKey());
		} else if (Constants.ACCESS_PERMISSION_APPROVER.equalsIgnoreCase(objDb.getAccessPermission())
				|| Constants.ACCESS_PERMISSION_BOTH.equalsIgnoreCase(objDb.getAccessPermission())
				|| Constants.ACCESS_PERMISSION_VERIFIER.equalsIgnoreCase(objDb.getAccessPermission())
				|| Constants.ACCESS_PERMISSION_INITIATOR.equalsIgnoreCase(objDb.getAccessPermission())) {

			String rejectionFlowId = "REJECTAPPLICATION";
			String stageId = "INPUTINPROGRESS";
			String action = "REJECT";

			Optional<WorkflowDefinition> workflowDef = wfDefnRepoLn.findByAppIdAndWorkFlowIdAndFromStageId(appId,
					rejectionFlowId, stageId);
			if (workflowDef.isPresent()) {
				WorkFlowDetails wf = new WorkFlowDetails();
				wf.setCurrentRole(roleId);
				wf.setAction(action);
				wf.setWorkflowId(rejectionFlowId);
				wf.setCurrentStage(stageId);
				wf.setNextStageId(workflowDef.get().getNextStageId());
				wf.setNextWorkflowStatus(workflowDef.get().getNextWorkflowStatus());
				FetchDeleteUserFields reqFields = new FetchDeleteUserFields();
				reqFields.setAppId(appId);
				reqFields.setStatus(AppStatus.REJECTED.getValue());
				reqFields.setUserId(userId);
				reqFields.setApplicationId(applicationId);
				reqFields.setVersionNum(Constants.INITIAL_VERSION_NO);
				reqFields.setCustDtlId(custDtlId);
				reqFields.setRemarks(remarks);
				reqFields.setWorkFlow(wf);
				FetchDeleteUserRequest req = new FetchDeleteUserRequest();
				req.setAppId(appId);
				req.setRequestObj(reqFields);
				logger.debug("Approve reject application request: {} ", req);
				response = rejectApplication(req, prop);
			}
		}
		return response;
	}

	private Mono<Response> rejectApplication(FetchDeleteUserRequest fetchDeleteUserRequest, Properties prop) {
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();
		response.setResponseHeader(responseHeader);
		FetchDeleteUserFields customerDataFields = fetchDeleteUserRequest.getRequestObj();
		String status = customerDataFields.getStatus();
		List<String> applnStatus = new ArrayList<>();
		applnStatus.add(AppStatus.PENDING.getValue());
		applnStatus.add(AppStatus.INPROGRESS.getValue());
		logger.debug("app id : {} ", customerDataFields.getAppId());
		logger.debug("application id : {} ", customerDataFields.getApplicationId());
		logger.debug("version no : {} ", customerDataFields.getVersionNum());
		logger.debug("application status : {} ", applnStatus);
		Optional<ApplicationMaster> masterObjDb = applicationMasterRepo
				.findByAppIdAndApplicationIdAndVersionNumAndApplicationStatusIn(customerDataFields.getAppId(),
						customerDataFields.getApplicationId(), customerDataFields.getVersionNum(), applnStatus);
		logger.debug("Getting optional master object");
		if (masterObjDb.isPresent()) {
			logger.debug("Master data value present.");
			Gson gson = new Gson();
			CustomerIdentificationCasa customerIdentification = new CustomerIdentificationCasa();
			ApplicationMaster masterObj = masterObjDb.get();
			masterObj.setRemarks(customerDataFields.getRemarks());
			masterObj.setAssignedTo(null);
			cobService.updateStatus(masterObj, status);
			if (!CommonUtils.isNullOrEmpty(masterObj.getRelatedApplicationId())) {
				Optional<ApplicationMaster> appMasterRelated = applicationMasterRepo
						.findByAppIdAndApplicationIdAndVersionNum(customerDataFields.getAppId(),
								masterObj.getRelatedApplicationId(), customerDataFields.getVersionNum());
				if (appMasterRelated.isPresent()) {
					ApplicationMaster appMasterObjRelated = appMasterRelated.get();
					cobService.updateStatus(appMasterObjRelated, status);
				}
			}
			PopulateapplnWFRequest req = new PopulateapplnWFRequest();
			PopulateapplnWFRequestFields reqFields = new PopulateapplnWFRequestFields();
			reqFields.setAppId(masterObj.getAppId());
			reqFields.setApplicationId(masterObj.getApplicationId());
			reqFields.setCreatedBy(customerDataFields.getUserId());
			reqFields.setVersionNum(masterObj.getVersionNum());
			reqFields.setApplicationStatus(masterObj.getApplicationStatus());
			WorkFlowDetails wf = customerDataFields.getWorkFlow();
			wf.setRemarks(customerDataFields.getRemarks());
			reqFields.setWorkflow(wf);
			req.setRequestObj(reqFields);
			commonParamService.populateApplnWorkFlow(req);
			responseHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());
			customerIdentification.setVersionNum(customerDataFields.getVersionNum());
			responseBody.setResponseObj(gson.toJson(customerIdentification));
			response.setResponseBody(responseBody);
			response.setResponseHeader(responseHeader);
			logger.debug("application status update completed");
		} else {
			responseHeader.setResponseCode(ResponseCodes.INVALID_APP_MASTER.getKey());
		}
		response.setResponseBody(responseBody);
		response.setResponseHeader(responseHeader);
		return Mono.just(response);
	}

	private Mono<Response> accountCreationStageOperations(HashMap<String, Object> hm,
														  ApplicationMaster applicationMaster, ApplyLoanRequestFields requestObj, boolean isSelfOnBoardingAppId,
														  ApplyLoanRequest applyLoanRequest, CustomerIdentificationLoan customerIdentification,
														  boolean isSelfOnBoardingHeaderAppId) {
		int version = (int) hm.get(versionHm);
		Properties prop = (Properties) hm.get(propHm);
		Header header = (Header) hm.get(headerHm);
		String applicationID = (String) hm.get(applicationIDHm);
		String[] strAr1 = new String[] { Constants.ACCOUNT_CREATION, "Y" };
		commonParamService.updateCurrentStageInMaster(applicationMaster, strAr1, version, requestObj.getAppId(),
				requestObj.getApplicationId());
		HashMap<String, Object> hm1 = new HashMap<>(); // HM is used to keep number of arguments less than 8 as per
		// sonarqube
		hm1.put(propHm, prop);
		hm1.put(versionHm, version);
		return createAccountInCbs(hm1, isSelfOnBoardingAppId, applyLoanRequest, customerIdentification, header,
				isSelfOnBoardingHeaderAppId, applicationID);
	}

	private Mono<Response> createAccountInCbs(HashMap<String, Object> hm1, boolean isSelfOnBoardingAppId,
											  ApplyLoanRequest applyLoanRequest, CustomerIdentificationLoan customerIdentification, Header header,
											  boolean isSelfOnBoardingHeaderAppId, String applicationID) {
		Properties prop = (Properties) hm1.get(propHm);
		int version = (int) hm1.get(versionHm);
		ApplyLoanRequestFields requestObj = applyLoanRequest.getRequestObj();
		ApplicationMaster masterRequest = requestObj.getApplicationMaster();
		Optional<ApplicationMaster> masterObjDb = applicationMasterRepo
				.findByAppIdAndApplicationIdAndVersionNumAndApplicationStatus(requestObj.getAppId(),
						requestObj.getApplicationId(), version, AppStatus.INPROGRESS.getValue());
		if (masterObjDb.isPresent()) {
			ApplicationMaster masterObj = masterObjDb.get();
			if (isSelfOnBoardingAppId) { // self onboarding
				if ("N".equalsIgnoreCase(prop.getProperty(CobFlagsProperties.LOAN_STP.getKey()))) {
					commonParamService.createAccountInCbsForNonStp(isSelfOnBoardingHeaderAppId, masterObj);
				} else if ("Y".equalsIgnoreCase(prop.getProperty(CobFlagsProperties.LOAN_STP.getKey()))) {
					String accNum;
					if (CommonUtils.isNullOrEmpty(masterRequest.getAccNumber())) {
						accNum = CommonUtils.generateRandomNumStr();
					} else {
						accNum = masterRequest.getAccNumber();
					}
					masterObj.setAccNumber(accNum);
					customerIdentification.setAccNumber(accNum);
					masterObj.setApplicationStatus(AppStatus.APPROVED.getValue());
					applicationMasterRepo.save(masterObj);

					// Hook to call external service for loan account creation.

					CreateLoanRequest extReq = formExtReq(requestObj.getAppId(), requestObj.getApplicationId(), version,
							accNum, masterObj.getCustomerId());
					String interfaceName = prop.getProperty(CobFlagsProperties.LOAN_ACC_CREATION_INTF.getKey());
					Mono<Object> extRes = interfaceAdapter.callExternalService(header, extReq, interfaceName);

					return extRes.flatMap(val -> {
						customerIdentification.setApplicationId(applicationID);
						customerIdentification.setVersionNum(version);
						ResponseBody responseBody = new ResponseBody();
						Response response = new Response();
						ResponseHeader responseHeader = new ResponseHeader();
						responseHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());
						Gson gson = new Gson();
						String responseStr = gson.toJson(customerIdentification);
						responseBody.setResponseObj(responseStr);
						response.setResponseBody(responseBody);
						response.setResponseHeader(responseHeader);
						return Mono.just(response);
					});
				}
			} else { // assisted on boarding
				masterObj.setApplicationStatus(AppStatus.PENDING.getValue());
				applicationMasterRepo.save(masterObj);
			}
		}
		ResponseBody responseBody = new ResponseBody();
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		responseHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());
		Gson gson = new Gson();
		String responseStr = gson.toJson(customerIdentification);
		responseBody.setResponseObj(responseStr);
		response.setResponseBody(responseBody);
		response.setResponseHeader(responseHeader);
		return Mono.just(response);
	}

	public CreateLoanRequest formExtReq(String appId, String applicationId, int version, String accNum,
										BigDecimal customerId) {
		Optional<ApplicationMaster> applicationMasterOpt = applicationMasterRepo
				.findByAppIdAndApplicationIdAndVersionNum(appId, applicationId, version);
		CreateLoanRequest request = null;
		if (applicationMasterOpt.isPresent()) {
			request = new CreateLoanRequest();
			CreateLoanRequestFields requestObj = new CreateLoanRequestFields();
			ApplicationMaster applicationMasterData = applicationMasterOpt.get();
			ApplicationMaster masterObj = new ApplicationMaster();
			BeanUtils.copyProperties(applicationMasterData, masterObj);
			masterObj.setAccNumber(accNum);
			masterObj.setCustomerId(customerId);
			masterObj.setCreateTs(null); // to avoid jackson parsing error. Need to send data based on external service
			// request during implementation.
			masterObj.setApplicationDate(null); // to avoid jackson parsing error. Need to send data based on external
			// service request during implementation.
			requestObj.setApplicationMaster(masterObj);

			LoanDetails loanDtl = loanDtlsRepo.findByApplicationIdAndAppIdAndVersionNum(applicationId, appId, version);
			requestObj.setLoanDetails(loanDtl);

			request.setRequestObj(requestObj);
		}
		return request;
	}

	private void populateOccupationdtls(ApplyLoanRequestFields requestObj,
										CustomerIdentificationLoan customerIdentification, String applicationID, BigDecimal custDtlId, int version,
										String custType) {
		Gson gson = new Gson();
		List<String> occupationList = new ArrayList<>();
		List<OccupationDetailsWrapper> occupationDetailsWrapperList = requestObj.getOccupationDetailsWrapperList();
		for (OccupationDetailsWrapper occupationDetailsWrapper : occupationDetailsWrapperList) {
			OccupationDetails occupationDetails = occupationDetailsWrapper.getOccupationDetails();

			Optional<OccupationDetails> occupationDetailsDb = this.occupationDtlRepo
					.findOccupationDetailsByCustomerType(custType, applicationID);

			if (occupationDetailsDb.isPresent()) {
				occupationDetails.setOccptDtlId(occupationDetailsDb.get().getOccptDtlId());
				occupationList.add(occupationDetailsDb.get().getOccptDtlId().toString());
			} else {
				BigDecimal occptnDtlId = CommonUtils.generateRandomNum();
				occupationDetails.setOccptDtlId(occptnDtlId);
				occupationList.add(occptnDtlId.toString());// to String is required to avoid rounding issue of Big
				// Decimal at front end.
			}
			occupationDetails.setAppId(requestObj.getAppId());
			occupationDetails.setApplicationId(applicationID);
			occupationDetails.setCustDtlId(custDtlId);
			occupationDetails.setVersionNum(version);
			String payload = gson.toJson(occupationDetails.getPayload());
			occupationDetails.setPayloadColumn(payload);
			occupationDtlRepo.save(occupationDetails);
			logger.warn("Data inserted into TB_ABOB_OCCUPATION_DETAILS for loans");
		}
		customerIdentification.setOccupationList(occupationList);
		customerIdentification.setCustDtlId(custDtlId.toString());// to String is required to avoid rounding issue of
		// Big Decimal at front end.
		customerIdentification.setApplicationId(applicationID);
		customerIdentification.setVersionNum(version);
		logger.warn("Data inserted into TB_ABOB_OCCUPATION_DETAILS for loans");
	}

	private void populateRenewalOccupationdtls(RenewalLeadDetails renewalLeadDetail,
											   RenewalLeadOccpInsDetails occInsDetail, ApplyLoanRequestFields requestObj,
											   CustomerIdentificationLoan customerIdentification, String applicationID, BigDecimal custDtlId,
											   BigDecimal coAppCustDtlId, int version) {
		Gson gson = new Gson();
		OccupationDetails occupationDetail = new OccupationDetails();
		BigDecimal appOoccptnDtlId = CommonUtils.generateRandomNum();
		occupationDetail.setOccptDtlId(appOoccptnDtlId);
		occupationDetail.setAppId(requestObj.getAppId());
		occupationDetail.setApplicationId(applicationID);
		occupationDetail.setCustDtlId(custDtlId);
		occupationDetail.setVersionNum(version);

		OccupationDetailsPayload appPayload = new OccupationDetailsPayload();

		appPayload.setOccupationType(getDefaultValueIfObjNull(occInsDetail.getOccupationType()));
		appPayload.setDesignation(getDefaultValueIfObjNull(occInsDetail.getDesignation()));
		appPayload.setAnnualIncome(
				occInsDetail.getAnnualIncome() == null ? null : new BigDecimal(occInsDetail.getAnnualIncome()));
		appPayload.setOrganisationName(getDefaultValueIfObjNull(occInsDetail.getOrganisationName()));
		appPayload.setOfficePhone("");
		appPayload.setOfficeEmail("");
		appPayload.setEmployeeId("");
		appPayload.setEmployeeSince(getDefaultValueIfObjNull(occInsDetail.getEmployeeSince()));
		appPayload.setExperience(getDefaultValueIfObjNull(occInsDetail.getExperience()));
		appPayload.setEmployer(getDefaultValueIfObjNull(occInsDetail.getEmployer()));
		appPayload.setRetirementAge(getDefaultValueIfObjNull(occInsDetail.getRetirementAge()));
		appPayload.setLastEmployer(getDefaultValueIfObjNull(occInsDetail.getLastEmployer()));
		appPayload.setPreviousJobYears(getDefaultValueIfObjNull(occInsDetail.getPreviousJobYears()));
		appPayload.setTypeOfEmployer(getDefaultValueIfObjNull(occInsDetail.getTypeOfEmployer()));
		appPayload.setNatureOfOccupation(getDefaultValueIfObjNull(occInsDetail.getNatureOfOccupation()));
		appPayload.setAddressProof("");
		appPayload.setBusinessAddressProof(getDefaultValueIfObjNull(occInsDetail.getBusinessAddressProof()));
		appPayload.setEmploymentProof(getDefaultValueIfObjNull(occInsDetail.getEmploymentProof()));
		appPayload.setEmployeeActivity(getDefaultValueIfObjNull(occInsDetail.getEmployeeActivity()));
		appPayload.setBusinessPremiseOwnerShip(getDefaultValueIfObjNull(occInsDetail.getBusinessPremiseOwnerShip()));
		appPayload.setFreqOfIncome(getDefaultValueIfObjNull(occInsDetail.getFreqOfIncome()));
		appPayload.setOtherSourceIncome(getDefaultValueIfObjNull(occInsDetail.getOtherSourceIncome()));
		appPayload.setOtherSourceAnnualIncome(getDefaultValueIfObjNull(occInsDetail.getOtherSourceAnnualIncome()));
		appPayload.setStreetVendor(getDefaultValueIfObjNull(occInsDetail.getStreetVendor()));
		appPayload.setModeOfIncome("");
		appPayload.setTypeofbusiness(getDefaultValueIfObjNull(occInsDetail.getTypeofbusiness()));
		appPayload.setBusinessEmpStartDate(
				CommonUtils.formatCustomDate(getDefaultValueIfObjNull(occInsDetail.getBusinessEmpStartDate()), "dd/MM/yyyy"));
		appPayload.setBusinessEmpVintageYear(getDefaultValueIfObjNull(occInsDetail.getBusinessEmpVintageYear()));
		appPayload.setOccupationTag(getDefaultValueIfObjNull(occInsDetail.getOccupationTag()));

		String PayloadStr = gson.toJson(appPayload);
		occupationDetail.setPayloadColumn(PayloadStr);
		occupationDtlRepo.save(occupationDetail);
		logger.warn("Data inserted into TB_ABOB_OCCUPATION_DETAILS for applicant loans");

		OccupationDetails coAppOccupationDetail = new OccupationDetails();
		BigDecimal coappOoccptnDtlId = CommonUtils.generateRandomNum();
		coAppOccupationDetail.setOccptDtlId(coappOoccptnDtlId);
		coAppOccupationDetail.setAppId(requestObj.getAppId());
		coAppOccupationDetail.setApplicationId(applicationID);
		coAppOccupationDetail.setCustDtlId(coAppCustDtlId);
		coAppOccupationDetail.setVersionNum(version);

		OccupationDetailsPayload coappPayload = new OccupationDetailsPayload();

		coappPayload.setOccupationType(getDefaultValueIfObjNull(occInsDetail.getCoOccupationtype()));
		coappPayload.setDesignation(getDefaultValueIfObjNull(occInsDetail.getCoDesignation()));
		coappPayload.setAnnualIncome(
				occInsDetail.getCoAnnualincome() == null ? null : new BigDecimal(occInsDetail.getCoAnnualincome()));
		coappPayload.setOrganisationName(getDefaultValueIfObjNull(occInsDetail.getCoOrganisationname()));
		coappPayload.setOfficePhone("");
		coappPayload.setOfficeEmail("");
		coappPayload.setEmployeeId("");
		coappPayload.setEmployeeSince(getDefaultValueIfObjNull(occInsDetail.getCoEmployeesince()));
		coappPayload.setExperience(getDefaultValueIfObjNull(occInsDetail.getCoExperience()));
		coappPayload.setEmployer(getDefaultValueIfObjNull(occInsDetail.getCoEmployer()));
		coappPayload.setRetirementAge(getDefaultValueIfObjNull(occInsDetail.getCoRetirementage()));
		coappPayload.setLastEmployer(getDefaultValueIfObjNull(occInsDetail.getCoLastemployer()));
		coappPayload.setPreviousJobYears(getDefaultValueIfObjNull(occInsDetail.getCoPreviousjobyears()));
		coappPayload.setTypeOfEmployer(getDefaultValueIfObjNull(occInsDetail.getCoTypeofemployer()));
		coappPayload.setNatureOfOccupation(getDefaultValueIfObjNull(occInsDetail.getCoNatureofoccupation()));
		coappPayload.setAddressProof("");
		coappPayload.setBusinessAddressProof(getDefaultValueIfObjNull(occInsDetail.getCoBusinessaddressproof()));
		coappPayload.setEmploymentProof(getDefaultValueIfObjNull(occInsDetail.getCoEmploymentproof()));
		coappPayload.setEmployeeActivity(getDefaultValueIfObjNull(occInsDetail.getCoEmployeeactivity()));
		coappPayload
				.setBusinessPremiseOwnerShip(getDefaultValueIfObjNull(occInsDetail.getCoBusinesspremiseownership()));
		coappPayload.setFreqOfIncome(getDefaultValueIfObjNull(occInsDetail.getCoFreqofincome()));
		coappPayload.setOtherSourceIncome(getDefaultValueIfObjNull(occInsDetail.getCoOthersourceincome()));
		coappPayload.setOtherSourceAnnualIncome(getDefaultValueIfObjNull(occInsDetail.getCoOthersourceannualincome()));
		coappPayload.setStreetVendor(getDefaultValueIfObjNull(occInsDetail.getCoStreetvendor()));
		coappPayload.setModeOfIncome("");
		coappPayload.setTypeofbusiness(getDefaultValueIfObjNull(occInsDetail.getCoTypeofbusiness()));
		coappPayload.setBusinessEmpStartDate(CommonUtils
				.formatCustomDate(getDefaultValueIfObjNull(occInsDetail.getCoBusinessempstartdate()), "dd/MM/yyyy"));
		coappPayload.setBusinessEmpVintageYear(getDefaultValueIfObjNull(occInsDetail.getCoBusinessempvintageyear()));
		coappPayload.setOccupationTag(getDefaultValueIfObjNull(occInsDetail.getCoOccupationtag()));

		String coAppPayloadStr = gson.toJson(coappPayload);
		coAppOccupationDetail.setPayloadColumn(coAppPayloadStr);
		occupationDtlRepo.save(coAppOccupationDetail);
		logger.warn("Data inserted into TB_ABOB_OCCUPATION_DETAILS for coapplicant loans");

	}

	private void populateCoAppOccupationAddressdtls(ApplyLoanRequestFields requestObj, String applicationID,
													BigDecimal custDtlId, int version) {
		Gson gson = new Gson();
		ObjectMapper objectMapper = new ObjectMapper();
		List<String> occupationList = new ArrayList<>();
		List<OccupationDetailsWrapper> occupationDetailsWrapperList = requestObj.getOccupationDetailsWrapperList();
		for (OccupationDetailsWrapper occupationDetailsWrapper : occupationDetailsWrapperList) {
			OccupationDetails occupationDetails = occupationDetailsWrapper.getOccupationDetails();
			if (occupationDetails.getOccptDtlId() == null) {// This is to handle the case if user changed the data after
				// its being inserted by using the back navigation within
				// the session.
				BigDecimal occptnDtlId = CommonUtils.generateRandomNum();
				occupationDetails.setOccptDtlId(occptnDtlId);
				occupationList.add(occptnDtlId.toString());// to String is required to avoid rounding issue of Big
				// Decimal at front end.
			} else {
				occupationList.add(occupationDetails.getOccptDtlId().toString());// to String is required to avoid
				// rounding issue of Big Decimal at
				// front end.
			}
			occupationDetails.setAppId(requestObj.getAppId());
			occupationDetails.setApplicationId(applicationID);
			occupationDetails.setCustDtlId(custDtlId);
			occupationDetails.setVersionNum(version);
			String payload = gson.toJson(occupationDetails.getPayload());
			occupationDetails.setPayloadColumn(payload);
			List<CustomerDetails> customerList = requestObj.getCustomerDetailsList();
			logger.warn("customerList : " + customerList);
			boolean isApplicant = false;
			for (CustomerDetails customer : customerList) {
				logger.warn("customer 1: " + (custDtlId.compareTo(customer.getCustDtlId()) == 0));
				logger.warn("customer 2: " + ("Applicant".equalsIgnoreCase(customer.getCustomerType())));
				if ((custDtlId.compareTo(customer.getCustDtlId()) == 0)
						&& ("Applicant".equalsIgnoreCase(customer.getCustomerType())))
					isApplicant = true;
				logger.warn("customer : " + customer + "-" + isApplicant);
				logger.warn("customer : " + custDtlId + " - " + customer.getCustDtlId() + " - "
						+ customer.getCustomerType());
			}
			logger.debug("isApplicant : " + isApplicant);
			try {
				if (isApplicant) {
					List<OccupationDetails> coAppOccupationDetailsList = occupationDtlRepo
							.findByApplicationIdAndAppIdAndCustDtlIdNot(applicationID, requestObj.getAppId(),
									custDtlId);
					logger.debug("coAppOccupationDetailsList : " + coAppOccupationDetailsList);
					for (OccupationDetails coAppOccupationDetail : coAppOccupationDetailsList) {
						logger.debug("coAppOccupationDetail : " + coAppOccupationDetail.toString());
						OccupationDetailsPayload coAppOccupationPayload = objectMapper
								.readValue(coAppOccupationDetail.getPayloadColumn(), OccupationDetailsPayload.class);
						logger.debug("coAppOccupationPayload.getOccupationTag() : "
								+ coAppOccupationPayload.getOccupationTag());
						if ((coAppOccupationPayload.getOccupationTag() != null)
								&& ("yes".equalsIgnoreCase(coAppOccupationPayload.getOccupationTag()))) {
							OccupationDetailsPayload applicantOccupationPayload = objectMapper
									.readValue(occupationDetails.getPayloadColumn(), OccupationDetailsPayload.class);

							applicantOccupationPayload.setOccupationTag(coAppOccupationPayload.getOccupationTag());
							coAppOccupationDetail.setPayloadColumn(gson.toJson(applicantOccupationPayload));
							occupationDtlRepo.save(coAppOccupationDetail);
							logger.warn("Data inserted into TB_ABOB_OCCUPATION_DETAILS for coApplicant loans");
							AddressDetails applicantAddressDetails = new AddressDetails();
							AddressDetails coApplicantAddressDetails = new AddressDetails();
							List<AddressDetails> appAddressDetailsList = addressDtlRepo
									.findByApplicationIdAndAppId(applicationID, requestObj.getAppId());
							for (AddressDetails addressDetail : appAddressDetailsList) {
								AddressDetailsPayload addrPayload = objectMapper
										.readValue(addressDetail.getPayloadColumn(), AddressDetailsPayload.class);

								for (Address address : addrPayload.getAddressList()) {
									if ("Office".equalsIgnoreCase(address.getAddressType())) {
										if (custDtlId.compareTo(addressDetail.getCustDtlId()) == 0) {
											applicantAddressDetails = addressDetail;
										} else {
											coApplicantAddressDetails = addressDetail;
										}
									}
								}
							}
							AddressDetailsPayload applicantPayload = objectMapper
									.readValue(applicantAddressDetails.getPayloadColumn(), AddressDetailsPayload.class);
							logger.debug("applicantPayload : " + applicantPayload.toString());
							logger.debug("applicantAddressDetails : " + applicantAddressDetails.toString());
							logger.debug("coApplicantAddressDetails : " + coApplicantAddressDetails.toString());
							coApplicantAddressDetails.setPayloadColumn(gson.toJson(applicantPayload));
							logger.debug("coApplicantAddressDetails after : " + coApplicantAddressDetails.toString());
							addressDtlRepo.save(coApplicantAddressDetails);
						}

					}
				}
			} catch (Exception e) {
				logger.error("Error : ", e.getMessage());
			}
		}
		logger.warn("Data inserted into TB_ABOB_OCCUPATION_DETAILS for loans");
	}

	private void populateInsuranceDtls(ApplyLoanRequestFields requestObj,
									   CustomerIdentificationLoan customerIdentification, String applicationID, BigDecimal custDtlId, int version,
									   String custType) {
		Gson gson = new Gson();
		List<String> insuranceList = new ArrayList<>();
		List<InsuranceDetailsWrapper> insuranceDetailsWrapperList = requestObj.getInsuranceDetailsWrapperList();
		for (InsuranceDetailsWrapper insuranceDetailsWrapper : insuranceDetailsWrapperList) {
			InsuranceDetails insuranceDetails = insuranceDetailsWrapper.getInsuranceDetails();

			Optional<InsuranceDetails> insuranceDetailsDb = insuranceDtlRepo
					.findInsuranceDetailsByCustomerType(custType, applicationID);

			if (insuranceDetailsDb.isPresent()) {
				insuranceDetails.setInsuranceDtlId(insuranceDetailsDb.get().getInsuranceDtlId());
				insuranceList.add(insuranceDetailsDb.get().getInsuranceDtlId().toString());
			} else {
				BigDecimal insuranceDtlId = CommonUtils.generateRandomNum();
				insuranceDetails.setInsuranceDtlId(insuranceDtlId);
				insuranceList.add(insuranceDtlId.toString());// to String is required to avoid rounding issue of Big
				// Decimal at front end.
			}
			insuranceDetails.setAppId(requestObj.getAppId());
			insuranceDetails.setApplicationId(applicationID);
			insuranceDetails.setCustDtlId(custDtlId);
			insuranceDetails.setVersionNum(version);
			String payload = gson.toJson(insuranceDetails.getPayload());
			insuranceDetails.setPayloadColumn(payload);
			insuranceDtlRepo.save(insuranceDetails);
			logger.warn("Data inserted into TB_CGOB_INSURANCE_DTLS for loans");
			if ((requestObj.getApplicationMaster().getCustDtlSlNum() <= 1)
					&& ("No".equalsIgnoreCase(insuranceDetails.getPayload().getCoApplicantInsurance()))) {
				Optional<InsuranceDetails> insuDtlObj = insuranceDtlRepo
						.findByApplicationIdAndAppIdAndCustDtlIdNot(applicationID, requestObj.getAppId(), custDtlId);
				logger.warn("insuDtlObj : " + insuDtlObj.toString());
				if (insuDtlObj.isPresent()) {
					ObjectMapper objectMapper = new ObjectMapper();
					InsuranceDetails insuDtl = insuDtlObj.get();
					InsuranceDetailsPayload insuDtlsPayload;
					try {
						insuDtlsPayload = objectMapper.readValue(insuDtl.getPayloadColumn(),
								InsuranceDetailsPayload.class);
						insuDtl.setPayload(insuDtlsPayload);
						insuDtl.getPayload().setAge("");
						insuDtl.getPayload().setCoApplicantInsurance("");
						insuDtl.getPayload().setInsuranceReqd("");
						insuDtl.getPayload().setInsuredName("");
						insuDtl.getPayload().setNomineeDob("");
						insuDtl.getPayload().setNomineeName("");
						insuDtl.getPayload().setNomineeRelation("");
						String coAppPayload = gson.toJson(insuDtl.getPayload());
						insuDtl.setPayloadColumn(coAppPayload);
						logger.warn("insuDtl : " + insuDtl.toString());
						insuranceDtlRepo.save(insuDtl);
						logger.warn("Data updated into TB_CGOB_INSURANCE_DTLS for loans");
					} catch (Exception e) {
						logger.error("Error while updating insurance details : {}" , e);
					}

				}
			}
		}
		customerIdentification.setInsuranceList(insuranceList);
		customerIdentification.setCustDtlId(custDtlId.toString());// to String is required to avoid rounding issue of
		// Big Decimal at front end.
		customerIdentification.setApplicationId(applicationID);
		customerIdentification.setVersionNum(version);
		logger.warn("Data inserted into TB_CGOB_INSURANCE_DTLS for loans");
	}

	private void populateRenewalInsuranceDtls(RenewalLeadDetails renewalLeadDetail,
											  RenewalLeadOccpInsDetails occInsDetail, ApplyLoanRequestFields requestObj,
											  CustomerIdentificationLoan customerIdentification, String applicationID, BigDecimal custDtlId,
											  BigDecimal coAppCustDtlId, int version) {
		Gson gson = new Gson();
		InsuranceDetails appInsuranceDetail = new InsuranceDetails();
		BigDecimal appInsuranceDtlId = CommonUtils.generateRandomNum();
		appInsuranceDetail.setInsuranceDtlId(appInsuranceDtlId);
		appInsuranceDetail.setAppId(requestObj.getAppId());
		appInsuranceDetail.setApplicationId(applicationID);
		appInsuranceDetail.setCustDtlId(custDtlId);
		appInsuranceDetail.setVersionNum(version);
		InsuranceDetailsPayload appPayload = new InsuranceDetailsPayload();
		String appPayloadInsuranceReqd = getDefaultValueIfObjNull(occInsDetail.getInsuranceReqd());
		if ("yes".equalsIgnoreCase(appPayloadInsuranceReqd)) {
			appPayloadInsuranceReqd = "Y";
		} else if ("no".equalsIgnoreCase(appPayloadInsuranceReqd)) {
			appPayloadInsuranceReqd = "N";
		}
		appPayload.setInsuranceReqd(appPayloadInsuranceReqd);
		appPayload.setInsuredName(getDefaultValueIfObjNull(occInsDetail.getInsuredName()));
		appPayload.setNomineeName(getDefaultValueIfObjNull(occInsDetail.getNomineeName()));
		appPayload.setNomineeRelation(getDefaultValueIfObjNull(occInsDetail.getNomineeRelation()));
		appPayload.setNomineeDob(
				CommonUtils.formatCustomDate(getDefaultValueIfObjNull(occInsDetail.getNomineeDob()), "dd/MM/yyyy"));
		appPayload.setAge(getDefaultValueIfObjNull(occInsDetail.getAge()));
		appPayload.setCoApplicantInsurance(getDefaultValueIfObjNull(occInsDetail.getCoApplicantInsurance()));
		String appPayloadStr = gson.toJson(appPayload);
		appInsuranceDetail.setPayloadColumn(appPayloadStr);
		insuranceDtlRepo.save(appInsuranceDetail);
		logger.warn("Data inserted into TB_CGOB_INSURANCE_DTLS for applicant loans");

		InsuranceDetails coappInsuranceDetail = new InsuranceDetails();
		BigDecimal coappInsuranceDtlId = CommonUtils.generateRandomNum();
		coappInsuranceDetail.setInsuranceDtlId(coappInsuranceDtlId);
		coappInsuranceDetail.setAppId(requestObj.getAppId());
		coappInsuranceDetail.setApplicationId(applicationID);
		coappInsuranceDetail.setCustDtlId(coAppCustDtlId);
		coappInsuranceDetail.setVersionNum(version);
		InsuranceDetailsPayload coappPayload = new InsuranceDetailsPayload();
		String coappPayloadInsuranceReqd = getDefaultValueIfObjNull(occInsDetail.getCoInsurancereqd());
		if ("yes".equalsIgnoreCase(coappPayloadInsuranceReqd)) {
			coappPayloadInsuranceReqd = "Y";
		} else if ("no".equalsIgnoreCase(coappPayloadInsuranceReqd)) {
			coappPayloadInsuranceReqd = "N";
		}
		coappPayload.setInsuranceReqd(coappPayloadInsuranceReqd);
		coappPayload.setInsuredName(getDefaultValueIfObjNull(occInsDetail.getCoInsuredname()));
		coappPayload.setNomineeName(getDefaultValueIfObjNull(occInsDetail.getCoNomineename()));
		coappPayload.setNomineeRelation(getDefaultValueIfObjNull(occInsDetail.getCoNomineerelation()));
		coappPayload.setNomineeDob(
				CommonUtils.formatCustomDate(getDefaultValueIfObjNull(occInsDetail.getCoNomineedob()), "dd/MM/yyyy"));
		coappPayload.setAge(getDefaultValueIfObjNull(occInsDetail.getCoAge()));
		coappPayload.setCoApplicantInsurance(getDefaultValueIfObjNull(occInsDetail.getCoCoapplicantinsurance()));
		String coappPayloadStr = gson.toJson(coappPayload);
		coappInsuranceDetail.setPayloadColumn(coappPayloadStr);
		insuranceDtlRepo.save(coappInsuranceDetail);
		logger.warn("Data inserted into TB_CGOB_INSURANCE_DTLS for coapplicant loans");

	}

	private void populateBankDtls(ApplyLoanRequestFields requestObj, CustomerIdentificationLoan customerIdentification,
								  String applicationID, BigDecimal custDtlId, int version) {
		Gson gson = new Gson();
		List<String> bankList = new ArrayList<>();
		List<BankDetailsWrapper> bankDetailsWrapperList = requestObj.getBankDetailsWrapperList();
		for (BankDetailsWrapper bankDetailsWrapper : bankDetailsWrapperList) {
			BankDetails bankDetails = bankDetailsWrapper.getBankDetails();

//			Optional<BankDetails> bankDetailsDb = bankDtlRepo.findByApplicationId(applicationID);
			Optional<BankDetails> bankDetailsDb = bankDtlRepo.findByApplicationIdAndCustDtlId(applicationID, custDtlId);

			if (bankDetailsDb.isPresent()) {
				bankDetails.setBankDtlId(bankDetailsDb.get().getBankDtlId());
				bankList.add(bankDetailsDb.get().getBankDtlId().toString());
			} else {
				BigDecimal bankDtlId = CommonUtils.generateRandomNum();
				bankDetails.setBankDtlId(bankDtlId);
				bankList.add(bankDtlId.toString());// to String is required to avoid rounding issue of Big
				// Decimal at front end.
			}
			bankDetails.setAppId(requestObj.getAppId());
			bankDetails.setApplicationId(applicationID);
			bankDetails.setCustDtlId(custDtlId);
			bankDetails.setVersionNum(version);
			String payload = gson.toJson(bankDetails.getPayload());
			bankDetails.setPayloadColumn(payload);
			bankDtlRepo.save(bankDetails);
		}
		customerIdentification.setBankList(bankList);
		customerIdentification.setCustDtlId(custDtlId.toString());// to String is required to avoid rounding issue of
		// Big Decimal at front end.
		customerIdentification.setApplicationId(applicationID);
		customerIdentification.setVersionNum(version);
		logger.warn("Data inserted into TB_CGOB_BANK_DTLS for loans");
	}

	private void populateAddressDtls(ApplyLoanRequestFields requestObj,
									 CustomerIdentificationLoan customerIdentification, String applicationID, BigDecimal custDtlId, int version,
									 String relatedScreen, String custType) {
		Gson gson = new Gson();
		ObjectMapper objectMapper = new ObjectMapper();
		List<String> addressList = new ArrayList<>();
		List<AddressDetailsWrapper> addressDetailsWrapperList = requestObj.getAddressDetailsWrapperList();
		for (AddressDetailsWrapper addressDetailsWrapper : addressDetailsWrapperList) {
			List<AddressDetails> addressDetailsList = addressDetailsWrapper.getAddressDetailsList();
			for (AddressDetails addressDetails : addressDetailsList) {
				logger.debug("addressDetails.getAddressType() : " + addressDetails.getAddressType().toString());
				Optional<AddressDetails> addressDetailsDb = addressDtlRepo
						.findAddressByCustomerTypeAndAddressTypeAndApplicationId(custType,
								addressDetails.getAddressType(), applicationID);

				if (addressDetailsDb.isPresent()) {
					logger.debug("inside present : " + addressDetailsDb.get().getAddressDtlsId());
					addressDetails.setAddressDtlsId(addressDetailsDb.get().getAddressDtlsId());
					addressList.add(addressDetailsDb.get().getAddressDtlsId().toString());
				} else {
					BigDecimal addressDtlId = CommonUtils.generateRandomNum();
					addressDetails.setAddressDtlsId(addressDtlId);
					addressList.add(addressDtlId.toString());

					if (Constants.CUSTOMER_DETAILS.equalsIgnoreCase(relatedScreen)) {
						addressDetails.setUniqueId(custDtlId);
					} else if (Constants.OCCUPATION_DETAILS.equalsIgnoreCase(relatedScreen)) {
						List<String> occupationList = customerIdentification.getOccupationList();
						for (String occptDtlId : occupationList) {
							addressDetails.setUniqueId(new BigDecimal(occptDtlId));
						}
					}
				}
				addressDetails.setAppId(requestObj.getAppId());
				addressDetails.setApplicationId(applicationID);
				addressDetails.setCustDtlId(custDtlId);
				addressDetails.setVersionNum(version);
				String payload = gson.toJson(addressDetails.getPayload());
				addressDetails.setPayloadColumn(payload);
				addressDtlRepo.save(addressDetails);
				try {
					boolean Updated = false;
					for (Address addrList : addressDetails.getPayload().getAddressList()) {
						logger.warn("addrList : " + addrList.toString());
						logger.warn("addrList.getAddressType() : " + addrList.getAddressType());
						if ("Office".equalsIgnoreCase(addrList.getAddressType())) {
							List<AddressDetails> custAddressDetailsList = addressDtlRepo
									.findByApplicationIdAndAppIdAndVersionNumAndCustDtlId(applicationID,
											requestObj.getAppId(), version, custDtlId);
							logger.warn("custAddressDetailsList.toString() : " + custAddressDetailsList.toString());
							for (AddressDetails addressDetail : custAddressDetailsList) {
								logger.warn("addressDetail.toString() : " + addressDetail.toString());
								AddressDetailsPayload addrPayload = objectMapper
										.readValue(addressDetail.getPayloadColumn(), AddressDetailsPayload.class);
								List<Address> updatedAddrList = new ArrayList<>();
								for (Address address : addrPayload.getAddressList()) {
									logger.warn("address.toString() : " + address.toString());
									logger.warn("updatedAddrList.toString() : " + updatedAddrList.toString());
									if ("business".equalsIgnoreCase(address.getAddressSameAs())
											&& "Communication".equalsIgnoreCase(address.getAddressType())) {
										Address UpdAddr = new Address();
										UpdAddr = addrList;
										logger.warn("UpdAddr.toString() 1 : " + UpdAddr.toString());
										UpdAddr.setAddressSameAs(address.getAddressSameAs());
										UpdAddr.setAddressType(address.getAddressType());
										logger.warn("UpdAddr.toString() 2 : " + UpdAddr.toString());
										updatedAddrList.add(UpdAddr);
										Updated = true;
									} else {
										updatedAddrList.add(address);
									}
								}
								if (Updated) {
									Updated = false;
									AddressDetailsPayload updtPayload = objectMapper
											.readValue(addressDetail.getPayloadColumn(), AddressDetailsPayload.class);
									logger.warn("inside Updated : " + Updated);
									updtPayload.setAddressList(updatedAddrList);
									addressDetail.setPayloadColumn(gson.toJson(updtPayload));
									logger.warn("addressDetail.toString() to be updated: " + addressDetail.toString());
									addressDtlRepo.save(addressDetail);
								}
							}
						}
					}
				} catch (Exception e) {
					logger.error("Error while populating address details: ", e);
				}

			}
			customerIdentification.setAddressList(addressList);
			customerIdentification.setCustDtlId(custDtlId.toString());// to String is required to avoid rounding issue
			// of Big Decimal at front end.
			customerIdentification.setApplicationId(applicationID);
			customerIdentification.setVersionNum(version);
		}
		logger.warn("Data inserted into TB_ABOB_ADDRESS_DETAILS for loans");
	}

	private void populateRenewalAddressDtls(RenewalLeadDetails renewalLeadDetail,
											RenewalLeadOccpInsDetails occInsDetail, ApplyLoanRequestFields requestObj,
											CustomerIdentificationLoan customerIdentification, String applicationID, BigDecimal custDtlId,
											BigDecimal coAppCustDtlId, int version, String relatedScreen) {
		Gson gson = new Gson();
		logger.debug("Entered populateRenewalAddressDtls method with parameters: renewalLeadDetail={}, occInsDetail={}, requestObj={}, customerIdentification={}, applicationID={}, custDtlId={}, coAppCustDtlId={}, version={}, relatedScreen={}",
				renewalLeadDetail, occInsDetail, requestObj, customerIdentification, applicationID, custDtlId, coAppCustDtlId, version, relatedScreen);
		AddressDetails applicantAddressDetail = new AddressDetails();
		BigDecimal applicantAddressDtlId = CommonUtils.generateRandomNum();
		applicantAddressDetail.setAddressDtlsId(applicantAddressDtlId);
		applicantAddressDetail.setAppId(requestObj.getAppId());
		applicantAddressDetail.setApplicationId(applicationID);
		applicantAddressDetail.setCustDtlId(custDtlId);
		applicantAddressDetail.setVersionNum(version);
		applicantAddressDetail.setAddressType(Constants.PERSONAL);
		List<Address> applicantAddressList = new ArrayList<Address>();
		Address presentAddress = new Address();
		Address permanentAddress = new Address();
		Address communicationAddress = new Address();

		presentAddress.setAddressType(Constants.PRESENT);
		presentAddress.setAddressLine1(getDefaultValueIfObjNull(renewalLeadDetail.getPresLine1()));
		presentAddress.setAddressLine2(getDefaultValueIfObjNull(renewalLeadDetail.getPresLine2()));
		presentAddress.setAddressLine3(getDefaultValueIfObjNull(renewalLeadDetail.getPresLine3()));
		presentAddress.setDistrict(getDefaultValueIfObjNull(renewalLeadDetail.getPresDist()));
		presentAddress.setCity(getDefaultValueIfObjNull(renewalLeadDetail.getPresCityTownVillage()));
		presentAddress.setState(getDefaultValueIfObjNull(renewalLeadDetail.getPresState()));
		presentAddress.setCountry(getDefaultValueIfObjNull(renewalLeadDetail.getPresCountry()));
		presentAddress.setPinCode(getDefaultValueIfObjNull(renewalLeadDetail.getPresPincode()));
		presentAddress.setLandMark(getDefaultValueIfObjNull(renewalLeadDetail.getPrestAddreLandmark()));
		presentAddress.setArea(getDefaultValueIfObjNull(renewalLeadDetail.getPresArea()));
		presentAddress
				.setCurrentAddressProof(getDefaultValueIfObjNull(renewalLeadDetail.getPrestAddreCurrentaddressproof()));
		presentAddress.setHouseType(getDefaultValueIfObjNull(renewalLeadDetail.getPrestAddreHousetype()));
		presentAddress.setLocateCoOrdinates(getDefaultValueIfObjNull(renewalLeadDetail.getPresLocationCoords()));
		presentAddress.setLocateCoOrdinatesFor("");
		presentAddress
				.setResidenceOwnership(getDefaultValueIfObjNull(renewalLeadDetail.getPrestAddreResidentownership()));
		presentAddress.setResidenceAddressSince(
				getDefaultValueIfObjNull(renewalLeadDetail.getPrestAddreResidenceaddresssince()));
		presentAddress
				.setResidenceCitySince(getDefaultValueIfObjNull(renewalLeadDetail.getPrestAddreResidencecitysince()));

		permanentAddress.setAddressType(Constants.PERMANENT);
		permanentAddress.setAddressSameAs("");
		permanentAddress.setAddressLine1(getDefaultValueIfObjNull(renewalLeadDetail.getPermLine1()));
		permanentAddress.setAddressLine2(getDefaultValueIfObjNull(renewalLeadDetail.getPermLine2()));
		permanentAddress.setAddressLine3(getDefaultValueIfObjNull(renewalLeadDetail.getPermLine3()));
		permanentAddress.setDistrict(getDefaultValueIfObjNull(renewalLeadDetail.getPermDist()));
		permanentAddress.setCity(getDefaultValueIfObjNull(renewalLeadDetail.getPermCityTownVillage()));
		permanentAddress.setState(getDefaultValueIfObjNull(renewalLeadDetail.getPermState()));
		permanentAddress.setCountry(getDefaultValueIfObjNull(renewalLeadDetail.getPermCountry()));
		permanentAddress.setPinCode(getDefaultValueIfObjNull(renewalLeadDetail.getPermPincode()));
		permanentAddress.setLandMark(getDefaultValueIfObjNull(renewalLeadDetail.getPermtAddreLandmark()));
		permanentAddress.setArea(getDefaultValueIfObjNull(renewalLeadDetail.getPermArea()));
		permanentAddress
				.setCurrentAddressProof(getDefaultValueIfObjNull(renewalLeadDetail.getPermtAddreCurrentaddressproof()));
		permanentAddress.setHouseType(getDefaultValueIfObjNull(renewalLeadDetail.getPermtAddreHousetype()));
		permanentAddress.setLocateCoOrdinates(getDefaultValueIfObjNull(renewalLeadDetail.getPermLocationCoords()));
		permanentAddress.setLocateCoOrdinatesFor("");
		permanentAddress
				.setResidenceOwnership(getDefaultValueIfObjNull(renewalLeadDetail.getPermtAddreResidentownership()));
		permanentAddress.setResidenceAddressSince(
				getDefaultValueIfObjNull(renewalLeadDetail.getPermtAddreResidenceaddresssince()));
		permanentAddress
				.setResidenceCitySince(getDefaultValueIfObjNull(renewalLeadDetail.getPermtAddreResidencecitysince()));

		communicationAddress.setAddressType(Constants.COMMUNICATION);
		communicationAddress.setAddressSameAs(getDefaultValueIfObjNull(occInsDetail.getAddressSameAs()));
		communicationAddress.setAddressLine1(getDefaultValueIfObjNull(occInsDetail.getAddressLine1()));
		communicationAddress.setAddressLine2(getDefaultValueIfObjNull(occInsDetail.getAddressLine2()));
		communicationAddress.setAddressLine3(getDefaultValueIfObjNull(occInsDetail.getAddressLine3()));
		communicationAddress.setDistrict(getDefaultValueIfObjNull(occInsDetail.getDistrict()));
		communicationAddress.setCity(getDefaultValueIfObjNull(occInsDetail.getCity()));
		communicationAddress.setState(getDefaultValueIfObjNull(occInsDetail.getState()));
		communicationAddress.setCountry(getDefaultValueIfObjNull(occInsDetail.getCountry()));
		communicationAddress.setPinCode(getDefaultValueIfObjNull(occInsDetail.getPinCode()));
		communicationAddress.setLandMark(getDefaultValueIfObjNull(occInsDetail.getLandMark()));
		communicationAddress.setArea(getDefaultValueIfObjNull(occInsDetail.getArea()));
		communicationAddress.setCurrentAddressProof("");
		communicationAddress.setHouseType("");
		communicationAddress.setLocateCoOrdinates("");
		communicationAddress.setLocateCoOrdinatesFor("");
		communicationAddress.setResidenceOwnership(getDefaultValueIfObjNull(occInsDetail.getResidenceOwnership()));
		communicationAddress
				.setResidenceAddressSince(getDefaultValueIfObjNull(occInsDetail.getResidenceAddressSince()));
		communicationAddress.setResidenceCitySince(getDefaultValueIfObjNull(occInsDetail.getResidenceCitySince()));

		applicantAddressList.add(presentAddress);
		applicantAddressList.add(permanentAddress);
		applicantAddressList.add(communicationAddress);
		AddressDetailsPayload payloadObj = new AddressDetailsPayload();
		payloadObj.setAddressList(applicantAddressList);

		String applicantAddrPayload = gson.toJson(payloadObj);
		applicantAddressDetail.setPayloadColumn(applicantAddrPayload);
		// addressDtlRepo.save(applicantAddressDetail);
		logger.warn("Data inserted into TB_ABOB_ADDRESS_DETAILS for applicant renewal loans: {}",
				applicantAddressDetail.toString());

		AddressDetails coApplicantAddressDetail = new AddressDetails();
		BigDecimal coAppAddressDtlId = CommonUtils.generateRandomNum();
		coApplicantAddressDetail.setAddressDtlsId(coAppAddressDtlId);
		coApplicantAddressDetail.setAppId(requestObj.getAppId());
		coApplicantAddressDetail.setApplicationId(applicationID);
		coApplicantAddressDetail.setCustDtlId(coAppCustDtlId);
		coApplicantAddressDetail.setVersionNum(version);
		coApplicantAddressDetail.setAddressType(Constants.PERSONAL);
		List<Address> coApplicantAddressList = new ArrayList<Address>();
		Address coPresentAddress = new Address();
		Address coPermanentAddress = new Address();
		Address coCommunicationAddress = new Address();

		coPresentAddress.setAddressType(Constants.PRESENT);
		coPresentAddress.setAddressLine1(getDefaultValueIfObjNull(renewalLeadDetail.getCoPresentLine1()));
		coPresentAddress.setAddressLine2(getDefaultValueIfObjNull(renewalLeadDetail.getCoPresentLine2()));
		coPresentAddress.setAddressLine3(getDefaultValueIfObjNull(renewalLeadDetail.getCoPresentLine3()));
		coPresentAddress.setDistrict(getDefaultValueIfObjNull(renewalLeadDetail.getCoPresentDistrict()));
		coPresentAddress.setCity(getDefaultValueIfObjNull(renewalLeadDetail.getCoPresentCityTownVillage()));
		coPresentAddress.setState(getDefaultValueIfObjNull(renewalLeadDetail.getCoPresentState()));
		coPresentAddress.setCountry(getDefaultValueIfObjNull(renewalLeadDetail.getCoPresentCountry()));
		coPresentAddress.setPinCode(getDefaultValueIfObjNull(renewalLeadDetail.getCoPresentPincode()));
		coPresentAddress.setLandMark(getDefaultValueIfObjNull(renewalLeadDetail.getCoPrestAddreslandmark()));
		coPresentAddress.setArea(getDefaultValueIfObjNull(renewalLeadDetail.getCoPresentArea()));
		coPresentAddress.setCurrentAddressProof(
				getDefaultValueIfObjNull(renewalLeadDetail.getCoPrestAddrescurrentaddressproof()));
		coPresentAddress.setHouseType(getDefaultValueIfObjNull(renewalLeadDetail.getCoPrestAddreshousetype()));
		coPresentAddress
				.setLocateCoOrdinates(getDefaultValueIfObjNull(renewalLeadDetail.getCoPresentLocationCoOrdinates()));
		coPresentAddress.setLocateCoOrdinatesFor(renewalLeadDetail.getCoLocationCoordinatesFor());
		coPresentAddress
				.setResidenceOwnership(getDefaultValueIfObjNull(renewalLeadDetail.getCoPrestAddresresidentownership()));
		coPresentAddress.setResidenceAddressSince(
				getDefaultValueIfObjNull(renewalLeadDetail.getCoPrestAddresresidenceaddresssince()));
		coPresentAddress.setResidenceCitySince(
				getDefaultValueIfObjNull(renewalLeadDetail.getCoPrestAddresresidencecitysince()));

		coPermanentAddress.setAddressType(Constants.PERMANENT);
		coPermanentAddress.setAddressSameAs(renewalLeadDetail.getCoPermanentSameAs());
		coPermanentAddress.setAddressLine1(getDefaultValueIfObjNull(renewalLeadDetail.getCoPermanentLine1()));
		coPermanentAddress.setAddressLine2(getDefaultValueIfObjNull(renewalLeadDetail.getCoPermanentLine2()));
		coPermanentAddress.setAddressLine3(getDefaultValueIfObjNull(renewalLeadDetail.getCoPermanentLine3()));
		coPermanentAddress.setDistrict(getDefaultValueIfObjNull(renewalLeadDetail.getCoPermanentDistrict()));
		coPermanentAddress.setCity(getDefaultValueIfObjNull(renewalLeadDetail.getCoPermanentCityTownVillage()));
		coPermanentAddress.setState(getDefaultValueIfObjNull(renewalLeadDetail.getCoPermanentState()));
		coPermanentAddress.setCountry(getDefaultValueIfObjNull(renewalLeadDetail.getCoPermanentCountry()));
		coPermanentAddress.setPinCode(getDefaultValueIfObjNull(renewalLeadDetail.getCoPermanentPincode()));
		coPermanentAddress.setLandMark(getDefaultValueIfObjNull(renewalLeadDetail.getCoPermtAddreslandmark()));
		coPermanentAddress.setArea(getDefaultValueIfObjNull(renewalLeadDetail.getCoPermanentArea()));
		coPermanentAddress.setCurrentAddressProof(
				getDefaultValueIfObjNull(renewalLeadDetail.getCoPermtAddrescurrentaddressproof()));
		coPermanentAddress.setHouseType(getDefaultValueIfObjNull(renewalLeadDetail.getCoPermtAddreshousetype()));
		coPermanentAddress
				.setLocateCoOrdinates(getDefaultValueIfObjNull(renewalLeadDetail.getCoPermanentLocationCoOrdinates()));
		coPermanentAddress.setLocateCoOrdinatesFor("");
		coPermanentAddress
				.setResidenceOwnership(getDefaultValueIfObjNull(renewalLeadDetail.getCoPermtAddresresidentownership()));
		coPermanentAddress.setResidenceAddressSince(
				getDefaultValueIfObjNull(renewalLeadDetail.getCoPermtAddresresidenceaddresssince()));
		coPermanentAddress.setResidenceCitySince(
				getDefaultValueIfObjNull(renewalLeadDetail.getCoPermtAddresresidencecitysince()));

		coCommunicationAddress.setAddressType(Constants.COMMUNICATION);
		coCommunicationAddress.setAddressSameAs(getDefaultValueIfObjNull(occInsDetail.getCoCommunicationAddressSameAs()));
		coCommunicationAddress.setAddressLine1(getDefaultValueIfObjNull(occInsDetail.getCoAddressline1()));
		coCommunicationAddress.setAddressLine2(getDefaultValueIfObjNull(occInsDetail.getCoAddressline2()));
		coCommunicationAddress.setAddressLine3(getDefaultValueIfObjNull(occInsDetail.getCoAddressline3()));
		coCommunicationAddress.setDistrict(getDefaultValueIfObjNull(occInsDetail.getCoDistrict()));
		coCommunicationAddress.setCity(getDefaultValueIfObjNull(occInsDetail.getCoCity()));
		coCommunicationAddress.setState(getDefaultValueIfObjNull(occInsDetail.getCoState()));
		coCommunicationAddress.setCountry(getDefaultValueIfObjNull(occInsDetail.getCoCountry()));
		coCommunicationAddress.setPinCode(getDefaultValueIfObjNull(occInsDetail.getCoPincode()));
		coCommunicationAddress.setLandMark(getDefaultValueIfObjNull(occInsDetail.getCoLandmark()));
		coCommunicationAddress.setArea(getDefaultValueIfObjNull(occInsDetail.getCoArea()));
		coCommunicationAddress.setCurrentAddressProof("");
		coCommunicationAddress.setHouseType("");
		coCommunicationAddress.setLocateCoOrdinates("");
		coCommunicationAddress.setLocateCoOrdinatesFor("");
		coCommunicationAddress.setResidenceOwnership(getDefaultValueIfObjNull(occInsDetail.getCoResidenceownership()));
		coCommunicationAddress
				.setResidenceAddressSince(getDefaultValueIfObjNull(occInsDetail.getCoResidenceaddresssince()));
		coCommunicationAddress.setResidenceCitySince(getDefaultValueIfObjNull(occInsDetail.getCoResidencecitysince()));

		coApplicantAddressList.add(coPresentAddress);
		coApplicantAddressList.add(coPermanentAddress);
		coApplicantAddressList.add(coCommunicationAddress);
		AddressDetailsPayload coAppPayloadObj = new AddressDetailsPayload();
		coAppPayloadObj.setAddressList(coApplicantAddressList);

		String coApplicantAddrPayload = gson.toJson(coAppPayloadObj);
		coApplicantAddressDetail.setPayloadColumn(coApplicantAddrPayload);
		addressDtlRepo.save(coApplicantAddressDetail);
		logger.warn("Data inserted into TB_ABOB_ADDRESS_DETAILS for co-applicant renewal loans: {}",
				coApplicantAddressDetail.toString());

		/*----------------------------- populating occupation address details -----------------------------*/
		AddressDetails applicantOccuAddressDetail = new AddressDetails();
		BigDecimal applicantOccuAddressDtlId = CommonUtils.generateRandomNum();
		applicantOccuAddressDetail.setAddressDtlsId(applicantOccuAddressDtlId);
		applicantOccuAddressDetail.setAppId(requestObj.getAppId());
		applicantOccuAddressDetail.setApplicationId(applicationID);
		applicantOccuAddressDetail.setCustDtlId(custDtlId);
		applicantOccuAddressDetail.setVersionNum(version);
		applicantOccuAddressDetail.setAddressType(Constants.OCCUPATION);
		List<Address> applicantOccuAddressList = new ArrayList<Address>();
		// Address occuPresentAddress = new Address();
		Address occuOfficeAddress = new Address();
		// Address communicationAddress = new Address();

		occuOfficeAddress.setAddressType(Constants.OFFICE);
		occuOfficeAddress.setAddressSameAs(getDefaultValueIfObjNull(occInsDetail.getOffAddressSameAs()));
		occuOfficeAddress.setAddressLine1(getDefaultValueIfObjNull(occInsDetail.getOffAddressLine1()));
		occuOfficeAddress.setAddressLine2(getDefaultValueIfObjNull(occInsDetail.getOffAddressLine2()));
		occuOfficeAddress.setAddressLine3(getDefaultValueIfObjNull(occInsDetail.getOffAddressLine3()));
		occuOfficeAddress.setDistrict(getDefaultValueIfObjNull(occInsDetail.getOffDistrict()));
		occuOfficeAddress.setCity(getDefaultValueIfObjNull(occInsDetail.getOffCity()));
		occuOfficeAddress.setState(getDefaultValueIfObjNull(occInsDetail.getOffState()));
		occuOfficeAddress.setCountry(getDefaultValueIfObjNull(occInsDetail.getOffCountry()));
		occuOfficeAddress.setPinCode(getDefaultValueIfObjNull(occInsDetail.getOffPinCode()));
		occuOfficeAddress.setLandMark(getDefaultValueIfObjNull(occInsDetail.getOffLandMark()));
		occuOfficeAddress.setArea(getDefaultValueIfObjNull(occInsDetail.getOffArea()));

		applicantOccuAddressList.add(occuOfficeAddress);
		AddressDetailsPayload appOccuPayloadObj = new AddressDetailsPayload();
		appOccuPayloadObj.setAddressList(applicantOccuAddressList);

		String applicantOccuAddrPayload = gson.toJson(appOccuPayloadObj);
		applicantOccuAddressDetail.setPayloadColumn(applicantOccuAddrPayload);
		addressDtlRepo.save(applicantOccuAddressDetail);
		logger.warn("Data inserted into TB_ABOB_ADDRESS_DETAILS for applicant Occupation renewal loans: {}",
				applicantOccuAddressDetail.toString());

		AddressDetails coApplicantOccuAddressDetail = new AddressDetails();
		BigDecimal coApplicantOccuAddressDtlId = CommonUtils.generateRandomNum();
		coApplicantOccuAddressDetail.setAddressDtlsId(coApplicantOccuAddressDtlId);
		coApplicantOccuAddressDetail.setAppId(requestObj.getAppId());
		coApplicantOccuAddressDetail.setApplicationId(applicationID);
		coApplicantOccuAddressDetail.setCustDtlId(coAppCustDtlId);
		coApplicantOccuAddressDetail.setVersionNum(version);
		coApplicantOccuAddressDetail.setAddressType(Constants.OCCUPATION);
		List<Address> coApplicantOccuAddressList = new ArrayList<Address>();
		// Address coOccuPresentAddress = new Address();
		Address coOccuOfficeAddress = new Address();

		coOccuOfficeAddress.setAddressType(Constants.OFFICE);
		coOccuOfficeAddress.setAddressSameAs(getDefaultValueIfObjNull(occInsDetail.getCoOffAddresssameas()));
		coOccuOfficeAddress.setAddressLine1(getDefaultValueIfObjNull(occInsDetail.getCoOffAddressline1()));
		coOccuOfficeAddress.setAddressLine2(getDefaultValueIfObjNull(occInsDetail.getCoOffAddressline2()));
		coOccuOfficeAddress.setAddressLine3(getDefaultValueIfObjNull(occInsDetail.getCoOffAddressline3()));
		coOccuOfficeAddress.setDistrict(getDefaultValueIfObjNull(occInsDetail.getCoOffDistrict()));
		coOccuOfficeAddress.setCity(getDefaultValueIfObjNull(occInsDetail.getCoOffCity()));
		coOccuOfficeAddress.setState(getDefaultValueIfObjNull(occInsDetail.getCoOffState()));
		coOccuOfficeAddress.setCountry(getDefaultValueIfObjNull(occInsDetail.getCoOffCountry()));
		coOccuOfficeAddress.setPinCode(getDefaultValueIfObjNull(occInsDetail.getCoOffPincode()));
		coOccuOfficeAddress.setLandMark(getDefaultValueIfObjNull(occInsDetail.getCoOffLandmark()));
		coOccuOfficeAddress.setArea(getDefaultValueIfObjNull(occInsDetail.getCoOffArea()));
		coApplicantOccuAddressList.add(coOccuOfficeAddress);
		AddressDetailsPayload coAppOccuPayloadObj = new AddressDetailsPayload();
		coAppOccuPayloadObj.setAddressList(coApplicantOccuAddressList);

		String coApplicantOccuAddrPayload = gson.toJson(coAppOccuPayloadObj);
		coApplicantOccuAddressDetail.setPayloadColumn(coApplicantOccuAddrPayload);
		addressDtlRepo.save(coApplicantOccuAddressDetail);
		logger.warn("Data inserted into TB_ABOB_ADDRESS_DETAILS for co-applicant Occupation renewal loans: {}",
				coApplicantOccuAddressDetail.toString());

	}

	private void populateExistingLoanDtls(ApplyLoanRequestFields requestObj,
										  CustomerIdentificationLoan customerIdentification, String applicationID, BigDecimal custDtlId, int version,
										  String relatedScreen) {
		Gson gson = new Gson();
		List<String> existingLoanList = new ArrayList<>();
		List<String> loanList = new ArrayList<>();
		List<ExistingLoanDetailsWrapper> existingLoanDetailsWrapperList = requestObj
				.getExistingLoanDetailsWrapperList();
		for (ExistingLoanDetailsWrapper existingLoanDetailsWrapper : existingLoanDetailsWrapperList) {
			List<ExistingLoanDetails> existingLoanDetailsList = existingLoanDetailsWrapper.getExistingLoanDetailsList();
			for (ExistingLoanDetails existingLoanDetails : existingLoanDetailsList) {
				if (existingLoanDetails.getLoanDtlsId() == null && existingLoanDetails.getExistingLoanId() == null) {// This
					// data
					// after its being inserted by using the back
					// navigation
					// within the session.
					BigDecimal loanDtlId = CommonUtils.generateRandomNum();
					existingLoanDetails.setLoanDtlsId(loanDtlId);
					loanList.add(loanDtlId.toString());// to String is required to avoid rounding issue of Big
					// Decimal at front end.

					BigDecimal existingLoanId = CommonUtils.generateRandomNum();
					existingLoanDetails.setExistingLoanId(existingLoanId);
					existingLoanList.add(existingLoanId.toString());

				} else {
					loanList.add(existingLoanDetails.getLoanDtlsId().toString());
					existingLoanList.add(existingLoanDetails.getExistingLoanId().toString());
				}
				existingLoanDetails.setAppId(requestObj.getAppId());
				existingLoanDetails.setApplicationId(applicationID);
				existingLoanDetails.setCustDtlId(custDtlId);
				existingLoanDetails.setVersionNum(version);
				String payload = gson.toJson(existingLoanDetails.getPayload());
				existingLoanDetails.setPayloadColumn(payload);
				existingLoanDtlRepo.save(existingLoanDetails);
			}
			customerIdentification.setCustDtlId(custDtlId.toString());// to String is required to avoid rounding issue
			// of Big Decimal at front end.
			customerIdentification.setApplicationId(applicationID);
			customerIdentification.setExistisingLoanDtlId(existingLoanList);
			customerIdentification.setLoanDtlIds(loanList);
			customerIdentification.setVersionNum(version);
		}
		logger.warn("Data inserted into TB_ABOB_ADDRESS_DETAILS for loans");
	}
	private void populateCustomerDtlsForDisabled(ApplyLoanRequestFields requestObj,String applicationID) {
		Gson gson = new Gson();
		String payload;

		List<CustomerDetails> customerDetailsList = requestObj.getCustomerDetailsList();
		List<CustomerDetails> customerDtl=custDtlRepo.findByApplicationId(applicationID);
		if(!customerDtl.isEmpty()) {
			for(CustomerDetails cust: customerDetailsList) {
				for(CustomerDetails custfind : customerDtl) {
					if(cust.getCustDtlId().equals(custfind.getCustDtlId())) {
						payload=gson.toJson(cust.getPayload());
						custfind.setPayloadColumn(payload);
						custDtlRepo.save(custfind);
					}
				}
			}
		}}

	private void populateCustomerDtls(ApplyLoanRequestFields requestObj,
									  CustomerIdentificationLoan customerIdentification, String applicationID, BigDecimal custDtlId,
									  int version) {
		Gson gson = new Gson();
		ObjectMapper objectMapper = new ObjectMapper();
		boolean cbRetrigger = false;
		String payload;
		List<CustomerDetails> customerDetailsList = requestObj.getCustomerDetailsList();
		for (CustomerDetails customerDetails : customerDetailsList) {
			customerDetails.setApplicationId(applicationID);
			customerDetails.setAppId(requestObj.getAppId());
			customerDetails.setVersionNum(version);
			payload = gson.toJson(customerDetails.getPayload());
			logger.debug(
					"customerDetails.getPayload().isRpcEditFlag() : " + customerDetails.getPayload().isRpcEditFlag());
			if (customerDetails.getPayload().isRpcEditFlag()) {
				cbRetrigger = true;
			}
			if(StringUtils.isBlank(customerDetails.getPayload().getPanNumber())){
				Optional<List<ApplicationDocuments>> panDocOpt = appLoanDocsRepository.
						findByApplicationIdAndCustTypeAndDocType(customerDetails.getCustomerType(), applicationID, Constants.PAN_NUMBER_DOC_TYPE);
				if(panDocOpt.isPresent() && !panDocOpt.get().isEmpty()) {
					appLoanDocsRepository.deleteAll(panDocOpt.get());
				}
			}
			customerDetails.setPayloadColumn(payload);
			customerDetails.setCustDtlId(custDtlId);
			customerDetails.setSeqNumber(requestObj.getApplicationMaster().getCustDtlSlNum());
			custDtlRepo.save(customerDetails);
		}
		try {
			if (cbRetrigger) {
				List<CibilDetails> cibilDtlList = cibilDtlRepo.findByApplicationIdAndAppId(applicationID,
						Constants.APPID);
				for (CibilDetails cibilDtl : cibilDtlList) {
					CibilDetailsPayload cibilDetailsPayload = objectMapper.readValue(cibilDtl.getPayloadColumn(),
							CibilDetailsPayload.class);
					cibilDetailsPayload.setCbRetrigger(cbRetrigger);
					cibilDtl.setPayloadColumn(gson.toJson(cibilDetailsPayload));
					cibilDtlRepo.save(cibilDtl);
				}
			}
		} catch (Exception e) {
			logger.error("Exception while updating CIBIL details", e);
		}
		customerIdentification.setCustDtlId(custDtlId.toString());// to String is required to avoid rounding issue of
		// Big Decimal at front end.
		customerIdentification.setApplicationId(applicationID);
		customerIdentification.setVersionNum(version);
		logger.warn("Data inserted into TB_ABOB_CUSTOMER_DETAILS for loans");
	}

	private BigDecimal populateRenewalCustomerDtls(RenewalLeadDetails renewalLeadDetail,
												   RenewalLeadOccpInsDetails occInsDetail, ApplyLoanRequestFields requestObj,
												   CustomerIdentificationLoan customerIdentification, String applicationID, BigDecimal custDtlId,
												   int version) {
		logger.debug("inside populateRenewalCustomerDtls  ");
		Gson gson = new Gson();
		String payload;
		CustomerDetails customerDetail = new CustomerDetails();
		BigDecimal coAppCustDtlId = CommonUtils.generateRandomNum();
		customerDetail.setCustDtlId(coAppCustDtlId);
		customerDetail.setAppId(requestObj.getAppId());
		customerDetail.setApplicationId(applicationID);
		customerDetail.setVersionNum(version);
		customerDetail.setCustomerType(Constants.COAPPLICANT);
		customerDetail.setCustomerName(getDefaultValueIfObjNull(renewalLeadDetail.getCoFullName()));
		customerDetail.setMobileNumber(getDefaultValueIfObjNull(renewalLeadDetail.getCoMobileNo()));
		customerDetail.setSeqNumber(2);
		customerDetail.setKycStatus("Pending");
		customerDetail.setAmlStatus("Pending");
		logger.debug("inside populateRenewalCustomerDtls customerDetail 1 " + customerDetail.toString());
		CustomerDetailsPayload payloadObj = new CustomerDetailsPayload();

		payloadObj.setTitle(getDefaultValueIfObjNull(renewalLeadDetail.getCoTitle()));
		payloadObj.setDob(getDefaultValueIfObjNull(renewalLeadDetail.getCoDateOfBirth()));
		payloadObj.setAge(getDefaultValueIfObjNull(renewalLeadDetail.getCoAge()));
		payloadObj.setGender(getDefaultValueIfObjNull(renewalLeadDetail.getCoGender()));
		payloadObj.setMaritalStatus(getDefaultValueIfObjNull(renewalLeadDetail.getCoMaritalStatus()));
		// payloadObj.setPan(getDefaultValueIfObjNull(renewalLeadDetail.getCopan()));
		payloadObj.setSpouseName(getDefaultValueIfObjNull(renewalLeadDetail.getCoSpouseName()));
		payloadObj.setFathersName(getDefaultValueIfObjNull(renewalLeadDetail.getCoFatherName()));
		// payloadObj.setAadhaarNumber(getDefaultValueIfObjNull(renewalLeadDetail.getCoA));
		payloadObj.setOccupation(getDefaultValueIfObjNull(renewalLeadDetail.getCoOccupation()));
		payloadObj.setCustId("");
		payloadObj.setFirstName(getDefaultValueIfObjNull(renewalLeadDetail.getCoFirstname()));
		payloadObj.setMiddleName(getDefaultValueIfObjNull(renewalLeadDetail.getCoMiddlename()));
		payloadObj.setLastName(getDefaultValueIfObjNull(renewalLeadDetail.getCoLastname()));
		payloadObj.setRelationShipWithApplicant(
				getDefaultValueIfObjNull(renewalLeadDetail.getCoRelationshipwithapplicant()));
		payloadObj.setNamePerKyc(getDefaultValueIfObjNull(renewalLeadDetail.getCoNameperkyc()));
		payloadObj.setReligion(getDefaultValueIfObjNull(renewalLeadDetail.getCoReligion()));
		payloadObj.setCaste(getDefaultValueIfObjNull(renewalLeadDetail.getCoCaste()));
		payloadObj.setPrimaryKycType(getDefaultValueIfObjNull(Constants.VOTER_ID));
		payloadObj.setPrimaryKycId(getDefaultValueIfObjNull(renewalLeadDetail.getCoVoterIdNo()));
		payloadObj.setPrimaryKycIdValStatus("");
		payloadObj.setSecondaryKycType("");
		payloadObj.setSecondaryKycId(getDefaultValueIfObjNull(""));
		payloadObj.setSecondaryKycIdValStatus("");
		payloadObj.setGkCustomerType("Co-Applicant");
		payloadObj.setEducation(getDefaultValueIfObjNull(renewalLeadDetail.getCoEducation()));
		payloadObj.setAlternateVoterId("");
		payloadObj.setAlternateVoterIdValStatus("");
		payloadObj.setCustomerIndex(2);

		payload = gson.toJson(payloadObj);
		logger.debug("inside populateRenewalCustomerDtls payload " + payload.toString());
		customerDetail.setPayloadColumn(payload);
		logger.debug("inside populateRenewalCustomerDtls customerDetail 2 " + customerDetail.toString());
		custDtlRepo.save(customerDetail);
		// customerIdentification.setApplicationId(applicationID);
		// customerIdentification.setVersionNum(version);
		logger.warn("Data inserted into TB_ABOB_CUSTOMER_DETAILS for Renewal Applicant");

		return coAppCustDtlId;
	}

	private void updateConfirmFlagInMaster(ApplyLoanRequestFields requestObj, int version, String applicationID,
										   CustomerIdentificationLoan customerIdentification, Properties prop, boolean isSelfOnBoardingAppId,
										   boolean isSelfOnBoardingHeaderAppId) {
		ApplicationMaster masterRequest = requestObj.getApplicationMaster();
		Optional<ApplicationMaster> masterObjDb = applicationMasterRepo
				.findByAppIdAndApplicationIdAndVersionNumAndApplicationStatus(requestObj.getAppId(),
						requestObj.getApplicationId(), version, AppStatus.INPROGRESS.getValue());
		if (masterObjDb.isPresent()) {
			WorkFlowDetails wfObj = requestObj.getWorkflow();
			PopulateapplnWFRequest apiRequest = new PopulateapplnWFRequest();
			PopulateapplnWFRequestFields requestObjWf = new PopulateapplnWFRequestFields();
			requestObjWf.setAppId(requestObj.getAppId());
			requestObjWf.setApplicationId(applicationID);
			requestObjWf.setVersionNum(version);
			requestObjWf.setWorkflow(wfObj);
			apiRequest.setRequestObj(requestObjWf);
			if (isSelfOnBoardingAppId) { // self onboarding
				updateConfirmFlagInMasterForSob(apiRequest, requestObjWf, isSelfOnBoardingHeaderAppId, prop,
						masterRequest);
			} else { // assisted on boarding
				requestObjWf.setApplicationStatus(AppStatus.PENDING.getValue());
				requestObjWf.setCreatedBy(masterRequest.getCreatedBy());
				String roleId = commonParamService.fetchRoleId(requestObj.getAppId(), masterRequest.getCreatedBy());
				if (wfObj != null) {
					if (wfObj.getCurrentRole().equalsIgnoreCase(roleId)) { // VAPT
						commonParamService.populateApplnWorkFlow(apiRequest);
					} else {
						logger.error("VAPT issue in updateAppMaster. Current role id from request is tampered.");
					}
				}
			}
			customerIdentification.setApplicationId(applicationID);
			customerIdentification.setVersionNum(version);
		}
	}

	private void updateConfirmFlagInMasterForSob(PopulateapplnWFRequest apiRequest,
												 PopulateapplnWFRequestFields requestObjWf, boolean isSelfOnBoardingHeaderAppId, Properties prop,
												 ApplicationMaster masterRequest) {
		if ("N".equalsIgnoreCase(prop.getProperty(CobFlagsProperties.LOAN_STP.getKey()))) {
			if (!isSelfOnBoardingHeaderAppId) { // INITIATOR submits it after review.
				requestObjWf.setApplicationStatus(AppStatus.PENDING.getValue());
				requestObjWf.setCreatedBy(masterRequest.getCreatedBy());
			} else {
				requestObjWf.setCreatedBy("Customer");
				requestObjWf.setApplicationStatus(AppStatus.INPROGRESS.getValue());
			}
			commonParamService.populateApplnWorkFlow(apiRequest);
		}

	}

	private void updateDeclarationFlagInMaster(ApplyLoanRequestFields requestObj, int version, String applicationID,
											   CustomerIdentificationLoan customerIdentification) {
		ApplicationMaster masterRequest = requestObj.getApplicationMaster();
		Optional<ApplicationMaster> masterObjDb = applicationMasterRepo
				.findByAppIdAndApplicationIdAndVersionNumAndApplicationStatus(requestObj.getAppId(),
						requestObj.getApplicationId(), version, AppStatus.INPROGRESS.getValue());
		if (masterObjDb.isPresent()) {
			ApplicationMaster masterObj = masterObjDb.get();
//			masterObj.setDeclarationFlag(masterRequest.getDeclarationFlag());
			applicationMasterRepo.save(masterObj);
			customerIdentification.setApplicationId(applicationID);
			customerIdentification.setVersionNum(version);
		}
	}

	private void populateApplicationDocs(ApplyLoanRequestFields requestObj,
										 CustomerIdentificationLoan customerIdentification, String applicationID, int version) {
		logger.debug("Inside populateApplicationDocs");
		Gson gson = new Gson();
		List<String> documentList = new ArrayList<>();
		List<ApplicationDocumentsWrapper> applicationDocumentsWrapperList = requestObj
				.getApplicationDocumentsWrapperList();

		List<CustomerDetails> customerDetailsWrapperList = requestObj.getCustomerDetailsList();

		// Changed date - 24/02/2025
		boolean isCustDtl = false;

		try {
			List<CustomerDetails> custDetails = custDtlRepo.findByApplicationIdAndAppId(applicationID,
					requestObj.getAppId());
			logger.debug("custDetails size"+ custDetails.size());
			logger.debug("custDetails"+ custDetails);
			if (custDetails.size()>0) {
				for (CustomerDetails customerDetails : customerDetailsWrapperList) {
					logger.debug("Size of customerDetailsWrapperList"+ customerDetailsWrapperList.size());
					for (CustomerDetails customerDtls : custDetails) {
						logger.debug("Size of custDetails"+ custDetails.size());
						logger.debug("custDtlId from request" + customerDetails.getCustDtlId());
						if (customerDetails.getCustDtlId().equals(customerDtls.getCustDtlId())) {
							isCustDtl = true;
							logger.debug("Success case of custdtlID");
							break;
						}
					}
					if (isCustDtl)
						break;
				}
			} else {
				isCustDtl = true;
				logger.debug("Cusrtdtl value " + isCustDtl);

			}
		} catch (Exception e) {
			logger.error("Error fetching customer details: " + e.getMessage());
		}
		logger.debug("Cusrtdtl value 1 " + isCustDtl);
		if (isCustDtl) {
			logger.debug("Customer details matched.");
			for (ApplicationDocumentsWrapper applicationDocumentsWrapper : applicationDocumentsWrapperList) {
				List<ApplicationDocuments> applicationDocumentsList = applicationDocumentsWrapper
						.getApplicationDocumentsList();
				for (ApplicationDocuments applicationDocuments : applicationDocumentsList) {
					if (applicationDocuments.getAppDocId() == null) {// This is to handle the case if user changed the
						// data
						// after its being inserted by using the back
						// navigation within the session.
						BigDecimal appDocId = CommonUtils.generateRandomNum();
						applicationDocuments.setAppDocId(appDocId);
						documentList.add(appDocId.toString());// to String is required to avoid rounding issue of Big
						// Decimal at front end.
						logger.debug("Generated new AppDocId: " + appDocId);
					}
					applicationDocuments.setApplicationId(applicationID);
					applicationDocuments.setVersionNum(version);
					applicationDocuments.setAppId(requestObj.getAppId());
					String payload = gson.toJson(applicationDocuments.getPayload());
					applicationDocuments.setPayloadColumn(payload);
					applicationDocuments.setStatus(AppStatus.ACTIVE_STATUS.getValue());
					logger.debug("Saving application document with AppDocId:" + applicationDocuments.getAppDocId());
					appLoanDocsRepository.save(applicationDocuments);
				}
			}
			customerIdentification.setDocumentList(documentList);
			customerIdentification.setApplicationId(applicationID);
			customerIdentification.setVersionNum(version);
			logger.warn("Data inserted into TB_ABOB_APPLN_DOCUMENTS for CASA");
		}
		logger.debug("Customer Details not matched");
	}

	private void populateOrUpdateLoanDtls(ApplyLoanRequestFields requestObj, int version, String applicationID,
										  CustomerIdentificationLoan customerIdentification, String src) {
		Gson gson = new Gson();
		String payload;
		LoanDetails loanDtlObj = new LoanDetails();
		LoanDetails loanDtlObjReq = requestObj.getLoanDetails();
		if (loanDtlObjReq != null) {

			Optional<LoanDetails> loanDetailsDb = loanDtlsRepo.findTopByApplicationIdAndAppId(applicationID,
					Constants.APPID);
			if (loanDetailsDb.isPresent()) {
				loanDtlObj.setLoanDtlId(loanDetailsDb.get().getLoanDtlId());
				loanDtlObj.setCoapplicantId(loanDetailsDb.get().getCoapplicantId());
				loanDtlObj.setT24LoanId(loanDetailsDb.get().getT24LoanId());
				loanDtlObj.setLoanStatus(loanDetailsDb.get().getLoanStatus());
				loanDtlObj.setCoapplicantUpdateId(loanDetailsDb.get().getCoapplicantUpdateId());
				loanDtlObj.setLoanRepaymentSchedule(loanDetailsDb.get().getLoanRepaymentSchedule());
			} else {
				loanDtlObj.setLoanDtlId(CommonUtils.generateRandomNum());
			}
			loanDtlObj.setAppId(requestObj.getAppId());
			loanDtlObj.setApplicationId(applicationID);
			loanDtlObj.setVersionNum(version);
			if (loanDtlObj != null) {
				if (Constants.LOAN_DETAILS.equalsIgnoreCase(src)) {
					loanDtlObj.setLoanAmount(loanDtlObjReq.getLoanAmount());
					loanDtlObj.setTenureInMonths(loanDtlObjReq.getTenureInMonths());
					loanDtlObj.setTenure(loanDtlObjReq.getTenure());
					loanDtlObj.setRoi(loanDtlObjReq.getRoi());
					loanDtlObj.setInterest(loanDtlObjReq.getInterest());
					loanDtlObj.setLoanClosureDate(null);
					loanDtlObj.setTotPayableAmount(loanDtlObjReq.getTotPayableAmount());
					payload = gson.toJson(loanDtlObjReq.getPayload());
					loanDtlObj.setPayloadColumn(payload);
				} else if (Constants.EMI_DETAILS.equalsIgnoreCase(src)) {
					loanDtlObj.setAutoEmiAccount(loanDtlObjReq.getAutoEmiAccount());
					loanDtlObj.setAutoEmiAccountType(loanDtlObjReq.getAutoEmiAccountType());
					loanDtlObj.setEmiDate(loanDtlObjReq.getEmiDate());
					loanDtlObj.setMonthlyEmi(loanDtlObjReq.getMonthlyEmi());
				} else if (Constants.LOAN_CR_DETAILS.equalsIgnoreCase(src)) {
					loanDtlObj.setLoanCrAccount(loanDtlObjReq.getLoanCrAccount());
					loanDtlObj.setLoanCrAccountType(loanDtlObjReq.getLoanCrAccountType());
				}
				customerIdentification.setLoanDtlId(loanDtlObj.getLoanDtlId().toString());
				customerIdentification.setVersionNum(version);
				customerIdentification.setApplicationId(applicationID);
				loanDtlsRepo.save(loanDtlObj);
			}
		}
	}

	private void updateCustomerDtlInMaster(ApplyLoanRequestFields requestObj, int version, String applicationID,
										   CustomerIdentificationLoan customerIdentification) {
		Optional<ApplicationMaster> masterObjDb = applicationMasterRepo
				.findByAppIdAndApplicationIdAndVersionNumAndApplicationStatus(requestObj.getAppId(),
						requestObj.getApplicationId(), version, AppStatus.INPROGRESS.getValue());
		if (masterObjDb.isPresent()) {
			ApplicationMaster masterRequest = requestObj.getApplicationMaster();
			ApplicationMaster masterObj = masterObjDb.get();
			if (!(CommonUtils.isNullOrEmpty(masterRequest.getCreatedBy()))) {
				masterObj.setCreatedBy(masterRequest.getCreatedBy());
			}
			if (!(CommonUtils.isNullOrEmpty(masterRequest.getMobileNumber()))) {
				masterObj.setMobileNumber(masterRequest.getMobileNumber());
			}
			if (!(CommonUtils.isNullOrEmpty(masterRequest.getEmailId()))) {
				masterObj.setEmailId(masterRequest.getEmailId());
			}
			customerIdentification.setApplicationId(applicationID);
			customerIdentification.setVersionNum(version);
			applicationMasterRepo.save(masterObj);
		}
	}

	private void updateCustIdAndBranchInMaster(ApplyLoanRequestFields requestObj, int version) {
		ApplicationMaster masterRequest = requestObj.getApplicationMaster();
		Optional<ApplicationMaster> masterObjDb = applicationMasterRepo
				.findByAppIdAndApplicationIdAndVersionNumAndApplicationStatus(requestObj.getAppId(),
						requestObj.getApplicationId(), version, AppStatus.INPROGRESS.getValue());
		if (masterObjDb.isPresent()) {
			ApplicationMaster masterObj = masterObjDb.get();
			masterObj.setCustomerId(masterRequest.getCustomerId());
			masterObj.setSearchCode1(masterRequest.getSearchCode1());
			applicationMasterRepo.save(masterObj);
		}
	}

	private void populateAppMasterAndApplnwf(ApplyLoanRequestFields requestObj, String applicationID, int version,
											 CustomerIdentificationLoan customerIdentification, boolean isSelfOnBoardingHeaderAppId, Properties prop) {
		ApplicationMaster appMasterReq = requestObj.getApplicationMaster();
		ApplicationMaster appMaster = new ApplicationMaster();
		appMaster.setAppId(requestObj.getAppId());
		appMaster.setApplicationDate(LocalDate.now());
		appMaster.setApplicationId(applicationID);
		appMaster.setApplicationStatus(AppStatus.INPROGRESS.getValue());
		if ("Y".equalsIgnoreCase(requestObj.getIsExistingCustomer())) {
			appMaster.setApplicationType(Constants.ETB);
		} else if ("N".equalsIgnoreCase(requestObj.getIsExistingCustomer())) {
			appMaster.setApplicationType(Constants.NTB);
		}
		appMaster.setCreatedBy(appMasterReq.getCreatedBy());
		appMaster.setUpdatedBy(appMasterReq.getCreatedBy());
		appMaster.setApplicantsCount(appMasterReq.getApplicantsCount());
		appMaster.setEmailId(appMasterReq.getEmailId());
		appMaster.setMobileNumber(appMasterReq.getMobileNumber());
		appMaster.setProductCode(appMasterReq.getProductCode());
		appMaster.setProductGroupCode(appMasterReq.getProductGroupCode());
		appMaster.setVersionNum(version);
		appMaster.setCurrentScreenId(appMasterReq.getCurrentScreenId().split("~")[0]);
		appMaster.setCustomerId(appMasterReq.getCustomerId());
		appMaster.setSearchCode1(appMasterReq.getSearchCode1());
		appMaster.setSearchCode2(appMasterReq.getSearchCode2());
		if (!(CommonUtils.isNullOrEmpty(appMasterReq.getMobileNumber()))) {
			appMaster.setMobileVerStatus("Y");
		}
		if (!(CommonUtils.isNullOrEmpty(appMasterReq.getEmailId()))) {
			appMaster.setEmailVerStatus("Y");
		}
		appMaster.setKendraId(appMasterReq.getKendraId());
		appMaster.setKendraName(appMasterReq.getKendraName());
		appMaster.setBranchId(appMasterReq.getBranchId());
		appMaster.setBranchName(appMasterReq.getBranchName());
		appMaster.setMemberId(appMasterReq.getMemberId());
		appMaster.setPrimaryKycType(appMasterReq.getPrimaryKycType());
		appMaster.setPrimaryKycId(appMasterReq.getPrimaryKycId());
		appMaster.setSecondaryKycType(appMasterReq.getSecondaryKycType());
		appMaster.setSecondaryKycId(appMasterReq.getSecondaryKycId());
		appMaster.setWorkitemNo(appMasterReq.getWorkitemNo());
		appMaster.setCurrentStageNo(appMasterReq.getCurrentStageNo());
		appMaster.setAlternateVoterId(appMasterReq.getAlternateVoterId());

		boolean isAnyBranchWhitelisted = whitelistedBranchesRepository.isAnyBranchWhitelisted(Arrays.asList(appMasterReq.getBranchId()));
		if(isAnyBranchWhitelisted){
			appMaster.setDeclarationFlag(Constants.IEXCEED_FLAG);
		}

		if (Constants.RENEWAL_LOAN_PRODUCT_CODE.equals(appMasterReq.getProductCode())) {
			Optional<ApplicationMaster> oldMasterData = applicationMasterRepo
					.findByAppIdAndMemberId(requestObj.getAppId(), appMasterReq.getMemberId());
			logger.debug("oldMasterData : " + oldMasterData.toString());
			if (!oldMasterData.isPresent()) {
				appMaster.setRelatedApplicationId(Constants.NEWGEN_RENEWAL_LOAN);
			} else {
				appMaster.setRelatedApplicationId(oldMasterData.get().getApplicationId());
			}
		}
		logger.debug("appMaster : " + appMaster.toString());
		customerIdentification.setRelatedApplicationId(appMaster.getRelatedApplicationId());
		customerIdentification.setApplicationId(applicationID);
		customerIdentification.setVersionNum(version);
		applicationMasterRepo.save(appMaster);
		if (!isSelfOnBoardingHeaderAppId
				|| ("N".equalsIgnoreCase(prop.getProperty(CobFlagsProperties.LOAN_STP.getKey())))) {
			WorkFlowDetails wfObj = requestObj.getWorkflow();
			PopulateapplnWFRequest apiRequest = new PopulateapplnWFRequest();
			PopulateapplnWFRequestFields requestObjWf = new PopulateapplnWFRequestFields();
			requestObjWf.setAppId(requestObj.getAppId());
			requestObjWf.setApplicationId(applicationID);
			requestObjWf.setApplicationStatus(AppStatus.INPROGRESS.getValue());
			if (!isSelfOnBoardingHeaderAppId) {
				requestObjWf.setCreatedBy(appMasterReq.getCreatedBy());
			} else {
				requestObjWf.setCreatedBy(Constants.CUSTOMER);
			}
			requestObjWf.setVersionNum(version);
			requestObjWf.setWorkflow(wfObj);
			apiRequest.setRequestObj(requestObjWf);
			commonParamService.populateApplnWorkFlow(apiRequest);
			logger.warn("Data inserted into TB_ABOB_APPLN_WORKFLOW");
			Optional<ApplicationWorkflow> workflow = applnWfRepository
					.findTopByAppIdAndApplicationIdAndVersionNumOrderByWorkflowSeqNumDesc(requestObj.getAppId(),
							applicationID, version);
			if (workflow.isPresent()) {
				ApplicationWorkflow applnWf = workflow.get();
				List<WorkflowDefinition> wfDefnList = wfDefnLoanRepo.findByFromStageId(applnWf.getNextWorkFlowStage());
				customerIdentification.setApplnWfDefinitionList(wfDefnList);
			}
		}
	}

	public boolean isVaptPassedForScreenElements(ApplyLoanRequest request, JSONArray array) {
		boolean flag = false;
		if (array == null) {
			flag = false;
		} else {
			JSONArray stageArray = null;
			ApplicationMaster appMasterReq = request.getRequestObj().getApplicationMaster();
			String[] currentStage = appMasterReq.getCurrentScreenId().split("~");
			for (Object element : array) {
				String stage = ((String) element).split("~")[0];
				if (stage.equalsIgnoreCase(Constants.CUSTOMER_DETAILS)
						&& currentStage[0].equalsIgnoreCase(Constants.CUSTOMER_DETAILS)) {
					stageArray = commonParamService.getJsonArrayForCmCodeAndKey(stage, Constants.COMM, stage); // to get
					// CUSTOMERDETAILS,
					// OCCUPATIONDETAILS
					List<CustomerDetails> customerDetailsList = request.getRequestObj().getCustomerDetailsList();
					List<AddressDetailsWrapper> addressDetailsWrapperList = request.getRequestObj()
							.getAddressDetailsWrapperList();
					flag = commonParamService.vaptForFieldsCustDtls(customerDetailsList, addressDetailsWrapperList,
							stageArray);
					break;
				} else if (stage.equalsIgnoreCase(Constants.OCCUPATION_DETAILS)
						&& currentStage[0].equalsIgnoreCase(Constants.OCCUPATION_DETAILS)) {
					stageArray = commonParamService.getJsonArrayForCmCodeAndKey(stage, Constants.COMM, stage); // to get
					// CUSTOMERDETAILS,
					// OCCUPATIONDETAILS
					List<OccupationDetailsWrapper> occupationDetailsWrapperList = request.getRequestObj()
							.getOccupationDetailsWrapperList();
					List<AddressDetailsWrapper> addressDetailsWrapperList = request.getRequestObj()
							.getAddressDetailsWrapperList();
					flag = commonParamService.vaptForFieldsOccupationDtls(occupationDetailsWrapperList,
							addressDetailsWrapperList, stageArray);
					break;
				} else if (stage.equalsIgnoreCase(Constants.CUST_VERIFICATION)
						&& currentStage[0].equalsIgnoreCase(Constants.CUST_VERIFICATION)) {
					if ("Y".equalsIgnoreCase(request.getRequestObj().getIsExistingCustomer())) {
						stageArray = commonParamService.getJsonArrayForCmCodeAndKey(stage, CodeTypes.LOAN_ETB.getKey(),
								stage); // to get CUSTVERIFICATION + LOAN_ETB
						break;
					} else if ("N".equalsIgnoreCase(request.getRequestObj().getIsExistingCustomer())) {
						stageArray = commonParamService.getJsonArrayForCmCodeAndKey(stage, Products.CASA.getKey(),
								stage); // to
						// get
						// CUSTVERIFICATION
						// +
						// CASA
						flag = commonParamService.vaptForFieldsCustVerificationCasa(appMasterReq, stageArray);
						break;
					}
				}
			}
			if (null == stageArray) { // If configuration is not found, consider it as true.
				return true;
			}
		}
		return flag;
	}

	public Mono<Object> fetchCustomerDetails(FetchCustDtlRequest request, Header header) {
		return interfaceAdapter.callExternalService(header, request, request.getInterfaceName());
	}

	public Mono<Response> checkApplication(CheckApplicationRequest request, Header header) throws IOException {
		Gson gson = new Gson();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();
		Properties prop = CommonUtils.readPropertyFile();
		if ("Y".equalsIgnoreCase(prop.getProperty(CobFlagsProperties.EXT_SYSTEM_DEDUPE_REQUIRED.getKey()))) {
			// dedupe check hook.
			Mono<Object> extResponse = interfaceAdapter.callExternalService(header, request,
					request.getInterfaceName());
			return extResponse.flatMap(val -> {
				Response response = new Response();
				ResponseWrapper res = adapterUtil.getResponseMapper(val, request.getInterfaceName(), header);
				if (ResponseParser.isExtCallSuccess(res.getApiResponse(), "checkApplication")) {
					if (ResponseParser.isNewCustomer(res.getApiResponse())) {
						response = checkApplication(request, responseHeader, prop, responseBody);
					} else {
						responseHeader.setResponseCode(ResponseCodes.APP_PRESENT_APPROVED_STATUS.getKey()); // IV109
						JSONArray customerList = ResponseParser.getApplicationList(res.getApiResponse());
						responseBody.setResponseObj(gson.toJson(customerList));
						response.setResponseHeader(responseHeader);
						response.setResponseBody(responseBody);
					}
				} else {
					// custom code to handle failure of external API.
					responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
					response.setResponseHeader(responseHeader);
				}
				return Mono.just(response);
			});
		} else if ("N".equalsIgnoreCase(prop.getProperty(CobFlagsProperties.EXT_SYSTEM_DEDUPE_REQUIRED.getKey()))) {
			Response response = checkApplication(request, responseHeader, prop, responseBody);
			return Mono.just(response);
		} else {
			return Mono.empty();
		}
	}

	public Response checkApplication(CheckApplicationRequest request, ResponseHeader responseHeader, Properties prop,
									 ResponseBody responseBody) {
		Gson gson = new Gson();
		Response response = new Response();
		CheckApplicationRes resElements = new CheckApplicationRes();
		List<String> inprogress = new ArrayList<>();
		String mobileNum = null;
		String emailId = null;
		String nationalId = null;
		String pan = null;
		String productGroupCode = null;
		String customerId = null;
		String res = "";
		CheckAppRequestFields requestFields = request.getRequestObj();
		responseHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());
		List<String> statusList = new ArrayList<>();
		statusList.add(AppStatus.INPROGRESS.getValue());
		statusList.add(AppStatus.APPROVED.getValue());
		if (!(CommonUtils.isNullOrEmpty(requestFields.getMobileNumber()))) {
			mobileNum = requestFields.getMobileNumber();
		}
		if (!(CommonUtils.isNullOrEmpty(requestFields.getNationalId()))) {
			nationalId = requestFields.getNationalId();
		}
		if (!(CommonUtils.isNullOrEmpty(requestFields.getEmailId()))) {
			emailId = requestFields.getEmailId();
		}
		if (!(CommonUtils.isNullOrEmpty(requestFields.getPan()))) {
			pan = requestFields.getPan();
		}
		if (!(CommonUtils.isNullOrEmpty(requestFields.getProductGroupCode()))) {
			productGroupCode = requestFields.getProductGroupCode();
		}
		if (!(CommonUtils.isNullOrEmpty(requestFields.getCustomerId()))) {
			customerId = requestFields.getCustomerId();
		}
		List<ApplicationMaster> appMasterObj = applicationMasterRepo.findData(requestFields.getAppId(), mobileNum,
				nationalId, pan, emailId, productGroupCode, statusList,
				customerId == null ? null : new BigDecimal(customerId));
		boolean iv108 = false;
		boolean iv115 = false;
		boolean iv109 = false;
		for (ApplicationMaster appMasterObjDb : appMasterObj) {
			String headerAppId = request.getAppId();
			JSONArray array;
			if (headerAppId.equalsIgnoreCase(prop.getProperty(CobFlagsProperties.APPID_SELF_ONBOARDING.getKey()))) {
				array = commonParamService.getJsonArrayForCmCodeAndKey(Constants.FUNCTIONSEQUENCE,
						CodeTypes.LOAN_ETB.getKey(), Constants.FUNCTIONSEQUENCE);
			} else {
				array = commonParamService.getJsonArrayForCmCodeAndKey(Constants.FUNCTIONSEQUENCE,
						CodeTypes.LOAN_BO_ETB.getKey(), Constants.FUNCTIONSEQUENCE);
			}
			String lastElementArr = ((String) array.get(array.length() - 1)).split("~")[0];
			String currentSrnId = appMasterObjDb.getCurrentScreenId();
			if (appMasterObjDb != null
					&& AppStatus.APPROVED.getValue().equalsIgnoreCase(appMasterObjDb.getApplicationStatus())) {
				iv109 = true;
			} else if (appMasterObjDb != null
					&& AppStatus.INPROGRESS.getValue().equalsIgnoreCase(appMasterObjDb.getApplicationStatus())
					&& lastElementArr.equalsIgnoreCase(currentSrnId)) {
				res = appMasterObjDb.getApplicationId() + "~" + appMasterObjDb.getAppId() + "~"
						+ appMasterObjDb.getVersionNum() + "~" + appMasterObjDb.getApplicationStatus() + "~"
						+ appMasterObjDb.getRelatedApplicationId() + "~" + appMasterObjDb.getProductGroupCode() + "~"
						+ appMasterObjDb.getProductCode();
				inprogress.add(res);
				iv115 = true; // All stages are done but still in inprogress status so dont allow to proceed.
				// IV115
			} else {
				String allowPartialApplication = prop
						.getProperty(CobFlagsProperties.ALLOW_PARTIAL_APPLICATION.getKey());
				if ("Y".equalsIgnoreCase(allowPartialApplication) && appMasterObjDb != null) {
					res = appMasterObjDb.getApplicationId() + "~" + appMasterObjDb.getAppId() + "~"
							+ appMasterObjDb.getVersionNum() + "~" + appMasterObjDb.getApplicationStatus() + "~"
							+ appMasterObjDb.getRelatedApplicationId() + "~" + appMasterObjDb.getProductGroupCode()
							+ "~" + appMasterObjDb.getProductCode();
					inprogress.add(res);
					if (AppStatus.INPROGRESS.getValue().equalsIgnoreCase(appMasterObjDb.getApplicationStatus())) {
						iv108 = true;
					}
				} else if ("N".equalsIgnoreCase(allowPartialApplication)) {
					String deleteRule = prop.getProperty(CobFlagsProperties.LOANS_DELETE_RULE.getKey());
					if (Constants.HARD_DELETE.equalsIgnoreCase(deleteRule) && appMasterObjDb != null) {
						deleteApplication(appMasterObjDb.getApplicationId(), appMasterObjDb.getAppId());
					} else if (Constants.MOVE_TO_HISTORY_TABLES.equalsIgnoreCase(deleteRule)) {
						populateHistoryTables(appMasterObjDb.getApplicationId(), appMasterObjDb.getAppId());
					} else if (Constants.UPDATE_STATUS.equalsIgnoreCase(deleteRule)) {
						appMasterObjDb.setApplicationStatus(AppStatus.DELETED.getValue());
						applicationMasterRepo.save(appMasterObjDb);
					} else {
						responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
					}
				}
			}
		}
		resElements.setInProgress(inprogress);
		responseBody.setResponseObj(gson.toJson(resElements));
		if (iv108 && !iv109 && !iv115) {
			responseHeader.setResponseCode(ResponseCodes.APP_PRESENT_INPROGRESS_STATUS.getKey()); // IV108
		} else if (!iv108 && iv109 && !iv115) {
			responseHeader.setResponseCode(ResponseCodes.APP_PRESENT_APPROVED_STATUS.getKey()); // IV109
		} else if (!iv108 && !iv109 && iv115) {
			responseHeader.setResponseCode(ResponseCodes.APP_PRESENT_INPROGRESS_LAST_STAGE.getKey()); // All stages are
			// done but
			// still in
			// inprogress
			// status so
			// dont allow to
			// proceed.
			// IV115
		} else if (iv108 && iv109 && !iv115) {
			responseHeader.setResponseCode(ResponseCodes.APP_PRESENT_INPROGRESS_STATUS.getKey()); // IV108
		} else if (!iv108 && iv109 && iv115) {
			responseHeader.setResponseCode(ResponseCodes.APP_PRESENT_INPROGRESS_LAST_STAGE.getKey()); // All stages are
			// done but
			// still in
			// inprogress
			// status so
			// dont allow to
			// proceed.
			// IV115
		}
		response.setResponseHeader(responseHeader);
		response.setResponseBody(responseBody);
		return response;
	}

	private void populateHistoryTables(String applicationId, String appId) {
		List<ApplicationMaster> appMasterOpt = applicationMasterRepo.findByAppIdAndApplicationId(appId, applicationId);
		if (null != appMasterOpt && appMasterOpt.size() > 0) {
			for (ApplicationMaster appMaster : appMasterOpt) {
				ApplicationMasterHistory appMasterHistory = new ApplicationMasterHistory();
				BeanUtils.copyProperties(appMaster, appMasterHistory);
				applicationMasterHisRepo.save(appMasterHistory);
				applicationMasterRepo.deleteByApplicationIdAndAppId(applicationId, appId);
			}

			LoanHisDetails loanHisDtls;
			List<LoanDetails> loanList = loanDtlsRepo.findByApplicationIdAndAppId(applicationId, appId);
			for (LoanDetails loan : loanList) {
				loanHisDtls = new LoanHisDetails();
				BeanUtils.copyProperties(loan, loanHisDtls);
				loanDtlsHisRepo.save(loanHisDtls);
			}
			loanDtlsRepo.deleteByApplicationIdAndAppId(applicationId, appId);

			ApplicationDocumentsHistory documentHistory;
			List<ApplicationDocuments> documentList = appLoanDocsRepository.findByApplicationIdAndAppId(applicationId,
					appId);
			for (ApplicationDocuments documentObj : documentList) {
				documentHistory = new ApplicationDocumentsHistory();
				BeanUtils.copyProperties(documentObj, documentHistory);
				appLoanDocsHisRepository.save(documentHistory);
			}
			appLoanDocsRepository.deleteByApplicationIdAndAppId(applicationId, appId);

			CustomerDetailsHistory custDtlHistory;
			List<CustomerDetails> custDtlList = custDtlRepo.findByApplicationIdAndAppId(applicationId, appId);
			for (CustomerDetails custdtlObj : custDtlList) {
				custDtlHistory = new CustomerDetailsHistory();
				BeanUtils.copyProperties(custdtlObj, custDtlHistory);
				custDtlHisRepo.save(custDtlHistory);
			}
			custDtlRepo.deleteByApplicationIdAndAppId(applicationId, appId);

			AddressDetailsHistory addresshistory;
			List<AddressDetails> addressList = addressDtlRepo.findByApplicationIdAndAppId(applicationId, appId);
			for (AddressDetails addressObj : addressList) {
				addresshistory = new AddressDetailsHistory();
				BeanUtils.copyProperties(addressObj, addresshistory);
				addressDtlHisRepo.save(addresshistory);
			}
			addressDtlRepo.deleteByApplicationIdAndAppId(applicationId, appId);

			OccupationDetailsHistory occupationHistory;
			List<OccupationDetails> occupationList = occupationDtlRepo.findByApplicationIdAndAppId(applicationId,
					appId);
			for (OccupationDetails ocupationObj : occupationList) {
				occupationHistory = new OccupationDetailsHistory();
				BeanUtils.copyProperties(ocupationObj, occupationHistory);
				occupationDtlHisRepo.save(occupationHistory);
			}
			occupationDtlRepo.deleteByApplicationIdAndAppId(applicationId, appId);
		}
	}

	private boolean populateHistoryTablesAndDiscardCoApplicant(DiscardCoApplicantRequestFields requestFields) {
		boolean flag = false;
		ObjectMapper objectMapper = new ObjectMapper();
		Gson gson = new Gson();
		try {
			String appId = requestFields.getAppId();
			String applicationId = requestFields.getApplicationId();
			BigDecimal custDtlId = requestFields.getCustDtlId();
			int versionNo = requestFields.getVersionNum();
			List<CustomerDetails> custDetails = custDtlRepo
					.findByApplicationIdAndAppIdAndVersionNumAndCustDtlId(applicationId, appId, versionNo, custDtlId);
			if (null != custDetails) {
				CustomerDetailsHistory custDtlHistory;
				for (CustomerDetails custdtlObj : custDetails) {
					custDtlHistory = new CustomerDetailsHistory();
					BeanUtils.copyProperties(custdtlObj, custDtlHistory);
					CustomerDetailsPayload customerDetailsPayload = objectMapper
							.readValue(custDtlHistory.getPayloadColumn(), CustomerDetailsPayload.class);
					customerDetailsPayload.setRemarks(requestFields.getRemarks());
					customerDetailsPayload.setReason(requestFields.getReason());
					custDtlHistory.setPayloadColumn(gson.toJson(customerDetailsPayload));
					custDtlHisRepo.save(custDtlHistory);
				}
				custDtlRepo.deleteByApplicationIdAndAppIdAndCustDtlId(applicationId, appId, custDtlId);

				AddressDetailsHistory addresshistory;
				List<AddressDetails> addressList = addressDtlRepo.findByApplicationIdAndAppIdAndVersionNumAndCustDtlId(
						applicationId, appId, versionNo, custDtlId);
				for (AddressDetails addressObj : addressList) {
					addresshistory = new AddressDetailsHistory();
					BeanUtils.copyProperties(addressObj, addresshistory);
					addressDtlHisRepo.save(addresshistory);
				}
				addressDtlRepo.deleteByApplicationIdAndAppIdAndCustDtlId(applicationId, appId, custDtlId);

				OccupationDetailsHistory occupationHistory;
				List<OccupationDetails> occupationList = occupationDtlRepo
						.findByApplicationIdAndAppIdAndVersionNumAndCustDtlId(applicationId, appId, versionNo,
								custDtlId);
				for (OccupationDetails ocupationObj : occupationList) {
					occupationHistory = new OccupationDetailsHistory();
					BeanUtils.copyProperties(ocupationObj, occupationHistory);
					occupationDtlHisRepo.save(occupationHistory);
				}
				occupationDtlRepo.deleteByApplicationIdAndAppIdAndCustDtlId(applicationId, appId, custDtlId);

				InsuranceDetailsHistory insuranceHistory;
				Optional<InsuranceDetails> insuranceDetails = insuranceDtlRepo
						.findByApplicationIdAndAppIdAndCustDtlId(applicationId, appId, custDtlId);
				if (insuranceDetails.isPresent()) {
					InsuranceDetails insuranceObj = insuranceDetails.get();

					insuranceHistory = new InsuranceDetailsHistory();
					BeanUtils.copyProperties(insuranceObj, insuranceHistory);
					insuranceDtlHisRepo.save(insuranceHistory);
					insuranceDtlRepo.deleteByApplicationIdAndAppIdAndCustDtlId(applicationId, appId, custDtlId);
				}

				CibilDetailsHistory cibilHisDetails;
				Optional<CibilDetails> cibilDetails = cibilDtlRepo
						.findByApplicationIdAndAppIdAndCustDtlId(applicationId, appId, custDtlId);
				if (cibilDetails.isPresent()) {
					CibilDetails cibilObj = cibilDetails.get();

					cibilHisDetails = new CibilDetailsHistory();
					BeanUtils.copyProperties(cibilObj, cibilHisDetails);
					cibilDtlHisRepo.save(cibilHisDetails);
					cibilDtlRepo.deleteByApplicationIdAndAppIdAndCustDtlId(applicationId, appId, custDtlId);
				}

				ApplicationDocumentsHistory documentHistory;
				Optional<List<ApplicationDocuments>> documentList = appLoanDocsRepository
						.findByApplicationIdAndCustDtlId(applicationId, custDtlId);

				if (documentList.isPresent()) {

					List<ApplicationDocuments> docList = documentList.get();
					for (ApplicationDocuments documentObj : docList) {
						documentHistory = new ApplicationDocumentsHistory();
						BeanUtils.copyProperties(documentObj, documentHistory);
						appLoanDocsHisRepository.save(documentHistory);
					}
					appLoanDocsRepository.deleteByApplicationIdAndCustDtlId(applicationId, custDtlId);
				}

				List<ApplicationMaster> appDetails = applicationMasterRepo.findByAppIdAndApplicationId(appId,
						applicationId);
				ApplicationMaster appMasterObj = appDetails.get(0);
				appMasterObj.setCurrentStageNo(4);
				appMasterObj.setCurrentScreenId(Constants.LOAN_DETAILS);
				applicationMasterRepo.save(appMasterObj);
				flag = true;
			}
		} catch (Exception e) {
			logger.error("Exception in discard co applicant " + e.getMessage());
		}
		return flag;
	}

	private void deleteApplication(String applicationId, String appId) {
		applicationMasterRepo.deleteByApplicationIdAndAppId(applicationId, appId);
		loanDtlsRepo.deleteByApplicationIdAndAppId(applicationId, appId);
		appLoanDocsRepository.deleteByApplicationIdAndAppId(applicationId, appId);
		custDtlRepo.deleteByApplicationIdAndAppId(applicationId, appId);
		addressDtlRepo.deleteByApplicationIdAndAppId(applicationId, appId);
		occupationDtlRepo.deleteByApplicationIdAndAppId(applicationId, appId);
	}

	public Response fetchApplication(FetchAppRequest request) {
		String applicationId = request.getRequestObj().getApplicationId();
		String appId = request.getRequestObj().getAppId();
		int versionNum = request.getRequestObj().getVersionNum();
		Gson gson = new Gson();
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		response.setResponseHeader(responseHeader);
		ResponseBody responseBody = new ResponseBody();
		Optional<ApplicationMaster> applicationMasterOpt = applicationMasterRepo
				.findTopByAppIdAndApplicationIdOrderByVersionNumDesc(appId, applicationId);
		if (applicationMasterOpt.isPresent()) {
			ApplicationMaster applicationMasterData = applicationMasterOpt.get();
			ApplyLoanRequestFields loanFields = getCustomerData(applicationMasterData, applicationId, appId,
					versionNum);
			String customerdata = gson.toJson(loanFields);
			customerdata = customerdata.replace(Constants.PAYLOAD_COLUMN, Constants.PAYLOAD);
			responseHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());
			responseBody.setResponseObj(customerdata);
			response.setResponseBody(responseBody);
			return response;
		} else {
			responseHeader.setResponseCode(ResponseCodes.INVALID_APP_MASTER.getKey());
			responseBody.setResponseObj(Constants.APP_MASTER_NOT_FOUND);
			response.setResponseBody(responseBody);
			return response;
		}
	}

	public boolean discardApplication(ApplyLoanRequest req) throws IOException {
		boolean flag = false;
		ApplyLoanRequestFields requestFields = req.getRequestObj();
		ApplicationMaster masterObj = requestFields.getApplicationMaster();
		String deleteRule;
		Optional<ApplicationMaster> applicationMasterOpt = applicationMasterRepo
				.findByAppIdAndApplicationIdAndVersionNumAndApplicationStatusAndCustomerId(requestFields.getAppId(),
						requestFields.getApplicationId(), requestFields.getVersionNum(),
						AppStatus.INPROGRESS.getValue(), masterObj.getCustomerId());
		if (applicationMasterOpt.isPresent()) {
			ApplicationMaster masterObjDb = applicationMasterOpt.get();
			Properties prop = CommonUtils.readPropertyFile();
			deleteRule = prop.getProperty(CobFlagsProperties.LOANS_DELETE_RULE.getKey());
			if (Constants.HARD_DELETE.equalsIgnoreCase(deleteRule)) {
				deleteApplication(requestFields.getApplicationId(), requestFields.getAppId());
			} else if (Constants.MOVE_TO_HISTORY_TABLES.equalsIgnoreCase(deleteRule)) {
				populateHistoryTables(requestFields.getApplicationId(), requestFields.getAppId());
			} else if (Constants.UPDATE_STATUS.equalsIgnoreCase(deleteRule)) {
				masterObjDb.setApplicationStatus(AppStatus.DELETED.getValue());
				applicationMasterRepo.save(masterObjDb);
			} else {
				flag = false;
			}
			if (!CommonUtils.isNullOrEmpty(masterObjDb.getRelatedApplicationId())) { // discard the corresponding casa
				CustomerDataFields requestObj = new CustomerDataFields();
				CreateModifyUserRequest apiRequest = new CreateModifyUserRequest();
				requestObj.setApplicationId(masterObjDb.getRelatedApplicationId());
				requestObj.setAppId(requestFields.getAppId());
				requestObj.setVersionNum(requestFields.getVersionNum());
				apiRequest.setRequestObj(requestObj);
				flag = cobService.discardApplication(apiRequest);
			}
			flag = true;
		}
		return flag;
	}

	public boolean discardApplicant(DiscardCoApplicantRequest req) throws IOException {
		DiscardCoApplicantRequestFields requestFields = req.getRequestObj();
		return populateHistoryTablesAndDiscardCoApplicant(requestFields);
	}

	public Response downloadApplication(FetchAppRequest fetchAppReq) {
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();
		String applicationId = fetchAppReq.getRequestObj().getApplicationId();
		String appId = fetchAppReq.getRequestObj().getAppId();
		int versionNum = fetchAppReq.getRequestObj().getVersionNum();
		Optional<ApplicationMaster> applicationMasterOpt = applicationMasterRepo
				.findTopByAppIdAndApplicationIdOrderByVersionNumDesc(appId, applicationId);
		if (applicationMasterOpt.isPresent()) {
			ApplicationMaster applicationMasterData = applicationMasterOpt.get();
			ApplyLoanRequestFields customerLoanDataFields = getCustomerData(applicationMasterData, applicationId, appId,
					versionNum);
			try {
				response = report.genratePdfService(customerLoanDataFields);
			} catch (FileNotFoundException e) {
				responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
				responseBody.setResponseObj(e.getMessage());
				response.setResponseHeader(responseHeader);
				response.setResponseBody(responseBody);
			}
		} else {
			responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
			responseBody.setResponseObj(Constants.APP_MASTER_NOT_FOUND);
			response.setResponseHeader(responseHeader);
			response.setResponseBody(responseBody);
		}
		return response;
	}

	private ApplyLoanRequestFields getCustomerData(ApplicationMaster applicationMasterData, String applicationId,
												   String appId, int versionNum) {
		ApplyLoanRequestFields loanFields = new ApplyLoanRequestFields();
		loanFields.setAppId(applicationMasterData.getAppId());
		loanFields.setApplicationId(applicationMasterData.getApplicationId());
		loanFields.setApplicationMaster(applicationMasterData);
		loanFields.setVersionNum(applicationMasterData.getVersionNum());

		List<CustomerDetails> customerDetailsList = custDtlRepo.findByApplicationIdAndAppIdAndVersionNum(applicationId,
				appId, versionNum);
		loanFields.setCustomerDetailsList(customerDetailsList);

		AddressDetailsWrapper addressDetailsWrapper = new AddressDetailsWrapper();
		List<AddressDetailsWrapper> addressDetailsWrapperList = new ArrayList<>();
		List<AddressDetails> addressDetailsList = addressDtlRepo.findByApplicationIdAndAppIdAndVersionNum(applicationId,
				appId, versionNum);
		addressDetailsWrapper.setAddressDetailsList(addressDetailsList);
		addressDetailsWrapperList.add(addressDetailsWrapper);
		loanFields.setAddressDetailsWrapperList(addressDetailsWrapperList);

		List<OccupationDetailsWrapper> occupationDetailsWrapperList = new ArrayList<>();
		OccupationDetailsWrapper occupationDetailsWrapper;
		List<OccupationDetails> occupationDetailsList = occupationDtlRepo
				.findByApplicationIdAndAppIdAndVersionNum(applicationId, appId, versionNum);
		for (OccupationDetails occupationDetails : occupationDetailsList) {
			occupationDetailsWrapper = new OccupationDetailsWrapper();
			occupationDetailsWrapper.setOccupationDetails(occupationDetails);
			occupationDetailsWrapperList.add(occupationDetailsWrapper);
		}
		loanFields.setOccupationDetailsWrapperList(occupationDetailsWrapperList);

		// insuranceDetails
		List<InsuranceDetailsWrapper> insuranceDetailsWrapper = new ArrayList<>();
		Optional<List<InsuranceDetails>> insuranceDetails = insuranceDtlRepo
				.findByApplicationIdAndAppIdAndVersionNum(applicationId, appId, versionNum);
		if (insuranceDetails.isPresent() && !insuranceDetails.get().isEmpty()) {
			insuranceDetails.get().forEach(insurance -> {
				InsuranceDetailsWrapper wrapperDetails = InsuranceDetailsWrapper.builder().insuranceDetails(insurance)
						.build();
				insuranceDetailsWrapper.add(wrapperDetails);
			});
			loanFields.setInsuranceDetailsWrapperList(insuranceDetailsWrapper);
		} else {
			loanFields.setInsuranceDetailsWrapperList(null);
		}
		// branchDetails
		List<BankDetailsWrapper> bankDetails = new ArrayList<>();
		Optional<List<BankDetails>> bankDetailsList = bankDtlRepo
				.findByApplicationIdAndAppIdAndVersionNum(applicationId, appId, versionNum);
		if (bankDetailsList.isPresent() && !bankDetailsList.get().isEmpty()) {
			bankDetailsList.get().forEach(bankDetail -> {
				BankDetailsWrapper detailsBankWrapper = BankDetailsWrapper.builder().bankDetails(bankDetail).build();
				bankDetails.add(detailsBankWrapper);
			});
			loanFields.setBankDetailsWrapperList(bankDetails);
		} else {
			loanFields.setBankDetailsWrapperList(null);
		}

		// CibilDetails
		List<CibilDetailsWrapper> cibilDetailsWrapper = new ArrayList<>();
		Optional<List<CibilDetails>> cibilDetails = cibilDtlRepo.findByApplicationIdAndAppIdAndVersionNum(applicationId,
				appId, versionNum);
		if (cibilDetails.isPresent() && !cibilDetails.get().isEmpty()) {
			cibilDetails.get().forEach(cibilDetail -> {
				CibilDetailsWrapper detailsWrapper = CibilDetailsWrapper.builder().cibilDetails(cibilDetail).build();
				cibilDetailsWrapper.add(detailsWrapper);
			});
			loanFields.setCibilDetailsWrapperList(cibilDetailsWrapper);
		} else {
			loanFields.setBankDetailsWrapperList(null);
		}

		List<ExistingLoanDetailsWrapper> existingLoanDetailsWrapper = new ArrayList<>();
		Optional<List<ExistingLoanDetails>> existingLoandDetails = existingLoanDtlRepo
				.findByApplicationIdAndAppIdAndVersionNum(applicationId, appId, versionNum);
		if (existingLoandDetails.isPresent() && !existingLoandDetails.get().isEmpty()) {
			ExistingLoanDetailsWrapper existingWrapper = ExistingLoanDetailsWrapper.builder()
					.existingLoanDetailsList(existingLoandDetails.get()).build();
			existingLoanDetailsWrapper.add(existingWrapper);
			loanFields.setExistingLoanDetailsWrapperList(existingLoanDetailsWrapper);
		} else {
			loanFields.setExistingLoanDetailsWrapperList(existingLoanDetailsWrapper);
		}

		LoanDetails loanDetails = loanDtlsRepo.findByApplicationIdAndAppIdAndVersionNum(applicationId, appId,
				versionNum);
		loanFields.setLoanDetails(loanDetails);

		ApplicationDocumentsWrapper applicationDocumentsWrapper = new ApplicationDocumentsWrapper();
		List<ApplicationDocumentsWrapper> applicationDocumentsWrapperList = new ArrayList<>();
		List<ApplicationDocuments> applicationDocumentsList = appLoanDocsRepository
				.findByApplicationIdAndAppIdAndVersionNumAndStatus(applicationId, appId, versionNum,
						AppStatus.ACTIVE_STATUS.getValue());
		applicationDocumentsWrapper.setApplicationDocumentsList(applicationDocumentsList);
		applicationDocumentsWrapperList.add(applicationDocumentsWrapper);
		loanFields.setApplicationDocumentsWrapperList(applicationDocumentsWrapperList);

		Optional<ApplicationWorkflow> workflow = applnWfRepository
				.findTopByAppIdAndApplicationIdAndVersionNumOrderByWorkflowSeqNumDesc(appId, applicationId, versionNum);

		if (workflow.isPresent()) {
			ApplicationWorkflow applnWf = workflow.get();
			List<WorkflowDefinition> wfDefnLis = wfDefnRepoLn.findByFromStageId(applnWf.getNextWorkFlowStage());
			loanFields.setApplnWfDefinitionList(wfDefnLis);
		}

		loanFields.setApplicationTimelineDtl(
				commonParamService.getApplicationTimelineDtl(applicationMasterData.getApplicationId()));

		return loanFields;
	}

	public void updateRelatedApplnIdDetails(ApplyLoanRequest apiRequest, String appId) {
		ApplyLoanRequestFields requestObj = apiRequest.getRequestObj();
		Optional<ApplicationMaster> appMasterObj = applicationMasterRepo
				.findTopByAppIdAndApplicationIdOrderByVersionNumDesc(appId, requestObj.getApplicationId());
		if (appMasterObj.isPresent()) {
			ApplicationMaster appMasterObjDb = appMasterObj.get();
			String relatedApplnId = appMasterObjDb.getRelatedApplicationId();
			if (!CommonUtils.isNullOrEmpty(relatedApplnId)) {
				Optional<ApplicationMaster> appMasterObjRelated = applicationMasterRepo
						.findTopByAppIdAndApplicationIdOrderByVersionNumDesc(appId, relatedApplnId);
				if (appMasterObj.isPresent()) {
					ApplicationMaster appMasterObjDbRelated = appMasterObjRelated.get();
					String[] arr = requestObj.getApplicationMaster().getCurrentScreenId().split("~");
					String currenctSrcId = arr[0];
					if ("Y".equalsIgnoreCase(arr[1])) {
						appMasterObjDbRelated.setCurrentScreenId(currenctSrcId);
						applicationMasterRepo.save(appMasterObjDbRelated);
					}
				}
			}
		}
	}

	public void duplicateLoanTablesETB(String appId, String applicationId, int newVersionNum, int oldVersionNum) {
		Optional<ApplicationMaster> appMasterForVersionCheck = applicationMasterRepo
				.findTopByAppIdAndApplicationIdOrderByVersionNumDesc(appId, applicationId);
		if (appMasterForVersionCheck.isPresent()) {
			BigDecimal newCustDtlId;
			ApplicationMaster appMaster = appMasterForVersionCheck.get();
			commonParamService.duplicateMasterData(appMaster, newVersionNum);
			duplicateLoanData(applicationId, appId, oldVersionNum, newVersionNum);
			List<CustomerDetails> custList = custDtlRepo.findByApplicationIdAndAppIdAndVersionNum(applicationId, appId,
					oldVersionNum);
			List<OccupationDetails> occupationDetailsList = occupationDtlRepo
					.findByApplicationIdAndAppIdAndVersionNum(applicationId, appId, oldVersionNum);
			for (CustomerDetails custObj : custList) {
				newCustDtlId = CommonUtils.generateRandomNum();
				commonParamService.duplicateCustomerData(custObj, newVersionNum, newCustDtlId);
				for (OccupationDetails occupationObj : occupationDetailsList) {
					commonParamService.duplicateOccupationData(occupationObj, newVersionNum, newCustDtlId);
				}
			}
		}
	}

	private void duplicateLoanData(String applicationId, String appId, int oldVersionNum, int newVersionNum) {
		LoanDetails loanDetails = loanDtlsRepo.findByApplicationIdAndAppIdAndVersionNum(applicationId, appId,
				oldVersionNum);
		if (null != loanDetails) {
			LoanDetails loanDetailsNew = new LoanDetails();
			BeanUtils.copyProperties(loanDetails, loanDetailsNew);
			loanDetailsNew.setVersionNum(newVersionNum);
			loanDetailsNew.setLoanDtlId(CommonUtils.generateRandomNum());
			loanDtlsRepo.save(loanDetailsNew);
		}
	}

	public void duplicateLoanTablesNTB(String appId, String applicationId, int newVersionNum, int oldVersionNum) {
		Optional<ApplicationMaster> appMasterForVersionCheck = applicationMasterRepo
				.findTopByAppIdAndApplicationIdOrderByVersionNumDesc(appId, applicationId);
		if (appMasterForVersionCheck.isPresent()) {
			ApplicationMaster appMaster = appMasterForVersionCheck.get();
			commonParamService.duplicateMasterData(appMaster, newVersionNum);
			duplicateLoanData(applicationId, appId, oldVersionNum, newVersionNum);
		}
	}

	public JSONArray fetchFunctionSeqArray(boolean isSelfOnBoardingHeaderAppId) {
		JSONArray array = null;
		if (isSelfOnBoardingHeaderAppId) {
			array = commonParamService.getJsonArrayForCmCodeAndKey(Constants.FUNCTIONSEQUENCE,
					CodeTypes.LOAN_ETB.getKey(), Constants.FUNCTIONSEQUENCE);
		} else {
			array = commonParamService.getJsonArrayForCmCodeAndKey(Constants.FUNCTIONSEQUENCE,
					CodeTypes.LOAN_BO_ETB.getKey(), Constants.FUNCTIONSEQUENCE);
		}
		return array;
	}

	@CircuitBreaker(name = "fallback", fallbackMethod = "validateKycFallback")
	public Mono<Object> validateKyc(ValidateKycRequest validateKycRequest, Header header, Properties prop) {
		String reqRefNo = CommonUtils.generateRandomNumStr();
		ValidateKycRequestExt validateKycRequestExt = new ValidateKycRequestExt();
		validateKycRequestExt.setAppId(validateKycRequest.getAppId());
		if (validateKycRequest.getRequestObj().getKycType().equals(Constants.VOTER)) {
			validateKycRequestExt
					.setInterfaceName(prop.getProperty(CobFlagsProperties.LOAN_VALIDATE_VOTER_ID_INTF.getKey()));
			ValidateVoterIdRequestFields voterIdRequestFields = new ValidateVoterIdRequestFields();
			voterIdRequestFields.setReqRefNo(reqRefNo);
			voterIdRequestFields.setKycId(validateKycRequest.getRequestObj().getKycId());
			validateKycRequestExt.setRequestObj(voterIdRequestFields);
		} else if (validateKycRequest.getRequestObj().getKycType().equals(Constants.PAN)) {
			validateKycRequestExt
					.setInterfaceName(prop.getProperty(CobFlagsProperties.LOAN_VALIDATE_PAN_INTF.getKey()));
			ValidatePanRequestFields panRequestFields = new ValidatePanRequestFields();
			panRequestFields.setReqRefNo(reqRefNo);
			panRequestFields.setKycId(validateKycRequest.getRequestObj().getKycId());
			validateKycRequestExt.setRequestObj(panRequestFields);
		} else if (validateKycRequest.getRequestObj().getKycType().equals(Constants.DRIVING_LICENSE)) {
			validateKycRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.LOAN_VALIDATE_DL_INTF.getKey()));
			ValidateDrivingLicenseRequestFields drivingLicenseRequestFields = new ValidateDrivingLicenseRequestFields();
			drivingLicenseRequestFields.setReqRefNo(reqRefNo);
			drivingLicenseRequestFields.setKycId(validateKycRequest.getRequestObj().getKycId());
			drivingLicenseRequestFields.setDob(validateKycRequest.getRequestObj().getDob());
			validateKycRequestExt.setRequestObj(drivingLicenseRequestFields);
		} else if (validateKycRequest.getRequestObj().getKycType().equals(Constants.PASSPORT)) {
			validateKycRequestExt
					.setInterfaceName(prop.getProperty(CobFlagsProperties.LOAN_VALIDATE_PASSPORT_INTF.getKey()));
			ValidatePassportRequestFields passportRequestFields = new ValidatePassportRequestFields();
			passportRequestFields.setReqRefNo(reqRefNo);
			passportRequestFields.setKycId(validateKycRequest.getRequestObj().getKycId());
			passportRequestFields.setDob(validateKycRequest.getRequestObj().getDob());
			validateKycRequestExt.setRequestObj(passportRequestFields);
		}
		//saveLog
		return interfaceAdapter.callExternalService(header, validateKycRequestExt,
				validateKycRequestExt.getInterfaceName());
	}

	@CircuitBreaker(name = "fallback", fallbackMethod = "kycDedupeFallback")
	public Mono<Object> kycDedupe(KycDedupeRequest kycDedupeRequest, Header header, Properties prop) {
		logger.debug("onEntry :: kycDedupe API Requsest: {} ", kycDedupeRequest.toString());
		JSONObject apiResponse = null;
		KycDedupeRequestExt kycDedupeRequestExt = new KycDedupeRequestExt();
		kycDedupeRequestExt.setAppId(kycDedupeRequest.getAppId());
		int count = -1;
		Optional<CustomerDetails> custDtl = Optional.empty();
		if (kycDedupeRequest.getRequestObj().getKycType().equalsIgnoreCase(Constants.VOTER)) {
			if (kycDedupeRequest.getRequestObj().getCustomerType() == 1) {
				if (StringUtils.isNotEmpty(kycDedupeRequest.getRequestObj().getApplicationId())) {
					List<String> applicationIds = new ArrayList<>();
					applicationIds.add(kycDedupeRequest.getRequestObj().getApplicationId());
					custDtl = custDtlRepo.findByPrimaryKycIdAndAlternateVoterIdAndApplicationIdNotIn(applicationIds,
							kycDedupeRequest.getRequestObj().getKycId(), kycDedupeRequest.getRequestObj().getKycId(),
							kycDedupeRequest.getRequestObj().getCustomerId());
				} else {
					custDtl = custDtlRepo.findByPrimaryKycIdAndAlternateVoterId(
							kycDedupeRequest.getRequestObj().getKycId(), kycDedupeRequest.getRequestObj().getKycId(),
							kycDedupeRequest.getRequestObj().getCustomerId());
				}
			} else if (kycDedupeRequest.getRequestObj().getCustomerType() == 2) {
				List<String> applicationIds = new ArrayList<>();
				applicationIds.add(kycDedupeRequest.getRequestObj().getApplicationId());
				custDtl = custDtlRepo.findByPrimaryKycIdAndAlternateVoterIdAndApplicationIdNotIn(applicationIds,
						kycDedupeRequest.getRequestObj().getKycId(), kycDedupeRequest.getRequestObj().getKycId(),
						kycDedupeRequest.getRequestObj().getCustomerId());
			}
			if (!custDtl.isPresent()) {
				kycDedupeRequestExt
						.setInterfaceName(prop.getProperty(CobFlagsProperties.LOAN_DEDUPE_VOTER_ID_INTF.getKey()));
				DedupeVoterIdRequestFields voterIdRequestFields = new DedupeVoterIdRequestFields();
				voterIdRequestFields.setGkv(prop.getProperty(CobFlagsProperties.LOAN_DEDUPE_VOTER_ID_GKV.getKey()));
				voterIdRequestFields
						.setMethod(prop.getProperty(CobFlagsProperties.LOAN_DEDUPE_VOTER_ID_METHOD.getKey()));
				voterIdRequestFields.setId(prop.getProperty(CobFlagsProperties.LOAN_DEDUPE_VOTER_ID_METHOD.getKey()));
				DedupeVoterIdLegalDocument legalDocument = new DedupeVoterIdLegalDocument();
				legalDocument.setId(kycDedupeRequest.getRequestObj().getKycId());
				voterIdRequestFields.setLegalDocument(legalDocument);
				kycDedupeRequestExt.setRequestObj(voterIdRequestFields);
				Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, kycDedupeRequestExt,
						kycDedupeRequestExt.getInterfaceName());
				return apiRespMono.flatMap(val -> {
					JSONObject resp = null;
					logger.debug("response from the API: {} ", val);
					JSONObject apiResp = new JSONObject(new Gson().toJson(val));
					logger.debug("JSON response from the API: {} ", apiResp);
					if (null != apiResp && apiResp.has("response")
							&& apiResp.getString("response").equalsIgnoreCase("Success")) {
						JSONArray result = apiResp.getJSONArray("result");
						if (kycDedupeRequest.getRequestObj().getCustomerType() == 1) {
							if (result.length() > 0 && result.getJSONObject(0).has("customer_id")) {
								for (Object jsonObj : result) {
									JSONObject json = (JSONObject) jsonObj;
									if (json.getString("customer_id")
											.equals(kycDedupeRequest.getRequestObj().getCustomerId())) {
										resp = adapterUtil.setSuccessResp(apiResp.toString());
									} else {
										logger.debug("External Dedupe match found");
										resp = adapterUtil
												.setError("Entered Voter Id is already mapped with other customer - "
														+ result.getJSONObject(0).get("customer_id"), "1");
										break;
									}
								}
							} else {
								resp = adapterUtil.setSuccessResp(apiResp.toString());
							}
						} else if (kycDedupeRequest.getRequestObj().getCustomerType() == 2) {
							if (result.length() > 0 && result.getJSONObject(0).has("customer_id")) {
								String coCustId = applicationMasterRepo
										.getCoAppCustId(kycDedupeRequest.getRequestObj().getCustomerId());
								for (Object jsonObj : result) {
									JSONObject json = (JSONObject) jsonObj;
									if(StringUtils.isEmpty(coCustId) || json.getString("customer_id").equals(coCustId)) {
										resp = adapterUtil.setSuccessResp(apiResp.toString());
									} else {
										logger.debug("External Dedupe match found");
										resp = adapterUtil
												.setError("Entered Voter Id is already mapped with other customer - "
														+ result.getJSONObject(0).get("customer_id"), "1");
										break;
									}
								}
							} else {
								resp = adapterUtil.setSuccessResp(apiResp.toString());
							}
						}
					} else {
						logger.error("error response from dedupe API. {}", apiResp);
						resp = adapterUtil.setError("error response from dedupe API.", "2");
						saveLog(kycDedupeRequest.getRequestObj().getApplicationId(), "VoterId Dedupe", kycDedupeRequestExt.toString(),
								apiResp.toString(), ResponseCodes.FAILURE.getValue(), apiResp.toString(), null);
					}
					return Mono.just(resp);
				});
			} else {
				logger.debug("Internal Dedupe match found");
				String applicationId = custDtl.get().getApplicationId();
				String custId = custDtl.get().getCustId();
				return Mono
						.just(adapterUtil.setError("Entered Voter Id is already mapped with other application/customer "+applicationId +"/"+ custId+".", "1"));
			}
		} else if (kycDedupeRequest.getRequestObj().getKycType().equalsIgnoreCase(Constants.MOBILE_NO)) {
			if (kycDedupeRequest.getRequestObj().getCustomerType() == 1) {
				count = custDtlRepo.countByMobileNoAndSecMobileNo(kycDedupeRequest.getRequestObj().getKycId(),
						kycDedupeRequest.getRequestObj().getKycId(), kycDedupeRequest.getRequestObj().getCustomerId());
			} else if (kycDedupeRequest.getRequestObj().getCustomerType() == 2) {
				List<String> applicationIds = new ArrayList<>();
				applicationIds.add(kycDedupeRequest.getRequestObj().getApplicationId());
				count = custDtlRepo.countByMobileNoAndSecMobileNoAndApplicationIdNotIn(applicationIds,
						kycDedupeRequest.getRequestObj().getKycId(), kycDedupeRequest.getRequestObj().getKycId(),
						kycDedupeRequest.getRequestObj().getCustomerId());
			}
			if (count == 0) {
				kycDedupeRequestExt
						.setInterfaceName(prop.getProperty(CobFlagsProperties.LOAN_DEDUPE_MOBILE_NO_INTF.getKey()));
				DedupeMobileNoRequestFields mobileNoRequestFields = new DedupeMobileNoRequestFields();
				mobileNoRequestFields.setGkv(prop.getProperty(CobFlagsProperties.LOAN_DEDUPE_VOTER_ID_GKV.getKey()));
				mobileNoRequestFields.setId(prop.getProperty(CobFlagsProperties.LOAN_DEDUPE_VOTER_ID_METHOD.getKey()));
				mobileNoRequestFields.setPhonenumber(kycDedupeRequest.getRequestObj().getKycId());
				kycDedupeRequestExt.setRequestObj(mobileNoRequestFields);
				Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, kycDedupeRequestExt,
						kycDedupeRequestExt.getInterfaceName());
				return apiRespMono.flatMap(val -> {
					JSONObject resp = null;
					JSONObject apiResp = new JSONObject(new Gson().toJson(val));
					if (null != apiResp && apiResp.has("response")
							&& apiResp.getString("response").equalsIgnoreCase("Success")) {
						JSONArray result = apiResp.getJSONArray("result");
						if (kycDedupeRequest.getRequestObj().getCustomerType() == 1) {
							if (result.length() > 0 && result.getJSONObject(0).has("customer_id")) {
								for (Object jsonObj : result) {
									JSONObject json = (JSONObject) jsonObj;
									if (json.getString("customer_id")
											.equals(kycDedupeRequest.getRequestObj().getCustomerId())) {
										resp = adapterUtil.setSuccessResp(apiResp.toString());
									} else {
										logger.debug("External Dedupe match found");
										resp = adapterUtil
												.setError("Entered Mobile number is already mapped with other customer - "
														+ result.getJSONObject(0).get("customer_id"), "1");
										break;
									}
								}
							} else {
								resp = adapterUtil.setSuccessResp(apiResp.toString());
							}
						} else if (kycDedupeRequest.getRequestObj().getCustomerType() == 2) {
							// Changed - 27-02-2024
							if (result.length() > 0 && result.getJSONObject(0).has("customer_id")) {
								String coCustId = applicationMasterRepo
										.getCoAppCustId(kycDedupeRequest.getRequestObj().getCustomerId());
								for (Object jsonObj : result) {
									JSONObject json = (JSONObject) jsonObj;
									if (StringUtils.isEmpty(coCustId) || json.getString("customer_id").equals(coCustId)) {
										resp = adapterUtil.setSuccessResp(apiResp.toString());
									} else {
										logger.debug("External Dedupe match found");
										resp = adapterUtil.setError(
												"Entered Mobile number is already mapped with other customer - "
														+ result.getJSONObject(0).get("customer_id"),
												"1");
										break;
									}
								}
							} else {
								resp = adapterUtil.setSuccessResp(apiResp.toString());
							}
						}
					} else {
						logger.error("error response from dedupe API. {}", apiResp);
						resp = adapterUtil.setError("error response from dedupe API.", "2");
						saveLog(kycDedupeRequest.getRequestObj().getApplicationId(), "MobileNo Dedupe ", kycDedupeRequestExt.toString(),
								apiResp.toString(), ResponseCodes.FAILURE.getValue(), apiResp.toString(), null);
					}
					return Mono.just(resp);
				});
			} else {
				logger.debug("Internal Dedupe match found");
				return Mono
						.just(adapterUtil.setError("Entered mobile no is already mapped with other application.", "1"));
			}

		} else if (kycDedupeRequest.getRequestObj().getKycType().equalsIgnoreCase(Constants.BANK_DETAILS)) {
			logger.debug("onEntry :: kycDedupe :: BANK_DETAILS");
			Optional<List<BankDetails>> bankDtlOpt = Optional.empty();
			BigDecimal custDtlId = null;

			logger.debug("Account No :" + kycDedupeRequest.getRequestObj().getKycId());
			if (kycDedupeRequest.getRequestObj().getCustomerType() == 1) {
				String custType = Constants.APPLICANT;
				Optional<CustomerDetails> customerDetails = custDtlRepo
						.findByApplicationIdAndAppIdAndCustomerType(kycDedupeRequest.getRequestObj().getApplicationId(), Constants.APPID, custType);

				if (customerDetails.isPresent()) {
					custDtlId = customerDetails.get().getCustDtlId();
					logger.debug("custDtlId : Applicant :: "+ custDtlId);
				}

				bankDtlOpt = bankDtlRepo.findByCustIdAndAccountNoAndApplicationIdNotIn(kycDedupeRequest.getRequestObj().getApplicationId(),
						kycDedupeRequest.getRequestObj().getKycId(), custDtlId, kycDedupeRequest.getRequestObj().getCustomerId());

			} else if (kycDedupeRequest.getRequestObj().getCustomerType() == 2) {
				String custType = Constants.COAPPLICANT;
				Optional<CustomerDetails> customerDetails = custDtlRepo
						.findByApplicationIdAndAppIdAndCustomerType(kycDedupeRequest.getRequestObj().getApplicationId(), Constants.APPID, custType);

				if (customerDetails.isPresent()) {
					custDtlId = customerDetails.get().getCustDtlId();
					logger.debug("custDtlId : Co-Applicant :: "+ custDtlId);
				}

				bankDtlOpt = bankDtlRepo.findByCustIdAndAccountNoAndApplicationIdNotIn(kycDedupeRequest.getRequestObj().getApplicationId(),
						kycDedupeRequest.getRequestObj().getKycId(), custDtlId, kycDedupeRequest.getRequestObj().getCustomerId());
			}

			List<BankDetails> bankDtlList = bankDtlOpt.orElse(Collections.emptyList());
			if (bankDtlList.isEmpty()) {
				logger.debug("bankDtl record not found!");
				kycDedupeRequestExt
						.setInterfaceName(prop.getProperty(CobFlagsProperties.LOAN_DEDUPE_BANK_INTF.getKey()));
				DedupeBankRequestFields bankRequestFields = new DedupeBankRequestFields();
				bankRequestFields.setGkv(prop.getProperty(CobFlagsProperties.LOAN_DEDUPE_BANK_ID_GKV.getKey()));
				bankRequestFields.setMethod(prop.getProperty(CobFlagsProperties.LOAN_DEDUPE_BANK_ID_METHOD.getKey()));
				bankRequestFields.setId(prop.getProperty(CobFlagsProperties.LOAN_DEDUPE_BANK_ID_ID.getKey()));
				bankRequestFields.setTypesrch(prop.getProperty(CobFlagsProperties.LOAN_DEDUPE_BANK_SEARCH_ID.getKey()));

				DedupeVoterIdLegalDocument legalDocumentParam = new DedupeVoterIdLegalDocument();
				legalDocumentParam.setId(kycDedupeRequest.getRequestObj().getKycId());
				bankRequestFields.setParam(legalDocumentParam);
				kycDedupeRequestExt.setRequestObj(bankRequestFields);

				logger.debug("F: {} ", kycDedupeRequestExt.toString());

				Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, kycDedupeRequestExt,
						kycDedupeRequestExt.getInterfaceName());
				return apiRespMono.flatMap(val -> {

					logger.debug("bankDedupeResponse 2 from the API: {} ", val);
					JSONObject resp = null;
					JSONObject apiResp = new JSONObject(new Gson().toJson(val));
					logger.debug("JSON response 3 from the API: {} ", apiResp);

					if (null != apiResp && apiResp.has("response")
							&& apiResp.getString("response").equalsIgnoreCase("Success")) {
						JSONArray result = apiResp.getJSONArray("result");
						if (kycDedupeRequest.getRequestObj().getCustomerType() == 1) {
							if (result.length() > 0 && result.getJSONObject(0).has("customer_id")) {
								for (Object jsonObj : result) {
									JSONObject json = (JSONObject) jsonObj;
									if (json.getString("customer_id")
											.equals(kycDedupeRequest.getRequestObj().getCustomerId())) {
										resp = adapterUtil.setSuccessResp(apiResp.toString());
									} else {
										logger.debug("External Dedupe match found");
										resp = adapterUtil
												.setError("Entered bank account number is already mapped with other customer - "
														+ result.getJSONObject(0).get("customer_id"), "1");
										break;
									}
								}
							} else {
								resp = adapterUtil.setSuccessResp(apiResp.toString());
							}
						} else if (kycDedupeRequest.getRequestObj().getCustomerType() == 2) {
							if (result.length() > 0 && result.getJSONObject(0).has("customer_id")) {
								String coCustId = applicationMasterRepo
										.getCoAppCustId(kycDedupeRequest.getRequestObj().getCustomerId());
								for (Object jsonObj : result) {
									JSONObject json = (JSONObject) jsonObj;
									if (StringUtils.isEmpty(coCustId) || json.getString("customer_id").equals(coCustId)) {
										resp = adapterUtil.setSuccessResp(apiResp.toString());
									} else {
										logger.debug("External Dedupe match found");
										resp = adapterUtil.setError(
												"Entered bank account number is already mapped with other customer - "
														+ result.getJSONObject(0).get("customer_id"),
												"1");
										break;
									}
								}
							} else {
								resp = adapterUtil.setSuccessResp(apiResp.toString());
							}
						}
					}else if(null != apiResp && apiResp.has("response") && apiResp.getString("response").equalsIgnoreCase(Constants.NO_RECORDS_FOUND)) {
						resp = adapterUtil.setSuccessResp(apiResp.toString());

//					} else if (apiResp != null && apiResp.has("response") && apiResp.has("status") && apiResp.has("status")) {
//						JSONArray result = apiResp.optJSONArray("result");
//						if (result.isEmpty() || (result.length() == 1 && result.getJSONObject(0).isEmpty()) && apiResp.getString("response").equalsIgnoreCase("No records available")) {
//							resp = adapterUtil.setSuccessResp(apiResp.toString());
//						}

					} else {
						logger.error("Error response from dedupe API. {}", apiResp);
						saveLog(kycDedupeRequest.getRequestObj().getApplicationId(), "Bank Dedupe", kycDedupeRequest.toString(),
								apiResp.toString(), ResponseCodes.FAILURE.getValue(), apiResp.toString(), "");
						resp = adapterUtil.setError("Error response from dedupe API.", "2");
					}
					return Mono.just(resp);
				});
			} else {
				logger.debug("Internal Dedupe Match Found!");
				if (bankDtlList.size() > 1) {
					logger.debug("Multiple Internal Dedupe Matches Found!");
//				        return Mono.just(adapterUtil.setError(
//				                "Multiple bank account matches found for given details.", "1"));
				}

				String mappedCustId = "";
				BankDetails matched = bankDtlList.get(0);
				logger.debug("Internal Dedupe match found for applicationId: {}", matched.getApplicationId());

				String applicationId = matched.getApplicationId();

				Optional<ApplicationMaster> applicationMasterOpt = applicationMasterRepo
						.findTopByAppIdAndApplicationIdOrderByVersionNumDesc(Constants.APPID, applicationId);
				if (applicationMasterOpt.isPresent()) {
					ApplicationMaster applicationMasterData = applicationMasterOpt.get();
					mappedCustId = applicationMasterData.getSearchCode2();
				}

				return Mono
						.just(adapterUtil.setError("Entered bank account Number is already mapped with other application/customer "+applicationId +"/"+ mappedCustId+".", "1"));

			}

		} else {
			logger.error("Invalid option");
			return Mono.just(adapterUtil.setError("Invalid option.", "1"));
		}
	}

	@CircuitBreaker(name = "fallback", fallbackMethod = "fetchIFSCFallback")
	public Mono<Object> fetchIFSC(FetchIFSCRequest fetchIFSCRequest, Header header, Properties prop) {
		try {
			FetchIFSCRequestExt fetchIFSCRequestExt = new FetchIFSCRequestExt();
			fetchIFSCRequestExt.setAppId(fetchIFSCRequest.getAppId());
			fetchIFSCRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.FETCH_IFSC_INTF.getKey()));
			IFSCFetchRequestFields fetchIFSCRequestFields = new IFSCFetchRequestFields();
			fetchIFSCRequestFields.setGkv(prop.getProperty(CobFlagsProperties.FETCH_IFSC_GKV.getKey()));
			fetchIFSCRequestFields.setMethod(prop.getProperty(CobFlagsProperties.FETCH_IFSC_METHOD.getKey()));
			fetchIFSCRequestFields.setId(prop.getProperty(CobFlagsProperties.FETCH_IFSC_ID.getKey()));
			fetchIFSCRequestFields.setIfsc(fetchIFSCRequest.getRequestObj().getIfsc());
			fetchIFSCRequestExt.setRequestObj(fetchIFSCRequestFields);
			logger.debug("request from the API: {} ", fetchIFSCRequestExt.toString());
			Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, fetchIFSCRequestExt,
					fetchIFSCRequestExt.getInterfaceName());
			return apiRespMono.flatMap(val -> {
				JSONObject resp = null;
				logger.debug("response from the API: {} ", val);
				JSONObject apiResp = new JSONObject(new Gson().toJson(val));
				logger.debug("JSON response from the API: {} ", apiResp);
				if (null != apiResp && apiResp.has("response")
						&& apiResp.getString("response").equalsIgnoreCase("Success")) {
					JSONArray result = apiResp.getJSONArray("result");
					resp = adapterUtil.setSuccessResp(apiResp.toString());
				} else {
					logger.error("error response from  fetch ifsc API. {}", apiResp);
					resp = adapterUtil.setError("error response from fetch ifsc API.", "2");
				}
				return Mono.just(resp);
			});
		} catch (Exception e) {
			logger.error("Exception occurred: " + e.getMessage());
			return Mono.just(adapterUtil.setError("Exception response from fetch ifsc API", "1"));
		}
	}

	@CircuitBreaker(name = "fallback", fallbackMethod = "fetchExistingLoanFallback")
	public Mono<Object> fetchExistingLoan(ExistingLoanRequest existingLoanRequest, Header header, Properties prop) {
		JSONObject soapApiResponse;
		String soapOutput = null;
		Gson gson = new Gson();
		try {
			List<ExistingGLLoanDetails> existingGLLoanDetails = existingGLLoanDetailsRepo
					.findByCustomerId(existingLoanRequest.getRequestObj().getMemberId());
			logger.debug("existingGLLoanDetails : " + existingGLLoanDetails.toString());
			logger.debug("Table retrieval is successful");
			soapOutput = gson.toJson(existingGLLoanDetails);
			soapApiResponse = adapterUtil.setSuccessResp(soapOutput);

		} catch (Exception e) {
			logger.error("Error occurred while executing the soap api, error = " + e.getMessage());
			soapApiResponse = adapterUtil.setError("Table retrieval failed", "4");
		}
		logger.debug("logging the request and response in db");

		logger.error("End : callService");

		return Mono.just(soapApiResponse);
	}


	@CircuitBreaker(name = "fallback", fallbackMethod = "breCBCheckFallback")
	public Mono<Object> breCBCheck(BRECBRequest brecbRequest, Header header, Properties prop) {
		String appnId = brecbRequest.getRequestObj().getBreCBValuesRequestvalues1().getBreCBInputRequestinput1().getBreCBValuesRequestvalues2().getBreCBInputRequestinput2().getApplicant().getAppId();
		try{
			final String loanId;
			final String applicantType;
			final String userId;

			logger.debug("request from the BRECBCheck API: {} ", brecbRequest.toString());
			Gson gson = new Gson();
			Map<String, Object> combinedRequest = new HashMap<>();
			combinedRequest.put("brecbRequest", brecbRequest);
			combinedRequest.put("header", header);
			String breCheckReq = gson.toJson(combinedRequest);
			logger.debug("Combined request JSON: {}", breCheckReq);

			BRECBCheckRequestExt CBCheckRequestExt = new BRECBCheckRequestExt();
			CBCheckRequestExt.setAppId(brecbRequest.getAppId());
			CBCheckRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.BRE_CB_CHECK_INTF.getKey()));
			BRECBRequestFields breRequest = brecbRequest.getRequestObj();
			BRECBInputRequest2 input = breRequest.getBreCBValuesRequestvalues1().getBreCBInputRequestinput1()
					.getBreCBValuesRequestvalues2().getBreCBInputRequestinput2();
			String loanAmount = input.getApplicant().getLoanAmount();

			if (StringUtils.isEmpty(loanAmount) || loanAmount.equalsIgnoreCase("NaN")) {
				LoanDetails loanDetails = loanDtlsRepo.findByApplicationId(input.getApplicant().getAppId());

				if (loanDetails != null) {
					BigDecimal appliedLoanAmount = loanDetails.getLoanAmount();
					BigDecimal bmRecommendedLoanAmount = loanDetails.getBmRecommendedLoanAmount();
					BigDecimal sanctionedLoanAmount = loanDetails.getSanctionedLoanAmount();

					BigDecimal minAmount = Stream.of(appliedLoanAmount, bmRecommendedLoanAmount, sanctionedLoanAmount)
							.filter(Objects::nonNull)
							.min(Comparator.naturalOrder())
							.orElse(BigDecimal.ZERO);

					// assign computed value to loanAmount
					loanAmount = minAmount.toPlainString();
				} else {
					loanAmount = "0";
				}
			}

			input.getApplicant().setLoanAmount(loanAmount);

			if (0 == input.getCoappFlag()) {
				loanId = "A01" + input.getApplicant().getAppId();
				applicantType = Constants.APPLICANT;
				userId = input.getApplicant().getDocumentDetails().get(0).getDocId();
			} else {
				loanId = "C01" + input.getApplicant().getAppId();
				applicantType = Constants.COAPPLICANT;

				userId = input.getHouseholdMember().get(0).getDocumentDetails().get(0).getDocId();
			}
			breRequest.getBreCBValuesRequestvalues1().getBreCBInputRequestinput1().getBreCBValuesRequestvalues2()
					.getBreCBInputRequestinput2().getApplicant().setLoanId(loanId);
			CBCheckRequestExt.setRequestObj(brecbRequest.getRequestObj());
			logger.debug("CBCheckRequestExt from the API: {} ", CBCheckRequestExt.toString());
			Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, CBCheckRequestExt,
					CBCheckRequestExt.getInterfaceName());

			// A
			String finalLoanAmount = loanAmount;
			return apiRespMono.flatMap(val -> {
				logger.debug("response 2 from the API: {} ", val);
				JSONObject resp = null;
				JSONObject apiResp = new JSONObject(new Gson().toJson(val));
				logger.debug("JSON response 3 from the API: {} ", apiResp);

				CibilDetails cblDetails = new CibilDetails();
				CibilDetailsPayload cblPayLoad = new CibilDetailsPayload();
				// response persistence
				try {
					// Fetch custDtlId from CustomerDetails table - by - applicationId and
					// customerType
					Optional<CustomerDetails> customerDetails = custDtlRepo
							.findByApplicationIdAndCustomerType(input.getApplicant().getAppId(), applicantType);
					if (customerDetails.isPresent()) {
						cblDetails.setCustDtlId(customerDetails.get().getCustDtlId());
					} else {
						logger.debug("No customer details found for the given applicationId and customerType.");
						return Mono.just(adapterUtil.setError(
								"No customer details found for the given applicationId and customerType", "1"));
					}
					int previousRetryAttempt = 0;
					// check for existing record
					Optional<CibilDetails> cblDetailsExtg = cibilDtlRepo.findByApplicationIdAndAppIdAndCustDtlId(
							input.getApplicant().getAppId(), brecbRequest.getAppId(),
							customerDetails.get().getCustDtlId());

					if (cblDetailsExtg.isPresent()) {
						/*
						 * If present delete the original record from CibilDetails and move it to
						 * CibilDetailsHistory table and then insert as new record in CibilDetails
						 */

						CibilDetails cibilDetails = cblDetailsExtg.get();
						previousRetryAttempt = gson.fromJson(cibilDetails.getPayloadColumn(), CibilDetailsPayload.class).getRetryAttempts();

						// Create a new instance of CibilDetailsHistory
						CibilDetailsHistory cblHistory = new CibilDetailsHistory();

						// Map fields from CibilDetails to CibilDetailsHistory
						cblHistory.setCbDtlId(cibilDetails.getCbDtlId());
						cblHistory.setApplicationId(cibilDetails.getApplicationId());
						cblHistory.setAppId(cibilDetails.getAppId());
						cblHistory.setVersionNum(cibilDetails.getVersionNum());
						cblHistory.setCustDtlId(cibilDetails.getCustDtlId());
						cblHistory.setRequestId(cibilDetails.getRequestId());
						cblHistory.setResponseId(cibilDetails.getResponseId());
						cblHistory.setCbDate(cibilDetails.getCbDate());
						cblHistory.setCbStatus(cibilDetails.getCbStatus());
						cblHistory.setAdditionalInfo(cibilDetails.getAdditionalInfo());
						cblHistory.setPayloadColumn(cibilDetails.getPayloadColumn());

						// Save the history record
						cibilDtlHisRepo.save(cblHistory);

						String applicationNum = input.getApplicant().getAppId();
						String fileName = loanId + ".pdf";
						String fileLocation = prop.getProperty(CobFlagsProperties.FILE_UPLOAD.getKey()) + "/"
								+ applicationNum + Constants.LOANPATH + applicationNum + "/";
						String filePath = fileLocation + fileName;
						File file = new File(filePath);

						if (file.exists()) {
							try {
								Path path = Paths.get(filePath);
								Files.delete(path);
								logger.info("file deleted successfully in path: {}", filePath);
							} catch (NoSuchFileException e) {
								logger.error("No such file in the path: " + e.getMessage());
							} catch (IOException e) {
								logger.error("Error while deleting the file : " + e.getMessage());
							}
						}

						logger.info("Moved record from CibilDetails to CibilDetailsHistory successfully.");
					} else {
						logger.warn("No record found in CibilDetails for the given criteria.");
						// insert as new record in CibilDetails
					}

					// insert as new record in CibilDetails
					BigDecimal cbDtlId = CommonUtils.generateRandomNum();
					cblDetails.setCbDtlId(cbDtlId);

					cblDetails.setAppId(brecbRequest.getAppId()); // APZCOB
					cblDetails.setVersionNum(1);
					cblDetails.setApplicationId(input.getApplicant().getAppId()); // 77777777

//					cblDetails.setCbDate(
//							CommonUtils.convertStringToLocalDate(apiResp.getString("request_Date"), "yyyy-MM-dd"));
					cblDetails.setCbDate(LocalDate.now());

					JSONObject addInfo = new JSONObject();
					if(StringUtils.isNotEmpty(input.getCaglOs())){
						addInfo.put("caglOs", input.getCaglOs());
					}
					if(StringUtils.isNotEmpty(input.getCurrentStage())){
						String stage_subStage = input.getCurrentStage();
						String[] stageSubStageArr = stage_subStage.split("\\|");
						String stage = stageSubStageArr[0].trim();
						String subStage = stageSubStageArr[1].trim();
						addInfo.put("stage", stage);
						addInfo.put("subStage", subStage);
					}
					cblDetails.setAdditionalInfo(gson.toJson(addInfo));

					// Set status
					if (apiResp.getString("Final_Decision").toLowerCase().indexOf("approved".toLowerCase()) != -1) {
						cblDetails.setCbStatus("PASS"); // Pass case
					} else { // Reject case
						cblDetails.setCbStatus("FAIL");
						cblPayLoad.setRejectionReason(apiResp.optString("Rejection_reason"));
					}
//					// delete the original record from CibilDetails
//					cibilDtlRepo.deleteByApplicationIdAndAppIdAndCustDtlId(input.getApplicant().getAppId(),
//							brecbRequest.getAppId(), customerDetails.get().getCustDtlId());
					cblPayLoad.setFlowResponse(apiResp.optString("flow_response"));

					cblPayLoad.setIrisMessage(apiResp.getString("IRIS_message"));
					String normalizedIrisMsg = cblPayLoad.getIrisMessage()
							.replaceAll("[–—]", "-") // replace en-dash/em-dash with hyphen
							.toLowerCase();
					String irisTempErrorMsgs = prop.getProperty(CobFlagsProperties.BRE_CHECK_TEMP_IRIS_ERRORS.getKey());
					String[] irisTempErrorMsgsIterable = irisTempErrorMsgs.toLowerCase().split(",");
					cblPayLoad.setRetryAttempts(0);
					int maxRetries = Integer.parseInt(
							prop.getProperty(CobFlagsProperties.BRE_RETRY_ATTEMPT.getKey())
					);
					for (String irisError : irisTempErrorMsgsIterable) {
						if (normalizedIrisMsg.equalsIgnoreCase(irisError)) {

							int retryCount;

							if (previousRetryAttempt == 0) {
								// case 1: first time, initialize from config
								retryCount = maxRetries;
							} else {
								// case 2: subsequent calls, decrement
								retryCount = Math.max(0, previousRetryAttempt - 1);
							}

							cblPayLoad.setRetryAttempts(retryCount);
							cblPayLoad.setEligibleAmt(finalLoanAmount);
							cblPayLoad.setRejectionReason(cblPayLoad.getRejectionReason() + " | BRE Error message: " + cblPayLoad.getIrisMessage());
							break; // exit loop once matched
						}
					}
					String irisRetryErrorMsgs = prop.getProperty(CobFlagsProperties.BRE_CHECK_RETRY_IRIS_ERRORS.getKey());
					String[] irisRetryErrorMsgsIterable = irisRetryErrorMsgs.toLowerCase().split(",");
					for (String irisErrorPattern : irisRetryErrorMsgsIterable) {
						if (normalizedIrisMsg.contains(irisErrorPattern)) {
							cblPayLoad.setRetryAttempts(maxRetries);
							cblPayLoad.setEligibleAmt(finalLoanAmount);
							cblPayLoad.setRejectionReason(cblPayLoad.getRejectionReason() + " | BRE Error Message: " + cblPayLoad.getIrisMessage());
							break;
						}
					}

					cblPayLoad.setFinalDecision(apiResp.getString("Final_Decision"));

					cblPayLoad.setCbLoanId(loanId);
					cblPayLoad.setAppliedLoanCode(apiResp.getString("applied_loan_code"));

//					String foir = String.valueOf((apiResp.getString("FOIR").split(":")[1])).split("\\|")[0].trim().split(",")[0].trim();			
					String foir = extractValue(apiResp.getString("FOIR"));
					cblPayLoad.setFoir(foir);
					String foirPercentage = String.valueOf(apiResp.getString("FOIR").split(",")[1].split("%")[0].trim());
					cblPayLoad.setFoirPercentage(foirPercentage);
					cblPayLoad.setApprovedLoanEMI(String.valueOf(apiResp.optInt("Installment_amt")));
					cblPayLoad.setFinalTenure(apiResp.getString("Final_Tenure"));
					String finalTenure = cblPayLoad.getFinalTenure();
					try {
						int tenure = Integer.parseInt(finalTenure);
						loanDtlsRepo.updateTenure(input.getApplicant().getAppId(), tenure);
					} catch (Exception e) {
						// ignore invalid or null values
						logger.debug("{} : tenue is not a number", finalTenure);
					}
					cblPayLoad.setEligibleAmt(String.valueOf(apiResp.opt("Approved_Loan_Amount")));
					if(cblPayLoad.getRetryAttempts()>0){
						cblPayLoad.setEligibleAmt(finalLoanAmount);
					}
					cblPayLoad.setEligibleEMI(apiResp.getString("Eligible_EMI"));

					if (0 == input.getCoappFlag()) { // Applicant
						cblPayLoad.setOverdueAmt(apiResp.getString("applicant_overdue_amount"));
						cblPayLoad.setWriteOffAmt(apiResp.getString("applicant_Write_Off_Amount"));
						cblPayLoad.setWriteoffSuitFiledFlag(apiResp.getString("applicant_Writeoff_Suit_filed_Flag"));
//						cblPayLoad.setTotIndebtness(apiResp.getString("applicant_Indebtedness"));
						cblPayLoad.setIndividualIndebtness(apiResp.getString("applicant_Indebtedness"));

//						String score = String.valueOf((apiResp.getString("score").split(":")[1])).split("\\|")[0].trim();
						String score = extractValue(apiResp.getString("score"));// "Score : 650 | Pass"
						cblPayLoad.setCbScore(score);

					} else { // Co-Applicant
						cblPayLoad.setOverdueAmt(apiResp.getString("co_applicant_Overdue_Amount"));
						cblPayLoad.setWriteOffAmt(apiResp.getString("co_applicant_Write_Off_Amount"));
						cblPayLoad.setWriteoffSuitFiledFlag(apiResp.getString("co_applicant_Writeoff_Suit_filed_Flag"));
//						cblPayLoad.setTotIndebtness(apiResp.getString("co_applicant_Indebtedness"));
						cblPayLoad.setIndividualIndebtness(apiResp.getString("co_applicant_Indebtedness"));

//						String coApptScore = String.valueOf((apiResp.getString("Score").split(":")[1])).split("\\|")[0].trim();
						String coApptScore = extractValue(apiResp.getString("Score"));
						cblPayLoad.setCbScore(coApptScore);
					}

					cblPayLoad.setTotIndebtness(apiResp.getString("Indebtedness"));

					cblPayLoad.setOtsFlag(apiResp.getString("OTS_flag"));
					cblPayLoad.setCaglDpdFlag(apiResp.getString("CAGL_DPD_Flag"));
					cblPayLoad.setCaglUnnatiFlag(apiResp.getString("CAGL_Unnati_Flag"));
					cblPayLoad.setEir(apiResp.getString("EIR"));
					cblPayLoad.setRoi(apiResp.getString("ROI"));
//					cblPayLoad.setProcessingFees(apiResp.getString("Processing_fees"));
					// Updated - Jun 03
					double processingFeesRaw = apiResp.optDouble("Processing_fees_incl_GST", 0.0);
					BigDecimal processingFees = BigDecimal.valueOf(processingFeesRaw)
							.setScale(0, RoundingMode.HALF_UP);
					cblPayLoad.setProcessingFees(processingFees.toPlainString());
					cblPayLoad.setInsuranceChargeMember(String.valueOf(apiResp.optInt("Insurance_charge_Member")));
					cblPayLoad.setInsuranceChargeSpouse(String.valueOf(apiResp.optInt("Insurance_charge_Spouse")));
					cblPayLoad.setInsuranceChargeJoint(String.valueOf(apiResp.optInt("Insurance_charge_Joint")));
					cblPayLoad.setStampDutyCharge(String.valueOf(apiResp.optInt("stamp_duty")));
					cblPayLoad.setRepaymentFrequency(String.valueOf(apiResp.opt("Repayment_frequency")));

					cblPayLoad.setMemberId(input.getApplicant().getCustId());
					cblPayLoad.setKycId(userId);
					cblPayLoad.setBureauName("BRE");
					// Updated - Jun 03
					cblPayLoad.setAppIndebtednessLimit(String.valueOf(apiResp.opt("app_indebtedness_limit")));
					cblPayLoad.setAppMaxLoanLimit(String.valueOf(apiResp.opt("app_max_loan_limit")));
					cblPayLoad.setCoappIndebtednessLimit(String.valueOf(apiResp.opt("coapp_indebtedness_limit")));
					cblPayLoad.setCoappMaxLoanLimit(String.valueOf(apiResp.opt("coapp_max_loan_limit")));
					cblPayLoad.setApplicantIndebtedness(extractValue(apiResp.getString("applicant_Indebtedness")));
					cblPayLoad.setCoApplicantIndebtedness(extractValue(apiResp.getString("co_applicant_Indebtedness")));

					String payload = new Gson().toJson(cblPayLoad);
					cblDetails.setPayloadColumn(payload);
					cblDetails.setRequest(breCheckReq);
					logger.debug("Cibil Details record: {}" , cblDetails);
					Optional<CibilDetails> existing =
							cibilDtlRepo.findByApplicationIdAndCustDtlId(cblDetails.getApplicationId(), cblDetails.getCustDtlId());
					existing.ifPresent(cibilDtlRepo::delete);
					cibilDtlRepo.save(cblDetails);
					logger.debug("Data inserted" + cblDetails.toString());

				} catch (Exception ex) {
					logger.error("Error occurred while executing the BRE details, error = {}" , ex.getMessage(), ex);
					saveLog(input.getApplicant().getAppId(), "breCBCheck", CBCheckRequestExt.toString(),
							ex.getMessage() , ResponseCodes.FAILURE.getValue(), ex.getMessage(), "");
					return Mono.just(adapterUtil.setError("Error occurred while executing the BRE api.", "1"));
				}

				// call report method
				BRECBReportRequest brecbReportRequest = new BRECBReportRequest();
				brecbReportRequest.setLoanId(loanId);
				brecbReportRequest.setAppId(brecbRequest.getAppId()); // APZCOB

				BRECBReportRequestFields reqRptObj = new BRECBReportRequestFields();
				reqRptObj.setCustomerId(input.getApplicant().getCustId());
				reqRptObj.setUserId(userId);
				brecbReportRequest.setRequestObj(reqRptObj);

				Mono<Object> apiReportRespMono = breCBReport(brecbReportRequest, header, prop, false);
				logger.debug("apiReportResp" + apiRespMono);

				return apiReportRespMono.flatMap(val1 -> {
					logger.debug("response 3 from the report API: {} ", val1);
					JSONObject apiReportResp = new JSONObject(new Gson().toJson(val1)).getJSONObject("map");

					// Create a combined response
					String jsonString = new Gson().toJson(cblDetails);
					JSONObject combinedResp = adapterUtil.setSuccessResp(jsonString);
					logger.debug("combinedResp1" + combinedResp);
					combinedResp.put("responseObj", new JSONObject(combinedResp.getString("responseObj")));
					logger.debug("breCbSuccessResonse :" + combinedResp);
					logger.debug("apiReportResp.toString() : " + apiReportResp.toString());
//		            Log.debug("breCbSuccessResonse type :"+ combinedResp.getClass());
					if (apiReportResp.has("base64")) {
						combinedResp.put("base64", apiReportResp.getString("base64"));
					}
					if (apiReportResp.has("filePath")) {
						combinedResp.put("filePath", apiReportResp.getString("filePath"));
					}
					logger.error("breFinalResonse :" + combinedResp);
					logger.debug("reponse type :" + combinedResp.getClass());
					return Mono.<Object>just(combinedResp);   // Mono<Object>
				});

			}).cache();

		} catch (Exception e) {
			logger.error("Error occurred while executing the BRE api, error = {}", e.getMessage(),e);
			saveLog(appnId, "breCBCheck", brecbRequest.toString(),
					e.getMessage() , ResponseCodes.FAILURE.getValue(), e.getMessage(), "");
			return Mono.just(adapterUtil.setError("Error occurred while executing the BRE api.", "1"));
		}
	}

	private void saveLog(String applicationId, String stepName, String request, String response, String status,
						 String errorMsg, String currentStage) {
		ApiExecutionLog log = new ApiExecutionLog();
		log.setApplicationId(applicationId);
		log.setApiName(stepName);
		log.setRequestPayload(request);
		log.setResponsePayload(response);
		log.setApiStatus(status);
		log.setErrorMessage(errorMsg);
		log.setCreateTs(LocalDateTime.now());
		log.setCurrentStage(currentStage);
		logRepository.save(log);
	}

	public String findCategoryId(String subPurposeObj, String searchValue, Properties prop) {

		try {
			JSONObject js = new JSONObject(subPurposeObj);

			Map<String, String> reverseLookupMap = new HashMap<>();
			JSONObject loanSubPurpose = js.getJSONObject("Loan Sub Purpose");
			Iterator<String> keys = loanSubPurpose.keys();

			while (keys.hasNext()) {
				String category = keys.next();
				JSONArray items = loanSubPurpose.getJSONArray(category);

				for (int i = 0; i < items.length(); i++) {
					String value = items.getString(i);

					String normalizedValue = normalizeString(value);
					reverseLookupMap.put(normalizedValue, category);
				}
			}

			String purposeId = prop.getProperty(CobFlagsProperties.LOANPURPOSE_IDENTIFIER.getKey());
			Map<String, String> categoryIdMap = new HashMap<>();
			JSONObject json = new JSONObject(purposeId);
			for (String key : json.keySet()) {
				categoryIdMap.put(key, json.getString(key));
			}

			logger.debug("Category Id "+ categoryIdMap);
			String normalizedSearchValue = normalizeString(searchValue);

			String category = reverseLookupMap.get(normalizedSearchValue);
			if (category != null) {
				return categoryIdMap.getOrDefault(category, "01");
			} else {
				return "Value Not Found";
			}
		} catch (Exception e) {
			logger.error("Exception in findCategory ID ",e);
		}
		return "Value not Found";
	}

	private static String normalizeString(String input) {
		if (input == null) {
			return "";
		}
		return input.replace("_", "")
				.replace(".", "")
				.toUpperCase()
				.trim();
	}

	public Mono<Object> loanCreation(String applicationId, String appId, String memberId, Header header,
									 Properties prop, String coApplicantId) {

		logger.debug("Loan Creation API Started for ID :" +	coApplicantId);

		CustomerDetailsPayload appPayload = null;
		CustomerDetailsPayload coappPayload =null;
		CibilDetailsPayload cibilPayload = null;
		String applicantCustId="";
		String coApplicantCustId = "";
		Gson gson = new Gson();
		String nomineeName = "";
		String nomineeRelation = "";
		String cbDateStr = "";
		String coAppFoir = "";
		String familyIncomeStr = "";
		String earningMembers = "";
		String annualPercentageRate = "";
		Boolean insuranceRequiredApplicant = false;
		Boolean insuranceRequiredCoapplicant = false;
		Gson gsonObj = new Gson();
		try {

			ApplyLoanRequestFields custFields = null;
			ApplicationMaster applicationMasterData = null;
			List<ApplicationMaster> appMasterDb = applicationMasterRepo
					.findByAppIdAndApplicationId(Constants.APPID,applicationId);

			if (null != appMasterDb && !appMasterDb.isEmpty()) {
				for (ApplicationMaster appMaster : appMasterDb) {
					applicationMasterData = appMaster;
				}
				String product = applicationMasterData.getProductCode();
				logger.debug("product :" + product);
				custFields = getCustomerData(applicationMasterData, applicationId, Constants.APPID, 1);
			}

			for (CustomerDetails custDtl : custFields.getCustomerDetailsList()) {
				logger.debug("customer Type : " + custDtl.getCustomerType());
				if (custDtl.getCustomerType().equalsIgnoreCase("Applicant")) {
					applicantCustId = String.valueOf(custDtl.getCustDtlId());
					logger.debug("applicantCustId : " + applicantCustId);
					appPayload = gsonObj.fromJson(custDtl.getPayloadColumn(), CustomerDetailsPayload.class);
					logger.debug("custApplicantPayload :" + appPayload);
				} else if (custDtl.getCustomerType().equalsIgnoreCase(Constants.COAPPLICANT)) {
					coApplicantCustId = String.valueOf(custDtl.getCustDtlId());
					logger.debug("coApplicantCustId : " + applicantCustId);
					coappPayload = gsonObj.fromJson(custDtl.getPayloadColumn(), CustomerDetailsPayload.class);
					logger.debug("custCo-ApplicantPayload :" + coappPayload);
				}
			}
			for (InsuranceDetailsWrapper wrapper : custFields.getInsuranceDetailsWrapperList()) {
				String custId = String.valueOf(wrapper.getInsuranceDetails().getCustDtlId());
				logger.debug("InsuranceDetailsWrapper CustId : {}", custId);

				InsuranceDetailsPayload payload = gsonObj.fromJson(
						wrapper.getInsuranceDetails().getPayloadColumn(),
						InsuranceDetailsPayload.class
				);
				logger.debug("insurancePayload Payload : {}", payload);

				String insuranceOption = payload != null ? payload.getInsuranceOption() : null;
				String insuranceReqd = payload != null ? payload.getInsuranceReqd() : null;

				List<String> insuranceEnableOptions = Arrays.asList(
						Constants.BOTH_INSURANCE_OPTION,
						Constants.JOINT_INSURANCE_OPTION
				);

				if (insuranceOption != null && !insuranceOption.trim().equalsIgnoreCase(Constants.NO_INSURANCE_OPTION)) {
					String normalizedOption = insuranceOption.trim().toUpperCase();

					boolean isInsuranceYes = Constants.YES.equalsIgnoreCase(insuranceReqd);
					boolean isEnabledOption = insuranceEnableOptions.contains(normalizedOption);

					if (custId.equals(applicantCustId)) {
						logger.debug("Matched applicantCustId with InsuranceDetailsWrapper CustId");
						if ((isEnabledOption || normalizedOption.equalsIgnoreCase(Constants.APPLICANT_INSURANCE_OPTION))
								&& isInsuranceYes) {
							insuranceRequiredApplicant = true;
						}
						logger.debug("applicant insuranceReqd: '{}'", insuranceRequiredApplicant);

					} else if (custId.equals(coApplicantCustId)) {
						logger.debug("Matched coApplicantCustId with InsuranceDetailsWrapper CustId");
						if (isEnabledOption && isInsuranceYes) {
							insuranceRequiredCoapplicant = true;
						}
						logger.debug("coapplicant insuranceReqd: '{}'", insuranceRequiredCoapplicant);

					} else {
						logger.debug("New Nominee added");
						nomineeName = payload.getNomineeName();
						nomineeRelation = payload.getNomineeRelation();
						logger.debug("Nominee Name: '{}', Relation: '{}'", nomineeName, nomineeRelation);
					}
				} else {
					logger.debug("No Insurance opted");
				}
			}

			for (CibilDetailsWrapper cibilDetailsWrapper : custFields.getCibilDetailsWrapperList()) {
				String custId = cibilDetailsWrapper.getCibilDetails().getCustDtlId().toString();
				logger.debug("CreditDetailsPayload Payload : " + cibilPayload);
				if (custId.equals(coApplicantCustId)) {
					cibilPayload = gsonObj.fromJson(
							cibilDetailsWrapper.getCibilDetails().getPayloadColumn(), CibilDetailsPayload.class);
					LocalDate cbDate = cibilDetailsWrapper.getCibilDetails().getCbDate();
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
					cbDateStr = cbDate.format(formatter);
					coAppFoir = cibilPayload.getFoir();
				}
			}
//			String roi = String.valueOf(custFields.getLoanDetails().getRoi());
			LoanCreationReqFields loanRequest = new LoanCreationReqFields();


			String borrowerInsurance = insuranceRequiredApplicant ? "YES" : "NO";
			String jointInsurance = insuranceRequiredApplicant
					&& insuranceRequiredCoapplicant ? "YES" : "NO";

			logger.debug("Computed borrowerInsurance: '{}'", borrowerInsurance);
			logger.debug("Computed jointInsurance: '{}'", jointInsurance);

			LoanDetails loanDetails = custFields.getLoanDetails();
			Integer year = loanDetails.getTenure() / 12;
			String yearInWeeks = (year >= 1 && year <= 3) ? (year * 52) + Constants.TERM_WEEK: "";

			LoanDetailsPayload loanPayload = gson.fromJson(loanDetails.getPayloadColumn(), LoanDetailsPayload.class);

			Optional<LovMaster> subPurposeId = lovMasterRepository.findById(loanDetailsLovId);
			logger.debug("Subpurpose Id :"+ subPurposeId.get().getLovDtls().toString());
			String purposeId = findCategoryId(subPurposeId.get().getLovDtls().toString(), loanPayload.getSubCategory().replace("_", "."), prop);

			// As per latest discussion - 19/05/2025
			Map <String, Integer> coCus = new HashMap<>();
			coCus.put("ltUnniCoCus", Integer.parseInt(coApplicantId));
			List<Map<String, Integer>> unnatiCoCustomerList = new ArrayList<>();
			unnatiCoCustomerList.add(coCus);

			loanRequest.setApplicantId(memberId);
			loanRequest.setUnnatiCoCustomer(unnatiCoCustomerList);
			loanRequest.setProduct(Constants.LOAN_PRODUCT);
			loanRequest.setCurrency(Constants.CURRENCY_INR); // Default value
			loanRequest.setTerm(yearInWeeks);
			loanRequest.setAmount(loanDetails.getSanctionedLoanAmount());
			loanRequest.setIntRate(cibilPayload.getRoi());
			String frequency = loanPayload.getFrequencyOfRepayment();
			String normalized = frequency.toUpperCase().trim();
// collapse multiple spaces or dashes into one dash
			normalized = normalized.replaceAll("[\\s\\-]+", "-");
// if it looks like any variant of BI-WEEKLY
			Matcher m = Constants.BI_WEEKLY_PATTERN.matcher(normalized);
			if (m.find()) {
				normalized = "BI-WEEKLY";
			}
			loanRequest.setFrequency(normalized);
			loanRequest.setDisburseMode(loanPayload.getModeOfDisbursement());
			loanRequest.setPurpose(purposeId);
			loanRequest.setSubPurpose(loanPayload.getSubCategory().replaceAll("[^A-Za-z0-9]+", "."));
			loanRequest.setNomnieeName(nomineeName);
			loanRequest.setNomineeRelation(nomineeRelation);
			loanRequest.setNomineePhone(""); // As per API Doc
			loanRequest.setCbResponseDate(cbDateStr);
			loanRequest.setCbRemarks("");
			loanRequest.setBorrowerInsurance(borrowerInsurance);
			loanRequest.setJointOwnerOrCoborrowerInsurance(jointInsurance);
			loanRequest.setSystemApplicationId(custFields.getApplicationId());
			loanRequest.setFoir(coAppFoir);
			if (applicationMasterData.getProductCode().equalsIgnoreCase(Constants.UNNATI_PRODUCT_CODE)) {
				loanRequest.setPreCloseType(prop.getProperty(CobFlagsProperties.UNNATI_PRE_CLOSE_TYPE.getKey())); // default for Unnati loan
			} else if (applicationMasterData.getProductCode().equalsIgnoreCase(Constants.RENEWAL_LOAN_PRODUCT_CODE)) {
				loanRequest.setPreCloseType(prop.getProperty(CobFlagsProperties.RENEWAL_PRE_CLOSE_TYPE.getKey()));
			} else {
				return Mono.just(adapterUtil.setError("Error occurred while executing the Loan Creation api. Unknown product code :" + applicationMasterData.getProductCode(), "1"));
			}
			loanRequest.setPayoffAccount(""); // NA
			loanRequest.setCompanyIdTemp(applicationMasterData.getBranchId());
			String eir = cibilPayload.getEir();
			if (eir != null) {
				annualPercentageRate = eir.replace("%", "").trim();
			}

			loanRequest.setAnnualPercentageRate(annualPercentageRate);


			Optional<BCMPIIncomeDetails> incomeDetails = bcmpiIncomeDetailsRepo.findById(applicationId);
			if(incomeDetails.isPresent()){
				BCMPIIncomeDetailsWrapper incomeDetailsWrapper = gson.fromJson(incomeDetails.get().getPayload(), BCMPIIncomeDetailsWrapper.class);
				BigDecimal fieldAssessedIncome = incomeDetailsWrapper.getFieldAssessedIncome();
				BigDecimal selfDeclaredIncome = incomeDetailsWrapper.getTotalDeclaredIncome();
				BigDecimal familyIncome = fieldAssessedIncome.min(selfDeclaredIncome);
				familyIncomeStr = String.valueOf(familyIncome);
			}else{
				logger.debug("Income details not present for application_id : {}", applicationId);
				return Mono.just(adapterUtil.setError("Income details not present for application Id : "+applicationId, "1"));
			}
			loanRequest.setFamilyIncome(familyIncomeStr);

			Optional<BCMPIOtherDetails> caOtherDetails = bcmpiOtherDetailsRepo.findById(applicationId);
			if(caOtherDetails.isPresent()){
				BCMPIOtherDetailsWrapper otherDetailsWrapper= gson.fromJson(caOtherDetails.get().getPayload(), BCMPIOtherDetailsWrapper.class);
				earningMembers = otherDetailsWrapper.getNoOfOtherEarningMembers();

			}
			loanRequest.setEarningMembers(earningMembers);
			Map<String, LoanCreationReqFields> mapReq = new HashMap<>();
			mapReq.put("body", loanRequest);
			logger.debug("JSON request for loan creation api :" + mapReq);

			LoanRequestExt loanCreationRequestExt = new LoanRequestExt();
			loanCreationRequestExt.setAppId(appId);
			loanCreationRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.LOAN_CREATION_INTF.getKey()));
			loanCreationRequestExt.setRequestObj(mapReq);
			logger.debug("Loan Creation from the API: {} ", loanCreationRequestExt.toString());

			setRequestLog(loanCreationRequestExt.toString());

			Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header , loanCreationRequestExt,
					loanCreationRequestExt.getInterfaceName());

			logger.debug("response 1 from the API1: {} ", apiRespMono);

			/*adapterUtil.generateResponseWrapper(apiRespMono,
					"LoanCreation", header);*/

			return apiRespMono.flatMap(val -> {
				logger.debug("response 3 from the report API: {} ", val);
				JSONObject apiReportResp = new JSONObject(new Gson().toJson(val));
				return Mono.just(apiReportResp);
			});

		} catch (Exception e) {
			logger.error("Error occurred while executing the Loan Creation api", e);
			return Mono.just(adapterUtil.setError("Error occurred while executing the Loan Creation api.", "1"));
		}
	}


	public Mono<Object> loanRejection(String applicationId, Properties prop, String appId, Header header) {
		logger.debug("Loan Rejection API started");
		try {
			ApplicationMaster applicationMasterData = null;
			List<ApplicationMaster> appMasterDb = applicationMasterRepo
					.findByAppIdAndApplicationId(Constants.APPID,applicationId);

			if (null != appMasterDb && !appMasterDb.isEmpty()) {
				for (ApplicationMaster appMaster : appMasterDb) {
					applicationMasterData = appMaster;
				}
			}

			String loanId = "";
			String branchId = "";
			Optional<LoanDetails> loanOpt =
					loanDtlsRepo.findTopByApplicationIdAndAppId(applicationId, appId);

			if (loanOpt.isPresent()) {
				loanId = loanOpt.get().getT24LoanId();
				if (loanId == null) {
					logger.debug("Loan ID is null inside the object.");
					Response failureJson = getFailureJson(Constants.LOAN_ID_NOT_GENERATED);
					return Mono.just(failureJson);
				}
			}else {
				Response failureJson = getFailureJson(Constants.LOAN_ID_NOT_GENERATED);
				return Mono.just(failureJson);
			}
			logger.debug("Loan id from db:" + loanId);

			if(null !=applicationMasterData && null !=applicationMasterData.getBranchId()) {
				branchId = applicationMasterData.getBranchId();
			}else {
				logger.debug("Unable to fetch application branch id.");
				Response failureJson = getFailureJson("Unable to fetch application branch id");
				return Mono.just(failureJson);
			}
			Map<String, Object> loanRequest = new HashMap<>();
			loanRequest.put("remarks", "rejected");
			loanRequest.put("companyIdTemp", branchId);
			loanRequest.put("loanIdTemp", loanId);

			Map<String, Object> mapReq = new HashMap<>();
			mapReq.put("body", loanRequest);
			logger.debug("Final request of loan rejection  "+ mapReq);

			LoanRequestExt loanRequestExt = new LoanRequestExt();
			loanRequestExt.setAppId(appId);
			loanRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.LOAN_REJECTION_INTF.getKey()));
			loanRequestExt.setRequestObj(mapReq);
			logger.debug("Loan Rejection from the API: {} ", loanRequestExt.toString());

			setRequestLog(loanRequestExt.toString());

			Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header , loanRequestExt, loanRequestExt.getInterfaceName());

			logger.debug("response 1 from the API1: {} ", apiRespMono);

			return apiRespMono.flatMap(val -> {
				logger.debug("response 2 from the API: " + val);
				JSONObject apiResp = new JSONObject(new Gson().toJson(val));
				logger.debug("JSON response 3 from the API: " + apiResp);
				return Mono.just(apiResp);
			});
		} catch (Exception e) {
			logger.error("Error occurred while executing the Loan Rejection api", e);
			return Mono.just(getFailureApiJson("Error occurred while executing the Loan Rejection api", Constants.LOAN_REJECTION));
		}
	}
	/**
	 * Function to get loan Details if error - Loan is already in processing stage
	 * @param applicationId
	 * @param prop
	 * @param header
	 * @return
	 */
	public Mono<Object> loanFetch(String applicationId, Properties prop, ApplicationMaster masterObj, Header header) {
		logger.debug("Loan Fetch API started");

		try {

			LoanFetchReqFields loanRequest = new LoanFetchReqFields();

			loanRequest.setCompany(prop.getProperty(CobFlagsProperties.LOAN_FETCH_COMPANY.getKey()));
			loanRequest.setOperand(prop.getProperty(CobFlagsProperties.LOAN_FETCH_OPERAND.getKey()));
			loanRequest.setPassword(prop.getProperty(CobFlagsProperties.LOAN_FETCH_PASSWORD.getKey()));
			loanRequest.setUserName(prop.getProperty(CobFlagsProperties.LOAN_FETCH_USERNAME.getKey()));
			loanRequest.setColumnName(prop.getProperty(CobFlagsProperties.LOAN_FETCH_COLUMN_NAME.getKey()));
			loanRequest.setCriteriaValue(masterObj.getMemberId());

			LoanRequestExt loanRequestExt = new LoanRequestExt();
			loanRequestExt.setAppId(masterObj.getAppId());
			loanRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.LOAN_FETCH_INTF.getKey()));
			loanRequestExt.setRequestObj(loanRequest);
			logger.debug("Loan Fetch from the API: {} ", loanRequestExt.toString());

			setRequestLog(loanRequestExt.toString());

			Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header , loanRequestExt, loanRequestExt.getInterfaceName());

			logger.debug("response 1 from the API1: {} ", apiRespMono);

			return apiRespMono.flatMap(val -> {
				logger.debug("response 2 from the API: " + val);
				JSONObject apiResp = new JSONObject(new Gson().toJson(val));
				logger.debug("JSON response 3 from the API: " + apiResp);
				return Mono.just(apiResp);
			});
		} catch (Exception e) {
			logger.error("Error occurred while executing the Loan Fetch api", e);
			return Mono.just(getFailureApiJson("Error occurred while executing the Loan Fetch api", Constants.LOAN_FETCH));
		}
	}

	public Mono<Object> CoCustomerFetch(String applicationId, Properties prop, ApplicationMaster masterObj, Header header, String coApplicantId) {
		logger.debug("CoCustomerFetch API started");

		try {

			LoanFetchReqFields loanRequest = new LoanFetchReqFields();

			loanRequest.setCompany(prop.getProperty(CobFlagsProperties.LOAN_FETCH_COMPANY.getKey()));
			loanRequest.setPassword(prop.getProperty(CobFlagsProperties.LOAN_FETCH_PASSWORD.getKey()));
			loanRequest.setUserName(prop.getProperty(CobFlagsProperties.LOAN_FETCH_USERNAME.getKey()));
			loanRequest.setTransactionId(coApplicantId);

			LoanRequestExt loanRequestExt = new LoanRequestExt();
			loanRequestExt.setAppId(masterObj.getAppId());
			loanRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.CO_CUST_FETCH_INTF.getKey()));
			loanRequestExt.setRequestObj(loanRequest);
			logger.debug("CoCustomerFetch from the API: {} ", loanRequestExt.toString());

			setRequestLog(loanRequestExt.toString());

			Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, loanRequestExt, loanRequestExt.getInterfaceName());

			logger.debug("response 1 from the API1: {} ", apiRespMono);

			return apiRespMono.flatMap(val -> {
				logger.debug("CoCustomerFetch : response 2 from the API: " + val);
				JSONObject apiResp = new JSONObject(new Gson().toJson(val));
				logger.debug("CoCustomerFetch : JSON response 3 from the API: " + apiResp);
				return Mono.just(apiResp);
			});
		} catch (Exception e) {
			logger.error("Error occurred while executing the CoCustomerFetch api", e);
			return Mono.just(getFailureApiJson("Error occurred while executing the CoCustomerFetch api", Constants.CO_CUSTOMER_FETCH));
		}
	}

	public Mono<Object> loanRepaySchedule(String applicationId, Properties prop, String appId, Header header) {
		logger.debug("Loan RepaySchedule API started");
		CustomerDetailsPayload appPayload = null;
		CustomerDetailsPayload coappPayload =null;
		CibilDetailsPayload cibilPayload = null;
		String applicantCustId="";
		String coApplicantCustId = "";
		Gson gsonObj = new Gson();
		try {

			ApplyLoanRequestFields custFields = null;

			ApplicationMaster applicationMasterData = null;
			List<ApplicationMaster> appMasterDb = applicationMasterRepo.findByAppIdAndApplicationId(Constants.APPID,applicationId);

			if (null != appMasterDb && !appMasterDb.isEmpty()) {
				for (ApplicationMaster appMaster : appMasterDb) {
					applicationMasterData = appMaster;
				}
				String product = applicationMasterData.getProductCode();
				logger.debug("product :" + product);
				custFields = getCustomerData(applicationMasterData, applicationId, Constants.APPID, Constants.INITIAL_VERSION_NO);
			}


			for (CustomerDetails custDtl : custFields.getCustomerDetailsList()) {
				logger.debug("customer Type : " + custDtl.getCustomerType());
				if (custDtl.getCustomerType().equalsIgnoreCase("Applicant")) {
					applicantCustId = String.valueOf(custDtl.getCustDtlId());
					logger.debug("applicantCustId : " + applicantCustId);
					appPayload = gsonObj.fromJson(custDtl.getPayloadColumn(), CustomerDetailsPayload.class);
					logger.debug("custApplicantPayload :" + appPayload);
				} else if (custDtl.getCustomerType().equalsIgnoreCase(Constants.COAPPLICANT)) {
					coApplicantCustId = String.valueOf(custDtl.getCustDtlId());
					coappPayload = gsonObj.fromJson(custDtl.getPayloadColumn(), CustomerDetailsPayload.class);
					logger.debug("custCo-ApplicantPayload :" + coappPayload);
				}
			}
			for (CibilDetailsWrapper cibilDetailsWrapper : custFields.getCibilDetailsWrapperList()) {
				String custId = cibilDetailsWrapper.getCibilDetails().getCustDtlId().toString();
				logger.debug("CreditDetailsPayload Payload : " + cibilPayload);
				if (custId.equals(coApplicantCustId)) {
					cibilPayload = gsonObj.fromJson(
							cibilDetailsWrapper.getCibilDetails().getPayloadColumn(), CibilDetailsPayload.class);
				}
			}

			LoanDetails loanDetails = custFields.getLoanDetails();

			Integer year = loanDetails.getTenure() / 12;
			String yearInWeeks = (year >= 1 && year <= 3) ? String.valueOf((year * 52)): "";

			Map<String, Object> loanReq = new HashMap<>();
			String normalized = cibilPayload.getRepaymentFrequency().toUpperCase().replaceAll("\\s", "-");
			// collapse multiple spaces or dashes into one dash
			normalized = normalized.replaceAll("[\\s\\-]+", "-");
			// if it looks like any variant of BI-WEEKLY
			Matcher m = Constants.BI_WEEKLY_PATTERN.matcher(normalized);
			if (m.find()) {
				normalized = "BI-WEEKLY";
			}
			loanReq.put("loanFrequency",normalized);
			loanReq.put("interestRate", cibilPayload.getRoi());
			loanReq.put("loanAmount", loanDetails.getSanctionedLoanAmount());
			loanReq.put("tenure", yearInWeeks);
			loanReq.put(Constants.CUSTOMERID1, applicationMasterData.getMemberId());
			loanReq.put("productID", Constants.LOAN_PRODUCT);

			Map<String, Object> mapReq = new HashMap<>();
			mapReq.put("body", loanReq);
			logger.debug("Final request of loan RepaySchedule  "+ mapReq);

			LoanRequestExt loanRequestExt = new LoanRequestExt();
			loanRequestExt.setAppId(appId);
			loanRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.LOAN_REPAYMENT_SCHEDULE_INTF.getKey()));
			loanRequestExt.setRequestObj(mapReq);
			logger.debug("Loan RepaySchedule from the API: {} ", loanRequestExt.toString());

			setRequestLog(loanRequestExt.toString());

			Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header , loanRequestExt, loanRequestExt.getInterfaceName());

			logger.debug("response 1 from the API1: {} ", apiRespMono);

			return apiRespMono.flatMap(val -> {
				logger.debug("response 2 from the API: " + val);
				JSONObject apiResp = new JSONObject(new Gson().toJson(val));
				logger.debug("JSON response 3 from the API: " + apiResp);
				return Mono.just(apiResp);
			});
		} catch (Exception e) {
			logger.error("Error occurred while executing the Loan RepaySchedule api", e);
			return Mono.just(getFailureApiJson("Error occurred while executing the Loan RepaySchedule api", Constants.LOAN_REJECTION));
		}
	}
	public Mono<Object> loanDisbursement(String applicationId, Properties prop, String appId, Header header) {

		try {

			CustomerDetailsPayload appPayload = null;
			CustomerDetailsPayload coappPayload =null;
			CibilDetailsPayload cibilPayload = null;
			String applicantCustId="";
			String coApplicantCustId = "";
			Gson gsonObj = new Gson();

			ApplyLoanRequestFields custFields = null;
			ApplicationMaster applicationMasterData = null;
			List<ApplicationMaster> appMasterDb = applicationMasterRepo
					.findByAppIdAndApplicationId(Constants.APPID,applicationId);

			if (null != appMasterDb && !appMasterDb.isEmpty()) {
				for (ApplicationMaster appMaster : appMasterDb) {
					applicationMasterData = appMaster;
				}
				String product = applicationMasterData.getProductCode();
				logger.debug("product :" + product);
				custFields = getCustomerData(applicationMasterData, applicationId, Constants.APPID, 1);
			}
			for (CustomerDetails custDtl : custFields.getCustomerDetailsList()) {
				logger.debug("customer Type : " + custDtl.getCustomerType());
				if (custDtl.getCustomerType().equalsIgnoreCase("Applicant")) {
					applicantCustId = String.valueOf(custDtl.getCustDtlId());
					logger.debug("applicantCustId : " + applicantCustId);
					appPayload = gsonObj.fromJson(custDtl.getPayloadColumn(), CustomerDetailsPayload.class);
					logger.debug("custApplicantPayload :" + appPayload);
				} else if (custDtl.getCustomerType().equalsIgnoreCase(Constants.COAPPLICANT)) {
					coApplicantCustId = String.valueOf(custDtl.getCustDtlId());
					coappPayload = gsonObj.fromJson(custDtl.getPayloadColumn(), CustomerDetailsPayload.class);
					logger.debug("custCo-ApplicantPayload :" + coappPayload);
				}
			}
			String loanId = "";
			Optional<LoanDetails> loanOpt =
					loanDtlsRepo.findTopByApplicationIdAndAppId(applicationId, appId);

			if (loanOpt.isPresent()) {
				loanId = loanOpt.get().getT24LoanId();
				if (loanId == null) {
					logger.debug("Loan ID is null inside the object.");
					Response failureJson = getFailureJson(Constants.LOAN_ID_NOT_GENERATED);
					return Mono.just(failureJson);
				}
			}else {
				Response failureJson = getFailureJson(Constants.LOAN_ID_NOT_GENERATED);
				return Mono.just(failureJson);
			}
			logger.debug("Loan id from db:" + loanId);

			Map<String, Object> loanRequest = new HashMap<>();
			loanRequest.put("stampDutyCharge", Integer.parseInt(Constants.ZERO));
			loanRequest.put("companyIdTemp", applicationMasterData.getBranchId());
			loanRequest.put("loanIdTemp", loanId);

			Map<String, Object> mapReq = new HashMap<>();
			mapReq.put("body", loanRequest);
			logger.debug("Final request of loan disbursement  "+ mapReq);

			LoanRequestExt loanRequestExt = new LoanRequestExt();
			loanRequestExt.setAppId(appId);
			loanRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.LOAN_DISBURSEMENT_INTF.getKey()));
			loanRequestExt.setRequestObj(mapReq);
			logger.debug("Loan disbursement from the API: {} ", loanRequestExt.toString());

			setRequestLog(loanRequestExt.toString());

			Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header , loanRequestExt, loanRequestExt.getInterfaceName());

			logger.debug("response 1 from the API1: {} ", apiRespMono);

			return apiRespMono.flatMap(val -> {
				logger.debug("response 2 from the API: " + val);
				JSONObject apiResp = new JSONObject(new Gson().toJson(val));
				logger.debug("JSON response 3 from the API: " + apiResp);
				return Mono.just(apiResp);
			});

		} catch (Exception e) {
			logger.error("Error occurred while executing the Loan Disbursement api", e);
			return Mono.just(getFailureApiJson("Error occurred while executing the Loan Disbursement api", Constants.LOAN_DISBURSEMENT));
		}
	}


	public Mono<Object> disbRepaySchedule(String applicationId, Properties prop, String appId, Header header) {
		logger.debug("Loan Repyament at Disbursemnt Schedule started");
		try {

			String loanId = "";
			Optional<LoanDetails> loanOpt = loanDtlsRepo.findTopByApplicationIdAndAppId(applicationId, appId);

			if (loanOpt.isPresent()) {
				loanId = loanOpt.get().getT24LoanId();
				if (loanId == null) {
					logger.debug("Loan ID is null inside the object.");
					Response failureJson = getFailureJson(Constants.LOAN_ID_NOT_GENERATED);
					return Mono.just(failureJson);
				}
			} else {
				Response failureJson = getFailureJson(Constants.LOAN_ID_NOT_GENERATED);
				return Mono.just(failureJson);
			}
			logger.debug("Loan id from db:" + loanId);

			Map<String, Object> loanRequest = new HashMap<>();
			loanRequest.put("loanIdTemp", loanId);

			Map<String, Object> mapReq = new HashMap<>();
			mapReq.put("body", loanRequest);
			logger.debug("Final request of loan Repyament disbursement  " + mapReq);

			LoanRequestExt loanRequestExt = new LoanRequestExt();
			loanRequestExt.setAppId(appId);
			loanRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.DISBURSEMENT_REPAYMENTSCHEDULE_INTF.getKey()));
			loanRequestExt.setRequestObj(mapReq);
			logger.debug("Loan Repyament disbursement from the API: {} ", loanRequestExt.toString());

			setRequestLog(loanRequestExt.toString());

			Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, loanRequestExt,
					loanRequestExt.getInterfaceName());

			logger.debug("response 1 from the API1: {} ", apiRespMono);

			return apiRespMono.flatMap(val -> {
				logger.debug("response 2 from the API: " + val);
				JSONObject apiResp = new JSONObject(new Gson().toJson(val));
				logger.debug("JSON response 3 from the API: " + apiResp);
				return Mono.just(apiResp);
			});

		} catch (Exception e) {
			logger.error("Error occurred while executing the Loan Repyament Disbursement api", e);
			return Mono.just(getFailureApiJson("Error occurred while executing the Loan Repyament Disbursement api",
					Constants.LOAN_DISBURSEMENT));
		}
	}

	public Mono<Object> dedupeTableUpdate(ApplicationMaster master, Header header, Properties prop, String targetCustomerId, boolean isCoapp) {
		logger.debug("Dedupe Table Update Started");
		Gson gson = new Gson();
		String updateApi = isCoapp ? Constants.COAPPLICANT_DEDUPE_UPDATE : Constants.APPLICANT_DEDUPE_UPDATE;
		try {
			String customerType = isCoapp ? Constants.COAPPLICANT : Constants.APPLICANT;

			DedupeTableUpdateRequest request = new DedupeTableUpdateRequest();
			request.setId("3");
			request.setGkv("1.2");
			request.setMethod("api.custUpdate");
			request.setCustomerId(targetCustomerId);
			request.setCustqualify("CREDIT.IL");

			Optional<CustomerDetails> customerOpt = custDtlRepo.findByApplicationIdAndCustomerType(master.getApplicationId(), customerType);
			if (!customerOpt.isPresent()) {
				logger.debug("Customer details not found for type: {}", customerType);
				return Mono.just(getFailureApiJson("Customer details not found.", updateApi));
			}
			CustomerDetails customerDetails = customerOpt.get();
			CustomerDetailsPayload custPayload = gson.fromJson(customerDetails.getPayloadColumn(), CustomerDetailsPayload.class);
			logger.debug("CustomerDetailsPayload: {}", custPayload);

			if(master.getProductCode().equalsIgnoreCase(Constants.UNNATI_PRODUCT_CODE)) {
				Optional<LeadDetails> leadDetailsOpt = leadDtlsRepo.findByCustomerId(master.getSearchCode2());
				if (!leadDetailsOpt.isPresent()) {
					logger.debug("Lead details not found for memberId: {}", master.getSearchCode2());
					return Mono.just(getFailureApiJson("Lead details not found.", updateApi));
				}

				LeadDetails leadDetails = leadDetailsOpt.get();
				request.setKendraId(leadDetails.getKendraId());
				request.setGroupId(leadDetails.getGroupId());
				request.setBranchId(leadDetails.getGlBranchId());
			}else if(master.getProductCode().equalsIgnoreCase(Constants.RENEWAL_LOAN_PRODUCT_CODE)){
				Optional<RenewalLeadDetails> renewalLeadDetailsOpt = renewalLeadDtlsRepo.findByCustomerId(master.getSearchCode2());
				if (!renewalLeadDetailsOpt.isPresent()) {
					logger.debug("Renewal Lead details not found for memberId: {}", master.getSearchCode2());
					return Mono.just(getFailureApiJson("Renewal Lead details not found.", updateApi));
				}
				RenewalLeadDetails renewalLeadDetails = renewalLeadDetailsOpt.get();
				request.setKendraId(renewalLeadDetails.getKendraId());
				request.setGroupId(renewalLeadDetails.getGroupId());
				request.setBranchId(renewalLeadDetails.getGlBranchId());
			}else{
				logger.debug("Unknown product code for dedupe update: {}", master.getProductCode());
				return Mono.just(getFailureApiJson("Unknown product code.", updateApi));
			}
			request.setName(custPayload.getFirstName());
			request.setRecordtype("ACTIVE");
			if(customerType.equalsIgnoreCase(Constants.COAPPLICANT)) {
				Optional<CustomerDetails> applicantCustOpt = custDtlRepo.findByApplicationIdAndCustomerType(master.getApplicationId(), Constants.APPLICANT);
				if (!applicantCustOpt.isPresent()) {
					logger.debug("Customer details not found for type:" + Constants.APPLICANT);
					return Mono.just(getFailureApiJson("Applicant customer details not found.", updateApi));
				}
				CustomerDetails applicantCustomerDetails = applicantCustOpt.get();
				CustomerDetailsPayload applicantCustPayload = gson.fromJson(applicantCustomerDetails.getPayloadColumn(), CustomerDetailsPayload.class);
				logger.debug("Applicant CustomerDetailsPayload: {}", applicantCustPayload);
				String customerMobile = customerDetails.getMobileNumber();
				String applicantMobile = applicantCustomerDetails.getMobileNumber();
				if (customerMobile != null
						&& (applicantMobile == null
						|| !customerMobile.equalsIgnoreCase(applicantMobile))) {
					request.setPhoneNum1(customerMobile);
				}
				if(custPayload.getMaritalStatus().equalsIgnoreCase(Constants.MARRIED) && custPayload.getRelationShipWithApplicant().equalsIgnoreCase(Constants.SPOUSE)) {
					boolean isPrimaryVerified = Constants.VERIFIED_STS.equalsIgnoreCase(applicantCustPayload.getPrimaryKycIdValStatus());
					boolean isAlternateVerified = Constants.VERIFIED_STS.equalsIgnoreCase(applicantCustPayload.getAlternateVoterIdValStatus());
					String apptKycNo = "";
					logger.debug("isPrimaryVerified: {}, isAlternateVerified: {}", isPrimaryVerified, isAlternateVerified);
					boolean hasAlternateId =
							StringUtils.isNotBlank(applicantCustPayload.getAlternateVoterId());
					boolean hasPrimaryId =
							StringUtils.isNotBlank(applicantCustPayload.getPrimaryKycId());
					if (isAlternateVerified && hasAlternateId) {
						// Highest priority
						apptKycNo = applicantCustPayload.getAlternateVoterId();
					} else if (isPrimaryVerified && hasPrimaryId) {
						// Fallback
						apptKycNo = applicantCustPayload.getPrimaryKycId();
					} else {
						logger.debug(
								"No valid verified Voter ID found for customerType {}. " +
										"Alternate verified={}, Alternate blank={}, " +
										"Primary verified={}, Primary blank={}", Constants.APPLICANT,
								isAlternateVerified, !hasAlternateId,
								isPrimaryVerified, !hasPrimaryId
						);

						return Mono.error(new RuntimeException(
								"No valid verified Voter ID found for the " + Constants.APPLICANT + ". Cannot proceed with Dedupe Table Update."
						));
					}
					request.setSpkycid(apptKycNo.toUpperCase());
					request.setSpkycname("VOTER-ID");
				}
			}else if(customerType.equalsIgnoreCase(Constants.APPLICANT)) {
				Optional<CustomerDetails> coApplicantCustOpt = custDtlRepo.findByApplicationIdAndCustomerType(master.getApplicationId(), Constants.COAPPLICANT);
				if (!coApplicantCustOpt.isPresent()) {
					logger.debug("Customer details not found for type:" + Constants.COAPPLICANT);
					return Mono.just(getFailureApiJson("Co-Applicant customer details not found.", updateApi));
				}
                request.setPhoneNum1(customerDetails.getMobileNumber());
				CustomerDetails coApplicantCustomerDetails = coApplicantCustOpt.get();
				CustomerDetailsPayload coApplicantCustPayload = gson.fromJson(coApplicantCustomerDetails.getPayloadColumn(), CustomerDetailsPayload.class);
				logger.debug("Co-Applicant CustomerDetailsPayload: {}", coApplicantCustPayload);
				if(coApplicantCustPayload.getMaritalStatus().equalsIgnoreCase(Constants.MARRIED) && coApplicantCustPayload.getRelationShipWithApplicant().equalsIgnoreCase(Constants.SPOUSE)) {
					boolean isPrimaryVerified = Constants.VERIFIED_STS.equalsIgnoreCase(coApplicantCustPayload.getPrimaryKycIdValStatus());
					boolean isAlternateVerified = Constants.VERIFIED_STS.equalsIgnoreCase(coApplicantCustPayload.getAlternateVoterIdValStatus());
					String coappKycNo = "";
					logger.debug("isPrimaryVerified: {}, isAlternateVerified: {}", isPrimaryVerified, isAlternateVerified);
					boolean hasAlternateId =
							StringUtils.isNotBlank(coApplicantCustPayload.getAlternateVoterId());
					boolean hasPrimaryId =
							StringUtils.isNotBlank(coApplicantCustPayload.getPrimaryKycId());
					if (isAlternateVerified && hasAlternateId) {
						// Highest priority
						coappKycNo = coApplicantCustPayload.getAlternateVoterId();
					} else if (isPrimaryVerified && hasPrimaryId) {
						// Fallback
						coappKycNo = coApplicantCustPayload.getPrimaryKycId();
					} else {
						logger.debug(
								"No valid verified Voter ID found for customerType {}. " +
										"Alternate verified={}, Alternate blank={}, " +
										"Primary verified={}, Primary blank={}", Constants.COAPPLICANT,
								isAlternateVerified, !hasAlternateId,
								isPrimaryVerified, !hasPrimaryId
						);

						return Mono.error(new RuntimeException(
								"No valid verified Voter ID found for the " + Constants.COAPPLICANT + ". Cannot proceed with Dedupe Table Update."
						));
					}
					request.setSpkycid(coappKycNo.toUpperCase());
					request.setSpkycname("VOTER-ID");
				}
			}

			Optional<BankDetails> bankDetailsOpt = bankDtlRepo.findByApplicationIdAndCustDtlId(master.getApplicationId(),customerDetails.getCustDtlId());
			if(bankDetailsOpt.isPresent()) {
				BankDetails bankDetails = bankDetailsOpt.get();
				BankDetailsPayload bankPayload = gson.fromJson(bankDetails.getPayloadColumn(), BankDetailsPayload.class);
				logger.debug("BankDetailsPayload: {}", bankPayload);
				request.setBankAccNo(bankPayload.getAccountNumber());
				request.setBankname(bankPayload.getBankName());
				request.setBankBranchName(bankPayload.getBranchName());
				request.setIfscCode(bankPayload.getIfsc());
				request.setAccHolderName(bankPayload.getAccountName());

			}
			request.setCb_status(Constants.PASS_STRING);//Pass since this method will called only when the credit check has been pased.
			List<DedupeTableUpdateRequest.LegalDocument> legalDocumentList = new ArrayList<>();
			DedupeTableUpdateRequest.LegalDocument voterId = new DedupeTableUpdateRequest.LegalDocument();
			voterId.setName("VOTER-ID");
			boolean isPrimaryVerified = Constants.VERIFIED_STS.equalsIgnoreCase(custPayload.getPrimaryKycIdValStatus());
			boolean isAlternateVerified = Constants.VERIFIED_STS.equalsIgnoreCase(custPayload.getAlternateVoterIdValStatus());
			String apptKycNo = "";
			logger.debug("isPrimaryVerified: {}, isAlternateVerified: {}", isPrimaryVerified, isAlternateVerified);
			boolean hasAlternateId =
					StringUtils.isNotBlank(custPayload.getAlternateVoterId());
			boolean hasPrimaryId =
					StringUtils.isNotBlank(custPayload.getPrimaryKycId());
			if (isAlternateVerified && hasAlternateId) {
				// Highest priority
				apptKycNo = custPayload.getAlternateVoterId();
			} else if (isPrimaryVerified && hasPrimaryId) {
				// Fallback
				apptKycNo = custPayload.getPrimaryKycId();
			} else {
				logger.debug(
						"No valid verified Voter ID found for customerType {}. " +
								"Alternate verified={}, Alternate blank={}, " +
								"Primary verified={}, Primary blank={}", customerType,
						isAlternateVerified, !hasAlternateId,
						isPrimaryVerified, !hasPrimaryId
				);

				return Mono.error(new RuntimeException(
						"No valid verified Voter ID found for the " + customerType + ". Cannot proceed with Dedupe Table Update."
				));
			}
			voterId.setId(apptKycNo);
			legalDocumentList.add(voterId);
			if("Driving Licence".equalsIgnoreCase(custPayload.getSecondaryKycType())
					&& StringUtils.isNotEmpty(custPayload.getSecondaryKycId()) && Constants.VERIFIED_STS.equalsIgnoreCase(custPayload.getSecondaryKycIdValStatus())){
				DedupeTableUpdateRequest.LegalDocument drivingLicence = new DedupeTableUpdateRequest.LegalDocument();
				drivingLicence.setName("DRIVING-ID");
				drivingLicence.setId(custPayload.getSecondaryKycId());
				legalDocumentList.add(drivingLicence);
			}
			request.setLegalDocument(legalDocumentList);


//            Map<String, DedupeTableUpdateRequest> mapReq = new HashMap<>();
//            mapReq.put("body", request);
//            logger.debug("JSON request for Dedupe Table Update api :" + mapReq);
			logger.debug("JSON request for Dedupe Table Update api :" + request);
			logger.debug("Final request object: {}", gson.toJson(request));
			LoanRequestExt loanCreationRequestExt = new LoanRequestExt();
			loanCreationRequestExt.setAppId(master.getAppId());
			loanCreationRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.DEDUPE_TABLE_UPDATE_INTF.getKey()));
//            loanCreationRequestExt.setRequestObj(mapReq);
			loanCreationRequestExt.setRequestObj(request);
			logger.debug("Dedupe Table Update API: {} ", loanCreationRequestExt.toString());

			setRequestLog(loanCreationRequestExt.toString());

			Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, loanCreationRequestExt,
					loanCreationRequestExt.getInterfaceName());
			logger.debug("response 1 from the API:" + apiRespMono);

			return apiRespMono.flatMap(val -> {
				logger.debug("response 2 from the API: " + val);
				JSONObject apiResp = new JSONObject(gson.toJson(val));
				logger.debug("JSON response 3 from the API: " + apiResp);
				return Mono.just(apiResp);
			});

		} catch (Exception e) {
			logger.error("Error occurred while executing the Dedupe Table Update api", e);
			return Mono.just(getFailureApiJson("Error occurred while executing the Dedupe Table Update api", updateApi));
		}
	}

	public Mono<Object> coapplicantCreation(String applicationId, String appId, String memId, Header header, Properties prop, boolean updateCall, String coapplCreationId, boolean coapplUpdate) {

		logger.debug("Create & Updated Co Applicant Started");
		Gson gsonObj = new Gson();
		try {
			CustomerDetailsPayload coappPayload =new CustomerDetailsPayload();
			OccupationDetails coApplicant = new OccupationDetails();
			OccupationDetailsPayload occAppPayload = new OccupationDetailsPayload();
			OccupationDetailsPayload occCoappPayload = new OccupationDetailsPayload();
			String applicantCustId="";
			String coApplicantCustId = "";
			String appCustName = "";
			String appPhnNum = "";
			String appGender = "";
			String appVoterId = "";
			String coAppName = "";
			String coAppPhnNum = "";
			String coAppGender = "";
			String coAppVoterId = "";
			String appStoredDOB ="";
			String coAppStoredDOB ="";
			String appMaritalStatus ="";
			String coAppMaritalStatus ="";
			String appFirstName = "";
			String coAppFirstName = "";
			String appLastName = "";
			String coAppLastName = "";
			CustomerDetailsPayload appPayload = null;
			ApplyLoanRequestFields custFields = null;
			ApplicationMaster applicationMasterData = null;

			boolean useCoApplicant = !updateCall || coapplUpdate;
			boolean useApplicant= updateCall && !coapplUpdate;

			List<ApplicationMaster> appMasterDb = applicationMasterRepo
					.findByAppIdAndApplicationId(Constants.APPID,applicationId);

			if (null != appMasterDb && !appMasterDb.isEmpty()) {
				for (ApplicationMaster appMaster : appMasterDb) {
					applicationMasterData = appMaster;
				}
				String product = applicationMasterData.getProductCode();
				logger.debug("product :" + product);
				custFields = getCustomerData(applicationMasterData, applicationId, Constants.APPID, 1);
			}
			for(CustomerDetails custDtl : custFields.getCustomerDetailsList()) {

				logger.debug("customer Type : " + custDtl.getCustomerType());
				if(custDtl.getCustomerType().equalsIgnoreCase("Applicant")) {
					applicantCustId = String.valueOf(custDtl.getCustDtlId());
					logger.debug("applicantCustId : " + applicantCustId);
					appPayload = gsonObj.fromJson(custDtl.getPayloadColumn(), CustomerDetailsPayload.class);
					logger.debug("custApplicantPayload :" + appPayload);
					appCustName = custDtl.getCustomerName();
					appFirstName = appPayload.getFirstName();
					appPhnNum = custDtl.getMobileNumber();
					appGender = appPayload.getGender();
					appVoterId = "";
					appStoredDOB = appPayload.getDob().replace("-","");
					appLastName = StringUtils.isNotBlank(appPayload.getLastName()) ? appPayload.getLastName() : "";
					appMaritalStatus = appPayload.getMaritalStatus();
					boolean isAlternateVerified =
							Constants.VERIFIED_STS.equalsIgnoreCase(appPayload.getAlternateVoterIdValStatus());
					boolean hasAlternateId =
							StringUtils.isNotBlank(appPayload.getAlternateVoterId());

					boolean isPrimaryVerified =
							Constants.VERIFIED_STS.equalsIgnoreCase(appPayload.getPrimaryKycIdValStatus());
					boolean hasPrimaryId =
							StringUtils.isNotBlank(appPayload.getPrimaryKycId());

					if (isAlternateVerified && hasAlternateId) {

						// Highest priority
						appVoterId = appPayload.getAlternateVoterId();

					} else if (isPrimaryVerified && hasPrimaryId) {

						// Fallback
						appVoterId = appPayload.getPrimaryKycId();

					} else {
						logger.debug(
								"No valid verified Voter ID found for applicant. " +
										"Alternate verified={}, Alternate blank={}, " +
										"Primary verified={}, Primary blank={}",
								isAlternateVerified, !hasAlternateId,
								isPrimaryVerified, !hasPrimaryId
						);

						return Mono.error(new RuntimeException(
								"No valid verified Voter ID found for applicant."
						));
					}

				}else if(custDtl.getCustomerType().equalsIgnoreCase(Constants.COAPPLICANT)) {
					coApplicantCustId = String.valueOf(custDtl.getCustDtlId());
					logger.debug("coApplicantCustId : " + coApplicantCustId);
					coappPayload  = gsonObj.fromJson(custDtl.getPayloadColumn(), CustomerDetailsPayload.class);
					coAppName = custDtl.getCustomerName();
					coAppFirstName = coappPayload.getFirstName();
					coAppPhnNum = custDtl.getMobileNumber();
					coAppGender = coappPayload.getGender();
					coAppVoterId = "";
					coAppStoredDOB = coappPayload.getDob().replace("-","");
					coAppLastName = StringUtils.isNotBlank(coappPayload.getLastName()) ? coappPayload.getLastName() : "";
					coAppMaritalStatus = coappPayload.getMaritalStatus();
					boolean isAlternateVerified =
							Constants.VERIFIED_STS.equalsIgnoreCase(coappPayload.getAlternateVoterIdValStatus());
					boolean hasAlternateId =
							StringUtils.isNotBlank(coappPayload.getAlternateVoterId());

					boolean isPrimaryVerified =
							Constants.VERIFIED_STS.equalsIgnoreCase(coappPayload.getPrimaryKycIdValStatus());
					boolean hasPrimaryId =
							StringUtils.isNotBlank(coappPayload.getPrimaryKycId());
					if (isAlternateVerified && hasAlternateId) {

						// Highest priority
						coAppVoterId = coappPayload.getAlternateVoterId();

					} else if (isPrimaryVerified && hasPrimaryId) {

						// Fallback
						coAppVoterId = coappPayload.getPrimaryKycId();

					} else {
						logger.debug(
								"No valid verified Voter ID for co-applicant. " +
										"Alternate verified={}, Alternate blank={}, " +
										"Primary verified={}, Primary blank={}",
								isAlternateVerified, !hasAlternateId,
								isPrimaryVerified, !hasPrimaryId
						);

						return Mono.error(new RuntimeException(
								"No valid verified Voter ID found for co-applicant."
						));
					}
					logger.debug("custCo-ApplicantPayload :" + coappPayload);
				}
			}
			String occup = normalize(appPayload.getOccupation());
			for (OccupationDetailsWrapper applicantwrpr : custFields.getOccupationDetailsWrapperList()) {
				logger.debug("applicantCustId " + applicantCustId);
				if (String.valueOf(applicantwrpr.getOccupationDetails().getCustDtlId()).equals(applicantCustId)) {
					occAppPayload = gsonObj.fromJson(applicantwrpr.getOccupationDetails().getPayloadColumn(),
							OccupationDetailsPayload.class);
					logger.debug("occupationApplicantPayload : " + occAppPayload.toString());
				} else if (String.valueOf(applicantwrpr.getOccupationDetails().getCustDtlId())
						.equals(coApplicantCustId)) {
					coApplicant = applicantwrpr.getOccupationDetails();
					occCoappPayload = gsonObj.fromJson(applicantwrpr.getOccupationDetails().getPayloadColumn(),
							OccupationDetailsPayload.class);
					logger.debug("occupationCo-appliocantPayload : " + occCoappPayload.toString());
				}
			}
			String relation = prop.getProperty(CobFlagsProperties.RELATIONSHIP_CODES.getKey());

			String coappRelation = "";
			JSONObject relationshipCodes = new JSONObject(relation);

			String inputRelation = normalize(StringUtils.isEmpty(coappPayload.getRelationShipWithApplicant())?"":coappPayload.getRelationShipWithApplicant());

			for (String key : relationshipCodes.keySet()) {
				if (normalize(key).equalsIgnoreCase(inputRelation)) {
					coappRelation = relationshipCodes.getString(key);
					break;
				}
			}

			logger.debug("coappRelation code" + coappRelation);
			List<CustomerDetails> customerDetailsList = custFields.getCustomerDetailsList();

// Always resolve the applicant's custDtlId
			String applicantCustDtlId = customerDetailsList.stream()
					.filter(c -> c.getCustomerType().equals(Constants.APPLICANT))
					.findFirst()
					.map(c -> c.getCustDtlId().toString())
					.orElseThrow(() -> new IllegalStateException("Applicant not found"));

// Find matching bank details for the applicant
			BankDetailsPayload bankPayload = custFields.getBankDetailsWrapperList().stream()
					.filter(b -> b.getBankDetails().getCustDtlId().toString().equals(applicantCustDtlId))
					.findFirst()
					.map(b -> gsonObj.fromJson(b.getBankDetails().getPayloadColumn(), BankDetailsPayload.class))
					.orElseThrow(() -> new IllegalStateException("No bank details found for applicant"));

			CoapplicantCreationRequestFields req = new CoapplicantCreationRequestFields();

			// First Name
			CustomerFirstNameRequestField firstNameObj = new CustomerFirstNameRequestField();
			firstNameObj.setFirstName(useCoApplicant ?
					(StringUtils.isNotBlank(coAppFirstName) ? coAppFirstName :coAppName ): (StringUtils.isNotBlank(appFirstName) ? appFirstName : appCustName));
			List<CustomerFirstNameRequestField> firstNameList = new ArrayList<>();
			firstNameList.add(firstNameObj);
			req.setCustomerFirstName(firstNameList);

			// Short Name
			CustomerShortNameRequestField shortNameObj = new CustomerShortNameRequestField();
			shortNameObj.setShortName(".."); // According to API spec
			List<CustomerShortNameRequestField> shortNameList = new ArrayList<>();
			shortNameList.add(shortNameObj);
			req.setCustomerShortName(shortNameList);

			//other details
			req.setDateOfBirth(useCoApplicant ? Long.parseLong(coAppStoredDOB) : Long.parseLong(appStoredDOB));
			req.setGender(useCoApplicant ? coAppGender.toUpperCase() : appGender.toUpperCase());
			req.setMaritalstatus(useCoApplicant ? coAppMaritalStatus.toUpperCase() : appMaritalStatus.toUpperCase());
			req.setVoterId(useCoApplicant ? coAppVoterId : appVoterId);

			// Basic info
			String title = useCoApplicant ? coappPayload.getTitle() : appPayload.getTitle();
			req.setTitle(title.endsWith(".") ? title : title + ".");
			req.setFamilyName("");

			// RationCard Pan DL Passport
			String secKycType = useCoApplicant ? coappPayload.getSecondaryKycType() : appPayload.getSecondaryKycType();
			String secKycId = useCoApplicant ? coappPayload.getSecondaryKycId() : appPayload.getSecondaryKycId();
			String kyc = secKycType != null ? secKycType.toLowerCase() : "";
			req.setRationCardNumber("");
			req.setPanIdNumber(kyc.contains("pan") ? secKycId : "");
			req.setDlNumber(kyc.contains("driving") || kyc.contains("license") ? secKycId : "");
			req.setPassport(kyc.contains("passport") ? secKycId : "");
			req.setOtherGovernmentId("");

			//BSN
			String bsn = prop.getProperty(CobFlagsProperties.BUSINESS_CODES.getKey());
			JSONObject js = new JSONObject(bsn);
			String nameOfbsn = "";

			if(useApplicant) {
				if (js.has(occAppPayload.getNatureOfOccupation())) {
					nameOfbsn = js.getString(occAppPayload.getNatureOfOccupation());
					System.out.println("Matched value: " + nameOfbsn);
					logger.debug("Matched Value :" + nameOfbsn);
				} else {
					logger.debug("No match found");
				}
			} else if (js.has(occCoappPayload.getNatureOfOccupation())) {
				nameOfbsn = js.getString(occCoappPayload.getNatureOfOccupation());
				System.out.println("Matched value: " + nameOfbsn);
				logger.debug("Matched Value :" + nameOfbsn);
			} else {
				logger.debug("No match found");
			}

			req.setNameOfBSN(nameOfbsn);

			req.setEmployeeNumber("");  // According to API spec


			// Phone Number
			if (appPhnNum == null || appPhnNum.trim().isEmpty()) {
				appPhnNum = prop.getProperty(CobFlagsProperties.COAPPL_PHONENUMBER_DEFAULT.getKey());
				logger.debug("Default phone num" + appPhnNum);
			}
			if (coAppPhnNum == null || coAppPhnNum.trim().isEmpty()) {
				coAppPhnNum = prop.getProperty(CobFlagsProperties.COAPPL_PHONENUMBER_DEFAULT.getKey());
				logger.debug("Default phone num" + coAppPhnNum);
			}
			PhoneNumberRequestField phoneObj = new PhoneNumberRequestField();
			phoneObj.setPhoneNumber(useCoApplicant ? coAppPhnNum : appPhnNum);
			List<PhoneNumberRequestField> phoneList = new ArrayList<>();
			phoneList.add(phoneObj);
			req.setPhoneNumber(phoneList);

			//Occuaption
			String occupation = prop.getProperty(CobFlagsProperties.CUSTOMER_OCCUPATION.getKey());
			JSONObject arr = new JSONObject(occupation);
			String custOccupation = "";

			String normalizedInput = normalize(coappPayload.getOccupation());

			Iterator<String> keys = arr.keys();
			while (keys.hasNext()) {
				String key = keys.next();
				if (normalize(key).equalsIgnoreCase(normalizedInput)) {
					custOccupation = arr.getString(key);
					break;
				}
			}
			logger.debug("custOccupation : "+ custOccupation);

			req.setCustomerOccupation(custOccupation);

			// Addresses
			JSONArray addrArray = getAllAddresses(custFields, useCoApplicant ? coApplicantCustId : applicantCustId);
			List<CustomerAddressRequestField> addrList = new Gson().fromJson(addrArray.toString(), new TypeToken<List<CustomerAddressRequestField>>(){}.getType());
			req.setCustomerAddress(addrList);

			//Co Applicant
			CoApplicantDetailRequestField coappObj = new CoApplicantDetailRequestField();
			coappObj.setCoApplicantCustomerID(useApplicant? coapplCreationId : "");//As per Api doc
			coappObj.setCoApplicantRelation(useApplicant? coappRelation : "");
			List<CoApplicantDetailRequestField> coAppList = new ArrayList<>();
			coAppList.add(coappObj);
			req.setCoApplicantDetails(coAppList);

			// Beneficiary details
			req.setBeneficiaryBankAccountNum(useApplicant? bankPayload.getAccountNumber() : "");
			req.setBeneficirayIfscCode(useApplicant? bankPayload.getIfsc() : "");
			req.setBeneficiaryBankName(useApplicant? bankPayload.getBankName() : "");
			req.setBeneficiaryBranchkName(useApplicant? bankPayload.getBranchName() : "");

			req.setCustomerIdTemp(useCoApplicant ? coapplCreationId : memId);
			req.setCompanyIdTemp(applicationMasterData.getBranchId());
			req.setIsUpdateCall(updateCall ? "Y" : "N");

			req.setLastName(useCoApplicant ? coAppLastName : appLastName);
			Map<String, CoapplicantCreationRequestFields> mapReq = new HashMap<>();
			mapReq.put("body", req);
			logger.debug("JSON request for coapplicant creation api :" + mapReq);
			logger.debug("Final request object: {}", new Gson().toJson(req));

			LoanRequestExt loanCreationRequestExt = new LoanRequestExt();
			loanCreationRequestExt.setAppId(appId);
			loanCreationRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.COAPPLICANT_UPDATION_INTF.getKey()));
			loanCreationRequestExt.setRequestObj(mapReq);
			logger.debug("Coapplicant creation API: {} ", loanCreationRequestExt.toString());

			setRequestLog(loanCreationRequestExt.toString());

			Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, loanCreationRequestExt,
					loanCreationRequestExt.getInterfaceName());
			logger.debug("response 1 from the API:"+ apiRespMono);

			return apiRespMono.flatMap(val -> {
				logger.debug("response 2 from the API: " + val);
				JSONObject apiResp = new JSONObject(new Gson().toJson(val));
				logger.debug("JSON response 3 from the API: " + apiResp);
				return Mono.just(apiResp);
			});

		} catch (Exception e) {
			logger.error("Error occurred while executing the Cust Orchestration api, error = " + e);
			return Mono.just(adapterUtil.setError("Error occurred while executing the Cust Orchestration api.", "1"));
		}
	}

	private static String normalize(String str) {
		return str.toLowerCase().replaceAll("[^a-z0-9]", "");
	}

	public JSONArray getAllAddresses(ApplyLoanRequestFields req, String custId) {

		Gson gson = new Gson();
		JSONArray finalArr = new JSONArray();

		try {
			for (AddressDetails addr : req.getAddressDetailsWrapperList().get(0).getAddressDetailsList()) {
				if (String.valueOf(addr.getCustDtlId()).equalsIgnoreCase(custId)) {

					if (addr.getAddressType().equalsIgnoreCase("Occupation")) {
						AddressDetailsPayload payload = gson.fromJson(addr.getPayloadColumn(), AddressDetailsPayload.class);

						for (Address address : payload.getAddressList()) {
							JSONObject addrObj = new JSONObject();
							String country = address.getCountry().equalsIgnoreCase("India") ? "IN" : " ";
							addrObj.put("address1", address.getAddressLine1());
							addrObj.put(Constants.ADDRESS_TYPE, address.getAddressType().toUpperCase());
							addrObj.put("pinCode", address.getPinCode());
							addrObj.put("residenceArea", address.getArea());
							addrObj.put("residenceCity", address.getCity());
							addrObj.put("residenceCountry", country);
							addrObj.put("residenceState", address.getState());
							addrObj.put("address2", address.getAddressLine2());
							addrObj.put("address3", address.getAddressLine3());
							addrObj.put("landmark", address.getLandMark() == null ? "" : address.getLandMark());
							if (StringUtils.isNotBlank(addrObj.getString("residenceState"))
									&& StringUtils.isNotBlank(addrObj.getString("address1"))
									&& StringUtils.isNotBlank(addrObj.getString("pinCode"))) {
								finalArr.put(addrObj);
							}
						}

					} else if (addr.getAddressType().equalsIgnoreCase("Personal")) {
						AddressDetailsPayload payload = gson.fromJson(addr.getPayloadColumn(), AddressDetailsPayload.class);

						for (Address address : payload.getAddressList()) {
							if (Constants.SECONDARY_KYC.equalsIgnoreCase(address.getAddressType())) {
								continue;
							}
							String country = address.getCountry().equalsIgnoreCase("India") ? "IN" : " ";
							JSONObject addrObj = new JSONObject();
							String landMark = address.getLandMark() == null ? "" : StringUtils.defaultString(address.getLandMark());
							addrObj.put("address1", StringUtils.defaultString(address.getAddressLine1()));
							addrObj.put(Constants.ADDRESS_TYPE, StringUtils.defaultString(address.getAddressType().toUpperCase()));
							addrObj.put("pinCode", StringUtils.defaultString(address.getPinCode()));
							addrObj.put("residenceArea", StringUtils.defaultString(address.getArea()));
							addrObj.put("residenceCity", StringUtils.defaultString(address.getCity()));
							addrObj.put("residenceCountry", country);
							addrObj.put("residenceState", StringUtils.defaultString(address.getState()));
							addrObj.put("address2", StringUtils.defaultString(address.getAddressLine2()));
							addrObj.put("address3", StringUtils.defaultString(address.getAddressLine3()));
							addrObj.put("landMark", landMark);
							String type = address.getAddressType();
							if (StringUtils.isNotBlank(addrObj.getString("residenceState"))
									&& StringUtils.isNotBlank(addrObj.getString("address1"))
									&& StringUtils.isNotBlank(addrObj.getString("pinCode"))) {
								if ("Present".equalsIgnoreCase(type)) {
									finalArr.put(addrObj);
								} else if ("Permanent".equalsIgnoreCase(type)) {
									finalArr.put(addrObj);
								} else if ("Communication".equalsIgnoreCase(type)) {
									finalArr.put(addrObj);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error in Address details list {}", e);
		}
		return finalArr;
	}


	@CircuitBreaker(name = "fallback", fallbackMethod = "highmarkCheckCBFallback")
	public Mono<Object> highmarkCheckCallback(HighMarkCheckCBRequest highMarkCheckCBRequest, Header header) {
		try {

			final String loanId;
			final String applicantType;

			logger.debug("request from the highmarkCheckCallback API: {} ", highMarkCheckCBRequest.toString());
			loanId = highMarkCheckCBRequest.getRequestObj().getLoanId();

			if (loanId.startsWith("A01")) {
				applicantType = Constants.APPLICANT;
			} else {
				applicantType = Constants.COAPPLICANT;
			}

			Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, highMarkCheckCBRequest,
					highMarkCheckCBRequest.getInterfaceName());
			logger.debug("response 1 from the API: {} ", apiRespMono);

			return apiRespMono.flatMap(val -> {
				logger.debug("response 2 from the API: {} ", val);

				JSONObject apiResp = new JSONObject(new Gson().toJson(val));
				logger.debug("JSON response 3 from the API: {} ", apiResp);

				CibilDetails cblDetails = new CibilDetails();
				CibilDetailsPayload cblPayLoad = new CibilDetailsPayload();

				try {
					// Fetch custDtlId from CustomerDetails table - by - applicationId and
					// customerType
					Optional<CustomerDetails> customerDetails = custDtlRepo.findByApplicationIdAndCustomerType(
							highMarkCheckCBRequest.getApplicationId(), applicantType);
					if (customerDetails.isPresent()) {
						cblDetails.setCustDtlId(customerDetails.get().getCustDtlId());
					} else {
						logger.debug("No customer details found for the given applicationId and customerType.");
						return Mono.just(adapterUtil.setError(
								"No customer details found for the given applicationId and customerType", "1"));
					}

					// check for existing record
					Optional<CibilDetails> cblDetailsExtg = cibilDtlRepo.findByApplicationIdAndAppIdAndCustDtlId(
							highMarkCheckCBRequest.getApplicationId(), highMarkCheckCBRequest.getAppId(),
							customerDetails.get().getCustDtlId());

					if (cblDetailsExtg.isPresent()) {
						/*
						 * If present delete the original record from CibilDetails and move it to
						 * CibilDetailsHistory table and then insert as new record in CibilDetails
						 */

						CibilDetails cibilDetails = cblDetailsExtg.get();

						// Create a new instance of CibilDetailsHistory
						CibilDetailsHistory cblHistory = new CibilDetailsHistory();

						// Map fields from CibilDetails to CibilDetailsHistory
						cblHistory.setCbDtlId(cibilDetails.getCbDtlId());
						cblHistory.setApplicationId(cibilDetails.getApplicationId());
						cblHistory.setAppId(cibilDetails.getAppId());
						cblHistory.setVersionNum(cibilDetails.getVersionNum());
						cblHistory.setCustDtlId(cibilDetails.getCustDtlId());
						cblHistory.setRequestId(cibilDetails.getRequestId());
						cblHistory.setResponseId(cibilDetails.getResponseId());
						cblHistory.setCbDate(cibilDetails.getCbDate());
						cblHistory.setCbStatus(cibilDetails.getCbStatus());

						cblHistory.setPayloadColumn(cibilDetails.getPayloadColumn());

						// Save the history record
						cibilDtlHisRepo.save(cblHistory);

						// delete the original record from CibilDetails
						cibilDtlRepo.deleteByApplicationIdAndAppIdAndCustDtlId(
								highMarkCheckCBRequest.getApplicationId(), highMarkCheckCBRequest.getAppId(),
								customerDetails.get().getCustDtlId());
						logger.info("Moved record from CibilDetails to CibilDetailsHistory successfully.");
					} else {
						logger.warn("No record found in CibilDetails for the given criteria.");
						// insert as new record in CibilDetails
					}

					// response persistence
					BigDecimal cbDtlId = CommonUtils.generateRandomNum();
					cblDetails.setCbDtlId(cbDtlId);

					cblDetails.setAppId(highMarkCheckCBRequest.getAppId()); // APZCOB
					cblDetails.setVersionNum(1);
					cblDetails.setApplicationId(highMarkCheckCBRequest.getApplicationId()); // 77777777

					cblDetails.setCbDate(LocalDate.now());
					// Set status
					if (apiResp.getString("FINAL_DECISION").equalsIgnoreCase("PASS")) {
						cblDetails.setCbStatus("PASS");
					} else {
						cblDetails.setCbStatus("FAIL");
					}

					cblPayLoad.setBureauName("Highmark");
					cblPayLoad.setHitNohit(apiResp.getString("Inquiry_decision"));
					cblPayLoad.setTotIndebtness(apiResp.getString("Unsecured_Indebtedness"));
					String score = extractValue(apiResp.getString("CB_SCORE"));
					cblPayLoad.setCbScore(score);
					cblPayLoad.setNoParInLastMonths(apiResp.getString("Industry_DPD"));
					cblPayLoad.setMaxDpdInLastMonths(apiResp.getString("CAGL_DPD"));
					cblPayLoad.setWrittenOff(apiResp.getString("Total_WrittenOffAmount"));
					cblPayLoad.setWriteOffAmt(apiResp.getString("Total_WrittenOffAmount"));
					cblPayLoad.setOverlapWithMmfl(apiResp.getString("OVERLAP_WITH_MMFL"));
					cblPayLoad.setOverdueAmt(apiResp.getString("Total_overdue"));

					String eligibleAmount = apiResp.has("ELIGIBLE_AMOUNT") ? apiResp.get("ELIGIBLE_AMOUNT").toString()
							: "0";
					cblPayLoad.setEligibleAmt(eligibleAmount);

					cblPayLoad.setCgpDpd(apiResp.getString("CAGL_DPD"));
					cblPayLoad.setFinalDecision(apiResp.getString("FINAL_DECISION"));
					cblPayLoad.setFoir("" + apiResp.get("FOIR"));

					cblPayLoad.setConsentType("OTP");
					cblPayLoad.setCbLoanId(loanId);

					Properties prop = CommonUtils.readPropertyFile();

					cblPayLoad.setCbReport(prop.getProperty(CobFlagsProperties.HIGHMARK_REPORT_URL.getKey()) + "?losId="
							+ apiResp.get("request_id").toString() + "&customerId="
							+ highMarkCheckCBRequest.getMemberId() + "&loanId=" + loanId);

					String payload = new Gson().toJson(cblPayLoad);
					logger.debug("cibilPayload : " + payload);
					cblDetails.setPayloadColumn(payload);

					cibilDtlRepo.save(cblDetails);
					logger.debug("Data inserted" + cblDetails.toString());
				} catch (Exception ex) {
					logger.error("Error occurred while executing the highmarkCheckCallback details, error = "
							+ ex.getMessage());
					return Mono.just(
							adapterUtil.setError("Error occurred while executing the highmarkCheckCallback api.", "1"));
				}

				String jsonString = new Gson().toJson(cblDetails);
				JSONObject finalResp = adapterUtil.setSuccessResp(jsonString);

				return Mono.just(finalResp);
			});

		} catch (Exception e) {
			logger.error("Error occurred while executing the highMarkCallback api, error = " + e.getMessage());
			return Mono
					.just(adapterUtil.setError("Error occurred while executing the highmarkCheckCallback api.", "1"));
		}

	}

	private String getDefaultValueIfObjNull(Object obj) {
		String value = "";
		if (null != obj) {
			value = String.valueOf(obj).trim();
		}
		return value;
	}

	@CircuitBreaker(name = "fallback", fallbackMethod = "wipDedupeCheckFallback")
	public Mono<Object> wipDedupeCheck(WipDedupeCheckRequest wipDedupeCheckRequest, Header header) {
		LocalDateTime startDateTime = LocalDateTime.now();
		JSONObject resp = new JSONObject();
		ObjectMapper mapperObj = new ObjectMapper();
		mapperObj.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		String reqStr = "";
		try {
			reqStr = mapperObj.writeValueAsString(wipDedupeCheckRequest);

			logger.debug("Request received for the API {}", reqStr);
			String customerId = wipDedupeCheckRequest.getRequestObj().getCustomerId();
			String primaryKycId = wipDedupeCheckRequest.getRequestObj().getPrimaryKycId();
			logger.debug("Customer ID received: {}", customerId);
			logger.debug("Primary KYC ID received: {}", primaryKycId);
			long count = applicationMasterRepo.countByMemberIdAndPrimaryKycId(customerId, primaryKycId);
			logger.debug("Count from database query: {}", count);
			resp.put(Constants.CUSTOMERID1, customerId);
			resp.put("primaryKycId", primaryKycId);
			if (count > 0) {
				resp.put("result", 1);
			} else {
				resp.put("result", 0);
			}

		} catch (JsonProcessingException e) {

			resp = adapterUtil.setError("Failed to parse the request object.", "1");
		}
		logger.debug("logging the request and response in db");

		logger.error("End : callService");
		return Mono.just(resp);
	}

	@CircuitBreaker(name = "fallback", fallbackMethod = "SignzyPennylessCheckFallback")
	public Mono<Object> SignzyPennylessCheck(SignzyPennylessRequest signzyPennylessRequest, Header header,
											 Properties prop) {
		try {
			logger.debug("request from the API: {} ", signzyPennylessRequest.toString());

			//set pennyStatus and pennyResp as empty
			savePennyDetails(null, signzyPennylessRequest.getApplicationId(), signzyPennylessRequest.getAppId(), "");

			SignzyPennylessCheckRequestExt signzyPennylessCheckRequestExt = new SignzyPennylessCheckRequestExt();
			signzyPennylessCheckRequestExt.setAppId(signzyPennylessRequest.getAppId());
			signzyPennylessCheckRequestExt
					.setInterfaceName(prop.getProperty(CobFlagsProperties.SIGNZY_PENNYLESS_CHECK_INTF.getKey()));
			signzyPennylessCheckRequestExt.setRequestObj(signzyPennylessRequest.getRequestObj());
			logger.debug("signzyPennylessCheckRequestExt from the API: {} ", signzyPennylessCheckRequestExt.toString());
			Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, signzyPennylessCheckRequestExt,
					signzyPennylessCheckRequestExt.getInterfaceName());
			logger.debug("response 1 from the API: {} ", apiRespMono);

			return apiRespMono.flatMap(val -> {
				String pennyStatus = "Fail";
				logger.debug("response 2 from the API: {} ", val);
				JSONObject resp = null;
				JSONObject apiResp = new JSONObject(new Gson().toJson(val));
				logger.debug("JSON response 3 from the API: {} ", apiResp);

				if (!apiResp.isEmpty()) {
					if (apiResp.has("result") && !apiResp.getJSONObject("result").isEmpty()) {
						if (apiResp.getJSONObject("result").has(Constants.REASON)) {
							String reason = apiResp.getJSONObject("result").getString(Constants.REASON);
							logger.debug("response :" + reason);
							if (ResponseCodes.SUCCESS.getValue().equalsIgnoreCase(reason)) {
								resp = adapterUtil.setSuccessResp(apiResp.toString());
								pennyStatus = "Pass";
							} else {
								logger.error("Error occurred while executing the Signzy Pennyless Check api, error = "
										+ reason + ",Response = " + apiResp.toString());
								savePennyDetails(apiResp, signzyPennylessRequest.getApplicationId(), signzyPennylessRequest.getAppId(), pennyStatus);
								//
								saveLog(signzyPennylessRequest.getApplicationId(), "SignzyPennylessCheck", signzyPennylessRequest.toString(),
										apiResp.toString(), ResponseCodes.FAILURE.getValue(), apiResp.toString(), "");
								return Mono.just(adapterUtil.setError("" + reason, "0"));
							}
						}
					} else {
						if(apiResp.has(Constants.ERROR1) && !apiResp.getJSONObject(Constants.ERROR1).isEmpty()) {
							String errorMsg = apiResp.getJSONObject(Constants.ERROR1).getString(Constants.MESSAGE);
							String errorReason = apiResp.getJSONObject(Constants.ERROR1).getString(Constants.REASON);
							logger.error("Error occurred while executing the Signzy Pennyless Check api, error = "
									+ errorMsg + ",Reason = " + errorReason);
							savePennyDetails(apiResp, signzyPennylessRequest.getApplicationId(), signzyPennylessRequest.getAppId(), pennyStatus);
							return Mono.just(adapterUtil.setError(errorMsg, "0"));
						}else {
							logger.error(
									"Error occurred while executing the Signzy Pennyless Check api, error = result is empty");
							savePennyDetails(apiResp, signzyPennylessRequest.getApplicationId(), signzyPennylessRequest.getAppId(), pennyStatus);
							//
							saveLog(signzyPennylessRequest.getApplicationId(), "SignzyPennylessCheck", signzyPennylessRequest.toString(),
									apiResp.toString(), ResponseCodes.FAILURE.getValue(), apiResp.toString(), "");
							return Mono.just(adapterUtil.setError(
									"Error occurred while executing the Signzy Pennyless Check api, error = result is empty",
									"1"));
						}

					}
				} else {
					logger.error(
							"Error occurred while executing the Signzy Pennyless Check api, error = empty response");
					savePennyDetails(apiResp, signzyPennylessRequest.getApplicationId(), signzyPennylessRequest.getAppId(), pennyStatus);
					saveLog(signzyPennylessRequest.getApplicationId(), "SignzyPennylessCheck", signzyPennylessRequest.toString(),
							apiResp.toString(), ResponseCodes.FAILURE.getValue(), apiResp.toString(), "");
					return Mono.just(adapterUtil.setError(
							"Error occurred while executing the Signzy Pennyless Check api, error = empty response",
							"1"));
				}


				/*
				 * ResponseBody responseBody = new ResponseBody(); Response response = new
				 * Response(); ResponseHeader responseHeader = new ResponseHeader();
				 * responseHeader.setResponseCode(ResponseCodes.SUCCESS.getKey()); Gson gson =
				 * new Gson(); String responseStr = gson.toJson(val);
				 * responseBody.setResponseObj(responseStr);
				 * response.setResponseBody(responseBody);
				 * response.setResponseHeader(responseHeader);
				 */
				savePennyDetails(apiResp, signzyPennylessRequest.getApplicationId(), signzyPennylessRequest.getAppId(), pennyStatus);
				return Mono.just(resp);
			});
		} catch (Exception e) {
			logger.error("Error occurred while executing the Signzy Pennyless Check api, error = " + e.getMessage());
			saveLog(signzyPennylessRequest.getApplicationId(), "SignzyPennylessCheck", signzyPennylessRequest.toString(),
					e.getMessage(), ResponseCodes.FAILURE.getValue(), e.getMessage(), "");
			return Mono
					.just(adapterUtil.setError("Error occurred while executing the Signzy Pennyless Check api.", "1"));
		}
	}


	private Mono<Object> savePennyDetails(JSONObject apiResp, String applicationId, String appId, String pennyStatus) {
		logger.debug("apiResp : {}", apiResp);
		logger.debug("applicationId : {}", applicationId);
		logger.debug("pennyStatus : {}", pennyStatus);
		try {
			//
//		Optional<BankDetails> bankDetailsDb = bankDtlRepo.findByApplicationId(applicationId);
			Optional<BankDetails> bankDetailsDb = bankDtlRepo.findBankDetailsByCustomerType(Constants.APPLICANT, applicationId);
			ObjectMapper objectMapper = new ObjectMapper();
			Gson gson = new Gson();
			BankDetails existingBankRecord = null;
			if (bankDetailsDb.isPresent()) {
				logger.debug("bankDetails data found: ");
				existingBankRecord = bankDetailsDb.get();

				BankDetailsPayload bankDetailsPayload = objectMapper.readValue(existingBankRecord.getPayloadColumn(),
						BankDetailsPayload.class);
				if(pennyStatus.equalsIgnoreCase("Pass")){
					bankDetailsPayload.setPennyResp(apiResp.toString());
				}else {
					bankDetailsPayload.setPennyResp("");
				}

				bankDetailsPayload.setPennyCheckStatus(pennyStatus);
				existingBankRecord.setPayloadColumn(gson.toJson(bankDetailsPayload));
				logger.debug("bankDetailsPayload " + existingBankRecord.getPayloadColumn());

				bankDtlRepo.save(existingBankRecord);

				logger.warn("Completed updating Bank Details - TB_CGOB_BANK_DTLS for loans");
				return Mono.just(adapterUtil.setSuccessResp(existingBankRecord.toString()));
			}else {
				logger.debug("record not found for bankDetails " + "applicationId :" +applicationId);
				return Mono.just(adapterUtil.setError("application not found.", "1"));
			}
		} catch (Exception ex) {
			logger.error("Error occurred while executing savePennyDetails, error = " + ex.getMessage());
			return Mono.just(adapterUtil.setError("Error occurred while savePennyDetails api.", "1"));

		}

	}


	private Mono<Object> validateKycFallback(ValidateKycRequest validateKycRequest, Header header, Properties prop,
											 Exception e) {
		logger.error("validateKycFallback error : ", e);
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Mono<Object> kycDedupeFallback(KycDedupeRequest kycDedupeRequest, Header header, Properties prop,
										   Exception e) {
		logger.error("kycDedupeFallback error : ", e);
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Mono<Object> fetchIFSCFallback(FetchIFSCRequest fetchIFSCRequest, Header header, Properties prop,
										   Exception e) {
		logger.error("fetchIFSCFallback error : ", e);
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Mono<Object> fetchExistingLoanFallback(ExistingLoanRequest existingLoanRequest, Header header,
												   Properties prop, Exception e) {
		logger.error("fetchExistingLoanFallback error : ", e);
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Mono<Object> highmarkCheckFallback(HighMarkCheckRequest highmarkCheckRequest, Header header,
											   Properties prop, Exception e) {
		logger.error("highmarkCheckFallback error : ", e);
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Mono<Object> breCBCheckFallback(
			BRECBRequest breCBCheckRequest,
			Header header,
			Properties prop,
			Exception e
	) {
		logger.error("breCBCheckFallback error", e);

		String currentStatus = breCBCheckRequest.getRequestObj()
				.getBreCBValuesRequestvalues1()
				.getBreCBInputRequestinput1()
				.getBreCBValuesRequestvalues2()
				.getBreCBInputRequestinput2()
				.getCurrentStage();

		if (currentStatus != null &&
				(
						currentStatus.toUpperCase().contains(AppStatus.CACOMPLETED.getValue().toUpperCase()) ||
								currentStatus.toUpperCase().contains(AppStatus.RESANCTION.getValue().toUpperCase())
				)
		) {
			return Mono.error(e);
		}
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Mono<Object> bIPFallback(BIPMasterRequest apiRequest, Header header, Properties prop,
									 Exception e) {
		logger.error("bIPFallback error : ", e);
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Mono<Object> loanCreationFallback(String applicationId, Header header, Properties prop,
											  Exception e) {
		logger.error("loanCreationFallback error : ", e);
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Mono<Object> coApplicantCreationFallback(String applicationId, Header header, Properties prop,
													 Exception e) {
		logger.error("coApplicantCreationFallback error : ", e);
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Mono<Object> breCBReportFallback(BRECBReportRequest breCBReportRequest, Header header, Properties prop,
											 boolean isExist, Exception e) {
		logger.error("breCBReportFallback error : ", e);
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Mono<Object> SignzyPennylessCheckFallback(SignzyPennylessRequest signzyPennylessRequest, Header header,
													  Properties prop, Exception e) {
		logger.error("SignzyPennylessCheckFallback error : ", e);
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Mono<Object> highmarkCheckCBFallback(HighMarkCheckCBRequest highMarkCheckCBRequest, Header header,
												 Exception e) {
		logger.error("highmarkCheckCBFallback error : ", e);
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Mono<Object> workitemCreationFallback(WorkitemCreationRequest workitemCreationRequest, Header header,
												  Properties prop, Exception e) {
		logger.error("workitemCreationFallback error : ", e);
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Mono<Object> sendBackWorkitemsFallback(SendbackWorkitemRequest sendbackWorkitemRequest, Header header,
												   Properties prop, Exception e) {
		logger.error("sendBackWorkitemsFallback error : ", e);
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Mono<Object> newgenWipDedupeFallback(WipDedupeRequest wipDedupeRequest, Header header, Properties prop,
												 Exception e) {
		logger.error("newgenWipDedupeFallback error : ", e);
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Mono<Object> sendBackDataFetchFallback(SendbackDataFetchRequest sendbackDataFetchRequest, Header header,
												   Properties prop, Exception e) {
		logger.error("sendBackDataFetchFallback error : ", e);
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Mono<Object> wipDedupeCheckFallback(WipDedupeCheckRequest wipDedupeCheckRequest, Header header,
												Exception e) {
		logger.error("wipDedupeCheckFallback error : ", e);
		return FallbackUtils.genericFallbackMonoObject();
	}

	private Response rpcUploadLoanFallback(UploadLoanRequest uploadLoanRequest, Exception e) {
		logger.error("rpcUploadLoanFallback error : ", e);
		return CommonUtils.setError(e.getMessage(), "");

	}

	// A
	@CircuitBreaker(name = "fallback", fallbackMethod = "breCBReportFallback")
	public Mono<Object> breCBReport(BRECBReportRequest brecbReportRequest, Header header, Properties prop,
									boolean isExist) {

		try {


			logger.debug("request from the breCBReport API: {} ", brecbReportRequest.toString());
			BRECBCheckRequestExt CBCheckRequestExt = new BRECBCheckRequestExt();
			CBCheckRequestExt.setAppId(brecbReportRequest.getAppId());
			CBCheckRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.BRE_CB_REPORT_INTF.getKey()));
			CBCheckRequestExt.setRequestObj(brecbReportRequest.getRequestObj());

			String applicationNum = brecbReportRequest.getLoanId().substring(3); // 2313213131311
			String fileName = brecbReportRequest.getLoanId() + ".pdf"; // A012313213131311.pdf
			String uploadLocation = prop.getProperty(CobFlagsProperties.FILE_UPLOAD.getKey()) + "/"
					+ brecbReportRequest.getAppId() + Constants.LOANPATH + applicationNum + "/";
			logger.debug("request from the breCBReport API: {} ", brecbReportRequest.toString());
			JSONObject resp = null;
			String filePath1 = uploadLocation + fileName;
			File file = new File(filePath1);

			// If the file exists, read it and convert to Base64
			if (isExist && file.exists()) {
				try {
					logger.debug("file exist ");
					byte[] fileContent = Files.readAllBytes(file.toPath());
					String base64String = java.util.Base64.getEncoder().encodeToString(fileContent);

					resp = adapterUtil.setSuccessResp("success");
					resp.put("status", "success");
					resp.put("filePath", filePath1);
					resp.put("base64", base64String);

					return Mono.just(resp);

				} catch (IOException e) {
					JSONObject errorResp = new JSONObject();
					errorResp.put("status", Constants.ERROR1);
					errorResp.put(Constants.MESSAGE, "File reading failed.");
					return Mono.just(errorResp);

				}
			} else {
				// If file does not exist
				logger.debug("file not exist ");
				return interfaceAdapter
						.callExternalService(header, CBCheckRequestExt, CBCheckRequestExt.getInterfaceName())
						.flatMap(val -> {
							logger.debug("val: {}", val.getClass().getName());
							logger.debug("val: {}", val);
							if (val instanceof byte[]) {
								if (CommonUtils.isPdf((byte[]) val)) {
									logger.debug("val: inside");
									// InputStream pdfStream = (InputStream) val;

									// Write the PDF to file
									Path writtenFilePath = writeBytePdfToFile((byte[]) val, uploadLocation, fileName);

									// Log the file path where the PDF was written
									logger.debug("File written to: {}", writtenFilePath);

									// Convert the file to Base64
									String base64String = convertFileToBase64(writtenFilePath);
									logger.debug("base64String generated");
									// Create a success response
									JSONObject resp1 = adapterUtil.setSuccessResp("success");
									resp1.put("status", "success");
									resp1.put("filePath", writtenFilePath.toString());
									resp1.put("base64", base64String);

									return Mono.just(resp1);
								} else if (CommonUtils.isJson((byte[]) val)) {
									JSONObject errorResp = new JSONObject();
									try {
										JSONObject response = CommonUtils.convertByteStreamToJson((byte[]) val);
										errorResp.put(Constants.MESSAGE, response.getString(Constants.MESSAGE));
									} catch (IOException e) {
										logger.error("Error converting byte stream to JSON: {}", e);
									}
									// If the response is not an InputStream, return an error response

									errorResp.put("status", Constants.ERROR1);

									return Mono.just(errorResp);
								} else {
									// If the response is not an InputStream, return an error response
									JSONObject errorResp = new JSONObject();
									errorResp.put("status", Constants.ERROR1);
									errorResp.put(Constants.MESSAGE, "Invalid response format.");
									saveLog(applicationNum, "breCBReport", CBCheckRequestExt.toString(),
											errorResp.toString(), ResponseCodes.FAILURE.getValue(), errorResp.toString(), "");
									return Mono.just(errorResp);
								}
							} else {
								// If the response is not an InputStream, return an error response
								JSONObject errorResp = new JSONObject();
								errorResp.put("status", Constants.ERROR1);
								errorResp.put(Constants.MESSAGE, "Invalid response format.");
								saveLog(applicationNum, "breCBReport", CBCheckRequestExt.toString(),
										errorResp.toString(), ResponseCodes.FAILURE.getValue(), errorResp.toString(), "");
								return Mono.just(errorResp);
							}
						});
			}
		} catch (Exception e) {
			logger.error("Error occurred while executing the BRE CB Report api, error = " + e.getMessage());
			saveLog(brecbReportRequest.getLoanId().substring(3), "breCBReport", brecbReportRequest.toString(),
					e.getMessage(), ResponseCodes.FAILURE.getValue(), e.getMessage(), "");
			return Mono.just(adapterUtil.setError("Error occurred while executing the BRE CB Report api.", "1"));
		}

	}

	// Method to write InputStream to a file
	private Path writeBytePdfToFile(byte[] fileContent, String filePath, String fileName) {
//      Path filePath = Paths.get("some/directory", fileName); // Define the folder where the PDF will be saved
		Path path = Paths.get(filePath, fileName);
		logger.debug("path: " + path.toString());
		try {
			// Ensure the directories exist
			Files.createDirectories(path.getParent());

			try (FileOutputStream fos = new FileOutputStream(path.toString())) {
				fos.write(fileContent); // Write the byte array to the file
				logger.debug("PDF file successfully written to " + filePath);
			} catch (IOException e) {
				logger.error("Error writing PDF file: " + e.getMessage());
			}
		} catch (IOException e) {
			logger.error("Error writing file: " + e.getMessage(), e);
			throw new RuntimeException("Error writing file: " + e.getMessage(), e);
		}

		return path;
	}

	/**
	 * Converts a file's content to a Base64 string.
	 *
	 * @param filePath Path to the file
	 * @return Base64 encoded string
	 */

	// Method to convert a file to Base64 string
	private String convertFileToBase64(Path filePath) {
		try {
			byte[] fileContent = Files.readAllBytes(filePath); // Read the file content
			return java.util.Base64.getEncoder().encodeToString(fileContent);
//            return new sun.misc.BASE64Encoder().encode(fileContent);

		} catch (IOException e) {
			logger.error("Error converting file to Base64: " + e.getMessage(), e);
			throw new RuntimeException("Error converting file to Base64: " + e.getMessage(), e);
		}
	}

	private String extractValue(String input) {
		// Normalize delimiters: Remove unnecessary spaces around ":" and ","
		String normalizedInput = CommonUtils.normalizeInput(input);

		// Find the first key-value pair ("Score : 650")
		int colonIndex = normalizedInput.indexOf(":");
		if (colonIndex != -1) {
			// Get the value after the colon
			int startIndex = colonIndex + 1;
			int endIndex = normalizedInput.indexOf(",", startIndex); // Find the next comma
			if (endIndex == -1) { // If no comma, find the next space or take till the end
				endIndex = normalizedInput.indexOf(" ", startIndex);
				if (endIndex == -1) {
					endIndex = normalizedInput.length(); // No space found; take till the end
				}
			}
			return normalizedInput.substring(startIndex, endIndex).trim(); // Extract and trim
		}
		return null; // No key-value pair found
	}

	//A
	public Response downloadLoanApplication(FetchAppRequest fetchAppReq) {
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();
		String applicationId = fetchAppReq.getRequestObj().getApplicationId();
		String appId = fetchAppReq.getRequestObj().getAppId();
		int versionNum = fetchAppReq.getRequestObj().getVersionNum();

		CustomerDataFields custmrDataFields = null;
		Optional<ApplicationMaster> applicationMasterOpt = applicationMasterRepo
				.findByAppIdAndApplicationIdAndVersionNum(appId, applicationId, versionNum);

		if (applicationMasterOpt.isPresent()) {
			ApplicationMaster applicationMasterData = applicationMasterOpt.get();
			custmrDataFields = cobService.getCustomerData(applicationMasterData, applicationId, appId, versionNum);
			logger.debug("custmrDataFields.toString() : " + custmrDataFields.toString());

			List<ApplicationWorkflow> workflow;
			workflow = applnWfRepository.findByAppIdAndApplicationIdAndVersionNumOrderByWorkflowSeqNumAsc(appId,
					applicationId, versionNum);
			logger.debug("Workflow details {} ", workflow);

			String bmId = "-";
			String kmId = "-";
			String usernameKM = "-";
			String usernameBM = "-";
			String kmSubmDateStr = "";
			String bmSubmDateStr = "";

			if (!workflow.isEmpty()) {
				custmrDataFields.setApplicationWorkflowList(workflow);

				LocalDateTime kmSubmDate = null;
				for (ApplicationWorkflow applnWorkflow : custmrDataFields.getApplicationWorkflowList()) {
					if (Constants.INITIATOR.equalsIgnoreCase(applnWorkflow.getCurrentRole()) && (WorkflowStatus.APPROVED
							.getValue().equalsIgnoreCase(applnWorkflow.getApplicationStatus())
							|| WorkflowStatus.PENDING_FOR_APPROVAL.getValue()
							.equalsIgnoreCase(applnWorkflow.getApplicationStatus()))) {
						kmId = applnWorkflow.getCreatedBy();
						kmSubmDate = applnWorkflow.getCreateTs();

						try {
							kmSubmDateStr = CommonUtils.formatDateTimeToDateStr(kmSubmDate);
							logger.debug("Formatted kmSubmDateStr: " + kmSubmDateStr);
						} catch (Exception e) {
							logger.error("error while formatted date : " + e);
						}
					}
				}

				if (Constants.NEW_LOAN_PRODUCT_CODE.equals(applicationMasterData.getProductCode())) {
					logger.info("Unnati application");

					String previousWorkflowStatus = null;
					for (ApplicationWorkflow appnWorkflow : custmrDataFields.getApplicationWorkflowList()) {
						String currentStatus = appnWorkflow.getApplicationStatus();
						if (Constants.APPROVED.equalsIgnoreCase(appnWorkflow.getApplicationStatus())) {
							if (WorkflowStatus.PENDING_FOR_APPROVAL.getValue()
									.equalsIgnoreCase(previousWorkflowStatus)) {
								bmId = appnWorkflow.getCreatedBy();
								LocalDateTime bmSubmDate = appnWorkflow.getCreateTs();

								try {
									bmSubmDate = kmSubmDate;
									bmSubmDateStr = CommonUtils.formatDateTimeToDateStr(bmSubmDate);
									logger.debug("Formatted bmSubmDateStr: {}", bmSubmDateStr);
								} catch (Exception e) {
									logger.error("Error while formatting date: ", e);
								}
							}
						}
						previousWorkflowStatus = currentStatus;
					}

				} else if (Constants.RENEWAL_LOAN_PRODUCT_CODE
						.equals(custmrDataFields.getApplicationMaster().getProductCode())) {
					logger.info("Renewal Unnati application");
					List<ApplicationWorkflow> list = custmrDataFields.getApplicationWorkflowList();

					for (int i = list.size() - 1; i >= 0; i--) {

						ApplicationWorkflow appnWorkflow = list.get(i);
						String currentStatus = appnWorkflow.getApplicationStatus();

						if (Constants.RPCVERIFIED.equalsIgnoreCase(currentStatus)) {

							// Check next workflow (forward direction)
							if (i + 1 < list.size()) {
								ApplicationWorkflow nextWorkflow = list.get(i + 1);
								String nextRole = nextWorkflow.getCurrentRole();
								String nextStatus = nextWorkflow.getApplicationStatus();
								logger.info("nextStatus - after RPCVERIFIED -" + nextStatus);
								if (Constants.APPROVER.equalsIgnoreCase(nextRole)) {
									// Take Approver's createdBy
									bmId = nextWorkflow.getCreatedBy();
									LocalDateTime bmSubmDate = nextWorkflow.getCreateTs();

									try {
										bmSubmDate = kmSubmDate;
										bmSubmDateStr = CommonUtils.formatDateTimeToDateStr(bmSubmDate);
										logger.debug("Formatted bmSubmDateStr: {}", bmSubmDateStr);
									} catch (Exception e) {
										logger.error("Error while formatting date: ", e);
									}
								}
							}
							break;
						}
					}

				} else {
					logger.info("Not an Unnati/Renewal application");
				}

				Optional<String> usernameOptKM = tbUserRepository.findUserNameByUserId(kmId);
				usernameKM = usernameOptKM.orElse("");
				logger.info("usernameKM : " + usernameKM);

				Optional<String> usernameOptBM = tbUserRepository.findUserNameByUserId(bmId);
				usernameBM = usernameOptBM.orElse("");
				logger.info("usernameBM : " + usernameBM);

			}

			Gson gsonObj = new Gson();
			LoanDetailsPayload payload = gsonObj.fromJson(custmrDataFields.getLoanDetails().getPayloadColumn(),
					LoanDetailsPayload.class);

			String inputlanguage = payload.getLanguage();
			String language = Constants.DEFAULTLANGUAGE;
			logger.debug("finputlanguage Language:" + inputlanguage);
			if(inputlanguage.equalsIgnoreCase("Kannada")){
				language = "Kannada";
			}
			logger.debug("final Language:" + language);

			String fileName = "jsonKeysFor" + language + "LoanApplication.json"; // jsonKeysForKannadaLoanApplication.json

			logger.debug("fileName :" + fileName);
			JSONObject keysForContent = new JSONObject();
			JSONObject fileContent = new JSONObject();
			try {
				try {
					fileContent = new JSONObject(adapterUtil.readJSONContentFromServer("LOANAPPLICATION/" + fileName));
					logger.debug("fileContent 1: " + fileContent.toString());
				} catch (IOException e) {
					logger.error(e.getMessage());
				}
				keysForContent = fileContent.getJSONObject("keysForContent");
				logger.debug("fileContent 2: " + keysForContent.toString());

			} catch (JSONException e) {
				logger.error(e.getMessage());
				responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
				responseBody.setResponseObj(e.getMessage());
				response.setResponseHeader(responseHeader);
				response.setResponseBody(responseBody);
			}
			logger.debug("Fetching json files 2: " + keysForContent.toString());
			try {
				response = new LoanApplication().generateLoanApplicationPdf(applicationMasterData,custmrDataFields, keysForContent,language,kmId, bmId, kmSubmDateStr, bmSubmDateStr, usernameKM, usernameBM);
			} catch (Exception e) {
				logger.error(e.getMessage());
				responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
				responseBody.setResponseObj(e.getMessage());
				response.setResponseHeader(responseHeader);
				response.setResponseBody(responseBody);
			}
		} else {
			responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
			responseBody.setResponseObj(Constants.APP_MASTER_NOT_FOUND);
			response.setResponseHeader(responseHeader);
			response.setResponseBody(responseBody);
		}
		return response;
	}

	public JSONObject readSpecificJsonFile(String fileName) {
		JSONObject content = new JSONObject();
		try {
			// Use PathMatchingResourcePatternResolver to get resources from the data folder
			logger.debug("Fetching fileName: " + fileName);
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver.getResources("classpath:*.json"); // Read all JSON files
			// in data folder
			logger.debug("Fetching resources: " + resources.toString());
			// Find the resource that matches the specific file name
			Optional<Resource> targetResource = Arrays.stream(resources)
					.filter(resource -> fileName.equals(resource.getFilename()))
					.findFirst();
			logger.debug("Fetching targetResource: " + targetResource.toString());
			logger.debug("Fetching targetResource.getURI(): " + targetResource.get().getURI());
			if (targetResource.isPresent()) {
				// Read and parse the content of the specific file
				content = new JSONObject(new String(Files.readAllBytes(Paths.get(targetResource.get().getURI()))));
			} else {
				logger.debug("File not found: " + fileName);
			}
		} catch (IOException e) {
		}
		return content;
	}

	public Response mergeImageToPdfAndDownload(MergeImageToPdfRequestWrapper mergeImageToPdfRequestWrapper) {
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();
		try {
			String applicationId = mergeImageToPdfRequestWrapper.getApplicationId();
			String applicantType = mergeImageToPdfRequestWrapper.getApplicantType();
			String documentType = mergeImageToPdfRequestWrapper.getDocumentType();
			ObjectMapper objectMapper = new ObjectMapper();
			String PdfResponse = null;
			logger.debug("Inside mergeImageToPdfAndDownload method");
			Properties prop = null;

			prop = CommonUtils.readPropertyFile();
			String filePath = prop.getProperty(CobFlagsProperties.FILE_UPLOAD.getKey()) + "/" + Constants.APPID
					+ Constants.LOANPATH + applicationId + "/";
			logger.debug("File path :: {}", filePath);
			Optional<List<ApplicationDocuments>> documentDetails = appLoanDocsRepository
					.findByApplicationIdAndCustType(applicantType, applicationId);
			List<ApplicationDocumentsPayload> payloads = new ArrayList<>();
			if (documentDetails.isPresent()) {
				for (ApplicationDocuments document : documentDetails.get()) {
					ApplicationDocumentsPayload documentPayload = objectMapper.readValue(document.getPayloadColumn(),
							ApplicationDocumentsPayload.class);
					if (documentType.equalsIgnoreCase(documentPayload.getDocumentName()))
						payloads.add(documentPayload);
				}
				if (payloads.size() != 0) {
					PdfResponse = CommonUtils.mergePDFFiles(StringUtils.EMPTY, payloads, filePath);
					JSONObject resp = new JSONObject();
					resp.put("status", "success");
					resp.put("base64", PdfResponse);
					responseHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());
					responseBody.setResponseObj(resp.toString());
				} else {
					responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
					responseBody.setResponseObj(ResponseCodes.BASE64_DATA_NOT_FOUND.getKey());
				}
			} else {
				responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
				responseBody.setResponseObj(ResponseCodes.BASE64_DATA_NOT_FOUND.getKey());
			}
		} catch (Exception e) {
			logger.error("Error occurred while merging the PDF files: " + e.getMessage());
			responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
			responseBody.setResponseObj(e.getMessage());
		}
		response.setResponseHeader(responseHeader);
		response.setResponseBody(responseBody);
		return response;
	}

	@CircuitBreaker(name = "fallback", fallbackMethod = "rpcUploadLoanFallback")
	public Response uploadLoan(UploadLoanRequest uploadLoanRequest) {
		logger.debug("OnEntry :: uploadLoan");
		Gson gson = new Gson();
		Response fetchUserDetailsResponse = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		fetchUserDetailsResponse.setResponseHeader(responseHeader);
		ResponseBody responseBody = new ResponseBody();
		CustomerDataFields customerDataFields;
		try {
			Properties prop = null;
			try {
				prop = CommonUtils.readPropertyFile();
			} catch (IOException e) {
				logger.error("Error while reading property file in populateRejectedData ", e);
				fetchUserDetailsResponse = CommonUtils.formFailResponse(ResponseCodes.FAILURE.getValue(),
						ResponseCodes.FAILURE.getKey());
			}

			UploadLoanRequestFields requestObj = uploadLoanRequest.getRequestObj();

			String applicationId = requestObj.getApplicationId();
			String appId = requestObj.getAppId();
			int versionNum = requestObj.getVersionNum();
			logger.debug("applicationID : " + applicationId.toString());
			logger.debug("appId : " + appId + ", versionNum : " + versionNum);
			logger.debug("customerType : " + requestObj.getCustomerType());

			ApplicationMaster applicationMasterData;
			Optional<ApplicationMaster> appMasterDb = applicationMasterRepo.findByAppIdAndApplicationIdAndVersionNum(
					requestObj.getAppId(), requestObj.getApplicationId(), requestObj.getVersionNum());
			if (appMasterDb.isPresent()) {
				logger.debug("appMasterDb data found");
				applicationMasterData = appMasterDb.get();
				logger.debug("appMasterDb data found: " + applicationMasterData.toString());
			} else {
				logger.debug("appMasterDb data not found");
				responseHeader.setResponseCode(ResponseCodes.INVALID_APP_MASTER.getKey());
				responseBody.setResponseObj(Constants.APP_MASTER_NOT_FOUND);
				fetchUserDetailsResponse.setResponseBody(responseBody);
				return fetchUserDetailsResponse;
			}

			String stageId = requestObj.getStageId();
			logger.debug("stageId : " + stageId);
			logger.debug("customerType: " + requestObj.getCustomerType());
			String custCode = requestObj.getCustomerType().equalsIgnoreCase("Applicant") ? "A" : "C";
			logger.debug("custCode : " + custCode);
			logger.debug("requestType : " + requestObj.getRequestType());

			ApplicationMaster applicationMasterReq = null;
			switch (stageId) {
				case "1": // primary Kyc Details
					logger.debug("Stage 1 : primary Kyc Details");
					if (requestObj.getRequestType().equalsIgnoreCase("Edit")) {

						applicationMasterReq = requestObj.getApplicationMaster();
						logger.debug("applicationMasterReq: " + applicationMasterReq);
						applicationMasterData.setPrimaryKycId(applicationMasterReq.getPrimaryKycId());
						applicationMasterData.setAlternateVoterId(applicationMasterReq.getAlternateVoterId());

						updateCustomerDtls(requestObj);
						updateEditedFields(1, requestObj, custCode);
					} else if (requestObj.getRequestType().equalsIgnoreCase("query")) {
						updateQueries(1, requestObj, custCode);
					} else {
						updateStageVerification(1, requestObj, custCode);
					}

					break;

				case "2": // Secondary Kyc Details
					if (requestObj.getRequestType().equalsIgnoreCase("Edit")) {
						updateCustomerDtls(requestObj);
						updateAddressDtls(2, requestObj, Constants.SECONDARY_KYC); // personal // present //secondary
						updateEditedFields(2, requestObj, custCode);

						applicationMasterReq = requestObj.getApplicationMaster();
						applicationMasterData.setSecondaryKycType(applicationMasterReq.getSecondaryKycType());
						applicationMasterData.setSecondaryKycId(applicationMasterReq.getSecondaryKycId());

					} else if (requestObj.getRequestType().equalsIgnoreCase("query")) {
						updateQueries(2, requestObj, custCode);
					} else {
						updateStageVerification(2, requestObj, custCode);
					}
					break;

				case "3": // Relationship Proof
					if (requestObj.getRequestType().equalsIgnoreCase("Edit")) {
						updateCustomerDtls(requestObj);
						updateEditedFields(3, requestObj, custCode);
					} else if (requestObj.getRequestType().equalsIgnoreCase("query")) {
						updateQueries(3, requestObj, custCode);
					} else {
						updateStageVerification(3, requestObj, custCode);
					}
					break;

				case "4": // Residence Address Proof
					if (requestObj.getRequestType().equalsIgnoreCase("Edit")) {
						updateAddressDtls(4, requestObj, "present"); // personal //present
						updateEditedFields(4, requestObj, custCode);
					} else if (requestObj.getRequestType().equalsIgnoreCase("query")) {
						updateQueries(4, requestObj, custCode);
					} else {
						updateStageVerification(4, requestObj, custCode);
					}
					break;

				case "5": // Residence Photo
					if (requestObj.getRequestType().equalsIgnoreCase("Edit")) {
						updateAddressDtls(5, requestObj, "present"); // personal //present
						updateEditedFields(5, requestObj, custCode);
					} else if (requestObj.getRequestType().equalsIgnoreCase("query")) {
						updateQueries(5, requestObj, custCode);
					} else {
						updateStageVerification(5, requestObj, custCode);
					}
					break;

				case "6": // Bussiness/Employment Proof
					if (requestObj.getRequestType().equalsIgnoreCase("Edit")) {
						updateOccupationdtls(requestObj);
						updateLoanDtls(requestObj);

						updateEditedFields(6, requestObj, custCode);
					} else if (requestObj.getRequestType().equalsIgnoreCase("query")) {
						updateQueries(6, requestObj, custCode);
					} else {
						updateStageVerification(6, requestObj, custCode);
					}
					break;

				case "7": // Bussiness Address Proof
					if (requestObj.getRequestType().equalsIgnoreCase("Edit")) {
						updateAddressDtls(7, requestObj, "office");
						updateOccupationdtls(requestObj);// occupation //office
						updateEditedFields(7, requestObj, custCode);
					} else if (requestObj.getRequestType().equalsIgnoreCase("query")) {
						updateQueries(7, requestObj, custCode);
					} else {
						updateStageVerification(7, requestObj, custCode);
					}
					break;

				case "8": // Bank Account Proof
					if (requestObj.getRequestType().equalsIgnoreCase("Edit")) {
						updateBankDtls(requestObj);
						updateEditedFields(8, requestObj, custCode);
					} else if (requestObj.getRequestType().equalsIgnoreCase("query")) {
						updateQueries(8, requestObj, custCode);
					} else {
						updateStageVerification(8, requestObj, custCode);
					}
					break;

				case "9": // Other Information
					if (requestObj.getRequestType().equalsIgnoreCase("query")) {
						updateQueries(9, requestObj, custCode);
					} else if (requestObj.getRequestType().equalsIgnoreCase("stageVerification")) {
						updateStageVerification(9, requestObj, custCode);
					}
					break;

				case Constants.UPLOAD_DOCS:
					uploadApplicationDocs(requestObj, applicationId, versionNum);
					uploadDocument(requestObj.getUploadDocumentRequestFields(), prop);

					break;

				case Constants.PENNY_CHECK_DOCS:
					deleteDocument(applicationId, appId);
					updateApplicationDocs(requestObj, applicationId, versionNum);
					uploadDocument(requestObj.getUploadDocumentRequestFields(), prop);

					break;

				case Constants.COAPPLICANT_BANK_DETAILS:
					updateCoApplicantBankDtls(requestObj, applicationId, appId, versionNum);
					break;

				case Constants.APPLICANT_BANK_DETAILS:
					updateCoApplicantBankDtls(requestObj, applicationId, appId, versionNum);
					break;
				default:
					logger.error("Invalid StageId");
					break;
			}

			logger.debug("Stage : " + stageId);
			if (!stageId.equalsIgnoreCase(Constants.COAPPLICANT_BANK_DETAILS)) {
				logger.debug("Stage1 : " + stageId);
				applicationMasterData.setUpdateTs(LocalDateTime.now());
				applicationMasterData.setUpdatedBy(uploadLoanRequest.getUserId());
				applicationMasterRepo.save(applicationMasterData);
				logger.debug("applicationMaster saved");
			}

			customerDataFields = cobService.getCustomerData(applicationMasterData, applicationId, appId, versionNum);
			logger.debug("customerDataFields  : {}" , customerDataFields);
			Optional<BCMPIStageVerification> bcmpiStageData = bcmpiStageVerificationRepository.findById(applicationId);
			if(bcmpiStageData.isPresent()){
				logger.debug("bcmpiStageData found");
				customerDataFields.setBcmpiStatDetails(
						CommonUtils.parseBCMPIStageVerificationData(bcmpiStageData.get().getEditedFields(), bcmpiStageData.get().getQueries()));
				customerDataFields.setBcmpiVerifiedStage(null != bcmpiStageData.get().getVerifiedStages()
						? Arrays.asList(bcmpiStageData.get().getVerifiedStages().split("\\|")) : new ArrayList<>());
			}
			Optional<BCMPIIncomeDetails> bcmpiIncomeDataOpt = bcmpiIncomeDetailsRepo.findById(applicationId);
			if(bcmpiIncomeDataOpt.isPresent()){
				logger.debug("bcmpiIncomeData found");
				BCMPIIncomeDetailsWrapper bcmpiIncomeDetailsWrapper = gson.fromJson(bcmpiIncomeDataOpt.get().getPayload(), BCMPIIncomeDetailsWrapper.class); // Object to be changed to a wrapper class
				BCMPIIncomeDetails bcmpiIncomeDetails = bcmpiIncomeDataOpt.get();
				bcmpiIncomeDetails.setBcmpiIncomeDetailsWrapper(bcmpiIncomeDetailsWrapper);
				customerDataFields.setBcmpiIncomeDetails(bcmpiIncomeDetails);
			}
			Optional<BCMPILoanObligations> bcmpiLoanObligationsOpt = bcmpiLoanObligationsRepo.findById(applicationId);
			if(bcmpiLoanObligationsOpt.isPresent()){
				logger.debug("bcmpiLoanObligations found");
				LoanObligationsWrapper loanObligationsWrapper = gson.fromJson(bcmpiLoanObligationsOpt.get().getPayload(), LoanObligationsWrapper.class);
				BCMPILoanObligations bcmpiLoanObligations = bcmpiLoanObligationsOpt.get();
				bcmpiLoanObligations.setLoanObligationsWrapper(loanObligationsWrapper);
				customerDataFields.setBcmpiLoanObligations(bcmpiLoanObligations);
			}
			Optional<BCMPIOtherDetails> bcmpiOtherDetailsOpt = bcmpiOtherDetailsRepo.findById(applicationId);
			if(bcmpiOtherDetailsOpt.isPresent()){
				logger.debug("bcmpiIncomeData found");
				BCMPIOtherDetailsWrapper bcmpiOtherDetailsWrapper = gson.fromJson(bcmpiOtherDetailsOpt.get().getPayload(), BCMPIOtherDetailsWrapper.class);
				BCMPIOtherDetails bcmpiOtherDetails = bcmpiOtherDetailsOpt.get();
				bcmpiOtherDetails.setBcmpiOtherDetailsWrapper(bcmpiOtherDetailsWrapper);
				customerDataFields.setBcmpiOtherDetails(bcmpiOtherDetails);
			}
			logger.debug("customerDataFields  :" + customerDataFields.toString());
			Optional<RpcStageVerification> rpcStageData = rpcStgVerificationRepo.findById(applicationId);
			if (rpcStageData.isPresent()) {
				logger.debug("rpcStageData found");
				customerDataFields.setRpcStatDetails(CommonUtils.parseRPCStageVerificationData(
						rpcStageData.get().getEditedFields(), rpcStageData.get().getQueries()));
				customerDataFields.setVerifiedStage(null != rpcStageData.get().getVerifiedStages()
						? Arrays.asList(rpcStageData.get().getVerifiedStages().split("\\|"))
						: new ArrayList<>());
			}
			Optional<List<Udhyam>> udhyamRecordsOpt = udhyamRepository.findByApplicationId(applicationId);
			if(udhyamRecordsOpt.isPresent()){
				List<Udhyam> udhyamRecords = udhyamRecordsOpt.get();
				customerDataFields.setUdhyamDetails(udhyamRecords);
			}

			Optional<DBKITStageVerification> dbKitStageData = dbkitStageVerificationRepository.findById(applicationId);
			if(dbKitStageData.isPresent()){
				logger.debug("dbKitStageData found");
				try{
					customerDataFields.setDbKitStatDetails(
							CommonUtils.parseBCMPIStageVerificationData("", dbKitStageData.get().getQueries()));
				}catch(Exception e){
					logger.error("error while parsing queries in dbkit: {}", e.getMessage(),e);
				}
				customerDataFields.setDbKitVerifiedStage(null != dbKitStageData.get().getVerifiedStages()
						? Arrays.asList(dbKitStageData.get().getVerifiedStages().split("\\|")) : new ArrayList<>());
				customerDataFields.setDbKitResponse(gson.fromJson(dbKitStageData.get().getResponse(), new TypeToken<List<DBKITResponse>>() {}.getType()));
				customerDataFields.setApprovedDocs(gson.fromJson(dbKitStageData.get().getApprovedDocs(), new TypeToken<List<String>>() {}.getType()));
				customerDataFields.setDbVerificationQueries(
						gson.fromJson(dbKitStageData.get().getQueryDocs(), new TypeToken<List<String>>() {
						}.getType()));
			}

			String customerdata = gson.toJson(customerDataFields);
			customerdata = customerdata.replace(Constants.PAYLOAD_COLUMN, Constants.PAYLOAD);
			responseHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());
			responseBody.setResponseObj(customerdata);
			fetchUserDetailsResponse.setResponseBody(responseBody);
			return fetchUserDetailsResponse;

		} catch (Exception e) {
			logger.error("Exception in uploadLoan : " + e.getMessage(), e);
			responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
			responseBody.setResponseObj(e.getMessage());
			fetchUserDetailsResponse.setResponseBody(responseBody);
			return fetchUserDetailsResponse;
		}

	}

	private void updateCustomerDtls(UploadLoanRequestFields requestObj) {
		try {
			Gson gson = new Gson();
			String payload;
			logger.debug("Onentry :: updateCustomerDtls");
			// Get the list of customer details from the request object
			List<CustomerDetails> customerDetailsList = requestObj.getCustomerDetailsList();
			logger.debug("customerDetailsList size :" + customerDetailsList.size());
			for (CustomerDetails customerDetails : customerDetailsList) {
				logger.debug("inside customerDetailsList : " + customerDetails.toString());
				// Check if a record exists for the given ApplicationId and CustomerType
				Optional<CustomerDetails> existingDetails = custDtlRepo.findByApplicationIdAndCustomerType(
						requestObj.getApplicationId(), requestObj.getCustomerType());
				logger.debug("existingDetails of customerDetails" + existingDetails.toString());
				if (existingDetails.isPresent()) {
					logger.debug("data found for customerDetails " + "applicationId :" + requestObj.getApplicationId()
							+ "and " + requestObj.getCustomerType());
					// Fetch the existing record
					CustomerDetails existingCustomerDetails = existingDetails.get();
					logger.debug("existingCustomerDetails : " + existingCustomerDetails.toString());
					// Update the fields with new values

					existingCustomerDetails.setVersionNum(Constants.INITIAL_VERSION_NO);
					existingCustomerDetails.setCustomerName(customerDetails.getCustomerName());
					existingCustomerDetails.setMobileNumber(customerDetails.getMobileNumber());
					existingCustomerDetails.setKycStatus(customerDetails.getKycStatus());
					existingCustomerDetails.setAmlStatus(customerDetails.getAmlStatus());

					payload = gson.toJson(customerDetails.getPayload());
					logger.debug("existingCustomerDetails : payload : " + payload);
					existingCustomerDetails.setPayloadColumn(payload);

					// Save the updated record
					custDtlRepo.save(existingCustomerDetails);
					logger.debug("Checking for InsuranceDetails with applicationId: {} and excluding custDtlId: {}", requestObj.getApplicationId(), existingCustomerDetails.getCustDtlId());
					Optional<InsuranceDetails> insuranceDetailsOpt = insuranceDtlRepo.findByApplicationIdAndCustDtlIdNot(requestObj.getApplicationId(), existingCustomerDetails.getCustDtlId());
					if (insuranceDetailsOpt.isPresent()) {
						InsuranceDetails insuranceDetails = insuranceDetailsOpt.get();
						logger.debug("InsuranceDetails found: {}", insuranceDetails);
						InsuranceDetailsPayload insuranceDetailsPayload = gson.fromJson(insuranceDetails.getPayloadColumn(), InsuranceDetailsPayload.class);
						logger.debug("InsuranceDetailsPayload nomineeRelation: {}, customer relationship: {}", insuranceDetailsPayload.getNomineeRelation(), customerDetails.getPayload().getRelationShipWithApplicant());
						if (StringUtils.isNoneEmpty(customerDetails.getPayload().getRelationShipWithApplicant())) {
							if (insuranceDetailsPayload.getNomineeRelation().equalsIgnoreCase(customerDetails.getPayload().getRelationShipWithApplicant())) {
								logger.debug("Nominee relation matches, updating nominee name and dob.");
								insuranceDetailsPayload.setNomineeName(customerDetails.getCustomerName());
								insuranceDetailsPayload.setNomineeDob(customerDetails.getPayload().getDob());
								insuranceDetailsPayload.setAge(customerDetails.getPayload().getAge());
								String insurancePayloadColumn = gson.toJson(insuranceDetailsPayload);
								insuranceDetails.setPayloadColumn(insurancePayloadColumn);
								logger.debug("Saving updated InsuranceDetails: {}", insuranceDetails);
								insuranceDtlRepo.save(insuranceDetails);
							}
						} else {
							Optional<CustomerDetails> coAppCustDetailsopt = custDtlRepo.findByApplicationIdAndAppIdAndCustomerType(requestObj.getApplicationId(),
									requestObj.getAppId(), Constants.COAPPLICANT);
							logger.debug("Checking for Co-Applicant CustomerDetails with applicationId: {} and appId: {}", requestObj.getApplicationId(), requestObj.getAppId());
							if (coAppCustDetailsopt.isPresent()) {
								logger.debug("Co-Applicant CustomerDetails found: {}", coAppCustDetailsopt.get());
								CustomerDetails coAppDetails = coAppCustDetailsopt.get();
								CustomerDetailsPayload coAppDetailsPayload = gson.fromJson(coAppDetails.getPayloadColumn(), CustomerDetailsPayload.class);
								logger.debug("Co-Applicant CustomerDetailsPayload nomineeRelation: {}, customer relationship: {}", insuranceDetailsPayload.getNomineeRelation(), coAppDetailsPayload.getRelationShipWithApplicant());
								if (coAppDetailsPayload.getRelationShipWithApplicant().equalsIgnoreCase("Father") && insuranceDetailsPayload.getNomineeRelation().equalsIgnoreCase("Daughter")
										|| coAppDetailsPayload.getRelationShipWithApplicant().equalsIgnoreCase("Mother") && insuranceDetailsPayload.getNomineeRelation().equalsIgnoreCase("Daughter")
										|| coAppDetailsPayload.getRelationShipWithApplicant().equalsIgnoreCase("Son") && insuranceDetailsPayload.getNomineeRelation().equalsIgnoreCase("Mother")
										|| coAppDetailsPayload.getRelationShipWithApplicant().equalsIgnoreCase("Daughter-in-law") && insuranceDetailsPayload.getNomineeRelation().equalsIgnoreCase("Mother-In-law")
										|| coAppDetailsPayload.getRelationShipWithApplicant().equalsIgnoreCase("Spouse") && insuranceDetailsPayload.getNomineeRelation().equalsIgnoreCase("Spouse")) {
									logger.debug("Nominee relation: {}, corresponds with Co-Applicant: {}, updating nominee name and dob.", insuranceDetailsPayload.getNomineeRelation(), coAppDetailsPayload.getRelationShipWithApplicant());
									insuranceDetailsPayload.setNomineeName(customerDetails.getCustomerName());
									insuranceDetailsPayload.setNomineeDob(customerDetails.getPayload().getDob());
									insuranceDetailsPayload.setAge(customerDetails.getPayload().getAge());
									String insurancePayloadColumn = gson.toJson(insuranceDetailsPayload);
									insuranceDetails.setPayloadColumn(insurancePayloadColumn);
									logger.debug("Saving updated InsuranceDetails: {}", insuranceDetails);
									insuranceDtlRepo.save(insuranceDetails);
								}
							}
						}
					}
					logger.warn("Completed updating customer details - TB_ABOB_CUSTOMER_DETAILS for loans");
				} else {
					logger.warn("No matching record found for ApplicationId: " + requestObj.getApplicationId()
							+ ", CustomerType: " + requestObj.getCustomerType() + ". Update skipped.");
				}
			}
		} catch (Exception e) {
			logger.error("Exception in updateCustomerDtls: " + e.getMessage(), e);
		}
	}

	private void updateOccupationdtls(UploadLoanRequestFields requestObj) {
		logger.debug("Onentry :: updateOccupationdtls");
		Gson gson = new Gson();
		try {
			List<OccupationDetailsWrapper> occupationDetailsWrapperList = requestObj.getOccupationDetailsWrapperList();

			for (OccupationDetailsWrapper occupationDetailsWrapper : occupationDetailsWrapperList) {
				logger.debug("inside occupationDetailsWrapperList : " + occupationDetailsWrapper.toString());
				Optional<OccupationDetails> occupationDetailsDb = occupationDtlRepo
						.findOccupationDetailsByCustomerTypeForRpc(requestObj.getCustomerType(),
								requestObj.getApplicationId());

				if (occupationDetailsDb.isPresent()) {
					logger.debug("data found for occupation Details " + "applicationId :" + requestObj.getApplicationId()
							+ "and " + requestObj.getCustomerType());
					OccupationDetails existingRecord = occupationDetailsDb.get(); // Get existing record

					existingRecord.setVersionNum(Constants.INITIAL_VERSION_NO);
					String payload = gson.toJson(occupationDetailsWrapper.getOccupationDetails().getPayload());
					existingRecord.setPayloadColumn(payload);

					occupationDtlRepo.save(existingRecord); // Save the updated record
					logger.warn("Completed updating occupation Details - TB_ABOB_OCCUPATION_DETAILS for loans");


					Optional<OccupationDetails> occupationDetailsDbCoApp = occupationDtlRepo
							.findOccupationDetailsByCustomerTypeForRpc(Constants.COAPPLICANT,
									requestObj.getApplicationId());

					if (occupationDetailsDbCoApp.isPresent()) {
						logger.debug("data found for customerDetails " + "applicationId :" + requestObj.getApplicationId()
								+ "and " + Constants.COAPPLICANT);
						OccupationDetails existingRecordCoApp = occupationDetailsDbCoApp.get(); // Get existing record

						OccupationDetailsPayload occpnPayloadDbCoApp = gson.fromJson(existingRecordCoApp.getPayloadColumn(),
								OccupationDetailsPayload.class);
						if(occpnPayloadDbCoApp.getOccupationTag()!=null	&& occpnPayloadDbCoApp.getOccupationTag().equalsIgnoreCase("YES")) {
							existingRecordCoApp.setVersionNum(Constants.INITIAL_VERSION_NO);
							String payloadCoApp = gson.toJson(occupationDetailsWrapper.getOccupationDetails().getPayload());

							OccupationDetailsPayload occpnPayloadDbCoApp1 = gson.fromJson(payloadCoApp,
									OccupationDetailsPayload.class);

							occpnPayloadDbCoApp1.setOccupationTag("YES");
							String payloadCoApp1 = gson.toJson(occpnPayloadDbCoApp1);

							existingRecordCoApp.setPayloadColumn(payloadCoApp1);

							occupationDtlRepo.save(existingRecordCoApp); // Save the updated record
							logger.warn("Completed updating occupation Details for Co-Applicant - TB_ABOB_OCCUPATION_DETAILS for loans");
						}
					}
				} else {
					logger.debug("record not found for Occupation Details " + "applicationId :"
							+ requestObj.getApplicationId() + "and " + requestObj.getCustomerType());
				}
			}
		} catch (Exception e) {
			logger.error("Exception in updateOccupationdtls: " + e.getMessage(), e);
		}
	}

	private void updateBankDtls(UploadLoanRequestFields requestObj) {
		logger.debug("Onentry :: updateBankDtls");
		Gson gson = new Gson();
		try {
			List<BankDetailsWrapper> bankDetailsWrapperList = requestObj.getBankDetailsWrapperList();
			for (BankDetailsWrapper bankDetailsWrapper : bankDetailsWrapperList) {
				logger.debug("inside bankDetailsWrapperList :: getBankDetails: "
						+ bankDetailsWrapper.getBankDetails().toString());
//				Optional<BankDetails> bankDetailsDb = bankDtlRepo.findByApplicationId(requestObj.getApplicationId());
				Optional<BankDetails> bankDetailsDb = bankDtlRepo.findBankDetailsByCustomerType(requestObj.getCustomerType(), requestObj.getApplicationId());
				if (bankDetailsDb.isPresent()) {
					logger.debug("updateBankDtls data found for the applicationId : " + requestObj.getApplicationId());
					BankDetails existingRecord = bankDetailsDb.get(); // Fetch the existing record
					existingRecord.setVersionNum(Constants.INITIAL_VERSION_NO);
					logger.debug("existingRecord : " + existingRecord.toString());

					boolean isAccountNoPresent = requestObj.getEditedFields().stream().anyMatch(s -> s.equalsIgnoreCase("accountNumber"));

					BankDetailsPayload bankPayloadDb = gson.fromJson(existingRecord.getPayloadColumn(),
							BankDetailsPayload.class);

					BankDetailsPayload bankDetailsPayloadReq = bankDetailsWrapper.getBankDetails().getPayload();
					if(isAccountNoPresent) {
						logger.debug("accountNumber found in EditedFields() ");
						deleteDocument(requestObj.getApplicationId(), requestObj.getAppId());
						bankPayloadDb.setPennyCheckStatus("");
						bankPayloadDb.setPennyResp("");
						bankPayloadDb.setAccountNumber(bankDetailsPayloadReq.getAccountNumber());
					}

					bankPayloadDb.setAccountName(bankDetailsPayloadReq.getAccountName());
					bankPayloadDb.setAccountType(bankDetailsPayloadReq.getAccountType());
					bankPayloadDb.setBankName(bankDetailsPayloadReq.getBankName());
					bankPayloadDb.setIfsc(bankDetailsPayloadReq.getIfsc());
					bankPayloadDb.setBranchName(bankDetailsPayloadReq.getBranchName());
					bankPayloadDb.setEditBankDetails(bankDetailsPayloadReq.getEditBankDetails());

					bankPayloadDb.setRpcEditCheck(bankDetailsPayloadReq.isRpcEditCheck());
					bankPayloadDb.setReEnterAccountNumber(bankDetailsPayloadReq.getReEnterAccountNumber());
					bankPayloadDb.setRpcaccntVerified(bankDetailsPayloadReq.getRpcaccntVerified());

					String payloadStr = gson.toJson(bankPayloadDb);
					existingRecord.setPayloadColumn(payloadStr);

					bankDtlRepo.save(existingRecord); // Save the updated record
					logger.warn("Completed updating Bank Details - TB_CGOB_BANK_DTLS for loans");
				} else {
					logger.debug("record not found for bankDetails " + "applicationId :" + requestObj.getApplicationId()
							+ "and " + requestObj.getCustomerType());
				}
			}
		} catch (Exception e) {
			logger.error("Exception in updateBankDtls: " + e.getMessage(), e);
		}
	}

	public void updateLoanDtls(UploadLoanRequestFields requestObj) {
		logger.debug("Onentry :: updateOccupationdtls");
		Gson gson = new Gson();
		String payload;
		try {
			LoanDetails loanDtlObjReq = requestObj.getLoanDetails();
			logger.debug("loanDtlObjReq: " + loanDtlObjReq.toString());
			if (loanDtlObjReq != null) {
				Optional<LoanDetails> loanDetailsDb = loanDtlsRepo
						.findTopByApplicationIdAndAppId(requestObj.getApplicationId(), Constants.APPID);

				if (loanDetailsDb.isPresent()) {
					logger.debug("data found: ");
					LoanDetails existingLoanDtl = loanDetailsDb.get(); // Fetch existing record
					logger.debug("existingLoanDtl : " + existingLoanDtl.toString());
					// Update
					existingLoanDtl.setVersionNum(Constants.INITIAL_VERSION_NO);
					existingLoanDtl.setLoanAmount(loanDtlObjReq.getLoanAmount());
					existingLoanDtl.setTenureInMonths(loanDtlObjReq.getTenureInMonths());
					existingLoanDtl.setTenure(loanDtlObjReq.getTenure());
					existingLoanDtl.setRoi(loanDtlObjReq.getRoi());
					existingLoanDtl.setInterest(loanDtlObjReq.getInterest());
					existingLoanDtl.setTotPayableAmount(loanDtlObjReq.getTotPayableAmount());

					existingLoanDtl.setAutoEmiAccount(loanDtlObjReq.getAutoEmiAccount());
					existingLoanDtl.setAutoEmiAccountType(loanDtlObjReq.getAutoEmiAccountType());
					existingLoanDtl.setMonthlyEmi(loanDtlObjReq.getMonthlyEmi());
					existingLoanDtl.setLoanClosureDate(loanDtlObjReq.getLoanClosureDate());
					existingLoanDtl.setEmiDate(loanDtlObjReq.getEmiDate());
					existingLoanDtl.setLoanClosureDate(loanDtlObjReq.getLoanClosureDate());
					existingLoanDtl.setLoanCrAccount(loanDtlObjReq.getLoanCrAccount());
					existingLoanDtl.setLoanCrAccountType(loanDtlObjReq.getLoanCrAccountType());

					payload = gson.toJson(loanDtlObjReq.getPayload());
					existingLoanDtl.setPayloadColumn(payload);

					loanDtlsRepo.save(existingLoanDtl); // Save the updated record
					logger.warn("Completed updating Loan Details");
				} else {
					logger.debug("record not found for Loan Details " + "applicationId :"
							+ requestObj.getApplicationId() + "and " + requestObj.getCustomerType());
				}
			}
		} catch (Exception e) {
			logger.error("Exception in updateLoanDtls: " + e.getMessage(), e);
		}
	}

	private void updateAddressDtls(Integer stageId, UploadLoanRequestFields requestObj, String addrType) {
		logger.debug("OnEntry :: updateAddressDtls : stageId :" + stageId + ", addrType :" + addrType);

		Gson gsonObj = new Gson();
		try {
			List<AddressDetailsWrapper> addressDetailsWrapperList = requestObj.getAddressDetailsWrapperList();
			String businessAddressProof = "";
			for (AddressDetailsWrapper addressDetailsWrapper : addressDetailsWrapperList) {
				List<AddressDetails> addressDetailsList = addressDetailsWrapper.getAddressDetailsList();
				logger.debug("addressDetailsList : " + addressDetailsList.toString());

				for (AddressDetails addressDetails : addressDetailsList) {
					logger.debug("addressDetails : " + addressDetails.toString());
					// Fetch existing AddressDetails from DB based on criteria
					Optional<AddressDetails> existingAddressOpt = addressDtlRepo
							.findAddressByCustomerTypeAndAddressTypeAndApplicationIdForRpc(requestObj.getCustomerType(),
									addressDetails.getAddressType(), requestObj.getApplicationId());
					logger.debug("existingAddressOpt : " + existingAddressOpt.toString());
					if (existingAddressOpt.isPresent()) {
						logger.debug("existingAddressOpt data found : ");
						AddressDetails existingAddress = existingAddressOpt.get();
						logger.debug("existingAddress : " + existingAddress.toString());

						AddressDetailsPayload addressPayload = addressDetails.getPayload();
						logger.debug("addressPayloadReq : " + addressPayload.toString());
						List<Address> addrPayLoadLstEdt = addressPayload.getAddressList();
						Address addressEdt = addrPayLoadLstEdt.get(0);
						logger.debug("addressEdtReq : " + addressEdt.toString());

						List<Address> addrPayLoadLstDb = null;
						AddressDetailsPayload addressPayloadDb = gsonObj.fromJson(existingAddress.getPayloadColumn(),
								AddressDetailsPayload.class);
						addrPayLoadLstDb = addressPayloadDb.getAddressList();
						logger.debug("addrPayLoadLstDb : " + addrPayLoadLstDb.toString());

						boolean addressFound = false;
						for (int i = 0; i < addrPayLoadLstDb.size(); i++) {
							Address addr = addrPayLoadLstDb.get(i);

							if (addr.getAddressType().equalsIgnoreCase(addrType)) {
								// Update the existing address
								addrPayLoadLstDb.set(i, addressEdt);
								addressFound = true;
								logger.info("Address of type '" + addrType + "' found, updating - address.");
								break;
							}
						}

						// If address not found, insert the new address
						if (!addressFound && addrType.equalsIgnoreCase(Constants.SECONDARY_KYC)) {
							addrPayLoadLstDb.add(addressEdt); // Add new address to the list
							logger.info(
									"Address of type '" + addrType + "' not found, so added a new record - address.");
						}

						// Update sameAs address (PERMANENT or COMMUNICATION)
						if(addressEdt.getAddressType().equalsIgnoreCase(Constants.PRESENT) && requestObj.getCustomerType().equalsIgnoreCase(Constants.APPLICANT) && addressDetails.getAddressType().equalsIgnoreCase(Constants.PERSONAL)) { //(addrType.equalsIgnoreCase(Constants.PRESENT){
							businessAddressProof = addressEdt.getCurrentAddressProof();
							Optional<OccupationDetails> applicantOccupationDetailsOpt = occupationDtlRepo.findOccupationDetailsByCustomerTypeForRpc(Constants.APPLICANT, requestObj.getApplicationId());
							if(applicantOccupationDetailsOpt.isPresent()) {
								OccupationDetails applicantOccupationDetails = applicantOccupationDetailsOpt.get();
								OccupationDetailsPayload occupationDetailsPayload = gsonObj.fromJson(applicantOccupationDetails.getPayloadColumn(), OccupationDetailsPayload.class);
								occupationDetailsPayload.setBusinessAddressProof(businessAddressProof);
								String occupationPayloadStr = gsonObj.toJson(occupationDetailsPayload);
								applicantOccupationDetails.setPayloadColumn(occupationPayloadStr);
								occupationDtlRepo.save(applicantOccupationDetails);
								logger.info("Updated business address proof in occupation details for applicant: " + applicantOccupationDetails.toString());
							}
							for (Address addr : addrPayLoadLstDb) {
								if ((addr.getAddressType().equalsIgnoreCase(Constants.PERMANENT) && addr.getAddressSameAs().equalsIgnoreCase("Y"))
										|| (addr.getAddressType().equalsIgnoreCase(Constants.COMMUNICATION) && addr.getAddressSameAs().equalsIgnoreCase(Constants.PRESENT)
								)) {

									copyAddressDetails(addressEdt, addr);

									logger.info("Copied address - '" + addr.getAddressType() + "'.");
									//break;//?
								}
							}

						}

						// Convert payload to JSON string and set it
						addressPayloadDb.setAddressList(addrPayLoadLstDb);
						String updatedPayload = gsonObj.toJson(addressPayloadDb);
						existingAddress.setPayloadColumn(updatedPayload);
						addressDtlRepo.save(existingAddress);
						logger.info("Updated occupation address details of Co-Applicant: " + existingAddress.toString());

						// Update OCCUPATION address if sameAs PRESENT
						if(addressEdt.getAddressType().equalsIgnoreCase(Constants.PRESENT) && requestObj.getCustomerType().equalsIgnoreCase(Constants.APPLICANT)) { //(addrType.equalsIgnoreCase(Constants.PRESENT){
							Optional<AddressDetails> existingAddressOccpnObj = addressDtlRepo
									.findAddressByCustomerTypeAndAddressTypeAndApplicationIdForRpc(
											Constants.APPLICANT, Constants.OCCUPATION,
											requestObj.getApplicationId());

							logger.debug("existingAddressOccpnObj : " + existingAddressOccpnObj.toString());
							if (existingAddressOccpnObj.isPresent()) {
								logger.debug("existingAddressOccpn data found : ");
								AddressDetails existingAddrOccpn = existingAddressOccpnObj.get();

								List<Address> addrPayLoadLstDbOccpn = null;
								AddressDetailsPayload occpnAddrPayLoadLstDb = gsonObj.fromJson(
										existingAddrOccpn.getPayloadColumn(), AddressDetailsPayload.class);

								addrPayLoadLstDbOccpn = occpnAddrPayLoadLstDb.getAddressList();
								logger.debug("addrPayLoadLstDbOccpn : " + addrPayLoadLstDbOccpn.toString());

								for (Address addr : addrPayLoadLstDbOccpn) {
									String addressSameAs = Optional.ofNullable(addr.getAddressSameAs()).orElse("");
									if(addr.getAddressType().equalsIgnoreCase(Constants.OFFICE) && addressSameAs.equalsIgnoreCase(Constants.PRESENT)) {

										copyAddressDetails(addressEdt, addr);

										break;

									}
								}

								// Convert updated personal address payload to JSON and save
								occpnAddrPayLoadLstDb.setAddressList(addrPayLoadLstDbOccpn);
								String updtOccpnAddrPayLoadLstDb = gsonObj.toJson(occpnAddrPayLoadLstDb);
								existingAddrOccpn.setPayloadColumn(updtOccpnAddrPayLoadLstDb);
								addressDtlRepo.save(existingAddrOccpn);
								logger.info("Updated occupation address details of applicant: " + existingAddrOccpn.toString());
							}else {
								logger.debug("existingAddress data not found : ");
							}
						}

//				   // Update PERSONAL with OFFICE if sameAs COMMUNICATION or PERMANENT
						if (Constants.OFFICE.equalsIgnoreCase(addressEdt.getAddressType())){

							logger.debug("addressEdt.getAddressType() :" + addressEdt.getAddressType());
							if (addressEdt.getAddressSameAs().equalsIgnoreCase(Constants.COMMUNICATION)
									|| addressEdt.getAddressSameAs().equalsIgnoreCase(Constants.PERMANENT)) {
								logger.debug("addressEdt.getAddressSameAs()" + addressEdt.getAddressSameAs());
								Optional<AddressDetails> existingAddressPersonalObj = addressDtlRepo
										.findAddressByCustomerTypeAndAddressTypeAndApplicationIdForRpc(
												requestObj.getCustomerType(), Constants.PERSONAL,
												requestObj.getApplicationId());

								logger.debug("existingAddressPersonal : " + existingAddressPersonalObj.toString());
								if (existingAddressPersonalObj.isPresent()) {
									logger.debug("existingAddressPersonal data found : ");
									AddressDetails existingAdrPersonal = existingAddressPersonalObj.get();

									List<Address> addrPayLoadLstDbPr = null;
									AddressDetailsPayload personalAddressPayload = gsonObj.fromJson(
											existingAdrPersonal.getPayloadColumn(), AddressDetailsPayload.class);
									addrPayLoadLstDbPr = personalAddressPayload.getAddressList();
									logger.debug("addrPayLoadLstDbPersonal : " + addrPayLoadLstDbPr.toString());

									for (Address addr : addrPayLoadLstDbPr) {
										if (addr.getAddressType().equalsIgnoreCase(addressEdt.getAddressSameAs())) {

											copyAddressDetails(addressEdt, addr);

											logger.info("Copied 'Office' address - '" + addr.getAddressType() + "'.");
											break;
										}
									}

									// Convert updated personal address payload to JSON and save
									personalAddressPayload.setAddressList(addrPayLoadLstDbPr);
									String updatedPersonalPayload = gsonObj.toJson(personalAddressPayload);
									existingAdrPersonal.setPayloadColumn(updatedPersonalPayload);
									addressDtlRepo.save(existingAdrPersonal);
									logger.info("Updated personal address details: " + existingAdrPersonal.toString());
								}
							}
						}


						//updating Co-applicant Address if request Address is Present
						if(addressEdt.getAddressType().equalsIgnoreCase(Constants.PRESENT)) { //if (addrType.equalsIgnoreCase(Constants.PRESENT)) {

							//updating co-applicant address //applicant and co-applicant present address should be same. 
							Optional<AddressDetails> existingAddressCoOpt = addressDtlRepo
									.findAddressByCustomerTypeAndAddressTypeAndApplicationIdForRpc(Constants.COAPPLICANT,
											Constants.PERSONAL, requestObj.getApplicationId()); //addressDetails.getAddressType()
							logger.debug("existingAddressCoAplicant : " + existingAddressCoOpt.toString());

							List<Address> addrPayLoadLstDbCo = null;
							AddressDetailsPayload addressPayloadDbCo = null;
							if (existingAddressCoOpt.isPresent()) {
								logger.debug("existingAddressCo data found : ");
								AddressDetails existingAddressCo = existingAddressCoOpt.get();
								logger.debug("existingAddressCo : " + existingAddress.toString());

								addressPayloadDbCo = gsonObj.fromJson(existingAddressCo.getPayloadColumn(),
										AddressDetailsPayload.class);
								addrPayLoadLstDbCo = addressPayloadDbCo.getAddressList();
								logger.debug("addrPayLoadLstDbCo : " + addrPayLoadLstDbCo.toString());


								for (int i = 0; i < addrPayLoadLstDbCo.size(); i++) {
									Address addr = addrPayLoadLstDbCo.get(i);

									if (addr.getAddressType().equalsIgnoreCase(addrType)) {
										// Update the existing address
										addrPayLoadLstDbCo.set(i, addressEdt);
										logger.info("Address of type '" + addrType + "' found, updating - address.");
										break; // Exit after updating the address
									}
								}

								//updating same as Address
								for (Address addr : addrPayLoadLstDbCo) {
									if ((addr.getAddressType().equalsIgnoreCase(Constants.PERMANENT) && addr.getAddressSameAs().equalsIgnoreCase("Y"))
											|| (addr.getAddressType().equalsIgnoreCase(Constants.COMMUNICATION) && addr.getAddressSameAs().equalsIgnoreCase(Constants.PRESENT)
									)) {

										copyAddressDetails(addressEdt, addr);
										logger.info("Copied address - '" + addr.getAddressType() + "'.");
										//break;//?
									}
								}

								// Convert payload to JSON string and set it
								addressPayloadDbCo.setAddressList(addrPayLoadLstDbCo);
								String updatedPayloadCo = gsonObj.toJson(addressPayloadDbCo);
								existingAddressCo.setPayloadColumn(updatedPayloadCo);
								addressDtlRepo.save(existingAddressCo);

								logger.warn("Address updated for Co-Applicant - personal");
							}else {
								logger.debug("existingAddressCo data not found : ");
							}
							//
							//if(addressEdt.getAddressSameAs().equalsIgnoreCase(Constants.OCCUPATION)){
							Optional<AddressDetails> existingAddressOccpnObjCo = addressDtlRepo
									.findAddressByCustomerTypeAndAddressTypeAndApplicationIdForRpc(
											Constants.COAPPLICANT, Constants.OCCUPATION,
											requestObj.getApplicationId());

							logger.debug("existingAddressOccpnObjCo : " + existingAddressOccpnObjCo.toString());
							if (existingAddressOccpnObjCo.isPresent()) {
								logger.debug("existingAddressOccpn data found : ");
								AddressDetails existingAddrOccpnCo = existingAddressOccpnObjCo.get();

								List<Address> addrPayLoadLstDbOccpnCo = null;
								AddressDetailsPayload occpnAddrPayLoadLstDbCo = gsonObj.fromJson(
										existingAddrOccpnCo.getPayloadColumn(), AddressDetailsPayload.class);

								addrPayLoadLstDbOccpnCo = occpnAddrPayLoadLstDbCo.getAddressList();
								logger.debug("addrPayLoadLstDbOccpn : " + addrPayLoadLstDbOccpnCo.toString());

								for (Address addr : addrPayLoadLstDbOccpnCo) {
									if(addr.getAddressType().equalsIgnoreCase(Constants.OFFICE) && addr.getAddressSameAs().equalsIgnoreCase(Constants.PRESENT)) {

										copyAddressDetails(addressEdt, addr);

										break;
									}
								}

								// Convert updated personal address payload to JSON and save
								occpnAddrPayLoadLstDbCo.setAddressList(addrPayLoadLstDbOccpnCo);
								String updtOccpnAddrPayLoadLstDbCo = gsonObj.toJson(occpnAddrPayLoadLstDbCo);
								existingAddrOccpnCo.setPayloadColumn(updtOccpnAddrPayLoadLstDbCo);
								addressDtlRepo.save(existingAddrOccpnCo);
								logger.info("Updated occupation address details of Co-Applicant: " + existingAddrOccpnCo.toString());
							}

						}

					} else {
						logger.warn("No existing Address Details found for Application ID: "
								+ requestObj.getApplicationId() + ", Address Type: " + addressDetails.getAddressType());
					}
				}
			}

		} catch (Exception e) {
			logger.error("Exception in updateAddressDtls: " + e.getMessage(), e);
		}

	}

	private void copyAddressDetails(Address source, Address target) {
		target.setAddressLine1(source.getAddressLine1());
		target.setAddressLine2(source.getAddressLine2());
		target.setAddressLine3(source.getAddressLine3());
		target.setDistrict(source.getDistrict());
		target.setCity(source.getCity());
		target.setState(source.getState());
		target.setCountry(source.getCountry());
		target.setPinCode(source.getPinCode());
		target.setLandMark(source.getLandMark());
		target.setArea(source.getArea());

		target.setCurrentAddressProof(source.getCurrentAddressProof());
		target.setHouseType(source.getHouseType());
		target.setLocateCoOrdinatesFor(source.getLocateCoOrdinatesFor());
		target.setLocateCoOrdinates(source.getLocateCoOrdinates());

		target.setResidenceOwnership(source.getResidenceOwnership());
		target.setResidenceAddressSince(source.getResidenceAddressSince());
		target.setResidenceCitySince(source.getResidenceCitySince());

	}


	private void updateEditedFields(int stageId, UploadLoanRequestFields requestObj, String custCode) {
		logger.debug("Entry updateEditedFields method");
		// Construct the new stage edit strings
		List<String> newEntries = requestObj.getEditedFields().stream()
				.map(field -> stageId + "_" + custCode + "_" + field).collect(Collectors.toList());
		logger.debug("newEntries" + newEntries);

		// Construct the new stage verification strings
		List<String> entriesTocompareStageVr = requestObj.getEditedFields().stream()
				.map(field -> stageId + "_" + custCode).collect(Collectors.toList());
		logger.debug("entriesTocompareStageVr" + entriesTocompareStageVr);

		// Fetch existing record from DB
		Optional<RpcStageVerification> rpcStageVerificationDb = rpcStgVerificationRepo
				.findById(requestObj.getApplicationId());
		logger.debug("rpcStageVerificationDb findById :" + requestObj.getApplicationId());
		logger.debug("rpcStageVerificationDb :" + rpcStageVerificationDb.toString());

		if (rpcStageVerificationDb.isPresent()) {
			logger.debug("rpcStageVerificationDb record found");
			RpcStageVerification stageVerificationDbObj = rpcStageVerificationDb.get();

			String existingEdits = stageVerificationDbObj.getEditedFields();
			logger.debug("existingEdits: " + existingEdits);
			if (StringUtils.isNotEmpty(existingEdits)) {
				// Convert existing DB string into a Set for quick lookup
				Set<String> existingSet = new HashSet<>(Arrays.asList(existingEdits.split("\\|")));
				logger.debug("existingSet: " + existingSet.toString());

				// Append only new values that are not already present
				for (String entry : newEntries) {
					if (!existingSet.contains(entry)) {
						logger.debug("entry" + entry);
						existingSet.add(entry); // Append new entry
						logger.debug("existingSet: " + existingSet.toString());
					}
				}

				logger.debug("existingSet final: " + existingSet.toString());

				// Update
				stageVerificationDbObj.setEditedFields(String.join("|", existingSet));
			} else {
				stageVerificationDbObj.setEditedFields(String.join("|", newEntries));
			}

//		        // Remove entries from stage same combination  StageId_ApplicantType
			if (stageVerificationDbObj.getVerifiedStages() != null) {
				String existingStagesVr = stageVerificationDbObj.getVerifiedStages();

				Set<String> existingStageVrSet = new HashSet<>(Arrays.asList(existingStagesVr.split("\\|")));
				// Identify entries that match the stageId_custCode pattern and remove them
				existingStageVrSet.removeIf(entry -> entriesTocompareStageVr.stream()
						.anyMatch(compareEntry -> entry.startsWith(compareEntry + "_")));
				logger.debug("existingStageVrSet : " + existingStageVrSet.toString());

				if (existingStageVrSet.isEmpty()) {
					stageVerificationDbObj.setVerifiedStages(null);
				} else {
					stageVerificationDbObj.setVerifiedStages(String.join("|", existingStageVrSet));
				}
			}
			rpcStgVerificationRepo.save(stageVerificationDbObj);

		} else {
			logger.debug("rpcStageVerificationDb record not found. Inserting new record.");
			// If no record exists, create a new one
			String newStageString = String.join("|", newEntries);
			RpcStageVerification newEntry = new RpcStageVerification();
			newEntry.setApplicationId(requestObj.getApplicationId());
			newEntry.setEditedFields(newStageString);
			rpcStgVerificationRepo.save(newEntry);
		}

		logger.debug("End updateEditedFields method");
	}

	private void updateQueries(int stageId, UploadLoanRequestFields requestObj, String custCode) {
		logger.debug("Entry updateQueries method");

		try {
			// Construct the new stage Queries strings
			List<String> newEntries = requestObj.getQueries().stream()
					.map(field -> stageId + "_" + custCode + "_" + field).collect(Collectors.toList());

			// Construct the new stage verification strings
			List<String> entriesTocompareStageVr = new ArrayList<>();
			if (requestObj.getQueries().isEmpty()) {
				entriesTocompareStageVr.add(stageId + "_" + custCode);
			} else {
				entriesTocompareStageVr.addAll(requestObj.getQueries().stream()
						.map(field -> stageId + "_" + custCode).collect(Collectors.toList()));
			}
			logger.debug("entriesTocompareStageVr" + entriesTocompareStageVr);

			// Fetch existing record from DB
			Optional<RpcStageVerification> rpcStageVerificationDb = rpcStgVerificationRepo
					.findById(requestObj.getApplicationId());

			logger.debug("rpcStageVerificationDb findById" + requestObj.getApplicationId());
			logger.debug("rpcStageVerificationDb :" + rpcStageVerificationDb.toString());

			if (rpcStageVerificationDb.isPresent()) {
				logger.debug("rpcStageVerificationDb record found");
				RpcStageVerification stageVerificationDbObj = rpcStageVerificationDb.get();
				logger.debug("stageVerificationDbObj");
				String existingQueries = stageVerificationDbObj.getQueries();
				logger.debug("existingQueries: " + existingQueries);

				if (StringUtils.isNotEmpty(existingQueries)) {
					Set<String> existingSet;
					logger.debug("existingQueries: " + existingQueries);
					// Convert existing DB string into a Set
					existingSet = new HashSet<>(Arrays.asList(existingQueries.split("\\|")));
					Set<String> filteredSet = existingSet.stream().filter(e -> e.startsWith(stageId + "_" + custCode))
							.collect(Collectors.toSet());
					logger.debug("Filtered Stream " + filteredSet);

					existingSet.removeIf(elem -> filteredSet.contains(elem));
					logger.debug("Set after operation :" + existingSet);

					// add only new values that are not already present
					for (String entry : newEntries) {
						if (!existingSet.contains(entry)) {
							existingSet.add(entry);
							logger.debug("Existing set : " + entry);
						}
					}

					logger.debug("existingSet final: " + existingSet.toString());
					stageVerificationDbObj.setQueries(String.join("|", existingSet));
				} else {
					stageVerificationDbObj.setQueries(String.join("|", newEntries));
				}

//			        // Remove entries with same stage and applicant type combination (1_A) -  that already exist
				if (stageVerificationDbObj.getVerifiedStages() != null) {
					String existingStagesVr = stageVerificationDbObj.getVerifiedStages();

					Set<String> existingStageVrSet = new HashSet<>(Arrays.asList(existingStagesVr.split("\\|")));
					logger.debug("existingStageVrSet : " + existingStageVrSet.toString());

					// Identify entries that match the stageId_custCode pattern and remove them
					existingStageVrSet.removeIf(entry -> entriesTocompareStageVr.stream()
							.anyMatch(compareEntry -> entry.startsWith(compareEntry + "_")));
					logger.debug("existingStageVrSet : " + existingStageVrSet.toString());

					if (existingStageVrSet.isEmpty()) {
						stageVerificationDbObj.setVerifiedStages(null);
					} else {
						stageVerificationDbObj.setVerifiedStages(String.join("|", existingStageVrSet));
					}
				}
				rpcStgVerificationRepo.save(stageVerificationDbObj);
			} else {
				logger.debug("rpcStageVerificationDb record not found. Inserting new record.");

				// If no record exists, create a new one
				String newStageString = String.join("|", newEntries);
				RpcStageVerification newEntry = new RpcStageVerification();
				newEntry.setApplicationId(requestObj.getApplicationId());
				newEntry.setQueries(newStageString);
				rpcStgVerificationRepo.save(newEntry);
			}

			logger.debug("End updateQueries method");
		} catch (Exception e) {
			logger.debug("Exception in updatde" + e);
		}
	}

	private void updateStageVerification(int stageId, UploadLoanRequestFields requestObj, String custCode) {
		logger.debug("Entry updateStageVerification method");
		// Construct the new stage verification strings

		String newEntry = stageId + "_" + custCode + "_" + LocalDateTime.now();

		Set<String> entriesTocompareStageVr = new HashSet<>();
		entriesTocompareStageVr.add(stageId + "_" + custCode);

		List<String> newEntries = Arrays.asList(newEntry);
		// Fetch existing record from DB
		Optional<RpcStageVerification> stageVerificationDb = rpcStgVerificationRepo
				.findById(requestObj.getApplicationId());
		logger.debug("Size of stage" + stageVerificationDb);

		if (stageVerificationDb.isPresent()) {
			logger.debug("rpcStageVerificationDb record found");
			RpcStageVerification stageVerificationDbObj = stageVerificationDb.get();
			String existingStages = stageVerificationDbObj.getVerifiedStages();
			logger.debug("existingStages: " + existingStages);
			// Convert existing DB string into a Set
			if (StringUtils.isNotEmpty(existingStages)) {
				Set<String> existingSet = null;
				// Convert existing DB string into a Set
				existingSet = new HashSet<>(Arrays.asList(existingStages.split("\\|")));

				existingSet.removeIf(entry -> entriesTocompareStageVr.stream()
						.anyMatch(compareEntry -> entry.startsWith(compareEntry + "_")));

				logger.debug("existingStageVrSet : " + existingSet.toString());
				//
				existingSet.add(newEntry);

				if (existingSet.isEmpty()) {
					stageVerificationDbObj.setVerifiedStages(null);
				} else {
					stageVerificationDbObj.setVerifiedStages(String.join("|", existingSet));
					logger.debug("existingStageVrSetFinal : " + existingSet.toString());
				}
			} else {
				stageVerificationDbObj.setVerifiedStages(newEntry);
			}

			rpcStgVerificationRepo.save(stageVerificationDbObj);
		} else {
			logger.debug("rpcStageVerificationDb record not found. Inserting new record.");
			// If no record exists, create a new one
			String newStageString = String.join("|", newEntries);
			logger.debug("Newstage:" + newStageString);
			RpcStageVerification newRpcStageVn = new RpcStageVerification();
			newRpcStageVn.setApplicationId(requestObj.getApplicationId());
			newRpcStageVn.setVerifiedStages(newStageString);
			rpcStgVerificationRepo.save(newRpcStageVn);
		}

		logger.debug("End updateStageVerification method");

	}

	private void updateApplicationDocs(UploadLoanRequestFields requestObj, String applicationID, int version) {
		logger.debug("Onentry :: updateApplicationDocs");
		String documentName = Constants.PENNY_DOCUMENT_NAME;
		String documentType = Constants.PENNY_DOCUMENT_TYPE;

		try {
			Gson gson = new Gson();
			List<ApplicationDocumentsWrapper> applicationDocumentsWrapperList = requestObj
					.getApplicationDocumentsWrapperList();
			logger.debug("applicationDocumentsWrapperList size :" + applicationDocumentsWrapperList.size());
			for (ApplicationDocumentsWrapper applicationDocumentsWrapper : applicationDocumentsWrapperList) {
				List<ApplicationDocuments> applicationDocumentsList = applicationDocumentsWrapper
						.getApplicationDocumentsList();
				for (ApplicationDocuments applicationDocuments : applicationDocumentsList) {
					if (applicationDocuments.getAppDocId() == null) {// This is to handle the case if user changed the
						// data
						// after its being inserted by using the back
						// navigation within the session.
						BigDecimal appDocId = CommonUtils.generateRandomNum();
						applicationDocuments.setAppDocId(appDocId);
					}
					applicationDocuments.setApplicationId(applicationID);
					applicationDocuments.setVersionNum(version);
					applicationDocuments.setAppId(requestObj.getAppId());
//					ObjectMapper objectMapper= new ObjectMapper();

					logger.debug("Doc Type :" + applicationDocuments.getPayload().getDocumentType());

					ApplicationDocumentsPayload payload = applicationDocuments.getPayload();

					logger.debug("Payload " + payload);
					JSONObject js = new JSONObject();
//					js.put("docLevel", payload.getDocLevel());
					js.put("documentType", documentType);
					js.put("documentName", documentName);
					js.put("docSide", payload.getDocSide());
					js.put("docStatus", payload.getDocStatus());
					js.put("documentFileName", payload.getDocumentFileName());
					js.put("documentFormat", payload.getDocumentFormat());
					js.put("documentLoc", payload.getDocumentLoc());
					js.put("expiryDate", payload.getExpiryDate());
					js.put("documentDesc", payload.getDocumentDesc());
					js.put("issueDate", payload.getIssueDate());
					js.put("screenId", payload.getScreenId());

					applicationDocuments.setPayloadColumn(js.toString());
					applicationDocuments.setStatus(AppStatus.ACTIVE_STATUS.getValue());

					appLoanDocsRepository.save(applicationDocuments);
				}
			}
			logger.warn("Data inserted into TB_ABOB_APPLN_DOCUMENTS for uploadLoan");

		} catch (Exception e) {
			logger.error("Exception in Dtls: " + e.getMessage(), e);
		}

	}

	public Response deleteDocument(String applicationId, String appId) {
		logger.debug("Inside Delete Document Function");
		String res = "";
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();
		String filePath = "";
		String fileName = "";
		String documentName = Constants.PENNY_DOCUMENT_NAME;
		String documentType = Constants.PENNY_DOCUMENT_TYPE;

		try {
			responseHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());
			Properties prop = null;
			try {
				prop = CommonUtils.readPropertyFile();
			} catch (IOException e) {
				logger.error("Error while reading property file in deleteDocument ", e);
				return CommonUtils.formFailResponse(ResponseCodes.FAILURE.getValue(), ResponseCodes.FAILURE.getKey());
			}
			List<ApplicationDocuments> applDocs = appLoanDocsRepository.findByApplicationIdAndAppId(applicationId, appId);
			for (int i = 0; i < applDocs.size(); i++) {
				String payloadColumn = applDocs.get(i).getPayloadColumn();
				logger.debug("payload " + payloadColumn);
				JSONObject request = new JSONObject(payloadColumn);
				if (request.getString("documentType").equalsIgnoreCase(documentType)
						&& request.getString("documentName").equalsIgnoreCase(documentName)) {

					appLoanDocsRepository.deleteById(applDocs.get(i).getAppDocId());
					filePath = request.getString("documentLoc");
					fileName = request.getString("documentFileName");
					logger.debug("FilePth " + filePath);
					responseHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());

					String uploadLocation = prop.getProperty(CobFlagsProperties.FILE_UPLOAD.getKey());
					if (null != uploadLocation && !"".equalsIgnoreCase(uploadLocation)) {
						Path path = Paths.get(uploadLocation +"/"+ filePath + "/"+ fileName);
						logger.debug("Path to be deleted " + path.toString());
						try {
							Files.deleteIfExists(path);
						} catch (IOException e) {
							logger.error("Error while deleting file in deleteDocument ", e);
							return CommonUtils.formFailResponse(ResponseCodes.FAILURE.getValue(),
									ResponseCodes.FAILURE.getKey());
						}
						res = "Success";
					} else {
						responseHeader.setResponseCode(ResponseCodes.PATH_NOT_CONFIGURED.getKey());
					}

					break;

				} else {
					responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
					res = "Failed to delete";
				}
			}


			responseBody.setResponseObj(res);
			response.setResponseBody(responseBody);
			response.setResponseHeader(responseHeader);
		} catch (JSONException e) {
			logger.error("Error while deleting file in deleteDocument Function ", e);
			return CommonUtils.formFailResponse(ResponseCodes.FAILURE.getValue(), ResponseCodes.FAILURE.getKey());
		}
		return response;
	}

	public Response uploadDocument(UploadDocumentRequestFields requestFields, Properties prop) {
		Gson gson = new Gson();
		CustomerIdentificationCasa customerIdentification = new CustomerIdentificationCasa();
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();
		BigDecimal docId = null;
		logger.debug("uploadDocument called with requestFields: {}", requestFields);
		if (requestFields.getDocumentId() == null) {
			docId = CommonUtils.generateRandomNum();
			logger.debug("Generated new docId: {}", docId);
		} else {
			docId = requestFields.getDocumentId();
			logger.debug("Using provided docId: {}", docId);
		}

		Optional<ApplicationMaster> masterObjDb = applicationMasterRepo.findByAppIdAndApplicationIdAndVersionNum(
				requestFields.getAppId(), requestFields.getApplicationId(), requestFields.getVersionNum());
		if (masterObjDb.isPresent()) {
			logger.debug("Application master found");
			byte[] docByte;
			if (!(CommonUtils.isNullOrEmpty(requestFields.getBase64Value()))) {
				logger.error("Base64 data found for file: {}", requestFields.getFileName());
				String uploadLocation = prop.getProperty(CobFlagsProperties.FILE_UPLOAD.getKey());
				logger.debug("Upload Location "+  uploadLocation);
				if (null != uploadLocation && !"".equalsIgnoreCase(uploadLocation)) {
					docByte = java.util.Base64.getDecoder().decode(requestFields.getBase64Value());
					logger.debug("Decoded Base64, docByte length: {}", docByte.length);
					if (!(CommonUtils.isNullOrEmpty(requestFields.getFilePath()))) {
						logger.debug("File path for file: {}", requestFields.getFileName());
						String[] splitFileName = requestFields.getFileName().split("\\.");
						logger.debug("File name is valid: {}", splitFileName);
						String fileFormat = splitFileName[splitFileName.length - 1];
						logger.debug("file format: {}", fileFormat);
						try {
							if (isFileFormatValid(docByte, fileFormat)) {
								String fileLoc = uploadLocation + "/"+  requestFields.getFilePath();
								logger.debug("File Location "+ fileLoc);
								File file = new File(fileLoc);
								if (!file.exists()) {
									boolean dirCreated = file.mkdirs();
									logger.debug("Directory {} created: {}", fileLoc, dirCreated);
								}
								File outputFile = new File(fileLoc + "/" + requestFields.getFileName());
								if (Constants.DOCFORMATPDF.equalsIgnoreCase(fileFormat)) {
									try (FileOutputStream fos = new FileOutputStream(
											fileLoc + "/" + requestFields.getFileName())) {
										fos.write(docByte);
										logger.debug("Saving PDF file to: {}", outputFile.getAbsolutePath());
									}
								} else {// other formats like jpeg, jpg, png
									try (ByteArrayInputStream bis = new ByteArrayInputStream(docByte)) {
										logger.debug("Saving image file to: {}", outputFile.getAbsolutePath());
										BufferedImage image = ImageIO.read(bis);
										File outputfile = new File(fileLoc + "/" + requestFields.getFileName());
										ImageIO.write(image, fileFormat, outputfile);
									}
								}
								logger.debug("File saved successfully: {}", outputFile.getAbsolutePath());
								responseHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());
								customerIdentification.setAppDocId(docId);
//								checkAppCreateAppElements.setCreateAppRes(customerIdentification);
//								responseBody.setResponseObj(gson.toJson(checkAppCreateAppElements));
							} else {
								logger.error("Invalid file format : {} ", fileFormat);
								responseHeader.setResponseCode(ResponseCodes.VAPT_ISSUE_FILE_FORMAT.getKey());
								responseBody.setResponseObj(ResponseCodes.VAPT_ISSUE_FILE_FORMAT.getValue());

							}
						} catch (Exception e) {
							logger.error("Exception in upload document IOException ", e);
							responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
						}
					}
				} else {
					responseHeader.setResponseCode(ResponseCodes.PATH_NOT_CONFIGURED.getKey());
					responseBody.setResponseObj("Document not uploaded.");

				}
			} else {
				responseHeader.setResponseCode(ResponseCodes.BASE64_DATA_NOT_FOUND.getKey());
				responseBody.setResponseObj(ResponseCodes.BASE64_DATA_NOT_FOUND.getValue());

			}
		} else {
			responseHeader.setResponseCode(ResponseCodes.INVALID_APP_MASTER.getKey());
			responseBody.setResponseObj(Constants.APP_MASTER_NOT_FOUND);
		}
		response.setResponseHeader(responseHeader);
		response.setResponseBody(responseBody);
		return response;
	}

	private boolean isFileFormatValid(byte[] docByte, String fileFormat) throws IOException {
		InputStream inputStream = new ByteArrayInputStream(docByte);
		Tika tika = new Tika();
		String fileMimeType = tika.detect(inputStream);
		return (Constants.DOCFORMATPDF.equalsIgnoreCase(fileFormat) && "application/pdf".equalsIgnoreCase(fileMimeType))
				|| (Constants.DOCFORMATPNG.equalsIgnoreCase(fileFormat) && "image/png".equalsIgnoreCase(fileMimeType))
				|| ((Constants.DOCFORMATJPEG.equalsIgnoreCase(fileFormat)
				|| Constants.DOCFORMATJPG.equalsIgnoreCase(fileFormat))
				&& "image/jpeg".equalsIgnoreCase(fileMimeType));
	}

	private void uploadApplicationDocs(UploadLoanRequestFields requestObj, String applicationID, int version) {
		logger.debug("Onentry :: updateApplicationDocs");
		try {
			Gson gson = new Gson();
			List<ApplicationDocumentsWrapper> applicationDocumentsWrapperList = requestObj
					.getApplicationDocumentsWrapperList();
			logger.debug("applicationDocumentsWrapperList size :" + applicationDocumentsWrapperList.size());
			for (ApplicationDocumentsWrapper applicationDocumentsWrapper : applicationDocumentsWrapperList) {
				List<ApplicationDocuments> applicationDocumentsList = applicationDocumentsWrapper
						.getApplicationDocumentsList();
				for (ApplicationDocuments applicationDocuments : applicationDocumentsList) {
					List<ApplicationDocuments> existingAppDocs = appLoanDocsRepository.findByApplicationIdAndCustDtlIdAndDocLevelAndDocumentType(
							applicationID, applicationDocuments.getCustDtlId(),
							applicationDocuments.getPayload().getDocLevel(),
							applicationDocuments.getPayload().getDocumentType()
					);

					if (!existingAppDocs.isEmpty()) {
						for (ApplicationDocuments appDocs : existingAppDocs) {
							appLoanDocsRepository.delete(appDocs);
						}
					}

					if (applicationDocuments.getAppDocId() == null) {// This is to handle the case if user changed the
						// data
						// after its being inserted by using the back
						// navigation within the session.
						BigDecimal appDocId = CommonUtils.generateRandomNum();
						applicationDocuments.setAppDocId(appDocId);
						applicationDocuments.setDocumentId(appDocId);
					}
					applicationDocuments.setApplicationId(applicationID);
					applicationDocuments.setVersionNum(version);
					applicationDocuments.setAppId(requestObj.getAppId());

					String payload = gson.toJson(applicationDocuments.getPayload());
					applicationDocuments.setPayloadColumn(payload);

					applicationDocuments.setStatus(AppStatus.ACTIVE_STATUS.getValue());
					logger.debug("Saving application document with AppDocId:" + applicationDocuments.getAppDocId());
					logger.debug("Payload " + payload);
					appLoanDocsRepository.save(applicationDocuments);
				}
			}
			logger.warn("Data inserted into TB_ABOB_APPLN_DOCUMENTS for uploadLoan");

		} catch (Exception e) {
			logger.error("Exception in Dtls: " + e.getMessage(), e);
		}
	}

	public Response getFailureJson(String error) {
		logger.debug("Inside getFailureJson");
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();

		responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
		responseBody.setResponseObj("{\"errorMessage\":\"" + error + "\", \"status\":\"" + ResponseCodes.FAILURE.getValue() + "\"}");
		response.setResponseHeader(responseHeader);
		response.setResponseBody(responseBody);
		logger.debug("FailureJson created");
		return response;
	}
	public Response getSuccessJson(String baseString) {
		logger.debug("Inside getSuccessJson");
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();

		responseHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());
		logger.debug("responseCode added to responseHeader");
		responseBody.setResponseObj(
				"{\"message\":\"" + baseString + "\", \"status\":\"" + ResponseCodes.SUCCESS.getValue() + "\"}");
		logger.debug("string added to resonseBody as responseObj");
		response.setResponseHeader(responseHeader);
		logger.debug("responseHeader added");
		response.setResponseBody(responseBody);

		logger.debug("SuccessJson created");
		return response;
	}
	public Response getFailureApiJson(String error, String apiName) {
		logger.debug("Inside getFailureJsonn");
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();
		responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
		responseBody.setResponseObj(
				"{\"apiName\":\"" + apiName + "\",\"errorMessage\":\"" + error + "\", \"status\":\"" + ResponseCodes.FAILURE.getValue() + "\"}");
		response.setResponseHeader(responseHeader);
		response.setResponseBody(responseBody);
		logger.debug("FailureJson created" + response.toString());
		return response;
	}

	private void updateCoApplicantBankDtls(UploadLoanRequestFields requestObj, String applicationID, String appId, int version) {
		Gson gson = new Gson();

		List<BankDetailsWrapper> bankDetailsWrapperList = requestObj.getBankDetailsWrapperList();
		for (BankDetailsWrapper bankDetailsWrapper : bankDetailsWrapperList) {
			BankDetails bankDetails = bankDetailsWrapper.getBankDetails();

			Optional<BankDetails> bankDetailsDb = bankDtlRepo.findBankDetailsByCustomerType(requestObj.getCustomerType(), requestObj.getApplicationId());
			if (bankDetailsDb.isPresent()) {
				logger.debug("records found for the given applicationId and customerType.");
				bankDetails.setBankDtlId(bankDetailsDb.get().getBankDtlId());
				bankDetails.setCustDtlId(bankDetailsDb.get().getCustDtlId());
			} else {
				logger.debug("creating new");
				BigDecimal bankDtlId = CommonUtils.generateRandomNum();
				bankDetails.setBankDtlId(bankDtlId);

				Optional<CustomerDetails> customerDetails = custDtlRepo.findByApplicationIdAndCustomerType(applicationID, requestObj.getCustomerType());
				if (customerDetails.isPresent()) {
					bankDetails.setCustDtlId(customerDetails.get().getCustDtlId());
				} else {
					logger.debug("No customer details found for the given applicationId and customerType.");
				}
			}
			bankDetails.setAppId(requestObj.getAppId());
			bankDetails.setApplicationId(applicationID);
			bankDetails.setVersionNum(version);
			String payload = gson.toJson(bankDetails.getPayload());
			bankDetails.setPayloadColumn(payload);
			bankDtlRepo.save(bankDetails);
			logger.debug("Co applicant bank details saved.");
			String eNachStatus = bankDetails != null
					? bankDetails.getPayload() != null
					? bankDetails.getPayload().getENachStatus()
					: null
					: null;
			if(eNachStatus != null && !eNachStatus.isEmpty()) {
				Optional<Enach> enachOpt = enachRepository.findByApplicationIdAndCustomerType(applicationID, requestObj.getCustomerType());
				if(enachOpt.isPresent()){
					Enach enachDetails = enachOpt.get();
					enachRepository.delete(enachDetails);
				}
			}
		}
	}

	@CircuitBreaker(name = "fallback", fallbackMethod = "bIPFallback")
	public Mono<Object> bussinessImgProcessingApi(BIPMasterRequest bipRequest, Header header, Properties prop) {
		try{

			//file path
			String applicationFolderPath = prop.getProperty(CobFlagsProperties.FILE_UPLOAD.getKey()) + "/" + bipRequest.getRequestObj().getAppId() + "/" + Constants.LOAN + "/"
					+ bipRequest.getRequestObj().getApplicationId() + "/";
			logger.debug("applicationFolderPath resolved as: {}", applicationFolderPath);


			logger.debug("request from the bussinessImgProcessing API: {} ", bipRequest.toString());

			List<String> imageFiles = bipRequest.getRequestObj().getFiles();
			List<Map<String, String>> imageUrls = new ArrayList<>();

			int index = 1;
			for (String imgName : imageFiles) {
				String filePath = applicationFolderPath + imgName;
				File file = new File(filePath);

				if (file.exists()) {
					try {
						byte[] fileContent = Files.readAllBytes(file.toPath());
						String base64 = java.util.Base64.getEncoder().encodeToString(fileContent);

//		                // detect file type
//		                String mimeType = Files.probeContentType(file.toPath());
//		                if (mimeType == null) {
//		                    mimeType = "image/jpeg";
//		                }
//
//		                String dataUrl = "data:" + mimeType + ";base64," + base64;
//		                Map<String, String> imageMap = new HashMap<>();
//		                imageMap.put("image" + index, dataUrl);

						String prefix = imgName.endsWith(".png")
								? "data:image/png;base64,"
								: "data:image/jpeg;base64,";

						Map<String, String> imageMap = new HashMap<>();
						imageMap.put("image" + index, prefix + base64);

						imageUrls.add(imageMap);
						index++;

					} catch (Exception e) {
						logger.error("Error while processing image: " + imgName, e);
					}
				}
			}

			BIPRequestExt bipRequestExt = new BIPRequestExt();
			bipRequestExt.setAppId(bipRequest.getRequestObj().getAppId());
			bipRequestExt.setInterfaceName(
					prop.getProperty(CobFlagsProperties.BIP_SUBMIT_IMG_INTF.getKey())
			);


			BIPInputRequest apiReq = new BIPInputRequest();

			String requestId = UUID.randomUUID().toString().replace("-", "");

			apiReq.setRequestId(requestId);
			apiReq.setBusinessType(bipRequest.getRequestObj().getDocumentType()); // 
			apiReq.setImageUrls(imageUrls);
			apiReq.setOptions(new BIPInputRequest.Options());

			bipRequestExt.setRequestObj(apiReq);

			logger.debug("final requsest :: bussinessImgProcessing  API: {} ", bipRequestExt.toString());
			Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, bipRequestExt,
					bipRequestExt.getInterfaceName());

			return apiRespMono.flatMap(val -> {
				logger.debug("response 2 from the API: {} ", val);
				JSONObject apiResp = new JSONObject(new Gson().toJson(val));
				logger.debug("JSON response 3 from the API: {} ", apiResp);

//             logger.debug("response 2 from the API: " + val);
//             String apiResp = new Gson().toJson(val);
//             logger.debug("JSON response 3 from the API: " + apiResp);

				if (apiResp.get("success").equals(true)){

					saveLog(bipRequest.getRequestObj().getApplicationId(), Constants.BIP_IMG_PROCESS, bipRequestExt.toString(),
							apiResp.toString(), ResponseCodes.SUCCESS.getValue(), null, "");

					return Mono.just(apiResp);
				}else {
					saveLog(bipRequest.getRequestObj().getApplicationId(), Constants.BIP_IMG_PROCESS, bipRequestExt.toString(),
							apiResp.toString(), ResponseCodes.FAILURE.getValue(), null, "");
					return Mono.just(getFailureApiJson(apiResp.toString(), Constants.BIP_IMG_PROCESS));
				}
			});


		}catch (Exception e) {
			saveLog(bipRequest.getRequestObj().getApplicationId(), Constants.BIP_IMG_PROCESS, bipRequest.toString(), null,
					ResponseCodes.FAILURE.getValue(), e.getMessage(), "");
			return Mono.just(getFailureApiJson(e.toString(), Constants.BIP_IMG_PROCESS));
		}

	}
	/**
	 * @author Ankit.CAG
	 */
	public Mono<Object> AdharRedact(AdharRedactOcrRequest apiRequest, Header header, Properties prop) {
		logger.debug("AdharRedact Ocr service: {} ", apiRequest.toString());

		// Printing Hole Object in JSON for REF:
		Gson gson = new Gson();
		Map<String, Object> combinedRequest = new HashMap<>();
		combinedRequest.put("brecbRequest", apiRequest);
		combinedRequest.put("header", header);
		String adharRedactReq = gson.toJson(combinedRequest);
		logger.debug("Combined request JSON: {}", adharRedactReq);

		AdharRedactOcrRequestExt adharRedactOcrRequestExt = new AdharRedactOcrRequestExt();
		adharRedactOcrRequestExt.setAppId(apiRequest.getAppId());
		adharRedactOcrRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.ADHAR_REDACT_OCR_INTF.getKey()));
		adharRedactOcrRequestExt.setRequestObj(apiRequest.getRequestObj());
		logger.debug("adharRedactOcrRequestExt from the API: {} ", adharRedactOcrRequestExt.toString());
		Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, adharRedactOcrRequestExt,
				adharRedactOcrRequestExt.getInterfaceName());
		logger.debug("AdharRedact Ocr APi Response: {} ", apiRespMono);
		return apiRespMono;
	}

	/**
	 * @author Ankit.CAG
	 */
	public Mono<Object> KycPassportService(KycPassportRequest apiRequest, Header header, Properties prop) {
		logger.debug("KycPassport service: {} ", apiRequest.toString());

		// Printing Hole Object in JSON for REF:
		Gson gson = new Gson();
		Map<String, Object> combinedRequest = new HashMap<>();
		combinedRequest.put("KycPassportRequest", apiRequest);
		combinedRequest.put("header", header);
		String KycPassportReq = gson.toJson(combinedRequest);
		logger.debug("Combined request JSON: {}", KycPassportReq);

		KycPassportRequestExt kycPassportRequestExt = new KycPassportRequestExt();
		kycPassportRequestExt.setAppId(apiRequest.getAppId());
		kycPassportRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.KYC_PASSPORT_INTF.getKey()));
		kycPassportRequestExt.setRequestObj(apiRequest.getRequestObj());
		logger.debug("KycPassportRequestExt from the API: {} ", kycPassportRequestExt.toString());
		Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, kycPassportRequestExt,
				kycPassportRequestExt.getInterfaceName());
		logger.debug("KycPassport APi Response: {} ", apiRespMono);
		return apiRespMono;
	}

	/**
	 * @author Ankit.CAG
	 */
	public Mono<Object> kycDrivingLicenseService(kycDrivingLicenseRequest apiRequest, Header header, Properties prop) {
		logger.debug("kycDrivingLicense service: {} ", apiRequest.toString());

		// Printing Hole Object in JSON for REF:
		Gson gson = new Gson();
		Map<String, Object> combinedRequest = new HashMap<>();
		combinedRequest.put("kycDrivingLicense Request", apiRequest);
		combinedRequest.put("header", header);
		String kycDrivingLicenseReq = gson.toJson(combinedRequest);
		logger.debug("Combined request JSON: {}", kycDrivingLicenseReq);

		kycDrivingLicenseRequestExt kycDrivingLicenseRequestExt = new kycDrivingLicenseRequestExt();
		kycDrivingLicenseRequestExt.setAppId(apiRequest.getAppId());
		kycDrivingLicenseRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.KYC_DRIVING_LICENSE_INTF.getKey()));
		kycDrivingLicenseRequestExt.setRequestObj(apiRequest.getRequestObj());
		logger.debug("kycDrivingLicenseRequestExt from the API: {} ", kycDrivingLicenseRequestExt.toString());
		Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, kycDrivingLicenseRequestExt,
				kycDrivingLicenseRequestExt.getInterfaceName());
		logger.debug("kycDrivingLicense APi Response: {} ", apiRespMono);
		return apiRespMono;
	}

	/**
	 * @author Ankit.CAG
	 */
	public Mono<Object> panCheckService(PanCheckRequest apiRequest, Header header, Properties prop) {
		logger.debug("panCheck service: {} ", apiRequest.toString());

		// Printing Hole Object in JSON for REF:
		Gson gson = new Gson();
		Map<String, Object> combinedRequest = new HashMap<>();
		combinedRequest.put("panCheck Request", apiRequest);
		combinedRequest.put("header", header);
		String panCheckReq = gson.toJson(combinedRequest);
		logger.debug("Combined request JSON: {}", panCheckReq);

		PanCheckRequestExt panCheckRequestExt = new PanCheckRequestExt();
		panCheckRequestExt.setAppId(apiRequest.getAppId());
		panCheckRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.PAN_CHECK_OCR_INTF.getKey()));
		panCheckRequestExt.setRequestObj(apiRequest.getRequestObj());
		logger.debug("panCheckRequestExt from the API: {} ", panCheckRequestExt.toString());
		Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, panCheckRequestExt,
				panCheckRequestExt.getInterfaceName());
		logger.debug("panCheck APi Response: {} ", apiRespMono);
		return apiRespMono;
	}

	/**
	 * @author Ankit.CAG
	 */
	public Mono<Object> DrivingLicenseOcrService(DrivingLicenseOcrRequest apiRequest, Header header, Properties prop) {
		logger.debug("DrivingLicenseOcrRequest service: {} ", apiRequest.toString());

		// Printing Hole Object in JSON for REF:
		Gson gson = new Gson();
		Map<String, Object> combinedRequest = new HashMap<>();
		combinedRequest.put("DrivingLicenseOcrRequest Request", apiRequest);
		combinedRequest.put("header", header);
		String DrivingLicenseOcrReq = gson.toJson(combinedRequest);
		logger.debug("Combined request JSON: {}", DrivingLicenseOcrReq);

		DrivingLicenseOcrRequestExt drivingLicenseOcrRequestExt = new DrivingLicenseOcrRequestExt();
		drivingLicenseOcrRequestExt.setAppId(apiRequest.getAppId());
		drivingLicenseOcrRequestExt.setInterfaceName(prop.getProperty(CobFlagsProperties.DRIVING_LICENSE_OCR_INTF.getKey()));
		drivingLicenseOcrRequestExt.setRequestObj(apiRequest.getRequestObj());
		logger.debug("drivingLicenseOcrRequestExt from the API: {} ", drivingLicenseOcrRequestExt.toString());
		Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, drivingLicenseOcrRequestExt,
				drivingLicenseOcrRequestExt.getInterfaceName());
		logger.debug("drivingLicenseOcr APi Response: {} ", apiRespMono);
		return apiRespMono;
	}

	/**
	 * @author Ankit.CAG
	 */
	public Mono<Object> SignzyPennyCheckService(SignzyPennylessRequest apiRequest, Header header, Properties prop) {
		logger.debug("SignzyPennyCheckService service: {} ", apiRequest.toString());

		// Printing Hole Object in JSON for REF:
		Gson gson = new Gson();
		Map<String, Object> combinedRequest = new HashMap<>();
		combinedRequest.put("SignzyPennyCheckService Request", apiRequest);
		combinedRequest.put("header", header);
		String SignzyPennyCheckReq = gson.toJson(combinedRequest);
		logger.debug("Combined request JSON: {}", SignzyPennyCheckReq);

		SignzyPennylessCheckRequestExt signzyPennylessCheckRequestExt = new SignzyPennylessCheckRequestExt();
		signzyPennylessCheckRequestExt.setAppId(apiRequest.getAppId());
		signzyPennylessCheckRequestExt
				.setInterfaceName(prop.getProperty(CobFlagsProperties.SIGNZY_PENNYLESS_CHECK_INTF.getKey()));
		signzyPennylessCheckRequestExt.setRequestObj(apiRequest.getRequestObj());
		logger.debug("signzyPennylessCheckRequestExt from the API: {} ", signzyPennylessCheckRequestExt.toString());
		Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, signzyPennylessCheckRequestExt,
				signzyPennylessCheckRequestExt.getInterfaceName());
		logger.debug("SignzyPennyCheckService APi Response: {} ", apiRespMono);
		return apiRespMono;
	}

	/**
	 * @author Ankit.CAG
	 */
	public Mono<Object> VoterFrontOcrService(VoterFrontOcrRequest apiRequest, Header header, Properties prop) {
		logger.debug("VoterFrontOcrService service: {} ", apiRequest.toString());

		// Printing Hole Object in JSON for REF:
		Gson gson = new Gson();
		Map<String, Object> combinedRequest = new HashMap<>();
		combinedRequest.put("VoterFrontOcrService Request", apiRequest);
		combinedRequest.put("header", header);
		String VoterFrontOcrReq = gson.toJson(combinedRequest);
		logger.debug("Combined request JSON: {}", VoterFrontOcrReq);

		VoterFrontOcrRequestExt voterFrontOcrRequestExt = new VoterFrontOcrRequestExt();
		voterFrontOcrRequestExt.setAppId(apiRequest.getAppId());
		voterFrontOcrRequestExt
				.setInterfaceName(prop.getProperty(CobFlagsProperties.VOTER_FRONT_OCR_INTF.getKey()));
		voterFrontOcrRequestExt.setRequestObj(apiRequest.getRequestObj());
		logger.debug("voterFrontOcrRequestExt from the API: {} ", voterFrontOcrRequestExt.toString());
		Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, voterFrontOcrRequestExt,
				voterFrontOcrRequestExt.getInterfaceName());
		logger.debug("VoterFrontOcrService APi Response: {} ", apiRespMono);
		return apiRespMono;
	}

	/**
	 * @author Ankit.CAG
	 */
	public Mono<Object> VoterBackOcrService(VoterBackOcrRequest apiRequest, Header header, Properties prop) {
		logger.debug("VoterBackOcrService service: {} ", apiRequest.toString());

		// Printing Hole Object in JSON for REF:
		Gson gson = new Gson();
		Map<String, Object> combinedRequest = new HashMap<>();
		combinedRequest.put("VoterBackOcrService Request", apiRequest);
		combinedRequest.put("header", header);
		String VoterBackOcrReq = gson.toJson(combinedRequest);
		logger.debug("Combined request JSON: {}", VoterBackOcrReq);

		VoterBackOcrRequestExt voterBackOcrRequestExt = new VoterBackOcrRequestExt();
		voterBackOcrRequestExt.setAppId(apiRequest.getAppId());
		voterBackOcrRequestExt
				.setInterfaceName(prop.getProperty(CobFlagsProperties.VOTER_BACK_OCR_INTF.getKey()));
		voterBackOcrRequestExt.setRequestObj(apiRequest.getRequestObj());
		logger.debug("voterBackOcrRequestExt from the API: {} ", voterBackOcrRequestExt.toString());
		Mono<Object> apiRespMono = interfaceAdapter.callExternalService(header, voterBackOcrRequestExt,
				voterBackOcrRequestExt.getInterfaceName());
		logger.debug("VoterBackOcrService APi Response: {} ", apiRespMono);
		return apiRespMono;
	}
}
