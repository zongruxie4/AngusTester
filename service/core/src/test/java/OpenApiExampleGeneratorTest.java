import cloud.xcan.angus.core.tester.infra.util.OpenApiExampleGenerator;
import cloud.xcan.angus.spec.utils.JsonUtils;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OpenApiExampleGeneratorTest {

  private OpenAPI openAPI;

  @BeforeClass
  public void setUp() throws Exception {
    final String content = FileUtils.readFileToString(
        new File(getClass().getClassLoader().getResource("petstore.yaml").getFile()));
    final ParseOptions options = new ParseOptions();
    options.setResolve(true);
    options.setFlatten(true);
    final SwaggerParseResult result = new OpenAPIParser().readContents(content, null, options);
    this.openAPI = result.getOpenAPI();
  }

  @Test
  public void testExampleFromSchema() throws Exception {
    final Schema petSchema = openAPI.getComponents().getSchemas().get("Pet");
    final OpenApiExampleGenerator exampleGenerator = new OpenApiExampleGenerator();

    final Object exampleList = exampleGenerator.getSchemaExample(openAPI, petSchema);
    System.out.println(JsonUtils.toJson(exampleList));
  }

  @Test
  public void testExampleWithRecursiveNodes() throws Exception {
    final Schema categorySchema = openAPI.getComponents().getSchemas().get("Category");
    final OpenApiExampleGenerator exampleGenerator = new OpenApiExampleGenerator();

    final Object exampleList = exampleGenerator.getSchemaExample(openAPI, categorySchema);
    System.out.println(JsonUtils.toJson(exampleList));
  }
}
