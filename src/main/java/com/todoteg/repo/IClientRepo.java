package com.todoteg.repo;

import org.bson.BsonDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.Query;

import com.todoteg.model.Client;
import com.todoteg.model.util.PageSupportUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



public interface IClientRepo extends IGenericRepo<Client, String> {
	//Mono<Cliente> findByNombres (String usuario);
	
	@Aggregation(pipeline = {
			"{$match: {identificacion: ?0}}",
			"{ $project: { huella:0 } }",
			"{$lookup: {from: 'planes', localField: 'subscripcion.plan._id' , foreignField: '_id' , as: 'subscripcion.plan'}}",
			"{ $unwind: '$subscripcion.plan' }",
			"{$lookup: {"
			+ "from: 'seguimiento', let: {customerId:'$identificacion'},"
			+ "pipeline: [{$match: {$expr:{ $eq: [ '$usuario.identificacion',  '$$customerId' ] }}}, { $project: { usuario:0 } }, {$sort:{_id: -1}},{$limit: 5}],"
			+ "as: 'listSeguimiento'}"
			+ "}}"
	})
	Mono<Client> searchClientsByCC(String id);
	
	/*
	 * @Query("{$or: [{'nombres': ?0},{'identificacion': ?0}]}") Mono<Cliente>
	 * buscarUsuario (String usuario);
	 */
	
	/* { 'nombres': /^?0/i  } -> indica que la busqueda debe iniciar por los caracteres que recibe dicha funcion sin distincion entre mayusculas y minusculas.
	@Query("{$or: [{'nombres': { $regex: /^?0|\\s?0\\b/, $options: 'i'} }, {'subscripcion.plan._id': ?0}]}")
	Flux<Cliente> buscarUsuarios(String nombres, Pageable pagina);
	
	@Query(value = "{$or: [{'nombres': { $regex: /^?0|\\s?0\\b/, $options: 'i'} }, {'subscripcion.plan._id': ?0}]}", count=true)
	Mono<Long> allByNombresAndPlan(String nombres);
	*/
	@Aggregation(pipeline = {
			"{$match: ?0}", 
			"{$sort: {'_id': -1}}", 
			"{$lookup: {from: 'planes', localField: 'subscripcion.plan._id' , foreignField: '_id' , as: 'subscripcion.plan'}}",
			"{$unwind: '$subscripcion.plan'}",
			"{$lookup: {"
					+ "from: 'seguimiento', let: {customerId:'$identificacion'},"
					+ "pipeline: [{$match: {$expr:{ $eq: [ '$usuario.identificacion',  '$$customerId' ] }}}, { $project: { usuario:0 } }, {$sort:{_id: -1}},{$limit: 5}],"
					+ "as: 'listSeguimiento'}"
					+ "}}",
			"{$facet: "
			+ "{content: [{$skip: ?1},{$limit: ?2}],"
			+ "totalElements:[{$count: 'count'}]}}"})
	Mono<PageSupportUtil> searchClients(BsonDocument match, int pageNumber, int pageSize);
	

	/*
	@Query(value ="{ 'subscripcion.fechaFinal': { $gt: new Date()}}", count=true)
	Mono<Long> allCustomersByExpiredSubscription();
	*/
	@Query("{}")
	Flux<Client> searchAllClientsPaginable(Pageable pageable);
	
	
}
