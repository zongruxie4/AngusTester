package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.api.pojo.CorsData;
import cloud.xcan.angus.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.model.remoting.MockServiceSetting;
import cloud.xcan.angus.validator.EnumPart;
import cloud.xcan.angus.validator.Port;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockServiceFileImportDto {

  @NotNull
  @Schema(description = "Project identifier required for service import operation", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @NotNull
  @EnumPart(enumClass = ApiImportSource.class, allowableValues = {"OPENAPI", "POSTMAN", "ANGUS"})
  @Schema(description = "Import source type for mock service", example = "OPENAPI", requiredMode = RequiredMode.REQUIRED)
  private ApiImportSource importType;

  @Schema(type = "string", format = "binary", description = "API document file for import. Either file or text must be specified. If both exist, only file is imported.")
  private MultipartFile file;

  @Schema(description = "API document text for import. Either file or text must be specified. If both exist, only file is imported.")
  private String text;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Mock service name for identification and display", example = "MockApis", requiredMode = RequiredMode.REQUIRED)
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
