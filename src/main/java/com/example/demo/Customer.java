package com.example.demo;

import com.example.demo.pagecomponent.FormBaseConstructor;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Customer extends BaseModel {

    @FormBaseConstructor()
    public Customer(String name, String description, Integer age) {
        super();
        this.name = name;
        this.description = description;
        this.age = age;
    }

    @FormBaseConstructor(name = "test")
    public Customer(String name, Integer age) {
        super();
        this.name = name;
        this.age = age;
    }

    private String name;
    private String description;
    private Integer age;

}
