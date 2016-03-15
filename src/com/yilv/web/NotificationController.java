package com.yilv.web;

/*
 * Copyright (C) 2010 Moduad Co., Ltd.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.androidpn.server.util.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.yilv.push.NotificationManager;

/**
 * A controller class to process the notification related requests.
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */
@Controller
public class NotificationController {

	@Autowired
	private NotificationManager notificationManager;

	@RequestMapping("notification")
	public String list(HttpServletRequest request, HttpServletResponse response) {
		return "notification/form";
	}

	@RequestMapping("notificationsend")
	public ModelAndView send(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String broadcast = ServletRequestUtils.getStringParameter(request, "broadcast", "Y");
		String username = ServletRequestUtils.getStringParameter(request, "username");
		String title = ServletRequestUtils.getStringParameter(request, "title");
		String message = ServletRequestUtils.getStringParameter(request, "message");
		String uri = ServletRequestUtils.getStringParameter(request, "uri");

		String apiKey = Config.getString("apiKey", "");
		// 在线用户
		if (broadcast.equalsIgnoreCase("Y")) {
			notificationManager.sendBroadcast(apiKey, title, message, uri);
			// 所有用户
		} else if (broadcast.equalsIgnoreCase("A")) {
			notificationManager.sendAllBroadcast(apiKey, title, message, uri);
			// 指定用户
		} else {
			notificationManager.sendNotifications(apiKey, username, title, message, uri);
		}

		ModelAndView mav = new ModelAndView();
		mav.setViewName("redirect:notification.do");
		return mav;
	}

}
