package com.todoteg.controller;

import java.net.URI;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Pattern;

import javax.validation.Valid;

import org.bson.BsonDocument;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import com.todoteg.model.Client;
import com.todoteg.pagination.PageSupport;
import com.todoteg.service.IClientService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/client") // se establece la direccion url particular
public class ClientController {

	@Autowired
	private IClientService service;
	
	//ResponseEntity: Clase que me permite extender funcionalidades sobre la peticion http, controlar el status code.
	@GetMapping("/all")
	public Mono<ResponseEntity<Flux<Client>>> clientsList(){
		
		Flux<Client> clients = service.getAll();
		
		return Mono.just(ResponseEntity
				.ok() // indica el status code de respuesta
				.contentType(MediaType.APPLICATION_JSON)
				.body(clients)); // se envia informacion en el body de la respuesta
	}
	
	@GetMapping
	public Mono<ResponseEntity<PageSupport<Client>>> clientsList(
			@RequestParam(name = "search", defaultValue = "$lt") String searchOrComparisonOperator,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "7") int size
			){
	
		Pageable pageRequest = PageRequest.of(page, size);
		
		Pattern patternForComparisonOperator = Pattern.compile("^\\$(eq|ne|gt|gte|lt|lte)$");
		boolean isComparisonOperator = patternForComparisonOperator.matcher(searchOrComparisonOperator).matches(); // Verdadero, si cumple las reglas establecidas por la expresion regular
		
		String isObjectId = ObjectId.isValid(searchOrComparisonOperator)? "ObjectId('%s')".formatted(searchOrComparisonOperator): "'%s'".formatted(searchOrComparisonOperator);
		String matchString = isComparisonOperator? 
				"{ 'subscripcion.fechaFinal': { %s: new Date()}}".formatted(searchOrComparisonOperator) // si recibe un operador de comparacion
				:
				"{$or: [{'nombres': { $regex: /^%s|\\s%s\\b/i} }, {'subscripcion.plan._id': %s}]}"
					.formatted(searchOrComparisonOperator,searchOrComparisonOperator,isObjectId);
		
		
		BsonDocument match = BsonDocument.parse(matchString);

		return service.clientsList(pageRequest, match)
				.map(pag -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(pag))
				.defaultIfEmpty(ResponseEntity.noContent().build());
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Client>> getByCC(@PathVariable("id") String id){
		return service.searchCustomerByCC(id) // Mono<Client> cliente por cedula
				.map(clientByCC -> ResponseEntity
						.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(clientByCC)) // se Transforma a Mono<ResponseEntity<Client>>
				.defaultIfEmpty(ResponseEntity.notFound().build()); //build: Cree la entidad de respuesta sin cuerpo.
	}
	
	@PutMapping("/prueba")
	public Mono<ResponseEntity<Flux<Client>>> ObjectIdAdd(){
		Flux<Client> clients = service.getAll()
				.flatMap(client -> {
					String oldId = client.getId();
					
					
					if(ObjectId.isValid(oldId)) {						
						return Mono.just(client);
					}
					ObjectId newId = new ObjectId(Date.from(client.getFechaCreacion().atZone(ZoneId.systemDefault()).toInstant()));
					client.setId(newId.toHexString());
					client.setIdentificacion(oldId);
					return service.modify(client).flatMap(c -> {
						 return service.deleteById(oldId)
								 .thenReturn(c);
					});
					
				});
		
		return Mono.just(ResponseEntity
				.ok() // indica el status code de respuesta
				.contentType(MediaType.APPLICATION_JSON)
				.body(clients)); // se envia informacion en el body de la respuesta
	}
	
	// @Valid Para hacer cumplir las validaciones propuestas en el modelo
	
	@PostMapping
	public Mono<ResponseEntity<Client>> register(@Valid  @RequestParam("token") String token, @RequestBody Client c, final ServerHttpRequest req){
		return service.register(token, c)
				.map(newClient -> ResponseEntity
						.created(URI.create(req.getURI().toString().concat("/").concat(newClient.getIdentificacion()))) // se obtiene url dinamica del recurso
						.contentType(MediaType.APPLICATION_JSON)
						.body(newClient));
	}
	

	
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Client>> modify(@Valid @PathVariable("id") String id, @RequestBody Client c){
		
		Mono<Client> ClientDB = service.getById(id);
		Mono<Client> ClientBody = Mono.just(c);
		
		return ClientDB.zipWith(ClientBody, (cDB, cBody) -> {
			cDB.setIdentificacion(cBody.getIdentificacion() != null ? cBody.getIdentificacion(): cDB.getIdentificacion());
			cDB.setNombres(cBody.getNombres() != null ? cBody.getNombres(): cDB.getNombres());
			cDB.setTelefono(cBody.getTelefono() != null ? cBody.getTelefono(): cDB.getTelefono());
			cDB.setPeso(cBody.getPeso() != null ? cBody.getPeso(): cDB.getPeso());
			cDB.setAltura(cBody.getAltura() != null ? cBody.getAltura(): cDB.getAltura());
			cDB.setSexo(cBody.getSexo() != null ? cBody.getSexo(): cDB.getSexo());
			cDB.setSubscripcion(cBody.getSubscripcion() != null ? cBody.getSubscripcion(): cDB.getSubscripcion());
			cDB.setActivo(cBody.getActivo() != null ? cBody.getActivo(): cDB.getActivo());
			
			return cDB;
		})
		.flatMap(service::modify) // usuarioModificado -> service.modificar(usuarioModificado) devuelve Mono<Client>
		.map(modifiedClient -> ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(modifiedClient))
		.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> delete(@PathVariable String id){
		return service.getById(id) // Mono<Client>
				.flatMap(clientToDelete -> {
					return service.deleteById(clientToDelete.getIdentificacion())// devuelve Mono<Void>
							.thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)); // <Mono<ResponseEntity<Void>>
				})
				.defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}
}

