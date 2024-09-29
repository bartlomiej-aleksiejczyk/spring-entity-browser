package com.example.demo;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.pagecomponent.PageComponentFormCreator;
import com.example.demo.pagecomponent.PageComponentTableCreator;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Query;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class EntityBrowserController {

    @Autowired
    Database database;

    @Autowired
    PageComponentFormCreator formCreator;

    @Autowired
    PageComponentTableCreator tableCreator;

    private static final List<String> ENTITY_LIST = List.of("Customer", "CustomerOrder");

    @GetMapping("/")
    public String entityBrowserHome(Model model) {
        model.addAttribute("entities", ENTITY_LIST);
        return "entity-browser/home";
    }

    @GetMapping("/view/{entityName}")
    public String viewEntity(
            @PathVariable String entityName,
            Model model, HttpServletRequest request) {

        // Query parameters are parsed manually since they are dynamic
        String pageParam = request.getParameter(entityName + "_page");
        String pageSizeParam = request.getParameter(entityName + "_pageSize");
        String orderParam = request.getParameter(entityName + "_order");

        int page = pageParam != null ? Integer.parseInt(pageParam) : 1;
        int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;
        String order = orderParam != null ? orderParam : null;

        try {

            Class<?> entityClass = Class.forName("com.example.demo." + entityName);
            String tableName = entityName;
            Query<?> query = DB.find(entityClass);

            boolean reverseOrder = false;

            if (order != null && !order.isEmpty()) {
                if (order.endsWith("_desc")) {
                    reverseOrder = true;
                    order = order.substring(0, order.length() - 5);
                    query.orderBy(order + " desc");
                } else if (order.endsWith("_asc")) {
                    order = order.substring(0, order.length() - 4);
                    query.orderBy(order + " asc");
                } else {
                    query.orderBy(order);
                }
            }

            List<?> results = query.findList();

            List<Map<String, Object>> mappedResults = new ArrayList<>();
            for (Object entity : results) {
                mappedResults.add(transformEntityToMap(entity));
            }

            int totalPages = (int) Math.ceil((double) mappedResults.size() / pageSize);

            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, mappedResults.size());
            List<Map<String, Object>> paginatedResults = mappedResults.subList(startIndex, endIndex);

            String baseUrl = request.getRequestURL().toString();

            PageComponentTableCreator.TableConfig config = new PageComponentTableCreator.TableConfig(
                    entityName, true, true, true, pageSize, page, totalPages, baseUrl, order, reverseOrder);

            String tableHtml = tableCreator.createTableFragment(paginatedResults, entityName, tableName, config);

            model.addAttribute("entityName", entityName);
            model.addAttribute("tableHtml", tableHtml);
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
            Class<?> entityClass = getEntityClass(entityName);

            String formHtml = formCreator.createFormFragmentFromConstructor(entityClass);

            model.addAttribute("formHtml", formHtml);
            model.addAttribute("entityName", entityName);
            return "entity-browser/add";
        } catch (ClassNotFoundException e) {
            model.addAttribute("error", "Entity not found: " + entityName);
            return "entity-browser/error";
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