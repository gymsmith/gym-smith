package com.todoteg.controller;

import java.net.URI;
import java.time.Duration;
/*import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;*/
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.todoteg.model.TemporaryFingerprint;
import com.todoteg.model.util.ClientFingerprintUtil;
import com.todoteg.model.util.HttpushResponseUtil;
import com.todoteg.model.util.TemporaryFingerprintEntry;
import com.todoteg.service.ITemporaryFingerprintService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
//import reactor.core.publisher.SynchronousSink;

@RestController
@RequestMapping("/huella_temp") // se establece la direccion url particular
public class TemporaryFingerprintController {

	@Autowired
	private ITemporaryFingerprintService service;
	
	//ResponseEntity: Clase que me permite extender funcionalidades sobre la peticion http, controlar el status code.
	@GetMapping
	public Mono<ResponseEntity<Flux<ClientFingerprintUtil>>> getAllClientsFingerprint(@RequestParam("desde") int desde, @RequestParam("hasta") int hasta){
		
		Flux<ClientFingerprintUtil> clients = service.getAllClientsFingerprint(desde, hasta);
		
		return Mono.just(ResponseEntity
				.ok() // indica el status code de respuesta
				.contentType(MediaType.APPLICATION_JSON)
				.body(clients)); // se envia informacion en el body de la respuesta
	}

	// Obtiene los Datos por cada Estado de la Huella 
	/*
	 * Consulta la fecha actualizacion en la bd cada que esta sea menor o igual a la que se envio por parametro;
	 * Esta fecha actualiza,cada que el sensor captura un nuevo estado; por esto debo verificar la fecha hasta que esta sea mayor a la registrada por url. 
	 * para final mente cuando se esta seguro que la fecha se acaba de registrar consultar el estado de la huella en la bd.*/
	@PostMapping("/httpush")
	public Mono<ResponseEntity<HttpushResponseUtil>> httpush(@RequestParam("timestamp") String timestamp, @RequestParam("token") String token) throws InterruptedException{
		
		Map<String, Long> initialValues = new HashMap<String, Long>();
		initialValues.put("current_date", (timestamp.equals("null")) ? 0: Long.parseLong(timestamp)); // fecha actual - se recibe por parametro en el momento de ejecucion
		initialValues.put("db_date", (long) 0);														  // fecha base de datos
		//Estado.put("tiempo_transcurrido", (long) 0);
		
		return service.getUpdateTimeOrCreationDateBySerial(token, "update_time")
				// repetir la solicitud hasta que los resultados no estén vacíos
				.expand(dateUpdate -> {
					if(dateUpdate.size() > 0) {
						if(dateUpdate.get(0).getStatusPlantilla() == "Muestras Restantes: 0") {
							return Mono.empty();
						}
						if(dateUpdate.get(0).getUpdate_time() != null) {
							
							initialValues.put("db_date", dateUpdate.get(0).getUpdate_time().getTime() / 1000); // actualizo la el mapa con la nueva fecha. 
							
						}
					}
					
					if(initialValues.get("db_date") <= initialValues.get("current_date")) {

						return service.getUpdateTimeOrCreationDateBySerial(token, "update_time");
					}
					
					
					return Mono.empty();
				})
				.take(Duration.ofSeconds(30))// emite los elementos durante el tiempo especificado
				.last()
				.flatMap(dateOfTheNewState -> service.getFingerprintNewState() // Obtiene el nuevo estado de la huella
												.map(newStateFingerprint -> ResponseEntity
														.ok()
														.contentType(MediaType.APPLICATION_JSON)
														.body(newStateFingerprint)));
	}
	
	// Permite que sea visible el recuadro de lectura de la huella, al detectar que se acaba de añadir el primer estado
	// en conclusion responde justo despues de la ejecucion del enpoint "/activate-sensor".
	@GetMapping("/habilitarSensor")
	public Mono<ResponseEntity<HashMap<String, Object>>> HabilitarSensor(@RequestParam("timestamp") String timestamp, @RequestParam("token") String token) throws InterruptedException{


		Map<String, Long> initialValues = new HashMap<String, Long>();
		initialValues.put("current_date", (timestamp.equals("null")) ? 0: Long.parseLong(timestamp));
		initialValues.put("db_date", (long) 0);
		//Estado.put("tiempo_transcurrido", (long) 0);
		

		return service.getUpdateTimeOrCreationDateBySerial(token,"fecha_creacion")
				.expand(creationDate -> {
					if (creationDate.size() > 0) {			   
						initialValues.put("db_date", creationDate.get(0).getFecha_creacion().getTime() / 1000);
						
					}
					if(initialValues.get("db_date") <= initialValues.get("current_date")) {
						
						//Estado.put("tiempo_transcurrido", Estado.get("tiempo_transcurrido")+1);

						return service.getUpdateTimeOrCreationDateBySerial(token,"fecha_creacion");
					}
					
					return Mono.empty();
				})
				.take(Duration.ofSeconds(20)).last().flatMap(e -> {
					return service.enableSensor(token)
							.map(response -> ResponseEntity
								.ok() // indica el status code de respuesta
								.contentType(MediaType.APPLICATION_JSON)
								.body(response)
							);
				});
	}
	
