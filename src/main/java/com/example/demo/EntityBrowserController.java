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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import org.springframework.ui.Model;

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
            model.addAttribute("iterateFields", new Mustache.Lambda() {
                @Override
                public void execute(Template.Fragment frag, Writer out) throws IOException {
                    Map<String, Object> context = (Map<String, Object>) frag.context(); // Get the current map (entity
                                                                                        // fields)
                    for (Map.Entry<String, Object> entry : context.entrySet()) {
                        out.write("<li>" + entry.getKey() + ": " + entry.getValue() + "</li>");
                    }
                }
            });

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
            // Load the entity class dynamically
            Class<?> entityClass = getEntityClass(entityName);

            // Create an instance using the first constructor with form data
            Object entityInstance = createEntityInstance(entityClass, formData);

            // Save the entity instance to the database
            DB.save(entityInstance);

            model.addAttribute("message", "Entity created successfully!");
            return "entity-browser/success";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to create entity: " + e.getMessage());
            e.printStackTrace(); // Added for diagnostics
            return "entity-browser/error";
        }
    }

    // Helper method to create an instance of the entity using constructor
    // parameters
    private Object createEntityInstance(Class<?> entityClass, Map<String, String> formData) throws Exception {
        Constructor<?> constructor = entityClass.getDeclaredConstructors()[1];
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Parameter[] parameters = constructor.getParameters();

        // Log constructor parameter types for diagnostics
        System.out.println("Constructor Parameters: ");
        for (int i = 0; i < parameterTypes.length; i++) {
            System.out.println(parameters[i].getName() + " : " + parameterTypes[i].getSimpleName());
        }

        // Create an array to hold constructor arguments
        Object[] constructorArgs = new Object[parameterTypes.length];

        // Fill constructor arguments from form data
        for (int i = 0; i < parameterTypes.length; i++) {
            Parameter parameter = parameters[i];
            String paramName = parameter.getName();
            String paramValue = formData.get(paramName);

            if (paramValue != null) {
                // Convert the value from form data to the appropriate type
                constructorArgs[i] = convertStringToFieldType(parameterTypes[i], paramValue);
            } else {
                throw new IllegalArgumentException("Missing value for parameter: " + paramName);
            }

            // Log the parameter conversion for diagnostics
            System.out.println("Converted " + paramName + " to " + constructorArgs[i] + " of type "
                    + parameterTypes[i].getSimpleName());
        }

        // Check that all constructorArgs match expected parameter types for diagnostics
        for (int i = 0; i < constructorArgs.length; i++) {
            if (!parameterTypes[i].isInstance(constructorArgs[i])) {
                throw new IllegalArgumentException(
                        "Argument type mismatch: Expected " + parameterTypes[i].getSimpleName()
                                + " but got " + constructorArgs[i].getClass().getSimpleName() + " for parameter "
                                + parameters[i].getName());
            }
        }

        // Instantiate the entity using the constructor and the converted arguments
        return constructor.newInstance(constructorArgs);
    }

    // Helper method to load entity class
    private Class<?> getEntityClass(String entityName) throws ClassNotFoundException {
        return Class.forName("com.example.demo." + entityName);
    }

    // Helper method to extract constructor parameters
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
                return value; // Default case: return string for objects or other types
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid format for value: " + value + " expected type " + fieldType.getSimpleName());
        }
    }

}