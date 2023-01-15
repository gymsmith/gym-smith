package com.todoteg.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.todoteg.model.Subscription;
import com.todoteg.repo.IGenericRepo;
import com.todoteg.repo.ISubscriptionRepo;
import com.todoteg.service.ISubscriptionService;

@Service
public class SubscriptionServiceImpl extends CRUDImpl<Subscription, String> implements ISubscriptionService{

	@Autowired
	private ISubscriptionRepo repo;
	
	@Override
	protected IGenericRepo<Subscription, String> getRepo() {
		return repo;
	}

}
