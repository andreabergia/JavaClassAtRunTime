package com.andreabergia.synthetic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassGeneratorTest {
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(FAIL_ON_EMPTY_BEANS, false);
        objectMapper.findAndRegisterModules();
    }

    @Test
    public void canGenerateClassFromEmptyMap() throws InvocationTargetException, InstantiationException, IllegalAccessException, JsonProcessingException {
        Map<String, Object> record = Map.of();
        var object = createClassInstance(record);
        var serialized = objectMapper.writeValueAsString(object);
        assertEquals("{}", serialized);
    }

    @Test
    public void canGenerateClassFromMapWithOneField() throws InvocationTargetException, InstantiationException, IllegalAccessException, JsonProcessingException {
        Map<String, Object> record = Map.of("name", "Andrea");
        var object = createClassInstance(record);
        var serialized = objectMapper.writeValueAsString(object);
        assertEquals("{\"name\":\"Andrea\"}", serialized);
    }

    @Test
    public void canGenerateClassFromMapWithTwoFields() throws InvocationTargetException, InstantiationException, IllegalAccessException, JsonProcessingException {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("age", 37);
        record.put("ageOfDeath", null);

        var object = createClassInstance(record);
        var serialized = objectMapper.writeValueAsString(object);
        assertEquals("{\"age\":37,\"ageOfDeath\":null}", serialized);
    }

    private static Object createClassInstance(Map<String, Object> record) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        var theClass = new ClassGenerator().generate(record);
        var constructors = theClass.getConstructors();
        assertEquals(1, constructors.length);
        return constructors[0].newInstance(record);
    }
}
