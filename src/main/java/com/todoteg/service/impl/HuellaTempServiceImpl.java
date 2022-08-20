package com.todoteg.service.impl;

//import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.todoteg.model.ClienteHuellaUtil;
import com.todoteg.model.HttpushResponseUtil;
import com.todoteg.model.HuellaTemp;
import com.todoteg.repo.IClienteRepo;
import com.todoteg.repo.IGenericRepo;
import com.todoteg.repo.IHuellaTempRepo;
import com.todoteg.service.IHuellaTempService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service
public class HuellaTempServiceImpl extends CRUDImpl<HuellaTemp, String> implements IHuellaTempService{

	@Autowired
	private IHuellaTempRepo repo;
	
	@Autowired
	private IClienteRepo repoCliente;
	
	@Override
	protected IGenericRepo<HuellaTemp, String> getRepo() {
		return repo;
	}

	@Override
	public Mono<HuellaTemp> ObtenerHuellaTempPorSerial(String serial) {
		return repo.findByPcSerial(serial);
	}
	
	@Override
	public Mono<List<HuellaTemp>> ObtenerUpdateTimePorSerial(String serial, String campo) {
		String ordenarPor = (campo == "update_time")? campo: "id";
		PageRequest condiciones = PageRequest.of(0, 1, Sort.Direction.DESC, ordenarPor);
		if (campo != "update_time")
			return repo.huellaUpdateTime(serial, campo, condiciones)
				.collectList();
		else
			return repo.huellaUpdateTime(serial, campo, "statusPlantilla", condiciones)
					.collectList();
	}

	@Override
	public Flux<ClienteHuellaUtil> ObtenerClienteHuella(int desde, int hasta) {
		Mono<Long> count = repoCliente.count();
		
		Pageable range = PageRequest.of(desde, hasta);
		
		
		return repoCliente.BuscarClientes(range)
				.flatMap(cliente -> {
					return count.flatMap(cou -> {
						ClienteHuellaUtil clienteTemporal = new ClienteHuellaUtil();
						clienteTemporal.setCount(cou);
						clienteTemporal.setDocumento(cliente.getIdentificacion());
						clienteTemporal.setNombre_completo(cliente.getNombres());
						clienteTemporal.setNombre_dedo("indice derecho");
						clienteTemporal.setHuella(cliente.getHuella());
						clienteTemporal.setImgHuella(cliente.getImgHuella());
						return Mono.just(clienteTemporal);
					});
					
				});
					

				
	}

	@Override
	public Mono<HttpushResponseUtil> ObtenerHuellaHttpush() {
		PageRequest condiciones = PageRequest.of(0, 1, Sort.Direction.DESC, "update_time");
		
		HttpushResponseUtil responseDefault = new HttpushResponseUtil("reintentar");
		
		// GLOSARIO
		// .switchIfEmpty -> devuelve un mono alternativo si el actual esta vacio
		// .all(predicado) -> Emite un solo valor booleano verdadero si todos los valores de esta secuencia coinciden con el Predicado.
		return repo.huellaHttpush(condiciones)
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
	public Mono<HashMap<String, Object>> HabilitarSensor(String token) {
		PageRequest condiciones = PageRequest.of(0, 1, Sort.Direction.DESC, "id");
		HashMap<String, Object> responseDefault = new HashMap<>();
		responseDefault.put("fecha_creacion", 0);
		responseDefault.put("opc", "reintentar");
		
		return repo.habilitarSensor(token, condiciones)
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
	public Mono<Void> eliminarTodo() {
		return repo.deleteAll();
	}



}
