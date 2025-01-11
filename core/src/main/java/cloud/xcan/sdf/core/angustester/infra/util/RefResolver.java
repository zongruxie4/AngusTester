package cloud.xcan.sdf.core.angustester.infra.util;

import static cloud.xcan.sdf.core.angustester.domain.apis.converter.ApiResponseConverter.OPENAPI_MAPPER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class RefResolver {

  public static Set<String> findPropertyValues(String json, String propertyName)
      throws JsonProcessingException {
    JsonNode rootNode = OPENAPI_MAPPER.readTree(json);
    return findPropertyValues(rootNode, propertyName, new HashSet<>());
  }

  private static Set<String> findPropertyValues(JsonNode node, String propertyName,
      Set<String> values) {
    if (node == null || node.isNull()) {
      return null;
    }
    if (node.isObject()) {
      Iterator<String> fieldNames = node.fieldNames();
      while (fieldNames.hasNext()) {
        String name = fieldNames.next();
        JsonNode childNode = node.get(name);
        if (name.equals(propertyName) && childNode.isValueNode()) {
          values.add(childNode.asText());
        } else {
          findPropertyValues(childNode, propertyName, values);
        }
      }
    } else if (node.isArray()) {
      for (int i = 0; i < node.size(); i++) {
        JsonNode childNode = node.get(i);
        findPropertyValues(childNode, propertyName, values);
      }
    }
    return values;
  }
}
