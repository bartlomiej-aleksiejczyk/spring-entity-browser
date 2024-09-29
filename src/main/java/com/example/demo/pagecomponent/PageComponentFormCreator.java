package com.example.demo.pagecomponent;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PageComponentFormCreator {

    private final TemplateEngine templateEngine;
    private final Map<String, String> customWidgets;

    public PageComponentFormCreator(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
        this.customWidgets = new HashMap<>();
    }

    public void addCustomWidget(String type, String templatePath) {
        customWidgets.put(type, templatePath);
    }

    public String getCustomWidget(String type) {
        return customWidgets.get(type);
    }

    public void addCustomWidgetForField(String fieldName, String templatePath) {
        customWidgets.put(fieldName, templatePath);
    }

    public String createFormFragmentFromConstructor(Class<?> entityClass, String constructorName) {
        Constructor<?> constructor = getAnnotatedConstructorByName(entityClass, constructorName);
        Parameter[] parameters = constructor.getParameters();

        List<FormField> formFields = new ArrayList<>();
        for (Parameter parameter : parameters) {
            formFields.add(new FormField(parameter.getName(), parameter.getType().getSimpleName()));
        }

        return generateForm(entityClass, formFields, null);
    }

    public String createFormFragmentFromConstructor(Class<?> entityClass) {
        // TODO: Extract default value to const
        return createFormFragmentFromConstructor(entityClass, "defaultConstructor");
    }

    private Constructor<?> getAnnotatedConstructorByName(Class<?> clazz, String name) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        // Returns first found constructor
        // TODO: add some checking aginst same name constucroes
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(FormBaseConstructor.class)) {
                FormBaseConstructor annotation = constructor.getAnnotation(FormBaseConstructor.class);
                if (annotation.name().equals(name)) {
                    return constructor;
                }
            }
        }

        throw new IllegalArgumentException("No constructor found with the given annotation name: " + name);
    }

    public String createFormFromAllFields(Class<?> entityClass) {
        Field[] fields = entityClass.getDeclaredFields();

        List<FormField> formFields = new ArrayList<>();
        for (Field field : fields) {
            formFields.add(new FormField(field.getName(), field.getType().getSimpleName()));
        }

        return generateForm(entityClass, formFields, null);
    }

    public String createFormFragment(Class<?> entityClass, List<String> selectedFields,
            Map<String, Object> customData) {
        List<Field> fields = getAllFields(entityClass);

        List<FormField> formFields = new ArrayList<>();
        if (selectedFields == null || selectedFields.isEmpty() || selectedFields.contains("ALL_FIELDS")) {
            for (Field field : fields) {
                formFields.add(new FormField(field.getName(), field.getType().getSimpleName()));
            }
        } else {
            for (Field field : fields) {
                if (selectedFields.contains(field.getName())) {
                    formFields.add(new FormField(field.getName(), field.getType().getSimpleName()));
                }
            }
        }

        if (customData == null) {
            customData = new HashMap<>();
        }

        return generateForm(entityClass, formFields, customData);
    }

    private List<Field> getAllFields(Class<?> entityClass) {
        List<Field> fields = new ArrayList<>();
        Class<?> currentClass = entityClass;

        while (currentClass != null) {
            for (Field field : currentClass.getDeclaredFields()) {
                fields.add(field);
            }
            currentClass = currentClass.getSuperclass();
        }

        return fields;
    }

    private String generateForm(Class<?> entityClass, List<FormField> formFields, Map<String, Object> customData) {
        Context context = new Context();
        context.setVariable("entityName", entityClass.getSimpleName());
        context.setVariable("fields", formFields);
        context.setVariable("customWidgets", customWidgets);
        if (customData != null) {
            context.setVariable("customData", customData);
        }

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
