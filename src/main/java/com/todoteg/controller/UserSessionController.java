package com.todoteg.controller;

import java.net.URI;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.todoteg.model.UserSession;
import com.todoteg.service.ITemporaryFingerprintService;
import com.todoteg.service.IUserSessionService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("register")
public class UserSessionController {
	
	@Autowired
	private IUserSessionService service;
	
	@Autowired
	private ITemporaryFingerprintService HuellaTempService;
	
	
	@GetMapping("/{username}")
	public Mono<ResponseEntity<UserSession>> getUser(@PathVariable("username") String username){
		return service.findOneByUser(username)
				.map(user -> ResponseEntity
										.ok()
										.contentType(MediaType.APPLICATION_JSON)
										.body(user)
										)
				.defaultIfEmpty(ResponseEntity.notFound().build());
				
	}
	
	@PostMapping
	public Mono<ResponseEntity<UserSession>> registerUser(@RequestBody UserSession u, final ServerHttpRequest req){
		return service.registerHash(u)
				.map(NewUser -> ResponseEntity
						.created(URI.create(req.getURI().toString().concat("/").concat(NewUser.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(NewUser));
	}
	
	@PutMapping("/{username}")
	public Mono<ResponseEntity<UserSession>> modifyUser(@PathVariable("username") String username, @RequestParam(name="serial", defaultValue = "") String serial, @RequestBody UserSession u){
		Mono<UserSession> userDB = service.findOneByUser(username);
		Mono<UserSession> userBody = Mono.just(u);
		return HuellaTempService.getTemporaryFingerprintBySerial(serial)
				.flatMap(fingerprint -> {
					return userDB.zipWith(userBody, (uDB, uBDY)-> {
						uDB.setIdentificacion(uBDY.getIdentificacion() != null? uBDY.getIdentificacion(): uDB.getIdentificacion());
						uDB.setNombres(uBDY.getNombres() != null? uBDY.getNombres(): uDB.getNombres());
						uDB.setImgHuella(fingerprint.getImgHuella());
						uDB.setHuella(fingerprint.getHuella());
						uDB.setFirma(uBDY.getFirma() != null? uBDY.getFirma(): uDB.getFirma());
						System.out.println(uBDY.getClave());
						uDB.setUsuario(uBDY.getUsuario() != null? uBDY.getUsuario(): uDB.getUsuario());
						uDB.setEstado(uBDY.getEstado() != null? uBDY.getEstado(): uDB.getEstado());
						uDB.setClave(uBDY.getClave() != null? uBDY.getClave(): uDB.getClave());
						
						return uDB;
					})
					.flatMap(service::registerHash)
					.map(modifiedUser -> ResponseEntity
									.ok()
									.contentType(MediaType.APPLICATION_JSON)
									.body(modifiedUser))
					.defaultIfEmpty(ResponseEntity.notFound().build());
				})
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deleteUserById(@PathVariable("id") String id){
		return service.getById(id)
				.flatMap(UserToDelete -> {
					return service.deleteById(UserToDelete.getId())
							.thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)); // <Mono<ResponseEntity<Void>>
				})
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
}
