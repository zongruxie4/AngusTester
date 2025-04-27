package cloud.xcan.angus.extension.angustester.plugin;


import static cloud.xcan.angus.extension.angustester.api.utils.OpenApiParser.checkAndParseOpenApi;

import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.extension.angustester.api.ApisParseProvider;
import cloud.xcan.angus.plugin.api.Extension;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.core.models.ParseOptions;


@Extension
public class OpenapiParseProvider implements ApisParseProvider {

  @Override
  public OpenAPI parse(String content) {
    // Check the content is valid OpenAPI documents
    ParseOptions parseOptions = new ParseOptions();
    parseOptions.setResolve(true);
    parseOptions.setInferSchemaType(false);
    return checkAndParseOpenApi(content, null, parseOptions);
  }

  @Override
  public ApiImportSource getSource() {
    return ApiImportSource.OPENAPI;
  }

}
