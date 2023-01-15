package com.todoteg.service;

import org.springframework.data.domain.Pageable;

import com.todoteg.model.Tracing;
import com.todoteg.pagination.PageSupport;

import reactor.core.publisher.Mono;

public interface ITracingService extends ICRUD<Tracing, String> {
	Mono<PageSupport<Tracing>> searchTracingByClient (String clientCC, Pageable paginacion);
}

