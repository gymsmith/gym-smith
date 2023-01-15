package com.todoteg.controller;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.todoteg.model.Subscription;
import com.todoteg.service.ISubscriptionService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/suscription")
public class SubscriptionController {
	
	@Autowired
	private ISubscriptionService service;
	
	@GetMapping
	public Mono<ResponseEntity<Flux<Subscription>>> getAllSubscriptions(){
		return Mono.just(ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.getAll()));
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Subscription>> getSubscriptionById(@PathVariable("id") String id){
		return service.getById(id)
				.map(Subs -> ResponseEntity
						.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(Subs))
				.defaultIfEmpty(ResponseEntity.notFound().build()); //build: Cree la entidad de respuesta sin cuerpo.
	}
	
	@PostMapping 
	public Mono<ResponseEntity<Subscription>> register(@Valid @RequestBody Subscription Subs, final ServerHttpRequest req){
		return service.register(Subs)
				.map(newSubs -> ResponseEntity
						.created(URI.create(req.getURI().toString().concat("/").concat(newSubs.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(newSubs));
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Subscription>> modify(@Valid @RequestBody Subscription Subs, @PathVariable String id){
		
		Mono<Subscription> SubsDB = service.getById(id);
		Mono<Subscription> SubsBody = Mono.just(Subs);
		
		return SubsDB.zipWith(SubsBody, (sDB, sBody)-> {
			sDB.setPlan(sBody.getPlan());
			sDB.setFechaInicial(sBody.getFechaInicial());
			sDB.setFechaFinal(sBody.getFechaFinal());
			sDB.setEstado(sBody.getEstado());
			
			return sDB;
		})
		.flatMap(service::modify) // Mono<Subscripcion>
		.map(modifiedSuscription -> ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(modifiedSuscription))
		.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deleteSucriptionById(@PathVariable String id){
		return service.getById(id) // Mono<Subscripcion>
				.flatMap(SubsToDelete -> {
					return service.deleteById(SubsToDelete.getId())
							.thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)); // <Mono<ResponseEntity<Void>>
				})
				.defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}

}

