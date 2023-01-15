package com.todoteg.repo;


import com.todoteg.model.UserSession;

import reactor.core.publisher.Mono;


public interface IUserSessionRepo extends IGenericRepo<UserSession, String>{

	//Necesario para la seccion de seguridad
	
	//SELECT * FROM USUARIO U WHERE U.USUARIO = ? 
	//{usuario : ?}
	//DerivedQueries
	Mono<UserSession> findOneByUsuario(String usuario);	

}
