package com.todoteg.service.impl;


import java.util.Arrays;
import org.bson.BsonDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.todoteg.model.Client;
import com.todoteg.model.Plan;
import com.todoteg.pagination.PageSupport;
import com.todoteg.repo.IClientRepo;
import com.todoteg.repo.IGenericRepo;
import com.todoteg.repo.ITemporaryFingerprintRepo;
import com.todoteg.repo.IPlanRepo;
import com.todoteg.service.IClientService;


import reactor.core.publisher.Mono;


@Service
public class ClientServiceImpl extends CRUDImpl<Client, String> implements IClientService {
	
	@Autowired
	private IClientRepo repo;
	
	@Autowired
	private IPlanRepo repoPlan;
	
	@Autowired
	private ITemporaryFingerprintRepo repoHuella;

	@Override
	protected IGenericRepo<Client, String> getRepo() {
		return repo;
	}
	
	@Override
	public Mono<Client> register(String serial, Client cliente){
		return repoHuella.findByPcSerial(serial)
				.flatMap(Huella -> {
					cliente.setHuella(Huella.getHuella());
					cliente.setImgHuella(Huella.getImgHuella());
					return repoHuella.deleteById(Huella.getId())
							.thenReturn(cliente);
					
				})
				.flatMap(repo::save);
		
	}
	
	@Override
	public Mono<Client> modify(Client cliente){
		return repo.save(cliente)
				.flatMap(c -> {
					if(c.getSubscripcion() != null) {
						return repoPlan.findById(c.getSubscripcion().getPlan().getId())
								.map(P -> {
									Plan plan = new Plan();
									plan.setId(P.getId());
									plan.setTitulo(P.getTitulo());
									c.getSubscripcion().setPlan(plan);
									return c;
									})
								.defaultIfEmpty(c);
					}
					return Mono.just(c);
						
				});
	}
	 

	public Mono<Client> searchCustomerByCC(String id) {
		return repo.searchClientsByCC(id);
	}
	
	@Override
	public Mono<PageSupport<Client>> clientsList(Pageable page,  BsonDocument match) {
		var skip =  page.getPageNumber()*page.getPageSize();
		var limit = page.getPageSize();

		return repo.searchClients(match, skip, limit)
						.flatMap(customer -> {
									
								PageSupport<Client> pageCustomer = new PageSupport<Client>(
										Arrays.asList(customer.getContent()),
										page.getPageNumber(),page.getPageSize(), customer.getTotalElements()
										); 
								return Mono.just(pageCustomer);
						});
							
		
	}
	

}
