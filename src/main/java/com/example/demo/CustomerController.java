package com.example.demo;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.ebean.Database;

@RestController
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    Database database;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @GetMapping
    public List<Customer> getContent() {
        return database.find(Customer.class).findList();
    }

    @PostMapping
    public Map<String, Object> add(@RequestBody CustomerDTO customerDTO) {
        Customer customer = new Customer(customerDTO.name());
        database.save(customer);
        return objectMapper.convertValue(customer, Map.class);

    }

    @GetMapping("/{id}")
    public Map<String, Object> getCustomerAsMap(@PathVariable Long id) {
        Customer customer = database.find(Customer.class, id);
        try {
            // Convert the customer to a map including BaseModel fields
            return EntityToMapConverter.toMap(customer);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error converting customer to map", e);
        }
    }
}
