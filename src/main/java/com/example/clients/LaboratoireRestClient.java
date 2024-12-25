package com.example.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.model.Laboratoire;

@FeignClient(name="LABO-CONTACT-ADRESSE-SERVICE")

public interface LaboratoireRestClient {
@GetMapping("/api/CrudLabo/laboratoires/{id}")
Laboratoire findlabobyid(@PathVariable long id ); 

}
