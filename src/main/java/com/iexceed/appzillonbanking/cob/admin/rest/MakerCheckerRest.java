package com.iexceed.appzillonbanking.cob.admin.rest;

import com.iexceed.appzillonbanking.cob.admin.payload.*;
import com.iexceed.appzillonbanking.cob.admin.service.MakerCheckerService;
import com.iexceed.appzillonbanking.cob.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.cob.core.utils.Errors;
import com.iexceed.appzillonbanking.cob.core.utils.ResponseCodes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/makerChecker")
@Component
@PropertySource("classpath:/Swagger-MC-Properties/Admin-rest.properties")
@Api(tags = "MakerChecker", value = "/makerChecker")
public class MakerCheckerRest {

	private static final Logger logger = LogManager.getLogger(MakerCheckerRest.class);

	@Autowired
	private MakerCheckerService makerCheckerService;

	private String sameMakerChecker = "SAME_MAKER_CHECKER";

	private String noData = "NO_DATA";

	@ApiOperation(value = "${makerCheckerIUAPI}", notes = "${makerCheckerIUAPI.Description}")
	@ApiResponses({
			@ApiResponse(code = 200, message = "AppzillonBanking API reachable", response = MakerCheckerPayloadEditResponseWrapper.class),
			@ApiResponse(code = 408, message = "Service Timed Out"),
			@ApiResponse(code = 500, message = "Internal Server Error"),
			@ApiResponse(code = 404, message = "AppzillonBanking not reachable") })
	@PostMapping(value = "/addEdit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MakerCheckerPayloadEditResponseWrapper> insertMakerCheckerPayload(
			@RequestBody MakerCheckerPayloadRequestWrapper makerCheckerPayloadRequestWrapper) {

		HttpStatus httpStatus = null;
		httpStatus = HttpStatus.OK;

		logger.warn("Inside makerChecker insertPayload......" + makerCheckerPayloadRequestWrapper.toString());
		MakerCheckerPayloadEditResponseWrapper makerCheckerPayloadEditResponseWrapper = new MakerCheckerPayloadEditResponseWrapper();
		try {
			MakerCheckerPayloadRequest makerCheckerPayloadRequest = makerCheckerPayloadRequestWrapper
					.getMakerCheckerPayloadRequest();
			String lStatus = this.makerCheckerService.insertUpdatePayload(makerCheckerPayloadRequest);
			logger.warn("lStatus in rest class "+lStatus);
			makerCheckerPayloadEditResponseWrapper = makerCheckerService
					.validateResponseStatus(makerCheckerPayloadRequest, lStatus);
		} catch (Exception e) {
			logger.error(com.iexceed.appzillonbanking.cob.admin.utils.CommonUtils.EXCEPTION_OCCURED, e);
			ResponseHeader respHeader = new ResponseHeader();
			respHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
			respHeader.setErrorCode(Errors.PROCESSING_REQ_ERROR.getErrorCode());
			respHeader.setResponseMessage(e.getMessage());
		}
		return new ResponseEntity<>(makerCheckerPayloadEditResponseWrapper, httpStatus);
	}

	@ApiOperation(value = "${makerCheckerFetchAPI}", notes = "${makerCheckerFetchAPI.Description}")
	@ApiResponses({
			@ApiResponse(code = 200, message = "AppzillonBanking API reachable", response = MakerCheckerPayloadResponseWrapper.class),
			@ApiResponse(code = 408, message = "Service Timed Out"),
			@ApiResponse(code = 500, message = "Internal Server Error"),
			@ApiResponse(code = 404, message = "AppzillonBanking not reachable") })
	@PostMapping(value = "/fetch", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MakerCheckerPayloadResponseWrapper> fetchPayload(
			@RequestBody MakerCheckerFetchPayloadRequestWrapper makerCheckerPayloadRequestWrapper) {

		HttpStatus httpStatus = null;
		httpStatus = HttpStatus.OK;
		logger.warn("Inside makerChecker fetchPayload......" + makerCheckerPayloadRequestWrapper.toString());
		MakerCheckerPayloadResponseWrapper makerCheckerPayloadFetchResponseWrapper = new MakerCheckerPayloadResponseWrapper();
		try {
			MakerCheckerFetchPayloadRequest makerCheckerPayloadRequest = makerCheckerPayloadRequestWrapper
					.getMakerCheckerPayloadRequest();
			List<MakerCheckerPayloadFetchResponse> lFetchResp = this.makerCheckerService.fetchAllData(makerCheckerPayloadRequest.getFeatureId(), makerCheckerPayloadRequest.getUserId());
			if (!lFetchResp.isEmpty()) {
				makerCheckerPayloadFetchResponseWrapper.setMakerCheckerPayloadResponse(lFetchResp);
				ResponseHeader respHeader = new ResponseHeader();
				respHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());
				respHeader.setErrorCode("");
				respHeader.setResponseMessage("");
				makerCheckerPayloadFetchResponseWrapper.setResponseHeader(respHeader);
			} else {
				ResponseHeader respHeader = new ResponseHeader();
				respHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
				respHeader.setErrorCode("1");
				respHeader.setResponseMessage("No Record Found");
				makerCheckerPayloadFetchResponseWrapper.setResponseHeader(respHeader);
			}
		} catch (Exception e) {
			logger.error(com.iexceed.appzillonbanking.cob.admin.utils.CommonUtils.EXCEPTION_OCCURED, e);
			ResponseHeader respHeader = new ResponseHeader();
			respHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
			respHeader.setErrorCode(Errors.PROCESSING_REQ_ERROR.getErrorCode());
			respHeader.setResponseMessage(e.getMessage());
		}
		return new ResponseEntity<>(makerCheckerPayloadFetchResponseWrapper, httpStatus);
	}

