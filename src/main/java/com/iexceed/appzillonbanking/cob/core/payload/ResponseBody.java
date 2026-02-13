package com.iexceed.appzillonbanking.cob.core.payload;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter
public class ResponseBody {

	@ApiModelProperty(example = "This field contains the actual api response")
	public String responseObj;
	
	public ResponseBody() {
		super();
	}
	
	public ResponseBody(String responseObj) {
		super();
		this.responseObj = responseObj;
	}

	@Override
	public String toString() {
		return "Response [responseObj=" + responseObj + "]";
	}
}
