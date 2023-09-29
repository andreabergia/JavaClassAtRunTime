package com.andreabergia.synthetic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
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

        var theClass = new ClassGenerator().generate(record);
        var constructors = theClass.getConstructors();
        assertEquals(1, constructors.length);

        var object = constructors[0].newInstance();
        var serialized = objectMapper.writeValueAsString(object);
        assertEquals("{}", serialized);
    }
}
