package com.todoteg.service;

import java.util.HashMap;
import java.util.List;

import com.todoteg.model.ClienteHuellaUtil;
import com.todoteg.model.HttpushResponseUtil;
import com.todoteg.model.HuellaTemp;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



public interface IHuellaTempService extends ICRUD<HuellaTemp, String> {
	
	Mono<Void> eliminarTodo();
	
	Mono<HuellaTemp> ObtenerHuellaTempPorSerial(String serial);
	
	Mono<List<HuellaTemp>> ObtenerUpdateTimePorSerial(String serial, String campo);
	
	Mono<HttpushResponseUtil> ObtenerHuellaHttpush();
	
	Mono<HashMap<String, Object>> HabilitarSensor(String Token);
	
	Flux<ClienteHuellaUtil> ObtenerClienteHuella(int desde, int hasta);
	
}
