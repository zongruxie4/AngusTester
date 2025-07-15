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

  @Schema(description = "Functional testing case id")
  private Long id;

  @NotNull
  @Schema(description = "Case apis id", requiredMode = RequiredMode.REQUIRED)
  private Long apisId;

  @Schema(description = "Enable test cases? default is enabled")
  private Boolean enabled;

  @Schema(description = "Apis test cases type, default is USER_DEFINED")
  private ApisCaseType type;

  @Schema(description = "Apis test cases method, default is `NULL`")
  private CaseTestMethod testMethod;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH_X4)
  @Schema(description = "Case name", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Length(max = MAX_DESC_LENGTH_X4)
  @Schema(description = "Case description")
  private String description;

  @Schema(example = "http")
  private ApisProtocol protocol;

  @Schema(example = "GET")
  private HttpMethod method;

  @Length(max = MAX_URL_LENGTH_X4)
  @Schema(example = "/comm/api/v1/country/{id}")
  private String endpoint;

  @Schema(description = "Current apis server")
  private Server currentServer;

  @Schema(description = "Current apis request parameters")
  private List<Parameter> parameters;

  @Schema(description = "Current apis request body")
  private RequestBody requestBody;

  @Schema(description = "Current apis request authentication")
  private SecurityScheme authentication;

  @Valid
  @Size(max = MAX_PARAM_SIZE)
  @Schema(description = "Current apis assertions")
  private List<Assertion<HttpExtraction>> assertions;

  @Schema(description = "Process actions when the dataset reaches the end of reading, default `RECYCLE`")
  private ActionOnEOF datasetActionOnEOF;

  @Schema(description = "Dataset sharing mode when multi threads, default `ALL_THREAD`")
  private SharingMode datasetSharingMode;

}
