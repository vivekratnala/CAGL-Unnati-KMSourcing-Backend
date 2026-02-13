package com.iexceed.appzillonbanking.cob.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iexceed.appzillonbanking.cob.core.payload.Header;
import com.iexceed.appzillonbanking.cob.core.payload.Response;
import com.iexceed.appzillonbanking.cob.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.cob.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.cob.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.cob.core.repository.ab.ApplicationMasterRepository;
import com.iexceed.appzillonbanking.cob.core.services.CommonParamService;
import com.iexceed.appzillonbanking.cob.core.utils.CobFlagsProperties;
import com.iexceed.appzillonbanking.cob.core.utils.CommonUtils;
import com.iexceed.appzillonbanking.cob.core.utils.Constants;
import com.iexceed.appzillonbanking.cob.core.utils.FallbackUtils;
import com.iexceed.appzillonbanking.cob.core.utils.ResponseCodes;
import com.iexceed.appzillonbanking.cob.domain.ab.RoleAccessMap;
import com.iexceed.appzillonbanking.cob.loans.payload.ApplyLoanRequest;
import com.iexceed.appzillonbanking.cob.loans.payload.ApplyLoanRequestWrapper;
import com.iexceed.appzillonbanking.cob.loans.payload.ApproveDeviationRaApplicationsReq;
import com.iexceed.appzillonbanking.cob.loans.payload.ApproveDeviationRaApplicationsReqWrapper;
import com.iexceed.appzillonbanking.cob.loans.service.LoanService;
import com.iexceed.appzillonbanking.cob.payload.CommonAPIRequest;
import com.iexceed.appzillonbanking.cob.payload.CommonAPIRequestWrapper;
import com.iexceed.appzillonbanking.cob.payload.FetchDeleteUserRequest;
import com.iexceed.appzillonbanking.cob.payload.FetchDeleteUserRequestWrapper;
import com.iexceed.appzillonbanking.cob.payload.PinCodeRequestWrapper;
import com.iexceed.appzillonbanking.cob.payload.PopulateRejectedDataRequest;
import com.iexceed.appzillonbanking.cob.payload.PopulateRejectedDataRequestWrapper;
import com.iexceed.appzillonbanking.cob.payload.SendSmsEmailRequestWrapper;
import com.iexceed.appzillonbanking.cob.service.COBService;
import com.iexceed.appzillonbanking.cob.service.CommonService;
import com.iexceed.appzillonbanking.cob.service.SendSmsAndEmailService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/common")
@Component
@Api(tags = "COMMON", value = "/common")
public class CommonAPI {

	private static final Logger logger = LogManager.getLogger(CommonAPI.class);

	@Autowired
	private CommonService commonService;

	@Autowired
	private SendSmsAndEmailService smsAndEmailService;

	@Autowired
	private CommonParamService commonCoreService;

	@Autowired
	private COBService cobBackOffService;

	@Autowired
	private LoanService loanService;

	@Autowired
	private ApplicationMasterRepository applicationMasterRepository;

