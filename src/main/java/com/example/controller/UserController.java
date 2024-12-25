package com.example.controller;

import java.io.FileNotFoundException;




import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.clients.LaboratoireRestClient;
import com.example.entities.Utilisateur;
import com.example.service.UserService;
import com.example.model.Laboratoire;


@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/CrudUser/Utilisateurs/")
public class UserController {

    private  final UserService userService;
  
    @Value("${uploads.signatures.directory}")
    private String uploadDir;
   ;
    private  final LaboratoireRestClient laborestclient;
    

    // Constructeur pour l'injection du service utilisateur
    public UserController(UserService userService,LaboratoireRestClient laborestclient
    		) {
        this.userService = userService;
        this.laborestclient=laborestclient;
        
      
    }

    @GetMapping("/active")
    public List<Utilisateur> getActiveUsers() {
        List<Utilisateur> utilisateurs = userService.getActiveUtilisateurs();
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        utilisateurs.forEach(user -> {
            // Ajouter le lien de signature si elle existe
            if (user.getSignature() != null) {
                user.setSignature(baseUrl + "/api/CrudUser/Utilisateurs/signature/" + user.getSignature());
            }
            
            // Récupérer et associer le laboratoire via Feign
            if (user.getLaboratoireId() != null) {
                try {
                    Laboratoire laboratoire = laborestclient.findlabobyid(user.getLaboratoireId());
                    user.setLaboratoire(laboratoire); // Associer le laboratoire trouvé
                } catch (Exception e) {
                    // Gérer les erreurs lors de la récupération du laboratoire
                    System.err.println("Erreur lors de la récupération du laboratoire pour l'utilisateur: " + user.getEmail());
                    e.printStackTrace();
                }
            }
        });

        return utilisateurs;
    }

 

    // Ajouter un utilisateur avec une option d'upload de signature
    @PostMapping("/add")
    public Utilisateur addUser(
            @RequestParam("email") String email,
            @RequestParam("nomComplet") String nomComplet,
            @RequestParam("profession") String profession,
            @RequestParam("numTel") String numTel,
            @RequestParam("role") String role,
            @RequestParam("motdepasse") String motdepasse,
            @RequestParam("laboratoireId") Long laboratoireId,
            @RequestParam(value = "signature", required = false) MultipartFile signature) throws IOException {
    	 // Obtenez un encodeur de mot de passe
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // Chiffrez le mot de passe
        String encodedPassword = passwordEncoder.encode(motdepasse);
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setNomComplet(nomComplet);
        utilisateur.setProfession(profession);
        utilisateur.setNumTel(numTel);
        utilisateur.setRole(role);
        utilisateur.setMotdepasse(encodedPassword);
        utilisateur.setLaboratoire(laborestclient.findlabobyid(laboratoireId));
        utilisateur.setLaboratoireId(laboratoireId);

        // Gérer l'upload de la signature
        if (signature != null && !signature.isEmpty()) {
            // Générer un nom unique pour le fichier
            String fileName = UUID.randomUUID() + "_" + signature.getOriginalFilename();

            // Créer le répertoire si nécessaire
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Sauvegarder le fichier dans le répertoire
            Path targetPath = uploadPath.resolve(fileName);
            Files.copy(signature.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Enregistrer le chemin du fichier dans l'utilisateur
            utilisateur.setSignature(fileName);
        }

        // Sauvegarde via le service
        return userService.saveUtilisateur(utilisateur);
    }

    // Récupérer une signature par son nom de fichier
    @GetMapping("/signature/{fileName}")
    public ResponseEntity<Resource> getSignature(@PathVariable String fileName) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            throw new FileNotFoundException("Fichier non trouvé : " + fileName);
        }

        // Détection automatique du type de contenu
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    
    @PutMapping("/{email}")
    public Utilisateur updateUtilisateur(@PathVariable String email,
    	
                                          @RequestParam(value = "nomComplet", required = false) String nomComplet,
                                          @RequestParam(value = "profession", required = false) String profession,
                                          @RequestParam(value = "numTel", required = false) String numTel,
                                          @RequestParam(value = "signature", required = false) MultipartFile signature,
                                          @RequestParam(value = "role", required = false) String role,
                                          @RequestParam(value = "motdepasse", required = false) String motdepasse,
                                          @RequestParam(value = "laboratoireId", required = false) Long laboratoireId) throws IOException {
        // Rechercher l'utilisateur existant
        Utilisateur existingUser = userService.getUtilisateurById(email);
       
        if (existingUser == null) {
            throw new RuntimeException("Utilisateur avec l'email " + email + " introuvable");
        }
      
        // Mettre à jour les champs de base
        if (nomComplet != null) {
            existingUser.setNomComplet(nomComplet);
        }
        if (profession != null) {
            existingUser.setProfession(profession);
        }
        if (numTel != null) {
            existingUser.setNumTel(numTel);
        }
        if (role != null) {
            existingUser.setRole(role);
        }
        if (motdepasse != null) {
            // Chiffrement du mot de passe avant mise à jour
           // String hashedPassword = BCrypt.hashpw(motdepasse, BCrypt.gensalt());
            existingUser.setMotdepasse(motdepasse);
        }
        if (laboratoireId != null) {
            existingUser.setLaboratoireId(laboratoireId);
        }
        existingUser.setLaboratoire(laborestclient.findlabobyid(laboratoireId));

        // Gestion de la signature
        if (signature != null && !signature.isEmpty()) {
            // Générer un nom unique pour le fichier
            String fileName = UUID.randomUUID() + "_" + signature.getOriginalFilename();
            
            // Construire le chemin complet du fichier
            Path uploadPath = Paths.get(uploadDir); // `uploadDir` doit être défini dans votre application
            
            // Créer le répertoire si nécessaire
            Files.createDirectories(uploadPath);
            
            // Copier le fichier dans le répertoire de stockage
            Files.copy(signature.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            
            // Mettre à jour la signature dans l'utilisateur
            existingUser.setSignature(fileName);
        }

        // Sauvegarder l'utilisateur mis à jour
        return userService.saveUtilisateur(existingUser);
    }
    
    @GetMapping("/{id}")
    public Utilisateur getUtilisateur(@PathVariable String id) {
        return userService.getUtilisateurById(id);
        
    }
    @PutMapping("/desactiver/{id}")
    public ResponseEntity<Void> desactiverUtilisateur(@PathVariable String id) {
    	userService.desactiverUtilisateur(id); // Appel à la méthode du service
        return ResponseEntity.noContent().build(); // Réponse 204 No Content
    }
   
     
}
