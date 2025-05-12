package cloud.xcan.angus.extension.angustester.api.utils;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.extension.angustester.api.TesterExtensionMessage.SERVICE_DOC_PARSE_ERROR_T;
import static io.swagger.v3.oas.models.media.Schema.BIND_TO_VALUE;
import static io.swagger.v3.oas.models.media.Schema.BIND_TYPE_AND_TYPES;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.spec.thread.ThreadContext;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.AuthorizationValue;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import java.util.List;

public class OpenApiParser {

  /**
   * Detail OpenApi validation:
   * <p>
   * - <a>https://validator.swagger.io/#/</a> -
   * <a>https://github.com/swagger-api/validator-badge.git</>
   */
  public static OpenAPI checkAndParseOpenApi(String content, List<AuthorizationValue> auth,
      ParseOptions options) {
    // Parse OpenAPI
    // @See OpenAPI Schema#getType()
    System.setProperty(BIND_TYPE_AND_TYPES, "true");
    // @See OpenAPI Schema#getExtensions() ... Apis#getAuthentication() ...
    ThreadContext.add(BIND_TO_VALUE, true);
    SwaggerParseResult result = new OpenAPIParser().readContents(content, auth, options);

    // Note:: The resolution of result. getMessages () may also be successful if it is not empty, such as the exception information in converting Swagger 2 to OpenAPI 3
    String parseError = nonNull(result.getOpenAPI()) && isNotEmpty(result.getMessages())
        ? result.getMessages().get(0) : "Parsing OpenAPI error";
    assertTrue(nonNull(result.getOpenAPI()), SERVICE_DOC_PARSE_ERROR_T, parseError);

    assertTrue(isNotEmpty(result.getOpenAPI().getInfo()), "Info schema is required");
    // Fix:: Since 3.1 is not required
    // ProtocolAssert.assertTrue(isNotEmpty(result.getOpenAPI().getPaths()),
    //    "Paths schema is required");

    // Return the parsed POJO
    return result.getOpenAPI();
  }
}
