package com.example.entities;

import com.example.model.Laboratoire;

import jakarta.persistence.*;

@Entity
public class Utilisateur {
    @Id
    private String email;
    private String nomComplet;
    private String profession;
    private String numTel;
    private String signature;
    private String role;
    private String motdepasse;
    private Boolean isActive=true;
   
    @Transient
    private Laboratoire laboratoire;
    private Long laboratoireId;

	public Utilisateur(String email, String nomComplet, String profession, String numTel, String signature, String role,
			String motdepasse, Boolean isActive, Laboratoire laboratoire) {
		super();
		this.email = email;
		this.nomComplet = nomComplet;
		this.profession = profession;
		this.numTel = numTel;
		this.signature = signature;
		this.role = role;
		this.motdepasse = motdepasse;
		this.isActive = isActive;
		this.laboratoire = laboratoire;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNomComplet() {
		return nomComplet;
	}

	public void setNomComplet(String nomComplet) {
		this.nomComplet = nomComplet;
	}

	public String getProfession() {
		return profession;
	}

	public void setProfession(String profession) {
		this.profession = profession;
	}

	public String getNumTel() {
		return numTel;
	}

	public void setNumTel(String numTel) {
		this.numTel = numTel;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getMotdepasse() {
		return motdepasse;
	}

	public void setMotdepasse(String motdepasse) {
		this.motdepasse = motdepasse;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Laboratoire getLaboratoire() {
		return laboratoire;
	}

	public void setLaboratoire(Laboratoire laboratoire) {
		this.laboratoire = laboratoire;
	}

	public Utilisateur() {
		super();
	}

	public Long getLaboratoireId() {
		return laboratoireId;
	}

	public void setLaboratoireId(Long laboratoireId) {
		this.laboratoireId = laboratoireId;
	}

	
    
	
    
}