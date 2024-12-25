package com.example.service;

import org.keycloak.admin.client.Keycloak;

import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.util.Collections;

@Service
public class KeycloakService {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakService.class);
    private final Keycloak keycloak;

    public KeycloakService() {
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl("http://localhost:9090") 
                .realm("Customer")                       
                .clientId("Customer")                    
                .username("aya")                 
                .password("aya")             
                .grantType("password")                  
                .build();
    }
    public void createUserInKeycloak(String email, String fullName, String password, String role) {
        logger.info("Starting to create user: {}", email);

        if (email == null || fullName == null || password == null || role == null) {
            logger.error("Invalid parameters: email={}, fullName={}, role={}", email, fullName, role);
            throw new IllegalArgumentException("Invalid parameters for user creation.");
        }

        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setFirstName(fullName.split(" ")[0]);
        user.setLastName(fullName.split(" ").length > 1 ? fullName.split(" ")[1] : "");
        user.setEnabled(true);
        user.setEmailVerified(true);

        try {
            logger.info("Creating user in Keycloak: {}", email);
            Response response = keycloak.realm("Customer").users().create(user);
            logger.info("Keycloak response status: {}", response.getStatus());

            if (response.getStatus() != 201) {
                String errorMessage = response.readEntity(String.class);
                logger.error("Keycloak response error: {}", errorMessage);
                throw new RuntimeException("Error creating user in Keycloak: " + errorMessage);
            }

            String userId = keycloak.realm("Customer").users().search(email, 0, 1).get(0).getId();
            logger.info("User created with ID: {}", userId);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);
            keycloak.realm("Customer").users().get(userId).resetPassword(credential);
            logger.info("Password reset for user ID: {}", userId);

            RoleRepresentation roleRepresentation = keycloak.realm("Customer").roles().get(role).toRepresentation();
            keycloak.realm("Customer").users().get(userId).roles().realmLevel().add(Collections.singletonList(roleRepresentation));
            logger.info("Role '{}' assigned to user ID: {}", role, userId);

        } catch (Exception e) {
            logger.error("Error occurred while creating user in Keycloak: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la synchronisation avec Keycloak", e);
        }
    }
    
    
    public boolean userExistsInKeycloak(String email) {
        try {
            // Recherche de l'utilisateur dans Keycloak par email
            logger.info("Checking if user exists in Keycloak with email: {}", email);
            return !keycloak.realm("Customer").users().search(email).isEmpty();
        } catch (Exception e) {
            logger.error("Error occurred while checking user existence in Keycloak: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la vérification de l'existence de l'utilisateur dans Keycloak", e);
        }
    }

    public void updateUserInKeycloak(String email, String fullName, String password, String role) {
        try {
            // Recherche de l'utilisateur dans Keycloak
            logger.info("Searching for user in Keycloak with email: {}", email);
            UserRepresentation user = keycloak.realm("Customer").users().search(email, 0, 1).get(0);
            String userId = user.getId();
            logger.info("User found in Keycloak with ID: {}", userId);

            // Mise à jour des informations utilisateur
            user.setFirstName(fullName.split(" ")[0]);
            user.setLastName(fullName.split(" ").length > 1 ? fullName.split(" ")[1] : "");
            user.setEmail(email);

            keycloak.realm("Customer").users().get(userId).update(user);
            logger.info("User information updated in Keycloak for user ID: {}", userId);

            // Mise à jour du mot de passe
            if (password != null) {
                CredentialRepresentation credential = new CredentialRepresentation();
                credential.setType(CredentialRepresentation.PASSWORD);
                credential.setValue(password);
                credential.setTemporary(false);
                keycloak.realm("Customer").users().get(userId).resetPassword(credential);
                logger.info("Password updated for user ID: {}", userId);
            }

            // Mise à jour du rôle
            if (role != null) {
                RoleRepresentation roleRepresentation = keycloak.realm("Customer").roles().get(role).toRepresentation();
                keycloak.realm("Customer").users().get(userId).roles().realmLevel().add(Collections.singletonList(roleRepresentation));
                logger.info("Role '{}' updated for user ID: {}", role, userId);
            }

        } catch (Exception e) {
            logger.error("Error occurred while updating user in Keycloak: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la mise à jour de l'utilisateur dans Keycloak", e);
        }
    }



}
