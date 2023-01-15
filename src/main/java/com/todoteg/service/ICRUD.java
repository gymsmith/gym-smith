package com.todoteg.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICRUD<T, ID> {
	Mono<T> register(T t);
	Mono<T> modify(T t);
	Flux<T> getAll();
	Mono<T> getById(ID id);
	Mono<Void> deleteById(ID id);
}
