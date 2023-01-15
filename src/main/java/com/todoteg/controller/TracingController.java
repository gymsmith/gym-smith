package com.todoteg.controller;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.todoteg.model.Tracing;
import com.todoteg.pagination.PageSupport;
import com.todoteg.service.ITracingService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/tracing")
public class TracingController {
	
	@Autowired
	private ITracingService service;
	
	@GetMapping
	public Mono<ResponseEntity<Flux<Tracing>>> ListAllTraces(){
		return Mono.just(ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.getAll()));
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Tracing>> getTracingById(@PathVariable("id") String id){
		return service.getById(id)
				.map(tracing -> ResponseEntity
						.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(tracing))
				.defaultIfEmpty(ResponseEntity.notFound().build()); //build: Cree la entidad de respuesta sin cuerpo.
	}
	
	@GetMapping("/client/{id}")
	public Mono<ResponseEntity<PageSupport<Tracing>>> ListTracingByClient(
			@PathVariable("id") String id,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "5") int size){
		
		Sort ordering = Sort.by(Sort.Direction.DESC, "fecha");
		Pageable pageRequest = PageRequest.of(page, size, ordering);
		Mono<PageSupport<Tracing>> listTracesPaginable =  service.searchTracingByClient(id, pageRequest);
		
		return listTracesPaginable
				.map(data -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(data))
				.defaultIfEmpty(ResponseEntity.noContent().build());
	}
	
	@PostMapping 
	public Mono<ResponseEntity<Tracing>> registerTracing(@Valid @RequestBody Tracing t, final ServerHttpRequest req){
		return service.register(t)
				.map(newTracing -> ResponseEntity
						.created(URI.create(req.getURI().toString().concat("/").concat(newTracing.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(newTracing));
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Tracing>> modifyTracing(@Valid @RequestBody Tracing t, @PathVariable String id){
		
		Mono<Tracing> tracingDB = service.getById(id);
		Mono<Tracing> tracingBody = Mono.just(t);
		
		return tracingDB.zipWith(tracingBody, (tDB, tBody)-> {
			tDB.setPeso(tBody.getPeso());
			tDB.setPeso(tBody.getPeso());
			tDB.setMedidaCuello(tBody.getMedidaCuello());
			tDB.setMedidaPecho(tBody.getMedidaPecho());
			tDB.setMedidaAbdomen(tBody.getMedidaAbdomen());
			tDB.setMedidaBiceps(tBody.getMedidaBiceps());
			tDB.setMedidaMuslo(tBody.getMedidaMuslo());
			tDB.setMedidaPantorrilla(tBody.getMedidaPantorrilla());
			tDB.setMedidaHombro(tBody.getMedidaHombro());
			tDB.setMedidaCintura(tBody.getMedidaCintura());
			tDB.setMedidaCadera(tBody.getMedidaCadera());
			
			return tDB;
		})
		.flatMap(service::modify) // Mono<Seguimiento>
		.map(modifiedTracing -> ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(modifiedTracing))
		.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deleteTracingById(@PathVariable String id){
		return service.getById(id) // Mono<Seguimiento>
				.flatMap(TracingToDelete -> {
					return service.deleteById(TracingToDelete.getId())
							.thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)); // <Mono<ResponseEntity<Void>>
				})
				.defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}

}

