package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DOMAIN_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.api.pojo.CorsData;
import cloud.xcan.angus.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.angus.model.remoting.MockServiceSetting;
import cloud.xcan.angus.validator.Port;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class MockServiceReplaceDto {

  @Schema(description = "Mock service identifier for replacement operation", example = "1")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier required for service management", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @NotEmpty
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Mock service name for identification and display", example = "XCAN-ANGUSAPI.BOOT", requiredMode = RequiredMode.REQUIRED)
  private String name;

  //Modification of nodes is not allowed
  //@NotNull
  @Schema(description = "Target node identifier for mock service deployment with creation requirement", example = "1"/*, requiredMode = RequiredMode.REQUIRED*/)
  private Long nodeId;

  //Modification of nodes is not allowed
  //@NotNull
  @Port
  @Schema(description = "Service port number for mock service deployment with creation requirement", example = "80"/*, requiredMode = RequiredMode.REQUIRED*/)
  private Integer servicePort;

  @Length(max = MAX_DOMAIN_LENGTH)
  @Schema(description = "Service domain configuration with cloud edition fixed format and private edition custom domain support", example = "service1.angusmock.cloud")
  private String serviceDomain;

  @Valid
  @Schema(description = "Mock API request authentication configuration for security management")
  private List<SimpleHttpAuth> apisSecurity;

  @Valid
  @Schema(description = "Mock API request CORS configuration for cross-origin access control")
  private CorsData apisCors;

  @Valid
  @Schema(description = "Mock service runtime configuration for comprehensive deployment settings")
  private MockServiceSetting setting;
}
