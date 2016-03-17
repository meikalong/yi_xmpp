package com.yilv.webservice;

import javax.jws.WebService;

@WebService
public interface GreetingService {
	public String greeting(String userName);
}