	@ApiOperation(value = "${makerCheckerAuthAPI}", notes = "${makerCheckerAuthAPI.Description}")
	@ApiResponses({
			@ApiResponse(code = 200, message = "AppzillonBanking API reachable", response = MakerCheckerPayloadEditResponseWrapper.class),
			@ApiResponse(code = 408, message = "Service Timed Out"),
			@ApiResponse(code = 500, message = "Internal Server Error"),
			@ApiResponse(code = 404, message = "AppzillonBanking not reachable") })
	@PostMapping(value = "/authorize", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<MakerCheckerPayloadEditResponseWrapper> authorizePayLoad(
			@RequestBody MakerCheckerFetchPayloadRequestWrapper makerCheckerPayloadRequestWrapper) {

		HttpStatus httpStatus = null;
		httpStatus = HttpStatus.OK;
		ResponseHeader respHeader = new ResponseHeader();
		logger.warn("Inside makerChecker insertPayload......" + makerCheckerPayloadRequestWrapper.toString());
		MakerCheckerPayloadEditResponse makerCheckerPayloadEditResponse = new MakerCheckerPayloadEditResponse();
		MakerCheckerPayloadEditResponseWrapper makerCheckerPayloadEditResponseWrapper = new MakerCheckerPayloadEditResponseWrapper();
		try {
			MakerCheckerFetchPayloadRequest makerCheckerPayloadRequest = makerCheckerPayloadRequestWrapper
					.getMakerCheckerPayloadRequest();
			String lStatus = this.makerCheckerService.authorizeMakerCheckerRecord(makerCheckerPayloadRequest);
			if (sameMakerChecker.equalsIgnoreCase(lStatus)) {
				makerCheckerPayloadEditResponse.setStatus("FAILURE");
				makerCheckerPayloadEditResponse.setErrorCode(sameMakerChecker);
				makerCheckerPayloadEditResponse.setErrorMessage("Same Maker Checker Authorization not allowed.");
				respHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
				respHeader.setErrorCode(sameMakerChecker);
				respHeader.setResponseMessage("Same Maker Checker Authorization not allowed.");
			} else if (com.iexceed.appzillonbanking.cob.admin.utils.CommonUtils.SUCCESS_UC
					.equalsIgnoreCase(lStatus)) {
				makerCheckerPayloadEditResponse.setStatus(com.iexceed.appzillonbanking.cob.admin.utils.CommonUtils.SUCCESS_UC);
				makerCheckerPayloadEditResponse.setErrorCode("");
				makerCheckerPayloadEditResponse.setErrorMessage("");
				respHeader.setResponseCode(ResponseCodes.SUCCESS.getKey());
				respHeader.setErrorCode("");
				respHeader.setResponseMessage("");
			} else if (noData.equalsIgnoreCase(lStatus)) {
				makerCheckerPayloadEditResponse.setStatus(com.iexceed.appzillonbanking.cob.admin.utils.CommonUtils.FAILURE_UC);
				makerCheckerPayloadEditResponse.setErrorCode(noData);
				makerCheckerPayloadEditResponse.setErrorMessage("No Data to Authorize.");
				respHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
				respHeader.setErrorCode(noData);
				respHeader.setResponseMessage("No Data to Authorize.");
			}
			makerCheckerPayloadEditResponse.setId(makerCheckerPayloadRequest.getId());
			makerCheckerPayloadEditResponse.setFeatureId(makerCheckerPayloadRequest.getFeatureId());
		} catch (Exception e) {
			logger.error(com.iexceed.appzillonbanking.cob.admin.utils.CommonUtils.EXCEPTION_OCCURED, e);
			respHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
			respHeader.setErrorCode(Errors.PROCESSING_REQ_ERROR.getErrorCode());
			respHeader.setResponseMessage(e.getMessage());
		}
		makerCheckerPayloadEditResponseWrapper.setResponseHeader(respHeader);
		makerCheckerPayloadEditResponseWrapper.setMakerCheckerPayloadResponse(makerCheckerPayloadEditResponse);
		return new ResponseEntity<>(makerCheckerPayloadEditResponseWrapper, httpStatus);
	}
}