	@ApiResponses({
			@ApiResponse(code = 200, message = "AppzillonBanking Customer onboarding API reachable", response = ResponseWrapper.class),
			@ApiResponse(code = 408, message = "Service Timed Out"),
			@ApiResponse(code = 500, message = "Internal Server Error"),
			@ApiResponse(code = 404, message = "AppzillonBanking Customer onboarding not reachable") })
	@ApiOperation(value = "Approve or Reject Application", notes = "API to Approve or Reject Application")
	@PostMapping(value = "/approverejectapplication", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> approveRejectApplication(
			@RequestBody FetchDeleteUserRequestWrapper requestWrapper,
			@RequestHeader(defaultValue = "APZRMB") String applicationId,
			@RequestHeader(defaultValue = "extractocrdata") String interfaceId,
			@RequestHeader(defaultValue = "000000000002") String userId,
			@RequestHeader(defaultValue = "12345678") String masterTxnRefNo,
			@RequestHeader(defaultValue = "abcd1234efgh5678") String deviceId) {
		logger.info("Start: approveRejectApplication method");
		Header header = CommonUtils.obtainHeader(applicationId, interfaceId, userId, masterTxnRefNo, deviceId);
		Mono<Response> response = Mono.empty();
		Mono<Object> objResponse = Mono.empty();
		Mono<Object> objResponse2 = Mono.empty();
		Properties prop = null;
		try {
			prop = CommonUtils.readPropertyFile();
			logger.debug("Property file read successfully");
		} catch (IOException e) {
			logger.error("Error while reading property file in approveRejectApplication ", e);
			response = FallbackUtils.genericFallbackMono();
		}
		if (null != prop) {
			boolean isSelfOnBoardingHeaderAppId = false;
			FetchDeleteUserRequest req = requestWrapper.getFetchDeleteUserRequest();
			String headerAppId = req.getAppId();
			logger.debug("Header App ID: {}", headerAppId);
			if (headerAppId.equalsIgnoreCase(prop.getProperty(CobFlagsProperties.APPID_SELF_ONBOARDING.getKey()))) {
				isSelfOnBoardingHeaderAppId = true;
				logger.debug("Self Onboarding Header App ID detected");
			}
            List<String> postDisbursementWorkflows = Arrays.asList(
                    Constants.DISBURSED.toUpperCase(),
                    Constants.LUC.toUpperCase(),
                    Constants.PENDINGLUCVERIFICATION.toUpperCase()
            );
			String roleId = commonCoreService.fetchRoleId(req.getAppId(), req.getRequestObj().getUserId());
			logger.debug("Fetched Role ID: {}", roleId);
			RoleAccessMap objDb = cobBackOffService.fetchRoleAccessMapObj(req.getAppId(), roleId);
			logger.debug("Fetched RoleAccessMap: {}", objDb);
			logger.error("Workflow ID: {}", req.getRequestObj().getWorkFlow().getWorkflowId());
			if (Constants.CREDITASSESSMENT.equalsIgnoreCase(req.getRequestObj().getWorkFlow().getWorkflowId())) {
				logger.error("Inside CREDITASSESSMENT Workflow");
				response = commonService.creditAssessmentApplicationMovement(req, prop, roleId);
			} else if (Constants.DBKITGENERATION.equalsIgnoreCase(req.getRequestObj().getWorkFlow().getWorkflowId())) {
				logger.error("Inside DBKITGENERATION Workflow");
				response = commonService.dbkitApplicationMovement(req, prop, roleId);
			} else if (Constants.DISBURSEMENT.equalsIgnoreCase(req.getRequestObj().getWorkFlow().getWorkflowId())) {
				logger.error("Inside DISBURSEMENT Workflow");
				objResponse2 = commonService.disbursementApplicationMovement(req, header, prop);
				response = objResponse2.map(obj -> (Response) obj);
			} else if (Constants.PENDINGDEVIATION.equalsIgnoreCase(req.getRequestObj().getWorkFlow().getWorkflowId())) {
				logger.error("Inside PENDINGDEVIATION Workflow");
				response = commonService.creditDeviationApplicationMovement(req, prop, roleId);
			} else if (Constants.PENDINGREASSESSMENT.equalsIgnoreCase(req.getRequestObj().getWorkFlow().getWorkflowId())) {
				logger.error("Inside PENDINGREASSESSMENT Workflow");
				response = commonService.creditReassessmentApplicationMovement(req, prop, roleId);
			} else if (Constants.PENDINGPRESANCTION.equalsIgnoreCase(req.getRequestObj().getWorkFlow().getWorkflowId())) {
				logger.error("Inside PENDINGPRESANCTION Workflow");
				response = commonService.preSanctionApplicationMovement(req, prop, roleId);
			} else if (Constants.SANCTION.equalsIgnoreCase(req.getRequestObj().getWorkFlow().getWorkflowId())) {
				logger.error("Inside SANCTION Workflow");
				objResponse = commonService.sanctionApplicationMovement(req, header, prop);
				response = objResponse.map(obj -> (Response) obj);
				logger.debug("Sanction Response" + response.toString());
			}else if(Constants.RESANCTION.equalsIgnoreCase(req.getRequestObj().getWorkFlow().getWorkflowId())){
				logger.debug("Inside RESANCTION Workflow");
				objResponse = commonService.ReSanctionApplicationMovement(req, header, prop);
				response = objResponse.map(obj -> (Response) obj);
			}else if (postDisbursementWorkflows.contains(req.getRequestObj().getWorkFlow().getWorkflowId())){
                logger.error("Inside DISBURSED Workflow");
                response = commonService.disbursedApplicationMovement(req, prop,roleId);
            }else if (Constants.ACCESS_PERMISSION_VIEWONLY.equalsIgnoreCase(objDb.getAccessPermission())) {
				logger.debug("Inside VIEWONLY permission");
				response = CommonUtils.formFailResponseMono(ResponseCodes.VAPT_ISSUE_PERMISSION.getValue(),
						ResponseCodes.VAPT_ISSUE_PERMISSION.getKey());
			} else if (Constants.ACCESS_PERMISSION_APPROVER.equalsIgnoreCase(objDb.getAccessPermission())
					|| Constants.ACCESS_PERMISSION_BOTH.equalsIgnoreCase(objDb.getAccessPermission())
					|| Constants.ACCESS_PERMISSION_VERIFIER.equalsIgnoreCase(objDb.getAccessPermission())
					|| Constants.ACCESS_PERMISSION_INITIATOR.equalsIgnoreCase(objDb.getAccessPermission())) {
				logger.debug("Inside APPROVER permission");
				logger.error("Inside APPROVER Workflow");
				response = commonService.approveRejectApplication(req, header, isSelfOnBoardingHeaderAppId, prop);
			} else if (Constants.ACCESS_PERMISSION_RPC.equalsIgnoreCase(objDb.getAccessPermission())) {
				if (Constants.DBKITVERIFICATION.equalsIgnoreCase(req.getRequestObj().getWorkFlow().getWorkflowId())) {
					logger.error("Inside DBKITVERIFICATION Workflow");
					response = commonService.dbkitVerificationApplicationMovement(req, prop, roleId);
				} else if (Constants.VERIFYAPPLICATION.equalsIgnoreCase(req.getRequestObj().getWorkFlow().getWorkflowId())) {
					logger.error("Inside VERIFYAPPLICATION Workflow");
					response = commonService.stageMovementApplication(req, prop, roleId);
				} else {
					logger.error("Inside SERVICE CALL Workflow - TBD");
				}
			}
		}
		logger.info("End: approveRejectApplication method");
		return response.flatMap(val -> {
			ResponseWrapper responseWrapper1 = ResponseWrapper.builder().apiResponse(val).build();
			logger.debug("ResponseWrapper built successfully" + responseWrapper1);
			return Mono.just(new ResponseEntity<>(responseWrapper1, HttpStatus.OK));
		});
	}

	@ApiResponses({
			@ApiResponse(code = 200, message = "AppzillonBanking Customer onboarding API reachable", response = ResponseWrapper.class),
			@ApiResponse(code = 408, message = "Service Timed Out"),
			@ApiResponse(code = 500, message = "Internal Server Error"),
			@ApiResponse(code = 404, message = "AppzillonBanking Customer onboarding not reachable") })
	@ApiOperation(value = "common api", notes = "API to perform common functionalities")
	@PostMapping(value = "/commonapi", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@Transactional
	public Response commonAPI(@RequestBody CommonAPIRequestWrapper requestWrapper,
			@RequestHeader(defaultValue = "APZRMB") String applicationId,
			@RequestHeader(defaultValue = "commonapi") String interfaceId,
			@RequestHeader(defaultValue = "000000000002") String userId,
			@RequestHeader(defaultValue = "12345678") String masterTxnRefNo,
			@RequestHeader(defaultValue = "abcd1234efgh5678") String deviceId) {
		Header header = CommonUtils.obtainHeader(applicationId, interfaceId, userId, masterTxnRefNo, deviceId);
		Mono<Response> response = Mono.empty();
		Response fetchUserDetailsResponse = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();
		Properties prop = null;
		try {
			prop = CommonUtils.readPropertyFile();
		} catch (IOException e) {
			logger.error("Error while reading property file in approveRejectApplication ", e);
			response = FallbackUtils.genericFallbackMono();
		}
		if (null != prop) {
			logger.debug("requestWrapper.toString() " + requestWrapper.toString());
			CommonAPIRequest req = requestWrapper.getCommonAPIRequest();
			String flag = req.getInterfaceName();
			logger.debug(" Flag -->" + flag);
			String userid = req.getRequestObj().getUserId();
			logger.debug(" User ID -->" + userid);
			if (flag.equalsIgnoreCase("lockUser")) {
				String applicationid = req.getRequestObj().getApplicationId();
				logger.debug(" Application ID -->" + applicationid);
				int i = applicationMasterRepository.updateTimestampOnLogin(applicationid, userid);
				logger.debug(" records updated -->" + i);
				responseHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());
				responseHeader.setResponseMessage("Success");
				responseBody.setResponseObj("userLocked");
				fetchUserDetailsResponse.setResponseBody(responseBody);
				fetchUserDetailsResponse.setResponseHeader(responseHeader);
				return fetchUserDetailsResponse;
			} else if (flag.equalsIgnoreCase("unlockUser")) {
				int i = applicationMasterRepository.updateLockOut(userid);
				logger.debug(" records updated -->" + i);
				responseHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());
				responseHeader.setResponseMessage("Success");
				responseBody.setResponseObj("userUnlocked");
				fetchUserDetailsResponse.setResponseBody(responseBody);
				fetchUserDetailsResponse.setResponseHeader(responseHeader);
				return fetchUserDetailsResponse;
			}
		}
		responseHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
		responseHeader.setResponseMessage("Failure");
		responseBody.setResponseObj("Failed to perform common functionalities");
		fetchUserDetailsResponse.setResponseBody(responseBody);
		fetchUserDetailsResponse.setResponseHeader(responseHeader);
		return fetchUserDetailsResponse;
	}

