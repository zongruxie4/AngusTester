package cloud.xcan.sdf.extension.angustester.api.utils;

import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.sdf.extension.angustester.api.TesterExtensionMessage.SERVICE_DOC_FORMAT_ERROR;
import static cloud.xcan.sdf.extension.angustester.api.TesterExtensionMessage.SERVICE_DOC_PARSE_ERROR_T;
import static cloud.xcan.sdf.extension.angustester.api.TesterExtensionMessage.SERVICE_DOC_VERSION_NOT_SUPPORTED_T;
import static io.swagger.v3.oas.models.media.Schema.BIND_TO_VALUE;
import static io.swagger.v3.oas.models.media.Schema.BIND_TYPE_AND_TYPES;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.spec.thread.ThreadContext;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.Utils;
import io.swagger.Utils.VERSION;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.converter.SwaggerConverter;
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
    JsonNode jsonNode = Utils.readNode(content);
    assertTrue(nonNull(jsonNode), SERVICE_DOC_FORMAT_ERROR);
    VERSION version = Utils.getVersion(jsonNode);
    assertTrue(version.equals(VERSION.V20) || version.equals(VERSION.V30)
            || version.equals(VERSION.V31), SERVICE_DOC_VERSION_NOT_SUPPORTED_T,
        new Object[]{version, "2, 3.0, 3.1"});
    // Parse OpenAPI
    System.setProperty(BIND_TYPE_AND_TYPES, "true"); // @See OpenAPI Schema#getType()
    ThreadContext.add(BIND_TO_VALUE,
        true); // @See OpenAPI Schema#getExtensions() ... Apis#getAuthentication() ...
    SwaggerParseResult result;
    if (version.equals(VERSION.V20)) {
      // From a V2 Content
      result = new SwaggerConverter().readContents(content, auth, options);
    } else {
      // From a V3 Content
      result = new OpenAPIParser().readContents(content, auth, options);
    }

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
