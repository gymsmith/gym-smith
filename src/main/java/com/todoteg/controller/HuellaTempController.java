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

import com.todoteg.model.ClienteHuellaUtil;
import com.todoteg.model.HttpushResponseUtil;
import com.todoteg.model.HuellaTemp;
import com.todoteg.model.HuellaTempEntrada;

import com.todoteg.service.IHuellaTempService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
//import reactor.core.publisher.SynchronousSink;

@RestController
@RequestMapping("/huella_temp") // se establece la direccion url particular
public class HuellaTempController {

	@Autowired
	private IHuellaTempService service;
	
	//ResponseEntity: Clase que me permite extender funcionalidades sobre la peticion http, controlar el status code.
	@GetMapping
	public Mono<ResponseEntity<Flux<ClienteHuellaUtil>>> listar(@RequestParam("desde") int desde, @RequestParam("hasta") int hasta){
		
		Flux<ClienteHuellaUtil> usuarios = service.ObtenerClienteHuella(desde, hasta);
		
		return Mono.just(ResponseEntity
				.ok() // indica el status code de respuesta
				.contentType(MediaType.APPLICATION_JSON)
				.body(usuarios)); // se envia informacion en el body de la respuesta
	}

	
	@PostMapping("/httpush")
	public Mono<ResponseEntity<HttpushResponseUtil>> httpush(@RequestParam("timestamp") String timestamp, @RequestParam("token") String token) throws InterruptedException{
		
		Map<String, Long> Estado = new HashMap<String, Long>();
		Estado.put("fecha_actual", (timestamp.equals("null")) ? 0: Long.parseLong(timestamp));
		Estado.put("fecha_bd", (long) 0);
		//Estado.put("tiempo_transcurrido", (long) 0);
		
		return service.ObtenerUpdateTimePorSerial(token, "update_time")
				.expand(fechaActualizacion -> {
					if(fechaActualizacion.size() > 0) {
						if(fechaActualizacion.get(0).getStatusPlantilla() != "Muestras Restantes: 0") {
							return Mono.empty();
						}
						if(fechaActualizacion.get(0).getUpdate_time() != null) {
							
							Estado.put("fecha_bd", fechaActualizacion.get(0).getUpdate_time().getTime() / 1000);
							
						}
					}
					
					if(Estado.get("fecha_bd") <= Estado.get("fecha_actual") && Estado.get("tiempo_transcurrido") <= 30) {

						return service.ObtenerUpdateTimePorSerial(token, "update_time");
					}
					
					
					return Mono.empty();
				})
				.take(Duration.ofSeconds(30))
				.last()
				.flatMap(fechaUltimoEstado -> service.ObtenerHuellaHttpush()
												.map(httpush -> ResponseEntity
														.ok()
														.contentType(MediaType.APPLICATION_JSON)
														.body(httpush)));
	}
	
	@GetMapping("/habilitarSensor")
	public Mono<ResponseEntity<HashMap<String, Object>>> HabilitarSensor(@RequestParam("timestamp") String timestamp, @RequestParam("token") String token) throws InterruptedException{


		Map<String, Long> Estado = new HashMap<String, Long>();
		Estado.put("fecha_actual", (timestamp.equals("null")) ? 0: Long.parseLong(timestamp));
		Estado.put("fecha_bd", (long) 0);
		//Estado.put("tiempo_transcurrido", (long) 0);
		

		return service.ObtenerUpdateTimePorSerial(token,"fecha_creacion")
				.expand(fechaCreacion -> {
					if (fechaCreacion.size() > 0) {			   
						Estado.put("fecha_bd", fechaCreacion.get(0).getFecha_creacion().getTime() / 1000);
						
					}
					if(Estado.get("fecha_bd") <= Estado.get("fecha_actual")) {
						
						//Estado.put("tiempo_transcurrido", Estado.get("tiempo_transcurrido")+1);

						return service.ObtenerUpdateTimePorSerial(token,"fecha_creacion");
					}
					
					return Mono.empty();
				})
				.take(Duration.ofSeconds(20)).last().flatMap(e -> {
					return service.HabilitarSensor(token)
							.map(response -> ResponseEntity
								.ok() // indica el status code de respuesta
								.contentType(MediaType.APPLICATION_JSON)
								.body(response)
							);
				});
	}
	
