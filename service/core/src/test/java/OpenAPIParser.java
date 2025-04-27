import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_ENCODING;

import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.utils.FileUtils;
import cloud.xcan.angus.spec.utils.GzipUtils;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.core.util.Yaml31;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.converter.SwaggerConverter;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import lombok.SneakyThrows;

public class OpenAPIParser {

  @SneakyThrows
  public static void main(String[] args) {
    //parseSwagger2x();
    parseSwagger3x();
    //convertSwagger2ToSwagger3();
    //gzipCompress();
  }

  public static void parseSwagger2x() throws IOException {
    SwaggerParser parser = new SwaggerParser();
    Swagger swagger = parser.parse(FileUtils.readFileToString(new File(
            "/Volumes/workspace/workspace_xcan/xcan-source/xcan-apps/xcan-angustester/docs/thirdapis/swagger-openapiv2-petstore.yaml"),
        DEFAULT_ENCODING));

    // Serialization and Deserialization - Core
    if (swagger != null) {
      System.out.println(Json31.pretty(
          swagger)); // deserialization (Json.mapper().readValue(specAsJsonString, OpenAPI.class)) and serialization (Yaml.pretty(openAPI)).
      System.out.println(Yaml31.pretty().writeValueAsString(swagger));
    }
  }

  public static void parseSwagger3x() throws Exception {
    // Parse a swagger description from the petstore and get the result
    // SwaggerParseResult result = new OpenAPIParser().readLocation("https://petstore3.swagger.io/api/v3/openapi.json", null, null);

    // Parser uses options as a way to customize the behavior while parsing:
    ParseOptions parseOptions = new ParseOptions();
    // - When remote or relative references are found in the parsed document, parser will attempt to:
    // 1. resolve the reference in the remote or relative location
    // 2. parse the resolved reference
    // 3. add the resolved "component" (e.g. parameter, schema, response, etc.) to the resolved `OpenAPI` POJO components section
    // 4. replace the remote/relative reference with a local reference,  e.g. : `#/components/schemas/NameOfRemoteSchema`.
    parseOptions.setResolve(true);
    // you might need to have all local references removed replacing the reference with the content of the referenced element.
    parseOptions.setResolveFully(true);
    // In some scenarios, you might need to have all schemas defined inline (e.g. a response schema) moved to the `components/schemas` section and replaced with a reference to the newly added schema within `components/schemas`
    // parseOptions.setFlatten(true); // In V3

    // parseOptions.setResolveRequestBody(true);

    // From a file
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    URL url = classLoader.getResource("openapi3_0.yaml");
    SwaggerParseResult result = new io.swagger.parser.OpenAPIParser().readLocation(url.getPath(),
        null, parseOptions);

    // The parsed POJO
    OpenAPI openAPI = result.getOpenAPI();

    if (result.getMessages() != null) {
      result.getMessages().forEach(System.err::println); // validation errors and warnings
    }

    // Serialization and Deserialization - Core
    if (openAPI != null) {
      // Up to versions < 2.2.x the available helper classes wrapping customized ObjectMapper were:
      //  - Json
      //  - Yaml
      System.out.println(io.swagger.util.Json.pretty(
          openAPI)); // deserialization (Json.mapper().readValue(specAsJsonString, OpenAPI.class)) and serialization (Yaml.pretty(openAPI)).
      System.out.println(io.swagger.util.Yaml.pretty().writeValueAsString(openAPI));

      // Versions >= 2.2.x introduce the corresponding 3.1 helper classes:
      //  - Json31
      //  - Yaml31
      System.out.println(Json31.pretty(
          openAPI)); // deserialization (Yaml31.mapper().readValue(specAsYamlString, OpenAPI.class)) and serialization (Json31.pretty(openAPI))
      System.out.println(Yaml31.pretty(openAPI));
    }
  }

  public static void convertSwagger2ToSwagger3() throws Exception {
    String swaggerAsString = FileUtils.readFileToString(new File(
            "/Volumes/workspace/workspace_xcan/xcan-source/xcan-apps/xcan-angustester/docs/thirdapis/swagger-openapiv2-angustester.json"),
        DEFAULT_ENCODING);
    SwaggerConverter converter = new SwaggerConverter();
    SwaggerParseResult result = converter.readContents(swaggerAsString, null, null);

    // The parsed POJO
    OpenAPI openAPI = result.getOpenAPI();

    // Serialization and Deserialization - Core
    if (openAPI != null) {
      // Up to versions < 2.2.x the available helper classes wrapping customized ObjectMapper were:
      //  - Json
      //  - Yaml
      System.out.println(io.swagger.util.Json.pretty(
          openAPI)); // deserialization (Json.mapper().readValue(specAsJsonString, OpenAPI.class)) and serialization (Yaml.pretty(openAPI)).
      System.out.println(io.swagger.util.Yaml.pretty().writeValueAsString(openAPI));

      // Versions >= 2.2.x introduce the corresponding 3.1 helper classes:
      //  - Json31
      //  - Yaml31
      System.out.println(Json31.pretty(
          openAPI)); // deserialization (Yaml31.mapper().readValue(specAsYamlString, OpenAPI.class)) and serialization (Json31.pretty(openAPI))
      System.out.println(Yaml31.pretty(openAPI));
    }
  }

  @SneakyThrows
  private static void gzipCompress() {
    // 20x compression
    //  Src size (KB):1490
    //  Res size (KB):55
    String doc = FileUtils.readFileToString(new File(
            "/Volumes/workspace/workspace_xcan/xcan-source/xcan-apps/xcan-angustester/docs/thirdapis/swagger-openapiv2-angustester.json"),
        DEFAULT_ENCODING);
    String result = GzipUtils.compress(doc);
    System.out.println("Src size (KB):" + doc.length() / 1024);
    System.out.println("Res size (KB):" + result.length() / 1024);
    Assert.assertTrue(doc.length() == GzipUtils.decompress(result).length(), "Not equal");

  }

}
