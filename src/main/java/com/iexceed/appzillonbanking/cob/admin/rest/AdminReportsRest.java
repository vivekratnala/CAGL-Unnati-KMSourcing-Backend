package com.iexceed.appzillonbanking.cob.admin.rest;

//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.iexceed.appzillonbanking.cob.admin.payload.MakerCheckerPayloadEditResponseWrapper;
import com.iexceed.appzillonbanking.cob.core.payload.Response;
import com.iexceed.appzillonbanking.cob.core.payload.ResponseHeader;
import com.iexceed.appzillonbanking.cob.core.payload.ResponseWrapper;
import com.iexceed.appzillonbanking.cob.core.utils.Errors;
import com.iexceed.appzillonbanking.cob.core.utils.ResponseCodes;
import com.iexceed.appzillonbanking.cob.payload.CustomerDataFields;
import com.iexceed.appzillonbanking.cob.payload.FetchDeleteUserRequest;
import com.iexceed.appzillonbanking.cob.payload.FetchDeleteUserRequestWrapper;
import com.iexceed.appzillonbanking.cob.service.COBService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/admin")
@Component
@Api(tags = "Report Generation from Admin", value = "/admin")
public class AdminReportsRest {
	
	@Autowired
	private COBService cobService;


	@ApiOperation(value = "Generate Report from Admin", notes = "API to Generate Report from Admin")
	@ApiResponses({
			@ApiResponse(code = 200, message = "AppzillonBanking API reachable", response = MakerCheckerPayloadEditResponseWrapper.class),
			@ApiResponse(code = 408, message = "Service Timed Out"),
			@ApiResponse(code = 500, message = "Internal Server Error"),
			@ApiResponse(code = 404, message = "AppzillonBanking not reachable") })
	@PostMapping(value = "/generatereport", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseWrapper> generateReport(@RequestBody FetchDeleteUserRequestWrapper requestWrapper) {
		HttpStatus httpStatus = null;
		httpStatus = HttpStatus.OK;
		ResponseWrapper responseWrapper = new ResponseWrapper();
		ResponseHeader respHeader = new ResponseHeader();
		Response response;
		try {
			FetchDeleteUserRequest apiRequest = requestWrapper.getFetchDeleteUserRequest();
			response = cobService.fetchApplication(apiRequest, "fetchapplication", true);
			String responseStr=response.getResponseBody().getResponseObj();
			Gson gson = new Gson();
			CustomerDataFields customerDataFields=gson.fromJson(responseStr, CustomerDataFields.class);	
			cobService.generateReport(customerDataFields);
			//XSSFWorkbook workbook;
			
			responseWrapper.setApiResponse(response);
		} catch (Exception e) {
			response=new Response();
			respHeader.setResponseCode(ResponseCodes.FAILURE.getKey());
			respHeader.setErrorCode(Errors.PROCESSING_REQ_ERROR.getErrorCode());
			respHeader.setResponseMessage(e.getMessage());
			response.setResponseHeader(respHeader);
			responseWrapper.setApiResponse(response);
		}
		return new ResponseEntity<>(responseWrapper, httpStatus);
	}
	
}