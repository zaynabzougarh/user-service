package com.example;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import com.example.entities.Utilisateur;
import com.example.repository.UserRepository;

@SpringBootApplication
@EnableFeignClients
public class UserServiceApplication  {
	  @Autowired
	    private UserRepository utilisateurRepository;

	   
	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}


