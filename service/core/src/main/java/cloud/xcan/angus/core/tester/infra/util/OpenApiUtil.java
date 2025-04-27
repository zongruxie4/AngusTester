package cloud.xcan.angus.core.tester.infra.util;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class OpenApiUtil {

  public static String getSimpleRef(String ref) {
    if (ref.startsWith("#/components/")) {
      ref = ref.substring(ref.lastIndexOf("/") + 1);
    }
    return ref;
  }

  public static Schema getSchemaFromName(String name, OpenAPI openAPI) {
    if (openAPI == null) {
      return null;
    }
    if (openAPI.getComponents() == null) {
      return null;
    }
    final Map<String, Schema> mapSchema = openAPI.getComponents().getSchemas();
    if (mapSchema == null || mapSchema.isEmpty()) {
      return null;
    }
    return mapSchema.get(name);
  }

  public static Schema getSchemaFromRefSchema(Schema refSchema, OpenAPI openAPI) {
    if (StringUtils.isBlank(refSchema.get$ref())) {
      return null;
    }
    final String name = getSimpleRef(refSchema.get$ref());
    return getSchemaFromName(name, openAPI);
  }

  public static Schema getPropertyFromAllOfSchema(String propertyName, List<Schema> schemas,
      OpenAPI openAPI) {
    for (Schema schema : schemas) {
      if (StringUtils.isNotBlank(schema.get$ref())) {
        schema = getSchemaFromRefSchema(schema, openAPI);
      }
      final Map<String, Schema> schemaProperties = schema.getProperties();
      if (schemaProperties == null) {
        continue;
      }
      if (schemaProperties.containsKey(propertyName)) {
        return schemaProperties.get(propertyName);
      }
    }
    return null;
  }

}
