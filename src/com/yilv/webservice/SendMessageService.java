package com.yilv.webservice;

import javax.jws.WebParam;
import javax.jws.WebService;

import com.yilv.base.common.web.Result;

@WebService
public interface SendMessageService {
	public Result sendMessage(@WebParam(name = "message") String message);
}
