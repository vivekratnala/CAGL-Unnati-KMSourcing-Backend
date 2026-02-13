package com.iexceed.appzillonbanking.cob.cards.rest;

import com.iexceed.appzillonbanking.cob.cards.payload.*;
import com.iexceed.appzillonbanking.cob.cards.service.CreditCardService;
import com.iexceed.appzillonbanking.cob.core.payload.Header;
import com.iexceed.appzillonbanking.cob.core.payload.Response;
import com.iexceed.appzillonbanking.cob.core.payload.ResponseBody;
import com.iexceed.appzillonbanking.cob.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.cob.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.cob.core.utils.*;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Properties;

@RestController
@RequestMapping("/creditcard")
@Component
@Api(tags = "CREDITCARD", value = "/creditcard")
public class CreditCardAPI {

    private static final Logger logger = LogManager.getLogger(CreditCardAPI.class);

    @Autowired
    private CreditCardService ccService;

    @Autowired
    private AdapterUtil adapterUtil;

    @ApiResponses({
            @ApiResponse(code = 200, message = "AppzillonBanking credit card API reachable", response = ResponseWrapper.class),
            @ApiResponse(code = 408, message = "Service Timed Out"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 404, message = "AppzillonBanking Customer onboarding not reachable")})
    @ApiOperation(value = "Apply for a credit card", notes = "API to Apply for a credit card")
    @PostMapping(value = "/applyCreditCard", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseWrapper>> applyCreditCard(@RequestBody ApplyCreditCardRequestWrapper requestWrapper,
                                                           @RequestHeader(defaultValue = "APZCOB") String reqAppId,
                                                           @RequestHeader(defaultValue = "applyCreditCard") String interfaceId,
                                                           @RequestHeader(defaultValue = "000000000002") String userId,
                                                           @RequestHeader(defaultValue = "12345678") String masterTxnRefNo,
                                                           @RequestHeader(defaultValue = "abcd1234efgh5678") String deviceId) {
        Response response = new Response();
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            Properties prop = CommonUtils.readPropertyFile();
            boolean isSelfOnBoardingHeaderAppId = false;
            boolean isSelfOnBoardingAppId = false;
            ApplyCreditCardRequest apiRequest = requestWrapper.getApiRequest();
            String headerAppId = apiRequest.getAppId();
            if (headerAppId.equalsIgnoreCase(prop.getProperty(CobFlagsProperties.APPID_SELF_ONBOARDING.getKey()))) {
                isSelfOnBoardingHeaderAppId = true;
            }
            if (headerAppId.equalsIgnoreCase(prop.getProperty(CobFlagsProperties.APPID_SELF_ONBOARDING.getKey()))) {
                isSelfOnBoardingAppId = true;
            }
            JSONArray array=ccService.fetchFunctionSeqArray(apiRequest, isSelfOnBoardingHeaderAppId);
            if (ccService.isValidStage(apiRequest, isSelfOnBoardingHeaderAppId, array)) { //VAPT
                if (ccService.isVaptPassedForScreenElements(apiRequest, array)) {  //VAPT
                Header header= CommonUtils.obtainHeader(reqAppId, interfaceId, userId, masterTxnRefNo, deviceId);
                Mono<Response> response1 = ccService.applyCreditCard(apiRequest, prop, isSelfOnBoardingAppId, isSelfOnBoardingHeaderAppId, array, header);
                return response1.flatMap(val -> {
                    ResponseWrapper responseWrapper1 = ResponseWrapper.builder().apiResponse(val).build();
                    return Mono.just(new ResponseEntity<>(responseWrapper1, HttpStatus.OK));
                });
                } else {
			    		logger.debug("VAPT failed for screen elements for cards");
						response=CommonUtils.formFailResponse(ResponseCodes.VAPT_ISSUE_FIELDS.getValue(), ResponseCodes.VAPT_ISSUE_FIELDS.getKey());
			    }
            } else {
                logger.debug("VAPT failed for stage validation for cards");
                response = CommonUtils.formFailResponse(ResponseCodes.VAPT_ISSUE_STAGE.getValue(), ResponseCodes.VAPT_ISSUE_STAGE.getKey());
            }
        } catch (Exception e) {
            logger.error("Exception in applyCreditCard method = ", e);
            CommonUtils.generateHeaderForGenericError(responseHeader);
            responseBody.setResponseObj("Exception in applyCreditCard method");
            response.setResponseHeader(responseHeader);
            response.setResponseBody(responseBody);
        }
        responseWrapper.setApiResponse(response);
        logger.warn("End : applyCreditCard method response is:: " + responseWrapper.toString());
        ResponseEntity<ResponseWrapper> res1= new ResponseEntity<>(responseWrapper, HttpStatus.OK);
        return Mono.just(res1);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "AppzillonBanking credit card API reachable", response = ResponseWrapper.class),
            @ApiResponse(code = 408, message = "Service Timed Out"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 404, message = "AppzillonBanking Customer onboarding not reachable")})
    @ApiOperation(value = "Check application is present", notes = "API to Check application is present")
    @PostMapping(value = "/checkapplication", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseWrapper>> checkApplication(@RequestBody CheckApplicationRequestWrapper requestWrapper,
                                                            @RequestHeader(defaultValue = "APZCOB") String appId,
                                                            @RequestHeader(defaultValue = "checkapplication") String interfaceId,
                                                            @RequestHeader(defaultValue = "000000000002") String userId,
                                                            @RequestHeader(defaultValue = "12345678") String masterTxnRefNo,
                                                            @RequestHeader(defaultValue = "abcd1234efgh5678") String deviceId) {
        Mono<Response> response;
        try {
            Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
            response= ccService.checkApplication(requestWrapper.getApiRequest(), header);
        } catch (Exception e) {
        	 response = FallbackUtils.genericFallbackMono();
        }
        return response.flatMap(val -> {
        	logger.warn("End : checkApplication method for credit cards response is:: " + val.toString());
            ResponseWrapper responseWrapper1 = ResponseWrapper.builder().apiResponse(val).build();
            return Mono.just(new ResponseEntity<>(responseWrapper1, HttpStatus.OK));
        });
     }

    @ApiResponses({
            @ApiResponse(code = 200, message = "AppzillonBanking Customer onboarding API reachable", response = ResponseWrapper.class),
            @ApiResponse(code = 408, message = "Service Timed Out"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 404, message = "AppzillonBanking Customer onboarding not reachable")})
    @ApiOperation(value = "Download Application", notes = "API to Download Application")
    @PostMapping(value = "/downloadapplication", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper> downloadApplication(@RequestBody FetchAppReqWrapper requestWrapper) {
        Response response = new Response();
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();
        ResponseWrapper responseWrapper = new ResponseWrapper();
        try {
            logger.warn("Inside downloadApplication" + requestWrapper.getFetchDeleteUserRequest());
            response = ccService.downloadApplication(requestWrapper.getFetchDeleteUserRequest());
        } catch (Exception e) {
            logger.error("Exception in downloadApplication method = ", e);
            CommonUtils.generateHeaderForGenericError(responseHeader);
            responseBody.setResponseObj("Exception in downloadApplication method");
            response.setResponseHeader(responseHeader);
            response.setResponseBody(responseBody);
        }
        responseWrapper.setApiResponse(response);
        logger.warn("End : downloadApplication method response is:: " + responseWrapper);
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "AppzillonBanking Customer onboarding API reachable", response = ResponseWrapper.class),
            @ApiResponse(code = 408, message = "Service Timed Out"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 404, message = "AppzillonBanking Customer onboarding not reachable")})
    @ApiOperation(value = "Fetch customer's data", notes = "API to fetch customer's data")
    @PostMapping(value = "/fetchapplication", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper> fetchApplication(@RequestBody FetchAppReqWrapper requestWrapper) {
        Response response = new Response();
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();
        ResponseWrapper responseWrapper = new ResponseWrapper();
        FetchAppReq req = requestWrapper.getFetchDeleteUserRequest();
        try {
            response = ccService.fetchApplication(req);
        } catch (Exception e) {
            logger.error("Exception in fetchapplication method = ", e);
            CommonUtils.generateHeaderForGenericError(responseHeader);
            responseBody.setResponseObj("Exception in fetchapplication method");
            response.setResponseHeader(responseHeader);
            response.setResponseBody(responseBody);
        }
        responseWrapper.setApiResponse(response);
        logger.warn("End : fetchApplication method response is:: " + responseWrapper.toString());
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }


    @ApiResponses({
            @ApiResponse(code = 200, message = "AppzillonBanking API reachable", response = ResponseWrapper.class),
            @ApiResponse(code = 408, message = "Service Timed Out"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 404, message = "AppzillonBanking not reachable")})
    @ApiOperation(value = "Fetch Customer Details", notes = "API to Fetch Customer Details")
    @PostMapping(value = "/fetchcustomerdetails", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseWrapper>> fetchCustomerDetails(@RequestBody FetchCustDtlReqWrapper requestWrapper,
                                                                      @RequestHeader(defaultValue = "APZCOB") String appId,
                                                                      @RequestHeader(defaultValue = "fetchcustomerdetails") String interfaceId,
                                                                      @RequestHeader(defaultValue = "000000000002") String userId,
                                                                      @RequestHeader(defaultValue = "12345678") String masterTxnRefNo,
                                                                      @RequestHeader(defaultValue = "abcd1234efgh5678") String deviceId) {
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);

        Mono<Object> response;
        try {
            response = ccService.fetchCustomerDetails(requestWrapper.getApiRequest(), header);
        } catch (Exception e) {
            logger.error("Fetch Customer Details ERROR for credit cards= ", e);
            response = FallbackUtils.genericFallbackMonoObject();
        }
        logger.debug("End : Fetch Eligible Cards response for credit cards:: " + response.toString());
        return adapterUtil.generateResponseWrapper(response, requestWrapper.getApiRequest().getInterfaceName(), header);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "AppzillonBanking API reachable", response = ResponseWrapper.class),
            @ApiResponse(code = 408, message = "Service Timed Out"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 404, message = "AppzillonBanking not reachable")})
    @ApiOperation(value = "Fetch Eligible Cards", notes = "API to Fetch Eligible Cards")
    @PostMapping(value = "/fetcheligiblecards", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseWrapper>> fetchEligibleCards(@RequestBody FetchEligibleCardsReqWrapper requestWrapper,
                                                                    @RequestHeader(defaultValue = "APZCOB") String appId,
                                                                    @RequestHeader(defaultValue = "fetcheligiblecards") String interfaceId,
                                                                    @RequestHeader(defaultValue = "000000000002") String userId,
                                                                    @RequestHeader(defaultValue = "12345678") String masterTxnRefNo,
                                                                    @RequestHeader(defaultValue = "abcd1234efgh5678") String deviceId) {
        Header header = CommonUtils.obtainHeader(appId, interfaceId, userId, masterTxnRefNo, deviceId);
        Mono<Object> response;
        try {
            response = ccService.fetchEligibleCards(requestWrapper.getApiRequest(), header);
        } catch (Exception e) {
            logger.error("Fetch Eligible Cards ERROR for credit cards= ", e);
            response = FallbackUtils.genericFallbackMonoObject();
        }
        logger.debug("End : Fetch Eligible Cards response for credit cards:: " + response.toString());
        return adapterUtil.generateResponseWrapper(response, requestWrapper.getApiRequest().getInterfaceName(), header);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "AppzillonBanking Customer onboarding API reachable", response = ResponseWrapper.class),
            @ApiResponse(code = 408, message = "Service Timed Out"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 404, message = "AppzillonBanking Customer onboarding not reachable")})
    @ApiOperation(value = "Discard and create new Credit Card Application", notes = "API to Discard and create new Credit Card Application")
    @PostMapping(value = "/discardandcreateapplication", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<ResponseWrapper>> discardAndCreateApp(@RequestBody ApplyCreditCardRequestWrapper requestWrapper,
                                                               @RequestHeader(defaultValue = "APZCOB") String reqAppId,
                                                               @RequestHeader(defaultValue = "fetchroi") String interfaceId,
                                                               @RequestHeader(defaultValue = "000000000002") String userId,
                                                               @RequestHeader(defaultValue = "12345678") String masterTxnRefNo,
                                                               @RequestHeader(defaultValue = "abcd1234efgh5678") String deviceId) {
        Response response = new Response();
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();
        ResponseWrapper responseWrapper = new ResponseWrapper();
        ApplyCreditCardRequest apiRequest = requestWrapper.getApiRequest();

        try {
            Properties prop = CommonUtils.readPropertyFile();
            boolean isSelfOnBoardingHeaderAppId = false;
            boolean isSelfOnBoardingAppId = false;
            String appId = apiRequest.getRequestObj().getAppId();
            String headerAppId = apiRequest.getAppId();
            if (headerAppId.equalsIgnoreCase(prop.getProperty(CobFlagsProperties.APPID_SELF_ONBOARDING.getKey()))) {
                isSelfOnBoardingHeaderAppId = true;
            }
            if (appId.equalsIgnoreCase(prop.getProperty(CobFlagsProperties.APPID_SELF_ONBOARDING.getKey()))) {
                isSelfOnBoardingAppId = true;
            }
            boolean discarded = ccService.discardApplication(apiRequest);
            if (discarded) {
                apiRequest.getRequestObj().setApplicationId(null); //set this to null so that new application Id will be created in createDeposit method.
                apiRequest.getRequestObj().getApplicationMaster().setCustDtlId(null); //set this to null so that new custDtlId will be created in createApplication method.
                Header header= CommonUtils.obtainHeader(reqAppId, interfaceId, userId, masterTxnRefNo, deviceId);
                Mono<Response> response1 = ccService.applyCreditCard(apiRequest, prop, isSelfOnBoardingAppId, isSelfOnBoardingHeaderAppId, null, header);
                return response1.flatMap(val -> {
                    ResponseWrapper responseWrapper1 = ResponseWrapper.builder().apiResponse(val).build();
                    return Mono.just(new ResponseEntity<>(responseWrapper1, HttpStatus.OK));
                });
            } else {
                responseHeader.setResponseCode(ResponseCodes.INVALID_DISCARD.getKey());
                responseBody.setResponseObj(ResponseCodes.INVALID_DISCARD.getValue());
                response.setResponseBody(responseBody);
                response.setResponseHeader(responseHeader);
            }
        } catch (Exception e) {
            logger.error("Exception in discardAndCreateApp method for cards= ", e);
            CommonUtils.generateHeaderForGenericError(responseHeader);
            responseBody.setResponseObj("Exception in discardApplication method");
            response.setResponseHeader(responseHeader);
            response.setResponseBody(responseBody);
        }
        responseWrapper.setApiResponse(response);
        logger.warn("End : applyCreditCard method response is:: " + responseWrapper.toString());
        ResponseEntity<ResponseWrapper> res1= new ResponseEntity<>(responseWrapper, HttpStatus.OK);
        return Mono.just(res1);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "AppzillonBanking Customer onboarding API reachable", response = ResponseWrapper.class),
            @ApiResponse(code = 408, message = "Service Timed Out"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 404, message = "AppzillonBanking Customer onboarding not reachable")})
    @ApiOperation(value = "Update common code from admin", notes = "API to Update common code from admin")
    @PostMapping(value = "/updatecommoncode", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper> updateCommonCode(@RequestBody UpdateCommonCodeRequestWrapper requestWrapper) {
        Response response = new Response();
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();
        ResponseWrapper responseWrapper = new ResponseWrapper();
        UpdateCommonCodeRequest req = requestWrapper.getApiRequest();
        try {
            response = ccService.updateCommonCode(req);
        } catch (Exception e) {
            logger.error("Exception in updateCommonCode method for cards= ", e);
            CommonUtils.generateHeaderForGenericError(responseHeader);
            responseBody.setResponseObj("Exception in updateCommonCode method");
            response.setResponseHeader(responseHeader);
            response.setResponseBody(responseBody);
        }
        responseWrapper.setApiResponse(response);
        logger.warn("End : updateCommonCode method response is:: " + responseWrapper.toString());
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }

    @ApiResponses({
            @ApiResponse(code = 200, message = "AppzillonBanking Customer onboarding API reachable", response = ResponseWrapper.class),
            @ApiResponse(code = 408, message = "Service Timed Out"),
            @ApiResponse(code = 500, message = "Internal Server Error"),
            @ApiResponse(code = 404, message = "AppzillonBanking Customer onboarding not reachable")})
    @ApiOperation(value = "Delete file from server", notes = "API to Delete file from server")
    @PostMapping(value = "/deletefile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseWrapper> deleteFile(@RequestBody DeleteFileRequestWrapper requestWrapper) {
        Response response = new Response();
        ResponseHeader responseHeader = new ResponseHeader();
        ResponseBody responseBody = new ResponseBody();
        ResponseWrapper responseWrapper = new ResponseWrapper();
        DeleteFileRequest req = requestWrapper.getApiRequest();
        try {
            response = ccService.deleteFile(req);
        } catch (Exception e) {
            logger.error("Exception in deletefile method for cards= ", e);
            CommonUtils.generateHeaderForGenericError(responseHeader);
            responseBody.setResponseObj("Exception in deletefile method");
            response.setResponseHeader(responseHeader);
            response.setResponseBody(responseBody);
        }
        responseWrapper.setApiResponse(response);
        logger.warn("End : deletefile method response is:: " + responseWrapper.toString());
        return new ResponseEntity<>(responseWrapper, HttpStatus.OK);
    }
}