package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class CustomerOrder extends BaseModel {

    public CustomerOrder(String name, Integer amount) {
        super();
        this.name = name;
        this.amount = amount;
    }

    @ManyToOne
    private Customer customer;
    private String name;
    private Integer amount;

}
