package serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class JsonPOJODeserializer<T> implements Deserializer<T> {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private Class<T> tClass;

  public JsonPOJODeserializer(Class<T> targetType) {
  this.tClass = targetType;
  }

  @Override
  public T deserialize(String topic, byte[] bytes) {
    if (bytes == null) {
      return null;
    }

    T data;

    try {
      data = objectMapper.readValue(bytes,tClass);
    } catch (Exception e) {
      throw  new SerializationException(e);
    }

    return data;
  }

  @Override
  public void close() {

  }
}
