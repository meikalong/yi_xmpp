package com.yilv.dao;

import java.lang.reflect.Field;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;

import com.yilv.base.common.dao.hibernate.interfaces.IEnableEntity;
import com.yilv.base.common.utils.Reflections;
import com.yilv.base.common.utils.StringUtils;

public class CommonDao {

	@Autowired
	protected SessionFactory sessionFactory;

	protected Session getSession() {
		// 事务必须是开启的(Required)，否则获取不到的
		return sessionFactory.getCurrentSession();
	}

	protected Criteria getCriteria(Class<?> clz) {
		return getSession().createCriteria(clz);
	}

	/**
	 * 创建查询标准
	 * 
	 * @param criteria
	 * @param list
	 *            list中的对象需要实现指定接口
	 * @param batch
	 *            当递归第一次运行的时候，这是true，其余的时候都为false<br>
	 *            进行联表查询的创建，这里必须设置为true，如果不设置为true，可能会漏掉需要连接的表
	 * @param batchTable
	 */
	protected void createCriterion(Criteria criteria, IEnableEntity entity, String... associationPaths) {

		for (Class<?> clazz = entity.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
			Field[] fields = clazz.getDeclaredFields();
			getSearchField(criteria, entity, fields);
		}

		if (associationPaths.length == 0) {
			return;
		}

		for (int i = 0, l = associationPaths.length; i < l; i++) {
			String associationPath = associationPaths[i];
			traversalClass(criteria, entity, associationPath);
		}

	}

	/**
	 * 对对象属性进行遍历，如果属性值不为空，那就加入搜索条件中，如果对象属性是一个实现了IEnableEntity接口的实体对象，就把该对象放到集合中
	 * 
	 * @param criteria
	 * @param entity
	 * @param fields
	 */
	private void getSearchField(Criteria criteria, IEnableEntity entity, Field[] fields) {
		for (Field field : fields) {
			try {
				String fieldName = field.getName();
				Object fieldValue = Reflections.invokeGetter(entity, fieldName);
				if (fieldValue == null || StringUtils.isEmpty(String.valueOf(fieldValue))) {
					continue;
				}

				if (fieldValue instanceof IEnableEntity) {
					continue;
				}

				String type = field.getType().toString();
				if (type.endsWith("int") || type.endsWith("Integer") || type.endsWith("double")
						|| type.endsWith("Double") || type.endsWith("long") || type.endsWith("Long")
						|| type.endsWith("short") || type.endsWith("Short") || type.endsWith("char")
						|| type.endsWith("Character") || type.endsWith("float") || type.endsWith("Float")
						|| type.endsWith("byte") || type.endsWith("Byte") || type.endsWith("boolean")
						|| type.endsWith("Boolean") || type.endsWith("String") || type.endsWith("Date")) {
					// System.out.println(entity.getClass().getSimpleName() +
					// ":" + fieldName + ">>>>>>" + fieldValue);
					Criterion c = Restrictions.eq(fieldName, fieldValue);
					criteria.add(c);
				}

			} catch (IllegalArgumentException e) {
				// 允许找不到get方法
			}
		}
	}

	/**
	 * 获得一个对象中的所有属性，包括父类
	 * 
	 * @param criteria
	 * @param entity
	 * @param associationPath2
	 *            待遍历对象
	 * @param list
	 *            用于收集对象
	 * @param batchTable
	 */
	private void traversalClass(Criteria criteria, IEnableEntity entity, String associationPath) {

		if (associationPath != null) {
			criteria = criteria.createCriteria(associationPath, JoinType.LEFT_OUTER_JOIN);
		}

		// 第一次运行到这里的时候batchFlag为true，其余的时候都为false
		// flag为true表示找到了要遍历的对象，否则就是没找到
		Object iEntity = null;
		try {
			iEntity = Reflections.invokeGetter(entity, associationPath);
		} catch (NullPointerException nullException) {
			// 考虑到这里对象可能为空，所以允许在运行方法的时候出现空指针异常
			// 为空之后，就不需要对属性进行遍历了，所以就返回函数
			return;
		}
		if (iEntity == null) {
			// 如果对象是空的，就不含有属性，无需遍历，直接返回函数
			return;
		}
		for (Class<?> clazz = iEntity.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
			Field[] fields = clazz.getDeclaredFields();
			getSearchField(criteria, (IEnableEntity) iEntity, fields);
		}

	}
}
