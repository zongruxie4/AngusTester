import cloud.xcan.sdf.core.angustester.infra.util.OAS3ExampleGenerator;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OAS3ExampleGeneratorTest {

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
    final OAS3ExampleGenerator exampleGenerator = new OAS3ExampleGenerator(openAPI);

    final List<Map<String, String>> exampleList = exampleGenerator.generate(null, null, petSchema);
    Map<String, String> exampleMap = exampleList.get(0);

    String example = exampleMap.get("example");
    Assert.assertNotNull(example);
    Assert.assertTrue(example.contains("\"name\" : \"doggie\""));
  }

  @Test
  public void testExampleWithRecursiveNodes() throws Exception {
    final Schema categorySchema = openAPI.getComponents().getSchemas().get("Category");
    final OAS3ExampleGenerator exampleGenerator = new OAS3ExampleGenerator(openAPI);

    final List<Map<String, String>> exampleList = exampleGenerator
        .generate(null, null, categorySchema);
    Assert.assertEquals(exampleList.size(), 1);
    final Map<String, String> example = exampleList.get(0);
    Assert.assertEquals(example.get("contentType"), "application/json");
    Assert.assertTrue(example.get("example").contains("\"name\" : \"Yinotheria\""));
  }
}
