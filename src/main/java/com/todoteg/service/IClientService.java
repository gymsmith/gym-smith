package com.todoteg.service;

import org.bson.BsonDocument;
import org.springframework.data.domain.Pageable;

import com.todoteg.model.Client;
import com.todoteg.pagination.PageSupport;

import reactor.core.publisher.Mono;


public interface IClientService extends ICRUD<Client, String> {
	//Mono<User> ObtenerUsuarioPorNombre(String usuario);
	Mono<Client> searchCustomerByCC(String id);
	Mono<Client> register(String serial, Client cliente);
	//Mono<PageSupport<ClienteInfoUtil>> ListarPagina(String busquedaPorNombre, Pageable pagina);
	Mono<PageSupport<Client>> clientsList(Pageable page,  BsonDocument match);
}
