package com.example.demo.pagecomponent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FormBaseConstructor {
    String name() default "defaultConstructor";
}