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
package org.androidpn.server.xmpp.session;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.androidpn.server.util.Config;
import org.androidpn.server.util.Global;
import org.androidpn.server.xmpp.net.Connection;
import org.androidpn.server.xmpp.net.ConnectionCloseListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.xmpp.packet.JID;

/**
 * This class manages the sessions connected to the server.
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */
@Component
public class SessionManager {

	private static final Log log = LogFactory.getLog(SessionManager.class);

	private static SessionManager instance = new SessionManager();

	private String serverName;

	private Map<String, ClientSession> preAuthSessions = new ConcurrentHashMap<String, ClientSession>();

	private Map<String, ClientSession> clientSessions = new ConcurrentHashMap<String, ClientSession>();

	private final AtomicInteger connectionsCounter = new AtomicInteger(0);

	private ClientSessionListener clientSessionListener = new ClientSessionListener();

	private SessionManager() {
		serverName = Global.SERVLETNAME;
		Thread thread = new Thread(new ExpiresProcessorBackground());
		// 设置为守护线程
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Returns the singleton instance of SessionManager.
	 * 
	 * @return the instance
	 */
	public static SessionManager getInstance() {
		return instance;
	}

	/**
	 * Creates a new ClientSession and returns it.
	 * 
	 * @param conn
	 *            the connection
	 * @return a newly created session
	 */
	public ClientSession createClientSession(Connection conn) {
		if (serverName == null) {
			throw new IllegalStateException("Server not initialized");
		}

		Random random = new Random();
		String streamId = Integer.toHexString(random.nextInt());

		ClientSession session = new ClientSession(serverName, conn, streamId);
		conn.init(session);
		conn.registerCloseListener(clientSessionListener);

		// Add to pre-authenticated sessions
		preAuthSessions.put(session.getAddress().getResource(), session);

		// Increment the counter of user sessions
		connectionsCounter.incrementAndGet();

		log.debug("创建ClientSession...");
		return session;
	}

	/**
	 * Adds a new session that has been authenticated.
	 * 
	 * @param session
	 *            the session
	 */
	public void addSession(ClientSession session) {
		preAuthSessions.remove(session.getStreamID().toString());
		clientSessions.put(session.getAddress().toString(), session);
	}

	/**
	 * Returns the session associated with the username.
	 * 
	 * @param username
	 *            the username of the client address
	 * @return the session associated with the username
	 */
	public ClientSession getSession(String username) {
		// TODO change by tzj
		return getSession(new JID(username, serverName, "AndroidpnClient"));
		// TODO old
		// return getSession(new JID(username, serverName, RESOURCE_NAME,true));
	}

	/**
	 * Returns the session associated with the JID.
	 * 
	 * @param from
	 *            the client address
	 * @return the session associated with the JID
	 */
	public ClientSession getSession(JID from) {
		if (from == null || serverName == null || !serverName.equals(from.getDomain())) {
			return null;
		}
		// Check pre-authenticated sessions
		if (from.getResource() != null) {
			ClientSession session = preAuthSessions.get(from.getResource());
			if (session != null) {
				return session;
			}
		}
		if (from.getResource() == null || from.getNode() == null) {
			return null;
		}
		ClientSession session = clientSessions.get(from.toString());
		return session;
	}

	/**
	 * Returns a list that contains all authenticated client sessions.
	 * 
	 * @return a list that contains all client sessions
	 */
	public Collection<ClientSession> getSessions() {
		return clientSessions.values();
	}

	/**
	 * Removes a client session.
	 * 
	 * @param session
	 *            the session to be removed
	 * @return true if the session was successfully removed
	 */
	public boolean removeSession(ClientSession session) {
		if (session == null || serverName == null) {
			return false;
		}
		JID fullJID = session.getAddress();

		// Remove the session from list
		boolean clientRemoved = clientSessions.remove(fullJID.toString()) != null;
		boolean preAuthRemoved = (preAuthSessions.remove(fullJID.getResource()) != null);

		// Decrement the counter of user sessions
		if (clientRemoved || preAuthRemoved) {
			connectionsCounter.decrementAndGet();
			return true;
		}
		return false;
	}

	/**
	 * Closes the all sessions.
	 */
	public void closeAllSessions() {
		try {
			// Send the close stream header to all connections
			Set<ClientSession> sessions = new HashSet<ClientSession>();
			sessions.addAll(preAuthSessions.values());
			sessions.addAll(clientSessions.values());

			for (ClientSession session : sessions) {
				try {
					session.getConnection().systemShutdown();
				} catch (Throwable t) {
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * A listner to handle a session that has been closed.
	 */
	private class ClientSessionListener implements ConnectionCloseListener {

		public void onConnectionClose(Object handback) {
			try {
				ClientSession session = (ClientSession) handback;
				removeSession(session);
			} catch (Exception e) {
				log.error("Could not close socket", e);
			}
		}
	}

	// TODO Added by ken
	private class ExpiresProcessorBackground implements Runnable {

		private final Log log = LogFactory.getLog(getClass());
		private long checkTimeoutInterval;
		private long maxInactiveInterval;
		private SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd hh:mm:ss");

		public ExpiresProcessorBackground() {
			checkTimeoutInterval = Config.getLong("xmpp.session.maxInactiveInterval", 10 * 1000);
			maxInactiveInterval = Config.getLong("xmpp.session.maxInactiveInterval", -1);
			log.debug("maxInactiveInterval:" + maxInactiveInterval);
		}

		@SuppressWarnings("static-access")
		public void run() {
			while (true) {
				processExpires();
				try {
					Thread.currentThread().sleep(checkTimeoutInterval);
				} catch (InterruptedException e) {
					log.debug(e);
				}
			}
		}

		public void processExpires() {
			long timeNow = System.currentTimeMillis();
			int expireHere = 0;
			int size = getSessions().size();

			if (log.isDebugEnabled()) {
				log.debug("开始检查session有效性,时间：" + dateFormat.format(new Date()) + ",检查session数量：" + size);
			}
			for (ClientSession session : getSessions()) {
				if (isValid(session)) {
					expireHere++;
					session.close();
				}
			}
			long timeEnd = System.currentTimeMillis();
			if (log.isDebugEnabled()) {
				log.debug("检查session完毕,耗时：" + (timeEnd - timeNow) + "ms 关闭session数量：" + expireHere);
			}
		}

		/**
		 * 检查session的时间，是否过期
		 * 
		 * @param session
		 * @return
		 */
		public boolean isValid(ClientSession session) {
			if (session == null) {
				return false;
			}
			if (maxInactiveInterval >= 0) {
				long timeNow = System.currentTimeMillis();
				int timeIdle = (int) ((timeNow - session.getLastActiveTimeMillis()) / 1000L);
				return timeIdle >= maxInactiveInterval / 1000L;
			}

			return false;
		}

	}

}
