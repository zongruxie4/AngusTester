package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.cases;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH_X4;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X4;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_SIZE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X4;

import cloud.xcan.angus.model.element.ActionOnEOF;
import cloud.xcan.angus.model.element.SharingMode;
import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.model.element.http.CaseTestMethod;
import cloud.xcan.angus.spec.http.HttpMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class ApisCaseReplaceDto {

  @Schema(description = "Functional testing case identifier for replacement operation")
  private Long id;

  @NotNull
  @Schema(description = "API identifier for test case association", requiredMode = RequiredMode.REQUIRED)
  private Long apisId;

  @Schema(description = "Test case enablement flag, defaults to enabled")
  private Boolean enabled;

  @Schema(description = "API test case type classification, defaults to USER_DEFINED")
  private ApisCaseType type;

  @Schema(description = "API test case method specification, defaults to NULL")
  private CaseTestMethod testMethod;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH_X4)
  @Schema(description = "Test case name for identification and organization", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Length(max = MAX_DESC_LENGTH_X4)
  @Schema(description = "Test case description for detailed documentation")
  private String description;

  @Schema(description = "API protocol specification for communication type definition", example = "http")
  private ApisProtocol protocol;

  @Schema(description = "HTTP method for API request specification", example = "GET")
  private HttpMethod method;

  @Length(max = MAX_URL_LENGTH_X4)
  @Schema(description = "API endpoint path for resource identification", example = "/comm/api/v1/country/{id}")
  private String endpoint;

  @Schema(description = "Current API server configuration for connection and deployment")
  private Server currentServer;

  @Schema(description = "Current API request parameters for test case configuration")
  private List<Parameter> parameters;

  @Schema(description = "Current API request body for test case configuration")
  private RequestBody requestBody;

  @Schema(description = "Current API request authentication for test case configuration")
  private SecurityScheme authentication;

  @Valid
  @Size(max = MAX_PARAM_SIZE)
  @Schema(description = "Current API assertions for test case validation")
  private List<Assertion<HttpExtraction>> assertions;

  @Schema(description = "Dataset end-of-file action configuration, defaults to RECYCLE")
  private ActionOnEOF datasetActionOnEOF;

  @Schema(description = "Dataset sharing mode for multi-thread execution, defaults to ALL_THREAD")
  private SharingMode datasetSharingMode;

}
