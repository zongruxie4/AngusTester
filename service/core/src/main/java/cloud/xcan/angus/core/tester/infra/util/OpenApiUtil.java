package cloud.xcan.angus.core.tester.infra.util;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for OpenAPI specification processing and schema management.
 * <p>
 * Provides methods for handling OpenAPI schema references, property extraction, 
 * and schema resolution from OpenAPI documents.
 * <p>
 * Implements reference resolution, schema navigation, and property lookup 
 * functionality for OpenAPI 3.x specifications.
 * <p>
 * Supports complex schema structures including allOf, refs, and nested properties.
 */
public class OpenAPIUtil {

  /**
   * Extracts the simple reference name from an OpenAPI reference string.
   * <p>
   * Converts full reference paths like "#/components/schemas/User" to simple names like "User".
   * <p>
   * Handles component references and returns the last segment of the reference path.
   * <p>
   * @param ref the full reference string (e.g., "#/components/schemas/User")
   * @return the simple reference name (e.g., "User") or the original string if not a component reference
   */
  public static String getSimpleRef(String ref) {
    // Validate input parameter
    if (ref == null || ref.trim().isEmpty()) {
      return ref;
    }
    
    // Extract simple name from component references
    if (ref.startsWith("#/components/")) {
      ref = ref.substring(ref.lastIndexOf("/") + 1);
    }
    return ref;
  }

  /**
   * Retrieves a schema from OpenAPI components by its name.
   * <p>
   * Looks up schemas in the OpenAPI components section using the provided schema name.
   * <p>
   * Handles null checks for OpenAPI object, components, and schemas map.
   * <p>
   * @param name the name of the schema to retrieve
   * @param openAPI the OpenAPI specification object
   * @return the schema if found, null otherwise
   */
  public static Schema getSchemaFromName(String name, OpenAPI openAPI) {
    // Validate OpenAPI object
    if (openAPI == null) {
      return null;
    }
    
    // Validate components section exists
    if (openAPI.getComponents() == null) {
      return null;
    }
    
    // Get schemas map and validate it exists
    final Map<String, Schema> mapSchema = openAPI.getComponents().getSchemas();
    if (mapSchema == null || mapSchema.isEmpty()) {
      return null;
    }
    
    // Return schema by name
    return mapSchema.get(name);
  }

  /**
   * Resolves a schema reference to its actual schema definition.
   * <p>
   * Extracts the reference name and looks up the actual schema from OpenAPI components.
   * <p>
   * Handles null reference checks and delegates to getSchemaFromName for resolution.
   * <p>
   * @param refSchema the schema containing a reference
   * @param openAPI the OpenAPI specification object
   * @return the resolved schema if reference is valid, null otherwise
   */
  public static Schema getSchemaFromRefSchema(Schema refSchema, OpenAPI openAPI) {
    // Validate reference schema has a valid $ref
    if (StringUtils.isBlank(refSchema.get$ref())) {
      return null;
    }
    
    // Extract simple reference name and resolve schema
    final String name = getSimpleRef(refSchema.get$ref());
    return getSchemaFromName(name, openAPI);
  }

  /**
   * Searches for a property across multiple schemas in an allOf structure.
   * <p>
   * Iterates through all schemas in the allOf list, resolving references and 
   * checking for the specified property.
   * <p>
   * Supports nested schema references and complex allOf structures.
   * <p>
   * @param propertyName the name of the property to search for
   * @param schemas the list of schemas to search through
   * @param openAPI the OpenAPI specification object for reference resolution
   * @return the property schema if found, null otherwise
   */
  public static Schema getPropertyFromAllOfSchema(String propertyName, List<Schema> schemas,
      OpenAPI openAPI) {
    // Iterate through all schemas in the allOf structure
    for (Schema schema : schemas) {
      // Resolve schema reference if present
      if (StringUtils.isNotBlank(schema.get$ref())) {
        schema = getSchemaFromRefSchema(schema, openAPI);
      }
      
      // Skip if schema has no properties
      final Map<String, Schema> schemaProperties = schema.getProperties();
      if (schemaProperties == null) {
        continue;
      }
      
      // Return property if found in current schema
      if (schemaProperties.containsKey(propertyName)) {
        return schemaProperties.get(propertyName);
      }
    }
    return null;
  }

}
