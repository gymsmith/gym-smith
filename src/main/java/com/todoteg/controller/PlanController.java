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

import com.todoteg.model.Plan;
import com.todoteg.service.IPlanService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/plan")
public class PlanController {
	
	@Autowired
	private IPlanService service;
	
	@GetMapping
	public Mono<ResponseEntity<Flux<Plan>>> getAllPlans(){
		return Mono.just(ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.getAll()));
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Plan>> getPlanById(@PathVariable("id") String id){
		return service.getById(id)
				.map(p -> ResponseEntity
						.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(p))
				.defaultIfEmpty(ResponseEntity.notFound().build()); //build: Cree la entidad de respuesta sin cuerpo.
	}
	
	@PostMapping 
	public Mono<ResponseEntity<Plan>> register(@Valid @RequestBody Plan p, final ServerHttpRequest req){
		return service.register(p)
				.map(newPlan -> ResponseEntity
						.created(URI.create(req.getURI().toString().concat("/").concat(newPlan.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(newPlan));
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Plan>> modify(@Valid @RequestBody Plan p, @PathVariable String id){
		
		Mono<Plan> PlanBD = service.getById(id);
		Mono<Plan> PlanBody = Mono.just(p);
		
		return PlanBD.zipWith(PlanBody, (pBD, pBody)-> {
			pBD.setTitulo(pBody.getTitulo());
			pBD.setSubtitulo(pBody.getSubtitulo());
			pBD.setPrecio(pBody.getPrecio());
			pBD.setDescripcion(pBody.getDescripcion());
			
			return pBD;
		})
		.flatMap(service::modify) // Mono<Plan>
		.map(modifiedPlan -> ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(modifiedPlan))
		.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deletePlanById(@PathVariable String id){
		return service.getById(id) // Mono<Plan>
				.flatMap(planToDelete -> {
					return service.deleteById(planToDelete.getId())
							.thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)); // <Mono<ResponseEntity<Void>>
				})
				.defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}

}

