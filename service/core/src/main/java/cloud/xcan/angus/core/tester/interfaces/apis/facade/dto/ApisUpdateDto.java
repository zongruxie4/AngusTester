package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_CODE_LENGTH_X5;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_DOC_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_OPENAPI_SUMMARY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_SIZE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X4;

import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.model.element.ActionOnEOF;
import cloud.xcan.angus.model.element.SharingMode;
import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.spec.http.HttpMethod;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import io.swagger.v3.oas.models.extension.ExtensionKey;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class ApisUpdateDto {

  @NotNull
  @Schema(description = "Apis id", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  //@Schema(example = "1") -> Instead by move
  //private Long serviceId;

  @Schema(example = "http")
  private ApisProtocol protocol;

  @Schema(example = "GET")
  private HttpMethod method;

  /**
   * Note:: Cannot include query parameters when saving.
   *
   * @see io.swagger.v3.oas.models.Paths#keySet()
   */
  @Length(max = MAX_URL_LENGTH_X4)
  @Schema(example = "/comm/api/v1/country/{id}")
  private String endpoint;

  /////////////////////////OpenAPI Document//////////////////////////
  private List<String> tags;

  @Length(max = MAX_OPENAPI_SUMMARY_LENGTH)
  private String summary;

  @Length(max = MAX_OPENAPI_DOC_DESC_LENGTH)
  private String description;

  private ExternalDocumentation externalDocs;

  @Length(max = MAX_CODE_LENGTH_X5)
  private String operationId;

  @Size(max = MAX_PARAM_SIZE)
  private List<Parameter> parameters;

  private RequestBody requestBody;

  private Map<String, ApiResponse> responses;

  private Boolean deprecated;

  private List<SecurityRequirement> security;

  private Server currentServer;

  /**
   * Include Extension:
   * <p>
   * - RequestSetting: Extension key in {@link Operation#getExtensions()} is
   * {@link ExtensionKey#REQUEST_SETTING_KEY}
   */
  @JsonAnySetter
  private Map<String, Object> extensions = new HashMap<>();
  /////////////////////////OpenAPI Document//////////////////////////

  private SecurityScheme authentication;

  @Valid
  @Size(max = MAX_PARAM_SIZE)
  private List<Assertion<HttpExtraction>> assertions;

  //Fix:: Security Bug
  //@Schema(description = "Whether to enable authorization control, default enabled")
  //private Boolean auth;

  @Schema(description = "Process actions when the dataset reaches the end of reading, default `RECYCLE`.")
  private ActionOnEOF datasetActionOnEOF;

  @Schema(description = "Dataset sharing mode when multi threads, default `ALL_THREAD`.")
  private SharingMode datasetSharingMode;

  @Schema(description = "Whether to enable functional testing, default enabled")
  private Boolean testFunc;

  @Schema(description = "Whether to enable performance testing, default enabled")
  private Boolean testPerf;

  @Schema(description = "Whether to enable stability testing, default enabled")
  private Boolean testStability;

  @Schema(example = "1")
  private Long ownerId;

  private ApiStatus status;

}
