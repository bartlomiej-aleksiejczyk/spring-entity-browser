package com.example.demo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.ebean.Database;

@Controller
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    Database database;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @GetMapping
    public List<Customer> getContent() {
        return database.find(Customer.class).findList();
    }

    // @PostMapping
    // public Map<String, Object> add(@RequestBody CustomerDTO customerDTO) {
    // Customer customer = new Customer(customerDTO.name(),
    // customerDTO.description(), 21);
    // database.save(customer);
    // return objectMapper.convertValue(customer, Map.class);

    // }

}
