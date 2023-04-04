package com.todoteg.repo;

import java.util.HashMap;

import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;

import com.todoteg.model.Tracing;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ITracingRepo extends IGenericRepo<Tracing, String> {
	
	@Query("{'usuario.identificacion': ?0}")
	Flux<Tracing> findByClient (String usuarioId, Pageable paginacion);
	
	@Query(value = "{'usuario.identificacion': ?0}", count=true)
	Mono<Long> allByUsuario(String usuarioId);
}
