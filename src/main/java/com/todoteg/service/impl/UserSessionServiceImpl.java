package com.todoteg.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.todoteg.model.UserSession;
import com.todoteg.repo.IGenericRepo;
import com.todoteg.repo.IRoleRepo;
import com.todoteg.repo.IUserSessionRepo;
import com.todoteg.security.User;
import com.todoteg.service.IUserSessionService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class UserSessionServiceImpl extends CRUDImpl<UserSession, String> implements IUserSessionService {
	private Pattern BCRYPT_PATTERN = Pattern.compile("\\A\\$2(a|y|b)?\\$(\\d\\d)\\$[./0-9A-Za-z]{53}");
	@Autowired
	private IUserSessionRepo repo;

	@Autowired
	private IRoleRepo rolRepo;
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
	@Override
	protected IGenericRepo<UserSession, String> getRepo() {
		return repo;
	}

	@Override
	public Mono<UserSession> registerHash(UserSession user) {
		Matcher matcher = this.BCRYPT_PATTERN.matcher(user.getClave()); // compara la entrada dada con el patron establecido
		
		// Verdadero si toda la secuencia coincide con el patron de dicho comparador
		if(!matcher.matches()) {			
			user.setClave(bcrypt.encode(user.getClave()));
		}
		return repo.save(user);		
	}

	@Override
	public Mono<User> searchUserLogin(String username) {
		Mono<UserSession> user = repo.findOneByUsuario(username); // trae todos los datos del usuario por su Atributo usuario
		
		List<String> roles = new ArrayList<String>(); // se intancia una lista vacia para listar los roles del usuario luego
		
		return user.flatMap(u -> {
			return Flux.fromIterable(u.getRoles())  // se crea un flux con cada id-rol del usuario como item 
					.flatMap(rol -> {				
						return rolRepo.findById(rol.getId()) // en este punto se consulta el id de cada rol para obtener sus datos completos
								.map(r -> {
									roles.add(r.getNombre());// se añade el nombre del  rol a la lista de roles antes instanciada
									return r;
								});
					}).collectList().flatMap(list -> { // se compacta en un Mono<List>
						u.setRoles(list); // Se Remplaza el bloque de roles en el objecto usuario por la nueva lista con los datos completos
						return Mono.just(u);
					});
		})	
		.flatMap(u -> {			
			// se cumple con el requerimiento del metodo; y se crea una instancia de user requerido por spring security
			return Mono.just(new User(u.getId(), u.getNombres(), u.getUsuario(), u.getClave(), u.getEstado(), roles)); 
		});
	}

	@Override
	public Mono<UserSession> findOneByUser(String username) {
		return repo.findOneByUsuario(username);
	}


}
