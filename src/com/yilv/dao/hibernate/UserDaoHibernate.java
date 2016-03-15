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
package com.yilv.dao.hibernate;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.yilv.base.common.dao.hibernate.interfaces.IEnableEntity;
import com.yilv.dao.CommonDao;
import com.yilv.dao.UserDao;
import com.yilv.entity.User;
import com.yilv.exception.UserNotFoundException;

/**
 * This class is the implementation of UserDAO using Spring's HibernateTemplate.
 * 
 * @author Sehwan Noh (devnoh@gmail.com)
 */
@Repository
public class UserDaoHibernate extends CommonDao implements UserDao, IEnableEntity {

	public User getUser(Long id) {
		return (User) getSession().get(User.class, id);
	}

	public User saveUser(User user) {
		getSession().saveOrUpdate(user);
		getSession().flush();
		return user;
	}

	public void removeUser(Long id) {
		getSession().delete(getUser(id));
	}

	public boolean exists(Long id) {
		User user = (User) getSession().get(User.class, id);
		return user != null;
	}

	@SuppressWarnings("unchecked")
	public List<User> getUsers() {
		return (List<User>) getSession().createQuery("from User u order by u.createdDate desc").list();
	}

	@SuppressWarnings("unchecked")
	public User getUserByUsername(String username) throws UserNotFoundException {
		List<User> users = getSession().createQuery("from User where username=?").setParameter(0, username).list();
		if (users == null || users.isEmpty()) {
			throw new UserNotFoundException("User '" + username + "' not found");
		} else {
			return (User) users.get(0);
		}
	}

}
