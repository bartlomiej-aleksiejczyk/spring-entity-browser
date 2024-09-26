package com.example.demo;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Query;

@Controller
public class EntityBrowserController {
    @Autowired
    Database database;

    private static final List<String> ENTITY_LIST = List.of("Customer");

    @GetMapping("/")
    public String entityBrowserHome(Model model) {
        model.addAttribute("entities", ENTITY_LIST);
        return "entity-browser/home";
    }

    @GetMapping("/view")
    public String viewEntity(@RequestParam String entityName, Model model) {
        try {
            Class<?> entityClass = Class.forName("com.example.demo." + entityName);

            Query<?> query = DB.find(entityClass);
            List<?> results = query.findList();

            List<Map<String, Object>> mappedResults = new ArrayList<>();
            for (Object entity : results) {
                mappedResults.add(transformEntityToMap(entity));
            }

            model.addAttribute("entityName", entityName);
            model.addAttribute("results", mappedResults);

            return "entity-browser/view";
        } catch (ClassNotFoundException e) {
            model.addAttribute("error", "Entity not found: " + entityName);
            return "entity-browser/error";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred: " + e.getMessage());
            return "entity-browser/error";
        }
    }

    private Map<String, Object> transformEntityToMap(Object entity) throws IllegalAccessException {
        Map<String, Object> fieldMap = new HashMap<>();
        Class<?> currentClass = entity.getClass();

        while (currentClass != null && currentClass != Object.class) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();

                if (fieldName.startsWith("_")) {
                    continue;
                }

                fieldMap.put(fieldName, field.get(entity));
            }
            currentClass = currentClass.getSuperclass();
        }
        return fieldMap;
    }

    @GetMapping("/add")
    public String addEntityForm(@RequestParam String entityName, Model model) {
        try {
            // Load the entity class dynamically
            Class<?> entityClass = getEntityClass(entityName);

            // Get constructor parameters
            List<Map<String, String>> paramList = getConstructorParameters(entityClass);

            model.addAttribute("entityName", entityName);
            model.addAttribute("fields", paramList);
            return "entity-browser/add";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading form: " + e.getMessage());
            return "entity-browser/error";
        }
    }

    @PostMapping("/add")
    public String handleAddForm(@RequestParam String entityName, @RequestParam Map<String, String> formData,
            Model model) {
        try {
            Class<?> entityClass = getEntityClass(entityName);

            Object entityInstance = createEntityInstance(entityClass, formData);

            DB.save(entityInstance);

            model.addAttribute("message", "Entity created successfully!");
            return "entity-browser/success";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create entity: " + e.getMessage());
            return "entity-browser/error";
        }
    }

    private Object createEntityInstance(Class<?> entityClass, Map<String, String> formData) throws Exception {
        Constructor<?> constructor = entityClass.getDeclaredConstructors()[1];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Parameter[] parameters = constructor.getParameters();

        Object[] constructorArgs = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Parameter parameter = parameters[i];
            String paramName = parameter.getName();
            String paramValue = formData.get(paramName);

            if (paramValue != null) {
                constructorArgs[i] = convertStringToFieldType(parameterTypes[i], paramValue);
            } else {
                throw new IllegalArgumentException("Missing value for parameter: " + paramName);
            }
        }

        for (int i = 0; i < constructorArgs.length; i++) {
            if (!parameterTypes[i].isInstance(constructorArgs[i])) {
                throw new IllegalArgumentException(
                        "Argument type mismatch: Expected " + parameterTypes[i].getSimpleName()
                                + " but got " + constructorArgs[i].getClass().getSimpleName() + " for parameter "
                                + parameters[i].getName());
            }
        }

        return constructor.newInstance(constructorArgs);
    }

    private Class<?> getEntityClass(String entityName) throws ClassNotFoundException {
        return Class.forName("com.example.demo." + entityName);
    }

    private List<Map<String, String>> getConstructorParameters(Class<?> entityClass) {
        Constructor<?> constructor = entityClass.getDeclaredConstructors()[1];
        Parameter[] parameters = constructor.getParameters();

        List<Map<String, String>> paramList = new ArrayList<>();
        for (Parameter parameter : parameters) {
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("fieldName", parameter.getName());
            paramMap.put("fieldType", parameter.getType().getSimpleName());
            System.out.println(paramMap);

            paramList.add(paramMap);
        }
        return paramList;
    }

    private Object convertStringToFieldType(Class<?> fieldType, String value) {
        try {
            if (fieldType == int.class || fieldType == Integer.class) {
                return Integer.parseInt(value);
            } else if (fieldType == double.class || fieldType == Double.class) {
                return Double.parseDouble(value);
            } else if (fieldType == long.class || fieldType == Long.class) {
                return Long.parseLong(value);
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                return Boolean.parseBoolean(value);
            } else {
                return value;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid format for value: " + value + " expected type " + fieldType.getSimpleName());
        }
    }

}