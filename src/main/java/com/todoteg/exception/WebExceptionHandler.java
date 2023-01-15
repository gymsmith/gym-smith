package com.todoteg.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.web.WebProperties.Resources;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

@Component // Hace saber a spring que se quiere que gestione la respuesta de los errores. 
@Order(-1) // Esto es equivalente a Ordered.HIGHEST_PRECEDENCE -> para que obedesca por sobre todas las cosas a lo que se configuro en esta clase, hace referencia al orden mas alto de lectura, ya que spring ya tiene un BEAN dispuesto para tal fin.
public class WebExceptionHandler extends AbstractErrorWebExceptionHandler {

	public WebExceptionHandler(ErrorAttributes errorAttributes, Resources resourcesProperties,
			ApplicationContext applicationContext, ServerCodecConfigurer configurer) {
		
		super(errorAttributes, resourcesProperties, applicationContext);
		
		this.setMessageWriters(configurer.getWriters()); // Configure un escritor de mensajes HTTP para serializar el cuerpo de la respuesta.
		
	}
	
	// Intercepta cualquier error que suceda durante la ejecucion de la aplicacion
	// sobrescribiendo este metodo podemos controlar las excepciones y darle forma a la respuesta dirigida al cliente
	// Este metodo retorna un nuevo operador reactivo RouterFunction este es un tipo de clase que permite hacer servicios funcionales. un enfoque distinto del manejo de respuestas de los servicios rest
	// este routerFunction va ha englobar la respuesta del servicio y recibe el error interceptado
	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
		// Interceptamos los errores de cualquier peticion y se pasan al metodo renderErrorResponse para su debido procesamiento
		
		// Interceptamos todas las rutas de peticion ya que en cualquiera puede pasar un error
		return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse ); // req -> renderErrorResponse(req)
		
	}
	
	private Mono<ServerResponse> renderErrorResponse(ServerRequest request){
		// se extrae la excepcion a partir del request recibido
		// getErrorAttributes : Recibe el request solicitado y lo que se quiere extraer. en este caso el bloque de errores
		Map<String, Object> errorGeneral = getErrorAttributes(request, ErrorAttributeOptions.defaults()); // devuelve un mapa con la informacion de los errores presentados en el request
		Map<String, Object> mapException = new HashMap<>(); // Mapa propio para personalizar los errores
		
		// var en java infiere el tipo de dato de la variable, no se puede cambiar el tipo de dato
		var httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
		Throwable error = getError(request);

		String statusCode = String.valueOf(errorGeneral.get("status")); // se extrae el status code implicado de la peticion http |errorGeneral.get("status")| esto devuelve un object por lo que se castea a un simple string
		switch(statusCode) { // se evalua el status code, y segun este se emite una respuesta
		case "500":
			if (error instanceof InterruptedException exception) {
	        	mapException.put("message", exception.getMessage());
	        	httpStatus = HttpStatus.UNAUTHORIZED;
	        	break;
	        }
			mapException.put("status", "500");
			mapException.put("exception", "ERROR INTERNO DEL SERVIDOR");
			httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
			break;
		case "400":
			mapException.put("status", "400");
			mapException.put("exception", "PETICION INCORRECTA, VERIFICA LOS DATOS");
			httpStatus = HttpStatus.BAD_REQUEST;
			break;
		case "406":
			mapException.put("status", "406");
			mapException.put("exception", "OTRO TIPO DE ERROR");
			httpStatus = HttpStatus.NOT_ACCEPTABLE;
			break;
		default:
			mapException.put("status", "418");
			mapException.put("exception", "ERROR POR DEFAULT");
			httpStatus = HttpStatus.I_AM_A_TEAPOT;
			break;
		}
		
        

		 //Cumplimos con lo que nos pide devolver este metodo
		return ServerResponse
				.status(httpStatus) //500
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(mapException));
	}

}
