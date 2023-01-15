package com.todoteg.repo;

import java.util.HashMap;

import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;

import com.todoteg.model.Tracing;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ITracingRepo extends IGenericRepo<Tracing, String> {
	Flux<Tracing> findByUsuario (HashMap<String, String> usuarioId, Pageable paginacion);
	
	@Query(value = "{ usuario: ?0 }", count=true)
	Mono<Long> allByUsuario(HashMap<String, String> usuarioId);
}
