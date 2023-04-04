package com.todoteg.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.todoteg.model.Tracing;
import com.todoteg.pagination.PageSupport;
import com.todoteg.repo.IGenericRepo;
import com.todoteg.repo.ITracingRepo;
import com.todoteg.service.ITracingService;

import reactor.core.publisher.Mono;

@Service
public class TracingServiceImpl extends CRUDImpl<Tracing, String> implements ITracingService{

	@Autowired
	private ITracingRepo repo;
	
	@Override
	protected IGenericRepo<Tracing, String> getRepo() {
		return repo;
	}

	@Override
	public Mono<PageSupport<Tracing>> searchTracingByClient(String clientCC, Pageable page ) {
		// se crea un mapa con el id del usuario para que coincida la busqueda con la estructura guardado en mongo
		//HashMap<String,String> client = new HashMap<String, String>(); 
		//client.put("identificacion", clientCC);
		return repo.allByUsuario(clientCC) // devuelve la cuenta del total de documentos utilizando como criterio el usuario
				.flatMap(total -> {
					return repo.findByClient(clientCC, page)
							.collectList()
							.map(ListTraces -> new PageSupport<>(ListTraces, page.getPageNumber(), page.getPageSize(), total));
				});
		
	}

}
