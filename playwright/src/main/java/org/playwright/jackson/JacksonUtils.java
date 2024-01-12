package org.playwright.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.playwright.PlaywrightException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class JacksonUtils {

  /**
   * Returns ObjectMapper object.
   *
   * @return ObjectMapper object
   */
  public static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    // enable
    objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
    // disable
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    // register
    objectMapper.registerModule(new JavaTimeModule());

    return objectMapper;
  }

  /**
   * Parse data into Java list.
   *
   * @param body json data
   * @param <T>  class to convert data into
   * @return List of objects
   */
  public static <T> List<T> deserializeToList(String body, Class<T> clazz) {
    ObjectMapper objectMapper = getObjectMapper();
    List<T> result;

    try {
      result = objectMapper.readValue(body, new TypeReference<>() {
      });
    } catch (IOException e) {
      throw new PlaywrightException("Error reading data!", e);
    }

    return (clazz != null)
        ? result.stream().map(i -> objectMapper.convertValue(i, clazz)).collect(Collectors.toList())
        : result;
  }

  /**
   * Parse data into Java list.
   *
   * @param body json data or file path to data
   * @param <T>  class to convert data into
   * @return map of objects
   */
  public static <T> Map<String, T> deserializeToMap(String body, Class<T> clazz) {
    ObjectMapper objectMapper = getObjectMapper();
    Map<String, T> result;

    try {
      if (body.endsWith(".json") || body.endsWith(".txt")) {
        result = objectMapper.readValue(Paths.get(body).toFile(), new TypeReference<>() {
        });
      } else {
        result = objectMapper.readValue(body, new TypeReference<>() {
        });
      }
    } catch (IOException e) {
      throw new PlaywrightException("Error reading data!", e);
    }
    if (clazz != null) {
      result.replaceAll((k, v) -> objectMapper.convertValue(v, clazz));
    }
    return result;
  }

  /**
   * Parse data into Java object.
   *
   * @param body json data
   * @param <T>  class to convert data into
   * @return object
   */
  public static <T> T deserializeToObj(String body, Class<T> clazz) {
    ObjectMapper objectMapper = getObjectMapper();
    T result;

    try {
      result = objectMapper.readValue(body, new TypeReference<>() {
      });
    } catch (IOException e) {
      throw new PlaywrightException("Error reading data!", e);
    }
    if (clazz != null) {
      result = objectMapper.convertValue(result, clazz);
    }
    return result;
  }


  /**
   * Serializes java object to String representation.
   *
   * @param obj object to serialize
   * @return string representation of java object
   */
  public static String serializeToString(Object obj) {
    ObjectMapper objectMapper = getObjectMapper();

    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      throw new PlaywrightException("Error serializing java object!", e);
    }
  }
}
