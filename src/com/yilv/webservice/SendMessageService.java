package com.yilv.webservice;

import javax.jws.WebParam;
import javax.jws.WebService;

@WebService
public interface SendMessageService {
	public String sendMessage(@WebParam(name = "message") String message);
}