	// registra el primer estado de la huella - solo en este Metodo se añade la fecha de creacion
	@PostMapping("/ActivarSensor")
	public Mono<ResponseEntity<TemporaryFingerprint>> ActivarSensorAdd(@Valid @RequestParam("token") String token,@RequestParam("opc") String opc, final ServerHttpRequest req){

		TemporaryFingerprint FingerprintStateInicial = new TemporaryFingerprint();
		FingerprintStateInicial.setPc_serial(token);
		
		switch (opc) {
        case "lectura":
        	FingerprintStateInicial.setTexto("El sensor de huella dactilar esta activado");
        	FingerprintStateInicial.setOpc("leer");
            break;
        case "capturar":
        	FingerprintStateInicial.setTexto("El sensor de huella dactilar esta activado");
        	FingerprintStateInicial.setStatusPlantilla("Muestras Restantes: 4");
        	FingerprintStateInicial.setOpc("capturar");
            break;
		}
		 
		 Mono<TemporaryFingerprint> fingerprint = Mono.just(FingerprintStateInicial);
		 
		 return fingerprint
				 .flatMap(f -> {
					 return service.getTemporaryFingerprintBySerial(token)
							 .map(fDB -> {
								 f.setId(fDB.getId());
								 return f;
							 })
							 .defaultIfEmpty(f);
				 })
				 .flatMap(f -> {
					 if (f.getId() != null) {
						 return service.deleteById(f.getId())
								 .thenReturn(f);
					 }
					 return Mono.just(f);
				 })
				 .flatMap(service::register)
				 .map(newFingerprint -> ResponseEntity
									.created(URI.create(req.getURI().toString().concat("/").concat(newFingerprint.getId()))) // se obtiene url dinamica del recurso
									.contentType(MediaType.APPLICATION_JSON)
									.body(newFingerprint));
				 
	}
	
	
	// @Valid Para hacer cumplir las validaciones propuestas en el modelo
	
	@PostMapping
	public Mono<ResponseEntity<TemporaryFingerprint>> register(@Valid @RequestBody TemporaryFingerprintEntry fingerprint, final ServerHttpRequest req){
		System.out.println(fingerprint.getSerial());
		Mono<TemporaryFingerprint> fingerprintDB = service.getTemporaryFingerprintBySerial(fingerprint.getSerial()); // Recupera el documento segun su pc_serial

		Mono<TemporaryFingerprintEntry> fingerprintBody = Mono.just(fingerprint);
		
		return fingerprintDB.zipWith(fingerprintBody, (fDB, fBody) -> {
			//hBD.setFecha_creacion(LocalDateTime.now());
			fDB.setHuella(fBody.getHuella() != null ? fBody.getHuella(): fDB.getHuella());
			fDB.setImgHuella(fBody.getImgHuella() != null ? fBody.getImgHuella(): fDB.getImgHuella());
			fDB.setUpdate_time(new Date());
			fDB.setStatusPlantilla(fBody.getStatusPlantilla() != null ? fBody.getStatusPlantilla(): fDB.getStatusPlantilla());
			fDB.setTexto(fBody.getTexto() != null ? fBody.getTexto(): fDB.getTexto());
			
			return fDB;
		})
		.flatMap(service::register)
		.map(newFingerprint -> ResponseEntity
				.created(URI.create(req.getURI().toString().concat("/").concat(newFingerprint.getId()))) // se obtiene url dinamica del recurso
				.contentType(MediaType.APPLICATION_JSON)
				.body(newFingerprint))
		.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@PutMapping
	public Mono<ResponseEntity<TemporaryFingerprint>> modify(@Valid @RequestBody TemporaryFingerprintEntry h){
		
		Mono<TemporaryFingerprint> FingerprintDB = service.getTemporaryFingerprintBySerial(h.getSerial());
		Mono<TemporaryFingerprintEntry> FingerprintoBody = Mono.just(h);
		
		return FingerprintDB.zipWith(FingerprintoBody, (fDB, fBody) -> {
			fDB.setImgHuella(fBody.getImgHuella() != null ? fBody.getImgHuella(): fDB.getImgHuella());
			fDB.setUpdate_time(new Date());
			fDB.setStatusPlantilla(fBody.getStatusPlantilla() != null ? fBody.getStatusPlantilla(): fDB.getStatusPlantilla());
			fDB.setTexto(fBody.getTexto() != null ? fBody.getTexto(): fDB.getTexto());
			if(h.getOption().equals("verificar")) {
				fDB.setDocumento(fBody.getDocumento() != null ? fBody.getDocumento(): fDB.getDocumento());
				fDB.setNombre(fBody.getNombre() != null ? fBody.getNombre(): fDB.getNombre());
				fDB.setDedo(fBody.getDedo() != null ? fBody.getDedo(): fDB.getDedo());
			}else {
				fDB.setOpc("stop");
			}
			
			
			return fDB;
		})
		.flatMap(service::modify) // usuarioModificado -> service.modificar(usuarioModificado) devuelve Mono<Usuario>
		.map(modifiedFingerprint -> ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(modifiedFingerprint))
		.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping
	public Mono<ResponseEntity<Void>> deleteAllFingerprint(){
		return service.deleteAllFingerprint()
				.thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deleteFingerprintById(@PathVariable String id){
		return service.getById(id) // Mono<Usuario>
				.flatMap(fingerprintToDelete -> {
					return service.deleteById(fingerprintToDelete.getId())// devuelve Mono<Void>
							.thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)); // <Mono<ResponseEntity<Void>>
				})
				.defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}
}

