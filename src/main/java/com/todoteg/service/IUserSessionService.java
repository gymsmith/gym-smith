package com.todoteg.service;

import com.todoteg.model.UserSession;
import com.todoteg.security.User;

import reactor.core.publisher.Mono;

public interface IUserSessionService extends ICRUD<UserSession, String>{
	Mono<UserSession> findOneByUser(String username);
	Mono<UserSession> registerHash(UserSession user);
	Mono<User> searchUserLogin(String username);
	
}
