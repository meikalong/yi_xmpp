package com.yilv.service;

import org.androidpn.server.util.Config;
import org.androidpn.server.xmpp.session.SessionManager;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xmpp.packet.JID;

import com.yilv.entity.Chat;
import com.yilv.exception.UserNotFoundException;
import com.yilv.push.NotificationManager;

@Service
public class ChatService {
	@Autowired
	private NotificationManager notificationManager;
	private SessionManager sessionManager = SessionManager.getInstance();

	public void sendMessage(Chat packet) {
		Element element = packet.getElement();
		Element msg = element.element("message");
		String message = msg.element("content").getText();
		String nickname = msg.element("nickname").getText();
		String apiKey = Config.getString("apiKey", "");
		JID sender = packet.getFrom();
		try {
			System.out.println("发送者session>>>>>>>>>>" + sessionManager.getSession(sender).getId());
		} catch (UserNotFoundException e) {
			e.printStackTrace();
		}
		notificationManager.sendAllBroadcast(apiKey, nickname, message, "");
	}

}
