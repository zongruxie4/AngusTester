package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.api.pojo.CorsData;
import cloud.xcan.angus.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.angus.model.remoting.MockServiceSetting;
import cloud.xcan.angus.validator.Port;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockServiceServicesAssocDto {

  //  @NotNull
  //  @Schema(requiredMode = RequiredMode.REQUIRED, value = "Import mock service associated services or sample type, fixed value: PROJECT, SAMPLE")
  //  @EnumPart(enumClass = MockServiceSource.class, allowableValues = {"PROJECT"})
  //  private MockServiceSource source;

  @NotNull
  @Schema(description = "Service identifier for association operation", requiredMode = RequiredMode.REQUIRED)
  private Long serviceId;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Name of the existing project or service for association", example = "XCAN-ANGUSAPI.BOOT", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotNull
  @Schema(description = "Target node identifier for mock service deployment", requiredMode = RequiredMode.REQUIRED)
  private Long nodeId;

  @NotNull
  @Port
  @Schema(description = "Service port number for mock service deployment on target node", example = "80", requiredMode = RequiredMode.REQUIRED)
  private Integer servicePort;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Service domain configuration with cloud edition fixed format and private edition custom domain support", example = "service1.angusmock.cloud")
  private String serviceDomain;

  //@NotEmpty
  @Size(min = 1)
  @Schema(description = "API identifiers to associate with the service")
  private HashSet<Long> apiIds;

  @Valid
  @Schema(description = "Mock API request authentication configuration for security management")
  private List<SimpleHttpAuth> apisSecurity;

  @Valid
  @Schema(description = "Mock API request CORS configuration for cross-origin access control")
  private CorsData apisCors;

  @Valid
  //@NotNull
  @Schema(description = "Mock service runtime configuration for comprehensive deployment settings")
  private MockServiceSetting setting;

}