	@ApiResponses({
			@ApiResponse(code = 200, message = "AppzillonBanking Customer onboarding API reachable", response = ResponseWrapper.class),
			@ApiResponse(code = 408, message = "Service Timed Out"),
			@ApiResponse(code = 500, message = "Internal Server Error"),
			@ApiResponse(code = 404, message = "AppzillonBanking Customer onboarding not reachable") })
	@ApiOperation(value = "Approve Renewal Application", notes = "API to Approve Renewal Application")
	@PostMapping(value = "/approverenewalapplication", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> approveRenewalApplication(
			@RequestBody ApplyLoanRequestWrapper requestWrapper,
			@RequestHeader(defaultValue = "APZRMB") String applicationId,
			@RequestHeader(defaultValue = "approverenewalapplication") String interfaceId,
			@RequestHeader(defaultValue = "000000000002") String userId,
			@RequestHeader(defaultValue = "12345678") String masterTxnRefNo,
			@RequestHeader(defaultValue = "abcd1234efgh5678") String deviceId) {
		Header header = CommonUtils.obtainHeader(applicationId, interfaceId, userId, masterTxnRefNo, deviceId);
		Mono<Response> response = Mono.empty();
		Properties prop = null;
		try {
			prop = CommonUtils.readPropertyFile();
		} catch (IOException e) {
			logger.error("Error while reading property file in approveRejectApplication ", e);
			response = FallbackUtils.genericFallbackMono();
		}
		if (null != prop) {
			boolean isSelfOnBoardingHeaderAppId = false;
			ApplyLoanRequest req = requestWrapper.getApiRequest();
			String headerAppId = req.getAppId();
			if (headerAppId.equalsIgnoreCase(prop.getProperty(CobFlagsProperties.APPID_SELF_ONBOARDING.getKey()))) {
				isSelfOnBoardingHeaderAppId = true;
			}
			String roleId = commonCoreService.fetchRoleId(req.getAppId(), req.getUserId());
			RoleAccessMap objDb = cobBackOffService.fetchRoleAccessMapObj(req.getAppId(), roleId);
			if (Constants.ACCESS_PERMISSION_VIEWONLY.equalsIgnoreCase(objDb.getAccessPermission())
			/*
			 * || Constants.ACCESS_PERMISSION_INITIATOR.equalsIgnoreCase(objDb.
			 * getAccessPermission())
			 */) {
				response = CommonUtils.formFailResponseMono(ResponseCodes.VAPT_ISSUE_PERMISSION.getValue(),
						ResponseCodes.VAPT_ISSUE_PERMISSION.getKey());
			} else if (Constants.ACCESS_PERMISSION_APPROVER.equalsIgnoreCase(objDb.getAccessPermission())
					|| Constants.ACCESS_PERMISSION_BOTH.equalsIgnoreCase(objDb.getAccessPermission())
					|| Constants.ACCESS_PERMISSION_VERIFIER.equalsIgnoreCase(objDb.getAccessPermission())
					|| Constants.ACCESS_PERMISSION_INITIATOR.equalsIgnoreCase(objDb.getAccessPermission())) {
				response = commonService.approveRenewalApplication(req, header, isSelfOnBoardingHeaderAppId, prop);
			}
		}
		return response.flatMap(val -> {
			ResponseWrapper responseWrapper1 = ResponseWrapper.builder().apiResponse(val).build();
			return Mono.just(new ResponseEntity<>(responseWrapper1, HttpStatus.OK));
		});
	}

	@ApiResponses({
			@ApiResponse(code = 200, message = "AppzillonBanking Customer onboarding API reachable", response = ResponseWrapper.class),
			@ApiResponse(code = 408, message = "Service Timed Out"),
			@ApiResponse(code = 500, message = "Internal Server Error"),
			@ApiResponse(code = 404, message = "AppzillonBanking Customer onboarding not reachable") })
	@ApiOperation(value = "Initiate Rejected Application", notes = "API to Initiate Rejected Application")
	@PostMapping(value = "/initiaterejectedapplication", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> initiateRejectApplication(
			@RequestBody FetchDeleteUserRequestWrapper requestWrapper,
			@RequestHeader(defaultValue = "APZRMB") String applicationId,
			@RequestHeader(defaultValue = "extractocrdata") String interfaceId,
			@RequestHeader(defaultValue = "000000000002") String userId,
			@RequestHeader(defaultValue = "12345678") String masterTxnRefNo,
			@RequestHeader(defaultValue = "abcd1234efgh5678") String deviceId) {
		Header header = CommonUtils.obtainHeader(applicationId, interfaceId, userId, masterTxnRefNo, deviceId);
		Mono<Response> response = Mono.empty();
		Properties prop = null;
		try {
			prop = CommonUtils.readPropertyFile();
		} catch (IOException e) {
			logger.error("Error while reading property file in approveRejectApplication ", e);
			response = FallbackUtils.genericFallbackMono();
		}
		if (null != prop) {
			boolean isSelfOnBoardingHeaderAppId = false;
			FetchDeleteUserRequest req = requestWrapper.getFetchDeleteUserRequest();
			String headerAppId = req.getAppId();
			if (headerAppId.equalsIgnoreCase(prop.getProperty(CobFlagsProperties.APPID_SELF_ONBOARDING.getKey()))) {
				isSelfOnBoardingHeaderAppId = true;
			}
			String roleId = commonCoreService.fetchRoleId(req.getAppId(), req.getRequestObj().getUserId());
			RoleAccessMap objDb = cobBackOffService.fetchRoleAccessMapObj(req.getAppId(), roleId);
			if (Constants.ACCESS_PERMISSION_VIEWONLY.equalsIgnoreCase(objDb.getAccessPermission())) {
				response = CommonUtils.formFailResponseMono(ResponseCodes.VAPT_ISSUE_PERMISSION.getValue(),
						ResponseCodes.VAPT_ISSUE_PERMISSION.getKey());
			} else if (Constants.ACCESS_PERMISSION_APPROVER.equalsIgnoreCase(objDb.getAccessPermission())
					|| Constants.ACCESS_PERMISSION_BOTH.equalsIgnoreCase(objDb.getAccessPermission())
					|| Constants.ACCESS_PERMISSION_VERIFIER.equalsIgnoreCase(objDb.getAccessPermission())
					|| Constants.ACCESS_PERMISSION_INITIATOR.equalsIgnoreCase(objDb.getAccessPermission())) {
				response = commonService.initiateRejectedApplication(req, header, isSelfOnBoardingHeaderAppId, prop);
			}
		}
		return response.flatMap(val -> {
			ResponseWrapper responseWrapper1 = ResponseWrapper.builder().apiResponse(val).build();
			return Mono.just(new ResponseEntity<>(responseWrapper1, HttpStatus.OK));
		});
	}

	@ApiResponses({
			@ApiResponse(code = 200, message = "AppzillonBanking Customer onboarding API reachable", response = ResponseWrapper.class),
			@ApiResponse(code = 408, message = "Service Timed Out"),
			@ApiResponse(code = 500, message = "Internal Server Error"),
			@ApiResponse(code = 404, message = "AppzillonBanking Customer onboarding not reachable") })
	@ApiOperation(value = "Insert rejected data and call fetch application", notes = "API to Insert rejected data and call fetch application")
	@PostMapping(value = "/modifyrejectapplication", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper> populateRejectedData(
			@RequestBody PopulateRejectedDataRequestWrapper requestWrapper,
			@RequestHeader(defaultValue = "APZRMB") String applicationId,
			@RequestHeader(defaultValue = "extractocrdata") String interfaceId,
			@RequestHeader(defaultValue = "000000000002") String userId,
			@RequestHeader(defaultValue = "12345678") String masterTxnRefNo,
			@RequestHeader(defaultValue = "abcd1234efgh5678") String deviceId) {
		Response response = null;
		Properties prop = null;
		ResponseWrapper responseWrapper = new ResponseWrapper();
		try {
			prop = CommonUtils.readPropertyFile();
		} catch (Exception e) {
			logger.error("Error while reading property file in populateRejectedData ", e);
			response = CommonUtils.formFailResponse(ResponseCodes.FAILURE.getValue(), ResponseCodes.FAILURE.getKey());
		}
		if (null != prop) {
			boolean isSelfOnBoardingAppId = false;
			PopulateRejectedDataRequest request = requestWrapper.getApiRequest();
			String appId = request.getRequestObj().getAppId();
			if (appId.equalsIgnoreCase(prop.getProperty(CobFlagsProperties.APPID_SELF_ONBOARDING.getKey()))) {
				isSelfOnBoardingAppId = true;
			}
			response = commonService.populateRejectedDataInAllTables(request, isSelfOnBoardingAppId);
		}
		responseWrapper.setApiResponse(response);
		logger.warn("%s %s", "End : populateRejectedData method response is:: ", responseWrapper.toString());
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}

	@ApiResponses({
			@ApiResponse(code = 200, message = "AppzillonBanking Customer onboarding API reachable", response = ResponseWrapper.class),
			@ApiResponse(code = 408, message = "Service Timed Out"),
			@ApiResponse(code = 500, message = "Internal Server Error"),
			@ApiResponse(code = 404, message = "AppzillonBanking Customer onboarding not reachable") })
	@ApiOperation(value = "Fetch the PinCode details", notes = "API to Fetch state/country/area details based on API")
	@PostMapping(value = "/pinCode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper> fetchPinCodeDetails(@RequestBody PinCodeRequestWrapper requestWrapper) {
		logger.warn("start: Fetch data based pinCodeDetails request: " + requestWrapper.toString());
		ResponseWrapper responseWrapper = new ResponseWrapper();
		Response pinCodeResponse = commonService.fetchDetailsBasedOnPinCode(requestWrapper.getApiRequest());
		responseWrapper.setApiResponse(pinCodeResponse);
		logger.warn("End : Fetch data based pinCodeDetails response is :: ", responseWrapper.toString());
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}

	@ApiResponses({
			@ApiResponse(code = 200, message = "AppzillonBanking Customer onboarding API reachable", response = ResponseWrapper.class),
			@ApiResponse(code = 408, message = "Service Timed Out"),
			@ApiResponse(code = 500, message = "Internal Server Error"),
			@ApiResponse(code = 404, message = "AppzillonBanking Customer onboarding not reachable") })
	@ApiOperation(value = "Send email and SMS", notes = "API to send email and sms to customer")
	@PostMapping(value = "/sendSmsAndEmail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> sendSmsandEmail(
			@RequestBody SendSmsEmailRequestWrapper requestWrapper) {
		ResponseWrapper responseWrapper = new ResponseWrapper();
		Properties prop = null;
		try {
			prop = CommonUtils.readPropertyFile();
		} catch (IOException e) {
			logger.error("Error while reading property file in approveRejectApplication ", e);
		}
		if (null != prop) {
			logger.warn("start: send email and sms request: " + requestWrapper.toString());
			logger.debug("Action Type in SMS :" + requestWrapper.getApiRequest().getRequestObject().getActionType());
			if(requestWrapper.getApiRequest().getRequestObject().getActionType().equalsIgnoreCase(Constants.SANCTION)) {
				logger.debug("Sanction SMS started");
				Response sendSanctionSmsResp = commonService.sendSanctionSms(requestWrapper.getApiRequest().getApplicationId(),
						requestWrapper.getApiRequest().getAppId(), prop, false);
				responseWrapper.setApiResponse(sendSanctionSmsResp);
				logger.warn("End : send email and sms sanction response is :: ", responseWrapper.toString());
			} else if(requestWrapper.getApiRequest().getRequestObject().getActionType().equalsIgnoreCase(Constants.DISBURSED)) {
				logger.debug("Disbursed SMS started");
				Response sendDisbursementSmsResp = commonService.sendSanctionSms(requestWrapper.getApiRequest().getApplicationId(),
						requestWrapper.getApiRequest().getAppId(), prop, true);
				responseWrapper.setApiResponse(sendDisbursementSmsResp);
				logger.warn("End : send email and sms disbursement response is :: ", responseWrapper.toString());
			}else {
			Response smsAndEmailResponse = smsAndEmailService.sendSmsAndEmailService(requestWrapper.getApiRequest(),
					prop, false, false);
			responseWrapper.setApiResponse(smsAndEmailResponse);
			logger.warn("End : send email and sms CB response is :: ", responseWrapper.toString());
			}
		} else {
			Response response = CommonUtils.formFailResponse(ResponseCodes.FAILURE.getValue(),
					ResponseCodes.FAILURE.getKey());
			responseWrapper.setApiResponse(response);
		}
		return Mono.just(new ResponseEntity<>(responseWrapper, HttpStatus.OK));
	}

	@ApiResponses({
			@ApiResponse(code = 200, message = "AppzillonBanking Customer onboarding API reachable", response = ResponseWrapper.class),
			@ApiResponse(code = 408, message = "Service Timed Out"),
			@ApiResponse(code = 500, message = "Internal Server Error"),
			@ApiResponse(code = 404, message = "AppzillonBanking Customer onboarding not reachable") })
	@ApiOperation(value = "Apply for loan and reject immediately", notes = "API to Apply for loan and reject the application")
	@PostMapping(value = "/applyrejectapplication", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<ResponseWrapper>> applyRejectApplication(
			@RequestBody ApplyLoanRequestWrapper requestWrapper,
			@RequestHeader(defaultValue = "APZCOB") String reqAppId,
			@RequestHeader(defaultValue = "applyloan") String interfaceId,
			@RequestHeader(defaultValue = "000000000002") String userId,
			@RequestHeader(defaultValue = "12345678") String masterTxnRefNo,
			@RequestHeader(defaultValue = "abcd1234efgh5678") String deviceId) {
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();
		ResponseWrapper responseWrapper = new ResponseWrapper();
		try {
			boolean isSelfOnBoardingHeaderAppId = false;
			logger.debug("Incoming request for apply reject loan: {}", requestWrapper);
			ApplyLoanRequest apiRequest = requestWrapper.getApiRequest();
			String headerAppId = apiRequest.getAppId();
			String appId = apiRequest.getRequestObj().getAppId();
			Properties prop = CommonUtils.readPropertyFile();
			if (headerAppId.equalsIgnoreCase(prop.getProperty(CobFlagsProperties.APPID_SELF_ONBOARDING.getKey()))) {
				isSelfOnBoardingHeaderAppId = true;
			}
			// if (loanService.isValidStage(apiRequest, isSelfOnBoardingHeaderAppId, array))
			// { // VAPT
			logger.debug("After stage validation");
			// if (loanService.isVaptPassedForScreenElements(apiRequest, array)) { // VAPT
			logger.debug("After field validation");
			Mono<Response> response1 = loanService.applyRejectLoan(apiRequest, isSelfOnBoardingHeaderAppId, prop);
			return response1.flatMap(val -> {
				// loanService.updateRelatedApplnIdDetails(apiRequest, appId);
				ResponseWrapper responseWrapper1 = ResponseWrapper.builder().apiResponse(val).build();
				return Mono.just(new ResponseEntity<>(responseWrapper1, HttpStatus.OK));
			});

			/*
			 * } else { logger.debug("VAPT failed for screen elements for loans"); response
			 * = CommonUtils.formFailResponse(ResponseCodes.VAPT_ISSUE_FIELDS.getValue(),
			 * ResponseCodes.VAPT_ISSUE_FIELDS.getKey()); }
			 */

			/*
			 * } else { logger.debug("VAPT failed for stage validation for loans"); response
			 * = CommonUtils.formFailResponse(ResponseCodes.VAPT_ISSUE_STAGE.getValue(),
			 * ResponseCodes.VAPT_ISSUE_STAGE.getKey()); }
			 */

		} catch (Exception e) {
			logger.error("Exception in applyLoan method = ", e);
			CommonUtils.generateHeaderForGenericError(responseHeader);
			responseBody.setResponseObj("Exception in apply reject Loan method");
			response.setResponseHeader(responseHeader);
			response.setResponseBody(responseBody);
		}
		responseWrapper.setApiResponse(response);
		logger.warn("End : apply reject Loan method response is:: {}", responseWrapper.toString());
		ResponseEntity<ResponseWrapper> res1 = new ResponseEntity<>(responseWrapper, HttpStatus.OK);
		return Mono.just(res1);
	}

	@ApiResponses({
			@ApiResponse(code = 200, message = "AppzillonBanking Customer onboarding API reachable", response = ResponseWrapper.class),
			@ApiResponse(code = 408, message = "Service Timed Out"),
			@ApiResponse(code = 500, message = "Internal Server Error"),
			@ApiResponse(code = 404, message = "AppzillonBanking Customer onboarding not reachable") })
	@ApiOperation(value = "approve for deviation or reassesment applications", notes = "API to approve for deviation or reassesment applications")
	@PostMapping(value = "/approveDeviationRaApplications", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper> approveDeviationRaApplications(
			@RequestBody ApproveDeviationRaApplicationsReqWrapper approveDeviationRaApplicationsReqWrapper) {
		Response response = new Response();
		ResponseHeader responseHeader = new ResponseHeader();
		ResponseBody responseBody = new ResponseBody();
		ResponseWrapper responseWrapper = new ResponseWrapper();
		ApproveDeviationRaApplicationsReq apiRequest = approveDeviationRaApplicationsReqWrapper.getApiRequest();
		try {
			logger.debug("Incoming api request for approveDeviationRaApplications: {}", apiRequest);
			response = commonService.approveDeviationRaApplications(apiRequest);
		} catch (Exception e) {
			logger.error("Exception in applyLoan method = ", e);
			CommonUtils.generateHeaderForGenericError(responseHeader);
			responseBody.setResponseObj(Constants.SOAP_ERROR_MSG);
			response.setResponseHeader(responseHeader);
			response.setResponseBody(responseBody);
		}
		responseWrapper.setApiResponse(response);
		logger.warn("End : approveDeviationRaApplications method response is:: {}", responseWrapper);
		return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
	}
}
