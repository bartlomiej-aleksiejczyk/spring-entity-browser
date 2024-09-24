package com.example.demo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class EntityToMapConverter {

    // Convert an object and its super class fields into a key-value map
    public static Map<String, Object> toMap(Object obj) throws IllegalAccessException {
        Map<String, Object> result = new HashMap<>();
        Class<?> clazz = obj.getClass();

        // Iterate through the fields of the class and add them to the map
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true); // Bypass private/protected fields
                result.put(field.getName(), field.get(obj)); // Add field name and value to map
            }
            clazz = clazz.getSuperclass(); // Go to superclass (e.g., BaseModel)
        }

        return result;
    }
}
