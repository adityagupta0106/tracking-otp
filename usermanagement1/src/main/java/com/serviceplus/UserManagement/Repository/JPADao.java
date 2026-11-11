package com.serviceplus.UserManagement.Repository;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

public abstract class JPADao<K,E> {


	protected Class<E> entityClass;
	
	@Autowired
	protected EntityManagerFactory entityManagerFactory;
	
	EntityManager entityManager;
	
	@PostConstruct
	public void init() {
		//super.setEntityManagerFactory(entityManagerFactory);
		//this.entityManager = entityManagerFactory.createEntityManager();
	}
	
	@SuppressWarnings("unchecked")
	public JPADao() {
		ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
		this.entityClass = (Class<E>) genericSuperclass.getActualTypeArguments()[1];
		//this.entityManager = entityManagerFactory.createEntityManager();
	}
	
	//@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
	public void persist(E entity) {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(entity);
			em.getTransaction().commit();
		} catch(Exception e) {
			em.getTransaction().rollback();
			e.printStackTrace();
			throw e;
		} finally {
			em.close();
		}
	}

	public void remove(E entity) {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.remove(entity);
			em.getTransaction().commit();
		} catch(Exception e) {
			em.getTransaction().rollback();
			e.printStackTrace();
			throw e;
		} finally {
			em.close();
		}
	}

	public E merge(E entity) {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			entity =  em.merge(entity);
			em.getTransaction().commit();
		} catch(Exception e) {
			em.getTransaction().rollback();
			e.printStackTrace();
			throw e;
		} finally {
			em.close();
		}
		return entity;
	}
	
	

	public void refresh(E entity) {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.refresh(entity);
			em.getTransaction().commit();
		} catch(Exception e) {
			em.getTransaction().rollback();
			e.printStackTrace();
			throw e;
		} finally {
			em.close();
		}
	}

	public E findById(K id) {
		EntityManager em = getEntityManager();
		try {
			return em.find(entityClass, id);
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			em.close();
		}
	}

	public E flush(E entity) {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.flush();
			em.getTransaction().commit();
		} catch(Exception e) {
			em.getTransaction().rollback();
			e.printStackTrace();
			throw e;
		} finally {
			em.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<E> findAll() {
		EntityManager em = getEntityManager();
		try {
			return (List<E>) em.createQuery( "from " + entityClass.getName() ).getResultList();
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			em.close();
		}
	}

	public EntityManager getEntityManager() {
		entityManager = entityManagerFactory.createEntityManager();
		return entityManager;
	}
	
	public void closeEntityManager() {
		entityManager.close();
	}

}
