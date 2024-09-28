package com.example.demo.pagecomponent;

import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class PageComponentFormCreator {

    private final TemplateEngine templateEngine;

    public PageComponentFormCreator(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String createFormFragment(Class<?> entityClass) {
        Constructor<?> constructor = entityClass.getDeclaredConstructors()[1];
        Parameter[] parameters = constructor.getParameters();

        List<FormField> formFields = new ArrayList<>();
        for (Parameter parameter : parameters) {
            formFields.add(new FormField(parameter.getName(), parameter.getType().getSimpleName()));
        }

        Context context = new Context();
        context.setVariable("entityName", entityClass.getSimpleName());
        context.setVariable("fields", formFields);

        return templateEngine.process("page-components/formFragmentTemplate", context);
    }

    public static class FormField {
        private final String name;
        private final String type;

        public FormField(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }
}