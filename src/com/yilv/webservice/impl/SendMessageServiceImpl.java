package com.yilv.webservice.impl;

import org.androidpn.server.util.Config;
import org.springframework.beans.factory.annotation.Autowired;

import com.yilv.base.common.web.Result;
import com.yilv.push.NotificationManager;
import com.yilv.webservice.SendMessageService;

public class SendMessageServiceImpl implements SendMessageService {

	@Autowired
	private NotificationManager notificationManager;

	@Override
	public Result sendMessage(String message) {
		String apiKey = Config.getString("apiKey", "");
		notificationManager.sendBroadcast(apiKey, "webservice", message, "");
		return new Result(Result.SUCCESS, message);
	}

}
