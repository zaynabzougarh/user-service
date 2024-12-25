package com.example.serviceimpl;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import org.springframework.stereotype.Service;

import com.example.entities.Utilisateur;
import com.example.repository.UserRepository;
import com.example.service.KeycloakService;
import com.example.service.UserService;

@Service
public class UserServiceImpl implements UserService{
	private UserRepository userrepo;
	private final KeycloakService keycloakService;
	
	private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	
	public UserServiceImpl(UserRepository userRepository, KeycloakService keycloakService) {
		super();
        this.userrepo= userRepository;
        this.keycloakService = keycloakService;
    }

	@Override
	public List<Utilisateur> getAllUtilisateurs() {
		
		return userrepo.findAll();
	}
	@Override
	public Utilisateur saveUtilisateur(Utilisateur utilisateur) {
	    try {
	        // Sauvegarder l'utilisateur dans la base de données locale
	        Utilisateur savedUser = userrepo.save(utilisateur);
	        logger.info("Utilisateur enregistré dans la base de données locale: {}", utilisateur.getEmail());

	        // Synchronisation avec Keycloak
	        try {
	            logger.info("Vérification de l'existence de l'utilisateur dans Keycloak: {}", utilisateur.getEmail());
	            boolean existsInKeycloak = keycloakService.userExistsInKeycloak(utilisateur.getEmail());

	            if (existsInKeycloak) {
	                logger.info("L'utilisateur existe déjà dans Keycloak, mise à jour en cours: {}", utilisateur.getEmail());
	                keycloakService.updateUserInKeycloak(
	                    utilisateur.getEmail(),
	                    utilisateur.getNomComplet(),
	                    utilisateur.getMotdepasse(),
	                    utilisateur.getRole()
	                );
	                logger.info("Utilisateur mis à jour dans Keycloak: {}", utilisateur.getEmail());
	            } else {
	                logger.info("Utilisateur non trouvé dans Keycloak, création en cours: {}", utilisateur.getEmail());
	                keycloakService.createUserInKeycloak(
	                    utilisateur.getEmail(),
	                    utilisateur.getNomComplet(),
	                    utilisateur.getMotdepasse(),
	                    utilisateur.getRole()
	                );
	                logger.info("Utilisateur créé dans Keycloak: {}", utilisateur.getEmail());
	            }
	        } catch (Exception e) {
	            // Gestion des erreurs de synchronisation avec Keycloak
	            logger.error("Erreur lors de la synchronisation avec Keycloak pour l'utilisateur: {}", utilisateur.getEmail(), e);
	            throw new RuntimeException("Erreur lors de la synchronisation avec Keycloak", e);
	        }

	        return savedUser;

	    } catch (Exception e) {
	        logger.error("Erreur lors de l'enregistrement de l'utilisateur dans la base de données locale: {}", utilisateur.getEmail(), e);
	        throw new RuntimeException("Erreur lors de l'enregistrement de l'utilisateur", e);
	    }
	}


	@Override
	public Utilisateur getUtilisateurById(String id) {
		
		return userrepo.getById(id);
	}

	@Override
	public Long countUtilisateurs() {
		
		return userrepo.count();
	}

	@Override
	public void deleteUtilisateurById(String id) {
		userrepo.deleteById(id);
		
	}

	@Override
	public Utilisateur editUtilisateur(String id,Utilisateur utilisateur) {
		Utilisateur existinguser = userrepo.findById(id).orElseThrow(() -> new RuntimeException("user non trouvée avec l'ID : " + id));
		existinguser.setEmail(utilisateur.getEmail());
		existinguser.setNomComplet(utilisateur.getNomComplet());
		existinguser.setMotdepasse(utilisateur.getMotdepasse());
		existinguser.setNumTel(utilisateur.getNumTel());
		existinguser.setProfession(utilisateur.getProfession());
		existinguser.setRole(utilisateur.getRole());
		existinguser.setSignature(utilisateur.getSignature());
		existinguser.setLaboratoireId(utilisateur.getLaboratoireId());
		 // Si le logo a été modifié, mettre à jour le champ logo
	    if (utilisateur.getSignature() != null && !utilisateur.getSignature().isEmpty()) {
	    	existinguser.setSignature(utilisateur.getSignature());
	    }
		return userrepo.save(utilisateur);
	}
	

	@Override
	public List<Utilisateur> getActiveUtilisateurs() {
		
		return userrepo.findByIsActiveTrue();
	}
	@Override
	 public void desactiverUtilisateur(String email) {
	        Utilisateur user = userrepo.findById(email)
	            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID : " + email));
	        user.setIsActive(false);
	        userrepo.save(user);
	    }

}
