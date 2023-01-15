package com.todoteg.repo;

import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;

import com.todoteg.model.TemporaryFingerprint;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ITemporaryFingerprintRepo extends IGenericRepo<TemporaryFingerprint, String> {
	Mono<TemporaryFingerprint> findByPcSerial (String serial);
	
	
	// Optiene Solo el campo update_time siempre y cuando pcSerial coincida
	@Query(value = "{'pcSerial' :  ?0 }", fields = "{ ?1 : 1 , '_id': 0}")
	Flux<TemporaryFingerprint> getOneFieldBySerial(String serial, String field, Pageable pageable);
	
	@Query(value = "{'pcSerial' :  ?0 }", fields = "{ ?1 : 1 , ?2 : 1, '_id': 0}")
	Flux<TemporaryFingerprint> getTwoSelectedFieldsBySerial(String serial, String field1, String field2, Pageable pageable);
	
	@Query(value = "{}", fields = "{'pcSerial': 1,'imgHuella': 1,'update_time': 1,'texto': 1,'statusPlantilla': 1,'documento': 1,'nombre': 1,'opc': 1}")
	Flux<TemporaryFingerprint> getFingerprintHttpush(Pageable pageable);
	
	@Query("{'pcSerial' :  ?0 },{'fecha_creacion': 1, 'opc': 1}")
	Flux<TemporaryFingerprint> getCreationDateAndOpcFieldsBySerial(String serial, Pageable pageable);
}
