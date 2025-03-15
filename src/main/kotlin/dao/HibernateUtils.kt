package dao

import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration

class HibernateUtils {
    val sessionFactory: SessionFactory = Configuration().configure().buildSessionFactory()

    fun getSession() = sessionFactory.openSession()

    fun <T> loadAllData(type: Class<T>, session: Session): List<T> {
        val builder: CriteriaBuilder = session.criteriaBuilder
        val criteria: CriteriaQuery<T> = builder.createQuery(type)
        criteria.from(type)
        val data: List<T> = session.createQuery(criteria).resultList
        return data
    }
}