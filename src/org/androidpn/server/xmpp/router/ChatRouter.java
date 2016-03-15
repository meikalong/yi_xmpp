package org.androidpn.server.xmpp.router;

import com.yilv.entity.Chat;
import com.yilv.service.ChatService;

public class ChatRouter {

	private ChatService chatService;

	/**
	 * Constucts a packet router registering new IQ handlers.
	 */
	public ChatRouter() {
		chatService = new ChatService();
	}

	public void route(Chat packet) {
		if (packet == null) {
			throw new NullPointerException();
		}
		chatService.sendMessage(packet);
	}

}
