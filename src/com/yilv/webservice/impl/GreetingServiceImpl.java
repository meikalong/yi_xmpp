package com.yilv.webservice.impl;

import java.util.Calendar;

import com.yilv.webservice.GreetingService;

public class GreetingServiceImpl implements GreetingService {

	public String greeting(String userName) {
		return "Hello " + userName + ", currentTime is " + Calendar.getInstance().getTime();
	}
}
