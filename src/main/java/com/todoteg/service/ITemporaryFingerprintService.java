package com.todoteg.service;

import java.util.HashMap;
import java.util.List;

import com.todoteg.model.TemporaryFingerprint;
import com.todoteg.model.util.ClientFingerprintUtil;
import com.todoteg.model.util.HttpushResponseUtil;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



public interface ITemporaryFingerprintService extends ICRUD<TemporaryFingerprint, String> {
	
	Mono<Void> deleteAllFingerprint();
	
	Mono<TemporaryFingerprint> getTemporaryFingerprintBySerial(String serial);
	
	Mono<List<TemporaryFingerprint>> getUpdateTimeOrCreationDateBySerial(String serial, String campo);
	
	Mono<HttpushResponseUtil> getFingerprintNewState();
	
	Mono<HashMap<String, Object>> enableSensor(String Token);
	
	Flux<ClientFingerprintUtil> getAllClientsFingerprint(int desde, int hasta);
	
}
