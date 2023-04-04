package com.todoteg.model;


import java.time.LocalDate;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.todoteg.model.util.LegalRepresentativeOfTheMinor;
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "clientes")
public class Client {

	@Id
	private String id;
	
	private String identificacion;
	
	@NotNull
	private String nombres;
	
	@Size(min = 10, max = 12)
	private String telefono;
	private LocalDate fechaNacimiento;
	//private LocalDateTime fechaCreacion = LocalDateTime.now();
	private String peso;
	private String altura;
	private String sexo;
	private byte[] huella;
	private byte[] imgHuella;
	private String firma;
	private Subscription subscripcion;
	private List<Tracing> listSeguimiento;
	private LegalRepresentativeOfTheMinor representanteLegal;
	private Boolean activo;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Subscription getSubscripcion() {
		return subscripcion;
	}
	public void setSubscripcion(Subscription subscripcion) {
		this.subscripcion = subscripcion;
	}
	public String getIdentificacion() {
		return identificacion;
	}
	public void setIdentificacion(String identificacion) {
		this.identificacion = identificacion;
	}
	public String getNombres() {
		return nombres;
	}
	public void setNombres(String nombres) {
		this.nombres = nombres;
	}
	public String getTelefono() {
		return telefono;
	}
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	public LocalDate getFechaNacimiento() {
		return fechaNacimiento;
	}
	public void setFechaNacimiento(LocalDate fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}
	public String getPeso() {
		return peso;
	}
	public void setPeso(String peso) {
		this.peso = peso;
	}
	public String getAltura() {
		return altura;
	}
	public void setAltura(String altura) {
		this.altura = altura;
	}
	public String getSexo() {
		return sexo;
	}
	public void setSexo(String sexo) {
		this.sexo = sexo;
	}
	public byte[] getHuella() {
		return huella;
	}
	public void setHuella(byte[] huella) {
		this.huella = huella;
	}
	public byte[] getImgHuella() {
		return imgHuella;
	}
	public void setImgHuella(byte[] huella) {
		this.imgHuella = huella;
	}
	public String getFirma() {
		return firma;
	}
	public void setFirma(String firma) {
		this.firma = firma;
	}

	public LegalRepresentativeOfTheMinor getRepresentanteLegal() {
		return representanteLegal;
	}
	public void setRepresentanteLegal(LegalRepresentativeOfTheMinor representanteLegal) {
		this.representanteLegal = representanteLegal;
	}

	/*
	 * public LocalDateTime getFechaCreacion() { return fechaCreacion;
	 }*/
	public List<Tracing> getListSeguimiento() {
		return listSeguimiento;
	}
	public void setListSeguimiento(List<Tracing> listSeguimiento) {
		this.listSeguimiento = listSeguimiento;
	}
	
	public Boolean getActivo() {
		return activo;
	}
	public void setActivo(Boolean activo) {
		this.activo = activo;
	}
	
		
}
