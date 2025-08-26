package cloud.xcan.angus.core.tester.infra.util;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OpenApiExampleGenerator {

  public static final String REF_PREFIX_SCHEMAS = "#/components/schemas/";

  private final ScalarSampler scalars = new ScalarSampler();

  public Object getSchemaExample(OpenAPI spec, Schema<?> schema) {
    if (schema.get$ref() != null) {
      var referent = lookupSchemaRef(spec, schema);
      return getSchemaExample(spec, referent);
    } else if (schema.getExample() != null) {
      return onPreDefinedExample(schema);
    } else if (schema.getProperties() != null) {
      return onProperties(spec, schema.getProperties());
    } else if (schema instanceof ObjectSchema) {
      ObjectSchema objectSchema = (ObjectSchema) schema;
      return onProperties(spec, objectSchema.getProperties());
    } else if (schema instanceof ArraySchema) {
      ArraySchema arraySchema = (ArraySchema) schema;
      return onArraySchema(spec, arraySchema);
    } else if (schema instanceof ComposedSchema) {
      ComposedSchema composedSchema = (ComposedSchema) schema;
      return onComposedSchema(spec, composedSchema);
    } else {
      return scalars.sample(schema);
    }
  }

  private static Object onPreDefinedExample(Schema<?> schema) {
    if (schema instanceof DateTimeSchema) {
      return ScalarSampler.DATE_TIME_FORMATTER.format((OffsetDateTime) schema.getExample());
    } else if (schema instanceof DateSchema) {
      return ScalarSampler.DATE_FORMATTER.format(((Date) schema.getExample()).toInstant());
    } else {
      return schema.getExample();
    }
  }

  private List<Object> onArraySchema(OpenAPI spec, ArraySchema schema) {
    var items = schema.getItems();
    return List.of(getSchemaExample(spec, items));
  }

  private Object onComposedSchema(OpenAPI spec, ComposedSchema schema) {
    if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
      var allOf = schema.getAllOf();
      var combinedExampleProperties = new HashMap<String, Object>();
      allOf.forEach(aSchema -> {
        var exampleMap = getSchemaExample(spec, aSchema);
        if (exampleMap instanceof Map) {
          combinedExampleProperties.putAll(((Map<String, Object>) exampleMap));
        }
      });
      return combinedExampleProperties;

    } else if (schema.getOneOf() != null && !schema.getOneOf().isEmpty()) {
      var oneOf = schema.getOneOf();
      return getSchemaExample(spec, oneOf.get(0));

    } else if (schema.getAnyOf() != null && !schema.getAnyOf().isEmpty()) {
      var anyOf = schema.getAnyOf();
      return getSchemaExample(spec, anyOf.get(0));

    } else if (schema.getNot() != null) {
      return null;

    } else {
      throw new IllegalStateException("Invalid composed schema; missing or empty [allOf, oneOf, anyOf]");
    }
  }

  private Map<String, Object> onProperties(OpenAPI spec, Map<String, Schema> properties) {
    if (properties == null) {
      return Collections.emptyMap();
    } else {
      return properties.entrySet().stream().collect(Collectors.toMap(
          Map.Entry::getKey,
          entry -> getSchemaExample(spec, entry.getValue()))
      );
    }
  }

  private static Schema<?> lookupSchemaRef(OpenAPI spec, Schema<?> referrer) {
    if (referrer.get$ref().startsWith(REF_PREFIX_SCHEMAS)) {
      String schemaName = referrer.get$ref().substring(REF_PREFIX_SCHEMAS.length());
      Schema<?> schema = spec.getComponents().getSchemas().get(schemaName);
      if (schema == null) {
        throw new IllegalStateException("Referenced schema not found in components section: " + schemaName);
      }
      return schema;
    } else {
      throw new IllegalStateException("Unsupported schema $ref: " + referrer.get$ref());
    }
  }
}
