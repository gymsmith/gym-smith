package com.todoteg;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Configuracion Adicional para Spring 2.6* 
// La raíz del problema es que ResourceProperties (que es el que estaba vinculado a spring.resources) se eliminó en Spring Boot 2.6 
@Configuration
public class ResourceWebPropertiesConfig {
	@Bean
	public WebProperties.Resources resources(){
		return new WebProperties.Resources();
	}
}
