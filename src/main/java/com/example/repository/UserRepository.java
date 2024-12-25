package com.example.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.example.entities.*;
@RepositoryRestResource
public interface UserRepository extends JpaRepository<Utilisateur,String> {
	List<Utilisateur> findByIsActiveTrue();


}
