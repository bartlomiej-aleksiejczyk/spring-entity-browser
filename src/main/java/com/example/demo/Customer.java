package com.example.demo;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Customer extends BaseModel {

    public Customer(String name, String description, Integer age) {
        super();
        this.name = name;
        this.description = description;
        this.age = age;
    }

    private String name;
    private String description;
    private Integer age;

}
