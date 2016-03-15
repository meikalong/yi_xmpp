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
package com.yilv.service.impl;

import java.util.List;

import javax.persistence.EntityExistsException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yilv.dao.UserDao;
import com.yilv.entity.User;
import com.yilv.exception.UserExistsException;
import com.yilv.exception.UserNotFoundException;
import com.yilv.service.UserService;

/**
 * This class is the implementation of UserService.
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

	protected final Log log = LogFactory.getLog(getClass());

	@Autowired
	private UserDao userDao;

	public User getUser(String userId) {
		return userDao.getUser(new Long(userId));
	}

	public List<User> getUsers() {
		return userDao.getUsers();
	}

	@Transactional(readOnly = false)
	public User saveUser(User user) throws UserExistsException {
		try {
			return userDao.saveUser(user);
		} catch (DataIntegrityViolationException e) {
			e.printStackTrace();
			log.warn(e.getMessage());
			throw new UserExistsException("User '" + user.getUsername() + "' already exists!");
		} catch (EntityExistsException e) { // needed for JPA
			e.printStackTrace();
			log.warn(e.getMessage());
			throw new UserExistsException("User '" + user.getUsername() + "' already exists!");
		}
	}

	public User getUserByUsername(String username) throws UserNotFoundException {
		return (User) userDao.getUserByUsername(username);
	}

	@Transactional(readOnly = false)
	public void removeUser(Long userId) {
		log.debug("removing user: " + userId);
		userDao.removeUser(userId);
	}

}
