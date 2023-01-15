package com.todoteg.service.impl;

import com.todoteg.repo.IGenericRepo;
import com.todoteg.service.ICRUD;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class CRUDImpl<T,ID> implements ICRUD<T,ID> {

	protected abstract IGenericRepo<T,ID> getRepo();
	
	@Override
	public Mono<T> register(T t) {
		return getRepo().save(t);
	}

	@Override
	public Mono<T> modify(T t) {
		return getRepo().save(t);
	}

	@Override
	public Flux<T> getAll() {
		return getRepo().findAll();
	}

	@Override
	public Mono<T> getById(ID id) {
		return getRepo().findById(id);
	}

	@Override
	public Mono<Void> deleteById(ID id) {
		return getRepo().deleteById(id);
	}

	


}
