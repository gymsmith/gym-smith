package com.todoteg.service.impl;

//import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.todoteg.model.TemporaryFingerprint;
import com.todoteg.model.util.ClientFingerprintUtil;
import com.todoteg.model.util.HttpushResponseUtil;
import com.todoteg.repo.IClientRepo;
import com.todoteg.repo.IGenericRepo;
import com.todoteg.repo.ITemporaryFingerprintRepo;
import com.todoteg.service.ITemporaryFingerprintService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class TemporaryFingerprintServiceImpl extends CRUDImpl<TemporaryFingerprint, String> implements ITemporaryFingerprintService{

	@Autowired
	private ITemporaryFingerprintRepo repo;
	
	@Autowired
	private IClientRepo repoCliente;
	
	@Override
	protected IGenericRepo<TemporaryFingerprint, String> getRepo() {
		return repo;
	}

	@Override
	public Mono<TemporaryFingerprint> getTemporaryFingerprintBySerial(String serial) {
		return repo.findByPcSerial(serial);
	}
	
	@Override
	public Mono<List<TemporaryFingerprint>> getUpdateTimeOrCreationDateBySerial(String serial, String field) {
		String orderBy = (field == "update_time")? field: "id";
		PageRequest pageRequest = PageRequest.of(0, 1, Sort.Direction.DESC, orderBy);
		if (field != "update_time")
			return repo.getOneFieldBySerial(serial, field, pageRequest)
				.collectList();
		else
			return repo.getTwoSelectedFieldsBySerial(serial, field, "statusPlantilla", pageRequest)
					.collectList();
	}

	@Override
	public Flux<ClientFingerprintUtil> getAllClientsFingerprint(int desde, int hasta) {
		Mono<Long> count = repoCliente.count();
		
		Pageable range = PageRequest.of(desde, hasta);
		
		
		return repoCliente.searchAllClientsPaginable(range)
				.flatMap(client -> {
					return count.flatMap(cou -> {
						ClientFingerprintUtil clientResponse = new ClientFingerprintUtil();
						clientResponse.setCount(cou);
						clientResponse.setDocumento(client.getIdentificacion());
						clientResponse.setNombre_completo(client.getNombres());
						clientResponse.setNombre_dedo("indice derecho");
						clientResponse.setHuella(client.getHuella());
						clientResponse.setImgHuella(client.getImgHuella());
						return Mono.just(clientResponse);
					});
					
				});
					

				
	}

	@Override
	public Mono<HttpushResponseUtil> getFingerprintNewState() {
		PageRequest pageRequest = PageRequest.of(0, 1, Sort.Direction.DESC, "update_time");
		
		HttpushResponseUtil responseDefault = new HttpushResponseUtil("reintentar");
		
		// GLOSARIO
		// .switchIfEmpty -> devuelve un mono alternativo si el actual esta vacio
		// .all(predicado) -> Emite un solo valor booleano verdadero si todos los valores de esta secuencia coinciden con el Predicado.
		return repo.getFingerprintHttpush(pageRequest)
				.flatMap(h ->{
					// Aqui hay un problema cuando h.getUpdate_time() llega nulo no se puede realizar h.getUpdate_time().getTime() / 1000
					// Esto ocurre cuando el status del sensor reporta los 4 capturas faltantes
					
					long timestamp = h.getUpdate_time() != null ? h.getUpdate_time().getTime() / 1000: 0; // cuando aun no existe fecha de actualizacion esta se establece en cero
					
					// respuesta adaptada a el modelo de datos requerido
					HttpushResponseUtil response = new HttpushResponseUtil(h.getPc_serial(),timestamp, h.getTexto(), h.getStatusPlantilla(), h.getNombre(),h.getDocumento(),h.getImgHuella(),h.getOpc());
					
					return Mono.just(response);
				})
				.defaultIfEmpty(responseDefault)
				.single();
	}

	@Override
	public Mono<HashMap<String, Object>> enableSensor(String token) {
		PageRequest pageRequest = PageRequest.of(0, 1, Sort.Direction.DESC, "id");
		HashMap<String, Object> responseDefault = new HashMap<>();
		responseDefault.put("fecha_creacion", 0);
		responseDefault.put("opc", "reintentar");
		
		return repo.getCreationDateAndOpcFieldsBySerial(token, pageRequest)
				.flatMap(h -> {
					HashMap<String, Object> response = new HashMap<>();
					response.put("fecha_creacion", h.getFecha_creacion().getTime() / 1000);
					response.put("opc", h.getOpc());
					
					return Mono.just(response);
				})
				.collectList().map(Lista -> {
					return (Lista.size() > 0) ? Lista.get(0):responseDefault;
				});
	}

	@Override
	public Mono<Void> deleteAllFingerprint() {
		return repo.deleteAll();
	}



}
