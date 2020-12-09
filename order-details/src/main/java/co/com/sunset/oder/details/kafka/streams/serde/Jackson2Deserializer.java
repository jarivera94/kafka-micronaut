package co.com.sunset.oder.details.kafka.streams.serde;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

@Slf4j
public class Jackson2Deserializer<T> implements Deserializer<T> {

  private final ObjectMapper objectMapper;

  private Class<T> forType;

  public Jackson2Deserializer(ObjectMapper objectMapper, Class<T> forType) {
    this.objectMapper = objectMapper;
    this.forType = forType;
  }

  public Jackson2Deserializer(Class<T> forType) {
    this(new ObjectMapper(), forType);
  }

  @Override
  public T deserialize(String topic, byte[] bytes) {
    if (bytes == null) {
      return null;
    }

    try {
      return objectMapper.readValue(bytes, forType);
    } catch (Exception e) {
      log.error("Jackson2Deserializer::deserialize error deserializer object");
      String s = Base64.getEncoder().encodeToString(bytes);
      try {
        return objectMapper.readValue("{}", forType);
      } catch (JsonProcessingException ex) {
        throw new SerializationException(e);
      }
    }
  }

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {}

  @Override
  public void close() {}
}