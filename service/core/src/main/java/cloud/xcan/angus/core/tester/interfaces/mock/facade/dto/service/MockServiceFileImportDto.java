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
  @Schema(description = "Project ID, required when creating a service", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @NotNull
  @EnumPart(enumClass = ApiImportSource.class, allowableValues = {"OPENAPI", "POSTMAN", "ANGUS"})
  @Schema(description = "Import the source of the mock service", example = "OPENAPI", requiredMode = RequiredMode.REQUIRED)
  private ApiImportSource importType;

  @Schema(description = "Apis document file, the apis documents file and text must specify one, and only import file when both exist")
  private MultipartFile file;

  @Schema(description = "Apis document text, the apis documents file and text must specify one, and only import file when both exist")
  private String text;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Mock service name", example = "MockApis", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotNull
  @Schema(description = "Specify the mock service running node ID", requiredMode = RequiredMode.REQUIRED)
  private Long nodeId;

  @NotNull
  @Port
  @Schema(description = "Specify the port on which to run the service on the node", example = "80", requiredMode = RequiredMode.REQUIRED)
  private Integer servicePort;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "The cloud service edition is fixed at xxx.angusmock.cloud, and the privatized version allows users to freely enter the domain name", example = "service1.angusmock.cloud")
  private String serviceDomain;

  @Valid
  @Schema(description = "Mock request authentication configuration")
  private List<SimpleHttpAuth> apisSecurity;

  @Valid
  @Schema(description = "Mock request CORS configuration")
  private CorsData apisCors;

  @Valid
  //@NotNull
  @Schema(description = "Mock service configuration")
  private MockServiceSetting setting;
}