	@PostMapping("/ActivarSensor")
	public Mono<ResponseEntity<HuellaTemp>> ActivarSensorAdd(@Valid @RequestParam("token") String token,@RequestParam("opc") String opc, final ServerHttpRequest req){

		HuellaTemp nuevaHuella = new HuellaTemp();
		nuevaHuella.setPc_serial(token);
		
		switch (opc) {
        case "lectura":
        	nuevaHuella.setTexto("El sensor de huella dactilar esta activado");
			nuevaHuella.setOpc("leer");
            break;
        case "capturar":
        	nuevaHuella.setTexto("El sensor de huella dactilar esta activado");
			nuevaHuella.setStatusPlantilla("Muestras Restantes: 4");
			nuevaHuella.setOpc("capturar");
            break;
		}
		 
		 Mono<HuellaTemp> huella = Mono.just(nuevaHuella);
		 
		 return huella
				 .flatMap(H -> {
					 return service.ObtenerHuellaTempPorSerial(token)
							 .map(hBD -> {
								 H.setId(hBD.getId());
								 return H;
							 })
							 .defaultIfEmpty(H);
				 })
				 .flatMap(H -> {
					 if (H.getId() != null) {
						 return service.eliminar(H.getId())
								 .thenReturn(H);
					 }
					 return Mono.just(H);
				 })
				 .flatMap(service::registrar)
				 .map(newHuella -> ResponseEntity
									.created(URI.create(req.getURI().toString().concat("/").concat(newHuella.getId()))) // se obtiene url dinamica del recurso
									.contentType(MediaType.APPLICATION_JSON)
									.body(newHuella));
				 
	}
	
	
	// @Valid Para hacer cumplir las validaciones propuestas en el modelo
	
	@PostMapping
	public Mono<ResponseEntity<HuellaTemp>> registrar(@Valid @RequestBody HuellaTempEntrada h, final ServerHttpRequest req){
		System.out.println(h.getSerial());
		Mono<HuellaTemp> HuellaTemporalBD = service.ObtenerHuellaTempPorSerial(h.getSerial()); // Recupera el documento segun su pc_serial

		Mono<HuellaTempEntrada> HuellaTempBody = Mono.just(h);
		
		return HuellaTemporalBD.zipWith(HuellaTempBody, (hBD, hBody) -> {
			//hBD.setFecha_creacion(LocalDateTime.now());
			hBD.setHuella(hBody.getHuella() != null ? hBody.getHuella(): hBD.getHuella());
			hBD.setImgHuella(hBody.getImgHuella() != null ? hBody.getImgHuella(): hBD.getImgHuella());
			hBD.setUpdate_time(new Date());
			hBD.setStatusPlantilla(hBody.getStatusPlantilla() != null ? hBody.getStatusPlantilla(): hBD.getStatusPlantilla());
			hBD.setTexto(hBody.getTexto() != null ? hBody.getTexto(): hBD.getTexto());
			
			return hBD;
		})
		.flatMap(service::registrar)
		.map(newHuella -> ResponseEntity
				.created(URI.create(req.getURI().toString().concat("/").concat(newHuella.getId()))) // se obtiene url dinamica del recurso
				.contentType(MediaType.APPLICATION_JSON)
				.body(newHuella))
		.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@PutMapping
	public Mono<ResponseEntity<HuellaTemp>> modificar(@Valid @RequestBody HuellaTempEntrada h){
		
		Mono<HuellaTemp> UsuarioBD = service.ObtenerHuellaTempPorSerial(h.getSerial());
		Mono<HuellaTempEntrada> UsuarioBody = Mono.just(h);
		
		return UsuarioBD.zipWith(UsuarioBody, (uBD, uBody) -> {
			uBD.setImgHuella(uBody.getImgHuella() != null ? uBody.getImgHuella(): uBD.getImgHuella());
			uBD.setUpdate_time(new Date());
			uBD.setStatusPlantilla(uBody.getStatusPlantilla() != null ? uBody.getStatusPlantilla(): uBD.getStatusPlantilla());
			uBD.setTexto(uBody.getTexto() != null ? uBody.getTexto(): uBD.getTexto());
			if(h.getOption().equals("verificar")) {
				uBD.setDocumento(uBody.getDocumento() != null ? uBody.getDocumento(): uBD.getDocumento());
				uBD.setNombre(uBody.getNombre() != null ? uBody.getNombre(): uBD.getNombre());
				uBD.setDedo(uBody.getDedo() != null ? uBody.getDedo(): uBD.getDedo());
			}else {
				uBD.setOpc("stop");
			}
			
			
			return uBD;
		})
		.flatMap(service::modificar) // usuarioModificado -> service.modificar(usuarioModificado) devuelve Mono<Usuario>
		.map(uModificado -> ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(uModificado))
		.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping
	public Mono<ResponseEntity<Void>> eliminar(){
		return service.eliminarTodo()
				.thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> eliminar(@PathVariable String id){
		return service.listarPorId(id) // Mono<Usuario>
				.flatMap(HuellaTempAEliminar -> {
					return service.eliminar(HuellaTempAEliminar.getId())// devuelve Mono<Void>
							.thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)); // <Mono<ResponseEntity<Void>>
				})
				.defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}
}

