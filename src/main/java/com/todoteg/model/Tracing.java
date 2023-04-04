package com.todoteg.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tracing {
	
	@Id
	private String id;
	private Client usuario;
	private String peso;
	//private String medidaCuello;
	private String medidaPecho;
	private String medidaAbdomen;
	private String medidaBicepD;
	private String medidaBicepI;
	private String medidaMusloD;
	private String medidaMusloI;
	//private String medidaPantorrilla;
	//private String medidaHombro;
	private String medidaCintura;
	private String medidaCadera;
	private LocalDate fecha;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Client getUsuario() {
		return usuario;
	}
	public void setUsuario(Client usuario) {
		this.usuario = usuario;
	}
	public String getPeso() {
		return peso;
	}
	public void setPeso(String peso) {
		this.peso = peso;
	}


	public String getMedidaPecho() {
		return medidaPecho;
	}
	public void setMedidaPecho(String medidaPecho) {
		this.medidaPecho = medidaPecho;
	}
	public String getMedidaAbdomen() {
		return medidaAbdomen;
	}
	public void setMedidaAbdomen(String medidaAbdomen) {
		this.medidaAbdomen = medidaAbdomen;
	}
	public String getMedidaBicepD() {
		return medidaBicepD;
	}
	public void setMedidaBicepD(String medidaBicepD) {
		this.medidaBicepD = medidaBicepD;
	}
	public String getMedidaBicepI() {
		return medidaBicepI;
	}
	public void setMedidaBicepI(String medidaBicepI) {
		this.medidaBicepI = medidaBicepI;
	}
	public String getMedidaMusloD() {
		return medidaMusloD;
	}
	public void setMedidaMusloD(String medidaMusloD) {
		this.medidaMusloD = medidaMusloD;
	}
	public String getMedidaMusloI() {
		return medidaMusloI;
	}
	public void setMedidaMusloI(String medidaMusloI) {
		this.medidaMusloI = medidaMusloI;
	}

	public String getMedidaCintura() {
		return medidaCintura;
	}
	public void setMedidaCintura(String medidaCintura) {
		this.medidaCintura = medidaCintura;
	}
	public String getMedidaCadera() {
		return medidaCadera;
	}
	public void setMedidaCadera(String medidaCadera) {
		this.medidaCadera = medidaCadera;
	}
	public LocalDate getFecha() {
		return fecha;
	}
	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}
	
	
	
}
