package cloud.xcan.sdf.extension.angustester.api;

import cloud.xcan.sdf.plugin.api.ExtensionPoint;
import io.swagger.v3.oas.models.OpenAPI;

public interface ApisParseProvider extends ExtensionPoint {

  OpenAPI parse(String content);

  ApiImportSource getSource();


}
