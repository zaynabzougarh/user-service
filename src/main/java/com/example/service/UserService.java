package com.example.service;

import java.util.List;

import com.example.entities.Utilisateur;

public interface UserService {
	 List<Utilisateur> getAllUtilisateurs();
	 Utilisateur saveUtilisateur(Utilisateur utilisateur);
	 Utilisateur getUtilisateurById(String id);
	    Long countUtilisateurs();
	    void deleteUtilisateurById(String id);
	    Utilisateur editUtilisateur(String id,Utilisateur utilisateur);
		List<Utilisateur> getActiveUtilisateurs();
		void desactiverUtilisateur(String email);
}
