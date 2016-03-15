/**
 * 
 */
package com.yilv.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.springframework.stereotype.Repository;

import com.yilv.dao.CommonDao;
import com.yilv.dao.NotificationDao;
import com.yilv.entity.NotificationMO;
import com.yilv.entity.ReportVO;

/**
 * @author chengqiang.liu
 * 
 */
@Repository
public class NotificationDaoHibernate extends CommonDao implements NotificationDao {

	public void deleteNotification(Long id) {
		getSession().delete(queryNotificationById(id));
	}

	public NotificationMO queryNotificationById(Long id) {
		NotificationMO notificationMO = (NotificationMO) getSession().get(NotificationMO.class, id);
		return notificationMO;
	}

	public void saveNotification(NotificationMO notificationMO) {
		getSession().saveOrUpdate(notificationMO);
		getSession().flush();
	}

	public void updateNotification(NotificationMO notificationMO) {
		getSession().update(notificationMO);
		getSession().flush();
	}

	@SuppressWarnings("unchecked")
	public List<NotificationMO> queryNotificationByUserName(String userName, String messageId) {
		return (List<NotificationMO>) getSession()
				.createQuery("from NotificationMO n where n.username=? and n.messageId=? order by n.createTime desc")
				.setParameter(0, userName).setParameter(1, messageId).list();
	}

	public int queryCountByStatus(String status, String messageId) {
		return 0;
	}

	@SuppressWarnings("unchecked")
	public List<NotificationMO> queryNotification(NotificationMO mo) {
		Criteria criteria = getCriteria(mo.getClass());

		createCriterion(criteria, mo);

		List<NotificationMO> list = criteria.setCacheable(false).list();

		return list;
	}

	public List<ReportVO> queryReportVO(NotificationMO mo) {
		// TODO Auto-generated method stub
		return null;
	}

}
