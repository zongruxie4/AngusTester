package cloud.xcan.angus.extension.angustester.api;

import cloud.xcan.angus.plugin.api.ExtensionPoint;
import io.swagger.v3.oas.models.OpenAPI;

public interface ApisParser extends ExtensionPoint {

  OpenAPI parse(String content);

  ApiImportSource getSource();

}
